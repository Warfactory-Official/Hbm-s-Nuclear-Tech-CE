package com.hbm.inventory.slot;

import com.hbm.items.machine.IItemFluidIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public class SlotFiltered extends SlotItemHandler {

    private final Predicate<ItemStack> filter;


    private SlotFiltered(IItemHandler itemHandler, int index, int x, int y, Predicate<ItemStack> filter) {
        super(itemHandler, index, x, y);
        this.filter = filter;
    }


    /**
     * Creates a slot that ONLY accepts items matching the predicate.
     * * @param whitelist Predicate returning true for items to ALLOW.
     */
    public static SlotFiltered withWhitelist(IItemHandler itemHandler, int index, int x, int y,
                                             Predicate<ItemStack> whitelist) {
        return new SlotFiltered(itemHandler, index, x, y, whitelist);
    }

    /**
     * Creates a slot that accepts anything EXCEPT items matching the predicate.
     * * @param blacklist Predicate returning true for items to REJECT.
     */
    public static SlotFiltered withBlacklist(IItemHandler itemHandler, int index, int x, int y,
                                             Predicate<ItemStack> blacklist) {
        return new SlotFiltered(itemHandler, index, x, y, blacklist.negate());
    }
    public static SlotFiltered fluidTypeSlot(IItemHandler itemHandler, int index, int x, int y
                                             ) {
        return new SlotFiltered(itemHandler, index, x, y, itemStack -> itemStack.getItem() instanceof IItemFluidIdentifier);
    }
    public static SlotFiltered takeOnly(IItemHandler itemHandler, int index, int x, int y
                                             ) {
        return new SlotFiltered(itemHandler, index, x, y, _ -> false);
    }
    public static SlotFiltered withCapability(IItemHandler itemHandler, int index, int x, int y,
                                             Capability<?>capability) {
        return new SlotFiltered(itemHandler, index, x, y, itemStack -> itemStack.getCapability(capability,null) != null);
    }

    public static SlotFiltered fluidHandlerSlot(IItemHandler itemHandler, int index, int x, int y) {
      return  withCapability(itemHandler, index, x, y, CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        boolean isValid = filter.test(stack);
        return isValid;
    }


    public Predicate<ItemStack> getFilter() {
        return this.filter;
    }
}


