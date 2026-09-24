package com.hbm.render.tileentity;

import com.hbm.blocks.generic.BlockWandStructure.TileEntityWandStructure;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderWandStructure extends TileEntitySpecialRenderer<TileEntityWandStructure> {

	@Override
	public void render(TileEntityWandStructure structure, double x, double y, double z, float interp, int destroyStage, float alpha) {

		GlStateManager.pushMatrix();

		double x1 = x;
		double y1 = y + 1;
		double z1 = z;

		double x2 = x + structure.sizeX;
		double y2 = y + structure.sizeY + 1;
		double z2 = z + structure.sizeZ;

		GlStateManager.disableLighting();
		GlStateManager.disableTexture2D();
		GlStateManager.color(1F, 1F, 1F);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

		buf.pos(x1, y2, z1).color(1F, 1F, 1F, 1F).endVertex();
		buf.pos(x1, y2, z2).color(1F, 1F, 1F, 1F).endVertex();

		buf.pos(x1, y2, z2).color(1F, 1F, 1F, 1F).endVertex();
		buf.pos(x2, y2, z2).color(1F, 1F, 1F, 1F).endVertex();

		buf.pos(x2, y2, z2).color(1F, 1F, 1F, 1F).endVertex();
		buf.pos(x2, y2, z1).color(1F, 1F, 1F, 1F).endVertex();

		buf.pos(x2, y2, z1).color(1F, 1F, 1F, 1F).endVertex();
		buf.pos(x1, y2, z1).color(1F, 1F, 1F, 1F).endVertex();

		buf.pos(x1, y1, z1).color(1F, 1F, 1F, 1F).endVertex();
		buf.pos(x1, y1, z2).color(1F, 1F, 1F, 1F).endVertex();

		buf.pos(x1, y1, z2).color(1F, 1F, 1F, 1F).endVertex();
		buf.pos(x2, y1, z2).color(1F, 1F, 1F, 1F).endVertex();

		buf.pos(x2, y1, z2).color(1F, 1F, 1F, 1F).endVertex();
		buf.pos(x2, y1, z1).color(1F, 1F, 1F, 1F).endVertex();

		buf.pos(x2, y1, z1).color(1F, 1F, 1F, 1F).endVertex();
		buf.pos(x1, y1, z1).color(1F, 1F, 1F, 1F).endVertex();

		buf.pos(x1, y1, z1).color(1F, 1F, 1F, 1F).endVertex();
		buf.pos(x1, y2, z1).color(1F, 1F, 1F, 1F).endVertex();

		buf.pos(x2, y1, z1).color(1F, 1F, 1F, 1F).endVertex();
		buf.pos(x2, y2, z1).color(1F, 1F, 1F, 1F).endVertex();

		buf.pos(x2, y1, z2).color(1F, 1F, 1F, 1F).endVertex();
		buf.pos(x2, y2, z2).color(1F, 1F, 1F, 1F).endVertex();

		buf.pos(x1, y1, z2).color(1F, 1F, 1F, 1F).endVertex();
		buf.pos(x1, y2, z2).color(1F, 1F, 1F, 1F).endVertex();

		tess.draw();

		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();

		GlStateManager.popMatrix();
	}
}
