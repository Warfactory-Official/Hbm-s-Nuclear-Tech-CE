package com.hbm.lib;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemStackHandlerWrapper implements IItemHandlerModifiable {
    protected final ItemStackHandler handle;
    private final int @Nullable [] validSlots;

    public ItemStackHandlerWrapper(@NotNull ItemStackHandler handle) {
        this.handle = handle;
        this.validSlots = null;
    }

    /**
     * @param validSlots null -> full access
     */
    public ItemStackHandlerWrapper(@NotNull ItemStackHandler handle, int @Nullable [] validSlots) {
        this.handle = handle;
        this.validSlots = validSlots;
    }

    @Override
    public int getSlots() {
        return handle.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return handle.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (validSlots == null) return handle.insertItem(slot, stack, simulate);
        for (int i : validSlots)
            if (i == slot) return handle.insertItem(slot, stack, simulate);
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (validSlots == null) return handle.extractItem(slot, amount, simulate);
        for (int i : validSlots)
            if (i == slot) return handle.extractItem(slot, amount, simulate);
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return handle.getSlotLimit(slot);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        handle.setStackInSlot(slot, stack);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (validSlots == null) return handle.isItemValid(slot, stack);
        for (int i : validSlots)
            if (i == slot) return handle.isItemValid(slot, stack);
        return false;
    }
}
