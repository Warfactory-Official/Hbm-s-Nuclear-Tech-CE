package com.hbm.render.item;

import com.hbm.render.model.BakedModelTransforms;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemBlock;
import org.jetbrains.annotations.Nullable;

public class ItemRenderBase extends TEISRBase {

    protected ItemCameraTransforms getBindingTransforms(Item item) {
        if (item instanceof ItemBlock || item instanceof ItemArmor) {
            return ItemCameraTransforms.DEFAULT;
        }
        return BakedModelTransforms.defaultItemTransforms();
    }

    @Override
    public ModelBinding createModelBinding(Item item) {
        if (item instanceof ItemArmor) {
            return ModelBinding.of(new ModelResourceLocation(item.getRegistryName(), "inventory"), ItemCameraTransforms.DEFAULT, false);
        }
        return ModelBinding.inventory(item, getBindingTransforms(item));
    }

	@Override
	//Norwood: nowhere in the source of MC in context of rendering, itemstack is considered non-null
	public void renderByItem(@Nullable ItemStack itemStackIn) {
		if (this.type == null || itemStackIn == null) {
			this.type = TransformType.NONE;
		}

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
		this.type = null;
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
