package com.hbm.world.phased;

import com.hbm.config.GeneralConfig;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.util.BufferUtil;
import com.hbm.util.RandomPool;
import com.hbm.world.phased.PhasedEventHandler.AbstractChunkWaitJob;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Lightweight phased structure manager with minimal allocations and per-chunk persistence.
 */
public class PhasedStructureGenerator implements IWorldGenerator {
    public static final PhasedStructureGenerator INSTANCE = new PhasedStructureGenerator(); // entrypoint, state is per-dimension
    private static final long CHUNK_RNG_SALT = 0xD2B74407B1CE6E93L;
    private static final long POST_RNG_SALT = 0xCA5A826395121157L;

    private PhasedStructureGenerator() {
    }

    private static long rotl64(long v, int r) {
        return (v << r) | (v >>> (64 - r));
    }

    // SplitMix64 finalizer
    private static long mix64(long z) {
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >>> 31);
    }

    static long computeBaseSeed(long worldSeed, long layoutSeed, long origin, long typeKeyHash) {
        long s = worldSeed;
        s ^= rotl64(layoutSeed, 17);
        s ^= rotl64(origin, 33);
        s ^= typeKeyHash;
        return mix64(s);
    }

    static long computePostSeed(long worldSeed, long layoutSeed, long origin, long typeKeyHash) {
        return mix64(computeBaseSeed(worldSeed, layoutSeed, origin, typeKeyHash) ^ POST_RNG_SALT);
    }

    static void clearState(DimensionState state) {
        state.recycleAllComponents();
        state.recycleAllStarts();
    }

    static void forceGenerateStructure(World world, Random rand, long originSerialized, IPhasedStructure structure, Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Object>> layout) {
        for (var blocks : layout.values()) {
            structure.generateForChunk(world, rand, originSerialized, blocks);
        }
        structure.postGenerate(world, rand, originSerialized);
    }

    private static LongArrayList translateOffsets(long originSerialized, LongArrayList relativeOffsets) {
        int baseChunkX = Library.getBlockPosX(originSerialized) >> 4;
        int baseChunkZ = Library.getBlockPosZ(originSerialized) >> 4;
        LongArrayList absolute = DimensionState.CHUNK_LIST_POOL.borrow();
        absolute.ensureCapacity(relativeOffsets.size());
        for (int i = 0; i < relativeOffsets.size(); i++) {
            long rel = relativeOffsets.getLong(i);
            int relChunkX = Library.getChunkPosX(rel);
            int relChunkZ = Library.getChunkPosZ(rel);
            absolute.add(ChunkPos.asLong(baseChunkX + relChunkX, baseChunkZ + relChunkZ));
        }
        return absolute;
    }

    static boolean generateForChunkFast(World world, DimensionState state, int chunkX, int chunkZ) {
        long key = ChunkPos.asLong(chunkX, chunkZ);
        long oldProcessingChunk = state.currentlyProcessingChunk;
        int oldProcessingDepth = state.processingDepth;

        state.currentlyProcessingChunk = key;
        state.processingDepth = oldProcessingDepth + 1;
        state.processingTasks = true;

        boolean generated = false;
        try {
            // Loop to handle tasks that might be registered during the processing of previous tasks (cascading gen)
            ArrayList<PhasedChunkTask> list;
            while ((list = state.componentsByChunk.remove(key)) != null) {
                if (list.isEmpty()) {
                    DimensionState.recycleTaskList(list);
                    continue;
                }

                try {
                    for (PhasedChunkTask task : list) {
                        if (task == null) continue;
                        PhasedStructureStart parent = task.parent;
                        if (parent == null) continue;
                        if (!parent.isValidForPostProcess(key)) {
                            continue;
                        }
                        task.generate(world, true); // true = full cleanup allowed (we are processing the whole chunk)
                        generated = true;
                    }
                } finally {
                    DimensionState.recycleTaskList(list);
                }
            }
            return generated;
        } finally {
            state.currentlyProcessingChunk = oldProcessingChunk;
            state.processingDepth = oldProcessingDepth;
            state.processingTasks = oldProcessingDepth > 0;
            if (oldProcessingDepth == 0) {
                state.drainRecycleQueue();
                state.drainCompletedStarts();
            }
        }
    }

    private static void onStructureComplete(DimensionState state, PhasedStructureStart start) {
        if (state.processingTasks) {
            state.completedStarts.add(start);
            return;
        }
        start.finalizeStart(state);
    }

    private static void registerComponent(DimensionState state, long key, PhasedChunkTask component) {
        if (state.processingTasks && key == state.currentlyProcessingChunk) {
            // This indicates cascading worldgen which shouldn't happen with phased structures.
            // Log it but still register the task - it will be processed on the next chunk event.
            if (GeneralConfig.enableDebugWorldGen) {
                MainRegistry.logger.warn("[PhasedStructureGenerator] Task registered for currently processing chunk {},{} - queueing for immediate execution", Library.getChunkPosX(key), Library.getChunkPosZ(key));
            }
        }
        ArrayList<PhasedChunkTask> list = state.componentsByChunk.get(key);
        if (list == null) {
            list = DimensionState.borrowTaskList();
            state.componentsByChunk.put(key, list);
        }
        list.add(component);
    }

    /**
     * Handles cleanup of tasks.
     *
     * @param fullCleanup If true, assumes the entire chunk is processed (nukes the
     *                    list).
     *                    If false, removes only the specific task (preserves
     *                    others).
     */
    private static void onChunkProcessed(DimensionState state, long key, PhasedChunkTask task, boolean fullCleanup) {
        ArrayList<PhasedChunkTask> list = state.componentsByChunk.get(key);
        if (list == null) return;

        if (fullCleanup) {
            state.componentsByChunk.remove(key);
            if (key == state.currentlyProcessingChunk) {
                state.recycleQueue.add(list);
            } else {
                DimensionState.recycleTaskList(list);
            }
        } else {
            list.remove(task);
            if (list.isEmpty()) {
                state.componentsByChunk.remove(key);
                DimensionState.recycleTaskList(list);
            }
            ArrayList<PhasedChunkTask> wrapper = DimensionState.borrowTaskList();
            wrapper.add(task);
            state.recycleQueue.add(wrapper);
        }
    }

    static boolean queueHeightPointValidation(WorldServer world, PendingValidationStructure pending, boolean dynamic) {
        DimensionState state = PhasedEventHandler.getState(world);

        PendingValidationJob job = checkHeightPoints(world, pending, dynamic);
        if (job != null) {
            job.registerWaiting(state);
            return false;
        }
        return true;
    }

    private static @Nullable PendingValidationJob checkHeightPoints(WorldServer server, PendingValidationStructure pending, boolean dynamic) {
        LongArrayList heightPoints = pending.getHeightPoints();
        if (heightPoints.isEmpty()) {
            return null;
        }
        ChunkProviderServer provider = server.getChunkProvider();
        PendingValidationJob job = null;
        int minY = Integer.MAX_VALUE;

        for (int i = 0, heightPointsSize = heightPoints.size(); i < heightPointsSize; i++) {
            long point = heightPoints.getLong(i);
            int heightChunkX = Library.getBlockPosX(point) >> 4;
            int heightChunkZ = Library.getBlockPosZ(point) >> 4;

            long absKey = ChunkPos.asLong(heightChunkX, heightChunkZ);
            int cached = pending.heightPointHeights[i];
            if (cached == Integer.MIN_VALUE) {
                Chunk chunk = provider.loadedChunks.get(absKey);
                if (chunk == null || !chunk.isTerrainPopulated()) {
                    if (job == null) {
                        job = PendingValidationJob.borrow(pending, dynamic);
                    }
                    job.waitingOn.add(absKey);
                    continue;
                }

                int localX = Library.getBlockPosX(point) & 15;
                int localZ = Library.getBlockPosZ(point) & 15;
                cached = chunk.getHeightValue(localX, localZ);
                pending.heightPointHeights[i] = cached;
            }
            if (cached < minY) {
                minY = cached;
            }
        }

        if (job == null) {
            pending.cachedMinHeight = minY;
            return null;
        }
        return job;
    }

    private static boolean rebuildHeightPointWaits(WorldServer world, PendingValidationStructure pending, LongOpenHashSet waitingOn) {
        waitingOn.clear();
        LongArrayList heightPoints = pending.getHeightPoints();
        if (heightPoints.isEmpty()) {
            return true;
        }
        ChunkProviderServer provider = world.getChunkProvider();
        int minY = Integer.MAX_VALUE;
        for (int i = 0, size = heightPoints.size(); i < size; i++) {
            long point = heightPoints.getLong(i);
            int cached = pending.heightPointHeights[i];
            if (cached == Integer.MIN_VALUE) {
                long absKey = ChunkPos.asLong(Library.getBlockPosX(point) >> 4, Library.getBlockPosZ(point) >> 4);
                Chunk chunk = provider.loadedChunks.get(absKey);
                if (chunk == null || !chunk.isTerrainPopulated()) {
                    waitingOn.add(absKey);
                    continue;
                }
                cached = chunk.getHeightValue(Library.getBlockPosX(point) & 15, Library.getBlockPosZ(point) & 15);
                pending.heightPointHeights[i] = cached;
            }
            if (cached < minY) {
                minY = cached;
            }
        }
        if (waitingOn.isEmpty()) {
            pending.cachedMinHeight = minY;
        }
        return waitingOn.isEmpty();
    }

    static void scheduleStructureForValidation(WorldServer world, long originSerialized, IPhasedStructure structure, Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Object>> layout, long layoutSeed) {
        DimensionState state = PhasedEventHandler.getState(world);

        if (layout.isEmpty()) {
            MainRegistry.logger.warn("Skipping structure {} generation at {} due to empty layout.", structure.getClass().getSimpleName(), originSerialized);
            return;
        }

        PendingValidationStructure pending = new PendingValidationStructure(originSerialized, structure, layout, world.getSeed(), layoutSeed);
        if (!queueHeightPointValidation(world, pending, false)) {
            return;
        }
        ReadyToGenerateStructure ready = pending.structure.validate(world, pending);

        if (ready == null) {
            if (GeneralConfig.enableDebugWorldGen) {
                MainRegistry.logger.info("Structure {} at {} failed to validate on fast path.", pending.structure.getClass().getSimpleName(), pending.origin);
            }
            return;
        }

        scheduleValidated(state, ready);
    }

    private static void scheduleValidated(DimensionState state, ReadyToGenerateStructure ready) {
        PhasedStructureStart start = PhasedStructureStart.borrow(state, ready);
        long key = ChunkPos.asLong(start.chunkPosX, start.chunkPosZ);
        ArrayList<PhasedStructureStart> bucket = state.structureMap.get(key);
        if (bucket == null) {
            bucket = new ArrayList<>(1);
            state.structureMap.put(key, bucket);
        }
        bucket.add(start);
        start.generateExistingChunks();
    }

    private static void unregisterJobFromAllWaitLists(DimensionState state, PendingValidationJob job) {
        LongIterator iterator = job.waitingOn.iterator();
        while (iterator.hasNext()) {
            long key = iterator.nextLong();
            ReferenceLinkedOpenHashSet<AbstractChunkWaitJob> list = state.waitingJobs.get(key);
            if (list != null) {
                list.remove(job);
                if (list.isEmpty()) {
                    state.waitingJobs.remove(key);
                }
            }
        }
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.isRemote) return;
        WorldServer server = (WorldServer) world;
        DimensionState state = PhasedEventHandler.getState(server);
        generateForChunkFast(world, state, chunkX, chunkZ);
    }

    static final class PendingValidationJob extends AbstractChunkWaitJob {
        private static final ArrayDeque<PendingValidationJob> POOL = new ArrayDeque<>(256);
        PendingValidationStructure pending;
        boolean dynamic;

        private PendingValidationJob() {
        }

        public static PendingValidationJob borrow(PendingValidationStructure pending, boolean dynamic) {
            PendingValidationJob job = POOL.pollLast();
            if (job == null) job = new PendingValidationJob();
            job.init(pending, dynamic);
            return job;
        }

        public static void recycle(PendingValidationJob job) {
            job.reset();
            if (POOL.size() < 256) POOL.addLast(job);
        }

        public void init(PendingValidationStructure pending, boolean dynamic) {
            this.pending = pending;
            this.dynamic = dynamic;
            waitingOn.clear();
        }

        public void reset() {
            pending = null;
            dynamic = false;
            waitingOn.clear();
        }

        @Override
        void onJobReady(DimensionState state, WorldServer world) {
            if (!pending.hasCachedHeight()) {
                if (!pending.cacheHeightFromLoadedChunks(world)) {
                    if (!rebuildHeightPointWaits(world, pending, waitingOn)) {
                        registerWaiting(state);
                        return;
                    }
                }
            }

            ReadyToGenerateStructure ready = pending.structure.validate(world, pending);
            if (ready == null) {
                if (GeneralConfig.enableDebugWorldGen) {
                    MainRegistry.logger.info("Structure {} at {} failed to validate on queued path.", pending.structure.getClass().getSimpleName(), pending.origin);
                }
                unregisterJobFromAllWaitLists(state, this);
                recycle(this);
                return;
            }

            if (dynamic) {
                DynamicStructureDispatcher.scheduleValidated(world, ready);
            } else {
                scheduleValidated(state, ready);
            }
            recycle(this);
        }

        void registerWaiting(DimensionState state) {
            LongIterator iterator = waitingOn.iterator();
            while (iterator.hasNext()) {
                long key = iterator.nextLong();
                ReferenceLinkedOpenHashSet<AbstractChunkWaitJob> list = state.waitingJobs.get(key);
                if (list == null) {
                    list = new ReferenceLinkedOpenHashSet<>(4);
                    state.waitingJobs.put(key, list);
                }
                list.add(this);
            }
        }
    }

    public static class ReadyToGenerateStructure {
        final PendingValidationStructure pending;
        final long finalOrigin; // y-adjusted origin if validation points are present

        public ReadyToGenerateStructure(PendingValidationStructure pending, long finalOrigin) {
            this.pending = pending;
            this.finalOrigin = finalOrigin;
        }
    }

    public static class PendingValidationStructure {
        public final long origin;
        final IPhasedStructure structure;
        final Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Object>> layout;
        final long worldSeed;
        final long layoutSeed;
        private @Nullable LongArrayList heightPoints;
        private int[] heightPointHeights;
        private int cachedMinHeight = Integer.MIN_VALUE;

        PendingValidationStructure(long origin, IPhasedStructure structure, Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Object>> layout, long worldSeed, long layoutSeed) {
            this.origin = origin;
            this.structure = structure;
            this.layout = layout;
            this.worldSeed = worldSeed;
            this.layoutSeed = layoutSeed;
        }

        LongArrayList getHeightPoints() {
            if (heightPoints == null) {
                LongArrayList points = structure.getHeightPoints(origin);
                if (points == null) {
                    heightPointHeights = PhasedConstants.ZERO_INT_ARRAY;
                    return heightPoints = PhasedConstants.EMPTY;
                }
                heightPoints = points;
                heightPointHeights = new int[heightPoints.size()];
                Arrays.fill(heightPointHeights, Integer.MIN_VALUE);
            }
            return heightPoints;
        }

        boolean hasCachedHeight() {
            return cachedMinHeight != Integer.MIN_VALUE;
        }

        int getCachedMinHeight() {
            return cachedMinHeight;
        }

        boolean cacheHeightFromLoadedChunks(WorldServer server) {
            LongArrayList points = getHeightPoints();
            if (points.isEmpty()) {
                return true;
            }
            ChunkProviderServer provider = server.getChunkProvider();
            int minY = Integer.MAX_VALUE;
            boolean allCached = true;
            for (int i = 0, size = points.size(); i < size; i++) {
                long point = points.getLong(i);
                int x = Library.getBlockPosX(point);
                int z = Library.getBlockPosZ(point);
                int cached = heightPointHeights[i];
                if (cached == Integer.MIN_VALUE) {
                    long absKey = ChunkPos.asLong(x >> 4, z >> 4);
                    Chunk chunk = provider.loadedChunks.get(absKey);
                    if (chunk == null || !chunk.isTerrainPopulated()) {
                        allCached = false;
                        continue;
                    }
                    cached = chunk.getHeightValue(x & 15, z & 15);
                    heightPointHeights[i] = cached;
                }
                int height = cached;
                if (height < minY) {
                    minY = height;
                }
            }
            if (!allCached) {
                return false;
            }
            cachedMinHeight = minY;
            return true;
        }
    }

    static class PhasedStructureStart {
        private static final ArrayDeque<PhasedStructureStart> POOL = new ArrayDeque<>(1024);
        private final LongOpenHashSet remainingChunks = new LongOpenHashSet(32);
        private final LongOpenHashSet processedChunks = new LongOpenHashSet(32);
        private final ArrayList<PhasedChunkTask> components = new ArrayList<>();
        int dimension;
        int chunkPosX;
        int chunkPosZ;
        private IPhasedStructure structure;
        private long finalOrigin = 0L;
        private long worldSeed;
        private long layoutSeed;
        private long typeKeyHash;
        private long baseSeed;
        private Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Object>> layout;
        private boolean postGenerated;
        private boolean completionQueued;
        private int minX;
        private int maxX;
        private int minZ;
        private int maxZ;

        private PhasedStructureStart() {
        }

        static PhasedStructureStart borrow(DimensionState state) {
            PhasedStructureStart start = POOL.pollLast();
            if (start == null) start = new PhasedStructureStart();
            start.dimension = state.dimension;
            return start;
        }

        static PhasedStructureStart borrow(DimensionState state, ReadyToGenerateStructure ready) {
            PhasedStructureStart start = borrow(state);
            start.init(ready, state);
            return start;
        }

        static void recycle(PhasedStructureStart start) {
            start.resetState();
            if (POOL.size() < 1024) POOL.addLast(start);
        }

        void resetState() {
            remainingChunks.clear();
            processedChunks.clear();
            structure = null;
            finalOrigin = 0L;
            worldSeed = 0L;
            layoutSeed = 0L;
            typeKeyHash = 0L;
            baseSeed = 0L;
            layout = null;
            postGenerated = false;
            chunkPosX = 0;
            chunkPosZ = 0;
            minX = 0;
            maxX = 0;
            minZ = 0;
            maxZ = 0;
            completionQueued = false;
            components.clear();
        }

        boolean isSerializable() {
            return structure != null && !remainingChunks.isEmpty();
        }

        void init(ReadyToGenerateStructure ready, DimensionState state) {
            resetState();
            dimension = state.dimension;
            chunkPosX = Library.getBlockPosX(ready.finalOrigin) >> 4;
            chunkPosZ = Library.getBlockPosZ(ready.finalOrigin) >> 4;
            structure = ready.pending.structure;
            finalOrigin = ready.finalOrigin;
            worldSeed = ready.pending.worldSeed;
            layoutSeed = ready.pending.layoutSeed;
            layout = ready.pending.layout;
            recomputeSeeds();
            buildComponentsFromLayout();
        }

        private void recomputeSeeds() {
            if (structure == null) {
                typeKeyHash = 0L;
                baseSeed = 0L;
                return;
            }
            typeKeyHash = PhasedStructureRegistry.getKeyHash(structure);
            baseSeed = computeBaseSeed(worldSeed, layoutSeed, finalOrigin, typeKeyHash);
        }

        private long chunkSeed(int absChunkX, int absChunkZ) {
            long s = baseSeed ^ CHUNK_RNG_SALT;
            s ^= ((long) absChunkX) * 0x9E3779B97F4A7C15L;
            s ^= ((long) absChunkZ) * 0xC2B2AE3D27D4EB4FL;
            return mix64(s);
        }

        private long postSeed() {
            return mix64(baseSeed ^ POST_RNG_SALT);
        }

        /**
         * Serialize this start to chunk NBT. Uses compact format:
         * - Structure ID as VarInt
         * - Seeds and origin as raw longs
         * - Chunk coords as delta-encoded ZigZag VarInts
         */
        void writeToBuf(ByteBuf out) {
            if (structure == null) return;

            int structureId = PhasedStructureRegistry.getId(structure);
            if (structureId < 0) {
                throw new IllegalStateException("Unknown structure ID for " + structure.getClass().getName() + ", was it registered?");
            }
            BufferUtil.writeVarInt(out, structureId);

            out.writeLong(layoutSeed);
            out.writeLong(finalOrigin);
            out.writeBoolean(postGenerated);

            // Write remaining chunks as delta-encoded coords
            BufferUtil.writeVarInt(out, remainingChunks.size());
            LongIterator remIter = remainingChunks.iterator();
            while (remIter.hasNext()) {
                long chunkKey = remIter.nextLong();
                int absX = Library.getChunkPosX(chunkKey);
                int absZ = Library.getChunkPosZ(chunkKey);
                BufferUtil.writeZigZagVarInt(out, absX - chunkPosX);
                BufferUtil.writeZigZagVarInt(out, absZ - chunkPosZ);
            }

            // Write processed chunks as delta-encoded coords
            BufferUtil.writeVarInt(out, processedChunks.size());
            LongIterator procIter = processedChunks.iterator();
            while (procIter.hasNext()) {
                long chunkKey = procIter.nextLong();
                int absX = Library.getChunkPosX(chunkKey);
                int absZ = Library.getChunkPosZ(chunkKey);
                BufferUtil.writeZigZagVarInt(out, absX - chunkPosX);
                BufferUtil.writeZigZagVarInt(out, absZ - chunkPosZ);
            }

            int lenIndex = out.writerIndex();
            out.writeInt(0);
            int payloadStart = out.writerIndex();
            int len = 0;
            if (PhasedStructureRegistry.shouldSerialize(structure)) {
                try {
                    PhasedStructureRegistry.serialize(structure, out);
                } catch (Exception e) {
                    MainRegistry.logger.warn("[PhasedStructureGenerator] Failed to serialize structure {} at {}: {}", structure.getClass().getSimpleName(), finalOrigin, e.getMessage());
                    out.writerIndex(payloadStart);
                }
                len = out.writerIndex() - payloadStart;
                if (len < 0) len = 0;
            } else {
                out.writerIndex(payloadStart);
            }
            out.setInt(lenIndex, len);
        }

        boolean readFromBuf(DimensionState state, ByteBuf in) {
            resetState();
            dimension = state.dimension;

            int structureId = BufferUtil.readVarInt(in);
            if (structureId < 0) {
                throw new IllegalStateException("Negative structure ID encountered: " + structureId);
            }
            String key = PhasedStructureRegistry.getKeyById(structureId);
            if (key == null) return false;

            worldSeed = state.worldSeed;
            layoutSeed = in.readLong();
            finalOrigin = in.readLong();
            postGenerated = in.readBoolean();
            chunkPosX = Library.getBlockPosX(finalOrigin) >> 4;
            chunkPosZ = Library.getBlockPosZ(finalOrigin) >> 4;

            // Read remaining chunks as delta-encoded coords
            remainingChunks.clear();
            int remainingCount = BufferUtil.readVarInt(in);
            if (remainingCount < 0 || remainingCount > 1_000_000) return false;
            for (int i = 0; i < remainingCount; i++) {
                int relX = BufferUtil.readZigZagVarInt(in);
                int relZ = BufferUtil.readZigZagVarInt(in);
                remainingChunks.add(ChunkPos.asLong(chunkPosX + relX, chunkPosZ + relZ));
            }

            // Read processed chunks as delta-encoded coords
            processedChunks.clear();
            int processedCount = BufferUtil.readVarInt(in);
            if (processedCount < 0 || processedCount > 1_000_000) return false;
            for (int i = 0; i < processedCount; i++) {
                int relX = BufferUtil.readZigZagVarInt(in);
                int relZ = BufferUtil.readZigZagVarInt(in);
                processedChunks.add(ChunkPos.asLong(chunkPosX + relX, chunkPosZ + relZ));
            }

            int dataLen = in.readInt();
            if (dataLen < 0 || in.readableBytes() < dataLen) return false;
            IPhasedStructure struct;
            try {
                struct = PhasedStructureRegistry.deserializeById(structureId, in, dataLen);
            } catch (Exception e) {
                MainRegistry.logger.warn("[PhasedStructureGenerator] Failed to deserialize structure ID {} at {}: {}", structureId, finalOrigin, e.getMessage());
                return false;
            }
            if (struct == null) {
                return false;
            }

            structure = struct;
            layout = null; // force rebuild from structure
            recomputeSeeds();

            return !remainingChunks.isEmpty();
        }

        void registerTasksForRemaining() {
            ensureLayout();
            if (remainingChunks.isEmpty()) return;
            components.clear();
            components.ensureCapacity(remainingChunks.size());
            LongOpenHashSet registered = new LongOpenHashSet(Math.max(4, remainingChunks.size()));

            DimensionState state = PhasedEventHandler.getState(dimension);
            int originChunkX = Library.getBlockPosX(finalOrigin) >> 4;
            int originChunkZ = Library.getBlockPosZ(finalOrigin) >> 4;
            minX = originChunkX;
            maxX = originChunkX;
            minZ = originChunkZ;
            maxZ = originChunkZ;

            var iterator = layout.long2ObjectEntrySet().fastIterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                long relKey = entry.getLongKey();
                int relChunkX = Library.getChunkPosX(relKey);
                int relChunkZ = Library.getChunkPosZ(relKey);
                int absChunkX = originChunkX + relChunkX;
                int absChunkZ = originChunkZ + relChunkZ;
                long absKey = ChunkPos.asLong(absChunkX, absChunkZ);
                if (!remainingChunks.contains(absKey)) continue;

                minX = Math.min(minX, absChunkX);
                maxX = Math.max(maxX, absChunkX);
                minZ = Math.min(minZ, absChunkZ);
                maxZ = Math.max(maxZ, absChunkZ);

                PhasedChunkTask component = PhasedChunkTask.borrow(this, relChunkX, relChunkZ, entry.getValue(), false);
                components.add(component);
                registerComponent(state, absKey, component);
                registered.add(absKey);
            }

            if (structure != null) {
                LongArrayList watched = structure.getWatchedChunkOffsets(finalOrigin);
                LongArrayList extras = watched == null ? null : translateOffsets(finalOrigin, watched);
                if (extras != null && !extras.isEmpty()) {
                    LongListIterator iter = extras.iterator();
                    while (iter.hasNext()) {
                        long extra = iter.nextLong();
                        if (!remainingChunks.contains(extra)) continue;
                        int absX = Library.getChunkPosX(extra);
                        int absZ = Library.getChunkPosZ(extra);
                        int relX = absX - originChunkX;
                        int relZ = absZ - originChunkZ;

                        minX = Math.min(minX, absX);
                        maxX = Math.max(maxX, absX);
                        minZ = Math.min(minZ, absZ);
                        maxZ = Math.max(maxZ, absZ);

                        PhasedChunkTask marker = PhasedChunkTask.borrow(this, relX, relZ, null, true);
                        components.add(marker);
                        registerComponent(state, extra, marker);
                        registered.add(extra);
                    }
                }
                if (extras != null) DimensionState.recycleChunkList(extras);
            }
            if (registered.isEmpty()) {
                remainingChunks.clear();
            } else {
                LongIterator remIter = remainingChunks.iterator();
                while (remIter.hasNext()) {
                    long key = remIter.nextLong();
                    if (!registered.contains(key)) {
                        remIter.remove();
                    }
                }
            }
            minX = (minX << 4);
            minZ = (minZ << 4);
            maxX = (maxX << 4) + 15;
            maxZ = (maxZ << 4) + 15;
        }

        private void buildComponentsFromLayout() {
            ensureLayout();
            remainingChunks.clear();
            components.clear();
            components.ensureCapacity(layout.size());

            DimensionState state = PhasedEventHandler.getState(dimension);
            int originChunkX = Library.getBlockPosX(finalOrigin) >> 4;
            int originChunkZ = Library.getBlockPosZ(finalOrigin) >> 4;
            minX = originChunkX;
            maxX = originChunkX;
            minZ = originChunkZ;
            maxZ = originChunkZ;

            var iterator = layout.long2ObjectEntrySet().fastIterator();

            while (iterator.hasNext()) {
                var entry = iterator.next();
                long relKey = entry.getLongKey();
                Long2ObjectOpenHashMap<Object> blocksForThisChunk = entry.getValue();

                int relChunkX = Library.getChunkPosX(relKey);
                int relChunkZ = Library.getChunkPosZ(relKey);
                int absChunkX = originChunkX + relChunkX;
                int absChunkZ = originChunkZ + relChunkZ;

                long chunkKey = ChunkPos.asLong(absChunkX, absChunkZ);
                PhasedChunkTask component = PhasedChunkTask.borrow(this, relChunkX, relChunkZ, blocksForThisChunk, false);
                components.add(component);
                remainingChunks.add(chunkKey);
                registerComponent(state, chunkKey, component);

                minX = Math.min(minX, absChunkX);
                maxX = Math.max(maxX, absChunkX);
                minZ = Math.min(minZ, absChunkZ);
                maxZ = Math.max(maxZ, absChunkZ);
            }

            if (structure != null) {
                LongArrayList watched = structure.getWatchedChunkOffsets(finalOrigin);
                LongArrayList extras = watched == null ? null : translateOffsets(finalOrigin, watched);
                if (extras != null && !extras.isEmpty()) {
                    LongListIterator iter = extras.iterator();
                    while (iter.hasNext()) {
                        long extra = iter.nextLong();
                        if (remainingChunks.contains(extra)) continue;
                        int absX = Library.getChunkPosX(extra);
                        int absZ = Library.getChunkPosZ(extra);
                        int relX = absX - originChunkX;
                        int relZ = absZ - originChunkZ;

                        PhasedChunkTask marker = PhasedChunkTask.borrow(this, relX, relZ, null, true);
                        components.add(marker);
                        remainingChunks.add(extra);
                        registerComponent(state, extra, marker);

                        minX = Math.min(minX, absX);
                        maxX = Math.max(maxX, absX);
                        minZ = Math.min(minZ, absZ);
                        maxZ = Math.max(maxZ, absZ);
                    }
                }
                if (extras != null) DimensionState.recycleChunkList(extras);
            }
            minX = (minX << 4);
            minZ = (minZ << 4);
            maxX = (maxX << 4) + 15;
            maxZ = (maxZ << 4) + 15;
        }

        private void ensureLayout() {
            if (layout != null) return;
            if (structure instanceof AbstractPhasedStructure aps) {
                layout = aps.buildLayout(finalOrigin, layoutSeed);
            } else {
                layout = new Long2ObjectOpenHashMap<>();
            }
        }

        @Nullable Long2ObjectOpenHashMap<Object> getBlocksFor(int relChunkX, int relChunkZ) {
            ensureLayout();
            long key = ChunkPos.asLong(relChunkX, relChunkZ);
            return layout.get(key);
        }

        boolean isValidForPostProcess(long pos) {
            return !processedChunks.contains(pos);
        }

        void notifyPostProcessAt(World world, long key, boolean fullCleanup, PhasedChunkTask task) {
            processedChunks.add(key);
            remainingChunks.remove(key);
            DimensionState state = PhasedEventHandler.getState(dimension);
            onChunkProcessed(state, key, task, fullCleanup);

            if (remainingChunks.isEmpty() && !postGenerated && structure != null) {
                postGenerated = true;
                Random rand = RandomPool.borrow(postSeed());
                try {
                    structure.postGenerate(world, rand, finalOrigin);
                } catch (Exception e) {
                    MainRegistry.logger.error("Error running postGenerate for {}", structure.getClass().getSimpleName(), e);
                } finally {
                    RandomPool.recycle(rand);
                }
            }

            if (remainingChunks.isEmpty()) {
                if (completionQueued) return;
                completionQueued = true;
                onStructureComplete(state, this);
            }
        }

        void markGenerated(World world, long chunkKey, boolean fullCleanup, PhasedChunkTask task) {
            notifyPostProcessAt(world, chunkKey, fullCleanup, task);
        }

        void generateExistingChunks() {
            DimensionState state = PhasedEventHandler.getState(dimension);
            WorldServer server = state.world;
            ChunkProviderServer provider = server.getChunkProvider();
            if (remainingChunks.isEmpty()) return;

            int oldProcessingDepth = state.processingDepth;
            state.processingDepth = oldProcessingDepth + 1;
            state.processingTasks = true;
            try {
                long[] snapshot = remainingChunks.toLongArray();
                for (long chunkKey : snapshot) {
                    Chunk chunk = provider.loadedChunks.get(chunkKey);
                    if (chunk == null || !chunk.isTerrainPopulated()) continue;
                    int chunkX = Library.getChunkPosX(chunkKey);
                    int chunkZ = Library.getChunkPosZ(chunkKey);
                    generateForChunkFast(server, state, chunkX, chunkZ);
                }
            } finally {
                state.processingDepth = oldProcessingDepth;
                state.processingTasks = oldProcessingDepth > 0;
                if (oldProcessingDepth == 0) {
                    state.drainRecycleQueue();
                    state.drainCompletedStarts();
                }
            }
        }

        void finalizeStart(DimensionState state) {
            long key = ChunkPos.asLong(chunkPosX, chunkPosZ);
            ArrayList<PhasedStructureStart> bucket = state.structureMap.get(key);
            if (bucket != null) {
                bucket.remove(this);
                if (bucket.isEmpty()) {
                    state.structureMap.remove(key);
                }
            }
            recycle(this);
        }
    }

    static class PhasedChunkTask {
        private static final ArrayDeque<PhasedChunkTask> POOL = new ArrayDeque<>(2048);

        @Nullable PhasedStructureStart parent;
        private int relChunkX;
        private int relChunkZ;
        private @Nullable Long2ObjectOpenHashMap<Object> blocks;
        private boolean markerOnly;
        private boolean generated;

        private PhasedChunkTask() {
        }

        static PhasedChunkTask borrow(PhasedStructureStart parent, int relChunkX, int relChunkZ, @Nullable Long2ObjectOpenHashMap<Object> blocks, boolean markerOnly) {
            PhasedChunkTask task = POOL.pollLast();
            if (task == null) task = new PhasedChunkTask();
            task.reset(parent, relChunkX, relChunkZ, blocks, markerOnly);
            return task;
        }

        static void recycle(PhasedChunkTask task) {
            task.release();
            if (POOL.size() < 2048) POOL.addLast(task);
        }

        void reset(PhasedStructureStart parent, int relChunkX, int relChunkZ, @Nullable Long2ObjectOpenHashMap<Object> blocks, boolean markerOnly) {
            this.parent = parent;
            this.relChunkX = relChunkX;
            this.relChunkZ = relChunkZ;
            this.blocks = blocks;
            this.markerOnly = markerOnly;
            generated = false;
        }

        void release() {
            parent = null;
            blocks = null;
            markerOnly = false;
            generated = false;
            relChunkX = 0;
            relChunkZ = 0;
        }

        long getChunkKey() {
            if (parent == null) {
                return ChunkPos.asLong(relChunkX, relChunkZ);
            }
            int absX = parent.chunkPosX + relChunkX;
            int absZ = parent.chunkPosZ + relChunkZ;
            return ChunkPos.asLong(absX, absZ);
        }

        void generate(World worldIn, boolean fullCleanup) {
            if (generated) return;
            if (parent == null || parent.structure == null) return;

            // Capture parent locally to prevent NPE if 'this' is recycled during recursive generation
            PhasedStructureStart localParent = parent;
            long chunkKey = getChunkKey();

            if (markerOnly) {
                generated = true;
                localParent.markGenerated(worldIn, chunkKey, fullCleanup, this);
                return;
            }
            if (blocks == null || blocks.isEmpty()) {
                Long2ObjectOpenHashMap<Object> rebuilt = localParent.getBlocksFor(relChunkX, relChunkZ);
                if (rebuilt != null) blocks = rebuilt;
            }
            if (blocks == null || blocks.isEmpty()) return;

            int absChunkX = localParent.chunkPosX + relChunkX;
            int absChunkZ = localParent.chunkPosZ + relChunkZ;

            Random rand = RandomPool.borrow(localParent.chunkSeed(absChunkX, absChunkZ));
            try {
                localParent.structure.generateForChunk(worldIn, rand, localParent.finalOrigin, blocks);
            } finally {
                RandomPool.recycle(rand);
            }
            generated = true;
            localParent.markGenerated(worldIn, chunkKey, fullCleanup, this);
        }
    }
}
