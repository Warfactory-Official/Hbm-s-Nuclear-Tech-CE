package com.hbm.handler.guncfg;

import com.hbm.entity.projectile.EntityBulletBase;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.handler.BulletConfiguration;
import com.hbm.handler.GunConfiguration;
import com.hbm.interfaces.IBulletImpactBehavior;
import com.hbm.inventory.RecipesCommon;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.potion.HbmPotion;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.render.anim.HbmAnimations.AnimType;
import com.hbm.render.misc.RenderScreenOverlay.Crosshair;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;

import java.util.ArrayList;

public class GunEnergyFactory {

	public static GunConfiguration getVortexConfig() {

		GunConfiguration config = new GunConfiguration();

		config.rateOfFire = 30;
		config.roundsPerCycle = 1;
		config.gunMode = GunConfiguration.MODE_NORMAL;
		config.firingMode = GunConfiguration.FIRE_AUTO;
		config.hasSights = false;
		config.reloadDuration = 20;
		config.firingDuration = 0;
		config.ammoCap = 8;
		config.reloadType = GunConfiguration.RELOAD_FULL;
		config.allowsInfinity = true;
		config.crosshair = Crosshair.NONE;
		config.durability = 12000;
		config.reloadSound = GunConfiguration.RSOUND_MAG; //MetalloloM: i'll add new sounds for it later
		config.firingSound = HBMSoundHandler.hksShoot;
		config.reloadSoundEnd = false;

		config.name = "Visual Operation Ranged Tactical Electromagnetic Xenoblaster";
		config.manufacturer = "Xon Corporation";

		config.comment.add("OBEY XON");
		
		config.animations.put(AnimType.CYCLE, new BusAnimation()
				.addBus("VORTEX_RECOIL", new BusAnimationSequence()
						.addKeyframe(new BusAnimationKeyframe(0, 1, -5, 25))
						.addKeyframe(new BusAnimationKeyframe(0, 0, 0, 400))
						));

		config.config = new ArrayList<Integer>();
		config.config.add(BulletConfigSyncingUtil.R556_STAR); //MetalloloM: wanted to make it use either an AP .50BMG or starmetal one

		return config;

	}

	public static GunConfiguration getEgonConfig() {
		GunConfiguration config = new GunConfiguration();

		config.rateOfFire = 2;
		config.roundsPerCycle = 1;
		config.gunMode = GunConfiguration.MODE_NORMAL;
		config.firingMode = GunConfiguration.FIRE_AUTO;
		config.hasSights = false;
		config.reloadDuration = 20;
		config.firingDuration = 0;
		config.ammoCap = 40;
		config.reloadType = GunConfiguration.RELOAD_NONE;
		config.allowsInfinity = true;
		config.crosshair = Crosshair.NONE;
		config.durability = 10000;
		config.reloadSound = GunConfiguration.RSOUND_MAG;
		config.firingSound = HBMSoundHandler.osiprShoot;
		config.reloadSoundEnd = false;

		config.name = "Gluon Gun";
		config.manufacturer = "Black Mesa Research Facility";

		config.comment.add("Damage starts at 5/s and gets doubled every 2s while on target");
		config.comment.add("Working to make a better tomorrow for all mankind.");

		config.config = new ArrayList<Integer>();
		config.config.add(BulletConfigSyncingUtil.SPECIAL_GAUSS);

		return config;
	}

	public static BulletConfiguration getZOMGBoltConfig() {

		BulletConfiguration bullet = new BulletConfiguration();

		bullet.ammo = new RecipesCommon.ComparableStack(ModItems.nugget_euphemium);
		bullet.ammoCount = 1000;
		bullet.wear = 1;
		bullet.velocity = 1F;
		bullet.spread = 0.125F;
		bullet.maxAge = 100;
		bullet.gravity = 0F;
		bullet.bulletsMin = 5;
		bullet.bulletsMax = 5;
		bullet.dmgMin = 10000;
		bullet.dmgMax = 25000;

		bullet.style = BulletConfiguration.STYLE_BOLT;
		bullet.trail = bullet.BOLT_ZOMG;

		bullet.effects = new ArrayList<>();
		bullet.effects.add(new PotionEffect(HbmPotion.bang, 10 * 20, 0));

		bullet.bImpact = new IBulletImpactBehavior() {

			@Override
			public void behaveBlockHit(EntityBulletBase bullet, int x, int y, int z) {

				if(!bullet.world.isRemote) {
					ExplosionChaos.explodeZOMG(bullet.world, (int) bullet.posX, (int) bullet.posY, (int) bullet.posZ, 5);
					bullet.world.playSound(null, bullet.posX, bullet.posY, bullet.posZ, HBMSoundHandler.bombDet, SoundCategory.HOSTILE, 5.0F, 1.0F);
					ExplosionLarge.spawnParticles(bullet.world, bullet.posX, bullet.posY, bullet.posZ, 5);
				}
			}
		};

		return bullet;
	}
	/*
	public static BulletConfiguration getTurretConfig() {
		BulletConfiguration bullet = getFlameConfig();
		bullet.spread *= 2F;
		bullet.gravity = 0.0025D;
		return bullet;
	}
*/
}