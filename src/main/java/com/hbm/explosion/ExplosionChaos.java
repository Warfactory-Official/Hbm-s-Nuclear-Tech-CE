package com.hbm.explosion;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockMeta;
import com.hbm.config.CompatibilityConfig;
import com.hbm.entity.grenade.EntityGrenadeTau;
import com.hbm.entity.grenade.EntityGrenadeZOMG;
import com.hbm.entity.projectile.*;
import com.hbm.handler.ArmorUtil;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.potion.HbmPotion;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorRegistry.HazardClass;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

//TODO:This whole class looks outdated as fuck
public class ExplosionChaos {

	private final static Random random = new Random();
	private static Random rand = new Random();

	/**
     * Optimized iteration algorithm to reduce CPU load during large scale block operations.
     */
    private static void forEachBlockInSphere(World world, Entity detonator, int x, int y, int z, int radius, Consumer<BlockPos.MutableBlockPos> action) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int radiusSqHalf = (radius * radius) / 2;

        for (int yy = -radius; yy < radius; yy++) {
            int currentY = y + yy;
            if (currentY < 0 || currentY > 255) continue;

            int YY = yy * yy;
            if (YY >= radiusSqHalf) continue;

            int xzRadius = (int) Math.sqrt(radiusSqHalf - YY);

            for (int xx = -xzRadius; xx <= xzRadius; xx++) {
                int XX = xx * xx;
                int YY_XX = YY + XX;
                if (YY_XX >= radiusSqHalf) continue;

                int zRadius = (int) Math.sqrt(radiusSqHalf - YY_XX);

                for (int zz = -zRadius; zz <= zRadius; zz++) {
                    action.accept(pos.setPos(x + xx, currentY, z + zz));
                }
            }
        }
    }

	public static void explode(World world, Entity detonator, int x, int y, int z, int bombStartStrength) {
		if(!CompatibilityConfig.isWarDim(world)) return;
		forEachBlockInSphere(world, detonator, x, y, z, bombStartStrength, pos -> destruction(world, detonator, pos));
	}
	
	private static void destruction(World world, Entity detonator, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block b = state.getBlock();
        if(b == Blocks.BEDROCK || b == ModBlocks.reinforced_brick || b == ModBlocks.reinforced_sand || b == ModBlocks.reinforced_glass || b == ModBlocks.reinforced_lamp_on || b == ModBlocks.reinforced_lamp_off || b.getExplosionResistance(null) > 2_000_000) {
            // Indestructible
        } else {
            world.setBlockToAir(pos);
        }
    }

	public static void spawnExplosion(World world, Entity detonator, int x, int y, int z, int bound) {
		if(!CompatibilityConfig.isWarDim(world)){
			return;
		}
		int randX;
		int randY;
		int randZ;

		for(int i = 0; i < 25; i++) {

			randX = random.nextInt(bound);
			randY = random.nextInt(bound);
			randZ = random.nextInt(bound);

			world.createExplosion(detonator, x + randX, y + randY, z + randZ, 10.0F, true);
			// ExplosionChaos.explode(world, x + randX, y + randY, z + randZ,
			// 5);

			randX = random.nextInt(bound);
			randY = random.nextInt(bound);
			randZ = random.nextInt(bound);

			world.createExplosion(detonator, x + randX, y - randY, z + randZ, 10.0F, true);
			// ExplosionChaos.explode(world, x - randX, y + randY, z + randZ,
			// 5);

			randX = random.nextInt(bound);
			randY = random.nextInt(bound);
			randZ = random.nextInt(bound);

			world.createExplosion(detonator, x + randX, y + randY, z - randZ, 10.0F, true);
			// ExplosionChaos.explode(world, x + randX, y - randY, z + randZ,
			// 5);

			randX = random.nextInt(bound);
			randY = random.nextInt(bound);
			randZ = random.nextInt(bound);

			world.createExplosion(detonator, x - randX, y + randY, z + randZ, 10.0F, true);
			// ExplosionChaos.explode(world, x + randX, y + randY, z - randZ,
			// 5);
			randX = random.nextInt(bound);
			randY = random.nextInt(bound);
			randZ = random.nextInt(bound);

			world.createExplosion(detonator, x - randX, y - randY, z + randZ, 10.0F, true);
			// ExplosionChaos.explode(world, x - randX, y - randY, z + randZ,
			// 5);

			randX = random.nextInt(bound);
			randY = random.nextInt(bound);
			randZ = random.nextInt(bound);

			world.createExplosion(detonator, x - randX, y + randY, z - randZ, 10.0F, true);
			// ExplosionChaos.explode(world, x - randX, y + randY, z - randZ,
			// 5);

			randX = random.nextInt(bound);
			randY = random.nextInt(bound);
			randZ = random.nextInt(bound);

			world.createExplosion(detonator, x + randX, y - randY, z - randZ, 10.0F, true);
			// ExplosionChaos.explode(world, x + randX, y - randY, z - randZ,
			// 5);

			randX = random.nextInt(bound);
			randY = random.nextInt(bound);
			randZ = random.nextInt(bound);

			world.createExplosion(detonator, x - randX, y - randY, z - randZ, 10.0F, true);
			// ExplosionChaos.explode(world, x - randX, y - randY, z - randZ,
			// 5);
		}
	}

	// Drillgon200: Descriptive method names anyone?
	// Alcater: Ill write this down - maybe ill need it later. c stands for cloudPoisoning
	public static void c(World world, int x, int y, int z, int bombStartStrength) {
		if(!CompatibilityConfig.isWarDim(world)){
			return;
		}
		float f = bombStartStrength;
		int i;
		int j;
		int k;
		double d5;
		double d6;
		double d7;
		double wat = bombStartStrength * 2;

		bombStartStrength *= 2.0F;
		i = MathHelper.floor(x - wat - 1.0D);
		j = MathHelper.floor(x + wat + 1.0D);
		k = MathHelper.floor(y - wat - 1.0D);
		int i2 = MathHelper.floor(y + wat + 1.0D);
		int l = MathHelper.floor(z - wat - 1.0D);
		int j2 = MathHelper.floor(z + wat + 1.0D);
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(i, k, l, j, i2, j2));

		for(int i1 = 0; i1 < list.size(); ++i1) {
			Entity entity = (Entity) list.get(i1);
			double d4 = entity.getDistance(x, y, z) / bombStartStrength;

			if(d4 <= 1.0D) {
				d5 = entity.posX - x;
				d6 = entity.posY + entity.getEyeHeight() - y;
				d7 = entity.posZ - z;
				double d9 = MathHelper.sqrt(d5 * d5 + d6 * d6 + d7 * d7);
				if(d9 < wat) {

					if(entity instanceof EntityPlayer) {
						if(!ArmorRegistry.hasProtection((EntityPlayer) entity, EntityEquipmentSlot.HEAD, HazardClass.GAS_BLISTERING)){
							ArmorUtil.damageSuit((EntityPlayer) entity, 0, 5);
							ArmorUtil.damageSuit((EntityPlayer) entity, 1, 5);
							ArmorUtil.damageSuit((EntityPlayer) entity, 2, 5);
							ArmorUtil.damageSuit((EntityPlayer) entity, 3, 5);
						}
					}

					if(!(entity instanceof EntityPlayer && ArmorUtil.checkForHazmat((EntityPlayer) entity))) {

						if(entity instanceof EntityLivingBase){
							EntityLivingBase livi = (EntityLivingBase)entity;
							if(livi.isPotionActive(HbmPotion.taint)) {
								livi.removePotionEffect(HbmPotion.taint);
								livi.addPotionEffect(new PotionEffect(HbmPotion.mutation, 1 * 60 * 60 * 20, 0, false, true));
							} else {
								if(ArmorRegistry.hasProtection(livi, EntityEquipmentSlot.HEAD, HazardClass.BACTERIA)){
									ArmorUtil.damageGasMaskFilter(livi, 1);
								}else{
									entity.attackEntityFrom(ModDamageSource.cloud, 3);
								}
							}
						}
					}
				}
			}
		}

		bombStartStrength = (int) f;
	}

	/**
	 * Sets all flammable blocks on fire
	 *
	 * @param world
	 * @param detonator
	 * @param bound
	 */
	public static void flameDeath(World world, Entity detonator, BlockPos pos, int bound) {
        if(!CompatibilityConfig.isWarDim(world)) return;
        MutableBlockPos mPosUp = new BlockPos.MutableBlockPos();
        
        forEachBlockInSphere(world, detonator, pos.getX(), pos.getY(), pos.getZ(), bound, mPos -> {
            mPosUp.setPos(mPos.getX(), mPos.getY() + 1, mPos.getZ());
            if(world.getBlockState(mPos).getBlock().isFlammable(world, mPos, EnumFacing.UP) && world.getBlockState(mPosUp).getBlock() == Blocks.AIR) {
                world.setBlockState(mPosUp, Blocks.FIRE.getDefaultState());
            }
        });
    }

	/**
	 * Sets all blocks on fire
	 *
	 * @param world
	 * @param detonator
	 * @param bound
	 */
	public static void burn(World world, Entity detonator, BlockPos pos, int bound) {
        if(!CompatibilityConfig.isWarDim(world)) return;
        MutableBlockPos mPosUp = new BlockPos.MutableBlockPos();
        
        forEachBlockInSphere(world, detonator, pos.getX(), pos.getY(), pos.getZ(), bound, mPos -> {
            mPosUp.setPos(mPos.getX(), mPos.getY() + 1, mPos.getZ());
            IBlockState upState = world.getBlockState(mPosUp);
            if((upState.getBlock() == Blocks.AIR || upState.getBlock() == Blocks.SNOW_LAYER) && world.getBlockState(mPos).getBlock() != Blocks.AIR) {
                world.setBlockState(mPosUp, Blocks.FIRE.getDefaultState());
            }
        });
    }

	public static void spawnChlorine(World world, double x, double y, double z, int count, double speed, int type) {
        if(!CompatibilityConfig.isWarDim(world)) return;
        for(int i = 0; i < count; i++) {
            NBTTagCompound data = new NBTTagCompound();
            data.setDouble("moX", rand.nextGaussian() * speed);
            data.setDouble("moY", rand.nextGaussian() * speed);
            data.setDouble("moZ", rand.nextGaussian() * speed);
            
            String particleType = switch (type) {
                case 0 -> "chlorinefx";
                case 1 -> "cloudfx";
                case 2 -> "pinkcloudfx";
                default -> "orangefx";
            };
            
            data.setString("type", particleType);
            PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z), new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, 128));
        }
    }
	
	// Alcater: pc for pinkCouldPoisoning
	public static void pc(World world, int x, int y, int z, int bombStartStrength) {
		if(!CompatibilityConfig.isWarDim(world)){
			return;
		}
		float f = bombStartStrength;
		int i;
		int j;
		int k;
		double d5;
		double d6;
		double d7;
		double wat = bombStartStrength * 2;

		bombStartStrength *= 2.0F;
		i = MathHelper.floor(x - wat - 1.0D);
		j = MathHelper.floor(x + wat + 1.0D);
		k = MathHelper.floor(y - wat - 1.0D);
		int i2 = MathHelper.floor(y + wat + 1.0D);
		int l = MathHelper.floor(z - wat - 1.0D);
		int j2 = MathHelper.floor(z + wat + 1.0D);
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(i, k, l, j, i2, j2));

		for(int i1 = 0; i1 < list.size(); ++i1) {
			Entity entity = (Entity) list.get(i1);
			double d4 = entity.getDistance(x, y, z) / bombStartStrength;

			if(d4 <= 1.0D) {
				d5 = entity.posX - x;
				d6 = entity.posY + entity.getEyeHeight() - y;
				d7 = entity.posZ - z;
				double d9 = MathHelper.sqrt(d5 * d5 + d6 * d6 + d7 * d7);
				if(d9 < wat) {

					if(entity instanceof EntityPlayer) {
						if(!ArmorRegistry.hasProtection((EntityPlayer) entity, EntityEquipmentSlot.HEAD, HazardClass.GAS_BLISTERING)){
							ArmorUtil.damageSuit((EntityPlayer) entity, 0, 25);
							ArmorUtil.damageSuit((EntityPlayer) entity, 1, 25);
							ArmorUtil.damageSuit((EntityPlayer) entity, 2, 25);
							ArmorUtil.damageSuit((EntityPlayer) entity, 3, 25);
						}
					}
					if(entity instanceof EntityLivingBase){
						if(ArmorRegistry.hasAllProtection((EntityLivingBase) entity, EntityEquipmentSlot.HEAD, HazardClass.BACTERIA, HazardClass.SAND)){
							ArmorUtil.damageGasMaskFilter((EntityLivingBase) entity, 2);
						}else{
							entity.attackEntityFrom(ModDamageSource.pc, 5);
						}
					}
				}
			}
		}

		bombStartStrength = (int) f;
	}

	//Alcater: used by grenades and Chlorine seal gas blocks
	public static void poison(World world, int x, int y, int z, int bombStartStrength) {
		if(!CompatibilityConfig.isWarDim(world)){
			return;
		}
		float f = bombStartStrength;
		int i;
		int j;
		int k;
		double d5;
		double d6;
		double d7;
		double wat = bombStartStrength * 2;

		bombStartStrength *= 2.0F;
		i = MathHelper.floor(x - wat - 1.0D);
		j = MathHelper.floor(x + wat + 1.0D);
		k = MathHelper.floor(y - wat - 1.0D);
		int i2 = MathHelper.floor(y + wat + 1.0D);
		int l = MathHelper.floor(z - wat - 1.0D);
		int j2 = MathHelper.floor(z + wat + 1.0D);
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(i, k, l, j, i2, j2));

		for(int i1 = 0; i1 < list.size(); ++i1) {
			Entity entity = (Entity) list.get(i1);
			double d4 = entity.getDistance(x, y, z) / bombStartStrength;

			if(d4 <= 1.0D) {
				d5 = entity.posX - x;
				d6 = entity.posY + entity.getEyeHeight() - y;
				d7 = entity.posZ - z;
				double d9 = MathHelper.sqrt(d5 * d5 + d6 * d6 + d7 * d7);
				if(d9 < wat) {
					if(!(entity instanceof EntityLivingBase))
						continue;
					
					EntityLivingBase entityLiving = (EntityLivingBase) entity;
					if(ArmorRegistry.hasAllProtection(entityLiving, EntityEquipmentSlot.HEAD, HazardClass.NERVE_AGENT)) {
						ArmorUtil.damageGasMaskFilter(entityLiving, 1);
					} else {
						entityLiving.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 5 * 20, 0));
						entityLiving.addPotionEffect(new PotionEffect(MobEffects.POISON, 20 * 20, 2));
						entityLiving.addPotionEffect(new PotionEffect(MobEffects.WITHER, 1 * 20, 1));
						entityLiving.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 30 * 20, 1));
						entityLiving.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 30 * 20, 2));
					}
				}
			}
		}

		bombStartStrength = (int) f;
	}


	public static void cluster(World world, int x, int y, int z, int count, double gravity) {
        double d1, d2, d3;
        EntityRocket fragment;

        for (int i = 0; i < count; i++) {
            d1 = rand.nextDouble();
            d2 = rand.nextDouble();
            d3 = rand.nextDouble();

            if (rand.nextInt(2) == 0) {
                d1 *= -1;
            }

            if (rand.nextInt(2) == 0) {
                d3 *= -1;
            }

            fragment = new EntityRocket(world, x, y, z, d1, d2, d3, 0.0125D);

            world.spawnEntity(fragment);
        }
	}

	public static void miniMirv(World world, double x, double y, double z) {
		double modifier = 1.25;
		double zeta = Math.sqrt(2) / 2;
		EntityMiniNuke mirv1 = new EntityMiniNuke(world);
		EntityMiniNuke mirv2 = new EntityMiniNuke(world);
		EntityMiniNuke mirv3 = new EntityMiniNuke(world);
		EntityMiniNuke mirv4 = new EntityMiniNuke(world);
		double vx1 = 1;
		double vy1 = rand.nextDouble() * -1;
		double vz1 = 0;

		mirv1.posX = x;
		mirv1.posY = y;
		mirv1.posZ = z;
		mirv1.motionY = vy1;
		mirv2.posX = x;
		mirv2.posY = y;
		mirv2.posZ = z;
		mirv2.motionY = vy1;
		mirv3.posX = x;
		mirv3.posY = y;
		mirv3.posZ = z;
		mirv3.motionY = vy1;
		mirv4.posX = x;
		mirv4.posY = y;
		mirv4.posZ = z;
		mirv4.motionY = vy1;

		mirv1.motionX = vx1 * modifier;
		mirv1.motionZ = vz1 * modifier;
		world.spawnEntity(mirv1);

		mirv2.motionX = -vz1 * modifier;
		mirv2.motionZ = vx1 * modifier;
		world.spawnEntity(mirv2);

		mirv3.motionX = -vx1 * modifier;
		mirv3.motionZ = -vz1 * modifier;
		world.spawnEntity(mirv3);

		mirv4.motionX = vz1 * modifier;
		mirv4.motionZ = -vx1 * modifier;
		world.spawnEntity(mirv4);

		EntityMiniNuke mirv5 = new EntityMiniNuke(world);
		EntityMiniNuke mirv6 = new EntityMiniNuke(world);
		EntityMiniNuke mirv7 = new EntityMiniNuke(world);
		EntityMiniNuke mirv8 = new EntityMiniNuke(world);
		// double vx2 = vx1 < theta ? vx1 + theta : vx1 - theta;
		// double vy2 = vy1;
		// double vz2 = Math.sqrt(Math.pow(1, 2) - Math.pow(vx2, 2));
		double vx2 = zeta;
		double vy2 = vy1;
		double vz2 = zeta;

		mirv5.posX = x;
		mirv5.posY = y;
		mirv5.posZ = z;
		mirv5.motionY = vy2;
		mirv6.posX = x;
		mirv6.posY = y;
		mirv6.posZ = z;
		mirv6.motionY = vy2;
		mirv7.posX = x;
		mirv7.posY = y;
		mirv7.posZ = z;
		mirv7.motionY = vy2;
		mirv8.posX = x;
		mirv8.posY = y;
		mirv8.posZ = z;
		mirv8.motionY = vy2;

		mirv5.motionX = vx2 * modifier;
		mirv5.motionZ = vz2 * modifier;
		world.spawnEntity(mirv5);

		mirv6.motionX = -vz2 * modifier;
		mirv6.motionZ = vx2 * modifier;
		world.spawnEntity(mirv6);

		mirv7.motionX = -vx2 * modifier;
		mirv7.motionZ = -vz2 * modifier;
		world.spawnEntity(mirv7);

		mirv8.motionX = vz2 * modifier;
		mirv8.motionZ = -vx2 * modifier;
		world.spawnEntity(mirv8);
	}

	public static void explodeZOMG(World world, int x, int y, int z, int bombStartStrength) {
		if(!CompatibilityConfig.isWarDim(world)) return;
		forEachBlockInSphere(world, null, x, y, z, bombStartStrength, pos -> {
			if(!(world.getBlockState(pos).getBlock().getExplosionResistance(null) > 2_000_000 && pos.getY() <= 0))
				world.setBlockToAir(pos);
		});
	}

	public static void frag(World world, int x, int y, int z, int count, boolean flame, Entity shooter) {

		double d1 = 0;
		double d2 = 0;
		double d3 = 0;
		EntityArrow fragment;

		for(int i = 0; i < count; i++) {
			d1 = rand.nextDouble();
			d2 = rand.nextDouble();
			d3 = rand.nextDouble();

			if(rand.nextInt(2) == 0) {
				d1 *= -1;
			}

			if(rand.nextInt(2) == 0) {
				d3 *= -1;
			}

			fragment = new EntityTippedArrow(world, x, y, z);

			fragment.motionX = d1;
			fragment.motionY = d2;
			fragment.motionZ = d3;
			fragment.shootingEntity = shooter;

			fragment.setIsCritical(true);
			if(flame) {
				fragment.setFire(1000);
			}

			fragment.setDamage(2.5);

			world.spawnEntity(fragment);
		}
	}

	public static void schrab(World world, int x, int y, int z, int count, int gravity) {

		double d1 = 0;
		double d2 = 0;
		double d3 = 0;
		EntitySchrab fragment;

		for(int i = 0; i < count; i++) {
			d1 = rand.nextDouble();
			d2 = rand.nextDouble();
			d3 = rand.nextDouble();

			if(rand.nextInt(2) == 0) {
				d1 *= -1;
			}

			if(rand.nextInt(2) == 0) {
				d3 *= -1;
			}

			fragment = new EntitySchrab(world, x, y, z, d1, d2, d3, 0.0125D);

			world.spawnEntity(fragment);
		}
	}

	@SuppressWarnings("deprecation")
	public static void pulse(World world, int x, int y, int z, int bombStartStrength) {
		if(!CompatibilityConfig.isWarDim(world)) return;
		forEachBlockInSphere(world, null, x, y, z, bombStartStrength, pos -> {
			if(world.getBlockState(pos).getBlock().getExplosionResistance(null) <= 70)
				pDestruction(world, pos.getX(), pos.getY(), pos.getZ());
        });
    }

	public static void pDestruction(World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		IBlockState state = world.getBlockState(pos);
		EntityFallingBlock entityfallingblock = new EntityFallingBlock(world, (double) ((float) x + 0.5F), (double) ((float) y + 0.5F), (double) ((float) z + 0.5F), state);
		world.spawnEntity(entityfallingblock);
	}

	public static void plasma(World world, int x, int y, int z, int radius) {
        if(!CompatibilityConfig.isWarDim(world)) return;
        int radiusSqHalf = (radius * radius) / 2;
        
        forEachBlockInSphere(world, null, x, y, z, radius, pos -> {
            if(world.rand.nextInt(radiusSqHalf / 2) > 0) { 
                IBlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                if(block.getExplosionResistance(null) > 0.1F) return;
                if(block != Blocks.BEDROCK && block != ModBlocks.statue_elb
                        && block != ModBlocks.statue_elb_g
                        && block != ModBlocks.statue_elb_w
                        && block != ModBlocks.statue_elb_f)
                    world.setBlockState(pos, ModBlocks.plasma.getDefaultState());
            }
        });
    }

	// Drillgon200: This method name irks me.
	public static void tauMeSinPi(World world, double x, double y, double z, int count, Entity shooter, EntityGrenadeTau tau) {

		double d1 = 0;
		double d2 = 0;
		double d3 = 0;
		EntityBullet fragment;

		if(shooter != null && shooter instanceof EntityPlayer)
			for(int i = 0; i < count; i++) {
				d1 = rand.nextDouble();
				d2 = rand.nextDouble();
				d3 = rand.nextDouble();

				if(rand.nextInt(2) == 0) {
					d1 *= -1;
				}

				if(rand.nextInt(2) == 0) {
					d2 *= -1;
				}

				if(rand.nextInt(2) == 0) {
					d3 *= -1;
				}

				if(rand.nextInt(5) == 0) {
					fragment = new EntityBullet(world, (EntityPlayer) shooter, 3.0F, 35, 45, false, "tauDay", tau);
					fragment.setDamage(rand.nextInt(301) + 100);
				} else {
					fragment = new EntityBullet(world, (EntityPlayer) shooter, 3.0F, 35, 45, false, "eyyOk", tau);
					fragment.setDamage(rand.nextInt(11) + 35);
				}

				fragment.motionX = d1 * 5;
				fragment.motionY = d2 * 5;
				fragment.motionZ = d3 * 5;
				fragment.shootingEntity = shooter;

				fragment.setIsCritical(true);

				world.spawnEntity(fragment);
			}
	}

	// Drillgon200: You know what? I'm changing this one.
	public static void zomg(World world, double x, double y, double z, int count, Entity shooter, EntityGrenadeZOMG zomg) {

		double d1 = 0;
		double d2 = 0;
		double d3 = 0;

		// if (shooter != null && shooter instanceof EntityPlayer)
		for(int i = 0; i < count; i++) {
			d1 = rand.nextDouble();
			d2 = rand.nextDouble();
			d3 = rand.nextDouble();

			if(rand.nextInt(2) == 0) {
				d1 *= -1;
			}

			if(rand.nextInt(2) == 0) {
				d2 *= -1;
			}

			if(rand.nextInt(2) == 0) {
				d3 *= -1;
			}

			EntityRainbow entityZomg = new EntityRainbow(world, (EntityPlayer) shooter, 1F, 10000, 100000, zomg);

			entityZomg.motionX = d1;// * 5;
			entityZomg.motionY = d2;// * 5;
			entityZomg.motionZ = d3;// * 5;
			entityZomg.shootingEntity = shooter;

			world.spawnEntity(entityZomg);
			world.playSound(null, zomg.posX, zomg.posY, zomg.posZ, HBMSoundHandler.zomgShoot, SoundCategory.AMBIENT, 10.0F, 0.8F + (rand.nextFloat() * 0.4F));
		}
	}

	public static void spawnVolley(World world, double x, double y, double z, int count, double speed) {
		if(!CompatibilityConfig.isWarDim(world)){
			return;
		}
		for(int i = 0; i < count; i++) {

			NBTTagCompound data = new NBTTagCompound();
			data.setDouble("moX", rand.nextGaussian() * speed);
			data.setDouble("moY", rand.nextGaussian() * speed * 7.5D);
			data.setDouble("moZ", rand.nextGaussian() * speed);

			data.setString("type", "orangefx");
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z), new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, 50));
		}
	}

	public static void floater(World world, Entity detonator, BlockPos pos, int radi, int height) {
		floater(world, detonator, pos.getX(), pos.getY(), pos.getZ(), radi, height);
	}

	public static void floater(World world, Entity detonator, int x, int y, int z, int radi, int height) {
        if(!CompatibilityConfig.isWarDim(world)) return;
        forEachBlockInSphere(world, detonator, x, y, z, radi, pos -> {
            IBlockState save = world.getBlockState(pos);
            world.setBlockToAir(pos);
            if(save.getBlock() != Blocks.AIR) {
                world.setBlockState(new BlockPos(pos.getX(), pos.getY() + height, pos.getZ()), save);
            }
        });
    }

	public static void move(World world, BlockPos pos, int radius, int a, int b, int c) {
		move(world, pos.getX(), pos.getY(), pos.getZ(), radius, a, b, c);
	}

	public static void move(World world, int x, int y, int z, int radius, int a, int b, int c) {
		float f = radius;
		int i;
		int j;
		int k;
		double d5;
		double d6;
		double d7;
		double wat = radius;
		int rand = 0;

		radius *= 2.0F;
		i = MathHelper.floor(x - wat - 1.0D);
		j = MathHelper.floor(x + wat + 1.0D);
		k = MathHelper.floor(y - wat - 1.0D);
		int i2 = MathHelper.floor(y + wat + 1.0D);
		int l = MathHelper.floor(z - wat - 1.0D);
		int j2 = MathHelper.floor(z + wat + 1.0D);
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(i, k, l, j, i2, j2));

		for(int i1 = 0; i1 < list.size(); ++i1) {
			Entity entity = (Entity) list.get(i1);
			double d4 = entity.getDistance(x, y, z) / radius;

			if(d4 <= 1.0D) {
				d5 = entity.posX - x;
				d6 = entity.posY + entity.getEyeHeight() - y;
				d7 = entity.posZ - z;
				if(entity instanceof EntityLiving && !(entity instanceof EntitySheep)) {
					rand = random.nextInt(2);
					if(rand == 0) {
						((EntityLiving) entity).setCustomNameTag("Dinnerbone");
					} else {
						((EntityLiving) entity).setCustomNameTag("Grumm");
					}
				}

				if(entity instanceof EntitySheep) {
					((EntityLiving) entity).setCustomNameTag("jeb_");
				}

				double d9 = MathHelper.sqrt(d5 * d5 + d6 * d6 + d7 * d7);
				if(d9 < wat) {
					entity.setPosition(entity.posX += a, entity.posY += b, entity.posZ += c);
				}
			}
		}

		radius = (int) f;
	}

	public static void levelDown(World world, int x, int y, int z, int radius) {
		if(!CompatibilityConfig.isWarDim(world)){
			return;
		}
		MutableBlockPos pos = new BlockPos.MutableBlockPos();
		if(!world.isRemote)
			for(int i = x - radius; i <= x + radius; i++)
				for(int j = z - radius; j <= z + radius; j++) {

					IBlockState b = world.getBlockState(pos.setPos(i, y, j));
					float k = b.getBlockHardness(world, pos.setPos(i, y, j));
					if(k < 6000 && k > 0 && b.getBlock() != Blocks.AIR) {

						EntityRubble rubble = new EntityRubble(world);
						rubble.posX = i + 0.5F;
						rubble.posY = y;
						rubble.posZ = j + 0.5F;

						rubble.motionY = 0.025F * 10 + 0.15F;
						rubble.setMetaBasedOnBlock(b.getBlock(), b.getBlock().getMetaFromState(b));

						world.spawnEntity(rubble);

						world.setBlockState(pos.setPos(i, y, j), Blocks.AIR.getDefaultState());
					}
				}
	}

	public static void decontaminate(World world, BlockPos pos) {
		// Bridged
		// if (!world.isRemote) {

		Random random = new Random();
		IBlockState b = world.getBlockState(pos);
		Block bblock = b.getBlock();

		if(bblock == ModBlocks.waste_earth && random.nextInt(3) != 0) {
			world.setBlockState(pos, Blocks.GRASS.getDefaultState());
		}
		
		else if(bblock == ModBlocks.waste_grass_tall && random.nextInt(3) != 0) {
			world.setBlockState(pos, Blocks.TALLGRASS.getDefaultState());
		}

		else if(bblock == ModBlocks.waste_mycelium && random.nextInt(5) == 0) {
			world.setBlockState(pos, Blocks.MYCELIUM.getDefaultState());
		}

		else if(bblock == ModBlocks.waste_leaves && random.nextInt(5) != 0) {
			world.setBlockState(pos, Blocks.LEAVES.getDefaultState());
		}

		else if(bblock == ModBlocks.waste_trinitite && random.nextInt(3) == 0) {
			world.setBlockState(pos, Blocks.SAND.getDefaultState());
		}

		else if(bblock == ModBlocks.waste_trinitite_red && random.nextInt(3) == 0) {
			world.setBlockState(pos, Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND));
		}

		else if(bblock == ModBlocks.waste_log && random.nextInt(3) != 0) {
			world.setBlockState(pos, Blocks.LOG.getDefaultState().withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.fromFacingAxis(world.getBlockState(pos).getValue(BlockLog.AXIS))));
		}

		else if(bblock == ModBlocks.waste_planks && random.nextInt(3) != 0) {
			world.setBlockState(pos, Blocks.PLANKS.getDefaultState());
		}

		else if(bblock == ModBlocks.block_trinitite && random.nextInt(10) == 0) {
			world.setBlockState(pos, ModBlocks.block_lead.getDefaultState());
		}

		else if(bblock == ModBlocks.block_waste && random.nextInt(10) == 0) {
			world.setBlockState(pos, ModBlocks.block_lead.getDefaultState());
		}

		else if(bblock == ModBlocks.sellafield && random.nextInt(10) == 0) {
			world.setBlockState(pos, ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 4));
		}

		else if(bblock == ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 4) && random.nextInt(5) == 0) {
			world.setBlockState(pos, ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 3));
		}

		else if(bblock == ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 3) && random.nextInt(5) == 0) {
			world.setBlockState(pos, ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 2));
		}

		else if(bblock == ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 2) && random.nextInt(5) == 0) {
			world.setBlockState(pos, ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 1));
		}

		else if(bblock == ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 1) && random.nextInt(5) == 0) {
			world.setBlockState(pos, ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 0));
		}

		else if(bblock == ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 0) && random.nextInt(5) == 0) {
			world.setBlockState(pos, ModBlocks.sellafield_slaked.getStateFromMeta(world.rand.nextInt(4)));
		}

		else if(bblock == ModBlocks.sellafield_slaked && random.nextInt(5) == 0) {
			world.setBlockState(pos, Blocks.STONE.getDefaultState());
		}

	}
	
	public static void hardenVirus(World world, int x, int y, int z, int bombStartStrength) {
        if(!CompatibilityConfig.isWarDim(world)) return;
        forEachBlockInSphere(world, null, x, y, z, bombStartStrength, pos -> {
            if (world.getBlockState(pos).getBlock() == ModBlocks.crystal_virus)
                world.setBlockState(pos, ModBlocks.crystal_hardened.getDefaultState());
        });
    }

	public static void spreadVirus(World world, int x, int y, int z, int bombStartStrength) {
        if(!CompatibilityConfig.isWarDim(world)) return;
        forEachBlockInSphere(world, null, x, y, z, bombStartStrength, pos -> {
            if (rand.nextInt(15) == 0 && world.getBlockState(pos).getBlock() != Blocks.AIR)
                world.setBlockState(pos, ModBlocks.cheater_virus_seed.getDefaultState());
        });
    }
}
