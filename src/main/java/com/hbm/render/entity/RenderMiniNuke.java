package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.projectile.EntityMiniNuke;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.model.ModelMiniNuke;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(factory = "FACTORY")
public class RenderMiniNuke extends Render<EntityMiniNuke> {

	public static final IRenderFactory<EntityMiniNuke> FACTORY = (RenderManager man) -> {return new RenderMiniNuke(man);};
	
	protected static ResourceLocation nuke_rl = new ResourceLocation(Tags.MODID + ":textures/models/projectiles/MiniNuke.png");
	private ModelMiniNuke miniNuke;
	
	protected RenderMiniNuke(RenderManager renderManager) {
		super(renderManager);
		miniNuke = new ModelMiniNuke();
	}
	
	@Override
	public void doRender(EntityMiniNuke entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks + 180, 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(1.5F, 1.5F, 1.5F);
		bindTexture(nuke_rl);
		miniNuke.renderAll(0.0625F);
		GlStateManager.popMatrix();
	}
	
	@Override
	public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {}

	@Override
	protected ResourceLocation getEntityTexture(EntityMiniNuke entity) {
		return nuke_rl;
	}

}
