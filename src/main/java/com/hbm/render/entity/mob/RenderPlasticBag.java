package com.hbm.render.entity.mob;

import com.hbm.entity.mob.EntityPlasticBag;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.jetbrains.annotations.NotNull;

@AutoRegister(entity = EntityPlasticBag.class, factory = "FACTORY")
public class RenderPlasticBag extends Render<EntityPlasticBag> {

	public static final IRenderFactory<EntityPlasticBag> FACTORY = RenderPlasticBag::new;

	public RenderPlasticBag(RenderManager manager) {
		super(manager);
		this.shadowOpaque = 0.0F;
	}

	@Override
	public void doRender(@NotNull EntityPlasticBag entity, double x, double y, double z, float yaw, float partialTicks) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.disableCull();
		GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks - 90, 0.0F, 0.0F, 1.0F);

		this.bindEntityTexture(entity);
		ResourceManager.plasticbag.renderAll();

		GlStateManager.enableCull();
		GlStateManager.popMatrix();
	}

	@Override
	protected @NotNull ResourceLocation getEntityTexture(@NotNull EntityPlasticBag entity) {
		return ResourceManager.plasticbag_tex;
	}
}
