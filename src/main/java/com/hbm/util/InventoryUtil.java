package com.hbm.util;

import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.recipes.anvil.AnvilRecipes.AnvilOutput;
import com.hbm.main.MainRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

//'t was about time
public class InventoryUtil {

	/**
	 * Will attempt to cram a much of the given itemstack into the stack array as possible
	 * The rest will be returned
	 * @param inv the stack array, usually a TE's inventory
	 * @param start the starting index (inclusive)
	 * @param end the end index (inclusive)
	 * @param stack the stack to be added to the inventory
	 * @return the remainder of the stack that could not have been added, can return null
	 */
	public static ItemStack tryAddItemToInventory(ItemStack[] inv, int start, int end, ItemStack stack) {

		ItemStack rem = tryAddItemToExistingStack(inv, start, end, stack);

		if(rem == null || rem.isEmpty())
			return ItemStack.EMPTY;

		boolean didAdd = tryAddItemToNewSlot(inv, start, end, rem);

		if(didAdd)
			return ItemStack.EMPTY;
		else
			return rem;
	}

	/**
	 * Functionally equal to tryAddItemToInventory, but will not try to create new stacks in empty slots
	 */
	public static ItemStack tryAddItemToExistingStack(ItemStack[] inv, int start, int end, ItemStack stack) {

		if(stack == null || stack.isEmpty())
			return ItemStack.EMPTY;

		for(int i = start; i <= end; i++) {

			if(doesStackDataMatch(inv[i], stack)) {

				int transfer = Math.min(stack.getCount(), inv[i].getMaxStackSize() - inv[i].getCount());

				if(transfer > 0) {
					inv[i].setCount(inv[i].getCount() + transfer);
					stack.setCount(stack.getCount() - transfer);

					if(stack.isEmpty())
						return ItemStack.EMPTY;
				}
			}
		}

		return stack;
	}

	/**
	 * Will place the stack in the first empty slot
	 * @return whether the stack could be added or not
	 */
	public static boolean tryAddItemToNewSlot(ItemStack[] inv, int start, int end, ItemStack stack) {

		if(stack == null || stack.isEmpty())
			return true;

		for(int i = start; i <= end; i++) {

			if(inv[i] == null || inv[i].isEmpty()) {
				inv[i] = stack;
				return true;
			}
		}

		return false;
	}

	/**
	 * Much of the same but with an ISidedInventory instance instead of a slot array
	 * @param stack This is not modified!
	 */
	public static ItemStack tryAddItemToInventory(IItemHandler inv, int start, int end, ItemStack stack) {
		ItemStack remaining = stack;
		for(int slot = start; slot <= end; slot++) {
			remaining = inv.insertItem(slot, remaining, false);
			if (remaining.isEmpty())
				return ItemStack.EMPTY;
		}
		return remaining;
	}

	/**
	 * Compares item, metadata and NBT data of two stacks. Also handles null values!
	 */
	public static boolean doesStackDataMatch(ItemStack stack1, ItemStack stack2) {

		if(stack1 == null && stack2 == null)
			return true;

		if(stack1 == null)
			return false;

		if(stack2 == null)
			return false;

		if(stack1.getItem() != stack2.getItem())
			return false;

		if(stack1.getItemDamage() != stack2.getItemDamage())
			return false;

		if(!stack1.hasTagCompound() && !stack2.hasTagCompound())
			return true;

		if(stack1.hasTagCompound() && !stack2.hasTagCompound())
			return false;

		if(!stack1.hasTagCompound() && stack2.hasTagCompound())
			return false;

		return stack1.getTagCompound().equals(stack2.getTagCompound());
	}

	public static int countAStackMatches(ItemStack[] inventory, AStack stack, boolean ignoreSize) {
		int count = 0;

		for(ItemStack itemStack : inventory) {
			if(!itemStack.isEmpty()) {
				if(stack.matchesRecipe(itemStack, true)) {
					count += itemStack.getCount();
				}
			}
		}
		return ignoreSize ? count : count / stack.stacksize;
	}

	public static int countAStackMatches(EntityPlayer player, AStack stack, boolean ignoreSize) {
		ItemStack[] inventoryArray = new ItemStack[player.inventory.mainInventory.size()];
		for(int i = 0; i < player.inventory.mainInventory.size(); i++) {
			inventoryArray[i] = player.inventory.mainInventory.get(i);
		}
		return countAStackMatches(inventoryArray, stack, ignoreSize);
	}

	public static boolean doesPlayerHaveAStack(EntityPlayer player, AStack stack, boolean shouldRemove, boolean ignoreSize) {
		ItemStack[] inventoryArray = new ItemStack[player.inventory.mainInventory.size()];
		for(int i = 0; i < player.inventory.mainInventory.size(); i++) {
			inventoryArray[i] = player.inventory.mainInventory.get(i);
		}
		return doesInventoryHaveAStack(inventoryArray, stack, shouldRemove, ignoreSize);
	}

	public static boolean doesInventoryHaveAStack(ItemStack[] inventory, AStack stack, boolean shouldRemove, boolean ignoreSize) {
		final int totalMatches;
		int totalStacks = 0;
		for(ItemStack itemStack : inventory) {
			if(itemStack != null && stack.matchesRecipe(itemStack, ignoreSize))
				totalStacks += itemStack.getCount();
			if(!shouldRemove && ignoreSize && totalStacks > 0)
				return true;
		}

		totalMatches = ignoreSize ? totalStacks : totalStacks / stack.stacksize;

		if(shouldRemove) {
			int consumedStacks = 0, requiredStacks = ignoreSize ? 1 : stack.stacksize;
			for(ItemStack itemStack : inventory) {
				if(consumedStacks > requiredStacks)
					break;
				if(itemStack != null && stack.matchesRecipe(itemStack, true)) {
					int toConsume = Math.min(itemStack.getCount(), requiredStacks - consumedStacks);
					itemStack.shrink(toConsume);
					consumedStacks += toConsume;
				}
			}
		}

		return totalMatches > 0;
	}
	
	/**
	 * Checks if a player has matching item stacks in his inventory and removes them if so desired
	 * @param player the player whose inventory to check
	 * @param stacks the AStacks (comparable or ore-dicted)
	 * @param shouldRemove whether it should just return true or false or if a successful check should also remove all the items
	 * @return whether the player has the required item stacks or not
	 */
	public static boolean doesPlayerHaveAStacks(EntityPlayer player, List<AStack> stacks, boolean shouldRemove) {
		
		NonNullList<ItemStack> original = player.inventory.mainInventory;
		ItemStack[] inventory = new ItemStack[original.size()];
		AStack[] input = new AStack[stacks.size()];
		
		//first we copy the inputs into an array because 1. it's easier to deal with and 2. we can dick around with the stack sized with no repercussions
		for(int i = 0; i < input.length; i++) {
			input[i] = stacks.get(i).copy();
		}
		
		//then we copy the inventory so we can dick around with it as well without making actual modifications to the player's inventory
		for(int i = 0; i < original.size(); i++) {
			inventory[i] = original.get(i).copy();
		}
		
		//now we go through every ingredient...
		for(int i = 0; i < input.length; i++) {
			
			AStack stack = input[i];
			
			//...and compare each ingredient to every stack in the inventory
			for(int j = 0; j < inventory.length; j++) {
				
				ItemStack inv = inventory[j];
				
				//we check if it matches but ignore stack size for now
				if(stack.matchesRecipe(inv, true)) {
					//and NOW we care about the stack size
					int size = Math.min(stack.count(), inv.getCount());
					stack.setCount(stack.count()-size);
					inv.setCount(inv.getCount()-size);
					
					//spent stacks are removed from the equation so that we don't cross ourselves later on
					if(stack.count() <= 0) {
						input[i] = null;
						break;
					}
					
					if(inv.getCount() <= 0) {
						inventory[j] = ItemStack.EMPTY;
					}
				}
			}
		}
		
		for(AStack stack : input) {
			if(stack != null) {
				return false;
			}
		}
		
		if(shouldRemove) {
			for(int i = 0; i < original.size(); i++) {
				if(inventory[i] != null && inventory[i].getCount() <= 0)
					original.set(i, ItemStack.EMPTY);
				else
					original.set(i, inventory[i]);
			}
		}
		
		return true;
	}
	
	public static void giveChanceStacksToPlayer(EntityPlayer player, List<AnvilOutput> stacks) {
		
		for(AnvilOutput out : stacks) {
			if(out.chance == 1.0F || player.getRNG().nextFloat() < out.chance) {
				if(!player.inventory.addItemStackToInventory(out.stack.copy())) {
					player.dropItem(out.stack.copy(), false);
				}
			}
		}
	}
	
	public static boolean hasOreDictMatches(EntityPlayer player, String dict, int count) {
		return countOreDictMatches(player, dict) >= count;
	}
	
	public static int countOreDictMatches(EntityPlayer player, String dict) {
		
		int count = 0;
		
		for(int i = 0; i < player.inventory.mainInventory.size(); i++) {
			
			ItemStack stack = player.inventory.mainInventory.get(i);
			
			if(!stack.isEmpty()) {
				
				int[] ids = OreDictionary.getOreIDs(stack);
				
				for(int id : ids) {
					if(OreDictionary.getOreName(id).equals(dict)) {
						count += stack.getCount();
						break;
					}
				}
			}
		}
		
		return count;
	}
	
	public static void consumeOreDictMatches(EntityPlayer player, String dict, int count) {
		
		for(int i = 0; i < player.inventory.mainInventory.size(); i++) {
			
			ItemStack stack = player.inventory.mainInventory.get(i);
			
			if(!stack.isEmpty()) {
				
				int[] ids = OreDictionary.getOreIDs(stack);
				
				for(int id : ids) {
					if(OreDictionary.getOreName(id).equals(dict)) {
						
						int toConsume = Math.min(count, stack.getCount());
						player.inventory.decrStackSize(i, toConsume);
						count -= toConsume;
						break;
					}
				}
			}
		}
	}

	/**
	 * Turns objects into 2D ItemStack arrays. Index 1: Ingredient slot, index 2: variation (ore dict)
	 * Handles:<br>
	 * <ul>
	 * <li>ItemStack</li>
	 * <li>ItemStack[]</li>
	 * <li>AStack</li>
	 * <li>AStack[]</li>
	 * </ul>
	 */
	public static ItemStack[][] extractObject(Object o) {

		if(o instanceof ItemStack) {
			ItemStack[][] stacks = new ItemStack[1][1];
			stacks[0][0] = ((ItemStack)o).copy();
			return stacks;
		}

		if(o instanceof ItemStack[] ingredients) {
            ItemStack[][] stacks = new ItemStack[ingredients.length][1];
			for(int i = 0; i < ingredients.length; i++) {
				stacks[i][0] = ingredients[i];
			}
			return stacks;
		}

		if(o instanceof ItemStack[][]) {
			return (ItemStack[][]) o;
		}

		if(o instanceof AStack astack) {
            ItemStack[] ext = astack.extractForJEI().toArray(new ItemStack[0]);
			ItemStack[][] stacks = new ItemStack[1][0];
			stacks[0] = ext; //untested, do java arrays allow that? the capacity set is 0 after all
			return stacks;
		}

		if(o instanceof AStack[] ingredients) {
            ItemStack[][] stacks = new ItemStack[ingredients.length][0];

			for(int i = 0; i < ingredients.length; i++) {
				stacks[i] = ingredients[i].extractForJEI().toArray(new ItemStack[0]);
			}

			return stacks;
		}

		/* in emergency situations with mixed types where AStacks coexist with NBT dependent ItemStacks, such as for fluid icons */
		if(o instanceof Object[] ingredients) {
            ItemStack[][] stacks = new ItemStack[ingredients.length][0];

			for(int i = 0; i < ingredients.length; i++) {
				Object ingredient = ingredients[i];

				if(ingredient instanceof AStack) {
					stacks[i] = ((AStack) ingredient).extractForJEI().toArray(new ItemStack[0]);
				}
				if(ingredient instanceof ItemStack) {
					stacks[i] = new ItemStack[1];
					stacks[i][0] = ((ItemStack) ingredient).copy();
				}
			}

			return stacks;
		}

        MainRegistry.logger.warn("InventoryUtil: extractObject failed for type {}", o);
		return new ItemStack[0][0];
	}

	public static boolean doesArrayHaveIngredients(IItemHandler inv, int start, int end, List<AStack> ingredients) {
		ItemStack[] copy = ItemStackUtil.carefulCopyArrayTruncate(inv, start, end);
		AStack[] req = new AStack[ingredients.size()];
		for (int idx = 0; idx < req.length; ++idx) {
			req[idx] = ingredients.get(idx) == null ? null : ingredients.get(idx).copy();
		}

		for (AStack ingredient : req) {
			if (ingredient == null) {
				continue;
			}

			for (ItemStack input : copy) {
				if (input == null || input.isEmpty()) {
					continue;
				}

				if (ingredient.matchesRecipe(input, true)) {
					int size = Math.min(input.getCount(), ingredient.count());
					ingredient.setCount(ingredient.count() - size);
					input.setCount(input.getCount() - size);

					if (ingredient.count() == 0) {
						break;
					}
				}
			}

			if (ingredient.count() > 0) {
				return false;
			}
		}

		return true;
	}

	public static boolean doesArrayHaveIngredients(IItemHandler inv, int start, int end, AStack... ingredients) {
		return doesArrayHaveIngredients(inv, start, end, Arrays.asList(ingredients));
	}

	public static boolean doesArrayHaveSpace(IItemHandler inv, int start, int end, ItemStack[] items) {
		ItemStack[] copy = ItemStackUtil.carefulCopyArrayTruncate(inv, start, end);

		for (ItemStack item : items) {
			if (item == null || item.isEmpty()) {
				continue;
			}

			ItemStack remainder = tryAddItemToInventory(copy, 0, copy.length - 1, item.copy());
			if (!remainder.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	public static boolean tryConsumeAStack(IItemHandlerModifiable inv, int start, int end, AStack stack) {
		AStack copy = stack.copy();
		for (int i = start; i <= end; ++i) {
			ItemStack input = inv.getStackInSlot(i);

			if (stack.matchesRecipe(input, true)) {
				int size = Math.min(copy.count(), input.getCount());
				inv.extractItem(i, size, false);
				copy.setCount(copy.count() - size);

				if (copy.count() == 0) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * A fixed re-implementation of the original Container.mergeItemStack that respects stack size and slot restrictions.
	 */
	public static boolean mergeItemStack(List<Slot> slots, ItemStack stack, int start, int end, boolean reverse) {

		boolean success = false;
		int index = start;

		if (reverse) {
			index = end - 1;
		}

		Slot slot;
		ItemStack current;

		if (stack.isStackable()) {

			while (stack.getCount() > 0 && (!reverse && index < end || reverse && index >= start)) {
				slot = slots.get(index);
				current = slot.getStack();

				if (!current.isEmpty()) {
					int max = Math.min(stack.getMaxStackSize(), slot.getSlotStackLimit());
					int toRemove = Math.min(stack.getCount(), max);

					if (slot.isItemValid(ItemStackUtil.carefulCopyWithSize(stack, toRemove)) && current.getItem() == stack.getItem() &&
							(!stack.getHasSubtypes() || stack.getItemDamage() == current.getItemDamage()) && ItemStack.areItemStackTagsEqual(stack, current)) {

						int currentSize = current.getCount() + stack.getCount();
						if (currentSize <= max) {
							stack.setCount(0);
							current.setCount(currentSize);
							slot.putStack(current);
							success = true;
						} else if (current.getCount() < max) {
							stack.shrink(max - current.getCount());
							current.setCount(max);
							slot.putStack(current);
							success = true;
						}
					}
				}

				if (reverse) {
					--index;
				} else {
					++index;
				}
			}
		}

		if (stack.getCount() > 0) {
			if (reverse) {
				index = end - 1;
			} else {
				index = start;
			}

			while ((!reverse && index < end || reverse && index >= start) && stack.getCount() > 0) {
				slot = slots.get(index);
				current = slot.getStack();

				if (current.isEmpty()) {

					int max = Math.min(stack.getMaxStackSize(), slot.getSlotStackLimit());
					int toRemove = Math.min(stack.getCount(), max);

					if (slot.isItemValid(ItemStackUtil.carefulCopyWithSize(stack, toRemove))) {
						current = stack.splitStack(toRemove);
						slot.putStack(current);
						success = true;
					}
				}

				if (reverse) {
					--index;
				} else {
					++index;
				}
			}
		}

		return success;
	}

    public static boolean hasItem(EntityPlayer player, Item item) {
        for (ItemStack stack : player.inventory.mainInventory) {
            if(stack.getItem() == item) return true;
        }
        for (ItemStack stack : player.inventory.armorInventory) {
            if(stack.getItem() == item) return true;
        }
        for (ItemStack stack : player.inventory.offHandInventory) {
            if(stack.getItem() == item) return true;
        }
        return false;
    }

    /**
     * Common implementation of transferStackInSlot, for use in containers with no special slots.
     * @param maxSlots How many slots are in the container that don't belong to the player's inventory
     */
    public static ItemStack transferStack(List<Slot> slots, int index, int maxSlots, boolean callOnTake, EntityPlayer player) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack stack = slot.getStack();
            result = stack.copy();

            if (index < maxSlots) {
                if (!mergeItemStack(slots, stack, maxSlots + 1, slots.size(), true)) return ItemStack.EMPTY;
            }

            else if (!mergeItemStack(slots, stack, 0, maxSlots, false)) return ItemStack.EMPTY;

            if (stack.getCount() == 0) slot.putStack(ItemStack.EMPTY);
            else slot.onSlotChanged();

            if (callOnTake) slot.onTake(player, stack);
        }

        return result;
    }

    public static ItemStack transferStack(List<Slot> slots, int index, int maxSlots) {
        return transferStack(slots, index, maxSlots, false, null);
    }

    /**
     * Common implementation of transferStackInSlot, for use in containers with one type of special slot.
     * @param p1 A predicate that checks if the stack to transfer should go in the special slot.
     * @param s1 The index of the last special slot. All slots from 0 - s1 will count as special.
     */
    public static ItemStack transferStack(List<Slot> slots, int index, int maxSlots, Predicate<ItemStack> p1, int s1) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack stack = slot.getStack();
            result = stack.copy();

            if (index < maxSlots) {
                if (!mergeItemStack(slots, stack, maxSlots + 1, slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (p1.test(stack) && !mergeItemStack(slots, stack, 0, s1, false)) return ItemStack.EMPTY;
                else if (!mergeItemStack(slots, stack, s1, maxSlots, false)) return ItemStack.EMPTY;
            }

            if (stack.getCount() == 0) slot.putStack(ItemStack.EMPTY);
            else slot.onSlotChanged();
        }

        return result;
    }

    /**
     * Common implementation of transferStackInSlot, for use in containers with two types of special slots.
     * @param p1 A predicate that checks if the stack to transfer should go in the first special slot.
     * @param s1 The index of the last slot of the first special slots. All slots from 0 - s1 will count as special.
     * @param p2 A predicate that checks if the stack to transfer should go in the second special slot. Done after p1 is checked.
     * @param s2 The index of the last slot of the second special slots. All slots from s1 - s2 will count as special.
     */
    public static ItemStack transferStack(List<Slot> slots, int index, int maxSlots, Predicate<ItemStack> p1, int s1, Predicate<ItemStack> p2, int s2) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack stack = slot.getStack();
            result = stack.copy();

            if (index < maxSlots) {
                if (!mergeItemStack(slots, stack, maxSlots + 1, slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (p1.test(stack) && !mergeItemStack(slots, stack, 0, s1, false)) return ItemStack.EMPTY;
                else if (p2.test(stack) && !mergeItemStack(slots, stack, s1, s2, false)) return ItemStack.EMPTY;
                else if (!mergeItemStack(slots, stack, s2, maxSlots, false)) return ItemStack.EMPTY;
            }

            if (stack.getCount() == 0) slot.putStack(ItemStack.EMPTY);
            else slot.onSlotChanged();
        }

        return result;
    }

    /**
     * Common implementation of transferStackInSlot, for use in containers with three types of special slots.
     * @param p1 A predicate that checks if the stack to transfer should go in the first special slot.
     * @param s1 The index of the last slot of the first special slots. All slots from 0 - s1 will count as special.
     * @param p2 A predicate that checks if the stack to transfer should go in the second special slot. Done after p1 is checked.
     * @param s2 The index of the last slot of the second special slots. All slots from s1 - s2 will count as special.
     * @param p3 A predicate that checks if the stack to transfer should go in the third special slot. Done after p2 is checked.
     * @param s3 The index of the last slot of the third special slots. All slots from s2 - s3 will count as special.
     */
    public static ItemStack transferStack(List<Slot> slots, int index, int maxSlots, Predicate<ItemStack> p1, int s1, Predicate<ItemStack> p2, int s2, Predicate<ItemStack> p3, int s3) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = slots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack stack = slot.getStack();
			result = stack.copy();
			int originalCount = stack.getCount();

			if (index < maxSlots) {
				if (!mergeItemStack(slots, stack, maxSlots, slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			}
			else {
				boolean moved = false;

				if (p1.test(stack)) {
					moved = mergeItemStack(slots, stack, 0, s1, false);
				}

				if (!moved && p2.test(stack)) {
					moved = mergeItemStack(slots, stack, s1, s2, false);
				}

				if (!moved && p3.test(stack)) {
					moved = mergeItemStack(slots, stack, s2, s3, false);
				}

				if (!moved && !mergeItemStack(slots, stack, s3, maxSlots, false)) {
					if (index < maxSlots + 27) {
						if (!mergeItemStack(slots, stack, maxSlots + 27, slots.size(), false)) return ItemStack.EMPTY;
					} else {
						if (!mergeItemStack(slots, stack, maxSlots, maxSlots + 27, false)) return ItemStack.EMPTY;
					}
				}
			}

			if (stack.getCount() == originalCount) {
				return ItemStack.EMPTY;
			}

			if (stack.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}

		return result;
    }

    /**
     * Common implementation of transferStackInSlot, for use in containers with four types of special slots.
     * @param p1 A predicate that checks if the stack to transfer should go in the first special slot.
     * @param s1 The index of the last slot of the first special slots. All slots from 0 - s1 will count as special.
     * @param p2 A predicate that checks if the stack to transfer should go in the second special slot. Done after p1 is checked.
     * @param s2 The index of the last slot of the second special slots. All slots from s1 - s2 will count as special.
     * @param p3 A predicate that checks if the stack to transfer should go in the third special slot. Done after p2 is checked.
     * @param s3 The index of the last slot of the third special slots. All slots from s2 - s3 will count as special.
     * @param p4 A predicate that checks if the stack to transfer should go in the fourth special slot. Done after p3 is checked.
     * @param s4 The index of the last slot of the fourth special slots. All slots from s3 - s4 will count as special.
     */
    public static ItemStack transferStack(List<Slot> slots, int index, int maxSlots, Predicate<ItemStack> p1, int s1, Predicate<ItemStack> p2, int s2, Predicate<ItemStack> p3, int s3, Predicate<ItemStack> p4, int s4) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack stack = slot.getStack();
            result = stack.copy();

            if (index < maxSlots) {
                if (!mergeItemStack(slots, stack, maxSlots + 1, slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (p1.test(stack) && !mergeItemStack(slots, stack, 0, s1, false)) return ItemStack.EMPTY;
                else if (p2.test(stack) && !mergeItemStack(slots, stack, s1, s2, false)) return ItemStack.EMPTY;
                else if (p3.test(stack) && !mergeItemStack(slots, stack, s2, s3, false)) return ItemStack.EMPTY;
                else if (p4.test(stack) && !mergeItemStack(slots, stack, s3, s4, false)) return ItemStack.EMPTY;
                else if (!mergeItemStack(slots, stack, s4, maxSlots, false)) return ItemStack.EMPTY;
            }

            if (stack.getCount() == 0) slot.putStack(ItemStack.EMPTY);
            else slot.onSlotChanged();
        }

        return result;
    }

    /**
     * Common implementation of transferStackInSlot, for use in containers with five types of special slots.
     * I think you can figure out what the parameters do
     */
    public static ItemStack transferStack(List<Slot> slots, int index, int maxSlots, Predicate<ItemStack> p1, int s1, Predicate<ItemStack> p2, int s2, Predicate<ItemStack> p3, int s3, Predicate<ItemStack> p4, int s4, Predicate<ItemStack> p5, int s5) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack stack = slot.getStack();
            result = stack.copy();

            if (index < maxSlots) {
                if (!mergeItemStack(slots, stack, maxSlots + 1, slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (p1.test(stack) && !mergeItemStack(slots, stack, 0, s1, false)) return ItemStack.EMPTY;
                else if (p2.test(stack) && !mergeItemStack(slots, stack, s1, s2, false)) return ItemStack.EMPTY;
                else if (p3.test(stack) && !mergeItemStack(slots, stack, s2, s3, false)) return ItemStack.EMPTY;
                else if (p4.test(stack) && !mergeItemStack(slots, stack, s3, s4, false)) return ItemStack.EMPTY;
                else if (p5.test(stack) && !mergeItemStack(slots, stack, s4, s5, false)) return ItemStack.EMPTY;
                else if (!mergeItemStack(slots, stack, s5, maxSlots, false)) return ItemStack.EMPTY;
            }

            if (stack.getCount() == 0) slot.putStack(ItemStack.EMPTY);
            else slot.onSlotChanged();
        }

        return result;
    }

    /**
     * Common implementation of transferStackInSlot, for use in containers with six types of special slots.
     */
    public static ItemStack transferStack(List<Slot> slots, int index, int maxSlots, Predicate<ItemStack> p1, int s1, Predicate<ItemStack> p2, int s2, Predicate<ItemStack> p3, int s3, Predicate<ItemStack> p4, int s4, Predicate<ItemStack> p5, int s5, Predicate<ItemStack> p6, int s6) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack stack = slot.getStack();
            result = stack.copy();

            if (index < maxSlots) {
                if (!mergeItemStack(slots, stack, maxSlots + 1, slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (p1.test(stack) && !mergeItemStack(slots, stack, 0, s1, false)) return ItemStack.EMPTY;
                else if (p2.test(stack) && !mergeItemStack(slots, stack, s1, s2, false)) return ItemStack.EMPTY;
                else if (p3.test(stack) && !mergeItemStack(slots, stack, s2, s3, false)) return ItemStack.EMPTY;
                else if (p4.test(stack) && !mergeItemStack(slots, stack, s3, s4, false)) return ItemStack.EMPTY;
                else if (p5.test(stack) && !mergeItemStack(slots, stack, s4, s5, false)) return ItemStack.EMPTY;
                else if (p6.test(stack) && !mergeItemStack(slots, stack, s5, s6, false)) return ItemStack.EMPTY;
                else if (!mergeItemStack(slots, stack, s6, maxSlots, false)) return ItemStack.EMPTY;
            }

            if (stack.getCount() == 0) slot.putStack(ItemStack.EMPTY);
            else slot.onSlotChanged();
        }

        return result;
    }

    /**
     * Common implementation of transferStackInSlot, for use in containers with six types of special slots.
     */
    public static ItemStack transferStack(List<Slot> slots, int index, int maxSlots, Predicate<ItemStack> p1, int s1, Predicate<ItemStack> p2, int s2, Predicate<ItemStack> p3, int s3, Predicate<ItemStack> p4, int s4, Predicate<ItemStack> p5, int s5, Predicate<ItemStack> p6, int s6, Predicate<ItemStack> p7, int s7) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack stack = slot.getStack();
            result = stack.copy();

            if (index < maxSlots) {
                if (!mergeItemStack(slots, stack, maxSlots + 1, slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (p1.test(stack) && !mergeItemStack(slots, stack, 0, s1, false)) return ItemStack.EMPTY;
                else if (p2.test(stack) && !mergeItemStack(slots, stack, s1, s2, false)) return ItemStack.EMPTY;
                else if (p3.test(stack) && !mergeItemStack(slots, stack, s2, s3, false)) return ItemStack.EMPTY;
                else if (p4.test(stack) && !mergeItemStack(slots, stack, s3, s4, false)) return ItemStack.EMPTY;
                else if (p5.test(stack) && !mergeItemStack(slots, stack, s4, s5, false)) return ItemStack.EMPTY;
                else if (p6.test(stack) && !mergeItemStack(slots, stack, s5, s6, false)) return ItemStack.EMPTY;
                else if (p7.test(stack) && !mergeItemStack(slots, stack, s6, s7, false)) return ItemStack.EMPTY;
                else if (!mergeItemStack(slots, stack, s7, maxSlots, false)) return ItemStack.EMPTY;
            }

            if (stack.getCount() == 0) slot.putStack(ItemStack.EMPTY);
            else slot.onSlotChanged();
        }

        return result;
    }
}