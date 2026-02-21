package com.hbm.util;

import com.hbm.lib.Library;
import com.hbm.lib.internal.UnsafeHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.hbm.lib.internal.UnsafeHolder.U;

/**
 * A lock-free, thread-safe MPSC stack that supports atomic drain.
 *
 * @author mlbv
 */
public class MpscCollector<T> {
    private static final long HEAD_OFF = UnsafeHolder.fieldOffset(MpscCollector.class, "head");
    private Node<T> head;

    public void push(T value) {
        Node<T> n = new Node<>(value, null);
        while (true) {
            // noinspection unchecked
            Node<T> h = (Node<T>) U.getObjectVolatile(this, HEAD_OFF);
            n.next = h;
            if (U.compareAndSwapObject(this, HEAD_OFF, h, n)) return;
            Library.onSpinWait();
        }
    }

    public void pushBatch(@NotNull List<T> values) {
        int size = values.size();
        if (size == 0) return;
        Node<T> headNode = null;
        Node<T> tailNode = null;
        for (int i = 0; i < size; i++) {
            Node<T> n = new Node<>(values.get(i), headNode);
            headNode = n;
            if (tailNode == null) tailNode = n;
        }
        while (true) {
            // noinspection unchecked
            Node<T> h = (Node<T>) U.getObjectVolatile(this, HEAD_OFF);
            tailNode.next = h;
            if (U.compareAndSwapObject(this, HEAD_OFF, h, headNode)) return;
            Library.onSpinWait();
        }
    }

    @NotNull
    public List<T> drain() {
        // noinspection unchecked
        Node<T> h = (Node<T>) U.getAndSetObject(this, HEAD_OFF, null);
        ArrayList<T> out = new ArrayList<>();
        for (Node<T> p = h; p != null; p = p.next) out.add(p.v);
        return out;
    }

    /**
     * @return the number of elements drained
     */
    @Contract(mutates = "param1")
    public int drainTo(@NotNull Collection<? super T> out) {
        // noinspection unchecked
        Node<T> h = (Node<T>) U.getAndSetObject(this, HEAD_OFF, null);
        int n = 0;
        for (Node<T> p = h; p != null; p = p.next) {
            out.add(p.v);
            n++;
        }
        return n;
    }

    private static final class Node<T> {
        private final T v;
        private Node<T> next;

        Node(T v, Node<T> next) {
            this.v = v;
            this.next = next;
        }
    }
}
