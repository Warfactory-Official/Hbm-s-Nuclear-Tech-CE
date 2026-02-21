package com.hbm.items.gear;

import com.hbm.Tags;
import com.hbm.handler.ArmorUtil;
import com.hbm.items.ModItems;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;

public class ArmorEuphemium extends ItemArmor implements ISpecialArmor {

	public ArmorEuphemium(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn, String s) {
		super(materialIn, renderIndexIn, equipmentSlotIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(null);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		if(stack.getItem().equals(ModItems.euphemium_helmet) || stack.getItem().equals(ModItems.euphemium_plate) || stack.getItem().equals(ModItems.euphemium_boots)) {
			return (Tags.MODID + ":textures/armor/euphemium_1.png");
		}
		if(stack.getItem().equals(ModItems.euphemium_legs)) {
			return (Tags.MODID + ":textures/armor/euphemium_2.png");
		}
		return null;
	}

	@Override
	public ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot) {
		if(player instanceof EntityPlayer && ArmorUtil.checkArmor((EntityPlayer)player, ModItems.euphemium_helmet, ModItems.euphemium_plate, ModItems.euphemium_legs, ModItems.euphemium_boots))
		{
			return new ArmorProperties(1, 1, MathHelper.floor(999999999));
		}
		return new ArmorProperties(0, 0, 0);
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
		stack.damageItem(damage * 0, entity);
	}
	
	@Override
	public EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.EPIC;
	}
	
	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
		if(player instanceof EntityPlayer && ArmorUtil.checkArmor(player, ModItems.euphemium_helmet, ModItems.euphemium_plate, ModItems.euphemium_legs, ModItems.euphemium_boots))
		{
			player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 5, 127, true, false));
			player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 5, 127, true, false));
			player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 5, 127, true, false));
			player.addPotionEffect(new PotionEffect(MobEffects.SATURATION, 5, 127, true, false));
		 
			if(player.motionY < -0.25D)
			{
				player.motionY = -0.25D;
				player.fallDistance = 0;
			}
			
		}
	}
	
	@Override
	public void setDamage(ItemStack stack, int damage) {}
	
	@Override
	public int getDamage(ItemStack stack) {
		return 0;
	}
	
	@Override
	public int getMaxDamage() {
		return Integer.MAX_VALUE;
	}
	
}
