package com.hbm.world;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.util.BufferUtil;
import com.hbm.world.phased.AbstractPhasedStructure;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class OilSandBubble extends AbstractPhasedStructure {
	private final int radius;
	private final LongArrayList chunkOffsets;

	public OilSandBubble(int radius) {
		this.radius = radius;
		this.chunkOffsets = collectChunkOffsetsByRadius(radius);
	}

	@Override
	public boolean isCacheable() {
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
		int ox = Library.getBlockPosX(finalOrigin);
		int oy = Library.getBlockPosY(finalOrigin);
		int oz = Library.getBlockPosZ(finalOrigin);
		OilSandBubble.spawnOil(world, rand, ox, oy, oz, this.radius);
	}

	private static void spawnOil(World world, Random rand, int x, int y, int z, int radius) {
		int r = radius;
		int r2 = r * r;
		int r22 = r2 / 2;

		MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int xx = -r; xx < r; xx++) {
			int X = xx + x;
			int XX = xx * xx;
			for (int yy = -r; yy < r; yy++) {
				int Y = yy + y;
				int YY = XX + yy * yy * 3;
				for (int zz = -r; zz < r; zz++) {
					int Z = zz + z;
					int ZZ = YY + zz * zz;
					if (ZZ < r22 + rand.nextInt(r22 / 3)) {
						pos.setPos(X, Y, Z);
						if(world.getBlockState(pos).getBlock() == Blocks.SAND)
							world.setBlockState(pos, ModBlocks.ore_oil_sand.getDefaultState());
					}
				}
			}
		}
	}


    public void writeToBuf(@NotNull ByteBuf out) {
		BufferUtil.writeVarInt(out, radius);
	}

    public static OilSandBubble readFromBuf(@NotNull ByteBuf in) {
        int radius;
        try {
			radius = BufferUtil.readVarInt(in);
        } catch (Exception ex) {
            MainRegistry.logger.warn("[OilSandBubble] Failed to read from buffer", ex);
            return null;
        }
        return new OilSandBubble(radius);
    }
}
