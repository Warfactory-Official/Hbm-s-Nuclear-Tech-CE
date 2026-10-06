package com.hbm.config;

import net.minecraftforge.common.config.Configuration;

public class WorldConfig {
	public static boolean newBedrockOres = true;

	public static int limestoneSpawn = 1;

	public static boolean enableHematite = true;
	public static boolean enableMalachite = true;
	public static boolean enableBauxite = true;

	public static boolean enableSulfurCave = true;
	public static boolean enableAsbestosCave = true;

	public static boolean enableMeteorStrikes = true;
	public static boolean enableMeteorShowers = true;
	public static boolean enableMeteorTails = true;
	public static boolean enableSpecialMeteors = true;
	public static int meteorStrikeChance = 20 * 60 * 60 * 5;
	public static int meteorShowerChance = 20 * 60 * 15;
	public static int meteorShowerDuration = 20 * 60 * 30;

	public static boolean enableCraterBiomes = true;
	public static float craterBiomeRad = 5F;
	public static float craterBiomeInnerRad = 25F;
	public static float craterBiomeOuterRad = 0.5F;
	public static float craterBiomeWaterMult = 5F;
    public static int bedrockGlowstoneSpawn = 100;
    public static int bedrockPhosphorusSpawn = 50;
    public static int bedrockQuartzSpawn = 100;

    public static int convertToInt(Object e){
		if(e == null)
			return 0;
		return (int)e;
	}

	public static void loadFromConfig(Configuration config) {
		newBedrockOres = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_ORES, "2.NB_newBedrockOres", "Enables the generation of bedrock ores", true);
		bedrockGlowstoneSpawn = CommonConfig.createConfigInt(config, CommonConfig.CATEGORY_ORES, "2.BN00_bedrockGlowstoneWeight", "Spawn weight for glowstone bedrock ore", 100);
		bedrockPhosphorusSpawn = CommonConfig.createConfigInt(config, CommonConfig.CATEGORY_ORES, "2.BN01_bedrockPhosphorusWeight", "Spawn weight for phosphorus bedrock ore", 50);
		bedrockQuartzSpawn = CommonConfig.createConfigInt(config, CommonConfig.CATEGORY_ORES, "2.BN02_bedrockQuartzWeight", "Spawn weight for quartz bedrock ore", 100);
		limestoneSpawn = CommonConfig.createConfigInt(config, CommonConfig.CATEGORY_ORES, "2.L02_limestoneSpawn", "Amount of limestone block veins per chunk", 1);

		enableHematite = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_ORES, "2.L00_enableHematite", "Toggles hematite deposits", true);
		enableMalachite = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_ORES, "2.L01_enableMalachite", "Toggles malachite deposits", true);
		enableBauxite = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_ORES, "2.L02_enableBauxite", "Toggles bauxite deposits", true);

		enableSulfurCave = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_ORES, "2.C00_enableSulfurCave", "Toggles sulfur caves", true);
		enableAsbestosCave = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_ORES, "2.C01_enableAsbestosCave", "Toggles asbestos caves", true);

		enableCraterBiomes = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_BIOMES, "17.B_toggle", "Enables the biome change caused by nuclear explosions", true);
		craterBiomeRad = (float) CommonConfig.createConfigDouble(config, CommonConfig.CATEGORY_BIOMES, "17.R00_craterBiomeRad", "RAD/s for the crater biome", 5D);
		craterBiomeInnerRad = (float) CommonConfig.createConfigDouble(config, CommonConfig.CATEGORY_BIOMES, "17.R01_craterBiomeInnerRad", "RAD/s for the inner crater biome", 25D);
		craterBiomeOuterRad = (float) CommonConfig.createConfigDouble(config, CommonConfig.CATEGORY_BIOMES, "17.R02_craterBiomeOuterRad", "RAD/s for the outer crater biome", 0.5D);
		craterBiomeWaterMult = (float) CommonConfig.createConfigDouble(config, CommonConfig.CATEGORY_BIOMES, "17.R03_craterBiomeWaterMult", "Multiplier for RAD/s in crater biomes when in water", 5D);
		
		enableMeteorStrikes = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_METEORS, "5.00_enableMeteorStrikes", "Toggles the spawning of meteors", true);
		enableMeteorShowers = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_METEORS, "5.01_enableMeteorShowers", "Toggles meteor showers, which start with a 1% chance for every spawned meteor", true);
		enableMeteorTails = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_METEORS, "5.02_enableMeteorTails", "Toggles the particle effect created by falling meteors", true);
		enableSpecialMeteors = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_METEORS, "5.03_enableSpecialMeteors", "Toggles rare, special meteor types with different impact effects", true);
		meteorStrikeChance = CommonConfig.createConfigInt(config, CommonConfig.CATEGORY_METEORS, "5.03_meteorStrikeChance", "The probability of a meteor spawning (an average of once every nTH ticks)", 20 * 60 * 60 * 5);
		meteorShowerChance = CommonConfig.createConfigInt(config, CommonConfig.CATEGORY_METEORS, "5.04_meteorShowerChance", "The probability of a meteor spawning during meteor shower (an average of once every nTH ticks)", 20 * 60 * 15);
		meteorShowerDuration = CommonConfig.createConfigInt(config, CommonConfig.CATEGORY_METEORS, "5.05_meteorShowerDuration", "Max duration of meteor shower in ticks", 20 * 60 * 30);
	}

	// EC compat: fields removed in 1.12 CE refactor but still accessed via direct field reference by EnergyControl CrossHBM.loadOreInfo
	// Keep as deprecated stubs to avoid NoSuchFieldError. Values are dummies - ore gen no longer uses them.
	@Deprecated public static int uraniumSpawn = 6;
	@Deprecated public static int thoriumSpawn = 7;
	@Deprecated public static int titaniumSpawn = 8;
	@Deprecated public static int sulfurSpawn = 5;
	@Deprecated public static int aluminiumSpawn = 7;
	@Deprecated public static int copperSpawn = 12;
	@Deprecated public static int fluoriteSpawn = 6;
	@Deprecated public static int niterSpawn = 6;
	@Deprecated public static int tungstenSpawn = 10;
	@Deprecated public static int leadSpawn = 6;
	@Deprecated public static int berylliumSpawn = 6;
	@Deprecated public static int ligniteSpawn = 2;
	@Deprecated public static int asbestosSpawn = 4;
	@Deprecated public static int rareSpawn = 6;
	@Deprecated public static int lithiumSpawn = 6;
	@Deprecated public static int cinnabarSpawn = 1;
	@Deprecated public static int cinnebarSpawn = 1; // EC typo
	@Deprecated public static int cobaltSpawn = 2;
	@Deprecated public static int gassshaleSpawn = 5;
	@Deprecated public static int gasbubbleSpawn = 4;
	@Deprecated public static int oilSpawn = 100;
	@Deprecated public static int ironClusterSpawn = 4;
	@Deprecated public static int titaniumClusterSpawn = 2;
	@Deprecated public static int aluminiumClusterSpawn = 3;
	@Deprecated public static int copperClusterSpawn = 4;
	// extra stubs that existed in older builds and might be referenced by other addons
	@Deprecated public static int oilcoalSpawn = 128;
	@Deprecated public static int explosivebubbleSpawn = 0;
	@Deprecated public static int netherUraniumuSpawn = 8;
	@Deprecated public static int netherTungstenSpawn = 10;
	@Deprecated public static int netherSulfurSpawn = 26;
	@Deprecated public static int netherPhosphorusSpawn = 24;
}
