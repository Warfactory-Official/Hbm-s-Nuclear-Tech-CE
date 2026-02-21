package com.hbm.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class BombConfig {

	public static int gadgetRadius = 150;
	public static int boyRadius = 120;
	public static int manRadius = 175;
	public static int mikeRadius = 250;
	public static int tsarRadius = 500;
	public static int prototypeRadius = 150;
	public static int fleijaRadius = 50;
	public static int soliniumRadius = 150;
	public static int n2Radius = 200;
	public static int missileRadius = 100;
	public static int mirvRadius = 100;
	public static int fatmanRadius = 35;
	public static int nukaRadius = 25;
	public static int aSchrabRadius = 20;
	public static int riggedStarRange = 50;
	public static int riggedStarTicks = 60 * 20;

	public static int maxCustomTNTRadius = 150;
	public static int maxCustomNukeRadius = 200;
	public static int maxCustomHydroRadius = 350;
	public static int maxCustomDirtyRadius = 200;
	public static int maxCustomBaleRadius = 350;
	public static int maxCustomSchrabRadius = 250;
	public static int maxCustomSolRadius = 350;
	public static int maxCustomEuphLvl = 20;
	
	public static int mk5 = 50;
	public static int blastSpeed = 1024;
	public static int falloutRange = 100;
	public static int fChunkSpeed = 5;
	public static int falloutDelay = 4;
	public static boolean spawnFire = false;
	public static int limitExplosionLifespan = 0;
	public static boolean disableNuclear = false;
	public static boolean enableNukeClouds = true;
	public static boolean enableNukeNBTSaving = true;
	public static boolean chunkloading = true;
	public static int explosionAlgorithm = 2;
	public static int maxThreads = -1;
    public static boolean safeCommit = false;
	
	public static void loadFromConfig(Configuration config) {
		Property propGadget = config.get(CommonConfig.CATEGORY_NUKES, "3.00_gadgetRadius", 150);
		propGadget.setComment("Radius of the Gadget");
		gadgetRadius = propGadget.getInt();
		Property propBoy = config.get(CommonConfig.CATEGORY_NUKES, "3.01_boyRadius", 120);
		propBoy.setComment("Radius of Little Boy");
		boyRadius = propBoy.getInt();
		Property propMan = config.get(CommonConfig.CATEGORY_NUKES, "3.02_manRadius", 175);
		propMan.setComment("Radius of Fat Man");
		manRadius = propMan.getInt();
		Property propMike = config.get(CommonConfig.CATEGORY_NUKES, "3.03_mikeRadius", 250);
		propMike.setComment("Radius of Ivy Mike");
		mikeRadius = propMike.getInt();
		Property propTsar = config.get(CommonConfig.CATEGORY_NUKES, "3.04_tsarRadius", 500);
		propTsar.setComment("Radius of the Tsar Bomba");
		tsarRadius = propTsar.getInt();
		Property propPrototype = config.get(CommonConfig.CATEGORY_NUKES, "3.05_prototypeRadius", 150);
		propPrototype.setComment("Radius of the Prototype");
		prototypeRadius = propPrototype.getInt();
		Property propFleija = config.get(CommonConfig.CATEGORY_NUKES, "3.06_fleijaRadius", 50);
		propFleija.setComment("Radius of F.L.E.I.J.A.");
		fleijaRadius = propFleija.getInt();
		Property propMissile = config.get(CommonConfig.CATEGORY_NUKES, "3.07_missileRadius", 100);
		propMissile.setComment("Radius of the nuclear missile");
		missileRadius = propMissile.getInt();
		Property propMirv = config.get(CommonConfig.CATEGORY_NUKES, "3.08_mirvRadius", 70);
		propMirv.setComment("Radius of a MIRV");
		mirvRadius = propMirv.getInt();
		Property propFatman = config.get(CommonConfig.CATEGORY_NUKES, "3.09_fatmanRadius", 35);
		propFatman.setComment("Radius of the Fatman Launcher");
		fatmanRadius = propFatman.getInt();
		Property propNuka = config.get(CommonConfig.CATEGORY_NUKES, "3.10_nukaRadius", 25);
		propNuka.setComment("Radius of the nuka grenade");
		nukaRadius = propNuka.getInt();
		Property propASchrab = config.get(CommonConfig.CATEGORY_NUKES, "3.11_aSchrabRadius", 20);
		propASchrab.setComment("Radius of dropped anti schrabidium");
		aSchrabRadius = propASchrab.getInt();
		Property propSolinium = config.get(CommonConfig.CATEGORY_NUKES, "3.12_soliniumRadius", 150);
		propSolinium.setComment("Radius of the blue rinse");
		soliniumRadius = propSolinium.getInt();
		Property propN2 = config.get(CommonConfig.CATEGORY_NUKES, "3.13_n2Radius", 200);
		propN2.setComment("Radius of the N2 mine");
		n2Radius = propN2.getInt();

		Property propRS1 = config.get(CommonConfig.CATEGORY_NUKES, "3.14_riggedStarRadius", 50);
		propRS1.setComment("Radius of the Rigged Star Blaster Energy Cell");
		riggedStarRange = propRS1.getInt();
		Property propRS2 = config.get(CommonConfig.CATEGORY_NUKES, "3.15_riggedStarFuse", 1200);
		propRS2.setComment("Time in ticks before the Rigged Star Blaster Energy Cell explodes after being dropped - default 60s");
		riggedStarTicks = propRS2.getInt();

		Property propTNT = config.get(CommonConfig.CATEGORY_NUKES, "3.16_maxCustomTNTRadius", 150);
		propTNT.setComment("Maximum TNT radius of custom nukes - default 150m");
		maxCustomTNTRadius = propTNT.getInt();

		Property propNuke = config.get(CommonConfig.CATEGORY_NUKES, "3.17_maxCustomNukeRadius", 200);
		propNuke.setComment("Maximum Nuke radius of custom nukes - default 200m");
		maxCustomNukeRadius = propNuke.getInt();

		Property propHydro = config.get(CommonConfig.CATEGORY_NUKES, "3.18_maxCustomHydroRadius", 350);
		propHydro.setComment("Maximum Thermonuclear radius of custom nukes - default 350m");
		maxCustomHydroRadius = propHydro.getInt();

		Property propDirty = config.get(CommonConfig.CATEGORY_NUKES, "3.19_maxCustomDirtyRadius", 200);
		propDirty.setComment("Maximum fallout additional radius that can be added to custom nukes - default 200m");
		maxCustomDirtyRadius = propDirty.getInt();
		
		Property propBale = config.get(CommonConfig.CATEGORY_NUKES, "3.20_maxCustomBaleRadius", 350);
		propBale.setComment("Maximum balefire radius of custom nukes - default 350m");
		maxCustomBaleRadius = propBale.getInt();

		Property propSchrab = config.get(CommonConfig.CATEGORY_NUKES, "3.21_maxCustomSchrabRadius", 250);
		propSchrab.setComment("Maximum Antischrabidium radius of custom nukes - default 250m");
		maxCustomSchrabRadius = propSchrab.getInt();

		Property propSol = config.get(CommonConfig.CATEGORY_NUKES, "3.22_maxCustomSolRadius", 350);
		propSol.setComment("Maximum Solinium radius of custom nukes - default 350m");
		maxCustomSolRadius = propSol.getInt();
		
		Property propEuph = config.get(CommonConfig.CATEGORY_NUKES, "3.23_maxCustomEuphLvl", 20);
		propEuph.setComment("Maximum Euphemium Lvl of custom nukes (1Lvl = 100 Rays) - default 20");
		maxCustomEuphLvl = propEuph.getInt();
		
        
		Property propLimitExplosionLifespan = config.get(CommonConfig.CATEGORY_EXPLOSIONS, "6.00_limitExplosionLifespan", 0);
		propLimitExplosionLifespan.setComment("How long an explosion can be unloaded until it dies in seconds. Based of system time. 0 disables the effect");
		limitExplosionLifespan = propLimitExplosionLifespan.getInt();
		// explosion speed
		Property propBlastSpeed = config.get(CommonConfig.CATEGORY_EXPLOSIONS, "6.01_blastSpeed", 1024);
		propBlastSpeed.setComment("Base speed of MK3 system (old and schrabidium) detonations (Blocks / tick)");
		blastSpeed = propBlastSpeed.getInt();
		// fallout range
		Property propFalloutRange = config.get(CommonConfig.CATEGORY_EXPLOSIONS, "6.02_mk5BlastTime", 40);
		propFalloutRange.setComment("Maximum amount of milliseconds per tick allocated for mk5 chunk processing");
		mk5 = propFalloutRange.getInt();
		// fallout speed
		Property falloutRangeProp = config.get(CommonConfig.CATEGORY_EXPLOSIONS, "6.03_falloutRange", 100);
		falloutRangeProp.setComment("Radius of fallout area (base radius * value in percent)");
		falloutRange = falloutRangeProp.getInt();
		// fallout speed
		Property falloutChunkSpeed = config.get(CommonConfig.CATEGORY_EXPLOSIONS, "6.04_falloutChunkSpeed", 5);
		falloutChunkSpeed.setComment("Process a Chunk every nth tick by the fallout rain");
		fChunkSpeed = falloutChunkSpeed.getInt();
		// new explosion speed
		Property falloutMSProp = config.get(CommonConfig.CATEGORY_EXPLOSIONS, "6.05_falloutTime", 30);
		falloutMSProp.setComment("Maximum amount of milliseconds per tick allocated for fallout chunk processing");
		falloutDelay = falloutMSProp.getInt();
		Property spawnFireP = config.get(CommonConfig.CATEGORY_EXPLOSIONS, "6.06_falloutFireSpawn", false);
		spawnFireP.setComment("Whether to spawn fire after the nuke. Is off to increase TPS");
		spawnFire = spawnFireP.getBoolean();
		//Whether fallout and nuclear radiation is enabled at all
		Property disableNuclearP = config.get(CommonConfig.CATEGORY_EXPLOSIONS, "6.07_disableNuclear", false);
		disableNuclearP.setComment("Disable the nuclear part of nukes");
		disableNuclear = disableNuclearP.getBoolean();

		Property enableNukeCloudsP = config.get(CommonConfig.CATEGORY_EXPLOSIONS, "6.08_enableMushroomClouds", true);
		enableNukeCloudsP.setComment("WARNING: AN OLD CONFIG OPTION. Allows for nuclear explosion to even happen.");
		enableNukeClouds = enableNukeCloudsP.getBoolean(true);

		Property enableNukeNBTSavingP = config.get(CommonConfig.CATEGORY_EXPLOSIONS, "6.09_enableNukeNBTSaving", true);
		enableNukeNBTSavingP.setComment("If true then nukes will save the blocks they want to destroy so they can resume work rather then restart after a crash/reload. For big nukes this can take a while tho.");
		enableNukeNBTSaving = enableNukeNBTSavingP.getBoolean();

		Property chunkloadingP = config.get(CommonConfig.CATEGORY_EXPLOSIONS, "6.10_enableChunkLoading", true);
		chunkloadingP.setComment("Allows mk5 explosion to generate new chunks.");
		chunkloading = chunkloadingP.getBoolean(true);

		Property explosionAlgorithmP = config.get(CommonConfig.CATEGORY_EXPLOSIONS, "6.11_explosionAlgorithm", 2);
		explosionAlgorithmP.setComment("Configures the algorithm of mk5 explosion. \n0 = Legacy, 1 = Threaded DDA, 2 = Threaded DDA with damage accumulation.");
		explosionAlgorithm = explosionAlgorithmP.getInt();

		Property maxThreadsP = config.get(CommonConfig.CATEGORY_EXPLOSIONS, "6.11.1_explosionMaxThreads", -1);
		maxThreadsP.setComment("Configures the maximum thread count for the threaded DDA explosion algorithm.\n -N = CPU count - N, 0 = CPU count, N = N");
		maxThreads = maxThreadsP.getInt();

        Property safeCommitP = config.get(CommonConfig.CATEGORY_EXPLOSIONS, "6.11.2_safeCommit", false);
        safeCommitP.setComment("Prefer safety over performance(~30% slower). Affects algorithm 1, 2, and fallout rain effect.");
        safeCommit = safeCommitP.getBoolean();
	}
}
