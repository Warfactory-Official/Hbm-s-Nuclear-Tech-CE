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

import java.util.Arrays;
import java.util.function.LongSupplier;

import static com.hbm.lib.internal.UnsafeHolder.U;

/**
 * A long-specialized derivative work of {@link org.jctools.queues.MpmcUnboundedXaddArrayQueue MpmcUnboundedXaddArrayQueue},
 * licensed under Apache 2.0.
 *
 * <p>Users should be aware that {@link #poll()} could spin while awaiting a new element to be available:
 * to avoid this behaviour {@link #relaxedPoll()} should be used instead, accounting for the semantic differences.
 *
 * <p>{@link Long#MIN_VALUE} is reserved as the EMPTY sentinel:
 * users must not offer this value, and shall treat it as null when returned.
 */
public final class MpmcUnboundedXaddArrayLongQueue extends MpUnboundedXaddArrayLongQueue<MpmcUnboundedXaddChunkLong> {
    public static final long EMPTY = MpscUnboundedXaddArrayLongQueue.EMPTY;

    /**
     * @param chunkSize The buffer size to be used in each chunk of this queue
     * @param maxPooledChunks The maximum number of reused chunks kept around to avoid allocation, chunks are pre-allocated
     */
    public MpmcUnboundedXaddArrayLongQueue(int chunkSize, int maxPooledChunks) {
        super(chunkSize, maxPooledChunks);
    }

    public MpmcUnboundedXaddArrayLongQueue(int chunkSize) {
        this(chunkSize, 2);
    }

    @Override
    MpmcUnboundedXaddChunkLong newChunk(long index, MpmcUnboundedXaddChunkLong prev, int chunkSize, boolean pooled) {
        return new MpmcUnboundedXaddChunkLong(index, prev, chunkSize, pooled);
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

        MpmcUnboundedXaddChunkLong pChunk = this.producerChunk;
        if (pChunk.index != piChunkIndex) {
            // Other producers may have advanced the producer chunk as we claimed a slot in a prev chunk, or we may have
            // now stepped into a brand new chunk which needs appending.
            pChunk = producerChunkForIndex(pChunk, piChunkIndex);
        }

        if (pChunk.pooled) {
            // wait any previous consumer to finish its job on this slot
            pChunk.spinForElement(piChunkOffset, true);
        }

        U.putLongRelease(pChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(piChunkOffset), v);

        if (pChunk.pooled) {
            // publish sequence after element
            pChunk.soSequence(piChunkOffset, piChunkIndex);
        }
        return true;
    }

    @Override
    public long poll() {
        final int chunkMask = this.chunkMask;
        final int chunkShift = this.chunkShift;

        long cIndex;
        MpmcUnboundedXaddChunkLong cChunk;
        int ciChunkOffset;
        boolean isFirstElementOfNewChunk;
        boolean pooled = false;
        long e = EMPTY;
        MpmcUnboundedXaddChunkLong next = null;
        long pIndex = -1; // start with bogus value, hope we don't need it
        long ciChunkIndex;

        while (true) {
            isFirstElementOfNewChunk = false;

            cIndex = this.consumerIndex;
            // chunk is in sync with the index, and is safe to mutate after CAS of index (because we pre-verify it
            // matched the indicated ciChunkIndex)
            cChunk = (MpmcUnboundedXaddChunkLong) U.getReferenceVolatile(this, C_CHUNK_OFFSET);

            ciChunkOffset = (int) (cIndex & chunkMask);
            ciChunkIndex = cIndex >> chunkShift;

            final long ccChunkIndex = cChunk.index;

            if (ciChunkOffset == 0 && cIndex != 0) {
                if (ciChunkIndex - ccChunkIndex != 1) {
                    continue;
                }
                isFirstElementOfNewChunk = true;
                next = cChunk.next;
                // next could have been modified by another racing consumer, but:
                // - if null: it still needs to check q empty + casConsumerIndex
                // - if !null: it will fail on casConsumerIndex
                if (next == null) {
                    if (cIndex >= pIndex && // test against cached pIndex
                            cIndex == (pIndex = this.producerIndex)) // update pIndex if we must
                    {
                        // strict empty check, this ensures [Queue.poll() == EMPTY iff isEmpty()]
                        return EMPTY;
                    }
                    // we will go ahead with the CAS and have the winning consumer spin for the next buffer
                }
                // not empty: can attempt the cas (and transition to next chunk if successful)
                if (U.compareAndSetLong(this, C_INDEX_OFFSET, cIndex, cIndex + 1)) {
                    break;
                }
                continue;
            }

            if (ccChunkIndex > ciChunkIndex) {
                // stale view of the world
                continue;
            }

            // mid chunk elements
            pooled = cChunk.pooled;
            if (ccChunkIndex == ciChunkIndex) {
                if (pooled) {
                    // Pooled chunks need a stronger guarantee than just element EMPTY checking in case of a stale view
                    // on a reused entry where a racing consumer has grabbed the slot but not yet EMPTY-ed it out and a
                    // producer has not yet set it to the new value.
                    final long sequence = cChunk.lvSequence(ciChunkOffset);
                    if (sequence == ciChunkIndex) {
                        if (U.compareAndSetLong(this, C_INDEX_OFFSET, cIndex, cIndex + 1)) {
                            break;
                        }
                        continue;
                    }
                    if (sequence > ciChunkIndex) {
                        // stale view of the world
                        continue;
                    }
                    // sequence < ciChunkIndex: element yet to be set?
                } else {
                    e = U.getLongVolatile(cChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(ciChunkOffset));
                    if (e != EMPTY) {
                        if (U.compareAndSetLong(this, C_INDEX_OFFSET, cIndex, cIndex + 1)) {
                            break;
                        }
                        continue;
                    }
                    // e == EMPTY: element yet to be set?
                }
            }

            // ccChunkIndex < ciChunkIndex || e == EMPTY || sequence < ciChunkIndex:
            if (cIndex >= pIndex && // test against cached pIndex
                    cIndex == (pIndex = this.producerIndex)) // update pIndex if we must
            {
                // strict empty check, this ensures [Queue.poll() == EMPTY iff isEmpty()]
                return EMPTY;
            }
        }

        // if we are the isFirstElementOfNewChunk we need to get the consumer chunk
        if (isFirstElementOfNewChunk) {
            e = switchToNextConsumerChunkAndPoll(cChunk, next, ciChunkIndex);
        } else {
            if (pooled) {
                e = U.getLongVolatile(cChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(ciChunkOffset));
            }
            // for pooled: if sequence matched, element must be visible now
            U.putLongRelease(cChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(ciChunkOffset), EMPTY);
        }
        return e;
    }

    private long switchToNextConsumerChunkAndPoll(
            MpmcUnboundedXaddChunkLong cChunk,
            MpmcUnboundedXaddChunkLong next,
            long expectedChunkIndex
    ) {
        if (next == null) {
            final long ccChunkIndex = expectedChunkIndex - 1;
            if (this.producerChunkIndex == ccChunkIndex) {
                next = appendNextChunks(cChunk, ccChunkIndex, 1);
            }
        }

        while (next == null) {
            Library.onSpinWait();
            next = cChunk.next;
        }

        final long e = next.spinForElement(0, false);

        if (next.pooled) {
            next.spinForSequence(0, expectedChunkIndex);
        }

        U.putLongRelease(next.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(0), EMPTY);
        moveToNextConsumerChunk(cChunk, next);
        return e;
    }

    @Override
    public long peek() {
        final int chunkMask = this.chunkMask;
        final int chunkShift = this.chunkShift;
        long cIndex;
        long e;

        do {
            e = EMPTY;
            cIndex = this.consumerIndex;

            MpmcUnboundedXaddChunkLong cChunk = (MpmcUnboundedXaddChunkLong) U.getReferenceVolatile(this, C_CHUNK_OFFSET);
            final int ciChunkOffset = (int) (cIndex & chunkMask);
            final long ciChunkIndex = cIndex >> chunkShift;
            final boolean firstElementOfNewChunk = ciChunkOffset == 0 && cIndex != 0;

            if (firstElementOfNewChunk) {
                final long expectedChunkIndex = ciChunkIndex - 1;
                if (expectedChunkIndex != cChunk.index) {
                    continue;
                }
                final MpmcUnboundedXaddChunkLong next = cChunk.next;
                if (next == null) {
                    continue;
                }
                cChunk = next;
            }

            if (cChunk.pooled) {
                if (cChunk.lvSequence(ciChunkOffset) != ciChunkIndex) {
                    continue;
                }
            } else {
                if (cChunk.index != ciChunkIndex) {
                    continue;
                }
            }

            e = U.getLongVolatile(cChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(ciChunkOffset));
        }
        while ((e == EMPTY && cIndex != this.producerIndex) ||
                (e != EMPTY && cIndex != this.consumerIndex));

        return e;
    }

    @Override
    public long relaxedPoll() {
        final int chunkMask = this.chunkMask;
        final int chunkShift = this.chunkShift;

        final long cIndex = consumerIndex;
        final MpmcUnboundedXaddChunkLong cChunk = consumerChunk;

        final int ciChunkOffset = (int) (cIndex & chunkMask);
        final long ciChunkIndex = cIndex >> chunkShift;

        final boolean firstElementOfNewChunk = ciChunkOffset == 0 && cIndex != 0;

        if (firstElementOfNewChunk) {
            final long expectedChunkIndex = ciChunkIndex - 1;

            final long ccChunkIndex = cChunk.index;
            final MpmcUnboundedXaddChunkLong next =
                    (expectedChunkIndex != ccChunkIndex)
                            ? null
                            : cChunk.next;

            if (next == null) {
                return EMPTY;
            }

            long e;
            if (next.pooled) {
                if (next.lvSequence(0) != ciChunkIndex) {
                    return EMPTY;
                }
                e = U.getLongVolatile(next.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(0));
            } else {
                e = U.getLongVolatile(next.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(0));
                if (e == EMPTY) {
                    return EMPTY;
                }
            }

            if (!U.compareAndSetLong(this, C_INDEX_OFFSET, cIndex, cIndex + 1)) {
                return EMPTY;
            }

            if (next.pooled) {
                e = U.getLongVolatile(next.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(0));
            }
            if (e == EMPTY) {
                return EMPTY;
            }

            U.putLongRelease(next.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(0), EMPTY);
            moveToNextConsumerChunk(cChunk, next);
            return e;
        } else {
            final boolean pooled = cChunk.pooled;
            long e;

            if (pooled) {
                if (cChunk.lvSequence(ciChunkOffset) != ciChunkIndex) {
                    return EMPTY;
                }
                e = U.getLongVolatile(cChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(ciChunkOffset));
            } else {
                final long ccChunkIndex = cChunk.index;
                e = U.getLongVolatile(cChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(ciChunkOffset));
                if (ccChunkIndex != ciChunkIndex || e == EMPTY) {
                    return EMPTY;
                }
            }

            if (!U.compareAndSetLong(this, C_INDEX_OFFSET, cIndex, cIndex + 1)) {
                return EMPTY;
            }

            if (pooled) {
                e = U.getLongVolatile(cChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(ciChunkOffset));
                if (e == EMPTY) {
                    return EMPTY;
                }
            }

            U.putLongRelease(cChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(ciChunkOffset), EMPTY);
            return e;
        }
    }

    @Override
    public long relaxedPeek() {
        final int chunkMask = this.chunkMask;
        final int chunkShift = this.chunkShift;

        final long cIndex = this.consumerIndex;
        final int ciChunkOffset = (int) (cIndex & chunkMask);
        final long ciChunkIndex = cIndex >> chunkShift;

        MpmcUnboundedXaddChunkLong consumerChunk = (MpmcUnboundedXaddChunkLong) U.getReferenceVolatile(this, C_CHUNK_OFFSET);

        final int chunkSize = chunkMask + 1;
        final boolean firstElementOfNewChunk = ciChunkOffset == 0 && cIndex >= chunkSize;

        if (firstElementOfNewChunk) {
            final long expectedChunkIndex = ciChunkIndex - 1;
            if (expectedChunkIndex != consumerChunk.index) {
                return EMPTY;
            }
            final MpmcUnboundedXaddChunkLong next = consumerChunk.next;
            if (next == null) {
                return EMPTY;
            }
            consumerChunk = next;
        }

        if (consumerChunk.pooled) {
            if (consumerChunk.lvSequence(ciChunkOffset) != ciChunkIndex) {
                return EMPTY;
            }
        } else {
            if (consumerChunk.index != ciChunkIndex) {
                return EMPTY;
            }
        }

        final long e = U.getLongVolatile(consumerChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(ciChunkOffset));
        // checking again vs consumerIndex changes is necessary to verify that e is still valid
        if (cIndex != this.consumerIndex) {
            return EMPTY;
        }
        return e;
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

        long producerSeq = U.getAndAddLong(this, P_INDEX_OFFSET, limit);
        MpmcUnboundedXaddChunkLong producerChunk = null;

        for (int i = 0; i < limit; i++) {
            final int pOffset = (int) (producerSeq & chunkMask);
            long chunkIndex = producerSeq >> chunkShift;

            if (producerChunk == null || producerChunk.index != chunkIndex) {
                producerChunk = producerChunkForIndex(producerChunk, chunkIndex);
                if (producerChunk.pooled) {
                    // keep parity with upstream: if pooled, trust the actual chunk index
                    chunkIndex = producerChunk.index;
                }
            }

            if (producerChunk.pooled) {
                final long[] buf = producerChunk.buffer;
                final long off = MpUnboundedXaddChunkLong.calcLongElementOffset(pOffset);
                while (U.getLongVolatile(buf, off) != EMPTY) {
                    Library.onSpinWait();
                }
            }

            final long v = s.getAsLong();
            if (v == EMPTY) {
                throw new IllegalArgumentException("Long.MIN_VALUE is reserved as EMPTY sentinel");
            }

            U.putLongRelease(producerChunk.buffer, MpUnboundedXaddChunkLong.calcLongElementOffset(pOffset), v);
            if (producerChunk.pooled) {
                producerChunk.soSequence(pOffset, chunkIndex);
            }

            producerSeq++;
        }

        return limit;
    }

    public boolean isEmpty() {
        return this.producerIndex == this.consumerIndex;
    }

    public void clear() {
        while (poll() != EMPTY) { /* drain */ }
    }
}

final class MpmcUnboundedXaddChunkLong extends MpUnboundedXaddChunkLong<MpmcUnboundedXaddChunkLong> {
    private final long[] sequence;

    MpmcUnboundedXaddChunkLong(long index, MpmcUnboundedXaddChunkLong prev, int size, boolean pooled) {
        super(index, prev, size, pooled);
        if (pooled) {
            this.sequence = new long[size];
            Arrays.fill(this.sequence, index - 1);
        } else {
            this.sequence = null;
        }
    }

    long lvSequence(int offset) {
        final long[] seq = this.sequence;
        if (seq == null) {
            return Long.MIN_VALUE;
        }
        return U.getLongVolatile(seq, MpUnboundedXaddChunkLong.calcLongElementOffset(offset));
    }

    void soSequence(int offset, long chunkIndex) {
        U.putLongRelease(this.sequence, MpUnboundedXaddChunkLong.calcLongElementOffset(offset), chunkIndex);
    }

    void spinForSequence(int offset, long expectedChunkIndex) {
        final long[] seq = this.sequence;
        final long off = MpUnboundedXaddChunkLong.calcLongElementOffset(offset);
        while (U.getLongVolatile(seq, off) != expectedChunkIndex) {
            Library.onSpinWait();
        }
    }
}
