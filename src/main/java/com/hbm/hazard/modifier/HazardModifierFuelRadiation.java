package com.hbm.hazard.modifier;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class HazardModifierFuelRadiation implements IHazardModifier {

    double target;
	
	public HazardModifierFuelRadiation(final double target) {
		this.target = target;
	}

	@Override
    public double modify(final ItemStack stack, final EntityLivingBase holder, double level) {
		final double depletion = Math.pow(stack.getItem().getDurabilityForDisplay(stack), 0.4D);
        level = (level + (this.target - level) * depletion);
		
		return level;
	}
}
