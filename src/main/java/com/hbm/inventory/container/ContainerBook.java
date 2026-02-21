package com.hbm.inventory.container;

import com.hbm.inventory.recipes.MagicRecipes;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class ContainerBook extends Container {

	public InventoryCrafting craftMatrix = new InventoryCrafting(this, 2, 2);
    public InventoryCraftResult craftResult = new InventoryCraftResult();
    public World world;
    public EntityPlayer player;
    
    public ContainerBook(InventoryPlayer inventory) {
    	this.world = inventory.player.world;
    	this.player = inventory.player;
    	this.addSlotToContainer(new SlotCrafting(inventory.player, this.craftMatrix, this.craftResult, 0, 124, 35){
    		@Override
    		public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack){
    			 NonNullList<ItemStack> nonnulllist = CraftingManager.getRemainingItems(craftMatrix, thePlayer.world);
    			 for(ItemStack sta : nonnulllist){
    				 sta.shrink(1);
    			 }
    			 onCraftMatrixChanged(craftMatrix);
    			return stack;
    		}
    	});

        for (int l = 0; l < 2; ++l) {
            for (int i1 = 0; i1 < 2; ++i1) {
                this.addSlotToContainer(new Slot(this.craftMatrix, i1 + l * 2, 30 + i1 * 36, 17 + l * 36));
            }
        }

        for(int l = 0; l < 3; ++l) {
            for (int i1 = 0; i1 < 9; ++i1) {
                this.addSlotToContainer(new Slot(inventory, i1 + l * 9 + 9, 8 + i1 * 18, 84 + l * 18));
            }
        }

        for(int l = 0; l < 9; ++l) {
            this.addSlotToContainer(new Slot(inventory, l, 8 + l * 18, 142));
        }

        this.onCraftMatrixChanged(this.craftMatrix);
	}
    
    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {
    	this.craftResult.setInventorySlotContents(0, MagicRecipes.getRecipe(this.craftMatrix));
    }
    
    @Override
    public void onContainerClosed(EntityPlayer player) {
    	super.onContainerClosed(player);

        if (!player.world.isRemote) {
        	this.clearContainer(player, player.world, this.craftMatrix);
        }
    }
    
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
    	ItemStack result = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            result = stack.copy();

            if (index == 0) {
                if (!this.mergeItemStack(stack, 10 - 5, 46 - 5, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(stack, result);
            }
            else if (index >= 10 - 5 && index < 37 - 5) {
                if (!this.mergeItemStack(stack, 37 - 5, 46 - 5, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (index >= 37 - 5 && index < 46 - 5) {
                if (!this.mergeItemStack(stack, 10 - 5, 37 - 5, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.mergeItemStack(stack, 10 - 5, 46 - 5, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            }
            else {
                slot.onSlotChanged();
            }

            if (stack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }

        return result;
    }
    
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return Library.hasInventoryItem(player.inventory, ModItems.book_of_);
	}
	
	@Override
	public boolean canMergeSlot(ItemStack stack, Slot slot) {
		return slot.inventory != this.craftResult && super.canMergeSlot(stack, slot);
	}

}
