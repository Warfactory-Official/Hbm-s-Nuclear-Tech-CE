package com.hbm.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class SlotPattern extends Slot {

    protected boolean canHover = true;

    protected boolean allowStackSize = false;

    public SlotPattern(IInventory inv, int index, int x, int y) {
        super(inv, index, x, y);
    }

    public SlotPattern allowStackSize() {
        this.allowStackSize = true;
        return this;
    }

    @Override
    public boolean canTakeStack(@NotNull EntityPlayer player) {
        return false;
    }

    @Override
    public int getSlotStackLimit() {
        return allowStackSize ? 64 : 1;
    }

    @Override
    public void putStack(@NotNull ItemStack stack) {
        if(!stack.isEmpty()) {
            stack = stack.copy();

            if(!allowStackSize)
                stack.setCount(1);
        }
        super.putStack(stack);
    }

    public SlotPattern disableHover() {
        this.canHover = false;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isEnabled() {
        return canHover;
    }
}
