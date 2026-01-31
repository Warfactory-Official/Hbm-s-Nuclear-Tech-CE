package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.tileentity.machine.albion.TileEntityPARFC;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ContainerPARFC extends Container {

    private final TileEntityPARFC rfc;

    public ContainerPARFC(InventoryPlayer playerInv, TileEntityPARFC tile) {
        rfc = tile;

        //Battery
        this.addSlotToContainer(new SlotBattery(tile.inventory, 0, 53, 72));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 122 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 180));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return rfc.isUseableByPlayer(player);
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 1);
    }
}
