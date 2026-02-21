package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.projectile.EntityBullet;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.model.ModelBullet;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import java.util.Random;
@AutoRegister(factory = "FACTORY")
public class RenderBullet extends Render<EntityBullet> {

	public static final IRenderFactory<EntityBullet> FACTORY = (RenderManager manager) -> new RenderBullet(manager);
	
	private ModelBullet miniNuke;
	
	protected RenderBullet(RenderManager renderManager) {
		super(renderManager);
		miniNuke = new ModelBullet();
	}

	@Override
	public void doRender(EntityBullet rocket, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x, (float) y, (float) z);
		GlStateManager.rotate(rocket.prevRotationYaw + (rocket.rotationYaw - rocket.prevRotationYaw) * partialTicks - 90.0F,
				0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(rocket.prevRotationPitch + (rocket.rotationPitch - rocket.prevRotationPitch) * partialTicks + 180,
				0.0F, 0.0F, 1.0F);
		GlStateManager.scale(1.5F, 1.5F, 1.5F);
		

		GlStateManager.rotate(new Random(rocket.getEntityId()).nextInt(360),
				1.0F, 0.0F, 0.0F);

		if (rocket instanceof EntityBullet && rocket.getIsChopper()) {
			bindTexture(new ResourceLocation(Tags.MODID + ":textures/models/projectiles/emplacer.png"));
		} else if (rocket instanceof EntityBullet && rocket.getIsCritical()) {
			bindTexture(new ResourceLocation(Tags.MODID + ":textures/models/projectiles/tau.png"));
		} else if (rocket instanceof EntityBullet) {
			bindTexture(new ResourceLocation(Tags.MODID + ":textures/models/projectiles/bullet.png"));
		}
		miniNuke.renderAll(0.0625F);
		
		//renderFlechette();
		//renderDart();
		
		GlStateManager.popMatrix();
	}
	
	@Override
	public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityBullet entity) {
		if (entity.getIsChopper()) {
			return new ResourceLocation(Tags.MODID + ":textures/models/projectiles/emplacer.png");
		} else if (entity.getIsCritical()) {
			return new ResourceLocation(Tags.MODID + ":textures/models/projectiles/tau.png");
		} else {
			return new ResourceLocation(Tags.MODID + ":textures/models/projectiles/bullet.png");
		}
	}

}
