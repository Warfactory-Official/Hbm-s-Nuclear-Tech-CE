package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotCraftingOutput;
import com.hbm.inventory.slot.SlotSmelting;
import com.hbm.tileentity.machine.TileEntityFurnaceBrick;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerFurnaceBrick extends ContainerBase {

    private TileEntityFurnaceBrick furnace;

    public ContainerFurnaceBrick(InventoryPlayer invPlayer, TileEntityFurnaceBrick tedf) {
        super(invPlayer, tedf.inventory);
        furnace = tedf;

        //input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 62, 35));
        //fuel
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 35, 17));
        //output
        this.addSlotToContainer(new SlotSmelting(invPlayer.player, tedf.inventory, 2, 116, 35));
        //ash
        this.addSlotToContainer(new SlotCraftingOutput(invPlayer.player, tedf.inventory, 3, 35, 53));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack rStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if(slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            rStack = stack.copy();

            if (index == 2) {
                if(!handleSmeltingTransfer(slot, stack, rStack, 4, this.inventorySlots.size())) {
                    return ItemStack.EMPTY;
                }
            }
            else if (index <= 3) {
                if(!this.mergeItemStack(stack, 4, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {

                if(!TileEntityFurnace.isItemFuel(stack)) {
                    if(!InventoryUtil.mergeItemStack(this.inventorySlots, stack, 0, 1, false))
                        return ItemStack.EMPTY;
                } else {
                    if(!this.mergeItemStack(stack, 1, 2, false) && !this.mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
                }
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

