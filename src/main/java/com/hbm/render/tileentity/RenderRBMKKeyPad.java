package com.hbm.render.tileentity;

import com.hbm.blocks.machine.rbmk.RBMKMiniPanelBase;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKKeyPad;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKKeyPad.KeyUnit;
import com.hbm.util.ColorUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;

@AutoRegister
public class RenderRBMKKeyPad extends TileEntitySpecialRenderer<TileEntityRBMKKeyPad> {

	@Override
	public void render(TileEntityRBMKKeyPad te,double x,double y,double z,float partialTicks,int destroyStage,float alpha) {

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
			KeyUnit key = te.keys[i];
			if(!key.active) continue;

			boolean glow = key.isPressed;
			float mult = glow ? 1F : 0.65F;

			GlStateManager.pushMatrix();
			GlStateManager.translate(0.25, (i / 2) * -0.5 + 0.25, (i % 2) * -0.5 + 0.25);

			GlStateManager.color(1F, 1F, 1F);
			this.bindTexture(ResourceManager.rbmk_keypad_tex);
			ResourceManager.rbmk_button.renderPart("Socket");

			GlStateManager.pushMatrix();
			GlStateManager.translate(key.isPressed ? -0.03125 : 0, 0, 0);
			GlStateManager.color(ColorUtil.fr(key.color) * mult, ColorUtil.fg(key.color) * mult, ColorUtil.fb(key.color) * mult);

			if(glow) {
				RenderArcFurnace.fullbright(true);
				GlStateManager.enableLighting(); // we want a glow, but normal lighting should still apply
			}
			ResourceManager.rbmk_button.renderPart("Button");
			if(glow) RenderArcFurnace.fullbright(false);
			GlStateManager.color(1F, 1F, 1F);

			GlStateManager.popMatrix();

			FontRenderer font = Minecraft.getMinecraft().fontRenderer;
			int height = font.FONT_HEIGHT;
			if(key.label != null && !key.label.isEmpty()) {

				GlStateManager.translate(0.01, 0.3125, 0);
				int width = font.getStringWidth(key.label);
				float f3 = Math.min(0.0125F, 0.4F / Math.max(width, 1));
				GlStateManager.scale(f3, -f3, f3);
				GlStateManager.glNormal3f(0.0F, 0.0F, -1.0F);
				GlStateManager.rotate(90, 0, 1, 0);

				RenderArcFurnace.fullbright(true);
				font.drawString(key.label, - width / 2, - height / 2, 0x00ff00);
				RenderArcFurnace.fullbright(false);
			}
			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();
	}
}
