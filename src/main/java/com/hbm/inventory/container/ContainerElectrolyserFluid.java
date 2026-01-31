package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityElectrolyser;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerElectrolyserFluid extends Container {

    private TileEntityElectrolyser electrolyser;

    public ContainerElectrolyserFluid(InventoryPlayer invPlayer, TileEntityElectrolyser tedf) {
        electrolyser = tedf;

        //Battery
        this.addSlotToContainer(new SlotBattery(tedf.inventory, 0, 186, 109));
        //Upgrades
        this.addSlotToContainer(new SlotUpgrade(tedf.inventory, 1, 186, 140));
        this.addSlotToContainer(new SlotUpgrade(tedf.inventory, 2, 186, 158));
        //Fluid ID
        this.addSlotToContainer(SlotFiltered.fluidTypeSlot(tedf.inventory, 3, 6, 18));
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 4, 6, 54));
        //Input
        this.addSlotToContainer(SlotFiltered.fluidHandlerSlot(tedf.inventory, 5, 24, 18));
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 6, 24, 54));
        //Output
        this.addSlotToContainer(SlotFiltered.fluidHandlerSlot(tedf.inventory, 7, 78, 18));
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 8, 78, 54));
        this.addSlotToContainer(SlotFiltered.fluidHandlerSlot(tedf.inventory, 9, 134, 18));
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 10, 134, 54));
        //Byproducts
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 11, 154, 18));
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 12, 154, 36));
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 13, 154, 54));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 122 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 180));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 14,
                Library::isBattery, 1,
                Library::isMachineUpgrade, 3,
                s -> s.getItem() instanceof IItemFluidIdentifier, 5);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return electrolyser.isUseableByPlayer(player);
    }
}
