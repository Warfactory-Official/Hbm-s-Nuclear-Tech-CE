package com.hbm.inventory.container;

import com.hbm.api.item.IDesignatorItem;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.tileentity.bomb.TileEntityLaunchPadBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerLaunchPadLarge extends Container {
	
	private TileEntityLaunchPadBase launchpad;
	
	public ContainerLaunchPadLarge(InventoryPlayer invPlayer, TileEntityLaunchPadBase tedf) {
		
		launchpad = tedf;
		
		//Missile
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 26, 36));
		//Designator
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 26, 72));
		//Battery
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 2, 107, 90));
		//Fuel in
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 125, 90));
		//Fuel out
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 4, 125, 108));
		//Oxidizer in
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 143, 90));
		//Oxidizer out
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 6, 143, 108));
		
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 154 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 212));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack stackCopy = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if(slot != null && slot.getHasStack()) {
			ItemStack stack = slot.getStack();
			stackCopy = stack.copy();

			if(index <= 6) {
				if(!this.mergeItemStack(stack, 7, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {
				
				if(Library.isBattery(stackCopy)) {
					if(!this.mergeItemStack(stack, 2, 3, false)) {
						return ItemStack.EMPTY;
					}
				} else if(launchpad.isMissileValid(stackCopy)) {
					if(!this.mergeItemStack(stack, 0, 1, false)) {
						return ItemStack.EMPTY;
					}
				} else if(stackCopy.getItem() == ModItems.fluid_barrel_infinite) {
					if(!this.mergeItemStack(stack, 3, 4, false)) if(!this.mergeItemStack(stack, 5, 6, false)) {
						return ItemStack.EMPTY;
					}
				} else if(FluidContainerRegistry.getFluidContent(stackCopy, launchpad.tanks[0].getTankType()) > 0) {
					if(!this.mergeItemStack(stack, 3, 4, false)) {
						return ItemStack.EMPTY;
					}
				} else if(FluidContainerRegistry.getFluidContent(stackCopy, launchpad.tanks[1].getTankType()) > 0) {
					if(!this.mergeItemStack(stack, 5, 6, false)) {
						return ItemStack.EMPTY;
					}
				} else if(stackCopy.getItem() instanceof IDesignatorItem) {
					if(!this.mergeItemStack(stack, 1, 2, false)) {
						return ItemStack.EMPTY;
					}
				} else {
					return ItemStack.EMPTY;
				}
			}

			if(stack.getCount() == 0) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}

		return stackCopy;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return launchpad.isUseableByPlayer(player);
	}
}
