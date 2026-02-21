package com.hbm.handler;

import com.hbm.config.GeneralConfig;
import com.hbm.config.MobConfig;
import com.hbm.config.WorldConfig;
import com.hbm.entity.mob.EntityFBI;
import com.hbm.entity.mob.EntityFBIDrone;
import com.hbm.entity.mob.EntityMaskMan;
import com.hbm.entity.mob.EntityRADBeast;
import com.hbm.entity.projectile.EntityMeteor;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.MutableVec3d;
import com.hbm.util.Vec3NT;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.eventhandler.Event.Result;

import java.util.Random;

public class BossSpawnHandler {
	//because some dimwit keeps resetting the world rand
	private static final Random meteorRand = new Random();

	public static void rollTheDice(World world) {

		if(MobConfig.enableMaskman) {
			if(world.getTotalWorldTime() % MobConfig.maskmanDelay == 0) {

				if(world.rand.nextInt(MobConfig.maskmanChance) == 0 && !world.playerEntities.isEmpty() && world.provider.isSurfaceWorld()) {	//33% chance only if there is a player online

					EntityPlayer player = world.playerEntities.get(world.rand.nextInt(world.playerEntities.size()));	//choose a random player
					
					
					if(ContaminationUtil.getRads(player) >= MobConfig.maskmanMinRad && (world.getHeight((int)player.posX, (int)player.posZ) > player.posY + 3 || !MobConfig.maskmanUnderground)) {	//if the player has more than 50 RAD and is underground
						player.sendMessage(new TextComponentString("The mask man is about to claim another victim.").setStyle(new Style().setColor(TextFormatting.RED)));
						
						double spawnX = player.posX + world.rand.nextGaussian() * 20;
						double spawnZ = player.posZ + world.rand.nextGaussian() * 20;
						double spawnY = world.getHeight((int)spawnX, (int)spawnZ);

						trySpawn(world, (float)spawnX, (float)spawnY, (float)spawnZ, new EntityMaskMan(world));
					}
				}
			}
		}

		if(MobConfig.enableRaids) {

			if(world.getTotalWorldTime() % MobConfig.raidDelay == 0) {

				if(world.rand.nextInt(MobConfig.raidChance) == 0 && !world.playerEntities.isEmpty() && world.provider.isSurfaceWorld()) {

					EntityPlayer player = world.playerEntities.get(world.rand.nextInt(world.playerEntities.size()));
					player.sendMessage(new TextComponentString("FBI, OPEN UP!").setStyle(new Style().setColor(TextFormatting.RED)));
					
					Vec3 vec = Vec3.createVectorHelper(MobConfig.raidAttackDistance, 0, 0);
					vec.rotateAroundY((float)(Math.PI * 2) * world.rand.nextFloat());

					for(int i = 0; i < MobConfig.raidAmount; i++) {

						double spawnX = player.posX + vec.xCoord + world.rand.nextGaussian() * 5;
						double spawnZ = player.posZ + vec.zCoord + world.rand.nextGaussian() * 5;
						double spawnY = world.getHeight((int)spawnX, (int)spawnZ);

						trySpawn(world, (float)spawnX, (float)spawnY, (float)spawnZ, new EntityFBI(world));
					}

					for(int i = 0; i < MobConfig.raidDrones; i++) {

						double spawnX = player.posX + vec.xCoord + world.rand.nextGaussian() * 5;
						double spawnZ = player.posZ + vec.zCoord + world.rand.nextGaussian() * 5;
						double spawnY = world.getHeight((int)spawnX, (int)spawnZ);

						trySpawn(world, (float)spawnX, (float)spawnY + 10, (float)spawnZ, new EntityFBIDrone(world));
					}
				}
			}
		}
		
		if(MobConfig.enableRaids) {
			
			if(world.getTotalWorldTime() % MobConfig.raidDelay == 0) {
				
				if(world.rand.nextInt(MobConfig.raidChance) == 0 && !world.playerEntities.isEmpty() && world.provider.isSurfaceWorld()) {
					
					EntityPlayer player = world.playerEntities.get(world.rand.nextInt(world.playerEntities.size()));
					
					if(player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getLong("fbiMark") < world.getTotalWorldTime()) {
						player.sendMessage(new TextComponentString("FBI, OPEN UP!").setStyle(new Style().setColor(TextFormatting.RED)));
						
						Vec3 vec = Vec3.createVectorHelper(MobConfig.raidAttackDistance, 0, 0);
						vec.rotateAroundY((float)(Math.PI * 2) * world.rand.nextFloat());
						
						for(int i = 0; i < MobConfig.raidAmount; i++) {
	
							double spawnX = player.posX + vec.xCoord + world.rand.nextGaussian() * 5;
							double spawnZ = player.posZ + vec.zCoord + world.rand.nextGaussian() * 5;
							double spawnY = world.getHeight((int)spawnX, (int)spawnZ);
							
							trySpawn(world, (float)spawnX, (float)spawnY, (float)spawnZ, new EntityFBI(world));
						}
					}
				}
			}
		}
		
		if(MobConfig.enableElementals) {

			if(world.getTotalWorldTime() % MobConfig.elementalDelay == 0) {

				if(world.rand.nextInt(MobConfig.elementalChance) == 0 && !world.playerEntities.isEmpty() && world.provider.isSurfaceWorld()) {

					EntityPlayer player = world.playerEntities.get(world.rand.nextInt(world.playerEntities.size()));

					if(player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getBoolean("radMark")) {

						player.sendMessage(new TextComponentString("You hear a faint clicking...").setStyle(new Style().setColor(TextFormatting.YELLOW)));
						player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).setBoolean("radMark", false);

						Vec3 vec = Vec3.createVectorHelper(MobConfig.raidAttackDistance, 0, 0);

						for(int i = 0; i < MobConfig.elementalAmount; i++) {

							vec.rotateAroundY((float)(Math.PI * 2) * world.rand.nextFloat());

							double spawnX = player.posX + vec.xCoord + world.rand.nextGaussian();
							double spawnZ = player.posZ + vec.zCoord + world.rand.nextGaussian();
							double spawnY = world.getHeight((int)spawnX, (int)spawnZ);

							EntityRADBeast rad = new EntityRADBeast(world);

							if(i == 0)
								rad.makeLeader();

							trySpawn(world, (float)spawnX, (float)spawnY, (float)spawnZ, rad);
						}
					}
				}
			}
		}

		if(GeneralConfig.enableMeteorStrikes && !world.isRemote) {
			meteorUpdate(world);
		}
	}
	
	private static void trySpawn(World world, float x, float y, float z, EntityLiving e) {

		e.setLocationAndAngles(x, y, z, world.rand.nextFloat() * 360.0F, 0.0F);
		Result canSpawn = ForgeEventFactory.canEntitySpawn(e, world, x, y, z, null);

		if (canSpawn == Result.ALLOW || canSpawn == Result.DEFAULT) {
			world.spawnEntity(e);
			ForgeEventFactory.doSpecialSpawn(e, world, x, y, z, null);
			e.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(e)), null);
		}
	}
	
	public static void markFBI(EntityPlayer player) {
		if(!player.world.isRemote)
			player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).setLong("fbiMark", player.world.getTotalWorldTime() + 20 * 60 * 20);
	}
	
	public static int meteorShower = 0;
	private static void meteorUpdate(World world) {
		if(meteorRand.nextInt(meteorShower > 0 ? WorldConfig.meteorShowerChance : WorldConfig.meteorStrikeChance) == 0) {
				if(!world.playerEntities.isEmpty()) {
					EntityPlayer p = world.playerEntities.get(world.rand.nextInt(world.playerEntities.size()));
					if(p != null && p.dimension == 0) {
						boolean repell = false;
						boolean strike = true;

						ItemStack armor = p.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
						if(!armor.isEmpty() && ArmorModHandler.hasMods(armor)) {
							ItemStack mod = ArmorModHandler.pryMods(armor)[ArmorModHandler.helmet_only];
							
							if(mod != null) {
								if(mod.getItem() == ModItems.protection_charm) {
									repell = true;
								}
								if(mod.getItem() == ModItems.meteor_charm) {
									strike = false;
								}
							}
						}

						if(strike)
							spawnMeteorAtPlayer(p, repell);
					}
				}
			}

		if(meteorShower > 0) {
			meteorShower--;
			if(meteorShower == 0 && GeneralConfig.enableDebugMode)
				MainRegistry.logger.info("Ended meteor shower.");
		}

		if(meteorRand.nextInt(WorldConfig.meteorStrikeChance * 100) == 0 && GeneralConfig.enableMeteorShowers) {
			meteorShower = (int)(WorldConfig.meteorShowerDuration * 0.75 + WorldConfig.meteorShowerDuration * 0.25 * meteorRand.nextFloat());

			if(GeneralConfig.enableDebugMode)
				MainRegistry.logger.info("Started meteor shower! Duration: " + meteorShower);
		}
	}

	public static void spawnMeteorAtPlayer(EntityPlayer player, boolean repell) {
		EntityMeteor meteor = new EntityMeteor(player.world);
		meteor.setPositionAndRotation(player.posX + meteorRand.nextInt(201) - 100, 384, player.posZ + meteorRand.nextInt(201) - 100, 0, 0);

        MutableVec3d vec;
		if(repell) {
			vec = new MutableVec3d(meteor.posX - player.posX, 0, meteor.posZ - player.posZ).normalizeSelf();
			double vel = meteorRand.nextDouble();
			vec.setX(vec.x * vel);
			vec.setZ(vec.z * vel);
			meteor.safe = true;
		} else {
			vec = new MutableVec3d(meteorRand.nextDouble() - 0.5D, 0, 0);
			vec.rotateYawSelf((float) (Math.PI * meteorRand.nextDouble()));
		}

		meteor.motionX = vec.x;
		meteor.motionY = -2.5;
		meteor.motionZ = vec.z;
		player.world.spawnEntity(meteor);
	}

}
