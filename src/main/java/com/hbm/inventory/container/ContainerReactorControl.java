package com.hbm.inventory.container;

import com.hbm.inventory.TransferStrategy;
import com.hbm.tileentity.machine.TileEntityReactorControl;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerReactorControl extends Container {

    private TileEntityReactorControl control;

    private static final TransferStrategy TRANSFER_STRATEGY = TransferStrategy.builder(1)
                                                                              .genericMachineRange(0)
                                                                              .build();

    public ContainerReactorControl(InventoryPlayer invPlayer, TileEntityReactorControl tedf) {

        control = tedf;

        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 92, 38));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++)
        {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
        return InventoryUtil.transferStack(this.inventorySlots, index, this.TRANSFER_STRATEGY, player);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return control.isUseableByPlayer(player);
    }
}
