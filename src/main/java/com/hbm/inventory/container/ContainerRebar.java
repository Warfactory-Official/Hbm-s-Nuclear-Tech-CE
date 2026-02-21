package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotPattern;
import com.hbm.items.tool.ItemRebarPlacer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerRebar extends Container {

    private final ItemRebarPlacer.InventoryRebar rebar;

    public ContainerRebar(InventoryPlayer invPlayer, ItemRebarPlacer.InventoryRebar rebar) {
        this.rebar = rebar;

        addSlotToContainer(new SlotPattern(rebar, 0, 53, 36));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 100 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 158));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        if (clickTypeIn == ClickType.SWAP && dragType == player.inventory.currentItem) return ItemStack.EMPTY;
        if (slotId == player.inventory.currentItem + 28) return ItemStack.EMPTY;
        if (slotId != 0) return super.slotClick(slotId, dragType, clickTypeIn, player);
        Slot slot = getSlot(0);
        ItemStack cursor = player.inventory.getItemStack();
        if (cursor.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
            return ItemStack.EMPTY;
        }
        ItemStack template = cursor.copy();
        template.setCount(1);
        slot.putStack(template);
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
