package com.hbm.inventory.recipes;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.items.ModItems;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.hbm.inventory.OreDictManager.*;

public class MagicRecipes {

	//Drillgon200: I hate these warnings. 
	//All it takes are these two characters                 ||
	//But bob evidently just hates not having warnings      VV
	public static List<MagicRecipe> recipes = new ArrayList<>();

	public static ItemStack getRecipe(InventoryCrafting matrix) {

		List<ComparableStack> comps = new ArrayList<>();

		for(int i = 0; i < 4; i++) {
			if(!matrix.getStackInSlot(i).isEmpty())
				comps.add(new ComparableStack(matrix.getStackInSlot(i)).makeSingular());
		}

		//Collections.sort(comps);

		for(MagicRecipe recipe : recipes) {
			if(recipe.matches(comps))
				return recipe.getResult();
		}
		
		return ItemStack.EMPTY;
	}

	public static void register() {
		recipes.add(new MagicRecipe(new ItemStack(ModItems.ingot_u238m2), new ComparableStack(ModItems.ingot_u238m2, 1, 1), new ComparableStack(ModItems.ingot_u238m2, 1, 2), new ComparableStack(ModItems.ingot_u238m2, 1, 3)));
		recipes.add(new MagicRecipe(new ItemStack(ModItems.rod_of_discord), new ComparableStack(Items.ENDER_PEARL), new ComparableStack(ModItems.nugget_euphemium), new ComparableStack(Items.BLAZE_ROD)));
		recipes.add(new MagicRecipe(new ItemStack(ModItems.balefire_and_steel), new OreDictStack(STEEL.ingot()), new ComparableStack(ModItems.egg_balefire_shard)));
		recipes.add(new MagicRecipe(new ItemStack(ModItems.mysteryshovel), new ComparableStack(Items.IRON_SHOVEL), new ComparableStack(Items.BONE), new ComparableStack(ModItems.ingot_starmetal), new ComparableStack(ModItems.ducttape)));
		recipes.add(new MagicRecipe(new ItemStack(ModItems.ingot_electronium), new ComparableStack(ModItems.pellet_charged), new ComparableStack(ModItems.pellet_charged), new OreDictStack(DNT.ingot()), new OreDictStack(DNT.ingot())));

		recipes.add(new MagicRecipe(new ItemStack(ModItems.diamond_gavel),
				new ComparableStack(ModBlocks.gravel_diamond),
				new ComparableStack(ModBlocks.gravel_diamond),
				new ComparableStack(ModBlocks.gravel_diamond),
				new ComparableStack(ModItems.lead_gavel)));
		recipes.add(new MagicRecipe(new ItemStack(ModItems.mese_gavel),
				new ComparableStack(ModItems.shimmer_handle),
				new ComparableStack(ModItems.powder_dineutronium),
				new ComparableStack(ModItems.blades_desh),
				new ComparableStack(ModItems.diamond_gavel)));
		recipes.add(new MagicRecipe(new ItemStack(ModBlocks.hadron_coil_mese),
				new ComparableStack(ModBlocks.hadron_coil_chlorophyte),
				new ComparableStack(ModItems.powder_dineutronium),
				new ComparableStack(ModItems.plate_desh),
				new OreDictStack(GOLD.ingot())));
	}

	public static List<MagicRecipe> getRecipes() {
		return recipes;
	}

	public static class MagicRecipe {

		public List<AStack> in;
		public ItemStack out;

		public MagicRecipe(ItemStack out, AStack... in) {
			this.out = out;
			this.in = Arrays.asList(in);
			//Collections.sort(this.in);
		}

		public boolean matches(List<ComparableStack> comps) {
			if(comps.size() != in.size())
				return false;

			for(int i = 0; i < in.size(); i++) {

				if(!in.get(i).isApplicable(comps.get(i)))
					return false;
			}

			return true;
		}

		public ItemStack getResult() {
			return out.copy();
		}
	}
}