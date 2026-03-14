package com.hbm.world.feature;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.Library;
import com.hbm.world.phased.AbstractPhasedStructure;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class PlantReeds extends AbstractPhasedStructure {
    public static final PlantReeds RIVER = new PlantReeds(BiomeDictionary.Type.RIVER);
    public static final PlantReeds BEACH = new PlantReeds(BiomeDictionary.Type.BEACH);

    private final BiomeDictionary.Type biomeType;

    private static final int HORIZONTAL_RADIUS = 8;
    private static final LongArrayList CHUNK_OFFSETS = collectChunkOffsetsByRadius(HORIZONTAL_RADIUS);

    public PlantReeds(BiomeDictionary.Type biome) {
        this.biomeType = biome;
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
    public LongArrayList getWatchedChunkOffsets(long origin) {
        return CHUNK_OFFSETS;
    }

    @Override
    public void postGenerate(@NotNull World world, @NotNull Random rand, long finalOrigin) {
        int x = Library.getBlockPosX(finalOrigin);
        int z = Library.getBlockPosZ(finalOrigin);

        for (int i = 0; i < 24; ++i) {
            int px = x + rand.nextInt(9) - rand.nextInt(9);
            int pz = z + rand.nextInt(9) - rand.nextInt(9);

            int y = world.getHeight(px, pz);
            BlockPos waterPos = new BlockPos(px, y - 1, pz);

            if (world.getBlockState(waterPos).getMaterial().isLiquid()) {
                BlockPos reedPos = waterPos.up();

                if (world.isAirBlock(reedPos)) {
                    world.setBlockState(reedPos, ModBlocks.reeds.getDefaultState(), 18);
                }
            }
        }
    }

    @Override
    public boolean checkSpawningConditions(@NotNull World world, long origin) {
        BlockPos pos = Library.fromLong(mutablePos, origin);
        return BiomeDictionary.hasType(world.getBiome(pos), biomeType);
    }
}
