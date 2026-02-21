package com.hbm.inventory.recipes;

import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemRBMKRod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.LinkedHashMap;
import java.util.Random;

public class RBMKFuelRecipes {
	public static Random rand = new Random();
	public static LinkedHashMap<ItemStack, ItemStack> recipes = new LinkedHashMap<>();
	
	public static void registerRecipes() {
		addRod(ModItems.rbmk_fuel_ueu);
		addRod(ModItems.rbmk_fuel_meu);
		addRod(ModItems.rbmk_fuel_heu233);
		addRod(ModItems.rbmk_fuel_heu235);
		addRod(ModItems.rbmk_fuel_thmeu);
		addRod(ModItems.rbmk_fuel_lep);
		addRod(ModItems.rbmk_fuel_mep);
		addRod(ModItems.rbmk_fuel_hep239);
		addRod(ModItems.rbmk_fuel_hep241);
		addRod(ModItems.rbmk_fuel_lea);
		addRod(ModItems.rbmk_fuel_mea);
		addRod(ModItems.rbmk_fuel_hea241);
		addRod(ModItems.rbmk_fuel_hea242);
		addRod(ModItems.rbmk_fuel_men);
		addRod(ModItems.rbmk_fuel_hen);
		addRod(ModItems.rbmk_fuel_mox);
		addRod(ModItems.rbmk_fuel_les);
		addRod(ModItems.rbmk_fuel_mes);
		addRod(ModItems.rbmk_fuel_hes);
		addRod(ModItems.rbmk_fuel_leaus);
		addRod(ModItems.rbmk_fuel_heaus);
		addRod(ModItems.rbmk_fuel_ra226be);
		addRod(ModItems.rbmk_fuel_po210be);
		addRod(ModItems.rbmk_fuel_pu238be);
		addRod(ModItems.rbmk_fuel_balefire_gold);
		addRod(ModItems.rbmk_fuel_flashlead);
		addRod(ModItems.rbmk_fuel_zfb_bismuth);
		addRod(ModItems.rbmk_fuel_zfb_pu241);
		addRod(ModItems.rbmk_fuel_zfb_am_mix);
		addRod(ModItems.rbmk_fuel_balefire);
		addRod(ModItems.rbmk_fuel_drx);
	}

	public static void addRod(ItemRBMKRod rod){
		for(int e = 0; e<5; e++){
			addRecipe(makeRBMKRod(rod, e, false), makeRBMKPellet(makeRBMKRod(rod, e, false)));
		}
		for(int e = 0; e<5; e++){
			addRecipe(makeRBMKRod(rod, e, true), makeRBMKPellet(makeRBMKRod(rod, e, true)));
		}
	}

	public static void addRecipe(ItemStack input, ItemStack output){
		recipes.put(input, output);
	}

	public static ItemStack makeRBMKRod(ItemRBMKRod rod, int enrichment, boolean xenon){
		ItemStack fuelRod = new ItemStack(rod);
		ItemRBMKRod.setCoreHeat(fuelRod, 20D+rand.nextDouble()*29.9D);
		ItemRBMKRod.setHullHeat(fuelRod, 20D+rand.nextDouble()*29.9D);
		ItemRBMKRod.setPoison(fuelRod, xenon ? 50D+rand.nextDouble()*50D : rand.nextDouble()*49.9D);
		ItemRBMKRod.setYield(fuelRod, rod.yield * Math.min(0.99D, ((-enrichment-1)*0.2D + 1D + rand.nextDouble()*0.199D)));
		return fuelRod;
	}

	public static ItemStack makeRBMKPellet(ItemStack rod){
		ItemStack result = new ItemStack(((ItemRBMKRod)rod.getItem()).pellet, 8);
		int enrichment = 4 - MathHelper.clamp((int)Math.ceil(ItemRBMKRod.getEnrichment(rod) * 5 - 1), 0, 4);
		int meta = enrichment + (ItemRBMKRod.getPoisonLevel(rod) >= 0.5D ? 5 : 0);
		result.setItemDamage(meta);
		return result;
	}
}
