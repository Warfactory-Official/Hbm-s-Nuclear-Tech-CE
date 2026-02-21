package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineBattery;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineBattery extends Container {

    public TileEntityMachineBattery machineBattery;

    public ContainerMachineBattery(InventoryPlayer invPlayer, TileEntityMachineBattery tile) {

        machineBattery = tile;

        this.addSlotToContainer(new SlotBattery(tile.inventory, 0, 53 - 18, 17));
        this.addSlotToContainer(SlotFiltered.takeOnly(tile.inventory, 1, 53 - 18, 53));
        this.addSlotToContainer(new SlotBattery(tile.inventory, 2, 125, 17));
        this.addSlotToContainer(SlotFiltered.takeOnly(tile.inventory, 3, 125, 53));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142));
        }
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer playerIn, int index) {
        ItemStack rStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            rStack = stack.copy();

            if (index <= 3) {
                if (!this.mergeItemStack(stack, 4, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (Library.isBattery(stack)) {
                    if (!this.mergeItemStack(stack, 0, 1, false)) {
                        if (!this.mergeItemStack(stack, 2, 3, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                } else return ItemStack.EMPTY;
            }

            if (stack.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return rStack;
    }

    @Override
    public void detectAndSendChanges() {
        machineBattery.networkPackNT(10);
        super.detectAndSendChanges();
    }

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer playerIn) {
        return machineBattery.isUseableByPlayer(playerIn);
    }
}
