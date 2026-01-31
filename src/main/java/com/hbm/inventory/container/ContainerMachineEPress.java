package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemStamp;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineEPress;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineEPress extends Container {

    private final TileEntityMachineEPress ePress;

    private int progress;

    public ContainerMachineEPress(InventoryPlayer invPlayer, @NotNull TileEntityMachineEPress ePress) {

        this.ePress = ePress;

        //Battery
        this.addSlotToContainer(new SlotItemHandler(ePress.inventory, 0, 44, 53));
        //Stamp
        this.addSlotToContainer(new SlotItemHandler(ePress.inventory, 1, 80, 17));
        //Input
        this.addSlotToContainer(new SlotItemHandler(ePress.inventory, 2, 80, 53));
        //Output
        this.addSlotToContainer(SlotFiltered.takeOnly(ePress.inventory, 3, 140, 35));
        //Upgrade
        this.addSlotToContainer(new SlotUpgrade(ePress.inventory, 4, 44, 21));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142));
        }
    }

    @NotNull
    @Override
    public ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
        ItemStack stackCopy = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack originalStack = slot.getStack();
            stackCopy = originalStack.copy();
            if (index <= 4) {
                if (!this.mergeItemStack(originalStack, 5, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            else {
                if (Library.isBattery(stackCopy)) {
                    if (!this.mergeItemStack(originalStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                else if (stackCopy.getItem() instanceof ItemMachineUpgrade) {
                    if (!this.mergeItemStack(originalStack, 4, 5, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                else if (stackCopy.getItem() instanceof ItemStamp) {
                    if (!this.mergeItemStack(originalStack, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                else {
                    if (!this.mergeItemStack(originalStack, 2, 3, false)) {
                        if (index < 32) {
                            if (!this.mergeItemStack(originalStack, 32, 41, false)) {
                                return ItemStack.EMPTY;
                            }
                        } else if (index < 41) {
                            if (!this.mergeItemStack(originalStack, 5, 32, false)) {
                                return ItemStack.EMPTY;
                            }
                        }
                    }
                }
            }

            if (originalStack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (originalStack.getCount() == stackCopy.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, originalStack);
        }

        return stackCopy;
    }

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer player) {
        return ePress.isUseableByPlayer(player);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (IContainerListener par1 : this.listeners) {
            if (this.progress != this.ePress.progress) {
                par1.sendWindowProperty(this, 0, this.ePress.progress);
            }
        }
        this.progress = this.ePress.progress;
    }

    @Override
    public void updateProgressBar(int id, int data) {
        if (id == 0) {
            ePress.progress = data;
        }
    }
}
