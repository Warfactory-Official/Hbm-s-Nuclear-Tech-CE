package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotSmelting;
import com.hbm.tileentity.machine.TileEntityFurnaceSteel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerFurnaceSteel extends ContainerBase {
	
	protected TileEntityFurnaceSteel furnace;
	
	public ContainerFurnaceSteel(InventoryPlayer invPlayer, TileEntityFurnaceSteel furnace) {
        super(invPlayer, furnace.inventory);
		this.furnace = furnace;

		//input
		this.addSlotToContainer(new SlotItemHandler(furnace.inventory, 0, 35, 17));
		this.addSlotToContainer(new SlotItemHandler(furnace.inventory, 1, 35, 35));
		this.addSlotToContainer(new SlotItemHandler(furnace.inventory, 2, 35, 53));
		//output
		this.addSlotToContainer(new SlotSmelting(invPlayer.player, furnace.inventory, 3, 125, 17));
		this.addSlotToContainer(new SlotSmelting(invPlayer.player, furnace.inventory, 4, 125, 35));
		this.addSlotToContainer(new SlotSmelting(invPlayer.player, furnace.inventory, 5, 125, 53));
		
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

            if(index >= 3 && index <= 5) {
                if(!handleSmeltingTransfer(slot, stack, rStack, 6, this.inventorySlots.size())) {
                    return ItemStack.EMPTY;
                }
            }
			else if(index < 3) {
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
