package com.hbm.render.tileentity;

import com.hbm.blocks.machine.rbmk.RBMKMiniPanelBase;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKNumitron;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKNumitron.DisplayUnit;
import com.hbm.util.BobMathUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;

@AutoRegister
public class RenderRBMKNumitron extends TileEntitySpecialRenderer<TileEntityRBMKNumitron> {

	private static final int PACKED_NORMAL_UP = NTMBufferBuilder.packNormal(0F, 1F, 0F);

	@Override
	public void render(TileEntityRBMKNumitron te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

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
			DisplayUnit unit = te.displays[i];
			if(!unit.active) continue;

			GlStateManager.pushMatrix();
			GlStateManager.translate(0.25, i * -0.5 + 0.25, 0);

			GlStateManager.color(1F, 1F, 1F);
			this.bindTexture(ResourceManager.rbmk_numitron_tex);
			ResourceManager.rbmk_numitron.renderAll();

			GlStateManager.pushMatrix();

			RenderArcFurnace.fullbright(true);
			GlStateManager.enableLighting();

			this.bindTexture(ResourceManager.rbmk_numitron_lights_tex);

			float scale = 200F;
			float w = 8F / scale;
			float h = 13F / scale;
			float yOffset = 0.5625F;

			String value = BobMathUtil.getShortNumber(unit.value);
			while(value.length() < 7) value = "0" + value;

			NTMBufferBuilder buf = NTMImmediate.INSTANCE.beginPositionTexNormalQuads(7);
			for(int j = 0; j < 7; j++) {
				float zOffset = (j - 3F) * 0.1F;
				char c = value.charAt(j);
				float u = -1F;
                float v = 0F;
				if(c == '.') {u = 0.9F; v = 0.5F;}
				if(c == '-') {u = 0.8F; v = 0.5F;}
				else if(c == 'k') {u = 0.0F; v = 0.5F;}
				else if(c == 'M') {u = 0.1F; v = 0.5F;}
				else if(c == 'G') {u = 0.2F; v = 0.5F;}
				else if(c == 'T') {u = 0.3F; v = 0.5F;}
				else if(c == 'P') {u = 0.4F; v = 0.5F;}
				else if(c == 'E') {u = 0.5F; v = 0.5F;} // i would love to say this sucks, but this is actually surprisingly easy to read and probably the most performant way of doing it
				int charVal = c - '0'; // no string operations, no int parsing, no nothing, we just rawdog shit shit
				if(charVal >= 0 && charVal <= 9) {u = 0.1F * charVal; v = 0.0F;}
				if(u == -1) {u = 0.8F; v = 0.5F;}
				buf.appendPositionTexNormalUnchecked(0.03135F, -h + yOffset,  w - zOffset, u,        v + 0.5F, PACKED_NORMAL_UP);
				buf.appendPositionTexNormalUnchecked(0.03135F,  h + yOffset,  w - zOffset, u,        v,        PACKED_NORMAL_UP);
				buf.appendPositionTexNormalUnchecked(0.03135F,  h + yOffset, -w - zOffset, u + 0.1F, v,        PACKED_NORMAL_UP);
				buf.appendPositionTexNormalUnchecked(0.03135F, -h + yOffset, -w - zOffset, u + 0.1F, v + 0.5F, PACKED_NORMAL_UP);
			}
			NTMImmediate.INSTANCE.draw();

			RenderArcFurnace.fullbright(false);

			GlStateManager.popMatrix();

			FontRenderer font = Minecraft.getMinecraft().fontRenderer;
			int height = font.FONT_HEIGHT;

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
