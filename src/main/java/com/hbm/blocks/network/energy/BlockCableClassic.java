package com.hbm.blocks.network.energy;

import com.hbm.render.model.BlockCableClassicBakedModel;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCableClassic extends BlockCable {

	public BlockCableClassic(Material material, String registryName, String texture) {
		super(material, registryName, texture);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void bakeModel(ModelBakeEvent event) {
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(textureLocation.toString());

		IBakedModel blockModel = BlockCableClassicBakedModel.forBlock(sprite);
		IBakedModel itemModel = BlockCableClassicBakedModel.forItem(sprite);

		event.getModelRegistry().putObject(new ModelResourceLocation(getRegistryName(), "normal"), blockModel);
		event.getModelRegistry().putObject(new ModelResourceLocation(getRegistryName(), "inventory"), itemModel);
	}
}
