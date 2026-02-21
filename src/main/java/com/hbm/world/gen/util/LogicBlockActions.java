package com.hbm.world.gen.util;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockSkeletonHolder;
import com.hbm.blocks.generic.LogicBlock;
import com.hbm.entity.missile.EntityMissileTier2;
import com.hbm.entity.mob.EntityUndeadSoldier;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.tileentity.TileEntityDoorGeneric;
import com.hbm.tileentity.machine.storage.TileEntityCrateBase;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.MobUtil;
import com.hbm.util.Vec3NT;
import com.hbm.world.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class LogicBlockActions {

	public static LinkedHashMap<String, Consumer<LogicBlock.TileEntityLogicBlock>> actions = new LinkedHashMap<>();

	public static Consumer<LogicBlock.TileEntityLogicBlock> PHASE_ABERRATOR = (tile) -> {
		World world = tile.getWorld();
		BlockPos pos = tile.getPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		if (tile.phase == 1 || tile.phase == 2) {
			tile.player = world.getClosestPlayer(x + 0.5D, y + 0.5D, z + 0.5D, 25.0D, false);
			if (tile.timer == 0) {
				Vec3NT vec = new Vec3NT(20, 0, 0);
				for (int i = 0; i < 10; i++) {

					if (vec.x > 8) vec.add(world.rand.nextInt(10) - 5, 0, 0);

					EntityUndeadSoldier mob = new EntityUndeadSoldier(world);
					for (int j = 0; j < 7; j++) {
						double sx = x + 0.5D + vec.x;
						double sz = z + 0.5D + vec.z;
						int sy = world.getHeight((int) Math.floor(sx), (int) Math.floor(sz));
						mob.setPositionAndRotation(sx, sy, sz, i * 36F, 0);
						if (mob.getCanSpawnHere()) {
							mob.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(sx, sy, sz)), null);
							if (tile.player != null) {
								mob.setAttackTarget(tile.player);
							}
							world.spawnEntity(mob);
							break;
						}
					}
					vec.rotateAroundYDeg(36D);
				}
			}
		}
		if (tile.phase > 2) {
			BlockPos holderPos = pos.up(18);
			TileEntity te = world.getTileEntity(holderPos);
			if (te instanceof BlockSkeletonHolder.TileEntitySkeletonHolder skeleton) {
				if (world.rand.nextInt(5) == 0) {
					skeleton.item = new ItemStack(ModItems.item_secret, 1, ItemEnums.EnumSecretType.ABERRATOR.ordinal());
				} else {
					skeleton.item = new ItemStack(ModItems.clay_tablet, 1, 1);
				}
				skeleton.markDirty();
				IBlockState state = world.getBlockState(holderPos);
				world.notifyBlockUpdate(holderPos, state, state, 3);
			}
			world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState(), 3);
		}
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> COLLAPSE_ROOF_RAD_5 = (tile) -> {
		World world = tile.getWorld();
		BlockPos pos = tile.getPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		if (tile.phase == 0) return;

		int r = 4;
		int r2 = r * r;
		int r22 = r2 / 2;

		for (int xx = -r; xx < r; xx++) {
			int X = xx + x;
			int XX = xx * xx;
			for (int yy = -r; yy < r; yy++) {
				int Y = yy + y;
				int YY = XX + yy * yy;
				for (int zz = -r; zz < r; zz++) {
					int Z = zz + z;
					int ZZ = YY + zz * zz;
					if (ZZ < r22) {
						BlockPos p = new BlockPos(X, Y, Z);
						IBlockState state = world.getBlockState(p);
						Block block = state.getBlock();

						if (!world.isAirBlock(p) && block.getExplosionResistance(null) <= 70.0F) {
							world.setBlockToAir(p);
							EntityFallingBlock entity = new EntityFallingBlock(world, X + 0.5D, Y + 0.5D, Z + 0.5D, state);
							world.spawnEntity(entity);
						}
					}
				}
			}
		}

		world.setBlockToAir(pos);
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> COLLAPSE_ROOF_RAD_10 = (tile) -> {
		World world = tile.getWorld();
		BlockPos pos = tile.getPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		if(tile.phase == 0) return;

		int r = 8;
		int r2 = r * r;
		int r22 = r2 / 2;

		for (int xx = -r; xx < r; xx++) {
			int X = xx + x;
			int XX = xx * xx;
			for (int yy = -r; yy < r; yy++) {
				int Y = yy + y;
				int YY = XX + yy * yy;
				for (int zz = -r; zz < r; zz++) {
					int Z = zz + z;
					int ZZ = YY + zz * zz;
					if (ZZ < r22) {
						BlockPos p = new BlockPos(X, Y, Z);
						IBlockState state = world.getBlockState(p);
						Block block = state.getBlock();

						if (!world.isAirBlock(p) && block.getExplosionResistance(null) <= 70.0F) {
							world.setBlockToAir(p);
							EntityFallingBlock entity = new EntityFallingBlock(world, X + 0.5D, Y + 0.5D, Z + 0.5D, state);
							world.spawnEntity(entity);
						}
					}
				}
			}
		}
		world.setBlockToAir(pos);
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> FODDER_WAVE = (tile) -> {
		World world = tile.getWorld();
		BlockPos pos = tile.getPos();
		int x = pos.getX();
		int z = pos.getZ();
		if (tile.phase == 1) {
			Vec3NT vec = new Vec3NT(5, 0, 0);
			for (int i = 0; i < 10; i++) {
				EntityZombie mob = new EntityZombie(world);
				mob.setPositionAndRotation(
						x + 0.5D + vec.x,
						world.getHeight(new BlockPos(x, 0, z)).getY(),
						z + 0.5D + vec.z,
						i * 36F,
						0
				);
				MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolAdv, new Random());
				world.spawnEntity(mob);

				vec.rotateAroundYDeg(36D);
			}
			world.setBlockState(pos, ModBlocks.block_steel.getDefaultState(), 3);
		}
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> SKELETON_GUN_TIER_1 = (tile) -> {
		World world = tile.getWorld();
		BlockPos pos = tile.getPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		if (tile.phase == 1) {
			Vec3NT vec = new Vec3NT(0, 0, 0);
			EntitySkeleton mob = new EntitySkeleton(world);
			mob.setPositionAndRotation(x, y, z, 0, 0);
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolGunsTier1, new Random());
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolMasks, new Random());
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolRanged, new Random());
			world.spawnEntity(mob);
			world.setBlockToAir(pos);
		}
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> SKELETON_GUN_TIER_2 = (tile) -> {
		World world = tile.getWorld();
		BlockPos pos = tile.getPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		if (tile.phase == 1) {
			Vec3NT vec = new Vec3NT(0, 0, 0);
			EntitySkeleton mob = new EntitySkeleton(world);
			mob.setPositionAndRotation(x, y, z, 0, 0);
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolGunsTier2, new Random());
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolMasks, new Random());
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolTierArmor, new Random());
			world.spawnEntity(mob);
			world.setBlockToAir(pos);
		}
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> SKELETON_GUN_TIER_3 = (tile) -> {
		World world = tile.getWorld();
		BlockPos pos = tile.getPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		if (tile.phase == 1) {
			Vec3NT vec = new Vec3NT(0, 0, 0);
			EntitySkeleton mob = new EntitySkeleton(world);
			mob.setPositionAndRotation(x, y, z, 0, 0);
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolGunsTier3, new Random());
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolMasks, new Random());
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolAdvRanged, new Random());
			world.spawnEntity(mob);
			world.setBlockToAir(pos);
		}
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> ZOMBIE_TIER_1 = (tile) -> {
		World world = tile.getWorld();
		BlockPos pos = tile.getPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		if (tile.phase == 1) {
			Vec3NT vec = new Vec3NT(0, 0, 0);
			EntityZombie mob = new EntityZombie(world);
			mob.setPositionAndRotation(x, y, z, 0, 0);
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolMelee, new Random());
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolTierArmor, new Random());
			world.spawnEntity(mob);
			world.setBlockToAir(pos);
		}
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> ZOMBIE_TIER_2 = (tile) -> {
		World world = tile.getWorld();
		BlockPos pos = tile.getPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		if (tile.phase == 1) {
			Vec3NT vec = new Vec3NT(0, 0, 0);
			EntityZombie mob = new EntityZombie(world);
			mob.setPositionAndRotation(x, y, z, 0, 0);
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolTierArmor, new Random());
			MobUtil.assignItemsToEntity(mob, MobUtil.slotPoolMelee, new Random());
			world.spawnEntity(mob);
			world.setBlockToAir(pos);
		}
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> PUZZLE_TEST = (tile) -> {
		World world = tile.getWorld();
		BlockPos pos = tile.getPos();
		int x = pos.getX();
		int z = pos.getZ();

		if (tile.phase == 2) {
			world.setBlockState(pos, ModBlocks.crate_steel.getDefaultState(), 3);

			EntityLightningBolt blitz = new EntityLightningBolt(world, x, world.getHeight(new BlockPos(x, 0, z)).getY() + 2, z, false);
			world.addWeatherEffect(blitz);

			TileEntityCrateBase crate = (TileEntityCrateBase) world.getTileEntity(pos);
			((IInventory) crate).setInventorySlotContents(15, new ItemStack(ModItems.gun_bolter));
		}
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> MISSILE_STRIKE = (tile) -> {
		World world = tile.getWorld();
		BlockPos pos = tile.getPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		if (tile.phase != 1) return;

		world.getClosestPlayer(x, y, z, 25.0D, false)
				.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "[COMMAND UNIT]" + TextFormatting.RESET + " Missile Fired"));

		EnumFacing parallel = tile.direction.rotateY();

		EntityMissileTier2.EntityMissileStrong missile =
				new EntityMissileTier2.EntityMissileStrong(
						world,
						x + tile.direction.getXOffset() * 300,
						200,
						z + tile.direction.getZOffset() * 300,
						x + parallel.getXOffset() * 30 + tile.direction.getXOffset() * 30,
						z + parallel.getZOffset() * 30 + tile.direction.getZOffset() * 30
				);
		WorldUtil.loadAndSpawnEntityInWorld(missile);

		world.setBlockState(pos, ModBlocks.block_electrical_scrap.getDefaultState(), 3);
	};

	public static Consumer<LogicBlock.TileEntityLogicBlock> RAD_CONTAINMENT_SYSTEM = (tile) -> {
		World world = tile.getWorld();
		BlockPos pos = tile.getPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		EnumFacing direction = tile.direction.getOpposite();
		EnumFacing rot = direction.rotateY();

		int dirX = direction.getXOffset();
		int dirZ = direction.getZOffset();
		int rotX = rot.getXOffset();
		int rotZ = rot.getZOffset();

		double minX = Math.min(x - rotX, x + rotX + dirX * 15);
		double minY = Math.min(y - 1, y + 1);
		double minZ = Math.min(z - rotZ, z + rotZ + dirZ * 15);
		double maxX = Math.max(x - rotX, x + rotX + dirX * 15);
		double maxY = Math.max(y - 1, y + 1);
		double maxZ = Math.max(z - rotZ, z + rotZ + dirZ * 15);

		AxisAlignedBB bb = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).grow(2, 2, 2);

		List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, bb);

		for (EntityLivingBase e : entities) {
			Vec3d vec = new Vec3d(
					e.posX - (x + 0.5D),
					(e.posY + e.getEyeHeight()) - (y + 0.5D),
					e.posZ - (z + 0.5D)
			);
			double len = vec.length();
			vec = vec.normalize();

			len = Math.max(len, 1D);

			float res = 0;

			for (int i = 1; i < len; i++) {
				int ix = (int) Math.floor(x + 0.5D + vec.x * i);
				int iy = (int) Math.floor(y + 0.5D + vec.y * i);
				int iz = (int) Math.floor(z + 0.5D + vec.z * i);

				Block block = world.getBlockState(new BlockPos(ix, iy, iz)).getBlock();
				res += block.getExplosionResistance(null);
			}

			if (res < 1) res = 1;

			float eRads = 100F;
			eRads /= res;
			eRads /= (float) (len * len);

			ContaminationUtil.contaminate(e, ContaminationUtil.HazardType.RADIATION, ContaminationUtil.ContaminationType.HAZMAT2, eRads);
		}

		if (tile.phase == 2 && tile.timer > 40) {
			world.getClosestPlayer(x, y, z, 25.0D, false).sendMessage(new TextComponentString(
					TextFormatting.LIGHT_PURPLE + "[RAD CONTAINMENT SYSTEM]" +
							TextFormatting.RESET + " Diagnostics found containment failure, commencing lockdown"));

			for (int i = 1; i < 20; i++) {
				int checkX = x + dirX * i;
				int checkY = y + 1;
				int checkZ = z + dirZ * i;
				BlockPos bp = new BlockPos(checkX, checkY, checkZ);
				Block block = world.getBlockState(bp).getBlock();
				TileEntity te = null;
				if (block instanceof BlockDummyable) {
					int[] coreCoords = ((BlockDummyable) block).findCore(world, checkX, checkY, checkZ);
					te = world.getTileEntity(new BlockPos(coreCoords[0], coreCoords[1], coreCoords[2]));
				}

				if (te instanceof TileEntityDoorGeneric door) {
					door.setPins(456);
					door.close();
					door.lock();
					break;
				}
			}

			tile.phase = 3;
		}
	};

	public static List<String> getActionNames(){
		return new ArrayList<>(actions.keySet());
	}

	//register new actions here
	static{
		//example actions
		actions.put("FODDER_WAVE", FODDER_WAVE);
		actions.put("ABERRATOR", PHASE_ABERRATOR);
		actions.put("COLLAPSE_ROOF_RAD_5", COLLAPSE_ROOF_RAD_5);
		actions.put("COLLAPSE_ROOF_RAD_10", COLLAPSE_ROOF_RAD_10);
		actions.put("PUZZLE_TEST", PUZZLE_TEST);
		actions.put("MISSILE_STRIKE", MISSILE_STRIKE);
		actions.put("IRRADIATE_ENTITIES_AOE", RAD_CONTAINMENT_SYSTEM);

		//Mob Block Actions
		actions.put("SKELETON_GUN_TIER_1", SKELETON_GUN_TIER_1);
		actions.put("SKELETON_GUN_TIER_2", SKELETON_GUN_TIER_2);
		actions.put("SKELETON_GUN_TIER_3", SKELETON_GUN_TIER_3);

		actions.put("ZOMBIE_TIER_1", ZOMBIE_TIER_1);
		actions.put("ZOMBIE_TIER_2", ZOMBIE_TIER_2);
	}



}
