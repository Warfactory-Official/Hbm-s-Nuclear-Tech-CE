package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotNonRetarded;
import com.hbm.tileentity.machine.storage.TileEntityBatterySocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBatterySocket extends Container {

    protected TileEntityBatterySocket socket;

    public ContainerBatterySocket(InventoryPlayer invPlayer, TileEntityBatterySocket tedf) {
        this.socket = tedf;

        this.addSlotToContainer(new SlotNonRetarded(socket.inventory, 0, 35, 35));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 99 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 157));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return socket.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            copy = stack.copy();

            if (index == 0) {
                if (!this.mergeItemStack(stack, 1, this.inventorySlots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (!this.mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
            }

            if (stack.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return copy;
    }
}
