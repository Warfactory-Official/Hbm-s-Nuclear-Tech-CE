package com.hbm.world.phased;

import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.util.RandomPool;
import com.hbm.world.phased.PhasedEventHandler.AbstractChunkWaitJob;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Random;

/**
 * Minimal dispatcher for post-generate-only structures that would otherwise overwhelm
 * {@link PhasedStructureGenerator} with marker tasks.
 */
public final class DynamicStructureDispatcher {
    private static final Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Object>> EMPTY_LAYOUT = new Long2ObjectOpenHashMap<>(0);

    private DynamicStructureDispatcher() {
    }

    public static void forceGenerate(World world, Random rand, long originSerialized, AbstractPhasedStructure structure) {
        if (world.isRemote) return;
        structure.postGenerate(world, rand, originSerialized);
    }

    private static LongList resolveOffsets(AbstractPhasedStructure structure, long origin) {
        LongArrayList offsets = structure.getWatchedChunkOffsets(origin);
        if (offsets == null || offsets.isEmpty()) {
            return PhasedConstants.ORIGIN_ONLY;
        }
        return offsets;
    }

    private static void addJobForOrigin(DimensionState state, long originChunkKey, PendingDynamicStructure job) {
        ArrayList<PendingDynamicStructure> bucket = state.jobsByOriginChunk.get(originChunkKey);
        if (bucket == null) {
            bucket = new ArrayList<>(4);
            state.jobsByOriginChunk.put(originChunkKey, bucket);
        }
        bucket.add(job);
    }

    public static void schedule(WorldServer world, long originSerialized, AbstractPhasedStructure structure, long layoutSeed) {
        var pending = new PhasedStructureGenerator.PendingValidationStructure(originSerialized, structure, EMPTY_LAYOUT, world.getSeed(), layoutSeed);

        if (!PhasedStructureGenerator.queueHeightPointValidation(world, pending, true)) {
            return;
        }

        var ready = structure.validate(world, pending);
        if (ready == null) return;

        scheduleValidated(world, ready);
    }

    static void scheduleValidated(WorldServer server, PhasedStructureGenerator.ReadyToGenerateStructure ready) {
        DimensionState state = PhasedEventHandler.getState(server);

        AbstractPhasedStructure structure = (AbstractPhasedStructure) ready.pending.structure;
        LongList watchedOffsets = resolveOffsets(structure, ready.finalOrigin);
        PendingDynamicStructure job = PendingDynamicStructure.borrow(structure, ready.finalOrigin, ready.pending.worldSeed, ready.pending.layoutSeed, watchedOffsets);

        int originChunkX = Library.getBlockPosX(ready.finalOrigin) >> 4;
        int originChunkZ = Library.getBlockPosZ(ready.finalOrigin) >> 4;
        long originChunkKey = ChunkPos.asLong(originChunkX, originChunkZ);

        ChunkProviderServer provider = server.getChunkProvider();
        if (job.evaluate(provider)) {
            job.run(server);
        } else {
            addJobForOrigin(state, originChunkKey, job);
            job.registerWaiting(state.waitingJobs);
        }
    }

    private static void onJobFinished(WorldServer world, PendingDynamicStructure job) {
        DimensionState state = PhasedEventHandler.getState(world);

        int originChunkX = Library.getBlockPosX(job.origin) >> 4;
        int originChunkZ = Library.getBlockPosZ(job.origin) >> 4;
        long originChunkKey = ChunkPos.asLong(originChunkX, originChunkZ);

        ArrayList<PendingDynamicStructure> bucket = state.jobsByOriginChunk.get(originChunkKey);
        if (bucket == null) return;

        boolean removed = bucket.remove(job);
        if (!removed) {
            return;
        }

        if (bucket.isEmpty()) {
            state.jobsByOriginChunk.remove(originChunkKey);
        }
    }

    public static final class PendingDynamicStructure extends AbstractChunkWaitJob {
        private static final ArrayDeque<PendingDynamicStructure> POOL = new ArrayDeque<>(256);
        private AbstractPhasedStructure structure;
        private long origin;
        private long worldSeed;
        private long layoutSeed;
        private long typeKeyHash;
        private LongList watchedOffsets;

        private PendingDynamicStructure() {
        }

        public static PendingDynamicStructure borrow(AbstractPhasedStructure structure, long origin, long worldSeed, long layoutSeed, LongList watchedOffsets) {
            PendingDynamicStructure job = POOL.pollLast();
            if (job == null) job = new PendingDynamicStructure();
            else job.waitingOn.clear();
            job.structure = structure;
            job.origin = origin;
            job.worldSeed = worldSeed;
            job.layoutSeed = layoutSeed;
            try {
                job.typeKeyHash = PhasedStructureRegistry.getKeyHash(structure);
            } catch (Exception ex) {
                MainRegistry.logger.warn("[PhasedGen] Error getting key hash", ex);
                job.typeKeyHash = 0L;
            }
            job.watchedOffsets = watchedOffsets;
            return job;
        }

        public static void recycle(PendingDynamicStructure job) {
            job.reset();
            if (POOL.size() < 256) POOL.addLast(job);
        }

        public void reset() {
            structure = null;
            origin = 0L;
            watchedOffsets = null;
            worldSeed = 0L;
            layoutSeed = 0L;
            typeKeyHash = 0L;
            waitingOn.clear();
        }

        AbstractPhasedStructure getStructure() {
            return structure;
        }

        long getOrigin() {
            return origin;
        }

        long getWorldSeed() {
            return worldSeed;
        }

        long getLayoutSeed() {
            return layoutSeed;
        }


        /**
         * Evaluates readiness, filling {@link #waitingOn} for missing or unpopulated
         * chunks.
         *
         * @return true if ready to run immediately.
         */
        boolean evaluate(ChunkProviderServer provider) {
            waitingOn.clear();
            if (watchedOffsets == null || watchedOffsets.isEmpty()) return true;

            int originChunkX = Library.getBlockPosX(origin) >> 4;
            int originChunkZ = Library.getBlockPosZ(origin) >> 4;

            for (int i = 0, len = watchedOffsets.size(); i < len; i++) {
                long rel = watchedOffsets.getLong(i);
                int offsetX = Library.getChunkPosX(rel);
                int offsetZ = Library.getChunkPosZ(rel);

                long absKey = ChunkPos.asLong(originChunkX + offsetX, originChunkZ + offsetZ);
                Chunk chunk = provider.loadedChunks.get(absKey);
                if (chunk == null || !chunk.isTerrainPopulated()) {
                    waitingOn.add(absKey);
                }
            }
            return waitingOn.isEmpty();
        }

        void registerWaiting(Long2ObjectOpenHashMap<ReferenceLinkedOpenHashSet<AbstractChunkWaitJob>> waitingByChunk) {
            LongIterator iterator = waitingOn.iterator();
            while (iterator.hasNext()) {
                long key = iterator.nextLong();
                var set = waitingByChunk.get(key);
                if (set == null) {
                    set = new ReferenceLinkedOpenHashSet<>(300);
                    waitingByChunk.put(key, set);
                }
                set.add(this);
            }
        }

        @Override
        void onJobReady(DimensionState state, WorldServer world) {
            ChunkProviderServer provider = world.getChunkProvider();
            // Re-check in case chunks unloaded since we were scheduled or if something weird happened.
            if (!evaluate(provider)) {
                registerWaiting(state.waitingJobs);
                return;
            }
            run(world);
        }

        void run(WorldServer world) {
            Random rand = RandomPool.borrow(PhasedStructureGenerator.computePostSeed(worldSeed, layoutSeed, origin, typeKeyHash));
            try {
                structure.postGenerate(world, rand, origin);
            } catch (Exception e) {
                MainRegistry.logger.error("Error generating dynamic structure {} at {}", structure != null ? structure.getClass().getSimpleName() : "null", origin, e);
            } finally {
                RandomPool.recycle(rand);
                onJobFinished(world, this);
                recycle(this);
            }
        }
    }
}
