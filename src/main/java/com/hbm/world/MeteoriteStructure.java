package com.hbm.world;

import com.hbm.lib.Library;
import com.hbm.world.phased.AbstractPhasedStructure;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class MeteoriteStructure extends AbstractPhasedStructure {

	public static final MeteoriteStructure INSTANCE = new MeteoriteStructure();

	private static final int COVERAGE_RADIUS = 5;

	private final LongArrayList chunkOffsets = collectChunkOffsetsByRadius(COVERAGE_RADIUS);

	@Override
	protected boolean isCacheable() {
		return false;
	}

	@Override
	protected boolean useDynamicScheduler() {
		return true;
	}

	@Override
	public LongArrayList getWatchedChunkOffsets(long origin) {
		return chunkOffsets;
	}

	@Override
	public void postGenerate(@NotNull World world, @NotNull Random rand, long finalOrigin) {
		int x = Library.getBlockPosX(finalOrigin);
		int z = Library.getBlockPosZ(finalOrigin);
		int y = world.getHeight(x, z) - rand.nextInt(10);

		if(y <= 1) return;

		BlockPos ground = new BlockPos(x, y - 2, z);
		IBlockState state = world.getBlockState(ground);
		if(state.getBlock().isAir(state, world, ground) || state.getMaterial().isLiquid()) return;

		new Meteorite().generate(world, rand, x, y, z, false, false, false);
	}
}
