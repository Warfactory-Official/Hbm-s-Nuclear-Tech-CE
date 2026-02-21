package com.hbm.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class WeaponConfig {

	public static int radarRange = 1000;
	public static int radarBuffer = 30;
	public static int radarAltitude = 55;
	public static int ciwsHitrate = 50;

	public static boolean dropMissileParts = true;
	
	public static boolean dropCell = true;
	public static boolean dropSing = true;
	public static boolean dropStar = true;
	public static boolean dropCrys = true;
	public static boolean dropDead = true;
	
	public static void loadFromConfig(Configuration config) {
		Property propRadarRange = config.get(CommonConfig.CATEGORY_MISSILE, "7.00_radarRange", 1000);
		propRadarRange.setComment("Range of the radar, 50 will result in 100x100 block area covered");
		radarRange = propRadarRange.getInt();
		Property propRadarBuffer = config.get(CommonConfig.CATEGORY_MISSILE, "7.01_radarBuffer", 30);
		propRadarBuffer.setComment("How high entities have to be above the radar to be detected");
		radarBuffer = propRadarBuffer.getInt();
		Property propRadarAltitude = config.get(CommonConfig.CATEGORY_MISSILE, "7.02_radarAltitude", 55);
		propRadarAltitude.setComment("Y height required for the radar to work");
		radarAltitude = propRadarAltitude.getInt();
		Property propCiwsHitrate = config.get(CommonConfig.CATEGORY_MISSILE, "7.03_ciwsAccuracy", 50);
		propCiwsHitrate.setComment("Additional modifier for CIWS accuracy");
		ciwsHitrate = propCiwsHitrate.getInt();

		dropMissileParts = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_MISSILE, "7.03_dropMissileParts", "Whether shot-down missiles drop items", true);

        dropCell = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_DROPS, "10.00_dropCell", "Whether antimatter cells should explode when dropped", true);
        dropSing = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_DROPS, "10.01_dropBHole", "Whether singularities and blaack holes should spawn when dropped", true);
        dropStar = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_DROPS, "10.02_dropStar", "Whether rigged star blaster cells should explode when dropped", true);
        dropCrys = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_DROPS, "10.04_dropCrys", "Whether xen crystals should move blocks when dropped", true);
        dropDead = CommonConfig.createConfigBool(config, CommonConfig.CATEGORY_DROPS, "10.05_dropDead", "Whether dead man's explosives should explode when dropped", true);
	}

}
