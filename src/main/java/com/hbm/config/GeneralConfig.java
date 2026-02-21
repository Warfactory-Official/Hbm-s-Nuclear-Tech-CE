package com.hbm.config;

import com.hbm.main.MainRegistry;
import com.hbm.render.GLCompat;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class GeneralConfig {

	public static double conversionRateHeToRF = 1.0F;
	public static boolean autoCableConversion = false;
	public static boolean enablePacketThreading = true;
	public static int packetThreadingCoreCount = 1;
	public static int packetThreadingMaxCount = 2;
	public static boolean packetThreadingErrorBypass = false;
    public static boolean enableThreadedNodeSpaceUpdate = true;
	public static boolean enableDebugMode = false;
	public static boolean enableDebugWorldGen = false;
	public static boolean enableSkyboxes = true;
	public static boolean enableKeybindOverlap = true;
	public static boolean enableFluidContainerCompat = true;
	public static boolean enableMycelium = false;
	public static boolean enablePlutoniumOre = false;
	public static boolean enableDungeons = true;
	public static boolean enableMDOres = true;
	public static boolean enableMines = true;
	public static boolean enableRad = true;
	public static boolean enableNITAN = true;
	public static boolean enableAutoCleanup = false;
	public static boolean enableMeteorStrikes = true;
	public static boolean enableMeteorShowers = true;
	public static boolean enableMeteorTails = true;
	public static boolean enableSpecialMeteors = true;
	public static boolean enableBomberShortMode = false;
	public static boolean enableVaults = true;
	public static boolean enableRads = true;
	public static boolean enableCoalGas = true;
	public static boolean enableAsbestosDust = true;
    public static boolean enableRadon = true;
    public static boolean enableCarbonMonoxide = true;
    public static boolean enableFlammableGas = true;
    public static boolean enableExplosiveGas = true;
    public static boolean enableMeltdownGas = true;
	public static boolean advancedRadiation = true;
	public static boolean enableCataclysm = false;
	public static boolean enableExtendedLogging = false;
	public static boolean enableHardcoreTaint = false;
	public static boolean enableGuns = true;
	public static boolean ssgAnim = true;
	public static boolean enableVirus = true;
	public static boolean enableCrosshairs = true;
	public static boolean instancedParticles = true;
	public static boolean callListModels = true;
	public static boolean useShaders = false;
	public static boolean useShaders2 = false;
	public static boolean bloom = true;
	public static boolean heatDistortion = true;
	public static boolean recipes = true;
	public static boolean jei = true;
	public static boolean changelog = true;
	public static boolean registerTanks = true;
	public static boolean duckButton = true;
    public static boolean enableMOTD = true;
    public static boolean enableGuideBook = true;
	public static boolean depthEffects = true;
	public static boolean flashlight = true;
	public static boolean flashlightVolumetric = true;
	public static boolean bulletHoleNormalMapping = true;
	public static int flowingDecalAmountMax = 20;
	public static boolean bloodFX = true;
	public static int hintPos = 0;
	public static int decoToIngotRate = 25;
	public static int crucibleMaxCharges = 16;
	public static boolean enableReEval = true;
	public static boolean enableSteamParticles = true;
	public static boolean enableServerRecipeSync = true;
	public static boolean enableExpensiveMode = false;
	
	public static boolean enable528 = false;
	public static boolean enable528ReasimBoilers = true;
	public static boolean enable528ColtanDeposit = true;
	public static boolean enable528ColtanSpawn = false;
	public static boolean enable528BedrockDeposit = true;
	public static boolean enable528BedrockSpawn = false;
	public static boolean enableReflectorCompat = false;
	public static int coltanRate = 2;
	public static int bedrockRate = 50;
	public static boolean enableThreadedAtmospheres = true;
	public static boolean enableHardcoreDarkness = false;

	public static boolean enableLBSM = false;
	public static boolean enableLBSMFullSchrab = true;
	public static boolean enableLBSMShorterDecay = true;
	public static boolean enableLBSMSimpleArmorRecipes = true;
	public static boolean enableLBSMSimpleToolRecipes = true;
	public static boolean enableLBSMSimpleAlloy = true;
	public static boolean enableLBSMSimpleChemsitry = true;
	public static boolean enableLBSMSimpleCentrifuge = true;
	public static boolean enableLBSMUnlockAnvil = true;
	public static boolean enableLBSMSimpleCrafting = true;
	public static boolean enableLBSMSimpleMedicineRecipes = true;
	public static boolean enableLBSMSafeCrates = true;
	public static boolean enableLBSMSafeMEDrives = true;
	public static boolean enableLBSMIGen = true;
    public static boolean enable528BosniaSimulator = false;

	public static boolean enableBlockReplcement = false;

    public static void loadFromConfig(Configuration config){
		enablePacketThreading = config.get(CommonConfig.CATEGORY_GENERAL, "0.01_enablePacketThreading", true, "Enables creation of a separate thread to increase packet processing speed on servers. Disable this if you are having anomalous crashes related to memory connections.").getBoolean(true);
		packetThreadingCoreCount = config.get(CommonConfig.CATEGORY_GENERAL, "0.02_packetThreadingCoreCount", 1, "Number of core threads to create for packets (recommended 1).").getInt(1);
		packetThreadingMaxCount = config.get(CommonConfig.CATEGORY_GENERAL, "0.03_packetThreadingMaxCount", 2, "Maximum number of threads to create for packet threading. Must be greater than or equal to 0.02_packetThreadingCoreCount.").getInt(2);
		packetThreadingErrorBypass = config.get(CommonConfig.CATEGORY_GENERAL, "0.04_packetThreadingErrorBypass", false, "Forces the bypassing of most packet threading errors, only enable this if directed to or if you know what you're doing.").getBoolean(false);
		enableServerRecipeSync = config.get(CommonConfig.CATEGORY_GENERAL, "0.05_enableServerRecipeSync", true, "Syncs any recipes customised via JSON to clients connecting to the server.").getBoolean(true);
        enableThreadedNodeSpaceUpdate = config.get(CommonConfig.CATEGORY_GENERAL, "0.07_enableThreadedNodeSpaceUpdate", true, "Enables threaded updating of the nodespace. This can improve performance, but may cause issues with certain mods.").getBoolean(true);
		enableBlockReplcement = config.get(CommonConfig.CATEGORY_GENERAL, "0.99_CE_01_enableBlockAutoReplacing", false, """
                Enables automatic block replacement for missing blocks to avoid giant holes in the ground when they got removed. This may severely impact chunkloading performance,
                only enable when you are sure that we removed some blocks AND we added that to this replacement system AND you are absolutely sure about what you are doing.
                Currently only works for hbm:waste_*.""").getBoolean(false);
		enableDebugMode = config.get(CommonConfig.CATEGORY_GENERAL, "1.00_enableDebugMode", false, "Enable debugging mode").getBoolean(false);
		enableDebugWorldGen = config.get(CommonConfig.CATEGORY_GENERAL, "1.00_enableDebugWorldGen", false, "Enable debugging mode for phased structure generation. Separate from the previous option!").getBoolean(false);
		enableSkyboxes = config.get(CommonConfig.CATEGORY_GENERAL, "1.00_enableSkybox", true, "If enabled, will try to use NTM's custom skyboxes.").getBoolean(true);
		enableMycelium = config.get(CommonConfig.CATEGORY_GENERAL, "1.01_enableMyceliumSpread", false, "Allows glowing mycelium to spread").getBoolean(false);
		enablePlutoniumOre = config.get(CommonConfig.CATEGORY_GENERAL, "1.02_enablePlutoniumNetherOre", false, "Enables plutonium ore generation in the nether").getBoolean(false);
		enableDungeons = config.get(CommonConfig.CATEGORY_GENERAL, "1.03_enableDungeonSpawn", true, "Allows structures and dungeons to spawn.").getBoolean(true);
		enableMDOres = config.get(CommonConfig.CATEGORY_GENERAL, "1.04_enableOresInModdedDimensions", true, "Allows NTM ores to generate in modded dimensions").getBoolean(true);
		enableMines = config.get(CommonConfig.CATEGORY_GENERAL, "1.05_enableLandmineSpawn", true, "Allows landmines to generate").getBoolean(true);
		enableRad = config.get(CommonConfig.CATEGORY_GENERAL, "1.06_enableRadHotspotSpawn", true, "Allows radiation hotspots to generate").getBoolean(true);
		enableNITAN = config.get(CommonConfig.CATEGORY_GENERAL, "1.07_enableNITANChestSpawn", true, "Allows chests to spawn at specific coordinates full of powders").getBoolean(true);
		enableAutoCleanup = config.get(CommonConfig.CATEGORY_GENERAL, "1.09_enableAutomaticRadCleanup", false, "Allows for waste earth blocks (dirt, grass, mycellium) to turn back into dirt immediately.").getBoolean(false);
		enableMeteorStrikes = config.get(CommonConfig.CATEGORY_GENERAL, "1.10_enableMeteorStrikes", true, "Enables the singular meteor strikes. If set to false, meteorites will never spawn.").getBoolean(true);
		enableMeteorShowers = config.get(CommonConfig.CATEGORY_GENERAL, "1.11_enableMeteorShowers", true, "Enables the meteor shower event. Separate from the previous option!").getBoolean(true);
		enableMeteorTails = config.get(CommonConfig.CATEGORY_GENERAL, "1.12_enableMeteorTails", true, "Enables the meteor smoke trail effect behind it.").getBoolean(true);
		enableSpecialMeteors = config.get(CommonConfig.CATEGORY_GENERAL, "1.13_enableSpecialMeteors", false, "Allows for special meteors to spawn. NOT RECOMMENDED FOR REGULAR SURVIVAL").getBoolean(false);
		enableBomberShortMode = config.get(CommonConfig.CATEGORY_GENERAL, "1.14_enableBomberShortMode", false, "Has bomber planes spawn in closer to the target for use with smaller render distances").getBoolean(false);
		enableVaults = config.get(CommonConfig.CATEGORY_GENERAL, "1.15_enableVaultSpawn", true, "Allows locked safes to spawn").getBoolean(true);
		enableRads = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_GENERAL, "1.16_enableRadiation", "GENERAL SWITCH: Enables radiation system", true);
		enableCataclysm = config.get(CommonConfig.CATEGORY_GENERAL, "1.17_enableCataclysm", false, "Causes satellites to fall whenever a mob dies").getBoolean(false);
		enableExtendedLogging = config.get(CommonConfig.CATEGORY_GENERAL, "1.18_enableExtendedLogging", false, "Logs uses of the detonator, nuclear explosions, missile launches, grenades, etc.").getBoolean(false);
		enableHardcoreTaint = config.get(CommonConfig.CATEGORY_GENERAL, "1.19_enableHardcoreTaint", false, "Allows taint blocks to basically be unstoppable. NOT RECOMMENDED FOR REGULAR SURVIVAL").getBoolean(false);
		enableGuns = config.get(CommonConfig.CATEGORY_GENERAL, "1.20_enableGuns", true, "Prevents new system guns to be fired").getBoolean(true);
		enableVirus = config.get(CommonConfig.CATEGORY_GENERAL, "1.21_enableVirus", false, "Allows virus blocks to spread").getBoolean(false);
		enableCrosshairs = config.get(CommonConfig.CATEGORY_GENERAL, "1.22_enableCrosshairs", true, "Shows custom crosshairs when an NTM gun is being held").getBoolean(true);
		useShaders2 = config.get(CommonConfig.CATEGORY_GENERAL, "1.23_enableShaders2", false, "Enables old NTM Reloaded shaders, courtesy of Drillgon. NOT RECOMMENDED TO TURN IT ON").getBoolean(false);
		Property ssg_anim = config.get(CommonConfig.CATEGORY_GENERAL, "1.24_ssgAnimType", true);
		ssg_anim.setComment("Which supershotgun reload animation to use. True is Drillgon's animation, false is Bob's animation");
		ssgAnim = ssg_anim.getBoolean();
		instancedParticles = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_GENERAL, "1.25_instancedParticles", "Enables instanced particle rendering for some particles, which makes them render several times faster. May not work on all computers, and will break with shaders.", true);
		depthEffects = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_GENERAL, "1.25_depthBufferEffects", "Enables effects that make use of reading from the depth buffer", true);
		flashlight = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_GENERAL, "1.25_flashlights", "Enables dynamic directional lights", true);
		flashlightVolumetric = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_GENERAL, "1.25_flashlight_volumetrics", "Enables volumetric lighting for directional lights", true);
		bulletHoleNormalMapping = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_GENERAL, "1.25_bullet_hole_normal_mapping", "Enables normal mapping on bullet holes, which can improve visuals", true);
		flowingDecalAmountMax = CommonConfig.createConfigInt(config, CommonConfig.CATEGORY_GENERAL, "1.25_flowing_decal_max", "The maximum number of 'flowing' decals that can exist at once (eg blood that can flow down walls)", 20);
		
		callListModels = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_GENERAL, "1.26_callListModels", "Enables call lists for a few models, making them render extremely fast", true);
		enableReflectorCompat = config.get(CommonConfig.CATEGORY_GENERAL, "1.24_enableReflectorCompat", false, "Enable old reflector oredict name (\"plateDenseLead\") instead of new \"plateTungCar\"").getBoolean(false);
		
		enableCoalGas = config.get(CommonConfig.CATEGORY_GENERAL, "1.26_enableCoalDust", true, "Allows the coal gas to spawn (e.g. after breaking coal ore).").getBoolean(true);
		enableAsbestosDust = config.get(CommonConfig.CATEGORY_GENERAL, "1.26_enableAsbestosDust", true, "Allows the asbestos gas to spawn (e.g. after breaking asbestos ore or chrysotile).").getBoolean(true);
        enableRadon = config.get(CommonConfig.CATEGORY_GENERAL, "1.26_enableRadonGas", true, "Allows the radon gas to spawn (e.g. after breaking uranium ore).").getBoolean(true);
        enableCarbonMonoxide = config.get(CommonConfig.CATEGORY_GENERAL, "1.26_enableCarbonMonoxide", true, "Allows the carbon monoxide gas to spawn (e.g. after breaking nether coal ore).").getBoolean(true);
        enableFlammableGas = config.get(CommonConfig.CATEGORY_GENERAL, "1.26_enableFlammableGas", true, "Allows the flammable gas to spawn in the world.").getBoolean(true);
        enableExplosiveGas = config.get(CommonConfig.CATEGORY_GENERAL, "1.26_enableExplosiveGas", true, "Allows the explosive gas to spawn in the world.").getBoolean(true);
        enableMeltdownGas = config.get(CommonConfig.CATEGORY_GENERAL, "1.26_enableMeltdownGas", true, "Allows the meltdown gas to spawn (e.g. after ZIRNOX explosion).").getBoolean(true);
		enableReEval = config.get(CommonConfig.CATEGORY_GENERAL, "1.27_enableReEval", true, "Allows re-evaluating power networks on link remove instead of destroying and recreating").getBoolean(true);
		enableSteamParticles = config.get(CommonConfig.CATEGORY_GENERAL, "1.27.1_enableSteamParticles", true, "If disabled, auxiliary cooling towers and large cooling towers will not emit steam particles when in use.").getBoolean(true);
		
		recipes = config.get(CommonConfig.CATEGORY_GENERAL, "1.28_enableRecipes", true, "A general switch for ALL crafting table/smelting recipes. If set to false, all recipes will be disabled.").getBoolean(true);
		registerTanks = config.get(CommonConfig.CATEGORY_GENERAL, "1.28_registerTanks", true, "A general switch for ALL the tanks items in the mod (e.g. universal fluid, lead, barrels, packed containers). If set to false, they won't be registered as items in the game." ).getBoolean(true);
		
		jei = config.get(CommonConfig.CATEGORY_GENERAL, "1.28_enableJei", true, "Enables JEI compatibility").getBoolean(true);
		changelog = config.get(CommonConfig.CATEGORY_GENERAL, "1.28_enableChangelog", true, "Enables the update notification in the chat. NOT USED FOR NOW").getBoolean(true);
		duckButton = config.get(CommonConfig.CATEGORY_GENERAL, "1.28_enableDuckButton", true, "Allows you to summon the duck via pressing O").getBoolean(true);
		bloom = config.get(CommonConfig.CATEGORY_GENERAL, "1.30_enableBloom", true, "Enables the bloom effect which can be visible on the Crucible. Only active if enableShaders2 is set to true.").getBoolean(true);
		heatDistortion = config.get(CommonConfig.CATEGORY_GENERAL, "1.30_enableHeatDistortion", true, "Enables the heat distortion effect. Only active if enableShaders2 is set to true.").getBoolean(true);
		
		Property adv_rads = config.get(CommonConfig.CATEGORY_GENERAL, "1.31_enableAdvancedRadiation", true);
		adv_rads.setComment("Enables a 3 dimensional version of the radiation system that also allows some blocks (like concrete bricks) to stop it from spreading");
		advancedRadiation = adv_rads.getBoolean(true);
		
		bloodFX = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_GENERAL, "1.32_enable_blood_effects", "Enables the over-the-top blood visual effects for some weapons", true);
	
		if((instancedParticles || depthEffects || flowingDecalAmountMax > 0 || bloodFX || bloom || heatDistortion) && (!GLCompat.error.isEmpty() || !useShaders2)){
			MainRegistry.logger.error("Warning - Open GL 3.3 not supported! Disabling 3.3 effects...");
			if(!useShaders2){
				MainRegistry.logger.error("Shader effects manually disabled");
			}
			instancedParticles = false;
			depthEffects = false;
			flowingDecalAmountMax = 0;
			bloodFX = false;
			useShaders2 = false;
			bloom = false;
			heatDistortion = false;
		}
		if(!depthEffects){
			flashlight = false;
			bulletHoleNormalMapping = false;
		}
		if(!flashlight){
			flashlightVolumetric = false;
		}
		
		crucibleMaxCharges = CommonConfig.createConfigInt(config, CommonConfig.CATEGORY_GENERAL, "1.33_crucible_max_charges", "How many times you can use the crucible before recharge", 16);
		
		if(crucibleMaxCharges <= 0){
			crucibleMaxCharges = 16;
		}
		conversionRateHeToRF = CommonConfig.createConfigDouble(config, CommonConfig.CATEGORY_GENERAL, "1.35_conversionRateHeToRF", "One HE is (insert number) RF - <number> (double)", 1.0D);
		autoCableConversion = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_GENERAL, "1.35.1_autoCableConversion", "If enabled, NTM cables will automatically convert FE <-> HE. Note: WILL MAKE ALL OTHER MODS' CABLES USELESS", false);

		hintPos = CommonConfig.createConfigInt(config, CommonConfig.CATEGORY_GENERAL, "1.36_infoOverlayPosition", "Positions where the info overlay will appear (from 0 to 3). 0: Top left\n1: Top right\n2: Center right\n3: Center Left", 0);
		enableFluidContainerCompat = config.get(CommonConfig.CATEGORY_GENERAL, "1.37_enableFluidContainerCompat", true, "If enabled, fluid containers will be oredicted and interchangable in recipes with other mods' containers. Should probably work with things like IE's/GC oil properly.").getBoolean(true);
        enableMOTD = config.get(CommonConfig.CATEGORY_GENERAL, "1.36_enableMOTD", true, "If enabled, shows the 'Loaded mod!' chat message as well as update notifications when joining a world").getBoolean(true);
        enableGuideBook = config.get(CommonConfig.CATEGORY_GENERAL, "1.37_enableGuideBook", true, "If enabled, gives players the guide book when joining the world for the first time").getBoolean(true);
        decoToIngotRate = CommonConfig.createConfigInt(config, CommonConfig.CATEGORY_GENERAL, "1.38_decoToIngotConversionRate", "Chance of successful turning a deco block into an ingot. Default is 25%", 25);
		enableThreadedAtmospheres = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_GENERAL, "1.39_threadedAtmospheres", "If enabled, will run atmosphere blobbing in a separate thread for performance", true);
		enableHardcoreDarkness = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_GENERAL, "1.40_hardcoreDarkness", "If enabled, sets night-time minimum fog to zero, to complement hardcore darkness mods", false);
		enableKeybindOverlap = config.get(CommonConfig.CATEGORY_GENERAL, "1.41_enableKeybindOverlap", true, "If enabled, will handle keybinds that would otherwise be ignored due to overlapping.").getBoolean(true);
		enableExpensiveMode = config.get(CommonConfig.CATEGORY_GENERAL, "1.99_enableExpensiveMode", false, "It does what the name implies.").getBoolean(false);
        

		config.addCustomCategoryComment(CommonConfig.CATEGORY_528, """
                CAUTION
                528 Mode: Please proceed with caution!
                528-Modus: Lassen Sie Vorsicht walten!
                способ-528: действовать с осторожностью!""");
		
		enable528 = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_528, "enable528Mode", "The central toggle for 528 mode.", false);
		enable528ReasimBoilers = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_528, "X528_forceReasimBoilers", "Keeps the RBMK dial for ReaSim boilers on, preventing use of non-ReaSim boiler columns and forcing the use of steam in-/outlets", true);
		enable528ColtanDeposit = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_528, "X528_enableColtanDepsoit", "Enables the coltan deposit. A large amount of coltan will spawn around a single random location in the world.", true);
		enable528ColtanSpawn = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_528, "X528_enableColtanSpawning", "Enables coltan ore as a random spawn in the world. Unlike the deposit option, coltan will not just spawn in one central location.", false);
		enable528BedrockDeposit = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_528, "X528_enableBedrockDepsoit", "Enables bedrock coltan ores in the coltan deposit. These ores can be drilled to extract infinite coltan, albeit slowly.", true);
		enable528BedrockSpawn = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_528, "X528_enableBedrockSpawning", "Enables the bedrock coltan ores as a rare spawn. These will be rarely found anywhere in the world.", false);
		coltanRate = CommonConfig.createConfigInt(config, CommonConfig.CATEGORY_528, "X528_oreColtanFrequency", "Determines how many coltan ore veins are to be expected in a chunk. These values do not affect the frequency in deposits, and only apply if random coltan spanwing is enabled.", 2);
		bedrockRate = CommonConfig.createConfigInt(config, CommonConfig.CATEGORY_528, "X528_bedrockColtanFrequency", "Determines how often (1 in X) bedrock coltan ores spawn. Applies for both the bedrock ores in the coltan deposit (if applicable) and the random bedrock ores (if applicable)", 50);

		config.addCustomCategoryComment(CommonConfig.CATEGORY_LBSM,
                """
                        Will most likely break standard progression!
                        However, the game gets generally easier and more enjoyable for casual players.
                        Progression-braking recipes are usually not too severe, so the mode is generally server-friendly!""");

		enableLBSM = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_LBSM, "enableLessBullshitMode", "The central toggle for LBS mode. Forced OFF when 528 is enabled!", false);
		enableLBSMFullSchrab = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_LBSM, "LBSM_fullSchrab", "When enabled, this will replace schraranium with full schrabidium ingots in the transmutator's output", true);
		enableLBSMShorterDecay = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_LBSM, "LBSM_shortDecay", "When enabled, this will highly accelerate the speed at which nuclear waste disposal drums decay their contents. 60x faster than 528 mode and 5-12x faster than on normal mode.", true);
		enableLBSMSimpleArmorRecipes = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_LBSM, "LBSM_recipeSimpleArmor", "When enabled, simplifies the recipe for armor sets like starmetal or schrabidium.", true);
		enableLBSMSimpleToolRecipes = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_LBSM, "LBSM_recipeSimpleTool", "When enabled, simplifies the recipe for tool sets like starmetal or scrhabidium", true);
		enableLBSMSimpleAlloy = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_LBSM, "LBSM_recipeSimpleAlloy", "When enabled, adds some blast furnace recipes to make certain things cheaper", true);
		enableLBSMSimpleChemsitry = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_LBSM, "LBSM_recipeSimpleChemistry", "When enabled, simplifies some chemical plant recipes", true);
		enableLBSMSimpleCentrifuge = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_LBSM, "LBSM_recipeSimpleCentrifuge", "When enabled, enhances centrifuge outputs to make rare materials more common", true);
		enableLBSMUnlockAnvil = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_LBSM, "LBSM_recipeUnlockAnvil", "When enabled, all anvil recipes are available at tier 1", true);
		enableLBSMSimpleCrafting = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_LBSM, "LBSM_recipeSimpleCrafting", "When enabled, some uncraftable or more expansive items get simple crafting recipes. Scorched uranium also becomes washable", true);
		enableLBSMSimpleMedicineRecipes = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_LBSM, "LBSM_recipeSimpleMedicine", "When enabled, makes some medicine recipes (like ones that require bismuth) much more affordable", true);
		enableLBSMSafeCrates = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_LBSM, "LBSM_safeCrates", "When enabled, prevents crates from becoming radioactive", true);
		enableLBSMSafeMEDrives = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_LBSM, "LBSM_safeMEDrives", "When enabled, prevents ME Drives and Portable Cells from becoming radioactive", true);
		enableLBSMIGen = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_LBSM, "LBSM_iGen", "When enabled, restores the industrial generator to pre-nerf power", true);

		if(enable528) enableLBSM = false;
		// Th3_Sl1ze: I'll temporarily move it here, if no one minds
		// TODO: remove/rework Alc's parser to smth managable and bring these parameters back to WorldConfig

		WorldConfig.newBedrockOres = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_ORES, "2.NB_newBedrockOres", "Enables the generation of bedrock ores", true);
		WorldConfig.limestoneSpawn = CommonConfig.createConfigInt(config, CommonConfig.CATEGORY_ORES, "2.L02_limestoneSpawn", "Amount of limestone block veins per chunk", 1);

		WorldConfig.enableHematite = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_ORES, "2.L00_enableHematite", "Toggles hematite deposits", true);
		WorldConfig.enableMalachite = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_ORES, "2.L01_enableMalachite", "Toggles malachite deposits", true);
		WorldConfig.enableBauxite = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_ORES, "2.L02_enableBauxite", "Toggles bauxite deposits", true);

		WorldConfig.enableSulfurCave = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_ORES, "2.C00_enableSulfurCave", "Toggles sulfur caves", true);
		WorldConfig.enableAsbestosCave = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_ORES, "2.C01_enableAsbestosCave", "Toggles asbestos caves", true);
        
		WorldConfig.enableCraterBiomes = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_BIOMES, "17.B_toggle", "Enables the biome change caused by nuclear explosions", true);
		WorldConfig.craterBiomeId = CommonConfig.createConfigInt(config, CommonConfig.CATEGORY_BIOMES, "17.B00_craterBiomeId", "The numeric ID for the crater biome", 80);
		WorldConfig.craterBiomeInnerId = CommonConfig.createConfigInt(config, CommonConfig.CATEGORY_BIOMES, "17.B01_craterBiomeInnerId", "The numeric ID for the inner crater biome", 81);
		WorldConfig.craterBiomeOuterId = CommonConfig.createConfigInt(config, CommonConfig.CATEGORY_BIOMES, "17.B02_craterBiomeOuterId", "The numeric ID for the outer crater biome", 82);
		WorldConfig.craterBiomeRad = (float) CommonConfig.createConfigDouble(config, CommonConfig.CATEGORY_BIOMES, "17.R00_craterBiomeRad", "RAD/s for the crater biome", 5D);
		WorldConfig.craterBiomeInnerRad = (float) CommonConfig.createConfigDouble(config, CommonConfig.CATEGORY_BIOMES, "17.R01_craterBiomeInnerRad", "RAD/s for the inner crater biome", 25D);
		WorldConfig.craterBiomeOuterRad = (float) CommonConfig.createConfigDouble(config, CommonConfig.CATEGORY_BIOMES, "17.R02_craterBiomeOuterRad", "RAD/s for the outer crater biome", 0.5D);
		WorldConfig.craterBiomeWaterMult = (float) CommonConfig.createConfigDouble(config, CommonConfig.CATEGORY_BIOMES, "17.R03_craterBiomeWaterMult", "Multiplier for RAD/s in crater biomes when in water", 5D);
	}
}
