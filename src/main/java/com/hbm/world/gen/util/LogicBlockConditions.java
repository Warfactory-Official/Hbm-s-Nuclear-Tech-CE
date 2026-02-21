package com.hbm.world.gen.util;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockPedestal;
import com.hbm.blocks.generic.LogicBlock;
import com.hbm.entity.mob.EntityUndeadSoldier;
import com.hbm.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

public class LogicBlockConditions {

	public static LinkedHashMap<String, Function<LogicBlock.TileEntityLogicBlock, Boolean>> conditions = new LinkedHashMap<>();

	// For use with interactions, for having them handle all conditional tasks
	public static Function<LogicBlock.TileEntityLogicBlock, Boolean> EMPTY = (tile) -> false;

	public static Function<LogicBlock.TileEntityLogicBlock, Boolean> ABERRATOR = (tile) -> {
		World world = tile.getWorld();
		if (world.getDifficulty() == EnumDifficulty.PEACEFUL) return false;

		BlockPos pos = tile.getPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		boolean aoeCheck = !world.getEntitiesWithinAABB(
				EntityPlayer.class,
				new AxisAlignedBB(x, y, z, x + 1, y - 2, z + 1).grow(10, 10, 10)
		).isEmpty();

		if (tile.phase == 0) {
			if (world.getTotalWorldTime() % 20 != 0) return false;
			return aoeCheck;
		}
		if (tile.phase < 3) {
			if (world.getTotalWorldTime() % 20 != 0 || tile.timer < 60) return false;

			// normalize AABB to ensure min <= max
			AxisAlignedBB bb = new AxisAlignedBB(
					Math.min(x, x - 2), Math.min(y, y + 1), Math.min(z, z + 1),
					Math.max(x, x - 2), Math.max(y, y + 1), Math.max(z, z + 1)
			).grow(50, 20, 50);

			return world.getEntitiesWithinAABB(EntityUndeadSoldier.class, bb).isEmpty() && aoeCheck;
		}
		return false;
	};

	public static Function<LogicBlock.TileEntityLogicBlock, Boolean> PLAYER_CUBE_3 = (tile) -> {
		World world = tile.getWorld();
		BlockPos pos = tile.getPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		return !world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(x, y, z, x + 1, y - 2, z + 1).expand(3, 3, 3)).isEmpty();
	};

	public static Function<LogicBlock.TileEntityLogicBlock, Boolean> PLAYER_CUBE_5 = (tile) -> {
		World world = tile.getWorld();
		BlockPos pos = tile.getPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		return !world.getEntitiesWithinAABB(
				EntityPlayer.class,
				new AxisAlignedBB(x, y, z, x + 1, y - 2, z + 1).grow(5, 5, 5)
		).isEmpty();
	};

	public static Function<LogicBlock.TileEntityLogicBlock, Boolean> PLAYER_CUBE_25 = (tile) -> {
		World world = tile.getWorld();
		BlockPos pos = tile.getPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		return !world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(x, y, z, x + 1, y - 2, z + 1).expand(25, 25, 25)).isEmpty();
	};

	public static Function<LogicBlock.TileEntityLogicBlock, Boolean> REDSTONE = (tile) -> {
		World world = tile.getWorld();
		BlockPos pos = tile.getPos();
		return world.isBlockPowered(pos);
	};

	public static Function<LogicBlock.TileEntityLogicBlock, Boolean> PUZZLE_TEST = (tile) -> {
		World world = tile.getWorld();
		BlockPos pos = tile.getPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		if (tile.phase == 0 && world.isBlockPowered(pos)) {
			EntityPlayer player = world.getClosestPlayer(x, y, z, 25, false);
			if (player != null) {
				player.sendMessage(new TextComponentString("Find a " + TextFormatting.GOLD + "great" + TextFormatting.RESET + " ancient weapon, of questionable use in the modern age"));
			}
			world.setBlockState(new BlockPos(x, y + 1, z), ModBlocks.pedestal.getDefaultState());
			return true;
		}

		TileEntity pedestal = world.getTileEntity(new BlockPos(x, y + 1, z));

		return tile.phase == 1
            && pedestal instanceof BlockPedestal.TileEntityPedestal
            && ((BlockPedestal.TileEntityPedestal) pedestal).item != null
            && ((BlockPedestal.TileEntityPedestal) pedestal).item.getItem() == ModItems.big_sword;
	};

	public static List<String> getConditionNames() {
		return new ArrayList<>(conditions.keySet());
	}

	// register new conditions here
	static {
		// example conditions
		conditions.put("EMPTY", EMPTY);
		conditions.put("ABERRATOR", ABERRATOR);
		conditions.put("PLAYER_CUBE_3", PLAYER_CUBE_3);
		conditions.put("PLAYER_CUBE_5", PLAYER_CUBE_5);
		conditions.put("PLAYER_CUBE_25", PLAYER_CUBE_25);
		conditions.put("REDSTONE", REDSTONE);
		conditions.put("PUZZLE_TEST", PUZZLE_TEST);
	}

}
