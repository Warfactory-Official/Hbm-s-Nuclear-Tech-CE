package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityGeiger;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderGeiger extends TileEntitySpecialRenderer<TileEntityGeiger> implements IItemRendererProvider {
	
	@Override
	public void render(TileEntityGeiger te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x + 0.5F, (float) y, (float) z + 0.5F);
		GlStateManager.enableLighting();
		GlStateManager.disableCull();
		switch (te.getBlockMetadata()) {
			case 2 -> GlStateManager.rotate(0, 0F, 1F, 0F);
			case 4 -> GlStateManager.rotate(90, 0F, 1F, 0F);
			case 3 -> GlStateManager.rotate(180, 0F, 1F, 0F);
			case 5 -> GlStateManager.rotate(270, 0F, 1F, 0F);
		}
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		bindTexture(ResourceManager.geiger_tex);
		ResourceManager.geiger.renderAll();
		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.enableCull();
		GlStateManager.popMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.geiger);
	}

	@Override
	public ItemRenderBase getRenderer(Item item) {
		return new ItemRenderBase() {
			public void renderInventory() {
				GlStateManager.scale(10, 10, 10);
			}

			public void renderCommon() {
				GlStateManager.translate(0.2F, 0, 0);
				GlStateManager.rotate(90, 0, 1, 0);
				GlStateManager.shadeModel(GL11.GL_SMOOTH);
				GlStateManager.disableCull();
				bindTexture(ResourceManager.geiger_tex);
				ResourceManager.geiger.renderAll();
				GlStateManager.enableCull();
				GlStateManager.shadeModel(GL11.GL_FLAT);
			}
		};
	}
}
