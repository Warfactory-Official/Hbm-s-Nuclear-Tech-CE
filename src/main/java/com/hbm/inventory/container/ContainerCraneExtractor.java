package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotPattern;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.items.ModItems;
import com.hbm.tileentity.network.TileEntityCraneExtractor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ContainerCraneExtractor extends ContainerBase  {
    protected TileEntityCraneExtractor extractor;

    public ContainerCraneExtractor(InventoryPlayer invPlayer, TileEntityCraneExtractor extractor) {
        super(invPlayer, extractor.inventory);
        this.extractor = extractor;

        //filter
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                this.addSlotToContainer(new SlotPattern(extractor.inventory, j + i * 3, 71 + j * 18, 17 + i * 18));
            }
        }

        //buffer
        addSlots(extractor.inventory, 9, 8, 17, 3, 3);

        //upgrades
        this.addSlotToContainer(new SlotUpgrade(extractor.inventory, 18, 152, 23));
        this.addSlotToContainer(new SlotUpgrade(extractor.inventory, 19, 152, 47));

        playerInv(invPlayer, 26, 103, 161);
    }

    @Override
    public @NotNull ItemStack slotClick(int slotId, int dragType, @NotNull ClickType clickTypeIn, @NotNull EntityPlayer player) {
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
            extractor.nextMode(slotId);
        } else {
            slot.putStack(held.isEmpty() ? ItemStack.EMPTY : held.copy());

            if (slot.getHasStack()) {
                slot.getStack().setCount(1);
            }

            slot.onSlotChanged();
            extractor.initPattern(slot.getStack(), slotId);
        }

        return ret;
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if(slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            result = stack.copy();

            if(index < 9) { // filters
                return ItemStack.EMPTY;
            }

            int size = extractor.inventory.getSlots();
            if(index <= size - 1) {
                if(!this.mergeItemStack(stack, size, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if(isUpgradeStack(result)) {
                    if(!this.mergeItemStack(stack, 18, 19, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if(isUpgradeEjector(result)) {
                    if(!this.mergeItemStack(stack, 19, 20, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if(!this.mergeItemStack(stack, 9, size, false)) {
                    return ItemStack.EMPTY;
                }

                return ItemStack.EMPTY;
            }

            if(stack.isEmpty()) {
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
    public boolean canInteractWith(EntityPlayer player) {
        return extractor.isUseableByPlayer(player);
    }
}
