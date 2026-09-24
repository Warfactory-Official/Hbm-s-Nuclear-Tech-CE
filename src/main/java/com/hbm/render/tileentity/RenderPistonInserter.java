package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.PistonInserter.TileEntityPistonInserter;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

@AutoRegister
public class RenderPistonInserter extends TileEntitySpecialRenderer<TileEntityPistonInserter> implements IItemRendererProvider {

	@Override
	public void render(TileEntityPistonInserter piston, double x, double y, double z, float interp, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
		GlStateManager.enableLighting();
		GlStateManager.enableCull();

		switch(piston.getBlockMetadata()) {
		case 0: GlStateManager.rotate(180, 1F, 0F, 0F); break;
		case 1: break;
		case 2: GlStateManager.rotate(-90, 1F, 0F, 0F);
				GlStateManager.rotate(180, 0F, 1F, 0F); break;
		case 4: GlStateManager.rotate(90, 0F, 0F, 1F);
				GlStateManager.rotate(-90, 0F, 1F, 0F); break;
		case 3: GlStateManager.rotate(90, 1F, 0F, 0F); break;
		case 5: GlStateManager.rotate(-90, 0F, 0F, 1F);
				GlStateManager.rotate(90, 0F, 1F, 0F); break;
		}

		GlStateManager.translate(0D, -0.5, 0D);

		bindTexture(ResourceManager.piston_inserter_tex);
		ResourceManager.piston_inserter.renderPart("Frame");

		double e = (piston.lastExtend + (piston.renderExtend - piston.lastExtend) * interp) / (double) TileEntityPistonInserter.maxExtend;
		GlStateManager.translate(0, e * 0.9375D, 0);
		ResourceManager.piston_inserter.renderPart("Piston");

		if(!piston.slot.isEmpty()) {
			ItemStack stack = piston.slot.copy();
			stack.setCount(1);

			if(stack.getItem() instanceof ItemBlock) {
				GlStateManager.translate(0.0D, 1.125D, 0.0D);
			} else {
				GlStateManager.translate(0.0D, 1.0625D, 0.1D);
				GlStateManager.rotate(90, -1, 0, 0);
			}

			Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
		}

		GlStateManager.popMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.piston_inserter);
	}

	@Override
	public ItemRenderBase getRenderer(Item item) {
		return new ItemRenderBase() {
			public void renderInventory() {
				GlStateManager.translate(0, -2.5, 0);
				double scale = 5;
				GlStateManager.scale(scale, scale, scale);
			}
			public void renderCommon(ItemStack stack) {
				GlStateManager.scale(2, 2, 2);
				bindTexture(ResourceManager.piston_inserter_tex);
				ResourceManager.piston_inserter.renderAll();
			}};
	}
}
