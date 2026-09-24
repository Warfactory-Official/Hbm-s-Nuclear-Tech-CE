package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineBlastFurnace;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;

@AutoRegister
public class RenderBlastFurnace extends TileEntitySpecialRenderer<TileEntityMachineBlastFurnace> implements IItemRendererProvider {

	@Override
	public void render(TileEntityMachineBlastFurnace furnace, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y, z + 0.5);
		GlStateManager.enableLighting();
		GlStateManager.disableCull();

		if(furnace.tilted) {
			GlStateManager.translate(0, -0.25, 0);
			GlStateManager.rotate(10, 0, 0, 1);
			GlStateManager.rotate(5, 0, 1, 0);
		}

		switch(furnace.getBlockMetadata() - BlockDummyable.offset) {
		case 2: GlStateManager.rotate(90, 0F, 1F, 0F); break;
		case 4: GlStateManager.rotate(180, 0F, 1F, 0F); break;
		case 3: GlStateManager.rotate(270, 0F, 1F, 0F); break;
		case 5: GlStateManager.rotate(0, 0F, 1F, 0F); break;
		}

		bindTexture(ResourceManager.blast_furnace_tex);
		ResourceManager.blast_furnace.renderAll();

		GlStateManager.enableCull();
		GlStateManager.popMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.machine_blast_furnace);
	}

	@Override
	public ItemRenderBase getRenderer(Item item) {
		return new ItemRenderBase() {
			public void renderInventory() {
				GlStateManager.translate(0, -4.5, 0);
				GlStateManager.scale(4, 4, 4);
			}

			public void renderCommon() {
				GlStateManager.scale(0.5, 0.5, 0.5);
				bindTexture(ResourceManager.blast_furnace_tex);
				ResourceManager.blast_furnace.renderAll();
			}
		};
	}
}
