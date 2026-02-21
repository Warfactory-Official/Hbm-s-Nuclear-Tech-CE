package com.hbm.hazard.transformer;

import com.hbm.blocks.generic.BlockStorageCrate;
import com.hbm.hazard.HazardEntry;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.items.ModItems;
import com.hbm.util.BobMathUtil;
import com.hbm.util.ItemStackUtil;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import java.util.List;

public class HazardTransformerRadiationContainer implements IHazardTransformer {

	@Override
	public void transformPre(final ItemStack stack, final List<HazardEntry> entries) { }

	@Override
	public void transformPost(final ItemStack stack, final List<HazardEntry> entries) {
		
		final boolean isCrate = Block.getBlockFromItem(stack.getItem()) instanceof BlockStorageCrate;
		final boolean isBox = stack.getItem() == ModItems.containment_box;
		boolean isBag = stack.getItem() == ModItems.plastic_bag;

		if(!isCrate && !isBox && !isBag) return;
		if(!isCrate && !isBox) return;
		if(!stack.hasTagCompound()) return;

        double radiation = 0D;
		
		if(isCrate) {
            radiation = stack.getTagCompound().getDouble(BlockStorageCrate.CRATE_RAD_KEY);
		}
		
		if(isBox) {

			//ItemStack[] fromNBT = ItemStackUtil.readStacksFromNBT(stack, 20);
			final ItemStack[] fromNBT = ItemStackUtil.readStacksFromNBT(stack);
			if(fromNBT == null) return;

            for (final ItemStack held : fromNBT) {
                if (held != null) {
                    radiation += HazardSystem.getHazardLevelFromStack(held, HazardRegistry.RADIATION) * held.getCount();
                }
            }

            radiation = BobMathUtil.sqrt(radiation);
        }
		
		if(isBag) {

			final ItemStack[] fromNBT = ItemStackUtil.readStacksFromNBT(stack);
			if(fromNBT == null) return;

            for (ItemStack held : fromNBT) {
                if (held != null) {
                    radiation += HazardSystem.getHazardLevelFromStack(held, HazardRegistry.RADIATION) * held.getCount();
                }
            }

            radiation *= 2D;
        }

        if (radiation > 0) {
            entries.add(new HazardEntry(HazardRegistry.RADIATION, radiation));
        }
    }
}
