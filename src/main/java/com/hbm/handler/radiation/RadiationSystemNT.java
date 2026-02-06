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
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
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

    static final int MAX_POCKETS = 2048;
    static final short NO_POCKET = -1;
    static final int KIND_NONE = 0, KIND_UNI = 1, KIND_SINGLE = 2, KIND_MULTI = 3;
    static final int CHUNK_META_DIRTY_SHIFT = 48;
    static final long CHUNK_META_KINDS_MASK = 0xFFFF_FFFFL;
    static final long CHUNK_META_ACTIVE_MASK = 0xFFFFL << 32;
    static final long CHUNK_META_DIRTY_MASK = 1L << CHUNK_META_DIRTY_SHIFT;

    static final int[] FACE_DX = {0, 0, 0, 0, -1, 1}, FACE_DY = {-1, 1, 0, 0, 0, 0}, FACE_DZ = {0, 0, -1, 1, 0, 0};
    static final int[] FACE_PLANE = new int[6 * 256];
    static final int SECTION_BLOCK_COUNT = 4096;
    static final ConcurrentMap<WorldServer, WorldRadiationData> worldMap = new ConcurrentHashMap<>(4);
    static final int[] BOUNDARY_MASKS = {0, 0, 0xF00, 0xF00, 0xFF0, 0xFF0}, LINEAR_OFFSETS = {-256, 256, -16, 16, -1, 1};
    static final int PROFILE_WINDOW = 200;
    static final String TAG_RAD = "hbmRadDataNT";
    static final byte MAGIC_0 = (byte) 'N', MAGIC_1 = (byte) 'T', MAGIC_2 = (byte) 'X', FMT_V6 = 6, FMT = 7;
    static final Object NOT_RES = new Object();
    static final ForkJoinPool RAD_POOL = ForkJoinPool.commonPool();
    static final int TARGET_TASK_CNT = RAD_POOL.getParallelism() << 2;

    /** Also used as the overlaps scratch in {@code remapPocketMass}: dead after flood-fill, before remap. */
    static final ThreadLocal<int[]> TL_FF_QUEUE = ThreadLocal.withInitial(() -> new int[SECTION_BLOCK_COUNT]);
    static final ThreadLocal<PalScratch> TL_PAL_SCRATCH = ThreadLocal.withInitial(PalScratch::new);
    static final ThreadLocal<int[]> TL_VOL_COUNTS = ThreadLocal.withInitial(() -> new int[MAX_POCKETS]);
    static final ThreadLocal<double[]> TL_NEW_MASS = ThreadLocal.withInitial(() -> new double[MAX_POCKETS]);
    /** Strided x,y,z layout: {@code [p*3]=x, [p*3+1]=y, [p*3+2]=z}. */
    static final ThreadLocal<long[]> TL_SUM_XYZ = ThreadLocal.withInitial(() -> new long[MAX_POCKETS * 3]);
    /** Also used as old-mass scratch inside {@code remapPocketMass}: dead before densities are written. */
    static final ThreadLocal<double[]> TL_DENSITIES = ThreadLocal.withInitial(() -> new double[MAX_POCKETS]);

    static final ThreadLocal<double[]> TL_ADD = ThreadLocal.withInitial(() -> new double[MAX_POCKETS + 1]);
    static final ThreadLocal<double[]> TL_SET = ThreadLocal.withInitial(() -> new double[MAX_POCKETS + 1]);
    /** Seq 0 = no set queued */
    static final ThreadLocal<long[]> TL_BEST_SET_SEQ = ThreadLocal.withInitial(() -> new long[MAX_POCKETS + 1]);
    /** Must be cleaned after use! */
    static final ThreadLocal<int[]> TL_TEMP_ARRAY = ThreadLocal.withInitial(() -> new int[MAX_POCKETS]);
    static final ThreadLocal<int[]> TL_TOUCHED = ThreadLocal.withInitial(() -> new int[512]);
    static final ThreadLocal<Long2IntOpenHashMap> TL_EDGE_COUNTS = ThreadLocal.withInitial(() -> {
        Long2IntOpenHashMap map = new Long2IntOpenHashMap(512);
        map.defaultReturnValue(0);
        return map;
    });
    static final ByteBuffer BUF = ByteBuffer.allocateDirect(65536 * 10 + 4);

    static final double RAD_EPSILON = 1.0e-5D;
    static final double RAD_MAX = Double.MAX_VALUE / 2.0D;
    static final double[] TEMP_DENSITIES = new double[MAX_POCKETS];
    static final long DESTROY_PROB_U64 = Long.divideUnsigned(-1L, 100L);

    static long fogProbU64;
    static long ticks;
    static @NotNull CompletableFuture<Void> radiationFuture = CompletableFuture.completedFuture(null);
    static Object[] STATE_CLASS;
    static int tickDelay = 1;
    static double dT = tickDelay / 20.0D;
    static double diffusionDt = 10.0 * dT;
    static double UU_E = Math.exp(-(diffusionDt / 128.0d));
    static double retentionDt = Math.pow(0.99424, dT);

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
            int id = data.onChunkLoaded(cx, cz, chunk);
            data.dirtyCk.add(ChunkPos.asLong(cx, cz), id);
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
        int ownerId = data.onChunkLoaded(chunk.x, chunk.z, chunk);
        int sy = Library.getSectionY(sck);
        int secIdx = (ownerId << 4) | sy;
        // unreachable if the section is truly resistant(filtered by isResistantAt)
        long meta = data.chunkMeta[ownerId];
        int kind = (int) ((meta >>> (sy << 1)) & 3);

        if (kind == KIND_NONE || data.dirtyCk.isDirty(ck, sy)) {
            int local = Library.blockPosToLocal(posLong);
            data.queueAdd(sck, local, amount);
            if (kind == KIND_NONE) data.dirtyCk.add(ck, ownerId, sy);
            chunk.markDirty();
            return;
        }

        int pocketIndex;
        if (kind == KIND_UNI) {
            pocketIndex = 0;
        } else {
            WorldRadiationData.SectionRef sec = data.complexSecs[secIdx];
            pocketIndex = sec.getPocketIndex(posLong);
        }
        if (pocketIndex < 0) return;

        double current;
        if (kind < KIND_MULTI) {
            current = data.uniformRads[secIdx];
        } else {
            WorldRadiationData.SectionRef sec = data.complexSecs[secIdx];
            current = ((WorldRadiationData.MultiSectionRef) sec).data[pocketIndex << 1];
        }

        if (current >= max) return;
        double next = current + amount;
        if (next > max) next = max;
        next = data.sanitize(next);

        if (next != current) {
            if (kind < KIND_MULTI) {
                data.uniformRads[secIdx] = next;
            } else {
                WorldRadiationData.SectionRef sec = data.complexSecs[secIdx];
                ((WorldRadiationData.MultiSectionRef) sec).data[pocketIndex << 1] = next;
            }
            if (next != 0.0D) data.chunkMeta[ownerId] |= (1L << (32 + sy));
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
        int ownerId = data.onChunkLoaded(chunk.x, chunk.z, chunk);
        int sy = Library.getSectionY(sck);
        int secIdx = (ownerId << 4) | sy;

        long meta = data.chunkMeta[ownerId];
        int kind = (int) ((meta >>> (sy << 1)) & 3);

        if (kind == KIND_NONE || data.dirtyCk.isDirty(ck, sy)) {
            int local = Library.blockPosToLocal(posLong);
            data.queueAdd(sck, local, -amount);
            if (kind == KIND_NONE) data.dirtyCk.add(ck, ownerId, sy);
            chunk.markDirty();
            return;
        }

        WorldRadiationData.SectionRef ref = data.complexSecs[secIdx];
        int pocketIndex = (kind == KIND_UNI) ? 0 : ref.getPocketIndex(posLong);
        if (pocketIndex < 0) return;

        double current;
        if (kind < KIND_MULTI) current = data.uniformRads[secIdx];
        else current = ((WorldRadiationData.MultiSectionRef) ref).data[pocketIndex << 1];

        if (current == 0.0D && data.minBound == 0.0D) return;
        double next = data.sanitize(current - amount);

        if (kind < KIND_MULTI) data.uniformRads[secIdx] = next;
        else ((WorldRadiationData.MultiSectionRef) ref).data[pocketIndex << 1] = next;

        chunk.markDirty(); // Note: Sleeping active status is naturally relegated to PostSweepTask.
    }

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
        int ownerId = data.onChunkLoaded(chunk.x, chunk.z, chunk);
        int sy = Library.getSectionY(sck);
        int secIdx = (ownerId << 4) | sy;

        long meta = data.chunkMeta[ownerId];
        int kind = (int) ((meta >>> (sy << 1)) & 3);

        if (kind == KIND_NONE || data.dirtyCk.isDirty(ck, sy)) {
            int local = Library.blockPosToLocal(posLong);
            data.queueSet(sck, local, amount);
            if (kind == KIND_NONE) data.dirtyCk.add(ck, ownerId, sy);
            chunk.markDirty();
            return;
        }

        int pocketIndex;
        if (kind == KIND_UNI) {
            pocketIndex = 0;
        } else {
            WorldRadiationData.SectionRef sec = data.complexSecs[secIdx];
            pocketIndex = sec.getPocketIndex(posLong);
        }
        if (pocketIndex < 0) return;

        double v = data.sanitize(amount);
        if (kind < KIND_MULTI) data.uniformRads[secIdx] = v;
        else {
            WorldRadiationData.SectionRef sec = data.complexSecs[secIdx];
            ((WorldRadiationData.MultiSectionRef) sec).data[pocketIndex << 1] = v;
        }

        if (v != 0.0D) data.chunkMeta[ownerId] |= (1L << (32 + sy));
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
        int ownerId = data.getId(ck);
        if (ownerId < 0) return 0D;
        if (ownerId >= data.cks.length || data.cks[ownerId] != ck) return 0D;

        int secIdx = (ownerId << 4) | sy;
        long meta = data.chunkMeta[ownerId];
        int kind = (int) ((meta >>> (sy << 1)) & 3);

        if (kind == KIND_NONE) {
            data.dirtyCk.add(ck, ownerId, sy);
            return 0D;
        }
        if (kind == KIND_UNI) return data.uniformRads[secIdx];

        WorldRadiationData.SectionRef sc = data.complexSecs[secIdx];
        if (sc == null || sc.pocketCount <= 0) {
            data.dirtyCk.add(ck, ownerId, sy);
            return 0D;
        }
        int pocketIndex = sc.getPocketIndex(posLong);
        if (pocketIndex < 0) {
            data.dirtyCk.add(ck, ownerId, sy);
            return 0D;
        }
        if (kind == KIND_SINGLE) {
            return data.uniformRads[secIdx];
        } else {
            return ((WorldRadiationData.MultiSectionRef) sc).data[pocketIndex << 1];
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
        int id = data.onChunkLoaded(chunk.x, chunk.z, chunk);
        data.dirtyCk.add(ck, id, sy);
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
            int id = data.onChunkLoaded(chunk.x, chunk.z, chunk);
            data.dirtyCk.add(ck, id, sy);
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
        int sy = (yz >>> 11) & 15;
        int cz = Library.getSectionZ(pocketKey);
        int targetPocketIndex = yz & 0x7FF;

        long ck = Library.sectionToChunkLong(pocketKey);
        int ownerId = data.getId(ck);
        if (ownerId < 0) return;
        Chunk mcChunk = data.mcChunks[ownerId];
        if (mcChunk == null) return;

        int secIdx = (ownerId << 4) | sy;
        long meta = data.chunkMeta[ownerId];
        int kind = (int) ((meta >>> (sy << 1)) & 3);
        if (kind == KIND_NONE) return;

        int baseX = cx << 4;
        int baseY = sy << 4;
        int baseZ = cz << 4;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        ExtendedBlockStorage storage = mcChunk.getBlockStorageArray()[sy];
        if (storage == null || storage.isEmpty()) return;
        BlockStateContainer container = storage.data;

        if (kind == KIND_UNI) {
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

        WorldRadiationData.SectionRef sc = data.complexSecs[secIdx];
        if (sc == null) return;

        for (int i = 0; i < SECTION_BLOCK_COUNT; i++) {
            if (world.rand.nextInt(3) != 0) continue;
            int actualPocketIndex = sc.paletteIndexOrNeg(i);
            if (actualPocketIndex < 0 || actualPocketIndex != targetPocketIndex) continue;
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
                MainRegistry.logger.error("Error in async rad simulation in dimension {} ({})",
                        String.valueOf(p.getDimension()), p.getDimensionType().getName(), t);
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
                        MainRegistry.logger.error("Error in async rad simulation in dimension {} ({})",
                                String.valueOf(p.getDimension()), p.getDimensionType().getName(), t);
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
            MainRegistry.logger.info("[RadiationSystemNT] dim {} ({}) lifetime: steps={}, avg={} ms, max={} ms", dimId,
                    dimType, steps, r3(avgMs), r3(maxMs));
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
        MainRegistry.logger.info(
                "[RadiationSystemNT] dim {} ({}) lifetime: steps={}, avg={} ms, 1% high(avg)={} ms, 0.1% high(avg)={} ms, p99={} ms, p999={} ms, max={} ms (sampleN={})",
                dimId, dimType, steps, r3(avgMs), r3(onePctHighAvg), r3(pointOnePctHigh), r3(p99), r3(p999), r3(maxMs),
                n);
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
                MainRegistry.logger.warn(
                        "[RadiationSystemNT] Skipped legacy radiation data for chunk {} in dimension {} ({})",
                        chunk.getPos(), String.valueOf(p.getDimension()), p.getDimensionType().getName());
            } else if (id == Constants.NBT.TAG_BYTE_ARRAY) {
                byte[] raw = nbt.getByteArray(TAG_RAD);
                payload = verifyPayload(raw);
            }
            if (payload == null || payload.length == 0) return;
            data.readPayload(cx, cz, payload);
        } catch (BufferUnderflowException | DecodeException ex) {
            var p = e.getWorld().provider;
            MainRegistry.logger.error("[RadiationSystemNT] Failed to decode data for chunk {} in dimension {} ({})",
                    chunk.getPos(), String.valueOf(p.getDimension()), p.getDimensionType().getName(), ex);
            nbt.removeTag(TAG_RAD);
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load e) {
        if (!GeneralConfig.enableRads || !GeneralConfig.advancedRadiation) return;
        if (e.getWorld().isRemote) return;
        Chunk chunk = e.getChunk();
        int cx = chunk.x, cz = chunk.z;
        if (((cx ^ (cx << 10) >> 10) | (cz ^ (cz << 10) >> 10)) != 0) return;
        WorldRadiationData data = getWorldRadData((WorldServer) e.getWorld());
        int id = data.onChunkLoaded(cx, cz, chunk);
        data.dirtyCk.add(ChunkPos.asLong(cx, cz), id);
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
        if (fmt != FMT && fmt != FMT_V6) throw new DecodeException("Unknown format: " + fmt);
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
        int sy = Library.getSectionY(sectionKey) & 15;
        int yz = (sy << 11) | (pocketIndex & 0x7FF);
        return Library.setSectionY(sectionKey, yz);
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
            Object[] byId = map.byId;

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

    static boolean exchangeUniExactXZ(double[] uni, int idxA, int idxB) {
        double ra = uni[idxA], rb = uni[idxB];
        if (ra == rb) return false;
        double avg = 0.5d * (ra + rb);
        double delta = 0.5d * (ra - rb) * UU_E;
        uni[idxA] = avg + delta;
        uni[idxB] = avg - delta;
        return true;
    }

    static boolean exchangeUniExactY(double[] uni, int idxA, int idxB) {
        double ra = uni[idxA];
        double rb = uni[idxB];
        if (ra == rb) return false;
        double avg = 0.5d * (ra + rb);
        double delta = 0.5d * (ra - rb) * UU_E;
        uni[idxA] = avg + delta;
        uni[idxB] = avg - delta;
        return true;
    }

    static final class PendingRad {
        static final int POCKETS_PER_SY = MAX_POCKETS;
        final Int2LongOpenHashMap[] bySy = new Int2LongOpenHashMap[16];
        int nonEmptySyMask16;

        boolean hasSy(int sy) {
            return (nonEmptySyMask16 & (1 << sy)) != 0;
        }

        boolean isEmpty() {
            return nonEmptySyMask16 == 0;
        }

        void clearAll() {
            for (int sy = 0; sy < 16; sy++) {
                Int2LongOpenHashMap m = bySy[sy];
                if (m != null) m.clear();
            }
            nonEmptySyMask16 = 0;
        }

        void clearSy(int sy) {
            Int2LongOpenHashMap m = bySy[sy];
            if (m != null) m.clear();
            nonEmptySyMask16 &= ~(1 << sy);
        }

        void put(int sy, int pi, long vBits) {
            if (pi < 0 || pi >= POCKETS_PER_SY) return;
            Int2LongOpenHashMap m = bySy[sy];
            if (m == null) {
                m = new Int2LongOpenHashMap(8);
                m.defaultReturnValue(0L);
                bySy[sy] = m;
            }
            m.put(pi, vBits);
            nonEmptySyMask16 |= (1 << sy);
        }

        long getBits(int sy, int pi) {
            if (pi < 0 || pi >= POCKETS_PER_SY) return 0L;
            Int2LongOpenHashMap m = bySy[sy];
            if (m == null) return 0L;
            return m.get(pi);
        }

        long takeBits(int sy, int pi) {
            if (pi < 0 || pi >= POCKETS_PER_SY) return 0L;
            Int2LongOpenHashMap m = bySy[sy];
            if (m == null) return 0L;
            long v = m.remove(pi);
            if (m.isEmpty()) nonEmptySyMask16 &= ~(1 << sy);
            return v;
        }

        void clearAbove(int sy, int keepCount) {
            if (keepCount >= POCKETS_PER_SY) return;
            Int2LongOpenHashMap m = bySy[sy];
            if (m == null || m.isEmpty()) {
                nonEmptySyMask16 &= ~(1 << sy);
                return;
            }
            if (keepCount <= 0) {
                clearSy(sy);
                return;
            }

            var it = m.keySet().iterator();
            while (it.hasNext()) {
                if (it.nextInt() >= keepCount) it.remove();
            }

            if (m.isEmpty()) nonEmptySyMask16 &= ~(1 << sy);
            else nonEmptySyMask16 |= (1 << sy);
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
    // - Chunk metadata active bits (chunkMeta)
    // - Radiation values (uniformRads / SingleMaskedSectionRef.rad / MultiSectionRef.data) are also plain.
    // If scheduling is changed (overlap sweeps, allow multiple simultaneous neighbor exchanges per chunk, etc.),
    // these plain accesses are no longer justified and must become atomic or be redesigned.
    static final class WorldRadiationData {
        final WorldServer world;
        final long worldSalt;
        final double minBound;

        final Long2IntOpenHashMap coordToId = new Long2IntOpenHashMap(4225);
        final DirtyChunkTracker dirtyCk = new DirtyChunkTracker(2048);
        final MpscUnboundedXaddArrayLongQueue destructionQueue = new MpscUnboundedXaddArrayLongQueue(64);
        final TLPool<short[]> pocketDataPool = new TLPool<>(() -> {
            short[] a = new short[4096];
            Arrays.fill(a, NO_POCKET);
            return a;
        }, a -> Arrays.fill(a, NO_POCKET), 256, 4096);
        final LongArrayList dirtyToRebuildScratch = new LongArrayList(16384);
        final IntArrayList editedChunkIds = new IntArrayList(256);
        final ObjectPool<EditTable> editTablePool = new ObjectPool<>(() -> new EditTable(32), EditTable::clear, 64);
        int capacity = 4096;
        int nextId;
        int[] freeIds = new int[1024];
        int freeTop;
        long[] cks = new long[capacity];
        Chunk[] mcChunks = new Chunk[capacity];
        PendingRad[] pending = new PendingRad[capacity];
        long[] chunkMeta = new long[capacity];
        int[] eastNeighborId = new int[capacity];
        int[] southNeighborId = new int[capacity];
        double[] uniformRads = new double[capacity * 16];
        SectionRef[] complexSecs = new SectionRef[capacity * 16];
        byte[] myBucket = new byte[capacity];
        int[] myBucketIndex = new int[capacity];
        int[][] xPairAByBucket = new int[][]{new int[4096], new int[4096], new int[4096], new int[4096]};
        int[][] xPairBByBucket = new int[][]{new int[4096], new int[4096], new int[4096], new int[4096]};
        int[][] zPairAByBucket = new int[][]{new int[4096], new int[4096], new int[4096], new int[4096]};
        int[][] zPairBByBucket = new int[][]{new int[4096], new int[4096], new int[4096], new int[4096]};
        int[] xPairCounts = new int[4];
        int[] zPairCounts = new int[4];
        boolean pairListsDirty = true;
        int[][] parityBucketIds = new int[][]{new int[4096], new int[4096], new int[4096], new int[4096]};
        int[] parityCounts = new int[4];
        EditTable[] editsById = new EditTable[capacity];
        long[] linkScratch = new long[512];
        int[] dirtyChunkIdsScratch = new int[1024];
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
            worldSalt = HashCommon.murmurHash3(
                    world.getSeed() ^ (long) world.provider.getDimension() * 0x9E3779B97F4A7C15L ^ 0xD1B54A32D192ED03L);
            coordToId.defaultReturnValue(-1);
            Arrays.fill(eastNeighborId, -1);
            Arrays.fill(southNeighborId, -1);
            Arrays.fill(myBucket, (byte) -1);
            Arrays.fill(myBucketIndex, -1);
            for (int b = 0; b < 4; b++) Arrays.fill(parityBucketIds[b], -1);
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

        static int[] ensureIntScratch(ThreadLocal<int[]> tl, int need) {
            int[] arr = tl.get();
            if (arr.length >= need) return arr;
            int n = grow(arr.length, need);
            arr = Arrays.copyOf(arr, n);
            tl.set(arr);
            return arr;
        }

        static long packSectionLocal(long sectionKey, int localIdx) {
            return sectionKey | (((long) localIdx & 0xFFFL) << 4);
        }

        static long stripLocal(long packedSectionLocal) {
            return packedSectionLocal & ~0xFFF0L;
        }

        static int floodFillPockets(SectionMask resistant, short[] pocketData, int[] queue, int @Nullable [] vols,
                                    long @Nullable [] sumXYZ) {
            if (vols != null) Arrays.fill(vols, 0);
            if (sumXYZ != null) Arrays.fill(sumXYZ, 0);
            int pc = 0;
            for (int blockIndex = 0; blockIndex < SECTION_BLOCK_COUNT; blockIndex++) {
                if (pocketData[blockIndex] != NO_POCKET) continue;
                if (resistant.get(blockIndex)) continue;

                int pocketIndex = (pc >= MAX_POCKETS) ? 0 : pc++;
                int head = 0, tail = 0;
                queue[tail++] = blockIndex;
                pocketData[blockIndex] = (short) pocketIndex;

                if (vols != null) vols[pocketIndex]++;
                if (sumXYZ != null) {
                    int base = pocketIndex * 3;
                    sumXYZ[base] += Library.getLocalX(blockIndex);
                    sumXYZ[base + 1] += Library.getLocalY(blockIndex);
                    sumXYZ[base + 2] += Library.getLocalZ(blockIndex);
                }

                while (head != tail) {
                    int cur = queue[head++];
                    for (int f = 0; f < 6; f++) {
                        int nei = cur + LINEAR_OFFSETS[f];
                        if (((nei & 0xF000) | ((cur ^ nei) & BOUNDARY_MASKS[f])) != 0) continue;
                        if (pocketData[nei] != NO_POCKET) continue;
                        if (resistant.get(nei)) continue;

                        pocketData[nei] = (short) pocketIndex;
                        queue[tail++] = nei;

                        if (vols != null) vols[pocketIndex]++;
                        if (sumXYZ != null) {
                            int base = pocketIndex * 3;
                            sumXYZ[base] += Library.getLocalX(nei);
                            sumXYZ[base + 1] += Library.getLocalY(nei);
                            sumXYZ[base + 2] += Library.getLocalZ(nei);
                        }
                    }
                }
            }
            return pc;
        }

        static void ensurePairBucketCapacity(int[][] aByBucket, int[][] bByBucket, int bucket, int need) {
            int[] aArr = aByBucket[bucket];
            if (aArr.length >= need) return;
            int n = aArr.length;
            while (n < need) n = n + (n >>> 1) + 16;
            aByBucket[bucket] = Arrays.copyOf(aArr, n);
            bByBucket[bucket] = Arrays.copyOf(bByBucket[bucket], n);
        }

        //@formatter:off
        void sweepX(int[][] aPairs, int[][] bPairs, int c0, int c1, int c2, int c3, int th0, int th1, int th2, int th3, boolean flip) {
            var t0 = new WorldRadiationData.DiffuseXTask(aPairs[0], bPairs[0], 0, c0, th0);
            var t1 = new WorldRadiationData.DiffuseXTask(aPairs[1], bPairs[1], 0, c1, th1);
            var t2 = new WorldRadiationData.DiffuseXTask(aPairs[2], bPairs[2], 0, c2, th2);
            var t3 = new WorldRadiationData.DiffuseXTask(aPairs[3], bPairs[3], 0, c3, th3);
            if (flip) { ForkJoinTask.invokeAll(t1, t3); ForkJoinTask.invokeAll(t0, t2);}
            else { ForkJoinTask.invokeAll(t0, t2); ForkJoinTask.invokeAll(t1, t3); }
        }

        void sweepZ(int[][] aPairs, int[][] bPairs, int c0, int c1, int c2, int c3, int th0, int th1, int th2, int th3, boolean flip) {
            var t0 = new WorldRadiationData.DiffuseZTask(aPairs[0], bPairs[0], 0, c0, th0);
            var t1 = new WorldRadiationData.DiffuseZTask(aPairs[1], bPairs[1], 0, c1, th1);
            var t2 = new WorldRadiationData.DiffuseZTask(aPairs[2], bPairs[2], 0, c2, th2);
            var t3 = new WorldRadiationData.DiffuseZTask(aPairs[3], bPairs[3], 0, c3, th3);
            if (flip) { ForkJoinTask.invokeAll(t2, t3); ForkJoinTask.invokeAll(t0, t1);}
            else { ForkJoinTask.invokeAll(t0, t1); ForkJoinTask.invokeAll(t2, t3); }
        }

        void sweepY(int[][] b, int c0, int c1, int c2, int c3, int th0, int th1, int th2, int th3, int startParity) {
            for (int p = 0; p < 2; p++) {
                int parity = startParity ^ p;
                var t0 = new DiffuseYTask(b[0], 0, c0, parity, th0).fork();
                var t1 = new DiffuseYTask( b[1], 0, c1, parity, th1).fork();
                var t2 = new DiffuseYTask( b[2], 0, c2, parity, th2).fork();
                new DiffuseYTask(b[3], 0, c3, parity, th3).invoke();
                t0.join(); t1.join(); t2.join();
            }
        }//@formatter:on

        void diffuseXZ(int aId, int bId, int faceA, int faceB) {
            long metaA = chunkMeta[aId];
            long metaB = chunkMeta[bId];
            int ksA = (int) metaA;
            int ksB = (int) metaB;
            int offA = aId << 4;
            int offB = bId << 4;
            double[] uniform = uniformRads;
            boolean d = false;

            if (ksA == 0x55555555 && ksB == 0x55555555) {
                for (int sy = 0; sy < 16; sy++) {
                    long actMask = 1L << (32 + sy);
                    if (((metaA | metaB) & actMask) != 0L) {
                        int idxA = offA + sy;
                        int idxB = offB + sy;
                        if (exchangeUniExactXZ(uniform, idxA, idxB)) {
                            chunkMeta[aId] |= actMask;
                            chunkMeta[bId] |= actMask;
                            d = true;
                        }
                    }
                }
                if (d) {
                    chunkMeta[aId] |= CHUNK_META_DIRTY_MASK;
                    chunkMeta[bId] |= CHUNK_META_DIRTY_MASK;
                }
                return;
            }

            for (int sy = 0; sy < 16; sy++) {
                long actMask = 1L << (32 + sy);
                if (((metaA | metaB) & actMask) == 0L) continue;

                int idxA = offA + sy;
                int idxB = offB + sy;
                int shift = sy << 1;
                int kA = (ksA >>> shift) & 3;
                int kB = (ksB >>> shift) & 3;

                if (kA == KIND_UNI && kB == KIND_UNI) {
                    if (exchangeUniExactXZ(uniform, idxA, idxB)) {
                        chunkMeta[aId] |= actMask;
                        chunkMeta[bId] |= actMask;
                        d = true;
                    }
                } else {
                    d |= exchangeFaceExact(idxA, kA, faceA, idxB, kB, faceB);
                }
            }
            if (d) {
                chunkMeta[aId] |= CHUNK_META_DIRTY_MASK;
                chunkMeta[bId] |= CHUNK_META_DIRTY_MASK;
            }
        }

        boolean exchangeFaceExactY(int idxA, int kA, int idxB, int kB) {
            if (kA == KIND_NONE || kB == KIND_NONE) return false;
            if (kA == KIND_UNI) {
                SectionRef b = complexSecs[idxB];
                return b.exchangeWithUniform(idxA, 0, 1);
            }
            if (kB == KIND_UNI) {
                SectionRef a = complexSecs[idxA];
                return a.exchangeWithUniform(idxB, 1, 0);
            }
            SectionRef a = complexSecs[idxA];
            SectionRef b = complexSecs[idxB];
            if (kB == KIND_SINGLE) return a.exchangeWithSingle((SingleMaskedSectionRef) b, 1, 0);
            return a.exchangeWithMulti((MultiSectionRef) b, 1, 0);
        }

        boolean exchangeFaceExact(int idxA, int kA, int faceA, int idxB, int kB, int faceB) {
            if (kA == KIND_NONE || kB == KIND_NONE) return false;
            if (kA == KIND_UNI) {
                SectionRef secB = complexSecs[idxB];
                return secB.exchangeWithUniform(idxA, faceB, faceA);
            } else if (kB == KIND_UNI) {
                SectionRef secA = complexSecs[idxA];
                return secA.exchangeWithUniform(idxB, faceA, faceB);
            } else {
                SectionRef secA = complexSecs[idxA];
                SectionRef secB = complexSecs[idxB];
                if (kB == KIND_SINGLE) return secA.exchangeWithSingle((SingleMaskedSectionRef) secB, faceA, faceB);
                return secA.exchangeWithMulti((MultiSectionRef) secB, faceA, faceB);
            }
        }

        boolean isSectionActive(int id, int sy) {
            return (chunkMeta[id] & (1L << (32 + sy))) != 0L;
        }

        int getId(long ck) {
            return coordToId.get(ck);
        }

        void ensureCapacity(int min) {
            if (capacity >= min) return;
            int oldCap = capacity;
            int n = grow(capacity, min);
            capacity = n;
            cks = Arrays.copyOf(cks, n);
            mcChunks = Arrays.copyOf(mcChunks, n);
            pending = Arrays.copyOf(pending, n);
            chunkMeta = Arrays.copyOf(chunkMeta, n);
            eastNeighborId = Arrays.copyOf(eastNeighborId, n);
            southNeighborId = Arrays.copyOf(southNeighborId, n);
            Arrays.fill(eastNeighborId, oldCap, n, -1);
            Arrays.fill(southNeighborId, oldCap, n, -1);
            uniformRads = Arrays.copyOf(uniformRads, n * 16);
            complexSecs = Arrays.copyOf(complexSecs, n * 16);
            editsById = Arrays.copyOf(editsById, n);
            myBucket = Arrays.copyOf(myBucket, n);
            myBucketIndex = Arrays.copyOf(myBucketIndex, n);
            Arrays.fill(myBucket, oldCap, n, (byte) -1);
            Arrays.fill(myBucketIndex, oldCap, n, -1);
        }

        void pushFreeId(int id) {
            if (freeTop == freeIds.length) freeIds = Arrays.copyOf(freeIds, grow(freeIds.length, freeTop + 1));
            freeIds[freeTop++] = id;
        }

        int popFreeId() {
            return freeIds[--freeTop];
        }

        int allocateId() {
            if (freeTop > 0) return popFreeId();
            int id = nextId++;
            if (id >= capacity) ensureCapacity(id + 1);
            return id;
        }

        void clearResidentState(int id) {
            mcChunks[id] = null;
            pending[id] = null;
            chunkMeta[id] = 0L;
            eastNeighborId[id] = -1;
            southNeighborId[id] = -1;
            EditTable t = editsById[id];
            if (t != null) {
                editTablePool.recycle(t);
                editsById[id] = null;
            }
            int si = id << 4;
            Arrays.fill(uniformRads, si, si + 16, 0.0D);
            Arrays.fill(complexSecs, si, si + 16, null);
            myBucket[id] = -1;
            myBucketIndex[id] = -1;
        }

        int ensureId(long ck) {
            int id = coordToId.get(ck);
            if (id >= 0) return id;
            id = allocateId();
            coordToId.put(ck, id);
            cks[id] = ck;
            clearResidentState(id);
            return id;
        }

        int getKinds(int id) {
            return (int) (chunkMeta[id] & CHUNK_META_KINDS_MASK);
        }

        boolean isChunkDirty(int id) {
            return (chunkMeta[id] & CHUNK_META_DIRTY_MASK) != 0L;
        }

        void setChunkDirty(int id) {
            chunkMeta[id] |= CHUNK_META_DIRTY_MASK;
        }

        void clearChunkDirty(int id) {
            chunkMeta[id] &= ~CHUNK_META_DIRTY_MASK;
        }

        int getKind(int id, int sy) {
            return (getKinds(id) >>> (sy << 1)) & 3;
        }

        void linkNonUniFace(SectionRef a, int kA, int faceA, int idxB) {
            assert kA > KIND_UNI;
            int kB = (idxB < 0) ? KIND_NONE : (int) ((chunkMeta[idxB >>> 4] >>> ((idxB & 15) << 1)) & 3);
            if (kB == KIND_UNI) {
                a.linkFaceToUniform(faceA);
                return;
            }
            if (kB == KIND_NONE) {
                a.clearFaceAllPockets(faceA);
                return;
            }
            SectionRef b = complexSecs[idxB];
            if (b == null) {
                a.clearFaceAllPockets(faceA);
                return;
            }
            if (kB == KIND_MULTI) {
                a.linkFaceToMulti((MultiSectionRef) b, faceA);
            } else {
                a.linkFaceToSingle((SingleMaskedSectionRef) b, faceA);
            }
        }

        void remapPocketMass(int secIdx, int oldKind, @Nullable SectionRef old, int newPocketCount,
                             short @Nullable [] newPocketData, int[] newVols, double[] outNewMass) {

            Arrays.fill(outNewMass, 0, newPocketCount, 0.0d);
            if (oldKind == KIND_NONE || newPocketCount == 0) return;

            if (newPocketData == null) {
                double totalMass = 0.0d;

                if (oldKind == KIND_UNI) {
                    double d = uniformRads[secIdx];
                    if (Math.abs(d) > RAD_EPSILON) totalMass = mulClamp(d, SECTION_BLOCK_COUNT);
                } else if (old != null && old.pocketCount > 0) {
                    if (oldKind == KIND_SINGLE) {
                        double d = uniformRads[secIdx];
                        if (Math.abs(d) > RAD_EPSILON)
                            totalMass = mulClamp(d, Math.max(1, ((SingleMaskedSectionRef) old).volume));
                    } else {
                        MultiSectionRef m = (MultiSectionRef) old;
                        int oldCnt = m.pocketCount & 0xFFFF;
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

            if (oldKind == KIND_UNI) {
                double d = uniformRads[secIdx];
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

            int oldCnt = old.pocketCount & 0xFFFF;
            short[] oldPocketData = old.pocketData;

            // pack (oldPocket,newPocket) into 22 bits (11 + 11), sort at most 4096 entries, then run-length count.
            // Reuse TL_FF_QUEUE as the overlaps scratch (dead after flood-fill, guaranteed >= 4096).
            int[] overlaps = TL_FF_QUEUE.get();

            int[] oldTotals = ensureIntScratch(TL_TEMP_ARRAY, oldCnt);
            // we have an invariant that this array is always zeroed!
            int pairCount = 0;
            for (int i = 0; i < SECTION_BLOCK_COUNT; i++) {
                int nIdx = newPocketData[i];
                if (nIdx < 0 || nIdx >= newPocketCount) continue;

                int oIdx = oldPocketData[i];
                if (oIdx < 0 || oIdx >= oldCnt) continue;

                overlaps[pairCount++] = (oIdx << 11) | nIdx;
                oldTotals[oIdx]++;
            }
            if (pairCount == 0) return;
            Arrays.sort(overlaps, 0, pairCount);

            // Reuse TL_DENSITIES as old-mass scratch (dead before densities are written after this method returns).
            double[] oldMass = TL_DENSITIES.get();
            Arrays.fill(oldMass, 0, oldCnt, 0.0d);

            if (oldKind == KIND_SINGLE) {
                double d0 = uniformRads[secIdx];
                if (Math.abs(d0) > RAD_EPSILON)
                    oldMass[0] = mulClamp(d0, Math.max(1, ((SingleMaskedSectionRef) old).volume));
            } else {
                MultiSectionRef m = (MultiSectionRef) old;
                for (int p = 0; p < oldCnt; p++) {
                    double dp = m.data[p << 1];
                    if (Math.abs(dp) > RAD_EPSILON) oldMass[p] = mulClamp(dp, Math.max(1, m.volume[p]));
                }
            }

            for (int i = 0; i < pairCount; ) {
                int key = overlaps[i];
                int j = i + 1;
                while (j < pairCount && overlaps[j] == key) j++;

                int o = key >>> 11;
                double mass = oldMass[o];
                if (Math.abs(mass) > RAD_EPSILON) {
                    int total = oldTotals[o];
                    if (total > 0) {
                        int n = key & 0x7FF;
                        int c = j - i;
                        outNewMass[n] = addClamp(outNewMass[n], mulClamp(mass / (double) total, c));
                    }
                }
                i = j;
            }
            Arrays.fill(oldTotals, 0, oldCnt, 0);
        }

        void ensureParityBucketCapacity(int bucket, int need) {
            int[] arr = parityBucketIds[bucket];
            if (arr.length >= need) return;
            int oldLen = arr.length;
            int n = oldLen;
            while (n < need) n = n + (n >>> 1) + 16;
            parityBucketIds[bucket] = Arrays.copyOf(arr, n);
            Arrays.fill(parityBucketIds[bucket], oldLen, n, -1);
        }

        void addLoadedToBucket(int id) {
            assert id >= 0;
            Chunk c = mcChunks[id];
            assert c != null && c.loaded : "Adding to bucket requires loaded chunk";
            assert myBucketIndex[id] < 0 : "Double-add to parity bucket";
            byte b = (byte) ((cks[id] & 1L) | ((cks[id] >>> 31) & 2L));
            int next = parityCounts[b] + 1;
            ensureParityBucketCapacity(b, next);
            int i = parityCounts[b]++;
            parityBucketIds[b][i] = id;
            myBucket[id] = b;
            myBucketIndex[id] = i;
            pairListsDirty = true;
        }

        void removeLoadedFromBucket(int id) {
            int i = myBucketIndex[id];
            if (i < 0) return;
            int b = myBucket[id];
            assert b >= 0 && b < 4;

            int last = --parityCounts[b];
            assert last >= 0;

            int[] ids = parityBucketIds[b];
            int swapId = ids[last];

            ids[i] = swapId;
            if (swapId >= 0) myBucketIndex[swapId] = i;

            ids[last] = -1;

            myBucket[id] = -1;
            myBucketIndex[id] = -1;
            pairListsDirty = true;
        }

        void clearBuckets() {
            for (int b = 0; b < 4; b++) {
                int[] ids = parityBucketIds[b];
                int n = parityCounts[b];
                for (int i = 0; i < n; i++) {
                    int id = ids[i];
                    if (id >= 0 && id < nextId) {
                        myBucket[id] = -1;
                        myBucketIndex[id] = -1;
                        chunkMeta[id] &= ~CHUNK_META_DIRTY_MASK;
                    }
                    ids[i] = -1;
                }
                parityCounts[b] = 0;
                xPairCounts[b] = 0;
                zPairCounts[b] = 0;
            }
            pairListsDirty = true;
        }

        void runExactExchangeSweeps() {
            rebuildPairListsIfNeeded();
            int[][] yBuckets = parityBucketIds;
            int[] yCounts = parityCounts;
            int yc0 = yCounts[0], yc1 = yCounts[1], yc2 = yCounts[2], yc3 = yCounts[3];
            int yth0 = getTaskThreshold(yc0, 64), yth1 = getTaskThreshold(yc1, 64), yth2 = getTaskThreshold(yc2,
                    64), yth3 = getTaskThreshold(yc3, 64);
            int xc0 = xPairCounts[0], xc1 = xPairCounts[1], xc2 = xPairCounts[2], xc3 = xPairCounts[3];
            int zc0 = zPairCounts[0], zc1 = zPairCounts[1], zc2 = zPairCounts[2], zc3 = zPairCounts[3];
            int xth0 = getTaskThreshold(xc0, 64), xth1 = getTaskThreshold(xc1, 64), xth2 = getTaskThreshold(xc2,
                    64), xth3 = getTaskThreshold(xc3, 64);
            int zth0 = getTaskThreshold(zc0, 64), zth1 = getTaskThreshold(zc1, 64), zth2 = getTaskThreshold(zc2,
                    64), zth3 = getTaskThreshold(zc3, 64);

            int s = workEpoch;
            boolean fx = (s & 1) != 0, fz = (s & 2) != 0;
            int yPar = (s & 4) != 0 ? 1 : 0;
            int perm = s % 6;
            if (perm < 0) perm += 6;

            switch (perm) {//@formatter:off
                case 0 -> { sweepX(xPairAByBucket, xPairBByBucket, xc0, xc1, xc2, xc3, xth0, xth1, xth2, xth3, fx); sweepZ(zPairAByBucket, zPairBByBucket, zc0, zc1, zc2, zc3, zth0, zth1, zth2, zth3, fz); sweepY(yBuckets, yc0, yc1, yc2, yc3, yth0, yth1, yth2, yth3, yPar); }
                case 1 -> { sweepX(xPairAByBucket, xPairBByBucket, xc0, xc1, xc2, xc3, xth0, xth1, xth2, xth3, fx); sweepY(yBuckets, yc0, yc1, yc2, yc3, yth0, yth1, yth2, yth3, yPar); sweepZ(zPairAByBucket, zPairBByBucket, zc0, zc1, zc2, zc3, zth0, zth1, zth2, zth3, fz); }
                case 2 -> { sweepY(yBuckets, yc0, yc1, yc2, yc3, yth0, yth1, yth2, yth3, yPar); sweepZ(zPairAByBucket, zPairBByBucket, zc0, zc1, zc2, zc3, zth0, zth1, zth2, zth3, fz); sweepX(xPairAByBucket, xPairBByBucket, xc0, xc1, xc2, xc3, xth0, xth1, xth2, xth3, fx); }
                case 3 -> { sweepY(yBuckets, yc0, yc1, yc2, yc3, yth0, yth1, yth2, yth3, yPar); sweepX(xPairAByBucket, xPairBByBucket, xc0, xc1, xc2, xc3, xth0, xth1, xth2, xth3, fx); sweepZ(zPairAByBucket, zPairBByBucket, zc0, zc1, zc2, zc3, zth0, zth1, zth2, zth3, fz); }
                case 4 -> { sweepZ(zPairAByBucket, zPairBByBucket, zc0, zc1, zc2, zc3, zth0, zth1, zth2, zth3, fz); sweepX(xPairAByBucket, xPairBByBucket, xc0, xc1, xc2, xc3, xth0, xth1, xth2, xth3, fx); sweepY(yBuckets, yc0, yc1, yc2, yc3, yth0, yth1, yth2, yth3, yPar); }
                default -> { sweepZ(zPairAByBucket, zPairBByBucket, zc0, zc1, zc2, zc3, zth0, zth1, zth2, zth3, fz); sweepY(yBuckets, yc0, yc1, yc2, yc3, yth0, yth1, yth2, yth3, yPar); sweepX(xPairAByBucket, xPairBByBucket, xc0, xc1, xc2, xc3, xth0, xth1, xth2, xth3, fx); }
            }//@formatter:on
        }

        void postSweepDecayAndEffects() {
            int[][] b = parityBucketIds;
            int[] c = parityCounts;
            int c0 = c[0], c1 = c[1], c2 = c[2], c3 = c[3];
            int th0 = getTaskThreshold(c0, 64), th1 = getTaskThreshold(c1, 64), th2 = getTaskThreshold(c2,
                    64), th3 = getTaskThreshold(c3, 64);
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
            MainRegistry.logger.info(
                    "[RadiationSystemNT] dim {} ({}) avg {} ms/step over last {} steps (total {} ms, last {} ms)",
                    dimId, dimType, avgWinMs, PROFILE_WINDOW, (int) Math.rint(totalMs), lastMs);
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
                long ck = dirtyCk.keys[pos];
                int id = dirtyCk.ids[pos];
                if (id < 0 || id >= nextId) continue;
                if (cks[id] != ck) continue;
                if (mcChunks[id] == null) continue;

                int m16 = dirtyCk.masks16[pos];
                if (m16 == 0) continue;

                dirtyChunkIdsScratch[batch] = id;
                dirtyChunkMasksScratchArr[batch] = m16;
                batch++;

                int m = m16;
                long sck = Library.sectionToLong(ck, 0);
                while (m != 0) {
                    int sy = Integer.numberOfTrailingZeros(m);
                    m &= (m - 1);
                    toRelink.add(Library.setSectionY(sck, sy));
                }
            }

            dirtyCk.reset();
            if (batch == 0) return;

            int threshold = getTaskThreshold(batch, 8);
            new RebuildDirtyChunkBatchTask(dirtyChunkIdsScratch, dirtyChunkMasksScratchArr, 0, batch,
                    threshold).invoke();

            int relinkCount = toRelink.size();
            if (relinkCount != 0) relinkKeys(toRelink.elements(), relinkCount);
        }

        EditTable editsFor(long ck) {
            int id = ensureId(ck);
            EditTable t = editsById[id];
            if (t != null) return t;
            t = editTablePool.borrow();
            editsById[id] = t;
            editedChunkIds.add(id);
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
            IntArrayList ids = editedChunkIds;
            int[] elements = ids.elements();
            for (int i = 0, n = ids.size(); i < n; i++) {
                int id = elements[i];
                EditTable t = editsById[id];
                if (t == null) continue;
                editTablePool.recycle(t);
                editsById[id] = null;
            }
            ids.clear();
        }

        void applyQueuedWrites(long sectionKey, short @Nullable [] pocketData, int pocketCount, double[] densityOut,
                               @Nullable EditTable edits) {
            if (edits == null || edits.isEmpty()) return;
            int sy = Library.getSectionY(sectionKey);
            if ((edits.touchedSyMask & (1 << sy)) == 0) return;
            double[] addPocket = TL_ADD.get();
            double[] setPocket = TL_SET.get();
            long[] bestSeq = TL_BEST_SET_SEQ.get();

            Arrays.fill(addPocket, 0, pocketCount, 0.0d);
            Arrays.fill(setPocket, 0, pocketCount, 0.0d);
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
                    pi = pocketData[local];
                    if (pi < 0 || pi >= pocketCount) continue;
                }
                double dAdd = addAcc[pos];
                if (dAdd != 0.0d) addPocket[pi] += dAdd;
                if ((flags[pos] & EditTable.HAS_SET) != 0) {
                    long seq = setS[pos];
                    if (Long.compareUnsigned(seq, bestSeq[pi]) > 0) {
                        bestSeq[pi] = seq;
                        setPocket[pi] = setV[pos];
                    }
                }
            }

            for (int p = 0; p < pocketCount; p++) {
                double base = bestSeq[p] != 0L ? setPocket[p] : densityOut[p];
                densityOut[p] = sanitize(base + addPocket[p]);
            }
        }

        double sanitize(double v) {
            if (Double.isNaN(v) || Math.abs(v) < RAD_EPSILON && v > minBound) return 0.0D;
            return Math.max(Math.min(v, RAD_MAX), minBound);
        }

        int onChunkLoaded(int cx, int cz, Chunk chunk) {
            assert ((cx ^ (cx << 10) >> 10) | (cz ^ (cz << 10) >> 10)) == 0;
            long ck = ChunkPos.asLong(cx, cz);
            int id = ensureId(ck);
            boolean wasLoaded = mcChunks[id] != null;
            mcChunks[id] = chunk;
            if (!wasLoaded) linkLoadedNeighbors(id);
            if (myBucketIndex[id] < 0) addLoadedToBucket(id);
            else assert myBucket[id] >= 0 && myBucket[id] < 4;
            return id;
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
                IBlockState state = (storage == null || storage.isEmpty()) ? Blocks.AIR.getDefaultState() : storage.data.get(
                        i);

                mp.setPos(x, y, z);
                if (state.getMaterial() != Material.AIR) continue;

                boolean nearGround = false;
                for (int d = 1; d <= 6; d++) {
                    int yy = y - d;
                    if (yy < 0) break;
                    int sy = yy >>> 4;
                    ExtendedBlockStorage e = stor[sy];
                    IBlockState below = (e == null || e.isEmpty()) ? Blocks.AIR.getDefaultState() : e.get(lx, yy & 15,
                            lz);
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

        void ensureDirtyChunkRefCapacity(int need) {
            if (dirtyChunkIdsScratch.length >= need) return;
            int n = grow(dirtyChunkIdsScratch.length, need);
            dirtyChunkIdsScratch = Arrays.copyOf(dirtyChunkIdsScratch, n);
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
            int id = getId(ck);
            if (id < 0) return;
            if (mcChunks[id] == null) return;
            removeLoadedFromBucket(id);
            unlinkLoadedNeighbors(id);
            mcChunks[id] = null;
            chunkMeta[id] &= ~(CHUNK_META_DIRTY_MASK | CHUNK_META_ACTIVE_MASK);
        }

        void removeChunkRef(long ck) {
            int id = coordToId.remove(ck);
            if (id < 0) return;
            assert mcChunks[id] == null : "removeChunkRef called for loaded chunk; must unload first";
            assert myBucketIndex[id] < 0 : "Bucket membership leaked across unload/remove";
            int si = id << 4;
            for (int sy = 0; sy < 16; sy++) {
                SectionRef old = complexSecs[si + sy];
                if (old != null) pocketDataPool.recycle(old.pocketData);
            }
            clearResidentState(id);
            pushFreeId(id);
        }

        @ServerThread
        byte @Nullable [] tryEncodePayload(long ck, Chunk chunk) {
            BUF.clear();
            BUF.putShort((short) 0);
            int count = 0;
            int id = getId(ck);
            EditTable edits = (id >= 0) ? editsById[id] : null;
            int touchedMask16 = (edits != null && !edits.isEmpty()) ? edits.touchedSyMask : 0;
            PendingRad pr = (id >= 0) ? pending[id] : null;
            if (id < 0) return null;
            int dirtyMask16 = dirtyCk.getMask16(ck);
            ExtendedBlockStorage[] stor = chunk.getBlockStorageArray();
            long baseSck = Library.sectionToLong(ck, 0);
            short[][] pOut = new short[1][];
            double[] temp = TEMP_DENSITIES;
            double[] dens = TL_DENSITIES.get();
            double[] newMass = TL_NEW_MASS.get();
            int[] vols = TL_VOL_COUNTS.get();

            long meta = chunkMeta[id];
            int kinds = (int) meta;

            for (int sy = 0; sy < 16; sy++) {
                boolean touchedSy = (touchedMask16 & (1 << sy)) != 0;
                boolean dirtySy = (dirtyMask16 & (1 << sy)) != 0;
                boolean pendingSy = pr != null && pr.hasSy(sy);
                int secIdx = (id << 4) | sy;

                int kind = kinds >>> (sy << 1) & 3;
                assert !pendingSy || kind == KIND_NONE;

                if (!touchedSy && pendingSy) {
                    Int2LongOpenHashMap syMap = pr.bySy[sy];
                    if (syMap != null && !syMap.isEmpty()) {
                        var iterator = syMap.int2LongEntrySet().fastIterator();
                        while (iterator.hasNext()) {
                            var entry = iterator.next();
                            long bits = entry.getLongValue();
                            if (bits == 0L) continue;
                            double v = sanitize(Double.longBitsToDouble(bits));
                            if (v == 0.0D) continue;
                            BUF.putShort((short) ((sy << 11) | (entry.getIntKey() & 0x7FF)));
                            BUF.putDouble(v);
                            count++;
                        }
                    }
                    continue;
                }

                if (!touchedSy && kind == KIND_NONE) continue;

                long sck = Library.setSectionY(baseSck, sy);

                if (dirtySy && kind != KIND_NONE) {
                    if (!touchedSy) {
                        if (kind < KIND_MULTI) {
                            if (sanitize(uniformRads[secIdx]) == 0.0D) continue;
                        } else {
                            MultiSectionRef m0 = (MultiSectionRef) complexSecs[secIdx];
                            int oldCnt = m0.pocketCount & 0xFFFF;
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

                    pOut[0] = null;
                    int pCount = computePocketMappingForEncode(sck, stor[sy], pOut, vols);
                    short[] pData = pOut[0];

                    if (pCount > 0) {
                        remapPocketMass(secIdx, kind, complexSecs[secIdx], pCount, pData, vols, newMass);
                        for (int p = 0; p < pCount; p++) {
                            int v = (pData == null) ? SECTION_BLOCK_COUNT : Math.max(1, vols[p]);
                            dens[p] = sanitize(newMass[p] / (double) v);
                        }

                        if (touchedSy) applyQueuedWrites(sck, pData, pCount, dens, edits);

                        for (int p = 0; p < pCount; p++) {
                            double v = dens[p];
                            if (v == 0.0D) continue;
                            BUF.putShort((short) ((sy << 11) | (p & 0x7FF)));
                            BUF.putDouble(v);
                            count++;
                        }
                    }

                    if (pData != null) pocketDataPool.recycle(pData);
                    continue;
                }

                if (!touchedSy) {
                    if (kind < KIND_MULTI) {
                        double v = sanitize(uniformRads[secIdx]);
                        if (v != 0.0D) {
                            BUF.putShort((short) (sy << 11));
                            BUF.putDouble(v);
                            count++;
                        }
                        continue;
                    }
                    MultiSectionRef m = (MultiSectionRef) complexSecs[secIdx];
                    int pCount = m.pocketCount & 0xFFFF;
                    double[] data = m.data;
                    for (int p = 0; p < pCount; p++) {
                        double v = sanitize(data[p << 1]);
                        if (v == 0.0D) continue;
                        BUF.putShort((short) ((sy << 11) | (p & 0x7FF)));
                        BUF.putDouble(v);
                        count++;
                    }
                    continue;
                }

                if (kind == KIND_NONE) {
                    pOut[0] = null;
                    int pCount = computePocketMappingForEncode(sck, stor[sy], pOut, null);
                    short[] pData = pOut[0];

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
                            BUF.putShort((short) ((sy << 11) | (p & 0x7FF)));
                            BUF.putDouble(v);
                            count++;
                        }
                    }

                    if (pData != null) pocketDataPool.recycle(pData);
                    continue;
                }

                int pCount;
                short[] pData = null;

                if (kind < KIND_MULTI) {
                    temp[0] = uniformRads[secIdx];
                    pCount = 1;
                } else {
                    SectionRef sc = complexSecs[secIdx];
                    pCount = sc.pocketCount & 0xFFFF;
                    pData = sc.pocketData;
                    MultiSectionRef m = (MultiSectionRef) sc;
                    for (int p = 0; p < pCount; p++) temp[p] = m.data[p << 1];
                }

                applyQueuedWrites(sck, pData, pCount, temp, edits);

                for (int p = 0; p < pCount; p++) {
                    double v = sanitize(temp[p]);
                    if (v == 0.0D) continue;
                    BUF.putShort((short) ((sy << 11) | (p & 0x7FF)));
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

        int computePocketMappingForEncode(long sectionKey, @Nullable ExtendedBlockStorage ebs, short[][] outPocketData,
                                          int @Nullable [] volsOut) {
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
            short[] scratch = pocketDataPool.borrow();
            int[] queue = TL_FF_QUEUE.get();
            int pc = floodFillPockets(resistant, scratch, queue, volsOut, null);
            if (pc <= 0) {
                pocketDataPool.recycle(scratch);
                return 0;
            }
            outPocketData[0] = scratch;
            return pc;
        }

        void readPayload(int cx, int cz, byte[] raw) throws DecodeException {
            long ck = ChunkPos.asLong(cx, cz);
            int id = ensureId(ck);
            int secBase = id << 4;
            for (int sy = 0; sy < 16; sy++) {
                SectionRef prev = complexSecs[secBase + sy];
                if (prev != null) pocketDataPool.recycle(prev.pocketData);
                complexSecs[secBase + sy] = null;
            }
            pending[id] = null;
            chunkMeta[id] = 0L;
            Arrays.fill(uniformRads, secBase, secBase + 16, 0.0D);
            dirtyCk.add(ck, id);
            byte fmt = raw[3];
            ByteBuffer b = ByteBuffer.wrap(raw, 4, raw.length - 4);
            if (b.remaining() < 2) throw new DecodeException("truncated v" + fmt + " header");
            int entryCount = b.getShort() & 0xFFFF;

            int need;
            if (fmt == FMT_V6) {
                need = entryCount * (1 + 8);
            } else if (fmt == FMT) {
                need = entryCount * (2 + 8);
            } else {
                throw new DecodeException("Unknown format: " + fmt);
            }
            if (b.remaining() < need)
                throw new DecodeException("truncated v" + fmt + " payload: need=" + need + " rem=" + b.remaining());

            PendingRad pr = null;
            if (fmt == FMT_V6) {
                for (int i = 0; i < entryCount; i++) {
                    int yz = b.get() & 0xFF;
                    int sy = (yz >>> 4) & 15;
                    int pi = yz & 15;

                    double rad = sanitize(b.getDouble());
                    if (rad == 0.0D) continue;
                    if (pr == null) pr = new PendingRad();
                    pr.put(sy, pi, Double.doubleToRawLongBits(rad));
                }
            } else {
                for (int i = 0; i < entryCount; i++) {
                    int sypi = b.getShort() & 0xFFFF;
                    int sy = (sypi >>> 11) & 15;
                    int pi = sypi & 0x7FF;

                    double rad = sanitize(b.getDouble());
                    if (rad == 0.0D) continue;
                    if (pr == null) pr = new PendingRad();
                    pr.put(sy, pi, Double.doubleToRawLongBits(rad));
                }
            }
            pending[id] = pr;
        }

        void rebuildChunkPocketsLoaded(int ownerId, int sy, long sectionKey, @Nullable ExtendedBlockStorage ebs,
                                       @Nullable EditTable edits) {
            int secIdx = (ownerId << 4) | sy;
            chunkMeta[ownerId] &= ~(1L << (32 + sy));
            long meta = chunkMeta[ownerId];
            int oldKind = (int) ((meta >>> (sy << 1)) & 3);
            SectionRef old = complexSecs[secIdx];
            PendingRad pr = pending[ownerId];
            assert (oldKind == KIND_NONE || oldKind == KIND_UNI) == (old == null);
            assert pr == null || !pr.hasSy(sy) || oldKind == KIND_NONE;
            short[] pocketData;

            int[] vols = TL_VOL_COUNTS.get();
            long[] sumXYZ = TL_SUM_XYZ.get();

            short[][] pOut = new short[1][];
            int pocketCount = computePocketMappingForRebuild(sectionKey, ebs, pOut, vols, sumXYZ);
            pocketData = pOut[0];
            assert pocketCount >= 0 && pocketCount <= MAX_POCKETS;
            assert pocketData != null || pocketCount == 1;

            if (pocketCount == 0) {
                if (old != null) pocketDataPool.recycle(old.pocketData);
                complexSecs[secIdx] = null;
                chunkMeta[ownerId] = (chunkMeta[ownerId] & ~(3L << (sy << 1)));

                if (pr != null && pr.hasSy(sy)) {
                    pr.clearSy(sy);
                    if (pr.isEmpty()) pending[ownerId] = null;
                }
                return;
            }

            int singleVolume0 = SECTION_BLOCK_COUNT;
            long singleFaceCounts = 0L;
            if (pocketCount == 1 && pocketData != null) {
                singleVolume0 = Math.max(1, vols[0]);
                for (int face = 0; face < 6; face++) {
                    int base = face << 8;
                    int c = 0;
                    for (int t = 0; t < 256; t++) {
                        int idx = FACE_PLANE[base + t];
                        if (pocketData[idx] == 0) c++;
                    }
                    singleFaceCounts |= ((long) c & 0x1FFL) << (face * 9);
                }
            }

            double[] newMass = TL_NEW_MASS.get();
            Arrays.fill(newMass, 0, pocketCount, 0.0d);

            if (oldKind != KIND_NONE) {
                remapPocketMass(secIdx, oldKind, old, pocketCount, pocketData, vols, newMass);
            }

            double[] densities = TL_DENSITIES.get();
            for (int p = 0; p < pocketCount; p++) {
                int v = (pocketData == null) ? SECTION_BLOCK_COUNT : Math.max(1, vols[p]);
                double d = newMass[p] / (double) v;
                densities[p] = sanitize(d);
            }

            if (pr != null && pr.hasSy(sy)) {
                for (int p = 0; p < pocketCount; p++) {
                    long bits = pr.takeBits(sy, p);
                    if (bits == 0L) continue;
                    double v = Double.longBitsToDouble(bits);
                    densities[p] = sanitize(v);
                }
                pr.clearAbove(sy, pocketCount);
                if (pr.isEmpty()) pending[ownerId] = null;
            }

            assert pr == null || !pr.hasSy(sy);

            applyQueuedWrites(sectionKey, pocketData, pocketCount, densities, edits);

            if (old != null) pocketDataPool.recycle(old.pocketData);

            if (pocketCount == 1 && pocketData == null) {
                double d = densities[0];
                uniformRads[secIdx] = d;
                chunkMeta[ownerId] = (chunkMeta[ownerId] & ~(3L << (sy << 1))) | (1L << (sy << 1));
                complexSecs[secIdx] = null;
                if (d != 0.0D) chunkMeta[ownerId] |= (1L << (32 + sy));
                return;
            }

            if (pocketCount == 1) {
                double density = densities[0];

                double inv = 1.0d / (double) singleVolume0;
                double cx = sumXYZ[0] * inv;
                double cy = sumXYZ[1] * inv;
                double cz = sumXYZ[2] * inv;

                SingleMaskedSectionRef masked = new SingleMaskedSectionRef(secIdx, pocketData, singleVolume0,
                        singleFaceCounts, cx, cy, cz);
                uniformRads[secIdx] = density;

                complexSecs[secIdx] = masked;
                chunkMeta[ownerId] = (chunkMeta[ownerId] & ~(3L << (sy << 1))) | (2L << (sy << 1));

                if (density != 0.0D) chunkMeta[ownerId] |= (1L << (32 + sy));
                return;
            }

            float[] faceDists = new float[pocketCount * 6];
            for (int p = 0; p < pocketCount; p++) {
                int v = Math.max(1, vols[p]);
                double inv = 1.0d / (double) v;

                int sBase = p * 3;
                double cx = sumXYZ[sBase] * inv;
                double cy = sumXYZ[sBase + 1] * inv;
                double cz = sumXYZ[sBase + 2] * inv;

                int base = p * 6;
                faceDists[base] = (float) (cy + 0.5d);
                faceDists[base + 1] = (float) (15.5d - cy);
                faceDists[base + 2] = (float) (cz + 0.5d);
                faceDists[base + 3] = (float) (15.5d - cz);
                faceDists[base + 4] = (float) (cx + 0.5d);
                faceDists[base + 5] = (float) (15.5d - cx);
            }

            MultiSectionRef sc = new MultiSectionRef(secIdx, (short) pocketCount, pocketData, faceDists);
            boolean active = false;
            for (int p = 0; p < pocketCount; p++) {
                int v = Math.max(1, vols[p]);
                int i2 = p << 1;
                double d = densities[p];
                sc.data[i2] = d;
                sc.data[i2 + 1] = 1.0d / (double) v;
                sc.volume[p] = v;

                if (d != 0.0D) active = true;
            }

            if (active) chunkMeta[ownerId] |= (1L << (32 + sy));
            complexSecs[secIdx] = sc;
            chunkMeta[ownerId] = (chunkMeta[ownerId] & ~(3L << (sy << 1))) | (3L << (sy << 1));
        }

        int computePocketMappingForRebuild(long sectionKey, @Nullable ExtendedBlockStorage ebs, short[][] outPocketData,
                                           int[] vols, long[] sumXYZ) {
            outPocketData[0] = null;

            if (ebs == null || ebs.isEmpty()) {
                Arrays.fill(vols, 0);
                vols[0] = SECTION_BLOCK_COUNT;
                Arrays.fill(sumXYZ, 0);
                return 1;
            }

            SectionMask resistant = scanResistantMask(world, sectionKey, ebs);
            if (resistant == null || resistant.isEmpty()) {
                Arrays.fill(vols, 0);
                vols[0] = SECTION_BLOCK_COUNT;
                Arrays.fill(sumXYZ, 0);
                return 1;
            }

            short[] scratch = pocketDataPool.borrow();
            int[] queue = TL_FF_QUEUE.get();

            int pc = floodFillPockets(resistant, scratch, queue, vols, sumXYZ);
            if (pc <= 0) {
                pocketDataPool.recycle(scratch);
                return 0;
            }

            outPocketData[0] = scratch;
            return pc;
        }

        void relinkKeys(long[] dirtyKeys, int hi) {
            if (hi <= 0) return;
            ensureLinkScratch(hi << 2);
            long[] keys = linkScratch;
            int n = 0;
            for (int i = 0; i < hi; i++) {
                long k = dirtyKeys[i];
                int sy = Library.getSectionY(k);
                assert (sy & ~15) == 0;
                int yzBase = sy << 4;
                keys[n++] = Library.setSectionY(k, yzBase | 7);
                keys[n++] = Library.setSectionY(Library.shiftSectionX(k, -1), yzBase | 1);
                keys[n++] = Library.setSectionY(Library.shiftSectionZ(k, -1), yzBase | 2);
                if (sy != 0) {
                    keys[n++] = Library.setSectionY(k, ((sy - 1) << 4) | 4);
                }
            }
            if (n == 0) return;
            if (n < 4096) LongArrays.radixSort(keys, 0, n);
            else LongArrays.parallelRadixSort(keys, 0, n);

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
            coordToId.clear();
            nextId = 0;
            freeTop = 0;
            Arrays.fill(mcChunks, null);
            Arrays.fill(pending, null);
            Arrays.fill(chunkMeta, 0L);
            Arrays.fill(eastNeighborId, -1);
            Arrays.fill(southNeighborId, -1);
            Arrays.fill(uniformRads, 0.0D);
            Arrays.fill(complexSecs, null);
            IntArrayList edited = editedChunkIds;
            int[] editedArr = edited.elements();
            for (int i = 0, n = edited.size(); i < n; i++) {
                int id = editedArr[i];
                EditTable t = editsById[id];
                if (t == null) continue;
                editTablePool.recycle(t);
                editsById[id] = null;
            }
            edited.clear();
            Arrays.fill(editsById, null);
            Arrays.fill(myBucket, (byte) -1);
            Arrays.fill(myBucketIndex, -1);
            for (int b = 0; b < 4; b++) {
                Arrays.fill(parityBucketIds[b], -1);
                xPairCounts[b] = 0;
                zPairCounts[b] = 0;
            }
            pairListsDirty = true;
            clearBuckets();
        }

        void linkLoadedNeighbors(int id) {
            long ck = cks[id];
            int cx = (int) ck;
            int cz = (int) (ck >>> 32);
            int east = coordToId.get(ChunkPos.asLong(cx + 1, cz));
            eastNeighborId[id] = (east >= 0 && mcChunks[east] != null) ? east : -1;
            int south = coordToId.get(ChunkPos.asLong(cx, cz + 1));
            southNeighborId[id] = (south >= 0 && mcChunks[south] != null) ? south : -1;

            int west = coordToId.get(ChunkPos.asLong(cx - 1, cz));
            if (west >= 0 && mcChunks[west] != null) eastNeighborId[west] = id;
            int north = coordToId.get(ChunkPos.asLong(cx, cz - 1));
            if (north >= 0 && mcChunks[north] != null) southNeighborId[north] = id;
            pairListsDirty = true;
        }

        void unlinkLoadedNeighbors(int id) {
            long ck = cks[id];
            int cx = (int) ck;
            int cz = (int) (ck >>> 32);
            int west = coordToId.get(ChunkPos.asLong(cx - 1, cz));
            if (west >= 0 && eastNeighborId[west] == id) eastNeighborId[west] = -1;
            int north = coordToId.get(ChunkPos.asLong(cx, cz - 1));
            if (north >= 0 && southNeighborId[north] == id) southNeighborId[north] = -1;
            eastNeighborId[id] = -1;
            southNeighborId[id] = -1;
            pairListsDirty = true;
        }

        void rebuildPairListsIfNeeded() {
            if (!pairListsDirty) return;
            for (int b = 0; b < 4; b++) {
                int[] ids = parityBucketIds[b];
                int n = parityCounts[b];
                int xCount = 0;
                int zCount = 0;
                for (int i = 0; i < n; i++) {
                    int aId = ids[i];
                    if (aId < 0) continue;

                    int bId = eastNeighborId[aId];
                    if (bId >= 0 && mcChunks[bId] != null) {
                        int need = xCount + 1;
                        if (need > xPairAByBucket[b].length)
                            ensurePairBucketCapacity(xPairAByBucket, xPairBByBucket, b, need);
                        xPairAByBucket[b][xCount] = aId;
                        xPairBByBucket[b][xCount] = bId;
                        xCount++;
                    }

                    bId = southNeighborId[aId];
                    if (bId >= 0 && mcChunks[bId] != null) {
                        int need = zCount + 1;
                        if (need > zPairAByBucket[b].length)
                            ensurePairBucketCapacity(zPairAByBucket, zPairBByBucket, b, need);
                        zPairAByBucket[b][zCount] = aId;
                        zPairBByBucket[b][zCount] = bId;
                        zCount++;
                    }
                }
                xPairCounts[b] = xCount;
                zPairCounts[b] = zCount;
            }
            pairListsDirty = false;
        }

        final class DiffuseXTask extends RecursiveAction {
            final int[] pairA;
            final int[] pairB;
            final int lo, hi, threshold;

            DiffuseXTask(int[] pairA, int[] pairB, int lo, int hi, int threshold) {
                this.pairA = pairA;
                this.pairB = pairB;
                this.lo = lo;
                this.hi = hi;
                this.threshold = threshold;
            }

            @Override
            protected void compute() {
                int n = hi - lo;
                if (n <= threshold) {
                    for (int i = lo; i < hi; i++) {
                        diffuseXZ(pairA[i], pairB[i], /*E*/ 5, /*W*/ 4);
                    }
                    return;
                }
                int mid = (lo + hi) >>> 1;
                var left = new DiffuseXTask(pairA, pairB, lo, mid, threshold).fork();
                new DiffuseXTask(pairA, pairB, mid, hi, threshold).compute();
                left.join();
            }
        }

        final class DiffuseZTask extends RecursiveAction {
            final int[] pairA;
            final int[] pairB;
            final int lo, hi, threshold;

            DiffuseZTask(int[] pairA, int[] pairB, int lo, int hi, int threshold) {
                this.pairA = pairA;
                this.pairB = pairB;
                this.lo = lo;
                this.hi = hi;
                this.threshold = threshold;
            }

            @Override
            protected void compute() {
                int n = hi - lo;
                if (n <= threshold) {
                    for (int i = lo; i < hi; i++) {
                        diffuseXZ(pairA[i], pairB[i], /*S*/ 3, /*N*/ 2);
                    }
                    return;
                }
                int mid = (lo + hi) >>> 1;
                var left = new DiffuseZTask(pairA, pairB, lo, mid, threshold).fork();
                new DiffuseZTask(pairA, pairB, mid, hi, threshold).compute();
                left.join();
            }
        }

        final class DiffuseYTask extends RecursiveAction {
            final int[] chunks;
            final int lo, hi, parity, threshold;

            DiffuseYTask(int[] chunks, int lo, int hi, int parity, int threshold) {
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
                    int id = chunks[i];
                    int off = id << 4;
                    long meta = chunkMeta[id];
                    int kinds = (int) meta;
                    double[] u = uniformRads;
                    boolean d = false;

                    if (kinds == 0x55555555) {
                        for (int sy = parity; sy < 15; sy += 2) {
                            long actMask = 3L << (32 + sy);
                            if ((meta & actMask) == 0L) continue;
                            int idx = off + sy;
                            int idxN = idx + 1;
                            if (exchangeUniExactY(u, idx, idxN)) {
                                chunkMeta[id] |= actMask;
                                d = true;
                            }
                        }
                        if (d) chunkMeta[id] |= CHUNK_META_DIRTY_MASK;
                        continue;
                    }

                    for (int sy = parity; sy < 15; sy += 2) {
                        long actMask = 3L << (32 + sy);
                        if ((meta & actMask) == 0L) continue;

                        int idx = off + sy;
                        int idxN = idx + 1;
                        int kk = (kinds >>> (sy << 1)) & 0xF;
                        int k = kk & 3;
                        int kN = (kk >>> 2) & 3;

                        if (k == KIND_UNI && kN == KIND_UNI) {
                            if (exchangeUniExactY(u, idx, idxN)) {
                                chunkMeta[id] |= actMask;
                                d = true;
                            }
                        } else {
                            d |= exchangeFaceExactY(idx, k, idxN, kN);
                        }
                    }
                    if (d) chunkMeta[id] |= CHUNK_META_DIRTY_MASK;
                }
            }
        }

        abstract sealed class SectionRef permits MultiSectionRef, SingleMaskedSectionRef {
            final int secIdx;
            final short pocketCount;
            final short @NotNull [] pocketData;

            SectionRef(int secIdx, short pocketCount, short[] pocketData) {
                this.secIdx = secIdx;
                this.pocketCount = pocketCount;
                this.pocketData = pocketData;
            }

            //@formatter:off
            abstract boolean exchangeWithMulti(MultiSectionRef other, int myFace, int otherFace);
            abstract boolean exchangeWithUniform(int otherIdx, int myFace, int otherFace);
            abstract boolean exchangeWithSingle(SingleMaskedSectionRef other, int myFace, int otherFace);
            abstract int getPocketIndex(long pos);
            abstract int paletteIndexOrNeg(int blockIndex);
            abstract void clearFaceAllPockets(int faceOrdinal);
            abstract void linkFaceToMulti(MultiSectionRef other, int myFace);
            abstract void linkFaceToSingle(SingleMaskedSectionRef single, int faceA);
            abstract void linkFaceToUniform(int faceA);
            //@formatter:on
        }

        final class SingleMaskedSectionRef extends SectionRef {
            static final long CONN_OFF = fieldOffset(SingleMaskedSectionRef.class, "connections");

            final int volume;
            final double invVolume, cx, cy, cz;
            final long packedFaceCounts;
            long connections;

            SingleMaskedSectionRef(int secIdx, short[] pocketData, int volume, long packedFaceCounts, double cx,
                                   double cy, double cz) {
                super(secIdx, (short) 1, pocketData);
                this.volume = volume;
                invVolume = 1.0d / volume;
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
                double ra = uniformRads[secIdx];
                double rb = uniformRads[other.secIdx];
                if (ra == rb) return false;

                double invVa = invVolume;
                double invVb = other.invVolume;
                double denomInv = invVa + invVb;
                double distSum = getFaceDist(myFace) + other.getFaceDist(otherFace);
                if (distSum <= 0.0D) return false;

                double e = Math.exp(-((area / distSum) * denomInv * diffusionDt));
                double rStar = (ra * invVb + rb * invVa) / denomInv;
                uniformRads[secIdx] = rStar + (ra - rStar) * e;
                uniformRads[other.secIdx] = rStar + (rb - rStar) * e;
                chunkMeta[secIdx >>> 4] |= (1L << (32 + (secIdx & 15)));
                chunkMeta[other.secIdx >>> 4] |= (1L << (32 + (other.secIdx & 15)));
                return true;
            }

            @Override
            boolean exchangeWithUniform(int otherIdx, int myFace, int otherFace) {
                int area = getFaceCount(myFace);
                double ra = uniformRads[otherIdx];
                double rb = uniformRads[secIdx];
                if (area <= 0 || ra == rb) return false;

                double distSum = 8.0d + getFaceDist(myFace);
                if (distSum <= 0.0D) return false;

                double invVb = invVolume;
                double denomInv = 2.44140625E-4 + invVb;
                double e = Math.exp(-((area / distSum) * denomInv * diffusionDt));
                double rStar = (ra * invVb + rb * 2.44140625E-4) / denomInv;

                uniformRads[otherIdx] = rStar + (ra - rStar) * e;
                uniformRads[secIdx] = rStar + (rb - rStar) * e;

                chunkMeta[otherIdx >>> 4] |= (1L << (32 + (otherIdx & 15)));
                chunkMeta[secIdx >>> 4] |= (1L << (32 + (secIdx & 15)));
                return true;
            }

            @Override
            boolean exchangeWithMulti(MultiSectionRef other, int myFace, int otherFace) {
                int[] edges = other.edgesByFace[otherFace];
                int edgeCount = other.getEdgeCount(otherFace);
                if (edges == null || edgeCount == 0) return false;

                boolean changed = false;
                double ra = uniformRads[secIdx];
                double invVa = invVolume;

                for (int i = 0; i < edgeCount; i++) {
                    int edge = edges[i];
                    int neiPi = (edge >>> 20) & 0x7FF; // other's perspective: pa=other, pb=this
                    int area = edge & 0x1FF;

                    int idxB = neiPi << 1;
                    double rb = other.data[idxB];
                    if (ra == rb) continue;

                    double invVb = other.data[idxB + 1];
                    double denomInv = invVa + invVb;
                    double distSum = getFaceDist(myFace) + other.faceDist[neiPi * 6 + otherFace];
                    if (distSum <= 0.0D) continue;

                    double e = Math.exp(-((area / distSum) * denomInv * diffusionDt));
                    double rStar = (ra * invVb + rb * invVa) / denomInv;

                    ra = rStar + (ra - rStar) * e;
                    other.data[idxB] = rStar + (rb - rStar) * e;
                    changed = true;
                }

                if (changed) {
                    uniformRads[secIdx] = ra;
                    chunkMeta[secIdx >>> 4] |= (1L << (32 + (secIdx & 15)));
                    chunkMeta[other.secIdx >>> 4] |= (1L << (32 + (other.secIdx & 15)));
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
                return pocketData[blockIndex] < 0 ? -1 : 0;
            }

            @Override
            int paletteIndexOrNeg(int blockIndex) {
                int pi = pocketData[blockIndex];
                if (pi < 0) return -1;
                return pi;
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
                short[] myData = pocketData;
                short[] otherData = other.pocketData;
                for (int t = 0; t < 256; t++) {
                    int idxA = FACE_PLANE[baseA + t];
                    if (myData[idxA] < 0) continue;
                    int idxB = FACE_PLANE[baseB + t];
                    if (otherData[idxB] < 0) continue;
                    count++;
                }
                other.updateConnections(faceB, count);
                updateConnections(faceA, count);
            }

            @Override
            void linkFaceToUniform(int faceA) {
                int area = getFaceCount(faceA);
                updateConnections(faceA, area);
            }
        }

        final class MultiSectionRef extends SectionRef {
            final double[] data;
            final float[] faceDist;
            final int[] volume;
            final int[][] edgesByFace = new int[6][];
            final short[] edgeCounts = new short[6];

            MultiSectionRef(int secIdx, short pocketCount, short[] pocketData, float[] faceDist) {
                super(secIdx, pocketCount, pocketData);
                this.faceDist = faceDist;
                int count = pocketCount & 0xFFFF;
                data = new double[count << 1];
                volume = new int[count];
            }

            int getEdgeCount(int face) {
                return edgeCounts[face] & 0xFFFF;
            }

            int[] ensureFaceEdgeCapacity(int face, int need) {
                int[] arr = edgesByFace[face];
                if (arr != null && arr.length >= need) return arr;
                int n = (arr == null) ? 16 : arr.length;
                while (n < need) n = n + (n >>> 1) + 16;
                arr = new int[n];
                edgesByFace[face] = arr;
                return arr;
            }

            @Override
            boolean exchangeWithMulti(MultiSectionRef other, int myFace, int otherFace) {
                int[] edges = edgesByFace[myFace];
                int edgeCount = getEdgeCount(myFace);
                if (edges == null || edgeCount == 0) return false;

                boolean changed = false;
                double[] myData = data;
                double[] otherData = other.data;

                for (int i = 0; i < edgeCount; i++) {
                    int edge = edges[i];
                    int myPi = (edge >>> 20) & 0x7FF;
                    int neiPi = (edge >>> 9) & 0x7FF;
                    int area = edge & 0x1FF;

                    int idxA = myPi << 1;
                    int idxB = neiPi << 1;

                    double ra = myData[idxA];
                    double rb = otherData[idxB];
                    if (ra == rb) continue;

                    double invVa = myData[idxA + 1];
                    double invVb = otherData[idxB + 1];
                    double denomInv = invVa + invVb;

                    double distSum = faceDist[myPi * 6 + myFace] + other.faceDist[neiPi * 6 + otherFace];
                    if (distSum <= 0.0D) continue;

                    double e = Math.exp(-((area / distSum) * denomInv * diffusionDt));
                    double rStar = (ra * invVb + rb * invVa) / denomInv;

                    myData[idxA] = rStar + (ra - rStar) * e;
                    otherData[idxB] = rStar + (rb - rStar) * e;
                    changed = true;
                }

                if (changed) {
                    chunkMeta[secIdx >>> 4] |= (1L << (32 + (secIdx & 15)));
                    chunkMeta[other.secIdx >>> 4] |= (1L << (32 + (other.secIdx & 15)));
                }
                return changed;
            }

            @Override
            boolean exchangeWithUniform(int otherIdx, int myFace, int otherFace) {
                int[] edges = edgesByFace[myFace];
                int edgeCount = getEdgeCount(myFace);
                if (edges == null || edgeCount == 0) return false;

                boolean changed = false;
                double[] myData = data;
                double ra = uniformRads[otherIdx];

                for (int i = 0; i < edgeCount; i++) {
                    int edge = edges[i];
                    int myPi = (edge >>> 20) & 0x7FF;
                    int area = edge & 0x1FF;

                    int idx = myPi << 1;
                    double rb = myData[idx];
                    if (ra == rb) continue;

                    double distSum = 8.0d + faceDist[myPi * 6 + myFace];
                    if (distSum <= 0.0D) continue;

                    double invVb = myData[idx + 1];
                    double denomInv = 2.44140625E-4 + invVb;
                    double e = Math.exp(-((area / distSum) * denomInv * diffusionDt));
                    double rStar = (ra * invVb + rb * 2.44140625E-4) / denomInv;

                    ra = rStar + (ra - rStar) * e;
                    myData[idx] = rStar + (rb - rStar) * e;
                    changed = true;
                }

                if (changed) {
                    uniformRads[otherIdx] = ra;
                    chunkMeta[otherIdx >>> 4] |= (1L << (32 + (otherIdx & 15)));
                    chunkMeta[secIdx >>> 4] |= (1L << (32 + (secIdx & 15)));
                }
                return changed;
            }

            @Override
            boolean exchangeWithSingle(SingleMaskedSectionRef other, int myFace, int otherFace) {
                int[] edges = edgesByFace[myFace];
                int edgeCount = getEdgeCount(myFace);
                if (edges == null || edgeCount == 0) return false;

                boolean changed = false;
                int oIdx = other.secIdx;
                double invVb = other.invVolume;
                double rb = uniformRads[oIdx];

                for (int i = 0; i < edgeCount; i++) {
                    int edge = edges[i];
                    int myPi = (edge >>> 20) & 0x7FF;
                    int area = edge & 0x1FF;

                    int idxA = myPi << 1;
                    double ra = data[idxA];
                    if (ra == rb) continue;

                    double invVa = data[idxA + 1];
                    double denomInv = invVa + invVb;
                    double distSum = faceDist[myPi * 6 + myFace] + other.getFaceDist(otherFace);
                    if (distSum <= 0.0D) continue;

                    double e = Math.exp(-((area / distSum) * denomInv * diffusionDt));
                    double rStar = (ra * invVb + rb * invVa) / denomInv;

                    data[idxA] = rStar + (ra - rStar) * e;
                    rb = rStar + (rb - rStar) * e;
                    changed = true;
                }

                if (changed) {
                    uniformRads[oIdx] = rb;
                    chunkMeta[secIdx >>> 4] |= (1L << (32 + (secIdx & 15)));
                    chunkMeta[oIdx >>> 4] |= (1L << (32 + (oIdx & 15)));
                }
                return changed;
            }

            @Override
            int getPocketIndex(long pos) {
                int blockIndex = Library.blockPosToLocal(pos);
                int pi = pocketData[blockIndex];
                if (pi < 0) return -1;
                return pi;
            }

            @Override
            int paletteIndexOrNeg(int blockIndex) {
                int pi = pocketData[blockIndex];
                if (pi < 0) return -1;
                return pi;
            }

            @Override
            void clearFaceAllPockets(int faceOrdinal) {
                edgeCounts[faceOrdinal] = 0;
            }

            void markSentinelPlane16x16(int faceA) {
                int[] tempArr = TL_TEMP_ARRAY.get();
                int[] touched = TL_TOUCHED.get();
                int touchCount = 0;

                int planeA = faceA << 8;
                for (int t = 0; t < 256; t++) {
                    int pa = paletteIndexOrNeg(FACE_PLANE[planeA + t]);
                    if (pa < 0) continue;

                    if (tempArr[pa] == 0) touched[touchCount++] = pa;
                    tempArr[pa]++;
                }

                if (touchCount == 0) {
                    edgeCounts[faceA] = 0;
                    return;
                }

                int[] aEdges = ensureFaceEdgeCapacity(faceA, touchCount);
                for (int i = 0; i < touchCount; i++) {
                    int pa = touched[i];
                    int area = tempArr[pa];
                    tempArr[pa] = 0;
                    aEdges[i] = (pa << 20) | area;
                }
                edgeCounts[faceA] = (short) touchCount;
            }

            @Override
            void linkFaceToMulti(MultiSectionRef multiB, int faceA) {
                int faceB = faceA ^ 1;
                Long2IntOpenHashMap edgeCountsMap = TL_EDGE_COUNTS.get();
                edgeCountsMap.clear();

                int planeA = faceA << 8;
                int planeB = faceB << 8;

                for (int t = 0; t < 256; t++) {
                    int pa = paletteIndexOrNeg(FACE_PLANE[planeA + t]);
                    int pb = multiB.paletteIndexOrNeg(FACE_PLANE[planeB + t]);
                    if (pa < 0 || pb < 0) continue;
                    long key = ((long) pa << 32) | (pb & 0xFFFF_FFFFL);
                    edgeCountsMap.addTo(key, 1);
                }

                int touchCount = edgeCountsMap.size();
                if (touchCount == 0) {
                    edgeCounts[faceA] = 0;
                    multiB.edgeCounts[faceB] = 0;
                    return;
                }

                int[] aEdges = ensureFaceEdgeCapacity(faceA, touchCount);
                int[] bEdges = multiB.ensureFaceEdgeCapacity(faceB, touchCount);

                int e = 0;
                var iterator = edgeCountsMap.long2IntEntrySet().fastIterator();
                while (iterator.hasNext()) {
                    var entry = iterator.next();
                    long key = entry.getLongKey();
                    int pa = (int) (key >>> 32);
                    int pb = (int) key;
                    int area = entry.getIntValue();

                    aEdges[e] = (pa << 20) | (pb << 9) | area;
                    bEdges[e] = (pb << 20) | (pa << 9) | area;
                    e++;
                }
                edgeCounts[faceA] = (short) e;
                multiB.edgeCounts[faceB] = (short) e;
            }

            @Override
            void linkFaceToSingle(SingleMaskedSectionRef single, int faceA) {
                int faceB = faceA ^ 1;

                int[] tempArr = TL_TEMP_ARRAY.get();
                int[] touched = TL_TOUCHED.get();
                int touchCount = 0;

                int planeA = faceA << 8;
                int planeB = faceB << 8;
                int singleCount = 0;

                for (int t = 0; t < 256; t++) {
                    int pa = paletteIndexOrNeg(FACE_PLANE[planeA + t]);
                    if (pa < 0) continue;

                    if (single.paletteIndexOrNeg(FACE_PLANE[planeB + t]) < 0) continue;

                    if (tempArr[pa] == 0) touched[touchCount++] = pa;
                    tempArr[pa]++;
                    singleCount++;
                }

                if (touchCount == 0) {
                    edgeCounts[faceA] = 0;
                    single.updateConnections(faceB, 0);
                    return;
                }

                int[] aEdges = ensureFaceEdgeCapacity(faceA, touchCount);
                for (int i = 0; i < touchCount; i++) {
                    int pa = touched[i];
                    int area = tempArr[pa];
                    tempArr[pa] = 0;
                    aEdges[i] = (pa << 20) | area;
                }
                edgeCounts[faceA] = (short) touchCount;
                single.updateConnections(faceB, singleCount);
            }

            @Override
            void linkFaceToUniform(int faceA) {
                int[] tempArr = TL_TEMP_ARRAY.get();
                int[] touched = TL_TOUCHED.get();
                int touchCount = 0;

                int planeA = faceA << 8;
                for (int t = 0; t < 256; t++) {
                    int pa = paletteIndexOrNeg(FACE_PLANE[planeA + t]);
                    if (pa < 0) continue;

                    if (tempArr[pa] == 0) touched[touchCount++] = pa;
                    tempArr[pa]++;
                }

                if (touchCount == 0) {
                    edgeCounts[faceA] = 0;
                    return;
                }

                int[] aEdges = ensureFaceEdgeCapacity(faceA, touchCount);
                for (int i = 0; i < touchCount; i++) {
                    int pa = touched[i];
                    int area = tempArr[pa];
                    tempArr[pa] = 0;
                    aEdges[i] = (pa << 20) | area;
                }
                edgeCounts[faceA] = (short) touchCount;
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
                invokeAll(new LinkCanonicalKeysTask(keys, lo, mid, threshold),
                        new LinkCanonicalKeysTask(keys, mid, hi, threshold));
            }

            void work(int start, int end) {
                long curCk = Long.MIN_VALUE;
                int idA = -1;
                int kindsA = 0;
                int idE = -1, idS = -1;
                int kindsE = 0, kindsS = 0;

                for (int i = start; i < end; i++) {
                    long k = keys[i];

                    int yz = Library.getSectionY(k);
                    int sy = yz >>> 4;
                    int dm = yz & 15;

                    long ck = Library.sectionToChunkLong(k);
                    if (ck != curCk) {
                        curCk = ck;

                        idA = coordToId.get(ck);
                        if (idA < 0 || mcChunks[idA] == null) {
                            idA = -1;
                            continue;
                        }
                        kindsA = (int) chunkMeta[idA];
                        idE = eastNeighborId[idA];
                        kindsE = (idE >= 0 && mcChunks[idE] != null) ? (int) chunkMeta[idE] : 0;
                        if (kindsE == 0) idE = -1;
                        idS = southNeighborId[idA];
                        kindsS = (idS >= 0 && mcChunks[idS] != null) ? (int) chunkMeta[idS] : 0;
                        if (kindsS == 0) idS = -1;
                    }

                    if (idA < 0) continue;

                    int shift = sy << 1;
                    int kA = (kindsA >>> shift) & 3;
                    int idxA = (idA << 4) | sy;
                    int idxE = (idE >= 0) ? ((idE << 4) | sy) : -1;
                    int idxS = (idS >= 0) ? ((idS << 4) | sy) : -1;

                    if ((dm & 1) != 0) {
                        if (kA == KIND_NONE) {
                            if (idE >= 0) {
                                int kB = (kindsE >>> shift) & 3;
                                if (kB == KIND_SINGLE || kB == KIND_MULTI) {
                                    SectionRef b = complexSecs[idxE];
                                    if (b != null) {
                                        b.clearFaceAllPockets(/*W*/ 4);
                                        if (kB == KIND_MULTI) ((MultiSectionRef) b).markSentinelPlane16x16(/*W*/ 4);
                                    }
                                }
                            }
                        } else if (kA == KIND_UNI) {
                            if (idE >= 0) {
                                int kB = (kindsE >>> shift) & 3;
                                if (kB != KIND_NONE && kB != KIND_UNI) {
                                    SectionRef b = complexSecs[idxE];
                                    if (b != null) b.linkFaceToUniform(/*W*/ 4);
                                }
                            }
                        } else {
                            SectionRef a = complexSecs[idxA];
                            if (a != null) linkNonUniFace(a, kA, /*E*/ 5, idxE);
                        }
                    }

                    if ((dm & 2) != 0) {
                        if (kA == KIND_NONE) {
                            if (idS >= 0) {
                                int kB = (kindsS >>> shift) & 3;
                                if (kB == KIND_SINGLE || kB == KIND_MULTI) {
                                    SectionRef b = complexSecs[idxS];
                                    if (b != null) {
                                        b.clearFaceAllPockets(/*N*/ 2);
                                        if (kB == KIND_MULTI) ((MultiSectionRef) b).markSentinelPlane16x16(/*N*/ 2);
                                    }
                                }
                            }
                        } else if (kA == KIND_UNI) {
                            if (idS >= 0) {
                                int kB = (kindsS >>> shift) & 3;
                                if (kB != KIND_NONE && kB != KIND_UNI) {
                                    SectionRef b = complexSecs[idxS];
                                    if (b != null) b.linkFaceToUniform(/*N*/ 2);
                                }
                            }
                        } else {
                            SectionRef a = complexSecs[idxA];
                            if (a != null) linkNonUniFace(a, kA, /*S*/ 3, idxS);
                        }
                    }

                    if ((dm & 4) != 0 && sy < 15) {
                        int shiftUp = (sy + 1) << 1;
                        int kB = (kindsA >>> shiftUp) & 3;
                        int idxU = idxA + 1;
                        if (kA == KIND_NONE) {
                            if (kB == KIND_SINGLE || kB == KIND_MULTI) {
                                SectionRef b = complexSecs[idxU];
                                if (b != null) {
                                    b.clearFaceAllPockets(/*D*/ 0);
                                    if (kB == KIND_MULTI) ((MultiSectionRef) b).markSentinelPlane16x16(/*D*/ 0);
                                }
                            }
                        } else if (kA == KIND_UNI) {
                            if (kB != KIND_NONE && kB != KIND_UNI) {
                                SectionRef b = complexSecs[idxU];
                                if (b != null) b.linkFaceToUniform(/*D*/ 0);
                            }
                        } else {
                            SectionRef a = complexSecs[idxA];
                            if (a != null) linkNonUniFace(a, kA, /*U*/ 1, idxU);
                        }
                    }
                }
            }
        }

        final class PostSweepTask extends RecursiveAction {
            final int[] chunks;
            final int lo, hi, threshold;

            PostSweepTask(int[] chunks, int lo, int hi, int threshold) {
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
                    int id = chunks[i];
                    Chunk chunk = mcChunks[id];
                    if (chunk == null) continue;
                    boolean dirty = isChunkDirty(id);
                    long baseSck = Library.sectionToLong(cks[id], 0);
                    int secBase = id << 4;
                    long meta = chunkMeta[id];
                    int kinds = (int) meta;

                    for (int sy = 0; sy < 16; sy++) {
                        if ((meta & (1L << (32 + sy))) == 0L) continue;

                        int secIdx = secBase + sy;
                        int kind = (kinds >>> (sy << 1)) & 3;

                        long sck = Library.setSectionY(baseSck, sy);

                        if (kind < KIND_MULTI) {
                            double prev = uniformRads[secIdx];
                            double next = sanitize(prev * retentionDt);
                            if (next != prev) {
                                uniformRads[secIdx] = next;
                                dirty = true;
                            }
                            if (next == 0.0D) {
                                chunkMeta[id] &= ~(1L << (32 + sy));
                                continue;
                            }

                            long pk = pocketKey(sck, 0);
                            long seed = 0L;

                            if (fogProbU64 != 0L && next > RadiationConfig.fogRad) {
                                seed = HashCommon.mix(pk ^ workEpochSalt);
                                if (Long.compareUnsigned(seed, fogProbU64) < 0) {
                                    spawnFog(kind == KIND_UNI ? null : complexSecs[secIdx], 0, sy, chunk, seed);
                                }
                            }

                            if (next >= 5.0D && pk != Long.MIN_VALUE) {
                                if (seed == 0L) seed = HashCommon.mix(pk ^ workEpochSalt);
                                if (Long.compareUnsigned(HashCommon.mix(seed + 0xD1B54A32D192ED03L),
                                        DESTROY_PROB_U64) < 0) {
                                    if (tickDelay == 1) pocketToDestroy = pk;
                                    else destructionQueue.offer(pk);
                                }
                            }
                        } else {
                            MultiSectionRef multi = (MultiSectionRef) complexSecs[secIdx];
                            int pCount = multi.pocketCount & 0xFFFF;
                            boolean anyAlive = false;

                            for (int p = 0; p < pCount; p++) {
                                int dataIdx = p << 1;
                                double prev = multi.data[dataIdx];
                                if (prev == 0.0D) continue;

                                double next = sanitize(prev * retentionDt);
                                if (next != prev) {
                                    multi.data[dataIdx] = next;
                                    dirty = true;
                                }

                                if (next != 0.0D) {
                                    anyAlive = true;

                                    long pk = pocketKey(sck, p);
                                    long seed = 0L;

                                    if (fogProbU64 != 0L && next > RadiationConfig.fogRad) {
                                        seed = HashCommon.mix(pk ^ workEpochSalt);
                                        if (Long.compareUnsigned(seed, fogProbU64) < 0) {
                                            spawnFog(multi, p, sy, chunk, seed);
                                        }
                                    }

                                    if (next >= 5.0D && pk != Long.MIN_VALUE) {
                                        if (seed == 0L) seed = HashCommon.mix(pk ^ workEpochSalt);
                                        if (Long.compareUnsigned(HashCommon.mix(seed + 0xD1B54A32D192ED03L),
                                                DESTROY_PROB_U64) < 0) {
                                            if (tickDelay == 1) pocketToDestroy = pk;
                                            else destructionQueue.offer(pk);
                                        }
                                    }
                                }
                            }

                            if (!anyAlive) {
                                chunkMeta[id] &= ~(1L << (32 + sy));
                            }
                        }
                    }
                    chunkMeta[id] &= ~CHUNK_META_DIRTY_MASK;
                    if (dirty) chunk.markDirty();
                }
            }
        }

        final class RebuildDirtyChunkBatchTask extends RecursiveAction {
            final int[] ids;
            final int[] masks16;
            final int lo, hi, threshold;

            RebuildDirtyChunkBatchTask(int[] ids, int[] masks16, int lo, int hi, int threshold) {
                this.ids = ids;
                this.masks16 = masks16;
                this.lo = lo;
                this.hi = hi;
                this.threshold = threshold;
            }

            @Override
            protected void compute() {
                if (hi - lo <= threshold) {
                    for (int i = lo; i < hi; i++) {
                        int id = ids[i];
                        Chunk chunk = mcChunks[id];
                        if (chunk == null) continue;
                        EditTable edits = editsById[id];
                        ExtendedBlockStorage[] stor = chunk.getBlockStorageArray();
                        int m = masks16[i];
                        while (m != 0) {
                            int sy = Integer.numberOfTrailingZeros(m);
                            m &= (m - 1);
                            long sck = Library.sectionToLong(chunk.x, sy, chunk.z);
                            rebuildChunkPocketsLoaded(id, sy, sck, stor[sy], edits);
                        }
                    }
                    return;
                }

                int mid = (lo + hi) >>> 1;
                var left = new RebuildDirtyChunkBatchTask(ids, masks16, lo, mid, threshold);
                var right = new RebuildDirtyChunkBatchTask(ids, masks16, mid, hi, threshold);
                left.fork();
                right.compute();
                left.join();
            }
        }

    }


    static final class DirtyChunkTracker {
        static final float LOAD_FACTOR = 0.6f;
        long[] keys;
        int[] ids;
        char[] masks16;
        int[] stamps;
        int[] slots;
        int mask, size, epoch, slotSize;

        DirtyChunkTracker(int expectedChunks) {
            int cap = HashCommon.nextPowerOfTwo(Math.max(16, (int) (expectedChunks / LOAD_FACTOR) + 1));
            keys = new long[cap];
            ids = new int[cap];
            masks16 = new char[cap];
            stamps = new int[cap];
            Arrays.fill(keys, Long.MIN_VALUE);
            Arrays.fill(ids, -1);
            mask = cap - 1;
            slots = new int[Math.max(16, expectedChunks)];
            epoch = 1;
        }

        void add(long ck, int id, int sy) {
            int bit = 1 << sy;
            if (size + 1 > (int) (keys.length * LOAD_FACTOR))
                rehash(HashCommon.nextPowerOfTwo(keys.length + (keys.length >>> 1) + 16));
            int pos = SectionKeyHash.hash(ck) & mask;
            while (true) {
                if (stamps[pos] != epoch) {
                    stamps[pos] = epoch;
                    keys[pos] = ck;
                    ids[pos] = id;
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
                    ids[pos] = id;
                    return;
                }
                pos = (pos + 1) & mask;
            }
        }

        void add(long ck, int id) {
            if (size + 1 > (int) (keys.length * LOAD_FACTOR))
                rehash(HashCommon.nextPowerOfTwo(keys.length + (keys.length >>> 1) + 16));
            int pos = SectionKeyHash.hash(ck) & mask;
            while (true) {
                if (stamps[pos] != epoch) {
                    stamps[pos] = epoch;
                    keys[pos] = ck;
                    ids[pos] = id;
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
                    ids[pos] = id;
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

        boolean isDirty(long ck, int sy) {
            int bit = 1 << sy;
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
            Arrays.fill(ids, -1);
            Arrays.fill(masks16, (char) 0);
            Arrays.fill(stamps, 0);
            size = 0;
            slotSize = 0;
            epoch = 1;
        }

        void rehash(int newCap) {
            long[] newKeys = new long[newCap];
            int[] newIds = new int[newCap];
            char[] newMasks = new char[newCap];
            int[] newStamps = new int[newCap];
            Arrays.fill(newKeys, Long.MIN_VALUE);
            Arrays.fill(newIds, -1);
            int newMask = newCap - 1;

            int[] newSlots = new int[Math.max(slots.length, slotSize)];
            int ns = 0;

            for (int i = 0; i < slotSize; i++) {
                int oldPos = slots[i];
                if (stamps[oldPos] != epoch) continue;
                long ck = keys[oldPos];
                char m16 = masks16[oldPos];
                int id = ids[oldPos];

                int pos = SectionKeyHash.hash(ck) & newMask;
                while (newStamps[pos] == epoch) pos = (pos + 1) & newMask;

                newStamps[pos] = epoch;
                newKeys[pos] = ck;
                newIds[pos] = id;
                newMasks[pos] = m16;
                newSlots[ns++] = pos;
            }

            keys = newKeys;
            ids = newIds;
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
