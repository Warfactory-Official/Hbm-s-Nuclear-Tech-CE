package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.TileEntityMachineTurbineGas;

import com.hbm.api.energymk2.IBatteryItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineTurbineGas extends Container {
	
	private TileEntityMachineTurbineGas turbinegas;
	
	public ContainerMachineTurbineGas(InventoryPlayer invPlayer, TileEntityMachineTurbineGas te) {
		
		turbinegas = te;
		
		//Battery
		this.addSlotToContainer(new SlotBattery(te.inventory, 0, 8, 109));
		//Fluid ID
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 1, 36, 17));
		
		for(int i = 0; i < 3; i++) { 
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 141 + i * 18)); //player's inventory
			}
		}

		for(int i = 0; i < 9; i++) { 
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 199)); //shit in the hotbar
		}
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) { //shit for shift clicking that works and idk how
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if(slot != null && slot.getHasStack()) {
			ItemStack originalStack = slot.getStack();
			stack = originalStack.copy();

			if(index <= 1) { //checks if the item is in the battery or fluidID slot
				if(!this.mergeItemStack(originalStack, 2, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
				
			} else if(originalStack.getItem() instanceof IBatteryItem) { //only yeets batteries in the battery slot

				if(!this.mergeItemStack(originalStack, 0, 1, true))
					return ItemStack.EMPTY;
				
			} else if(originalStack.getItem() instanceof IItemFluidIdentifier identifier) {

				FluidType type = identifier.getType(player.world, turbinegas.getPos().getX(), turbinegas.getPos().getY(), turbinegas.getPos().getZ(), originalStack);
				if (type != Fluids.GAS && type != Fluids.PETROLEUM && type != Fluids.LPG ) //doesn't let you yeet random identifiers in the identifier slot
					return ItemStack.EMPTY;

				if(!this.mergeItemStack(originalStack, 1, 2, true))
					return ItemStack.EMPTY;
				
			} else {
				return ItemStack.EMPTY;
			}

			if(stack.isEmpty()) {
				slot.putStack((ItemStack.EMPTY));
			} else {
				slot.onSlotChanged();
			}
		}

		return stack;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return turbinegas.isUseableByPlayer(player);
	}
}