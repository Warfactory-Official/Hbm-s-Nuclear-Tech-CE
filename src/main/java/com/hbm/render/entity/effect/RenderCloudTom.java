package com.hbm.render.entity.effect;

import com.hbm.entity.effect.EntityCloudTom;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ClientProxy;
import com.hbm.main.ResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;
@AutoRegister(factory = "FACTORY")
public class RenderCloudTom extends Render<EntityCloudTom> {

	public static final IRenderFactory<EntityCloudTom> FACTORY = man -> new RenderCloudTom(man);
	
	protected RenderCloudTom(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityCloudTom entity, double x, double y, double z, float entityYaw, float partialTicks) {
		if (!ClientProxy.renderingConstant)
			return;

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.disableCull();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.depthMask(false);
		GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);

		EntityCloudTom blast = entity;

		double scale = blast.age + partialTicks;

		int segments = 16;
		float angle = (float) Math.toRadians(360D / segments);
		int height = 20;
		int depth = 20;

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buffer = tess.getBuffer();

		bindTexture(this.getEntityTexture(blast));

		GlStateManager.matrixMode(GL11.GL_TEXTURE);
		GlStateManager.loadIdentity();

		float movement = -(Minecraft.getMinecraft().player.ticksExisted + partialTicks) * 0.005F * 10;
		GlStateManager.translate(0, movement, 0);

		GlStateManager.matrixMode(GL11.GL_MODELVIEW);

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

		for (int i = 0; i < segments; i++) {
			for (int j = 0; j < 5; j++) {
				double mod = 1 - j * 0.025;
				double h = height + j * 10;
				double off = 1D / j;

				Vec3d vec = new Vec3d(scale, 0, 0);
				vec = vec.rotateYaw(angle * i);
				double x0 = vec.x * mod;
				double z0 = vec.z * mod;

				buffer.pos(x0, h, z0).tex(0, 1 + off).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
				buffer.pos(x0, -depth, z0).tex(0, 0 + off).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();

				vec = vec.rotateYaw(angle);
				x0 = vec.x * mod;
				z0 = vec.z * mod;

				buffer.pos(x0, -depth, z0).tex(1, 0 + off).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
				buffer.pos(x0, h, z0).tex(1, 1 + off).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
			}
		}

		tess.draw();

		GlStateManager.matrixMode(GL11.GL_TEXTURE);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);

		GlStateManager.depthMask(true);
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableAlpha();
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}

	@Override
	public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
	}


	@Override
	protected ResourceLocation getEntityTexture(EntityCloudTom entity) {
		return ResourceManager.tomblast;
	}

}
