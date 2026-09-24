package com.hbm.config;

import com.hbm.potion.HbmPotion;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;

public class VersatileConfig {

	public static int getSchrabOreChance() {

		if(GeneralConfig.enableLBSM)
			return 20;

		return 250;
	}
	
	public static void applyPotionSickness(EntityLivingBase entity, int duration) {
		
		if(PotionConfig.potionSickness == 0)
			return;
		
		if(PotionConfig.potionSickness == 2)
			duration *= 12;
		
		entity.addPotionEffect(new PotionEffect(HbmPotion.potionsickness, duration * 20));
	}

	public static boolean hasPotionSickness(EntityLivingBase entity) {
		return entity.isPotionActive(HbmPotion.potionsickness);
	}

	public static boolean rtgDecay() {
		return GeneralConfig.enable528 || MachineConfig.doRTGsDecay;
	}

	static int minute = 60 * 20;
	static int hour = 60 * minute;
	
	public static int getLongDecayChance() {
		return GeneralConfig.enable528 ? 15 * hour : (GeneralConfig.enableLBSM && GeneralConfig.enableLBSMShorterDecay) ? 15 * minute : 3 * hour;
	}

	public static int getShortDecayChance() {
		return GeneralConfig.enable528 ? 15 * hour : (GeneralConfig.enableLBSM && GeneralConfig.enableLBSMShorterDecay) ? 15 * minute : 3 * hour;
	}
}
