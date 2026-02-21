package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.mob.botprime.EntityBOTPrimeBody;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.loader.IModelCustom;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;
@AutoRegister(factory = "FACTORY")
public class RenderWormBody extends Render<EntityBOTPrimeBody> {

	public static final IRenderFactory<EntityBOTPrimeBody> FACTORY = man -> new RenderWormBody(man);
	
	public static final IModelCustom body = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/mobs/bot_prime_body.obj"));
	public static final ResourceLocation texture = new ResourceLocation(Tags.MODID, "textures/entity/mark_zero_body.png");

	protected RenderWormBody(RenderManager renderManager) {
		super(renderManager);
		this.shadowOpaque = 0.0F;
	}
	
	@Override
	public void doRender(EntityBOTPrimeBody entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks - 90, 0.0F, 0.0F, 1.0F);

		this.bindEntityTexture(entity);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.disableCull();
		body.renderAll();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableCull();

		GlStateManager.popMatrix();
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityBOTPrimeBody entity) {
		return texture;
	}
	
}
