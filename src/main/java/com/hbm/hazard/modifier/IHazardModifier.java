package com.hbm.hazard.modifier;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import java.util.List;

//mlbv: made it an interface to be more flexible. was abstract class HazardModifier
public interface IHazardModifier {

    double modify(ItemStack stack, EntityLivingBase holder, double level);
	
	/**
	 * Returns the level after applying all modifiers to it, in order.
	 * @param stack
	 * @param entity nullable
	 * @param level
	 * @param mods
	 * @return
	 */
    static double evalAllModifiers(final ItemStack stack, final EntityLivingBase entity, double level, final List<IHazardModifier> mods) {
		for(final IHazardModifier mod : mods) {
			level = mod.modify(stack, entity, level);
		}
		return level;
	}
}
