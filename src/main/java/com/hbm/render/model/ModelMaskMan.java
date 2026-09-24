package com.hbm.render.model;

import com.hbm.entity.mob.EntityMaskMan;
import com.hbm.main.ResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

public class ModelMaskMan extends ModelBase {

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);

		GlStateManager.pushMatrix();

		GlStateManager.rotate(180, 1, 0, 0);
		GlStateManager.translate(0, -1.5F, 0);
		GlStateManager.rotate(-90, 0, 1, 0);
		EntityMaskMan man = (EntityMaskMan)entity;
		
		//boolean target = entity.worldObj.getEntityByID(man.getDataWatcher().getWatchableObjectInt(man.dwTargetPlayer)) != null;

		//if(target)
		//	GL11.glRotated(-f3, 0, 1, 0);


        //float f7 = man.limbSwing - man.limbSwingAmount * (1.0F - f5);
        float f7 = f;
        //float f6 = (man.prevLimbSwingAmount + (man.limbSwingAmount - man.prevLimbSwingAmount) * f5) * 0.5F;
        float f6 = f1;

        double swing = Math.toDegrees(MathHelper.cos(f7 / 2F + (float)Math.PI) * 1.4F * f6);

		GlStateManager.rotate((float) (swing * -0.1), 1, 0, 0);
		ResourceManager.maskman.renderPart("Torso");

		GlStateManager.pushMatrix();
		GlStateManager.translate(-0.5F, 1.75F, -0.5F);
		GlStateManager.rotate((float) (swing), 0, 0, 1);
		ResourceManager.maskman.renderPart("LLeg");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(-0.5F, 1.75F, 0.5F);
		GlStateManager.rotate((float) (swing * -1), 0, 0, 1);
		ResourceManager.maskman.renderPart("RLeg");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(-0.5F, 3.75F, -1.5F);
		GlStateManager.rotate((float) (swing * 0.25), 0, 0, 1);
		ResourceManager.maskman.renderPart("LArm");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(-0.5F, 3.75F, 1.5F);
		GlStateManager.rotate((float) (swing * -0.25), 0, 0, 1);
		ResourceManager.maskman.renderPart("RArm");
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.5F, 4F, 0);
		GlStateManager.rotate((float) (-f3), 0, 1, 0);

		if(man.getHealth() >= man.getMaxHealth() / 2) {
			ResourceManager.maskman.renderPart("Head");
		} else {
			ResourceManager.maskman.renderPart("Skull");
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.iou);
			ResourceManager.maskman.renderPart("IOU");
		}
		GlStateManager.popMatrix();

		GlStateManager.popMatrix();
	}
}