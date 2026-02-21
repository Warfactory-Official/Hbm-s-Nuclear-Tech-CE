package com.hbm.hazard.modifier;

import com.hbm.items.machine.ItemRTGPellet;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class HazardModifierRTGRadiation implements IHazardModifier {

    double target;
			
	public HazardModifierRTGRadiation(final double target) {
			this.target = target;
	}

	@Override
    public double modify(final ItemStack stack, final EntityLivingBase holder, double level) {
				
		if(stack.getItem() instanceof ItemRTGPellet fuel) {
            final double depletion = fuel.getDurabilityForDisplay(stack);

            level = (level + (this.target - level) * depletion);
					
		}
				
		return level;
	}
	
}
