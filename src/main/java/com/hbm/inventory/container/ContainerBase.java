package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotCraftingOutput;
import com.hbm.inventory.slot.SlotNonRetarded;
import com.hbm.inventory.slot.SlotSmelting;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/**
 * For now, only used for stuff with filters and crates as a reference
 * implementation, because I really needed to get the te from a container But
 * you should very much use this to kill the giant amount of boilerplate in
 * container classes
 *
 * @author 70k
 **/
public class ContainerBase extends Container {

    public IItemHandler tile;

    public ContainerBase(InventoryPlayer invPlayer, IItemHandler handler) {
        this.tile = handler;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    /** Respects slot restrictions */
    @Override
    protected boolean mergeItemStack(ItemStack slotStack, int start, int end, boolean direction) {
        return super.mergeItemStack(slotStack, start, end, direction);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, this.tile.getSlots());
    }

    /** Standard player inventory with default hotbar offset */
    public void playerInv(InventoryPlayer invPlayer, int playerInvX, int playerInvY) {
        playerInv(invPlayer, playerInvX, playerInvY, playerInvY + 58);
    }

    /** Used to quickly set up the player inventory */
    public void playerInv(InventoryPlayer invPlayer, int playerInvX, int playerInvY, int playerHotbarY) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, playerInvX + j * 18, playerInvY + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, playerInvX + i * 18, playerHotbarY));
        }
    }

    /**
     * Used to add several conventional inventory slots at a time
     *
     * @param inv the inventory to add the slots to
     * @param from the slot index to start from
     */
    public void addSlots(IItemHandler inv, int from, int x, int y, int rows, int cols) {
        addSlots(inv, from, x, y, rows, cols, 18);
    }

    public void addSlots(IItemHandler inv, int from, int x, int y, int rows, int cols, int slotSize) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                this.addSlotToContainer(new SlotNonRetarded(inv, col + row * cols + from, x + col * slotSize, y + row * slotSize));
            }
        }
    }

    public void addOutputSlots(EntityPlayer player, IItemHandler inv, int from, int x, int y, int rows, int cols) {
        addOutputSlots(player, inv, from, x, y, rows, cols, 18);
    }

    public void addOutputSlots(EntityPlayer player, IItemHandler inv, int from, int x, int y, int rows, int cols, int slotSize) {
        for (int row = 0; row < rows; row++) for (int col = 0; col < cols; col++) {
            this.addSlotToContainer(new SlotCraftingOutput(player, inv, col + row * cols + from, x + col * slotSize, y + row * slotSize));
        }
    }

    public void addTakeOnlySlots(IItemHandler inv, int from, int x, int y, int rows, int cols) {
        addTakeOnlySlots(inv, from, x, y, rows, cols, 18);
    }

    public void addTakeOnlySlots(IItemHandler inv, int from, int x, int y, int rows, int cols, int slotSize) {
        for (int row = 0; row < rows; row++) for (int col = 0; col < cols; col++) {
            this.addSlotToContainer(SlotFiltered.takeOnly(inv, col + row * cols + from, x + col * slotSize, y + row * slotSize));
        }
    }

    public boolean handleSmeltingTransfer(Slot slot, ItemStack stack, ItemStack rStack, int startIndex, int endIndex) {
        int originalCount = stack.getCount();

        if (!this.mergeItemStack(stack, startIndex, endIndex, true)) {
            return false;
        }

        int movedCount = originalCount - stack.getCount();

        if (movedCount > 0 && slot instanceof SlotSmelting slotSmelting) {
            slotSmelting.awardXP(rStack, movedCount);
        }

        return true;
    }
}
