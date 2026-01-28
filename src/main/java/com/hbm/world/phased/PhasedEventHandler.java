package com.hbm.world.phased;

import com.hbm.config.StructureConfig;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.util.BufferUtil;
import com.hbm.world.phased.DynamicStructureDispatcher.PendingDynamicStructure;
import com.hbm.world.phased.PhasedStructureGenerator.PhasedStructureStart;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

/**
 * Handles persistence and event routing for the phased worldgen system.
 * <ul>
 *   <li><b>Load Sequence:</b>
 *     <ol>
 *       <li>{@link ChunkDataEvent.Load} - Fired early. Data is read, structures restored, and tasks registered.</li>
 *       <li>{@link ChunkEvent.Load} - Fired on main thread. Triggers {@link PhasedStructureGenerator#generateForChunkFast} to process registered tasks.</li>
 *     </ol>
 *   </li>
 *   <li><b>Unload Sequence:</b>
 *     <ol>
 *       <li>{@link ChunkEvent.Unload} - Fired first. Chunk is marked for unload.</li>
 *       <li>{@link ChunkDataEvent.Save} - Fired after unload. Data is serialized.</li>
 *     </ol>
 *   </li>
 *   <li><b>Periodic Save:</b>
 *     <ul>
 *       <li>{@link ChunkDataEvent.Save} fires <b>without</b> a preceding {@link ChunkEvent.Unload}.</li>
 *     </ul>
 *   </li>
 * </ul>
 */
public final class PhasedEventHandler {

    public static final PhasedEventHandler INSTANCE = new PhasedEventHandler();

    private static final String TAG_DATA = "HbmPhasedData";
    private static final int TAG_VERSION = 1;

    private static final String TAG_PHASED_LEGACY = "HbmPhasedStructures";
    private static final String TAG_DYNAMIC_LEGACY = "HbmDynamicJobs";
    private static final ByteBuf SHARED_DIRECT_BUF = Unpooled.directBuffer(4096);
    private static final Int2ObjectOpenHashMap<DimensionState> STATES = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectOpenHashMap<ArrayList<DynamicStructureDispatcher.PendingDynamicStructure>> DYNAMIC_GROUPS = new Int2ObjectOpenHashMap<>();

    private PhasedEventHandler() {
    }

    static ByteBuf borrowSharedDirectBuffer() {
        SHARED_DIRECT_BUF.clear();
        return SHARED_DIRECT_BUF;
    }

    static DimensionState getState(WorldServer world) {
        int dim = world.provider.getDimension();
        DimensionState state = STATES.get(dim);
        if (state != null) return state;
        state = new DimensionState(dim, world);
        STATES.put(dim, state);
        return state;
    }

    static DimensionState getState(int dim) {
        DimensionState state = STATES.get(dim);
        if (state == null) {
            throw new NullPointerException("Phased DimensionState not initialized for dim " + dim);
        }
        return state;
    }

    private static void removeLegacyTags(NBTTagCompound data) {
        data.removeTag(TAG_PHASED_LEGACY);
        data.removeTag(TAG_DYNAMIC_LEGACY);
    }

    private static int writePhasedSection(ByteBuf out, WorldServer world, long chunkKey) {
        DimensionState state = getState(world);

        ArrayList<PhasedStructureStart> bucket = state.structureMap.get(chunkKey);
        if (bucket == null || bucket.isEmpty()) return 0;

        var validCount = 0;
        for (PhasedStructureStart start : bucket) {
            if (start == null || !start.isSerializable()) continue;
            validCount++;
        }
        if (validCount == 0) return 0;

        var startIndex = out.writerIndex();
        BufferUtil.writeVarInt(out, validCount);

        var actuallyWritten = 0;
        for (PhasedStructureStart start : bucket) {
            if (start == null || !start.isSerializable()) continue;

            var entryStart = out.writerIndex();
            try {
                start.writeToBuf(out);
                actuallyWritten++;
            } catch (Exception e) {
                out.writerIndex(entryStart);
                MainRegistry.logger.warn("Failed to serialize phased structure: {}", e.getMessage());
            }
        }

        // If count mismatch, reset entire section
        if (actuallyWritten != validCount) {
            out.writerIndex(startIndex);
            if (actuallyWritten == 0) return 0;

            BufferUtil.writeVarInt(out, actuallyWritten);
            for (PhasedStructureStart start : bucket) {
                if (start == null || !start.isSerializable()) continue;
                try {
                    start.writeToBuf(out);
                } catch (Exception e) {
                    MainRegistry.logger.warn("Failed to serialize phased structure in fallback loop: {}", e.getMessage());
                }
            }
        }

        return out.writerIndex() - startIndex;
    }

    private static void readPhasedSection(ByteBuf in, WorldServer world, long chunkKey) {
        if (!in.isReadable()) return;

        DimensionState state = getState(world);

        ArrayList<PhasedStructureStart> existing = state.structureMap.get(chunkKey);
        var skipLoad = existing != null && !existing.isEmpty();

        int count;
        try {
            count = BufferUtil.readVarInt(in);
        } catch (Exception ex) {
            MainRegistry.logger.warn("Failed to read phased structure count for chunk {},{}", Library.getChunkPosX(chunkKey), Library.getChunkPosZ(chunkKey));
            return;
        }
        if (count <= 0 || count > 1_000_000) return;

        ArrayList<PhasedStructureStart> bucket = null;
        ArrayList<PhasedStructureStart> loaded = null;
        if (!skipLoad) {
            bucket = new ArrayList<>(count);
            loaded = new ArrayList<>(count);
            state.structureMap.put(chunkKey, bucket);
        }

        try {
            for (int i = 0; i < count; i++) {
                if (skipLoad) {
                    skipPhasedEntry(in);
                    continue;
                }

                int entryStart = in.readerIndex();
                var start = PhasedStructureStart.borrow(state);
                boolean ok;
                try {
                    ok = start.readFromBuf(state, in);
                } catch (Exception ex) {
                    MainRegistry.logger.warn("Exception during phased structure readFromBuf for chunk {},{}", Library.getChunkPosX(chunkKey), Library.getChunkPosZ(chunkKey), ex);
                    ok = false;
                }
                if (!ok) {
                    PhasedStructureStart.recycle(start);
                    try {
                        in.readerIndex(entryStart);
                        skipPhasedEntry(in);
                    } catch (Exception ex) {
                        MainRegistry.logger.warn("Critical failure skipping invalid phased entry for chunk {},{}", Library.getChunkPosX(chunkKey), Library.getChunkPosZ(chunkKey), ex);
                        break;
                    }
                    continue;
                }
                if (ChunkPos.asLong(start.chunkPosX, start.chunkPosZ) != chunkKey) {
                    PhasedStructureStart.recycle(start);
                    continue;
                }

                start.registerTasksForRemaining();
                if (!start.isSerializable()) {
                    PhasedStructureStart.recycle(start);
                    continue;
                }
                bucket.add(start);
                start.generateExistingChunks();
                loaded.add(start);
            }
        } catch (Exception ex) {
            if (bucket != null) {
                for (PhasedStructureStart start : bucket) {
                    PhasedStructureStart.recycle(start);
                }
            }
            state.structureMap.remove(chunkKey);
            MainRegistry.logger.warn("Failed to decode packed phased structures for chunk {},{}", Library.getChunkPosX(chunkKey), Library.getChunkPosZ(chunkKey), ex);
            return;
        }

        if (skipLoad) return;

        if (bucket.isEmpty()) {
            state.structureMap.remove(chunkKey);
        }
    }

    private static void skipPhasedEntry(ByteBuf in) throws IllegalStateException {
        if (in.readableBytes() < 1) throw new IllegalStateException("Buffer underflow at structure ID");
        var structureId = BufferUtil.readVarInt(in);
        if (structureId < 0) {
            throw new IllegalStateException("Negative structure ID encountered: " + structureId);
        }

        if (in.readableBytes() < 8 + 8 + 1) throw new IllegalStateException("Buffer underflow at fixed fields");
        in.skipBytes(8 + 8); // layoutSeed + origin
        in.skipBytes(1); // postGenerated

        // Skip remaining chunks (delta-encoded, 2 VarInts per chunk)
        if (in.readableBytes() < 1) throw new IllegalStateException("Buffer underflow at remaining count");
        var remainingCount = BufferUtil.readVarInt(in);
        if (remainingCount < 0 || remainingCount > 1_000_000)
            throw new IllegalStateException("Invalid remaining count: " + remainingCount);
        for (int i = 0; i < remainingCount; i++) {
            BufferUtil.readVarInt(in); // relX (ZigZag)
            BufferUtil.readVarInt(in); // relZ (ZigZag)
        }

        // Skip processed chunks (delta-encoded, 2 VarInts per chunk)
        if (in.readableBytes() < 1) throw new IllegalStateException("Buffer underflow at processed count");
        var processedCount = BufferUtil.readVarInt(in);
        if (processedCount < 0 || processedCount > 1_000_000)
            throw new IllegalStateException("Invalid processed count: " + processedCount);
        for (int i = 0; i < processedCount; i++) {
            BufferUtil.readVarInt(in); // relX (ZigZag)
            BufferUtil.readVarInt(in); // relZ (ZigZag)
        }

        if (in.readableBytes() < 4) throw new IllegalStateException("Buffer underflow at data length");
        var dataLen = in.readInt();
        if (dataLen < 0 || dataLen > in.readableBytes())
            throw new IllegalStateException("Invalid data length: " + dataLen);
        if (dataLen > 0) {
            in.skipBytes(dataLen);
        }
    }

    private static int writeDynamicSection(ByteBuf out, WorldServer world, long chunkKey) {
        DimensionState state = getState(world);

        ArrayList<PendingDynamicStructure> jobs = state.jobsByOriginChunk.get(chunkKey);
        if (jobs == null || jobs.isEmpty()) return 0;

        // Group jobs by structure ID
        DYNAMIC_GROUPS.clear();
        for (PendingDynamicStructure job : jobs) {
            var structure = job == null ? null : job.getStructure();
            if (structure == null) continue;
            int structureId = PhasedStructureRegistry.getId(structure);
            if (structureId < 0) continue;
            DYNAMIC_GROUPS.computeIfAbsent(structureId, k -> new ArrayList<>()).add(job);
        }

        if (DYNAMIC_GROUPS.isEmpty()) return 0;

        var startIndex = out.writerIndex();
        BufferUtil.writeVarInt(out, DYNAMIC_GROUPS.size());

        int originChunkX = Library.getChunkPosX(chunkKey);
        int originChunkZ = Library.getChunkPosZ(chunkKey);

        var iterator = DYNAMIC_GROUPS.int2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            int structureId = entry.getIntKey();
            ArrayList<PendingDynamicStructure> groupJobs = entry.getValue();

            BufferUtil.writeVarInt(out, structureId);
            BufferUtil.writeVarInt(out, groupJobs.size());

            for (PendingDynamicStructure job : groupJobs) {
                int absX = Library.getBlockPosX(job.getOrigin());
                int absY = Library.getBlockPosY(job.getOrigin());
                int absZ = Library.getBlockPosZ(job.getOrigin());

                // Compact relative origin: 4 bits X, 8 bits Y, 4 bits Z
                int relX = absX - (originChunkX << 4);
                int relZ = absZ - (originChunkZ << 4);

                BufferUtil.writePosCompact(out, relX, absY, relZ);
                out.writeLong(job.getLayoutSeed());

                var lenIndex = out.writerIndex();
                out.writeInt(0);

                var payloadStart = out.writerIndex();
                try {
                    PhasedStructureRegistry.serialize(job.getStructure(), out);
                } catch (Exception ex) {
                    MainRegistry.logger.warn("Failed to serialize dynamic structure {} at {}: {}", job.getStructure().getClass().getSimpleName(), job.getOrigin(), ex.getMessage());
                    out.writerIndex(payloadStart);
                }

                var len = out.writerIndex() - payloadStart;
                out.setInt(lenIndex, len);
            }
        }

        return out.writerIndex() - startIndex;
    }

    private static void readDynamicSection(ByteBuf in, WorldServer world, long chunkKey) {
        DimensionState state = getState(world);

        var skipLoad = state.jobsByOriginChunk.containsKey(chunkKey);

        var groupCount = BufferUtil.readVarInt(in);
        if (groupCount < 0 || groupCount > 10_000_000) return;

        int originChunkX = Library.getChunkPosX(chunkKey);
        int originChunkZ = Library.getChunkPosZ(chunkKey);

        var provider = world.getChunkProvider();
        long worldSeed = state.worldSeed;

        for (int g = 0; g < groupCount; g++) {
            var structureId = BufferUtil.readVarInt(in);
            var jobCount = BufferUtil.readVarInt(in);

            for (int i = 0; i < jobCount; i++) {
                if (in.readableBytes() < 2 + 8 + 4) return; // relOrigin + layoutSeed + dataLen

                long relPos = BufferUtil.readPosCompact(in);
                long layoutSeed = in.readLong();
                var dataLen = in.readInt();

                if (dataLen < 0 || in.readableBytes() < dataLen) return;

                if (skipLoad) {
                    in.skipBytes(dataLen);
                    continue;
                }

                long absOrigin = Library.blockPosToLong((originChunkX << 4) + Library.getBlockPosX(relPos), Library.getBlockPosY(relPos), (originChunkZ << 4) + Library.getBlockPosZ(relPos));

                IPhasedStructure decoded;
                try {
                    decoded = PhasedStructureRegistry.deserializeById(structureId, in, dataLen);
                } catch (Exception ex) {
                    MainRegistry.logger.warn("Failed to deserialize dynamic job id={} at origin {}", structureId, absOrigin, ex);
                    continue;
                }

                if (!(decoded instanceof AbstractPhasedStructure structure)) {
                    continue;
                }

                LongList watched = resolveOffsets(structure, absOrigin);
                var job = PendingDynamicStructure.borrow(structure, absOrigin, worldSeed, layoutSeed, watched);

                if (job.evaluate(provider)) {
                    job.run(world);
                } else {
                    ArrayList<PendingDynamicStructure> bucket = state.jobsByOriginChunk.computeIfAbsent(chunkKey, k -> new ArrayList<>(4));
                    bucket.add(job);
                    job.registerWaiting(state.waitingJobs);
                }
            }
        }
    }

    private static LongList resolveOffsets(AbstractPhasedStructure structure, long origin) {
        var offsets = structure.getWatchedChunkOffsets(origin);
        if (offsets == null || offsets.isEmpty()) {
            return PhasedConstants.ORIGIN_ONLY;
        }
        return offsets;
    }

    static void processChunkAvailable(WorldServer world, long chunkKey) {
        DimensionState state = getState(world);

        ReferenceLinkedOpenHashSet<AbstractChunkWaitJob> waiters = state.waitingJobs.remove(chunkKey);
        if (waiters == null || waiters.isEmpty()) return;

        for (AbstractChunkWaitJob job : waiters) {
            job.waitingOn.remove(chunkKey);

            if (job.waitingOn.isEmpty()) {
                job.onJobReady(state, world);
            }
        }
    }

    public static void onServerStopped() {
        STATES.clear();
        DYNAMIC_GROUPS.clear();
    }

    @SubscribeEvent
    public void onChunkPopulated(PopulateChunkEvent.Post event) {
        var world = event.getWorld();
        if (world.isRemote) return;

        processChunkAvailable((WorldServer) world, ChunkPos.asLong(event.getChunkX(), event.getChunkZ()));
    }

    @SubscribeEvent
    public void onChunkLoaded(ChunkEvent.Load event) {
        var world = event.getWorld();
        if (world.isRemote) return;
        WorldServer server = (WorldServer) world;
        var chunk = event.getChunk();
        if (!chunk.isTerrainPopulated()) return;

        DimensionState state = getState(server);

        processChunkAvailable((WorldServer) world, ChunkPos.asLong(chunk.x, chunk.z));
        PhasedStructureGenerator.generateForChunkFast(world, state, chunk.x, chunk.z);
    }

    @SubscribeEvent
    public void onChunkDataSave(ChunkDataEvent.Save event) {
        World world = event.getWorld();
        if (world.isRemote) return;
        WorldServer server = (WorldServer) world;

        NBTTagCompound data = event.getData();
        removeLegacyTags(data);

        Chunk chunk = event.getChunk();
        long chunkKey = ChunkPos.asLong(chunk.x, chunk.z);

        ByteBuf out = borrowSharedDirectBuffer();
        BufferUtil.writeVarInt(out, TAG_VERSION);
        BufferUtil.writeVarInt(out, PhasedStructureRegistry.getEpoch());

        // Write BOTH length placeholders upfront so they're adjacent
        int phasedLenIndex = out.writerIndex();
        out.writeInt(0);
        int dynamicLenIndex = out.writerIndex();
        out.writeInt(0);

        // Now write phased data
        int phasedStart = out.writerIndex();
        int phasedLen = writePhasedSection(out, server, chunkKey);
        if (phasedLen == 0) {
            out.writerIndex(phasedStart);
        }
        out.setInt(phasedLenIndex, phasedLen);

        // Now write dynamic data
        int dynamicStart = out.writerIndex();
        int dynamicLen = StructureConfig.enableDynamicStructureSaving ? writeDynamicSection(out, server, chunkKey) : 0;
        if (dynamicLen == 0) {
            out.writerIndex(dynamicStart);
        }
        out.setInt(dynamicLenIndex, dynamicLen);

        if (phasedLen == 0 && dynamicLen == 0) {
            data.removeTag(TAG_DATA);
            return;
        }

        byte[] bytes = new byte[out.writerIndex()];
        out.getBytes(0, bytes);
        data.setByteArray(TAG_DATA, bytes);
        MainRegistry.logger.debug("[PHASED SAVE] chunk {},{}: phasedLen={}, dynamicLen={}, totalBytes={}", chunk.x, chunk.z, phasedLen, dynamicLen, bytes.length);
    }

    @SubscribeEvent
    public void onChunkDataLoad(ChunkDataEvent.Load event) {
        World world = event.getWorld();
        if (world.isRemote) return;
        WorldServer server = (WorldServer) world;

        NBTTagCompound data = event.getData();
        removeLegacyTags(data);

        if (!data.hasKey(TAG_DATA, Constants.NBT.TAG_BYTE_ARRAY)) return;

        byte[] bytes = data.getByteArray(TAG_DATA);
        if (bytes.length == 0) return;

        Chunk chunk = event.getChunk();
        long chunkKey = ChunkPos.asLong(chunk.x, chunk.z);

        try {
            ByteBuf in = Unpooled.wrappedBuffer(bytes);
            int version = BufferUtil.readVarInt(in);
            if (version != TAG_VERSION) {
                return;
            }

            int storedEpoch = BufferUtil.readVarInt(in);
            if (storedEpoch < PhasedStructureRegistry.getEpoch()) {
                return;
            }

            int phasedLen = in.readInt();
            int dynamicLen = in.readInt();
            if (phasedLen < 0 || dynamicLen < 0 || in.readableBytes() < phasedLen + dynamicLen) {
                return;
            }

            if (phasedLen > 0) {
                MainRegistry.logger.debug("[PHASED LOAD] chunk {},{}: phasedLen={}, dynamicLen={}, readable={}", chunk.x, chunk.z, phasedLen, dynamicLen, in.readableBytes());
                readPhasedSection(in.readSlice(phasedLen), server, chunkKey);
            }

            if (StructureConfig.enableDynamicStructureSaving && dynamicLen > 0) {
                readDynamicSection(in.readSlice(dynamicLen), server, chunkKey);
            }
        } catch (Exception ex) {
            MainRegistry.logger.warn("Failed to decode packed phased data for chunk {},{}", chunk.x, chunk.z, ex);
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        var world = event.getWorld();
        if (world.isRemote) return;
        STATES.put(world.provider.getDimension(), new DimensionState(world.provider.getDimension(), (WorldServer) world));
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        World world = event.getWorld();
        if (world.isRemote) return;
        DimensionState state = STATES.remove(world.provider.getDimension());
        if (state != null) {
            PhasedStructureGenerator.clearState(state);
        }
    }

    static abstract class AbstractChunkWaitJob {
        final LongOpenHashSet waitingOn = new LongOpenHashSet(16);

        abstract void onJobReady(DimensionState state, WorldServer world);
    }
}
