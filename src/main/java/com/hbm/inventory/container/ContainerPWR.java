package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.TileEntityPWRController;

import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerPWR extends Container {

    TileEntityPWRController controller;

    public ContainerPWR(InventoryPlayer invPlayer, TileEntityPWRController controller) {
        this.controller = controller;

        this.addSlotToContainer(new SlotItemHandler(controller.inventory, 0, 53, 5));
        this.addSlotToContainer(SlotFiltered.takeOnly(controller.inventory, 1, 89, 32));  // Output slot
        this.addSlotToContainer(new SlotItemHandler(controller.inventory, 2, 8, 59));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 106 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 164));
        }
    }

    @Override
    public ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 3,
                s -> !(s.getItem() instanceof IItemFluidIdentifier), 2,
                s -> s.getItem() instanceof IItemFluidIdentifier, 3
        );
    }

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer player) {
        return controller.isUseableByPlayer(player);
    }

}