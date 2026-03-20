package com.hbm.inventory.recipes.anvil;

import com.hbm.inventory.RecipesCommon.ComparableStack;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class AnvilSmithingRenameRecipe extends AnvilSmithingRecipe {
	
	public AnvilSmithingRenameRecipe() {
		super(1, new ItemStack(Items.IRON_SWORD), new ComparableStack(Items.IRON_SWORD), new ComparableStack(Items.NAME_TAG, 1));
	}
	
	@Override
	public boolean matches(ItemStack left, ItemStack right) {
		return doesStackMatch(right, this.right) && getDisplayName(right) != null;
	}

	@Override
	public int matchesInt(ItemStack left, ItemStack right) {
		return matches(left, right) ? 0 : -1;
	}
	
	@Override
	public ItemStack getOutput(ItemStack left, ItemStack right) {
		
		ItemStack out = left.copy();
		out.setCount(1);
		
		String name = getDisplayName(right);
				
		if(name != null) {
			name = name.replace("\\&", "§");
			out.setStackDisplayName("§r" + name);
		}
		
		return out;
	}

	@Override
	public int amountConsumed(int index, boolean mirrored) {
		if(index == 0)
			return mirrored ? 0 : left.stacksize;
		if(index == 1)
			return mirrored ? left.stacksize : 0;

		return 0;
	}

	public String getDisplayName(ItemStack stack) {
		String s = null;

		if(stack.getTagCompound() != null && stack.getTagCompound().hasKey("display", 10)) {
			NBTTagCompound nbttagcompound = stack.getTagCompound().getCompoundTag("display");

			if(nbttagcompound.hasKey("Name", 8)) {
				s = nbttagcompound.getString("Name");
			}
		}

		return s;
	}
}