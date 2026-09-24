package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.network.energy.TileEntityPylon;
import com.hbm.tileentity.network.energy.TileEntityPylonBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@AutoRegister(tileentity = TileEntityPylon.class)
public class RenderPylon extends RenderPylonBase implements IItemRendererProvider {

	@Override
	public void render(TileEntityPylonBase pyl, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if(!(pyl instanceof TileEntityPylon tepylon)) return;
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y, z + 0.5);
		GlStateManager.enableLighting();
		GlStateManager.disableCull();

		if(pyl.getBlockType() == ModBlocks.red_pylon) {
			bindTexture(ResourceManager.pylon_tex);
			ResourceManager.pylon.renderPart("Pylon");
		} else {
			bindTexture(ResourceManager.pylon_steel_tex);
			ResourceManager.pylon.renderPart("Pylon_steel");
		}

		GlStateManager.enableCull();
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		this.renderLinesGeneric(tepylon, x, y, z);
		GlStateManager.popMatrix();
	}

	@Override
	public Item[] getItemsForRenderer() {
		return new Item[] {
				Item.getItemFromBlock(ModBlocks.red_pylon),
				Item.getItemFromBlock(ModBlocks.red_pylon_steel_small)
		};
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.red_pylon);
	}

	@Override
	public ItemRenderBase getRenderer(Item item) {
		return new ItemRenderBase() {
			public void renderInventory() {
				GlStateManager.translate(0, -5, 0);
				GlStateManager.scale(2.9, 2.9, 2.9);
			}

			public void renderCommon(ItemStack stack) {
				GlStateManager.disableCull();
				if(stack.getItem() == Item.getItemFromBlock(ModBlocks.red_pylon)) {
					bindTexture(ResourceManager.pylon_tex);
					ResourceManager.pylon.renderPart("Pylon");
				} else {
					bindTexture(ResourceManager.pylon_steel_tex);
					ResourceManager.pylon.renderPart("Pylon_steel");
				}
				GlStateManager.enableCull();
			}
		};
	}
}
