package com.hbm.render.tileentity;

import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.pile.TileEntityPileControl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderPileControl extends TileEntitySpecialRenderer<TileEntityPileControl> {

	@Override
	public void render(@NotNull TileEntityPileControl control, double x, double y, double z, float interp, int destroyStage, float alpha) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y, z + 0.5);
		GlStateManager.enableLighting();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		switch(control.getBlockMetadata() % 4) {
		case 0: GlStateManager.rotate(90, 0, 1, 0); break;
		case 1: GlStateManager.rotate(270, 0, 1, 0); break;
		case 2: GlStateManager.rotate(180, 0, 1, 0); break;
		case 3: GlStateManager.rotate(0, 0, 1, 0); break;
		}

		double level = control.lastLevel + (control.level - control.lastLevel) * interp;

		bindTexture(ResourceManager.pile_control_tex);
		ResourceManager.pile_control.renderPart("Base");
		GlStateManager.translate(0, level * 0.75, 0);
		ResourceManager.pile_control.renderPart("Rod");

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.popMatrix();
	}
}
