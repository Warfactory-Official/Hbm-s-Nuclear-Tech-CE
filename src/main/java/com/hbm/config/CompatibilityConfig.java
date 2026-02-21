package com.hbm.config;

import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;

import java.util.HashMap;
import java.util.HashSet;

public class CompatibilityConfig {

	public static HashMap<Integer, Float> dimensionRad;
	public static HashSet<String> bedrockOreBlacklist;

	public static HashMap<Integer, Integer> uraniumSpawn;
	public static HashMap<Integer, Integer> thoriumSpawn;
	public static HashMap<Integer, Integer> titaniumSpawn;
	public static HashMap<Integer, Integer> sulfurSpawn;
	public static HashMap<Integer, Integer> aluminiumSpawn;
	public static HashMap<Integer, Integer> copperSpawn;
	public static HashMap<Integer, Integer> fluoriteSpawn;
	public static HashMap<Integer, Integer> niterSpawn;
	public static HashMap<Integer, Integer> tungstenSpawn;
	public static HashMap<Integer, Integer> leadSpawn;
	public static HashMap<Integer, Integer> berylliumSpawn;
	public static HashMap<Integer, Integer> ligniteSpawn;
	public static HashMap<Integer, Integer> asbestosSpawn;
	public static HashMap<Integer, Integer> rareSpawn;
	public static HashMap<Integer, Integer> lithiumSpawn;
	public static HashMap<Integer, Integer> cinnabarSpawn;
	public static HashMap<Integer, Integer> oilcoalSpawn;
	public static HashMap<Integer, Integer> gassshaleSpawn;
	public static HashMap<Integer, Integer> gasbubbleSpawn;
	public static HashMap<Integer, Integer> explosivebubbleSpawn;
	public static HashMap<Integer, Integer> alexandriteSpawn;
	public static HashMap<Integer, Integer> oilBubbleSpawn;
	public static HashMap<Integer, Integer> cobaltSpawn;
	
	public static HashMap<Integer, Integer> ironClusterSpawn;
	public static HashMap<Integer, Integer> titaniumClusterSpawn;
	public static HashMap<Integer, Integer> aluminiumClusterSpawn;
	public static HashMap<Integer, Integer> copperClusterSpawn;

	public static HashMap<Integer, Integer> malachiteSpawn;

	public static HashMap<Integer, Integer> reiiumSpawn;
	public static HashMap<Integer, Integer> weidaniumSpawn;
	public static HashMap<Integer, Integer> australiumSpawn;
	public static HashMap<Integer, Integer> verticiumSpawn;
	public static HashMap<Integer, Integer> unobtainiumSpawn;
	public static HashMap<Integer, Integer> daffergonSpawn;
	
	public static HashMap<Integer, Integer> netherUraniumSpawn;
	public static HashMap<Integer, Integer> netherTungstenSpawn;
	public static HashMap<Integer, Integer> netherSulfurSpawn;
	public static HashMap<Integer, Integer> netherPhosphorusSpawn;
	public static HashMap<Integer, Integer> netherCoalSpawn;
	public static HashMap<Integer, Integer> netherPlutoniumSpawn;
	public static HashMap<Integer, Integer> netherCobaltSpawn;

	public static HashMap<Integer, Integer> endTixiteSpawn;

	public static HashMap<Integer, Integer> bedrockOilSpawn;

	public static HashMap<Integer, Integer> dunaOilSpawn;
	public static HashMap<Integer, Integer> laytheOilSpawn;
	public static HashMap<Integer, Integer> eveGasSpawn;
	
	public static HashMap<Integer, Integer> radioStructure;
	public static HashMap<Integer, Integer> antennaStructure;
	public static HashMap<Integer, Integer> atomStructure;
	public static HashMap<Integer, Integer> vertibirdStructure;
	public static HashMap<Integer, Integer> dungeonStructure;
	public static HashMap<Integer, Integer> relayStructure;
	public static HashMap<Integer, Integer> satelliteStructure;
	public static HashMap<Integer, Integer> bunkerStructure;
	public static HashMap<Integer, Integer> siloStructure;
	public static HashMap<Integer, Integer> factoryStructure;
	public static HashMap<Integer, Integer> dudStructure;
	public static HashMap<Integer, Integer> spaceshipStructure;
	public static HashMap<Integer, Integer> barrelStructure;
	public static HashMap<Integer, Integer> geyserWater;
	public static HashMap<Integer, Integer> geyserChlorine;
	public static HashMap<Integer, Integer> geyserVapor;
	public static HashMap<Integer, Integer> geyserNether;
	public static HashMap<Integer, Integer> capsuleStructure;
	public static HashMap<Integer, Integer> broadcaster;
	public static HashMap<Integer, Integer> minefreq;
	public static HashMap<Integer, Integer> radfreq;
	public static HashMap<Integer, Integer> vaultfreq;
	public static HashMap<Integer, Integer> arcticStructure;
	public static HashMap<Integer, Integer> jungleStructure;
	public static HashMap<Integer, Integer> pyramidStructure;
	
	public static HashMap<Integer, Integer> meteorStrikeChance;
	public static HashMap<Integer, Integer> meteorShowerChance;
	public static HashMap<Integer, Integer> meteorShowerDuration;

	public static HashMap<String, Float> mobModRadresistance;
	public static HashSet<String> mobModRadimmune;

	public static HashMap<String, Float> mobRadresistance;
	public static HashSet<String> mobRadimmune;

	public static boolean mobGear = true;

	public static boolean modLoot = true;

	public static boolean doEvaporateWater = true;
	public static boolean doFillCraterWithWater = true;
	public static HashMap<Integer, Integer> fillCraterWithWater;

	public static boolean peaceDimensionsIsWhitelist = true;
	public static HashSet<Integer> peaceDimensions;


	
	public static void loadFromConfig(Configuration config) {
		String dimRadComment = "Amount of background radiation in the dimension in Rad/s - <dimID:Rad> (Int:Float)";
		dimensionRad = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_RADIATION, "01.01_dimensionRadiation", dimRadComment, Integer.class, Float.class, new String[]{  
			"-1:0.1", //Nether
			"1:0.01", //End

			"-13:2.5", //Mercury
			"-61:5", //Mercury - Orbit
			"-60:5", //Mercury - Orbit (Static)

			"-27:0.05", //Earth - Orbit
			"-26:0.05", //Earth - Orbit (Static)

			"-28:0.3", //Moon

			"-29:0.1", //Mars
			"-67:0.15", //Mars - Orbit
			"-66:0.15", //Mars - Orbit (Static)

			"-1502:0.3", //Phobos
			"-1503:0.4", //Deimos

			"-30:1", //Asteroides

			"-31:0.05", //Venus
			"-63:4", //Venus - Orbit
			"-62:4", //Venus - Orbit (Static)
			
			"-20:5", //Ceres
			"-65:5", //Ceres - Orbit
			"-64:5", //Ceres - Orbit (Static)

			"-15:5", //Jupiter
			"-69:20", //Jupiter - Orbit
			"-68:20", //Jupiter - Orbit (Static)

			"-1500:6", //IO
			"-1501:5", //Europa
			"-1506:4.5", //Ganymede
			"-1505:4", //Callisto

			"-16:6", //Saturn
			"-71:16", //Saturn - Orbit
			"-70:16", //Saturn - Orbit (Static)

			"-1507:4", //Rhea
			"-1508:2", //Titan
			"-1511:1", //Iapetus

			"-17:5", //Uranus
			"-73:12", //Uranus - Orbit
			"-72:12", //Uranus - Orbit (Static)

			"-1510:4", //Titania
			"-1509:2", //Oberon

			"-18:4", //Neptune
			"-75:8", //Neptune - Orbit
			"-74:8", //Neptune - Orbit (Static)

			"-1504:1.5", //Triton

			"-19:0.3", //Pluto
			"-77:0.3", //Pluto - Orbit
			"-76:0.3", //Pluto - Orbit (Static)

			"-21:0.1", //Eris
			"-79:0.1", //Eris - Orbit
			"-78:0.1", //Eris - Orbit (Static)
		}, ":");

		//Ores
		uraniumSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.01_uraniumSpawnrate", "Amount of uranium ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:7", "-6:7" }, ":");
		titaniumSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.02_titaniumSpawnrate", "Amount of titanium ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:8", "-6:8" }, ":");
		sulfurSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.03_sulfurSpawnrate", "Amount of sulfur ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:5", "-6:5" }, ":");
		aluminiumSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.04_aluminiumSpawnrate", "Amount of aluminium ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:7", "-6:7" }, ":");
		copperSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.05_copperSpawnrate", "Amount of copper ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:12", "-6:12" }, ":");
		fluoriteSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.06_fluoriteSpawnrate", "Amount of fluorite ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:6", "-6:6" }, ":");
		niterSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.07_niterSpawnrate", "Amount of niter ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:6", "-6:6" }, ":");
		tungstenSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.08_tungstenSpawnrate", "Amount of tungsten ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:10", "-6:10" }, ":");
		leadSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.09_leadSpawnrate", "Amount of lead ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:6", "-6:6" }, ":");
		berylliumSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.10_berylliumSpawnrate", "Amount of beryllium ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:6", "-6:6" }, ":");
		thoriumSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.11_thoriumSpawnrate", "Amount of thorium ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:7", "-6:7" }, ":");
		ligniteSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.12_ligniteSpawnrate", "Amount of lignite ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:2", "-6:2" }, ":");
		asbestosSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.13_asbestosSpawnRate", "Amount of asbestos ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:2", "-6:2" }, ":");
		lithiumSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.14_lithiumSpawnRate", "Amount of schist lithium ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:6", "-6:6" }, ":");
		rareSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.15_rareEarthSpawnRate", "Amount of rare earth ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:6", "-6:6" }, ":");
		oilcoalSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.16_oilCoalSpawnRate", "Spawns an oily coal vein every nTH chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:128", "-6:128" }, ":");
		gassshaleSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.17_gasShaleSpawnRate", "Amount of oil shale veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:5", "-6:5" }, ":");
		explosivebubbleSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.18_explosiveBubbleSpawnRate", "Spawns an explosive gas bubble every nTH chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:80", "-6:80" }, ":");
		gasbubbleSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.19_gasBubbleSpawnRate", "Spawns a gas bubble every nTH chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:40", "-6:40" }, ":");
		cinnabarSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.20_cinnabarSpawnRate", "Amount of cinnabar ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:1", "-6:1" }, ":");
		cobaltSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.21_cobaltSpawnRate", "Amount of cobalt ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:2", "-6:2" }, ":");
		
		ironClusterSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.22_ironClusterSpawn", "Amount of iron cluster veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:4", "-6:4" }, ":");
		titaniumClusterSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.23_titaniumClusterSpawn", "Amount of titanium cluster veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:2", "-6:2" }, ":");
		aluminiumClusterSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.24_aluminiumClusterSpawn", "Amount of aluminium cluster veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:3", "-6:3" }, ":");
		copperClusterSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.24_copperClusterSpawn", "Amount of copper cluster veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:3", "-6:3" }, ":");
		
		malachiteSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.24_malachiteSpawn", "Amount of malachite veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:1" }, ":");
		
		reiiumSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.25_reiiumSpawnRate", "Amount of reiium ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "-29:1" }, ":");
		weidaniumSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.26_weidaniumSpawnRate", "Amount of weidanium ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "-31:1" }, ":");
		australiumSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.27_australiumSpawnRate", "Amount of australium ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "-31:1" }, ":");
		verticiumSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.28_verticiumSpawnRate", "Amount of verticium ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "-30:1" }, ":");
		unobtainiumSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.29_unobtainiumSpawnRate", "Amount of unobtainium ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "-28:1" }, ":");
		daffergonSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.30_daffergonSpawnRate", "Amount of daffergon ore veins per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "-30:1" }, ":");
		
		bedrockOilSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.31_bedrockOilSpawnRate", "Spawn bedrock oil every nTH chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:200", "-6:200" }, ":");
		alexandriteSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.32_alexandriteSpawnRate", "Spawns an alexandrite vein every nTH chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:100" }, ":");
		oilBubbleSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "01.33_oilSpawnRate", "Spawns an oil bubble every nTH chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:100" }, ":");
		netherUraniumSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "02.N00_uraniumSpawnrate", "Amount of nether uranium per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "-1:8" }, ":");
		netherTungstenSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "02.N01_tungstenSpawnrate", "Amount of nether tungsten per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "-1:10" }, ":");
		netherSulfurSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "02.N02_sulfurSpawnrate", "Amount of nether sulfur per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "-1:26" }, ":");
		netherPhosphorusSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "02.N03_phosphorusSpawnrate", "Amount of nether phosphorus per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "-1:24" }, ":");
		netherCoalSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "02.N04_coalSpawnrate", "Amount of nether coal per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "-1:24" }, ":");
		netherPlutoniumSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "02.N05_plutoniumSpawnrate", "Amount of nether plutonium per chunk, if enabled - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "-1:8" }, ":");
		netherCobaltSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "02.N06_cobaltSpawnrate", "Amount of nether cobalt per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "-1:2" }, ":");
		dunaOilSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "0.2S1_oilSpawnRate", "Spawns an oil bubble every nTH chunk (on Duna) - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "16:100" }, ":");
		laytheOilSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "0.2S2_oilSpawnRate", "Spawns a DS oil bubble every nTH chunk (on Laythe) - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "22:100" }, ":");
		eveGasSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "0.2S3_gasSpawnRate", "Spawns a natural gas bubble every nTH chunk (on Eve) - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "18:100" }, ":");
		endTixiteSpawn = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_ORES, "03.E01_tixiteSpawnrate", "Amount of end trixite per chunk - <dimID:amount> (Int:Int)", Integer.class, Integer.class, new String[]{ "1:8" }, ":");
		

		//Structures
		radioStructure = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_STRUCTURES, "03.01_radioSpawn", "Spawn radio station on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:1000" }, ":");
		antennaStructure = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_STRUCTURES, "03.02_antennaSpawn", "Spawn antenna on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:750" }, ":");
		atomStructure = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_STRUCTURES, "03.03_atomSpawn", "Spawn power plant on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:500" }, ":");
		vertibirdStructure = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_STRUCTURES, "03.04_vertibirdSpawn", "Spawn vertibird on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:500" }, ":");
		dungeonStructure = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_STRUCTURES, "03.05_dungeonSpawn", "Spawn library dungeon on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:64" }, ":");
		relayStructure = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_STRUCTURES, "03.06_relaySpawn", "Spawn relay on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:500" }, ":");
		satelliteStructure = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_STRUCTURES, "03.07_satelliteSpawn", "Spawn satellite dish on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:500" }, ":");
		bunkerStructure = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_STRUCTURES, "03.08_bunkerSpawn", "Spawn bunker on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:1000" }, ":");
		siloStructure = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_STRUCTURES, "03.09_siloSpawn", "Spawn missile silo on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:1000" }, ":");
		factoryStructure = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_STRUCTURES, "03.10_factorySpawn", "Spawn factory on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:1000" }, ":");
		dudStructure = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_STRUCTURES, "03.11_dudSpawn", "Spawn dud on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:500" }, ":");
		spaceshipStructure = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_STRUCTURES, "03.12_spaceshipSpawn", "Spawn spaceship on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:1000" }, ":");
		barrelStructure = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_STRUCTURES, "03.13_barrelSpawn", "Spawn waste tank on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:5000" }, ":");
		broadcaster = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_STRUCTURES, "03.14_broadcasterSpawn", "Spawn corrupt broadcaster on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:5000" }, ":");
		minefreq = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_STRUCTURES, "03.15_landmineSpawn", "Spawn AP landmine on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:64" }, ":");
		radfreq = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_STRUCTURES, "03.17_radHotsoptSpawn", "Spawn big radiation hotspot on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:5000" }, ":");
		vaultfreq = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_STRUCTURES, "03.18_vaultSpawn", "Spawn locked safe on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:2500" }, ":");
		
		geyserWater = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_STRUCTURES, "03.19_geyserWaterSpawn", "Spawn water geyser on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:3000" }, ":");
		geyserChlorine = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_STRUCTURES, "03.20_geyserChlorineSpawn", "Spawn poison geyser on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:3000" }, ":");
		geyserVapor = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_STRUCTURES, "03.21_geyserVaporSpawn", "Spawn vapor geyser on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:500" }, ":");
		geyserNether = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_STRUCTURES, "03.22_geyserNetherSpawn", "Spawn nether geyser on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "-1:2" }, ":");
        
		capsuleStructure = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_DUNGEONS, "03.24_capsuleSpawn", "Spawn landing capsule on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:100" }, ":");
		arcticStructure = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_DUNGEONS, "03.25_arcticVaultSpawn", "Spawn artic code vault on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:500" }, ":");
		jungleStructure = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_DUNGEONS, "03.26_jungleDungeonSpawn", "Spawn jungle dungeon on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:2000" }, ":");
		pyramidStructure = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_DUNGEONS, "03.27_pyramidSpawn", "Spawn pyramid on every nTH chunk - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:4000" }, ":");
		
		meteorStrikeChance = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_METEORS, "05.01_meteorStrikeChance", "The probability of a meteor spawning per tick (an average of once every nTH ticks) - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ 
			"0:"+20 * 10 * 60 * 5, //Earth
			"-28:"+20 * 10 * 5, //Moon
			"-29:"+20 * 10 * 13, //Mars
			"-31:"+20 * 10 * 60 * 15,  //Venus
			"-13:"+20 * 10 * 30, //Mercury
			"-20:"+20 * 10 * 60 * 10, //Ceres
			"-1500:"+20 * 10 * 3, //IO
			"-1501:"+20 * 10 * 25, //Europa
			"-1506:"+20 * 10 * 60, //Ganymede
			"-1505:"+20 * 10 * 60 * 2, //Callisto
			"-1507:"+20 * 10 * 6, //Rhea
			"-1508:"+20 * 10 * 60 * 8, //Titan
			"-1511:"+20 * 10 * 60 * 4, //Iapetus
			"-1510:"+20 * 10 * 60 * 3, //Titania
			"-1509:"+20 * 10 * 60 * 9, //Oberon
			"-1504:"+20 * 10 * 60 * 20 //Triton
		}, ":");
		meteorShowerChance = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_METEORS, "05.02_meteorShowerChance", "The probability of a meteor spawning during meteor shower per tick (an average of once every nTH ticks) - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ 
			"0:"+20 * 60 * 60 * 5, //Earth
			"-28:"+20 * 60 * 5, //Moon
			"-29:"+20 * 60 * 13, //Mars
			"-31:"+20 * 60 * 60 * 15,  //Venus
			"-13:"+20 * 60 * 30, //Mercury
			"-20:"+20 * 60 * 60 * 10, //Ceres
			"-1500:"+20 * 60 * 3, //IO
			"-1501:"+20 * 60 * 25, //Europa
			"-1506:"+20 * 60 * 60, //Ganymede
			"-1505:"+20 * 60 * 60 * 2, //Callisto
			"-1507:"+20 * 60 * 6, //Rhea
			"-1508:"+20 * 60 * 60 * 8, //Titan
			"-1511:"+20 * 60 * 60 * 4, //Iapetus
			"-1510:"+20 * 60 * 60 * 3, //Titania
			"-1509:"+20 * 60 * 60 * 9, //Oberon
			"-1504:"+20 * 60 * 60 * 20 //Triton
		}, ":");
		meteorShowerDuration = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_METEORS, "05.03_meteorShowerDuration", "Max duration of meteor shower in ticks - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ 
			"0:"+20 * 60 * 5, //Earth
			"-28:"+20 * 5, //Moon
			"-29:"+20 * 13, //Mars
			"-31:"+20 * 60 * 15,  //Venus
			"-13:"+20 * 30, //Mercury
			"-20:"+20 * 60 * 10, //Ceres
			"-1500:"+20 * 3, //IO
			"-1501:"+20 * 25, //Europa
			"-1506:"+20 * 60, //Ganymede
			"-1505:"+20 * 60 * 2, //Callisto
			"-1507:"+20 * 6, //Rhea
			"-1508:"+20 * 60 * 8, //Titan
			"-1511:"+20 * 60 * 4, //Iapetus
			"-1510:"+20 * 60 * 3, //Titania
			"-1509:"+20 * 60 * 9, //Oberon
			"-1504:"+20 * 60 * 20 //Triton
		}, ":");
		

		String mobModRadComment = "Amount of radiation resistance all the mobs of that mod get. Radresistance s is calculated as s=(1-0.1^r). So a resistance value of 3.0 means that 99.9%=(1-0.1^3.0) of the radiation gets blocked. - <mod=radresistance> (String:Float)";
		mobModRadresistance = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_MOBS, "12.01_mob_Mod_Radresistance", mobModRadComment, String.class, Float.class, new String[]{ 
			"srparasites=0.2",
			"thaumcraft=0.75",
		}, "=");
		

		String mobRadComment = "Amount of radiation resistance the mob gets. Radresistance s is calculated as s=(1-0.1^r). So a resistance value of 3.0 means that 99.9%=(1-0.1^3.0) of the radiation gets blocked. - <mod:mobitentifier=radresistance> (String:Float)";
		mobRadresistance = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_MOBS, "12.02_mob_Radresistance", mobRadComment, String.class, Float.class, new String[]{ 
			"minecraft:parrot=0.5", 
			"minecraft:rabbit=1.0", 
			"techguns:ghastling=1.2",
			"minecraft:enderman=1.5", 
			"minecraft:blaze=2.0", 
			"techguns:alienbug=2.2",
			"minecraft:bat=2.5", 
			"minecraft:ghast=3.0", 
			"mutantbeasts:mutant_creeper=3.2",
			"minecraft:squid=3.5", 
			"minecraft:spider=4.0", 
			"mutantbeasts:mutant_enderman=4.2",
			"techguns:outcast=4.5",
			"minecraft:cave_spider=5.0", 
			"minecraft:silverfish=6.0", 
			"techguns:stormtrooper=6.4",
			"techguns:cyberdemon=6.5",
			"minecraft:endermite=7.0", 
			"pvj:pvj_beach_starfish=7.5", 
			"pvj:pvj_clam=7.8", 
			"minecraft:shulker=8.0",
			"pvj:pvj_starfish=8.2", 
			"techguns:attackhelicopter=8.5",
			"minecraft:ender_dragon=9.0", 
			"pvj:pvj_snail=9.1", 
			"pvj:pvj_firefly=10.0", 
			"pvj:pvj_fly=11.0",
			"mysticalworld:entity_hell_sprout=2.0",
			"mysticalworld:entity_lava_cat=2.0",
			"thaumcraft:taintseed=1.2",
			"thaumcraft:taintseedprime=4.0"
		}, "=");
		mobModRadimmune = CommonConfig.createConfigHashSet(config, CommonConfig.CATEGORY_MOBS, "12.03_mob_Mod_Radimmune", "List of mods whose entities should all be immune to radiation. - <mod> (String)", String.class, new String[]{
			"biomesoplenty",
			"galacticraftcore", 
			"galacticraftplanets", 
			"extraplanets",
			"thaumicaugmentation",
                        "enderskills",
                        "thaumadditions",
			"cyberware",
			"rewired"
		});
		mobRadimmune = CommonConfig.createConfigHashSet(config, CommonConfig.CATEGORY_MOBS, "12.04_mob_Radimmune", "List of mobs that are immune to radiation. - <mod:mobitentifier> (String)", String.class, new String[]{ 
			"minecraft:magma_cube", 
			"minecraft:slime", 
			"minecraft:vex", 
			"minecraft:villager_golem", 
			"minecraft:snowman", 
			"minecraft:witch",
			"pvj:pvj_icecube",
			"tconstruct:blueslime",
			"mutantbeasts:mutant_snow_golem",
			"mutantbeasts:mutant_zombie",
			"mutantbeasts:mutant_skeleton",
			"techguns:zombieminer",
			"techguns:zombiefarmer",
			"techguns:zombiesoldier",
			"techguns:zombiepigmansoldier",
			"techguns:zombiepoliceman",
			"techguns:skeletonsoldier",
			"techguns:supermutantbasic",
			"techguns:supermutantelite",
			"techguns:supermutantheavy",
			"deepmoblearning:glitch",
			"divinefavor:entity.ping",
			"divinefavor:entity.rope_barrier",
			"divinefavor:entity.rope_explosive",
			"divinefavor:entity.rope_glowing",
			"divinefavor:entity.rope_guide",
			"divinefavor:entity.rope_inert",
			"divinefavor:entity.rope_luminous",
			"divinefavor:entity.rope_teleporting",
			"divinefavor:entity.spell_arrow",
			"divinefavor:entity.spooky",
			"divinefavor:entity.stoneball",
			"embers:ancient_golem",
			"embers:ember_light",
			"embers:ember_packet",
			"embers:ember_projectile",
			"embers:magma_projectile",
			"thaumcraft:eldritchwarden",
			"thaumcraft:eldritchguardian",
			"thaumcraft:cultistportalgreater",
			"thaumcraft:cultistportallesser",
			"thaumcraft:eldritchgolem",
			"thaumcraft:arcanebore",
			"thaumcraft:fluxrift",
			"thaumcraft:golem",
			"thaumcraft:mindspider",
			"thaumcraft:spellbat",
			"thaumcraft:turretadvanced",
			"thaumcraft:turretbasic",
			"tombstone:ghostly_shape"
		});
	
		mobGear = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_MOBS, "12.05_mobGear", "If true then mobs will be given gear (armor/weapons/gasmasks) from this mod when spawned", true);
		modLoot = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_MOBS, "12.06_modLoot", "If true then this mod will generarte loot for chests", true);
	

		doFillCraterWithWater =  CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_NUKES, "03.04_doFillCraterWithWater", "If true then nukes will fill the crater with water if it is in a wet place. It creates a bit of lagg but looks better than without it.", true);
		fillCraterWithWater = CommonConfig.createConfigHashMap(config, CommonConfig.CATEGORY_NUKES, "03.04_fillCraterWithWater", "Waterlevel per dimension which the nuke uses to fill the crater. {+n=>waterlevel height, 0=>dimension waterlevel, -n=> n blocks below dimension waterlevel } - <dimID:n> (Int:Int)", Integer.class, Integer.class, new String[]{ "0:0" }, ":");
		
		peaceDimensionsIsWhitelist =  CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_NUKES, "03.05_peaceDimensionsIsWhitelist", "If true then the listed dimensions below are all peacefull. If false then the listed dimensions are the only ones where destruction happens.", true);
		peaceDimensions = CommonConfig.createConfigHashSet(config, CommonConfig.CATEGORY_ORES, "03.06_peaceDimensions", "List of Dimensions where block destruction and damage is disabled (Used for server lobbies/science servers/pvp arenas) - <dimID> (Int)", Integer.class, new String[]{
		});

		bedrockOreBlacklist = CommonConfig.createConfigHashSet(config, CommonConfig.CATEGORY_ORES, "08.01_bedrockOreBlacklist", "List of OreDict entries that should not have bedrock ores - <ore> (String)", String.class, new String[]{
			"oreTh232",
			"oreThorium232",
			"oreVolcanic",
			"oreSteel"
		});
	}

	public static boolean isWarDim(World world){
		return isWarDim(world.provider.getDimension());
	}

	public static boolean isWarDim(int dimID){
		if(peaceDimensionsIsWhitelist)
			return !peaceDimensions.contains(dimID);
		else
			return peaceDimensions.contains(dimID);
	}
}
