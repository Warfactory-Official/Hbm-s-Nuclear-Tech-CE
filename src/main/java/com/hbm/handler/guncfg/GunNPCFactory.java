package com.hbm.handler.guncfg;

import com.hbm.entity.projectile.EntityBulletBase;
import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.handler.BulletConfiguration;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.IBulletImpactBehavior;
import com.hbm.interfaces.IBulletUpdateBehavior;
import com.hbm.inventory.RecipesCommon;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.util.BobMathUtil;
import com.hbm.util.ContaminationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import java.util.List;
import java.util.Random;

public class GunNPCFactory {

	public static BulletConfiguration getMaskmanOrb() {

		BulletConfiguration bullet = new BulletConfiguration();

		bullet.ammo = new RecipesCommon.ComparableStack(ModItems.coin_maskman);
		bullet.velocity = 0.25F;
		bullet.spread = 0.000F;
		bullet.wear = 10;
		bullet.bulletsMin = 1;
		bullet.bulletsMax = 1;
		bullet.dmgMin = 100;
		bullet.dmgMax = 100;
		bullet.gravity = 0.0F;
		bullet.maxAge = 60;
		bullet.doesRicochet = false;
		bullet.ricochetAngle = 0;
		bullet.HBRC = 0;
		bullet.LBRC = 0;
		bullet.bounceMod = 1.0;
		bullet.doesPenetrate = false;
		bullet.doesBreakGlass = false;
		bullet.style = BulletConfiguration.STYLE_ORB;
		bullet.trail = 1;
		bullet.explosive = 1.5F;

		bullet.bUpdate = new IBulletUpdateBehavior() {

			@Override
			public void behaveUpdate(EntityBulletBase bullet) {

				if(bullet.world.isRemote)
					return;

				if(bullet.ticksExisted % 10 != 5)
					return;

				List<EntityPlayer> players = bullet.world.getEntitiesWithinAABB(EntityPlayer.class, bullet.getEntityBoundingBox().grow(50, 50, 50));

				for(EntityPlayer player : players) {

					Vec3 motion = Vec3.createVectorHelper(player.posX - bullet.posX, (player.posY + player.getEyeHeight()) - bullet.posY, player.posZ - bullet.posZ);
					motion = motion.normalize();

					EntityBulletBase bolt = new EntityBulletBase(bullet.world, BulletConfigSyncingUtil.MASKMAN_BOLT);
					bolt.shooter = bullet.shooter;
					bolt.setPosition(bullet.posX, bullet.posY, bullet.posZ);
					bolt.shoot(motion.xCoord, motion.yCoord, motion.zCoord, 0.5F, 0.05F);
					bullet.world.spawnEntity(bolt);
				}
			}
		};

		return bullet;
	}

	public static BulletConfiguration getMaskmanBolt() {

		BulletConfiguration bullet = BulletConfigFactory.standardBulletConfig();

		bullet.ammo = new RecipesCommon.ComparableStack(ModItems.coin_maskman);
		bullet.spread = 0.0F;
		bullet.dmgMin = 15;
		bullet.dmgMax = 20;
		bullet.wear = 10;
		bullet.leadChance = 0;
		bullet.explosive = 0.5F;
		bullet.setToBolt(BulletConfiguration.BOLT_LACUNAE);
		bullet.vPFX = "reddust";

		return bullet;
	}

	public static BulletConfiguration getMaskmanBullet() {

		BulletConfiguration bullet = BulletConfigFactory.standardBulletConfig();

		bullet.ammo = new RecipesCommon.ComparableStack(ModItems.coin_maskman);
		bullet.spread = 0.0F;
		bullet.dmgMin = 5;
		bullet.dmgMax = 10;
		bullet.wear = 10;
		bullet.leadChance = 15;
		bullet.style = BulletConfiguration.STYLE_FLECHETTE;
		bullet.vPFX = "bluedust";

		return bullet;
	}

	public static BulletConfiguration getMaskmanTracer() {

		BulletConfiguration bullet = BulletConfigFactory.standardBulletConfig();

		bullet.ammo = new RecipesCommon.ComparableStack(ModItems.coin_maskman);
		bullet.spread = 0.0F;
		bullet.dmgMin = 15;
		bullet.dmgMax = 20;
		bullet.wear = 10;
		bullet.leadChance = 0;
		bullet.setToBolt(BulletConfiguration.BOLT_NIGHTMARE);
		bullet.vPFX = "reddust";

		bullet.bImpact = new IBulletImpactBehavior() {

			@Override
			public void behaveBlockHit(EntityBulletBase bullet, int x, int y, int z) {

				if(bullet.world.isRemote)
					return;

				EntityBulletBase meteor = new EntityBulletBase(bullet.world, BulletConfigSyncingUtil.MASKMAN_METEOR);
				meteor.setPosition(bullet.posX, bullet.posY + 30 + meteor.world.rand.nextInt(10), bullet.posZ);
				meteor.motionY = -1D;
				meteor.shooter = bullet.shooter;
				bullet.world.spawnEntity(meteor);
			}
		};

		return bullet;
	}

	public static BulletConfiguration getMaskmanRocket() {

		BulletConfiguration bullet = BulletConfigFactory.standardGrenadeConfig();

		bullet.ammo = new RecipesCommon.ComparableStack(ModItems.coin_maskman);
		bullet.gravity = 0.1F;
		bullet.velocity = 1.0F;
		bullet.dmgMin = 15;
		bullet.dmgMax = 20;
		bullet.blockDamage = false;
		bullet.explosive = 5.0F;
		bullet.style = BulletConfiguration.STYLE_ROCKET;

		return bullet;
	}

	public static BulletConfiguration getMaskmanMeteor() {

		BulletConfiguration bullet = BulletConfigFactory.standardGrenadeConfig();

		bullet.ammo = new RecipesCommon.ComparableStack(ModItems.coin_maskman);
		bullet.gravity = 0.1F;
		bullet.velocity = 1.0F;
		bullet.dmgMin = 20;
		bullet.dmgMax = 30;
		bullet.incendiary = 3;
		bullet.explosive = 2.5F;
		bullet.style = BulletConfiguration.STYLE_METEOR;

		bullet.bUpdate = new IBulletUpdateBehavior() {

			@Override
			public void behaveUpdate(EntityBulletBase bullet) {

				if(!bullet.world.isRemote)
					return;

				Random rand = bullet.world.rand;

				for(int i = 0; i < 5; i++) {
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setString("type", "vanillaExt");
					nbt.setString("mode", "flame");
					nbt.setDouble("posX", bullet.posX + rand.nextDouble() * 0.5 - 0.25);
					nbt.setDouble("posY", bullet.posY + rand.nextDouble() * 0.5 - 0.25);
					nbt.setDouble("posZ", bullet.posZ + rand.nextDouble() * 0.5 - 0.25);
					MainRegistry.proxy.effectNT(nbt);
				}
			}
		};

		return bullet;
	}
	
	public static BulletConfiguration getWormBolt() {

		BulletConfiguration bullet = BulletConfigFactory.standardBulletConfig();

		bullet.ammo = new RecipesCommon.ComparableStack(ModItems.coin_worm);
		bullet.spread = 0.0F;
		bullet.maxAge = 60;
		bullet.dmgMin = 15;
		bullet.dmgMax = 25;
		bullet.leadChance = 0;
		bullet.doesRicochet = false;
		bullet.setToBolt(BulletConfiguration.BOLT_WORM);

		return bullet;
	}

	public static BulletConfiguration getWormHeadBolt() {

		BulletConfiguration bullet = BulletConfigFactory.standardBulletConfig();

		bullet.ammo = new RecipesCommon.ComparableStack(ModItems.coin_worm);
		bullet.spread = 0.0F;
		bullet.maxAge = 100;
		bullet.dmgMin = 35;
		bullet.dmgMax = 60;
		bullet.leadChance = 0;
		bullet.doesRicochet = false;
		bullet.setToBolt(BulletConfiguration.BOLT_LASER);

		return bullet;
	}
	
	public static BulletConfiguration getRocketUFOConfig() {
		
		BulletConfiguration bullet = GunRocketFactory.getRocketConfig();
		
		bullet.vPFX = "reddust";
		bullet.destroysBlocks = false;
		bullet.explosive = 0F;
		
		bullet.bUpdate = new IBulletUpdateBehavior() {
			
			double angle = 90;
			double range = 100;

			@Override
			public void behaveUpdate(EntityBulletBase bullet) {
				
				if(bullet.world.isRemote)
					return;
				
				if(bullet.world.getEntityByID(bullet.getEntityData().getInteger("homingTarget")) == null) {
					chooseTarget(bullet);
				}
				
				Entity target = bullet.world.getEntityByID(bullet.getEntityData().getInteger("homingTarget"));
				
				if(target != null) {
					
					if(bullet.getDistance(target) < 5) {
						bullet.getConfig().bImpact.behaveBlockHit(bullet, -1, -1, -1);
						bullet.setDead();
						return;
					}
					
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
					RayTraceResult r = bullet.world.rayTraceBlocks(new Vec3d(bullet.posX, bullet.posY, bullet.posZ), Vec3.createVectorHelper(e.posX, e.posY + e.height / 2, e.posZ).toVec3d(), false, true, false);
					if(r != null && r.typeOfHit != Type.MISS)
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
		
		bullet.bImpact = new IBulletImpactBehavior() {

			@Override
			public void behaveBlockHit(EntityBulletBase bullet, int x, int y, int z) {

				bullet.world.playSound(null, bullet.posX, bullet.posY, bullet.posZ, HBMSoundHandler.ufoBlast, SoundCategory.HOSTILE, 5.0F, 0.9F + bullet.world.rand.nextFloat() * 0.2F);
				bullet.world.playSound(null, bullet.posX, bullet.posY, bullet.posZ, SoundEvents.ENTITY_FIREWORK_BLAST, SoundCategory.HOSTILE, 5.0F, 0.5F);
				ContaminationUtil.radiate(bullet.world, bullet.posX, bullet.posY, bullet.posZ, 50, 0, 0, 500);
				
				for(int i = 0; i < 3; i++) {
					NBTTagCompound data = new NBTTagCompound();
					data.setString("type", "plasmablast");
					data.setFloat("r", 0.0F);
					data.setFloat("g", 0.75F);
					data.setFloat("b", 1.0F);
					data.setFloat("pitch", -30F + 30F * i);
					data.setFloat("yaw", bullet.world.rand.nextFloat() * 180F);
					data.setFloat("scale", 5F);
					PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, bullet.posX, bullet.posY, bullet.posZ),
							new TargetPoint(bullet.world.provider.getDimension(), bullet.posX, bullet.posY, bullet.posZ, 100));
				}
			}
		};
		
		return bullet;
	}
}
