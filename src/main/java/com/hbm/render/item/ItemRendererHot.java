package com.hbm.render.item;

import com.hbm.Tags;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.model.BakedModelTransforms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
@AutoRegister(item = "ingot_steel_dusted")
@AutoRegister(item = "ingot_chainsteel")
@AutoRegister(item = "ingot_meteorite")
@AutoRegister(item = "ingot_meteorite_forged")
@AutoRegister(item = "blade_meteorite")
public class ItemRendererHot extends TEISRBase {

	@Override
	public ModelBinding createModelBinding(Item item) {
		return ModelBinding.of(new ModelResourceLocation(new ResourceLocation(Tags.MODID, "items/" + item.getRegistryName().getPath()), "inventory"), BakedModelTransforms.defaultItemTransforms(), false);
	}

	@Override
	public void renderByItem(ItemStack stack) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.5, 0.5, 0);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		IBakedModel resolvedModel = itemModel.getOverrides().handleItemState(itemModel, stack, world, entity);
		Minecraft.getMinecraft().getRenderItem().renderItem(stack, resolvedModel);
		GlStateManager.popMatrix();
	}
}
