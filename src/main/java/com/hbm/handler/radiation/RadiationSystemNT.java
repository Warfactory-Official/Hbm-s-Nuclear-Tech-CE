package com.hbm.handler.radiation;

import com.hbm.Tags;
import com.hbm.config.CompatibilityConfig;
import com.hbm.config.GeneralConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.IRadResistantBlock;
import com.hbm.interfaces.ServerThread;
import com.hbm.lib.Library;
import com.hbm.lib.TLPool;
import com.hbm.lib.maps.NonBlockingHashMapLong;
import com.hbm.lib.maps.NonBlockingHashSetLong;
import com.hbm.lib.maps.NonBlockingLong2LongHashMap;
import com.hbm.lib.queues.MpscUnboundedXaddArrayLongQueue;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.saveddata.AuxSavedData;
import com.hbm.util.DecodeException;
import com.hbm.util.ObjectPool;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BitArray;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.*;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import org.jctools.queues.SpscArrayQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Field;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.function.LongConsumer;

import static com.hbm.lib.internal.UnsafeHolder.*;

/**
 * A concurrent radiation system using Operator Splitting with exact pairwise exchange.
 * <p>
 * It solves for radiation density (&rho;) using the analytical solution for 2-node diffusion:
 * <center>
 * &Delta;&rho; = (&rho;<sub>eq</sub>&minus; &rho;) &times; (1 &minus; e<sup>-k&Delta;t</sup>)
 * </center>
 *
 * @author mlbv
 */
@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(modid = Tags.MODID)
public final class RadiationSystemNT {

    static final int NO_POCKET = 15, NEI_SLOTS = 16, NEI_SHIFT = 1;
    static final int[] FACE_DX = {0, 0, 0, 0, -1, 1}, FACE_DY = {-1, 1, 0, 0, 0, 0}, FACE_DZ = {0, 0, -1, 1, 0, 0};
    static final int[] FACE_PLANE = new int[6 * 256];
    static final int SECTION_BLOCK_COUNT = 4096;
    static final ConcurrentMap<WorldServer, WorldRadiationData> worldMap = new ConcurrentHashMap<>(4);
    private static final int[] BOUNDARY_MASKS = {0, 0, 0xF00, 0xF00, 0xFF0, 0xFF0}, LINEAR_OFFSETS = {-256, 256, -16, 16, -1, 1};
    private static final int PROFILE_WINDOW = 200;
    private static final String TAG_RAD = "hbmRadDataNT";
    private static final byte MAGIC_0 = (byte) 'N', MAGIC_1 = (byte) 'T', MAGIC_2 = (byte) 'X', FMT = 6;
    private static final Object NOT_RES = new Object();
    private static final ForkJoinPool RAD_POOL = ForkJoinPool.commonPool(); // safe: we don't lock in sim path
    private static final ThreadLocal<int[]> TL_FF_QUEUE = ThreadLocal.withInitial(() -> new int[SECTION_BLOCK_COUNT]);
    private static final ThreadLocal<PalScratch> TL_PAL_SCRATCH = ThreadLocal.withInitial(PalScratch::new);
    private static final ThreadLocal<int[]> TL_VOL_COUNTS = ThreadLocal.withInitial(() -> new int[NO_POCKET]);
    private static final ThreadLocal<double[]> TL_NEW_MASS = ThreadLocal.withInitial(() -> new double[NO_POCKET]);
    private static final ThreadLocal<double[]> TL_OLD_MASS = ThreadLocal.withInitial(() -> new double[NO_POCKET]);
    private static final ThreadLocal<int[]> TL_OVERLAPS = ThreadLocal.withInitial(() -> new int[NO_POCKET * NO_POCKET]);
    private static final ThreadLocal<long[]> TL_SUM_X = ThreadLocal.withInitial(() -> new long[NO_POCKET]);
    private static final ThreadLocal<long[]> TL_SUM_Y = ThreadLocal.withInitial(() -> new long[NO_POCKET]);
    private static final ThreadLocal<long[]> TL_SUM_Z = ThreadLocal.withInitial(() -> new long[NO_POCKET]);

    // Scratch for applyQueuedWrites
    private static final ThreadLocal<double[]> TL_ADD = ThreadLocal.withInitial(() -> new double[NO_POCKET + 1]);
    private static final ThreadLocal<double[]> TL_SET = ThreadLocal.withInitial(() -> new double[NO_POCKET + 1]);
    private static final ThreadLocal<boolean[]> TL_HAS_SET = ThreadLocal.withInitial(() -> new boolean[NO_POCKET + 1]);

    private static final double RAD_EPSILON = 1.0e-5D;
    private static final double RAD_MAX = Double.MAX_VALUE / 2.0D;

    private static final int ACTIVE_STRIPES = computeActiveStripes(4);
    private static final int ACTIVE_STRIPE_SHIFT = 64 - Integer.numberOfTrailingZeros(ACTIVE_STRIPES);
    private static final int MAX_BYTES = Short.BYTES + 16 * (NO_POCKET + 1) * (Byte.BYTES + Double.BYTES); // 2306
    private static final ByteBuffer BUF = ByteBuffer.allocateDirect(MAX_BYTES);
    private static final double[] TEMP_DENSITIES = new double[NO_POCKET];
    private static final long DESTROY_PROB_U64 = Long.divideUnsigned(-1L, 100L);
    private static long fogProbU64;
    private static long ticks;
    private static @NotNull CompletableFuture<Void> radiationFuture = CompletableFuture.completedFuture(null);
    private static Object[] STATE_CLASS;
    private static int tickDelay = 1;
    private static double dT = tickDelay / 20.0D;
    private static double diffusionDt = 10.0 * dT;
    private static double UU_E = Math.exp(-(diffusionDt / 128.0d));
    private static double retentionDt = Math.pow(0.99424, dT); // 2min

    static {
        int[] rowShifts = {4, 4, 8, 8, 8, 8}, colShifts = {0, 0, 0, 0, 4, 4}, bases = {0, 15 << 8, 0, 15 << 4, 0, 15};
        for (int face = 0; face < 6; face++) {
            int base = face << 8;
            int rowShift = rowShifts[face];
            int colShift = colShifts[face];
            int fixedBits = bases[face];
            int t = 0;
            for (int r = 0; r < 16; r++) {
                int rBase = r << rowShift;
                for (int c = 0; c < 16; c++) {
                    FACE_PLANE[base + (t++)] = rBase | (c << colShift) | fixedBits;
                }
            }
        }
    }

    private RadiationSystemNT() {
    }

    private static int computeActiveStripes(int scale) {
        int p = RAD_POOL.getParallelism();
        if (p < 3) p = 1;
        return HashCommon.nextPowerOfTwo(p * scale);
    }

    private static int getTaskThreshold(int size, int minGrain) {
        int p = RAD_POOL.getParallelism();
        if (p <= 1) return size;
        int target = p * 4;
        int th = size / target;
        return Math.max(minGrain, th);
    }

    public static void onLoadComplete() {
        // noinspection deprecation
        STATE_CLASS = new Object[Block.BLOCK_STATE_IDS.size() + 1024];
        tickDelay = RadiationConfig.radTickRate;
        if (tickDelay <= 0) throw new IllegalStateException("Radiation tick rate must be positive");
        dT = tickDelay / 20.0D;
        diffusionDt = RadiationConfig.radDiffusivity * dT;
        if (diffusionDt <= 0.0D || !Double.isFinite(diffusionDt))
            throw new IllegalStateException("Radiation diffusivity must be positive and finite");
        UU_E = Math.exp(-(diffusionDt / 128.0d));
        double hl = RadiationConfig.radHalfLifeSeconds;
        if (hl <= 0.0D || !Double.isFinite(hl))
            throw new IllegalStateException("Radiation HalfLife must be positive and finite");
        retentionDt = Math.exp(Math.log(0.5) * (dT / hl));
        double ch = RadiationConfig.fogCh;
        fogProbU64 = (ch > 0.0D && Double.isFinite(ch)) ? probU64(dT / ch) : 0L;
    }

    private static long probU64(double p) {
        if (!(p > 0.0D) || !Double.isFinite(p)) return 0L;
        if (p >= 1.0D) return -1L;
        double v = p * 4294967296.0D;
        long hi = (long) v;
        if (hi >= 0x1_0000_0000L) return -1L;
        double frac = v - (double) hi;
        long lo = (long) (frac * 4294967296.0D);
        if (lo >= 0x1_0000_0000L) lo = 0xFFFF_FFFFL;
        return (hi << 32) | (lo & 0xFFFF_FFFFL);
    }

    public static void onServerStopping() {
        try {
            radiationFuture.join();
        } catch (Exception e) {
            MainRegistry.logger.error("Radiation system error during shutdown.", e);
        }
    }

    public static void onServerStopped() {
        worldMap.clear();
    }

    public static CompletableFuture<Void> onServerTickLast(TickEvent.ServerTickEvent e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation || e.phase != Phase.END)
            return CompletableFuture.completedFuture(null);
        ticks++;
        if ((ticks + 17) % tickDelay == 0) {
            // to be immediately joined on server thread
            // this provides a quiescent server thread and sufficient happens-before for structural updates
            return radiationFuture = CompletableFuture.runAsync(RadiationSystemNT::runParallelSimulation, RAD_POOL);
        }
        return CompletableFuture.completedFuture(null);
    }

    @SubscribeEvent
    public static void onWorldUpdate(TickEvent.WorldTickEvent e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation || e.world.isRemote) return;
        WorldServer worldServer = (WorldServer) e.world;

        if (e.phase == Phase.START) {
            RadiationWorldHandler.handleWorldDestruction(worldServer);
        }
        if (GeneralConfig.enableRads) {
            int thunder = AuxSavedData.getThunder(worldServer);
            if (thunder > 0) AuxSavedData.setThunder(worldServer, thunder - 1);
        }
    }

    @ServerThread
    public static void jettisonData(WorldServer world) {
        WorldRadiationData data = worldMap.get(world);
        if (data == null) return;
        data.clearAllSections();
        data.clearDirtyAll();
        data.clearQueues();
        data.destructionQueue.clear(true);
        data.clearPendingAll();
        data.cleanupPools();
        data.clearQueuedWrites();
        for (Chunk chunk : world.getChunkProvider().loadedChunks.values()) {
            ChunkRef cr = data.onChunkLoaded(chunk);
            for (int sy = 0; sy < 16; sy++) {
                if (cr.getKind(sy) == ChunkRef.KIND_NONE) {
                    data.markDirty(Library.sectionToLong(chunk.x, sy, chunk.z));
                }
            }
        }
    }

    @ServerThread
    public static void incrementRad(WorldServer world, BlockPos pos, double amount, double max) {
        if (Math.abs(amount) < RAD_EPSILON || isOutsideWorldY(pos.getY())) return;
        long posLong = pos.toLong();
        long sck = Library.blockPosToSectionLong(posLong);
        long ck = Library.sectionToChunkLong(sck);
        Chunk chunk = world.getChunkProvider().loadedChunks.get(ck);
        if (chunk == null) return;
        if (isResistantAt(world, chunk, pos)) return;
        WorldRadiationData data = getWorldRadData(world);
        ChunkRef owner = data.getOrCreateChunkRef(ck);
        owner.mcChunk = chunk;

        int sy = Library.getSectionY(sck);

        int kind = owner.getKind(sy);
        if (kind == ChunkRef.KIND_NONE || data.dirtySections.contains(sck)) {
            int local = Library.blockPosToLocal(posLong);
            data.queueAdd(sck, local, amount);
            data.markDirty(sck);
            chunk.markDirty();
            return;
        }

        int pocketIndex;
        if (kind == ChunkRef.KIND_UNI) {
            pocketIndex = 0;
        } else {
            pocketIndex = owner.sec[sy].getPocketIndex(posLong);
        }

        if (pocketIndex < 0) return;

        double current;
        if (kind == ChunkRef.KIND_UNI) {
            current = owner.uniformRads[sy];
        } else if (kind == ChunkRef.KIND_SINGLE) {
            current = ((SingleMaskedSectionRef) owner.sec[sy]).rad;
        } else {
            current = ((MultiSectionRef) owner.sec[sy]).data[pocketIndex << 1];
        }

        if (current >= max) return;
        double next = current + amount;
        if (next > max) next = max;
        next = data.sanitize(next);
        if (next != current) {
            if (kind == ChunkRef.KIND_UNI) {
                owner.uniformRads[sy] = next;
            } else if (kind == ChunkRef.KIND_SINGLE) {
                ((SingleMaskedSectionRef) owner.sec[sy]).rad = next;
            } else {
                ((MultiSectionRef) owner.sec[sy]).data[pocketIndex << 1] = next;
            }

            if (next != 0.0D) {
                if (owner.setActiveBit(sy, pocketIndex)) {
                    data.enqueueActiveNext(pocketKey(sck, pocketIndex));
                }
            }
            chunk.markDirty();
        }
    }

    @ServerThread
    public static void decrementRad(WorldServer world, BlockPos pos, double amount) {
        if (Math.abs(amount) < RAD_EPSILON || isOutsideWorldY(pos.getY())) return;

        long posLong = pos.toLong();
        long sck = Library.blockPosToSectionLong(posLong);
        long ck = Library.sectionToChunkLong(sck);
        Chunk chunk = world.getChunkProvider().loadedChunks.get(ck);
        if (chunk == null) return;
        if (isResistantAt(world, chunk, pos)) return;
        WorldRadiationData data = getWorldRadData(world);
        ChunkRef owner = data.getOrCreateChunkRef(ck);
        owner.mcChunk = chunk;
        int sy = Library.getSectionY(sck);
        int kind = owner.getKind(sy);
        if (kind == ChunkRef.KIND_NONE || data.dirtySections.contains(sck)) {
            int local = Library.blockPosToLocal(posLong);
            data.queueAdd(sck, local, -amount);
            data.markDirty(sck);
            chunk.markDirty();
            return;
        }
        int pocketIndex;
        SectionRef ref = owner.sec[sy];
        if (kind == ChunkRef.KIND_UNI) {
            pocketIndex = 0;
        } else {
            pocketIndex = ref.getPocketIndex(posLong);
        }
        if (pocketIndex < 0) return;
        double current;
        if (kind == ChunkRef.KIND_UNI) {
            current = owner.uniformRads[sy];
        } else if (kind == ChunkRef.KIND_SINGLE) {
            current = ((SingleMaskedSectionRef) ref).rad;
        } else {
            current = ((MultiSectionRef) ref).data[pocketIndex << 1];
        }
        if (current == 0.0D && data.minBound == 0.0D) return;
        double next = data.sanitize(current - amount);
        if (kind == ChunkRef.KIND_UNI) {
            owner.uniformRads[sy] = next;
        } else if (kind == ChunkRef.KIND_SINGLE) {
            ((SingleMaskedSectionRef) ref).rad = next;
        } else {
            ((MultiSectionRef) ref).data[pocketIndex << 1] = next;
        }
        long pk = pocketKey(sck, pocketIndex);
        if (next != 0.0D) {
            if (owner.setActiveBit(sy, pocketIndex)) {
                data.enqueueActiveNext(pk);
            }
        } else {
            owner.clearActiveBit(sy, pocketIndex);
        }
        chunk.markDirty();
    }

    /**
     * @param amount clamped to [-backGround, Double.MAX_VALUE / 2]
     */
    @ServerThread
    public static void setRadForCoord(WorldServer world, BlockPos pos, double amount) {
        if (isOutsideWorldY(pos.getY())) return;
        long posLong = pos.toLong();
        long sck = Library.blockPosToSectionLong(posLong);
        long ck = Library.sectionToChunkLong(sck);
        Long2ObjectMap<Chunk> loaded = world.getChunkProvider().loadedChunks;
        Chunk chunk = loaded.get(ck);
        if (chunk == null) return;
        if (isResistantAt(world, chunk, pos)) return;
        WorldRadiationData data = getWorldRadData(world);
        ChunkRef owner = data.getOrCreateChunkRef(ck);
        owner.mcChunk = chunk;
        int sy = Library.getSectionY(sck);
        int kind = owner.getKind(sy);
        if (kind == ChunkRef.KIND_NONE || data.dirtySections.contains(sck)) {
            int local = Library.blockPosToLocal(posLong);
            data.queueSet(sck, local, amount);
            data.markDirty(sck);
            chunk.markDirty();
            return;
        }

        int pocketIndex;
        if (kind == ChunkRef.KIND_UNI) {
            pocketIndex = 0;
        } else {
            pocketIndex = owner.sec[sy].getPocketIndex(posLong);
        }
        if (pocketIndex < 0) return;

        double v = data.sanitize(amount);

        if (kind == ChunkRef.KIND_UNI) {
            owner.uniformRads[sy] = v;
        } else if (kind == ChunkRef.KIND_SINGLE) {
            ((SingleMaskedSectionRef) owner.sec[sy]).rad = v;
        } else {
            ((MultiSectionRef) owner.sec[sy]).data[pocketIndex << 1] = v;
        }

        long pk = pocketKey(sck, pocketIndex);
        if (v != 0.0D) {
            if (owner.setActiveBit(sy, pocketIndex)) {
                data.enqueueActiveNext(pk);
            }
        } else {
            owner.clearActiveBit(sy, pocketIndex);
        }
        chunk.markDirty();
    }

    @ServerThread
    public static double getRadForCoord(WorldServer world, BlockPos pos) {
        if (isOutsideWorldY(pos.getY())) return 0D;
        long posLong = pos.toLong();
        long sck = Library.blockPosToSectionLong(posLong);
        long ck = Library.sectionToChunkLong(sck);
        Chunk chunk = world.getChunkProvider().loadedChunks.get(ck);
        if (chunk == null) return 0D;
        WorldRadiationData data = worldMap.get(world);
        if (data == null) return 0D;
        if (isResistantAt(world, chunk, pos)) return 0D;
        SectionRef sc = data.getSection(sck);
        int sy = Library.getSectionY(sck);
        ChunkRef owner = data.chunkRefs.get(ck);
        if (owner == null) return 0D;

        int kind = owner.getKind(sy);
        if (kind == ChunkRef.KIND_NONE) {
            data.markDirty(sck);
            return 0D;
        }
        if (kind == ChunkRef.KIND_UNI) {
            return owner.uniformRads[sy];
        }
        // Kind is SINGLE or MULTI, sc should be valid
        if (sc == null || sc.pocketCount <= 0) {
            data.markDirty(sck);
            return 0D;
        }
        int pocketIndex = sc.getPocketIndex(posLong);
        if (pocketIndex < 0) return 0D;
        if (kind == ChunkRef.KIND_SINGLE) {
            return ((SingleMaskedSectionRef) sc).rad;
        } else {
            return ((MultiSectionRef) sc).data[pocketIndex << 1];
        }
    }

    @ServerThread
    public static void markSectionForRebuild(World world, BlockPos pos) {
        if (world.isRemote || !GeneralConfig.advancedRadiation) return;
        if (isOutsideWorldY(pos.getY())) return;
        WorldServer ws = (WorldServer) world;
        long sck = Library.blockPosToSectionLong(pos);
        long ck = Library.sectionToChunkLong(sck);
        Chunk chunk = ws.getChunkProvider().loadedChunks.get(ck);
        if (chunk == null) return;
        WorldRadiationData data = getWorldRadData(ws);
        data.markDirty(sck);
        chunk.markDirty();
    }

    @ServerThread
    static void handleWorldDestruction(WorldServer world) {
        WorldRadiationData data = worldMap.get(world);
        if (data == null) return;

        long pocketKey;
        if (tickDelay == 1) {
            pocketKey = data.pocketToDestroy;
            data.pocketToDestroy = Long.MIN_VALUE;
        } else {
            pocketKey = data.destructionQueue.poll();
        }
        if (pocketKey == Long.MIN_VALUE) return;

        int cx = Library.getSectionX(pocketKey);
        int yz = Library.getSectionY(pocketKey);
        int cz = Library.getSectionZ(pocketKey);
        int cy = yz >>> 4;
        int targetPocketIndex = yz & 15;

        ChunkRef cr = data.chunkRefs.get(Library.sectionToChunkLong(pocketKey));
        if (cr == null) return;
        int kind = cr.getKind(cy);
        if (kind == ChunkRef.KIND_NONE) return;

        int baseX = cx << 4;
        int baseY = cy << 4;
        int baseZ = cz << 4;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Chunk mcChunk = cr.mcChunk;
        ExtendedBlockStorage storage = mcChunk.getBlockStorageArray()[cy];
        if (storage == null || storage.isEmpty()) return;
        BlockStateContainer container = storage.data;

        if (kind == ChunkRef.KIND_UNI) {
            if (targetPocketIndex != 0) return;
            for (int i = 0; i < SECTION_BLOCK_COUNT; i++) {
                if (world.rand.nextInt(3) != 0) continue;
                IBlockState state = container.get(i);
                if (state.getMaterial() == Material.AIR) continue;
                int lx = Library.getLocalX(i);
                int lz = Library.getLocalZ(i);
                int ly = Library.getLocalY(i);
                int topY = mcChunk.getHeightValue(lx, lz) - 1;
                int myY = baseY + ly;
                if (myY < topY - 1 || myY > topY) continue;
                pos.setPos(baseX + lx, myY, baseZ + lz);
                RadiationWorldHandler.decayBlock(world, pos, state);
            }
            return;
        }

        SectionRef sc = cr.sec[cy];

        for (int i = 0; i < SECTION_BLOCK_COUNT; i++) {
            if (world.rand.nextInt(3) != 0) continue;
            int actualPocketIndex = sc.paletteIndexOrNeg(i);
            if (actualPocketIndex < 0) continue;
            if (actualPocketIndex != targetPocketIndex) continue;
            IBlockState state = container.get(i);
            if (state.getMaterial() == Material.AIR) continue;
            int lx = Library.getLocalX(i);
            int lz = Library.getLocalZ(i);
            int ly = Library.getLocalY(i);
            int topY = mcChunk.getHeightValue(lx, lz) - 1;
            int myY = baseY + ly;
            if (myY < topY - 1 || myY > topY) continue;
            pos.setPos(baseX + lx, myY, baseZ + lz);
            RadiationWorldHandler.decayBlock(world, pos, state);
        }
    }

    private static void runParallelSimulation() {
        WorldRadiationData[] all = worldMap.values().toArray(new WorldRadiationData[0]);
        int n = all.length;
        if (n == 0) return;

        if (n == 1) {
            WorldRadiationData data = all[0];
            if (data.world.getMinecraftServer() == null) return;
            try {
                data.processWorldSimulation();
            } catch (Throwable t) {
                MainRegistry.logger.error("Error in async rad simulation for world {}", data.world.provider.getDimension(), t);
            }
        } else {
            ForkJoinTask<?>[] tasks = new ForkJoinTask<?>[n];
            for (int i = 0; i < n; i++) {
                WorldRadiationData data = all[i];
                tasks[i] = ForkJoinTask.adapt(() -> {
                    if (data.world.getMinecraftServer() == null) return;
                    try {
                        data.processWorldSimulation();
                    } catch (Throwable t) {
                        MainRegistry.logger.error("Error in async rad simulation for world {}", data.world.provider.getDimension(), t);
                    }
                });
            }
            ForkJoinTask.invokeAll(tasks);
        }
    }

    @SuppressWarnings("AutoBoxing")
    private static void logLifetimeProfiling(@Nullable WorldRadiationData data) {
        if (!GeneralConfig.enableDebugMode || data == null) return;
        long steps = data.profSteps;
        if (steps <= 0) return;
        int dimId = data.world.provider.getDimension();
        String dimType = data.world.provider.getDimensionType().getName();
        double avgMs = data.profTotalMs / (double) steps;
        DoubleArrayList samples = data.profSamplesMs;
        if (samples == null || samples.isEmpty()) {
            MainRegistry.logger.info("[RadiationSystemNT] dim {} ({}) lifetime: steps={}, avg={} ms, max={} ms", dimId, dimType, steps, r3(avgMs),
                    r3(data.profMaxMs));
            return;
        }
        double[] a = samples.toDoubleArray();
        Arrays.parallelSort(a);
        int n = a.length;
        int k1 = Math.max(1, (int) Math.ceil(n * 0.01));
        int k01 = Math.max(1, (int) Math.ceil(n * 0.001));
        double onePctHighAvg = meanOfLargestK(a, k1);
        double pointOnePctHigh = meanOfLargestK(a, k01);
        double p99 = a[Math.min(n - 1, (int) Math.ceil(n * 0.99) - 1)];
        double p999 = a[Math.min(n - 1, (int) Math.ceil(n * 0.999) - 1)];
        MainRegistry.logger.info(
                "[RadiationSystemNT] dim {} ({}) lifetime: steps={}, avg={} ms, 1% high(avg)={} ms, 0.1% high(avg)={} ms, p99={} ms, p999={} ms, max={} ms",
                dimId, dimType, steps, r3(avgMs), r3(onePctHighAvg), r3(pointOnePctHigh), r3(p99), r3(p999), r3(data.profMaxMs));
        data.profSamplesMs = null;
    }

    private static double meanOfLargestK(double[] sortedAscending, int k) {
        int n = sortedAscending.length;
        int start = n - k;
        double sum = 0.0;
        for (int i = start; i < n; i++) sum += sortedAscending[i];
        return sum / (double) k;
    }

    private static double r3(double v) {
        return Math.rint(v * 1000.0) / 1000.0;
    }

    // @formatter:off
    private static void runExactExchangeSweeps(WorldRadiationData data, LongBag wakeBag) {
        ChunkRef[][] b = data.parityBuckets;
        int[] c = data.parityCounts;
        int c0 = c[0], c1 = c[1], c2 = c[2], c3 = c[3];
        int th0 = getTaskThreshold(c0, 64), th1 = getTaskThreshold(c1, 64), th2 = getTaskThreshold(c2, 64), th3 = getTaskThreshold(c3, 64);
        int s = data.workEpoch;
        boolean fx = (s & 1) != 0, fz = (s & 2) != 0;
        int yPar = (s & 4) != 0 ? 1 : 0;
        int perm = s % 6;
        if (perm < 0) perm += 6;
        switch (perm) {
            case 0 -> { sweepX(s, b, c0, c1, c2, c3, th0, th1, th2, th3, wakeBag, fx);sweepZ(s, b, c0, c1, c2, c3, th0, th1, th2, th3, wakeBag, fz);sweepY(s, b, c0, c1, c2, c3, th0, th1, th2, th3, wakeBag, yPar); }
            case 1 -> { sweepX(s, b, c0, c1, c2, c3, th0, th1, th2, th3, wakeBag, fx);sweepY(s, b, c0, c1, c2, c3, th0, th1, th2, th3, wakeBag, yPar);sweepZ(s, b, c0, c1, c2, c3, th0, th1, th2, th3, wakeBag, fz); }
            case 2 -> { sweepY(s, b, c0, c1, c2, c3, th0, th1, th2, th3, wakeBag, yPar);sweepZ(s, b, c0, c1, c2, c3, th0, th1, th2, th3, wakeBag, fz);sweepX(s, b, c0, c1, c2, c3, th0, th1, th2, th3, wakeBag, fx); }
            case 3 -> { sweepY(s, b, c0, c1, c2, c3, th0, th1, th2, th3, wakeBag, yPar);sweepX(s, b, c0, c1, c2, c3, th0, th1, th2, th3, wakeBag, fx);sweepZ(s, b, c0, c1, c2, c3, th0, th1, th2, th3, wakeBag, fz); }
            case 4 -> { sweepZ(s, b, c0, c1, c2, c3, th0, th1, th2, th3, wakeBag, fz);sweepX(s, b, c0, c1, c2, c3, th0, th1, th2, th3, wakeBag, fx);sweepY(s, b, c0, c1, c2, c3, th0, th1, th2, th3, wakeBag, yPar); }
            default -> { sweepZ(s, b, c0, c1, c2, c3, th0, th1, th2, th3, wakeBag, fz);sweepY(s, b, c0, c1, c2, c3, th0, th1, th2, th3, wakeBag, yPar);sweepX(s, b, c0, c1, c2, c3, th0, th1, th2, th3, wakeBag, fx); }
        }
    }

    private static void sweepX(int e, ChunkRef[][] b, int c0, int c1, int c2, int c3, int th0, int th1, int th2, int th3, LongBag bag, boolean flip) {
        var t0 = new DiffuseXTask(e, b[0], 0, c0, bag, th0);
        var t1 = new DiffuseXTask(e, b[1], 0, c1, bag, th1);
        var t2 = new DiffuseXTask(e, b[2], 0, c2, bag, th2);
        var t3 = new DiffuseXTask(e, b[3], 0, c3, bag, th3);
        if (flip) { ForkJoinTask.invokeAll(t1, t3); ForkJoinTask.invokeAll(t0, t2); }
        else      { ForkJoinTask.invokeAll(t0, t2); ForkJoinTask.invokeAll(t1, t3); }
    }

    private static void sweepZ(int e, ChunkRef[][] b, int c0, int c1, int c2, int c3, int th0, int th1, int th2, int th3, LongBag bag, boolean flip) {
        var t0 = new DiffuseZTask(e, b[0], 0, c0, bag, th0);
        var t1 = new DiffuseZTask(e, b[1], 0, c1, bag, th1);
        var t2 = new DiffuseZTask(e, b[2], 0, c2, bag, th2);
        var t3 = new DiffuseZTask(e, b[3], 0, c3, bag, th3);
        if (flip) { ForkJoinTask.invokeAll(t2, t3); ForkJoinTask.invokeAll(t0, t1); }
        else      { ForkJoinTask.invokeAll(t0, t1); ForkJoinTask.invokeAll(t2, t3); }
    }

    private static void sweepY(int e, ChunkRef[][] b, int c0, int c1, int c2, int c3, int th0, int th1, int th2, int th3, LongBag bag, int startParity) {
        for (int p = 0; p < 2; p++) {
            int parity = startParity ^ p;
            invokeAll4(new DiffuseYTask(e, b[0], 0, c0, parity, bag, th0), new DiffuseYTask(e, b[1], 0, c1, parity, bag, th1), new DiffuseYTask(e, b[2], 0, c2, parity, bag, th2), new DiffuseYTask(e, b[3], 0, c3, parity, bag, th3));
        }
    }

    private static void invokeAll4(ForkJoinTask<?> a, ForkJoinTask<?> b, ForkJoinTask<?> c, ForkJoinTask<?> d) {
        a.fork();b.fork();c.fork();d.invoke();c.join();b.join();a.join();
    } // @formatter:on

    private static void exchangeFaceExact(long aKey, ChunkRef crA, int faceA,
                                          long bKey, ChunkRef crB, int faceB,
                                          LongBag wakeBag, int epoch) {
        int syA = Library.getSectionY(aKey);
        int syB = Library.getSectionY(bKey);

        int kA = crA.getKind(syA);
        int kB = crB.getKind(syB);

        if (kA == ChunkRef.KIND_UNI && kB == ChunkRef.KIND_UNI) {
            //hoist values that are likely to fall into the same cache line
            double[] uniA = crA.uniformRads;
            Chunk cA = crA.mcChunk;
            double[] uniB = crB.uniformRads;
            Chunk cB = crB.mcChunk;
            double ra = uniA[syA];
            double rb = uniB[syB];
            if (ra == rb) return;

            double avg = 0.5d * (ra + rb);
            double delta = 0.5d * (ra - rb) * UU_E;
            double na = avg + delta;
            double nb = avg - delta;

            uniA[syA] = na;
            uniB[syB] = nb;
            if (na != 0.0D && crA.markUniEpoch(syA, epoch)) {
                crA.setActiveBit(syA, 0);
                wakeBag.tryAdd(pocketKey(aKey, 0));
            }
            if (nb != 0.0D && crB.markUniEpoch(syB, epoch)) {
                crB.setActiveBit(syB, 0);
                wakeBag.tryAdd(pocketKey(bKey, 0));
            }
            cA.markDirty();
            if (crA == crB) return;
            cB.markDirty();
            return;
        }

        boolean changed;

        if (kA == ChunkRef.KIND_UNI) {
            changed = crB.sec[syB].exchangeWithUniform(crA, bKey, aKey, faceB, faceA, syA, wakeBag, epoch);
        } else if (kB == ChunkRef.KIND_UNI) {
            changed = crA.sec[syA].exchangeWithUniform(crB, aKey, bKey, faceA, faceB, syB, wakeBag, epoch);
        } else {
            if (kB == ChunkRef.KIND_SINGLE) {
                changed = crA.sec[syA].exchangeWithSingle((SingleMaskedSectionRef) crB.sec[syB], aKey, bKey, faceA, faceB, wakeBag, epoch);
            } else {
                changed = crA.sec[syA].exchangeWithMulti((MultiSectionRef) crB.sec[syB], aKey, bKey, faceA, faceB, wakeBag, epoch);
            }
        }

        if (changed) {
            crA.mcChunk.markDirty();
            if (crA == crB) return;
            crB.mcChunk.markDirty();
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;

        WorldRadiationData data = getWorldRadData((WorldServer) e.getWorld());
        Chunk chunk = e.getChunk();
        ChunkRef cr = data.onChunkLoaded(chunk);
        for (int sy = 0; sy < 16; sy++) {
            if (cr.getKind(sy) == ChunkRef.KIND_NONE) {
                data.markDirty(Library.sectionToLong(chunk.x, sy, chunk.z));
            }
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;

        WorldRadiationData data = getWorldRadData((WorldServer) e.getWorld());
        Chunk chunk = e.getChunk();
        data.unloadChunk(chunk.x, chunk.z);
    }

    @SubscribeEvent
    public static void onChunkDataLoad(ChunkDataEvent.Load e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;
        WorldRadiationData data = getWorldRadData((WorldServer) e.getWorld());

        Chunk chunk = e.getChunk();
        NBTTagCompound nbt = e.getData();
        try {
            byte[] payload = null;
            int id = nbt.getTagId(TAG_RAD);
            if (id == Constants.NBT.TAG_COMPOUND) {
                WorldProvider provider = e.getWorld().provider;
                MainRegistry.logger.warn("[RadiationSystemNT] Skipped legacy radiation data for chunk {} in dimension {} ({})", chunk.getPos(),
                        provider.getDimension(), provider.getDimensionType().getName());
            } else if (id == Constants.NBT.TAG_BYTE_ARRAY) {
                byte[] raw = nbt.getByteArray(TAG_RAD);
                payload = verifyPayload(raw);
            }
            data.readPayload(chunk.x, chunk.z, payload);
        } catch (BufferUnderflowException | DecodeException ex) {
            WorldProvider provider = e.getWorld().provider;
            MainRegistry.logger.error("[RadiationSystemNT] Failed to decode data for chunk {} in dimension {} ({})", chunk.getPos(),
                    provider.getDimension(), provider.getDimensionType().getName(), ex);
            nbt.removeTag(TAG_RAD);
        }
    }

    @SubscribeEvent //can happen before or after ChunkUnload
    public static void onChunkDataSave(ChunkDataEvent.Save e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;
        WorldRadiationData data = getWorldRadData((WorldServer) e.getWorld());

        Chunk chunk = e.getChunk();
        byte[] payload = data.tryEncodePayload(chunk.x, chunk.z);

        if (payload != null && payload.length > 0) {
            e.getData().setByteArray(TAG_RAD, payload);
        } else if (e.getData().hasKey(TAG_RAD)) {
            e.getData().removeTag(TAG_RAD);
        }

        // Garbage Collect ONLY if the chunk is actually unloaded
        long ck = ChunkPos.asLong(chunk.x, chunk.z);
        if (!data.world.getChunkProvider().loadedChunks.containsKey(ck)) {
            data.removeChunkRef(ck);
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;
        worldMap.computeIfAbsent((WorldServer) e.getWorld(), WorldRadiationData::new);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload e) {
        if (e.getWorld().isRemote) return;
        WorldRadiationData data = worldMap.remove((WorldServer) e.getWorld());
        logLifetimeProfiling(data);
    }

    private static byte[] verifyPayload(byte[] raw) throws DecodeException {
        if (raw.length == 0) return null;
        if (raw.length < 6) throw new DecodeException("Payload too short: " + raw.length);
        if (raw[0] != MAGIC_0 || raw[1] != MAGIC_1 || raw[2] != MAGIC_2) throw new DecodeException("Invalid magic");
        byte fmt = raw[3];
        if (fmt != FMT) throw new DecodeException("Unknown format: " + fmt);
        return raw;
    }

    @NotNull
    private static WorldRadiationData getWorldRadData(WorldServer world) {
        return worldMap.computeIfAbsent(world, WorldRadiationData::new);
    }

    private static boolean isResistantAt(WorldServer w, Chunk chunk, BlockPos pos) {
        Block b = chunk.getBlockState(pos).getBlock();
        return (b instanceof IRadResistantBlock r) && r.isRadResistant(w, pos);
    }

    private static boolean isOutsideWorldY(int y) {
        return (y & ~255) != 0;
    }

    private static boolean isInvalidSectionY(int sy) {
        return (sy & ~15) != 0;
    }

    private static long pocketKey(long sectionKey, int pocketIndex) {
        int sy = Library.getSectionY(sectionKey);
        // sectionKey allows sy ∈ [-524_288, 524_287]
        return Library.setSectionY(sectionKey, (sy << 4) | (pocketIndex & 15));
    }

    private static int pocketIndexFromPocketKey(long pocketKey) {
        return Library.getSectionY(pocketKey) & 15;
    }

    private static long sectionKeyFromPocketKey(long pocketKey) {
        int yz = Library.getSectionY(pocketKey);
        return Library.setSectionY(pocketKey, (yz >>> 4) & 15);
    }

    private static void writeNibble(byte[] pocketData, int blockIndex, int paletteIndex) {
        int byteIndex = blockIndex >> 1;
        int b = pocketData[byteIndex] & 0xFF;
        if ((blockIndex & 1) == 0) {
            b = (b & 0x0F) | ((paletteIndex & 0x0F) << 4);
        } else {
            b = (b & 0xF0) | (paletteIndex & 0x0F);
        }
        pocketData[byteIndex] = (byte) b;
    }

    static int readNibble(byte[] pocketData, int blockIndex) {
        int byteIndex = blockIndex >> 1;
        int b = pocketData[byteIndex] & 0xFF;
        return ((blockIndex & 1) == 0) ? ((b >> 4) & 0x0F) : (b & 0x0F);
    }

    @SuppressWarnings("deprecation")
    private static SectionMask scanResistantMask(WorldServer world, long sectionKey, @Nullable ExtendedBlockStorage ebs) {
        if (ebs == null || ebs.isEmpty()) return null;

        BlockStateContainer c = ebs.getData();
        BitArray storage = c.storage;
        IBlockStatePalette pal = c.palette;

        int baseX = Library.getSectionX(sectionKey) << 4;
        int baseY = Library.getSectionY(sectionKey) << 4;
        int baseZ = Library.getSectionZ(sectionKey) << 4;
        BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();

        long[] data = storage.getBackingLongArray();
        int bits = c.bits;
        long entryMask = (1L << bits) - 1L;
        int stateSize = Block.BLOCK_STATE_IDS.size();
        Object[] cache = STATE_CLASS;
        if (cache == null || cache.length < stateSize) cache = ensureStateClassCapacity(stateSize);

        if (pal == BlockStateContainer.REGISTRY_BASED_PALETTE) {
            SectionMask mask = null;
            int li = 0, bo = 0;

            for (int idx = 0; idx < SECTION_BLOCK_COUNT; idx++) {
                int globalId;
                if (bo + bits <= 64) {
                    globalId = (int) ((data[li] >>> bo) & entryMask);
                    bo += bits;
                    if (bo == 64) {
                        bo = 0;
                        li++;
                    }
                } else {
                    int spill = 64 - bo;
                    long v = (data[li] >>> bo) | (data[li + 1] << spill);
                    globalId = (int) (v & entryMask);
                    li++;
                    bo = bits - spill;
                }

                if (globalId < 0 || globalId >= stateSize) {
                    int newSize = Block.BLOCK_STATE_IDS.size();
                    if (globalId < 0 || globalId >= newSize) continue;
                    stateSize = newSize;
                    if (cache.length < stateSize) cache = ensureStateClassCapacity(stateSize);
                }

                Object cls = cache[globalId];
                if (cls == null) {
                    IBlockState s = Block.BLOCK_STATE_IDS.getByValue(globalId);
                    if (s == null) {
                        cache[globalId] = NOT_RES;
                        continue;
                    }
                    Block b = s.getBlock();
                    Object nv = (b instanceof IRadResistantBlock) ? b : NOT_RES;
                    cache[globalId] = nv;
                    cls = nv;
                }

                if (cls == NOT_RES) continue;

                int x = Library.getLocalX(idx);
                int y = Library.getLocalY(idx);
                int z = Library.getLocalZ(idx);
                mp.setPos(baseX + x, baseY + y, baseZ + z);

                if (((IRadResistantBlock) cls).isRadResistant(world, mp)) {
                    if (mask == null) mask = new SectionMask();
                    mask.set(idx);
                }
            }
            return mask;
        }

        PalScratch sc = TL_PAL_SCRATCH.get();
        int gen = sc.nextGen();
        Object[] lcls = sc.cls;
        int[] lstamp = sc.stamp;

        boolean anyCandidate = false;

        if (pal instanceof BlockStatePaletteLinear p) {
            IBlockState[] states = p.states;
            int n = p.arraySize;

            for (int i = 0; i < n; i++) {
                IBlockState s = states[i];
                if (s == null) continue;

                int gid = Block.BLOCK_STATE_IDS.get(s);
                Object cls;
                if (gid < 0) {
                    cls = NOT_RES;
                } else {
                    if (gid >= stateSize) {
                        int newSize = Block.BLOCK_STATE_IDS.size();
                        if (gid >= newSize) {
                            cls = NOT_RES;
                        } else {
                            stateSize = newSize;
                            if (cache.length < stateSize) cache = ensureStateClassCapacity(stateSize);
                            cls = cache[gid];
                        }
                    } else {
                        cls = cache[gid];
                    }

                    if (cls == null) {
                        Block b = s.getBlock();
                        Object nv = (b instanceof IRadResistantBlock) ? b : NOT_RES;
                        cache[gid] = nv;
                        cls = nv;
                    }
                }

                lcls[i] = cls;
                lstamp[i] = gen;
                if (cls != NOT_RES) anyCandidate = true;
            }

            if (!anyCandidate) return null;

            SectionMask mask = null;
            int li = 0, bo = 0;

            for (int idx = 0; idx < SECTION_BLOCK_COUNT; idx++) {
                int localId;
                if (bo + bits <= 64) {
                    localId = (int) ((data[li] >>> bo) & entryMask);
                    bo += bits;
                    if (bo == 64) {
                        bo = 0;
                        li++;
                    }
                } else {
                    int spill = 64 - bo;
                    long v = (data[li] >>> bo) | (data[li + 1] << spill);
                    localId = (int) (v & entryMask);
                    li++;
                    bo = bits - spill;
                }
                if ((localId & ~255) != 0) continue;
                if (lstamp[localId] != gen) continue;
                Object cls = lcls[localId];
                if (cls == NOT_RES) continue;

                int x = Library.getLocalX(idx);
                int y = Library.getLocalY(idx);
                int z = Library.getLocalZ(idx);
                mp.setPos(baseX + x, baseY + y, baseZ + z);

                if (((IRadResistantBlock) cls).isRadResistant(world, mp)) {
                    if (mask == null) mask = new SectionMask();
                    mask.set(idx);
                }
            }
            return mask;
        }

        if (pal instanceof BlockStatePaletteHashMap p) {
            IntIdentityHashBiMap<IBlockState> map = p.statePaletteMap;
            Object[] byId = map.byId; // erasure

            int cap = 1 << bits;
            int lim = Math.min(cap, byId.length);

            for (int i = 0; i < lim; i++) {
                IBlockState s = (IBlockState) byId[i];
                if (s == null) continue;

                int gid = Block.BLOCK_STATE_IDS.get(s);
                Object cls;
                if (gid < 0) {
                    cls = NOT_RES;
                } else {
                    if (gid >= stateSize) {
                        int newSize = Block.BLOCK_STATE_IDS.size();
                        if (gid >= newSize) {
                            cls = NOT_RES;
                        } else {
                            stateSize = newSize;
                            if (cache.length < stateSize) cache = ensureStateClassCapacity(stateSize);
                            cls = cache[gid];
                        }
                    } else {
                        cls = cache[gid];
                    }

                    if (cls == null) {
                        Block b = s.getBlock();
                        Object nv = (b instanceof IRadResistantBlock) ? b : NOT_RES;
                        cache[gid] = nv;
                        cls = nv;
                    }
                }

                lcls[i] = cls;
                lstamp[i] = gen;
                if (cls != NOT_RES) anyCandidate = true;
            }

            if (!anyCandidate) return null;

            SectionMask mask = null;
            int li2 = 0, bo2 = 0;

            for (int idx = 0; idx < SECTION_BLOCK_COUNT; idx++) {
                int localId;
                if (bo2 + bits <= 64) {
                    localId = (int) ((data[li2] >>> bo2) & entryMask);
                    bo2 += bits;
                    if (bo2 == 64) {
                        bo2 = 0;
                        li2++;
                    }
                } else {
                    int spill = 64 - bo2;
                    long v = (data[li2] >>> bo2) | (data[li2 + 1] << spill);
                    localId = (int) (v & entryMask);
                    li2++;
                    bo2 = bits - spill;
                }

                if ((localId & ~255) != 0) continue;
                if (localId >= lim) continue;
                if (lstamp[localId] != gen) continue;

                Object cls = lcls[localId];
                if (cls == NOT_RES) continue;

                int x = Library.getLocalX(idx);
                int y = Library.getLocalY(idx);
                int z = Library.getLocalZ(idx);
                mp.setPos(baseX + x, baseY + y, baseZ + z);

                if (((IRadResistantBlock) cls).isRadResistant(world, mp)) {
                    if (mask == null) mask = new SectionMask();
                    mask.set(idx);
                }
            }
            return mask;
        }

        throw new UnsupportedOperationException("Unexpected palette format: " + pal.getClass());
    }

    // it seems that the size of total blockstate id count can fucking grow after FMLLoadCompleteEvent, making STATE_CLASS throw AIOOBE
    // I can't explain it, either there are registration happening after that event, or that ObjectIntIdentityMap went out
    // of sync internally (it uses IdentityHashMap to map blockstate to id, with an ArrayList to map ids back)
    // Anyway, we introduce a manual resize here to address this weird growth issue.
    private static Object[] ensureStateClassCapacity(int minSize) {
        Object[] a = STATE_CLASS;
        if (a != null && a.length >= minSize) return a;
        synchronized (RadiationSystemNT.class) {
            a = STATE_CLASS;
            if (a != null && a.length >= minSize) return a;

            int newLen = (a == null) ? 256 : a.length;
            while (newLen < minSize) newLen = newLen + (newLen >>> 1) + 16;
            STATE_CLASS = (a == null) ? new Object[newLen] : Arrays.copyOf(a, newLen);
            return STATE_CLASS;
        }
    }

    private static void processDiffuseGroup(ChunkRef aCr, ChunkRef bCr, long unionMask, int group, LongBag wakeBag, int epoch, int faceA, int faceB) {
        while (unionMask != 0L) {
            int lane = Long.numberOfTrailingZeros(unionMask) >>> 4;
            int sy = (group << 2) + lane;
            long laneMask = 0xFFFFL << (lane << 4);
            unionMask &= ~laneMask;
            int kA = aCr.getKind(sy);
            int kB = bCr.getKind(sy);
            if (kA == ChunkRef.KIND_NONE || kB == ChunkRef.KIND_NONE) continue;
            long aKey = Library.sectionToLong(aCr.ck, sy);
            long bKey = Library.sectionToLong(bCr.ck, sy);
            exchangeFaceExact(aKey, aCr, faceA, bKey, bCr, faceB, wakeBag, epoch);
        }
    }

    static final class ChunkRef {
        static final long MASK_BASE;
        static final long TOUCHED_EPOCH_OFF;
        static final int KIND_NONE = 0;
        static final int KIND_UNI = 1;
        static final int KIND_SINGLE = 2;
        static final int KIND_MULTI = 3;

        static {
            long off0 = fieldOffset(ChunkRef.class, "mask0");
            long off1 = fieldOffset(ChunkRef.class, "mask1");
            long off2 = fieldOffset(ChunkRef.class, "mask2");
            long off3 = fieldOffset(ChunkRef.class, "mask3");
            if (off1 - off0 != 8 || off2 - off1 != 8 || off3 - off2 != 8) {
                throw new AssertionError("Critical memory layout mismatch. Expected 8-byte strides for mask fields, " +
                        "but got: " + off0 + ", " + off1 + ", " + off2 + ", " + off3);
            }
            TOUCHED_EPOCH_OFF = fieldOffset(ChunkRef.class, "touchedEpoch");
            MASK_BASE = off0;
        }

        final long ck;
        // non-null if KIND is not NONE/UNI. If not, invariant breach should throw NPE
        final SectionRef[] sec = new SectionRef[16];
        final double[] uniformRads = new double[16];
        Chunk mcChunk; // non-null for async compute, nullable for server thread ops
        @Nullable ChunkRef north, south, west, east;
        int sectionKinds; // 2 bits per section
        int touchedEpoch;
        long mask0, mask1, mask2, mask3;
        int uniEpoch;
        int uniSeenMask;

        ChunkRef(long ck) {
            this.ck = ck;
        }

        void wakeUni(long sck, int sy, LongBag wakeBag, int epoch) {
            if (uniformRads[sy] == 0.0D) return;
            if (!markUniEpoch(sy, epoch)) return;
            setActiveBit(sy, 0);
            wakeBag.tryAdd(pocketKey(sck, 0));
        }

        boolean markUniEpoch(int sy, int epoch) {
            int bit = 1 << sy;
            if (uniEpoch != epoch) {
                uniEpoch = epoch;
                uniSeenMask = bit;
                return true;
            }
            int m = uniSeenMask;
            if ((m & bit) != 0) return false;
            uniSeenMask = m | bit;
            return true;
        }

        int getKind(int sy) {
            return (sectionKinds >>> (sy << 1)) & 3;
        }

        // only called at rebuild, should be safe to leave the fields plain
        void setKind(int sy, int kind) {
            int shift = sy << 1;
            int clear = ~(3 << shift);
            int set = (kind & 3) << shift;
            sectionKinds = (sectionKinds & clear) | set;
        }

        boolean isInactive(int sy, int pi) {
            long off = MASK_BASE + ((long) (sy >>> 2) << 3);
            long bit = 1L << (((sy & 3) << 4) + pi);
            return (U.getLong(this, off) & bit) == 0L;
        }

        boolean setActiveBit(int sy, int pi) {
            long off = MASK_BASE + ((long) (sy >>> 2) << 3);
            long bit = 1L << (((sy & 3) << 4) + pi);
            long cur = U.getLong(this, off);
            long next = cur | bit;
            if (cur == next) return false;
            U.putLong(this, off, next);
            return true;
        }

        void clearActiveBit(int sy, int pi) {
            long off = MASK_BASE + ((long) (sy >>> 2) << 3);
            long bit = 1L << (((sy & 3) << 4) + pi);
            long cur = U.getLong(this, off);
            long next = cur & ~bit;
            if (cur == next) return;
            U.putLong(this, off, next);
        }

        void clearActiveBitMask(int sy) {
            long off = MASK_BASE + ((long) (sy >>> 2) << 3);
            long mask = ~(0xFFFFL << ((sy & 3) << 4));
            long cur = U.getLong(this, off);
            long next = cur & mask;
            if (cur == next) return;
            U.putLong(this, off, next);
        }

        boolean tryMarkTouched(int epoch) {
            int cur = touchedEpoch;
            if (cur == epoch) return false;
            return U.compareAndSetInt(this, TOUCHED_EPOCH_OFF, cur, epoch);
        }
    }

    private static final class PalScratch {
        final Object[] cls = new Object[256]; // localId -> (IRadResistantBlock instance) or NOT_RES
        final int[] stamp = new int[256];     // localId -> generation
        int gen = 1;

        int nextGen() {
            int g = gen + 1;
            gen = g == 0 ? 1 : g;
            return gen;
        }
    }

    private static final class SectionMask {
        final long[] words = new long[64];

        boolean get(int bit) {
            int w = bit >>> 6;
            return (words[w] & (1L << (bit & 63))) != 0L;
        }

        void set(int bit) {
            int w = bit >>> 6;
            words[w] |= (1L << (bit & 63));
        }

        boolean isEmpty() {
            for (long w : words) if (w != 0L) return false;
            return true;
        }
    }

    private static final class DiffuseXTask extends RecursiveAction {
        final ChunkRef[] chunks;
        final LongBag wakeBag;
        final int epoch, lo, hi, threshold;

        DiffuseXTask(int epoch, ChunkRef[] chunks, int lo, int hi, LongBag wakeBag, int threshold) {
            this.epoch = epoch;
            this.chunks = chunks;
            this.lo = lo;
            this.hi = hi;
            this.wakeBag = wakeBag;
            this.threshold = threshold;
        }

        @Override
        protected void compute() {
            if (hi - lo <= threshold) {
                work(lo, hi);
            } else {
                int mid = (lo + hi) >>> 1;
                invokeAll(new DiffuseXTask(epoch, chunks, lo, mid, wakeBag, threshold), new DiffuseXTask(epoch, chunks, mid, hi, wakeBag, threshold));
            }
        }

        private void work(int start, int end) {
            for (int i = start; i < end; i++) {
                ChunkRef aCr = chunks[i];
                ChunkRef bCr = aCr.east;
                if (bCr == null) continue;
                long a0 = aCr.mask0, a1 = aCr.mask1, a2 = aCr.mask2, a3 = aCr.mask3;
                long b0 = bCr.mask0, b1 = bCr.mask1, b2 = bCr.mask2, b3 = bCr.mask3;
                if (((a0 | a1 | a2 | a3) | (b0 | b1 | b2 | b3)) == 0L) continue;
                processDiffuseGroup(aCr, bCr, a0 | b0, 0, wakeBag, epoch, 5, 4);
                processDiffuseGroup(aCr, bCr, a1 | b1, 1, wakeBag, epoch, 5, 4);
                processDiffuseGroup(aCr, bCr, a2 | b2, 2, wakeBag, epoch, 5, 4);
                processDiffuseGroup(aCr, bCr, a3 | b3, 3, wakeBag, epoch, 5, 4);
            }
        }
    }

    private static final class DiffuseZTask extends RecursiveAction {
        final ChunkRef[] chunks;
        final LongBag wakeBag;
        final int epoch, lo, hi, threshold;

        DiffuseZTask(int epoch, ChunkRef[] chunks, int lo, int hi, LongBag wakeBag, int threshold) {
            this.epoch = epoch;
            this.chunks = chunks;
            this.lo = lo;
            this.hi = hi;
            this.wakeBag = wakeBag;
            this.threshold = threshold;
        }

        @Override
        protected void compute() {
            if (hi - lo <= threshold) {
                work(lo, hi);
            } else {
                int mid = (lo + hi) >>> 1;
                invokeAll(new DiffuseZTask(epoch, chunks, lo, mid, wakeBag, threshold), new DiffuseZTask(epoch, chunks, mid, hi, wakeBag, threshold));
            }
        }

        private void work(int start, int end) {
            for (int i = start; i < end; i++) {
                ChunkRef aCr = chunks[i];
                ChunkRef bCr = aCr.south;
                if (bCr == null) continue;
                long a0 = aCr.mask0, a1 = aCr.mask1, a2 = aCr.mask2, a3 = aCr.mask3;
                long b0 = bCr.mask0, b1 = bCr.mask1, b2 = bCr.mask2, b3 = bCr.mask3;
                if (((a0 | a1 | a2 | a3) | (b0 | b1 | b2 | b3)) == 0L) continue;
                processDiffuseGroup(aCr, bCr, a0 | b0, 0, wakeBag, epoch, 3, 2);
                processDiffuseGroup(aCr, bCr, a1 | b1, 1, wakeBag, epoch, 3, 2);
                processDiffuseGroup(aCr, bCr, a2 | b2, 2, wakeBag, epoch, 3, 2);
                processDiffuseGroup(aCr, bCr, a3 | b3, 3, wakeBag, epoch, 3, 2);
            }
        }
    }

    private static final class DiffuseYTask extends RecursiveAction {
        final ChunkRef[] chunks;
        final LongBag wakeBag;
        final int epoch, lo, hi, parity, threshold;

        DiffuseYTask(int epoch, ChunkRef[] chunks, int lo, int hi, int parity, LongBag wakeBag, int threshold) {
            this.epoch = epoch;
            this.chunks = chunks;
            this.lo = lo;
            this.hi = hi;
            this.parity = parity;
            this.wakeBag = wakeBag;
            this.threshold = threshold;
        }

        @Override
        protected void compute() {
            if (hi - lo <= threshold) {
                work(lo, hi);
            } else {
                int mid = (lo + hi) >>> 1;
                invokeAll(new DiffuseYTask(epoch, chunks, lo, mid, parity, wakeBag, threshold),
                        new DiffuseYTask(epoch, chunks, mid, hi, parity, wakeBag, threshold));
            }
        }

        private void work(int start, int end) {
            for (int i = start; i < end; i++) {
                ChunkRef cr = chunks[i];
                long m0 = cr.mask0, m1 = cr.mask1, m2 = cr.mask2, m3 = cr.mask3;
                if ((m0 | m1 | m2 | m3) == 0L) continue;

                for (int sy = parity; sy < 15; sy += 2) {
                    int kA = cr.getKind(sy);
                    int kB = cr.getKind(sy + 1);
                    if (kA == ChunkRef.KIND_NONE || kB == ChunkRef.KIND_NONE) continue;

                    long aKey = Library.sectionToLong(cr.ck, sy);
                    long bKey = Library.sectionToLong(cr.ck, sy + 1);
                    exchangeFaceExact(aKey, cr, 1, bKey, cr, 0, wakeBag, epoch);
                }
            }
        }
    }

    // SectionRef: change return types to boolean.
    static abstract sealed class SectionRef permits MultiSectionRef, SingleMaskedSectionRef {
        final ChunkRef owner;
        final int sy;
        final byte pocketCount;
        final byte @NotNull [] pocketData;

        SectionRef(ChunkRef owner, int sy, byte pocketCount, byte[] pocketData) {
            this.owner = owner;
            this.sy = sy;
            this.pocketCount = pocketCount;
            this.pocketData = pocketData;
        }

        // @formatter:off
        abstract boolean exchangeWithMulti(MultiSectionRef other, long myKey, long otherKey, int myFace, int otherFace, LongBag wakeBag, int epoch);
        abstract boolean exchangeWithUniform(ChunkRef other, long myKey, long otherKey, int myFace, int otherFace, int otherSy, LongBag wakeBag, int epoch);
        abstract boolean exchangeWithSingle(SingleMaskedSectionRef other, long myKey, long otherKey, int myFace, int otherFace, LongBag wakeBag, int epoch);
        abstract int getPocketIndex(long pos);
        abstract int paletteIndexOrNeg(int blockIndex);
        abstract void clearFaceAllPockets(int faceOrdinal);
        abstract void linkFaceToMulti(MultiSectionRef other, int myFace);
        abstract void linkFaceToSingle(SingleMaskedSectionRef single, int faceA);
        abstract void linkFaceToUniform(ChunkRef crA, int faceA);
        // @formatter:on
    }

    static final class SingleMaskedSectionRef extends SectionRef {
        static final long CONN_OFF = fieldOffset(SingleMaskedSectionRef.class, "connections");
        final int volume;
        final double invVolume;
        final long packedFaceCounts;
        final double cx, cy, cz;
        long connections;
        int neighborMarkEpoch;
        double rad;

        SingleMaskedSectionRef(ChunkRef owner, int sy, byte[] pocketData, int volume, short[] faceCountsInput, double cx, double cy, double cz) {
            super(owner, sy, (byte) 1, pocketData);
            this.volume = volume;
            invVolume = 1.0d / volume;
            this.cx = cx;
            this.cy = cy;
            this.cz = cz;
            long packed = 0L;
            for (int i = 0; i < 6; i++) {
                long val = faceCountsInput[i] & 0x1FFL;
                packed |= (val << (i * 9));
            }
            packedFaceCounts = packed;
        }

        @Override
        boolean exchangeWithSingle(SingleMaskedSectionRef other, long myKey, long otherKey, int myFace, int otherFace, LongBag wakeBag, int epoch) {
            long conns = connections;
            int area = (int) ((conns >>> (myFace * 9)) & 0x1FFL);
            if (area == 0) return false;
            double ra = rad;
            double rb = other.rad;
            if (ra == rb) return false;
            double invVa = invVolume;
            double invVb = other.invVolume;
            double denomInv = invVa + invVb;
            double distSum = getFaceDist(myFace) + other.getFaceDist(otherFace);
            if (distSum <= 0.0D) return false;
            double e = Math.exp(-((area / distSum) * denomInv * diffusionDt));
            double rStar = (ra * invVb + rb * invVa) / denomInv;
            rad = rStar + (ra - rStar) * e;
            other.rad = rStar + (rb - rStar) * e;
            wake(myKey, wakeBag, epoch);
            other.wake(otherKey, wakeBag, epoch);
            return true;
        }

        @Override
        boolean exchangeWithUniform(ChunkRef other, long myKey, long otherKey, int myFace, int otherFace, int otherSy, LongBag wakeBag, int epoch) {
            int area = getFaceCount(myFace);
            double ra = other.uniformRads[otherSy];
            double rb = rad;
            if (area <= 0 || ra == rb) return false;
            double distSum = 8.0d + getFaceDist(myFace);
            if (distSum <= 0.0D) return false;
            // invVa = 1.0d / 4096.0d = 2.44140625E-4
            double invVb = invVolume;
            double denomInv = 2.44140625E-4 + invVb;
            double e = Math.exp(-((area / distSum) * denomInv * diffusionDt));
            double rStar = (ra * invVb + rb * 2.44140625E-4) / denomInv;
            other.uniformRads[otherSy] = rStar + (ra - rStar) * e;
            rad = rStar + (rb - rStar) * e;
            other.wakeUni(otherKey, otherSy, wakeBag, epoch);
            wake(myKey, wakeBag, epoch);
            return true;
        }

        @Override
        boolean exchangeWithMulti(MultiSectionRef other, long myKey, long otherKey, int myFace, int otherFace, LongBag wakeBag, int epoch) {
            boolean changed = false;
            int bCount = Math.min(other.pocketCount & 0xFF, NO_POCKET);
            int stride = 6 * NEI_SLOTS;
            char[] conn = other.connectionArea;
            byte[] faceAct = other.faceActive;
            int slot0 = (otherFace << 4) + NEI_SHIFT;
            double[] otherData = other.data, otherFaceDist = other.faceDist;
            double myInvVol = invVolume, distA = getFaceDist(myFace);
            double myRad = rad;
            for (int pi = 0; pi < bCount; pi++) {
                int distIdx = pi * 6 + otherFace;
                if (faceAct[distIdx] == 0) continue;
                int area = conn[pi * stride + slot0];
                if (area == 0) continue;
                double ra = myRad;
                int idx = pi << 1;
                double rb = otherData[idx];
                if (ra == rb) continue;
                double invVb = otherData[idx + 1];
                double denomInv = myInvVol + invVb;
                double distSum = distA + otherFaceDist[distIdx];
                if (distSum <= 0.0D) continue;
                double e = Math.exp(-((area / distSum) * denomInv * diffusionDt));
                double rStar = (ra * invVb + rb * myInvVol) / denomInv;
                myRad = rStar + (ra - rStar) * e;
                otherData[idx] = rStar + (rb - rStar) * e;
                changed = true;
                other.wake(otherKey, pi, wakeBag, epoch);
            }
            if (changed) {
                rad = myRad;
                wake(myKey, wakeBag, epoch);
            }
            return changed;
        }

        @Override
        void clearFaceAllPockets(int faceOrdinal) {
            updateConnections(faceOrdinal, 0);
        }

        double getFaceDist(int face) {
            return switch (face) {
                case 0 -> cy + 0.5d;
                case 1 -> 15.5d - cy;
                case 2 -> cz + 0.5d;
                case 3 -> 15.5d - cz;
                case 4 -> cx + 0.5d;
                case 5 -> 15.5d - cx;
                default -> throw new IllegalArgumentException("Invalid face ordinal: " + face);
            };
        }

        boolean markWakeEpoch(int epoch) {
            if (neighborMarkEpoch == epoch) return false;
            neighborMarkEpoch = epoch;
            return true;
        }

        void wake(long sck, LongBag wakeBag, int epoch) {
            if (rad == 0.0D) return;
            if (!markWakeEpoch(epoch)) return;
            owner.setActiveBit(sy, 0);
            wakeBag.tryAdd(pocketKey(sck, 0));
        }

        void updateConnections(int face, int value) {
            int shift = face * 9;
            long mask = 0x1FFL << shift;
            long bits = ((long) value & 0x1FFL) << shift;
            while (true) {
                long current = U.getLongVolatile(this, CONN_OFF);
                long next = (current & ~mask) | bits;
                if (current == next) return;
                if (U.compareAndSetLong(this, CONN_OFF, current, next)) return;
            }
        }

        int getFaceCount(int face) {
            return (int) ((packedFaceCounts >>> (face * 9)) & 0x1FFL);
        }

        @Override
        int getPocketIndex(long pos) {
            int blockIndex = Library.blockPosToLocal(pos);
            int nibble = readNibble(pocketData, blockIndex);
            return (nibble == 0) ? 0 : -1;
        }

        @Override
        int paletteIndexOrNeg(int blockIndex) {
            int nibble = readNibble(pocketData, blockIndex);
            return (nibble == 0) ? 0 : -1;
        }

        @Override
        void linkFaceToMulti(MultiSectionRef other, int myFace) {
            other.linkFaceToSingle(this, myFace ^ 1);
        }

        @Override
        void linkFaceToSingle(SingleMaskedSectionRef other, int faceA) {
            int area;
            int faceB = faceA ^ 1;
            int baseA = faceA << 8;
            int baseB = faceB << 8;
            int count = 0;
            byte[] myData = pocketData;
            byte[] otherData = other.pocketData;
            for (int t = 0; t < 256; t++) {
                int idxA = FACE_PLANE[baseA + t];
                if (readNibble(myData, idxA) != 0) continue;
                int idxB = FACE_PLANE[baseB + t];
                if (readNibble(otherData, idxB) != 0) continue;
                count++;
            }
            area = count;
            other.updateConnections(faceB, area);
            updateConnections(faceA, area);
        }

        @Override
        void linkFaceToUniform(ChunkRef crA, int faceA) {
            int area = getFaceCount(faceA);
            // neighbor is open air, the connection area is exactly my exposed face area
            // so we just confirm the link.
            updateConnections(faceA, area);
        }
    }

    static final class MultiSectionRef extends SectionRef {
        static final int STRIDE = 6 * NEI_SLOTS;
        final byte[] faceActive;
        final char[] connectionArea;
        final double[] data; // interleaved: even indices = rad, odd indices = invVolume
        final double[] faceDist;
        final int[] volume;
        int neighborEpoch;
        char neighborSeenMask;

        MultiSectionRef(ChunkRef owner, int sy, byte pocketCount, byte[] pocketData, double[] faceDist) {
            super(owner, sy, pocketCount, pocketData);
            this.faceDist = faceDist;

            int count = pocketCount & 0xFF;
            connectionArea = new char[count * STRIDE];
            faceActive = new byte[count * 6];
            data = new double[count << 1];
            volume = new int[count];
        }

        @Override
        boolean exchangeWithMulti(MultiSectionRef other, long myKey, long otherKey, int myFace, int otherFace, LongBag wakeBag, int epoch) {
            boolean changed = false;
            char[] conn = connectionArea;
            byte[] faceAct = faceActive;
            int aCount = pocketCount & 0xFF;
            int bCount = Math.min(other.pocketCount & 0xFF, NO_POCKET);
            int faceBase0 = myFace << 4;
            double[] myData = data, otherData = other.data, myFaceDist = faceDist, otherFaceDist = other.faceDist;
            for (int pi = 0; pi < aCount; pi++) {
                int pi6 = pi * 6;
                if (faceAct[pi6 + myFace] == 0) continue;
                int base = pi * STRIDE + faceBase0;
                if (conn[base] == 0) continue;
                int idxA = pi << 1;
                double ra = myData[idxA];
                boolean myChanged = false;
                for (int npi = 0; npi < bCount; npi++) {
                    int area = conn[base + NEI_SHIFT + npi];
                    if (area == 0) continue;
                    int idxB = npi << 1;
                    double rb = otherData[idxB];
                    if (ra == rb) continue;
                    double invVa = myData[idxA + 1];
                    double invVb = otherData[idxB + 1];
                    double denomInv = invVa + invVb;
                    double distSum = myFaceDist[pi6 + myFace] + otherFaceDist[npi * 6 + otherFace];
                    if (distSum <= 0.0D) continue;
                    double e = Math.exp(-((area / distSum) * denomInv * diffusionDt));
                    double rStar = (ra * invVb + rb * invVa) / denomInv;
                    ra = rStar + (ra - rStar) * e;
                    otherData[idxB] = rStar + (rb - rStar) * e;
                    changed = true;
                    myChanged = true;
                    other.wake(otherKey, npi, wakeBag, epoch);
                }
                if (myChanged) {
                    myData[idxA] = ra;
                    wake(myKey, pi, wakeBag, epoch);
                }
            }
            return changed;
        }

        @Override
        boolean exchangeWithUniform(ChunkRef other, long myKey, long otherKey, int myFace, int otherFace, int otherSy, LongBag wakeBag, int epoch) {
            boolean changed = false;
            char[] conn = connectionArea;
            byte[] faceAct = faceActive;
            double[] myData = data, myFaceDist = faceDist;
            int aCount = pocketCount & 0xFF;
            int slot0 = (myFace << 4) + NEI_SHIFT;
            double[] otherUni = other.uniformRads;
            double ra = otherUni[otherSy];
            boolean otherChanged = false;
            for (int pi = 0; pi < aCount; pi++) {
                int pi6 = pi * 6;
                if (faceAct[pi6 + myFace] == 0) continue;
                int area = conn[pi * STRIDE + slot0];
                if (area == 0) continue;
                int idx = pi << 1;
                double rb = myData[idx];
                if (ra == rb) continue;
                double distSum = 8.0d + myFaceDist[pi6 + myFace];
                if (distSum <= 0.0D) continue;
                // invVa = 1.0d / 4096.0d = 2.44140625E-4
                double invVb = myData[idx + 1];
                double denomInv = 2.44140625E-4 + invVb;
                double e = Math.exp(-((area / distSum) * denomInv * diffusionDt));
                double rStar = (ra * invVb + rb * 2.44140625E-4) / denomInv;
                ra = rStar + (ra - rStar) * e;
                myData[idx] = rStar + (rb - rStar) * e;
                changed = true;
                otherChanged = true;
                wake(myKey, pi, wakeBag, epoch);
            }
            if (otherChanged) {
                otherUni[otherSy] = ra;
                other.wakeUni(otherKey, otherSy, wakeBag, epoch);
            }
            return changed;
        }

        @Override
        boolean exchangeWithSingle(SingleMaskedSectionRef other, long myKey, long otherKey, int myFace, int otherFace, LongBag wakeBag, int epoch) {
            boolean changed = false;
            char[] conn = connectionArea;
            byte[] faceAct = faceActive;
            double[] myData = data;
            int aCount = pocketCount & 0xFF;
            int slot0 = (myFace << 4) + NEI_SHIFT;
            double rb = other.rad, invVb = other.invVolume;
            boolean otherChanged = false;
            for (int pi = 0; pi < aCount; pi++) {
                if (faceAct[pi * 6 + myFace] == 0) continue;
                int area = conn[pi * STRIDE + slot0];
                if (area == 0) continue;
                int idx = pi << 1;
                double ra = myData[idx];
                if (ra == rb) continue;
                double invVa = myData[idx + 1];
                double denomInv = invVa + invVb;
                double distSum = faceDist[pi * 6 + myFace] + other.getFaceDist(otherFace);
                if (distSum <= 0.0D) continue;
                double e = Math.exp(-((area / distSum) * denomInv * diffusionDt));
                double rStar = (ra * invVb + rb * invVa) / denomInv;
                myData[idx] = rStar + (ra - rStar) * e;
                rb = rStar + (rb - rStar) * e;
                changed = true;
                otherChanged = true;
                wake(myKey, pi, wakeBag, epoch);
            }
            if (otherChanged) {
                other.rad = rb;
                other.wake(otherKey, wakeBag, epoch);
            }
            return changed;
        }

        boolean markWakeEpoch(int pi, int epoch) {
            int bit = 1 << pi;
            if (neighborEpoch != epoch) {
                neighborEpoch = epoch;
                neighborSeenMask = (char) bit;
                return true;
            }
            int seen = neighborSeenMask;
            if ((seen & bit) != 0) return false;
            neighborSeenMask = (char) (seen | bit);
            return true;
        }

        void wake(long sck, int pi, LongBag wakeBag, int epoch) {
            if (data[pi << 1] == 0.0D) return;
            if (!markWakeEpoch(pi, epoch)) return;
            owner.setActiveBit(sy, pi);
            wakeBag.tryAdd(pocketKey(sck, pi));
        }

        @Override
        int getPocketIndex(long pos) {
            int blockIndex = Library.blockPosToLocal(pos);
            int nibble = readNibble(pocketData, blockIndex);
            if (nibble == NO_POCKET) return -1;
            if (nibble >= (pocketCount & 0xFF))
                throw new IllegalStateException("Invalid nibble " + nibble + " >= pocketCount " + (pocketCount & 0xFF));
            return nibble;
        }

        @Override
        int paletteIndexOrNeg(int blockIndex) {
            int nibble = readNibble(pocketData, blockIndex);
            if (nibble == NO_POCKET) return -1;
            if (nibble >= (pocketCount & 0xFF))
                throw new IllegalStateException("Invalid nibble " + nibble + " >= pocketCount " + (pocketCount & 0xFF));
            return nibble;
        }

        @Override
        void clearFaceAllPockets(int faceOrdinal) {
            int len = pocketCount & 0xFF;
            int faceBase = faceOrdinal * NEI_SLOTS;
            for (int p = 0; p < len; p++) {
                int off = p * STRIDE + faceBase;
                Arrays.fill(connectionArea, off, off + NEI_SLOTS, (char) 0);
                faceActive[p * 6 + faceOrdinal] = 0;
            }
        }

        void markSentinelPlane16x16(int ordinal) {
            int slotBase = ordinal * NEI_SLOTS;
            int planeBase = ordinal << 8;
            char[] conn = connectionArea;
            byte[] face = faceActive;
            for (int t = 0; t < 256; t++) {
                int idx = FACE_PLANE[planeBase + t];
                int pi = paletteIndexOrNeg(idx);
                if (pi >= 0) {
                    conn[pi * STRIDE + slotBase] = 1;
                    face[pi * 6 + ordinal] = 1;
                }
            }
        }

        @Override
        void linkFaceToMulti(MultiSectionRef multiB, int faceA) {
            int faceB = faceA ^ 1;
            clearFaceAllPockets(faceA);
            multiB.clearFaceAllPockets(faceB);
            char[] aConn = connectionArea, bConn = multiB.connectionArea;
            byte[] aFace = faceActive, bFace = multiB.faceActive;
            int aFaceBase0 = faceA * NEI_SLOTS;
            int bFaceBase0 = faceB * NEI_SLOTS;

            int planeA = faceA << 8;
            int planeB = faceB << 8;

            for (int t = 0; t < 256; t++) {
                int aIdx = FACE_PLANE[planeA + t];
                int bIdx = FACE_PLANE[planeB + t];

                int pa = paletteIndexOrNeg(aIdx);
                if (pa < 0) continue;

                int pb = multiB.paletteIndexOrNeg(bIdx);
                if (pb < 0) continue;

                int aOff = pa * STRIDE + aFaceBase0;
                aConn[aOff] = 1; // sentinel
                aConn[aOff + NEI_SHIFT + pb]++;

                aFace[pa * 6 + faceA] = 1;

                int bOff = pb * STRIDE + bFaceBase0;
                bConn[bOff] = 1; // sentinel
                bConn[bOff + NEI_SHIFT + pa]++;

                bFace[pb * 6 + faceB] = 1;
            }
        }

        @Override
        void linkFaceToSingle(SingleMaskedSectionRef single, int faceA) {
            int faceB = faceA ^ 1;
            clearFaceAllPockets(faceA);
            char[] aConn = connectionArea;
            byte[] aFace = faceActive;
            int aFaceBase0 = faceA * NEI_SLOTS;
            int planeA = faceA << 8;
            int planeB = faceB << 8;
            for (int t = 0; t < 256; t++) {
                int aIdx = FACE_PLANE[planeA + t];
                int pa = paletteIndexOrNeg(aIdx);
                if (pa < 0) continue;

                // neighbor is single (masked). Check if it exposes face there.
                int bIdx = FACE_PLANE[planeB + t];
                if (single.paletteIndexOrNeg(bIdx) < 0) continue;

                int aOff = pa * STRIDE + aFaceBase0;
                aConn[aOff] = 1;
                aConn[aOff + NEI_SHIFT]++; // neighbor pocket index 0
                aFace[pa * 6 + faceA] = 1;
            }
        }

        @Override
        void linkFaceToUniform(ChunkRef crA, int faceA) {
            clearFaceAllPockets(faceA);
            char[] aConn = connectionArea;
            byte[] aFace = faceActive;
            int aFaceBase0 = faceA * NEI_SLOTS;
            int planeA = faceA << 8;

            for (int t = 0; t < 256; t++) {
                int aIdx = FACE_PLANE[planeA + t];
                int pa = paletteIndexOrNeg(aIdx);
                if (pa < 0) continue;

                // neighbor is Uniform -> always open
                int aOff = pa * STRIDE + aFaceBase0;
                aConn[aOff] = 1;
                aConn[aOff + NEI_SHIFT]++;
                aFace[pa * 6 + faceA] = 1;
            }
        }
    }

    // Concurrency invariants and why plain reads/writes are OK here.
    // This code is not “thread-safe” in the generic sense. It is correct only because:
    // 1) Server thread exclusivity:
    //    - All @ServerThread methods and all Forge event handlers in this class run only on the server thread.
    // 2) No overlap with async simulation:
    //    - The async simulation step (processWorldSimulation / runParallelSimulation) does not overlap with any
    //      server-thread mutation of radiation state.
    // 3) Single-writer per chunk per phase:
    //    - Sweep scheduling ensures that a chunk participates in at most one exchange for a direction in a phase via
    //      parity partitioning, and X/Z/Y sweeps do not overlap.
    //      In other words, a section is not exchanged by two tasks at once.
    // Given those, the simulation is allowed to use non-volatile fields and plain RMW:
    // - ChunkRef mask words (mask0...mask3) are updated via U.getLong + U.putLong (Unsafe here for branchless access).
    // - Radiation values (uniformRads / Single.rad / Multi.data) and per-epoch “seen” markers are also plain.
    // If scheduling is changed (overlap sweeps, allow multiple simultaneous neighbor exchanges per chunk, etc.),
    // these plain accesses are no longer justified and must become atomic or be redesigned.
    static final class WorldRadiationData {
        private static final long CI_OFF, CC_OFF, FCP_OFF, NEXT_OFF, PREV_OFF, BUF_OFF, POOLED_OFF;

        static {// @formatter:off
            try {
                Class<?> qc = MpscUnboundedXaddArrayLongQueue.class;
                CI_OFF = off(qc, "consumerIndex");
                CC_OFF = off(qc, "consumerChunk");
                FCP_OFF = off(qc, "freeChunksPool");
                Class<?> cc = find(qc, "consumerChunk").getType();
                NEXT_OFF = off(cc, "next");
                PREV_OFF = off(cc, "prev");
                BUF_OFF = off(cc, "buffer");
                POOLED_OFF = off(cc, "pooled");
            } catch (Exception e) { throw new RuntimeException(e); }
        }

        final WorldServer world;
        final long worldSalt;
        final NonBlockingHashMapLong<ChunkRef> chunkRefs = new NonBlockingHashMapLong<>(4096, false);
        final LongOpenHashSet pendingUnloads = new LongOpenHashSet(256);
        final NonBlockingHashSetLong dirtySections = new NonBlockingHashSetLong(16384);
        // reserved value: Long.MIN_VALUE
        final MpscUnboundedXaddArrayLongQueue dirtyQueue = new MpscUnboundedXaddArrayLongQueue(16384);
        // only used when tickrate != 1.
        final MpscUnboundedXaddArrayLongQueue destructionQueue = new MpscUnboundedXaddArrayLongQueue(64);
        final TLPool<byte[]> pocketDataPool = new TLPool<>(() -> new byte[2048], _ -> /*@formatter:off*/{}/*@formatter:on*/, 256, 4096);
        final SectionRetireBag retiredSections = new SectionRetireBag(16384);
        final NonBlockingLong2LongHashMap pendingPocketRadBits = new NonBlockingLong2LongHashMap(16384);
        final LongArrayList dirtyToRebuildScratch = new LongArrayList(16384);
        final Long2IntOpenHashMap dirtyChunkMasksScratch = new Long2IntOpenHashMap(16384);
        final double minBound;
        // Pending write queues for unstable sections
        final Long2ObjectOpenHashMap<Int2DoubleOpenHashMap> writeAdd = new Long2ObjectOpenHashMap<>(256);
        final Long2ObjectOpenHashMap<Int2DoubleOpenHashMap> writeSet = new Long2ObjectOpenHashMap<>(64);
        final LongOpenHashSet hasSet = new LongOpenHashSet(256);
        final ObjectPool<Int2DoubleOpenHashMap> localMapPool = new ObjectPool<>(Int2DoubleOpenHashMap::new, Int2DoubleOpenHashMap::clear, 64);
        final int[] activeStripeOffsets = new int[ACTIVE_STRIPES];
        final int[][] parityStripeCounts = new int[ACTIVE_STRIPES][4];
        final int[][] parityStripeOffsets = new int[ACTIVE_STRIPES][4];
        final long[][] activeStripeBufs = new long[ACTIVE_STRIPES][];
        final ChunkRef[][] touchedStripeRefs = new ChunkRef[ACTIVE_STRIPES][];
        final int[] activeStripeCounts = new int[ACTIVE_STRIPES];
        final int[] touchedStripeCounts = new int[ACTIVE_STRIPES];
        final SectionRef[][] activeStripeRefs = new SectionRef[ACTIVE_STRIPES][];
        final ChunkRef[][] activeStripeChunkRefs = new ChunkRef[ACTIVE_STRIPES][];
        final ChunkRef[][] parityBuckets = new ChunkRef[4][4096];
        final int[] parityCounts = new int[4];
        final LongConsumer clearAux = sck -> {
            dirtySections.remove(sck);
            Int2DoubleOpenHashMap a = writeAdd.remove(sck);
            if (a != null) localMapPool.recycle(a);
            Int2DoubleOpenHashMap s = writeSet.remove(sck);
            if (s != null) localMapPool.recycle(s);
            for (int p = 0; p <= NO_POCKET; p++) pendingPocketRadBits.remove(pocketKey(sck, p));
        };
        SectionRef[] activeRefs = new SectionRef[32768];
        ChunkRef[] activeChunkRefs = new ChunkRef[32768];
        MpscUnboundedXaddArrayLongQueue[] activeQueuesCurrent, activeQueuesNext;
        LongBag wokenBag = new LongBag(32768);
        long[] activeBuf = new long[32768];
        long[] linkScratch = new long[512];
        long[] dirtyChunkKeysScratch = new long[4096];
        int[] dirtyChunkMasksScratchArr = new int[4096];
        int[] finalizeRunStarts = new int[32768 + 1];
        int finalizeRunCount;
        long[] wokenKeys = new long[32768];
        int[] wokenRunStarts = new int[32768 + 1];
        int wokenKeyCount;
        int wokenRunCount;
        // only used when tickrate == 1. races are tolerable.
        long pocketToDestroy = Long.MIN_VALUE;
        int workEpoch = 0;
        long workEpochSalt;
        double executionTimeAccumulator = 0.0D;
        int executionSampleCount = 0;
        long profSteps;
        double profTotalMs;
        double profMaxMs;
        // only allocated if profiling is enabled
        DoubleArrayList profSamplesMs;

        WorldRadiationData(WorldServer world) {
            this.world = world;
            Object v = CompatibilityConfig.dimensionRad.get(world.provider.getDimension());
            double mb = -((v instanceof Number n) ? n.doubleValue() : 0D);
            if (!Double.isFinite(mb) || mb > 0.0D) mb = 0.0D;
            minBound = mb;
            dirtyChunkMasksScratch.defaultReturnValue(0);
            int p = Math.max(256, 131072 / ACTIVE_STRIPES); // bruh
            MpscUnboundedXaddArrayLongQueue[] cur = new MpscUnboundedXaddArrayLongQueue[ACTIVE_STRIPES];
            MpscUnboundedXaddArrayLongQueue[] nxt = new MpscUnboundedXaddArrayLongQueue[ACTIVE_STRIPES];
            for (int i = 0; i < ACTIVE_STRIPES; i++) {
                cur[i] = new MpscUnboundedXaddArrayLongQueue(p);
                nxt[i] = new MpscUnboundedXaddArrayLongQueue(p);
                activeStripeBufs[i] = new long[p];
                touchedStripeRefs[i] = new ChunkRef[p];
                activeStripeRefs[i] = new SectionRef[p];
                activeStripeChunkRefs[i] = new ChunkRef[p];
            }
            activeQueuesCurrent = cur;
            activeQueuesNext = nxt;
            worldSalt = HashCommon.murmurHash3(world.getSeed() ^ (long) world.provider.getDimension() * 0x9E3779B97F4A7C15L ^ 0xD1B54A32D192ED03L);
        }

        private static long off(Class<?> c, String n) { return U.objectFieldOffset(find(c, n)); }

        private static Field find(Class<?> c, String n) {
            for (; c != Object.class; c = c.getSuperclass())
                try { Field f = c.getDeclaredField(n); f.setAccessible(true); return f; } catch (Exception ignored) {}
            throw new Error("Field not found: " + n);
        }//@formatter:on

        static long packLocal(long sectionKey, int localIdx) {
            return (sectionKey << 12) | (localIdx & 0xFFFL);
        }

        private static int buildLinkKeys(long[] keys, int hi, int negFace, long[] out) {
            int n = 0;
            for (int i = 0; i < hi; i++) {
                long k = keys[i];
                out[n++] = k;
                out[n++] = Library.sectionToLong(Library.getSectionX(k) + FACE_DX[negFace], Library.getSectionY(k) + FACE_DY[negFace],
                        Library.getSectionZ(k) + FACE_DZ[negFace]);
            }
            Arrays.parallelSort(out, 0, n);

            int u = 0;
            long prev = Long.MIN_VALUE;
            for (int i = 0; i < n; i++) {
                long v = out[i];
                if (i == 0 || v != prev) out[u++] = v;
                prev = v;
            }
            return u;
        }

        private static int stripeIndex(long pocketKey) {
            return (int) ((Library.sectionToChunkLong(pocketKey) * 0x9E3779B97F4A7C15L) >>> ACTIVE_STRIPE_SHIFT);
        }

        static double mulClamp(double a, int b) {
            if (a == 0.0D) return 0.0D;
            if (!Double.isFinite(a)) return Math.copySign(Double.MAX_VALUE, a);
            double lim = Double.MAX_VALUE / (double) b;
            double aa = Math.abs(a);
            if (aa >= lim) return Math.copySign(Double.MAX_VALUE, a);
            return a * (double) b;
        }

        static double addClamp(double a, double b) {
            double s = a + b;
            if (s == Double.POSITIVE_INFINITY) return Double.MAX_VALUE;
            if (s == Double.NEGATIVE_INFINITY) return -Double.MAX_VALUE;
            return Double.isNaN(s) ? 0.0D : s;
        }

        void finalizeRange(long[] keys, SectionRef @Nullable [] refs,
                           @Nullable ChunkRef @Nullable [] chunkRefs, int start, int end) {
            for (int i = start; i < end; i++) {
                long pk = keys[i];
                long sck = sectionKeyFromPocketKey(pk);
                int pi = pocketIndexFromPocketKey(pk);

                ChunkRef cr;
                SectionRef sc;

                if (chunkRefs != null) {
                    cr = chunkRefs[i];
                    sc = refs[i];
                    // Clear buffers to avoid leak
                    chunkRefs[i] = null;
                    refs[i] = null;
                } else {
                    cr = this.chunkRefs.get(Library.sectionToChunkLong(sck));
                    if (cr != null) {
                        int sy = Library.getSectionY(sck);
                        int k = cr.getKind(sy);
                        sc = (k >= ChunkRef.KIND_SINGLE) ? cr.sec[sy] : null;
                    } else {
                        sc = null;
                    }
                }

                if (cr == null) continue;
                int sy = Library.getSectionY(sck);
                int kind = cr.getKind(sy);
                long seed = 0L;
                if (kind == ChunkRef.KIND_UNI) {
                    if (pi != 0) {
                        cr.clearActiveBit(sy, pi);
                        continue;
                    }
                    double prev = cr.uniformRads[sy];
                    if (fogProbU64 != 0L && prev > RadiationConfig.fogRad) {
                        seed = HashCommon.mix(pk ^ workEpochSalt);
                        if (Long.compareUnsigned(seed, fogProbU64) < 0) {
                            spawnFog(sc, pi, sy, cr.mcChunk, seed);
                        }
                    }
                    double next = sanitize(prev * retentionDt);
                    if (next != prev) {
                        cr.uniformRads[sy] = next;
                        cr.mcChunk.markDirty();
                        if (prev >= 5.0D && pk != Long.MIN_VALUE) {
                            if (seed == 0L) seed = HashCommon.mix(pk ^ workEpochSalt);
                            if (Long.compareUnsigned(HashCommon.mix(seed + 0xD1B54A32D192ED03L), DESTROY_PROB_U64) < 0) {
                                if (tickDelay == 1) pocketToDestroy = pk;
                                else destructionQueue.offer(pk);
                            }
                        }
                    }

                    if (next != 0.0D) {
                        enqueueActiveNext(pk);
                    } else {
                        cr.clearActiveBit(sy, 0);
                    }
                    continue;
                }

                if (sc == null || sc.pocketCount <= 0) {
                    deactivatePocket(pk);
                    continue;
                }

                int len = sc.pocketCount & 0xFF;
                if (pi < 0 || pi >= len) {
                    sc.owner.clearActiveBit(sy, pi);
                    continue;
                }

                double prev;
                if (kind == ChunkRef.KIND_SINGLE) {
                    prev = ((SingleMaskedSectionRef) sc).rad;
                } else {
                    prev = ((MultiSectionRef) sc).data[pi << 1];
                }

                if (fogProbU64 != 0L && prev > RadiationConfig.fogRad) {
                    seed = HashCommon.mix(pk ^ workEpochSalt);
                    if (Long.compareUnsigned(seed, fogProbU64) < 0) {
                        spawnFog(sc, pi, sy, sc.owner.mcChunk, seed);
                    }
                }

                double next = sanitize(prev * retentionDt);

                if (next != prev) {
                    if (kind == ChunkRef.KIND_SINGLE) {
                        ((SingleMaskedSectionRef) sc).rad = next;
                    } else {
                        ((MultiSectionRef) sc).data[pi << 1] = next;
                    }
                    sc.owner.mcChunk.markDirty();

                    if (prev >= 5.0D && pk != Long.MIN_VALUE) {
                        if (seed == 0L) seed = HashCommon.mix(pk ^ workEpochSalt);
                        if (Long.compareUnsigned(HashCommon.mix(seed + 0xD1B54A32D192ED03L), DESTROY_PROB_U64) < 0) {
                            if (tickDelay == 1) pocketToDestroy = pk;
                            else destructionQueue.offer(pk);
                        }
                    }
                }

                if (next != 0.0D) {
                    enqueueActiveNext(pk);
                } else {
                    sc.owner.clearActiveBit(sc.sy, pi);
                }
            }
        }

        @ServerThread
        byte @Nullable [] tryEncodePayload(int cx, int cz) {
            ByteBuffer buf = BUF;
            // we don't need to clear it because only indices [0, pocketCount) is ever written or read
            // this includes applyQueuedWrites
            double[] tempDensities = TEMP_DENSITIES;
            buf.clear();
            buf.putShort((short) 0);
            int count = 0;
            long ck = ChunkPos.asLong(cx, cz);
            ChunkRef cr = chunkRefs.get(ck);

            for (int sy = 0; sy < 16; sy++) {
                long sck = Library.sectionToLong(cx, sy, cz);

                // Priority 1: Unresolved pending data
                boolean hasPending = false;
                for (int p = 0; p <= NO_POCKET; p++) {
                    if (pendingPocketRadBits.containsKey(pocketKey(sck, p))) {
                        hasPending = true;
                        break;
                    }
                }
                if (hasPending) {
                    for (int p = 0; p <= NO_POCKET; p++) {
                        long bits = pendingPocketRadBits.get(pocketKey(sck, p));
                        if (bits != 0L) {
                            double v = Double.longBitsToDouble(bits);
                            if (v == 0.0D) continue;
                            buf.put((byte) ((sy << 4) | (p & 15)));
                            buf.putDouble(v);
                            count++;
                        }
                    }
                    continue; // Skip looking at ChunkRef, pending bits supersede it
                }

                // Priority 2: Active ChunkRef data + Queued Writes
                if (cr == null) continue;
                int kind = cr.getKind(sy);
                if (kind == ChunkRef.KIND_NONE) continue;

                int pCount;
                byte[] pData = null;

                if (kind == ChunkRef.KIND_UNI) {
                    tempDensities[0] = cr.uniformRads[sy];
                    pCount = 1;
                } else {
                    SectionRef sc = cr.sec[sy];
                    if (sc == null || sc.pocketCount <= 0) continue;
                    pCount = sc.pocketCount & 0xFF;
                    if (pCount > NO_POCKET) pCount = NO_POCKET;
                    if (kind == ChunkRef.KIND_SINGLE) {
                        tempDensities[0] = ((SingleMaskedSectionRef) sc).rad;
                    } else {
                        MultiSectionRef msc = (MultiSectionRef) sc;
                        for (int p = 0; p < pCount; p++) tempDensities[p] = msc.data[p << 1];
                    }
                    pData = sc.pocketData;
                }
                applyQueuedWrites(sck, pData, pCount, tempDensities);
                for (int p = 0; p < pCount; p++) {
                    double v = sanitize(tempDensities[p]);
                    if (v == 0.0D) continue;
                    buf.put((byte) ((sy << 4) | (p & 15)));
                    buf.putDouble(v);
                    count++;
                }
            }

            if (count == 0) return null;
            buf.putShort(0, (short) count);
            buf.flip();
            byte[] out = new byte[4 + buf.limit()];
            out[0] = MAGIC_0;
            out[1] = MAGIC_1;
            out[2] = MAGIC_2;
            out[3] = FMT;
            buf.get(out, 4, buf.limit());
            return out;
        }

        void logProfilingMessage(long stepStartNs) {
            if (!GeneralConfig.enableDebugMode) return;
            double ms = (System.nanoTime() - stepStartNs) * 1.0e-6;
            executionTimeAccumulator += ms;
            int n = ++executionSampleCount;
            if (n < PROFILE_WINDOW) return;
            double totalMs = executionTimeAccumulator;
            double avgMs = Math.rint((totalMs / PROFILE_WINDOW) * 1000.0) / 1000.0;
            double lastMs = Math.rint(ms * 1000.0) / 1000.0;
            int dimId = world.provider.getDimension();
            String dimType = world.provider.getDimensionType().getName();
            //noinspection AutoBoxing
            MainRegistry.logger.info("[RadiationSystemNT] dim {} ({}) avg {} ms/step over last {} steps (total {} ms, last {} ms)", dimId, dimType, avgMs,
                    PROFILE_WINDOW, (int) Math.rint(totalMs), lastMs);
            executionTimeAccumulator = 0.0D;
            executionSampleCount = 0;
            profSteps++;
            profTotalMs += ms;
            if (ms > profMaxMs) profMaxMs = ms;
            DoubleArrayList samples = profSamplesMs;
            if (samples == null) profSamplesMs = samples = new DoubleArrayList(8192);
            samples.add(ms);
        }

        void processWorldSimulation() {
            long time = System.nanoTime();
            rebuildDirtySections();
            clearQueuedWrites();
            swapQueues();

            int epoch = nextWorkEpoch();
            MpscUnboundedXaddArrayLongQueue[] qs = activeQueuesCurrent;

            if (ACTIVE_STRIPES == 1) {
                new DrainStripeTask(qs[0], 0, epoch).invoke();
            } else {
                ForkJoinTask<?>[] drainTasks = new ForkJoinTask<?>[ACTIVE_STRIPES];
                for (int s = 0; s < ACTIVE_STRIPES; s++) {
                    drainTasks[s] = new DrainStripeTask(qs[s], s, epoch);
                }
                ForkJoinTask.invokeAll(drainTasks);
            }

            int activeCount = 0;
            int[] activeOffsets = activeStripeOffsets;
            int[] activeStripeCounts = this.activeStripeCounts;
            for (int s = 0; s < ACTIVE_STRIPES; s++) {
                activeOffsets[s] = activeCount;
                activeCount += activeStripeCounts[s];
            }
            if (activeCount == 0) {
                logProfilingMessage(time);
                return;
            }
            if (activeBuf.length < activeCount) {
                int newSize = activeCount + (activeCount >>> 1);
                activeBuf = new long[newSize];
                activeRefs = new SectionRef[newSize];
                activeChunkRefs = new ChunkRef[newSize];
            }
            new MergeActiveAndCountParityTask(activeOffsets, 0, ACTIVE_STRIPES).invoke();
            BuildFinalizeRunsTask runsTask = new BuildFinalizeRunsTask(activeCount);
            runsTask.fork();
            int[] globalParityCounts = parityCounts;
            Arrays.fill(globalParityCounts, 0);
            int[][] stripeCounts = parityStripeCounts;
            int[][] stripeOffsets = parityStripeOffsets;
            for (int s = 0; s < ACTIVE_STRIPES; s++) {// @formatter:off
                int[] sCounts = stripeCounts[s];
                int[] sOffsets = stripeOffsets[s];
                int c0 = sCounts[0]; sOffsets[0] = globalParityCounts[0]; globalParityCounts[0] += c0;
                int c1 = sCounts[1]; sOffsets[1] = globalParityCounts[1]; globalParityCounts[1] += c1;
                int c2 = sCounts[2]; sOffsets[2] = globalParityCounts[2]; globalParityCounts[2] += c2;
                int c3 = sCounts[3]; sOffsets[3] = globalParityCounts[3]; globalParityCounts[3] += c3;
            }// @formatter:on
            for (int b = 0; b < 4; b++) {
                int count = globalParityCounts[b];
                if (parityBuckets[b].length < count) {
                    parityBuckets[b] = new ChunkRef[count + (count >>> 1) + 128];
                }
            }
            new ScatterParityTask(0, ACTIVE_STRIPES).invoke();
            int maxWake = (activeCount <= (Integer.MAX_VALUE / 90)) ? (activeCount * 90) : Integer.MAX_VALUE;
            LongBag wokenBag = getWokenBag(maxWake);
            wokenBag.clear();

            runExactExchangeSweeps(this, wokenBag);

            int wokenCount = wokenBag.size();
            BuildWokenRunsTask wokenPrep = (wokenCount > 0) ? new BuildWokenRunsTask(wokenBag, wokenCount) : null;
            if (wokenPrep != null) wokenPrep.fork();
            runsTask.join();
            int activeThreshold = getTaskThreshold(activeCount, 64);
            new FinalizeTask(0, finalizeRunCount, activeThreshold).invoke();
            if (wokenPrep != null) {
                wokenPrep.join();
                int bagThreshold = getTaskThreshold(wokenKeyCount, 64);
                new FinalizeWokenTask(0, wokenRunCount, bagThreshold).invoke();
            }

            if (workEpoch % 200 == 13) {
                cleanupPools();
                processUnloadedQueue();
            }
            logProfilingMessage(time);
        }

        void rebuildDirtySections() {
            if (dirtyQueue.isEmpty()) return;
            LongArrayList toRebuild = dirtyToRebuildScratch;
            Long2IntOpenHashMap chunkMasks = dirtyChunkMasksScratch;
            toRebuild.clear();
            chunkMasks.clear();
            MpscUnboundedXaddArrayLongQueue q = dirtyQueue;
            long cIndex = U.getLong(q, CI_OFF);
            Object cChunk = U.getReference(q, CC_OFF);
            int chunkMask = q.chunkSize() - 1;
            while (true) { //hand-rolled fancy version of while ((pk = q.plainPoll()) != Long.MIN_VALUE)
                int co = (int) (cIndex & chunkMask);
                long[] buf = (long[]) U.getReference(cChunk, BUF_OFF);
                long elOff = offLong(co);
                long sck;
                if (co == 0 && cIndex != 0) {
                    Object next = U.getReference(cChunk, NEXT_OFF);
                    if (next == null) break;
                    long[] nBuf = (long[]) U.getReference(next, BUF_OFF);
                    sck = U.getLong(nBuf, JA_BASE);
                    if (sck == MpscUnboundedXaddArrayLongQueue.EMPTY) break;
                    U.putReference(cChunk, NEXT_OFF, null);
                    U.putReference(next, PREV_OFF, null);
                    if (U.getBoolean(cChunk, POOLED_OFF)) {
                        // noinspection unchecked, rawtypes
                        ((SpscArrayQueue) U.getReference(q, FCP_OFF)).offer(cChunk);
                    }
                    cChunk = next;
                    U.putLong(nBuf, JA_BASE, MpscUnboundedXaddArrayLongQueue.EMPTY);
                } else {
                    sck = U.getLong(buf, elOff);
                    if (sck == MpscUnboundedXaddArrayLongQueue.EMPTY) break;
                    U.putLong(buf, elOff, MpscUnboundedXaddArrayLongQueue.EMPTY);
                }
                cIndex++;
                if (!dirtySections.remove(sck)) continue;
                int sy = Library.getSectionY(sck);
                if (isInvalidSectionY(sy)) continue;
                toRebuild.add(sck);
                long ck = Library.sectionToChunkLong(sck);
                chunkMasks.put(ck, chunkMasks.get(ck) | (1 << sy));
            }
            U.putLong(q, CI_OFF, cIndex);
            U.putReference(q, CC_OFF, cChunk);
            int size = toRebuild.size();
            if (size == 0) return;

            int batchCount = chunkMasks.size();
            ensureDirtyBatchCapacity(batchCount);

            long[] chunkKeys = dirtyChunkKeysScratch;
            int[] masks = dirtyChunkMasksScratchArr;

            int bi = 0;
            var iterator = chunkMasks.long2IntEntrySet().fastIterator();
            while (iterator.hasNext()) {
                var e = iterator.next();
                chunkKeys[bi] = e.getLongKey();
                masks[bi] = e.getIntValue();
                bi++;
            }

            int threshold = getTaskThreshold(batchCount, 8);
            new RebuildChunkBatchTask(chunkKeys, masks, 0, batchCount, threshold).invoke();
            relinkKeys(toRebuild.elements(), size);
        }

        void queueSet(long sck, int local, double density) {
            long pl = packLocal(sck, local);
            hasSet.add(pl);

            Int2DoubleOpenHashMap sm = writeSet.get(sck);
            if (sm == null) {
                sm = localMapPool.borrow();
                sm.defaultReturnValue(0.0);
                writeSet.put(sck, sm);
            }
            sm.put(local, density);

            // Set overrides previous Adds for strict ordering
            Int2DoubleOpenHashMap am = writeAdd.get(sck);
            if (am != null) {
                am.remove(local);
                if (am.isEmpty()) {
                    writeAdd.remove(sck);
                    localMapPool.recycle(am);
                }
            }
        }

        void queueAdd(long sck, int local, double delta) {
            long pl = packLocal(sck, local);
            if (hasSet.contains(pl)) return;
            Int2DoubleOpenHashMap am = writeAdd.get(sck);
            if (am == null) {
                am = localMapPool.borrow();
                am.defaultReturnValue(0.0);
                writeAdd.put(sck, am);
            }
            am.addTo(local, delta);
        }

        void clearQueuedWrites() {
            for (Int2DoubleOpenHashMap m : writeAdd.values()) localMapPool.recycle(m);
            for (Int2DoubleOpenHashMap m : writeSet.values()) localMapPool.recycle(m);
            writeAdd.clear();
            writeSet.clear();
            hasSet.clear();
        }

        void applyQueuedWrites(long sectionKey, byte @Nullable [] pocketData, int pocketCount, double[] densityOut) {
            Int2DoubleOpenHashMap sm = writeSet.get(sectionKey);
            Int2DoubleOpenHashMap am = writeAdd.get(sectionKey);

            if (sm == null && am == null) return;

            double[] addPocket = TL_ADD.get();
            double[] setPocket = TL_SET.get();
            boolean[] hasSetPocket = TL_HAS_SET.get();
            Arrays.fill(addPocket, 0, pocketCount, 0.0d);
            Arrays.fill(setPocket, 0, pocketCount, 0.0d);
            Arrays.fill(hasSetPocket, 0, pocketCount, false);

            if (sm != null) {
                ObjectIterator<Int2DoubleOpenHashMap.Entry> iter = sm.int2DoubleEntrySet().fastIterator();
                while (iter.hasNext()) {
                    Int2DoubleOpenHashMap.Entry e = iter.next();
                    int local = e.getIntKey();
                    int pi;
                    if (pocketData == null) {
                        pi = 0;
                    } else {
                        pi = readNibble(pocketData, local);
                        if (pi == NO_POCKET || pi >= pocketCount) continue;
                    }
                    setPocket[pi] = e.getDoubleValue();
                    hasSetPocket[pi] = true;
                }
            }

            if (am != null) {
                ObjectIterator<Int2DoubleOpenHashMap.Entry> iter = am.int2DoubleEntrySet().fastIterator();
                while (iter.hasNext()) {
                    Int2DoubleOpenHashMap.Entry e = iter.next();
                    int local = e.getIntKey();
                    int pi;
                    if (pocketData == null) {
                        pi = 0;
                    } else {
                        pi = readNibble(pocketData, local);
                        if (pi == NO_POCKET || pi >= pocketCount) continue;
                    }
                    if (hasSetPocket[pi]) {
                        setPocket[pi] += e.getDoubleValue();
                    } else {
                        addPocket[pi] += e.getDoubleValue();
                    }
                }
            }

            for (int i = 0; i < pocketCount; i++) {
                double v;
                if (hasSetPocket[i]) {
                    v = setPocket[i];
                } else {
                    v = densityOut[i] + addPocket[i];
                }
                densityOut[i] = sanitize(v);
            }
        }

        double sanitize(double v) {
            if (Double.isNaN(v) || Math.abs(v) < RAD_EPSILON && v > minBound) return 0.0D;
            return Math.max(Math.min(v, RAD_MAX), minBound);
        }

        void clearAllSections() {
            chunkRefs.clear(true);
        }

        void clearQueues() {
            MpscUnboundedXaddArrayLongQueue[] cur = activeQueuesCurrent;
            MpscUnboundedXaddArrayLongQueue[] nxt = activeQueuesNext;
            for (int i = 0; i < ACTIVE_STRIPES; i++) {
                cur[i].clear(true);
                nxt[i].clear(true);
            }
        }

        void swapQueues() {
            MpscUnboundedXaddArrayLongQueue[] cur = activeQueuesCurrent;
            for (int i = 0; i < ACTIVE_STRIPES; i++) cur[i].clear(false);
            activeQueuesCurrent = activeQueuesNext;
            activeQueuesNext = cur;
        }

        void enqueueActiveNext(long pocketKey) {
            if (pocketKey == Long.MIN_VALUE) return;
            activeQueuesNext[stripeIndex(pocketKey)].offer(pocketKey);
        }

        void deactivatePocket(long pocketKey) {
            long sck = sectionKeyFromPocketKey(pocketKey);
            long ck = Library.sectionToChunkLong(sck);
            ChunkRef cr = chunkRefs.get(ck);
            if (cr == null) return;

            int yz = Library.getSectionY(pocketKey);
            int sy = (yz >>> 4) & 15;
            int pi = yz & 15;

            cr.clearActiveBit(sy, pi);
        }

        ChunkRef onChunkLoaded(Chunk chunk) {
            int x = chunk.x, z = chunk.z;
            ChunkRef cr = chunkRefs.computeIfAbsent(ChunkPos.asLong(x, z), ChunkRef::new);
            cr.mcChunk = chunk;
            notifyNeighbours(x, z, cr);
            return cr;
        }

        @Nullable SectionRef getSection(long sck) {
            int sy = Library.getSectionY(sck);
            if (isInvalidSectionY(sy)) return null;
            long ck = Library.sectionToChunkLong(sck);
            ChunkRef cr = chunkRefs.get(ck);
            if (cr == null) return null;
            return cr.sec[sy];
        }

        ChunkRef getOrCreateChunkRef(long ck) {
            ChunkRef cr = chunkRefs.get(ck);
            if (cr != null) return cr;

            int cx = Library.getChunkPosX(ck);
            int cz = Library.getChunkPosZ(ck);
            cr = chunkRefs.computeIfAbsent(ck, ChunkRef::new);
            notifyNeighbours(cx, cz, cr);
            return cr;
        }

        private void notifyNeighbours(int cx, int cz, ChunkRef cr) {
            ChunkRef n = chunkRefs.get(ChunkPos.asLong(cx, cz - 1));
            if (n != null && n.mcChunk != null) {
                cr.north = n;
                n.south = cr;
            }
            ChunkRef s = chunkRefs.get(ChunkPos.asLong(cx, cz + 1));
            if (s != null && s.mcChunk != null) {
                cr.south = s;
                s.north = cr;
            }
            ChunkRef w = chunkRefs.get(ChunkPos.asLong(cx - 1, cz));
            if (w != null && w.mcChunk != null) {
                cr.west = w;
                w.east = cr;
            }
            ChunkRef e = chunkRefs.get(ChunkPos.asLong(cx + 1, cz));
            if (e != null && e.mcChunk != null) {
                cr.east = e;
                e.west = cr;
            }
        }

        LongBag getWokenBag(int cap) {
            if (wokenBag.capacity < cap) wokenBag = new LongBag(cap);
            return wokenBag;
        }

        void spawnFog(@Nullable SectionRef sc, int pocketIndex, int cy, Chunk chunk, long seed) {
            int bx = chunk.x << 4;
            int by = cy << 4;
            int bz = chunk.z << 4;
            BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();
            ExtendedBlockStorage[] stor = chunk.getBlockStorageArray();
            ExtendedBlockStorage storage = stor[cy];
            for (int k = 0; k < 10; k++) {
                seed += 0x9E3779B97F4A7C15L;
                int i = (int) (HashCommon.mix(seed) >>> 52);
                int lx = Library.getLocalX(i);
                int lz = Library.getLocalZ(i);
                int ly = Library.getLocalY(i);
                int x = bx + lx;
                int y = by + ly;
                int z = bz + lz;
                long posLong = Library.blockPosToLong(x, y, z);
                if (sc != null && sc.getPocketIndex(posLong) != pocketIndex) continue;
                IBlockState state = (storage == null || storage.isEmpty()) ? Blocks.AIR.getDefaultState() : storage.data.get(i);

                mp.setPos(x, y, z);
                if (state.getMaterial() != Material.AIR) continue;

                boolean nearGround = false;
                for (int d = 1; d <= 6; d++) {
                    int yy = y - d;
                    if (yy < 0) break;
                    int sy = yy >>> 4;
                    ExtendedBlockStorage e = stor[sy];
                    IBlockState below = (e == null || e.isEmpty()) ? Blocks.AIR.getDefaultState() : e.get(lx, yy & 15, lz);
                    mp.setPos(x, yy, z);
                    if (below.getMaterial() != Material.AIR) {
                        nearGround = true;
                        break;
                    }
                }
                if (!nearGround) continue;

                float fx = x + 0.5F, fy = y + 0.5F, fz = z + 0.5F;
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("type", "radiationfog");
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(tag, fx, fy, fz),
                        new TargetPoint(world.provider.getDimension(), fx, fy, fz, 100));
                break;
            }
        }

        void markDirty(long sck) {
            if (sck == Long.MIN_VALUE || !dirtySections.add(sck)) return;
            dirtyQueue.offer(sck);
        }

        void clearDirtyAll() {
            dirtySections.clear(true);
            dirtyQueue.clear(true);
        }

        void ensureDirtyBatchCapacity(int need) {
            if (dirtyChunkKeysScratch.length >= need && dirtyChunkMasksScratchArr.length >= need) return;
            int n = Math.max(dirtyChunkKeysScratch.length, 16);
            while (n < need) n = n + (n >>> 1) + 16;
            dirtyChunkKeysScratch = Arrays.copyOf(dirtyChunkKeysScratch, n);
            dirtyChunkMasksScratchArr = Arrays.copyOf(dirtyChunkMasksScratchArr, n);
        }

        void retireIfNeeded(SectionRef sc) {
            retiredSections.add(sc);
        }

        void clearPendingAll() {
            pendingPocketRadBits.clear(true);
        }

        int nextWorkEpoch() {
            int e = ++workEpoch == 0 ? ++workEpoch : workEpoch;
            workEpochSalt = HashCommon.murmurHash3(worldSalt + (long) e * 0x9E3779B97F4A7C15L);
            return e;
        }

        void unloadChunk(int cx, int cz) {
            long ck = ChunkPos.asLong(cx, cz);
            ChunkRef cr = chunkRefs.get(ck);
            if (cr == null) return;
            ChunkRef n = cr.north;
            if (n != null && n.south == cr) n.south = null;
            ChunkRef s = cr.south;
            if (s != null && s.north == cr) s.north = null;
            ChunkRef w = cr.west;
            if (w != null && w.east == cr) w.east = null;
            ChunkRef e = cr.east;
            if (e != null && e.west == cr) e.west = null;
            cr.north = cr.south = cr.west = cr.east = null;
            cr.mcChunk = null;
            for (int sy = 0; sy < 16; sy++) {
                cr.clearActiveBitMask(sy);
            }
            pendingUnloads.add(ck);
            // Data remains in chunkRefs/writeAdd/writeSet for persistence
        }

        void processUnloadedQueue() {
            if (pendingUnloads.isEmpty()) return;
            Long2ObjectMap<Chunk> loadedChunks = world.getChunkProvider().loadedChunks;
            LongIterator it = pendingUnloads.iterator();
            while (it.hasNext()) {
                long ck = it.nextLong();
                ChunkRef cr = chunkRefs.get(ck);
                if (cr == null) continue;
                if (cr.mcChunk != null) continue;
                if (loadedChunks.containsKey(ck)) continue;
                removeChunkRef(ck);
            }
            pendingUnloads.clear();
        }

        void removeChunkRef(long ck) {
            ChunkRef cr = chunkRefs.remove(ck);

            if (cr == null) {
                int cx = Library.getChunkPosX(ck);
                int cz = Library.getChunkPosZ(ck);
                for (int sy = 0; sy < 16; sy++) clearAux.accept(Library.sectionToLong(cx, sy, cz));
                return;
            }

            for (int sy = 0; sy < 16; sy++) {
                long sck = Library.sectionToLong(Library.getChunkPosX(ck), sy, Library.getChunkPosZ(ck));
                SectionRef old = cr.sec[sy];
                if (old != null) retireIfNeeded(old);
                clearAux.accept(sck);
            }
        }

        void cleanupPools() {
            destructionQueue.clear(true);
            retiredSections.drainAndRecycle(pocketDataPool);
        }

        void readPayload(int cx, int cz, byte @Nullable [] raw) throws DecodeException {
            long ck = ChunkPos.asLong(cx, cz);
            ChunkRef owner = getOrCreateChunkRef(ck);

            for (int sy = 0; sy < 16; sy++) {
                long sck = Library.sectionToLong(cx, sy, cz);
                SectionRef prev = owner.sec[sy];
                if (prev != null) retireIfNeeded(prev);
                owner.clearActiveBitMask(sy);
                owner.sec[sy] = null;
                owner.setKind(sy, ChunkRef.KIND_NONE);

                dirtySections.remove(sck);
                markDirty(sck);
            }

            for (int sy = 0; sy < 16; sy++) {
                long sck = Library.sectionToLong(cx, sy, cz);
                for (int p = 0; p <= NO_POCKET; p++) pendingPocketRadBits.remove(pocketKey(sck, p));
            }

            if (raw == null || raw.length == 0) return;

            ByteBuffer b = ByteBuffer.wrap(raw, 4, raw.length - 4);
            if (b.remaining() < 2) throw new DecodeException("truncated v6 header");
            int entryCount = b.getShort() & 0xFFFF;

            int need = entryCount * (1 + 8);
            if (b.remaining() < need)
                throw new DecodeException("truncated v6 payload: need=" + need + " rem=" + b.remaining());

            for (int i = 0; i < entryCount; i++) {
                int yz = b.get() & 0xFF;
                int sy = (yz >>> 4) & 15;
                int pi = yz & 15;

                double rad = sanitize(b.getDouble());
                if (rad == 0.0D) continue;
                long sck = Library.sectionToLong(cx, sy, cz);
                long pk = pocketKey(sck, pi);
                pendingPocketRadBits.put(pk, Double.doubleToRawLongBits(rad));
            }
        }

        void rebuildChunkPocketsLoaded(long sectionKey, @Nullable ExtendedBlockStorage ebs) {
            int sy = Library.getSectionY(sectionKey);
            long ck = Library.sectionToChunkLong(sectionKey);
            ChunkRef owner = getOrCreateChunkRef(ck);
            owner.clearActiveBitMask(sy);

            int oldKind = owner.getKind(sy);
            SectionRef old = owner.sec[sy];
            byte[] pocketData;
            int pocketCount;
            int[] vols = TL_VOL_COUNTS.get();
            long[] sumX = TL_SUM_X.get();
            long[] sumY = TL_SUM_Y.get();
            long[] sumZ = TL_SUM_Z.get();

            int singleVolume0 = SECTION_BLOCK_COUNT;
            short[] singleFaceCounts = null;

            SectionMask resistant = scanResistantMask(world, sectionKey, ebs);

            if (resistant == null || resistant.isEmpty()) {
                pocketCount = 1;
                pocketData = null;
            } else {
                byte[] scratch = pocketDataPool.borrow();
                Arrays.fill(scratch, (byte) 0xFF);
                int[] queue = TL_FF_QUEUE.get();
                Arrays.fill(vols, 0);
                Arrays.fill(sumX, 0);
                Arrays.fill(sumY, 0);
                Arrays.fill(sumZ, 0);

                int pc = 0;

                for (int blockIndex = 0; blockIndex < SECTION_BLOCK_COUNT; blockIndex++) {
                    if (readNibble(scratch, blockIndex) != NO_POCKET) continue;
                    if (resistant.get(blockIndex)) continue;

                    int currentPaletteIndex = (pc >= NO_POCKET) ? 0 : pc++;
                    int head = 0, tail = 0;
                    queue[tail++] = blockIndex;
                    writeNibble(scratch, blockIndex, currentPaletteIndex);

                    vols[currentPaletteIndex]++;
                    sumX[currentPaletteIndex] += Library.getLocalX(blockIndex);
                    sumY[currentPaletteIndex] += Library.getLocalY(blockIndex);
                    sumZ[currentPaletteIndex] += Library.getLocalZ(blockIndex);

                    while (head != tail) {
                        int currentIndex = queue[head++];
                        for (int i = 0; i < 6; i++) {
                            int neighborIndex = currentIndex + LINEAR_OFFSETS[i];
                            if (((neighborIndex & 0xF000) | ((currentIndex ^ neighborIndex) & BOUNDARY_MASKS[i])) != 0)
                                continue;
                            if (readNibble(scratch, neighborIndex) != NO_POCKET) continue;
                            if (resistant.get(neighborIndex)) continue;
                            writeNibble(scratch, neighborIndex, currentPaletteIndex);
                            queue[tail++] = neighborIndex;

                            vols[currentPaletteIndex]++;
                            sumX[currentPaletteIndex] += Library.getLocalX(neighborIndex);
                            sumY[currentPaletteIndex] += Library.getLocalY(neighborIndex);
                            sumZ[currentPaletteIndex] += Library.getLocalZ(neighborIndex);
                        }
                    }
                }

                pocketCount = pc;
                if (pocketCount > 0) {
                    pocketData = scratch;
                    if (pocketCount == 1) {
                        singleVolume0 = Math.max(1, vols[0]);
                        singleFaceCounts = new short[6];
                        for (int face = 0; face < 6; face++) {
                            int base = face << 8;
                            int count = 0;
                            for (int t = 0; t < 256; t++) {
                                int idx = FACE_PLANE[base + t];
                                if (readNibble(pocketData, idx) == 0) count++;
                            }
                            singleFaceCounts[face] = (short) count;
                        }
                    }
                } else {
                    pocketDataPool.recycle(scratch);
                    pocketData = null;
                }
            }

            if (pocketCount <= 0) {
                if (old != null) retireIfNeeded(old);
                owner.sec[sy] = null;
                owner.setKind(sy, ChunkRef.KIND_NONE);
                for (int p = 0; p <= NO_POCKET; p++) pendingPocketRadBits.remove(pocketKey(sectionKey, p));
                return;
            }
            double[] newPocketMasses = TL_NEW_MASS.get();
            Arrays.fill(newPocketMasses, 0, pocketCount, 0.0d);

            // Handle transition mass
            if (oldKind != ChunkRef.KIND_NONE) {
                if (pocketCount == 1 && pocketData == null) {
                    double totalMass = 0.0d;
                    if (oldKind == ChunkRef.KIND_UNI) {
                        double d = owner.uniformRads[sy];
                        if (Math.abs(d) > RAD_EPSILON) totalMass = mulClamp(d, SECTION_BLOCK_COUNT);
                    } else if (oldKind == ChunkRef.KIND_SINGLE) {
                        SingleMaskedSectionRef single = (SingleMaskedSectionRef) old;
                        int v = Math.max(1, single.volume);
                        double d = single.rad;
                        if (Math.abs(d) > RAD_EPSILON) totalMass = mulClamp(d, v);
                    } else if (oldKind == ChunkRef.KIND_MULTI) {
                        MultiSectionRef mob = (MultiSectionRef) old;
                        int oldCnt = mob.pocketCount & 0xFF;
                        for (int i = 0; i < oldCnt; i++) {
                            int v = Math.max(1, mob.volume[i]);
                            double d = mob.data[i << 1];
                            if (Math.abs(d) > RAD_EPSILON) totalMass = addClamp(totalMass, mulClamp(d, v));
                        }
                    }
                    newPocketMasses[0] = totalMass;
                } else {
                    if (oldKind == ChunkRef.KIND_UNI) {
                        double d = owner.uniformRads[sy];
                        if (Math.abs(d) > RAD_EPSILON) {
                            double oldMass = mulClamp(d, SECTION_BLOCK_COUNT);
                            long totalNewAir = 0L;
                            for (int i = 0; i < pocketCount; i++) totalNewAir += Math.max(1, vols[i]);
                            if (totalNewAir > 0L) {
                                double massPerBlock = oldMass / (double) totalNewAir;
                                for (int i = 0; i < pocketCount; i++) {
                                    int v = Math.max(1, vols[i]);
                                    newPocketMasses[i] = mulClamp(massPerBlock, v);
                                }
                            }
                        }
                    } else {
                        int oldCnt = Math.min(old.pocketCount & 0xFF, NO_POCKET);
                        double[] oldTotalMass = TL_OLD_MASS.get();
                        Arrays.fill(oldTotalMass, 0, oldCnt, 0.0d);

                        if (oldKind == ChunkRef.KIND_SINGLE) {
                            SingleMaskedSectionRef single = (SingleMaskedSectionRef) old;
                            int v = Math.max(1, single.volume);
                            double d = single.rad;
                            if (Math.abs(d) > RAD_EPSILON) oldTotalMass[0] = mulClamp(d, v);
                        } else if (oldKind == ChunkRef.KIND_MULTI) {
                            MultiSectionRef mob = (MultiSectionRef) old;
                            for (int i = 0; i < oldCnt; i++) {
                                int v = Math.max(1, mob.volume[i]);
                                double d = mob.data[i << 1];
                                if (Math.abs(d) > RAD_EPSILON) oldTotalMass[i] = mulClamp(d, v);
                            }
                        }

                        int[] overlaps = TL_OVERLAPS.get();
                        Arrays.fill(overlaps, 0, oldCnt * pocketCount, 0);
                        byte[] oldPocketData = old.pocketData;
                        for (int i = 0; i < SECTION_BLOCK_COUNT; i++) {
                            int nIdx = readNibble(pocketData, i);
                            if (nIdx >= pocketCount) continue;
                            int oIdx = readNibble(oldPocketData, i);
                            if (oIdx >= oldCnt) continue;
                            overlaps[oIdx * pocketCount + nIdx]++;
                        }

                        for (int o = 0; o < oldCnt; o++) {
                            double mass = oldTotalMass[o];
                            if (Math.abs(mass) <= RAD_EPSILON) continue;
                            int totalRemainingBlocks = 0;
                            int row = o * pocketCount;
                            for (int n = 0; n < pocketCount; n++) totalRemainingBlocks += overlaps[row + n];
                            if (totalRemainingBlocks != 0) {
                                double massPerBlock = mass / (double) totalRemainingBlocks;
                                for (int n = 0; n < pocketCount; n++) {
                                    int count = overlaps[row + n];
                                    if (count != 0)
                                        newPocketMasses[n] = addClamp(newPocketMasses[n], mulClamp(massPerBlock, count));
                                }
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < pocketCount; i++) {
                double m = newPocketMasses[i];
                if (Math.abs(m) < RAD_EPSILON) newPocketMasses[i] = 0.0D;
                else if (m > Double.MAX_VALUE) newPocketMasses[i] = Double.MAX_VALUE;
                else if (m < -Double.MAX_VALUE) newPocketMasses[i] = -Double.MAX_VALUE;
                else if (Double.isNaN(m)) newPocketMasses[i] = 0.0D;
            }

            double[] densities = new double[pocketCount];
            for (int i = 0; i < pocketCount; i++) {
                int vol = (pocketCount == 1 && pocketData == null) ? SECTION_BLOCK_COUNT : Math.max(1, vols[i]);
                densities[i] = newPocketMasses[i] / (double) vol;
                long pk = pocketKey(sectionKey, i);
                long bits = pendingPocketRadBits.remove(pk);
                if (bits != 0L) {
                    double v = Double.longBitsToDouble(bits);
                    if (Double.isFinite(v) && Math.abs(v) > RAD_EPSILON) densities[i] = v;
                }
            }
            for (int i = pocketCount; i <= NO_POCKET; i++) pendingPocketRadBits.remove(pocketKey(sectionKey, i));

            applyQueuedWrites(sectionKey, pocketData, pocketCount, densities);

            if (old != null) retireIfNeeded(old);

            // New is Uniform
            if (pocketCount == 1 && pocketData == null) {
                owner.uniformRads[sy] = densities[0];
                owner.setKind(sy, ChunkRef.KIND_UNI);
                owner.sec[sy] = null;

                if (densities[0] != 0.0D) {
                    if (owner.setActiveBit(sy, 0)) enqueueActiveNext(pocketKey(sectionKey, 0));
                }
                return;
            }

            // New is Single Masked
            if (pocketCount == 1) {
                double density = densities[0];
                double inv = 1.0d / singleVolume0;
                double cx = sumX[0] * inv;
                double cy = sumY[0] * inv;
                double cz = sumZ[0] * inv;

                SingleMaskedSectionRef masked = new SingleMaskedSectionRef(owner, sy, pocketData, singleVolume0, singleFaceCounts, cx, cy, cz);
                masked.rad = density;

                owner.sec[sy] = masked;
                owner.setKind(sy, ChunkRef.KIND_SINGLE);

                if (density != 0.0D) {
                    if (owner.setActiveBit(sy, 0)) enqueueActiveNext(pocketKey(sectionKey, 0));
                }
                return;
            }

            // New is Multi
            double[] faceDists = new double[pocketCount * 6];
            for (int i = 0; i < pocketCount; i++) {
                int v = Math.max(1, vols[i]);
                double inv = 1.0d / v;
                double cx = sumX[i] * inv;
                double cy = sumY[i] * inv;
                double cz = sumZ[i] * inv;
                int base = i * 6;
                faceDists[base] = cy + 0.5d;
                faceDists[base + 1] = 15.5d - cy;
                faceDists[base + 2] = cz + 0.5d;
                faceDists[base + 3] = 15.5d - cz;
                faceDists[base + 4] = cx + 0.5d;
                faceDists[base + 5] = 15.5d - cx;
            }

            MultiSectionRef sc = new MultiSectionRef(owner, sy, (byte) pocketCount, pocketData, faceDists);
            for (int i = 0; i < pocketCount; i++) {
                int vol = Math.max(1, vols[i]);
                int dataBase = i << 1;
                sc.data[dataBase] = densities[i];
                sc.data[dataBase + 1] = 1.0d / vol;
                sc.volume[i] = vol;
            }

            owner.sec[sy] = sc;
            owner.setKind(sy, ChunkRef.KIND_MULTI);

            for (int i = 0; i < pocketCount; i++) {
                double rad = sc.data[i << 1];
                if (rad != 0.0D) {
                    if (owner.setActiveBit(sy, i)) enqueueActiveNext(pocketKey(sectionKey, i));
                }
            }
        }

        void relinkKeys(long[] keys, int hi) {
            if (hi <= 0) return;
            ensureLinkScratch(hi << 1);
            int eN = buildLinkKeys(keys, hi, EnumFacing.WEST.ordinal(), linkScratch);
            int threshold = getTaskThreshold(eN, 32);
            if (eN > 0) new LinkDirTask(linkScratch, 0, eN, EnumFacing.EAST.ordinal(), threshold).invoke();
            int uN = buildLinkKeys(keys, hi, EnumFacing.DOWN.ordinal(), linkScratch);
            threshold = getTaskThreshold(uN, 32);
            if (uN > 0) new LinkDirTask(linkScratch, 0, uN, EnumFacing.UP.ordinal(), threshold).invoke();
            int sN = buildLinkKeys(keys, hi, EnumFacing.NORTH.ordinal(), linkScratch);
            threshold = getTaskThreshold(sN, 32);
            if (sN > 0) new LinkDirTask(linkScratch, 0, sN, EnumFacing.SOUTH.ordinal(), threshold).invoke();
        }

        private void ensureLinkScratch(int need) {
            long[] a = linkScratch;
            if (a.length >= need) return;
            int n = a.length;
            while (n < need) n = n + (n >>> 1) + 16;
            linkScratch = Arrays.copyOf(a, n);
        }

        void linkCanonical(long aKey, int faceA) {
            int ay = Library.getSectionY(aKey);
            if (isInvalidSectionY(ay)) return;
            ChunkRef crA = chunkRefs.get(Library.sectionToChunkLong(aKey));
            if (crA == null) return;
            int kA = crA.getKind(ay);
            if (kA == ChunkRef.KIND_NONE) return;
            int by = ay + FACE_DY[faceA];
            if (isInvalidSectionY(by)) {
                if (kA != ChunkRef.KIND_UNI) crA.sec[ay].clearFaceAllPockets(faceA);
                return;
            }
            long bKey = Library.sectionToLong(Library.getSectionX(aKey) + FACE_DX[faceA], by, Library.getSectionZ(aKey) + FACE_DZ[faceA]);
            ChunkRef crB = chunkRefs.get(Library.sectionToChunkLong(bKey));
            int kB = (crB == null) ? ChunkRef.KIND_NONE : crB.getKind(by);
            if (kB == ChunkRef.KIND_NONE) {
                if (kA != ChunkRef.KIND_UNI) {
                    SectionRef a = crA.sec[ay];
                    a.clearFaceAllPockets(faceA);
                    if (kA == ChunkRef.KIND_MULTI) ((MultiSectionRef) a).markSentinelPlane16x16(faceA);
                }
                return;
            }
            if (kA == ChunkRef.KIND_UNI) {
                if (kB != ChunkRef.KIND_UNI) crB.sec[by].linkFaceToUniform(crA, faceA ^ 1);
                return;
            }
            SectionRef a = crA.sec[ay];
            if (kB == ChunkRef.KIND_UNI) {
                a.linkFaceToUniform(crB, faceA);
            } else if (kB == ChunkRef.KIND_MULTI) {
                a.linkFaceToMulti((MultiSectionRef) crB.sec[by], faceA);
            } else {
                a.linkFaceToSingle((SingleMaskedSectionRef) crB.sec[by], faceA);
            }
        }

        final class MergeActiveAndCountParityTask extends RecursiveAction {
            final int[] activeOffsets;
            final int lo, hi;

            MergeActiveAndCountParityTask(int[] activeOffsets, int lo, int hi) {
                this.activeOffsets = activeOffsets;
                this.lo = lo;
                this.hi = hi;
            }

            @Override
            protected void compute() {
                if (hi - lo <= 4) {
                    long[] buf = activeBuf;
                    SectionRef[] refs = activeRefs;
                    ChunkRef[] cRefs = activeChunkRefs;
                    long[][] sBufs = activeStripeBufs;
                    SectionRef[][] sRefs = activeStripeRefs;
                    ChunkRef[][] sCRefs = activeStripeChunkRefs;
                    int[] activeCounts = activeStripeCounts;
                    int[] touchedCounts = touchedStripeCounts;
                    ChunkRef[][] touchedRefs = touchedStripeRefs;
                    int[][] stripeCounts = parityStripeCounts;

                    for (int s = lo; s < hi; s++) {
                        int ac = activeCounts[s];
                        if (ac != 0) {
                            int dest = activeOffsets[s];
                            System.arraycopy(sBufs[s], 0, buf, dest, ac);
                            System.arraycopy(sRefs[s], 0, refs, dest, ac);
                            System.arraycopy(sCRefs[s], 0, cRefs, dest, ac);
                        }
                        int[] my = stripeCounts[s];
                        my[0] = my[1] = my[2] = my[3] = 0;
                        int tc = touchedCounts[s];
                        if (tc == 0) continue;
                        ChunkRef[] tr = touchedRefs[s];
                        int c0 = 0, c1 = 0, c2 = 0, c3 = 0;
                        for (int i = 0; i < tc; i++) {
                            long ck = tr[i].ck;
                            int b = (int) ((ck & 1L) | ((ck >>> 31) & 2L));
                            switch (b) {
                                case 0 -> c0++;
                                case 1 -> c1++;
                                case 2 -> c2++;
                                case 3 -> c3++;
                            }
                        }
                        my[0] = c0;
                        my[1] = c1;
                        my[2] = c2;
                        my[3] = c3;
                    }
                } else {
                    int mid = (lo + hi) >>> 1;
                    invokeAll(new MergeActiveAndCountParityTask(activeOffsets, lo, mid),
                            new MergeActiveAndCountParityTask(activeOffsets, mid, hi));
                }
            }
        }

        final class BuildFinalizeRunsTask extends RecursiveAction {
            final int count;

            BuildFinalizeRunsTask(int count) {
                this.count = count;
            }

            @Override
            protected void compute() {
                if (count == 0) return;
                ChunkRef[] crArr = activeChunkRefs;
                int[] rs = finalizeRunStarts;
                if (rs.length < count + 1) {
                    finalizeRunStarts = rs = new int[count + 1];
                }

                int rc = 0;
                rs[rc++] = 0;

                ChunkRef prev = crArr[0]; // invariant: non-null
                for (int i = 1; i < count; i++) {
                    ChunkRef cur = crArr[i];
                    if (cur != prev) {
                        rs[rc++] = i; // start of next run
                        prev = cur;
                    }
                }

                rs[rc] = count; // sentinel end
                finalizeRunCount = rc;
            }
        }

        final class BuildWokenRunsTask extends RecursiveAction {
            final LongBag bag;
            final int count;

            BuildWokenRunsTask(LongBag bag, int count) {
                this.bag = bag;
                this.count = count;
            }

            @Override
            protected void compute() {
                if (count <= 0) {
                    wokenKeyCount = 0;
                    wokenRunCount = 0;
                    return;
                }
                long[] out = wokenKeys;
                if (out.length < count) wokenKeys = out = new long[count + (count >>> 1) + 16];
                int n = Math.min(count, bag.capacity);
                for (int i = 0; i < n; i++) {
                    long[] chunk = bag.chunks[i >>> LongBag.CHUNK_SHIFT];
                    out[i] = chunk[i & LongBag.CHUNK_MASK];
                }
                for (int i = 0; i < n; i++) out[i] = Long.rotateLeft(out[i], 20);
                Arrays.sort(out, 0, n);
                int u = 0;
                long prev = Long.MIN_VALUE;
                for (int i = 0; i < n; i++) {
                    long v = out[i];
                    if (i == 0 || v != prev) out[u++] = v;
                    prev = v;
                }
                for (int i = 0; i < u; i++) out[i] = Long.rotateRight(out[i], 20);
                int[] rs = wokenRunStarts;
                if (rs.length < u + 1) wokenRunStarts = rs = new int[u + 1];

                int rc = 0;
                rs[rc++] = 0;

                long prevCk = Library.sectionToChunkLong(out[0]);
                for (int i = 1; i < u; i++) {
                    long ck = Library.sectionToChunkLong(out[i]);
                    if (ck != prevCk) {
                        rs[rc++] = i;
                        prevCk = ck;
                    }
                }
                rs[rc] = u;

                wokenKeyCount = u;
                wokenRunCount = rc;
            }
        }

        final class FinalizeTask extends RecursiveAction {
            final int runLo, runHi;
            final int elemThreshold;

            FinalizeTask(int runLo, int runHi, int elemThreshold) {
                this.runLo = runLo;
                this.runHi = runHi;
                this.elemThreshold = elemThreshold;
            }

            @Override
            protected void compute() {
                int lo = finalizeRunStarts[runLo];
                int hi = finalizeRunStarts[runHi];
                int runs = runHi - runLo;
                if (hi - lo <= elemThreshold || runs <= 8) {
                    finalizeRange(activeBuf, activeRefs, activeChunkRefs, lo, hi);
                    return;
                }
                int mid = (runLo + runHi) >>> 1;
                invokeAll(new FinalizeTask(runLo, mid, elemThreshold),
                        new FinalizeTask(mid, runHi, elemThreshold)
                );
            }
        }

        final class FinalizeWokenTask extends RecursiveAction {
            final int runLo, runHi;
            final int elemThreshold;

            FinalizeWokenTask(int runLo, int runHi, int elemThreshold) {
                this.runLo = runLo;
                this.runHi = runHi;
                this.elemThreshold = elemThreshold;
            }

            @Override
            protected void compute() {
                int lo = wokenRunStarts[runLo];
                int hi = wokenRunStarts[runHi];
                int runs = runHi - runLo;
                if (hi - lo <= elemThreshold || runs <= 8) {
                    finalizeRange(wokenKeys, null, null, lo, hi);
                    return;
                }
                int mid = (runLo + runHi) >>> 1;
                invokeAll(new FinalizeWokenTask(runLo, mid, elemThreshold),
                        new FinalizeWokenTask(mid, runHi, elemThreshold)
                );
            }
        }

        final class ScatterParityTask extends RecursiveAction {
            final int lo, hi;

            ScatterParityTask(int lo, int hi) {
                this.lo = lo;
                this.hi = hi;
            }

            @Override
            protected void compute() {
                if (hi - lo <= 4) {
                    int[] touchedCounts = touchedStripeCounts;
                    ChunkRef[][] touchedRefs = touchedStripeRefs;
                    int[][] stripeOffsets = parityStripeOffsets;
                    ChunkRef[][] buckets = parityBuckets;
                    ChunkRef[] b0 = buckets[0];
                    ChunkRef[] b1 = buckets[1];
                    ChunkRef[] b2 = buckets[2];
                    ChunkRef[] b3 = buckets[3];

                    for (int s = lo; s < hi; s++) {
                        int c = touchedCounts[s];
                        if (c == 0) continue;
                        ChunkRef[] src = touchedRefs[s];
                        int[] myOffsets = stripeOffsets[s];
                        int i0 = myOffsets[0];
                        int i1 = myOffsets[1];
                        int i2 = myOffsets[2];
                        int i3 = myOffsets[3];

                        for (int i = 0; i < c; i++) {
                            ChunkRef cr = src[i];
                            src[i] = null;
                            long ck = cr.ck;
                            int bucketIdx = (int) ((ck & 1) | ((ck >>> 31) & 2));
                            switch (bucketIdx) {
                                case 0 -> b0[i0++] = cr;
                                case 1 -> b1[i1++] = cr;
                                case 2 -> b2[i2++] = cr;
                                case 3 -> b3[i3++] = cr;
                            }
                        }
                    }
                } else {
                    int mid = (lo + hi) >>> 1;
                    invokeAll(new ScatterParityTask(lo, mid), new ScatterParityTask(mid, hi));
                }
            }
        }

        final class LinkDirTask extends RecursiveAction {
            final long[] keys;
            final int lo, hi;
            final int canonicalFace;
            final int threshold;

            LinkDirTask(long[] keys, int lo, int hi, int canonicalFace, int threshold) {
                this.keys = keys;
                this.lo = lo;
                this.hi = hi;
                this.canonicalFace = canonicalFace;
                this.threshold = threshold;
            }

            @Override
            protected void compute() {
                int n = hi - lo;
                if (n <= threshold) {
                    for (int i = lo; i < hi; i++) linkCanonical(keys[i], canonicalFace);
                } else {
                    int mid = (lo + hi) >>> 1;
                    invokeAll(new LinkDirTask(keys, lo, mid, canonicalFace, threshold),
                            new LinkDirTask(keys, mid, hi, canonicalFace, threshold));
                }
            }
        }

        final class RebuildChunkBatchTask extends RecursiveAction {
            final long[] chunkKeys;
            final int[] masks;

            final int lo;
            final int hi;
            final int threshold;

            RebuildChunkBatchTask(long[] chunkKeys, int[] masks, int lo, int hi, int threshold) {
                this.chunkKeys = chunkKeys;
                this.masks = masks;
                this.lo = lo;
                this.hi = hi;
                this.threshold = threshold;
            }

            @Override
            protected void compute() {
                if (hi - lo <= threshold) {
                    Long2ObjectMap<Chunk> loadedChunks = world.getChunkProvider().loadedChunks;
                    for (int i = lo; i < hi; i++) {
                        long ck = chunkKeys[i];
                        int mask = masks[i];
                        Chunk chunk = loadedChunks.get(ck);
                        if (chunk == null) continue;

                        onChunkLoaded(chunk);

                        ExtendedBlockStorage[] stor = chunk.getBlockStorageArray();
                        int m = mask;
                        while (m != 0) {
                            int sy = Integer.numberOfTrailingZeros(m);
                            m &= (m - 1);
                            long sck = Library.sectionToLong(chunk.x, sy, chunk.z);
                            rebuildChunkPocketsLoaded(sck, stor[sy]);
                        }
                    }
                    return;
                }

                int mid = (lo + hi) >>> 1;
                invokeAll(
                        new RebuildChunkBatchTask(chunkKeys, masks, lo, mid, threshold),
                        new RebuildChunkBatchTask(chunkKeys, masks, mid, hi, threshold)
                );
            }
        }

        final class DrainStripeTask extends RecursiveAction {
            final MpscUnboundedXaddArrayLongQueue q;
            final int stripe, epoch;

            DrainStripeTask(MpscUnboundedXaddArrayLongQueue q, int stripe, int epoch) {
                this.q = q;
                this.stripe = stripe;
                this.epoch = epoch;
            }

            @Override
            protected void compute() {
                long cIndex = U.getLong(q, CI_OFF);
                Object cChunk = U.getReference(q, CC_OFF);
                int chunkMask = q.chunkSize() - 1;
                long[] outPockets = activeStripeBufs[stripe];
                int qCount = 0;
                while (true) {
                    int co = (int) (cIndex & chunkMask);
                    long[] buf = (long[]) U.getReference(cChunk, BUF_OFF);
                    long elOff = offLong(co);
                    long pk;
                    if (co == 0 && cIndex != 0) {
                        Object next = U.getReference(cChunk, NEXT_OFF);
                        if (next == null) break;
                        long[] nBuf = (long[]) U.getReference(next, BUF_OFF);
                        pk = U.getLong(nBuf, JA_BASE);
                        if (pk == MpscUnboundedXaddArrayLongQueue.EMPTY) break;
                        U.putReference(cChunk, NEXT_OFF, null);
                        U.putReference(next, PREV_OFF, null);
                        if (U.getBoolean(cChunk, POOLED_OFF)) {
                            // noinspection unchecked, rawtypes
                            ((SpscArrayQueue) U.getReference(q, FCP_OFF)).offer(cChunk);
                        }
                        cChunk = next;
                        U.putLong(nBuf, JA_BASE, MpscUnboundedXaddArrayLongQueue.EMPTY);
                    } else {
                        pk = U.getLong(buf, elOff);
                        if (pk == MpscUnboundedXaddArrayLongQueue.EMPTY) break;
                        U.putLong(buf, elOff, MpscUnboundedXaddArrayLongQueue.EMPTY);
                    }
                    cIndex++;
                    if (qCount == outPockets.length) {
                        outPockets = Arrays.copyOf(outPockets, outPockets.length << 1);
                        activeStripeBufs[stripe] = outPockets;
                    }
                    outPockets[qCount++] = Long.rotateLeft(pk, 20);
                }
                U.putLong(q, CI_OFF, cIndex);
                U.putReference(q, CC_OFF, cChunk);
                if (qCount == 0) {
                    activeStripeCounts[stripe] = 0;
                    touchedStripeCounts[stripe] = 0;
                    return;
                }
                Arrays.sort(outPockets, 0, qCount);
                SectionRef[] outRefs = activeStripeRefs[stripe];
                ChunkRef[] outChunkRefs = activeStripeChunkRefs[stripe];
                if (outRefs.length < outPockets.length) {
                    outRefs = new SectionRef[outPockets.length];
                    activeStripeRefs[stripe] = outRefs;
                    outChunkRefs = new ChunkRef[outPockets.length];
                    activeStripeChunkRefs[stripe] = outChunkRefs;
                }
                ChunkRef[] outTouched = touchedStripeRefs[stripe];
                if (outTouched.length < outPockets.length) {
                    outTouched = new ChunkRef[outPockets.length << 1];
                    touchedStripeRefs[stripe] = outTouched;
                }
                int pocketWriteIdx = 0, touchedWriteIdx = 0, readIdx = 0;
                int prevPocketCount = activeStripeCounts[stripe];
                int prevTouchedCount = touchedStripeCounts[stripe];
                while (readIdx < qCount) {
                    long groupRotatedKey = outPockets[readIdx];
                    if (readIdx > 0 && groupRotatedKey == outPockets[readIdx - 1]) {
                        readIdx++;
                        continue;
                    }
                    long ck = Library.sectionToChunkLong(Long.rotateRight(groupRotatedKey, 20));
                    ChunkRef cr = chunkRefs.get(ck);
                    boolean chunkHasActivePockets = false;
                    while (readIdx < qCount) {
                        long nextRotated = outPockets[readIdx];
                        if ((nextRotated & 0xFFFFFFFFFFF00000L) != (groupRotatedKey & 0xFFFFFFFFFFF00000L)) break;
                        if (readIdx > 0 && nextRotated == outPockets[readIdx - 1]) {
                            readIdx++;
                            continue;
                        }
                        readIdx++;
                        if (cr == null) continue;
                        long realPk = Long.rotateRight(nextRotated, 20);
                        int yz = Library.getSectionY(realPk), sy = (yz >>> 4) & 15, pi = yz & 15;
                        if (cr.isInactive(sy, pi)) continue;
                        int kind = cr.getKind(sy);
                        if (kind == ChunkRef.KIND_UNI) {
                            if (pi != 0) {
                                cr.clearActiveBit(sy, pi);
                                continue;
                            }
                            if (cr.markUniEpoch(sy, epoch)) {
                                outPockets[pocketWriteIdx] = realPk;
                                outRefs[pocketWriteIdx] = null;
                                outChunkRefs[pocketWriteIdx++] = cr;
                                chunkHasActivePockets = true;
                            }
                        } else if (kind == ChunkRef.KIND_SINGLE) {
                            if (pi != 0) {
                                cr.clearActiveBit(sy, pi);
                                continue;
                            }
                            SingleMaskedSectionRef single = (SingleMaskedSectionRef) cr.sec[sy];
                            if (single.markWakeEpoch(epoch)) {
                                outPockets[pocketWriteIdx] = realPk;
                                outRefs[pocketWriteIdx] = single;
                                outChunkRefs[pocketWriteIdx++] = cr;
                                chunkHasActivePockets = true;
                            }
                        } else { // MULTI
                            MultiSectionRef multi = (MultiSectionRef) cr.sec[sy];
                            if (multi.markWakeEpoch(pi, epoch)) {
                                outPockets[pocketWriteIdx] = realPk;
                                outRefs[pocketWriteIdx] = multi;
                                outChunkRefs[pocketWriteIdx++] = cr;
                                chunkHasActivePockets = true;
                            }
                        }
                    }

                    if (chunkHasActivePockets) {
                        if (touchedWriteIdx + 9 > outTouched.length) {
                            outTouched = Arrays.copyOf(outTouched, outTouched.length + (outTouched.length >>> 1) + 16);
                            touchedStripeRefs[stripe] = outTouched;
                        }
                        // Mark Center
                        if (cr.tryMarkTouched(epoch)) outTouched[touchedWriteIdx++] = cr;
                        ChunkRef n = cr.north, s = cr.south, e = cr.east, w = cr.west;
                        if (n != null && n.tryMarkTouched(epoch)) outTouched[touchedWriteIdx++] = n;
                        if (s != null && s.tryMarkTouched(epoch)) outTouched[touchedWriteIdx++] = s;
                        if (e != null && e.tryMarkTouched(epoch)) outTouched[touchedWriteIdx++] = e;
                        if (w != null && w.tryMarkTouched(epoch)) outTouched[touchedWriteIdx++] = w;

                        // Diagonals
                        ChunkRef ne = (n != null) ? n.east : (e != null) ? e.north : null;
                        if (ne != null && ne.tryMarkTouched(epoch)) outTouched[touchedWriteIdx++] = ne;
                        ChunkRef nw = (n != null) ? n.west : (w != null) ? w.north : null;
                        if (nw != null && nw.tryMarkTouched(epoch)) outTouched[touchedWriteIdx++] = nw;
                        ChunkRef se = (s != null) ? s.east : (e != null) ? e.south : null;
                        if (se != null && se.tryMarkTouched(epoch)) outTouched[touchedWriteIdx++] = se;
                        ChunkRef sw = (s != null) ? s.west : (w != null) ? w.south : null;
                        if (sw != null && sw.tryMarkTouched(epoch)) outTouched[touchedWriteIdx++] = sw;
                    }
                }
                if (pocketWriteIdx < prevPocketCount) {
                    Arrays.fill(outRefs, pocketWriteIdx, prevPocketCount, null);
                    Arrays.fill(outChunkRefs, pocketWriteIdx, prevPocketCount, null);
                }
                if (touchedWriteIdx < prevTouchedCount)
                    Arrays.fill(outTouched, touchedWriteIdx, prevTouchedCount, null);

                activeStripeCounts[stripe] = pocketWriteIdx;
                touchedStripeCounts[stripe] = touchedWriteIdx;
            }
        }
    }

    private static final class LongBag {
        static final int CHUNK_SHIFT = 10;
        static final int CHUNK_SIZE = 1 << CHUNK_SHIFT;
        static final int CHUNK_MASK = CHUNK_SIZE - 1;
        static final long SIZE_OFF = fieldOffset(LongBag.class, "size");

        final long[][] chunks;
        final int capacity;
        volatile int size;

        LongBag(int cap) {
            int chunkCount = (int) (((long) cap + CHUNK_SIZE - 1L) >>> CHUNK_SHIFT);
            chunks = new long[chunkCount][];
            capacity = chunkCount * CHUNK_SIZE;
        }

        void clear() {
            size = 0;
        }

        boolean tryAdd(long v) {
            int i = U.getAndAddInt(this, SIZE_OFF, 1);
            if (i >= capacity) {
                while (true) {
                    int s = size;
                    if (s <= capacity) break;
                    if (U.compareAndSetInt(this, SIZE_OFF, s, capacity)) break;
                }
                return false;
            }
            int c = i >>> CHUNK_SHIFT;
            int o = i & CHUNK_MASK;
            long[] chunk = chunks[c];
            if (chunk == null) {
                long chunkAddr = offReference(c);
                chunk = (long[]) U.getReferenceVolatile(chunks, chunkAddr);
                if (chunk == null) {
                    long[] newChunk = new long[CHUNK_SIZE];
                    if (U.compareAndSetReference(chunks, chunkAddr, null, newChunk)) chunk = newChunk;
                    else chunk = (long[]) U.getReferenceVolatile(chunks, chunkAddr);
                }
            }
            chunk[o] = v;
            return true;
        }

        int size() {
            return Math.min(size, capacity);
        }
    }

    private static final class SectionRetireBag {
        static final int CHUNK_SHIFT = 10;
        static final int CHUNK_SIZE = 1 << CHUNK_SHIFT;
        static final int CHUNK_MASK = CHUNK_SIZE - 1;
        static final long SIZE_OFF = fieldOffset(SectionRetireBag.class, "size");

        final SectionRef[][] chunks;
        final int capacity;
        volatile int size;

        SectionRetireBag(int cap) {
            int chunkCount = (int) (((long) cap + CHUNK_SIZE - 1L) >>> CHUNK_SHIFT);
            chunks = new SectionRef[chunkCount][];
            capacity = chunkCount * CHUNK_SIZE;
        }

        void add(SectionRef v) {
            int i = U.getAndAddInt(this, SIZE_OFF, 1);
            if (i >= capacity) {
                while (true) {
                    int s = size;
                    if (s <= capacity) break;
                    if (U.compareAndSetInt(this, SIZE_OFF, s, capacity)) break;
                }
                return;
            }

            int c = i >>> CHUNK_SHIFT;
            int o = i & CHUNK_MASK;

            SectionRef[] chunk = chunks[c];
            if (chunk == null) {
                long addr = offReference(c);
                chunk = (SectionRef[]) U.getReferenceVolatile(chunks, addr);
                if (chunk == null) {
                    SectionRef[] newChunk = new SectionRef[CHUNK_SIZE];
                    if (U.compareAndSetReference(chunks, addr, null, newChunk)) chunk = newChunk;
                    else chunk = (SectionRef[]) U.getReferenceVolatile(chunks, addr);
                }
            }
            chunk[o] = v;
        }

        void drainAndRecycle(TLPool<byte[]> pp) {
            int sz = size;
            if (sz > capacity) sz = capacity;
            for (int i = 0; i < sz; i++) {
                int c = i >>> CHUNK_SHIFT;
                int o = i & CHUNK_MASK;

                SectionRef[] chunk = chunks[c];
                if (chunk == null) continue;

                SectionRef sc = chunk[o];
                if (sc != null) {
                    pp.recycle(sc.pocketData);
                    chunk[o] = null;
                }
            }
            size = 0;
        }
    }
}
