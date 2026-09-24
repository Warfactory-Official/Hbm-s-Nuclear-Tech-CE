package com.hbm.render.tileentity;

import com.hbm.blocks.machine.rbmk.RBMKMiniPanelBase;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKLever;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKLever.LeverUnit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;

@AutoRegister
public class RenderRBMKLever extends TileEntitySpecialRenderer<TileEntityRBMKLever> {

	@Override
	public void render(TileEntityRBMKLever te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

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

		for(int i = 0; i < 2; i++) {
			LeverUnit unit = te.levers[i];
			if(!unit.active) continue;

			GlStateManager.pushMatrix();
			GlStateManager.translate(0.25, 0, i * -0.5 + 0.25);

			GlStateManager.color(1F, 1F, 1F);
			this.bindTexture(ResourceManager.rbmk_lever_tex);
			ResourceManager.rbmk_lever.renderPart("Base");

			GlStateManager.pushMatrix();
			float progress = unit.prevFlipProgress + (unit.flipProgress - unit.prevFlipProgress) * partialTicks;
			GlStateManager.translate(0.125, 0.5625, 0);
			GlStateManager.rotate(-180 * progress, 0, 0, 1);
			GlStateManager.translate(-0.125, -0.5625, 0);
			ResourceManager.rbmk_lever.renderPart("Lever");
			GlStateManager.popMatrix();

			FontRenderer font = Minecraft.getMinecraft().fontRenderer;
			int height = font.FONT_HEIGHT;
			if(unit.label != null && !unit.label.isEmpty()) {

				GlStateManager.translate(0.01, 0.0625, 0);
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
