package com.hbm.world;

import com.hbm.blocks.ModBlocks;
import com.hbm.world.phased.AbstractPhasedStructure;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@SuppressWarnings({"PointlessArithmeticExpression"})
public class Geyser extends AbstractPhasedStructure {
	public static final Geyser INSTANCE = new Geyser();
	private Geyser() {}

	@Override
	public @NotNull LongArrayList getHeightPoints(long origin) {
		LongArrayList points = new LongArrayList(1);
		points.add(origin);
		return points;
	}

	@Override
	public void buildStructure(@NotNull LegacyBuilder builder, @NotNull Random rand) {
		generate_r0(builder, rand, 0, 0, 0);
	}

	public boolean generate_r0(LegacyBuilder world, Random rand, int x, int y, int z) {

		MutableBlockPos pos = this.mutablePos;
		x -= 2;
		y -= 11;
		z -= 2;
		world.setBlockState(pos.setPos(x + 1, y + 5, z + 0), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 5, z + 0), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 5, z + 0), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 5, z + 1), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 5, z + 1), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 5, z + 1), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 5, z + 1), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 5, z + 1), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 5, z + 2), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 5, z + 2), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 5, z + 2), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 5, z + 2), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 5, z + 2), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 5, z + 3), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 5, z + 3), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 5, z + 3), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 5, z + 3), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 5, z + 3), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 5, z + 4), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 5, z + 4), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 5, z + 4), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 6, z + 0), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 6, z + 0), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 6, z + 0), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 6, z + 1), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 6, z + 1), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 6, z + 1), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 6, z + 1), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 6, z + 1), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 6, z + 2), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 6, z + 2), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 6, z + 2), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 6, z + 2), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 6, z + 2), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 6, z + 3), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 6, z + 3), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 6, z + 3), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 6, z + 3), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 6, z + 3), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 6, z + 4), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 6, z + 4), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 6, z + 4), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 7, z + 0), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 7, z + 0), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 7, z + 0), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 7, z + 1), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 7, z + 1), Blocks.WATER.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 7, z + 1), ModBlocks.block_yellowcake.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 7, z + 1), Blocks.WATER.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 7, z + 1), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 7, z + 2), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 7, z + 2), ModBlocks.block_yellowcake.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 7, z + 2), Blocks.WATER.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 7, z + 2), Blocks.WATER.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 7, z + 2), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 7, z + 3), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 7, z + 3), Blocks.WATER.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 7, z + 3), ModBlocks.block_yellowcake.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 7, z + 3), ModBlocks.block_yellowcake.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 7, z + 3), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 7, z + 4), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 7, z + 4), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 7, z + 4), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 8, z + 0), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 8, z + 0), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 8, z + 0), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 8, z + 1), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 8, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 8, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 8, z + 1), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 8, z + 1), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 8, z + 2), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 8, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 8, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 8, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 8, z + 2), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 8, z + 3), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 8, z + 3), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 8, z + 3), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 8, z + 3), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 8, z + 3), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 8, z + 4), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 8, z + 4), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 8, z + 4), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 9, z + 0), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 9, z + 0), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 9, z + 0), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 9, z + 1), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 9, z + 1), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 9, z + 1), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 9, z + 1), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 9, z + 2), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 9, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 9, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 9, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 9, z + 2), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 9, z + 3), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 9, z + 3), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 9, z + 3), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 9, z + 3), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 9, z + 4), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 9, z + 4), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 9, z + 4), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 10, z + 0), Blocks.GRASS.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 10, z + 0), Blocks.GRASS.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 10, z + 0), Blocks.GRASS.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 10, z + 1), Blocks.GRASS.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 10, z + 1), Blocks.GRAVEL.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 10, z + 1), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 10, z + 1), Blocks.GRASS.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 10, z + 1), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 10, z + 2), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 10, z + 2), Blocks.GRASS.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 10, z + 2), ModBlocks.geysir_chlorine.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 10, z + 2), Blocks.GRASS.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 10, z + 2), Blocks.GRAVEL.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 10, z + 3), Blocks.GRASS.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 10, z + 3), Blocks.STONE.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 10, z + 3), Blocks.GRASS.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 10, z + 3), Blocks.GRAVEL.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 10, z + 3), Blocks.GRASS.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 10, z + 4), Blocks.GRASS.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 10, z + 4), Blocks.GRASS.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 10, z + 4), Blocks.GRASS.getDefaultState(), 3);
		return true;

	}

}
