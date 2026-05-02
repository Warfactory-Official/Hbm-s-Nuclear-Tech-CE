package com.hbm.inventory.container;

import com.hbm.inventory.TransferStrategy;
import com.hbm.inventory.slot.SlotBattery;
import com.hbm.tileentity.machine.fusion.TileEntityFusionKlystron;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ContainerFusionKlystron extends Container {

    protected TileEntityFusionKlystron klystron;

    private static final TransferStrategy TRANSFER_STRATEGY = TransferStrategy.builder(1)
                                                                              .genericMachineRange(0)
                                                                              .build();

    public ContainerFusionKlystron(InventoryPlayer invPlayer, TileEntityFusionKlystron tedf) {
        this.klystron = tedf;

        this.addSlotToContainer(new SlotBattery(klystron.inventory, 0, 8, 72));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 17 + j * 18, 118 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 17 + i * 18, 176));
        }
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, this.TRANSFER_STRATEGY, player);
    }

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer player) {
        return klystron.isUseableByPlayer(player);
    }
}
