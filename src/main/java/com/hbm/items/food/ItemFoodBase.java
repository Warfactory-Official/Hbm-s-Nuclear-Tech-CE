package com.hbm.items.food;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.items.IDynamicModels;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.potion.HbmPotion;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
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

public class ItemFoodBase extends ItemFood implements IDynamicModels {
	String texturePath;

	public ItemFoodBase(int amount, float saturation, boolean isWolfFood, String s){
		super(amount, saturation, isWolfFood);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.texturePath = s;
		INSTANCES.add(this);

		ModItems.ALL_ITEMS.add(this);
	}
	public ItemFoodBase(int amount, float saturation, boolean isWolfFood, String s, String texturePath){
		super(amount, saturation, isWolfFood);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.texturePath = texturePath;
		INSTANCES.add(this);

		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public void bakeModel(ModelBakeEvent event) {
		try {
			IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "item/generated"));
			ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID, ROOT_PATH + texturePath);
			IModel retexturedModel = baseModel.retexture(
					ImmutableMap.of(
							"layer0", spriteLoc.toString()
					)

			);
			IBakedModel bakedModel = retexturedModel.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
			ModelResourceLocation bakedModelLocation = new ModelResourceLocation(spriteLoc, "inventory");
			event.getModelRegistry().putObject(bakedModelLocation, bakedModel);

		} catch (Exception e) {
			e.printStackTrace();
		}
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
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn){
		if(this == ModItems.bomb_waffle) {
			list.add("60s of Insanity");
			list.add("§4[DEMON CORE]§r");
		}
		if(this == ModItems.cotton_candy) {
			list.add("Gives you a radioactive sugarshock");
			list.add("§b[SPEED V]§r");
		}
		if(this == ModItems.schnitzel_vegan) {
			list.add("Wasteschnitzel is all i need.");
			list.add("§c[STRENGTH X]§r");
		}
		super.addInformation(stack, world, list, flagIn);
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
		if(stack.getItem() == ModItems.bomb_waffle){
			player.setFire(60 * 20);
			player.motionY = -2;
			player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 60 * 20, 20));
			player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 60 * 20, 10));
			player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 60 * 20, 20));
			player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 60 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 60 * 20, 10));
			player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 60 * 20, 10));
			worldIn.spawnEntity(EntityNukeExplosionMK5.statFac(worldIn, (int)(BombConfig.fatmanRadius * 1.5), player.posX, player.posY, player.posZ).setDetonator(player));
			EntityNukeTorex.statFac(worldIn, player.posX, player.posY, player.posZ, (int)(BombConfig.fatmanRadius * 1.5));
		}
		if(stack.getItem() == ModItems.cotton_candy){
			player.addPotionEffect(new PotionEffect(MobEffects.WITHER, 5 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.POISON, 15 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 25 * 20, 2));
			player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 25 * 20, 5));
			player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 25 * 20, 5));
		}
		if(stack.getItem() == ModItems.schnitzel_vegan){
			player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 10 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 3 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 3 * 60 * 20, 4));
			player.addPotionEffect(new PotionEffect(MobEffects.WITHER, 3 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 30 * 20, 10));

			player.setFire(5 * 20);
			player.motionY = 2;
		}
	}
}
