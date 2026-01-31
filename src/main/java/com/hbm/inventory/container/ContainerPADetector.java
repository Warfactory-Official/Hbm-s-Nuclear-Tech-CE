package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.albion.TileEntityPADetector;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerPADetector extends Container {

    private final TileEntityPADetector detector;

    public ContainerPADetector(InventoryPlayer playerInv, TileEntityPADetector tile) {
        detector = tile;

        //Battery
        this.addSlotToContainer(new SlotBattery(tile.inventory, 0, 8, 72));
        //Containers
        this.addSlotToContainer(new SlotItemHandler(tile.inventory, 1, 62, 18));
        this.addSlotToContainer(new SlotItemHandler(tile.inventory, 2, 80, 18));
        //Outputs
        this.addSlotToContainer(SlotFiltered.takeOnly(tile.inventory, 3, 62, 45));
        this.addSlotToContainer(SlotFiltered.takeOnly(tile.inventory, 4, 80, 45));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 122 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 180));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return detector.isUseableByPlayer(player);
    }

    @NotNull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 5,
                Library::isBattery, 1
        );
    }
}
