package com.hbm.inventory.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class SlotPattern extends SlotItemHandler {

    protected boolean canHover = true;
    protected boolean allowStackSize = false;

    public SlotPattern(IItemHandler inv, int index, int x, int y) {
        super(inv, index, x, y);
    }

    public SlotPattern(IItemHandler inv, int index, int x, int y, boolean allowStackSize) {
        super(inv, index, x, y);
        this.allowStackSize = allowStackSize;
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return false;
    }

    @Override
    public void putStack(@NotNull ItemStack stack) {
        if (!stack.isEmpty()) {
            stack = stack.copy();

            if (!allowStackSize)
                stack.setCount(1);
        }
        super.putStack(stack);
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }

    @Override
    public int getItemStackLimit(@NotNull ItemStack stack) {
        return 1;
    }

    public SlotPattern disableHover() {
        this.canHover = false;
        return this;
    }
//
//    @Override
//    public boolean isItemValid(ItemStack stack) {
//        return stack != null;
//    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isEnabled() {
        return canHover;
    }
}
