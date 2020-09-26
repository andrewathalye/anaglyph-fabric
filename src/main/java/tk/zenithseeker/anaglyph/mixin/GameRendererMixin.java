package tk.zenithseeker.anaglyph.mixin;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import tk.zenithseeker.anaglyph.AnaglyphMod;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	// Inject after call to Profiler.push("level")
	@Inject(at=@At(value="INVOKE", target="Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", shift=At.Shift.AFTER), method="render(FJZ)V") 
	public void renderBeforeInject(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
            RenderSystem.colorMask(true, true, false, false);
	    AnaglyphMod.pass=0; //Store pass (externally) because it is not feasible to modify renderWorld's arguments
            this.renderWorld(tickDelta, startTime, new MatrixStack());
            RenderSystem.colorMask(false, false, true, false);
	    AnaglyphMod.pass=1;
	}
	// A call to renderWorld is already present after this, so use it in order to improve compatibility

	//Reset colormask after the additional call to renderWorld
	@Inject(at=@At(value="INVOKE", target="Lnet/minecraft/client/render/GameRenderer;renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V", shift=At.Shift.AFTER), method="render(FJZ)V")
	public void renderAfterInject(CallbackInfo ci) {
        	RenderSystem.colorMask(true, true, true, false);
	}

	//Shadow renderWorld for above injection
	@Shadow
	public abstract void renderWorld(float tickDelta, long limitTime, MatrixStack matrix);

	//Inject after Camera.update in renderWorld
	@Inject(at=@At(value="INVOKE", target="Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V", shift=At.Shift.AFTER), method="renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V", locals=LocalCapture.CAPTURE_FAILHARD)
	public void renderWorldInject(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci, boolean bl, Camera camera, MatrixStack matrixStack, Matrix4f matrix4f) {
        	float angle=camera.getYaw();
        	angle-=360*((int)(angle/360));
        	if(angle>0)
            		angle=360-angle;
        	else
            		angle=Math.abs(angle);
        	if(AnaglyphMod.pass==0)
            		camera.setPos(Math.cos(Math.toRadians(angle))*0.07+camera.getPos().x,camera.getPos().y,Math.sin(Math.toRadians(angle))*-0.07+camera.getPos().z);
        	else
            		camera.setPos(Math.cos(Math.toRadians(angle))*-0.07+camera.getPos().x,camera.getPos().y,Math.sin(Math.toRadians(angle))*0.07+camera.getPos().z);
	}
	//NB: This uses an AccessWidener currently
	//TODO: Investigate why this does not work when using an Accessor or Duck and casting (it causes stack overflows and recurses through the two different setPos methods and throws IllegalAccessException for the Duck in production)
	//Possible ideas: setPos(DDD)V and setPos(Vec3d)V are both named similarly and return void - maybe this confuses @Invoker?
}
