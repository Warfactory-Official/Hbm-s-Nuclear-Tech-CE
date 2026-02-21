package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineCompressorBase;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerCompressor extends Container {
    private final TileEntityMachineCompressorBase compressor;

    public ContainerCompressor(InventoryPlayer playerInv, TileEntityMachineCompressorBase tile) {
        compressor = tile;

        //Fluid ID
        this.addSlotToContainer(new SlotItemHandler(tile.inventory, 0, 17, 72));
        //Battery
        this.addSlotToContainer(new SlotBattery(tile.inventory, 1, 152, 72));
        //Upgrades
        this.addSlotToContainer(new SlotUpgrade(tile.inventory, 2, 52, 72));
        this.addSlotToContainer(new SlotUpgrade(tile.inventory, 3, 70, 72));

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
        return compressor.isUseableByPlayer(player);
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 4,
                s -> s.getItem() instanceof IItemFluidIdentifier, 1,
                Library::isBattery, 2);
    }
}