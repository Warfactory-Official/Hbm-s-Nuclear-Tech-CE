package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotSmelting;
import com.hbm.tileentity.machine.TileEntityFurnaceIron;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerFurnaceIron extends ContainerBase {
	
	protected TileEntityFurnaceIron furnace;
	
	public ContainerFurnaceIron(InventoryPlayer invPlayer, TileEntityFurnaceIron furnace) {
        super(invPlayer, furnace.inventory);
		this.furnace = furnace;

		//input
		this.addSlotToContainer(new SlotItemHandler(furnace.inventory, 0, 53, 17));
		//fuel
		this.addSlotToContainer(new SlotItemHandler(furnace.inventory, 1, 53, 53));
		this.addSlotToContainer(new SlotItemHandler(furnace.inventory, 2, 71, 53));
		//output
		this.addSlotToContainer(new SlotSmelting(invPlayer.player, furnace.inventory, 3, 125, 35));
		//upgrade
		this.addSlotToContainer(new SlotItemHandler(furnace.inventory, 4, 17, 35));
		
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

            if (index == 3) {
                if(!handleSmeltingTransfer(slot, stack, rStack, 6, this.inventorySlots.size())) {
                    return ItemStack.EMPTY;
                }
            }
			else if(index <= 5) {
				if(!this.mergeItemStack(stack, 6, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
				
				slot.onSlotChange(stack, rStack);
				
			} else if(!this.mergeItemStack(stack, 0, 3, false)) {
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
