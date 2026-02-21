package com.hbm.creativetabs;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.items.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ControlTab extends CreativeTabs {

	public ControlTab(int index, String label) {
		super(index, label);
	}

	@Override
    @SideOnly(Side.CLIENT)
	public ItemStack createIcon() {
		if(ModItems.pellet_rtg != null){
			return new ItemStack(ModItems.pellet_rtg);
		}
		return new ItemStack(Items.IRON_PICKAXE, 1);
	}
	
	@Override
    @SideOnly(Side.CLIENT)
	public void displayAllRelevantItems(NonNullList<ItemStack> list) {
		super.displayAllRelevantItems(list);
		List<ItemStack> batteries = new ArrayList<>();

		for(ItemStack stack : list) {

			if(stack instanceof ItemStack) {

                if(stack.getItem() instanceof IBatteryItem) {
					batteries.add(stack);
				}
			}
		}

		for(ItemStack stack : batteries) {

			if(!(stack.getItem() instanceof IBatteryItem battery)) //shouldn't happen but just to make sure
				continue;

            ItemStack empty = stack.copy();
			ItemStack full = stack.copy();

			battery.setCharge(empty, 0);
			battery.setCharge(full, battery.getMaxCharge(full));

			int index = list.indexOf(stack);

			list.remove(index);
			list.add(index, full);
			//do not list empty versions of SU batteries
			if(battery.getChargeRate(stack) > 0)
				list.add(index, empty);
		}
	}
}
