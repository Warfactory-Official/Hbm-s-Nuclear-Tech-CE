package com.hbm.lib.queues;

import com.hbm.lib.Library;
import org.jctools.queues.SpscArrayQueue;
import org.jctools.util.Pow2;

import java.util.Arrays;

import static com.hbm.lib.internal.UnsafeHolder.U;
import static com.hbm.lib.internal.UnsafeHolder.fieldOffset;

abstract class MpUnboundedXaddArrayLongQueue<R extends MpUnboundedXaddChunkLong<R>> extends MpUnboundedXaddArrayLongQueuePad3<R> {

    // must be != MpUnboundedXaddChunkLong.NOT_USED
    private static final long ROTATION = -2;

    final int chunkMask;
    final int chunkShift;
    private final int maxPooledChunks;
    final SpscArrayQueue<R> freeChunksPool;

    MpUnboundedXaddArrayLongQueue(int chunkSize, int maxPooledChunks) {
        if (maxPooledChunks < 0) {
            throw new IllegalArgumentException("Expecting a non-negative maxPooledChunks, but got: " + maxPooledChunks);
        }
        chunkSize = Pow2.roundToPowerOfTwo(chunkSize);

        this.chunkMask = chunkSize - 1;
        this.chunkShift = Integer.numberOfTrailingZeros(chunkSize);
        this.freeChunksPool = new SpscArrayQueue<>(maxPooledChunks);

        final R first = newChunk(0, null, chunkSize, maxPooledChunks > 0);
        U.putReferenceRelease(this, P_CHUNK_OFFSET, first);
        U.putLongRelease(this, P_CHUNK_INDEX_OFFSET, 0);
        U.putReferenceRelease(this, C_CHUNK_OFFSET, first);

        for (int i = 1; i < maxPooledChunks; i++) {
            freeChunksPool.offer(newChunk(MpUnboundedXaddChunkLong.NOT_USED, null, chunkSize, true));
        }
        this.maxPooledChunks = maxPooledChunks;
    }

    public final int chunkSize() {
        return chunkMask + 1;
    }

    public final int maxPooledChunks() {
        return maxPooledChunks;
    }

    abstract R newChunk(long index, R prev, int chunkSize, boolean pooled);

    /**
     * Chase the linked chunks to the appropriate chunk. More than one producer may race to add/discover new chunks.
     */
    final R producerChunkForIndex(final R initialChunk, final long requiredChunkIndex) {
        R currentChunk = initialChunk;
        long jumpBackward;

        while (true) {
            if (currentChunk == null) {
                currentChunk = producerChunk;
            }
            final long currentChunkIndex = currentChunk.index;
            assert currentChunkIndex != MpUnboundedXaddChunkLong.NOT_USED;

            jumpBackward = currentChunkIndex - requiredChunkIndex;
            if (jumpBackward >= 0) {
                break;
            }
            // validate against last producer chunk index
            if (producerChunkIndex == currentChunkIndex) {
                currentChunk = appendNextChunks(currentChunk, currentChunkIndex, -jumpBackward);
            } else {
                currentChunk = null;
            }
        }

        for (long i = 0; i < jumpBackward; i++) {
            currentChunk = currentChunk.prev;
            assert currentChunk != null;
        }
        assert currentChunk.index == requiredChunkIndex;
        return currentChunk;
    }

    final R appendNextChunks(R currentChunk, long currentChunkIndex, long chunksToAppend) {
        assert currentChunkIndex != MpUnboundedXaddChunkLong.NOT_USED;

        // prevent other concurrent attempts on append
        if (!U.compareAndSetLong(this, P_CHUNK_INDEX_OFFSET, currentChunkIndex, ROTATION)) {
            return null;
        }

        /* LOCKED FOR APPEND */
        {
            assert currentChunkIndex == currentChunk.index;

            for (long i = 1; i <= chunksToAppend; i++) {
                R newChunk = newOrPooledChunk(currentChunk, currentChunkIndex + i);
                U.putReferenceRelease(this, P_CHUNK_OFFSET, newChunk);
                // link next only when finished
                U.putReferenceRelease(currentChunk, MpUnboundedXaddChunkLong.NEXT_OFFSET, newChunk);
                currentChunk = newChunk;
            }

            // release appending
            U.putLongRelease(this, P_CHUNK_INDEX_OFFSET, currentChunkIndex + chunksToAppend);
        }
        /* UNLOCKED FOR APPEND */

        return currentChunk;
    }

    private R newOrPooledChunk(R prevChunk, long nextChunkIndex) {
        R newChunk = freeChunksPool.poll();
        if (newChunk != null) {
            assert newChunk.index < prevChunk.index;
            U.putReference(newChunk, MpUnboundedXaddChunkLong.PREV_OFFSET, prevChunk);
            U.putLongRelease(newChunk, MpUnboundedXaddChunkLong.INDEX_OFFSET, nextChunkIndex);
        } else {
            newChunk = newChunk(nextChunkIndex, prevChunk, chunkMask + 1, false);
        }
        return newChunk;
    }

    final void moveToNextConsumerChunk(R cChunk, R next) {
        U.putReferenceRelease(cChunk, MpUnboundedXaddChunkLong.NEXT_OFFSET, null);
        U.putReference(next, MpUnboundedXaddChunkLong.PREV_OFFSET, null);

        if (cChunk.pooled) {
            final boolean pooled = freeChunksPool.offer(cChunk);
            assert pooled;
        }
        U.putReferenceRelease(this, C_CHUNK_OFFSET, next);
    }

    public abstract long poll();

    public abstract long peek();

    public abstract long relaxedPoll();

    public abstract long relaxedPeek();
}

abstract class MpUnboundedXaddChunkLong<R> {
    static final int NOT_USED = -1;

    static final long PREV_OFFSET = fieldOffset(MpUnboundedXaddChunkLong.class, "prev");
    static final long NEXT_OFFSET = fieldOffset(MpUnboundedXaddChunkLong.class, "next");
    static final long INDEX_OFFSET = fieldOffset(MpUnboundedXaddChunkLong.class, "index");

    static final long LONG_ARR_BASE = U.arrayBaseOffset(long[].class);
    static final int LONG_ARR_SHIFT = Integer.numberOfTrailingZeros(U.arrayIndexScale(long[].class));

    final boolean pooled;
    final long[] buffer;

    volatile R prev;
    volatile long index;
    volatile R next;

    MpUnboundedXaddChunkLong(long index, R prev, int size, boolean pooled) {
        this.buffer = new long[size];
        Arrays.fill(this.buffer, MpscUnboundedXaddArrayLongQueue.EMPTY);
        U.putReference(this, PREV_OFFSET, prev);
        U.putLong(this, INDEX_OFFSET, index);
        this.pooled = pooled;
    }

    static long calcLongElementOffset(int index) {
        return LONG_ARR_BASE + (((long) index) << LONG_ARR_SHIFT);
    }

    final long spinForElement(int index, boolean isEmpty) {
        final long[] buf = this.buffer;
        final long off = calcLongElementOffset(index);
        long v;
        do {
            v = U.getLongVolatile(buf, off);
            Library.onSpinWait();
        } while (isEmpty != (v == MpscUnboundedXaddArrayLongQueue.EMPTY));
        return v;
    }
}

final class MpscUnboundedXaddChunkLong extends MpUnboundedXaddChunkLong<MpscUnboundedXaddChunkLong> {
    MpscUnboundedXaddChunkLong(long index, MpscUnboundedXaddChunkLong prev, int size, boolean pooled) {
        super(index, prev, size, pooled);
    }
}

abstract class MpUnboundedXaddArrayLongQueuePad0 {
    @SuppressWarnings("unused")
    long p00, p01, p02, p03, p04, p05, p06; // 56B
}

abstract class MpUnboundedXaddArrayLongQueueProducerFields<R extends MpUnboundedXaddChunkLong<R>> extends MpUnboundedXaddArrayLongQueuePad0 {
    static final long P_INDEX_OFFSET = fieldOffset(MpUnboundedXaddArrayLongQueueProducerFields.class, "producerIndex");
    volatile long producerIndex;
}

abstract class MpUnboundedXaddArrayLongQueuePad1<R extends MpUnboundedXaddChunkLong<R>> extends MpUnboundedXaddArrayLongQueueProducerFields<R> {

    @SuppressWarnings("unused")
    long p10, p11, p12, p13, p14, p15, p16; // 56B
}

abstract class MpUnboundedXaddArrayLongQueueProducerChunk<R extends MpUnboundedXaddChunkLong<R>> extends MpUnboundedXaddArrayLongQueuePad1<R> {

    static final long P_CHUNK_OFFSET = fieldOffset(MpUnboundedXaddArrayLongQueueProducerChunk.class, "producerChunk");
    static final long P_CHUNK_INDEX_OFFSET = fieldOffset(MpUnboundedXaddArrayLongQueueProducerChunk.class, "producerChunkIndex");

    volatile R producerChunk;
    volatile long producerChunkIndex;
}

abstract class MpUnboundedXaddArrayLongQueuePad2<R extends MpUnboundedXaddChunkLong<R>> extends MpUnboundedXaddArrayLongQueueProducerChunk<R> {

    @SuppressWarnings("unused")
    long p20, p21, p22, p23, p24, p25, p26; // 56B
}

abstract class MpUnboundedXaddArrayLongQueueConsumerFields<R extends MpUnboundedXaddChunkLong<R>> extends MpUnboundedXaddArrayLongQueuePad2<R> {

    static final long C_INDEX_OFFSET = fieldOffset(MpUnboundedXaddArrayLongQueueConsumerFields.class, "consumerIndex");
    static final long C_CHUNK_OFFSET = fieldOffset(MpUnboundedXaddArrayLongQueueConsumerFields.class, "consumerChunk");
    volatile long consumerIndex;
    volatile R consumerChunk;
}

abstract class MpUnboundedXaddArrayLongQueuePad3<R extends MpUnboundedXaddChunkLong<R>> extends MpUnboundedXaddArrayLongQueueConsumerFields<R> {
    @SuppressWarnings("unused")
    long p30, p31, p32, p33, p34, p35, p36; // 56B
}
