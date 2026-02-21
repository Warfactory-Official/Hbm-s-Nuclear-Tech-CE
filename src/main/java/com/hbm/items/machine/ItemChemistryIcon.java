package com.hbm.items.machine;

import com.hbm.inventory.recipes.ChemplantRecipes;
import com.hbm.items.ModItems;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemChemistryIcon extends Item {

	public ItemChemistryIcon(String s){
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(null);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack stack) {
		ChemplantRecipes.ChemRecipe recipe = ChemplantRecipes.indexMapping.get(stack.getItemDamage());
		if(recipe == null) {
			return TextFormatting.RED + "Broken Template" + TextFormatting.RESET;
		} else {
			String s = ("" + I18n.format(ModItems.chemistry_template.getTranslationKey() + ".name")).trim();
			String s1 = ("" + I18n.format("chem." + recipe.name)).trim();

			if (s1 != null) {
				s = s + " " + s1;
			}
			return s;
		}
	}
	
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
		if(tab == this.getCreativeTab()){
			for (int i: ChemplantRecipes.recipeNames.keySet()){
				list.add(new ItemStack(this, 1, i));
        	}
		}
	}
}
