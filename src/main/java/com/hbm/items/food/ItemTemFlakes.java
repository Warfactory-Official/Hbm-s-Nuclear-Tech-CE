package com.hbm.items.food;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.items.ClaimedModelLocationRegistry;
import com.hbm.items.IClaimedModelLocation;
import com.hbm.items.IDynamicModels;
import com.hbm.items.ModItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import static com.hbm.items.ItemEnumMulti.ROOT_PATH;

public class ItemTemFlakes extends ItemFood implements IDynamicModels, IClaimedModelLocation {
	private final String textureName;
	public ItemTemFlakes(int amount, float saturation, boolean isWolfFood, String s) {
		super(amount, saturation, isWolfFood);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		textureName = s;
		this.setHasSubtypes(true);
		this.setAlwaysEdible();

		ModItems.ALL_ITEMS.add(this);
		IDynamicModels.INSTANCES.add(this);
        ClaimedModelLocationRegistry.register(this);
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
		player.heal(2.0F);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if(stack.getItemDamage() == 0)
		{
			tooltip.add("Heals 2HP DISCOUNT FOOD OF TEM!!!");
		}
		if(stack.getItemDamage() == 1)
		{
			tooltip.add("Heals 2HP food of tem");
		}
		if(stack.getItemDamage() == 2)
		{
			tooltip.add("Heals food of tem (expensiv)");
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void bakeModel(ModelBakeEvent event) {
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModel() {
		ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID, ROOT_PATH + textureName);
		ModelResourceLocation mrl = new ModelResourceLocation(spriteLoc, "inventory");
		ModelLoader.setCustomModelResourceLocation(this, 0, mrl);
		ModelLoader.setCustomModelResourceLocation(this, 1, mrl);
		ModelLoader.setCustomModelResourceLocation(this, 2, mrl);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerSprite(TextureMap map) {
		map.registerSprite(new ResourceLocation(Tags.MODID, ROOT_PATH + textureName));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean ownsModelLocation(ModelResourceLocation location) {
		return IClaimedModelLocation.isInventoryLocation(location, new ResourceLocation(Tags.MODID, ROOT_PATH + textureName));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IModel loadModel(ModelResourceLocation location) {
		try {
			IModel generated = ModelLoaderRegistry.getModel(new ResourceLocation("item/generated"));
			return generated.retexture(ImmutableMap.of("layer0", new ResourceLocation(Tags.MODID, ROOT_PATH + textureName).toString()));
		} catch (Exception e) {
			return IClaimedModelLocation.super.loadModel(location);
		}
	}
}
