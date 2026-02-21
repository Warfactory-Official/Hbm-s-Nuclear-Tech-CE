package com.hbm.items.food;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.items.IDynamicModels;
import com.hbm.items.ModItems;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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

public class ItemTemFlakes extends ItemFood implements IDynamicModels {
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
		try {
			IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "item/generated"));
			ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID, ROOT_PATH + textureName);
			IModel retexturedModel = baseModel.retexture(ImmutableMap.of("layer0", spriteLoc.toString()));
			IBakedModel bakedModel = retexturedModel.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
			ModelResourceLocation bakedModelLocation = new ModelResourceLocation(spriteLoc, "inventory");
			event.getModelRegistry().putObject(bakedModelLocation, bakedModel);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
}
