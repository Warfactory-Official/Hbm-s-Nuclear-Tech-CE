package com.hbm.inventory.container;

import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.TileEntityHeaterOilburner;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerOilburner extends Container {
    private final TileEntityHeaterOilburner heater;

    public ContainerOilburner(InventoryPlayer player, TileEntityHeaterOilburner heater) {
        this.heater = heater;

        // In
        this.addSlotToContainer(new SlotItemHandler(heater.inventory, 0, 26, 17));
        // Out
        this.addSlotToContainer(new SlotItemHandler(heater.inventory, 1, 26, 53));
        // Fluid ID
        this.addSlotToContainer(new SlotItemHandler(heater.inventory, 2, 44, 71));

        int offset = 37;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(player, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + offset));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(player, i, 8 + i * 18, 142 + offset));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return heater.isUseableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 3,
                s -> !(s.getItem() instanceof IItemFluidIdentifier), 2,
                s -> s.getItem() instanceof IItemFluidIdentifier, 3);
    }
}
