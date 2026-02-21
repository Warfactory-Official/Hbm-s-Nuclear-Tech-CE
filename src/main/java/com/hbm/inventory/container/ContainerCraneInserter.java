package com.hbm.inventory.container;

import com.hbm.tileentity.network.TileEntityCraneInserter;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerCraneInserter extends Container {
    protected TileEntityCraneInserter inserter;

    public ContainerCraneInserter(InventoryPlayer invPlayer, TileEntityCraneInserter inserter) {
        this.inserter = inserter;

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 7; j++) {
                this.addSlotToContainer(new SlotItemHandler(inserter.inventory, j + i * 7, 26 + j * 18, 17 + i * 18));
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
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
        return InventoryUtil.transferStack(this.inventorySlots, index, 21);
    }


    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return inserter.isUseableByPlayer(player);
    }
}
