package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.oil.TileEntityMachineSolidifier;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerSolidifier extends Container {

    private TileEntityMachineSolidifier solidifier;

    public ContainerSolidifier(InventoryPlayer playerInv, TileEntityMachineSolidifier tile) {
        solidifier = tile;

        // Output
        this.addSlotToContainer(SlotFiltered.takeOnly(tile.inventory, 0, 71, 45));
        // Battery
        this.addSlotToContainer(new SlotBattery(tile.inventory, 1, 134, 72));
        // Upgrades
        this.addSlotToContainer(new SlotUpgrade(tile.inventory, 2, 98, 36));
        this.addSlotToContainer(new SlotUpgrade(tile.inventory, 3, 98, 54));
        // ID
        this.addSlotToContainer(new SlotItemHandler(tile.inventory, 4, 71, 72));

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
        return solidifier.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 5,
                Library::isBattery, 2,
                Library::isMachineUpgrade, 4,
                s -> s.getItem() instanceof IItemFluidIdentifier, 5
        );
    }
}
