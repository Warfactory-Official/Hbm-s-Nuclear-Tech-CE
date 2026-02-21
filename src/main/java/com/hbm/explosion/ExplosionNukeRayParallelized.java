package com.hbm.explosion;

import com.hbm.config.BombConfig;
import com.hbm.config.CompatibilityConfig;
import com.hbm.handler.threading.BombForkJoinPool;
import com.hbm.interfaces.BitMask;
import com.hbm.interfaces.IExplosionRay;
import com.hbm.interfaces.ServerThread;
import com.hbm.lib.Library;
import com.hbm.lib.TLPool;
import com.hbm.lib.maps.NonBlockingHashMapLong;
import com.hbm.lib.queues.MpscUnboundedXaddArrayLongQueue;
import com.hbm.main.MainRegistry;
import com.hbm.util.ChunkUtil;
import com.hbm.util.ConcurrentBitSet;
import com.hbm.util.MpscIntArrayListCollector;
import com.hbm.util.OffHeapBitSet;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.util.Constants;
import org.jctools.queues.MpscUnboundedXaddArrayQueue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

import static com.hbm.config.BombConfig.safeCommit;
import static com.hbm.lib.internal.UnsafeHolder.U;
import static com.hbm.lib.internal.UnsafeHolder.fieldOffset;

/**
 * Threaded DDA raytracer for mk5 explosion.
 *
 * @author mlbv
 */
public class ExplosionNukeRayParallelized implements IExplosionRay, BombForkJoinPool.IJobCancellable {
    static final int WORLD_HEIGHT = 256;
    static final int BITSET_SIZE = 16 * WORLD_HEIGHT * 16;
    static final int SUB_MASK_SIZE = 16 * 16 * 16;
    static final int SUBCHUNK_PER_CHUNK = WORLD_HEIGHT >> 4;
    static final float NUKE_RESISTANCE_CUTOFF = 2_000_000F;
    static final float INITIAL_ENERGY_FACTOR = 0.3F; // Scales crater, no impact on performance
    static final double RESOLUTION_FACTOR = 1.0;  // Scales ray density, no impact on crater radius
    static final int LUT_RESISTANCE_BINS = 256;
    static final int LUT_DISTANCE_BINS = 256;
    static final float LUT_MAX_RESISTANCE = 100.0F;
    static final float[][] ENERGY_LOSS_LUT = new float[LUT_RESISTANCE_BINS][LUT_DISTANCE_BINS];
    static final float DAMAGE_PER_BLOCK = 0.50F; // fallback for low resistance blocks
    static final float DAMAGE_THRESHOLD_MULT = 1.00F;
    static final float LOW_R_BOUND = 0.25F;
    static final float LOW_R_PASS_LENGTH_BREAK = 0.75F;
    static final double GOLDEN_ANGLE = Math.PI * (3.0 - Math.sqrt(5.0));

    static final double RAY_DIRECTION_EPSILON = 1e-6;
    static final double PROCESSING_EPSILON = 1e-9;
    static final float MIN_EFFECTIVE_DIST_FOR_ENERGY_CALC = 0.01f;

    static final long EMPTY_LONG = Long.MIN_VALUE;

    static final ThreadLocal<MutableBlockPos> TL_POS = ThreadLocal.withInitial(MutableBlockPos::new);
    static final ThreadLocal<LocalAgg> TL_LOCAL_AGG = ThreadLocal.withInitial(LocalAgg::new);
    static final TLPool<IntDoubleAccumulator> ACC_POOL = new TLPool<>(IntDoubleAccumulator::new, IntDoubleAccumulator::clear, 64, 4096);
    static final ThreadLocal<Long2ObjectOpenHashMap<IBlockState>> TL_MODIFIED = ThreadLocal.withInitial(() -> new Long2ObjectOpenHashMap<>(16));
    static final ThreadLocal<LongOpenHashSet> TL_EDGES = ThreadLocal.withInitial(() -> new LongOpenHashSet(64));
    static final TLPool<LongArrayList> LONG_LIST_POOL = new TLPool<>(() -> new LongArrayList(64), LongArrayList::clear, 8, 512);
    static final TLPool<Long2IntOpenHashMap> LONG2INT_POOL = new TLPool<>(Long2IntOpenHashMap::new, Long2IntOpenHashMap::clear, 4, 256);
    static final TLPool<IntArrayList> INT_LIST_POOL = new TLPool<>(() -> new IntArrayList(64), IntArrayList::clear, 16, 512);
    static final TLPool<Long2ObjectOpenHashMap<IBlockState>> LONG2OBJECT_POOL = new TLPool<>(Long2ObjectOpenHashMap::new, Long2ObjectOpenHashMap::clear, 4, 256);
    static final long OFF_MAP_ACQUIRED = fieldOffset(ExplosionNukeRayParallelized.class, "mapAcquired");
    static final long OFF_CONSOLIDATION_STARTED = fieldOffset(ExplosionNukeRayParallelized.class, "consolidationStarted");
    static final long OFF_PENDING_RAYS = fieldOffset(ExplosionNukeRayParallelized.class, "pendingRays");
    static final long OFF_PENDING_CARVE_NOTIFIES = fieldOffset(ExplosionNukeRayParallelized.class, "pendingCarveNotifies");
    static final long OFF_FINISH_QUEUED = fieldOffset(ExplosionNukeRayParallelized.class, "finishQueued");
    static final long OFF_COLLECT_FINISHED = fieldOffset(ExplosionNukeRayParallelized.class, "collectFinished");
    static final long OFF_CONSOLIDATION_FINISHED = fieldOffset(ExplosionNukeRayParallelized.class, "consolidationFinished");
    static final long OFF_DESTROY_FINISHED = fieldOffset(ExplosionNukeRayParallelized.class, "destroyFinished");
    static final long OFF_POOL_ACQUIRED = fieldOffset(ExplosionNukeRayParallelized.class, "poolAcquired");
    static final long OFF_JOB_REGISTERED = fieldOffset(ExplosionNukeRayParallelized.class, "jobRegistered");

    static {
        for (int r = 0; r < LUT_RESISTANCE_BINS; r++) {
            float resistance = (r / (float) (LUT_RESISTANCE_BINS - 1)) * LUT_MAX_RESISTANCE;
            for (int d = 0; d < LUT_DISTANCE_BINS; d++) {
                float distFrac = d / (float) (LUT_DISTANCE_BINS - 1);
                ENERGY_LOSS_LUT[r][d] = (float) (Math.pow(resistance + 1.0, 3.0 * distFrac) - 1.0);
            }
        }
    }

    final WorldServer world;
    final double explosionX, explosionY, explosionZ;
    final double invRadius, invRayIndexScale;
    final int originX, originY, originZ;
    final int radius;
    final int strength;
    final NonBlockingHashMapLong<ConcurrentBitSet> destructionMap;
    final NonBlockingHashMapLong<ChunkAgg> aggMap;
    final NonBlockingHashMapLong<MpscIntArrayListCollector> waitingRoom;
    final NonBlockingHashMapLong<MpscUnboundedXaddArrayQueue<ResumeItem>> postLoadQueues;
    final MpscUnboundedXaddArrayLongQueue chunkLoadQueue;
    final Long2IntOpenHashMap sectionMaskByChunk;
    final MpscUnboundedXaddArrayQueue<PendingCarve> pendingCarves;

    final CarveApplier applier;

    int algorithm;
    int rayCount;
    int[] rayOrder;
    ForkJoinPool pool;
    NonBlockingHashMapLong<Chunk> mirror;
    volatile UUID detonator;
    int jobDimension = Integer.MIN_VALUE;

    @SuppressWarnings("unused")
    volatile int mapAcquired, consolidationStarted, finishQueued, pendingRays, pendingCarveNotifies,
            collectFinished, consolidationFinished, destroyFinished, poolAcquired, jobRegistered;

    volatile boolean isContained = true;

    public ExplosionNukeRayParallelized(World world, double x, double y, double z, int strength, int radius, int algorithm) {
        this(world, x, y, z, strength, radius);
        this.algorithm = algorithm;
        initializeAndStartWorkers();
    }

    public ExplosionNukeRayParallelized(World world, double x, double y, double z, int strength, int radius) {
        this.world = (WorldServer) world; // Casted here
        explosionX = x;
        explosionY = y;
        explosionZ = z;
        originX = (int) Math.floor(x);
        originY = (int) Math.floor(y);
        originZ = (int) Math.floor(z);
        this.strength = strength;
        this.radius = radius;
        invRadius = radius > 0 ? 1.0 / radius : 0.0;
        applier = safeCommit ? new SafeApplier() : new FastApplier();

        if (!CompatibilityConfig.isWarDim(world)) {
            U.putIntRelease(this, OFF_COLLECT_FINISHED, 1);
            U.putIntRelease(this, OFF_CONSOLIDATION_FINISHED, 1);
            U.putIntRelease(this, OFF_DESTROY_FINISHED, 1);
            isContained = true;
            chunkLoadQueue = new MpscUnboundedXaddArrayLongQueue(1024);
            pendingCarves = new MpscUnboundedXaddArrayQueue<>(1024);
            destructionMap = new NonBlockingHashMapLong<>(16);
            aggMap = new NonBlockingHashMapLong<>(16);
            waitingRoom = new NonBlockingHashMapLong<>(16);
            postLoadQueues = new NonBlockingHashMapLong<>(16);
            sectionMaskByChunk = new Long2IntOpenHashMap(0);
            sectionMaskByChunk.defaultReturnValue(0);
            invRayIndexScale = 0.0;
            rayCount = 0;
            pendingRays = 0;
            return;
        }

        rayCount = Math.max(0, (int) (2.5 * Math.PI * strength * strength * RESOLUTION_FACTOR));
        invRayIndexScale = rayCount > 1 ? 1.0 / (rayCount - 1) : 0.0;
        pendingRays = rayCount;

        int estimatedChunkCount = Math.max(16, count(false));
        int chunkCap = capFor(estimatedChunkCount * 2);
        int subChunkCap = capFor(Math.max(16, count(true)));

        destructionMap = new NonBlockingHashMapLong<>(chunkCap);
        aggMap = new NonBlockingHashMapLong<>(chunkCap);
        waitingRoom = new NonBlockingHashMapLong<>(subChunkCap);
        postLoadQueues = new NonBlockingHashMapLong<>(subChunkCap);
        sectionMaskByChunk = new Long2IntOpenHashMap(estimatedChunkCount);
        sectionMaskByChunk.defaultReturnValue(0);
        final int pooledLong = 4;
        int csLoad = chooseChunkSizeForSegments(Math.max(subChunkCap, 1024), 32, 1024, 8192);
        chunkLoadQueue = new MpscUnboundedXaddArrayLongQueue(csLoad, pooledLong);

        int csCarves = chooseChunkSizeForSegments(Math.max(estimatedChunkCount, 1024), 32, 1024, 4096);
        pendingCarves = new MpscUnboundedXaddArrayQueue<>(csCarves);
    }

    static int capFor(int n) {
        int c = 1;
        while (c < n) c <<= 1;
        return Math.max(16, c);
    }

    static int ceilPow2(int x) {
        if (x <= 1) return 1;
        int hb = Integer.highestOneBit(x - 1);
        int r = hb << 1;
        return r > 0 ? r : (1 << 30);
    }

    static int chooseChunkSizeForSegments(int peakDepth, int targetSegments, int minPow2, int maxPow2) {
        if (peakDepth <= 0) return minPow2;
        int want = (peakDepth + targetSegments - 1) / targetSegments;
        int cs = ceilPow2(want);
        if (cs < minPow2) cs = minPow2;
        if (cs > maxPow2) cs = maxPow2;
        return cs;
    }

    @Contract(pure = true)
    int count(boolean perSubchunk) {
        int cr = (radius + 15) >> 4;
        int minCX = (originX >> 4) - cr;
        int maxCX = (originX >> 4) + cr;
        int minCZ = (originZ >> 4) - cr;
        int maxCZ = (originZ >> 4) + cr;
        int minSubY = Math.max(0, (originY - radius) >> 4);
        int maxSubY = Math.min(SUBCHUNK_PER_CHUNK - 1, (originY + radius) >> 4);
        if (minSubY > maxSubY) return 0;
        double R2 = (radius + 14) * (radius + 14);
        int total = 0;
        for (int cx = minCX; cx <= maxCX; cx++) {
            double dx = ((cx << 4) + 8) - explosionX;
            double dx2 = dx * dx;

            for (int cz = minCZ; cz <= maxCZ; cz++) {
                double dz = ((cz << 4) + 8) - explosionZ;
                double base = dx2 + dz * dz;
                if (base > R2) continue;
                double ry = Math.sqrt(R2 - base);
                int firstY = (int) Math.ceil((explosionY - ry - 8.0) / 16.0);
                int lastY = (int) Math.floor((explosionY + ry - 8.0) / 16.0);
                if (firstY < minSubY) firstY = minSubY;
                if (lastY > maxSubY) lastY = maxSubY;
                if (perSubchunk) {
                    int countY = lastY - firstY + 1;
                    if (countY > 0) total += countY;
                } else {
                    if (lastY >= firstY) total++;
                }
            }
        }
        return total;
    }

    @SuppressWarnings({"DataFlowIssue", "deprecation"})
    static float getNukeResistance(IBlockState state) {
        if (state.getMaterial().isLiquid()) return 0.1F;
        Block block = state.getBlock();
        if (block == Blocks.SANDSTONE) return 4.0F;
        if (block == Blocks.OBSIDIAN) return 18.0F;
        return block.getExplosionResistance(null);
    }

    static double getEnergyLossFactor(float resistance, double distFrac) {
        if (resistance >= NUKE_RESISTANCE_CUTOFF) return resistance;
        if (resistance <= 0) return 0.0;
        if (resistance > LUT_MAX_RESISTANCE) return Math.pow(resistance + 1.0, 3.0 * distFrac) - 1.0;
        int rBin = (int) (resistance * (LUT_RESISTANCE_BINS - 1) / LUT_MAX_RESISTANCE);
        if (rBin == 0 && resistance > 0) return Math.pow(resistance + 1.0, 3.0 * distFrac) - 1.0;
        int dBin = (int) (distFrac * (LUT_DISTANCE_BINS - 1));
        return ENERGY_LOSS_LUT[rBin][dBin];
    }

    void initializeAndStartWorkers() {
        if (rayCount <= 0) {
            U.putIntRelease(this, OFF_COLLECT_FINISHED, 1);
            U.putIntRelease(this, OFF_CONSOLIDATION_FINISHED, 1);
            return;
        }
        if (U.compareAndSetInt(this, OFF_POOL_ACQUIRED, 0, 1)) {
            pool = BombForkJoinPool.acquire();
        }
        registerJobIfNeeded();
        buildRayOrder();
        mirror = ChunkUtil.acquireMirrorMap(world);
        U.putIntRelease(this, OFF_MAP_ACQUIRED, 1);
        int minRayGrain = (rayOrder != null) ? 4096 : 512;
        pool.submit(new RayTracerTask(0, rayCount, computeTaskGrain(rayCount, minRayGrain)));
    }

    void releasePoolIfHeld() {
        unregisterJobIfNeeded();
        if (U.getAndSetInt(this, OFF_POOL_ACQUIRED, 0) != 0) {
            BombForkJoinPool.release();
            pool = null;
        }
    }

    void registerJobIfNeeded() {
        if (U.compareAndSetInt(this, OFF_JOB_REGISTERED, 0, 1)) {
            int dim = world == null ? Integer.MIN_VALUE : world.provider.getDimension();
            jobDimension = dim;
            BombForkJoinPool.register(dim, this);
        }
    }

    void unregisterJobIfNeeded() {
        if (U.getAndSetInt(this, OFF_JOB_REGISTERED, 0) != 0) {
            int dim = jobDimension;
            jobDimension = Integer.MIN_VALUE;
            if (dim != Integer.MIN_VALUE) BombForkJoinPool.unregister(dim, this);
        }
    }

    private void buildRayOrder() {
        int n = rayCount;
        if (n <= 0) return;
        if (n < 200_000) return;
        long[] packed = new long[n];
        ForkJoinPool p = pool;
        int par = (p == null) ? 1 : p.getParallelism();
        boolean doParallelFill = par > 1 && n >= 512_000;
        if (doParallelFill) {
            int grain = computeTaskGrain(n, 16384);
            p.invoke(new BuildPackedTask(packed, 0, n, grain, invRayIndexScale));
        } else {
            for (int i = 0; i < n; i++) {
                double y = 1.0 - (i * invRayIndexScale) * 2.0;
                double r = Math.sqrt(Math.max(0.0, 1.0 - y * y));
                double t = GOLDEN_ANGLE * i;
                double x = Math.cos(t) * r;
                double z = Math.sin(t) * r;
                int key = rayKeyCubeMorton(x, y, z);
                packed[i] = (((long) key) << 32) | (i & 0xFFFF_FFFFL);
            }
        }
        if (n >= 1_000_000) {
            LongArrays.parallelRadixSort(packed);
        } else {
            LongArrays.radixSort(packed);
        }

        int[] order = new int[n];
        if (par > 1 && n >= 1_000_000) {
            int grain = computeTaskGrain(n, 32768);
            p.invoke(new UnpackOrderTask(packed, order, 0, n, grain));
        } else {
            for (int i = 0; i < n; i++) order[i] = (int) packed[i];
        }
        rayOrder = order;
    }

    static final class BuildPackedTask extends RecursiveAction {
        final long[] packed;
        final int lo, hi, threshold;
        final double invRayIndexScale;

        BuildPackedTask(long[] packed, int lo, int hi, int threshold, double invRayIndexScale) {
            this.packed = packed;
            this.lo = lo;
            this.hi = hi;
            this.threshold = Math.max(1, threshold);
            this.invRayIndexScale = invRayIndexScale;
        }

        @Override
        protected void compute() {
            int len = hi - lo;
            if (len <= threshold) {
                for (int i = lo; i < hi; i++) {
                    double y = 1.0 - (i * invRayIndexScale) * 2.0;
                    double r = Math.sqrt(Math.max(0.0, 1.0 - y * y));
                    double t = GOLDEN_ANGLE * i;
                    double x = Math.cos(t) * r;
                    double z = Math.sin(t) * r;
                    int key = rayKeyCubeMorton(x, y, z);
                    packed[i] = (((long) key) << 32) | (i & 0xFFFF_FFFFL);
                }
                return;
            }
            int mid = lo + (len >>> 1);
            invokeAll(new BuildPackedTask(packed, lo, mid, threshold, invRayIndexScale), new BuildPackedTask(packed, mid, hi, threshold, invRayIndexScale));
        }
    }

    static final class UnpackOrderTask extends RecursiveAction {
        final long[] packed;
        final int[] order;
        final int lo, hi, threshold;

        UnpackOrderTask(long[] packed, int[] order, int lo, int hi, int threshold) {
            this.packed = packed;
            this.order = order;
            this.lo = lo;
            this.hi = hi;
            this.threshold = Math.max(1, threshold);
        }

        @Override
        protected void compute() {
            int len = hi - lo;
            if (len <= threshold) {
                for (int i = lo; i < hi; i++) order[i] = (int) packed[i];
                return;
            }
            int mid = lo + (len >>> 1);
            invokeAll(new UnpackOrderTask(packed, order, lo, mid, threshold), new UnpackOrderTask(packed, order, mid, hi, threshold));
        }
    }

    private static int rayKeyCubeMorton(double x, double y, double z) {
        double ax = Math.abs(x), ay = Math.abs(y), az = Math.abs(z);
        int face;
        double u, v, inv; //@formatter:off
        if (ax >= ay && ax >= az) {
            if (x >= 0) { // +X
                face = 0; inv = 1.0 / ax; u = (-z * inv + 1.0) * 0.5;
            } else {      // -X
                face = 1; inv = 1.0 / ax; u = ( z * inv + 1.0) * 0.5;
            }
            v = (-y * inv + 1.0) * 0.5;
        } else if (ay >= az) {
            if (y >= 0) { // +Y
                face = 2; inv = 1.0 / ay; u = ( x * inv + 1.0) * 0.5; v = ( z * inv + 1.0) * 0.5;
            } else {      // -Y
                face = 3; inv = 1.0 / ay; u = ( x * inv + 1.0) * 0.5; v = (-z * inv + 1.0) * 0.5;
            }
        } else {
            if (z >= 0) { // +Z
                face = 4; inv = 1.0 / az; u = ( x * inv + 1.0) * 0.5;
            } else {      // -Z
                face = 5; inv = 1.0 / az; u = (-x * inv + 1.0) * 0.5;
            }
            v = (-y * inv + 1.0) * 0.5;
        } //@formatter:on
        int uq = (int) (u * 1023.0);
        int vq = (int) (v * 1023.0);
        if (uq < 0) uq = 0; else if (uq > 1023) uq = 1023;
        if (vq < 0) vq = 0; else if (vq > 1023) vq = 1023;
        int morton = morton10(uq, vq); // 20 bits
        return (face << 20) | morton;  // 23 bits total
    }

    private static int morton10(int x, int y) {
        return (part1By1_10(x) << 1) | part1By1_10(y);
    }

    private static int part1By1_10(int x) {
        x &= 0x3FF;
        x = (x | (x << 8)) & 0x00FF00FF;
        x = (x | (x << 4)) & 0x0F0F0F0F;
        x = (x | (x << 2)) & 0x33333333;
        x = (x | (x << 1)) & 0x55555555;
        return x;
    }

    private int computeTaskGrain(int totalRays, int minGrain) {
        int p = pool == null ? 1 : pool.getParallelism();
        int targetTasks = Math.max(1, p << 2);
        int batch = (totalRays + targetTasks - 1) / targetTasks;
        batch = ceilPow2(batch);
        if (batch < minGrain) batch = minGrain;
        if (batch > totalRays && totalRays > 0) batch = totalRays;
        return batch;
    }

    private int computeKeyTaskGrain(int totalKeys) {
        int p = pool == null ? 1 : pool.getParallelism();
        int targetTasks = Math.max(1, p << 2);
        int batch = (totalKeys + targetTasks - 1) / targetTasks;
        batch = ceilPow2(batch);
        if (batch < 64) batch = 64;
        if (batch > totalKeys && totalKeys > 0) batch = totalKeys;
        return batch;
    }

    void onAllRaysFinished() {
        U.putIntRelease(this, OFF_COLLECT_FINISHED, 1);
        rayOrder = null;
        if (U.compareAndSetInt(this, OFF_CONSOLIDATION_STARTED, 0, 1)) {
            ForkJoinPool p = pool;
            if (p != null && !p.isShutdown()) {
                p.submit(this::runConsolidation);
            } else {
                runConsolidation();
            }
        }
    }

    @ServerThread
    void loadMissingChunks(int timeBudgetMs) {
        long deadline = System.nanoTime() + (timeBudgetMs * 1_000_000L);
        while (System.nanoTime() < deadline) {
            long ck = chunkLoadQueue.relaxedPoll();
            if (ck == EMPTY_LONG) break;
            processChunkLoadRequest(ck);
        }
    }

    @ServerThread
    void processChunkLoadRequest(long chunkPos) {
        Chunk chunk = world.getChunk(Library.getChunkPosX(chunkPos), Library.getChunkPosZ(chunkPos));
        ForkJoinPool p = pool;
        boolean poolActive = (p != null && !p.isShutdown());
        MpscIntArrayListCollector waiters = waitingRoom.remove(chunkPos);
        if (waiters != null) {
            if (!poolActive) {
                ensureCollectorRegistered(chunkPos, waiters);
                chunkLoadQueue.offer(chunkPos);
            } else {
                IntArrayList batch = waiters.drain();
                if (!batch.isEmpty()) {
                    p.submit(new ResumeBatchTask(batch, 0, batch.size(), computeTaskGrain(batch.size(), rayOrder == null ? 512 : 4096)));
                }
            }
        }
        MpscUnboundedXaddArrayQueue<ResumeItem> q = postLoadQueues.remove(chunkPos);
        if (q != null) {
            if (!poolActive) {
                MpscUnboundedXaddArrayQueue<ResumeItem> prev = postLoadQueues.putIfAbsent(chunkPos, q);
                if (prev != null && prev != q) {
                    ResumeItem it;
                    while ((it = q.poll()) != null) prev.offer(it);
                }
                chunkLoadQueue.offer(chunkPos);
                return;
            }

            ArrayList<CarveSubTask> rebuilt = new ArrayList<>(8);
            ExtendedBlockStorage[] ebs = chunk.getBlockStorageArray();
            int cx = Library.getChunkPosX(chunkPos);
            int cz = Library.getChunkPosZ(chunkPos);
            ResumeItem item;
            while ((item = q.poll()) != null) {
                switch (item.kind) {
                    case ResumeItem.CARVE:
                        CarveSubTask t = prepareOneSub(cx, cz, item.subY, item.mask, ebs);
                        if (t != null) rebuilt.add(t);
                        else item.mask.free();
                        break;
                    case ResumeItem.APPLY_MASKS:
                        Int2ObjectOpenHashMap<BitMask> masks = item.masks;
                        p.submit(() -> {
                            applier.apply(chunkPos, ebs, masks);
                            maybeFinish();
                        });
                        break;
                    case ResumeItem.APPLY_AGG:
                        ChunkAgg agg = item.agg;
                        p.submit(() -> {
                            applier.apply(chunkPos, ebs, buildMasksFromAgg(agg, ebs));
                            agg.clear();
                            maybeFinish();
                        });
                        break;
                }
            }
            if (!rebuilt.isEmpty()) pendingCarves.offer(new PendingCarve(chunkPos, rebuilt));
        }
    }

    @ServerThread
    void secondPass() {
        PlayerChunkMap playerChunkMap = world.getPlayerChunkMap();
        Long2ObjectMap<Chunk> loaded = world.getChunkProvider().loadedChunks;
        ObjectIterator<Long2IntMap.Entry> iterator = sectionMaskByChunk.long2IntEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2IntMap.Entry e = iterator.next();
            int changedMask = e.getIntValue();
            if (changedMask == 0) continue;
            long cp = e.getLongKey();
            Chunk chunk = loaded.get(cp);
            if (chunk == null) continue;
            ExtendedBlockStorage[] storages = chunk.getBlockStorageArray();
            boolean groundUp = false;
            for (int subY = 0; subY < storages.length; subY++) {
                if (((changedMask >>> subY) & 1) == 0) continue;

                ExtendedBlockStorage s = storages[subY];
                if (s == Chunk.NULL_BLOCK_STORAGE) {
                    groundUp = true;
                } else if (s.isEmpty()) {
                    storages[subY] = Chunk.NULL_BLOCK_STORAGE;
                    groundUp = true;
                }
            }
            chunk.generateSkylightMap();
            chunk.resetRelightChecks();
            PlayerChunkMapEntry entry = playerChunkMap.getEntry(Library.getChunkPosX(cp), Library.getChunkPosZ(cp));
            if (entry != null) {
                entry.sendPacket(new SPacketChunkData(chunk, groundUp ? 0xFFFF : changedMask));
            }
        }
        sectionMaskByChunk.clear();
    }

    @Override
    public boolean isComplete() {
        return collectFinished != 0 && consolidationFinished != 0 && destroyFinished != 0;
    }

    @Override
    public boolean isContained() {
        return isContained;
    }

    static Int2ObjectOpenHashMap<BitMask> splitBySubchunk(@NotNull ConcurrentBitSet chunkMask) {
        Int2ObjectOpenHashMap<BitMask> m = new Int2ObjectOpenHashMap<>();
        int bit = chunkMask.nextSetBit(0);
        while (bit >= 0) {
            int yGlobal = WORLD_HEIGHT - 1 - (bit >>> 8);
            int subY = yGlobal >>> 4;
            int xLocal = (bit >>> 4) & 0xF;
            int zLocal = bit & 0xF;
            int yLocal = yGlobal & 0xF;
            int localIndex = Library.packLocal(xLocal, yLocal, zLocal);
            m.computeIfAbsent(subY, k -> new OffHeapBitSet(SUB_MASK_SIZE)).set(localIndex);
            bit = chunkMask.nextSetBit(bit + 1);
        }
        return m;
    }

    static Int2ObjectOpenHashMap<BitMask> buildMasksFromAgg(@NotNull ChunkAgg agg, @NotNull ExtendedBlockStorage[] storages) {
        Int2ObjectOpenHashMap<BitMask> subChunkBitSets = new Int2ObjectOpenHashMap<>();
        Int2DoubleOpenHashMap dmg = agg.damage;
        Int2DoubleOpenHashMap len = agg.passLen;

        if (!dmg.isEmpty()) {
            ObjectIterator<Int2DoubleMap.Entry> iterator = dmg.int2DoubleEntrySet().fastIterator();
            while (iterator.hasNext()) {
                Int2DoubleOpenHashMap.Entry e = iterator.next();
                int bitIndex = e.getIntKey(); // global index
                int yGlobal = WORLD_HEIGHT - 1 - (bitIndex >>> 8);
                int subY = yGlobal >>> 4;
                int xLocal = (bitIndex >>> 4) & 0xF;
                int zLocal = bitIndex & 0xF;
                int yLocal = yGlobal & 0xF;
                int localIndex = Library.packLocal(xLocal, yLocal, zLocal);
                if (shouldDestroy(bitIndex, storages, e.getDoubleValue(), len.get(bitIndex))) {
                    subChunkBitSets.computeIfAbsent(subY, k -> new OffHeapBitSet(SUB_MASK_SIZE)).set(localIndex);
                }
            }
        }

        if (!len.isEmpty()) {
            ObjectIterator<Int2DoubleMap.Entry> iterator = len.int2DoubleEntrySet().fastIterator();
            while (iterator.hasNext()) {
                Int2DoubleOpenHashMap.Entry e = iterator.next();
                int bitIndex = e.getIntKey(); // global index
                if (dmg.containsKey(bitIndex)) continue;
                int yGlobal = WORLD_HEIGHT - 1 - (bitIndex >>> 8);
                int subY = yGlobal >>> 4;
                int xLocal = (bitIndex >>> 4) & 0xF;
                int zLocal = bitIndex & 0xF;
                int yLocal = yGlobal & 0xF;
                int localIndex = Library.packLocal(xLocal, yLocal, zLocal);
                if (shouldDestroy(bitIndex, storages, 0.0, e.getDoubleValue())) {
                    subChunkBitSets.computeIfAbsent(subY, k -> new OffHeapBitSet(SUB_MASK_SIZE)).set(localIndex);
                }
            }
        }
        return subChunkBitSets;
    }

    static boolean shouldDestroy(int bitIndex, ExtendedBlockStorage[] storages, double accumulatedDamage, double passLen) {
        int yGlobal = WORLD_HEIGHT - 1 - (bitIndex >>> 8);
        int subY = yGlobal >>> 4;
        ExtendedBlockStorage s = storages[subY];
        if (s == Chunk.NULL_BLOCK_STORAGE || s.isEmpty()) return false;
        int xLocal = (bitIndex >>> 4) & 0xF;
        int zLocal = bitIndex & 0xF;
        int yLocal = yGlobal & 0xF;
        IBlockState st = s.get(xLocal, yLocal, zLocal);
        if (st.getBlock() == Blocks.AIR) return false;
        float resistance = getNukeResistance(st);
        if (accumulatedDamage >= (resistance * DAMAGE_THRESHOLD_MULT)) return true;
        return resistance <= LOW_R_BOUND && passLen >= LOW_R_PASS_LENGTH_BREAK;
    }

    @Override
    public void setDetonator(UUID detonator) {
        this.detonator = detonator;
    }

    @Override
    public void update(int processTimeMs) {
        if (safeCommit) {
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(processTimeMs);
            loadMissingChunks(processTimeMs / 2);
            while (System.nanoTime() < deadline) {
                PendingCarve job = pendingCarves.poll();
                if (job == null) break;
                applyCarveJobOnMain(job);
            }
            maybeFinish();
        } else {
            loadMissingChunks(processTimeMs);
        }
    }

    @Override
    public void cancel() {
        U.putIntRelease(this, OFF_COLLECT_FINISHED, 1);
        U.putIntRelease(this, OFF_CONSOLIDATION_FINISHED, 1);
        U.putIntRelease(this, OFF_DESTROY_FINISHED, 1);

        if (waitingRoom != null) waitingRoom.clear();
        if (sectionMaskByChunk != null) sectionMaskByChunk.clear();
        PendingCarve job;
        while ((job = pendingCarves.poll()) != null) {
            for (CarveSubTask t : job.tasks()) {
                if (t != null && t.mask != null) t.mask.free();
            }
        }
        if (postLoadQueues != null) {
            long[] keys = postLoadQueues.keySetLong();
            for (long cp : keys) {
                MpscUnboundedXaddArrayQueue<ResumeItem> q = postLoadQueues.remove(cp);
                if (q != null) {
                    ResumeItem item;
                    while ((item = q.poll()) != null) {
                        switch (item.kind) {
                            case ResumeItem.CARVE:
                                if (item.mask != null) item.mask.free();
                                break;
                            case ResumeItem.APPLY_MASKS:
                                if (item.masks != null) {
                                    ObjectIterator<Int2ObjectMap.Entry<BitMask>> it = item.masks.int2ObjectEntrySet().fastIterator();
                                    while (it.hasNext()) it.next().getValue().free();
                                    item.masks.clear();
                                }
                                break;
                            case ResumeItem.APPLY_AGG:
                                if (item.agg != null) item.agg.clear();
                                break;
                        }
                    }
                }
            }
            postLoadQueues.clear();
        }

        releasePoolIfHeld();
        rayOrder = null;
        if (U.getAndSetInt(this, OFF_MAP_ACQUIRED, 0) != 0) {
            ChunkUtil.releaseMirrorMap(world);
        }
        if (destructionMap != null) destructionMap.clear();
        if (aggMap != null) aggMap.clear();
        if (chunkLoadQueue != null) chunkLoadQueue.clear();
    }

    @Override
    public void cancelJob() {
        cancel();
    }

    /**
     * Run once all rays finish. Converts sources → masks → applies via applier.
     */
    void runConsolidation() {
        if (algorithm == 2) {
            long[] keys = aggMap.keySetLong();
            if (keys.length != 0) {
                int thresh = computeKeyTaskGrain(keys.length);
                new ConsolidateAggTask(keys, 0, keys.length, thresh).invoke();
            }
            aggMap.clear();
        } else {
            long[] keys = destructionMap.keySetLong();
            if (keys.length != 0) {
                int thresh = computeKeyTaskGrain(keys.length);
                new ConsolidateMaskTask(keys, 0, keys.length, thresh).invoke();
            }
        }
        U.putIntRelease(this, OFF_CONSOLIDATION_FINISHED, 1);
        maybeFinish();
    }

    void maybeFinish() {
        boolean doneCollect = (collectFinished != 0);
        boolean doneConsolidate = (consolidationFinished != 0);
        boolean doneDestroy = (destroyFinished != 0);
        if (!doneCollect || !doneConsolidate || doneDestroy) return;
        if (pendingCarveNotifies != 0) return;
        if (safeCommit && !pendingCarves.isEmpty()) return;
        if (!waitingRoom.isEmpty()) return;
        if (!postLoadQueues.isEmpty()) return;
        if (!U.compareAndSetInt(this, OFF_FINISH_QUEUED, 0, 1)) return;
        world.addScheduledTask(() -> {
            secondPass();
            U.putIntRelease(this, OFF_DESTROY_FINISHED, 1);
            releasePoolIfHeld();
            if (U.getAndSetInt(this, OFF_MAP_ACQUIRED, 0) != 0) {
                ChunkUtil.releaseMirrorMap(world);
            }
        });
    }

    int carveSubchunkAndSwap(BitMask subMask, long cpLong, ExtendedBlockStorage[] storages, Long2ObjectOpenHashMap<IBlockState> modified,
                                     LongArrayList neighborNotifies, int selfMask, Long2IntOpenHashMap neighborMask, int subY) {
        int cx = Library.getChunkPosX(cpLong);
        int cz = Library.getChunkPosZ(cpLong);

        ExtendedBlockStorage expected = storages[subY];
        if (expected == Chunk.NULL_BLOCK_STORAGE || expected.isEmpty()) {
            return selfMask | (1 << subY);
        }

        Long2ObjectOpenHashMap<IBlockState> modifiedLocal = TL_MODIFIED.get();
        LongOpenHashSet edges = TL_EDGES.get();

        modifiedLocal.clear();
        edges.clear();

        while (true) {
            modifiedLocal.clear();
            edges.clear();
            updateModified(cx, cz, subY, subMask, expected, modifiedLocal);
            ExtendedBlockStorage carved = ChunkUtil.copyAndCarveLocal(world, cx, cz, subY, storages, subMask, edges);
            if (ChunkUtil.casEbsAt(expected, carved, storages, subY)) {
                selfMask |= (1 << subY);
                modified.putAll(modifiedLocal);
                if (!edges.isEmpty()) {
                    MutableBlockPos pos = TL_POS.get();
                    LongIterator it = edges.iterator();
                    while (it.hasNext()) {
                        long lp = it.nextLong();
                        neighborNotifies.add(lp);
                        Library.fromLong(pos, lp);
                        long nck = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
                        int m = neighborMask.get(nck);
                        m |= 1 << (pos.getY() >>> 4);
                        neighborMask.put(nck, m);
                    }
                }
                break;
            }
            expected = storages[subY];
            if (expected == Chunk.NULL_BLOCK_STORAGE || expected.isEmpty()) {
                selfMask |= (1 << subY);
                break;
            }
            modifiedLocal.clear();
            edges.clear();
        }
        return selfMask;
    }

    void notifyMainThread(long cpLong, Long2ObjectOpenHashMap<IBlockState> teRemovals, LongArrayList neighborNotifies, int selfMask,
                                  Long2IntOpenHashMap neighborMask) {
        U.getAndAddInt(this, OFF_PENDING_CARVE_NOTIFIES, 1);
        world.addScheduledTask(() -> {
            try {
                chunkFixup(cpLong, teRemovals, neighborNotifies, selfMask, neighborMask);
                Chunk chunk = world.getChunkProvider().loadedChunks.get(cpLong);
                if (chunk != null) chunk.markDirty();
            } finally {
                LONG2OBJECT_POOL.recycle(teRemovals);
                LONG_LIST_POOL.recycle(neighborNotifies);
                LONG2INT_POOL.recycle(neighborMask);
                int prev = U.getAndAddInt(this, OFF_PENDING_CARVE_NOTIFIES, -1);
                if (prev - 1 == 0) {
                    maybeFinish();
                }
            }
        });
    }

    void prepareAndEnqueue(long cpLong, Int2ObjectOpenHashMap<BitMask> masks, ExtendedBlockStorage[] storages) {
        if (masks == null || masks.isEmpty()) return;

        int cx = Library.getChunkPosX(cpLong);
        int cz = Library.getChunkPosZ(cpLong);
        List<CarveSubTask> tasks = new ArrayList<>(masks.size());
        ObjectIterator<Int2ObjectMap.Entry<BitMask>> it = masks.int2ObjectEntrySet().fastIterator();
        while (it.hasNext()) {
            Int2ObjectMap.Entry<BitMask> e = it.next();
            int subY = e.getIntKey();
            BitMask bitset = e.getValue();
            CarveSubTask t = prepareOneSub(cx, cz, subY, bitset, storages);
            if (t != null) tasks.add(t);
            else bitset.free();
        }
        if (!tasks.isEmpty()) pendingCarves.offer(new PendingCarve(cpLong, tasks));
    }

    CarveSubTask prepareOneSub(int cx, int cz, int subY, BitMask subBitset, ExtendedBlockStorage[] storages) {
        ExtendedBlockStorage expected = storages[subY];
        if (expected == Chunk.NULL_BLOCK_STORAGE || expected.isEmpty()) return null;

        Long2ObjectOpenHashMap<IBlockState> modifiedLocal = TL_MODIFIED.get();
        LongOpenHashSet edges = TL_EDGES.get();
        modifiedLocal.clear();
        edges.clear();
        updateModified(cx, cz, subY, subBitset, expected, modifiedLocal);
        ExtendedBlockStorage carved = ChunkUtil.copyAndCarveLocal(world, cx, cz, subY, storages, subBitset, edges);
        CarveSubTask task = new CarveSubTask(subY, subBitset);
        task.expected = expected;
        task.carved = carved;
        task.modified.putAll(modifiedLocal);
        if (!edges.isEmpty()) {
            MutableBlockPos pos = TL_POS.get();
            LongIterator it = edges.iterator();
            while (it.hasNext()) {
                long lp = it.nextLong();
                task.neighborNotifies.add(lp);
                Library.fromLong(pos, lp);
                long nck = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
                int m = task.neighborMask.get(nck);
                m |= 1 << (pos.getY() >>> 4);
                task.neighborMask.put(nck, m);
            }
        }
        return task;
    }

    static void updateModified(int cx, int cz, int subY, BitMask subBitset, ExtendedBlockStorage expected,
                                       Long2ObjectOpenHashMap<IBlockState> modified) {
        if (!expected.isEmpty()) {
            int xBase = cx << 4;
            int yBase = subY << 4;
            int zBase = cz << 4;
            for (int idx = subBitset.nextSetBit(0); idx >= 0 && idx < 4096; idx = subBitset.nextSetBit(idx + 1)) {
                int x = Library.getLocalX(idx);
                int z = Library.getLocalZ(idx);
                int y = Library.getLocalY(idx);
                IBlockState state = expected.getData().get(idx);
                if (state.getBlock() != Blocks.AIR) {
                    long pos = Library.blockPosToLong(xBase | x, yBase | y, zBase | z);
                    modified.put(pos, state);
                }
            }
        }
    }

    @ServerThread
    void applyCarveJobOnMain(@NotNull PendingCarve job) {
        long ck = job.chunkPos();
        int cx = Library.getChunkPosX(ck);
        int cz = Library.getChunkPosZ(ck);
        Chunk chunk = world.getChunkProvider().loadedChunks.get(ck);
        if (chunk == null) {
            for (CarveSubTask t : job.tasks()) {
                enqueueForMissingChunk(ck, new ResumeItem(t.subY, t.mask));
            }
            maybeFinish();
            return;
        }
        ExtendedBlockStorage[] storages = chunk.getBlockStorageArray();
        Long2ObjectOpenHashMap<IBlockState> teRemovals = LONG2OBJECT_POOL.borrow();
        LongArrayList neighborNotifies = LONG_LIST_POOL.borrow();
        Long2IntOpenHashMap neighborMask = LONG2INT_POOL.borrow();
        teRemovals.clear();
        neighborNotifies.clear();
        neighborMask.clear();
        int selfMask = 0;

        for (int i = 0, n = job.tasks().size(); i < n; i++) {
            CarveSubTask t = job.tasks().get(i);
            int subY = t.subY;
            ExtendedBlockStorage cur = storages[subY];
            if (cur == Chunk.NULL_BLOCK_STORAGE || cur.isEmpty()) {
                t.mask.free();
                continue;
            }
            if (cur == t.expected) {
                storages[subY] = t.carved;
                selfMask |= (1 << subY);
                teRemovals.putAll(t.modified);
                for (int j = 0, m = t.neighborNotifies.size(); j < m; j++)
                    neighborNotifies.add(t.neighborNotifies.getLong(j));
                if (!t.neighborMask.isEmpty()) {
                    ObjectIterator<Long2IntMap.Entry> it = t.neighborMask.long2IntEntrySet().fastIterator();
                    while (it.hasNext()) {
                        Long2IntMap.Entry e = it.next();
                        neighborMask.put(e.getLongKey(), neighborMask.get(e.getLongKey()) | e.getIntValue());
                    }
                }
                t.mask.free();
            } else {
                BitMask mask = t.mask;
                ForkJoinPool p = pool;
                if (p == null || p.isShutdown()) {
                    enqueueForMissingChunk(ck, new ResumeItem(subY, mask));
                    continue;
                }
                p.submit(() -> {
                    ExtendedBlockStorage[] ebs = ChunkUtil.getLoadedEBS(mirror, ck);
                    if (ebs == null) {
                        enqueueForMissingChunk(ck, new ResumeItem(subY, mask));
                    } else {
                        CarveSubTask rebuilt = prepareOneSub(cx, cz, subY, mask, ebs);
                        if (rebuilt != null) pendingCarves.offer(new PendingCarve(ck, Collections.singletonList(rebuilt)));
                        else mask.free();
                        maybeFinish();
                    }
                });
            }
        }

        if (selfMask != 0) {
            notifyMainThread(ck, teRemovals, neighborNotifies, selfMask, neighborMask);
        } else {
            LONG2OBJECT_POOL.recycle(teRemovals);
            LONG_LIST_POOL.recycle(neighborNotifies);
            LONG2INT_POOL.recycle(neighborMask);
        }
    }

    void chunkFixup(long cpLong, Long2ObjectOpenHashMap<IBlockState> modified, LongArrayList neighborNotifies, int selfMask,
                            Long2IntOpenHashMap neighborMask) {
        sectionMaskByChunk.put(cpLong, sectionMaskByChunk.get(cpLong) | selfMask);
        ObjectIterator<Long2IntMap.Entry> iterator = neighborMask.long2IntEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2IntMap.Entry e = iterator.next();
            long cpk = e.getLongKey();
            int m = e.getIntValue();
            sectionMaskByChunk.put(cpk, sectionMaskByChunk.get(cpk) | m);
        }
        MutableBlockPos p = TL_POS.get();
        ObjectIterator<Long2ObjectMap.Entry<IBlockState>> modifiedIterator = modified.long2ObjectEntrySet().fastIterator();
        while (modifiedIterator.hasNext()) {
            Long2ObjectMap.Entry<IBlockState> entry = modifiedIterator.next();
            long lp = entry.getLongKey();
            IBlockState state = entry.getValue();
            Library.fromLong(p, lp);
            state.getBlock().breakBlock(world, p, state); // this should handle te removals
        }
        for (int i = 0, n = neighborNotifies.size(); i < n; i++) {
            long lp = neighborNotifies.getLong(i);
            Library.fromLong(p, lp);
            world.notifyNeighborsOfStateChange(p, Blocks.AIR, true);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        if (!CompatibilityConfig.isWarDim(world)) {
            U.putIntRelease(this, OFF_COLLECT_FINISHED, 1);
            U.putIntRelease(this, OFF_CONSOLIDATION_FINISHED, 1);
            U.putIntRelease(this, OFF_DESTROY_FINISHED, 1);
            return;
        }

        algorithm = nbt.getInteger("algorithm");
        rayCount = nbt.getInteger("rayCount");
        isContained = nbt.getBoolean("isContained");
        U.putIntRelease(this, OFF_COLLECT_FINISHED, nbt.getBoolean("collectDone") ? 1 : 0);
        U.putIntRelease(this, OFF_CONSOLIDATION_FINISHED, nbt.getBoolean("consolidateDone") ? 1 : 0);
        U.putIntRelease(this, OFF_DESTROY_FINISHED, nbt.getBoolean("destroyDone") ? 1 : 0);
        if (nbt.hasKey("sectionMask", Constants.NBT.TAG_LIST)) {
            NBTTagList list = nbt.getTagList("sectionMask", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound t = list.getCompoundTagAt(i);
                long ck = ChunkPos.asLong(t.getInteger("cX"), t.getInteger("cZ"));
                sectionMaskByChunk.put(ck, t.getInteger("mask"));
            }
        }
        if (nbt.hasKey("destructionMap", Constants.NBT.TAG_LIST)) {
            NBTTagList list = nbt.getTagList("destructionMap", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound tag = list.getCompoundTagAt(i);
                long ck = ChunkPos.asLong(tag.getInteger("cX"), tag.getInteger("cZ"));
                long[] bitsetData = ((NBTTagLongArray) tag.getTag("bitset")).data;
                ConcurrentBitSet bs = ConcurrentBitSet.fromLongArray(bitsetData, BITSET_SIZE);
                destructionMap.put(ck, bs);
            }
        }
        if (collectFinished != 0 && consolidationFinished != 0 && destroyFinished == 0) {
            if (U.compareAndSetInt(this, OFF_FINISH_QUEUED, 0, 1)) {
                world.addScheduledTask(() -> {
                    secondPass();
                    U.putIntRelease(this, OFF_DESTROY_FINISHED, 1);
                });
            }
        } else if (collectFinished == 0 || consolidationFinished == 0) {
            if (!CompatibilityConfig.isWarDim(world)) {
                U.putIntRelease(this, OFF_COLLECT_FINISHED, 1);
                U.putIntRelease(this, OFF_CONSOLIDATION_FINISHED, 1);
                U.putIntRelease(this, OFF_DESTROY_FINISHED, 1);
                return;
            }
            U.putIntRelease(this, OFF_PENDING_RAYS, rayCount);
            initializeAndStartWorkers();
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        if (!BombConfig.enableNukeNBTSaving) return;
        nbt.setInteger("algorithm", algorithm);
        nbt.setInteger("rayCount", rayCount);
        nbt.setBoolean("isContained", isContained);
        nbt.setBoolean("collectDone", collectFinished != 0);
        nbt.setBoolean("consolidateDone", consolidationFinished != 0);
        nbt.setBoolean("destroyDone", destroyFinished != 0);
        if (collectFinished != 0 && consolidationFinished != 0 && destroyFinished == 0 && !sectionMaskByChunk.isEmpty()) {
            NBTTagList list = new NBTTagList();
            ObjectIterator<Long2IntMap.Entry> iterator = sectionMaskByChunk.long2IntEntrySet().fastIterator();
            while (iterator.hasNext()) {
                Long2IntMap.Entry e = iterator.next();
                long ck = e.getLongKey();
                int mask = e.getIntValue();
                NBTTagCompound t = new NBTTagCompound();
                t.setInteger("cX", Library.getChunkPosX(ck));
                t.setInteger("cZ", Library.getChunkPosZ(ck));
                t.setInteger("mask", mask);
                list.appendTag(t);
            }
            nbt.setTag("sectionMask", list);
        }
        if (collectFinished == 0 && !destructionMap.isEmpty()) {
            NBTTagList list = new NBTTagList();
            destructionMap.forEach((long ck, ConcurrentBitSet bitset) -> {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("cX", Library.getChunkPosX(ck));
                tag.setInteger("cZ", Library.getChunkPosZ(ck));
                tag.setTag("bitset", new NBTTagLongArray(bitset.toLongArray()));
                list.appendTag(tag);
            });
            nbt.setTag("destructionMap", list);
        }
    }

    void handleMissingChunk(LocalAgg agg, long chunkPos, int dirIndex) {
        MpscIntArrayListCollector group = waitingRoom.get(chunkPos);
        if (group == null) {
            MpscIntArrayListCollector created = new MpscIntArrayListCollector();
            MpscIntArrayListCollector prev = waitingRoom.putIfAbsent(chunkPos, created);
            group = (prev != null) ? prev : created;
            if (prev == null) {
                chunkLoadQueue.offer(chunkPos);
                group.push(dirIndex);
                return;
            }
        }
        agg.deferMissing(chunkPos, dirIndex);
    }

    void flushDeferredMissing(LocalAgg agg) {
        if (!agg.hasDeferredMissing()) return;
        Long2ObjectOpenHashMap<IntArrayList> map = agg.missingChunks();
        ObjectIterator<Long2ObjectMap.Entry<IntArrayList>> iterator = map.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<IntArrayList> entry = iterator.next();
            IntArrayList list = entry.getValue();
            if (list == null || list.isEmpty()) continue;
            long chunkPos = entry.getLongKey();
            MpscIntArrayListCollector collector = waitingRoom.get(chunkPos);
            boolean amFirst = false;
            if (collector == null) {
                MpscIntArrayListCollector created = new MpscIntArrayListCollector();
                MpscIntArrayListCollector prev = waitingRoom.putIfAbsent(chunkPos, created);
                if (prev == null) {
                    collector = created;
                    amFirst = true;
                } else {
                    collector = prev;
                }
            }
            collector.pushBatch(list);
            boolean requeued = ensureCollectorRegistered(chunkPos, collector);
            if (amFirst || requeued) chunkLoadQueue.offer(chunkPos);
            list.clear();
            INT_LIST_POOL.recycle(list);
        }
        map.clear();
    }

    boolean ensureCollectorRegistered(long chunkPos, MpscIntArrayListCollector collector) {
        while (true) {
            MpscIntArrayListCollector current = waitingRoom.get(chunkPos);
            if (current == collector) return false;
            if (current == null) {
                if (waitingRoom.putIfAbsent(chunkPos, collector) == null) return true;
                continue;
            }
            IntArrayList drained = collector.drain();
            if (!drained.isEmpty()) {
                for (int i = drained.size() - 1; i >= 0; i--) {
                    current.push(drained.getInt(i));
                }
            }
            collector = current;
        }
    }

    void enqueueForMissingChunk(long chunkPos, ResumeItem item) {
        MpscUnboundedXaddArrayQueue<ResumeItem> q = postLoadQueues.get(chunkPos);
        if (q == null) {
            MpscUnboundedXaddArrayQueue<ResumeItem> created = new MpscUnboundedXaddArrayQueue<>(64);
            MpscUnboundedXaddArrayQueue<ResumeItem> prev = postLoadQueues.putIfAbsent(chunkPos, created);
            q = prev == null ? created : prev;
            if (prev == null) chunkLoadQueue.offer(chunkPos);
        }
        q.offer(item);
    }

    boolean traceSingle(int dirIndex, LocalAgg agg) {
        double energy = strength * INITIAL_ENERGY_FACTOR;
        double px = explosionX, py = explosionY, pz = explosionZ;
        int x = originX, y = originY, z = originZ;
        double dirY = 1.0 - (dirIndex * invRayIndexScale) * 2.0;
        double r = Math.sqrt(Math.max(0.0, 1.0 - dirY * dirY));
        double t = GOLDEN_ANGLE * dirIndex;
        double dirX = Math.cos(t) * r;
        double dirZ = Math.sin(t) * r;
        double currentRayPosition = 0.0;
        double absDirX = Math.abs(dirX);
        int stepX = (absDirX < RAY_DIRECTION_EPSILON) ? 0 : (dirX > 0 ? 1 : -1);
        double tDeltaX = (stepX == 0) ? Double.POSITIVE_INFINITY : 1.0 / absDirX;

        double absDirY = Math.abs(dirY);
        int stepY = (absDirY < RAY_DIRECTION_EPSILON) ? 0 : (dirY > 0 ? 1 : -1);
        double tDeltaY = (stepY == 0) ? Double.POSITIVE_INFINITY : 1.0 / absDirY;

        double absDirZ = Math.abs(dirZ);
        int stepZ = (absDirZ < RAY_DIRECTION_EPSILON) ? 0 : (dirZ > 0 ? 1 : -1);
        double tDeltaZ = (stepZ == 0) ? Double.POSITIVE_INFINITY : 1.0 / absDirZ;

        double minDeltaT = Math.min(tDeltaX, Math.min(tDeltaY, tDeltaZ));
        double radiusLimit = radius - PROCESSING_EPSILON;
        boolean useAggDamage = (algorithm == 2);

        double tMaxX = (stepX == 0) ? Double.POSITIVE_INFINITY : ((stepX > 0 ? (x + 1 - px) : (px - x)) * tDeltaX);
        double tMaxY = (stepY == 0) ? Double.POSITIVE_INFINITY : ((stepY > 0 ? (y + 1 - py) : (py - y)) * tDeltaY);
        double tMaxZ = (stepZ == 0) ? Double.POSITIVE_INFINITY : ((stepZ > 0 ? (z + 1 - pz) : (pz - z)) * tDeltaZ);

        long cachedCPLong = 0L;
        ExtendedBlockStorage[] storages = null;
        int lastCX = Integer.MIN_VALUE, lastCZ = Integer.MIN_VALUE;
        int chunkMinX = 0, chunkMaxX = 0, chunkMinZ = 0, chunkMaxZ = 0;
        int emptySubMask = 0;
        long bitsetCPLong = Long.MIN_VALUE;
        ConcurrentBitSet currentBits = null;
        int loopCount = 0;

        try {
            if (energy <= 0) return true;
            while (energy > 0) {
                if ((loopCount++ & 0x3FF) == 0) {
                    if (destroyFinished != 0 || y < 0 || y >= WORLD_HEIGHT || Thread.currentThread().isInterrupted()) break;
                    if (currentRayPosition >= radiusLimit) break;
                } else {
                    if (currentRayPosition >= radiusLimit) break;
                    if (y < 0 || y >= WORLD_HEIGHT) break;
                }
                int cx = x >> 4;
                int cz = z >> 4;

                if (cx != lastCX || cz != lastCZ) {
                    cachedCPLong = ChunkPos.asLong(cx, cz);
                    storages = ChunkUtil.getLoadedEBS(mirror, cachedCPLong);
                    if (storages == null) {
                        handleMissingChunk(agg, cachedCPLong, dirIndex);
                        return false;
                    }
                    lastCX = cx;
                    lastCZ = cz;
                    chunkMinX = cx << 4;
                    chunkMaxX = chunkMinX + 16;
                    chunkMinZ = cz << 4;
                    chunkMaxZ = chunkMinZ + 16;
                    int mask = 0;
                    for (int i = 0; i < storages.length; i++) {
                        ExtendedBlockStorage s = storages[i];
                        if (s == Chunk.NULL_BLOCK_STORAGE || s.isEmpty()) {
                            mask |= (1 << i);
                        }
                    }
                    emptySubMask = mask;
                    if (!useAggDamage) {
                        if (bitsetCPLong != cachedCPLong) {
                            ConcurrentBitSet bs = destructionMap.get(cachedCPLong);
                            if (bs == null) {
                                ConcurrentBitSet created = new ConcurrentBitSet(BITSET_SIZE);
                                ConcurrentBitSet prev = destructionMap.putIfAbsent(cachedCPLong, created);
                                bs = (prev != null) ? prev : created;
                            }
                            currentBits = bs;
                            bitsetCPLong = cachedCPLong;
                        }
                    }
                }

                int subY = y >> 4;
                boolean subIsEmpty = ((emptySubMask >>> subY) & 1) != 0;
                if (subIsEmpty) {
                    double minY = (subY << 4);
                    double maxY = minY + 16.0;

                    double tExitAbs = Double.POSITIVE_INFINITY;
                    if (stepX > 0) tExitAbs = Math.min(tExitAbs, (chunkMaxX - px) * tDeltaX);
                    else if (stepX < 0) tExitAbs = Math.min(tExitAbs, (px - chunkMinX) * tDeltaX);
                    if (stepY > 0) tExitAbs = Math.min(tExitAbs, (maxY - py) * tDeltaY);
                    else if (stepY < 0) tExitAbs = Math.min(tExitAbs, (py - minY) * tDeltaY);
                    if (stepZ > 0) tExitAbs = Math.min(tExitAbs, (chunkMaxZ - pz) * tDeltaZ);
                    else if (stepZ < 0) tExitAbs = Math.min(tExitAbs, (pz - chunkMinZ) * tDeltaZ);

                    double deltaT = Math.min(tExitAbs, radius) - currentRayPosition;
                    if (deltaT <= PROCESSING_EPSILON) deltaT = minDeltaT;
                    currentRayPosition += deltaT;
                    if (currentRayPosition >= radiusLimit) break;

                    final double bias = 1e-9;
                    x = (int) Math.floor(px + dirX * (currentRayPosition - bias));
                    y = (int) Math.floor(py + dirY * (currentRayPosition - bias));
                    z = (int) Math.floor(pz + dirZ * (currentRayPosition - bias));

                    tMaxX = (stepX == 0) ? Double.POSITIVE_INFINITY : ((stepX > 0 ? (x + 1 - px) : (px - x)) * tDeltaX);
                    tMaxY = (stepY == 0) ? Double.POSITIVE_INFINITY : ((stepY > 0 ? (y + 1 - py) : (py - y)) * tDeltaY);
                    tMaxZ = (stepZ == 0) ? Double.POSITIVE_INFINITY : ((stepZ > 0 ? (z + 1 - pz) : (pz - z)) * tDeltaZ);
                    continue;
                }
                ExtendedBlockStorage storage = storages[subY];

                int xLocal = x & 0xF, yLocal = y & 0xF, zLocal = z & 0xF;
                IBlockState state = storage.get(xLocal, yLocal, zLocal);

                double tExitVoxel = Math.min(tMaxX, Math.min(tMaxY, tMaxZ));
                double segLen = tExitVoxel - currentRayPosition;
                double remaining = radius - currentRayPosition;
                if (remaining <= PROCESSING_EPSILON) break;
                boolean clipAtRadius = segLen > remaining - 1e-12;
                if (clipAtRadius) segLen = remaining;

                if (state.getBlock() != Blocks.AIR && segLen > PROCESSING_EPSILON) {
                    float resistance = getNukeResistance(state);
                    if (resistance >= NUKE_RESISTANCE_CUTOFF) {
                        energy = 0;
                    } else {
                        double distFrac = Math.max(currentRayPosition, MIN_EFFECTIVE_DIST_FOR_ENERGY_CALC) * invRadius;
                        double energyLoss = getEnergyLossFactor(resistance, distFrac) * segLen;
                        energy -= energyLoss;

                        if (useAggDamage) {
                            int bitIndex = ((WORLD_HEIGHT - 1 - y) << 8) | ((x & 0xF) << 4) | (z & 0xF);
                            double damageInc = Math.max(DAMAGE_PER_BLOCK * segLen, energyLoss) * INITIAL_ENERGY_FACTOR;
                            agg.recordHit(cachedCPLong, bitIndex, damageInc, segLen);
                        } else if (energy > 0) {
                            if (energyLoss > 0) {
                                int bitIndex = ((WORLD_HEIGHT - 1 - y) << 8) | ((x & 0xF) << 4) | (z & 0xF);
                                currentBits.set(bitIndex);
                            }
                        }
                    }
                }

                currentRayPosition = tExitVoxel;
                if (energy <= 0.0 || clipAtRadius) break;
                if (tMaxX < tMaxY) {
                    if (tMaxX < tMaxZ) {
                        x += stepX;
                        tMaxX += tDeltaX;
                    } else {
                        z += stepZ;
                        tMaxZ += tDeltaZ;
                    }
                } else {
                    if (tMaxY < tMaxZ) {
                        y += stepY;
                        tMaxY += tDeltaY;
                    } else {
                        z += stepZ;
                        tMaxZ += tDeltaZ;
                    }
                }
            }

            if (energy > 0) isContained = false;
            return true;
        } catch (Exception e) {
            MainRegistry.logger.error("Ray {} finished exceptionally", dirIndex, e);
            return true;
        }
    }

    void mergeLocalAgg(LocalAgg agg) {
        if (algorithm != 2) return;
        ObjectIterator<Long2ObjectMap.Entry<IntDoubleAccumulator>> damageIter = agg.localDamage.long2ObjectEntrySet().fastIterator();
        while (damageIter.hasNext()) {
            Long2ObjectMap.Entry<IntDoubleAccumulator> entry = damageIter.next();
            long ck = entry.getLongKey();
            IntDoubleAccumulator accumulator = entry.getValue();
            aggMap.computeIfAbsent(ck, _ -> new ChunkAgg()).merge(accumulator, true);
        }
        ObjectIterator<Long2ObjectMap.Entry<IntDoubleAccumulator>> lenIter = agg.localLen.long2ObjectEntrySet().fastIterator();
        while (lenIter.hasNext()) {
            Long2ObjectMap.Entry<IntDoubleAccumulator> entry = lenIter.next();
            long ck = entry.getLongKey();
            IntDoubleAccumulator accumulator = entry.getValue();
            aggMap.computeIfAbsent(ck, _ -> new ChunkAgg()).merge(accumulator, false);
        }
        agg.localDamage.clear();
        agg.localLen.clear();
        agg.clear();
    }

    interface CarveApplier {
        void apply(long cpLong, ExtendedBlockStorage[] storages, Int2ObjectOpenHashMap<BitMask> masks);
    }

    static final class CarveSubTask {
        final int subY;
        final BitMask mask;
        final Long2ObjectOpenHashMap<IBlockState> modified = new Long2ObjectOpenHashMap<>(64);
        final LongArrayList neighborNotifies = new LongArrayList(128);
        final Long2IntOpenHashMap neighborMask = new Long2IntOpenHashMap();
        ExtendedBlockStorage expected;
        ExtendedBlockStorage carved;

        CarveSubTask(int subY, BitMask mask) {
            this.subY = subY;
            this.mask = mask;
        }
    }

    record ResumeItem(int kind, int subY, BitMask mask, Int2ObjectOpenHashMap<BitMask> masks, ChunkAgg agg) {
        static final int CARVE = 0;
        static final int APPLY_MASKS = 1;
        static final int APPLY_AGG = 2;

        ResumeItem(int subY, BitMask mask) {
            this(CARVE, subY, mask, null, null);
        }

        ResumeItem(Int2ObjectOpenHashMap<BitMask> masks) {
            this(APPLY_MASKS, 0, null, masks, null);
        }

        ResumeItem(ChunkAgg agg) {
            this(APPLY_AGG, 0, null, null, agg);
        }
    }

    record PendingCarve(long chunkPos, List<CarveSubTask> tasks) {
    }

    static final class IntDoubleAccumulator {
        static final int EMPTY = Integer.MIN_VALUE;
        static final int BASE_CAPACITY = 128;
        static final int MAX_RETAINED_CAPACITY = 4096;
        static final double LOAD = 0.50;
        static final int HASH_MUL = 0xC2B2AE35;

        int[] keys;
        double[] vals;
        int mask;
        int shift;
        int size;
        int resizeThreshold;

        IntDoubleAccumulator() {
            this(BASE_CAPACITY);
        }

        IntDoubleAccumulator(int expected) {
            init(capFor(expected));
        }

        void init(int capacity) {
            keys = new int[capacity];
            Arrays.fill(keys, EMPTY);
            vals = new double[capacity];
            mask = capacity - 1;
            shift = 32 - Integer.numberOfTrailingZeros(capacity);
            size = 0;
            resizeThreshold = (int) (capacity * LOAD);
        }

        void clear() {
            if (keys.length > MAX_RETAINED_CAPACITY) {
                init(BASE_CAPACITY);
                return;
            }
            Arrays.fill(keys, EMPTY);
            size = 0;
        }

        void add(int key, double delta) {
            int[] ks = keys;
            double[] vs = vals;
            int m = mask;
            int idx = (key * HASH_MUL) >>> shift;
            while (true) {
                int k = ks[idx];
                if (k == EMPTY) {
                    ks[idx] = key;
                    vs[idx] = delta;
                    if (++size >= resizeThreshold) rehash();
                    return;
                }
                if (k == key) {
                    vs[idx] += delta;
                    return;
                }
                idx = (idx + 1) & m;
            }
        }

        int size() {
            return size;
        }

        void accumulateTo(Int2DoubleOpenHashMap map) {
            int[] ks = keys;
            double[] vs = vals;
            for (int i = 0; i < ks.length; i++) {
                int k = ks[i];
                if (k != EMPTY) map.addTo(k, vs[i]);
            }
        }

        void rehash() {
            int[] oldK = keys;
            double[] oldV = vals;
            int newCap = oldK.length << 1;
            int[] newK = new int[newCap];
            Arrays.fill(newK, EMPTY);
            double[] newV = new double[newCap];
            int newMask = newCap - 1;
            int newShift = 32 - Integer.numberOfTrailingZeros(newCap);
            int newThreshold = (int) (newCap * LOAD);
            int newSize = 0;
            for (int i = 0; i < oldK.length; i++) {
                int k = oldK[i];
                if (k == EMPTY) continue;

                int idx = (k * HASH_MUL) >>> newShift;
                while (newK[idx] != EMPTY) idx = (idx + 1) & newMask;
                newK[idx] = k;
                newV[idx] = oldV[i];
                newSize++;
            }
            keys = newK;
            vals = newV;
            mask = newMask;
            shift = newShift;
            resizeThreshold = newThreshold;
            size = newSize;
        }
    }

    static final class LocalAgg {
        final Long2ObjectOpenHashMap<IntDoubleAccumulator> localDamage = new Long2ObjectOpenHashMap<>(16);
        final Long2ObjectOpenHashMap<IntDoubleAccumulator> localLen = new Long2ObjectOpenHashMap<>(16);
        final Long2ObjectOpenHashMap<IntArrayList> deferredMissing = new Long2ObjectOpenHashMap<>(4);
        long lastCp = Long.MIN_VALUE;
        IntDoubleAccumulator lastDmg;
        IntDoubleAccumulator lastLen;

        void clear() {
            if (!localDamage.isEmpty()) {
                ObjectIterator<Long2ObjectMap.Entry<IntDoubleAccumulator>> it = localDamage.long2ObjectEntrySet().fastIterator();
                while (it.hasNext()) ACC_POOL.recycle(it.next().getValue());
                localDamage.clear();
            }
            if (!localLen.isEmpty()) {
                ObjectIterator<Long2ObjectMap.Entry<IntDoubleAccumulator>> it = localLen.long2ObjectEntrySet().fastIterator();
                while (it.hasNext()) ACC_POOL.recycle(it.next().getValue());
                localLen.clear();
            }
            lastCp = Long.MIN_VALUE;
            lastDmg = null;
            lastLen = null;

            if (!deferredMissing.isEmpty()) {
                for (IntArrayList list : deferredMissing.values()) {
                    list.clear();
                    INT_LIST_POOL.recycle(list);
                }
                deferredMissing.clear();
            }
        }

        void deferMissing(long chunkPos, int dirIndex) {
            IntArrayList list = deferredMissing.get(chunkPos);
            if (list == null) {
                list = INT_LIST_POOL.borrow();
                deferredMissing.put(chunkPos, list);
            }
            list.add(dirIndex);
        }

        boolean hasDeferredMissing() {
            return !deferredMissing.isEmpty();
        }

        Long2ObjectOpenHashMap<IntArrayList> missingChunks() {
            return deferredMissing;
        }

        void recordHit(long chunkPos, int bitIndex, double damageInc, double segLen) {
            if (damageInc <= 0.0 && segLen <= 0.0) return;

            if (chunkPos != lastCp) {
                lastCp = chunkPos;
                lastDmg = localDamage.get(chunkPos);
                lastLen = localLen.get(chunkPos);
            }

            if (damageInc > 0.0) {
                IntDoubleAccumulator acc = lastDmg;
                if (acc == null) {
                    acc = ACC_POOL.borrow();
                    localDamage.put(chunkPos, acc);
                    lastDmg = acc;
                }
                acc.add(bitIndex, damageInc);
            }

            if (segLen > 0.0) {
                IntDoubleAccumulator acc = lastLen;
                if (acc == null) {
                    acc = ACC_POOL.borrow();
                    localLen.put(chunkPos, acc);
                    lastLen = acc;
                }
                acc.add(bitIndex, segLen);
            }
        }
    }

    static final class ChunkAgg {
        final Int2DoubleOpenHashMap damage = new Int2DoubleOpenHashMap(64);
        final Int2DoubleOpenHashMap passLen = new Int2DoubleOpenHashMap(64);
        final MpscUnboundedXaddArrayQueue<IntDoubleAccumulator> qDmg = new MpscUnboundedXaddArrayQueue<>(64);
        final MpscUnboundedXaddArrayQueue<IntDoubleAccumulator> qLen = new MpscUnboundedXaddArrayQueue<>(64);

        void merge(@NotNull IntDoubleAccumulator accumulator, boolean isDamage) {
            int sz = accumulator.size();
            if (sz == 0) {
                ACC_POOL.recycle(accumulator);
                return;
            }
            if (isDamage) qDmg.offer(accumulator);
            else qLen.offer(accumulator);
        }

        void drainUnlocked() {
            IntDoubleAccumulator a;
            while ((a = qDmg.poll()) != null) {
                if (a.size() > 0) a.accumulateTo(damage);
                ACC_POOL.recycle(a);
            }
            while ((a = qLen.poll()) != null) {
                if (a.size() > 0) a.accumulateTo(passLen);
                ACC_POOL.recycle(a);
            }
        }

        void clear() {
            damage.clear();
            passLen.clear();

            IntDoubleAccumulator a;
            while ((a = qDmg.poll()) != null) ACC_POOL.recycle(a);
            while ((a = qLen.poll()) != null) ACC_POOL.recycle(a);
        }
    }

    final class FastApplier implements CarveApplier {
        @Override
        public void apply(long cpLong, ExtendedBlockStorage[] storages, Int2ObjectOpenHashMap<BitMask> masks) {
            if (masks == null || masks.isEmpty()) return;
            Long2ObjectOpenHashMap<IBlockState> teRemovals = LONG2OBJECT_POOL.borrow();
            LongArrayList neighborNotifies = LONG_LIST_POOL.borrow();
            Long2IntOpenHashMap neighborMask = LONG2INT_POOL.borrow();
            teRemovals.clear();
            neighborNotifies.clear();
            neighborMask.clear();
            int selfMask = 0;
            ObjectIterator<Int2ObjectMap.Entry<BitMask>> it = masks.int2ObjectEntrySet().fastIterator();
            while (it.hasNext()) {
                Int2ObjectMap.Entry<BitMask> e = it.next();
                BitMask bs = e.getValue();
                selfMask = carveSubchunkAndSwap(bs, cpLong, storages, teRemovals, neighborNotifies, selfMask, neighborMask, e.getIntKey());
                bs.free();
            }
            if (selfMask != 0) {
                notifyMainThread(cpLong, teRemovals, neighborNotifies, selfMask, neighborMask);
            } else {
                LONG2OBJECT_POOL.recycle(teRemovals);
                LONG_LIST_POOL.recycle(neighborNotifies);
                LONG2INT_POOL.recycle(neighborMask);
            }
        }
    }

    final class SafeApplier implements CarveApplier {
        @Override
        public void apply(long cpLong, ExtendedBlockStorage[] storages, Int2ObjectOpenHashMap<BitMask> masks) {
            prepareAndEnqueue(cpLong, masks, storages);
        }
    }

    final class ConsolidateAggTask extends RecursiveAction {
        final long[] keys;
        final int start, end, threshold;

        ConsolidateAggTask(long[] keys, int start, int end, int threshold) {
            this.keys = keys;
            this.start = start;
            this.end = end;
            this.threshold = Math.max(1, threshold);
        }

        @Override
        protected void compute() {
            int len = end - start;
            if (len <= threshold) {
                for (int i = start; i < end; i++) {
                    long cpLong = keys[i];
                    ChunkAgg agg = aggMap.get(cpLong);
                    if (agg == null) continue;
                    agg.drainUnlocked();
                    ExtendedBlockStorage[] storages = ChunkUtil.getLoadedEBS(mirror, cpLong);
                    aggMap.remove(cpLong);
                    if (storages == null) {
                        enqueueForMissingChunk(cpLong, new ResumeItem(agg));
                    } else {
                        applier.apply(cpLong, storages, buildMasksFromAgg(agg, storages));
                        agg.clear();
                    }
                }
            } else {
                int mid = start + (len >>> 1);
                invokeAll(new ConsolidateAggTask(keys, start, mid, threshold), new ConsolidateAggTask(keys, mid, end, threshold));
            }
        }
    }

    final class ConsolidateMaskTask extends RecursiveAction {
        final long[] keys;
        final int start, end, threshold;

        ConsolidateMaskTask(long[] keys, int start, int end, int threshold) {
            this.keys = keys;
            this.start = start;
            this.end = end;
            this.threshold = Math.max(1, threshold);
        }

        @Override
        protected void compute() {
            int len = end - start;
            if (len <= threshold) {
                for (int i = start; i < end; i++) {
                    long cpLong = keys[i];
                    ConcurrentBitSet chunkBitSet = destructionMap.get(cpLong);
                    if (chunkBitSet == null || chunkBitSet.isEmpty()) {
                        destructionMap.remove(cpLong);
                        continue;
                    }

                    ExtendedBlockStorage[] storages = ChunkUtil.getLoadedEBS(mirror, cpLong);
                    Int2ObjectOpenHashMap<BitMask> masks = splitBySubchunk(chunkBitSet);

                    if (storages == null) {
                        enqueueForMissingChunk(cpLong, new ResumeItem(masks));
                    } else {
                        applier.apply(cpLong, storages, masks);
                    }

                    destructionMap.remove(cpLong);
                }
            } else {
                int mid = start + (len >>> 1);
                invokeAll(new ConsolidateMaskTask(keys, start, mid, threshold), new ConsolidateMaskTask(keys, mid, end, threshold));
            }
        }
    }

    class RayTracerTask extends RecursiveAction {
        final int start, end, threshold;

        RayTracerTask(int start, int end, int threshold) {
            this.start = start;
            this.end = end;
            this.threshold = Math.max(1, threshold);
        }

        @Override
        protected void compute() {
            int len = end - start;
            if (len <= threshold) {
                LocalAgg agg = TL_LOCAL_AGG.get();
                agg.clear();
                int completed = 0;
                for (int i = start; i < end; i++) {
                    if (Thread.currentThread().isInterrupted() || destroyFinished != 0) break;
                    int dirIndex = (rayOrder != null) ? rayOrder[i] : i;
                    if (traceSingle(dirIndex, agg)) completed++;
                }
                flushDeferredMissing(agg);
                mergeLocalAgg(agg);
                if (completed > 0) {
                    int prev = U.getAndAddInt(ExplosionNukeRayParallelized.this, OFF_PENDING_RAYS, -completed);
                    if (prev - completed == 0) onAllRaysFinished();
                }
            } else {
                int mid = start + (len >>> 1);
                invokeAll(new RayTracerTask(start, mid, threshold), new RayTracerTask(mid, end, threshold));
            }
        }
    }

    class ResumeBatchTask extends RecursiveAction {
        final IntArrayList indices;
        final int start, end, threshold;

        ResumeBatchTask(IntArrayList indices, int start, int end, int threshold) {
            this.indices = indices;
            this.start = start;
            this.end = end;
            this.threshold = Math.max(1, threshold);
        }

        @Override
        protected void compute() {
            int len = end - start;
            if (len <= threshold) {
                LocalAgg agg = TL_LOCAL_AGG.get();
                agg.clear();
                int completed = 0;
                for (int i = start; i < end; i++) {
                    int dirIndex = indices.getInt(i);
                    if (Thread.currentThread().isInterrupted() || destroyFinished != 0) break;
                    if (traceSingle(dirIndex, agg)) completed++;
                }
                flushDeferredMissing(agg);
                mergeLocalAgg(agg);
                if (completed > 0) {
                    int prev = U.getAndAddInt(ExplosionNukeRayParallelized.this, OFF_PENDING_RAYS, -completed);
                    if (prev - completed == 0) onAllRaysFinished();
                }
            } else {
                int mid = start + (len >>> 1);
                invokeAll(new ResumeBatchTask(indices, start, mid, threshold), new ResumeBatchTask(indices, mid, end, threshold));
            }
        }
    }
}
