package com.hbm.util;

import com.hbm.interfaces.BitMask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.LongAdder;

import static com.hbm.lib.internal.UnsafeHolder.U;

/**
 * Thread-safe bitset
 *
 * @author mlbv
 */
@ThreadSafe
public class ConcurrentBitSet implements BitMask, Cloneable {
    private static final long ABASE;
    private static final int ASHIFT;

    static {
        int scale = U.arrayIndexScale(long[].class);
        if ((scale & (scale - 1)) != 0) {
            throw new Error("long[] element size not power of two");
        }
        ABASE = U.arrayBaseOffset(long[].class);
        ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
    }

    private final long[] words;
    private final int wordCount;
    private final int logicalSize;
    private final LongAdder bitCount = new LongAdder();

    public ConcurrentBitSet(int logicalSize) {
        if (logicalSize < 0) throw new NegativeArraySizeException("logicalSize < 0: " + logicalSize);
        this.logicalSize = logicalSize;
        this.wordCount = (logicalSize + 63) >>> 6;
        this.words = new long[wordCount];
    }

    public ConcurrentBitSet(@NotNull ConcurrentBitSet other) {
        this.logicalSize = other.logicalSize;
        this.wordCount = other.wordCount;
        this.words = new long[wordCount];
        for (int i = 0; i < wordCount; i++) {
            this.words[i] = U.getLongVolatile(other.words, byteOffset(i));
        }
        this.bitCount.add(other.bitCount.sum());
    }

    @Contract(pure = true)
    private static long byteOffset(int index) {
        return ((long) index << ASHIFT) + ABASE;
    }

    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static ConcurrentBitSet fromLongArray(long[] data, int logicalSize) {
        ConcurrentBitSet bitSet = new ConcurrentBitSet(logicalSize);
        if (logicalSize == 0) return bitSet;

        int wordsToCopy = Math.min(data.length, bitSet.wordCount);
        int lastIdx = (logicalSize - 1) >>> 6;
        int rem = logicalSize & 63;
        long tailMask = rem == 0 ? -1L : ((1L << rem) - 1L);

        long totalBits = 0L;
        for (int i = 0; i < wordsToCopy; i++) {
            long w = data[i];
            if (i > lastIdx) w = 0L;
            else if (i == lastIdx && rem != 0) w &= tailMask;
            bitSet.words[i] = w;
            totalBits += Long.bitCount(w);
        }
        bitSet.bitCount.add(totalBits);
        return bitSet;
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static ConcurrentBitSet valueOf(long @NotNull [] data) {
        int len = 0;
        for (int i = data.length - 1; i >= 0; i--) {
            long w = data[i];
            if (w != 0) {
                len = (i << 6) + (64 - Long.numberOfLeadingZeros(w));
                break;
            }
        }
        return fromLongArray(data, len);
    }

    public static ConcurrentBitSet fromWords(@NotNull ByteBuffer buf, int logicalSize) {
        ConcurrentBitSet bitSet = new ConcurrentBitSet(logicalSize);
        int wc = bitSet.wordCount;
        int need = wc << 3;
        if (buf.remaining() < need) {
            throw new IllegalArgumentException("Buffer underflow: need " + need + " bytes for words, have " + buf.remaining());
        }
        if (wc == 0) return bitSet;
        long totalBits = 0L;
        int last = wc - 1;
        long tailMask = bitSet.lastWordMask();
        for (int i = 0; i < last; i++) {
            long w = buf.getLong();
            bitSet.words[i] = w;
            totalBits += Long.bitCount(w);
        }
        long wLast = buf.getLong() & tailMask;
        bitSet.words[last] = wLast;
        totalBits += Long.bitCount(wLast);
        bitSet.bitCount.add(totalBits);
        return bitSet;
    }

    public void toWords(@NotNull ByteBuffer buf) {
        int need = wordCount << 3;
        if (buf.remaining() < need) {
            throw new IllegalArgumentException("Buffer too small: need " + need + " bytes remaining, have " + buf.remaining());
        }
        if (wordCount == 0) return;

        int last = wordCount - 1;
        long tailMask = lastWordMask();

        for (int i = 0; i < last; i++) {
            buf.putLong(U.getLongVolatile(words, byteOffset(i)));
        }
        buf.putLong(U.getLongVolatile(words, byteOffset(last)) & tailMask);
    }

    @Override
    @Contract(pure = true)
    public boolean get(int bit) {
        if (bit < 0) throw new IndexOutOfBoundsException("bit < 0: " + bit);
        if (bit >= logicalSize) return false;
        int wordIndex = bit >>> 6;
        long mask = 1L << (bit & 63);
        long word = U.getLongVolatile(words, byteOffset(wordIndex));
        return (word & mask) != 0;
    }

    @Override
    public void set(int bit) {
        if (bit < 0 || bit >= logicalSize) return;
        int wordIndex = bit >>> 6;
        long offset = byteOffset(wordIndex);
        long mask = 1L << (bit & 63);
        while (true) {
            long oldWord = U.getLongVolatile(words, offset);
            long newWord = oldWord | mask;
            if (oldWord == newWord) return;
            if (U.compareAndSetLong(words, offset, oldWord, newWord)) {
                bitCount.increment();
                return;
            }
        }
    }

    @Override
    public boolean getAndSet(int bit) {
        if (bit < 0 || bit >= logicalSize) throw new IndexOutOfBoundsException("bit index out of bounds: " + bit);
        int wordIndex = bit >>> 6;
        long offset = byteOffset(wordIndex);
        long mask = 1L << (bit & 63);
        while (true) {
            long oldWord = U.getLongVolatile(words, offset);
            if ((oldWord & mask) != 0) return true;
            long newWord = oldWord | mask;
            if (U.compareAndSetLong(words, offset, oldWord, newWord)) {
                bitCount.increment();
                return false;
            }
        }
    }

    public void set(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > logicalSize || fromIndex > toIndex) throw new IndexOutOfBoundsException();
        if (fromIndex == toIndex) return;
        int startWord = fromIndex >>> 6;
        int endWord = (toIndex - 1) >>> 6;
        long startMask = -1L << (fromIndex & 63);
        long endMask = (toIndex & 63) == 0 ? -1L : ((1L << (toIndex & 63)) - 1L);
        if (startWord == endWord) {
            setBits(startWord, startMask & endMask);
        } else {
            setBits(startWord, startMask);
            for (int i = startWord + 1; i < endWord; i++) setBits(i, -1L);
            if (endMask != 0) setBits(endWord, endMask);
        }
    }

    private void setBits(int wi, long mask) {
        if (mask == 0) return;
        long offset = byteOffset(wi);
        while (true) {
            long oldWord = U.getLongVolatile(words, offset);
            long newWord = oldWord | mask;
            if (oldWord == newWord) return;
            if (U.compareAndSetLong(words, offset, oldWord, newWord)) {
                bitCount.add(Long.bitCount(newWord ^ oldWord));
                return;
            }
        }
    }

    public void clear(int bit) {
        if (bit < 0 || bit >= logicalSize) return;
        int wordIndex = bit >>> 6;
        long offset = byteOffset(wordIndex);
        long mask = ~(1L << (bit & 63));
        while (true) {
            long oldWord = U.getLongVolatile(words, offset);
            long newWord = oldWord & mask;
            if (oldWord == newWord) return;
            if (U.compareAndSetLong(words, offset, oldWord, newWord)) {
                bitCount.decrement();
                return;
            }
        }
    }

    public boolean getAndClear(int bit) {
        if (bit < 0 || bit >= logicalSize) throw new IndexOutOfBoundsException("bit index out of bounds: " + bit);
        int wordIndex = bit >>> 6;
        long offset = byteOffset(wordIndex);
        long mask = 1L << (bit & 63);
        while (true) {
            long oldWord = U.getLongVolatile(words, offset);
            if ((oldWord & mask) == 0) return false;
            long newWord = oldWord & ~mask;
            if (U.compareAndSetLong(words, offset, oldWord, newWord)) {
                bitCount.decrement();
                return true;
            }
        }
    }

    public void clear(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > logicalSize || fromIndex > toIndex) throw new IndexOutOfBoundsException();
        if (fromIndex == toIndex) return;
        int startWord = fromIndex >>> 6;
        int endWord = (toIndex - 1) >>> 6;
        long startMask = -1L << (fromIndex & 63);
        long endMask = (toIndex & 63) == 0 ? -1L : ((1L << (toIndex & 63)) - 1L);
        if (startWord == endWord) {
            clearBits(startWord, startMask & endMask);
        } else {
            clearBits(startWord, startMask);
            for (int i = startWord + 1; i < endWord; i++) clearBits(i, -1L);
            if (endMask != 0) clearBits(endWord, endMask);
        }
    }

    private void clearBits(int wi, long mask) {
        if (mask == 0) return;
        long offset = byteOffset(wi);
        while (true) {
            long oldWord = U.getLongVolatile(words, offset);
            long newWord = oldWord & ~mask;
            if (oldWord == newWord) return;
            if (U.compareAndSetLong(words, offset, oldWord, newWord)) {
                bitCount.add(-Long.bitCount(oldWord ^ newWord));
                return;
            }
        }
    }

    public void clear() {
        clear(0, logicalSize);
    }

    public void flip(int bit) {
        if (bit < 0 || bit >= logicalSize) return;
        int wordIndex = bit >>> 6;
        long offset = byteOffset(wordIndex);
        long mask = 1L << (bit & 63);
        while (true) {
            long oldWord = U.getLongVolatile(words, offset);
            long newWord = oldWord ^ mask;
            if (U.compareAndSetLong(words, offset, oldWord, newWord)) {
                bitCount.add((oldWord & mask) == 0 ? 1 : -1);
                return;
            }
        }
    }

    public boolean getAndFlip(int bit) {
        if (bit < 0 || bit >= logicalSize) throw new IndexOutOfBoundsException("bit index out of bounds: " + bit);
        int wordIndex = bit >>> 6;
        long offset = byteOffset(wordIndex);
        long mask = 1L << (bit & 63);
        while (true) {
            long oldWord = U.getLongVolatile(words, offset);
            boolean wasSet = (oldWord & mask) != 0;
            long newWord = oldWord ^ mask;
            if (U.compareAndSetLong(words, offset, oldWord, newWord)) {
                if (wasSet) bitCount.decrement();
                else bitCount.increment();
                return wasSet;
            }
        }
    }

    public void flip(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > logicalSize || fromIndex > toIndex) throw new IndexOutOfBoundsException();
        if (fromIndex == toIndex) return;
        int startWord = fromIndex >>> 6;
        int endWord = (toIndex - 1) >>> 6;
        long startMask = -1L << (fromIndex & 63);
        long endMask = (toIndex & 63) == 0 ? -1L : ((1L << (toIndex & 63)) - 1L);
        if (startWord == endWord) {
            flipBits(startWord, startMask & endMask);
        } else {
            flipBits(startWord, startMask);
            for (int i = startWord + 1; i < endWord; i++) flipBits(i, -1L);
            if (endMask != 0) flipBits(endWord, endMask);
        }
    }

    private void flipBits(int wi, long mask) {
        if (mask == 0) return;
        long offset = byteOffset(wi);
        while (true) {
            long oldWord = U.getLongVolatile(words, offset);
            long newWord = oldWord ^ mask;
            if (U.compareAndSetLong(words, offset, oldWord, newWord)) {
                bitCount.add(Long.bitCount(mask) - 2L * Long.bitCount(mask & oldWord));
                return;
            }
        }
    }

    @Override
    @Contract(pure = true)
    public int nextSetBit(int from) {
        if (from < 0) from = 0;
        int wordIndex = from >>> 6;
        if (wordIndex >= wordCount) return -1;
        long word = U.getLongVolatile(words, byteOffset(wordIndex)) & (~0L << (from & 63));
        while (true) {
            if (word != 0) {
                int idx = (wordIndex << 6) + Long.numberOfTrailingZeros(word);
                return (idx < logicalSize) ? idx : -1;
            }
            wordIndex++;
            if (wordIndex >= wordCount) return -1;
            word = U.getLongVolatile(words, byteOffset(wordIndex));
        }
    }

    @Override
    @Contract(pure = true)
    public int nextClearBit(int from) {
        if (from < 0) throw new IndexOutOfBoundsException("from < 0: " + from);
        if (from >= logicalSize) return from;
        int wordIndex = from >>> 6;
        if (wordIndex >= wordCount) return from;
        long word = ~U.getLongVolatile(words, byteOffset(wordIndex)) & (-1L << (from & 63));
        while (true) {
            if (word != 0) {
                int idx = (wordIndex << 6) + Long.numberOfTrailingZeros(word);
                return Math.min(idx, logicalSize);
            }
            wordIndex++;
            if (wordIndex >= wordCount) return logicalSize;
            word = ~U.getLongVolatile(words, byteOffset(wordIndex));
        }
    }

    @Override
    @Contract(pure = true)
    public int previousSetBit(int from) {
        if (from < 0) return -1;
        if (from >= logicalSize) from = logicalSize - 1;
        if (from < 0) return -1;
        int wordIndex = from >>> 6;
        long mask = ~0L >>> (63 - (from & 63));
        long word = U.getLongVolatile(words, byteOffset(wordIndex)) & mask;
        while (true) {
            if (word != 0) return (wordIndex << 6) + (63 - Long.numberOfLeadingZeros(word));
            wordIndex--;
            if (wordIndex < 0) return -1;
            word = U.getLongVolatile(words, byteOffset(wordIndex));
        }
    }

    @Override
    @Contract(pure = true)
    public int previousClearBit(int from) {
        if (from < 0) return -1;
        if (from >= logicalSize) from = logicalSize - 1;
        if (from < 0) return -1;
        int wordIndex = from >>> 6;
        long mask = ~0L >>> (63 - (from & 63));
        long word = ~U.getLongVolatile(words, byteOffset(wordIndex)) & mask;
        while (true) {
            if (word != 0) return (wordIndex << 6) + (63 - Long.numberOfLeadingZeros(word));
            wordIndex--;
            if (wordIndex < 0) return -1;
            word = ~U.getLongVolatile(words, byteOffset(wordIndex));
        }
    }

    private void requireSameSize(@NotNull ConcurrentBitSet set) {
        if (this.logicalSize != set.logicalSize) {
            throw new IllegalArgumentException("Expected size: " + logicalSize + ", actual size: " + set.logicalSize);
        }
    }

    @Override
    @Contract(pure = true)
    public boolean isEmpty() {
        return bitCount.sum() == 0;
    }

    //fucking retarded word scan
    public boolean isEmptyExact() {
        if (wordCount == 0) return true;
        int last = wordCount - 1;
        long tailMask = lastWordMask();

        for (int i = 0; i < last; i++) {
            if (U.getLongVolatile(words, byteOffset(i)) != 0L) return false;
        }
        return (U.getLongVolatile(words, byteOffset(last)) & tailMask) == 0L;
    }

    @Override
    @Contract(pure = true)
    public long cardinality() {
        return bitCount.sum();
    }

    public long cardinalityExact() {
        if (wordCount == 0) return 0L;
        int last = wordCount - 1;
        long tailMask = lastWordMask();

        long total = 0L;
        for (int i = 0; i < last; i++) {
            total += Long.bitCount(U.getLongVolatile(words, byteOffset(i)));
        }
        total += Long.bitCount(U.getLongVolatile(words, byteOffset(last)) & tailMask);
        return total;
    }

    public boolean isFullExact() {
        if (logicalSize == 0) return true;
        int last = wordCount - 1;
        if ((logicalSize & 63) == 0) {
            for (int i = 0; i <= last; i++) {
                if (U.getLongVolatile(words, byteOffset(i)) != -1L) return false;
            }
            return true;
        }
        for (int i = 0; i < last; i++) {
            if (U.getLongVolatile(words, byteOffset(i)) != -1L) return false;
        }
        long tailMask = lastWordMask();
        long wLast = U.getLongVolatile(words, byteOffset(last)) & tailMask;
        return wLast == tailMask;
    }

    @Override
    @Contract(pure = true)
    public int length() {
        if (logicalSize == 0) return 0;
        int maxWord = (logicalSize - 1) >>> 6;
        long mask = lastWordMask();
        for (int i = maxWord; i >= 0; i--) {
            long w = U.getLongVolatile(words, byteOffset(i));
            if (i == maxWord) w &= mask;
            if (w != 0L) {
                return (i << 6) + (64 - Long.numberOfLeadingZeros(w));
            }
        }
        return 0;
    }

    @Override
    @Contract(pure = true)
    public int size() {
        return wordCount << 6;
    }

    @Override
    @Contract(pure = true)
    public int logicalSize() {
        return logicalSize;
    }

    @Contract(pure = true)
    public boolean intersects(@NotNull ConcurrentBitSet set) {
        requireSameSize(set);
        for (int i = 0; i < wordCount; i++) {
            long a = U.getLongVolatile(words, byteOffset(i));
            long b = U.getLongVolatile(set.words, byteOffset(i));
            if ((a & b) != 0) return true;
        }
        return false;
    }

    public void and(@NotNull ConcurrentBitSet set) {
        requireSameSize(set);
        for (int i = 0; i < wordCount; i++) {
            long offset = byteOffset(i);
            long other = U.getLongVolatile(set.words, offset);
            while (true) {
                long oldWord = U.getLongVolatile(words, offset);
                long newWord = oldWord & other;
                if (oldWord == newWord) break;
                if (U.compareAndSetLong(words, offset, oldWord, newWord)) {
                    bitCount.add(-Long.bitCount(oldWord ^ newWord));
                    break;
                }
            }
        }
    }

    public void or(@NotNull ConcurrentBitSet set) {
        requireSameSize(set);
        for (int i = 0; i < wordCount; i++) {
            long offset = byteOffset(i);
            long other = U.getLongVolatile(set.words, offset);
            while (true) {
                long oldWord = U.getLongVolatile(words, offset);
                long newWord = oldWord | other;
                if (oldWord == newWord) break;
                if (U.compareAndSetLong(words, offset, oldWord, newWord)) {
                    bitCount.add(Long.bitCount(oldWord ^ newWord));
                    break;
                }
            }
        }
    }

    public void xor(@NotNull ConcurrentBitSet set) {
        requireSameSize(set);
        for (int i = 0; i < wordCount; i++) {
            long offset = byteOffset(i);
            long otherWord = U.getLongVolatile(set.words, offset);
            while (true) {
                long oldWord = U.getLongVolatile(words, offset);
                long newWord = oldWord ^ otherWord;
                if (oldWord == newWord) break;
                if (U.compareAndSetLong(words, offset, oldWord, newWord)) {
                    bitCount.add(Long.bitCount(otherWord) - 2L * Long.bitCount(otherWord & oldWord));
                    break;
                }
            }
        }
    }

    public void andNot(@NotNull ConcurrentBitSet set) {
        requireSameSize(set);
        for (int i = 0; i < wordCount; i++) {
            long offset = byteOffset(i);
            long otherWord = U.getLongVolatile(set.words, offset);
            while (true) {
                long oldWord = U.getLongVolatile(words, offset);
                long newWord = oldWord & ~otherWord;
                if (oldWord == newWord) break;
                if (U.compareAndSetLong(words, offset, oldWord, newWord)) {
                    bitCount.add(-Long.bitCount(oldWord ^ newWord));
                    break;
                }
            }
        }
    }

    @Override
    @Contract(pure = true)
    public long[] toLongArray() {
        int len = length();
        if (len == 0) return new long[0];

        int used = (len + 63) >>> 6;
        long[] out = new long[used];
        int last = used - 1;
        int rem = len & 63;
        long tailMask = rem == 0 ? -1L : ((1L << rem) - 1L);

        for (int i = 0; i < used; i++) {
            long v = U.getLongVolatile(words, byteOffset(i));
            if (i == last && rem != 0) v &= tailMask;
            out[i] = v;
        }
        return out;
    }

    public void toLongArray(long @NotNull [] out) {
        if (out.length != wordCount) {
            throw new IllegalArgumentException("Expected long[" + wordCount + "], got long[" + out.length + "]");
        }
        if (wordCount == 0) return;
        int last = wordCount - 1;
        long tailMask = lastWordMask();
        for (int i = 0; i < last; i++) {
            out[i] = U.getLongVolatile(words, byteOffset(i));
        }
        out[last] = U.getLongVolatile(words, byteOffset(last)) & tailMask;
    }

    private long lastWordMask() {
        int r = logicalSize & 63;
        return r == 0 ? -1L : ((1L << r) - 1L);
    }

    @Override
    @Contract(value = "null -> false", pure = true)
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConcurrentBitSet other)) return false;
        if (logicalSize != other.logicalSize) return false;
        for (int i = 0; i < wordCount; i++) {
            if (U.getLongVolatile(words, byteOffset(i)) != U.getLongVolatile(other.words, byteOffset(i))) return false;
        }
        return true;
    }

    @Override
    @Contract(pure = true)
    public int hashCode() {
        long h = 1234;
        int used = (length() + 63) >>> 6;
        for (int i = used - 1; i >= 0; i--) {
            h ^= U.getLongVolatile(words, byteOffset(i)) * (i + 1L);
        }
        return Long.hashCode(h);
    }

    @NotNull
    @Override
    @Contract(pure = true)
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        int i = nextSetBit(0);
        if (i != -1) {
            sb.append(i);
            while (true) {
                i = nextSetBit(i + 1);
                if (i == -1) break;
                sb.append(", ").append(i);
            }
        }
        sb.append('}');
        return sb.toString();
    }

    @NotNull
    @Override
    @Contract(value = "-> new", pure = true)
    public ConcurrentBitSet clone() {
        return new ConcurrentBitSet(this);
    }
}
