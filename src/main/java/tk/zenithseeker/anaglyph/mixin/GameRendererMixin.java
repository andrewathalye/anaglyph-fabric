package tk.zenithseeker.anaglyph.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import tk.zenithseeker.anaglyph.AnaglyphMod;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	// Inject after call to getProfiler().push("level")
	@Inject(at=@At(value="INVOKE", target="Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", shift=At.Shift.AFTER), method="render(FJZ)V") 
	public void renderBeforeInject(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
            RenderSystem.colorMask(true, true, false, false);
	    AnaglyphMod.pass=0; //Store pass (externally) because it is not feasible to modify renderWorld's arguments
            this.renderLevel(tickDelta, startTime, new PoseStack());
            RenderSystem.colorMask(false, false, true, false);
	    AnaglyphMod.pass=1;
	}
	// A call to renderWorld is already present after this, so use it in order to improve compatibility

	//Reset colormask after the additional call to renderWorld
	@Inject(at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/GameRenderer;renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V", shift=At.Shift.AFTER), method="render(FJZ)V")
	public void renderAfterInject(CallbackInfo ci) {
        	RenderSystem.colorMask(true, true, true, false);
	}

	//Shadow renderLevel for above injection
	@Shadow
	public abstract void renderLevel(float tickDelta, long limitTime, PoseStack stack);

	//Inject after Camera.setup in renderLevel
	@Inject(at=@At(value="INVOKE", target="Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V", shift=At.Shift.AFTER), method="renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V", locals=LocalCapture.CAPTURE_FAILHARD)
	public void renderLevelInject(float tickDelta, long limitTime, PoseStack stack, CallbackInfo ci, boolean bl, Camera camera) {
        	float angle=camera.getXRot();
        	angle-=360*((int)(angle/360));
        	if(angle>0)
            		angle=360-angle;
        	else
            		angle=Math.abs(angle);
        	if(AnaglyphMod.pass==0)
            		camera.setPosition(Math.cos(Math.toRadians(angle))*0.07+camera.getPosition().x,camera.getPosition().y,Math.sin(Math.toRadians(angle))*-0.07+camera.getPosition().z);
        	else
            		camera.setPosition(Math.cos(Math.toRadians(angle))*-0.07+camera.getPosition().x,camera.getPosition().y,Math.sin(Math.toRadians(angle))*0.07+camera.getPosition().z);
	}
	//NB: This uses an AccessWidener currently
	//TODO: Investigate why this does not work when using an Accessor or Duck and casting (it causes stack overflows and recurses through the two different setPos methods and throws IllegalAccessException for the Duck in production)
	//Possible ideas: setPos(DDD)V and setPos(Vec3d)V are both named similarly and return void - maybe this confuses @Invoker?
}
