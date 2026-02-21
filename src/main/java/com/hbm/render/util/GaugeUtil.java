package com.hbm.render.util;

import com.hbm.Tags;
import com.hbm.util.MutableVec3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class GaugeUtil {

	public enum Gauge {

		ROUND_SMALL(new ResourceLocation(Tags.MODID + ":textures/gui/gauges/small_round.png"), 18, 18, 13),
		ROUND_LARGE(new ResourceLocation(Tags.MODID + ":textures/gui/gauges/large_round.png"), 36, 36, 13),
		BOW_SMALL(new ResourceLocation(Tags.MODID + ":textures/gui/gauges/small_bow.png"), 18, 18, 13),
		BOW_LARGE(new ResourceLocation(Tags.MODID + ":textures/gui/gauges/large_bow.png"), 36, 36, 13),
		WIDE_SMALL(new ResourceLocation(Tags.MODID + ":textures/gui/gauges/small_wide.png"), 18, 12, 7),
		WIDE_LARGE(new ResourceLocation(Tags.MODID + ":textures/gui/gauges/large_wide.png"), 36, 24, 11),
		BAR_SMALL(new ResourceLocation(Tags.MODID + ":textures/gui/gauges/small_bar.png"), 36, 12, 16);

		ResourceLocation texture;
		int width;
		int height;
		int count;

		Gauge(ResourceLocation texture, int width, int height, int count) {
			this.texture = texture;
			this.width = width;
			this.height = height;
			this.count = count;
		}
	}

	/**
	 * 
	 * @param gauge The gauge enum to use
	 * @param x The x coord in the GUI (left)
	 * @param y The y coord in the GUI (top)
	 * @param z The z-level (from GUI.zLevel)
	 * @param progress Double from 0-1 how far the gauge has progressed
	 */
	public static void renderGauge(Gauge gauge, double x, double y, double z, double progress) {

		Minecraft.getMinecraft().renderEngine.bindTexture(gauge.texture);

		int frameNum = (int) Math.round((gauge.count - 1) * progress);
		double singleFrame = 1D / (double)gauge.count;
		double frameOffset = singleFrame * frameNum;

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buf.pos(x, 				 y + gauge.height, 	z).tex(0, 	frameOffset + singleFrame).endVertex();
		buf.pos(x + gauge.width, y + gauge.height,  z).tex(1, 	frameOffset + singleFrame).endVertex();
		buf.pos(x + gauge.width, y, 				z).tex(1, 	frameOffset).endVertex();
		buf.pos(x, 				 y, 				z).tex(0, 	frameOffset).endVertex();
		tess.draw();
	}

	public static void drawSmoothGauge(int x, int y, double z, double progress, double tipLength, double backLength, double backSide, int color) {
		drawSmoothGauge(x, y, z, progress, tipLength, backLength, backSide, color, 0x000000);
	}

	public static void drawSmoothGauge(int x, int y, double z, double progress, double tipLength, double backLength, double backSide, int color, int colorOuter) {
		GlStateManager.disableTexture2D();

		progress = MathHelper.clamp(progress, 0, 1);
		float angle = (float) Math.toRadians(-progress * 270 - 45);

		MutableVec3d tip = new MutableVec3d(0, tipLength, 0);
		MutableVec3d left = new MutableVec3d(backSide, -backLength, 0);
		MutableVec3d right = new MutableVec3d(-backSide, -backLength, 0);

		tip.rotateRollSelf(angle);
		left.rotateRollSelf(angle);
		right.rotateRollSelf(angle);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();

		float r_outer = (float)(colorOuter >> 16 & 255) / 255.0F;
		float g_outer = (float)(colorOuter >> 8 & 255) / 255.0F;
		float b_outer = (float)(colorOuter & 255) / 255.0F;

		float r_inner = (float)(color >> 16 & 255) / 255.0F;
		float g_inner = (float)(color >> 8 & 255) / 255.0F;
		float b_inner = (float)(color & 255) / 255.0F;

		bufferbuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);

		double mult = 1.5;
		bufferbuilder.pos(x + tip.x * mult, y + tip.y * mult, z).color(r_outer, g_outer, b_outer, 1.0F).endVertex();
		bufferbuilder.pos(x + left.x * mult, y + left.y * mult, z).color(r_outer, g_outer, b_outer, 1.0F).endVertex();
		bufferbuilder.pos(x + right.x * mult, y + right.y * mult, z).color(r_outer, g_outer, b_outer, 1.0F).endVertex();

		bufferbuilder.pos(x + tip.x, y + tip.y, z).color(r_inner, g_inner, b_inner, 1.0F).endVertex();
		bufferbuilder.pos(x + left.x, y + left.y, z).color(r_inner, g_inner, b_inner, 1.0F).endVertex();
		bufferbuilder.pos(x + right.x, y + right.y, z).color(r_inner, g_inner, b_inner, 1.0F).endVertex();

		tessellator.draw();

		GlStateManager.enableTexture2D();
	}
}