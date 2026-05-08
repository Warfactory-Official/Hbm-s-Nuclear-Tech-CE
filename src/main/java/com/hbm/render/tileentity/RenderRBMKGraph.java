package com.hbm.render.tileentity;

import com.hbm.blocks.machine.rbmk.RBMKMiniPanelBase;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKGraph;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKGraph.GraphUnit;
import com.hbm.util.BobMathUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderRBMKGraph extends TileEntitySpecialRenderer<TileEntityRBMKGraph> {
	@Override
	public void render(TileEntityRBMKGraph te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

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
			GraphUnit unit = te.graphs[i];
			if(!unit.active) continue;

			GlStateManager.pushMatrix();
			GlStateManager.translate(0.25, i * -0.5 + 0.25, 0);

			GlStateManager.color(1F, 1F, 1F);
			this.bindTexture(ResourceManager.rbmk_numitron_tex);
			ResourceManager.rbmk_numitron.renderAll();

			GlStateManager.pushMatrix();

			RenderArcFurnace.fullbright(true);
			GlStateManager.disableTexture2D();
			GL11.glLineWidth(2F);

			FontRenderer font = Minecraft.getMinecraft().fontRenderer;
			int height = font.FONT_HEIGHT;

			long lowest = unit.minBound ? unit.min : BobMathUtil.min(unit.values);
			long highest = unit.maxBound ? unit.max : BobMathUtil.max(unit.values);
			if(lowest > highest) {
				if(unit.minBound && !unit.maxBound) highest = lowest;
				else lowest = highest;
			}

			GlStateManager.color(0, 1, 0);
			int segments = unit.values.length - 1;
			NTMBufferBuilder buf = NTMImmediate.INSTANCE.beginPosition(GL11.GL_LINES, segments * 2);
			long range = highest - lowest;
			for(int v = 0; v < segments; v++) {
				for(int j = 0; j < 2; j++) {
					int k = v + j;
					long flux = unit.values[k];
					if(flux < lowest) flux = lowest;
					if(flux > highest) flux = highest;
					float dx = 0.03225F;
					float dy = 0.5F - 0.03125F + (flux - lowest) * 0.1875F / Math.max(range, 1);
					float dz = 0.375F - k * 0.75F / (unit.values.length - 1);
					buf.appendPositionUnchecked(dx, dy, dz);
				}
			}
			NTMImmediate.INSTANCE.draw();
			GlStateManager.color(1, 1, 1);
			GlStateManager.enableTexture2D();

			GlStateManager.pushMatrix();
			String labelLower = "" + lowest;
			String labelUpper = "" + highest;
			double lineScale = 0.0025D;
			GlStateManager.translate(0.032, 0.5 - 0.03125 * 1.5, -0.375 + 0.03125);
			GlStateManager.scale(lineScale, -lineScale, lineScale);
			GlStateManager.rotate(90, 0, 1, 0);
			font.drawString("" + labelLower, -font.getStringWidth(labelLower), -height / 2, 0x00ff00);
			GlStateManager.translate(0, -0.03125 * 7 / lineScale, 0);
			font.drawString("" + labelUpper, -font.getStringWidth(labelUpper), -height / 2, 0x00ff00);
			GlStateManager.popMatrix();

			RenderArcFurnace.fullbright(false);

			GlStateManager.popMatrix();

			if(unit.label != null && !unit.label.isEmpty()) {

				GlStateManager.translate(0.01, 0.3125, 0);
				int width = font.getStringWidth(unit.label);
				float f3 = Math.min(0.0125F, 0.75F / Math.max(width, 1));
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
