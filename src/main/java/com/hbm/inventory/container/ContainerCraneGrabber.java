package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotPattern;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.items.ModItems;
import com.hbm.tileentity.network.TileEntityCraneGrabber;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerCraneGrabber extends ContainerBase {
    protected TileEntityCraneGrabber grabber;

    public ContainerCraneGrabber(InventoryPlayer invPlayer, TileEntityCraneGrabber grabber) {
        super(invPlayer, grabber.inventory);
        this.grabber = grabber;
        //filter
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.addSlotToContainer(new SlotPattern(grabber.inventory, j + i * 3, 40 + j * 18, 17 + i * 18));
            }
        }

        this.addSlotToContainer(new SlotUpgrade(grabber.inventory, 9, 121, 23));
        this.addSlotToContainer(new SlotUpgrade(grabber.inventory, 10, 121, 47));

        playerInv(invPlayer, 8, 103, 161);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if(slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            result = stack.copy();

            if(index < 9) { //filters
                return ItemStack.EMPTY;
            }

            int size = grabber.inventory.getSlots();
            if(index <= size - 1) {
                if(!this.mergeItemStack(stack, size, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {

                if(isUpgradeStack(result)) {
                    if(!this.mergeItemStack(stack, 9, 10, false))
                        return ItemStack.EMPTY;
                } else if(isUpgradeEjector(result)) {
                    if(!this.mergeItemStack(stack, 10, 11, false))
                        return ItemStack.EMPTY;
                }

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

    private static boolean isUpgradeStack(ItemStack item) {
        return item.getItem() == ModItems.upgrade_stack_1 || item.getItem() == ModItems.upgrade_stack_2 || item.getItem() == ModItems.upgrade_stack_3;
    }

    private static boolean isUpgradeEjector(ItemStack item) {
        return item.getItem() == ModItems.upgrade_ejector_1 ||  item.getItem() == ModItems.upgrade_ejector_2 ||  item.getItem() == ModItems.upgrade_ejector_3;
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        if (slotId < 0 || slotId >= 9) {
            return super.slotClick(slotId, dragType, clickTypeIn, player);
        }

        Slot slot = this.inventorySlots.get(slotId);

        ItemStack ret = ItemStack.EMPTY;
        ItemStack held = player.inventory.getItemStack();

        if (slot.getHasStack()) {
            ret = slot.getStack().copy();
        }

        if (clickTypeIn == ClickType.PICKUP && dragType == 1 && slot.getHasStack()) {
            grabber.nextMode(slotId);
            return ret;
        } else {
            slot.putStack(held.isEmpty() ? ItemStack.EMPTY : held.copy());

            if (slot.getHasStack()) {
                slot.getStack().setCount(1);
            }

            slot.onSlotChanged();

            grabber.matcher.initPatternStandard(grabber.getWorld(), slot.getStack(), slotId);
            return ret;
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return grabber.isUseableByPlayer(player);
    }

}
