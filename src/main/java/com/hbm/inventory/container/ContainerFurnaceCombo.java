package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotSmelting;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.tileentity.machine.TileEntityFurnaceCombination;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerFurnaceCombo extends ContainerBase {

    protected TileEntityFurnaceCombination furnace;

    public ContainerFurnaceCombo(InventoryPlayer invPlayer, TileEntityFurnaceCombination furnace) {
        super(invPlayer, furnace.inventory);
        this.furnace = furnace;

        //input
        this.addSlotToContainer(new SlotItemHandler(furnace.inventory, 0, 26, 36));
        // output
        this.addSlotToContainer(new SlotSmelting(invPlayer.player, furnace.inventory, 1, 89, 36));
        this.addSlotToContainer(new SlotItemHandler(furnace.inventory, 2, 136, 18));
        this.addSlotToContainer(SlotFiltered.takeOnly(furnace.inventory, 3, 136, 54));
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 104 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 162));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack rStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if(slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            rStack = stack.copy();

            if(index == 1) {
                if(!handleSmeltingTransfer(slot, stack, rStack, 4, this.inventorySlots.size())) {
                    return ItemStack.EMPTY;
                }
            }
            else if(index <= 3) {
                if(!this.mergeItemStack(stack, 4, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(stack, rStack);

            } else if(!this.mergeItemStack(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if(stack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return rStack;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return furnace.isUseableByPlayer(player);
    }
}
