package com.hbm.util;

import com.hbm.interfaces.BitMask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static com.hbm.lib.internal.UnsafeHolder.U;

/**
 * For single writer usage, not thread-safe. Don't forget the free the memory!
 *
 * @author mlbv
 */
public final class OffHeapBitSet implements BitMask, Cloneable {
    private final int logicalSizeLocal;
    private final int wordCountLocal;
    private long addr;
    private long bitCount;

    public OffHeapBitSet(int logicalSize) {
        if (logicalSize < 0) throw new NegativeArraySizeException("logicalSize < 0: " + logicalSize);
        this.logicalSizeLocal = logicalSize;
        this.wordCountLocal = (logicalSize + 63) >>> 6;
        long bytes = ((long) wordCountLocal) << 3;
        long p = U.allocateMemory(bytes);
        U.setMemory(p, bytes, (byte) 0);
        this.addr = p;
        this.bitCount = 0L;
    }

    @Override
    public void free() {
        if (addr != 0L) U.freeMemory(addr);
        addr = 0L;
    }

    private long wordAddr(int index) {
        return addr + (((long) index) << 3);
    }

    private long getWord(int index) {
        return U.getLong(wordAddr(index));
    }

    private void setWord(int index, long v) {
        U.putLong(wordAddr(index), v);
    }

    @Override
    public boolean get(int bit) {
        if (bit < 0 || bit >= logicalSizeLocal) return false;
        int wi = bit >>> 6;
        long mask = 1L << (bit & 63);
        return (getWord(wi) & mask) != 0L;
    }

    @Override
    public void set(int bit) {
        if (bit < 0 || bit >= logicalSizeLocal) return;
        int wi = bit >>> 6;
        long mask = 1L << (bit & 63);
        long old = getWord(wi);
        if ((old & mask) != 0L) return;
        setWord(wi, old | mask);
        bitCount++;
    }

    @Override
    public boolean getAndSet(int bit) {
        if (bit < 0 || bit >= logicalSizeLocal) throw new IndexOutOfBoundsException("bit index out of bounds: " + bit);
        int wi = bit >>> 6;
        long mask = 1L << (bit & 63);
        long old = getWord(wi);
        if ((old & mask) != 0L) return true;
        setWord(wi, old | mask);
        bitCount++;
        return false;
    }

    public void clear(int bit) {
        if (bit < 0 || bit >= logicalSizeLocal) return;
        int wi = bit >>> 6;
        long mask = 1L << (bit & 63);
        long old = getWord(wi);
        if ((old & mask) == 0L) return;
        setWord(wi, old & ~mask);
        bitCount--;
    }

    public boolean getAndClear(int bit) {
        if (bit < 0 || bit >= logicalSizeLocal) throw new IndexOutOfBoundsException("bit index out of bounds: " + bit);
        int wi = bit >>> 6;
        long mask = 1L << (bit & 63);
        long old = getWord(wi);
        if ((old & mask) == 0L) return false;
        setWord(wi, old & ~mask);
        bitCount--;
        return true;
    }

    @Override
    @Contract(pure = true)
    public int nextSetBit(int from) {
        if (from < 0) from = 0;
        int wi = from >>> 6;
        if (wi >= wordCountLocal) return -1;
        long word = getWord(wi) & (~0L << (from & 63));
        while (true) {
            if (word != 0L) {
                int idx = (wi << 6) + Long.numberOfTrailingZeros(word);
                return (idx < logicalSizeLocal) ? idx : -1;
            }
            wi++;
            if (wi >= wordCountLocal) return -1;
            word = getWord(wi);
        }
    }

    @Override
    @Contract(pure = true)
    public int nextClearBit(int from) {
        if (from < 0) throw new IndexOutOfBoundsException("from < 0: " + from);
        if (from >= logicalSizeLocal) return from;
        int wi = from >>> 6;
        if (wi >= wordCountLocal) return from;
        long word = ~getWord(wi) & (-1L << (from & 63));
        while (true) {
            if (word != 0L) {
                int idx = (wi << 6) + Long.numberOfTrailingZeros(word);
                return Math.min(idx, logicalSizeLocal);
            }
            wi++;
            if (wi >= wordCountLocal) return logicalSizeLocal;
            word = ~getWord(wi);
        }
    }

    @Override
    @Contract(pure = true)
    public int previousSetBit(int from) {
        if (from < 0) return -1;
        if (from >= logicalSizeLocal) from = logicalSizeLocal - 1;
        if (from < 0) return -1;
        int wi = from >>> 6;
        long mask = ~0L >>> (63 - (from & 63));
        long word = getWord(wi) & mask;
        while (true) {
            if (word != 0L) return (wi << 6) + (63 - Long.numberOfLeadingZeros(word));
            wi--;
            if (wi < 0) return -1;
            word = getWord(wi);
        }
    }

    @Override
    @Contract(pure = true)
    public int previousClearBit(int from) {
        if (from < 0) return -1;
        if (from >= logicalSizeLocal) from = logicalSizeLocal - 1;
        if (from < 0) return -1;
        int wi = from >>> 6;
        long mask = ~0L >>> (63 - (from & 63));
        long word = ~getWord(wi) & mask;
        while (true) {
            if (word != 0L) return (wi << 6) + (63 - Long.numberOfLeadingZeros(word));
            wi--;
            if (wi < 0) return -1;
            word = ~getWord(wi);
        }
    }

    @Override
    @Contract(pure = true)
    public boolean isEmpty() {
        return bitCount == 0L;
    }

    @Override
    @Contract(pure = true)
    public long cardinality() {
        return bitCount;
    }

    @Override
    @Contract(pure = true)
    public int length() {
        if (logicalSizeLocal == 0) return 0;
        int maxWord = (logicalSizeLocal - 1) >>> 6;
        long mask = lastWordMask();
        for (int i = maxWord; i >= 0; i--) {
            long w = getWord(i);
            if (i == maxWord) w &= mask;
            if (w != 0L) return (i << 6) + (64 - Long.numberOfLeadingZeros(w));
        }
        return 0;
    }

    @Override
    @Contract(pure = true)
    public int size() {
        return wordCountLocal << 6;
    }

    @Override
    @Contract(pure = true)
    public int logicalSize() {
        return logicalSizeLocal;
    }

    private long lastWordMask() {
        int r = logicalSizeLocal & 63;
        return r == 0 ? -1L : ((1L << r) - 1L);
    }

    @Override
    public long[] toLongArray() {
        int len = length();
        if (len == 0) return new long[0];
        int used = (len + 63) >>> 6;
        long[] out = new long[used];
        int last = used - 1;
        int rem = len & 63;
        long tailMask = rem == 0 ? -1L : ((1L << rem) - 1L);
        for (int i = 0; i < used; i++) {
            long v = getWord(i);
            if (i == last && rem != 0) v &= tailMask;
            out[i] = v;
        }
        return out;
    }

    @NotNull
    @Override
    @Contract(value = "-> new", pure = true)
    public OffHeapBitSet clone() {
        OffHeapBitSet b = new OffHeapBitSet(this.logicalSizeLocal);
        long bytes = ((long) this.wordCountLocal) << 3;
        U.copyMemory(null, this.addr, null, b.addr, bytes);
        b.bitCount = this.bitCount;
        return b;
    }
}
