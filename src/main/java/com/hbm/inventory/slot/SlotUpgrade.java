package com.hbm.inventory.slot;

import com.hbm.lib.Library;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class SlotUpgrade extends SlotItemHandler {

	public SlotUpgrade(IItemHandler inventory, int index, int xPosition, int yPosition) {
		super(inventory, index, xPosition, yPosition);
	}

	@Override
	public boolean isItemValid(@NotNull ItemStack stack) {
        return Library.isMachineUpgrade(stack);
    }

	@Override
    public void onSlotChange(ItemStack sta1, ItemStack sta2) {
		super.onSlotChange(sta1, sta2);
    }
}