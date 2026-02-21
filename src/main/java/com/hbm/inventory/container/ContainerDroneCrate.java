package com.hbm.inventory.container;

import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.network.TileEntityDroneCrate;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerDroneCrate extends Container {
    protected TileEntityDroneCrate crate;

    public ContainerDroneCrate(InventoryPlayer invPlayer, TileEntityDroneCrate crate) {
        this.crate = crate;

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 6; j++) {
                this.addSlotToContainer(new SlotItemHandler(crate.inventory, j + i * 6, 8 + j * 18, 17 + i * 18));
            }
        }

        this.addSlotToContainer(new SlotItemHandler(crate.inventory, 18, 125, 53));

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
        return InventoryUtil.transferStack(this.inventorySlots, index, 19,
                s -> !(s.getItem() instanceof IItemFluidIdentifier), 18,
                s -> s.getItem() instanceof IItemFluidIdentifier, 19);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return crate.isUseableByPlayer(playerIn);
    }
}
