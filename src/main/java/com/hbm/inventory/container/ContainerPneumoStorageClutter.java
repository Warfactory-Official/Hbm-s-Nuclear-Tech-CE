package com.hbm.inventory.container;

import com.hbm.tileentity.network.TileEntityPneumoStorageClutter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerPneumoStorageClutter extends ContainerBase {

    private final TileEntityPneumoStorageClutter storage;

    public ContainerPneumoStorageClutter(InventoryPlayer invPlayer, TileEntityPneumoStorageClutter storage) {
        super(invPlayer, storage.inventory);
        this.storage = storage;

        addSlots(storage.inventory, 0, 8, 17, 6, 9);
        playerInv(invPlayer, 8, 153);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return storage.isUseableByPlayer(player);
    }
}
