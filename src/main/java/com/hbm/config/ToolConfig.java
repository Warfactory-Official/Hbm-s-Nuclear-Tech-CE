package com.hbm.config;

import net.minecraftforge.common.config.Configuration;

public class ToolConfig {

	public static int recursionDepth = 500;
	public static boolean recursiveStone = true;
	public static boolean recursiveNetherrack = true;
	
	public static boolean abilityHammer = true;
	public static boolean abilityVein = true;
	public static boolean abilityLuck = true;
	public static boolean abilitySilk = true;
	public static boolean abilityFurnace = true;
	public static boolean abilityShredder = true;
	public static boolean abilityCentrifuge = true;
	public static boolean abilityCrystallizer = true;
	public static boolean abilityMercury = true;
	public static boolean abilityExplosion = true;
	
	public static void loadFromConfig(Configuration config) {
		recursionDepth = CommonConfig.createConfigInt(config, CommonConfig.CATEGORY_TOOLS, "11.00_recursionDepth", "Limits veinminer's recursive function. Usually not an issue, unless you're using bukkit which is especially sensitive for some reason.", 1000);
        recursiveStone = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_TOOLS, "11.01_recursionDepth", "Determines whether veinminer can break stone", false);
        recursiveNetherrack = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_TOOLS, "11.02_recursionDepth", "Determines whether veinminer can break netherrack", false);

		abilityHammer = config.get(CommonConfig.CATEGORY_TOOLS, "11.03_hammerAbility", true, "Allows AoE ability").getBoolean(true);
		abilityVein = config.get(CommonConfig.CATEGORY_TOOLS, "11.04_abilityVein", true, "Allows veinminer ability").getBoolean(true);
		abilityLuck = config.get(CommonConfig.CATEGORY_TOOLS, "11.05_abilityLuck", true, "Allow luck (fortune) ability").getBoolean(true);
		abilitySilk = config.get(CommonConfig.CATEGORY_TOOLS, "11.06_abilitySilk", true, "Allow silk touch ability").getBoolean(true);
		abilityFurnace = config.get(CommonConfig.CATEGORY_TOOLS, "11.07_abilityFurnace", true, "Allow auto-smelter ability").getBoolean(true);
		abilityShredder = config.get(CommonConfig.CATEGORY_TOOLS, "11.08_abilityShredder", true, "Allow auto-shredder ability").getBoolean(true);
		abilityCentrifuge = config.get(CommonConfig.CATEGORY_TOOLS, "11.09_abilityCentrifuge", true, "Allow auto-centrifuge ability").getBoolean(true);
		abilityCrystallizer = config.get(CommonConfig.CATEGORY_TOOLS, "11.10_abilityCrystallizer", true, "Allow auto-crystallizer ability").getBoolean(true);
		abilityMercury = config.get(CommonConfig.CATEGORY_TOOLS, "11.11_abilityMercury", true, "Allow mercury touch ability (digging redstone gives mercury)").getBoolean(true);
		abilityExplosion = config.get(CommonConfig.CATEGORY_TOOLS, "11.12_abilityExplosion", true, "Allow explosion ability").getBoolean(true);
	}

	
}
