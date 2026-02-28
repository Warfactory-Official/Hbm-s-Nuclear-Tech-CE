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

public class ExplosionChaos {

	/** Single shared Random instance — no per-method allocations. */
	private static final Random rand = new Random();

	/**
	 * Iterates over every block inside a sphere of the given radius using a
	 * proper r² check, avoiding sqrt in the inner-most loop.
	 *
	 * <p>The MutableBlockPos passed to {@code action} is reused on every call;
	 * callers must NOT store it between iterations.
	 */
	private static void forEachBlockInSphere(World world, Entity detonator,
			int x, int y, int z, int radius, Consumer<BlockPos.MutableBlockPos> action) {

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int radiusSq = radius * radius;

		for (int yy = -radius; yy <= radius; yy++) {
			int currentY = y + yy;
			if (currentY < 0 || currentY > 255) continue;

			int ySq = yy * yy;
			if (ySq > radiusSq) continue;

			int xzRadiusSq = radiusSq - ySq;
			int xzRadius = (int) Math.sqrt(xzRadiusSq);

			for (int xx = -xzRadius; xx <= xzRadius; xx++) {
				int xSq = xx * xx;
				int rem = xzRadiusSq - xSq;
				if (rem < 0) continue;

				int zRadius = (int) Math.sqrt(rem);

				for (int zz = -zRadius; zz <= zRadius; zz++) {
					action.accept(pos.setPos(x + xx, currentY, z + zz));
				}
			}
		}
	}

	public static void explode(World world, Entity detonator, int x, int y, int z, int bombStartStrength) {
		if (!CompatibilityConfig.isWarDim(world)) return;
		forEachBlockInSphere(world, detonator, x, y, z, bombStartStrength,
				pos -> destruction(world, detonator, pos));
	}

	private static void destruction(World world, Entity detonator, BlockPos pos) {
		Block b = world.getBlockState(pos).getBlock();
		if (b == Blocks.BEDROCK
				|| b == ModBlocks.reinforced_brick
				|| b == ModBlocks.reinforced_sand
				|| b == ModBlocks.reinforced_glass
				|| b == ModBlocks.reinforced_lamp_on
				|| b == ModBlocks.reinforced_lamp_off
				|| b.getExplosionResistance(null) > 2_000_000) {
			return; // indestructible
		}
		world.setBlockToAir(pos);
	}

	public static void spawnExplosion(World world, Entity detonator, int x, int y, int z, int bound) {
		if (!CompatibilityConfig.isWarDim(world)) return;

		// Signs for all 8 octants
		final int[][] signs = {
			{ 1,  1,  1}, { 1, -1,  1}, {-1,  1,  1},
			{-1, -1,  1}, {-1,  1, -1}, { 1, -1, -1},
			{-1, -1, -1}, { 1,  1, -1}
		};

		for (int i = 0; i < 25; i++) {
			int[] s = signs[i & 7]; // cycle through octants
			world.createExplosion(detonator,
					x + s[0] * rand.nextInt(bound),
					y + s[1] * rand.nextInt(bound),
					z + s[2] * rand.nextInt(bound),
					10.0F, true);
		}
	}

	// Gas / poison area effects
	//   c()  -> cloudPoisoning  (green blistering cloud)
	//   pc() -> pinkCloudPoisoning
	//   poison() -> nerve agent cloud

	@Deprecated
	public static void c(World world, int x, int y, int z, int bombStartStrength) {
		cloudPoisoning(world, x, y, z, bombStartStrength);
	}

	/** Green blistering cloud — damages suits and applies mutation. */
	public static void cloudPoisoning(World world, int x, int y, int z, int bombStartStrength) {
		if (!CompatibilityConfig.isWarDim(world)) return;

		double radius = bombStartStrength * 2.0;
		double radiusSq = radius * radius;

		for (Entity entity : getEntitiesInRadius(world, x, y, z, radius)) {
			if (distanceSq(entity, x, y, z) > radiusSq) continue;

			if (entity instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) entity;
				if (!ArmorRegistry.hasProtection(player, EntityEquipmentSlot.HEAD, HazardClass.GAS_BLISTERING)) {
					ArmorUtil.damageSuit(player, 0, 5);
					ArmorUtil.damageSuit(player, 1, 5);
					ArmorUtil.damageSuit(player, 2, 5);
					ArmorUtil.damageSuit(player, 3, 5);
				}
			}

			if (entity instanceof EntityPlayer && ArmorUtil.checkForHazmat((EntityPlayer) entity)) continue;

			if (entity instanceof EntityLivingBase) {
				EntityLivingBase living = (EntityLivingBase) entity;
				if (living.isPotionActive(HbmPotion.taint)) {
					living.removePotionEffect(HbmPotion.taint);
					living.addPotionEffect(new PotionEffect(HbmPotion.mutation, 1 * 60 * 60 * 20, 0, false, true));
				} else if (ArmorRegistry.hasProtection(living, EntityEquipmentSlot.HEAD, HazardClass.BACTERIA)) {
					ArmorUtil.damageGasMaskFilter(living, 1);
				} else {
					entity.attackEntityFrom(ModDamageSource.cloud, 3);
				}
			}
		}
	}

	/** @deprecated Use the named alias {@link #pinkCloudPoisoning} */
	@Deprecated
	public static void pc(World world, int x, int y, int z, int bombStartStrength) {
		pinkCloudPoisoning(world, x, y, z, bombStartStrength);
	}

	/** Pink blistering cloud — heavier suit damage and bacteria check. */
	public static void pinkCloudPoisoning(World world, int x, int y, int z, int bombStartStrength) {
		if (!CompatibilityConfig.isWarDim(world)) return;

		double radius = bombStartStrength * 2.0;
		double radiusSq = radius * radius;

		for (Entity entity : getEntitiesInRadius(world, x, y, z, radius)) {
			if (distanceSq(entity, x, y, z) > radiusSq) continue;

			if (entity instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) entity;
				if (!ArmorRegistry.hasProtection(player, EntityEquipmentSlot.HEAD, HazardClass.GAS_BLISTERING)) {
					ArmorUtil.damageSuit(player, 0, 25);
					ArmorUtil.damageSuit(player, 1, 25);
					ArmorUtil.damageSuit(player, 2, 25);
					ArmorUtil.damageSuit(player, 3, 25);
				}
			}

			if (entity instanceof EntityLivingBase) {
				EntityLivingBase living = (EntityLivingBase) entity;
				if (ArmorRegistry.hasAllProtection(living, EntityEquipmentSlot.HEAD, HazardClass.BACTERIA, HazardClass.SAND)) {
					ArmorUtil.damageGasMaskFilter(living, 2);
				} else {
					entity.attackEntityFrom(ModDamageSource.pc, 5);
				}
			}
		}
	}

	/** Nerve-agent cloud — blindness, poison, wither, slowness, fatigue. */
	public static void poison(World world, int x, int y, int z, int bombStartStrength) {
		if (!CompatibilityConfig.isWarDim(world)) return;

		double radius = bombStartStrength * 2.0;
		double radiusSq = radius * radius;

		for (Entity entity : getEntitiesInRadius(world, x, y, z, radius)) {
			if (distanceSq(entity, x, y, z) > radiusSq) continue;
			if (!(entity instanceof EntityLivingBase)) continue;

			EntityLivingBase living = (EntityLivingBase) entity;
			if (ArmorRegistry.hasAllProtection(living, EntityEquipmentSlot.HEAD, HazardClass.NERVE_AGENT)) {
				ArmorUtil.damageGasMaskFilter(living, 1);
			} else {
				living.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS,      5 * 20, 0));
				living.addPotionEffect(new PotionEffect(MobEffects.POISON,        20 * 20, 2));
				living.addPotionEffect(new PotionEffect(MobEffects.WITHER,         1 * 20, 1));
				living.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS,      30 * 20, 1));
				living.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 30 * 20, 2));
			}
		}
	}

	/** EMP / gravity flip — renames mobs and teleports entities in the radius. */
	public static void move(World world, BlockPos pos, int radius, int a, int b, int c) {
		move(world, pos.getX(), pos.getY(), pos.getZ(), radius, a, b, c);
	}

	public static void move(World world, int x, int y, int z, int radius, int a, int b, int c) {
		double radiusDouble = radius;
		double radiusSq = radiusDouble * radiusDouble;

		for (Entity entity : getEntitiesInRadius(world, x, y, z, radiusDouble)) {
			if (distanceSq(entity, x, y, z) > radiusSq) continue;

			if (entity instanceof EntityLiving && !(entity instanceof EntitySheep)) {
				((EntityLiving) entity).setCustomNameTag(rand.nextInt(2) == 0 ? "Dinnerbone" : "Grumm");
			}
			if (entity instanceof EntitySheep) {
				((EntityLiving) entity).setCustomNameTag("jeb_");
			}

			entity.setPosition(entity.posX + a, entity.posY + b, entity.posZ + c);
		}
	}


	/**
	 * Sets flammable blocks (top face) on fire within the sphere.
	 */
	public static void flameDeath(World world, Entity detonator, BlockPos pos, int bound) {
		if (!CompatibilityConfig.isWarDim(world)) return;
		MutableBlockPos mPosUp = new BlockPos.MutableBlockPos();

		forEachBlockInSphere(world, detonator, pos.getX(), pos.getY(), pos.getZ(), bound, mPos -> {
			mPosUp.setPos(mPos.getX(), mPos.getY() + 1, mPos.getZ());
			if (world.getBlockState(mPos).getBlock().isFlammable(world, mPos, EnumFacing.UP)
					&& world.getBlockState(mPosUp).getBlock() == Blocks.AIR) {
				world.setBlockState(mPosUp, Blocks.FIRE.getDefaultState());
			}
		});
	}

	/**
	 * Sets every solid block on fire (puts fire on the block above) within the sphere.
	 */
	public static void burn(World world, Entity detonator, BlockPos pos, int bound) {
		if (!CompatibilityConfig.isWarDim(world)) return;
		MutableBlockPos mPosUp = new BlockPos.MutableBlockPos();

		forEachBlockInSphere(world, detonator, pos.getX(), pos.getY(), pos.getZ(), bound, mPos -> {
			mPosUp.setPos(mPos.getX(), mPos.getY() + 1, mPos.getZ());
			IBlockState upState = world.getBlockState(mPosUp);
			if ((upState.getBlock() == Blocks.AIR || upState.getBlock() == Blocks.SNOW_LAYER)
					&& world.getBlockState(mPos).getBlock() != Blocks.AIR) {
				world.setBlockState(mPosUp, Blocks.FIRE.getDefaultState());
			}
		});
	}


	public static void spawnChlorine(World world, double x, double y, double z, int count, double speed, int type) {
		if (!CompatibilityConfig.isWarDim(world)) return;

		final String particleType;
		switch (type) {
			case 0:  particleType = "chlorinefx";  break;
			case 1:  particleType = "cloudfx";     break;
			case 2:  particleType = "pinkcloudfx"; break;
			default: particleType = "orangefx";    break;
		}

		NetworkRegistry.TargetPoint tp =
				new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, 128);

		for (int i = 0; i < count; i++) {
			NBTTagCompound data = new NBTTagCompound();
			data.setDouble("moX", rand.nextGaussian() * speed);
			data.setDouble("moY", rand.nextGaussian() * speed);
			data.setDouble("moZ", rand.nextGaussian() * speed);
			data.setString("type", particleType);
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z), tp);
		}
	}


	public static void cluster(World world, int x, int y, int z, int count, double gravity) {
		for (int i = 0; i < count; i++) {
			double d1 = (rand.nextInt(2) == 0 ? -1 : 1) * rand.nextDouble();
			double d2 = rand.nextDouble();
			double d3 = (rand.nextInt(2) == 0 ? -1 : 1) * rand.nextDouble();
			world.spawnEntity(new EntityRocket(world, x, y, z, d1, d2, d3, 0.0125D));
		}
	}

	public static void miniMirv(World world, double x, double y, double z) {
		final double modifier = 1.25;
		final double zeta = Math.sqrt(2) / 2; // = sin(45°)
		final double vy = -rand.nextDouble();

		// Two rings of 4 warheads — axial and diagonal — spawn 8 total.
		// Ring 1: aligned to axes (vx, vz) ∈ {(1,0),(0,1),(-1,0),(0,-1)}
		spawnMirvRing(world, x, y, z, vy, modifier, 1, 0, zeta);
	}

	/**
	 * Spawns a ring of 4 {@link EntityMiniNuke} entities arranged at 90° intervals.
	 * Called twice by {@link #miniMirv}: once for the axis-aligned ring (scale=1,
	 * diag=0) and once for the diagonal ring (scale=zeta, diag=zeta).
	 */
	private static void spawnMirvRing(World world, double x, double y, double z,
			double vy, double modifier, double scale, double diag, double zeta) {

		double[][] motions = {
			{ scale,  diag},
			{-diag,  scale},
			{-scale, -diag},
			{ diag, -scale}
		};

		for (double[] m : motions) {
			EntityMiniNuke mirv = new EntityMiniNuke(world);
			mirv.setPosition(x, y, z);
			mirv.motionX = m[0] * modifier;
			mirv.motionY = vy;
			mirv.motionZ = m[1] * modifier;
			world.spawnEntity(mirv);
		}

		// Diagonal ring
		if (diag == 0) {
			double[][] diagMotions = {
				{ zeta,  zeta},
				{-zeta,  zeta},
				{-zeta, -zeta},
				{ zeta, -zeta}
			};
			for (double[] m : diagMotions) {
				EntityMiniNuke mirv = new EntityMiniNuke(world);
				mirv.setPosition(x, y, z);
				mirv.motionX = m[0] * modifier;
				mirv.motionY = vy;
				mirv.motionZ = m[1] * modifier;
				world.spawnEntity(mirv);
			}
		}
	}

	public static void explodeZOMG(World world, int x, int y, int z, int bombStartStrength) {
		if (!CompatibilityConfig.isWarDim(world)) return;
		forEachBlockInSphere(world, null, x, y, z, bombStartStrength, pos -> {
			if (!(world.getBlockState(pos).getBlock().getExplosionResistance(null) > 2_000_000 && pos.getY() <= 0))
				world.setBlockToAir(pos);
		});
	}

	public static void frag(World world, int x, int y, int z, int count, boolean flame, Entity shooter) {
		for (int i = 0; i < count; i++) {
			double d1 = (rand.nextInt(2) == 0 ? -1 : 1) * rand.nextDouble();
			double d2 = rand.nextDouble();
			double d3 = (rand.nextInt(2) == 0 ? -1 : 1) * rand.nextDouble();

			EntityArrow fragment = new EntityTippedArrow(world, x, y, z);
			fragment.motionX = d1;
			fragment.motionY = d2;
			fragment.motionZ = d3;
			fragment.shootingEntity = shooter;
			fragment.setIsCritical(true);
			if (flame) fragment.setFire(1000);
			fragment.setDamage(2.5);
			world.spawnEntity(fragment);
		}
	}

	public static void schrab(World world, int x, int y, int z, int count, int gravity) {
		for (int i = 0; i < count; i++) {
			double d1 = (rand.nextInt(2) == 0 ? -1 : 1) * rand.nextDouble();
			double d2 = rand.nextDouble();
			double d3 = (rand.nextInt(2) == 0 ? -1 : 1) * rand.nextDouble();
			world.spawnEntity(new EntitySchrab(world, x, y, z, d1, d2, d3, 0.0125D));
		}
	}


	@SuppressWarnings("deprecation")
	public static void pulse(World world, int x, int y, int z, int bombStartStrength) {
		if (!CompatibilityConfig.isWarDim(world)) return;
		forEachBlockInSphere(world, null, x, y, z, bombStartStrength, pos -> {
			if (world.getBlockState(pos).getBlock().getExplosionResistance(null) <= 70)
				pDestruction(world, pos.getX(), pos.getY(), pos.getZ());
		});
	}

	public static void pDestruction(World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		IBlockState state = world.getBlockState(pos);
		world.spawnEntity(new EntityFallingBlock(world,
				x + 0.5D, y + 0.5D, z + 0.5D, state));
	}

	public static void plasma(World world, int x, int y, int z, int radius) {
		if (!CompatibilityConfig.isWarDim(world)) return;
		int threshold = Math.max(1, (radius * radius) / 4);

		forEachBlockInSphere(world, null, x, y, z, radius, pos -> {
			if (world.rand.nextInt(threshold) > 0) return;
			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			if (block.getExplosionResistance(null) > 0.1F) return;
			if (block != Blocks.BEDROCK
					&& block != ModBlocks.statue_elb
					&& block != ModBlocks.statue_elb_g
					&& block != ModBlocks.statue_elb_w
					&& block != ModBlocks.statue_elb_f) {
				world.setBlockState(pos, ModBlocks.plasma.getDefaultState());
			}
		});
	}


	// Drillgon200: This method name irks me.
	public static void tauMeSinPi(World world, double x, double y, double z,
			int count, Entity shooter, EntityGrenadeTau tau) {

		if (!(shooter instanceof EntityPlayer)) return;
		EntityPlayer player = (EntityPlayer) shooter;

		for (int i = 0; i < count; i++) {
			double d1 = (rand.nextInt(2) == 0 ? -1 : 1) * rand.nextDouble();
			double d2 = (rand.nextInt(2) == 0 ? -1 : 1) * rand.nextDouble();
			double d3 = (rand.nextInt(2) == 0 ? -1 : 1) * rand.nextDouble();

			EntityBullet fragment;
			if (rand.nextInt(5) == 0) {
				fragment = new EntityBullet(world, player, 3.0F, 35, 45, false, "tauDay", tau);
				fragment.setDamage(rand.nextInt(301) + 100);
			} else {
				fragment = new EntityBullet(world, player, 3.0F, 35, 45, false, "eyyOk", tau);
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
	public static void zomg(World world, double x, double y, double z,
			int count, Entity shooter, EntityGrenadeZOMG zomgEntity) {

		NetworkRegistry.TargetPoint tp = new NetworkRegistry.TargetPoint(
				world.provider.getDimension(), x, y, z, 256);

		for (int i = 0; i < count; i++) {
			double d1 = (rand.nextInt(2) == 0 ? -1 : 1) * rand.nextDouble();
			double d2 = (rand.nextInt(2) == 0 ? -1 : 1) * rand.nextDouble();
			double d3 = (rand.nextInt(2) == 0 ? -1 : 1) * rand.nextDouble();

			EntityRainbow entityZomg = new EntityRainbow(world, (EntityPlayer) shooter, 1F, 10000, 100000, zomgEntity);
			entityZomg.motionX = d1;
			entityZomg.motionY = d2;
			entityZomg.motionZ = d3;
			entityZomg.shootingEntity = shooter;
			world.spawnEntity(entityZomg);

			world.playSound(null, zomgEntity.posX, zomgEntity.posY, zomgEntity.posZ,
					HBMSoundHandler.zomgShoot, SoundCategory.AMBIENT,
					10.0F, 0.8F + rand.nextFloat() * 0.4F);
		}
	}

	public static void spawnVolley(World world, double x, double y, double z, int count, double speed) {
		if (!CompatibilityConfig.isWarDim(world)) return;

		NetworkRegistry.TargetPoint tp =
				new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, 50);

		for (int i = 0; i < count; i++) {
			NBTTagCompound data = new NBTTagCompound();
			data.setDouble("moX", rand.nextGaussian() * speed);
			data.setDouble("moY", rand.nextGaussian() * speed * 7.5D);
			data.setDouble("moZ", rand.nextGaussian() * speed);
			data.setString("type", "orangefx");
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z), tp);
		}
	}


	public static void floater(World world, Entity detonator, BlockPos pos, int radi, int height) {
		floater(world, detonator, pos.getX(), pos.getY(), pos.getZ(), radi, height);
	}

	public static void floater(World world, Entity detonator, int x, int y, int z, int radi, int height) {
		if (!CompatibilityConfig.isWarDim(world)) return;
		forEachBlockInSphere(world, detonator, x, y, z, radi, pos -> {
			IBlockState save = world.getBlockState(pos);
			world.setBlockToAir(pos);
			if (save.getBlock() != Blocks.AIR) {
				world.setBlockState(new BlockPos(pos.getX(), pos.getY() + height, pos.getZ()), save);
			}
		});
	}

	/**
	 * "Level down" — converts the top layer of solid blocks at height {@code y}
	 * into rising rubble entities over a square region.
	 *
	 * <p>Uses a single {@link MutableBlockPos} and caches {@link IBlockState} /
	 * {@link Block} to avoid redundant chunk lookups.
	 */
	public static void levelDown(World world, int x, int y, int z, int radius) {
		if (!CompatibilityConfig.isWarDim(world) || world.isRemote) return;

		MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int i = x - radius; i <= x + radius; i++) {
			for (int j = z - radius; j <= z + radius; j++) {
				pos.setPos(i, y, j);
				IBlockState state = world.getBlockState(pos);
				Block block = state.getBlock();

				if (block == Blocks.AIR) continue;

				float hardness = block.getBlockHardness(state, world, pos);
				if (hardness <= 0 || hardness >= 6000) continue;

				EntityRubble rubble = new EntityRubble(world);
				rubble.setPosition(i + 0.5D, y, j + 0.5D);
				rubble.motionY = 0.4D; // 0.025 * 10 + 0.15
				rubble.setMetaBasedOnBlock(block, block.getMetaFromState(state));
				world.spawnEntity(rubble);
				world.setBlockToAir(pos);
			}
		}
	}

	/**
	 * Probabilistically replaces waste/irradiated blocks with their clean
	 * equivalents. Uses {@code world.rand} to avoid allocating a new Random.
	 */
	public static void decontaminate(World world, BlockPos pos) {
		Random rng = world.rand; // reuse world's Random, no allocation
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		if (block == ModBlocks.waste_earth && rng.nextInt(3) != 0) {
			world.setBlockState(pos, Blocks.GRASS.getDefaultState());
		} else if (block == ModBlocks.waste_grass_tall && rng.nextInt(3) != 0) {
			world.setBlockState(pos, Blocks.TALLGRASS.getDefaultState());
		} else if (block == ModBlocks.waste_mycelium && rng.nextInt(5) == 0) {
			world.setBlockState(pos, Blocks.MYCELIUM.getDefaultState());
		} else if (block == ModBlocks.waste_leaves && rng.nextInt(5) != 0) {
			world.setBlockState(pos, Blocks.LEAVES.getDefaultState());
		} else if (block == ModBlocks.waste_trinitite && rng.nextInt(3) == 0) {
			world.setBlockState(pos, Blocks.SAND.getDefaultState());
		} else if (block == ModBlocks.waste_trinitite_red && rng.nextInt(3) == 0) {
			world.setBlockState(pos, Blocks.SAND.getDefaultState()
					.withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND));
		} else if (block == ModBlocks.waste_log && rng.nextInt(3) != 0) {
			world.setBlockState(pos, Blocks.LOG.getDefaultState()
					.withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.fromFacingAxis(
							state.getValue(BlockLog.AXIS))));
		} else if (block == ModBlocks.waste_planks && rng.nextInt(3) != 0) {
			world.setBlockState(pos, Blocks.PLANKS.getDefaultState());
		} else if (block == ModBlocks.block_trinitite && rng.nextInt(10) == 0) {
			world.setBlockState(pos, ModBlocks.block_lead.getDefaultState());
		} else if (block == ModBlocks.block_waste && rng.nextInt(10) == 0) {
			world.setBlockState(pos, ModBlocks.block_lead.getDefaultState());
		} else if (block == ModBlocks.sellafield) {
			decontaminateSellafield(world, pos, state, rng);
		} else if (block == ModBlocks.sellafield_slaked && rng.nextInt(5) == 0) {
			world.setBlockState(pos, Blocks.STONE.getDefaultState());
		}
	}

	/**
	 * Sellafield decay chain: meta 10 → 4 → 3 → 2 → 1 → 0 → slaked → stone.
	 * Extracted to keep {@link #decontaminate} readable.
	 */
	private static void decontaminateSellafield(World world, BlockPos pos, IBlockState state, Random rng) {
		int meta = state.getValue(BlockMeta.META);
		if (meta == 10 && rng.nextInt(10) == 0) {
			world.setBlockState(pos, ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 4));
		} else if (meta == 4 && rng.nextInt(5) == 0) {
			world.setBlockState(pos, ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 3));
		} else if (meta == 3 && rng.nextInt(5) == 0) {
			world.setBlockState(pos, ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 2));
		} else if (meta == 2 && rng.nextInt(5) == 0) {
			world.setBlockState(pos, ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 1));
		} else if (meta == 1 && rng.nextInt(5) == 0) {
			world.setBlockState(pos, ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 0));
		} else if (meta == 0 && rng.nextInt(5) == 0) {
			world.setBlockState(pos, ModBlocks.sellafield_slaked.getStateFromMeta(world.rand.nextInt(4)));
		}
	}


	public static void hardenVirus(World world, int x, int y, int z, int bombStartStrength) {
		if (!CompatibilityConfig.isWarDim(world)) return;
		forEachBlockInSphere(world, null, x, y, z, bombStartStrength, pos -> {
			if (world.getBlockState(pos).getBlock() == ModBlocks.crystal_virus)
				world.setBlockState(pos, ModBlocks.crystal_hardened.getDefaultState());
		});
	}

	public static void spreadVirus(World world, int x, int y, int z, int bombStartStrength) {
		if (!CompatibilityConfig.isWarDim(world)) return;
		forEachBlockInSphere(world, null, x, y, z, bombStartStrength, pos -> {
			if (rand.nextInt(15) == 0 && world.getBlockState(pos).getBlock() != Blocks.AIR)
				world.setBlockState(pos, ModBlocks.cheater_virus_seed.getDefaultState());
		});
	}


	/**
	 * Returns all entities inside an AABB bounding the given sphere radius.
	 * We intentionally over-approximate with the AABB and then check the exact
	 * squared distance in each caller — avoids repeated AABB construction.
	 */
	private static List<Entity> getEntitiesInRadius(World world, int x, int y, int z, double radius) {
		return world.getEntitiesWithinAABBExcludingEntity(null,
				new AxisAlignedBB(x - radius, y - radius, z - radius,
						x + radius, y + radius, z + radius));
	}

	/**
	 * Squared distance from an entity's eye position to a point.
	 * Avoids {@link Math#sqrt} compared to {@link Entity#getDistance}.
	 */
	private static double distanceSq(Entity entity, double x, double y, double z) {
		double dx = entity.posX - x;
		double dy = entity.posY + entity.getEyeHeight() - y;
		double dz = entity.posZ - z;
		return dx * dx + dy * dy + dz * dz;
	}
}
