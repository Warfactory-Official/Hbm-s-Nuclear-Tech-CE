package com.hbm.render.entity.projectile;

import com.hbm.entity.projectile.EntitySiegeLaser;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.NTMImmediate;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

@AutoRegister(entity = EntitySiegeLaser.class, factory = "FACTORY")
public class RenderSiegeLaser extends Render<EntitySiegeLaser> {

	public static final IRenderFactory<EntitySiegeLaser> FACTORY = RenderSiegeLaser::new;

	public RenderSiegeLaser(RenderManager manager) {
		super(manager);
	}

	@Override
	public void doRender(@NotNull EntitySiegeLaser laser, double x, double y, double z, float yaw, float partialTicks) {

		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x, (float) y, (float) z);
		GlStateManager.rotate(laser.prevRotationYaw + (laser.rotationYaw - laser.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(laser.prevRotationPitch + (laser.rotationPitch - laser.prevRotationPitch) * partialTicks + 180, 0.0F, 0.0F, 1.0F);

		this.renderDart(laser);

		GlStateManager.popMatrix();
	}

	@Override
	protected @NotNull ResourceLocation getEntityTexture(@NotNull EntitySiegeLaser entity) {
		return ResourceManager.universal;
	}

	private void renderDart(EntitySiegeLaser laser) {

		GlStateManager.pushMatrix();
		GlStateManager.disableTexture2D();
		GlStateManager.disableCull();
		GlStateManager.disableLighting();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GlStateManager.depthMask(false);

		GlStateManager.scale(1F / 4F, 1F / 8F, 1F / 8F);
		GlStateManager.scale(-1, 1, 1);
		GlStateManager.scale(2, 2, 2);

		int color = laser.getColor();
		int r = (color >> 16) & 255;
		int g = (color >> 8) & 255;
		int b = color & 255;

		BufferBuilder buf = NTMImmediate.INSTANCE.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);

		buf.pos(6, 0, 0).color(r, g, b, 255).endVertex();
		buf.pos(3, -1, -1).color(r, g, b, 0).endVertex();
		buf.pos(3, 1, -1).color(r, g, b, 0).endVertex();

		buf.pos(3, -1, 1).color(r, g, b, 0).endVertex();
		buf.pos(6, 0, 0).color(r, g, b, 255).endVertex();
		buf.pos(3, 1, 1).color(r, g, b, 0).endVertex();

		buf.pos(3, -1, -1).color(r, g, b, 0).endVertex();
		buf.pos(6, 0, 0).color(r, g, b, 255).endVertex();
		buf.pos(3, -1, 1).color(r, g, b, 0).endVertex();

		buf.pos(6, 0, 0).color(r, g, b, 255).endVertex();
		buf.pos(3, 1, -1).color(r, g, b, 0).endVertex();
		buf.pos(3, 1, 1).color(r, g, b, 0).endVertex();

		buf.pos(6, 0, 0).color(r, g, b, 255).endVertex();
		buf.pos(4, -0.5, -0.5).color(r, g, b, 255).endVertex();
		buf.pos(4, 0.5, -0.5).color(r, g, b, 255).endVertex();

		buf.pos(4, -0.5, 0.5).color(r, g, b, 255).endVertex();
		buf.pos(6, 0, 0).color(r, g, b, 255).endVertex();
		buf.pos(4, 0.5, 0.5).color(r, g, b, 255).endVertex();

		buf.pos(4, -0.5, -0.5).color(r, g, b, 255).endVertex();
		buf.pos(6, 0, 0).color(r, g, b, 255).endVertex();
		buf.pos(4, -0.5, 0.5).color(r, g, b, 255).endVertex();

		buf.pos(6, 0, 0).color(r, g, b, 255).endVertex();
		buf.pos(4, 0.5, -0.5).color(r, g, b, 255).endVertex();
		buf.pos(4, 0.5, 0.5).color(r, g, b, 255).endVertex();

		NTMImmediate.INSTANCE.draw();

		buf = NTMImmediate.INSTANCE.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		buf.pos(4, 0.5, -0.5).color(r, g, b, 255).endVertex();
		buf.pos(4, 0.5, 0.5).color(r, g, b, 255).endVertex();
		buf.pos(0, 0.5, 0.5).color(r, g, b, 0).endVertex();
		buf.pos(0, 0.5, -0.5).color(r, g, b, 0).endVertex();

		buf.pos(4, -0.5, -0.5).color(r, g, b, 255).endVertex();
		buf.pos(4, -0.5, 0.5).color(r, g, b, 255).endVertex();
		buf.pos(0, -0.5, 0.5).color(r, g, b, 0).endVertex();
		buf.pos(0, -0.5, -0.5).color(r, g, b, 0).endVertex();

		buf.pos(4, -0.5, 0.5).color(r, g, b, 255).endVertex();
		buf.pos(4, 0.5, 0.5).color(r, g, b, 255).endVertex();
		buf.pos(0, 0.5, 0.5).color(r, g, b, 0).endVertex();
		buf.pos(0, -0.5, 0.5).color(r, g, b, 0).endVertex();

		buf.pos(4, -0.5, -0.5).color(r, g, b, 255).endVertex();
		buf.pos(4, 0.5, -0.5).color(r, g, b, 255).endVertex();
		buf.pos(0, 0.5, -0.5).color(r, g, b, 0).endVertex();
		buf.pos(0, -0.5, -0.5).color(r, g, b, 0).endVertex();

		NTMImmediate.INSTANCE.draw();

		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.depthMask(true);

		GlStateManager.popMatrix();
	}
}
