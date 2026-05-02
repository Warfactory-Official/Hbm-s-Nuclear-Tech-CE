package com.hbm.render.tileentity;

import com.hbm.blocks.machine.rbmk.RBMKMiniPanelBase;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKGauge;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKGauge.GaugeUnit;
import com.hbm.util.BobMathUtil;
import com.hbm.util.ColorUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;

@AutoRegister
public class RenderRBMKGauge extends TileEntitySpecialRenderer<TileEntityRBMKGauge> {
	@Override
	public void render(TileEntityRBMKGauge te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y, z + 0.5);
		GlStateManager.enableCull();
		GlStateManager.enableLighting();

		EnumFacing facing = te.getWorld().getBlockState(te.getPos()).getValue(RBMKMiniPanelBase.FACING);
		switch(facing) {
			case NORTH: GlStateManager.rotate(90, 0F, 1F, 0F); break;
			case WEST: GlStateManager.rotate(180, 0F, 1F, 0F); break;
			case SOUTH: GlStateManager.rotate(270, 0F, 1F, 0F); break;
			case EAST: GlStateManager.rotate(0, 0F, 1F, 0F); break;
			default: break;
		}

		for(int i = 0; i < 4; i++) {
			GaugeUnit unit = te.gauges[i];
			if(!unit.active) continue;

			GlStateManager.pushMatrix();
			GlStateManager.translate(0.25, (i / 2) * -0.5 + 0.25, (i % 2) * -0.5 + 0.25);

			GlStateManager.color(1F, 1F, 1F);
			this.bindTexture(ResourceManager.rbmk_gauge_tex);
			ResourceManager.rbmk_gauge.renderPart("Gauge");

			GlStateManager.pushMatrix();
			GlStateManager.color(ColorUtil.fr(unit.color), ColorUtil.fg(unit.color), ColorUtil.fb(unit.color));

			double value = unit.lastRenderValue + (unit.renderValue - unit.lastRenderValue) * partialTicks;
			long lower = Math.min(unit.min, unit.max);
			long upper = Math.max(unit.min, unit.max);
			if(lower == upper) upper += 1;
			long range = upper - lower;
			double angle = (double) (value - lower) / (double) range * 50D;
			if(unit.min > unit.max) angle = 50 - angle;

			angle = MathHelper.clamp(angle, 0, 80);

			GlStateManager.translate(0, 0.4375, -0.125);
			GlStateManager.rotate((float) (angle - 85), -1, 0, 0);
			GlStateManager.translate(0, -0.4375, 0.125);

			GlStateManager.disableTexture2D();
			RenderArcFurnace.fullbright(true);
			GlStateManager.enableLighting();
			ResourceManager.rbmk_gauge.renderPart("Needle");
			RenderArcFurnace.fullbright(false);
			GlStateManager.enableTexture2D();

			GlStateManager.popMatrix();

			FontRenderer font = Minecraft.getMinecraft().fontRenderer;
			int height = font.FONT_HEIGHT;

			double lineScale = 0.0025D;
			String lineLower = unit.min <= 10_000 ? unit.min + "" : BobMathUtil.getShortNumber(unit.min);
			String lineUpper = unit.max <= 10_000 ? unit.max + "" : BobMathUtil.getShortNumber(unit.max);

			for(int j = 0; j < 2; j++) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0.4375, -0.125);
				GlStateManager.rotate(10 + j * 50, -1, 0, 0);
				GlStateManager.translate(0, -0.4375, 0.125);

				GlStateManager.translate(0.032, 0.4375, 0.125);
				GlStateManager.scale(lineScale, -lineScale, lineScale);
				GlStateManager.glNormal3f(0.0F, 0.0F, -1.0F);
				GlStateManager.rotate(90, 0, 1, 0);
				font.drawString(j == 0 ? lineLower : lineUpper, 0, -height / 2, 0x000000);
				GlStateManager.popMatrix();
			}

			if(unit.label != null && !unit.label.isEmpty()) {

				GlStateManager.translate(0.01, 0.3125, 0);
				int width = font.getStringWidth(unit.label);
				float f3 = Math.min(0.0125F, 0.4F / Math.max(width, 1));
				GlStateManager.scale(f3, -f3, f3);
				GlStateManager.glNormal3f(0.0F, 0.0F, -1.0F);
				GlStateManager.rotate(90, 0, 1, 0);

				RenderArcFurnace.fullbright(true);
				font.drawString(unit.label, -width / 2, -height / 2, 0x00ff00);
				RenderArcFurnace.fullbright(false);
			}
			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();
	}
}
