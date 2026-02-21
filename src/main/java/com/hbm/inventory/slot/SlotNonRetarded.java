package com.hbm.inventory.slot;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Because vanilla slots have severe mental disabilities that prevent them from working as expected.
 * @author hbm
 */
public class SlotNonRetarded extends SlotItemHandler {

    public SlotNonRetarded(IItemHandler inventory, int id, int x, int y) {
        super(inventory, id, x, y);
    }

    /**
     * Because if slots have higher stacksizes than the maximum allowed by the tile, the display just stops working.
     * Why was that necessary? Sure it's not intended but falsifying information isn't very cool.
     */
    @Override
    public int getSlotStackLimit() {
        return Math.max(super.getSlotStackLimit(), this.getHasStack() ? this.getStack().getCount() : 1);
    }
}
