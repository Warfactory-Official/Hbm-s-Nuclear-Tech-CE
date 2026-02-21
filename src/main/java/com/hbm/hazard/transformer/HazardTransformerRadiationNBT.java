package com.hbm.hazard.transformer;

import com.hbm.hazard.HazardEntry;
import com.hbm.hazard.HazardRegistry;
import net.minecraft.item.ItemStack;

import java.util.List;

public class HazardTransformerRadiationNBT implements IHazardTransformer {
	
	public static final String RAD_KEY = "hfrHazRadiation";

	@Override
	public void transformPre(final ItemStack stack, final List<HazardEntry> entries) { }

	@Override
	public void transformPost(final ItemStack stack, final List<HazardEntry> entries) {

        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(RAD_KEY)) {
            entries.add(new HazardEntry(HazardRegistry.RADIATION, stack.getTagCompound().getFloat(RAD_KEY)));
        }
	}

}
