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
import com.hbm.lib.queues.MpscUnboundedXaddArrayLongQueue;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.saveddata.AuxSavedData;
import com.hbm.util.DecodeException;
import com.hbm.util.ObjectPool;
import com.hbm.util.SectionKeyHash;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BitArray;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.*;

import static com.hbm.lib.internal.UnsafeHolder.U;
import static com.hbm.lib.internal.UnsafeHolder.fieldOffset;

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
    static final int[] BOUNDARY_MASKS = {0, 0, 0xF00, 0xF00, 0xFF0, 0xFF0}, LINEAR_OFFSETS = {-256, 256, -16, 16, -1, 1};
    static final int PROFILE_WINDOW = 200;
    static final String TAG_RAD = "hbmRadDataNT";
    static final byte MAGIC_0 = (byte) 'N', MAGIC_1 = (byte) 'T', MAGIC_2 = (byte) 'X', FMT = 6;
    static final Object NOT_RES = new Object();
    static final ForkJoinPool RAD_POOL = ForkJoinPool.commonPool(); // safe: we don't lock in sim path
    static final int TARGET_TASK_CNT = RAD_POOL.getParallelism() << 2;
    static final ThreadLocal<int[]> TL_FF_QUEUE = ThreadLocal.withInitial(() -> new int[SECTION_BLOCK_COUNT]);
    static final ThreadLocal<PalScratch> TL_PAL_SCRATCH = ThreadLocal.withInitial(PalScratch::new);
    static final ThreadLocal<int[]> TL_VOL_COUNTS = ThreadLocal.withInitial(() -> new int[NO_POCKET]);
    static final ThreadLocal<double[]> TL_NEW_MASS = ThreadLocal.withInitial(() -> new double[NO_POCKET]);
    static final ThreadLocal<double[]> TL_OLD_MASS = ThreadLocal.withInitial(() -> new double[NO_POCKET]);
    static final ThreadLocal<int[]> TL_OVERLAPS = ThreadLocal.withInitial(() -> new int[NO_POCKET * NO_POCKET]);
    static final ThreadLocal<long[]> TL_SUM_X = ThreadLocal.withInitial(() -> new long[NO_POCKET]);
    static final ThreadLocal<long[]> TL_SUM_Y = ThreadLocal.withInitial(() -> new long[NO_POCKET]);
    static final ThreadLocal<long[]> TL_SUM_Z = ThreadLocal.withInitial(() -> new long[NO_POCKET]);
    static final ThreadLocal<double[]> TL_DENSITIES = ThreadLocal.withInitial(() -> new double[NO_POCKET]);

    // Scratch for applyQueuedWrites
    static final ThreadLocal<double[]> TL_ADD = ThreadLocal.withInitial(() -> new double[NO_POCKET + 1]);
    static final ThreadLocal<double[]> TL_SET = ThreadLocal.withInitial(() -> new double[NO_POCKET + 1]);
    static final ThreadLocal<boolean[]> TL_HAS_SET = ThreadLocal.withInitial(() -> new boolean[NO_POCKET + 1]);
    static final ThreadLocal<long[]> TL_BEST_SET_SEQ = ThreadLocal.withInitial(() -> new long[NO_POCKET + 1]);

    static final double RAD_EPSILON = 1.0e-5D;
    static final double RAD_MAX = Double.MAX_VALUE / 2.0D;

    static final int MAX_BYTES = Short.BYTES + 16 * (NO_POCKET + 1) * (Byte.BYTES + Double.BYTES); // 2306
    static final ByteBuffer BUF = ByteBuffer.allocateDirect(MAX_BYTES);
    static final double[] TEMP_DENSITIES = new double[NO_POCKET];
    static final long DESTROY_PROB_U64 = Long.divideUnsigned(-1L, 100L);
    static long fogProbU64;
    static long ticks;
    static @NotNull CompletableFuture<Void> radiationFuture = CompletableFuture.completedFuture(null);
    static Object[] STATE_CLASS;
    static int tickDelay = 1;
    static double dT = tickDelay / 20.0D;
    static double diffusionDt = 10.0 * dT;
    static double UU_E = Math.exp(-(diffusionDt / 128.0d));
    static double retentionDt = Math.pow(0.99424, dT); // 2min

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

    static int getTaskThreshold(int size, int minGrain) {
        int th = size / TARGET_TASK_CNT;
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

    static long probU64(double p) {
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
        data.clearAllChunkRefs();
        data.dirtyCk.clearAll();
        data.pocketToDestroy = Long.MIN_VALUE;
        data.destructionQueue.clear(true);
        data.clearQueuedWrites();
        for (Chunk chunk : world.getChunkProvider().loadedChunks.values()) {
            if (!chunk.loaded) continue;
            int cx = chunk.x, cz = chunk.z;
            if (((cx ^ (cx << 10) >> 10) | (cz ^ (cz << 10) >> 10)) != 0) continue;
            ChunkRef cr = data.onChunkLoaded(cx, cz, chunk);
            data.dirtyCk.add(cr);
        }
    }

    @ServerThread
    public static void incrementRad(WorldServer world, BlockPos pos, double amount, double max) {
        if (Math.abs(amount) < RAD_EPSILON || isOutsideWorld(pos)) return;
        long posLong = pos.toLong();
        long sck = Library.blockPosToSectionLong(posLong);
        long ck = Library.sectionToChunkLong(sck);
        Chunk chunk = world.getChunkProvider().loadedChunks.get(ck);
        if (chunk == null) return;
        if (isResistantAt(world, chunk, pos)) return;
        WorldRadiationData data = getWorldRadData(world);
        ChunkRef owner = data.onChunkLoaded(chunk.x, chunk.z, chunk);
        int sy = Library.getSectionY(sck);

        int kind = owner.getKind(sy);
        // unreachable if the section is truly resistant(filtered by isResistantAt)
        if (kind == ChunkRef.KIND_NONE || data.dirtyCk.isDirty(owner, sy)) {
            int local = Library.blockPosToLocal(posLong);
            data.queueAdd(sck, local, amount);
            if (kind == ChunkRef.KIND_NONE) data.dirtyCk.add(owner, sy);
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
            if (next != 0.0D) owner.setActiveBit(sy, pocketIndex);
            chunk.markDirty();
        }
    }

    @ServerThread
    public static void decrementRad(WorldServer world, BlockPos pos, double amount) {
        if (Math.abs(amount) < RAD_EPSILON || isOutsideWorld(pos)) return;
        long posLong = pos.toLong();
        long sck = Library.blockPosToSectionLong(posLong);
        long ck = Library.sectionToChunkLong(sck);
        Chunk chunk = world.getChunkProvider().loadedChunks.get(ck);
        if (chunk == null) return;
        if (isResistantAt(world, chunk, pos)) return;
        WorldRadiationData data = getWorldRadData(world);
        ChunkRef owner = data.onChunkLoaded(chunk.x, chunk.z, chunk);
        int sy = Library.getSectionY(sck);
        int kind = owner.getKind(sy);
        if (kind == ChunkRef.KIND_NONE || data.dirtyCk.isDirty(owner, sy)) {
            int local = Library.blockPosToLocal(posLong);
            data.queueAdd(sck, local, -amount);
            if (kind == ChunkRef.KIND_NONE) data.dirtyCk.add(owner, sy);
            chunk.markDirty();
            return;
        }
        int pocketIndex;
        SectionRef ref = owner.sec[sy];
        if (kind == ChunkRef.KIND_UNI) pocketIndex = 0;
        else pocketIndex = ref.getPocketIndex(posLong);
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
        if (kind == ChunkRef.KIND_UNI) owner.uniformRads[sy] = next;
        else if (kind == ChunkRef.KIND_SINGLE) ((SingleMaskedSectionRef) ref).rad = next;
        else ((MultiSectionRef) ref).data[pocketIndex << 1] = next;
        if (next != 0.0D) owner.setActiveBit(sy, pocketIndex);
        else owner.clearActiveBit(sy, pocketIndex);
        chunk.markDirty();
    }

    /**
     * @param amount clamped to [-backGround, Double.MAX_VALUE / 2]
     */
    @ServerThread
    public static void setRadForCoord(WorldServer world, BlockPos pos, double amount) {
        if (isOutsideWorld(pos)) return;
        long posLong = pos.toLong();
        long sck = Library.blockPosToSectionLong(posLong);
        long ck = Library.sectionToChunkLong(sck);
        Chunk chunk = world.getChunkProvider().loadedChunks.get(ck);
        if (chunk == null) return;
        if (isResistantAt(world, chunk, pos)) return;
        WorldRadiationData data = getWorldRadData(world);
        ChunkRef owner = data.onChunkLoaded(chunk.x, chunk.z, chunk);
        int sy = Library.getSectionY(sck);
        int kind = owner.getKind(sy);
        if (kind == ChunkRef.KIND_NONE || data.dirtyCk.isDirty(owner, sy)) {
            int local = Library.blockPosToLocal(posLong);
            data.queueSet(sck, local, amount);
            if (kind == ChunkRef.KIND_NONE) data.dirtyCk.add(owner, sy);
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
        if (kind == ChunkRef.KIND_UNI) owner.uniformRads[sy] = v;
        else if (kind == ChunkRef.KIND_SINGLE) ((SingleMaskedSectionRef) owner.sec[sy]).rad = v;
        else ((MultiSectionRef) owner.sec[sy]).data[pocketIndex << 1] = v;
        if (v != 0.0D) owner.setActiveBit(sy, pocketIndex);
        else owner.clearActiveBit(sy, pocketIndex);
        chunk.markDirty();
    }

    @ServerThread
    public static double getRadForCoord(WorldServer world, BlockPos pos) {
        if (isOutsideWorld(pos)) return 0D;
        long posLong = pos.toLong();
        long sck = Library.blockPosToSectionLong(posLong);
        long ck = Library.sectionToChunkLong(sck);
        Chunk chunk = world.getChunkProvider().loadedChunks.get(ck);
        if (chunk == null) return 0D;
        WorldRadiationData data = worldMap.get(world);
        if (data == null) return 0D;
        if (isResistantAt(world, chunk, pos)) return 0D;
        int sy = Library.getSectionY(sck);
        ChunkRef owner = data.chunkRefs.get(ck);
        if (owner == null) return 0D;
        SectionRef sc = owner.sec[sy];
        int kind = owner.getKind(sy);
        if (kind == ChunkRef.KIND_NONE) {
            data.dirtyCk.add(owner, sy);
            return 0D;
        }
        if (kind == ChunkRef.KIND_UNI) {
            return owner.uniformRads[sy];
        }
        // Kind is SINGLE or MULTI, sc should be valid
        if (sc == null || sc.pocketCount <= 0) {
            data.dirtyCk.add(owner, sy);
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
        if (isOutsideWorld(pos)) return;
        markSectionForRebuild(world, Library.blockPosToSectionLong(pos));
    }

    @ServerThread
    public static void markSectionForRebuild(World world, long sck) {
        if (world.isRemote || !GeneralConfig.advancedRadiation) return;
        WorldServer ws = (WorldServer) world;
        long ck = Library.sectionToChunkLong(sck);
        Chunk chunk = ws.getChunkProvider().loadedChunks.get(ck);
        if (chunk == null) return;
        WorldRadiationData data = getWorldRadData(ws);
        int sy = Library.getSectionY(sck);
        if ((sy & ~15) != 0) return;
        ChunkRef cr = data.onChunkLoaded(chunk.x, chunk.z, chunk);
        data.dirtyCk.add(cr, sy);
        chunk.markDirty();
    }

    @ServerThread
    public static void markSectionsForRebuild(World world, LongIterable sections) {
        if (world.isRemote || !GeneralConfig.advancedRadiation) return;
        WorldServer ws = (WorldServer) world;
        WorldRadiationData data = getWorldRadData(ws);
        LongIterator it = sections.iterator();
        while (it.hasNext()) {
            long sck = it.nextLong();
            long ck = Library.sectionToChunkLong(sck);
            Chunk chunk = ws.getChunkProvider().loadedChunks.get(ck);
            if (chunk == null) continue;

            int sy = Library.getSectionY(sck);
            if ((sy & ~15) != 0) continue;
            ChunkRef cr = data.onChunkLoaded(chunk.x, chunk.z, chunk);
            data.dirtyCk.add(cr, sy);
            chunk.markDirty();
        }
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
        Chunk mcChunk = cr.mcChunk;
        if (mcChunk == null) return; //chunk unloaded
        int kind = cr.getKind(cy);
        if (kind == ChunkRef.KIND_NONE) return;

        int baseX = cx << 4;
        int baseY = cy << 4;
        int baseZ = cz << 4;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
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

    static void runParallelSimulation() {
        WorldRadiationData[] all = worldMap.values().toArray(new WorldRadiationData[0]);
        int n = all.length;
        if (n == 0) return;

        if (n == 1) {
            WorldRadiationData data = all[0];
            if (data.world.getMinecraftServer() == null) return;
            try {
                data.processWorldSimulation();
            } catch (Throwable t) {
                var p = data.world.provider;
                MainRegistry.logger.error("Error in async rad simulation in dimension {} ({})", String.valueOf(p.getDimension()), p.getDimensionType().getName(), t);
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
                        var p = data.world.provider;
                        MainRegistry.logger.error("Error in async rad simulation in dimension {} ({})", String.valueOf(p.getDimension()), p.getDimensionType().getName(), t);
                    }
                });
            }
            ForkJoinTask.invokeAll(tasks);
        }
    }

    @SuppressWarnings("AutoBoxing")
    static void logLifetimeProfiling(@Nullable WorldRadiationData data) {
        if (!GeneralConfig.enableDebugMode || data == null) return;
        long steps = data.profSteps;
        if (steps <= 0) return;
        int dimId = data.world.provider.getDimension();
        String dimType = data.world.provider.getDimensionType().getName();
        double avgMs = data.profTotalMs / (double) steps;
        double maxMs = data.profMaxMs;
        DoubleArrayList samples = data.profSamplesMs;
        int n = (samples == null) ? 0 : samples.size();
        if (n == 0) {
            MainRegistry.logger.info("[RadiationSystemNT] dim {} ({}) lifetime: steps={}, avg={} ms, max={} ms", dimId, dimType, steps, r3(avgMs), r3(maxMs));
            return;
        }
        double[] a = Arrays.copyOf(samples.elements(), n);
        DoubleArrays.radixSort(a);
        int k1 = Math.max(1, (int) Math.ceil(n * 0.01));
        int k01 = Math.max(1, (int) Math.ceil(n * 0.001));
        double onePctHighAvg = meanOfLargestK(a, k1);
        double pointOnePctHigh = meanOfLargestK(a, k01);
        double p99 = a[Math.min(n - 1, (int) Math.ceil(n * 0.99) - 1)];
        double p999 = a[Math.min(n - 1, (int) Math.ceil(n * 0.999) - 1)];
        MainRegistry.logger.info("[RadiationSystemNT] dim {} ({}) lifetime: steps={}, avg={} ms, 1% high(avg)={} ms, 0.1% high(avg)={} ms, p99={} ms, p999={} ms, max={} ms (sampleN={})", dimId, dimType, steps, r3(avgMs), r3(onePctHighAvg), r3(pointOnePctHigh), r3(p99), r3(p999), r3(maxMs), n);
        data.profSamplesMs = null;
    }

    static double meanOfLargestK(double[] sortedAscending, int k) {
        int n = sortedAscending.length;
        int start = n - k;
        double sum = 0.0;
        for (int i = start; i < n; i++) sum += sortedAscending[i];
        return sum / (double) k;
    }

    static double r3(double v) {
        return Math.rint(v * 1000.0) / 1000.0;
    }

    @SubscribeEvent
    public static void onChunkDataLoad(ChunkDataEvent.Load e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;
        Chunk chunk = e.getChunk();
        int cx = chunk.x, cz = chunk.z;
        if (((cx ^ (cx << 10) >> 10) | (cz ^ (cz << 10) >> 10)) != 0) return;
        WorldRadiationData data = getWorldRadData((WorldServer) e.getWorld());
        NBTTagCompound nbt = e.getData();
        try {
            byte[] payload = null;
            int id = nbt.getTagId(TAG_RAD);
            if (id == Constants.NBT.TAG_COMPOUND) {
                var p = e.getWorld().provider;
                MainRegistry.logger.warn("[RadiationSystemNT] Skipped legacy radiation data for chunk {} in dimension {} ({})", chunk.getPos(), String.valueOf(p.getDimension()), p.getDimensionType().getName());
            } else if (id == Constants.NBT.TAG_BYTE_ARRAY) {
                byte[] raw = nbt.getByteArray(TAG_RAD);
                payload = verifyPayload(raw);
            }
            if (payload == null || payload.length == 0) return;
            data.readPayload(cx, cz, payload);
        } catch (BufferUnderflowException | DecodeException ex) {
            var p = e.getWorld().provider;
            MainRegistry.logger.error("[RadiationSystemNT] Failed to decode data for chunk {} in dimension {} ({})", chunk.getPos(), String.valueOf(p.getDimension()), p.getDimensionType().getName(), ex);
            nbt.removeTag(TAG_RAD);
        }
    }

    // ChunkEvent.Load is posted after ChunkDataEvent.Load
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;
        Chunk chunk = e.getChunk();
        int cx = chunk.x, cz = chunk.z;
        if (((cx ^ (cx << 10) >> 10) | (cz ^ (cz << 10) >> 10)) != 0) return;
        WorldRadiationData data = getWorldRadData((WorldServer) e.getWorld());
        ChunkRef cr = data.onChunkLoaded(cx, cz, chunk);
        data.dirtyCk.add(cr);
    }

    // order here:
    // A) command save or automatic periodic save:
    //      1. MinecraftServer saveAllWorlds called
    //          -> call WorldServer.saveAllChunks(true) on all loaded worlds
    //              Note that all call sites pass the same boolean "true"
    //          -> delegate to ChunkProviderServer.saveChunks(true)
    //      2. Filter chunks that need to be saved for saving with Chunk.needsSaving(true)
    //          This is effectively hasEntities && world.getTotalWorldTime() != lastSaveTime || dirty
    //          Note that this is the only call site of Chunk.needsSaving, and read site of Chunk#dirty
    //      3. Call saveChunkData. Sets Chunk#lastSaveTime to world.getTotalWorldTime(), then delegate to
    //          AnvilChunkLoader.saveChunk:
    //              i) Minecraft writes data to NBT
    //              ii) Update Dormant Chunk Cache (if enabled)
    //              iii) POST ChunkDataEvent.Save
    //              iv) queue <ChunkPos, NBTTagCompound> for async file I/O
    // B) unload(see ChunkProviderServer#tick):
    //      1. Filter. Unload happens AND only happens to chunks that are
    //          - in ChunkProviderServer#droppedChunks (up to 100 per tick), AND
    //          - exists in ChunkProviderServer#loadedChunks, AND
    //          - have Chunk#unloadQueued == true
    //      2. Chunk.onUnload() called
    //          i) Chunk#loaded = false
    //          ii) mark tileEntities for removal by adding to World#tileEntitiesToBeRemoved
    //          iii) mark Entities removed by adding to World#unloadedEntityList
    //          iv) POST ChunkEvent.Unload
    //      3. ForgeChunkManager.putDormantChunk() called
    //          the Chunk object MAY be put into dormantChunk if it is enabled
    //      4. chunkLoader.saveChunkData() called -> delegated to AnvilChunkLoader.saveChunk(), as shown above
    //      5. call loadedChunks.remove(ck). After this, the chunk must be either dormant or discarded for GC
    // It is also worth noting that a chunk MAY get loaded and then unloaded within the same tick
    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;
        Chunk chunk = e.getChunk();
        int cx = chunk.x, cz = chunk.z;
        if (((cx ^ (cx << 10) >> 10) | (cz ^ (cz << 10) >> 10)) != 0) return;
        WorldRadiationData data = getWorldRadData((WorldServer) e.getWorld());
        data.unloadChunk(cx, cz);
    }

    @SubscribeEvent
    public static void onChunkDataSave(ChunkDataEvent.Save e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;
        WorldRadiationData data = getWorldRadData((WorldServer) e.getWorld());
        Chunk chunk = e.getChunk();
        int cx = chunk.x, cz = chunk.z;
        if (((cx ^ (cx << 10) >> 10) | (cz ^ (cz << 10) >> 10)) != 0) return;
        long ck = ChunkPos.asLong(cx, cz);
        byte[] payload = data.tryEncodePayload(ck, chunk);
        if (payload != null && payload.length > 0) {
            e.getData().setByteArray(TAG_RAD, payload);
        } else if (e.getData().hasKey(TAG_RAD)) {
            e.getData().removeTag(TAG_RAD);
        }
        if (!chunk.loaded) data.removeChunkRef(ck);
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

    static byte[] verifyPayload(byte[] raw) throws DecodeException {
        if (raw.length == 0) return null;
        if (raw.length < 6) throw new DecodeException("Payload too short: " + raw.length);
        if (raw[0] != MAGIC_0 || raw[1] != MAGIC_1 || raw[2] != MAGIC_2) throw new DecodeException("Invalid magic");
        byte fmt = raw[3];
        if (fmt != FMT) throw new DecodeException("Unknown format: " + fmt);
        return raw;
    }

    @NotNull
    static WorldRadiationData getWorldRadData(WorldServer world) {
        return worldMap.computeIfAbsent(world, WorldRadiationData::new);
    }

    static boolean isResistantAt(WorldServer w, Chunk chunk, BlockPos pos) {
        Block b = chunk.getBlockState(pos).getBlock();
        return (b instanceof IRadResistantBlock r) && r.isRadResistant(w, pos);
    }

    static boolean isOutsideWorld(BlockPos pos) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        int bad = ((x << 6) >> 6) ^ x;
        bad |= ((z << 6) >> 6) ^ z;
        bad |= y & ~255;
        return bad != 0;
    }

    static long pocketKey(long sectionKey, int pocketIndex) {
        int sy = Library.getSectionY(sectionKey);
        // x22 | z22 | y20
        return Library.setSectionY(sectionKey, (sy << 4) | (pocketIndex & 15));
    }

    static void writeNibble(byte[] pocketData, int blockIndex, int paletteIndex) {
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
    static SectionMask scanResistantMask(WorldServer world, long sectionKey, @Nullable ExtendedBlockStorage ebs) {
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
    static Object[] ensureStateClassCapacity(int minSize) {
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

    //@formatter:off
    static void sweepX(ChunkRef[][] b, int c0, int c1, int c2, int c3, int th0, int th1, int th2, int th3, boolean flip) {
        var t0 = new DiffuseXTask(b[0], 0, c0, th0);
        var t1 = new DiffuseXTask(b[1], 0, c1, th1);
        var t2 = new DiffuseXTask(b[2], 0, c2, th2);
        var t3 = new DiffuseXTask(b[3], 0, c3, th3);
        if (flip) { ForkJoinTask.invokeAll(t1, t3); ForkJoinTask.invokeAll(t0, t2);}
        else { ForkJoinTask.invokeAll(t0, t2); ForkJoinTask.invokeAll(t1, t3); }
    }

    static void sweepZ(ChunkRef[][] b, int c0, int c1, int c2, int c3, int th0, int th1, int th2, int th3, boolean flip) {
        var t0 = new DiffuseZTask(b[0], 0, c0, th0);
        var t1 = new DiffuseZTask(b[1], 0, c1, th1);
        var t2 = new DiffuseZTask(b[2], 0, c2, th2);
        var t3 = new DiffuseZTask(b[3], 0, c3, th3);
        if (flip) { ForkJoinTask.invokeAll(t2, t3); ForkJoinTask.invokeAll(t0, t1);}
        else { ForkJoinTask.invokeAll(t0, t1); ForkJoinTask.invokeAll(t2, t3); }
    }

    static void sweepY(ChunkRef[][] b, int c0, int c1, int c2, int c3, int th0, int th1, int th2, int th3, int startParity) {
        for (int p = 0; p < 2; p++) {
            int parity = startParity ^ p;
            var t0 = new DiffuseYTask(b[0], 0, c0, parity, th0).fork();
            var t1 = new DiffuseYTask(b[1], 0, c1, parity, th1).fork();
            var t2 = new DiffuseYTask(b[2], 0, c2, parity, th2).fork();
            new DiffuseYTask(b[3], 0, c3, parity, th3).invoke();
            t0.join(); t1.join(); t2.join();
        }
    }//@formatter:on

    static boolean exchangeFaceExact(ChunkRef crA, int syA, int kA, int faceA, ChunkRef crB, int syB, int kB, int faceB) {
        if (kA == ChunkRef.KIND_NONE || kB == ChunkRef.KIND_NONE) return false;
        if (kA == ChunkRef.KIND_UNI) {
            return crB.sec[syB].exchangeWithUniform(crA, faceB, faceA, syA);
        } else if (kB == ChunkRef.KIND_UNI) {
            return crA.sec[syA].exchangeWithUniform(crB, faceA, faceB, syB);
        } else {
            if (kB == ChunkRef.KIND_SINGLE)
                return crA.sec[syA].exchangeWithSingle((SingleMaskedSectionRef) crB.sec[syB], faceA, faceB);
            else return crA.sec[syA].exchangeWithMulti((MultiSectionRef) crB.sec[syB], faceA, faceB);
        }
    }

    static boolean exchangeFaceExactY(ChunkRef cr, int syA, int kA, int syB, int kB) {
        if (kA == ChunkRef.KIND_NONE || kB == ChunkRef.KIND_NONE) return false;
        if (kA == ChunkRef.KIND_UNI) {
            SectionRef b = cr.sec[syB];
            assert b != null;
            return b.exchangeWithUniform(cr, 0, 1, syA);
        }
        if (kB == ChunkRef.KIND_UNI) {
            SectionRef a = cr.sec[syA];
            assert a != null;
            return a.exchangeWithUniform(cr, 1, 0, syB);
        }
        SectionRef a = cr.sec[syA];
        SectionRef b = cr.sec[syB];
        assert a != null;
        assert b != null;
        if (kB == ChunkRef.KIND_SINGLE) return a.exchangeWithSingle((SingleMaskedSectionRef) b, 1, 0);
        return a.exchangeWithMulti((MultiSectionRef) b, 1, 0);
    }

    static final class ChunkRef {
        static final int KIND_NONE = 0;
        static final int KIND_UNI = 1;
        static final int KIND_SINGLE = 2;
        static final int KIND_MULTI = 3;

        final long ck;
        final SectionRef[] sec = new SectionRef[16];
        final double[] uniformRads = new double[16];
        Chunk mcChunk; // non-null iff loaded for simulation (and iff parityIndex >= 0)
        @Nullable ChunkRef north, south, west, east;
        @Nullable PendingRad pending;
        int sectionKinds; // 2 bits per section
        // it is profiled that direct branchy field access is FASTER than four longs with U.getChar/putChar, LMAO HOW
        // long mask0, mask1, mask2, mask3;
        char c0, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15;
        byte parityBucket = -1;
        int parityIndex = -1;
        boolean dirtyFlag;

        ChunkRef(long ck) {
            this.ck = ck;
        }

        int getKind(int sy) {
            return (sectionKinds >>> (sy << 1)) & 3;
        }

        void setKind(int sy, int kind) {
            int shift = sy << 1;
            int clear = ~(3 << shift);
            int set = (kind & 3) << shift;
            sectionKinds = (sectionKinds & clear) | set;
        }

        int lane16(int sy) {
            return switch (sy) {//@formatter:off
                case 0 -> c0; case 1 -> c1; case 2 -> c2; case 3 -> c3;
                case 4 -> c4; case 5 -> c5; case 6 -> c6; case 7 -> c7;
                case 8 -> c8; case 9 -> c9; case 10 -> c10; case 11 -> c11;
                case 12 -> c12; case 13 -> c13; case 14 -> c14; default -> c15;
            };
        }

        void setLane16(int sy, char v) {
            switch (sy) {
                case 0 -> c0 = v; case 1 -> c1 = v; case 2 -> c2 = v; case 3 -> c3 = v;
                case 4 -> c4 = v; case 5 -> c5 = v; case 6 -> c6 = v; case 7 -> c7 = v;
                case 8 -> c8 = v; case 9 -> c9 = v; case 10 -> c10 = v; case 11 -> c11 = v;
                case 12 -> c12 = v; case 13 -> c13 = v; case 14 -> c14 = v; default -> c15 = v;
            }//@formatter:on
        }

        boolean isInactive(int sy, int pi) {
            return (lane16(sy) & (1 << pi)) == 0;
        }

        void setActive0(int sy) {
            int cur = lane16(sy);
            if ((cur & 1) != 0) return;
            setLane16(sy, (char) (cur | 1));
        }

        void clearActive0(int sy) {
            int cur = lane16(sy);
            if ((cur & 1) == 0) return;
            setLane16(sy, (char) (cur & ~1));
        }

        void setActiveBit(int sy, int pi) {
            int cur = lane16(sy);
            int next = cur | (1 << pi);
            if (cur == next) return;
            setLane16(sy, (char) next);
        }

        void clearActiveBit(int sy, int pi) {
            int cur = lane16(sy);
            int next = cur & ~(1 << pi);
            if (cur == next) return;
            setLane16(sy, (char) next);
        }

        void clearAllMasks() {
            c0 = c1 = c2 = c3 = c4 = c5 = c6 = c7 = c8 = c9 = c10 = c11 = c12 = c13 = c14 = c15 = 0;
        }
    }

    static final class PendingRad {
        final char[] secMask = new char[16];
        final long[] bits = new long[256];
        int nonEmptySyMask16;

        boolean hasSy(int sy) {
            return (nonEmptySyMask16 & (1 << sy)) != 0;
        }

        boolean isEmpty() {
            return nonEmptySyMask16 == 0;
        }

        void clearAll() {
            Arrays.fill(secMask, (char) 0);
            nonEmptySyMask16 = 0;
        }

        void put(int sy, int pi, long vBits) {
            int idx = (sy << 4) | (pi & 15);
            bits[idx] = vBits;

            int bit = 1 << (pi & 15);
            char m = secMask[sy];
            if (m == 0) nonEmptySyMask16 |= (1 << sy);
            secMask[sy] = (char) (m | bit);
        }

        long getBits(int sy, int pi) {
            int bit = 1 << (pi & 15);
            if ((secMask[sy] & bit) == 0) return 0L;
            return bits[(sy << 4) | (pi & 15)];
        }

        long takeBits(int sy, int pi) {
            int bit = 1 << (pi & 15);
            char m = secMask[sy];
            if ((m & bit) == 0) return 0L;

            int idx = (sy << 4) | (pi & 15);
            long v = bits[idx];

            m = (char) (m & ~bit);
            secMask[sy] = m;
            if (m == 0) nonEmptySyMask16 &= ~(1 << sy);

            return v;
        }

        // Clear pi >= keepCount for a section in one shot.
        void clearAbove(int sy, int keepCount) {
            if (keepCount >= 16) return;
            int keepMask = (keepCount <= 0) ? 0 : ((1 << keepCount) - 1);
            int newMask = (secMask[sy] & 0xFFFF) & keepMask;
            secMask[sy] = (char) newMask;
            if (newMask == 0) nonEmptySyMask16 &= ~(1 << sy);
        }
    }

    static final class PalScratch {
        final Object[] cls = new Object[256]; // localId -> (IRadResistantBlock instance) or NOT_RES
        final int[] stamp = new int[256];     // localId -> generation
        int gen = 1;

        int nextGen() {
            int g = gen + 1;
            gen = g == 0 ? 1 : g;
            return gen;
        }
    }

    static final class SectionMask {
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

    static final class DiffuseXTask extends RecursiveAction {
        final ChunkRef[] chunks;
        final int lo, hi, threshold;

        DiffuseXTask(ChunkRef[] chunks, int lo, int hi, int threshold) {
            this.chunks = chunks;
            this.lo = lo;
            this.hi = hi;
            this.threshold = threshold;
        }

        @Override
        protected void compute() {
            int n = hi - lo;
            if (n <= threshold) {
                for (int i = lo; i < hi; i++) {
                    ChunkRef aCr = chunks[i];
                    ChunkRef bCr = aCr.east;
                    if (bCr != null) diffuseXZ(aCr, bCr, /*E*/ 5, /*W*/ 4);
                }
                return;
            }
            int mid = (lo + hi) >>> 1;
            var left = new DiffuseXTask(chunks, lo, mid, threshold).fork();
            new DiffuseXTask(chunks, mid, hi, threshold).compute();
            left.join();
        }

    }

    static final class DiffuseZTask extends RecursiveAction {
        final ChunkRef[] chunks;
        final int lo, hi, threshold;

        DiffuseZTask(ChunkRef[] chunks, int lo, int hi, int threshold) {
            this.chunks = chunks;
            this.lo = lo;
            this.hi = hi;
            this.threshold = threshold;
        }

        @Override
        protected void compute() {
            int n = hi - lo;
            if (n <= threshold) {
                for (int i = lo; i < hi; i++) {
                    ChunkRef aCr = chunks[i];
                    ChunkRef bCr = aCr.south;
                    if (bCr != null) diffuseXZ(aCr, bCr, /*S*/ 3, /*N*/ 2);
                }
                return;
            }
            int mid = (lo + hi) >>> 1;
            var left = new DiffuseZTask(chunks, lo, mid, threshold).fork();
            new DiffuseZTask(chunks, mid, hi, threshold).compute();
            left.join();
        }
    }

    static boolean exchangeUniExactXZ(double[] uniA, double[] uniB, int i) {
        double ra = uniA[i], rb = uniB[i];
        if (ra == rb) return false;
        double avg = 0.5d * (ra + rb);
        double delta = 0.5d * (ra - rb) * UU_E;
        uniA[i] = avg + delta;
        uniB[i] = avg - delta;
        return true;
    }

    static void diffuseXZ(ChunkRef aCr, ChunkRef bCr, int faceA, int faceB) {
        int ksA = aCr.sectionKinds, ksB = bCr.sectionKinds;
        double[] uA = aCr.uniformRads, uB = bCr.uniformRads;
        boolean d = false;

        if ((aCr.c0 | bCr.c0) != 0) {
            int kA = ksA & 3;
            int kB = ksB & 3;
            if (kA == ChunkRef.KIND_UNI && kB == ChunkRef.KIND_UNI) {
                if (exchangeUniExactXZ(uA, uB, 0)) {
                    // na == 0.0D || nb == 0.0D should be extremely rare (but not impossible); even if it happens, postSweep will clean it up.
                    // same rationale for SectionRef exchangeWith* methods
                    aCr.c0 = bCr.c0 = 1;
                    d = true;
                }
            } else {
                d |= exchangeFaceExact(aCr, 0, kA, faceA, bCr, 0, kB, faceB);
            }
        }
        // I wish I had macros in java...
        // <editor-fold defaultstate="collapsed" desc="sy 1~15 boilerplate to have branchless access to c0~15, same as above.">
        if ((aCr.c1 | bCr.c1) != 0) {
            int kA = (ksA >>> 2) & 3;
            int kB = (ksB >>> 2) & 3;
            if (kA == ChunkRef.KIND_UNI && kB == ChunkRef.KIND_UNI) {
                if (exchangeUniExactXZ(uA, uB, 1)) {
                    aCr.c1 = bCr.c1 = 1;
                    d = true;
                }
            } else {
                d |= exchangeFaceExact(aCr, 1, kA, faceA, bCr, 1, kB, faceB);
            }
        }

        if ((aCr.c2 | bCr.c2) != 0) {
            int kA = (ksA >>> 4) & 3;
            int kB = (ksB >>> 4) & 3;
            if (kA == ChunkRef.KIND_UNI && kB == ChunkRef.KIND_UNI) {
                if (exchangeUniExactXZ(uA, uB, 2)) {
                    aCr.c2 = bCr.c2 = 1;
                    d = true;
                }
            } else {
                d |= exchangeFaceExact(aCr, 2, kA, faceA, bCr, 2, kB, faceB);
            }
        }

        if ((aCr.c3 | bCr.c3) != 0) {
            int kA = (ksA >>> 6) & 3;
            int kB = (ksB >>> 6) & 3;
            if (kA == ChunkRef.KIND_UNI && kB == ChunkRef.KIND_UNI) {
                if (exchangeUniExactXZ(uA, uB, 3)) {
                    aCr.c3 = bCr.c3 = 1;
                    d = true;
                }
            } else {
                d |= exchangeFaceExact(aCr, 3, kA, faceA, bCr, 3, kB, faceB);
            }
        }

        if ((aCr.c4 | bCr.c4) != 0) {
            int kA = (ksA >>> 8) & 3;
            int kB = (ksB >>> 8) & 3;
            if (kA == ChunkRef.KIND_UNI && kB == ChunkRef.KIND_UNI) {
                if (exchangeUniExactXZ(uA, uB, 4)) {
                    aCr.c4 = bCr.c4 = 1;
                    d = true;
                }
            } else {
                d |= exchangeFaceExact(aCr, 4, kA, faceA, bCr, 4, kB, faceB);
            }
        }

        if ((aCr.c5 | bCr.c5) != 0) {
            int kA = (ksA >>> 10) & 3;
            int kB = (ksB >>> 10) & 3;
            if (kA == ChunkRef.KIND_UNI && kB == ChunkRef.KIND_UNI) {
                if (exchangeUniExactXZ(uA, uB, 5)) {
                    aCr.c5 = bCr.c5 = 1;
                    d = true;
                }
            } else {
                d |= exchangeFaceExact(aCr, 5, kA, faceA, bCr, 5, kB, faceB);
            }
        }

        if ((aCr.c6 | bCr.c6) != 0) {
            int kA = (ksA >>> 12) & 3;
            int kB = (ksB >>> 12) & 3;
            if (kA == ChunkRef.KIND_UNI && kB == ChunkRef.KIND_UNI) {
                if (exchangeUniExactXZ(uA, uB, 6)) {
                    aCr.c6 = bCr.c6 = 1;
                    d = true;
                }
            } else {
                d |= exchangeFaceExact(aCr, 6, kA, faceA, bCr, 6, kB, faceB);
            }
        }

        if ((aCr.c7 | bCr.c7) != 0) {
            int kA = (ksA >>> 14) & 3;
            int kB = (ksB >>> 14) & 3;
            if (kA == ChunkRef.KIND_UNI && kB == ChunkRef.KIND_UNI) {
                if (exchangeUniExactXZ(uA, uB, 7)) {
                    aCr.c7 = bCr.c7 = 1;
                    d = true;
                }
            } else {
                d |= exchangeFaceExact(aCr, 7, kA, faceA, bCr, 7, kB, faceB);
            }
        }

        if ((aCr.c8 | bCr.c8) != 0) {
            int kA = (ksA >>> 16) & 3;
            int kB = (ksB >>> 16) & 3;
            if (kA == ChunkRef.KIND_UNI && kB == ChunkRef.KIND_UNI) {
                if (exchangeUniExactXZ(uA, uB, 8)) {
                    aCr.c8 = bCr.c8 = 1;
                    d = true;
                }
            } else {
                d |= exchangeFaceExact(aCr, 8, kA, faceA, bCr, 8, kB, faceB);
            }
        }

        if ((aCr.c9 | bCr.c9) != 0) {
            int kA = (ksA >>> 18) & 3;
            int kB = (ksB >>> 18) & 3;
            if (kA == ChunkRef.KIND_UNI && kB == ChunkRef.KIND_UNI) {
                if (exchangeUniExactXZ(uA, uB, 9)) {
                    aCr.c9 = bCr.c9 = 1;
                    d = true;
                }
            } else {
                d |= exchangeFaceExact(aCr, 9, kA, faceA, bCr, 9, kB, faceB);
            }
        }

        if ((aCr.c10 | bCr.c10) != 0) {
            int kA = (ksA >>> 20) & 3;
            int kB = (ksB >>> 20) & 3;
            if (kA == ChunkRef.KIND_UNI && kB == ChunkRef.KIND_UNI) {
                if (exchangeUniExactXZ(uA, uB, 10)) {
                    aCr.c10 = bCr.c10 = 1;
                    d = true;
                }
            } else {
                d |= exchangeFaceExact(aCr, 10, kA, faceA, bCr, 10, kB, faceB);
            }
        }

        if ((aCr.c11 | bCr.c11) != 0) {
            int kA = (ksA >>> 22) & 3;
            int kB = (ksB >>> 22) & 3;
            if (kA == ChunkRef.KIND_UNI && kB == ChunkRef.KIND_UNI) {
                if (exchangeUniExactXZ(uA, uB, 11)) {
                    aCr.c11 = bCr.c11 = 1;
                    d = true;
                }
            } else {
                d |= exchangeFaceExact(aCr, 11, kA, faceA, bCr, 11, kB, faceB);
            }
        }

        if ((aCr.c12 | bCr.c12) != 0) {
            int kA = (ksA >>> 24) & 3;
            int kB = (ksB >>> 24) & 3;
            if (kA == ChunkRef.KIND_UNI && kB == ChunkRef.KIND_UNI) {
                if (exchangeUniExactXZ(uA, uB, 12)) {
                    aCr.c12 = bCr.c12 = 1;
                    d = true;
                }
            } else {
                d |= exchangeFaceExact(aCr, 12, kA, faceA, bCr, 12, kB, faceB);
            }
        }

        if ((aCr.c13 | bCr.c13) != 0) {
            int kA = (ksA >>> 26) & 3;
            int kB = (ksB >>> 26) & 3;
            if (kA == ChunkRef.KIND_UNI && kB == ChunkRef.KIND_UNI) {
                if (exchangeUniExactXZ(uA, uB, 13)) {
                    aCr.c13 = bCr.c13 = 1;
                    d = true;
                }
            } else {
                d |= exchangeFaceExact(aCr, 13, kA, faceA, bCr, 13, kB, faceB);
            }
        }

        if ((aCr.c14 | bCr.c14) != 0) {
            int kA = (ksA >>> 28) & 3;
            int kB = (ksB >>> 28) & 3;
            if (kA == ChunkRef.KIND_UNI && kB == ChunkRef.KIND_UNI) {
                if (exchangeUniExactXZ(uA, uB, 14)) {
                    aCr.c14 = bCr.c14 = 1;
                    d = true;
                }
            } else {
                d |= exchangeFaceExact(aCr, 14, kA, faceA, bCr, 14, kB, faceB);
            }
        }

        if ((aCr.c15 | bCr.c15) != 0) {
            int kA = (ksA >>> 30) & 3;
            int kB = (ksB >>> 30) & 3;
            if (kA == ChunkRef.KIND_UNI && kB == ChunkRef.KIND_UNI) {
                if (exchangeUniExactXZ(uA, uB, 15)) {
                    aCr.c15 = bCr.c15 = 1;
                    d = true;
                }
            } else {
                d |= exchangeFaceExact(aCr, 15, kA, faceA, bCr, 15, kB, faceB);
            }
        }
        // </editor-fold>
        if (d) aCr.dirtyFlag = bCr.dirtyFlag = true;
    }

    static boolean exchangeUniExactY(double[] uni, int a, int b) {
        double ra = uni[a];
        double rb = uni[b];
        if (ra == rb) return false;
        double avg = 0.5d * (ra + rb);
        double delta = 0.5d * (ra - rb) * UU_E;
        uni[a] = avg + delta;
        uni[b] = avg - delta;
        return true;
    }

    static final class DiffuseYTask extends RecursiveAction {
        final ChunkRef[] chunks;
        final int lo, hi, parity, threshold;

        DiffuseYTask(ChunkRef[] chunks, int lo, int hi, int parity, int threshold) {
            this.chunks = chunks;
            this.lo = lo;
            this.hi = hi;
            this.parity = parity;
            this.threshold = threshold;
        }

        @Override
        protected void compute() {
            int n = hi - lo;
            if (n <= threshold) {
                work(lo, hi);
                return;
            }
            int mid = (lo + hi) >>> 1;
            var left = new DiffuseYTask(chunks, lo, mid, parity, threshold).fork();
            new DiffuseYTask(chunks, mid, hi, parity, threshold).compute();
            left.join();
        }

        void work(int start, int end) {
            for (int i = start; i < end; i++) {
                ChunkRef cr = chunks[i];
                boolean d = false;
                int k = cr.sectionKinds;
                double[] u = cr.uniformRads;
                // <editor-fold defaultstate="collapsed" desc="unrolled boilerplate">
                if (parity == 0) {
                    if ((cr.c0 | cr.c1) != 0) {
                        int k0 = k & 3;
                        int k1 = (k >>> 2) & 3;
                        if (k0 == ChunkRef.KIND_UNI && k1 == ChunkRef.KIND_UNI) {
                            if (exchangeUniExactY(u, 0, 1)) {
                                cr.c0 = cr.c1 = 1;
                                d = true;
                            }
                        } else d |= exchangeFaceExactY(cr, 0, k0, 1, k1);
                    }
                    if ((cr.c2 | cr.c3) != 0) {
                        int k2 = (k >>> 4) & 3;
                        int k3 = (k >>> 6) & 3;
                        if (k2 == ChunkRef.KIND_UNI && k3 == ChunkRef.KIND_UNI) {
                            if (exchangeUniExactY(u, 2, 3)) {
                                cr.c2 = cr.c3 = 1;
                                d = true;
                            }
                        } else d |= exchangeFaceExactY(cr, 2, k2, 3, k3);
                    }
                    if ((cr.c4 | cr.c5) != 0) {
                        int k4 = (k >>> 8) & 3;
                        int k5 = (k >>> 10) & 3;
                        if (k4 == ChunkRef.KIND_UNI && k5 == ChunkRef.KIND_UNI) {
                            if (exchangeUniExactY(u, 4, 5)) {
                                cr.c4 = cr.c5 = 1;
                                d = true;
                            }
                        } else d |= exchangeFaceExactY(cr, 4, k4, 5, k5);
                    }
                    if ((cr.c6 | cr.c7) != 0) {
                        int k6 = (k >>> 12) & 3;
                        int k7 = (k >>> 14) & 3;
                        if (k6 == ChunkRef.KIND_UNI && k7 == ChunkRef.KIND_UNI) {
                            if (exchangeUniExactY(u, 6, 7)) {
                                cr.c6 = cr.c7 = 1;
                                d = true;
                            }
                        } else d |= exchangeFaceExactY(cr, 6, k6, 7, k7);
                    }
                    if ((cr.c8 | cr.c9) != 0) {
                        int k8 = (k >>> 16) & 3;
                        int k9 = (k >>> 18) & 3;
                        if (k8 == ChunkRef.KIND_UNI && k9 == ChunkRef.KIND_UNI) {
                            if (exchangeUniExactY(u, 8, 9)) {
                                cr.c8 = cr.c9 = 1;
                                d = true;
                            }
                        } else d |= exchangeFaceExactY(cr, 8, k8, 9, k9);
                    }
                    if ((cr.c10 | cr.c11) != 0) {
                        int k10 = (k >>> 20) & 3;
                        int k11 = (k >>> 22) & 3;
                        if (k10 == ChunkRef.KIND_UNI && k11 == ChunkRef.KIND_UNI) {
                            if (exchangeUniExactY(u, 10, 11)) {
                                cr.c10 = cr.c11 = 1;
                                d = true;
                            }
                        } else d |= exchangeFaceExactY(cr, 10, k10, 11, k11);
                    }
                    if ((cr.c12 | cr.c13) != 0) {
                        int k12 = (k >>> 24) & 3;
                        int k13 = (k >>> 26) & 3;
                        if (k12 == ChunkRef.KIND_UNI && k13 == ChunkRef.KIND_UNI) {
                            if (exchangeUniExactY(u, 12, 13)) {
                                cr.c12 = cr.c13 = 1;
                                d = true;
                            }
                        } else d |= exchangeFaceExactY(cr, 12, k12, 13, k13);
                    }
                    if ((cr.c14 | cr.c15) != 0) {
                        int k14 = (k >>> 28) & 3;
                        int k15 = (k >>> 30) & 3;
                        if (k14 == ChunkRef.KIND_UNI && k15 == ChunkRef.KIND_UNI) {
                            if (exchangeUniExactY(u, 14, 15)) {
                                cr.c14 = cr.c15 = 1;
                                d = true;
                            }
                        } else d |= exchangeFaceExactY(cr, 14, k14, 15, k15);
                    }
                } else {
                    if ((cr.c1 | cr.c2) != 0) {
                        int k1 = (k >>> 2) & 3;
                        int k2 = (k >>> 4) & 3;
                        if (k1 == ChunkRef.KIND_UNI && k2 == ChunkRef.KIND_UNI) {
                            if (exchangeUniExactY(u, 1, 2)) {
                                cr.c1 = cr.c2 = 1;
                                d = true;
                            }
                        } else d |= exchangeFaceExactY(cr, 1, k1, 2, k2);
                    }
                    if ((cr.c3 | cr.c4) != 0) {
                        int k3 = (k >>> 6) & 3;
                        int k4 = (k >>> 8) & 3;
                        if (k3 == ChunkRef.KIND_UNI && k4 == ChunkRef.KIND_UNI) {
                            if (exchangeUniExactY(u, 3, 4)) {
                                cr.c3 = cr.c4 = 1;
                                d = true;
                            }
                        } else d |= exchangeFaceExactY(cr, 3, k3, 4, k4);
                    }
                    if ((cr.c5 | cr.c6) != 0) {
                        int k5 = (k >>> 10) & 3;
                        int k6 = (k >>> 12) & 3;
                        if (k5 == ChunkRef.KIND_UNI && k6 == ChunkRef.KIND_UNI) {
                            if (exchangeUniExactY(u, 5, 6)) {
                                cr.c5 = cr.c6 = 1;
                                d = true;
                            }
                        } else d |= exchangeFaceExactY(cr, 5, k5, 6, k6);
                    }
                    if ((cr.c7 | cr.c8) != 0) {
                        int k7 = (k >>> 14) & 3;
                        int k8 = (k >>> 16) & 3;
                        if (k7 == ChunkRef.KIND_UNI && k8 == ChunkRef.KIND_UNI) {
                            if (exchangeUniExactY(u, 7, 8)) {
                                cr.c7 = cr.c8 = 1;
                                d = true;
                            }
                        } else d |= exchangeFaceExactY(cr, 7, k7, 8, k8);
                    }
                    if ((cr.c9 | cr.c10) != 0) {
                        int k9 = (k >>> 18) & 3;
                        int k10 = (k >>> 20) & 3;
                        if (k9 == ChunkRef.KIND_UNI && k10 == ChunkRef.KIND_UNI) {
                            if (exchangeUniExactY(u, 9, 10)) {
                                cr.c9 = cr.c10 = 1;
                                d = true;
                            }
                        } else d |= exchangeFaceExactY(cr, 9, k9, 10, k10);
                    }
                    if ((cr.c11 | cr.c12) != 0) {
                        int k11 = (k >>> 22) & 3;
                        int k12 = (k >>> 24) & 3;
                        if (k11 == ChunkRef.KIND_UNI && k12 == ChunkRef.KIND_UNI) {
                            if (exchangeUniExactY(u, 11, 12)) {
                                cr.c11 = cr.c12 = 1;
                                d = true;
                            }
                        } else d |= exchangeFaceExactY(cr, 11, k11, 12, k12);
                    }
                    if ((cr.c13 | cr.c14) != 0) {
                        int k13 = (k >>> 26) & 3;
                        int k14 = (k >>> 28) & 3;
                        if (k13 == ChunkRef.KIND_UNI && k14 == ChunkRef.KIND_UNI) {
                            if (exchangeUniExactY(u, 13, 14)) {
                                cr.c13 = cr.c14 = 1;
                                d = true;
                            }
                        } else d |= exchangeFaceExactY(cr, 13, k13, 14, k14);
                    }
                }
                // </editor-fold>
                if (d) cr.dirtyFlag = true;
            }
        }
    }

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

        //@formatter:off
        abstract boolean exchangeWithMulti(MultiSectionRef other, int myFace, int otherFace);
        abstract boolean exchangeWithUniform(ChunkRef other, int myFace, int otherFace, int otherSy);
        abstract boolean exchangeWithSingle(SingleMaskedSectionRef other, int myFace, int otherFace);
        abstract int getPocketIndex(long pos);
        abstract int paletteIndexOrNeg(int blockIndex);
        abstract void clearFaceAllPockets(int faceOrdinal);
        abstract void linkFaceToMulti(MultiSectionRef other, int myFace);
        abstract void linkFaceToSingle(SingleMaskedSectionRef single, int faceA);
        abstract void linkFaceToUniform(ChunkRef crA, int faceA);
        //@formatter:on
    }

    static final class SingleMaskedSectionRef extends SectionRef {
        static final long CONN_OFF = fieldOffset(SingleMaskedSectionRef.class, "connections");

        final int volume;
        final double invVolume, cx, cy, cz;
        final long packedFaceCounts;
        long connections;
        double rad;

        SingleMaskedSectionRef(ChunkRef owner, int sy, byte[] pocketData, int volume, long packedFaceCounts, double cx, double cy, double cz) {
            super(owner, sy, (byte) 1, pocketData);
            this.volume = volume;
            this.invVolume = 1.0d / volume;
            this.cx = cx;
            this.cy = cy;
            this.cz = cz;
            this.packedFaceCounts = packedFaceCounts;
        }

        @Override
        boolean exchangeWithSingle(SingleMaskedSectionRef other, int myFace, int otherFace) {
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
            owner.setActive0(sy);
            other.owner.setActive0(other.sy);
            return true;
        }

        @Override
        boolean exchangeWithUniform(ChunkRef other, int myFace, int otherFace, int otherSy) {
            int area = getFaceCount(myFace);
            double ra = other.uniformRads[otherSy];
            double rb = rad;
            if (area <= 0 || ra == rb) return false;
            double distSum = 8.0d + getFaceDist(myFace);
            if (distSum <= 0.0D) return false;
            double invVb = invVolume;
            double denomInv = 2.44140625E-4 + invVb;
            double e = Math.exp(-((area / distSum) * denomInv * diffusionDt));
            double rStar = (ra * invVb + rb * 2.44140625E-4) / denomInv;
            other.uniformRads[otherSy] = rStar + (ra - rStar) * e;
            rad = rStar + (rb - rStar) * e;
            other.setActive0(otherSy);
            owner.setActive0(sy);
            return true;
        }

        @Override
        boolean exchangeWithMulti(MultiSectionRef other, int myFace, int otherFace) {
            boolean changed = false;
            int bCount = Math.min(other.pocketCount & 0xFF, NO_POCKET);
            int stride = 6 * NEI_SLOTS;
            char[] conn = other.connectionArea;
            byte[] faceAct = other.faceActive;
            int slot0 = (otherFace << 4) + NEI_SHIFT;
            double[] otherData = other.data;
            double[] otherFaceDist = other.faceDist;
            double myInvVol = invVolume;
            double distA = getFaceDist(myFace);
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
                other.owner.setActiveBit(other.sy, pi);
            }
            if (changed) {
                rad = myRad;
                owner.setActive0(sy);
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

        void updateConnections(int face, int value) {
            int shift = face * 9;
            long mask = 0x1FFL << shift;
            long bits = ((long) value & 0x1FFL) << shift;
            while (true) {
                long cur = U.getLongVolatile(this, CONN_OFF);
                long next = (cur & ~mask) | bits;
                if (cur == next) return;
                if (U.compareAndSetLong(this, CONN_OFF, cur, next)) return;
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
            other.updateConnections(faceB, count);
            updateConnections(faceA, count);
        }

        @Override
        void linkFaceToUniform(ChunkRef crA, int faceA) {
            int area = getFaceCount(faceA);
            updateConnections(faceA, area);
        }
    }

    static final class MultiSectionRef extends SectionRef {
        static final int STRIDE = 6 * NEI_SLOTS;
        final byte[] faceActive;
        final char[] connectionArea;
        final double[] data; // even=rad, odd=invVolume
        final double[] faceDist;
        final int[] volume;

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
        boolean exchangeWithMulti(MultiSectionRef other, int myFace, int otherFace) {
            boolean changed = false;
            char[] conn = connectionArea;
            byte[] faceAct = faceActive;
            int aCount = pocketCount & 0xFF;
            int bCount = Math.min(other.pocketCount & 0xFF, NO_POCKET);
            int faceBase0 = myFace << 4;
            double[] myData = data;
            double[] otherData = other.data;
            double[] myFaceDist = faceDist;
            double[] otherFaceDist = other.faceDist;
            for (int pi = 0; pi < aCount; pi++) {
                int pi6 = pi * 6;
                if (faceAct[pi6 + myFace] == 0) continue;
                int base = pi * STRIDE + faceBase0;
                if (conn[base] == 0) continue;
                int idxA = pi << 1;
                double ra = myData[idxA];
                double invVa = myData[idxA + 1];
                double distA = myFaceDist[pi6 + myFace];
                boolean myChanged = false;
                for (int npi = 0; npi < bCount; npi++) {
                    int area = conn[base + NEI_SHIFT + npi];
                    if (area == 0) continue;
                    int idxB = npi << 1;
                    double rb = otherData[idxB];
                    if (ra == rb) continue;
                    double invVb = otherData[idxB + 1];
                    double denomInv = invVa + invVb;
                    double distSum = distA + otherFaceDist[npi * 6 + otherFace];
                    if (distSum <= 0.0D) continue;
                    double e = Math.exp(-((area / distSum) * denomInv * diffusionDt));
                    double rStar = (ra * invVb + rb * invVa) / denomInv;
                    ra = rStar + (ra - rStar) * e;
                    otherData[idxB] = rStar + (rb - rStar) * e;
                    changed = myChanged = true;
                    other.owner.setActiveBit(other.sy, npi);
                }
                if (myChanged) {
                    myData[idxA] = ra;
                    owner.setActiveBit(sy, pi);
                }
            }
            return changed;
        }

        @Override
        boolean exchangeWithUniform(ChunkRef other, int myFace, int otherFace, int otherSy) {
            boolean changed = false;
            char[] conn = connectionArea;
            byte[] faceAct = faceActive;
            double[] myData = data;
            double[] myFaceDist = faceDist;
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
                double invVb = myData[idx + 1];
                double denomInv = 2.44140625E-4 + invVb;
                double e = Math.exp(-((area / distSum) * denomInv * diffusionDt));
                double rStar = (ra * invVb + rb * 2.44140625E-4) / denomInv;
                ra = rStar + (ra - rStar) * e;
                myData[idx] = rStar + (rb - rStar) * e;
                changed = true;
                otherChanged = true;
                owner.setActiveBit(sy, pi);
            }
            if (otherChanged) {
                otherUni[otherSy] = ra;
                other.setActive0(otherSy);
            }
            return changed;
        }

        @Override
        boolean exchangeWithSingle(SingleMaskedSectionRef other, int myFace, int otherFace) {
            boolean changed = false;
            char[] conn = connectionArea;
            byte[] faceAct = faceActive;
            double[] myData = data;
            int aCount = pocketCount & 0xFF;
            int slot0 = (myFace << 4) + NEI_SHIFT;
            double rb = other.rad;
            double invVb = other.invVolume;
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
                owner.setActiveBit(sy, pi);
            }
            if (otherChanged) {
                other.rad = rb;
                other.owner.setActive0(other.sy);
            }
            return changed;
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
    // - ChunkRef mask words (c0...c15)
    // - Radiation values (uniformRads / Single.rad / Multi.data) are also plain.
    // If scheduling is changed (overlap sweeps, allow multiple simultaneous neighbor exchanges per chunk, etc.),
    // these plain accesses are no longer justified and must become atomic or be redesigned.
    static final class WorldRadiationData {
        final WorldServer world;
        final long worldSalt;
        final double minBound;
        // ChunkRef objects in chunkRef map values must have a nonnull mcChunk
        final Long2ReferenceOpenHashMap<ChunkRef> chunkRefs = new Long2ReferenceOpenHashMap<>(4225);//32 vd
        final DirtyChunkTracker dirtyCk = new DirtyChunkTracker(2048);
        final MpscUnboundedXaddArrayLongQueue destructionQueue = new MpscUnboundedXaddArrayLongQueue(64);
        final TLPool<byte[]> pocketDataPool = new TLPool<>(() -> new byte[2048], _ -> /*@formatter:off*/{}/*@formatter:on*/, 256, 4096);
        final LongArrayList dirtyToRebuildScratch = new LongArrayList(16384);
        final Long2ReferenceOpenHashMap<EditTable> writes = new Long2ReferenceOpenHashMap<>(256);
        final ObjectPool<EditTable> editTablePool = new ObjectPool<>(() -> new EditTable(32), EditTable::clear, 64);
        final ChunkRef[][] parityBuckets = new ChunkRef[4][4096];
        final int[] parityCounts = new int[4];
        long[] linkScratch = new long[512];
        ChunkRef[] dirtyChunkRefsScratch = new ChunkRef[1024];
        int[] dirtyChunkMasksScratchArr = new int[4096];
        long pocketToDestroy = Long.MIN_VALUE;
        int workEpoch, executionSampleCount;
        long workEpochSalt, profSteps, setSeq;
        double profTotalMs, profMaxMs, executionTimeAccumulator;
        DoubleArrayList profSamplesMs;

        WorldRadiationData(WorldServer world) {
            this.world = world;
            //noinspection AutoBoxing
            Object v = CompatibilityConfig.dimensionRad.get(world.provider.getDimension());
            double mb = -((v instanceof Number n) ? n.doubleValue() : 0D);
            if (!Double.isFinite(mb) || mb > 0.0D) mb = 0.0D;
            minBound = mb;
            worldSalt = HashCommon.murmurHash3(world.getSeed() ^ (long) world.provider.getDimension() * 0x9E3779B97F4A7C15L ^ 0xD1B54A32D192ED03L);
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

        static int grow(int current, int need) {
            int n = Math.max(current, 16);
            while (n < need) n = n + (n >>> 1) + 16;
            return n;
        }

        static long packSectionLocal(long sectionKey, int localIdx) {
            return sectionKey | (((long) localIdx & 0xFFFL) << 4);
        }

        static long stripLocal(long packedSectionLocal) {
            return packedSectionLocal & ~0xFFF0L;
        }

        static void linkNonUniFace(SectionRef a, int kA, int faceA, @Nullable ChunkRef crB, int kindsB, @Nullable SectionRef @Nullable [] secB, int syB) {
            assert kA > ChunkRef.KIND_UNI;
            assert (syB & ~15) == 0;
            int kB = (crB == null || secB == null) ? ChunkRef.KIND_NONE : ((kindsB >>> (syB << 1)) & 3);
            if (kB == ChunkRef.KIND_UNI) {
                a.linkFaceToUniform(crB, faceA);
                return;
            }
            if (kB == ChunkRef.KIND_NONE) {
                a.clearFaceAllPockets(faceA);
                if (kA == ChunkRef.KIND_MULTI) ((MultiSectionRef) a).markSentinelPlane16x16(faceA);
                return;
            }
            SectionRef b = secB[syB];
            assert b != null;
            if (kB == ChunkRef.KIND_MULTI) {
                a.linkFaceToMulti((MultiSectionRef) b, faceA);
            } else {
                a.linkFaceToSingle((SingleMaskedSectionRef) b, faceA);
            }
        }

        static int floodFillPockets(SectionMask resistant, byte[] pocketData, int[] queue, int @Nullable [] vols, long @Nullable [] sumX, long @Nullable [] sumY, long @Nullable [] sumZ) {
            if (vols != null) Arrays.fill(vols, 0);
            if (sumX != null) Arrays.fill(sumX, 0);
            if (sumY != null) Arrays.fill(sumY, 0);
            if (sumZ != null) Arrays.fill(sumZ, 0);
            int pc = 0;
            for (int blockIndex = 0; blockIndex < SECTION_BLOCK_COUNT; blockIndex++) {
                if (readNibble(pocketData, blockIndex) != NO_POCKET) continue;
                if (resistant.get(blockIndex)) continue;

                int pocketIndex = (pc >= NO_POCKET) ? 0 : pc++;
                int head = 0, tail = 0;
                queue[tail++] = blockIndex;
                writeNibble(pocketData, blockIndex, pocketIndex);

                if (vols != null) vols[pocketIndex]++;
                if (sumX != null) {
                    assert sumY != null && sumZ != null;
                    sumX[pocketIndex] += Library.getLocalX(blockIndex);
                    sumY[pocketIndex] += Library.getLocalY(blockIndex);
                    sumZ[pocketIndex] += Library.getLocalZ(blockIndex);
                }

                while (head != tail) {
                    int cur = queue[head++];
                    for (int f = 0; f < 6; f++) {
                        int nei = cur + LINEAR_OFFSETS[f];
                        if (((nei & 0xF000) | ((cur ^ nei) & BOUNDARY_MASKS[f])) != 0) continue;
                        if (readNibble(pocketData, nei) != NO_POCKET) continue;
                        if (resistant.get(nei)) continue;

                        writeNibble(pocketData, nei, pocketIndex);
                        queue[tail++] = nei;

                        if (vols != null) vols[pocketIndex]++;
                        if (sumX != null) {
                            sumX[pocketIndex] += Library.getLocalX(nei);
                            sumY[pocketIndex] += Library.getLocalY(nei);
                            sumZ[pocketIndex] += Library.getLocalZ(nei);
                        }
                    }
                }
            }
            return pc;
        }

        static void remapPocketMass(ChunkRef owner, int sy, int oldKind, @Nullable SectionRef old, int newPocketCount, byte @Nullable [] newPocketData, int[] newVols, double[] outNewMass) {

            Arrays.fill(outNewMass, 0, newPocketCount, 0.0d);
            if (oldKind == ChunkRef.KIND_NONE || newPocketCount == 0) return;

            // New mapping is uniform.
            if (newPocketData == null) {
                double totalMass = 0.0d;

                if (oldKind == ChunkRef.KIND_UNI) {
                    double d = owner.uniformRads[sy];
                    if (Math.abs(d) > RAD_EPSILON) totalMass = mulClamp(d, SECTION_BLOCK_COUNT);
                } else if (old != null && old.pocketCount > 0) {
                    if (oldKind == ChunkRef.KIND_SINGLE) {
                        SingleMaskedSectionRef s = (SingleMaskedSectionRef) old;
                        double d = s.rad;
                        if (Math.abs(d) > RAD_EPSILON) totalMass = mulClamp(d, Math.max(1, s.volume));
                    } else { // MULTI
                        MultiSectionRef m = (MultiSectionRef) old;
                        int oldCnt = Math.min(m.pocketCount & 0xFF, NO_POCKET);
                        for (int p = 0; p < oldCnt; p++) {
                            double d = m.data[p << 1];
                            if (Math.abs(d) > RAD_EPSILON)
                                totalMass = addClamp(totalMass, mulClamp(d, Math.max(1, m.volume[p])));
                        }
                    }
                }

                outNewMass[0] = totalMass;
                return;
            }

            // Old was uniform -> distribute by new volumes.
            if (oldKind == ChunkRef.KIND_UNI) {
                double d = owner.uniformRads[sy];
                if (Math.abs(d) <= RAD_EPSILON) return;

                double oldMass = mulClamp(d, SECTION_BLOCK_COUNT);
                long totalNewAir = 0L;
                for (int p = 0; p < newPocketCount; p++) totalNewAir += Math.max(1, newVols[p]);
                if (totalNewAir <= 0L) return;

                double massPerBlock = oldMass / (double) totalNewAir;
                for (int p = 0; p < newPocketCount; p++)
                    outNewMass[p] = mulClamp(massPerBlock, Math.max(1, newVols[p]));
                return;
            }

            if (old == null || old.pocketCount <= 0) return;

            int oldCnt = Math.min(old.pocketCount & 0xFF, NO_POCKET);

            byte[] oldPocketData = old.pocketData;

            int[] overlaps = TL_OVERLAPS.get();
            Arrays.fill(overlaps, 0, oldCnt * newPocketCount, 0);

            for (int i = 0; i < SECTION_BLOCK_COUNT; i++) {
                int nIdx = readNibble(newPocketData, i);
                if (nIdx == NO_POCKET || nIdx >= newPocketCount) continue;

                int oIdx = readNibble(oldPocketData, i);
                if (oIdx == NO_POCKET || oIdx >= oldCnt) continue;

                overlaps[oIdx * newPocketCount + nIdx]++;
            }

            double[] oldMass = TL_OLD_MASS.get();
            Arrays.fill(oldMass, 0, oldCnt, 0.0d);

            if (oldKind == ChunkRef.KIND_SINGLE) {
                SingleMaskedSectionRef s = (SingleMaskedSectionRef) old;
                double d0 = s.rad;
                if (Math.abs(d0) > RAD_EPSILON) oldMass[0] = mulClamp(d0, Math.max(1, s.volume));
            } else { // MULTI
                MultiSectionRef m = (MultiSectionRef) old;
                for (int p = 0; p < oldCnt; p++) {
                    double dp = m.data[p << 1];
                    if (Math.abs(dp) > RAD_EPSILON) oldMass[p] = mulClamp(dp, Math.max(1, m.volume[p]));
                }
            }

            for (int o = 0; o < oldCnt; o++) {
                double mass = oldMass[o];
                if (Math.abs(mass) <= RAD_EPSILON) continue;

                int row = o * newPocketCount;
                int total = 0;
                for (int n = 0; n < newPocketCount; n++) total += overlaps[row + n];
                if (total == 0) continue;

                double massPerBlock = mass / (double) total;
                for (int n = 0; n < newPocketCount; n++) {
                    int c = overlaps[row + n];
                    if (c != 0) outNewMass[n] = addClamp(outNewMass[n], mulClamp(massPerBlock, c));
                }
            }
        }

        void ensureParityBucketCapacity(int bucket, int need) {
            ChunkRef[] arr = parityBuckets[bucket];
            if (arr.length >= need) return;
            int n = arr.length;
            while (n < need) n = n + (n >>> 1) + 16;
            parityBuckets[bucket] = Arrays.copyOf(arr, n);
        }

        void addLoadedToBucket(ChunkRef cr) {
            assert cr.mcChunk != null && cr.mcChunk.loaded : "Adding to bucket requires loaded mcChunk";
            assert cr.parityIndex < 0 : "Double-add to parity bucket";
            byte b = (byte) ((cr.ck & 1L) | ((cr.ck >>> 31) & 2L));
            int i = parityCounts[b]++;
            ensureParityBucketCapacity(b, parityCounts[b]);
            parityBuckets[b][i] = cr;
            cr.parityBucket = b;
            cr.parityIndex = i;
        }

        void removeLoadedFromBucket(ChunkRef cr) {
            int i = cr.parityIndex;
            if (i < 0) return;
            int b = cr.parityBucket;
            assert b >= 0 && b < 4;

            int last = --parityCounts[b];
            assert last >= 0;

            ChunkRef[] arr = parityBuckets[b];
            ChunkRef swap = arr[last];
            assert swap != null;

            arr[i] = swap;
            swap.parityIndex = i;

            arr[last] = null;

            cr.parityBucket = -1;
            cr.parityIndex = -1;
        }

        void clearBuckets() {
            for (int b = 0; b < 4; b++) {
                ChunkRef[] arr = parityBuckets[b];
                int n = parityCounts[b];
                for (int i = 0; i < n; i++) {
                    ChunkRef cr = arr[i];
                    if (cr != null) {
                        cr.parityBucket = -1;
                        cr.parityIndex = -1;
                        cr.dirtyFlag = false;
                    }
                    arr[i] = null;
                }
                parityCounts[b] = 0;
            }
        }

        void runExactExchangeSweeps() {
            ChunkRef[][] b = parityBuckets;
            int[] c = parityCounts;
            int c0 = c[0], c1 = c[1], c2 = c[2], c3 = c[3];
            int th0 = getTaskThreshold(c0, 64), th1 = getTaskThreshold(c1, 64), th2 = getTaskThreshold(c2, 64), th3 = getTaskThreshold(c3, 64);

            int s = workEpoch;
            boolean fx = (s & 1) != 0, fz = (s & 2) != 0;
            int yPar = (s & 4) != 0 ? 1 : 0;
            int perm = s % 6;
            if (perm < 0) perm += 6;

            switch (perm) {//@formatter:off
                case 0 -> { sweepX(b, c0, c1, c2, c3, th0, th1, th2, th3, fx); sweepZ(b, c0, c1, c2, c3, th0, th1, th2, th3, fz); sweepY(b, c0, c1, c2, c3, th0, th1, th2, th3, yPar); }
                case 1 -> { sweepX(b, c0, c1, c2, c3, th0, th1, th2, th3, fx); sweepY(b, c0, c1, c2, c3, th0, th1, th2, th3, yPar); sweepZ(b, c0, c1, c2, c3, th0, th1, th2, th3, fz); }
                case 2 -> { sweepY(b, c0, c1, c2, c3, th0, th1, th2, th3, yPar); sweepZ(b, c0, c1, c2, c3, th0, th1, th2, th3, fz); sweepX(b, c0, c1, c2, c3, th0, th1, th2, th3, fx); }
                case 3 -> { sweepY(b, c0, c1, c2, c3, th0, th1, th2, th3, yPar); sweepX(b, c0, c1, c2, c3, th0, th1, th2, th3, fx); sweepZ(b, c0, c1, c2, c3, th0, th1, th2, th3, fz); }
                case 4 -> { sweepZ(b, c0, c1, c2, c3, th0, th1, th2, th3, fz); sweepX(b, c0, c1, c2, c3, th0, th1, th2, th3, fx); sweepY(b, c0, c1, c2, c3, th0, th1, th2, th3, yPar); }
                default -> { sweepZ(b, c0, c1, c2, c3, th0, th1, th2, th3, fz); sweepY(b, c0, c1, c2, c3, th0, th1, th2, th3, yPar); sweepX(b, c0, c1, c2, c3, th0, th1, th2, th3, fx); }
            }//@formatter:on
        }

        void postSweepDecayAndEffects() {
            ChunkRef[][] b = parityBuckets;
            int[] c = parityCounts;
            int c0 = c[0], c1 = c[1], c2 = c[2], c3 = c[3];
            int th0 = getTaskThreshold(c0, 64), th1 = getTaskThreshold(c1, 64), th2 = getTaskThreshold(c2, 64), th3 = getTaskThreshold(c3, 64);
            var t0 = new PostSweepTask(b[0], 0, c0, th0).fork();
            var t1 = new PostSweepTask(b[1], 0, c1, th1).fork();
            var t2 = new PostSweepTask(b[2], 0, c2, th2).fork();
            new PostSweepTask(b[3], 0, c3, th3).invoke();
            t0.join();
            t1.join();
            t2.join();
        }

        void processWorldSimulation() {
            long time = System.nanoTime();
            rebuildDirtySections();
            clearQueuedWrites();
            nextWorkEpoch();
            runExactExchangeSweeps();
            postSweepDecayAndEffects();
            cleanupAndLog(time);
        }

        void cleanupAndLog(long time) {
            if (tickDelay != 1 && workEpoch % 200 == 13) {
                destructionQueue.clear(true);
            }
            logProfilingMessage(time);
        }

        void logProfilingMessage(long stepStartNs) {
            if (!GeneralConfig.enableDebugMode) return;
            double ms = (System.nanoTime() - stepStartNs) * 1.0e-6;
            profSteps++;
            profTotalMs += ms;
            if (ms > profMaxMs) profMaxMs = ms;
            DoubleArrayList samples = profSamplesMs;
            if (samples == null) {
                profSamplesMs = samples = new DoubleArrayList(8192);
            }
            int n = samples.size();
            if (n < 8192) {
                samples.add(ms);
            } else {
                long seen = profSteps;
                long r = HashCommon.mix(workEpochSalt + seen * 0x9E3779B97F4A7C15L);
                long j = Long.remainderUnsigned(r, seen);
                if (j < 8192) {
                    samples.set((int) j, ms);
                }
            }
            executionTimeAccumulator += ms;
            int w = ++executionSampleCount;
            if (w < PROFILE_WINDOW) return;
            double totalMs = executionTimeAccumulator;
            double avgWinMs = Math.rint((totalMs / PROFILE_WINDOW) * 1000.0) / 1000.0;
            double lastMs = Math.rint(ms * 1000.0) / 1000.0;
            int dimId = world.provider.getDimension();
            String dimType = world.provider.getDimensionType().getName();
            //noinspection AutoBoxing
            MainRegistry.logger.info("[RadiationSystemNT] dim {} ({}) avg {} ms/step over last {} steps (total {} ms, last {} ms)", dimId, dimType, avgWinMs, PROFILE_WINDOW, (int) Math.rint(totalMs), lastMs);
            executionTimeAccumulator = 0.0D;
            executionSampleCount = 0;
        }

        void rebuildDirtySections() {
            int dirtyChunks = dirtyCk.slotSize;
            if (dirtyChunks == 0) return;

            ensureDirtyChunkRefCapacity(dirtyChunks);
            LongArrayList toRelink = dirtyToRebuildScratch;
            toRelink.clear();

            int batch = 0;
            int[] slots = dirtyCk.slots;

            for (int i = 0; i < dirtyChunks; i++) {
                int pos = slots[i];
                ChunkRef cr = dirtyCk.refs[pos];
                if (cr == null) continue;
                if (cr.mcChunk == null) continue; // Chunk unloaded

                int m16 = dirtyCk.masks16[pos];
                if (m16 == 0) continue;

                dirtyChunkRefsScratch[batch] = cr;
                dirtyChunkMasksScratchArr[batch] = m16;
                batch++;

                int m = m16;
                long sck = Library.sectionToLong(cr.ck, 0);
                while (m != 0) {
                    int sy = Integer.numberOfTrailingZeros(m);
                    m &= (m - 1);
                    toRelink.add(Library.setSectionY(sck, sy));
                }
            }

            dirtyCk.reset();
            if (batch == 0) return;

            int threshold = getTaskThreshold(batch, 8);
            new RebuildDirtyChunkBatchTask(dirtyChunkRefsScratch, dirtyChunkMasksScratchArr, 0, batch, threshold).invoke();

            int relinkCount = toRelink.size();
            if (relinkCount != 0) relinkKeys(toRelink.elements(), relinkCount);
        }

        EditTable editsFor(long ck) {
            EditTable t = writes.get(ck);
            if (t != null) return t;
            t = editTablePool.borrow();
            writes.put(ck, t);
            return t;
        }

        void queueSet(long sck, int local, double density) {
            long ck = Library.sectionToChunkLong(sck);
            long sckl = packSectionLocal(sck, local);
            long seq = ++setSeq;
            editsFor(ck).putSet(sckl, density, seq);
        }

        void queueAdd(long sck, int local, double delta) {
            long ck = Library.sectionToChunkLong(sck);
            long sckl = packSectionLocal(sck, local);
            editsFor(ck).addTo(sckl, delta);
        }

        void clearQueuedWrites() {
            for (var t : writes.values()) editTablePool.recycle(t);
            writes.clear();
        }

        void applyQueuedWrites(long sectionKey, byte @Nullable [] pocketData, int pocketCount, double[] densityOut, @Nullable EditTable edits) {
            if (edits == null || edits.isEmpty()) return;
            int sy = Library.getSectionY(sectionKey);
            if ((edits.touchedSyMask & (1 << sy)) == 0) return;
            double[] addPocket = TL_ADD.get();
            double[] setPocket = TL_SET.get();
            boolean[] hasSetPocket = TL_HAS_SET.get();
            long[] bestSeq = TL_BEST_SET_SEQ.get();

            Arrays.fill(addPocket, 0, pocketCount, 0.0d);
            Arrays.fill(setPocket, 0, pocketCount, 0.0d);
            Arrays.fill(hasSetPocket, 0, pocketCount, false);
            Arrays.fill(bestSeq, 0, pocketCount, 0L);

            int e = edits.epoch;
            int n = edits.slotSize;
            int[] slots = edits.slots;
            int[] st = edits.stamps;
            long[] keys = edits.keys;
            double[] addAcc = edits.addAcc;
            double[] setV = edits.setVal;
            long[] setS = edits.setSeq;
            byte[] flags = edits.flags;

            for (int i = 0; i < n; i++) {
                int pos = slots[i];
                assert st[pos] == e;
                long k = keys[pos];
                if (stripLocal(k) != sectionKey) continue;
                int local = (int) ((k >>> 4) & 0xFFFL);
                int pi;
                if (pocketData == null) {
                    pi = 0;
                } else {
                    pi = readNibble(pocketData, local);
                    if (pi == NO_POCKET || pi >= pocketCount) continue;
                }
                double dAdd = addAcc[pos];
                if (dAdd != 0.0d) addPocket[pi] += dAdd;
                if ((flags[pos] & EditTable.HAS_SET) != 0) {
                    long seq = setS[pos];
                    if (!hasSetPocket[pi] || Long.compareUnsigned(seq, bestSeq[pi]) > 0) {
                        bestSeq[pi] = seq;
                        setPocket[pi] = setV[pos];
                        hasSetPocket[pi] = true;
                    }
                }
            }

            for (int p = 0; p < pocketCount; p++) {
                double base = hasSetPocket[p] ? setPocket[p] : densityOut[p];
                densityOut[p] = sanitize(base + addPocket[p]);
            }
        }

        double sanitize(double v) {
            if (Double.isNaN(v) || Math.abs(v) < RAD_EPSILON && v > minBound) return 0.0D;
            return Math.max(Math.min(v, RAD_MAX), minBound);
        }

        ChunkRef onChunkLoaded(int cx, int cz, Chunk chunk) {
            assert ((cx ^ (cx << 10) >> 10) | (cz ^ (cz << 10) >> 10)) == 0;
            long ck = ChunkPos.asLong(cx, cz);
            ChunkRef cr = chunkRefs.get(ck);
            if (cr == null) {
                cr = new ChunkRef(ck);
                chunkRefs.put(ck, cr);
            }
            cr.mcChunk = chunk;
            notifyNeighbours(cx, cz, cr);
            if (cr.parityIndex < 0) addLoadedToBucket(cr);
            else assert cr.parityBucket >= 0 && cr.parityBucket < 4;
            return cr;
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
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(tag, fx, fy, fz), new TargetPoint(world.provider.getDimension(), fx, fy, fz, 100));
                break;
            }
        }

        void ensureDirtyChunkRefCapacity(int need) {
            if (dirtyChunkRefsScratch.length >= need) return;
            int n = grow(dirtyChunkRefsScratch.length, need);
            dirtyChunkRefsScratch = Arrays.copyOf(dirtyChunkRefsScratch, n);
            if (dirtyChunkMasksScratchArr.length < n) {
                dirtyChunkMasksScratchArr = Arrays.copyOf(dirtyChunkMasksScratchArr, n);
            }
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
            removeLoadedFromBucket(cr);
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
            cr.dirtyFlag = false;
            for (int sy = 0; sy < 16; sy++) cr.setLane16(sy, (char) 0);
        }

        void removeChunkRef(long ck) {
            ChunkRef cr = chunkRefs.remove(ck);
            EditTable t = writes.remove(ck);
            if (t != null) editTablePool.recycle(t);
            if (cr == null) return;
            assert cr.mcChunk == null : "removeChunkRef called for loaded chunk; must unload first";
            assert cr.parityIndex < 0 : "Bucket membership leaked across unload/remove";
            cr.pending = null;
            for (int sy = 0; sy < 16; sy++) {
                SectionRef old = cr.sec[sy];
                if (old != null) pocketDataPool.recycle(old.pocketData);
            }
        }

        @ServerThread
        byte @Nullable [] tryEncodePayload(long ck, Chunk chunk) {
            BUF.clear();
            BUF.putShort((short) 0);
            int count = 0;
            EditTable edits = writes.get(ck);
            int touchedMask16 = (edits != null && !edits.isEmpty()) ? edits.touchedSyMask : 0;
            ChunkRef cr = chunkRefs.get(ck);
            PendingRad pr = (cr == null) ? null : cr.pending;
            if (cr == null && touchedMask16 == 0) return null;
            int dirtyMask16 = dirtyCk.getMask16(ck);
            ExtendedBlockStorage[] stor = chunk.getBlockStorageArray();
            long baseSck = Library.sectionToLong(ck, 0);
            byte[][] pOut = new byte[1][];
            double[] temp = TEMP_DENSITIES;
            double[] dens = TL_DENSITIES.get();
            double[] newMass = TL_NEW_MASS.get();
            int[] vols = TL_VOL_COUNTS.get();

            for (int sy = 0; sy < 16; sy++) {
                boolean touchedSy = (touchedMask16 & (1 << sy)) != 0;
                boolean dirtySy = (dirtyMask16 & (1 << sy)) != 0;
                boolean pendingSy = pr != null && pr.hasSy(sy);

                int kind = (cr == null) ? ChunkRef.KIND_NONE : cr.getKind(sy);
                assert !pendingSy || kind == ChunkRef.KIND_NONE;

                // No edits: pending-only is the cheapest possible path.
                if (!touchedSy && pendingSy) {
                    int m = pr.secMask[sy] & 0xFFFF;
                    while (m != 0) {
                        int pi = Integer.numberOfTrailingZeros(m);
                        m &= (m - 1);
                        long bits = pr.bits[(sy << 4) | pi];
                        if (bits == 0L) continue;
                        double v = sanitize(Double.longBitsToDouble(bits));
                        if (v == 0.0D) continue;
                        BUF.put((byte) ((sy << 4) | (pi & 15)));
                        BUF.putDouble(v);
                        count++;
                    }
                    continue;
                }

                // No edits and no in-memory section state.
                if (!touchedSy && kind == ChunkRef.KIND_NONE) continue;

                long sck = Library.setSectionY(baseSck, sy);

                // Dirty snapshot path (mapping may have changed; must serialize in current mapping space).
                // Only relevant if the section currently has some built state (kind != NONE).
                if (dirtySy && kind != ChunkRef.KIND_NONE) {
                    // Avoid flood-fill if we would emit nothing anyway.
                    if (!touchedSy) {
                        if (kind == ChunkRef.KIND_UNI) {
                            if (sanitize(cr.uniformRads[sy]) == 0.0D) continue;
                        } else {
                            SectionRef sc0 = cr.sec[sy];
                            if (sc0 == null || sc0.pocketCount <= 0) continue;
                            if (kind == ChunkRef.KIND_SINGLE) {
                                if (sanitize(((SingleMaskedSectionRef) sc0).rad) == 0.0D) continue;
                            } else {
                                MultiSectionRef m0 = (MultiSectionRef) sc0;
                                int oldCnt = Math.min(m0.pocketCount & 0xFF, NO_POCKET);
                                boolean any = false;
                                for (int p = 0; p < oldCnt; p++) {
                                    if (sanitize(m0.data[p << 1]) != 0.0D) {
                                        any = true;
                                        break;
                                    }
                                }
                                if (!any) continue;
                            }
                        }
                    }

                    pOut[0] = null;
                    int pCount = computePocketMappingForEncode(sck, stor[sy], pOut, vols);
                    byte[] pData = pOut[0];

                    if (pCount > 0) {
                        remapPocketMass(cr, sy, kind, cr.sec[sy], pCount, pData, vols, newMass);
                        for (int p = 0; p < pCount; p++) {
                            int v = (pData == null) ? SECTION_BLOCK_COUNT : Math.max(1, vols[p]);
                            dens[p] = sanitize(newMass[p] / (double) v);
                        }

                        if (touchedSy) applyQueuedWrites(sck, pData, pCount, dens, edits);

                        for (int p = 0; p < pCount; p++) {
                            double v = dens[p];
                            if (v == 0.0D) continue;
                            BUF.put((byte) ((sy << 4) | (p & 15)));
                            BUF.putDouble(v);
                            count++;
                        }
                    }

                    if (pData != null) pocketDataPool.recycle(pData);
                    continue;
                }

                // Untouched + clean: direct serialize current in-memory state (fast path).
                if (!touchedSy) {
                    if (kind == ChunkRef.KIND_UNI) {
                        double v = sanitize(cr.uniformRads[sy]);
                        if (v != 0.0D) {
                            BUF.put((byte) (sy << 4));
                            BUF.putDouble(v);
                            count++;
                        }
                        continue;
                    }

                    SectionRef sc = cr.sec[sy];
                    if (sc == null || sc.pocketCount <= 0) continue;

                    if (kind == ChunkRef.KIND_SINGLE) {
                        double v = sanitize(((SingleMaskedSectionRef) sc).rad);
                        if (v != 0.0D) {
                            BUF.put((byte) (sy << 4));
                            BUF.putDouble(v);
                            count++;
                        }
                        continue;
                    }

                    MultiSectionRef m = (MultiSectionRef) sc;
                    int pCount = m.pocketCount & 0xFF;
                    if (pCount > NO_POCKET) pCount = NO_POCKET;
                    double[] data = m.data;
                    for (int p = 0; p < pCount; p++) {
                        double v = sanitize(data[p << 1]);
                        if (v == 0.0D) continue;
                        BUF.put((byte) ((sy << 4) | (p & 15)));
                        BUF.putDouble(v);
                        count++;
                    }
                    continue;
                }

                // Touched sections: if we have no in-memory mapping (kind NONE), we must compute mapping to place edits.
                if (kind == ChunkRef.KIND_NONE) {
                    pOut[0] = null;
                    int pCount = computePocketMappingForEncode(sck, stor[sy], pOut, null);
                    byte[] pData = pOut[0];

                    if (pCount > 0) {
                        Arrays.fill(dens, 0, pCount, 0.0D);

                        if (pendingSy) {
                            for (int p = 0; p < pCount; p++) {
                                long bits = pr.getBits(sy, p);
                                if (bits != 0L) dens[p] = sanitize(Double.longBitsToDouble(bits));
                            }
                        }

                        applyQueuedWrites(sck, pData, pCount, dens, edits);

                        for (int p = 0; p < pCount; p++) {
                            double v = dens[p];
                            if (v == 0.0D) continue;
                            BUF.put((byte) ((sy << 4) | (p & 15)));
                            BUF.putDouble(v);
                            count++;
                        }
                    }

                    if (pData != null) pocketDataPool.recycle(pData);
                    continue;
                }

                // Touched + clean built state: reuse existing mapping and apply edits on a temp density buffer.
                int pCount;
                byte[] pData = null;

                if (kind == ChunkRef.KIND_UNI) {
                    temp[0] = cr.uniformRads[sy];
                    pCount = 1;
                } else {
                    SectionRef sc = cr.sec[sy];
                    if (sc == null || sc.pocketCount <= 0) continue;
                    pCount = sc.pocketCount & 0xFF;
                    if (pCount > NO_POCKET) pCount = NO_POCKET;
                    pData = sc.pocketData;

                    if (kind == ChunkRef.KIND_SINGLE) {
                        temp[0] = ((SingleMaskedSectionRef) sc).rad;
                    } else {
                        MultiSectionRef m = (MultiSectionRef) sc;
                        for (int p = 0; p < pCount; p++) temp[p] = m.data[p << 1];
                    }
                }

                applyQueuedWrites(sck, pData, pCount, temp, edits);

                for (int p = 0; p < pCount; p++) {
                    double v = sanitize(temp[p]);
                    if (v == 0.0D) continue;
                    BUF.put((byte) ((sy << 4) | (p & 15)));
                    BUF.putDouble(v);
                    count++;
                }
            }

            if (count == 0) return null;

            BUF.putShort(0, (short) count);
            BUF.flip();

            byte[] out = new byte[4 + BUF.limit()];
            out[0] = MAGIC_0;
            out[1] = MAGIC_1;
            out[2] = MAGIC_2;
            out[3] = FMT;
            BUF.get(out, 4, BUF.limit());
            return out;
        }

        int computePocketMappingForEncode(long sectionKey, @Nullable ExtendedBlockStorage ebs, byte[][] outPocketData, int @Nullable [] volsOut) {
            outPocketData[0] = null;
            if (ebs == null || ebs.isEmpty()) {
                if (volsOut != null) volsOut[0] = SECTION_BLOCK_COUNT;
                return 1;
            }
            SectionMask resistant = scanResistantMask(world, sectionKey, ebs);
            if (resistant == null || resistant.isEmpty()) {
                if (volsOut != null) volsOut[0] = SECTION_BLOCK_COUNT;
                return 1;
            }
            byte[] scratch = pocketDataPool.borrow();
            Arrays.fill(scratch, (byte) 0xFF);
            int[] queue = TL_FF_QUEUE.get();
            int pc = floodFillPockets(resistant, scratch, queue, volsOut, null, null, null);
            if (pc <= 0) {
                pocketDataPool.recycle(scratch);
                return 0;
            }
            outPocketData[0] = scratch;
            return pc;
        }

        void readPayload(int cx, int cz, byte[] raw) throws DecodeException {
            long ck = ChunkPos.asLong(cx, cz);
            ChunkRef owner = chunkRefs.get(ck);
            if (owner == null) {
                owner = new ChunkRef(ck);
                chunkRefs.put(ck, owner);
            } else {
                for (int sy = 0; sy < 16; sy++) {
                    SectionRef prev = owner.sec[sy];
                    if (prev != null) pocketDataPool.recycle(prev.pocketData);
                    owner.sec[sy] = null;
                }
                owner.clearAllMasks();
                owner.sectionKinds = 0;
                owner.pending = null;
            }
            dirtyCk.add(owner);
            ByteBuffer b = ByteBuffer.wrap(raw, 4, raw.length - 4);
            if (b.remaining() < 2) throw new DecodeException("truncated v6 header");
            int entryCount = b.getShort() & 0xFFFF;

            int need = entryCount * (1 + 8);
            if (b.remaining() < need)
                throw new DecodeException("truncated v6 payload: need=" + need + " rem=" + b.remaining());
            PendingRad pr = null;
            for (int i = 0; i < entryCount; i++) {
                int yz = b.get() & 0xFF;
                int sy = (yz >>> 4) & 15;
                int pi = yz & 15;

                double rad = sanitize(b.getDouble());
                if (rad == 0.0D) continue;
                if (pi == NO_POCKET) continue;
                if (pr == null) pr = new PendingRad();
                pr.put(sy, pi, Double.doubleToRawLongBits(rad));
            }
            owner.pending = pr;
        }

        void rebuildChunkPocketsLoaded(ChunkRef owner, long sectionKey, @Nullable ExtendedBlockStorage ebs, @Nullable EditTable edits) {
            int sy = Library.getSectionY(sectionKey);
            owner.setLane16(sy, (char) 0);

            int oldKind = owner.getKind(sy);
            SectionRef old = owner.sec[sy];
            PendingRad pr = owner.pending;
            assert (oldKind == ChunkRef.KIND_NONE || oldKind == ChunkRef.KIND_UNI) == (old == null);
            assert pr == null || !pr.hasSy(sy) || oldKind == ChunkRef.KIND_NONE;
            byte[] pocketData;

            int[] vols = TL_VOL_COUNTS.get();
            long[] sumX = TL_SUM_X.get();
            long[] sumY = TL_SUM_Y.get();
            long[] sumZ = TL_SUM_Z.get();

            byte[][] pOut = new byte[1][];
            int pocketCount = computePocketMappingForRebuild(sectionKey, ebs, pOut, vols, sumX, sumY, sumZ);
            pocketData = pOut[0]; // may be null iff uniform
            assert pocketCount >= 0 && pocketCount <= NO_POCKET;
            assert pocketData != null || pocketCount == 1;
            if (pocketCount == 0) {
                if (old != null) pocketDataPool.recycle(old.pocketData);
                owner.sec[sy] = null;
                owner.setKind(sy, ChunkRef.KIND_NONE);

                if (pr != null && pr.hasSy(sy)) {
                    pr.secMask[sy] = 0;
                    pr.nonEmptySyMask16 &= ~(1 << sy);
                    if (pr.isEmpty()) owner.pending = null;
                }
                return;
            }

            // For the SINGLE case we still need face counts; compute only when pocketCount==1 && pocketData!=null.
            int singleVolume0 = SECTION_BLOCK_COUNT;
            long singleFaceCounts = 0L;
            if (pocketCount == 1 && pocketData != null) {
                singleVolume0 = Math.max(1, vols[0]);
                for (int face = 0; face < 6; face++) {
                    int base = face << 8;
                    int c = 0;
                    for (int t = 0; t < 256; t++) {
                        int idx = FACE_PLANE[base + t];
                        if (readNibble(pocketData, idx) == 0) c++;
                    }
                    singleFaceCounts |= ((long) c & 0x1FFL) << (face * 9);
                }
            }

            // Transition mass: old state -> newMass in the coordinate system of the *current* pocket mapping.
            double[] newMass = TL_NEW_MASS.get();
            Arrays.fill(newMass, 0, pocketCount, 0.0d);

            if (oldKind != ChunkRef.KIND_NONE) {
                remapPocketMass(owner, sy, oldKind, old, pocketCount, pocketData, vols, newMass);
            }

            // Convert mass -> density (and sanitize immediately so rebuild without edits stays valid).
            double[] densities = TL_DENSITIES.get();
            for (int p = 0; p < pocketCount; p++) {
                int v = (pocketData == null) ? SECTION_BLOCK_COUNT : Math.max(1, vols[p]);
                double d = newMass[p] / (double) v;
                densities[p] = sanitize(d);
            }

            // Pending overrides: interpret pending pocket indices in the rebuilt mapping space (same as before).
            if (pr != null && pr.hasSy(sy)) {
                for (int p = 0; p < pocketCount; p++) {
                    long bits = pr.takeBits(sy, p);
                    if (bits == 0L) continue;
                    double v = Double.longBitsToDouble(bits);
                    densities[p] = sanitize(v);
                }
                pr.clearAbove(sy, pocketCount);
                if (pr.isEmpty()) owner.pending = null;
            }

            assert pr == null || !pr.hasSy(sy);

            // Apply queued writes on top (always sanitizes).
            applyQueuedWrites(sectionKey, pocketData, pocketCount, densities, edits);

            // Free old pocketData storage now that we’re done reading it.
            if (old != null) pocketDataPool.recycle(old.pocketData);

            // New is Uniform (no resistant blocks, or effectively uniform mapping).
            if (pocketCount == 1 && pocketData == null) {
                double d = densities[0];
                owner.uniformRads[sy] = d;
                owner.setKind(sy, ChunkRef.KIND_UNI);
                owner.sec[sy] = null;

                if (d != 0.0D) owner.setActive0(sy);
                return;
            }

            // New is Single Masked (resistant exists, but only one pocket).
            if (pocketCount == 1) {
                double density = densities[0];

                double inv = 1.0d / (double) singleVolume0;
                double cx = sumX[0] * inv;
                double cy = sumY[0] * inv;
                double cz = sumZ[0] * inv;

                SingleMaskedSectionRef masked = new SingleMaskedSectionRef(owner, sy, pocketData, singleVolume0, singleFaceCounts, cx, cy, cz);
                masked.rad = density;

                owner.sec[sy] = masked;
                owner.setKind(sy, ChunkRef.KIND_SINGLE);

                if (density != 0.0D) owner.setActive0(sy);
                return;
            }

            // New is Multi.
            double[] faceDists = new double[pocketCount * 6];
            for (int p = 0; p < pocketCount; p++) {
                int v = Math.max(1, vols[p]);
                double inv = 1.0d / (double) v;

                double cx = sumX[p] * inv;
                double cy = sumY[p] * inv;
                double cz = sumZ[p] * inv;

                int base = p * 6;
                faceDists[base] = cy + 0.5d;
                faceDists[base + 1] = 15.5d - cy;
                faceDists[base + 2] = cz + 0.5d;
                faceDists[base + 3] = 15.5d - cz;
                faceDists[base + 4] = cx + 0.5d;
                faceDists[base + 5] = 15.5d - cx;
            }

            MultiSectionRef sc = new MultiSectionRef(owner, sy, (byte) pocketCount, pocketData, faceDists);
            for (int p = 0; p < pocketCount; p++) {
                int v = Math.max(1, vols[p]);
                int i2 = p << 1;
                double d = densities[p];
                sc.data[i2] = d;
                sc.data[i2 + 1] = 1.0d / (double) v;
                sc.volume[p] = v;

                if (d != 0.0D) owner.setActiveBit(sy, p);
            }

            owner.sec[sy] = sc;
            owner.setKind(sy, ChunkRef.KIND_MULTI);
        }

        int computePocketMappingForRebuild(long sectionKey, @Nullable ExtendedBlockStorage ebs, byte[][] outPocketData, int[] vols, long[] sumX, long[] sumY, long[] sumZ) {
            outPocketData[0] = null;

            if (ebs == null || ebs.isEmpty()) {
                Arrays.fill(vols, 0);
                vols[0] = SECTION_BLOCK_COUNT;
                Arrays.fill(sumX, 0);
                Arrays.fill(sumY, 0);
                Arrays.fill(sumZ, 0);
                // sums not meaningful for uniform; caller shouldn’t use them unless pocketData != null
                return 1;
            }

            SectionMask resistant = scanResistantMask(world, sectionKey, ebs);
            if (resistant == null || resistant.isEmpty()) {
                Arrays.fill(vols, 0);
                vols[0] = SECTION_BLOCK_COUNT;
                Arrays.fill(sumX, 0);
                Arrays.fill(sumY, 0);
                Arrays.fill(sumZ, 0);
                return 1;
            }

            byte[] scratch = pocketDataPool.borrow();
            Arrays.fill(scratch, (byte) 0xFF);
            int[] queue = TL_FF_QUEUE.get();

            int pc = floodFillPockets(resistant, scratch, queue, vols, sumX, sumY, sumZ);
            if (pc <= 0) {
                pocketDataPool.recycle(scratch);
                return 0;
            }

            outPocketData[0] = scratch;
            return pc;
        }

        void relinkKeys(long[] dirtyKeys, int hi) {
            if (hi <= 0) return;
            // max 4 keys per dirty section: self + west + north + down(if sy!=0)
            ensureLinkScratch(hi << 2);
            long[] keys = linkScratch;
            int n = 0;
            for (int i = 0; i < hi; i++) {
                long k = dirtyKeys[i];
                int sy = Library.getSectionY(k);
                assert (sy & ~15) == 0;
                int yzBase = sy << 4;
                // E = 1, S = 2, U = 4
                keys[n++] = Library.setSectionY(k, yzBase | /*E|S|U*/ 7);
                keys[n++] = Library.setSectionY(Library.shiftSectionX(k, -1), yzBase | /*E*/ 1);
                keys[n++] = Library.setSectionY(Library.shiftSectionZ(k, -1), yzBase | /*S*/ 2);
                if (sy != 0) {
                    keys[n++] = Library.setSectionY(k, ((sy - 1) << 4) | /*U*/ 4);
                }
            }
            if (n == 0) return;
            if (n < 4096) LongArrays.radixSort(keys, 0, n);
            else LongArrays.parallelRadixSort(keys, 0, n);

            // unique by section coordinate (ignore low nibble), OR masks into low nibble
            int u = 0;
            for (int i = 0; i < n; i++) {
                long k = keys[i];
                long base = k & ~0xFL;
                int dm = (int) (k & 0xFL);
                if (u == 0) {
                    keys[u++] = base | (long) dm;
                    continue;
                }
                long prev = keys[u - 1];
                long prevBase = prev & ~0xFL;
                if (base != prevBase) {
                    keys[u++] = base | (long) dm;
                } else {
                    int prevDm = (int) (prev & 0xFL);
                    keys[u - 1] = prevBase | (long) (prevDm | dm);
                }
            }
            int threshold = getTaskThreshold(u, 256);
            if (u > 0) new LinkCanonicalKeysTask(keys, 0, u, threshold).invoke();
        }

        void ensureLinkScratch(int need) {
            long[] a = linkScratch;
            if (a.length >= need) return;
            int n = a.length;
            while (n < need) n = n + (n >>> 1) + 16;
            linkScratch = Arrays.copyOf(a, n);
        }

        void clearAllChunkRefs() {
            chunkRefs.clear();
            clearBuckets();
        }

        void notifyNeighbours(int cx, int cz, ChunkRef cr) {
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

        final class LinkCanonicalKeysTask extends RecursiveAction {
            final long[] keys;
            final int lo, hi, threshold;

            LinkCanonicalKeysTask(long[] keys, int lo, int hi, int threshold) {
                this.keys = keys;
                this.lo = lo;
                this.hi = hi;
                this.threshold = threshold;
            }

            @Override
            protected void compute() {
                int n = hi - lo;
                if (n <= threshold) {
                    work(lo, hi);
                    return;
                }
                int mid = (lo + hi) >>> 1;
                invokeAll(new LinkCanonicalKeysTask(keys, lo, mid, threshold), new LinkCanonicalKeysTask(keys, mid, hi, threshold));
            }

            void work(int start, int end) {
                long curCk = Long.MIN_VALUE;

                ChunkRef crA = null;
                int kindsA = 0;
                SectionRef[] secA = null;

                ChunkRef crE = null, crS = null;
                int kindsE = 0, kindsS = 0;
                SectionRef[] secE = null, secS = null;

                for (int i = start; i < end; i++) {
                    long k = keys[i];

                    int yz = Library.getSectionY(k);   // yz = (sy<<4) | dm
                    int sy = yz >>> 4;
                    int dm = yz & 15;
                    assert (sy & ~15) == 0;

                    long ck = Library.sectionToChunkLong(k);
                    if (ck != curCk) {
                        curCk = ck;

                        crA = chunkRefs.get(ck);
                        if (crA == null) {
                            secA = null;
                            continue;
                        }
                        assert crA.mcChunk != null;
                        kindsA = crA.sectionKinds;
                        secA = crA.sec;

                        crE = crA.east;
                        if (crE != null) {
                            assert crE.mcChunk != null;
                            kindsE = crE.sectionKinds;
                            secE = crE.sec;
                        } else {
                            secE = null;
                        }

                        crS = crA.south;
                        if (crS != null) {
                            assert crS.mcChunk != null;
                            kindsS = crS.sectionKinds;
                            secS = crS.sec;
                        } else {
                            secS = null;
                        }
                    }

                    if (secA == null) continue;

                    int shift = sy << 1;
                    int kA = (kindsA >>> shift) & 3;

                    // E direction (faceA = E=5, neighbor face to clear/link = W=4)
                    if ((dm & 1) != 0) {
                        if (kA == ChunkRef.KIND_NONE) {
                            if (secE != null) {
                                int kB = (kindsE >>> shift) & 3;
                                if (kB == ChunkRef.KIND_SINGLE || kB == ChunkRef.KIND_MULTI) {
                                    SectionRef b = secE[sy];
                                    assert b != null;
                                    b.clearFaceAllPockets(/*W*/ 4);
                                    if (kB == ChunkRef.KIND_MULTI)
                                        ((MultiSectionRef) b).markSentinelPlane16x16(/*W*/ 4);
                                }
                            }
                        } else if (kA == ChunkRef.KIND_UNI) {
                            if (secE != null) {
                                int kB = (kindsE >>> shift) & 3;
                                if (kB != ChunkRef.KIND_NONE && kB != ChunkRef.KIND_UNI) {
                                    SectionRef b = secE[sy];
                                    assert b != null;
                                    b.linkFaceToUniform(crA, /*W*/ 4);
                                }
                            }
                        } else {
                            SectionRef a = secA[sy];
                            assert a != null;
                            linkNonUniFace(a, kA, /*E*/ 5, crE, kindsE, secE, sy);
                        }
                    }

                    // S direction (faceA = S=3, neighbor face to clear/link = N=2)
                    if ((dm & 2) != 0) {
                        if (kA == ChunkRef.KIND_NONE) {
                            if (secS != null) {
                                int kB = (kindsS >>> shift) & 3;
                                if (kB == ChunkRef.KIND_SINGLE || kB == ChunkRef.KIND_MULTI) {
                                    SectionRef b = secS[sy];
                                    assert b != null;
                                    b.clearFaceAllPockets(/*N*/ 2);
                                    if (kB == ChunkRef.KIND_MULTI)
                                        ((MultiSectionRef) b).markSentinelPlane16x16(/*N*/ 2);
                                }
                            }
                        } else if (kA == ChunkRef.KIND_UNI) {
                            if (secS != null) {
                                int kB = (kindsS >>> shift) & 3;
                                if (kB != ChunkRef.KIND_NONE && kB != ChunkRef.KIND_UNI) {
                                    SectionRef b = secS[sy];
                                    assert b != null;
                                    b.linkFaceToUniform(crA, /*N*/ 2);
                                }
                            }
                        } else {
                            SectionRef a = secA[sy];
                            assert a != null;
                            linkNonUniFace(a, kA, /*S*/ 3, crS, kindsS, secS, sy);
                        }
                    }

                    // U direction (faceA = U=1, neighbor is sy+1 in same chunk, neighbor face to clear/link = D=0)
                    if ((dm & 4) != 0 && sy < 15) {
                        int shiftUp = (sy + 1) << 1;
                        int kB = (kindsA >>> shiftUp) & 3;
                        if (kA == ChunkRef.KIND_NONE) {
                            if (kB == ChunkRef.KIND_SINGLE || kB == ChunkRef.KIND_MULTI) {
                                SectionRef b = secA[sy + 1];
                                assert b != null;
                                b.clearFaceAllPockets(/*D*/ 0);
                                if (kB == ChunkRef.KIND_MULTI) ((MultiSectionRef) b).markSentinelPlane16x16(/*D*/ 0);
                            }
                        } else if (kA == ChunkRef.KIND_UNI) {
                            if (kB != ChunkRef.KIND_NONE && kB != ChunkRef.KIND_UNI) {
                                SectionRef b = secA[sy + 1];
                                assert b != null;
                                b.linkFaceToUniform(crA, /*D*/ 0);
                            }
                        } else {
                            SectionRef a = secA[sy];
                            assert a != null;
                            linkNonUniFace(a, kA, /*U*/ 1, crA, kindsA, secA, sy + 1);
                        }
                    }
                }
            }
        }

        final class PostSweepTask extends RecursiveAction {
            final ChunkRef[] chunks;
            final int lo, hi, threshold;

            PostSweepTask(ChunkRef[] chunks, int lo, int hi, int threshold) {
                this.chunks = chunks;
                this.lo = lo;
                this.hi = hi;
                this.threshold = threshold;
            }

            @Override
            protected void compute() {
                int n = hi - lo;
                if (n <= threshold) {
                    work(lo, hi);
                    return;
                }
                int mid = (lo + hi) >>> 1;
                var left = new PostSweepTask(chunks, lo, mid, threshold);
                var right = new PostSweepTask(chunks, mid, hi, threshold);
                left.fork();
                right.compute();
                left.join();
            }

            void work(int start, int end) {
                for (int i = start; i < end; i++) {
                    ChunkRef cr = chunks[i];
                    assert cr != null;
                    assert cr.mcChunk != null : "Bucket contains unloaded chunk";
                    assert cr.parityIndex >= 0;
                    boolean dirty = cr.dirtyFlag;
                    long baseSck = Library.sectionToLong(cr.ck, 0);
                    for (int sy = 0; sy < 16; sy++) {
                        int lane = cr.lane16(sy);
                        if (lane == 0) continue;

                        int kind = cr.getKind(sy);
                        assert kind != ChunkRef.KIND_NONE : "Active bit set for KIND_NONE section";

                        // Build section key once per section, used only if fog/destroy triggers.
                        long sck = Library.setSectionY(baseSck, sy);

                        if (kind == ChunkRef.KIND_UNI) {
                            assert (lane & ~1) == 0 : "UNI must only use pocket 0";

                            double prev = cr.uniformRads[sy];
                            double next = sanitize(prev * retentionDt);
                            if (next != prev) {
                                cr.uniformRads[sy] = next;
                                dirty = true;
                            }
                            if (next == 0.0D) {
                                cr.clearActive0(sy);
                                continue;
                            }

                            long pk = pocketKey(sck, 0);
                            long seed = 0L;

                            if (fogProbU64 != 0L && next > RadiationConfig.fogRad) {
                                seed = HashCommon.mix(pk ^ workEpochSalt);
                                if (Long.compareUnsigned(seed, fogProbU64) < 0) {
                                    spawnFog(null, 0, sy, cr.mcChunk, seed);
                                }
                            }

                            if (next >= 5.0D && pk != Long.MIN_VALUE) {
                                if (seed == 0L) seed = HashCommon.mix(pk ^ workEpochSalt);
                                if (Long.compareUnsigned(HashCommon.mix(seed + 0xD1B54A32D192ED03L), DESTROY_PROB_U64) < 0) {
                                    if (tickDelay == 1) pocketToDestroy = pk;
                                    else destructionQueue.offer(pk);
                                }
                            }
                            continue;
                        }

                        SectionRef sc = cr.sec[sy];
                        assert sc != null : "KIND_SINGLE/MULTI requires non-null SectionRef";

                        if (kind == ChunkRef.KIND_SINGLE) {
                            assert (lane & ~1) == 0 : "SINGLE must only use pocket 0";

                            SingleMaskedSectionRef single = (SingleMaskedSectionRef) sc;
                            double prev = single.rad;
                            double next = sanitize(prev * retentionDt);
                            if (next != prev) {
                                single.rad = next;
                                dirty = true;
                            }
                            if (next == 0.0D) {
                                cr.clearActive0(sy);
                                continue;
                            }

                            long pk = pocketKey(sck, 0);
                            long seed = 0L;

                            if (fogProbU64 != 0L && next > RadiationConfig.fogRad) {
                                seed = HashCommon.mix(pk ^ workEpochSalt);
                                if (Long.compareUnsigned(seed, fogProbU64) < 0) {
                                    spawnFog(sc, 0, sy, cr.mcChunk, seed);
                                }
                            }

                            if (next >= 5.0D && pk != Long.MIN_VALUE) {
                                if (seed == 0L) seed = HashCommon.mix(pk ^ workEpochSalt);
                                if (Long.compareUnsigned(HashCommon.mix(seed + 0xD1B54A32D192ED03L), DESTROY_PROB_U64) < 0) {
                                    if (tickDelay == 1) pocketToDestroy = pk;
                                    else destructionQueue.offer(pk);
                                }
                            }
                            continue;
                        }

                        // MULTI
                        MultiSectionRef multi = (MultiSectionRef) sc;
                        int pCount = multi.pocketCount & 0xFF;
                        int bits = lane;

                        while (bits != 0) {
                            int pi = Integer.numberOfTrailingZeros(bits);
                            bits &= (bits - 1);

                            assert pi < pCount : "Active bit pi out of range for Multi pocketCount";

                            int idx = pi << 1;
                            double prev = multi.data[idx];
                            double next = sanitize(prev * retentionDt);
                            if (next != prev) {
                                multi.data[idx] = next;
                                dirty = true;
                            }
                            if (next == 0.0D) {
                                cr.clearActiveBit(sy, pi);
                                continue;
                            }

                            long pk = pocketKey(sck, pi);
                            long seed = 0L;

                            if (fogProbU64 != 0L && next > RadiationConfig.fogRad) {
                                seed = HashCommon.mix(pk ^ workEpochSalt);
                                if (Long.compareUnsigned(seed, fogProbU64) < 0) {
                                    spawnFog(sc, pi, sy, cr.mcChunk, seed);
                                }
                            }

                            if (next >= 5.0D && pk != Long.MIN_VALUE) {
                                if (seed == 0L) seed = HashCommon.mix(pk ^ workEpochSalt);
                                if (Long.compareUnsigned(HashCommon.mix(seed + 0xD1B54A32D192ED03L), DESTROY_PROB_U64) < 0) {
                                    if (tickDelay == 1) pocketToDestroy = pk;
                                    else destructionQueue.offer(pk);
                                }
                            }
                        }
                    }
                    cr.dirtyFlag = false;
                    if (dirty) cr.mcChunk.markDirty();
                }
            }
        }

        final class RebuildDirtyChunkBatchTask extends RecursiveAction {
            final ChunkRef[] refs;
            final int[] masks16;
            final int lo, hi, threshold;

            RebuildDirtyChunkBatchTask(ChunkRef[] refs, int[] masks16, int lo, int hi, int threshold) {
                this.refs = refs;
                this.masks16 = masks16;
                this.lo = lo;
                this.hi = hi;
                this.threshold = threshold;
            }

            @Override
            protected void compute() {
                if (hi - lo <= threshold) {
                    for (int i = lo; i < hi; i++) {
                        ChunkRef cr = refs[i];
                        Chunk chunk = cr.mcChunk;
                        if (chunk == null) continue;
                        long ck = cr.ck;
                        EditTable edits = writes.get(ck);
                        ExtendedBlockStorage[] stor = chunk.getBlockStorageArray();
                        int m = masks16[i];
                        while (m != 0) {
                            int sy = Integer.numberOfTrailingZeros(m);
                            m &= (m - 1);
                            long sck = Library.sectionToLong(chunk.x, sy, chunk.z);
                            rebuildChunkPocketsLoaded(cr, sck, stor[sy], edits);
                        }
                    }
                    return;
                }

                int mid = (lo + hi) >>> 1;
                var left = new RebuildDirtyChunkBatchTask(refs, masks16, lo, mid, threshold);
                var right = new RebuildDirtyChunkBatchTask(refs, masks16, mid, hi, threshold);
                left.fork();
                right.compute();
                left.join();
            }
        }

    }


    static final class DirtyChunkTracker {
        static final float LOAD_FACTOR = 0.6f;
        long[] keys;// ck
        ChunkRef[] refs;// may contain unloaded chunks, for them mcChunk == null
        char[] masks16;// 16-bit section mask per ck (unsigned)
        int[] stamps;
        int[] slots;// indices into table for iteration (no re-probing)
        int mask, size, epoch, slotSize;

        DirtyChunkTracker(int expectedChunks) {
            int cap = HashCommon.nextPowerOfTwo(Math.max(16, (int) (expectedChunks / LOAD_FACTOR) + 1));
            keys = new long[cap];
            refs = new ChunkRef[cap];
            masks16 = new char[cap];
            stamps = new int[cap];
            Arrays.fill(keys, Long.MIN_VALUE);
            mask = cap - 1;
            slots = new int[Math.max(16, expectedChunks)];
            epoch = 1;
        }

        void add(ChunkRef cr, int sy) {
            int bit = 1 << sy;
            if (size + 1 > (int) (keys.length * LOAD_FACTOR))
                rehash(HashCommon.nextPowerOfTwo(keys.length + (keys.length >>> 1) + 16));
            long ck = cr.ck;
            int pos = SectionKeyHash.hash(ck) & mask;
            while (true) {
                if (stamps[pos] != epoch) {
                    stamps[pos] = epoch;
                    keys[pos] = ck;
                    refs[pos] = cr;
                    masks16[pos] = (char) bit;
                    size++;
                    int i = slotSize;
                    if (i == slots.length) slots = Arrays.copyOf(slots, slots.length + (slots.length >>> 1) + 16);
                    slots[i] = pos;
                    slotSize = i + 1;
                    return;
                }
                if (keys[pos] == ck) {
                    masks16[pos] |= (char) bit;
                    refs[pos] = cr;
                    return;
                }
                pos = (pos + 1) & mask;
            }
        }

        void add(ChunkRef cr) {
            if (size + 1 > (int) (keys.length * LOAD_FACTOR))
                rehash(HashCommon.nextPowerOfTwo(keys.length + (keys.length >>> 1) + 16));
            long ck = cr.ck;
            int pos = SectionKeyHash.hash(ck) & mask;
            while (true) {
                if (stamps[pos] != epoch) {
                    stamps[pos] = epoch;
                    keys[pos] = ck;
                    refs[pos] = cr;
                    masks16[pos] = (char) 0xFFFF;
                    size++;
                    int i = slotSize;
                    if (i == slots.length) slots = Arrays.copyOf(slots, slots.length + (slots.length >>> 1) + 16);
                    slots[i] = pos;
                    slotSize = i + 1;
                    return;
                }
                if (keys[pos] == ck) {
                    masks16[pos] = (char) 0xFFFF;
                    refs[pos] = cr;
                    return;
                }
                pos = (pos + 1) & mask;
            }
        }

        int getMask16(long ck) {
            int pos = SectionKeyHash.hash(ck) & mask;
            int e = epoch;
            while (true) {
                if (stamps[pos] != e) return 0;
                if (keys[pos] == ck) return masks16[pos] & 0xFFFF;
                pos = (pos + 1) & mask;
            }
        }

        boolean isDirty(ChunkRef cr, int sy) {
            int bit = 1 << sy;
            long ck = cr.ck;
            int pos = SectionKeyHash.hash(ck) & mask;
            while (true) {
                if (stamps[pos] != epoch) return false;
                if (keys[pos] == ck) return (masks16[pos] & bit) != 0;
                pos = (pos + 1) & mask;
            }
        }

        void reset() {
            size = 0;
            slotSize = 0;

            int e = epoch + 1;
            if (e == 0) {
                Arrays.fill(stamps, 0);
                e = 1;
            }
            epoch = e;
        }

        void clearAll() {
            Arrays.fill(keys, Long.MIN_VALUE);
            Arrays.fill(refs, null);
            Arrays.fill(masks16, (char) 0);
            Arrays.fill(stamps, 0);
            size = 0;
            slotSize = 0;
            epoch = 1;
        }

        void rehash(int newCap) {
            long[] newKeys = new long[newCap];
            ChunkRef[] newRefs = new ChunkRef[newCap];
            char[] newMasks = new char[newCap];
            int[] newStamps = new int[newCap];
            Arrays.fill(newKeys, Long.MIN_VALUE);
            int newMask = newCap - 1;

            int[] newSlots = new int[Math.max(slots.length, slotSize)];
            int ns = 0;

            for (int i = 0; i < slotSize; i++) {
                int oldPos = slots[i];
                if (stamps[oldPos] != epoch) continue;
                long ck = keys[oldPos];
                char m16 = masks16[oldPos];
                ChunkRef cr = refs[oldPos];

                int pos = SectionKeyHash.hash(ck) & newMask;
                while (newStamps[pos] == epoch) pos = (pos + 1) & newMask;

                newStamps[pos] = epoch;
                newKeys[pos] = ck;
                newRefs[pos] = cr;
                newMasks[pos] = m16;
                newSlots[ns++] = pos;
            }

            keys = newKeys;
            refs = newRefs;
            masks16 = newMasks;
            stamps = newStamps;
            mask = newMask;
            slots = newSlots;
            slotSize = ns;
            size = ns;
        }
    }

    static final class EditTable {
        static final float LOAD_FACTOR = 0.6f;
        static final byte HAS_SET = 1;

        long[] keys, setSeq;
        double[] addAcc, setVal;
        int[] stamps, slots;
        byte[] flags;
        int touchedSyMask, mask, size, epoch, slotSize;

        EditTable(int cap) {
            keys = new long[cap];
            addAcc = new double[cap];
            setVal = new double[cap];
            setSeq = new long[cap];
            flags = new byte[cap];
            stamps = new int[cap];
            slots = new int[16];
            mask = cap - 1;
            epoch = 1;
            touchedSyMask = 0;
        }

        boolean isEmpty() {
            return slotSize == 0;
        }

        void clear() {
            size = 0;
            slotSize = 0;
            int e = epoch + 1;
            if (e == 0) {
                Arrays.fill(stamps, 0);
                e = 1;
            }
            epoch = e;
            touchedSyMask = 0;
        }

        void ensureCapacityForAdd() {
            if (size + 1 <= (int) (keys.length * LOAD_FACTOR)) return;
            rehash(HashCommon.nextPowerOfTwo(keys.length + (keys.length >>> 1) + 16));
        }

        int findOrInsert(long k) {
            ensureCapacityForAdd();
            int pos = SectionKeyHash.hash(k) & mask;
            while (true) {
                if (stamps[pos] != epoch) {
                    stamps[pos] = epoch;
                    keys[pos] = k;
                    addAcc[pos] = 0.0d;
                    setVal[pos] = 0.0d;
                    setSeq[pos] = 0L;
                    flags[pos] = 0;
                    size++;
                    int i = slotSize;
                    if (i == slots.length) slots = Arrays.copyOf(slots, slots.length + (slots.length >>> 1) + 16);
                    slots[i] = pos;
                    slotSize = i + 1;
                    return pos;
                }
                if (keys[pos] == k) return pos;
                pos = (pos + 1) & mask;
            }
        }

        void putSet(long k, double v, long seq) {
            int pos = findOrInsert(k);
            flags[pos] |= HAS_SET;
            setVal[pos] = v;
            setSeq[pos] = seq;
            addAcc[pos] = 0.0d;
            touchedSyMask |= 1 << ((int) k & 15);
        }

        void addTo(long k, double dv) {
            int pos = findOrInsert(k);
            // SET is considered admin command, overwrite
            if ((flags[pos] & HAS_SET) != 0) return;
            addAcc[pos] += dv;
            touchedSyMask |= 1 << ((int) k & 15);
        }

        void rehash(int newCap) {
            long[] newKeys = new long[newCap];
            double[] newAdd = new double[newCap];
            double[] newSetV = new double[newCap];
            long[] newSetS = new long[newCap];
            byte[] newFlags = new byte[newCap];
            int[] newStamps = new int[newCap];

            int newMask = newCap - 1;
            int[] newSlots = new int[Math.max(slots.length, slotSize)];
            int ns = 0;

            int e = epoch;
            for (int i = 0; i < slotSize; i++) {
                int oldPos = slots[i];
                if (stamps[oldPos] != e) continue;
                long k = keys[oldPos];

                int pos = SectionKeyHash.hash(k) & newMask;
                while (newStamps[pos] == e) pos = (pos + 1) & newMask;

                newStamps[pos] = e;
                newKeys[pos] = k;
                newAdd[pos] = addAcc[oldPos];
                newSetV[pos] = setVal[oldPos];
                newSetS[pos] = setSeq[oldPos];
                newFlags[pos] = flags[oldPos];
                newSlots[ns++] = pos;
            }

            keys = newKeys;
            addAcc = newAdd;
            setVal = newSetV;
            setSeq = newSetS;
            flags = newFlags;
            stamps = newStamps;
            mask = newMask;
            slots = newSlots;
            slotSize = ns;
            size = ns;
        }
    }
}
