package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.tileentity.machine.TileEntityMachinePlasmaHeater;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachinePlasmaHeater extends Container {

    private final TileEntityMachinePlasmaHeater plasmaHeater;

    public ContainerMachinePlasmaHeater(InventoryPlayer invPlayer, TileEntityMachinePlasmaHeater tedf) {

        plasmaHeater = tedf;

        this.addSlotToContainer(new SlotBattery(tedf.inventory, 0, 8, 53));
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 44, 17));
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 2, 44, 53));
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 152, 17));
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 4, 152, 53));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142));
        }
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 5);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return plasmaHeater.isUseableByPlayer(player);
    }
}
