package com.hbm.inventory.slot;

import com.hbm.lib.Library;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
// Th3_Sl1ze: fuck it, I'll make a SlotBattery to avoid accidential shift-clicking there
// yes I KNOW I can fix that in transferStackInSlot, but that's a more reliable solution.
// also it will ban people from inserting shit into battery slot on LMB
public class SlotBattery extends SlotItemHandler {

    public SlotBattery(IItemHandler inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(@NotNull ItemStack stack) {
        return !stack.isEmpty() && Library.isBattery(stack);
    }

    @Override
    public void onSlotChange(ItemStack sta1, ItemStack sta2) {
        super.onSlotChange(sta1, sta2);
    }
}
