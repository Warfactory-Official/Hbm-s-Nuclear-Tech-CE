package com.hbm.creativetabs;

import com.hbm.items.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TemplateTab extends CreativeTabs {

	public TemplateTab(int index, String label) {
		super(index, label);
	}

	@Override
    @SideOnly(Side.CLIENT)
	public ItemStack createIcon() {
		if(ModItems.crucible_template != null){
			return new ItemStack(ModItems.crucible_template);
		}
		return new ItemStack(Items.IRON_PICKAXE);
	}
	
	@Override
	public boolean hasSearchBar() {
		return true;
	}

    @Override
    @SideOnly(Side.CLIENT)
    public String getBackgroundImageName() {
        return "item_search.png";
    }

}
