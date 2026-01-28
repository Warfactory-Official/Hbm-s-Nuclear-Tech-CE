package com.hbm.world.phased;

import com.hbm.config.GeneralConfig;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public interface IPhasedStructure {

    /**
     * Generates the part of the structure that lies within a specific chunk.
     *
     * @param structureOrigin The absolute origin (corner) of the entire structure in the world.
     * @param blockInfos      The blocks to generate keyed by serialized relative pos.
     */
    void generateForChunk(World world, Random rand, long structureOrigin, Long2ObjectOpenHashMap<@NotNull Object> blockInfos);

    @Nullable
    default PhasedStructureGenerator.ReadyToGenerateStructure validate(WorldServer world, PhasedStructureGenerator.PendingValidationStructure pending) {
        LongArrayList heightPoints = pending.getHeightPoints();
        if (heightPoints.isEmpty()) {
            return checkSpawningConditions(world, pending.origin) // empty -> underground
                    ? new PhasedStructureGenerator.ReadyToGenerateStructure(pending, pending.origin)
                    : null;
        }
        if (!pending.hasCachedHeight()) {
            if (!pending.cacheHeightFromLoadedChunks(world)) {
                return null;
            }
        }
        int newY = pending.getCachedMinHeight();

        if (newY > 0 && newY < world.getHeight()) {
            int x = Library.getBlockPosX(pending.origin);
            int z = Library.getBlockPosZ(pending.origin);
            long serialized = Library.blockPosToLong(x, newY, z);
            if (checkSpawningConditions(world, serialized)) {
                return new PhasedStructureGenerator.ReadyToGenerateStructure(pending, serialized);
            } else if (GeneralConfig.enableDebugWorldGen) {
                MainRegistry.logger.info("Structure {} at [{}, {}, {}] did not pass spawn condition check.", this.getClass().getSimpleName(), x, newY, z);
            }
        }
        return null;
    }

    /**
     * Dynamic part. All chunks required must be either explicitly declared by {@link #getHeightPoints},
     * or is covered by {@link #generateForChunk}.<br>
     * Violation is guaranteed to cause cascading worldgen.
     */
    default void postGenerate(World world, Random rand, long finalOrigin){
    }

    /**
     * Points used to determine the y of the structure.
     *
     * @param origin point of the origin, only x and z should be used.
     */
    @Nullable
    default LongArrayList getHeightPoints(long origin) {
        return null;
    }

    /**
     * Override to use custom spawning condition.
     * @return true if the structure can spawn at the given position.
     */
    default boolean checkSpawningConditions(World world, long origin) {
        return true;
    }

    /**
     * Relative chunk offsets (from the origin chunk) that must be present before post-generation runs.
     * Defaults to none.
     */
    @Nullable
    default LongArrayList getWatchedChunkOffsets(long origin) {
        return null;
    }
}
