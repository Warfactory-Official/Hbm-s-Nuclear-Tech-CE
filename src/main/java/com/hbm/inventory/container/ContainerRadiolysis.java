package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.ItemRTGPellet;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineRadiolysis;

import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import java.util.function.Predicate;

public class ContainerRadiolysis extends Container {

    private TileEntityMachineRadiolysis radiolysis;

    public ContainerRadiolysis(InventoryPlayer playerInv, TileEntityMachineRadiolysis tile) {
        radiolysis = tile;

        //RTG
        for(byte i = 0; i < 2; i++) {
            for(byte j = 0; j < 5; j++) {
                this.addSlotToContainer(new SlotItemHandler(tile.inventory, j + i * 5, 188 + i * 18, 8 + j * 18));
            }
        }

        //Fluid IO
        this.addSlotToContainer(new SlotItemHandler(tile.inventory, 10, 34, 17));
        this.addSlotToContainer(SlotFiltered.takeOnly(tile.inventory, 11, 34, 53));

        //Sterilization
        this.addSlotToContainer(new SlotItemHandler(tile.inventory, 12, 148, 17));
        this.addSlotToContainer(SlotFiltered.takeOnly(tile.inventory, 13, 148, 53));

        //Battery
        this.addSlotToContainer(new SlotBattery(tile.inventory, 14, 8, 53));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return radiolysis.isUseableByPlayer(player);
    }

    /** my eye, my eye, coctor coctor coctor **/ //???
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 15,
                s -> s.getItem() instanceof ItemRTGPellet, 10,
                s -> s.getItem() instanceof IItemFluidIdentifier, 11,
                Predicate.not(Library::isChargeableBattery), 14,
                Library::isChargeableBattery, 15
        );
    }
}
