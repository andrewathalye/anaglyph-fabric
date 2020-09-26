package tk.zenithseeker.anaglyph.mixin;

import net.minecraft.client.render.GameRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	//Inject after Profiler.push("level") in render
	/*
            RenderSystem.colorMask(true, true, false, false);
	    //Store render pass: 0
            this.renderWorld(tickDelta, startTime, new MatrixStack());
            RenderSystem.colorMask(false, false, true, false);
	    //Store render pass: 1
	*/
	//NB: Mixin does not yet have a great way to remove lines of code,
	//so make use of the additional call to renderWorld which is already
	//present

	//Reset colormask after the additional call to renderWorld
	/*
        	RenderSystem.colorMask(true, true, true, false);
	*/

	//Shadow renderWorld for above injection

	//Inject after Camera.update in renderWorld
	/*
        	float angle=lv.getYaw();
        	angle-=360*((int)(angle/360));
        	if(angle>0)
            		angle=360-angle;
        	else
            		angle=Math.abs(angle);
        	if(pass==0)
            		lv.setPos(Math.cos(Math.toRadians(angle))*0.07+lv.getPos().x,lv.getPos().y,Math.sin(Math.toRadians(angle))*-0.07+lv.getPos().z);
        	else
            		lv.setPos(Math.cos(Math.toRadians(angle))*-0.07+lv.getPos().x,lv.getPos().y,Math.sin(Math.toRadians(angle))*0.07+lv.getPos().z);
	*/ 
	//NB: Get local variable lv of type Camera
	//TODO: Use Accessor or Access Widener to make Camera.setPos accessible


	//Theoretically, that is all :)
}
