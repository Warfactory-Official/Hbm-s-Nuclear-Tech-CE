package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.albion.TileEntityPAQuadrupole;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerPAQuadrupole extends Container {

    private final TileEntityPAQuadrupole quadrupole;

    public ContainerPAQuadrupole(InventoryPlayer playerInv, TileEntityPAQuadrupole tile) {
        quadrupole = tile;

        //Battery
        this.addSlotToContainer(new SlotBattery(tile.inventory, 0, 26, 72));
        //Coil
        this.addSlotToContainer(new SlotItemHandler(tile.inventory, 1, 71, 36));

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
        return quadrupole.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 2,
                Library::isBattery, 1
        );
    }
}
