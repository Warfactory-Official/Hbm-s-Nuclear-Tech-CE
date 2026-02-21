package com.hbm.world.feature;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.Library;
import com.hbm.world.generator.DungeonToolbox;
import com.hbm.world.phased.AbstractPhasedStructure;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BedrockOilDeposit extends AbstractPhasedStructure {
    public static final BedrockOilDeposit INSTANCE = new BedrockOilDeposit();
    private static final int WORLDGEN_FLAGS = 2 | 16;
    private static final int DXZ_LIMIT = 4;
    private static final int MAX_Y = 4;
    private static final int L1_MAX = 6;
    private static final int OIL_SPOT_RADIUS = 5;
    private static final int OIL_SPOT_HEIGHT = 50;
    private static final boolean OIL_SPOT_RICH = true;
    private static final int ADDITIONAL_RADIUS = 32;
    private static final LongArrayList CHUNK_OFFSETS = collectChunkOffsetsByRadius(ADDITIONAL_RADIUS);
    private BedrockOilDeposit() {
    }

    public static void generate(@NotNull World world, int x, int z) {
        BlockPos origin = new BlockPos(x, 0, z);
        INSTANCE.generate(world, world.rand, origin);
    }

    @Override
    protected boolean useDynamicScheduler() {
        return true;
    }

    @Override
    protected boolean isCacheable() {
        return false;
    }

    @Override
    protected void buildStructure(@NotNull LegacyBuilder builder, @NotNull Random rand) {
    }

    @Override
    public LongArrayList getWatchedChunkOffsets(long origin) {
        return CHUNK_OFFSETS;
    }

    @Override
    public void postGenerate(@NotNull World world, @NotNull Random rand, long finalOrigin) {
        int centerX = Library.getBlockPosX(finalOrigin);
        int centerZ = Library.getBlockPosZ(finalOrigin);

        BlockPos.MutableBlockPos pos = this.mutablePos;

        for (int dx = -DXZ_LIMIT; dx <= DXZ_LIMIT; dx++) {
            for (int y = 0; y <= MAX_Y; y++) {
                for (int dz = -DXZ_LIMIT; dz <= DXZ_LIMIT; dz++) {
                    if (Math.abs(dx) + Math.abs(y) + Math.abs(dz) <= L1_MAX) {
                        int x = centerX + dx;
                        int z = centerZ + dz;
                        pos.setPos(x, y, z);
                        if (world.getBlockState(pos).getBlock() == Blocks.BEDROCK) {
                            world.setBlockState(pos, ModBlocks.ore_bedrock_oil.getDefaultState(), WORLDGEN_FLAGS);
                        }
                    }
                }
            }
        }

        int chunkMinX = (centerX >> 4) << 4;
        int chunkMinZ = (centerZ >> 4) << 4;
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, 16, 8, 10, 50, ModBlocks.stone_porous);
        OilSpot.generateOilSpot(world, centerX, centerZ, OIL_SPOT_RADIUS, OIL_SPOT_HEIGHT, OIL_SPOT_RICH);
    }
}
