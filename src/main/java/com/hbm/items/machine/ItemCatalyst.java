package com.hbm.items.machine;

import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class ItemCatalyst extends Item {

	int color;
	long powerAbs;
	float powerMod;
	float heatMod;
	float fuelMod;
	
	public ItemCatalyst(int color, String s) {
		this.color = color;
		this.powerAbs = 0;
		this.powerMod = 1.0F;
		this.heatMod = 1.0F;
		this.fuelMod = 1.0F;
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	public ItemCatalyst(int color, long powerAbs, float powerMod, float heatMod, float fuelMod, String s) {
		this.color = color;
		this.powerAbs = powerAbs;
		this.powerMod = powerMod;
		this.heatMod = heatMod;
		this.fuelMod = fuelMod;
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	public int getColor() {
		return this.color;
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("Adds spice to the core.");
        tooltip.add("Look at all those colors!");
    }
	
	public static long getPowerAbs(ItemStack stack) {
		if(stack == null || !(stack.getItem() instanceof ItemCatalyst))
			return 0;
		return ((ItemCatalyst)stack.getItem()).powerAbs;
	}
	
	public static float getPowerMod(ItemStack stack) {
		if(stack == null || !(stack.getItem() instanceof ItemCatalyst))
			return 1F;
		return ((ItemCatalyst)stack.getItem()).powerMod;
	}
	
	public static float getHeatMod(ItemStack stack) {
		if(stack == null || !(stack.getItem() instanceof ItemCatalyst))
			return 1F;
		return ((ItemCatalyst)stack.getItem()).heatMod;
	}
	
	public static float getFuelMod(ItemStack stack) {
		if(stack == null || !(stack.getItem() instanceof ItemCatalyst))
			return 1F;
		return ((ItemCatalyst)stack.getItem()).fuelMod;
	}
}
