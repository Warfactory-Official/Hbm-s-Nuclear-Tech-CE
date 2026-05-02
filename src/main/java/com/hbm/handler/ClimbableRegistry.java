package com.hbm.handler;

import com.hbm.interfaces.IClimbable;
import com.hbm.lib.Library;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

@ParametersAreNonnullByDefault
public final class ClimbableRegistry {
    private static final int SECTION_PROMOTION_THRESHOLD = 8;
    private static final Int2ObjectMap<Long2ObjectOpenHashMap<Bucket>> CLIENT_BY_DIM = new Int2ObjectOpenHashMap<>();
    private static final IdentityHashMap<IClimbable, Entry> CLIENT_REVERSE = new IdentityHashMap<>();
    private static final Int2ObjectMap<Long2ObjectOpenHashMap<Bucket>> SERVER_BY_DIM = new Int2ObjectOpenHashMap<>();
    private static final IdentityHashMap<IClimbable, Entry> SERVER_REVERSE = new IdentityHashMap<>();
    private ClimbableRegistry() {
    }

    private static Int2ObjectMap<Long2ObjectOpenHashMap<Bucket>> byDim(World w) {
        return w.isRemote ? CLIENT_BY_DIM : SERVER_BY_DIM;
    }

    private static IdentityHashMap<IClimbable, Entry> reverse(World w) {
        return w.isRemote ? CLIENT_REVERSE : SERVER_REVERSE;
    }

    /**
     * Register a climbable across all chunks overlapped by its climb AABB
     */
    public static void register(IClimbable c) {
        World w = c.world();
        int dim = w.provider.getDimension();

        IdentityHashMap<IClimbable, Entry> rev = reverse(w);
        if (rev.containsKey(c)) {
            unregister(c);
        }

        AxisAlignedBB aabb = c.getClimbAABBForIndexing();
        Entry entry = new Entry(dim);

        if (aabb == null) {
            long key = Library.chunkKey(c.pos());
            addToChunk(w, dim, key, c);
            entry.keys.add(key);
        } else {
            int minCX = MathHelper.floor(aabb.minX) >> 4;
            int maxCX = MathHelper.floor(aabb.maxX) >> 4;
            int minCZ = MathHelper.floor(aabb.minZ) >> 4;
            int maxCZ = MathHelper.floor(aabb.maxZ) >> 4;

            for (int cx = minCX; cx <= maxCX; cx++) {
                for (int cz = minCZ; cz <= maxCZ; cz++) {
                    long key = ChunkPos.asLong(cx, cz);
                    addToChunk(w, dim, key, c);
                    entry.keys.add(key);
                }
            }
        }

        rev.put(c, entry);
    }

    /**
     * Remove a climbable from every chunk it was registered to (safe if not present).
     */
    public static void unregister(IClimbable c) {
        World w = c.world();
        IdentityHashMap<IClimbable, Entry> rev = reverse(w);
        Entry e = rev.remove(c);
        if (e == null) return;

        Long2ObjectOpenHashMap<Bucket> byChunk = byDim(w).get(e.dim);
        if (byChunk == null) return;

        for (long key : e.keys) {
            Bucket bucket = byChunk.get(key);
            if (bucket == null) continue;
            bucket.remove(c);
            if (bucket.isEmpty()) {
                byChunk.remove(key);
            }
        }

        if (byChunk.isEmpty()) {
            byDim(w).remove(e.dim);
        }
    }

    /**
     * If a climbable's AABB or anchor changed, call this to rebuild its index.
     */
    public static void refresh(IClimbable c) {
        unregister(c);
        register(c);
    }

    /**
     * Hot-path query: is the entity intersecting any climbable in nearby chunks?
     */
    public static boolean isEntityOnAny(World w, EntityLivingBase e) {
        int dim = w.provider.getDimension();
        Long2ObjectOpenHashMap<Bucket> byChunk = byDim(w).get(dim);
        if (byChunk == null || byChunk.isEmpty()) return false;

        AxisAlignedBB bb = e.getEntityBoundingBox();

        int minCX = MathHelper.floor(bb.minX) >> 4;
        int maxCX = MathHelper.floor(bb.maxX) >> 4;
        int minSY = minSectionCoord(bb);
        int maxSY = maxSectionCoord(bb);
        int minCZ = MathHelper.floor(bb.minZ) >> 4;
        int maxCZ = MathHelper.floor(bb.maxZ) >> 4;

        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                long key = ChunkPos.asLong(cx, cz);
                Bucket bucket = byChunk.get(key);
                if (bucket == null || bucket.isEmpty()) continue;
                if (bucket.isEntityOnAny(w, e, minSY, maxSY)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Clear all climbables for a dimension on the given side
     */
    public static void clearDimension(World w) {
        int dim = w.provider.getDimension();
        byDim(w).remove(dim);
        reverse(w).values().removeIf(entry -> entry.dim == dim);
    }

    /**
     * Clear everything on both sides.
     */
    public static void clearAll() {
        CLIENT_BY_DIM.clear();
        CLIENT_REVERSE.clear();
        SERVER_BY_DIM.clear();
        SERVER_REVERSE.clear();
    }

    /**
     * Count climbables registered in a dimension on the given side.
     */
    public static int countClimbablesInDim(World w, int dim) {
        int total = 0;
        for (Entry entry : reverse(w).values()) {
            if (entry.dim == dim) {
                total++;
            }
        }
        return total;
    }

    public static List<IClimbable> getClimbablesInAABB(World w, @Nullable AxisAlignedBB aabb) {
        ArrayList<IClimbable> out = new ArrayList<>();
        if (aabb == null) return out;

        int dim = w.provider.getDimension();
        Long2ObjectOpenHashMap<Bucket> byChunk = byDim(w).get(dim);
        if (byChunk == null) return out;
        ReferenceOpenHashSet<IClimbable> seen = new ReferenceOpenHashSet<>();
        AxisAlignedBB q = aabb.grow(1.0e-6);

        int minCX = MathHelper.floor(aabb.minX) >> 4;
        int maxCX = MathHelper.floor(aabb.maxX) >> 4;
        int minSY = minSectionCoord(aabb);
        int maxSY = maxSectionCoord(aabb);
        int minCZ = MathHelper.floor(aabb.minZ) >> 4;
        int maxCZ = MathHelper.floor(aabb.maxZ) >> 4;

        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                long key = ChunkPos.asLong(cx, cz);
                Bucket bucket = byChunk.get(key);
                if (bucket == null || bucket.isEmpty()) continue;
                bucket.collectIntersecting(w, q, minSY, maxSY, seen, out);
            }
        }
        return out;
    }

    private static void addToChunk(World w, int dim, long key, IClimbable c) {
        Int2ObjectMap<Long2ObjectOpenHashMap<Bucket>> side = byDim(w);
        Long2ObjectOpenHashMap<Bucket> byChunk = side.get(dim);
        if (byChunk == null) {
            byChunk = new Long2ObjectOpenHashMap<>();
            side.put(dim, byChunk);
        }
        Bucket bucket = byChunk.get(key);
        if (bucket == null) {
            bucket = new FlatBucket(key);
            byChunk.put(key, bucket);
        }
        Bucket next = bucket.add(c);
        if (next != bucket) {
            byChunk.put(key, next);
        }
    }

    private static int minSectionCoord(AxisAlignedBB aabb) {
        return MathHelper.floor(aabb.minY) >> 4;
    }

    private static int maxSectionCoord(AxisAlignedBB aabb) {
        return MathHelper.floor(aabb.maxY) >> 4;
    }

    private static int minSectionCoord(IClimbable c) {
        AxisAlignedBB aabb = c.getClimbAABBForIndexing();
        return aabb != null ? minSectionCoord(aabb) : c.pos().getY() >> 4;
    }

    private static int maxSectionCoord(IClimbable c) {
        AxisAlignedBB aabb = c.getClimbAABBForIndexing();
        return aabb != null ? maxSectionCoord(aabb) : c.pos().getY() >> 4;
    }

    private static boolean spansMultipleSections(ArrayList<IClimbable> list) {
        int minSection = Integer.MAX_VALUE;
        int maxSection = Integer.MIN_VALUE;
        for (IClimbable c : list) {
            if (c == null) continue;
            int cMin = minSectionCoord(c);
            int cMax = maxSectionCoord(c);
            if (cMin < minSection) minSection = cMin;
            if (cMax > maxSection) maxSection = cMax;
            if (minSection < maxSection) return true;
        }
        return false;
    }

    private static boolean removeIdentity(ArrayList<IClimbable> list, IClimbable c) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == c) {
                list.remove(i);
                return true;
            }
        }
        return false;
    }

    private static void collectCandidate(World w, AxisAlignedBB q, ReferenceOpenHashSet<IClimbable> seen,
                                         ArrayList<IClimbable> out, IClimbable c) {
        if (c == null || !seen.add(c)) return;
        if (c.world() != w) return;
        AxisAlignedBB idx = c.getClimbAABBForIndexing();
        if (idx != null) {
            if (idx.intersects(q)) {
                out.add(c);
            }
            return;
        }

        BlockPos p = c.pos();
        AxisAlignedBB anchor = new AxisAlignedBB(p);
        if (anchor.intersects(q)) {
            out.add(c);
        }
    }

    private interface Bucket {
        @NotNull Bucket add(IClimbable c);

        boolean remove(IClimbable c);

        boolean isEmpty();

        boolean isEntityOnAny(World w, EntityLivingBase e, int minSectionY, int maxSectionY);

        void collectIntersecting(World w, AxisAlignedBB q, int minSectionY, int maxSectionY,
                                 ReferenceOpenHashSet<IClimbable> seen, ArrayList<IClimbable> out);
    }

    private static final class FlatBucket implements Bucket {
        final long chunkKey;
        final ArrayList<IClimbable> list = new ArrayList<>(1);

        private FlatBucket(long chunkKey) {
            this.chunkKey = chunkKey;
        }

        @Override
        public @NotNull Bucket add(IClimbable c) {
            for (IClimbable existing : list) {
                if (existing == c) return this;
            }
            list.add(c);
            if (list.size() >= SECTION_PROMOTION_THRESHOLD && spansMultipleSections(list)) {
                return new SectionBucket(chunkKey, list);
            }
            return this;
        }

        @Override
        public boolean remove(IClimbable c) {
            return removeIdentity(list, c);
        }

        @Override
        public boolean isEmpty() {
            return list.isEmpty();
        }

        @Override
        public boolean isEntityOnAny(World w, EntityLivingBase e, int minSectionY, int maxSectionY) {
            for (IClimbable c : list) {
                if (c == null) continue;
                if (c.world() != w) continue;
                if (c.isEntityInClimbAABB(e)) return true;
            }
            return false;
        }

        @Override
        public void collectIntersecting(World w, AxisAlignedBB q, int minSectionY, int maxSectionY,
                                        ReferenceOpenHashSet<IClimbable> seen, ArrayList<IClimbable> out) {
            for (IClimbable c : list) {
                collectCandidate(w, q, seen, out, c);
            }
        }
    }

    private static final class SectionBucket implements Bucket {
        final long chunkKey;
        final Long2ObjectOpenHashMap<ArrayList<IClimbable>> bySection = new Long2ObjectOpenHashMap<>();

        private SectionBucket(long chunkKey, ArrayList<IClimbable> seed) {
            this.chunkKey = chunkKey;
            for (IClimbable c : seed) {
                add(c);
            }
        }

        @Override
        public @NotNull Bucket add(IClimbable c) {
            int minSectionY = minSectionCoord(c);
            int maxSectionY = maxSectionCoord(c);
            for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                long sectionKey = Library.sectionToLong(chunkKey, sectionY);
                ArrayList<IClimbable> list = bySection.get(sectionKey);
                if (list == null) {
                    list = new ArrayList<>(1);
                    bySection.put(sectionKey, list);
                }
                boolean duplicate = false;
                for (IClimbable existing : list) {
                    if (existing == c) {
                        duplicate = true;
                        break;
                    }
                }
                if (!duplicate) {
                    list.add(c);
                }
            }
            return this;
        }

        @Override
        public boolean remove(IClimbable c) {
            boolean removed = false;
            LongOpenHashSet emptied = null;
            for (var iterator = bySection.long2ObjectEntrySet().fastIterator(); iterator.hasNext(); ) {
                var entry = iterator.next();
                ArrayList<IClimbable> list = entry.getValue();
                if (!removeIdentity(list, c)) continue;
                removed = true;
                if (list.isEmpty()) {
                    if (emptied == null) {
                        emptied = new LongOpenHashSet();
                    }
                    emptied.add(entry.getLongKey());
                }
            }
            if (emptied != null) {
                for (long sectionKey : emptied) {
                    bySection.remove(sectionKey);
                }
            }
            return removed;
        }

        @Override
        public boolean isEmpty() {
            return bySection.isEmpty();
        }

        @Override
        public boolean isEntityOnAny(World w, EntityLivingBase e, int minSectionY, int maxSectionY) {
            ReferenceOpenHashSet<IClimbable> seen = minSectionY == maxSectionY ? null : new ReferenceOpenHashSet<>();
            for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                ArrayList<IClimbable> list = bySection.get(Library.sectionToLong(chunkKey, sectionY));
                if (list == null || list.isEmpty()) continue;
                for (IClimbable c : list) {
                    if (c == null) continue;
                    if (seen != null && !seen.add(c)) continue;
                    if (c.world() != w) continue;
                    if (c.isEntityInClimbAABB(e)) return true;
                }
            }
            return false;
        }

        @Override
        public void collectIntersecting(World w, AxisAlignedBB q, int minSectionY, int maxSectionY,
                                        ReferenceOpenHashSet<IClimbable> seen, ArrayList<IClimbable> out) {
            for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                ArrayList<IClimbable> list = bySection.get(Library.sectionToLong(chunkKey, sectionY));
                if (list == null || list.isEmpty()) continue;
                for (IClimbable c : list) {
                    collectCandidate(w, q, seen, out, c);
                }
            }
        }
    }

    private static final class Entry {
        final int dim;
        final LongOpenHashSet keys = new LongOpenHashSet();

        Entry(int dim) {
            this.dim = dim;
        }
    }
}
