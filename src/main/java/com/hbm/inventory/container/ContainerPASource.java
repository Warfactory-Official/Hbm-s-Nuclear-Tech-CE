package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.albion.TileEntityPASource;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerPASource extends Container {
    private TileEntityPASource source;

    public ContainerPASource(InventoryPlayer playerInv, TileEntityPASource tile) {
        source = tile;
        //Battery
        this.addSlotToContainer(new SlotBattery(tile.inventory, 0, 8, 72));
        //Inputs
        this.addSlotToContainer(new SlotItemHandler(tile.inventory, 1, 62, 18));
        this.addSlotToContainer(new SlotItemHandler(tile.inventory, 2, 80, 18));
        //Containers
        this.addSlotToContainer(SlotFiltered.takeOnly(tile.inventory, 3, 62, 45));
        this.addSlotToContainer(SlotFiltered.takeOnly(tile.inventory, 4, 80, 45));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 122 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 180));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return source.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 5,
                Library::isBattery, 1
        );
    }
}
