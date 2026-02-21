package com.hbm.hazard.transformer;

import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import com.hbm.hazard.HazardEntry;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.util.Compat;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

import java.util.List;

public class HazardTransformerRadiationME implements IHazardTransformer {
    private static final boolean ae2Loaded = Loader.isModLoaded(Compat.ModIds.AE2);

	@Override
	public void transformPre(final ItemStack stack, final List<HazardEntry> entries) { }

	@Override
	public void transformPost(final ItemStack stack, final List<HazardEntry> entries) {
		if(!ae2Loaded) return;
        if(stack.getItem() instanceof IStorageCell<?> storageCell && storageCell.getChannel() instanceof IItemStorageChannel) {
			final IItemList<IAEItemStack> stacks = Compat.scrapeItemFromME(stack);
            if (stacks == null) return;
            double radiation = 0D;

            for (IAEItemStack held : stacks) {
                radiation += HazardSystem.getHazardLevelFromStack(held.asItemStackRepresentation(), HazardRegistry.RADIATION) * held.getStackSize();
            }

            if (radiation > 0) {
                entries.add(new HazardEntry(HazardRegistry.RADIATION, radiation));
            }
		}
	}
}
