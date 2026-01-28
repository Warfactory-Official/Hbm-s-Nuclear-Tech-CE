package com.hbm.world.phased;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.util.math.ChunkPos;

/**
 * Shared constants for the phased worldgen system.
 */
final class PhasedConstants {
    static final LongArrayList ORIGIN_ONLY = new ImmutableLongArrayList(new long[]{ChunkPos.asLong(0, 0)});
    static final LongArrayList EMPTY = new ImmutableLongArrayList(new long[0]);
    static final int[] ZERO_INT_ARRAY = new int[0];

    static class ImmutableLongArrayList extends LongArrayList {
        public ImmutableLongArrayList(long[] a) {
            super(a);
        }

        @Override
        public void add(int index, long element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean add(long element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long set(int index, long element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long removeLong(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean rem(long element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void ensureCapacity(int capacity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trim() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trim(int n) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void size(int size) {
            throw new UnsupportedOperationException();
        }
    }

    private PhasedConstants() {
    }
}
