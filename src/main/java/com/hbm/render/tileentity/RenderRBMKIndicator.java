package com.hbm.render.tileentity;

import com.hbm.blocks.machine.rbmk.RBMKMiniPanelBase;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKIndicator;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKIndicator.IndicatorUnit;
import com.hbm.util.ColorUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;

@AutoRegister
public class RenderRBMKIndicator extends TileEntitySpecialRenderer<TileEntityRBMKIndicator> {

	@Override
	public void render(TileEntityRBMKIndicator te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

		if(!te.anyActive) return;

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

		for(int i = 0; i < 6; i++) {
			IndicatorUnit unit = te.indicators[i];
			if(!unit.active) continue;

			GlStateManager.pushMatrix();
			GlStateManager.translate(0.25, (i / 2) * -0.3125 + 0.3125, (i % 2) * -0.5 + 0.25);

			GlStateManager.color(1F, 1F, 1F);
			this.bindTexture(ResourceManager.rbmk_indicator_tex);
			ResourceManager.rbmk_indicator.renderPart("Base");

			float mult = unit.light ? 1F : 0.35F;
			GlStateManager.color(ColorUtil.fr(unit.color) * mult, ColorUtil.fg(unit.color) * mult, ColorUtil.fb(unit.color) * mult);
			if(unit.light) RenderArcFurnace.fullbright(true);
			ResourceManager.rbmk_indicator.renderPart("Light");
			if(unit.light) RenderArcFurnace.fullbright(false);
			GlStateManager.color(1F, 1F, 1F);

			FontRenderer font = Minecraft.getMinecraft().fontRenderer;
			int height = font.FONT_HEIGHT;
			if(unit.label != null && !unit.label.isEmpty()) {

				GlStateManager.translate(0.0725, 0.5, 0);
				int width = font.getStringWidth(unit.label);
				float f3 = Math.min(0.0125F, 0.3F / Math.max(width, 1));
				GlStateManager.scale(f3, -f3, f3);
				GlStateManager.glNormal3f(0.0F, 0.0F, -1.0F);
				GlStateManager.rotate(90, 0, 1, 0);

				font.drawString(unit.label, -width / 2, -height / 2, 0x000000);
			}

			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();
	}
}
