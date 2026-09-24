package com.hbm.inventory.container;

import com.hbm.inventory.TransferStrategy;
import com.hbm.inventory.slot.SlotBattery;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineRadarNT;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import java.util.function.Predicate;

public class ContainerMachineRadarNT extends Container {

    private TileEntityMachineRadarNT radar;

    private static final TransferStrategy TRANSFER_STRATEGY = TransferStrategy.builder(10)
                                                                              .rule(0, 9,
                                                                                      Predicate.not(Library::isBattery))
                                                                              .genericMachineRange(9)
                                                                              .build();

    public ContainerMachineRadarNT(InventoryPlayer invPlayer, TileEntityMachineRadarNT tedf) {
        this.radar = tedf;

        for(int i = 0; i < 8; i++) this.addSlotToContainer(new SlotItemHandler(tedf.inventory, i, 26 + i * 18, 17));

        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 8, 26, 44));
        this.addSlotToContainer(new SlotBattery(tedf.inventory, 9, 152, 44));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 103 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 161));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, this.TRANSFER_STRATEGY, player);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return radar.isUseableByPlayer(player);
    }
}
