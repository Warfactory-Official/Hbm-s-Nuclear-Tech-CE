package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.pile.BlockPileDevice;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.pile.TileEntityPileLoader;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderPileLoader extends TileEntitySpecialRenderer<TileEntityPileLoader> implements IItemRendererProvider {

	@Override
	public void render(@NotNull TileEntityPileLoader loader, double x, double y, double z, float interp, int destroyStage, float alpha) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y, z + 0.5);
		GlStateManager.enableLighting();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		switch(loader.getBlockMetadata() % 4) {
		case 0: GlStateManager.rotate(90, 0, 1, 0); break;
		case 1: GlStateManager.rotate(270, 0, 1, 0); break;
		case 2: GlStateManager.rotate(180, 0, 1, 0); break;
		case 3: GlStateManager.rotate(0, 0, 1, 0); break;
		}

		double position = loader.lastLevel + (loader.level - loader.lastLevel) * interp;

		bindTexture(ResourceManager.pile_loader_tex);
		ResourceManager.pile_loader.renderPart("Loader");

		GlStateManager.pushMatrix();
		GlStateManager.translate(-0.1875, 0.5, 0);
		GlStateManager.rotate((float) (position * 90), 0, 0, 1);
		GlStateManager.translate(0.1875, -0.5, 0);
		ResourceManager.pile_loader.renderPart("Lever");
		GlStateManager.popMatrix();

		GlStateManager.translate(position * -0.5, 0, 0);
		ResourceManager.pile_loader.renderPart("Slider");
		if(!loader.syncStack.isEmpty()) ResourceManager.pile_loader.renderPart("Rod");

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.popMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.pile_device);
	}

	@Override
	public ItemRenderBase getRenderer(Item item) {

		return new ItemRenderBase() {

			public void renderInventory() {
				GlStateManager.translate(0, -3.5, 0);
				GlStateManager.scale(5, 5, 5);
			}

			public void renderCommon(ItemStack stack) {
				GlStateManager.scale(2, 2, 2);
				GlStateManager.shadeModel(GL11.GL_SMOOTH);
				if(stack.getItemDamage() == BlockPileDevice.ITEM_META_LOADER) {
					bindTexture(ResourceManager.pile_loader_tex);
					ResourceManager.pile_loader.renderAll();
				}
				if(stack.getItemDamage() == BlockPileDevice.ITEM_META_VENT) {
					bindTexture(ResourceManager.pile_vent_tex);
					ResourceManager.pile_vent.renderAll();
				}
				if(stack.getItemDamage() == BlockPileDevice.ITEM_META_CONTROL) {
					bindTexture(ResourceManager.pile_control_tex);
					ResourceManager.pile_control.renderAll();
				}
				GlStateManager.shadeModel(GL11.GL_FLAT);
			}
		};
	}
}
