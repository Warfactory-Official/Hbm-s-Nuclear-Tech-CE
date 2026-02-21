package com.hbm.config;

import net.minecraftforge.common.config.Configuration;

public class WorldConfig {
	public static int uraniumSpawn = 6;
	public static int thoriumSpawn = 7;
	public static int titaniumSpawn = 8;
	public static int sulfurSpawn = 5;
	public static int aluminiumSpawn = 7;
	public static int copperSpawn = 12;
	public static int fluoriteSpawn = 6;
	public static int niterSpawn = 6;
	public static int tungstenSpawn = 10;
	public static int leadSpawn = 6;
	public static int berylliumSpawn = 6;
	public static int ligniteSpawn = 2;
	public static int asbestosSpawn = 4;
	public static int rareSpawn = 6;
	public static int lithiumSpawn = 6;
	public static int cinnabarSpawn = 1;
	public static int oilcoalSpawn = 128;
	public static int gassshaleSpawn = 5;
	public static int gasbubbleSpawn = 4;
	public static int explosivebubbleSpawn = 0;
	public static int cobaltSpawn = 2;
	public static int oilSpawn = 100;
	public static boolean newBedrockOres = true;
	public static int dunaOilSpawn = 100;
	public static int eveGasSpawn = 100;
	public static int laytheOilSpawn = 100;

	public static int ironClusterSpawn = 4;
	public static int titaniumClusterSpawn = 2;
	public static int aluminiumClusterSpawn = 3;
	public static int copperClusterSpawn = 4;
	public static int alexandriteSpawn = 100;

	public static int limestoneSpawn = 1;

	public static int netherUraniumuSpawn = 8;
	public static int netherTungstenSpawn = 10;
	public static int netherSulfurSpawn = 26;
	public static int netherPhosphorusSpawn = 24;
	public static int netherCoalSpawn = 24;
	public static int netherPlutoniumSpawn = 8;
	public static int netherCobaltSpawn = 2;

	public static int endTikiteSpawn = 8;

	public static boolean enableHematite = true;
	public static boolean enableMalachite = true;
	public static boolean enableBauxite = true;

	public static boolean enableSulfurCave = true;
	public static boolean enableAsbestosCave = true;

	public static int radioStructure = 500;
	public static int antennaStructure = 750;
	public static int atomStructure = 500;
	public static int vertibirdStructure = 500;
	public static int dungeonStructure = 64;
	public static int relayStructure = 500;
	public static int satelliteStructure = 500;
	public static int bunkerStructure = 1000;
	public static int siloStructure = 1000;
	public static int factoryStructure = 1000;
	public static int dudStructure = 500;
	public static int spaceshipStructure = 1000;
	public static int barrelStructure = 5000;
	public static int geyserWater = 3000;
	public static int geyserChlorine = 3000;
	public static int geyserVapor = 500;
	public static int capsuleStructure = 100;
	public static int broadcaster = 5000;
	public static int minefreq = 64;
	public static int radfreq = 5000;
	public static int vaultfreq = 2500;
	public static int arcticStructure = 500;
	public static int jungleStructure = 2000;
	public static int pyramidStructure = 4000;

	public static int meteorStrikeChance = 20 * 60 * 180;
	public static int meteorShowerChance = 20 * 60 * 5;
	public static int meteorShowerDuration = 6000;

	public static boolean enableCraterBiomes = true;
	public static int craterBiomeId = 80;
	public static int craterBiomeInnerId = 81;
	public static int craterBiomeOuterId = 82;
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
		final String CATEGORY_OREGEN = CommonConfig.CATEGORY_ORES;

		uraniumSpawn = convertToInt(CompatibilityConfig.uraniumSpawn.get(0));
		titaniumSpawn = convertToInt(CompatibilityConfig.titaniumSpawn.get(0));
		sulfurSpawn = convertToInt(CompatibilityConfig.sulfurSpawn.get(0));
		aluminiumSpawn = convertToInt(CompatibilityConfig.aluminiumSpawn.get(0));
		copperSpawn = convertToInt(CompatibilityConfig.copperSpawn.get(0));
		fluoriteSpawn = convertToInt(CompatibilityConfig.fluoriteSpawn.get(0));
		niterSpawn = convertToInt(CompatibilityConfig.niterSpawn.get(0));
		tungstenSpawn = convertToInt(CompatibilityConfig.tungstenSpawn.get(0));
		leadSpawn = convertToInt(CompatibilityConfig.leadSpawn.get(0));
		berylliumSpawn = convertToInt(CompatibilityConfig.berylliumSpawn.get(0));
		thoriumSpawn = convertToInt(CompatibilityConfig.thoriumSpawn.get(0));
		ligniteSpawn = convertToInt(CompatibilityConfig.ligniteSpawn.get(0));
		asbestosSpawn = convertToInt(CompatibilityConfig.asbestosSpawn.get(0));
		lithiumSpawn = convertToInt(CompatibilityConfig.lithiumSpawn.get(0));
		rareSpawn = convertToInt(CompatibilityConfig.rareSpawn.get(0));
		oilcoalSpawn = convertToInt(CompatibilityConfig.oilcoalSpawn.get(0));
		gassshaleSpawn = convertToInt(CompatibilityConfig.gassshaleSpawn.get(0));
		explosivebubbleSpawn = convertToInt(CompatibilityConfig.explosivebubbleSpawn.get(0));
		alexandriteSpawn = convertToInt(CompatibilityConfig.alexandriteSpawn.get(0));
		oilSpawn = convertToInt(CompatibilityConfig.oilBubbleSpawn.get(0));
		gasbubbleSpawn = convertToInt(CompatibilityConfig.gasbubbleSpawn.get(0));
		cinnabarSpawn = convertToInt(CompatibilityConfig.cinnabarSpawn.get(0));
		cobaltSpawn = convertToInt(CompatibilityConfig.cobaltSpawn.get(0));

		ironClusterSpawn = convertToInt(CompatibilityConfig.ironClusterSpawn.get(0));
		titaniumClusterSpawn = convertToInt(CompatibilityConfig.titaniumClusterSpawn.get(0));
		aluminiumClusterSpawn = convertToInt(CompatibilityConfig.aluminiumClusterSpawn.get(0));
		copperClusterSpawn = convertToInt(CompatibilityConfig.copperClusterSpawn.get(0));

		netherUraniumuSpawn = convertToInt(CompatibilityConfig.netherUraniumSpawn.get(-1));
		netherTungstenSpawn = convertToInt(CompatibilityConfig.netherTungstenSpawn.get(-1));
		netherSulfurSpawn = convertToInt(CompatibilityConfig.netherSulfurSpawn.get(-1));
		netherPhosphorusSpawn = convertToInt(CompatibilityConfig.netherPhosphorusSpawn.get(-1));
		netherCoalSpawn = convertToInt(CompatibilityConfig.netherCoalSpawn.get(-1));
		netherPlutoniumSpawn = convertToInt(CompatibilityConfig.netherPlutoniumSpawn.get(-1));
		netherCobaltSpawn = convertToInt(CompatibilityConfig.netherCobaltSpawn.get(-1));

		endTikiteSpawn = convertToInt(CompatibilityConfig.endTixiteSpawn.get(1));

		radioStructure = convertToInt(CompatibilityConfig.radioStructure.get(0));
		antennaStructure = convertToInt(CompatibilityConfig.antennaStructure.get(0));
		atomStructure = convertToInt(CompatibilityConfig.atomStructure.get(0));
		vertibirdStructure = convertToInt(CompatibilityConfig.vertibirdStructure.get(0));
		dungeonStructure = convertToInt(CompatibilityConfig.dungeonStructure.get(0));
		relayStructure = convertToInt(CompatibilityConfig.relayStructure.get(0));
		satelliteStructure = convertToInt(CompatibilityConfig.satelliteStructure.get(0));
		bunkerStructure = convertToInt(CompatibilityConfig.bunkerStructure.get(0));
		siloStructure = convertToInt(CompatibilityConfig.siloStructure.get(0));
		factoryStructure = convertToInt(CompatibilityConfig.factoryStructure.get(0));
		dudStructure = convertToInt(CompatibilityConfig.dudStructure.get(0));
		spaceshipStructure = convertToInt(CompatibilityConfig.spaceshipStructure.get(0));
		barrelStructure = convertToInt(CompatibilityConfig.barrelStructure.get(0));
		broadcaster = convertToInt(CompatibilityConfig.broadcaster.get(0));
		minefreq = convertToInt(CompatibilityConfig.minefreq.get(0));
		dunaOilSpawn = convertToInt(CompatibilityConfig.dunaOilSpawn.get(0));
		laytheOilSpawn = convertToInt(CompatibilityConfig.laytheOilSpawn.get(0));
		eveGasSpawn = convertToInt(CompatibilityConfig.eveGasSpawn.get(0));
		radfreq = convertToInt(CompatibilityConfig.radfreq.get(0));
		vaultfreq = convertToInt(CompatibilityConfig.vaultfreq.get(0));
		geyserWater = convertToInt(CompatibilityConfig.geyserWater.get(0));
		geyserChlorine = convertToInt(CompatibilityConfig.geyserChlorine.get(0));


		geyserVapor = convertToInt(CompatibilityConfig.geyserVapor.get(0));
		capsuleStructure = convertToInt(CompatibilityConfig.capsuleStructure.get(0));
		arcticStructure = convertToInt(CompatibilityConfig.arcticStructure.get(0));
		jungleStructure = convertToInt(CompatibilityConfig.jungleStructure.get(0));
		pyramidStructure = convertToInt(CompatibilityConfig.pyramidStructure.get(0));


		meteorStrikeChance = convertToInt(CompatibilityConfig.meteorStrikeChance.get(0));
		meteorShowerChance = convertToInt(CompatibilityConfig.meteorShowerChance.get(0));
		meteorShowerDuration = convertToInt(CompatibilityConfig.meteorShowerDuration.get(0));

	}
}
