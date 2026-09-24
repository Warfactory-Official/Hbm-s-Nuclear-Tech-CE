package com.hbm.handler.guncfg;

import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.handler.GunConfiguration;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.render.anim.HbmAnimations.AnimType;
import com.hbm.render.misc.RenderScreenOverlay.Crosshair;

import java.util.ArrayList;

public class Gun12GaugeFactory {
	
	public static GunConfiguration getShottyConfig() {
		
		GunConfiguration config = new GunConfiguration();
		
		config.rateOfFire = 26;
		config.roundsPerCycle = 2;
		config.gunMode = GunConfiguration.MODE_NORMAL;
		config.firingMode = GunConfiguration.FIRE_MANUAL;
		config.reloadDuration = 10;
		config.firingDuration = 0;
		config.ammoCap = 0;
		config.durability = 3000;
		config.reloadType = GunConfiguration.RELOAD_NONE;
		config.allowsInfinity = true;
		config.hasSights = true;
		config.crosshair = Crosshair.L_CIRCLE;
		config.reloadSound = GunConfiguration.RSOUND_REVOLVER;
		config.firingSound = HBMSoundHandler.shottyShoot;
		
		config.animations.put(AnimType.ALT_CYCLE, new BusAnimation()
				.addBus("MEATHOOK_RECOIL", new BusAnimationSequence()
						.addKeyframe(new BusAnimationKeyframe(0, -0.5, 1.5, 25))
						.addKeyframe(new BusAnimationKeyframe(0, 0, 0, 250))));
		
		config.animations.put(AnimType.CYCLE, new BusAnimation()
				.addBus("SHOTTY_RECOIL", new BusAnimationSequence()
						.addKeyframe(new BusAnimationKeyframe(0.5, 0, 0, 50))
						.addKeyframe(new BusAnimationKeyframe(0, 0, 0, 50))
						)
				.addBus("SHOTTY_BREAK", new BusAnimationSequence()
						.addKeyframe(new BusAnimationKeyframe(0, 0, 0, 100))	//do nothing for 100ms
						.addKeyframe(new BusAnimationKeyframe(0, 0, 60, 200))	//open
						.addKeyframe(new BusAnimationKeyframe(0, 0, 60, 500))	//do nothing for 500ms
						.addKeyframe(new BusAnimationKeyframe(0, 0, 0, 200))	//close
						)
				.addBus("SHOTTY_EJECT", new BusAnimationSequence()
						.addKeyframe(new BusAnimationKeyframe(0, 0, 0, 300))	//do nothing for 300ms
						.addKeyframe(new BusAnimationKeyframe(1, 0, 0, 700))	//fling!
						)
				.addBus("SHOTTY_INSERT", new BusAnimationSequence()
						.addKeyframe(new BusAnimationKeyframe(0, 0, 0, 300))	//do nothing for 300ms
						.addKeyframe(new BusAnimationKeyframe(1, 0, 1, 0))		//reposition
						.addKeyframe(new BusAnimationKeyframe(1, 0, 0, 350))	//come in from the side
						.addKeyframe(new BusAnimationKeyframe(0, 0, 0, 150))	//push
						)
				);
		
		config.name = "???";
		config.manufacturer = "???";
		config.comment.add("but bOB WhY iS TExtURE no woRk");
		config.comment.add("hoW do I cRAFT PleasE HElp");
		
		config.config = new ArrayList<Integer>();
		config.config.add(BulletConfigSyncingUtil.G12_NORMAL);
		config.config.add(BulletConfigSyncingUtil.G12_INCENDIARY);
		config.config.add(BulletConfigSyncingUtil.G12_SHRAPNEL);
		config.config.add(BulletConfigSyncingUtil.G12_DU);
		config.config.add(BulletConfigSyncingUtil.G12_AM);
		config.config.add(BulletConfigSyncingUtil.G12_SLEEK);
		
		return config;
	}
}
