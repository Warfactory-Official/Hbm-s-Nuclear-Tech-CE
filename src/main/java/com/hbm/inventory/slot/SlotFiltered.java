package com.hbm.inventory.slot;

import net.minecraft.item.ItemStack;
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


    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        boolean isValid = filter.test(stack);
        return isValid;
    }


    public Predicate<ItemStack> getFilter() {
        return this.filter;
    }
}


