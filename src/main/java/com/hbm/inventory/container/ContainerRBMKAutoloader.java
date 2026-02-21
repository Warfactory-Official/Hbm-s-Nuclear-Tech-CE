package com.hbm.inventory.container;

import com.hbm.tileentity.machine.rbmk.TileEntityRBMKAutoloader;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerRBMKAutoloader extends ContainerBase {

    public TileEntityRBMKAutoloader loader;

    public ContainerRBMKAutoloader(InventoryPlayer invPlayer, TileEntityRBMKAutoloader tedf) {
        super(invPlayer, tedf.inventory);
        loader = tedf;

        this.addSlots(loader.inventory, 0, 17, 18, 3, 3);
        this.addTakeOnlySlots(loader.inventory, 9, 107, 18, 3, 3);
        this.playerInv(invPlayer, 8, 100);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return loader.isUseableByPlayer(player);
    }
}
