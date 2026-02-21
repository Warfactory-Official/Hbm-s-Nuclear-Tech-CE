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

import java.util.function.LongConsumer;

import static com.hbm.lib.internal.UnsafeHolder.U;
import static com.hbm.lib.internal.UnsafeHolder.fieldOffset;

/**
 * This is a derivative work of {@link org.jctools.queues.atomic.MpscLinkedAtomicQueue MpscLinkedAtomicQueue}, licensed under Apache 2.0. <br>
 * {@linkplain Long#MIN_VALUE} is reserved as empty sentinel. Users must not offer this value, and shall treat this value as null when
 * {@link #poll()}, {@link #relaxedPoll()}, {@link #peek()}, or {@link #relaxedPeek()} returns that value.
 */
public final class MpscLinkedAtomicLongQueue extends MpscLinkedAtomicLongQueuePad2 {

    public static final long EMPTY = Long.MIN_VALUE;
    private static final long P_NODE_OFFSET = fieldOffset(MpscLinkedAtomicLongQueueProducerNodeRef.class, "producerNode");
    private static final long C_NODE_OFFSET = fieldOffset(MpscLinkedAtomicLongQueueConsumerNodeRef.class, "consumerNode");
    private static final long NEXT_OFFSET = fieldOffset(Node.class, "next");

    public MpscLinkedAtomicLongQueue() {
        final Node stub = new Node();
        U.putReferenceRelease(this, C_NODE_OFFSET, stub);
        U.putReferenceRelease(this, P_NODE_OFFSET, stub);
    }

    private static Node spinWaitForNextNode(Node curr) {
        Node next;
        while ((next = curr.lvNext()) == null) {
            Library.onSpinWait();
        }
        return next;
    }

    public boolean offer(long v) {
        final Node next = new Node(v);
        final Node prev = (Node) U.getAndSetReference(this, P_NODE_OFFSET, next);
        prev.soNext(next);
        return true;
    }

    public long poll() {
        final Node curr = (Node) U.getReferenceVolatile(this, C_NODE_OFFSET);
        Node next = curr.lvNext();
        if (next != null) {
            return getSingleConsumerNodeValue(curr, next);
        }
        if (curr != U.getReferenceVolatile(this, P_NODE_OFFSET)) {
            next = spinWaitForNextNode(curr);
            return getSingleConsumerNodeValue(curr, next);
        }
        return EMPTY;
    }

    public long relaxedPoll() {
        final Node curr = (Node) U.getReferenceVolatile(this, C_NODE_OFFSET);
        final Node next = curr.lvNext();
        if (next == null) return EMPTY;
        return getSingleConsumerNodeValue(curr, next);
    }

    public long relaxedPeek() {
        final Node next = ((Node) U.getReferenceVolatile(this, C_NODE_OFFSET)).lvNext();
        return next == null ? EMPTY : next.value;
    }

    public long peek() {
        final Node curr = (Node) U.getReferenceVolatile(this, C_NODE_OFFSET);
        Node next = curr.lvNext();
        if (next != null) return next.value;
        if (curr != U.getReferenceVolatile(this, P_NODE_OFFSET)) return spinWaitForNextNode(curr).value;
        return EMPTY;
    }

    public boolean poll(LongConsumer c) {
        final Node curr = (Node) U.getReferenceVolatile(this, C_NODE_OFFSET);
        Node next = curr.lvNext();
        if (next == null) {
            if (curr == U.getReferenceVolatile(this, P_NODE_OFFSET)) return false;
            next = spinWaitForNextNode(curr);
        }
        final long v = next.value;
        curr.soNext(curr);
        U.putReferenceRelease(this, C_NODE_OFFSET, next);
        c.accept(v);
        return true;
    }

    public boolean isEmpty() {
        return U.getReferenceVolatile(this, C_NODE_OFFSET) == U.getReferenceVolatile(this, P_NODE_OFFSET);
    }

    public void clear() {
        while (poll() != EMPTY) { /* drain */ }
    }

    public void clear(boolean ignored) {
        final Node stub = new Node();
        U.putReference(this, C_NODE_OFFSET, stub);
        U.putReference(this, P_NODE_OFFSET, stub);
    }

    private long getSingleConsumerNodeValue(Node curr, Node next) {
        final long v = next.value;
        curr.soNext(curr);
        U.putReferenceRelease(this, C_NODE_OFFSET, next);
        return v;
    }

    static final class Node {
        long value;
        @SuppressWarnings("unused")
        volatile Node next;

        Node() {
        }

        Node(long v) {
            this.value = v;
        }

        Node lvNext() {
            return (Node) U.getReferenceAcquire(this, NEXT_OFFSET);
        }

        void soNext(Node n) {
            U.putReferenceRelease(this, NEXT_OFFSET, n);
        }
    }
}

abstract class MpscLinkedAtomicLongQueuePad0 {
    @SuppressWarnings("unused")
    long p00, p01, p02, p03, p04, p05, p06; // 56B
}

abstract class MpscLinkedAtomicLongQueueProducerNodeRef extends MpscLinkedAtomicLongQueuePad0 {
    @SuppressWarnings("unused")
    volatile MpscLinkedAtomicLongQueue.Node producerNode;
}

abstract class MpscLinkedAtomicLongQueuePad1 extends MpscLinkedAtomicLongQueueProducerNodeRef {
    @SuppressWarnings("unused")
    long p10, p11, p12, p13, p14, p15, p16; // 56B
}

abstract class MpscLinkedAtomicLongQueueConsumerNodeRef extends MpscLinkedAtomicLongQueuePad1 {
    @SuppressWarnings("unused")
    volatile MpscLinkedAtomicLongQueue.Node consumerNode;
}

abstract class MpscLinkedAtomicLongQueuePad2 extends MpscLinkedAtomicLongQueueConsumerNodeRef {
    @SuppressWarnings("unused")
    long p20, p21, p22, p23, p24, p25, p26; // 56B
}
