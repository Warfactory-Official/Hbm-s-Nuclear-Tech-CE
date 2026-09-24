package com.hbm.render.chunk;

import com.hbm.lib.Library;
import com.hbm.util.SectionKeyHash;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

/** Client thread only; compile tasks read snapshots. */
@SideOnly(Side.CLIENT)
final class SectionGeometryIndex {
    private static final long[] NO_SECTIONS = new long[0];
    private final Long2IntOpenHashMap owners = new Long2IntOpenHashMap();
    private final Long2ObjectOpenCustomHashMap<long[]> sections = new Long2ObjectOpenCustomHashMap<>(SectionKeyHash.STRATEGY);
    private final Long2ObjectOpenHashMap<IntArrayList> chunkOwners = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<long[]> tiles = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenCustomHashMap<ReferenceArrayList<TileEntity>> sectionTiles =
            new Long2ObjectOpenCustomHashMap<>(SectionKeyHash.STRATEGY);
    private final Long2ObjectOpenHashMap<LongOpenHashSet> chunkTiles = new Long2ObjectOpenHashMap<>();
    private final LongOpenHashSet loadedChunks = new LongOpenHashSet();
    private final IntArrayList free = new IntArrayList();
    private long[] positions = new long[16];
    private IBlockState[] states = new IBlockState[16];
    private SectionGeometryLayout[] layouts = new SectionGeometryLayout[16];
    private int[][] contributionSlots = new int[16][];
    private int[] chunkSlots = new int[16];
    private int used;

    SectionGeometryIndex() {
        owners.defaultReturnValue(-1);
    }

    private static long worldSection(BlockPos pos, long local) {
        return Library.sectionToLong((pos.getX() >> 4) + Library.getSectionX(local),
                (pos.getY() >> 4) + Library.getSectionY(local), (pos.getZ() >> 4) + Library.getSectionZ(local));
    }

    private static void dirty(long section) {
        int x = Library.getSectionX(section) << 4, y = Library.getSectionY(section) << 4;
        int z = Library.getSectionZ(section) << 4;
        Minecraft.getMinecraft().renderGlobal.markBlockRangeForRenderUpdate(x + 1, y + 1, z + 1, x + 14, y + 14, z + 14);
    }

    boolean has(long section) {
        return sections.containsKey(section) || sectionTiles.containsKey(section);
    }

    void update(BlockPos pos, IBlockState state) {
        long key = pos.toLong();
        int previous = owners.get(key);
        var mesh = SectionGeometry.mesh(state);
        if (previous != -1 && states[previous] == state) return;
        if (previous != -1) remove(previous);
        if (mesh == null) return;
        int id = free.isEmpty() ? used++ : free.removeInt(free.size() - 1);
        if (id == positions.length) {
            int capacity = positions.length * 2;
            positions = Arrays.copyOf(positions, capacity);
            states = Arrays.copyOf(states, capacity);
            layouts = Arrays.copyOf(layouts, capacity);
            contributionSlots = Arrays.copyOf(contributionSlots, capacity);
            chunkSlots = Arrays.copyOf(chunkSlots, capacity);
        }
        owners.put(key, id);
        positions[id] = key;
        states[id] = state;
        layouts[id] = SectionGeometry.acquire(mesh, pos);
        var chunkRow = chunkOwners.computeIfAbsent(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4),
                k -> new IntArrayList());
        chunkSlots[id] = chunkRow.size();
        chunkRow.add(id);
        addParts(id);
    }

    private void addParts(int id) {
        BlockPos pos = BlockPos.fromLong(positions[id]);
        var layout = layouts[id];
        int[] slots = new int[layout.sections.length];
        Arrays.fill(slots, -1);
        contributionSlots[id] = slots;
        for (int part = 0; part < layout.sections.length; part++) {
            long section = worldSection(pos, layout.sections[part]);
            int sectionY = Library.getSectionY(section);
            if (sectionY < 0 || sectionY > 15) continue;
            long[] row = sections.get(section);
            if (row == null) {
                row = new long[6];
                sections.put(section, row);
            }
            int size = (int) row[0];
            if (size + 2 == row.length) {
                // A nearly full row needs slack; compacting one hole per replacement would be quadratic.
                if (row[1] * 2 <= size) {
                    compactContributions(row);
                    size = (int) row[0];
                } else {
                    row = Arrays.copyOf(row, row.length * 2);
                    sections.put(section, row);
                }
            }
            slots[part] = size + 2;
            row[size + 2] = (long) id << 32 | part;
            row[0] = size + 1;
            row[1]++;
            dirty(section);
        }
    }

    private void removeParts(int id) {
        BlockPos pos = BlockPos.fromLong(positions[id]);
        long[] local = layouts[id].sections;
        for (int part = 0; part < local.length; part++) {
            int at = contributionSlots[id][part];
            if (at < 0) continue;
            long section = worldSection(pos, local[part]);
            long[] row = sections.get(section);
            assert row[at] == ((long) id << 32 | part);
            row[at] = -1;
            contributionSlots[id][part] = -1;
            if (--row[1] == 0) sections.remove(section);
            dirty(section);
        }
    }

    private void compactContributions(long[] row) {
        if (row[0] == row[1]) return;
        int write = 2;
        int end = (int) row[0] + 2;
        for (int read = 2; read < end; read++) {
            long contribution = row[read];
            if (contribution < 0) continue;
            row[write] = contribution;
            contributionSlots[(int) (contribution >>> 32)][(int) contribution] = write++;
        }
        row[0] = write - 2;
        assert row[0] == row[1];
    }

    private void remove(int id) {
        BlockPos pos = BlockPos.fromLong(positions[id]);
        removeParts(id);
        owners.remove(positions[id]);
        long chunk = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        var row = chunkOwners.get(chunk);
        int at = chunkSlots[id];
        int moved = row.removeInt(row.size() - 1);
        if (moved != id) {
            row.set(at, moved);
            chunkSlots[moved] = at;
        }
        if (row.isEmpty()) chunkOwners.remove(chunk);
        states[id] = null;
        SectionGeometry.release(layouts[id]);
        layouts[id] = null;
        contributionSlots[id] = null;
        free.add(id);
    }

    void tile(TileEntity tile, boolean dirtyOwn) {
        long key = tile.getPos().toLong();
        long[] previous = tiles.get(key);
        long[] next = NO_SECTIONS;
        boolean global = false;
        if (TileEntityRendererDispatcher.instance.getRenderer(tile) != null) {
            AxisAlignedBB box = tile.getRenderBoundingBox();
            if (box == TileEntity.INFINITE_EXTENT_AABB) {
                global = true;
            } else {
                int ownX = tile.getPos().getX() >> 4, ownY = tile.getPos().getY() >> 4, ownZ = tile.getPos().getZ() >> 4;
                int minX = MathHelper.floor(box.minX) >> 4, maxX = MathHelper.ceil(box.maxX) - 1 >> 4;
                int minY = MathHelper.floor(box.minY) >> 4, maxY = MathHelper.ceil(box.maxY) - 1 >> 4;
                int minZ = MathHelper.floor(box.minZ) >> 4, maxZ = MathHelper.ceil(box.maxZ) - 1 >> 4;
                global = minX < ownX - 1 || maxX > ownX + 1 || minY < ownY - 1 || maxY > ownY + 1
                        || minZ < ownZ - 1 || maxZ > ownZ + 1;
                if (!global) {
                    minY = Math.max(minY, 0);
                    maxY = Math.min(maxY, 15);
                    long[] owned = new long[(maxX - minX + 1) * Math.max(maxY - minY + 1, 0) * (maxZ - minZ + 1)];
                    int count = 0;
                    for (int x = minX; x <= maxX; x++) {
                        for (int y = minY; y <= maxY; y++) {
                            for (int z = minZ; z <= maxZ; z++) {
                                if (x != ownX || y != ownY || z != ownZ) owned[count++] = Library.sectionToLong(x, y, z);
                            }
                        }
                    }
                    if (count != 0) next = Arrays.copyOf(owned, count);
                }
            }
        }
        ISectionGeometryTile flag = (ISectionGeometryTile) tile;
        if (flag.hbm$globalRender() != global) {
            flag.hbm$globalRender(global);
            if (dirtyOwn) dirty(Library.blockPosToSectionLong(tile.getPos()));
        }
        if (previous != null && Arrays.equals(previous, next) && sectionTiles.get(previous[0]).contains(tile)) return;
        if (previous != null) removeTile(key);
        if (next.length == 0) return;
        tiles.put(key, next);
        chunkTiles.computeIfAbsent(ChunkPos.asLong(tile.getPos().getX() >> 4, tile.getPos().getZ() >> 4),
                k -> new LongOpenHashSet()).add(key);
        for (long section : next) {
            sectionTiles.computeIfAbsent(section, k -> new ReferenceArrayList<>()).add(tile);
            dirty(section);
        }
    }

    void removeTile(long key) {
        long[] owned = tiles.remove(key);
        if (owned == null) return;
        BlockPos pos = BlockPos.fromLong(key);
        long chunk = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        var row = chunkTiles.get(chunk);
        row.remove(key);
        if (row.isEmpty()) chunkTiles.remove(chunk);
        for (long section : owned) {
            var members = sectionTiles.get(section);
            members.removeIf(tile -> tile.getPos().toLong() == key);
            if (members.isEmpty()) sectionTiles.remove(section);
            dirty(section);
        }
    }

    void load(Chunk chunk) {
        long chunkKey = ChunkPos.asLong(chunk.x, chunk.z);
        loadedChunks.add(chunkKey);
        var previous = chunkOwners.get(chunkKey);
        if (previous != null) {
            for (int id : previous.toIntArray()) {
                BlockPos pos = BlockPos.fromLong(positions[id]);
                update(pos, chunk.getBlockState(pos));
            }
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (ExtendedBlockStorage storage : chunk.getBlockStorageArray()) {
            if (storage == Chunk.NULL_BLOCK_STORAGE || storage.isEmpty()) continue;
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        IBlockState state = storage.get(x, y, z);
                        if (!((ISectionGeometryState) state).hbm$sectioned()) continue;
                        pos.setPos(chunk.x << 4 | x, storage.getYLocation() + y, chunk.z << 4 | z);
                        update(pos.toImmutable(), state);
                    }
                }
            }
        }
        var tileRow = chunkTiles.get(chunkKey);
        if (tileRow != null) {
            for (long key : tileRow.toLongArray()) {
                TileEntity tile = chunk.getTileEntityMap().get(BlockPos.fromLong(key));
                if (tile == null) removeTile(key);
            }
        }
        for (TileEntity tile : chunk.getTileEntityMap().values()) tile(tile, true);
    }

    void unload(int chunkX, int chunkZ) {
        long chunkKey = ChunkPos.asLong(chunkX, chunkZ);
        loadedChunks.remove(chunkKey);
        var row = chunkOwners.get(chunkKey);
        if (row != null) {
            while (!row.isEmpty()) remove(row.getInt(row.size() - 1));
        }
        var tileRow = chunkTiles.get(chunkKey);
        if (tileRow != null) {
            for (long key : tileRow.toLongArray()) removeTile(key);
        }
    }

    void reload(World world) {
        long[] chunks = loadedChunks.toLongArray();
        clear();
        for (long key : chunks) {
            Chunk chunk = world.getChunkProvider().getLoadedChunk((int) key, (int) (key >> 32));
            if (chunk != null) load(chunk);
        }
    }

    void clear() {
        for (int id : owners.values()) SectionGeometry.release(layouts[id]);
        owners.clear();
        sections.clear();
        chunkOwners.clear();
        tiles.clear();
        sectionTiles.clear();
        chunkTiles.clear();
        loadedChunks.clear();
        free.clear();
        Arrays.fill(states, null);
        Arrays.fill(layouts, null);
        Arrays.fill(contributionSlots, null);
        used = 0;
    }

    /** The compile being captured reads the refreshed global flags itself. */
    SectionGeometry.@Nullable Snapshot capture(@Nullable Chunk chunk, int sectionX, int sectionY, int sectionZ) {
        if (chunk != null) {
            for (TileEntity tile : chunk.getTileEntityMap().values()) {
                if (tile.getPos().getY() >> 4 == sectionY) tile(tile, false);
            }
        }
        long section = Library.sectionToLong(sectionX, sectionY, sectionZ);
        long[] row = sections.get(section);
        var members = sectionTiles.get(section);
        if (row == null && members == null) return null;
        var parts = new ArrayList<SectionGeometry.Part>();
        if (row != null) {
            compactContributions(row);
            int count = (int) row[0];
            for (int i = 2; i < count + 2; i++) {
                long contribution = row[i];
                int id = (int) (contribution >>> 32);
                BlockPos pos = BlockPos.fromLong(positions[id]);
                parts.add(new SectionGeometry.Part(states[id], SectionGeometry.mesh(states[id]), layouts[id],
                        (int) contribution, pos.getX() - (sectionX << 4), pos.getY() - (sectionY << 4),
                        pos.getZ() - (sectionZ << 4)));
            }
        }
        return new SectionGeometry.Snapshot(sectionX << 4, sectionY << 4, sectionZ << 4,
                parts.toArray(new SectionGeometry.Part[0]),
                members == null ? new TileEntity[0] : members.toArray(new TileEntity[0]));
    }
}
