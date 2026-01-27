package com.hbm.handler;

import com.hbm.entity.projectile.EntityBulletBaseNT;
import com.hbm.handler.guncfg.BulletConfigFactory;
import com.hbm.interfaces.*;
import com.hbm.inventory.RecipesCommon;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.MainRegistry;
import com.hbm.particle.SpentCasing;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class BulletConfiguration {

	//what item this specific configuration consumes
	public RecipesCommon.ComparableStack ammo;
	//how many ammo units one item restores
	public int ammoCount = 1;
	//how fast the bullet is (in sanics per second, or sps)
	public float velocity;
	//spread of bullets in gaussian range
	public float spread;
	//weapon durability reduced (centered around 10)
	public int wear;
	//greatest amount of pellets created each shot
	public int bulletsMin;
	//least amount of pellets created each shot
	public int bulletsMax;

	//damage bounds
	public float dmgMin;
	public float dmgMax;
	public float headshotMult = 1.0F;

	//acceleration torwards neg Y
	public float gravity;
	//max age in ticks before despawning
	public int maxAge;

	//whether the projectile should be able to bounce off of blocks
	public boolean doesRicochet;
	//the maximum angle at which the projectile should bounce
	public double ricochetAngle;
	//lower bound ricochet chance (below R angle)
	public int LBRC;
	//higher bound ricochet chance (above R angle)
	public int HBRC;
	//how much of the initial velocity is kept after bouncing
	public double bounceMod;
	//how many ticks until the projectile can hurt the shooter
	public int selfDamageDelay = 5;

	//whether or not the bullet should penetrate mobs
	public boolean doesPenetrate;
	//disables collisions with blocks entirely
	public boolean isSpectral;
	//whether or not the bullet should break glass
	public boolean doesBreakGlass;
	//bullets still call the impact function when hitting blocks but do not get destroyed
	public boolean liveAfterImpact;

	//creates a "muzzle flash" and a ton of smoke with every projectile spawned
	public boolean blackPowder = false;

	// bullet effects
	public List<PotionEffect> effects;
	public int incendiary;
	public int emp;
	public boolean blockDamage = true;
	public float explosive;
	public double jolt;
	public int rainbow;
	public int nuke;
	public int shrapnel;
	public int chlorine;
	public int leadChance;
	public int caustic;
	public boolean boxcar;
	public boolean boat;
	public boolean destroysWood;
	public boolean destroysBlocks;
	public boolean instakill;

	public IBulletHurtBehavior bHurt;
	public IBulletHitBehavior bHit;
	public IBulletRicochetBehavior bRicochet;
	public IBulletImpactBehavior bImpact;
	public IBulletUpdateBehavior bUpdate;

	public EntityBulletBaseNT.IBulletHurtBehaviorNT bntHurt;
	public EntityBulletBaseNT.IBulletHitBehaviorNT bntHit;
	public EntityBulletBaseNT.IBulletRicochetBehaviorNT bntRicochet;
	public EntityBulletBaseNT.IBulletImpactBehaviorNT bntImpact;
	public EntityBulletBaseNT.IBulletUpdateBehaviorNT bntUpdate;

	// appearance
	public int style;
	// additional appearance data, i.e. particle effects
	public int trail;
	// ricochet sound type
	public int plink;
	// vanilla particle FX
	public String vPFX = "";
	public SpentCasing spentCasing;

	//energy projectiles
	//power consumed per shot
	public int dischargePerShot;
	//unlocalised firing mode name
	public String modeName;
	//firing mode text colour
	public TextFormatting chatColour = TextFormatting.WHITE;
	//firing rate
	public int firingRate;

	public String damageType = ModDamageSource.s_bullet;
	public boolean dmgProj = true;
	public boolean dmgFire = false;
	public boolean dmgExplosion = false;
	public boolean dmgBypass = false;

	public static final int STYLE_NONE = -1;
	public static final int STYLE_NORMAL = 0;
	public static final int STYLE_FLECHETTE = 1;
	public static final int STYLE_PELLET = 2;
	public static final int STYLE_BOLT = 3;
	public static final int STYLE_FOLLY = 4;
	public static final int STYLE_ROCKET = 5;
	public static final int STYLE_STINGER = 6;
	public static final int STYLE_NUKE = 7;
	public static final int STYLE_MIRV = 8;
	public static final int STYLE_GRENADE = 9;
	public static final int STYLE_BF = 10;
	public static final int STYLE_ORB = 11;
	public static final int STYLE_METEOR = 12;
	public static final int STYLE_TRACER = 13;
	public static final int STYLE_APDS = 14;
	public static final int STYLE_BLADE = 15;
	public static final int STYLE_BARREL = 16;
	public static final int STYLE_TAU = 17;
	public static final int STYLE_LEADBURSTER = 18;

	public static final int PLINK_NONE = 0;
	public static final int PLINK_BULLET = 1;
	public static final int PLINK_GRENADE = 2;
	public static final int PLINK_ENERGY = 3;
	public static final int PLINK_SING = 4;

	public static final int BOLT_LACUNAE = 0;
	public static final int BOLT_NIGHTMARE = 1;
	public static final int BOLT_LASER = 2;
	public static final int BOLT_ZOMG = 3;
	public static final int BOLT_WORM = 4;
	public static final int BOLT_GLASS_CYAN = 5;
	public static final int BOLT_GLASS_BLUE = 6;

	public BulletConfiguration setToBolt(int trail) {

		this.style = STYLE_BOLT;
		this.trail = trail;
		return this;
	}

	public BulletConfiguration setToFire(int duration) {
		this.incendiary = duration;
		return this;
	}
	
	public BulletConfiguration setToGuided() {
		this.bUpdate = BulletConfigFactory.getLaserSteering();
		this.doesRicochet = false;
		return this;
	}
	
	public BulletConfiguration setToHoming(ItemStack ammo) {

		this.ammo = new RecipesCommon.ComparableStack(ammo);
		this.bUpdate = BulletConfigFactory.getHomingBehavior(200, 45);
		this.dmgMin *= 1.5F;
		this.dmgMax *= 1.5F;
		this.wear *= 0.5;
		this.doesRicochet = false;
		this.doesPenetrate = false;
		this.vPFX = "greendust";
		return this;
	}
	
	public BulletConfiguration accuracyMod(float mod) {
		
		this.spread *= mod;
		return this;
	}

	public DamageSource getDamage(EntityBulletBaseNT bullet, EntityLivingBase shooter) {

		DamageSource dmg;

		String unloc = damageType;

		if(unloc.equals(ModDamageSource.s_zomg_prefix))
			unloc += (bullet.world.rand.nextInt(5) + 1); //pain

		if(shooter != null)
			dmg = new EntityDamageSourceIndirect(unloc, bullet, shooter);
		else
			dmg = new DamageSource(unloc);

		if(this.dmgProj) dmg.setProjectile();
		if(this.dmgFire) dmg.setFireDamage();
		if(this.dmgExplosion) dmg.setExplosion();
		if(this.dmgBypass) dmg.setDamageBypassesArmor();

		return dmg;
	}

	@Override
	public BulletConfiguration clone() {
		try {
			return (BulletConfiguration) super.clone();
		} catch(CloneNotSupportedException e) {
			MainRegistry.logger.catching(e);
			return new BulletConfiguration();
		}
	}

	public static interface IBulletHurtBehaviorNT { public void behaveEntityHurt(EntityBulletBaseNT bullet, Entity hit); }
	public static interface IBulletHitBehaviorNT { public void behaveEntityHit(EntityBulletBaseNT bullet, Entity hit); }
	public static interface IBulletRicochetBehaviorNT { public void behaveBlockRicochet(EntityBulletBaseNT bullet, int x, int y, int z); }
	public static interface IBulletImpactBehaviorNT { public void behaveBlockHit(EntityBulletBaseNT bullet, int x, int y, int z, int sideHit); }
	public static interface IBulletUpdateBehaviorNT { public void behaveUpdate(EntityBulletBaseNT bullet); }
}
