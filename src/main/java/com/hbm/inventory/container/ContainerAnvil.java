package com.hbm.inventory.container;

import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import com.hbm.inventory.recipes.anvil.AnvilSmithingRecipe;
import com.hbm.inventory.slot.SlotMachineOutputVanilla;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;

public class ContainerAnvil extends Container {
	
	public InventoryBasic input = new InventoryBasic("Input", false, 8);
	public IInventory output = new InventoryCraftResult();
	
	public int tier; //because we can't trust these rascals with their packets
	
	public ContainerAnvil(InventoryPlayer inventory, int tier) {
		this.tier = tier;
		
		this.addSlotToContainer(new SmithingSlot(input, 0, 17, 27));
		this.addSlotToContainer(new SmithingSlot(input, 1, 53, 27));
		this.addSlotToContainer(new SlotMachineOutputVanilla(output, 0, 89, 27) {
			
			@Override
			public ItemStack onTake(EntityPlayer player, ItemStack stack) {
				
				ItemStack left = ContainerAnvil.this.input.getStackInSlot(0);
				ItemStack right = ContainerAnvil.this.input.getStackInSlot(1);
				
				if(left.isEmpty() || right.isEmpty()) {
					return stack;
				}
				
				for(AnvilSmithingRecipe rec : AnvilRecipes.getSmithing()) {
					
					int i = rec.matchesInt(left, right);
					
					if(i != -1) {
						ContainerAnvil.this.input.decrStackSize(0, rec.amountConsumed(0, i == 1));
						ContainerAnvil.this.input.decrStackSize(1, rec.amountConsumed(1, i == 1));
						ContainerAnvil.this.updateSmithing();
						return stack;
					}
				}
				return super.onTake(player, stack);
			}
			
		});
		
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + 56));
			}
		}
		
		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(inventory, i, 8 + i * 18, 142 + 56));
		}
		
		this.onCraftMatrixChanged(this.input);
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int par2) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(par2);
		
		if (slot != null && slot.getHasStack()) {
			ItemStack stack = slot.getStack();
			result = stack.copy();
			
			if (par2 == 2) {
				if (!this.mergeItemStack(stack, 3, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
				slot.onSlotChange(stack, result);
			} else if (par2 <= 1) {
				if (!this.mergeItemStack(stack, 3, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				} else {
					slot.onTake(player, stack);
				}
			} else {
				if (!this.mergeItemStack(stack, 0, 2, false))
					return ItemStack.EMPTY;
			}
			
			if (stack.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
			
			slot.onTake(player, stack);
		}
		
		return result;
	}
	
	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		
		if(!player.world.isRemote) {
			for(int i = 0; i < this.input.getSizeInventory(); ++i) {
				ItemStack itemstack = this.input.getStackInSlot(i);
				
				if(!itemstack.isEmpty()) {
					player.dropItem(itemstack, false);
				}
			}
		}
	}
	
	public class SmithingSlot extends Slot {
		
		public SmithingSlot(IInventory inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}
		
		public void onSlotChanged() {
			super.onSlotChanged();
			ContainerAnvil.this.updateSmithing();
		}
		
		@Override
		public void putStack(ItemStack stack) {
			super.putStack(stack);
		}
		
		@Override
		public ItemStack onTake(EntityPlayer player, ItemStack stack) {
			ContainerAnvil.this.updateSmithing();
			return super.onTake(player, stack);
		}
	}
	
	private void updateSmithing() {
		
		ItemStack left = this.input.getStackInSlot(0);
		ItemStack right = this.input.getStackInSlot(1);
		
		if(left.isEmpty() || right.isEmpty()) {
			this.output.setInventorySlotContents(0, ItemStack.EMPTY);
			return;
		}
		
		for(AnvilSmithingRecipe rec : AnvilRecipes.getSmithing()) {
			
			if(rec.matches(left, right) && rec.tier <= this.tier) {
				this.output.setInventorySlotContents(0, rec.getOutput(left, right));
				return;
			}
		}
		
		this.output.setInventorySlotContents(0, ItemStack.EMPTY);
	}
}