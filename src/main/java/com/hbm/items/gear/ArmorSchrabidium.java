package com.hbm.items.gear;

import com.hbm.Tags;
import com.hbm.items.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;

public class ArmorSchrabidium extends ItemArmor implements ISpecialArmor {

	public ArmorSchrabidium(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn, String s) {
		super(materialIn, renderIndexIn, equipmentSlotIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(CreativeTabs.COMBAT);
		
		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		if(stack.getItem().equals(ModItems.schrabidium_helmet) || stack.getItem().equals(ModItems.schrabidium_plate) || stack.getItem().equals(ModItems.schrabidium_boots)) {
			return (Tags.MODID + ":textures/armor/schrabidium_1.png");
		}
		if(stack.getItem().equals(ModItems.schrabidium_legs)) {
			return (Tags.MODID + ":textures/armor/schrabidium_2.png");
		}
		return null;
	}
	
	@Override
	public EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.RARE;
	}

	@Override
	public ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot) {
		if(damage >= 20)
		{
			player.setHealth(player.getHealth() - 1F);
			return new ArmorProperties(1, 1, 2000);
		}
		return new ArmorProperties(1, 1, 2000);
	}

	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
		if(slot == 0)
		{
			return 3;
		}
		if(slot == 1)
		{
			return 8;
		}
		if(slot == 2)
		{
			return 6;
		}
		if(slot == 3)
		{
			return 3;
		}
		return 0;
	}

	@Override
	public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {
		stack.damageItem(damage * 1, entity);
	}
	
	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack armor) {
		if(armor.getItem() == ModItems.schrabidium_helmet)
		 {
			 player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 5, 0, true, false));
			 player.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 5, 9, true, false));
		 }
		 
		 if(armor.getItem() == ModItems.schrabidium_plate)
		 {
			 player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 5, 0, true, false));
		 }
		 
		 if(armor.getItem() == ModItems.schrabidium_legs)
		 {
			 player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 5, 2, true, false));
		 }
		 
		 if(armor.getItem() == ModItems.schrabidium_boots)
		 {
			 player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 5, 2, true, false));
		 }
	}
	
}
