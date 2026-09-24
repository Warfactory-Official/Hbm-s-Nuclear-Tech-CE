package com.hbm.render.tileentity;

import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.pile.TileEntityPileVent;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderPileVent extends TileEntitySpecialRenderer<TileEntityPileVent> {

	@Override
	public void render(@NotNull TileEntityPileVent vent, double x, double y, double z, float interp, int destroyStage, float alpha) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y, z + 0.5);
		GlStateManager.enableLighting();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		switch(vent.getBlockMetadata() % 4) {
		case 0: GlStateManager.rotate(90, 0, 1, 0); break;
		case 1: GlStateManager.rotate(270, 0, 1, 0); break;
		case 2: GlStateManager.rotate(180, 0, 1, 0); break;
		case 3: GlStateManager.rotate(0, 0, 1, 0); break;
		}

		float rot = vent.lastFan + (vent.fan - vent.lastFan) * interp;

		bindTexture(ResourceManager.pile_vent_tex);
		ResourceManager.pile_vent.renderPart("Pipe");
		GlStateManager.rotate(rot, 0, 1, 0);
		ResourceManager.pile_vent.renderPart("Fan");

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.popMatrix();
	}
}
