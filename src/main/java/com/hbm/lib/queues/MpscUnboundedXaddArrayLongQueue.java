/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hbm.lib.queues;

import com.hbm.lib.Library;
import org.jctools.util.PortableJvmInfo;

import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

import static com.hbm.lib.internal.UnsafeHolder.U;

/**
 * A long-specialized derivative work of {@link org.jctools.queues.MpscUnboundedXaddArrayQueue MpscUnboundedXaddArrayQueue},
 * licensed under Apache 2.0.
 *
 * <p>{@link Long#MIN_VALUE} is reserved as the EMPTY sentinel:
 * users must not offer this value, and shall treat it as null when returned.
 */
public final class MpscUnboundedXaddArrayLongQueue extends MpUnboundedXaddArrayLongQueue<MpscUnboundedXaddChunkLong> {

    public static final long EMPTY = Long.MIN_VALUE;

    public MpscUnboundedXaddArrayLongQueue(int chunkSize, int maxPooledChunks) {
        super(chunkSize, maxPooledChunks);
    }

    public MpscUnboundedXaddArrayLongQueue(int chunkSize) {
        this(chunkSize, 2);
    }

    @Override
    MpscUnboundedXaddChunkLong newChunk(long index, MpscUnboundedXaddChunkLong prev, int chunkSize, boolean pooled) {
        return new MpscUnboundedXaddChunkLong(index, prev, chunkSize, pooled);
    }

    public boolean offer(long v) {
        if (v == EMPTY) {
            throw new IllegalArgumentException("Long.MIN_VALUE is reserved as EMPTY sentinel");
        }

        final int chunkMask = this.chunkMask;
        final int chunkShift = this.chunkShift;

        final long pIndex = U.getAndAddLong(this, P_INDEX_OFFSET, 1L);

        final int piChunkOffset = (int) (pIndex & chunkMask);
        final long piChunkIndex = pIndex >> chunkShift;

        MpscUnboundedXaddChunkLong pChunk = producerChunk;
        if (pChunk.index != piChunkIndex) {
            pChunk = producerChunkForIndex(pChunk, piChunkIndex);
        }
        U.putLongRelease(pChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(piChunkOffset), v);
        return true;
    }

    @Override
    public long poll() {
        final int chunkMask = this.chunkMask;
        final long cIndex = U.getLong(this, C_INDEX_OFFSET);
        final int ciChunkOffset = (int) (cIndex & chunkMask);

        MpscUnboundedXaddChunkLong cChunk = consumerChunk;
        // start of new chunk?
        if (ciChunkOffset == 0 && cIndex != 0) {
            cChunk = pollNextBuffer(cChunk, cIndex);
            if (cChunk == null) {
                return EMPTY;
            }
        }

        long e = U.getLongVolatile(cChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(ciChunkOffset));
        if (e == EMPTY) {
            if (producerIndex == cIndex) {
                return EMPTY;
            } else {
                e = cChunk.spinForElement(ciChunkOffset, true);
            }
        }
        U.putLongRelease(cChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(ciChunkOffset), EMPTY);
        U.putLongRelease(this, C_INDEX_OFFSET, cIndex + 1);
        return e;
    }

    @Override
    public long peek() {
        final int chunkMask = this.chunkMask;
        final long cIndex = U.getLong(this, C_INDEX_OFFSET);
        final int ciChunkOffset = (int) (cIndex & chunkMask);

        MpscUnboundedXaddChunkLong cChunk = (MpscUnboundedXaddChunkLong) U.getReference(this, C_CHUNK_OFFSET);
        // start of new chunk?
        if (ciChunkOffset == 0 && cIndex != 0) {
            cChunk = spinForNextIfNotEmpty(cChunk, cIndex);
            if (cChunk == null) {
                return EMPTY;
            }
        }

        long e = U.getLongVolatile(cChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(ciChunkOffset));
        if (e == EMPTY) {
            if (producerIndex == cIndex) {
                return EMPTY;
            } else {
                e = cChunk.spinForElement(ciChunkOffset, true);
            }
        }
        return e;
    }

    @Override
    public long relaxedPoll() {
        final int chunkMask = this.chunkMask;
        final long cIndex = U.getLong(this, C_INDEX_OFFSET);
        final int ciChunkOffset = (int) (cIndex & chunkMask);

        MpscUnboundedXaddChunkLong cChunk = (MpscUnboundedXaddChunkLong) U.getReference(this, C_CHUNK_OFFSET);
        long e;

        // start of new chunk?
        if (ciChunkOffset == 0 && cIndex != 0) {
            final MpscUnboundedXaddChunkLong next = cChunk.next;
            if (next == null) {
                return EMPTY;
            }
            e = U.getLongVolatile(next.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(0));

            // if the next chunk doesn't have the first element set we give up
            if (e == EMPTY) {
                return EMPTY;
            }
            moveToNextConsumerChunk(cChunk, next);
            cChunk = next;
        } else {
            e = U.getLongVolatile(cChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(ciChunkOffset));
            if (e == EMPTY) {
                return EMPTY;
            }
        }

        U.putLongRelease(cChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(ciChunkOffset), EMPTY);
        U.putLongRelease(this, C_INDEX_OFFSET, cIndex + 1);
        return e;
    }

    public long plainPoll() {
        final int chunkMask = this.chunkMask;
        final long cIndex = U.getLong(this, C_INDEX_OFFSET);
        final int ciChunkOffset = (int) (cIndex & chunkMask);
        MpscUnboundedXaddChunkLong cChunk = (MpscUnboundedXaddChunkLong) U.getReference(this, C_CHUNK_OFFSET);
        if (ciChunkOffset == 0 && cIndex != 0) {
            final MpscUnboundedXaddChunkLong next = cChunk.next;
            if (next == null) return EMPTY;
            long e = next.buffer[0];
            if (e == EMPTY) return EMPTY;
            U.putReference(cChunk, MpUnboundedXaddChunkLong.NEXT_OFFSET, null);
            U.putReference(next, MpUnboundedXaddChunkLong.PREV_OFFSET, null);
            if (cChunk.pooled) {
                freeChunksPool.offer(cChunk);
            }
            U.putReference(this, C_CHUNK_OFFSET, next);
            next.buffer[0] = EMPTY;
            U.putLong(this, C_INDEX_OFFSET, cIndex + 1);
            return e;
        }
        long e = cChunk.buffer[ciChunkOffset];
        if (e == EMPTY) return EMPTY;
        cChunk.buffer[ciChunkOffset] = EMPTY;
        U.putLong(this, C_INDEX_OFFSET, cIndex + 1);
        return e;
    }

    @Override
    public long relaxedPeek() {
        final int chunkMask = this.chunkMask;
        final long cIndex = U.getLong(this, C_INDEX_OFFSET);
        final int cChunkOffset = (int) (cIndex & chunkMask);

        MpscUnboundedXaddChunkLong cChunk = (MpscUnboundedXaddChunkLong) U.getReference(this, C_CHUNK_OFFSET);
        // start of new chunk?
        if (cChunkOffset == 0 && cIndex != 0) {
            cChunk = cChunk.next;
            if (cChunk == null) {
                return EMPTY;
            }
        }
        return U.getLongVolatile(cChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(cChunkOffset));
    }

    public int drain(LongConsumer c, int limit) {
        if (c == null) throw new IllegalArgumentException("c is null");
        if (limit < 0) throw new IllegalArgumentException("limit is negative: " + limit);
        if (limit == 0) return 0;

        final int chunkMask = this.chunkMask;

        long cIndex = U.getLong(this, C_INDEX_OFFSET);
        MpscUnboundedXaddChunkLong cChunk = (MpscUnboundedXaddChunkLong) U.getReference(this, C_CHUNK_OFFSET);

        for (int i = 0; i < limit; i++) {
            final int consumerOffset = (int) (cIndex & chunkMask);

            long e;
            if (consumerOffset == 0 && cIndex != 0) {
                final MpscUnboundedXaddChunkLong next = cChunk.next;
                if (next == null) {
                    return i;
                }
                e = U.getLongVolatile(next.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(0));

                // if the next chunk doesn't have the first element set we give up
                if (e == EMPTY) {
                    return i;
                }
                moveToNextConsumerChunk(cChunk, next);
                cChunk = next;
            } else {
                e = U.getLongVolatile(cChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(consumerOffset));
                if (e == EMPTY) {
                    return i;
                }
            }

            U.putLongRelease(cChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(consumerOffset), EMPTY);
            final long nextConsumerIndex = cIndex + 1;
            U.putLongRelease(this, C_INDEX_OFFSET, nextConsumerIndex);
            c.accept(e);
            cIndex = nextConsumerIndex;
        }
        return limit;
    }

    public int fill(LongSupplier s) {
        if (s == null) throw new IllegalArgumentException("supplier is null");

        long result = 0; // long to keep periodic safepoints
        final int capacity = chunkMask + 1;
        final int offerBatch = Math.min(PortableJvmInfo.RECOMENDED_OFFER_BATCH, capacity);
        do {
            final int filled = fill(s, offerBatch);
            if (filled == 0) {
                return (int) result;
            }
            result += filled;
        } while (result <= capacity);
        return (int) result;
    }

    public int fill(LongSupplier s, int limit) {
        if (s == null) throw new IllegalArgumentException("supplier is null");
        if (limit < 0) throw new IllegalArgumentException("limit is negative:" + limit);
        if (limit == 0) return 0;

        final int chunkShift = this.chunkShift;
        final int chunkMask = this.chunkMask;

        long pIndex = U.getAndAddLong(this, P_INDEX_OFFSET, limit);

        MpscUnboundedXaddChunkLong pChunk = null;
        for (int i = 0; i < limit; i++) {
            final int pChunkOffset = (int) (pIndex & chunkMask);
            final long chunkIndex = pIndex >> chunkShift;
            if (pChunk == null || pChunk.index != chunkIndex) {
                pChunk = producerChunkForIndex(pChunk, chunkIndex);
            }

            final long v = s.getAsLong();
            if (v == EMPTY) {
                throw new IllegalArgumentException("Long.MIN_VALUE is reserved as EMPTY sentinel");
            }
            U.putLongRelease(pChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(pChunkOffset), v);
            pIndex++;
        }
        return limit;
    }

    public boolean isEmpty() {
        return producerIndex == consumerIndex;
    }

    public void clear() {
        while (poll() != EMPTY) { /* drain */ }
    }

    // non-atomic clear
    public void clear(boolean scrub) {
        final long pIndex = this.producerIndex;
        final int pOffset = (int) (pIndex & this.chunkMask);
        final MpscUnboundedXaddChunkLong pChunk = this.producerChunk;
        if (scrub) {
            for (int i = 0; i < pOffset; i++) U.putLongRelease(pChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(i), EMPTY);
        }
        if (pChunk.prev != null) U.putReferenceRelease(pChunk, MpUnboundedXaddChunkLong.PREV_OFFSET, null);
        if (pChunk.next != null) U.putReferenceRelease(pChunk, MpUnboundedXaddChunkLong.NEXT_OFFSET, null);
        U.putReferenceRelease(this, C_CHUNK_OFFSET, pChunk);
        U.putLongRelease(this, C_INDEX_OFFSET, pIndex);
    }

    private MpscUnboundedXaddChunkLong pollNextBuffer(MpscUnboundedXaddChunkLong cChunk, long cIndex) {
        final MpscUnboundedXaddChunkLong next = spinForNextIfNotEmpty(cChunk, cIndex);
        if (next == null) {
            return null;
        }
        moveToNextConsumerChunk(cChunk, next);
        assert next.index == (cIndex >> chunkShift);
        return next;
    }

    private MpscUnboundedXaddChunkLong spinForNextIfNotEmpty(MpscUnboundedXaddChunkLong cChunk, long cIndex) {
        MpscUnboundedXaddChunkLong next = cChunk.next;
        if (next == null) {
            if (producerIndex == cIndex) {
                return null;
            }
            final long ccChunkIndex = cChunk.index;
            if (producerChunkIndex == ccChunkIndex) {
                // don't help too much: preserve consumer latency
                next = appendNextChunks(cChunk, ccChunkIndex, 1);
            }
            while (next == null) {
                Library.onSpinWait();
                next = cChunk.next;
            }
        }
        return next;
    }
}
