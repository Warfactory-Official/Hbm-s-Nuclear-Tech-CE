package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.ModItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.oil.TileEntityMachineCatalyticReformer;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineCatalyticReformer extends Container {

    private TileEntityMachineCatalyticReformer reformer;

    public ContainerMachineCatalyticReformer(InventoryPlayer invPlayer, TileEntityMachineCatalyticReformer tedf) {

        reformer = tedf;

        //Battery
        this.addSlotToContainer(new SlotBattery(tedf.inventory, 0, 17, 90));
        //Canister Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 35, 90));
        //Canister Output
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 2, 35, 108));
        //Reformate Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 107, 90));
        //Reformate Output
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 4, 107, 108));
        //Gas Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 125, 90));
        //Gas Output
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 6, 125, 108));
        //Hydrogen Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 7, 143, 90));
        //Hydrogen Oil Output
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 8, 143, 108));
        //Fluid ID
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 9, 17, 108));
        //Catalyst
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 10, 71, 36));

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
        return InventoryUtil.transferStack(this.inventorySlots, index, 11,
                Library::isBattery, 1,
                ContainerMachineCatalyticReformer::isNormal, 9,
                s -> s.getItem() instanceof IItemFluidIdentifier, 10,
                s -> s.getItem() == ModItems.catalytic_converter, 11);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return reformer.isUseableByPlayer(player);
    }
}
