package com.hbm.tileentity.machine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;

import java.util.List;

public interface IHandHeldCrate {

    void finishHeldInventorySession(EntityPlayer player);

    default boolean blocksHeldSlotInteraction(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player, List<Slot> inventorySlots) {
        if (slotId >= 0 && slotId < inventorySlots.size()) {
            Slot slot = inventorySlots.get(slotId);
            if (slot != null && slot.inventory == player.inventory && slot.getSlotIndex() == player.inventory.currentItem) {
                return true;
            }
        }

        return clickTypeIn == ClickType.SWAP && dragType == player.inventory.currentItem;
    }
}
