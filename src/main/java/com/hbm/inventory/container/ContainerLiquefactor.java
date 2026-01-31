package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.oil.TileEntityMachineLiquefactor;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerLiquefactor extends Container {

    private final TileEntityMachineLiquefactor liquefactor;

    public ContainerLiquefactor(InventoryPlayer playerInv, TileEntityMachineLiquefactor tile) {
        liquefactor = tile;

        //Input
        this.addSlotToContainer(new SlotItemHandler(tile.inventory, 0, 35, 54));
        //Battery
        this.addSlotToContainer(new SlotBattery(tile.inventory, 1, 134, 72));
        //Upgrades
        this.addSlotToContainer(new SlotUpgrade(tile.inventory, 2, 98, 36));
        this.addSlotToContainer(new SlotUpgrade(tile.inventory, 3, 98, 54));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 122 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 180));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return liquefactor.isUseableByPlayer(player);
    }

    private static boolean isNormal(ItemStack stack) {
        return !Library.isBattery(stack) && !Library.isMachineUpgrade(stack);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 4,
                ContainerLiquefactor::isNormal, 1,
                Library::isBattery, 2,
                Library::isMachineUpgrade, 4);
    }
}
