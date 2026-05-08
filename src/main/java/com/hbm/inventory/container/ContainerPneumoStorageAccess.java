package com.hbm.inventory.container;

import com.hbm.tileentity.network.TileEntityPneumoStorageAccess;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerPneumoStorageAccess extends Container {

    private final TileEntityPneumoStorageAccess access;

    public ContainerPneumoStorageAccess(InventoryPlayer invPlayer, TileEntityPneumoStorageAccess access) {
        this.access = access;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlotToContainer(new Slot(invPlayer, col + row * 9 + 9, 8 + col * 18, 169 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlotToContainer(new Slot(invPlayer, col, 8 + col * 18, 227));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player.getDistanceSq(access.getPos().getX() + 0.5D, access.getPos().getY() + 0.5D, access.getPos().getZ() + 0.5D) <= 15D * 15D;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return ItemStack.EMPTY;
    }
}
