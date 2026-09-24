package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

public class ModelWormHead extends ModelBase {

	@Override
	public void render(Entity entity, float x, float y, float z, float f3, float f4, float f5) {
		super.render(entity, x, y, z, f3, f4, f5);

		GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * f5 - 90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * f5 - 90, 0.0F, 0.0F, 1.0F);

		ResourceManager.bot_prime_head.renderAll();
	}

}