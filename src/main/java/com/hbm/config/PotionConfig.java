package com.hbm.config;

import net.minecraftforge.common.config.Configuration;

import java.util.HashSet;

public class PotionConfig {

	public static boolean doJumpBoost = true;
	public static int potionSickness = 0;
	public static HashSet<String> potionBlacklist;

	public static void loadFromConfig(Configuration config){
		potionBlacklist = CommonConfig.createConfigHashSet(config, CommonConfig.CATEGORY_POTION, "08.01_hazmatPotionBlacklist", "List of Potions that get blocked while wearing a hazmat suit with bacteria protection - <potion> (String)", String.class, new String[]{
			"srparasites:coth",
			"srparasites:viral"
		});
		doJumpBoost = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_POTION, "8.02_doJumpBoost", "Whether Servos and Armors should give Jumpboost", true);
		String s = CommonConfig.createConfigString(config, CommonConfig.CATEGORY_POTION, "8.03_potionSickness", "Valid configs include \"NORMAL\" and \"TERRARIA\", otherwise potion sickness is turned off", "OFF");
		if("normal".equalsIgnoreCase(s))
			potionSickness = 1;
		if("terraria".equalsIgnoreCase(s))
			potionSickness = 2;

	}
	
}
