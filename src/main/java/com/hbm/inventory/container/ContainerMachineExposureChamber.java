package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineExposureChamber;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineExposureChamber extends Container {

    private final TileEntityMachineExposureChamber chamber;

    public ContainerMachineExposureChamber(InventoryPlayer invPlayer, TileEntityMachineExposureChamber tedf) {
        this.chamber = tedf;

        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 8, 18));
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 2, 8, 54));
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 80, 36));
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 4, 116, 36));
        this.addSlotToContainer(new SlotBattery(tedf.inventory, 5, 152, 54));
        this.addSlotToContainer(new SlotUpgrade(tedf.inventory, 6, 44, 54));
        this.addSlotToContainer(new SlotUpgrade(tedf.inventory, 7, 62, 54));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 104 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 162));
        }
    }

    private static boolean isNormal(ItemStack stack) {
        return !Library.isBattery(stack) && !Library.isMachineUpgrade(stack);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 7,
                ContainerMachineExposureChamber::isNormal, 4,
                Library::isBattery, 5,
                Library::isMachineUpgrade, 7);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return chamber.isUseableByPlayer(player);
    }
}
