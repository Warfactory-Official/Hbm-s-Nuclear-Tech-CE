package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.misc.DiamondPronter;
import com.hbm.render.misc.EnumSymbol;
import com.hbm.tileentity.machine.TileEntityMachineBigAssTank;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderBigAssTank extends TileEntitySpecialRenderer<TileEntityMachineBigAssTank> implements IItemRendererProvider {

	@Override
	public void render(TileEntityMachineBigAssTank bat, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5D, y, z + 0.5D);
		GlStateManager.enableLighting();
		GlStateManager.enableCull();

		if(bat.tilted) {
			GlStateManager.translate(0, -1, 0);
			GlStateManager.rotate(10, 0, 0, 1);
			GlStateManager.rotate(5, 0, 1, 0);
		}

		switch(bat.getBlockMetadata() - 10) {
		case 2: GlStateManager.rotate(270, 0F, 1F, 0F); break;
		case 4: GlStateManager.rotate(0, 0F, 1F, 0F); break;
		case 3: GlStateManager.rotate(90, 0F, 1F, 0F); break;
		case 5: GlStateManager.rotate(180, 0F, 1F, 0F); break;
		}

		bindTexture(ResourceManager.bigasstank_tex);

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		ResourceManager.bigasstank.renderAll();
		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.disableLighting();

		FluidType type = bat.tankNew.getTankType();

		if(type != null && type != Fluids.NONE) {

			GlStateManager.pushMatrix();
			int poison = type.poison;
			int flammability = type.flammability;
			int reactivity = type.reactivity;
			EnumSymbol symbol = type.symbol;

			GlStateManager.rotate(22.5F, 0, 1, 0);

			for(int j = 0; j < 2; j++) {

				GlStateManager.pushMatrix();
				GlStateManager.translate(5.5, 2, 0);
				DiamondPronter.pront(poison, flammability, reactivity, symbol);
				GlStateManager.popMatrix();
				GlStateManager.rotate(180, 0, 1, 0);
			}
			GlStateManager.popMatrix();
		}

		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.disableCull();
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GlStateManager.color(1F, 1F, 1F);

		bindTexture(bat.tankNew.getTankType().getTexture());

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buffer = tess.getBuffer();

		double height = bat.tankNew.getFill() * 1.5D / bat.tankNew.getMaxFill();
		double off = 5.9375;
		double speed = 250D;
		double scaleFactor = 0.5D; // small number make it zoom in, big number make it zoom out

		double minU = -((bat.getWorld().getTotalWorldTime() % speed + partialTicks) / speed) % 1D;
		double maxU = minU + 1 * scaleFactor;

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		buffer.pos(-off, 1.75, -0.25).tex(minU, 0).endVertex();
		buffer.pos(-off, 1.75 + height, -0.25).tex(minU, -height * 2 * scaleFactor).endVertex();
		buffer.pos(-off, 1.75 + height, 0.25).tex(maxU, -height * 2 * scaleFactor).endVertex();
		buffer.pos(-off, 1.75, 0.25).tex(maxU, 0).endVertex();

		buffer.pos(off, 1.75, -0.25).tex(maxU, 0).endVertex();
		buffer.pos(off, 1.75 + height, -0.25).tex(maxU, -height * 2 * scaleFactor).endVertex();
		buffer.pos(off, 1.75 + height, 0.25).tex(minU, -height * 2 * scaleFactor).endVertex();
		buffer.pos(off, 1.75, 0.25).tex(minU, 0).endVertex();

		tess.draw();

		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.enableCull();

		GlStateManager.popMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.machine_bigasstank);
	}

	@Override
	public ItemRenderBase getRenderer(Item item) {
		return new ItemRenderBase() {
			public void renderInventory() {
				GlStateManager.translate(0, -1, 0);
				GlStateManager.scale(2.5, 2.5, 2.5);
			}

			public void renderCommon() {
				GlStateManager.scale(0.5, 0.5, 0.5);
				GlStateManager.shadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.bigasstank_tex);
				ResourceManager.bigasstank.renderAll();
				GlStateManager.shadeModel(GL11.GL_FLAT);
			}
		};
	}
}
