package com.hbm.inventory.container;

import com.hbm.tileentity.machine.TileEntityCoreEmitter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerCoreEmitter extends Container {

    private TileEntityCoreEmitter nukeBoy;

    public ContainerCoreEmitter(EntityPlayer player, TileEntityCoreEmitter tedf) {
        InventoryPlayer invPlayer = player.inventory;
        if (player instanceof EntityPlayerMP)
            nukeBoy = tedf;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 88 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 146));
        }
    }


    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return nukeBoy.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            return ItemStack.EMPTY;
        }

        return result;
    }
}