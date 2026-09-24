package com.hbm.render.entity.effect;

import com.hbm.entity.effect.EntitySpear;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

import java.util.Random;
@AutoRegister(factory = "FACTORY")
public class RenderSpear extends Render<EntitySpear> {

	public static final IRenderFactory<EntitySpear> FACTORY = man -> new RenderSpear(man);
	
	protected RenderSpear(RenderManager renderManager){
		super(renderManager);
	}
	
	@Override
	public void doRender(EntitySpear entity, double x, double y, double z, float entityYaw, float partialTicks){
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x, (float) y + 15, (float) z);
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		
		GlStateManager.rotate(180, 1, 0, 0);
		GlStateManager.scale(2, 2, 2);
		
		EntitySpear spear = (EntitySpear) entity;
		
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		bindTexture(ResourceManager.lance_tex);
		ResourceManager.lance.renderPart("Spear");
		
		if(spear.ticksInGround > 0) {
			float occupancy = Math.min((spear.ticksInGround + partialTicks) / 100F, 1F);
			GlStateManager.color(1F, 1F, 1F, occupancy);

			GlStateManager.disableLighting();
			GlStateManager.enableBlend();
			GlStateManager.alphaFunc(GL11.GL_GEQUAL, 0.0F);
			GlStateManager.disableTexture2D();
			
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.DST_ALPHA);
			
			ResourceManager.lance.renderPart("Spear");
			
			GlStateManager.enableTexture2D();
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
			GlStateManager.disableBlend();
			GlStateManager.enableLighting();
			
			GlStateManager.color(1, 1, 1, 1);
			
			renderFlash((spear.ticksInGround + partialTicks) / 200D);
		}
		
		GlStateManager.shadeModel(GL11.GL_FLAT);
		
		GlStateManager.popMatrix();
	}

	private void renderFlash(double intensity) {

		GlStateManager.scale(0.2F, 0.2F, 0.2F);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buf = tessellator.getBuffer();
		RenderHelper.disableStandardItemLighting();

		Random random = new Random(432L);
		GlStateManager.disableTexture2D();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		GlStateManager.disableAlpha();
		GlStateManager.enableCull();
		GlStateManager.depthMask(false);

		GlStateManager.pushMatrix();

		float scale = 25;

		for(int i = 0; i < 64; i++) {

			GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);

			float vert1 = (random.nextFloat() * 20.0F + 5.0F + 1 * 10.0F) * (float) (intensity * intensity * scale);
			float vert2 = (random.nextFloat() * 2.0F + 1.0F + 1 * 2.0F) * (float) (intensity * intensity * scale);

			buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
			buf.pos(0, 0, 0).color(1.0F, 0.6F, 0.6F, (float) (intensity * intensity) * 2F).endVertex();
			buf.pos(-0.866D * vert2, vert1, -0.5F * vert2).color(1.0F, 0.6F, 0.6F, 0.0F).endVertex();
			buf.pos(0.866D * vert2, vert1, -0.5F * vert2).color(1.0F, 0.6F, 0.6F, 0.0F).endVertex();
			buf.pos(0.0D, vert1, 1.0F * vert2).color(1.0F, 0.6F, 0.6F, 0.0F).endVertex();
			buf.pos(-0.866D * vert2, vert1, -0.5F * vert2).color(1.0F, 0.6F, 0.6F, 0.0F).endVertex();
			tessellator.draw();
		}

		GlStateManager.popMatrix();

		GlStateManager.depthMask(true);
		GlStateManager.disableCull();
		GlStateManager.disableBlend();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.enableTexture2D();
		GlStateManager.enableAlpha();
		RenderHelper.enableStandardItemLighting();
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntitySpear entity){
		return ResourceManager.lance_tex;
	}

}
