package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.ModItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.oil.TileEntityMachineHydrotreater;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineHydrotreater extends Container {

    private TileEntityMachineHydrotreater hydrotreater;

    public ContainerMachineHydrotreater(InventoryPlayer invPlayer, TileEntityMachineHydrotreater tedf) {

        hydrotreater = tedf;

        //Battery
        this.addSlotToContainer(new SlotBattery(tedf.inventory, 0, 17, 90));
        //Canister Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 35, 90));
        //Canister Output
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 2, 35, 108));
        //Hydrogen Input (removed, requires pressurization)
        //so why the fuck did you not remove them Bob, gottverdammt
        // this.addSlotToContainer(new SlotDeprecated(tedf, 3, 53, 90));
        //Hydrogen Output (samesies)
        // this.addSlotToContainer(new SlotDeprecated(tedf, 4, 53, 108));
        //Desulfated Oil Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 125, 90));
        //Desulfated Oil Output
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 4, 125, 108));
        //Sour Gas Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 143, 90));
        //Sour Gas Oil Output
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 6, 143, 108));
        //Fluid ID
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 7, 17, 108));
        //Catalyst
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 8, 89, 36));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 156 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 214));
        }
    }

    private static boolean isNormal(ItemStack stack) {
        return !(stack.getItem() instanceof IItemFluidIdentifier) && stack.getItem() != ModItems.catalytic_converter;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
         return InventoryUtil.transferStack(this.inventorySlots, index, 9,
                 Library::isBattery, 1,
                 ContainerMachineHydrotreater::isNormal, 7,
                 s -> s.getItem() instanceof IItemFluidIdentifier, 8,
                 s -> s.getItem() == ModItems.catalytic_converter, 9);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return hydrotreater.isUseableByPlayer(player);
    }
}
