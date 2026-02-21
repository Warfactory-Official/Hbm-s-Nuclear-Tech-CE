package com.hbm.util;

import com.hbm.Tags;
import com.hbm.config.GeneralConfig;
import com.hbm.interfaces.BitMask;
import com.hbm.interfaces.ServerThread;
import com.hbm.interfaces.ThreadSafeMethod;
import com.hbm.lib.Library;
import com.hbm.lib.internal.UnsafeHolder;
import com.hbm.lib.maps.NonBlockingHashMapLong;
import com.hbm.main.MainRegistry;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BitArray;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.*;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.hbm.lib.internal.UnsafeHolder.U;

/**
 * <p>
 * High-performance, non-blocking utilities for working with Minecraft 1.12.2 {@link Chunk}
 * internals. This class provides a small set of low-level, allocation-conscious helpers around:
 * </p>
 *
 * <ul>
 *   <li>Maintaining a <strong>mirror map</strong> of currently loaded chunks per dimension for
 *       concurrent, read-mostly access (see {@link #acquireMirrorMap} and
 *       {@link #getLoadedChunk}).</li>
 *   <li>Copy-on-write mutation of sub-chunks ({@link ExtendedBlockStorage}) with a single
 *       <strong>CAS publish</strong> per sub-chunk (see {@link #applyAndSwap}
 *       and {@link #casEbsAt};{@link #copyAndModify} builds a modified copy without publishing).</li>
 *   <li>Selective carving of blocks inside a sub-chunk from a bitmask while collecting metadata
 *       such as tile-entity removals and edge contacts (see {@link #copyAndCarve} and
 *       {@link #copyAndCarveLocal}).</li>
 *   <li>TileEntity lifecycle fix-ups after block-state transitions (see {@link #flushTileEntity}).</li>
 * </ul>
 *
 * <h3>Concurrency & Memory Model</h3>
 * <ul>
 *   <li>For a given {@code Chunk} instance, the {@code ExtendedBlockStorage[]} array returned by
 *       {@link Chunk#getBlockStorageArray} is <em>stable</em> for the lifetime of that {@code Chunk}:
 *       it is created in the constructor and never replaced. Only the <em>elements</em>
 *       ({@code ExtendedBlockStorage} per sub-Y) can be swapped or mutated.</li>
 *   <li>Publishing a new sub-chunk is done via {@link #casEbsAt}; the CAS provides volatile write
 *       semantics for the slot. <strong>Readers must load the slot with a volatile read</strong>
 *       via {@link #getEbsVolatile} to reliably observe swaps.
 *       Direct array reads like {@code arr[subY]} may see stale values.</li>
 * </ul>
 *
 * <h3>Mirror Map</h3>
 * <ul>
 *   <li>The mirror map is an auxiliary, non-blocking concurrent structure mapping
 *       {@code dimension → (chunkPos → Chunk)} used to locate chunks from worker threads without
 *       touching the vanilla
 *       {@link net.minecraft.world.gen.ChunkProviderServer#loadedChunks loadedChunks}, which is a
 *       non-threadsafe {@link Long2ObjectOpenHashMap}.</li>
 *   <li>Reference-counted: call {@link #acquireMirrorMap} before concurrent work and
 *       {@link #releaseMirrorMap} afterwards. When the reference count drops to zero the
 *       mirror map is cleared.</li>
 * </ul>
 *
 * <h3>Indexing conventions</h3>
 * <ul>
 *   <li>Local sub-chunk indices (0..4095) are laid out as:
 *       {@code index = x | (z << 4) | (y << 8)}. See {@link #indexToX(int, int)},
 *       {@link #indexToZ(int, int)}, and {@link #indexToY(int)}.</li>
 *   <li>When using a height-descending bitset (as in {@link #copyAndCarve}), the bit index for a
 *       global position {@code (x,y,z)} within a world of height {@code H} is
 *       {@code ((H - 1 - y) << 8) | (z << 4) | x}.</li>
 *   <li>"Packed local" integers used in maps follow the same layout and can be created/decomposed
 *       via helpers in {@link Library}.</li>
 * </ul>
 *
 * <h3>Safety notes</h3>
 * <ul>
 *   <li>The {@code Chunk}/{@code ExtendedBlockStorage[]} array instance passed to helpers must be current.
 *       If a stale reference is passed, the modification will fail <strong>silently</strong> because
 *       the actual modification happens on an orphaned instance that was supposed to be garbage collected.
 *       This can happen if the chunk was unloaded or reloaded. </li>
 *   <li>{@link #getLoadedChunk} clears the {@link Chunk#unloadQueued} flag on the retrieved chunk
 *       to reduce races with the provider's unload pass. This can keep the chunk alive slightly
 *       longer but doesn't ensure the chunk is always loaded. Use {@link net.minecraftforge.common.ForgeChunkManager.Ticket}
 *       to enforce this behavior.</li>
 * </ul>
 *
 * @author mlbv
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Mod.EventBusSubscriber(modid = Tags.MODID)
public final class ChunkUtil {

    private static final long ARR_BASE = U.arrayBaseOffset(ExtendedBlockStorage[].class);
    private static final long ARR_SCALE = U.arrayIndexScale(ExtendedBlockStorage[].class);
    private static final long UNLOAD_QUEUED_OFFSET = UnsafeHolder.fieldOffset(Chunk.class, "unloadQueued", "field_189550_d");
    private static final long BSL_STATES_OFFSET = UnsafeHolder.fieldOffset(BlockStatePaletteLinear.class, "states", "field_186042_a");
    private static final long BSL_ARRAY_SIZE_OFFSET = UnsafeHolder.fieldOffset(BlockStatePaletteLinear.class, "arraySize", "field_186045_d");
    private static final long BSHM_MAP_OFFSET = UnsafeHolder.fieldOffset(BlockStatePaletteHashMap.class, "statePaletteMap", "field_186046_a");
    private static final long IIHBM_VALUES_OFFSET = UnsafeHolder.fieldOffset(IntIdentityHashBiMap.class, "values", "field_186818_b");
    private static final long IIHBM_INTKEYS_OFFSET = UnsafeHolder.fieldOffset(IntIdentityHashBiMap.class, "intKeys", "field_186819_c");
    private static final long IIHBM_BYID_OFFSET = UnsafeHolder.fieldOffset(IntIdentityHashBiMap.class, "byId", "field_186820_d");
    private static final long IIHBM_NEXTFREE_OFFSET = UnsafeHolder.fieldOffset(IntIdentityHashBiMap.class, "nextFreeIndex", "field_186821_e");
    private static final long IIHBM_MAPSIZE_OFFSET = UnsafeHolder.fieldOffset(IntIdentityHashBiMap.class, "mapSize", "field_186822_f");

    private static final ThreadLocal<Int2ObjectOpenHashMap<IBlockState>[]> TL_BUCKET = ThreadLocal.withInitial(() -> {
        // noinspection unchecked
        Int2ObjectOpenHashMap<IBlockState>[] maps = new Int2ObjectOpenHashMap[16];
        for (int i = 0; i < maps.length; i++) {
            maps[i] = new Int2ObjectOpenHashMap<>();
        }
        return maps;
    });
    private static final ThreadLocal<Long2ObjectOpenHashMap<IBlockState>> TL_SCRATCH = ThreadLocal.withInitial(Long2ObjectOpenHashMap::new);
    private static final ThreadLocal<IBlockState[]> TL_OVERRIDES = ThreadLocal.withInitial(() -> new IBlockState[4096]);
    private static final IBlockState AIR_DEFAULT_STATE = Blocks.AIR.getDefaultState();

    /**
     * Dimension → active task count (used to decide when to build/tear down the mirror map).
     */
    private static final Int2IntOpenHashMap activeTask = new Int2IntOpenHashMap();

    /**
     * Dimension → (ChunkPos long key → Chunk) mirror. Only present while there are active tasks.
     */
    private static final NonBlockingHashMapLong<NonBlockingHashMapLong<Chunk>> chunkMap = new NonBlockingHashMapLong<>();

    /**
     * Heuristic threshold: switch to dense path when sub-chunk updates exceed roughly 1/3 of 4096.
     */
    private static final int DENSE_THRESHOLD = 4096 / 3;

    /**
     * Global reference count across all dimensions indicating how many concurrent tasks are active.
     */
    private static int refCounter = 0;

    /**
     * Build (or reference) the mirror map for the given dimension and increment its task count.
     *
     * <p><strong>Threading:</strong> Must be called from the server thread. Typical usage is to
     * acquire once before kicking off parallel work that will rely on {@link #getLoadedChunk} or
     * {@link #getLoadedEBS}.</p>
     *
     * @param world the server world whose dimension will be mirrored
     */
    @ServerThread
    public static NonBlockingHashMapLong<Chunk> acquireMirrorMap(WorldServer world) {
        int key = world.provider.getDimension();
        NonBlockingHashMapLong<Chunk> thisDim;
        if (activeTask.addTo(key, 1) == 0) {
            thisDim = new NonBlockingHashMapLong<>(4096); // half the initial capacity of loadedChunks
            // This parallel traversal assumes the server thread is quiescent for this world's provider
            world.getChunkProvider().loadedChunks.values().parallelStream().forEach(chunk -> thisDim.put(ChunkPos.asLong(chunk.x, chunk.z), chunk));
            chunkMap.put(key, thisDim);
        } else thisDim = chunkMap.get(key);
        refCounter++;
        if (GeneralConfig.enableExtendedLogging) {
            MainRegistry.logger.info("Acquired mirror map for dimension {}. Active tasks of this dim = {}, refCounter = {}.\nAll active dimensions: {}", key, activeTask.get(key), refCounter, chunkMap.keySetLong());
        }
        return Objects.requireNonNull(thisDim);
    }

    /**
     * Decrement the dimension task count and, if it hits zero, drop the mirror map for that
     * dimension. Also decrements the global reference counter.
     *
     * <p><strong>Threading:</strong> Must be called from the server thread and should be paired
     * with {@link #acquireMirrorMap(WorldServer)}.</p>
     *
     * @param world the server world whose dimension mirror should be released (if last user)
     */
    @ServerThread
    public static void releaseMirrorMap(WorldServer world) {
        int key = world.provider.getDimension();
        if (activeTask.addTo(key, -1) == 1) chunkMap.remove(key);
        refCounter--;
        if (GeneralConfig.enableExtendedLogging) {
            MainRegistry.logger.info("Released mirror map for dimension {}. Active tasks of this dim = {}, refCounter = {}.\nAll active dimensions: {}", key, activeTask.get(key), refCounter, chunkMap.keySetLong());
        }
    }

    /**
     * Lookup a loaded {@link Chunk} from the mirror map by packed chunk position.
     *
     * <p>
     * If found, this method also forcibly clears the chunk's {@code unloadQueued} flag to reduce
     * the chance that the provider unloads it while off-thread work is still in-flight. This reduces
     * but does not eliminate unload races; callers should tolerate {@code null} on subsequent lookups.</p>
     * <p>
     * It is strongly advised to keep the chunk loaded via {@link net.minecraftforge.common.ForgeChunkManager.Ticket ForgeChunkManager.Ticket}
     *
     * @param loaded    the world mirror map
     * @param chunkPos packed long chunk position (see {@link ChunkPos#asLong(int, int)})
     * @return the loaded {@link Chunk} reference, or {@code null} if absent in the mirror
     */
    @ThreadSafeMethod
    public static @Nullable Chunk getLoadedChunk(Long2ObjectMap<? extends Chunk> loaded, long chunkPos) {
        Chunk chunk = loaded.get(chunkPos);
        if (chunk == null) return null;
        U.putBooleanVolatile(chunk, UNLOAD_QUEUED_OFFSET, false);
        return chunk;
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (refCounter == 0) return;
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();
        if (world.isRemote) return;
        int key = world.provider.getDimension();
        if (activeTask.get(key) == 0) return;
        NonBlockingHashMapLong<Chunk> dimMap = chunkMap.get(key);
        if (dimMap != null) dimMap.put(ChunkPos.asLong(chunk.x, chunk.z), chunk);
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (refCounter == 0) return;
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();
        if (world.isRemote) return;
        int key = world.provider.getDimension();
        NonBlockingHashMapLong<Chunk> dimMap = chunkMap.get(key);
        if (dimMap != null) dimMap.remove(ChunkPos.asLong(chunk.x, chunk.z));
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        World world = event.getWorld();
        if (world.isRemote) return;
        int key = world.provider.getDimension();
        if (GeneralConfig.enableExtendedLogging)
            MainRegistry.logger.info("Dimension {} unloaded with {} active tasks. refCounter = {}", key, activeTask.get(key), refCounter);
        activeTask.put(key, 0);
        chunkMap.remove(key);
    }

    public static void onServerStopping() {
        chunkMap.clear();
        activeTask.clear();
        if (GeneralConfig.enableExtendedLogging)
            MainRegistry.logger.info("Server stopping with {} active tasks, refCounter = {}", Arrays.stream(activeTask.values().toIntArray())
                                                                                                    .sum(), refCounter);
        refCounter = 0;
    }

    /**
     * Make a deep copy of a {@link BlockStateContainer}, including its palette and backing bit-storage.
     *
     * @param srcData the source container
     * @return a new, independent {@link BlockStateContainer} with identical content
     */
    @ThreadSafeMethod
    @Contract("_ -> new")
    public static BlockStateContainer copyOf(BlockStateContainer srcData) {
        int bits = srcData.bits;
        IBlockStatePalette srcPalette = srcData.palette;
        BlockStateContainer copied = UnsafeHolder.allocateInstance(BlockStateContainer.class);
        copied.bits = bits;

        if (bits <= 4) {
            copied.palette = new BlockStatePaletteLinear(bits, copied);
            int arraySize = U.getInt(srcPalette, BSL_ARRAY_SIZE_OFFSET);
            U.putInt(copied.palette, BSL_ARRAY_SIZE_OFFSET, arraySize);
            IBlockState[] srcStates = (IBlockState[]) U.getReference(srcPalette, BSL_STATES_OFFSET);
            IBlockState[] dstStates = (IBlockState[]) U.getReference(copied.palette, BSL_STATES_OFFSET);
            System.arraycopy(srcStates, 0, dstStates, 0, arraySize);
        } else if (bits <= 8) {
            copied.palette = new BlockStatePaletteHashMap(bits, copied);
            Object srcMap = U.getReference(srcPalette, BSHM_MAP_OFFSET);
            Object dstMap = U.getReference(copied.palette, BSHM_MAP_OFFSET);

            int nextFree = U.getInt(srcMap, IIHBM_NEXTFREE_OFFSET);
            int mapSize = U.getInt(srcMap, IIHBM_MAPSIZE_OFFSET);
            U.putInt(dstMap, IIHBM_NEXTFREE_OFFSET, nextFree);
            U.putInt(dstMap, IIHBM_MAPSIZE_OFFSET, mapSize);

            Object[] srcValues = (Object[]) U.getReference(srcMap, IIHBM_VALUES_OFFSET);
            int[] srcIntKeys = (int[]) U.getReference(srcMap, IIHBM_INTKEYS_OFFSET);
            Object[] srcById = (Object[]) U.getReference(srcMap, IIHBM_BYID_OFFSET);

            U.putReference(dstMap, IIHBM_VALUES_OFFSET, srcValues.clone());
            U.putReference(dstMap, IIHBM_INTKEYS_OFFSET, srcIntKeys.clone());
            U.putReference(dstMap, IIHBM_BYID_OFFSET, srcById.clone());
        } else {
            copied.palette = BlockStateContainer.REGISTRY_BASED_PALETTE;
        }

        BitArray srcStorage = srcData.storage;
        copied.storage = new BitArray(bits, 4096);
        long[] srcLongs = srcStorage.getBackingLongArray();
        long[] dstLongs = copied.storage.getBackingLongArray();
        System.arraycopy(srcLongs, 0, dstLongs, 0, srcLongs.length);
        return copied;
    }

    private static boolean checkNeighbor(NonBlockingHashMapLong<Chunk> loaded, int chunkX, int chunkZ, int subY, int height, ExtendedBlockStorage[] srcs,
                                         NeighborCache nc, int x, int y, int z, @Nullable BitMask localMask) {
        if (x >= 0 && x <= 15 && y >= 0 && y <= 15 && z >= 0 && z <= 15) {
            if (localMask != null) {
                int nIdx = Library.packLocal(x, y, z);
                if (localMask.get(nIdx)) return false;
            }
            ExtendedBlockStorage src = srcs[subY];
            return src != null && !src.isEmpty() && src.get(x, y, z).getBlock() != Blocks.AIR;
        }

        if (y < 0) {
            if (subY == 0) return false;
            ExtendedBlockStorage below = srcs[subY - 1];
            return below != null && !below.isEmpty() && below.get(x, 15, z).getBlock() != Blocks.AIR;
        }
        if (y > 15) {
            if (subY >= (height >> 4) - 1) return false;
            ExtendedBlockStorage above = srcs[subY + 1];
            return above != null && !above.isEmpty() && above.get(x, 0, z).getBlock() != Blocks.AIR;
        }
        if (x < 0) {
            if (nc.negX == null) nc.negX = getLoadedEBS(loaded, ChunkPos.asLong(chunkX - 1, chunkZ));
            if (nc.negX != null) {
                ExtendedBlockStorage n = nc.negX[subY];
                return n != null && !n.isEmpty() && n.get(15, y, z).getBlock() != Blocks.AIR;
            }
            return false;
        }
        if (x > 15) {
            if (nc.posX == null) nc.posX = getLoadedEBS(loaded, ChunkPos.asLong(chunkX + 1, chunkZ));
            if (nc.posX != null) {
                ExtendedBlockStorage n = nc.posX[subY];
                return n != null && !n.isEmpty() && n.get(0, y, z).getBlock() != Blocks.AIR;
            }
            return false;
        }
        if (z < 0) {
            if (nc.negZ == null) nc.negZ = getLoadedEBS(loaded, ChunkPos.asLong(chunkX, chunkZ - 1));
            if (nc.negZ != null) {
                ExtendedBlockStorage n = nc.negZ[subY];
                return n != null && !n.isEmpty() && n.get(x, y, 15).getBlock() != Blocks.AIR;
            }
            return false;
        }
        // z must >= 16
        if (nc.posZ == null) nc.posZ = getLoadedEBS(loaded, ChunkPos.asLong(chunkX, chunkZ + 1));
        if (nc.posZ != null) {
            ExtendedBlockStorage n = nc.posZ[subY];
            return n != null && !n.isEmpty() && n.get(x, y, 0).getBlock() != Blocks.AIR;
        }
        return false;
    }

    /**
     * Produce a modified copy of the target sub-chunk by <em>carving out</em> positions marked in
     * {@code bs}. For every non-air block removed, tile-entity removals and edge contacts are
     * recorded.
     *
     * <p>Edge contact logic: if a removed block position is adjacent to a non-air block (that is NOT
     * also being removed), the removed block's global packed position is added to {@code edgeOut}.</p>
     *
     * <p>Only the bit range that corresponds to this {@code subY} is scanned; set bits for other
     * sub-chunks are skipped.</p>
     *
     * <p>Neighbor reads are done via {@link #getLoadedEBS(WorldServer, long)} using the mirror map.</p>
     *
     * @param world   the world (for height and skylight info)
     * @param chunkX  chunk X coordinate
     * @param chunkZ  chunk Z coordinate
     * @param subY    sub-chunk Y index (0..height/16-1)
     * @param srcs    the source chunk's {@code ExtendedBlockStorage[]} array
     * @param bs      bitset of positions to carve; bits are ordered by descending global Y
     * @param edgeOut sink of global packed positions that touch non-air outside the sub-chunk
     * @return a copied {@link ExtendedBlockStorage} with carved positions set to air, or
     * {@code null} if the source is empty.
     */
    @ThreadSafeMethod
    @Contract(mutates = "param7") // edgeOut
    public static @Nullable ExtendedBlockStorage copyAndCarve(WorldServer world, int chunkX, int chunkZ, int subY,
                                                              @Nullable ExtendedBlockStorage @NotNull [] srcs, BitMask bs,
                                                              LongCollection edgeOut) {
        ExtendedBlockStorage src = getEbsVolatile(srcs, subY);
        if (src == null || src.isEmpty()) return null;
        int height = world.getHeight();
        ExtendedBlockStorage dst = copyOf(src);
        NeighborCache nc = new NeighborCache();
        var loaded = chunkMap.get(world.provider.getDimension());
        int startBit = (height - 1 - ((subY << 4) + 15)) << 8;
        int endBit = ((height - 1 - (subY << 4)) << 8) | 0xFF;

        for (int bit = bs.nextSetBit(startBit); bit >= 0 && bit <= endBit; bit = bs.nextSetBit(bit + 1)) {
            int yGlobal = height - 1 - (bit >>> 8);
            int xGlobal = (chunkX << 4) | ((bit >>> 4) & 0xF);
            int zGlobal = (chunkZ << 4) | (bit & 0xF);

            int xLocal = xGlobal & 0xF;
            int yLocal = yGlobal & 0xF;
            int zLocal = zGlobal & 0xF;

            IBlockState old = dst.get(xLocal, yLocal, zLocal);
            if (old.getMaterial() != Material.AIR) {
                if (checkNeighbor(loaded, chunkX, chunkZ, subY, height, srcs, nc, xLocal - 1, yLocal, zLocal, null) ||
                        checkNeighbor(loaded, chunkX, chunkZ, subY, height, srcs, nc, xLocal + 1, yLocal, zLocal, null) ||
                        checkNeighbor(loaded, chunkX, chunkZ, subY, height, srcs, nc, xLocal, yLocal - 1, zLocal, null) ||
                        checkNeighbor(loaded, chunkX, chunkZ, subY, height, srcs, nc, xLocal, yLocal + 1, zLocal, null) ||
                        checkNeighbor(loaded, chunkX, chunkZ, subY, height, srcs, nc, xLocal, yLocal, zLocal - 1, null) ||
                        checkNeighbor(loaded, chunkX, chunkZ, subY, height, srcs, nc, xLocal, yLocal, zLocal + 1, null)) {
                    edgeOut.add(Library.blockPosToLong(xGlobal, yGlobal, zGlobal));
                }
                dst.set(xLocal, yLocal, zLocal, AIR_DEFAULT_STATE);
            }
        }
        return dst;
    }

    /**
     * Same as {@link #copyAndCarve(WorldServer, int, int, int, ExtendedBlockStorage[], BitMask, LongCollection)},
     * but accepts a <strong>local</strong> (0..4095) bitmask for the target sub-chunk. Each set bit represents
     * {@code index = x | (z << 4) | (y << 8)}.
     *
     * <p>Only local indices in range [0,4095] are considered; others are ignored.</p>
     *
     * @return a copied {@link ExtendedBlockStorage} with carved positions set to air, or {@code null} if the source is empty.
     */
    @ThreadSafeMethod
    @Contract(mutates = "param7") // edgeOut
    public static @Nullable ExtendedBlockStorage copyAndCarveLocal(WorldServer world, int chunkX, int chunkZ, int subY,
                                                                   @Nullable ExtendedBlockStorage @NotNull [] srcs, BitMask localMask,
                                                                   LongCollection edgeOut) {
        ExtendedBlockStorage src = getEbsVolatile(srcs, subY);
        if (src == null || src.isEmpty()) return null;
        int height = world.getHeight();
        ExtendedBlockStorage dst = copyOf(src);
        NeighborCache nc = new NeighborCache();
        var loaded = chunkMap.get(world.provider.getDimension());
        int xBase = chunkX << 4, yBase = subY << 4, zBase = chunkZ << 4;
        for (int idx = localMask.nextSetBit(0); idx >= 0 && idx < 4096; idx = localMask.nextSetBit(idx + 1)) {
            int xLocal = Library.getLocalX(idx);
            int yLocal = Library.getLocalY(idx);
            int zLocal = Library.getLocalZ(idx);

            IBlockState old = dst.get(xLocal, yLocal, zLocal);
            if (old.getMaterial() != Material.AIR) {
                if (checkNeighbor(loaded, chunkX, chunkZ, subY, height, srcs, nc, xLocal - 1, yLocal, zLocal, localMask) ||
                        checkNeighbor(loaded, chunkX, chunkZ, subY, height, srcs, nc, xLocal + 1, yLocal, zLocal, localMask) ||
                        checkNeighbor(loaded, chunkX, chunkZ, subY, height, srcs, nc, xLocal, yLocal - 1, zLocal, localMask) ||
                        checkNeighbor(loaded, chunkX, chunkZ, subY, height, srcs, nc, xLocal, yLocal + 1, zLocal, localMask) ||
                        checkNeighbor(loaded, chunkX, chunkZ, subY, height, srcs, nc, xLocal, yLocal, zLocal - 1, localMask) ||
                        checkNeighbor(loaded, chunkX, chunkZ, subY, height, srcs, nc, xLocal, yLocal, zLocal + 1, localMask)) {
                    int xGlobal = xBase | xLocal;
                    int yGlobal = yBase | yLocal;
                    int zGlobal = zBase | zLocal;
                    edgeOut.add(Library.blockPosToLong(xGlobal, yGlobal, zGlobal));
                }
                dst.set(xLocal, yLocal, zLocal, AIR_DEFAULT_STATE);
            }
        }
        return dst;
    }

    /**
     * Atomically swap a sub-chunk slot using a single compare-and-swap (CAS).
     *
     * @param expect the expected current value in {@code arr[subY]}.
     *               The CAS is guaranteed to fail if the reference is stale.
     * @param update the value to publish on success (can be {@code null})
     * @param arr    the chunk's {@code ExtendedBlockStorage[]} array
     * @param subY   the sub-chunk Y index
     * @return {@code true} if the swap succeeded; {@code false} if the expected value did not match
     */
    @ThreadSafeMethod
    public static boolean casEbsAt(@Nullable ExtendedBlockStorage expect, @Nullable ExtendedBlockStorage update,
                                   @Nullable ExtendedBlockStorage @NotNull [] arr, int subY) {
        long off = ARR_BASE + ((long) subY) * ARR_SCALE;
        return U.compareAndSetReference(arr, off, expect, update);
    }

    /**
     * Load a sub-chunk slot using volatile reads, semantically equivalent to {@code arr[subY]}.
     * This must be used by concurrent readers that need to observe published swaps promptly.
     *
     * @param arr  the chunk's {@code ExtendedBlockStorage[]} array
     * @param subY the sub-chunk Y index
     * @return the {@link ExtendedBlockStorage} reference at {@code arr[subY]}, can be {@code null}
     */
    @ThreadSafeMethod
    private static @Nullable ExtendedBlockStorage getEbsVolatile(@Nullable ExtendedBlockStorage @NotNull [] arr, int subY) {
        long off = ARR_BASE + ((long) subY) * ARR_SCALE;
        return (ExtendedBlockStorage) U.getReferenceVolatile(arr, off);
    }

    /**
     * Fetch the sub-chunk array for a loaded chunk using the mirror map.
     *
     * @param loaded    the world mirror map
     * @param chunkPos packed long chunk position
     * @return the sub-chunk array reference or {@code null} if the chunk is not present in the mirror
     */
    @ThreadSafeMethod
    public static @Nullable ExtendedBlockStorage @Nullable [] getLoadedEBS(Long2ObjectMap<? extends Chunk> loaded, long chunkPos) {
        Chunk chunk = getLoadedChunk(loaded, chunkPos);
        if (chunk == null) return null;
        return chunk.getBlockStorageArray();
    }

    /**
     * Copy block/skylight nibble arrays, palette+storage and ref counts from
     * {@code src} to {@code dst}. Does not copy {@link ExtendedBlockStorage#yBase}.
     * <p>
     * Intended for cross-dimension copying.
     */
    public static void copyEBS(boolean hasSky, ExtendedBlockStorage src, ExtendedBlockStorage dst) {
        dst.data = copyOf(src.getData());
        dst.blockLight = new NibbleArray(src.getBlockLight().getData().clone());
        dst.skyLight = hasSky ? src.skyLight != null ? new NibbleArray(src.skyLight.getData().clone()) : new NibbleArray() : null;
        dst.blockRefCount = src.blockRefCount;
        dst.tickRefCount = src.tickRefCount;
    }

    /**
     * @return a deep copy of {@code src}
     */
    @Contract(value = "_ -> new", pure = true)
    public static ExtendedBlockStorage copyOf(ExtendedBlockStorage src) {
        ExtendedBlockStorage dst = UnsafeHolder.allocateInstance(ExtendedBlockStorage.class);
        dst.yBase = src.yBase;
        dst.data = copyOf(src.getData());
        dst.blockLight = new NibbleArray(src.getBlockLight().getData().clone());
        dst.skyLight = src.skyLight != null ? new NibbleArray(src.skyLight.getData().clone()) : null;
        dst.blockRefCount = src.blockRefCount;
        dst.tickRefCount = src.tickRefCount;
        return dst;
    }

    /**
     * Apply block-state changes to a chunk <em>in place</em> using a copy-on-write strategy and a
     * per-subchunk CAS publish. This method retries when the CAS fails due to concurrent swaps.
     *
     * <p>The provided {@code function} must return a map of <strong>global packed block position</strong>
     * (see {@link Library#blockPosToLong(int, int, int)}) to <strong>new</strong> {@link IBlockState}
     * for positions that should change. Entries outside the target chunk or outside world height are
     * ignored.</p>
     *
     * <p>For each sub-chunk touched, this method builds a working copy, applies changes, and attempts
     * a CAS publish. If the CAS fails due to a concurrent swap, it retries by reading the latest
     * source again. Two paths are used:
     * <ul>
     *   <li><em>Sparse path</em> (updates &lt; {@link #DENSE_THRESHOLD}): iterate only the changed
     *       indices.</li>
     *   <li><em>Dense path</em>: prebuild a 4096-entry override array and sweep 0..4095.</li>
     * </ul>
     * </p>
     *
     * <p>If {@code oldStatesOut} is non-null, it is populated <em>only after</em> a successful CAS
     * with the <strong>old</strong> states of all positions changed in that sub-chunk, using global
     * packed positions. Only entries that actually changed (identity compare) are emitted, and the
     * old state may be {@link Blocks#AIR}'s default state when the sub-chunk was empty.</p>
     *
     * @param chunk        the chunk to mutate
     * @param function     producer of desired mutations; may return {@code null} or an empty map to signal no-op;
     *                     values inside the returned map must be non-null
     * @param oldStatesOut optional sink for old states (global packed positions)
     * @throws NullPointerException if {@code chunk} or {@code function} is {@code null}, or if any
     *                              value in the map returned by {@code function.apply(chunk)} is {@code null}
     */
    @ThreadSafeMethod
    public static void applyAndSwap(Chunk chunk, Function<? super Chunk, ? extends @Nullable Long2ObjectOpenHashMap<@NotNull IBlockState>> function,
                                    @Nullable Long2ObjectMap<? super @NotNull IBlockState> oldStatesOut) {

        Long2ObjectOpenHashMap<IBlockState> newStates = function.apply(chunk);
        if (newStates == null || newStates.isEmpty()) return;

        WorldServer world = (WorldServer) chunk.getWorld();
        boolean hasSky = world.provider.hasSkyLight();
        int height = world.getHeight();
        int chunkX = chunk.x, chunkZ = chunk.z;
        ExtendedBlockStorage[] arr = chunk.getBlockStorageArray();

        Int2ObjectOpenHashMap<IBlockState>[] bySub = TL_BUCKET.get();
        for (Int2ObjectOpenHashMap<IBlockState> map : bySub) map.clear();

        // bucket updates per subY with local-packed indices
        ObjectIterator<Long2ObjectMap.Entry<IBlockState>> iterator = newStates.long2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<IBlockState> e = iterator.next();
            long p = e.getLongKey();
            int x = Library.getBlockPosX(p);
            int y = Library.getBlockPosY(p);
            int z = Library.getBlockPosZ(p);
            if ((x >> 4) != chunkX || (z >> 4) != chunkZ) continue;
            if (y < 0 || y >= height) continue;

            int subY = y >> 4;
            Int2ObjectOpenHashMap<IBlockState> b = bySub[subY];
            if (b == null) bySub[subY] = b = new Int2ObjectOpenHashMap<>();
            b.put(Library.blockPosToLocal(x, y, z), e.getValue());
        }

        Long2ObjectOpenHashMap<IBlockState> subScratch = oldStatesOut == null ? null : TL_SCRATCH.get();
        for (int subY = 0; subY < 16; subY++) {
            if (subScratch != null) subScratch.clear();
            Int2ObjectOpenHashMap<IBlockState> bucket = bySub[subY];
            if (bucket == null || bucket.isEmpty()) continue;

            if (bucket.size() < DENSE_THRESHOLD) {
                // sparse path
                while (true) {
                    ExtendedBlockStorage src = getEbsVolatile(arr, subY);
                    ExtendedBlockStorage working = null;
                    boolean any = false;

                    int yBase = subY << 4;
                    int xBase = chunkX << 4, zBase = chunkZ << 4;

                    for (Int2ObjectMap.Entry<IBlockState> e : bucket.int2ObjectEntrySet()) {
                        int local = e.getIntKey();
                        int lx = Library.getLocalX(local);
                        int ly = Library.getLocalY(local);
                        int lz = Library.getLocalZ(local);
                        IBlockState ns = e.getValue();
                        if (ns == null) throw new NullPointerException("newState");

                        IBlockState os = (src != null && !src.isEmpty()) ? src.get(lx, ly, lz) : AIR_DEFAULT_STATE;
                        if (os == ns) continue;

                        if (working == null) {
                            if (src != null && !src.isEmpty()) {
                                working = copyOf(src);
                            } else {
                                if (ns.getBlock() == Blocks.AIR) continue;
                                working = new ExtendedBlockStorage(yBase, hasSky);
                            }
                        }

                        working.set(lx, ly, lz, ns);
                        any = true;

                        if (subScratch != null) {
                            long gpos = Library.blockPosToLong(xBase | lx, yBase | ly, zBase | lz);
                            subScratch.put(gpos, os);
                        }
                    }

                    if (!any) break;
                    @SuppressWarnings("DataFlowIssue")
                    ExtendedBlockStorage update = working.isEmpty() ? null : working;
                    if (casEbsAt(src, update, arr, subY)) {
                        if (oldStatesOut != null) //noinspection DataFlowIssue
                            oldStatesOut.putAll(subScratch);
                        break;
                    } else if (subScratch != null) {
                        subScratch.clear();
                    }
                }
            } else {
                // Dense path: build a 4096 override array, then sweep 0..4095 using indexTo*
                IBlockState[] overrides = TL_OVERRIDES.get();
                Arrays.fill(overrides, null);
                for (Int2ObjectMap.Entry<IBlockState> e : bucket.int2ObjectEntrySet()) {
                    overrides[e.getIntKey()] = e.getValue();
                }

                while (true) {
                    ExtendedBlockStorage src = getEbsVolatile(arr, subY);
                    ExtendedBlockStorage working = null;
                    boolean any = false;

                    for (int idx = 0; idx < 4096; idx++) {
                        IBlockState newState = overrides[idx];
                        if (newState == null) continue;

                        int lx = Library.getLocalX(idx);
                        int ly = Library.getLocalY(idx);
                        int lz = Library.getLocalZ(idx);

                        IBlockState oldState = src == null || src.isEmpty() ? AIR_DEFAULT_STATE : src.get(lx, ly, lz);
                        if (oldState == newState) continue;

                        if (working == null) {
                            if (src == null || src.isEmpty()) {
                                working = new ExtendedBlockStorage(subY << 4, hasSky);
                            } else {
                                working = copyOf(src);
                            }
                        }

                        working.set(lx, ly, lz, newState);
                        any = true;

                        if (subScratch != null) {
                            int x = indexToX(idx, chunkX);
                            int y = indexToY(idx) | (subY << 4);
                            int z = indexToZ(idx, chunkZ);
                            subScratch.put(Library.blockPosToLong(x, y, z), oldState);
                        }
                    }

                    if (!any) break;
                    ExtendedBlockStorage update = working.isEmpty() ? null : working;
                    if (casEbsAt(src, update, arr, subY)) {
                        if (oldStatesOut != null) //noinspection DataFlowIssue
                            oldStatesOut.putAll(subScratch);
                        break;
                    } else if (subScratch != null) {
                        subScratch.clear();
                    }
                }
            }
        }
    }

    /**
     * Create a modified <em>copy</em> of a sub-chunk by applying local (0..4095) overrides and,
     * optionally, recording the <strong>old</strong> global states into {@code oldStatesOut}.
     *
     * @param chunkX       chunk X coordinate
     * @param chunkZ       chunk Z coordinate
     * @param subY         sub-chunk Y index (0..15)
     * @param hasSky       whether the world has skylight
     * @param src          source sub-chunk; may be {@code null} or empty
     * @param toUpdate     map of <em>packed local</em> index ({@code x | (z << 4) | (y << 8)}) → new state
     * @param oldStatesOut optional sink of pre-change states keyed by <em>global packed</em> positions
     * @return {@code null} for no-op, {@code Optional.empty()} for became empty,
     * or {@code Optional<ExtendedBlockStorage>} for a non-empty modified copy
     * @throws NullPointerException if any value in {@code toUpdate} is {@code null}
     */
    @ThreadSafeMethod
    @SuppressWarnings("OptionalAssignedToNull")
    public static @Nullable Optional<ExtendedBlockStorage> copyAndModify(int chunkX, int chunkZ, int subY, boolean hasSky,
                                                                         @Nullable ExtendedBlockStorage src,
                                                                         Int2ObjectMap<@NotNull IBlockState> toUpdate,
                                                                         @Nullable Long2ObjectMap<? super @NotNull IBlockState> oldStatesOut) {
        if (toUpdate.isEmpty()) return null;

        ExtendedBlockStorage dst = null;
        boolean anyChange = false;
        int xBase = chunkX << 4;
        int yBase = subY << 4;
        int zBase = chunkZ << 4;

        for (Int2ObjectMap.Entry<IBlockState> e : toUpdate.int2ObjectEntrySet()) {
            int packedLocal = e.getIntKey();
            int lx = Library.getLocalX(packedLocal);
            int ly = Library.getLocalY(packedLocal);
            int lz = Library.getLocalZ(packedLocal);

            IBlockState newState = e.getValue();
            if (newState == null) throw new NullPointerException("newState");

            IBlockState oldState = (src != null && !src.isEmpty()) ? src.get(lx, ly, lz) : AIR_DEFAULT_STATE;
            if (oldState == newState) continue;

            if (dst == null) {
                if (src != null && !src.isEmpty()) {
                    dst = copyOf(src);
                } else {
                    if (newState.getBlock() == Blocks.AIR) continue;
                    dst = new ExtendedBlockStorage(yBase, hasSky);
                }
            }

            // record OLD state before change
            if (oldStatesOut != null) {
                int xGlobal = xBase | lx;
                int yGlobal = yBase | ly;
                int zGlobal = zBase | lz;
                oldStatesOut.put(Library.blockPosToLong(xGlobal, yGlobal, zGlobal), oldState);
            }

            dst.set(lx, ly, lz, newState);
            anyChange = true;
        }

        if (!anyChange) return null;
        return dst.isEmpty() ? Optional.empty() : Optional.of(dst);
    }

    /**
     * Server-thread fix-up for TileEntity lifecycle around a block-state transition at {@code pos}.
     *
     * <p>Calls {@link Block#breakBlock(World, BlockPos, IBlockState)} on the old block if the block
     * type changes, removes the existing TE if {@link TileEntity#shouldRefresh(World, BlockPos, IBlockState, IBlockState)}
     * says so, and then creates/attaches a new TE if the new block has one. Finally calls
     * {@link TileEntity#updateContainingBlockInfo()} on the TE if present. This mirrors the TE
     * lifecycle parts of {@code setBlockState} without neighbor notifications and may trigger block
     * drops/cleanup via {@code breakBlock} when the type changes.</p>
     *
     * @param chunk    the chunk containing {@code pos}
     * @param pos      target position
     * @param oldState previous block state
     * @param newState new block state
     */
    @ServerThread
    public static void flushTileEntity(Chunk chunk, BlockPos pos, IBlockState oldState, IBlockState newState) {
        World world = chunk.getWorld();
        Block oldBlock = oldState.getBlock();
        Block newBlock = newState.getBlock();
        if (oldBlock != newBlock) oldBlock.breakBlock(world, pos, oldState);
        TileEntity te = chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
        if (te != null && te.shouldRefresh(world, pos, oldState, newState)) world.removeTileEntity(pos);
        Block block = newState.getBlock();
        if (!block.hasTileEntity(newState)) return;
        TileEntity newTileEntity = chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
        if (newTileEntity == null) {
            newTileEntity = block.createTileEntity(world, newState);
            if (newTileEntity != null) world.setTileEntity(pos, newTileEntity);
        }
        if (newTileEntity != null) newTileEntity.updateContainingBlockInfo();
    }

    /**
     * Convert a local 0..4095 index to a global X coordinate for the given chunk X.
     */
    @Contract(pure = true)
    public static int indexToX(int index, int chunkX) {
        return (chunkX << 4) | (index & 15);
    }

    /**
     * Extract the local Y (0..15) from a 0..4095 local index.
     */
    @Contract(pure = true)
    public static int indexToY(int index) {
        return index >>> 8;
    }

    /**
     * Convert a local 0..4095 index to a global Z coordinate for the given chunk Z.
     */
    @Contract(pure = true)
    public static int indexToZ(int index, int chunkZ) {
        return (chunkZ << 4) | ((index >>> 4) & 15);
    }

    /**
     * Lazy neighbor storage cache for edge-contact checks while carving.
     */
    private static final class NeighborCache {
        @Nullable ExtendedBlockStorage[] negX, posX, negZ, posZ;
    }
}
