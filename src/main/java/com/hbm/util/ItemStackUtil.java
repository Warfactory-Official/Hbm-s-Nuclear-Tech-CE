package com.hbm.util;

import com.hbm.inventory.RecipesCommon.ComparableStack;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemStackUtil {

	public static ItemStack carefulCopy(final ItemStack stack) {
		if(stack == null)
			return null;
		else
			return stack.copy();
	}

	/**
	 * Creates a new array that only contains the copied range.
	 * @param inv
	 * @param start
	 * @param end
	 * @return copied items
	 */
	@Nonnull
	public static ItemStack[] carefulCopyArrayTruncate(@Nonnull final IItemHandler inv, final int start, final int end) {
		if (end < start) {
			throw new IllegalArgumentException("end must be >= start");
		}

		final int length = end - start + 1;
		final ItemStack[] copy = new ItemStack[length];
		for (int idx = 0; idx < length; idx++) {
			copy[idx] = carefulCopy(inv.getStackInSlot(start + idx));
		}

		return copy;
	}

	public static ItemStack carefulCopyWithSize(final ItemStack stack, final int size) {
		if(stack == null)
			return null;

		final ItemStack copy = stack.copy();
		copy.setCount(size);
		return copy;
	}

	/**
	 * Runs carefulCopy over the entire ItemStack array.
	 * @param array
	 * @return
	 */
	public static ItemStack[] carefulCopyArray(final ItemStack[] array) {
		return carefulCopyArray(array, 0, array.length - 1);
	}

	/**
	 * Recreates the ItemStack array and only runs carefulCopy over the supplied range. All other fields remain null.
	 * @param array
	 * @param start
	 * @param end
	 * @return
	 */
	public static ItemStack[] carefulCopyArray(final ItemStack[] array, final int start, final int end) {
		if(array == null)
			return null;

		final ItemStack[] copy = new ItemStack[array.length];

		for(int i = start; i <= end; i++) {
			copy[i] = carefulCopy(array[i]);
		}

		return copy;
	}

	/**
	 * Creates a new array that only contains the copied range.
	 * @param array
	 * @param start
	 * @param end
	 * @return
	 */
	public static ItemStack[] carefulCopyArrayTruncate(final ItemStack[] array, final int start, final int end) {
		if(array == null)
			return null;

		final int length = end - start + 1;
		final ItemStack[] copy = new ItemStack[length];

		for(int i = 0; i < length; i++) {
			copy[i] = carefulCopy(array[start + i]);
		}

		return copy;
	}

	/**
	 * UNSAFE! Will ignore all existing display tags and override them! In its current state, only fit for items we know don't have any display tags!
	 * Will, however, respect existing NBT tags
	 * @param stack
	 * @param lines
	 */
	public static ItemStack addTooltipToStack(final ItemStack stack, final String... lines) {
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		final NBTTagCompound display = new NBTTagCompound();
		final NBTTagList lore = new NBTTagList();

		for(final String line : lines) {
			lore.appendTag(new NBTTagString(TextFormatting.RESET + "" + TextFormatting.GRAY + line));
		}

		display.setTag("Lore", lore);
		stack.getTagCompound().setTag("display", display);

		return stack;
	}

    /**
     * Automatically adds multistack labels for displays that use a ton of items (like construction recipe handlers).
     * @param stack
     * @return
     */
    public static ItemStack addStackSizeLabel(ItemStack stack) {

        if(stack.getCount() > 64) {
            int stacks = stack.getCount() / 64;
            int items = stack.getCount() % 64;
            addTooltipToStack(stack, TextFormatting.RED + "" + stacks + "x64" + (items > 0 ? (" + " + items) : ""));
        }

        return stack;
    }

	public static void addStacksToNBT(final ItemStack stack, final ItemStack... stacks) {

		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		final NBTTagList tags = new NBTTagList();

		for(int i = 0; i < stacks.length; i++) {
			if(stacks[i] != null) {
				final NBTTagCompound slotNBT = new NBTTagCompound();
				slotNBT.setByte("slot", (byte) i);
				stacks[i].writeToNBT(slotNBT);
				tags.appendTag(slotNBT);
			}
		}
		stack.getTagCompound().setTag("items", tags);
	}

    public static ItemStack[] readStacksFromNBT(ItemStack stack, int count) {
        if(!stack.hasTagCompound())
            return null;

        final NBTTagList list = stack.getTagCompound().getTagList("items", 10);
        if (count < 1)
            count = list.tagCount();

        final ItemStack[] stacks = new ItemStack[count];

        for(int i = 0; i < count; i++) {
            final NBTTagCompound slotNBT = list.getCompoundTagAt(i);
            final byte slot = slotNBT.getByte("slot");
            if(slot >= 0 && slot < stacks.length) {
                stacks[slot] = new ItemStack(slotNBT);
            }
        }

        return stacks;
    }

    public static ItemStack[] readStacksFromNBT(ItemStack stack) {
        return readStacksFromNBT(stack, 0);
    }

	/**
	 * Returns a List<String> of all ore dict names for this stack. Stack cannot be null, list is empty when there are no ore dict entries.
	 * @param stack
	 * @return
	 */
	public static List<String> getOreDictNames(final ItemStack stack) {
		if (stack == null || stack.isEmpty() || stack.getItem() == null) {
			return Collections.emptyList();
		}

		final List<String> list = new ArrayList<>();
		final int[] ids = OreDictionary.getOreIDs(stack);

		for (final int i : ids) {
			list.add(OreDictionary.getOreName(i));
		}

		return list;
	}

	public static boolean isSameMetaItem(final ItemStack stack1, final ItemStack stack2) {
		return stack1.getItem() == stack2.getItem() && stack1.getMetadata() == stack2.getMetadata();
	}

	public static boolean isSameMetaItem(final ItemStack stack, final Item item) {
		return stack.getItem() == item;
	}

	// ItemStack from Item

	public static ItemStack itemStackFrom(final Item item) {
		return new ItemStack(item);
	}

	public static ItemStack itemStackFrom(final Item item, final int amount) {
		return new ItemStack(item, amount);
	}

	public static ItemStack itemStackFrom(final Item item, final int amount, final int meta) {
		return new ItemStack(item, amount, meta);
	}

	// ItemStack from Block

	public static ItemStack itemStackFrom(final Block block) {
		return new ItemStack(block);
	}

	public static ItemStack itemStackFrom(final Block block, final int amount) {
		return new ItemStack(block, amount);
	}

	public static ItemStack itemStackFrom(final Block block, final int amount, final int meta) {
		return new ItemStack(block, amount, meta);
	}

	// ItemStack from ItemStack, required for MetaItems

	@Deprecated
	public static ItemStack itemStackFrom(final ItemStack stack) {
		return stack;
	}

	public static ItemStack itemStackFrom(final ItemStack stack, final int amount) {
		return new ItemStack(stack.getItem(), amount, stack.getMetadata());
	}

	// ComparableStack from Item

	public static ComparableStack comparableStackFrom(final Item item) {
		return new ComparableStack(item);
	}

	public static ComparableStack comparableStackFrom(final Item item, final int amount) {
		return new ComparableStack(item, amount);
	}

	public static ComparableStack comparableStackFrom(final Item item, final int amount, final int meta) {
		return new ComparableStack(item, amount, meta);
	}

	// ComparableStack from Block

	public static ComparableStack comparableStackFrom(final Block block) {
		return new ComparableStack(block);
	}

	public static ComparableStack comparableStackFrom(final Block block, final int amount) {
		return new ComparableStack(block, amount);
	}

	public static ComparableStack comparableStackFrom(final Block block, final int amount, final int meta) {
		return new ComparableStack(block, amount, meta);
	}

	// ComparableStack from ItemStack, required for MetaItems

	public static ComparableStack comparableStackFrom(final ItemStack stack) {
		return new ComparableStack(stack);
	}

	public static ComparableStack comparableStackFrom(final ItemStack stack, final int amount) {
		return new ComparableStack(stack.getItem(), amount, stack.getMetadata());
	}

	// ItemStack from NBTTagCompound

	public static ItemStack itemStackFrom(final NBTTagCompound compoundTag) {
		return new ItemStack(compoundTag);
	}

	public static boolean areStacksCompatible(ItemStack sta1, ItemStack sta2) {
		return sta1.getItem() == sta2.getItem() && sta1.getItemDamage() == sta2.getItemDamage() && ItemStack.areItemStackTagsEqual(sta1, sta2) && sta1.areCapsCompatible(sta2);
	}
}
