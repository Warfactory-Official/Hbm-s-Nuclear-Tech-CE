package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.tileentity.machine.TileEntityMachinePress;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachinePress extends Container {

    private final TileEntityMachinePress press;

    private int lastSpeed;
    private int lastBurnTime;
    private int lastProgress;

    public ContainerMachinePress(InventoryPlayer invPlayer, TileEntityMachinePress te) {
        this.press = te;

        // Slot 0: Fuel
        this.addSlotToContainer(new SlotItemHandler(te.inventory, 0, 26, 53));
        // Slot 1: Stamp
        this.addSlotToContainer(new SlotItemHandler(te.inventory, 1, 80, 17));
        // Slot 2: Input
        this.addSlotToContainer(new SlotItemHandler(te.inventory, 2, 80, 53));
        // Slot 3: Output
        this.addSlotToContainer(SlotFiltered.takeOnly(te.inventory, 3, 140, 35));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142));
        }
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, this.press.speed);
        listener.sendWindowProperty(this, 1, this.press.burnTime);
        listener.sendWindowProperty(this, 2, this.press.progress);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (IContainerListener listener : this.listeners) {
            if (this.lastSpeed != this.press.speed) {
                listener.sendWindowProperty(this, 0, this.press.speed);
            }
            if (this.lastBurnTime != this.press.burnTime) {
                listener.sendWindowProperty(this, 1, this.press.burnTime);
            }
            if (this.lastProgress != this.press.progress) {
                listener.sendWindowProperty(this, 2, this.press.progress);
            }
        }

        this.lastSpeed = this.press.speed;
        this.lastBurnTime = this.press.burnTime;
        this.lastProgress = this.press.progress;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        switch (id) {
            case 0 -> this.press.speed = data;
            case 1 -> this.press.burnTime = data;
            case 2 -> this.press.progress = data;
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.press.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        int machineSlots = 4;

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < machineSlots) {
                if (!this.mergeItemStack(itemstack1, machineSlots, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(itemstack1, itemstack);
            } else {
                boolean moved = this.press.isItemValidForSlot(0, itemstack1) && this.mergeItemStack(itemstack1, 0, 1, false);

                if (!itemstack1.isEmpty() && this.press.isItemValidForSlot(1, itemstack1) && this.mergeItemStack(itemstack1, 1, 2, false)) {
                    moved = true;
                }

                if (!itemstack1.isEmpty() && this.press.isItemValidForSlot(2, itemstack1) && this.mergeItemStack(itemstack1, 2, 3, false)) {
                    moved = true;
                }

                if (!moved) {
                    if (index < machineSlots + 27) {
                        if (!this.mergeItemStack(itemstack1, machineSlots + 27, machineSlots + 36, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (index < machineSlots + 36 && !this.mergeItemStack(itemstack1, machineSlots, machineSlots + 27, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }
}