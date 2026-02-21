package com.hbm.inventory.container;

import com.hbm.tileentity.network.TileEntityCraneBoxer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerCraneBoxer extends Container {

    protected TileEntityCraneBoxer boxer;

    public ContainerCraneBoxer(InventoryPlayer invPlayer, TileEntityCraneBoxer boxer) {
        this.boxer = boxer;

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 7; j++) {
                this.addSlotToContainer(new SlotItemHandler(boxer.inventory, j + i * 7, 8 + j * 18, 17 + i * 18));
            }
        }

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 103 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 161));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot =  this.inventorySlots.get(index);

        if(slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            result = stack.copy();

            if(index <= 21) {
                if(!this.mergeItemStack(stack, 21, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if(!this.mergeItemStack(stack, 0, 21, false)) {
                    return ItemStack.EMPTY;
                }

                return ItemStack.EMPTY;
            }

            if(stack.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

        }

        return result;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return boxer.isUseableByPlayer(player);
    }
}
