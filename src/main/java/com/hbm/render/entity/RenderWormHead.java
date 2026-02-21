package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.mob.botprime.EntityBOTPrimeHead;
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
public class RenderWormHead extends Render<EntityBOTPrimeHead> {

	public static final IRenderFactory<EntityBOTPrimeHead> FACTORY = man -> new RenderWormHead(man);
	
	public static final IModelCustom body = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/mobs/bot_prime_head.obj"));
	public static final ResourceLocation texture = new ResourceLocation(Tags.MODID, "textures/entity/mark_zero_head.png");
	
	public RenderWormHead(RenderManager rendermanagerIn) {
		super(rendermanagerIn);
		this.shadowOpaque = 0.0F;
	}
	
	@Override
	public void doRender(EntityBOTPrimeHead entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

		GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks - 90, 0.0F, 0.0F, 1.0F);

		this.bindEntityTexture(entity);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.disableCull();
		body.renderAll();
		GlStateManager.enableCull();
		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBOTPrimeHead entity) {
		return texture;
	}

}
