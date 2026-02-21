package com.hbm.handler;

import com.hbm.handler.guncfg.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map.Entry;

public class BulletConfigSyncingUtil {

private static HashMap<Integer, BulletConfiguration> configSet = new HashMap<Integer, BulletConfiguration>();
	
	static int i = 0;
	
	/// duplicate ids will cause wrong configs to be loaded ///
	public static int TEST_CONFIG = i++;

	public static int TURBINE = i++;

	public static int IRON_REVOLVER = i++;
	public static int STEEL_REVOLVER = i++;
	public static int LEAD_REVOLVER = i++;
	public static int GOLD_REVOLVER = i++;
	public static int CURSED_REVOLVER = i++;
	public static int SCHRABIDIUM_REVOLVER = i++;
	public static int NIGHT_REVOLVER = i++;
	public static int NIGHT2_REVOLVER = i++;
	public static int SATURNITE_REVOLVER = i++;
	public static int DESH_REVOLVER = i++;

	public static int G20_NORMAL = i++;
	public static int G20_SLUG = i++;
	public static int G20_FLECHETTE = i++;
	public static int G20_FIRE = i++;
	public static int G20_SHRAPNEL = i++;
	public static int G20_EXPLOSIVE = i++;
	public static int G20_CAUSTIC = i++;
	public static int G20_SHOCK = i++;
	public static int G20_WITHER = i++;
	public static int G20_SLEEK = i++;
	
	public static int ROCKET_NORMAL_LASER = i++;
	public static int ROCKET_HE_LASER = i++;
	public static int ROCKET_INCENDIARY_LASER = i++;
	public static int ROCKET_SHRAPNEL_LASER = i++;
	public static int ROCKET_EMP_LASER = i++;
	public static int ROCKET_GLARE_LASER = i++;
	public static int ROCKET_SLEEK_LASER = i++;
	public static int ROCKET_NUKE_LASER = i++;
	public static int ROCKET_CHAINSAW_LASER = i++;
	public static int ROCKET_TOXIC_LASER = i++;
	public static int ROCKET_PHOSPHORUS_LASER = i++;
	
	public static int SHELL_NORMAL = i++;
	public static int SHELL_EXPLOSIVE = i++;
	public static int SHELL_AP = i++;
	public static int SHELL_DU = i++;
	public static int SHELL_W9 = i++;
	public static int DGK_NORMAL = i++;
	public static int FLA_NORMAL = i++;

	public static int ROCKET_NORMAL = i++;
	public static int ROCKET_HE = i++;
	public static int ROCKET_INCENDIARY = i++;
	public static int ROCKET_SHRAPNEL = i++;
	public static int ROCKET_EMP = i++;
	public static int ROCKET_GLARE = i++;
	public static int ROCKET_SLEEK = i++;
	public static int ROCKET_NUKE = i++;
	public static int ROCKET_CHAINSAW = i++;
	public static int ROCKET_TOXIC = i++;
	public static int ROCKET_PHOSPHORUS = i++;
	public static int ROCKET_CANISTER = i++;

	public static int GRENADE_NORMAL = i++;
	public static int GRENADE_HE = i++;
	public static int GRENADE_INCENDIARY = i++;
	public static int GRENADE_CHEMICAL = i++;
	public static int GRENADE_SLEEK = i++;
	public static int GRENADE_CONCUSSION = i++;
	public static int GRENADE_FINNED = i++;
	public static int GRENADE_NUCLEAR = i++;
	public static int GRENADE_PHOSPHORUS = i++;
	public static int GRENADE_TRACER = i++;
	public static int GRENADE_KAMPF = i++;

	public static int G12_NORMAL = i++;
	public static int G12_INCENDIARY = i++;
	public static int G12_SHRAPNEL = i++;
	public static int G12_DU = i++;
	public static int G12_AM = i++;
	public static int G12_SLEEK = i++;

	public static int LR22_NORMAL = i++;
	public static int LR22_AP = i++;
	public static int LR22_NORMAL_FIRE = i++;
	public static int LR22_AP_FIRE = i++;

	public static int M44_NORMAL = i++;
	public static int M44_AP = i++;
	public static int M44_DU = i++;
	public static int M44_STAR = i++;
	public static int M44_PIP = i++;
	public static int M44_BJ = i++;
	public static int M44_SILVER = i++;
	public static int M44_ROCKET = i++;
	public static int M44_PHOSPHORUS = i++;

	public static int P9_NORMAL = i++;
	public static int P9_AP = i++;
	public static int P9_DU = i++;
	public static int P9_ROCKET = i++;

	public static int BMG50_NORMAL = i++;
	public static int BMG50_INCENDIARY = i++;
	public static int BMG50_EXPLOSIVE = i++;
	public static int BMG50_AP = i++;
	public static int BMG50_DU = i++;
	public static int BMG50_STAR = i++;
	public static int BMG50_PHOSPHORUS = i++;
	public static int BMG50_SLEEK = i++;
	public static int BMG50_FLECHETTE_NORMAL = i++;
	public static int BMG50_FLECHETTE_AM = i++;
	public static int BMG50_FLECHETTE_PO = i++;

	public static int R5_NORMAL = i++;
	public static int R5_EXPLOSIVE = i++;
	public static int R5_DU = i++;
	public static int R5_STAR = i++;
	public static int R5_NORMAL_BOLT = i++;
	public static int R5_EXPLOSIVE_BOLT = i++;
	public static int R5_DU_BOLT = i++;
	public static int R5_STAR_BOLT = i++;

	public static int AE50_NORMAL = i++;
	public static int AE50_AP = i++;
	public static int AE50_DU = i++;
	public static int AE50_STAR = i++;

	public static int G4_NORMAL = i++;
	public static int G4_SLUG = i++;
	public static int G4_FLECHETTE = i++;
	public static int G4_FLECHETTE_PHOSPHORUS = i++;
	public static int G4_EXPLOSIVE = i++;
	public static int G4_SEMTEX = i++;
	public static int G4_BALEFIRE = i++;
	public static int G4_KAMPF = i++;
	public static int G4_CANISTER = i++;
	public static int G4_SLEEK = i++;
	public static int G4_CLAW = i++;
	public static int G4_VAMPIRE = i++;
	public static int G4_VOID = i++;

	public static int SPECIAL_OSIPR = i++;
	public static int SPECIAL_OSIPR_CHARGED = i++;
	public static int SPECIAL_GAUSS = i++;
	public static int SPECIAL_GAUSS_CHARGED = i++;
	public static int SPECIAL_EMP = i++;
	
	public static int FLAMER_NORMAL = i++;
	public static int FLAMER_NAPALM = i++;
	public static int FLAMER_WP = i++;
	public static int FLAMER_VAPORIZER = i++;
	public static int FLAMER_GAS = i++;
	
	public static int R556_NORMAL = i++;
	public static int R556_GOLD = i++;
	public static int R556_PHOSPHORUS = i++;
	public static int R556_AP = i++;
	public static int R556_DU = i++;
	public static int R556_STAR = i++;
	public static int R556_SLEEK = i++;
	public static int R556_TRACER = i++;
	public static int R556_FLECHETTE = i++;
	public static int R556_FLECHETTE_INCENDIARY = i++;
	public static int R556_FLECHETTE_PHOSPHORUS = i++;
	public static int R556_FLECHETTE_DU = i++;
	public static int R556_FLECHETTE_SLEEK = i++;
	public static int R556_K = i++;
	
	public static int B75_NORMAL = i++;
	public static int B75_INCENDIARY = i++;
	public static int B75_HE = i++;

	public static int G20_NORMAL_FIRE = i++;
	public static int G20_SHRAPNEL_FIRE = i++;
	public static int G20_SLUG_FIRE = i++;
	public static int G20_FLECHETTE_FIRE = i++;
	public static int G20_EXPLOSIVE_FIRE = i++;
	public static int G20_CAUSTIC_FIRE = i++;
	public static int G20_SHOCK_FIRE = i++;
	public static int G20_WITHER_FIRE = i++;

	public static int NUKE_NORMAL = i++;
	public static int NUKE_LOW = i++;
	public static int NUKE_HIGH = i++;
	public static int NUKE_TOTS = i++;
	public static int NUKE_SAFE = i++;
	public static int NUKE_PUMPKIN = i++;
	public static int NUKE_PROTO_NORMAL = i++;
	public static int NUKE_PROTO_LOW = i++;
	public static int NUKE_PROTO_HIGH = i++;
	public static int NUKE_PROTO_TOTS = i++;
	public static int NUKE_PROTO_SAFE = i++;
	public static int NUKE_PROTO_PUMPKIN = i++;
	public static int NUKE_MIRV_NORMAL = i++;
	public static int NUKE_MIRV_LOW = i++;
	public static int NUKE_MIRV_HIGH = i++;
	public static int NUKE_MIRV_SAFE = i++;
	public static int NUKE_MIRV_SPECIAL = i++;

	public static int NUKE_AMAT = i++;
	
	public static int ZOMG_BOLT = i++;

	public static int CHL_LR22 = i++;
	public static int CHL_LR22_FIRE = i++;
	public static int CHL_M44 = i++;
	public static int CHL_P9 = i++;
	public static int CHL_BMG50 = i++;
	public static int CHL_R5 = i++;
	public static int CHL_R5_BOLT = i++;
	public static int CHL_AE50 = i++;
	public static int CHL_R556 = i++;
	public static int CHL_R556_FLECHETTE = i++;
	
	public static int MASKMAN_BULLET = i++;
	public static int MASKMAN_ORB = i++;
	public static int MASKMAN_BOLT = i++;
	public static int MASKMAN_ROCKET = i++;
	public static int MASKMAN_TRACER = i++;
	public static int MASKMAN_METEOR = i++;
	
	public static int WORM_BOLT = i++;
	public static int WORM_LASER = i++;
	
	public static int NEEDLE_GPS = i++;
	
	public static int UFO_ROCKET = i++;
	
	public static void loadConfigsForSync() {
		
		configSet.put(TEST_CONFIG, BulletConfigFactory.getTestConfig());

		configSet.put(SHELL_NORMAL, GunCannonFactory.getShellConfig());
		configSet.put(SHELL_EXPLOSIVE, GunCannonFactory.getShellExplosiveConfig());
		configSet.put(SHELL_AP, GunCannonFactory.getShellAPConfig());
		configSet.put(SHELL_DU, GunCannonFactory.getShellDUConfig());
		configSet.put(SHELL_W9, GunCannonFactory.getShellW9Config());
		configSet.put(DGK_NORMAL, GunDGKFactory.getDGKConfig());
		//configSet.put(FLA_NORMAL, GunEnergyFactory.getTurretConfig());

		configSet.put(ZOMG_BOLT, GunEnergyFactory.getZOMGBoltConfig());

		configSet.put(MASKMAN_BULLET, GunNPCFactory.getMaskmanBullet());
		configSet.put(MASKMAN_ORB, GunNPCFactory.getMaskmanOrb());
		configSet.put(MASKMAN_BOLT, GunNPCFactory.getMaskmanBolt());
		configSet.put(MASKMAN_ROCKET, GunNPCFactory.getMaskmanRocket());
		configSet.put(MASKMAN_TRACER, GunNPCFactory.getMaskmanTracer());
		configSet.put(MASKMAN_METEOR, GunNPCFactory.getMaskmanMeteor());
		
		configSet.put(WORM_BOLT, GunNPCFactory.getWormBolt());
		configSet.put(WORM_LASER, GunNPCFactory.getWormHeadBolt());
		

		configSet.put(UFO_ROCKET, GunNPCFactory.getRocketUFOConfig());
	}

	@Nullable
	public static BulletConfiguration pullConfig(int key) {
		
		return configSet.get(key);
	}
	
	public static int getKey(BulletConfiguration config) {
		
		for(Entry<Integer, BulletConfiguration> e : configSet.entrySet()) {
			
			if(e.getValue() == config)
				return e.getKey();
		}
		
		return -1;
	}
}
