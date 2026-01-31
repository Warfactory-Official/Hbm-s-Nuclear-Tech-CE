package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotNonRetarded;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.fusion.TileEntityFusionTorus;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ContainerFusionTorus extends Container {

    private TileEntityFusionTorus torus;

    public ContainerFusionTorus(InventoryPlayer invPlayer, TileEntityFusionTorus torus) {
        this.torus = torus;

        this.addSlotToContainer(new SlotBattery(torus.inventory, 0, 8, 82));
        this.addSlotToContainer(new SlotNonRetarded(torus.inventory, 1, 71, 81));
        this.addSlotToContainer(new SlotNonRetarded(torus.inventory, 2, 130, 36));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 35 + j * 18, 162 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 35 + i * 18, 220));
        }
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 3,
                Library::isBattery, 1,
                s -> s.getItem() == ModItems.blueprints, 2);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return torus.isUseableByPlayer(player);
    }

}
