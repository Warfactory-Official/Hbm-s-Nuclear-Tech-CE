package com.hbm.wiaj.cannery;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.PlantEnums;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;

import java.util.HashMap;

public class Jars {

	public static HashMap<ComparableStack, CanneryBase> canneries = new HashMap<ComparableStack, CanneryBase>();
	
	public static void initJars() {
		canneries.put(new ComparableStack(ModBlocks.heater_firebox), new CanneryFirebox());
		canneries.put(new ComparableStack(ModBlocks.machine_gascent), new CanneryCentrifuge());
		canneries.put(new ComparableStack(ModBlocks.machine_fensu), new CanneryFEnSU());
		canneries.put(new ComparableStack(ModBlocks.machine_fel), new CannerySILEX());
		canneries.put(new ComparableStack(ModBlocks.machine_silex), new CannerySILEX());
		canneries.put(new ComparableStack(ModBlocks.machine_stirling), new CanneryStirling());
		canneries.put(new ComparableStack(ModBlocks.machine_stirling_steel), new CanneryStirling());

		canneries.put(new ComparableStack(OreDictManager.DictFrame.fromOne(ModItems.plant_item, ItemEnums.EnumPlantType.MUSTARDWILLOW)), new CanneryWillow());
		canneries.put(new ComparableStack(OreDictManager.DictFrame.fromOne(ModBlocks.plant_flower, PlantEnums.EnumFlowerPlantType.MUSTARD_WILLOW_0)), new CanneryWillow());

	}
}
