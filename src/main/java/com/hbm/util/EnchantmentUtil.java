package com.hbm.util;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

public class EnchantmentUtil {

	public static void addEnchantment(ItemStack stack, Enchantment enchantment, int level) {

		stack.addEnchantment(enchantment, level);
	}

	public static void removeEnchantment(ItemStack stack, Enchantment enchantment) {
		if(stack.getEnchantmentTagList() == null || !stack.hasTagCompound())
			return;
		
		int i = 0;
		for( ; i < stack.getEnchantmentTagList().tagCount(); i++) {
			if(stack.getEnchantmentTagList().getCompoundTagAt(i).getShort("id") == Enchantment.getEnchantmentID(enchantment))
				break;
		}

		if(i < stack.getEnchantmentTagList().tagCount())
			stack.getEnchantmentTagList().removeTag(i);

		if(stack.getEnchantmentTagList().tagCount() == 0)
			stack.getTagCompound().removeTag("ench");
	}

	/**
	 * Removes an amount of experience from a player and updates their level
	 * @param entityPlayer the player to remove experience from
	 * @param amount the amount of experience to remove
	 */
	public static void removeExperience(EntityPlayer entityPlayer, float amount) {
		if (entityPlayer.experienceTotal < amount) {
			entityPlayer.experienceLevel = 0;
			entityPlayer.experience = 0;
			entityPlayer.experienceTotal = 0;
			return;
		}

		entityPlayer.experienceTotal -= amount;
		if (entityPlayer.experience * (float)entityPlayer.xpBarCap() < amount) {
			amount -= entityPlayer.experience * (float)entityPlayer.xpBarCap();
			entityPlayer.experience = 1.0f;
			entityPlayer.experienceLevel--;
		}

		while (entityPlayer.xpBarCap() < amount) {
			amount -= entityPlayer.xpBarCap();
			entityPlayer.experienceLevel--;
		}
		entityPlayer.experience -= amount / (float)entityPlayer.xpBarCap();
	}
	public static int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
		if (stack.isEmpty() || enchantment == null)
			return 0;

		NBTTagList list = stack.getEnchantmentTagList();
		if (list == null)
			return 0;

		ResourceLocation targetId = Enchantment.REGISTRY.getNameForObject(enchantment);

		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			Enchantment ench = Enchantment.getEnchantmentByID(tag.getShort("id"));
			if (ench != null && Enchantment.REGISTRY.getNameForObject(ench).equals(targetId))
				return tag.getShort("lvl");
		}

		return 0;
	}

}