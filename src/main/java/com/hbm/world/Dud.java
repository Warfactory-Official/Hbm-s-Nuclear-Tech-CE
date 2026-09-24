package com.hbm.world;

import com.hbm.blocks.BlockEnumMeta;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockCrashedBomb;
import com.hbm.lib.Library;
import com.hbm.world.phased.AbstractPhasedStructure;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class Dud extends AbstractPhasedStructure {

	public static final Dud INSTANCE = new Dud();

	@Override
	protected boolean isCacheable() {
		return false;
	}

	@Override
	protected boolean useDynamicScheduler() {
		return true;
	}

	@Override
	protected boolean isValidSpawnBlock(Block block) {
		return super.isValidSpawnBlock(block) || block == Blocks.SANDSTONE;
	}

	@Override
	public void postGenerate(@NotNull World world, @NotNull Random rand, long finalOrigin) {
		int x = Library.getBlockPosX(finalOrigin);
		int z = Library.getBlockPosZ(finalOrigin);
		int y = world.getHeight(x, z);

		if(y <= 0 || y >= world.getHeight()) return;
		if(!locationIsValidSpawn(world, Library.blockPosToLong(x, y, z))) return;

		world.setBlockState(mutablePos.setPos(x, y, z), ModBlocks.crashed_bomb.getDefaultState().withProperty(BlockEnumMeta.META, rand.nextInt(BlockCrashedBomb.EnumDudType.VALUES.length)), 2 | 16);
	}
}
