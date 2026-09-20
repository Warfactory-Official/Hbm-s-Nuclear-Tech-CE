package com.hbm.items.machine;

import com.hbm.items.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemFuelRod extends Item {

	public int lifeTime;

	public ItemFuelRod(int life, String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.lifeTime = life;
		this.canRepair = false;

		ModItems.ALL_ITEMS.add(this);
	}

	public static void setLifeTime(ItemStack stack, int time) {

		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("life", time);
	}

	public static int getLifeTime(ItemStack stack) {

		if(!stack.hasTagCompound()) return 0;

		return stack.getTagCompound().getInteger("life");
	}

	public boolean showDurabilityBar(ItemStack stack) {
		return getDurabilityForDisplay(stack) > 0D;
	}

	public double getDurabilityForDisplay(ItemStack stack) {
		return (double)getLifeTime(stack) / (double)((ItemFuelRod)stack.getItem()).lifeTime;
	}
}
