package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotNonRetarded;
import com.hbm.items.machine.ItemCrucibleTemplate;
import com.hbm.tileentity.machine.TileEntityCrucible;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ContainerCrucible extends Container {

    protected TileEntityCrucible crucible;

    public ContainerCrucible(InventoryPlayer invPlayer, TileEntityCrucible crucible) {
        this.crucible = crucible;

        this.addSlotToContainer(new SlotCrucibleTemplate(crucible.inventory, 0, 107, 81));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.addSlotToContainer(new SlotOneItem(crucible.inventory, j + i * 3 + 1, 107 + j * 18, 18 + i * 18));
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 132 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 190));
        }
    }

    @Override
    public ItemStack slotClick(int index, int button, ClickType clickTypeIn, EntityPlayer player) {
        if (clickTypeIn == ClickType.CLONE) return ItemStack.EMPTY;
        return super.slotClick(index, button, clickTypeIn, player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            ret = stackInSlot.copy();

            if (index < 10) {
                if (!this.mergeItemStack(stackInSlot, 10, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(stackInSlot, ret);
            } else {
                if (!this.mergeItemStack(stackInSlot, 0, 10, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return ret;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return crucible.isUseableByPlayer(player);
    }

    public static class SlotCrucibleTemplate extends SlotNonRetarded {
        public SlotCrucibleTemplate(IItemHandler inv, int index, int x, int y) {
            super(inv, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return !stack.isEmpty() && stack.getItem() instanceof ItemCrucibleTemplate;
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }
    }

    public static class SlotOneItem extends SlotNonRetarded {
        public SlotOneItem(IItemHandler inv, int index, int x, int y) {
            super(inv, index, x, y);
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }
    }
}
