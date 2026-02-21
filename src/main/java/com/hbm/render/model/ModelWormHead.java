package com.hbm.render.model;

import com.hbm.Tags;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.loader.IModelCustom;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class ModelWormHead extends ModelBase {

	public static final IModelCustom head = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/mobs/bot_prime_head.obj")).asVBO();

	@Override
	public void render(Entity entity, float x, float y, float z, float f3, float f4, float f5) {
		super.render(entity, x, y, z, f3, f4, f5);

		GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * f5 - 90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * f5 - 90, 0.0F, 0.0F, 1.0F);

		head.renderAll();
	}

}