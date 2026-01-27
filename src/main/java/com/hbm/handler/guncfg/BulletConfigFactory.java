package com.hbm.handler.guncfg;

import com.hbm.config.CompatibilityConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.entity.projectile.EntityBulletBase;
import com.hbm.handler.ArmorUtil;
import com.hbm.handler.BulletConfiguration;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.IBulletImpactBehavior;
import com.hbm.interfaces.IBulletUpdateBehavior;
import com.hbm.inventory.RecipesCommon;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.potion.HbmPotion;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.BobMathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import java.util.List;

public class BulletConfigFactory {

	/// configs should never be loaded manually due to syncing issues: use the
		/// syncing util and pass the UID in the DW of the bullet to make the client
		/// load the config correctly ////

	public static BulletConfiguration getTestConfig() {

		BulletConfiguration bullet = new BulletConfiguration();

		bullet.ammo = new RecipesCommon.ComparableStack(ModItems.ammo_standard);
		bullet.velocity = 5.0F;
		bullet.spread = 0.05F;
		bullet.wear = 10;
		bullet.dmgMin = 15;
		bullet.dmgMax = 17;
		bullet.bulletsMin = 1;
		bullet.bulletsMax = 1;
		bullet.gravity = 0F;
		bullet.maxAge = 100;
		bullet.doesRicochet = true;
		bullet.ricochetAngle = 10;
		bullet.HBRC = 2;
		bullet.LBRC = 90;
		bullet.bounceMod = 0.8;
		bullet.doesPenetrate = false;
		bullet.doesBreakGlass = true;
		bullet.style = 0;
		bullet.plink = 1;

		return bullet;

	}

	/// STANDARD CONFIGS ///
	// do not include damage or ammo
	public static BulletConfiguration standardBulletConfig() {

		BulletConfiguration bullet = new BulletConfiguration();

		bullet.velocity = 5.0F;
		bullet.spread = 0.005F;
		bullet.wear = 10;
		bullet.bulletsMin = 1;
		bullet.bulletsMax = 1;
		bullet.gravity = 0F;
		bullet.maxAge = 100;
		bullet.doesRicochet = true;
		bullet.ricochetAngle = 5;
		bullet.HBRC = 2;
		bullet.LBRC = 95;
		bullet.bounceMod = 0.8;
		bullet.doesPenetrate = true;
		bullet.doesBreakGlass = true;
		bullet.destroysBlocks = false;
		bullet.style = BulletConfiguration.STYLE_NORMAL;
		bullet.plink = BulletConfiguration.PLINK_BULLET;
		bullet.leadChance = 5;

		return bullet;
	}

	public static BulletConfiguration standardShellConfig() {
		
		BulletConfiguration bullet = new BulletConfiguration();
		
		bullet.velocity = 3.0F;
		bullet.spread = 0.005F;
		bullet.wear = 10;
		bullet.bulletsMin = 1;
		bullet.bulletsMax = 1;
		bullet.gravity = 0.005F;
		bullet.maxAge = 300;
		bullet.doesRicochet = true;
		bullet.ricochetAngle = 10;
		bullet.HBRC = 2;
		bullet.LBRC = 100;
		bullet.bounceMod = 0.8;
		bullet.doesPenetrate = false;
		bullet.doesBreakGlass = false;
		bullet.style = BulletConfiguration.STYLE_GRENADE;
		bullet.plink = BulletConfiguration.PLINK_GRENADE;
		bullet.vPFX = "smoke";
		
		return bullet;
	}
	
	public static BulletConfiguration standardNukeConfig() {

		BulletConfiguration bullet = new BulletConfiguration();
		
		bullet.velocity = 3.0F;
		bullet.spread = 0.005F;
		bullet.wear = 10;
		bullet.bulletsMin = 1;
		bullet.bulletsMax = 1;
		bullet.dmgMin = 1000;
		bullet.dmgMax = 1000;
		bullet.gravity = 0.025F;
		bullet.maxAge = 300;
		bullet.doesRicochet = false;
		bullet.ricochetAngle = 0;
		bullet.HBRC = 0;
		bullet.LBRC = 0;
		bullet.bounceMod = 1.0;
		bullet.doesPenetrate = true;
		bullet.doesBreakGlass = false;
		bullet.style = BulletConfiguration.STYLE_NUKE;
		bullet.plink = BulletConfiguration.PLINK_GRENADE;
		
		return bullet;
	}

	public static BulletConfiguration standardBuckshotConfig() {

		BulletConfiguration bullet = new BulletConfiguration();

		bullet.velocity = 5.0F;
		bullet.spread = 0.05F;
		bullet.wear = 10;
		bullet.bulletsMin = 5;
		bullet.bulletsMax = 8;
		bullet.gravity = 0F;
		bullet.maxAge = 100;
		bullet.doesRicochet = true;
		bullet.ricochetAngle = 15;
		bullet.HBRC = 5;
		bullet.LBRC = 65;
		bullet.bounceMod = 0.8;
		bullet.doesPenetrate = false;
		bullet.doesBreakGlass = true;
		bullet.style = BulletConfiguration.STYLE_PELLET;
		bullet.plink = BulletConfiguration.PLINK_BULLET;
		bullet.leadChance = 10;

		return bullet;
	}

	public static BulletConfiguration standardRocketConfig() {

		BulletConfiguration bullet = new BulletConfiguration();

		bullet.velocity = 2.0F;
		bullet.spread = 0.005F;
		bullet.wear = 10;
		bullet.bulletsMin = 1;
		bullet.bulletsMax = 1;
		bullet.gravity = 0.005F;
		bullet.maxAge = 300;
		bullet.doesRicochet = true;
		bullet.ricochetAngle = 10;
		bullet.HBRC = 2;
		bullet.LBRC = 100;
		bullet.bounceMod = 0.8;
		bullet.doesPenetrate = false;
		bullet.doesBreakGlass = false;
		bullet.explosive = 5.0F;
		bullet.style = BulletConfiguration.STYLE_ROCKET;
		bullet.plink = BulletConfiguration.PLINK_GRENADE;

		return bullet;
	}

	public static BulletConfiguration standardGrenadeConfig() {

		BulletConfiguration bullet = new BulletConfiguration();

		bullet.velocity = 2.0F;
		bullet.spread = 0.005F;
		bullet.wear = 10;
		bullet.bulletsMin = 1;
		bullet.bulletsMax = 1;
		bullet.gravity = 0.035F;
		bullet.maxAge = 300;
		bullet.doesRicochet = false;
		bullet.ricochetAngle = 0;
		bullet.HBRC = 0;
		bullet.LBRC = 0;
		bullet.bounceMod = 1.0;
		bullet.doesPenetrate = false;
		bullet.doesBreakGlass = false;
		bullet.explosive = 2.5F;
		bullet.style = BulletConfiguration.STYLE_GRENADE;
		bullet.plink = BulletConfiguration.PLINK_GRENADE;
		bullet.vPFX = "smoke";

		return bullet;
	}
	
	public static IBulletImpactBehavior getPhosphorousEffect(final int radius, final int duration, final int count, final double motion, float hazeChance) {
		
		IBulletImpactBehavior impact = new IBulletImpactBehavior() {

			@Override
			public void behaveBlockHit(EntityBulletBase bullet, int x, int y, int z) {
				if(CompatibilityConfig.isWarDim(bullet.world)){
					List<Entity> hit = bullet.world.getEntitiesWithinAABBExcludingEntity(bullet, new AxisAlignedBB(bullet.posX - radius, bullet.posY - radius, bullet.posZ - radius, bullet.posX + radius, bullet.posY + radius, bullet.posZ + radius));
					
					for(Entity e : hit) {
						
						if(!Library.isObstructed(bullet.world, bullet.posX, bullet.posY, bullet.posZ, e.posX, e.posY + e.getEyeHeight(), e.posZ)) {
							e.setFire(5);
							
							if(e instanceof EntityLivingBase) {
								
								PotionEffect eff = new PotionEffect(HbmPotion.phosphorus, duration, 0, true, false);
								eff.getCurativeItems().clear();
								((EntityLivingBase)e).addPotionEffect(eff);
							}
						}
					}
				}
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "vanillaburst");
				data.setString("mode", "flame");
				data.setInteger("count", count);
				data.setDouble("motion", motion);

				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, bullet.posX, bullet.posY, bullet.posZ), new TargetPoint(bullet.dimension, bullet.posX, bullet.posY, bullet.posZ, 50));
				if(bullet.world.rand.nextFloat() < hazeChance) {
					NBTTagCompound haze = new NBTTagCompound();
					haze.setString("type", "haze");
					PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(haze, bullet.posX, bullet.posY, bullet.posZ), new TargetPoint(bullet.dimension, bullet.posX, bullet.posY, bullet.posZ, 150));
				}
			}
		};
		
		return impact;
	}
	
	public static IBulletImpactBehavior getGasEffect(final int radius, final int duration) {
		
		IBulletImpactBehavior impact = new IBulletImpactBehavior() {

			@Override
			public void behaveBlockHit(EntityBulletBase bullet, int x, int y, int z) {
				
				List<Entity> hit = bullet.world.getEntitiesWithinAABBExcludingEntity(bullet, new AxisAlignedBB(bullet.posX - radius, bullet.posY - radius, bullet.posZ - radius, bullet.posX + radius, bullet.posY + radius, bullet.posZ + radius));
				
				for(Entity e : hit) {
					
					if(!Library.isObstructed(bullet.world, bullet.posX, bullet.posY, bullet.posZ, e.posX, e.posY + e.getEyeHeight(), e.posZ)) {
						
						if(e instanceof EntityLivingBase) {
		
							EntityLivingBase entityLiving = (EntityLivingBase) e;
							
							if(ArmorRegistry.hasAllProtection(entityLiving, EntityEquipmentSlot.HEAD, HazardClass.GAS_CHLORINE)) {
								ArmorUtil.damageGasMaskFilter(entityLiving, 1);
							} else {
								PotionEffect eff0 = new PotionEffect(MobEffects.POISON, duration, 2, true, false);
								PotionEffect eff1 = new PotionEffect(MobEffects.MINING_FATIGUE, duration, 2, true, false);
								PotionEffect eff2 = new PotionEffect(MobEffects.WEAKNESS, duration, 4, true, false);
								PotionEffect eff3 = new PotionEffect(MobEffects.WITHER, (int)Math.ceil(duration * 0.1), 0, true, false);
								eff0.getCurativeItems().clear();
								eff1.getCurativeItems().clear();
								eff2.getCurativeItems().clear();
								eff3.getCurativeItems().clear();
								entityLiving.addPotionEffect(eff0);
								entityLiving.addPotionEffect(eff1);
								entityLiving.addPotionEffect(eff2);
								entityLiving.addPotionEffect(eff3);
							}
						}
					}
				}
				
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "vanillaburst");
				data.setString("mode", "cloud");
				data.setInteger("count", 15);
				data.setDouble("motion", 0.1D);

				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, bullet.posX, bullet.posY, bullet.posZ), new TargetPoint(bullet.dimension, bullet.posX, bullet.posY, bullet.posZ, 50));
			}
		};
		
		return impact;
	}
	
	public static IBulletUpdateBehavior getLaserSteering() {

		IBulletUpdateBehavior onUpdate = new IBulletUpdateBehavior() {

			@Override
			public void behaveUpdate(EntityBulletBase bullet) {

				if(bullet.shooter == null || !(bullet.shooter instanceof EntityPlayer))
					return;
				
				if(Vec3.createVectorHelper(bullet.posX - bullet.shooter.posX, bullet.posY - bullet.shooter.posY, bullet.posZ - bullet.shooter.posZ).length() > 100)
					return;

				RayTraceResult mop = Library.rayTraceIncludeEntities((EntityPlayer)bullet.shooter, 200, 1);
				
				if(mop == null || mop.hitVec == null)
					return;
				if(mop.typeOfHit == Type.ENTITY){
					Entity ent = mop.entityHit;
					mop.hitVec = new Vec3d(ent.posX, ent.posY + ent.getEyeHeight()/2, ent.posZ);
				}

				Vec3 vec = Vec3.createVectorHelper(mop.hitVec.x - bullet.posX, mop.hitVec.y - bullet.posY, mop.hitVec.z - bullet.posZ);

				if(vec.length() < 1)
					return;

				vec = vec.normalize();

				double speed = Vec3.createVectorHelper(bullet.motionX, bullet.motionY, bullet.motionZ).length();

				bullet.motionX = vec.xCoord * speed;
				bullet.motionY = vec.yCoord * speed;
				bullet.motionZ = vec.zCoord * speed;
			}

		};

		return onUpdate;
	}
	
	public static IBulletUpdateBehavior getHomingBehavior(final double range, final double angle) {

		IBulletUpdateBehavior onUpdate = new IBulletUpdateBehavior() {

			@Override
			public void behaveUpdate(EntityBulletBase bullet) {

				if(bullet.world.isRemote)
					return;

				if(bullet.world.getEntityByID(bullet.getEntityData().getInteger("homingTarget")) == null) {
					chooseTarget(bullet);
				}

				Entity target = bullet.world.getEntityByID(bullet.getEntityData().getInteger("homingTarget"));

				if(target != null) {

					Vec3 delta = Vec3.createVectorHelper(target.posX - bullet.posX, target.posY + target.height / 2 - bullet.posY, target.posZ - bullet.posZ);
					delta = delta.normalize();

					double vel = Vec3.createVectorHelper(bullet.motionX, bullet.motionY, bullet.motionZ).length();

					bullet.motionX = delta.xCoord * vel;
					bullet.motionY = delta.yCoord * vel;
					bullet.motionZ = delta.zCoord * vel;
				}
			}

			private void chooseTarget(EntityBulletBase bullet) {

				List<EntityLivingBase> entities = bullet.world.getEntitiesWithinAABB(EntityLivingBase.class, bullet.getEntityBoundingBox().grow(range, range, range));

				Vec3d mot = new Vec3d(bullet.motionX, bullet.motionY, bullet.motionZ);

				EntityLivingBase target = null;
				double targetAngle = angle;

				for(EntityLivingBase e : entities) {
					if(!e.isEntityAlive() || e == bullet.shooter)
						continue;

					Vec3d delta = new Vec3d(e.posX - bullet.posX, e.posY + e.height / 2 - bullet.posY, e.posZ - bullet.posZ);

					if(bullet.world.rayTraceBlocks(new Vec3d(bullet.posX, bullet.posY, bullet.posZ), new Vec3d(e.posX, e.posY + e.height / 2, e.posZ)) != null)
						continue;
					
					double dist = e.getDistanceSq(bullet);

					if(dist < range * range) {
						double deltaAngle = BobMathUtil.getCrossAngle(mot, delta);
						if(deltaAngle < targetAngle) {
							target = e;
							targetAngle = deltaAngle;
						}
					}
				}

				if(target != null) {
					bullet.getEntityData().setInteger("homingTarget", target.getEntityId());
				}
			}
		};

		return onUpdate;
	}
	
	/*
	 * Sizes:
	 * 0 - safe
	 * 1 - tot
	 * 2 - small
	 * 3 - medium
	 * 4 - big
	 */
	public static void nuclearExplosion(EntityBulletBase bullet, int x, int y, int z, int size) {
		
		if(!bullet.world.isRemote) {

			double posX = bullet.posX;
			double posY = bullet.posY + 0.5;
			double posZ = bullet.posZ;
			
			if(y >= 0) {
				posX = x + 0.5;
				posY = y + 1.5;
				posZ = z + 0.5;
			}
			if(size > 0)
				bullet.world.spawnEntity(EntityNukeExplosionMK5.statFac(bullet.world, size, posX, posY, posZ));
            EntityNukeTorex.statFac(bullet.world, posX, posY, posZ, size == 0 ? 15 : size);
		}
	}
}
