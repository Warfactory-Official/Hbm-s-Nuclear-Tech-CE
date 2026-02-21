package com.hbm.render.item;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class ItemRenderBase extends TEISRBase {

	@Override
	public void renderByItem(ItemStack itemStackIn) {
		GlStateManager.pushMatrix();
		GlStateManager.enableCull();

		if (type != TransformType.GUI) GlStateManager.translate(0.5F, 0F, 0.5F);
		switch (type) {
			case FIRST_PERSON_RIGHT_HAND -> {
				GlStateManager.translate(0F, 0.3F, 0F);
				GlStateManager.scale(0.2F, 0.2F, 0.2F);
				GlStateManager.rotate(135F, 0F, 1F, 0F);
				renderNonInv(itemStackIn);
                renderFirstPersonRightHand();
			}
			case FIRST_PERSON_LEFT_HAND -> {
				GlStateManager.translate(0F, 0.3F, 0F);
				GlStateManager.scale(0.2F, 0.2F, 0.2F);
				GlStateManager.rotate(45F, 0F, 1F, 0F);
				renderNonInv(itemStackIn);
			}
			case THIRD_PERSON_RIGHT_HAND, HEAD -> {
				GlStateManager.translate(0F, 0.25F, 0F);
				GlStateManager.scale(0.1875F, 0.1875F, 0.1875F);
				GlStateManager.rotate(180F, 0F, 1F, 0F);
				renderNonInv(itemStackIn);
			}
			case THIRD_PERSON_LEFT_HAND -> {
				GlStateManager.translate(0F, 0.25F, 0F);
				GlStateManager.scale(0.1875F, 0.1875F, 0.1875F);
				renderNonInv(itemStackIn);
			}
			case GROUND -> {
				GlStateManager.translate(0F, 0.3F, 0F);
				GlStateManager.scale(0.125F, 0.125F, 0.125F);
				GlStateManager.rotate(90F, 0F, 1F, 0F);
				renderGround();
			}
			case FIXED -> {
				GlStateManager.translate(0F, 0.3F, 0F);
				GlStateManager.scale(0.25F, 0.25F, 0.25F);
				GlStateManager.rotate(90F, 0F, 1F, 0F);
				renderNonInv(itemStackIn);
			}
			case GUI -> {
				GlStateManager.enableLighting();
				GlStateManager.rotate(30F, 1F, 0F, 0F);
				GlStateManager.rotate(225F, 0F, 1F, 0F); // 45 + 180
				GlStateManager.scale(0.0620F, 0.0620F, 0.0620F);
				GlStateManager.translate(0F, 11.3F, -11.3F);
				renderInventory(itemStackIn);
			}
			case NONE -> {}
		}

		renderCommon(itemStackIn);
		GlStateManager.popMatrix();
	}

	public void renderNonInv(ItemStack stack) { renderNonInv(); }
	public void renderInventory(ItemStack stack) { renderInventory(); }
	public void renderCommon(ItemStack stack) { renderCommon(); }
	public void renderNonInv() { }
	public void renderInventory() { }
	public void renderCommon() { }
    public void renderGround() { }
    public void renderFirstPersonRightHand() { }
}
