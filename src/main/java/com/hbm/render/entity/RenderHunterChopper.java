package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.mob.EntityHunterChopper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.model.ModelHunterChopper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(factory = "FACTORY")
public class RenderHunterChopper extends Render<EntityHunterChopper> {

	public static final IRenderFactory<EntityHunterChopper> FACTORY = (RenderManager man) -> {return new RenderHunterChopper(man);};
	
	public static final ResourceLocation chopper_rl = new ResourceLocation(Tags.MODID + ":textures/entity/chopper.png");
	
	//ProtoCopter mine;
	ModelHunterChopper mine2;
	
	protected RenderHunterChopper(RenderManager renderManager) {
		super(renderManager);
		//mine = new ProtoCopter();
		mine2 = new ModelHunterChopper();
	}
	
	@Override
	public void doRender(EntityHunterChopper rocket, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.translate(0.0625F * 0, 0.0625F * 32, 0.0625F * 0);
		GlStateManager.translate(0.0625F * 0, 0.0625F * 12, 0.0625F * 0);
		GlStateManager.scale(4F, 4F, 4F);
		GlStateManager.rotate(180, 1, 0, 0);

		GlStateManager.rotate(rocket.prevRotationYaw + (rocket.rotationYaw - rocket.prevRotationYaw) * partialTicks - 90.0F, 0, 1.0F, 0);
		GlStateManager.rotate(rocket.prevRotationPitch + (rocket.rotationPitch - rocket.prevRotationPitch) * partialTicks, 0, 0, 1.0F);
		
		bindTexture(getEntityTexture(rocket));
		
        //if(rocket instanceof EntityHunterChopper)
        //	mine2.setGunRotations((EntityHunterChopper)rocket, yaw, pitch);
		
		mine2.renderAll(0.0625F);
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityHunterChopper entity) {
		return chopper_rl;
	}

	
}
