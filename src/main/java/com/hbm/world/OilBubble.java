package com.hbm.world;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.util.BufferUtil;
import com.hbm.world.phased.AbstractPhasedStructure;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class OilBubble extends AbstractPhasedStructure {
	public final int radius;
	private final LongArrayList chunkOffsets;

	public OilBubble(int radius) {
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
		int oz = Library.getBlockPosZ(finalOrigin);
        int oy = Library.getBlockPosY(finalOrigin);
		this.spawnOil(world, ox, oy, oz, this.radius);
	}

	private void spawnOil(World world, int x, int y, int z, int radius) {
		int r = radius;
		int r2 = r * r;
		int r22 = r2 / 2;

		MutableBlockPos pos = mutablePos;
		for(int xx = -r; xx < r; xx++) {
			int X = xx + x;
			int XX = xx * xx;
			for(int yy = -r; yy < r; yy++) {
				int Y = yy + y;
				int YY = XX + yy * yy * 3;
				for(int zz = -r; zz < r; zz++) {
					int Z = zz + z;
					int ZZ = YY + zz * zz;
					if(ZZ < r22) {
						pos.setPos(X, Y, Z);
						if(world.getBlockState(pos).getBlock() == Blocks.STONE)
							world.setBlockState(pos, ModBlocks.ore_oil.getDefaultState(), 2 | 16);
					}
				}
			}
		}
	}

	public void spawnOil(World world, int x, int y, int z, int radius, Block block, int meta, Block target) {
		int r = radius;
		int r2 = r * r;
		int r22 = r2 / 2;

		MutableBlockPos pos = mutablePos;

		for (int xx = -r; xx < r; xx++) {
			int X = xx + x;
			int XX = xx * xx;
			for (int yy = -r; yy < r; yy++) {
				int Y = yy + y;
				int YY = XX + yy * yy * 3;
				for (int zz = -r; zz < r; zz++) {
					int Z = zz + z;
					int ZZ = YY + zz * zz;
					if (ZZ < r22) {
						pos.setPos(X, Y, Z);
						if(world.getBlockState(pos).getBlock() == target)
							world.setBlockState(pos, block.getDefaultState(), 2 | 16);
					}
				}
			}
		}
	}


    public void writeToBuf(@NotNull ByteBuf out) {
		BufferUtil.writeVarInt(out, radius);
	}

    public static OilBubble readFromBuf(@NotNull ByteBuf in) {
        int radius;
        try {
			radius = BufferUtil.readVarInt(in);
        } catch (Exception ex) {
            MainRegistry.logger.warn("[OilBubble] Failed to read from buffer", ex);
            return null;
        }
        return new OilBubble(radius);
    }
}
