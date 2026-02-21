package com.hbm.hazard.transformer;

import com.hbm.hazard.HazardEntry;
import net.minecraft.item.ItemStack;

import java.util.List;

//mlbv: made it an interface to be more flexible, was abstract class HazardTransformerBase
public interface IHazardTransformer {

	void transformPre(ItemStack stack, List<HazardEntry> entries);
	void transformPost(ItemStack stack, List<HazardEntry> entries);
}
