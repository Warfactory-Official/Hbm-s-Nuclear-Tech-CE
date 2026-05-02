package com.hbm.items.food;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.items.ClaimedModelLocationRegistry;
import com.hbm.items.IClaimedModelLocation;
import com.hbm.items.IDynamicModels;
import com.hbm.items.ModItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemSoup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.hbm.items.ItemEnumMulti.ROOT_PATH;

public class ItemFoodSoup extends ItemSoup implements IDynamicModels, IClaimedModelLocation {
	private final String texturePath;
	public ItemFoodSoup(int i, String s) {
		super(i);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		texturePath = s;
		
		ModItems.ALL_ITEMS.add(this);
		IDynamicModels.INSTANCES.add(this);
        ClaimedModelLocationRegistry.register(this);
	}

	@Override
	public void bakeModel(ModelBakeEvent event) {
	}


	@Override
	public void registerModel() {
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(new ResourceLocation(Tags.MODID, ROOT_PATH + texturePath), "inventory"));
	}

	@Override
	public void registerSprite(TextureMap map) {
		map.registerSprite(new ResourceLocation(Tags.MODID, ROOT_PATH + texturePath));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean ownsModelLocation(ModelResourceLocation location) {
		return IClaimedModelLocation.isInventoryLocation(location, new ResourceLocation(Tags.MODID, ROOT_PATH + texturePath));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IModel loadModel(ModelResourceLocation location) {
		try {
			IModel generated = ModelLoaderRegistry.getModel(new ResourceLocation("item/generated"));
			return generated.retexture(ImmutableMap.of("layer0", new ResourceLocation(Tags.MODID, ROOT_PATH + texturePath).toString()));
		} catch (Exception e) {
			return IClaimedModelLocation.super.loadModel(location);
		}
	}
}
