package com.hbm.util;

import com.hbm.lib.Library;
import com.hbm.lib.TLPool;
import com.hbm.lib.internal.UnsafeHolder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static com.hbm.lib.internal.UnsafeHolder.U;

/**
 * A lock-free, thread-safe MPSC {@link IntArrayList} stack that supports atomic drain.
 *
 * @author mlbv
 */
public class MpscIntArrayListCollector {
    private static final long HEAD_OFF = UnsafeHolder.fieldOffset(MpscIntArrayListCollector.class, "head");
    private static final TLPool<Node> NODE_POOL = new TLPool<>(() -> new Node(0, null), n -> n.next = null, 64, 4096);
    private Node head;

    public void push(int i) {
        Node n = NODE_POOL.borrow();
        n.v = i;
        n.next = null;
        while (true) {
            Node h = (Node) U.getObjectVolatile(this, HEAD_OFF);
            n.next = h;
            if (U.compareAndSwapObject(this, HEAD_OFF, h, n)) return;
            Library.onSpinWait();
        }
    }

    public void pushBatch(@NotNull IntList values) {
        int size = values.size();
        if (size == 0) return;
        Node headNode = null;
        Node tailNode = null;
        for (int i = 0; i < size; i++) {
            Node n = NODE_POOL.borrow();
            n.v = values.getInt(i);
            n.next = headNode;
            headNode = n;
            if (tailNode == null) tailNode = n;
        }
        while (true) {
            Node h = (Node) U.getObjectVolatile(this, HEAD_OFF);
            tailNode.next = h;
            if (U.compareAndSwapObject(this, HEAD_OFF, h, headNode)) return;
            Library.onSpinWait();
        }
    }

    @NotNull
    public IntArrayList drain() {
        Node h = (Node) U.getAndSetObject(this, HEAD_OFF, null);
        if (h == null) return new IntArrayList(0);
        int n = 0;
        for (Node p = h; p != null; p = p.next) n++;
        IntArrayList out = new IntArrayList(n);
        out.size(n);
        int[] a = out.elements();
        int i = 0;
        for (Node p = h; p != null; ) {
            a[i++] = p.v; // LIFO
            Node next = p.next;
            NODE_POOL.recycle(p);
            p = next;
        }
        return out;
    }


    /**
     * @return the number of elements drained
     */
    @Contract(mutates = "param1")
    public int drainTo(@NotNull IntArrayList l) {
        Node h = (Node) U.getAndSetObject(this, HEAD_OFF, null);
        if (h == null) return 0;
        int n = 0;
        for (Node p = h; p != null; p = p.next) n++;
        int base = l.size();
        l.ensureCapacity(base + n);
        l.size(base + n);
        int[] a = l.elements();
        int i = base;
        for (Node p = h; p != null; ) {
            a[i++] = p.v;
            Node next = p.next;
            NODE_POOL.recycle(p);
            p = next;
        }
        return n;
    }

    private static final class Node {
        private int v;
        private Node next;

        Node(int v, Node next) {
            this.v = v;
            this.next = next;
        }
    }
}
