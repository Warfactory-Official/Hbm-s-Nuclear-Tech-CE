package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotPattern;
import com.hbm.tileentity.network.TileEntityDroneRequester;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerDroneRequester extends Container {
    protected TileEntityDroneRequester crate;

    public ContainerDroneRequester(InventoryPlayer invPlayer, TileEntityDroneRequester te) {
        this.crate = te;

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                this.addSlotToContainer(new SlotPattern(te.inventory, j + i * 3, 98 + j * 18, 17 + i * 18));
            }
        }

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                this.addSlotToContainer(new SlotItemHandler(te.inventory, j + i * 3 + 9, 26 + j * 18, 17 + i * 18));
            }
        }

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
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if(slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            result = stack.copy();

            if(index < 9) return ItemStack.EMPTY; //ignore filters

            if(index <= crate.getSizeInventory() - 1) {
                if(!this.mergeItemStack(stack, crate.getSizeInventory(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if(!this.mergeItemStack(stack, 9, crate.getSizeInventory(), false)) {
                return ItemStack.EMPTY;
            }

            if(stack.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            slot.onTake(player, stack);
        }

        return result;
    }

    @Override
    public ItemStack slotClick(int index, int button, ClickType mode, EntityPlayer player) {

        if(index < 0 || index > 8) {
            return super.slotClick(index, button, mode, player);
        }

        Slot slot = this.getSlot(index);

        ItemStack ret = ItemStack.EMPTY;
        ItemStack held = player.inventory.getItemStack();
        TileEntityDroneRequester requester = crate;

        if(slot.getHasStack())
            ret = slot.getStack().copy();

        if(button == 1 && mode == ClickType.PICKUP && slot.getHasStack()) {
            requester.nextMode(index);
            return ret;

        } else {
            slot.putStack(held);
            requester.matcher.initPatternStandard(requester.getWorld(), slot.getStack(), index);

            return ret;
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return crate.isUsableByPlayer(player);
    }
}
