package com.hbm.world.dungeon;

import com.hbm.lib.Library;
import com.hbm.world.phased.AbstractPhasedStructure;
import com.hbm.world.phased.PhasedStructureGenerator;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Random;

public class AncientTombStructure extends AbstractPhasedStructure {
    public static final AncientTombStructure INSTANCE = new AncientTombStructure();
    private AncientTombStructure() {}

    @Override
    protected boolean isCacheable() {
        return false;
    }

    @Override
    public @NotNull LongArrayList getHeightPoints(long origin) {
        final int inner = 16;
        LongArrayList points = new LongArrayList(4);
        points.add(origin);
        points.add(Library.shiftBlockPos(origin, inner, 0, inner));
        points.add(Library.shiftBlockPos(origin, inner, 0, -inner));
        points.add(Library.shiftBlockPos(origin, -inner, 0, inner));
        points.add(Library.shiftBlockPos(origin, -inner, 0, -inner));
        return points;
    }

    @Override
    protected void buildStructure(@NotNull LegacyBuilder builder, @NotNull Random rand) {
        new AncientTomb().buildChamber(builder, rand, 0, 0, 0);
    }

    @Override
    public PhasedStructureGenerator.ReadyToGenerateStructure validate(@NotNull WorldServer world, @NotNull PhasedStructureGenerator.PendingValidationStructure pending) {
        long origin = pending.origin;
        int originX = Library.getBlockPosX(origin);
        int originZ = Library.getBlockPosZ(origin);
        int surfaceY = world.getHeight(originX, originZ);
        if (surfaceY > 35) {
            long finalOrigin = Library.blockPosToLong(originX, 20, originZ);
            return new PhasedStructureGenerator.ReadyToGenerateStructure(pending, finalOrigin);
        }
        return null;
    }

    @Override
    public void postGenerate(@NotNull World world, @NotNull Random rand, long finalOrigin) {
        new AncientTomb().buildSurfaceFeatures(world, rand, Library.getBlockPosX(finalOrigin), Library.getBlockPosZ(finalOrigin));
    }

    @Override
    public LongArrayList getWatchedChunkOffsets(long origin) {
        int radiusChunks = 2;
        LongArrayList offsets = new LongArrayList();
        for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
            for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
                offsets.add(ChunkPos.asLong(dx, dz));
            }
        }
        return offsets;
    }
}
