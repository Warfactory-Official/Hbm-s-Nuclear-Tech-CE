package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.ModItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.TileEntityICFPress;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerICFPress extends Container {

    private final TileEntityICFPress press;

    public ContainerICFPress(InventoryPlayer invPlayer, TileEntityICFPress tedf) {

        press = tedf;

        //Empty Capsule
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 98, 18));
        //Filled Capsule
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 1, 98, 54));
        //Filled Muon
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 8, 18));
        //Empty Muon
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 3, 8, 54));
        //Solid Fuels
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 62, 54));
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 134, 54));
        //Fluid IDs
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 6, 62, 18));
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 7, 134, 18));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 97 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 155));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 8,
                s -> s.getItem() == ModItems.icf_pellet_empty, 2,
                s -> s.getItem() == ModItems.particle_muon, 4,
                s -> !(s.getItem() instanceof IItemFluidIdentifier), 6,
                s -> s.getItem() instanceof IItemFluidIdentifier, 8);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return press.isUseableByPlayer(player);
    }
}
