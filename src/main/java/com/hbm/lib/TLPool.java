package com.hbm.lib;

import org.jctools.queues.MpmcArrayQueue;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TLPool<T> {
    private final ThreadLocal<ArrayDeque<T>> local;
    private final MpmcArrayQueue<T> shared;

    private final Supplier<T> factory;
    private final Consumer<T> reset;
    private final int localCap;

    public TLPool(Supplier<T> factory, Consumer<T> reset, int localCap, int sharedCap) {
        this.factory = Objects.requireNonNull(factory);
        this.reset = Objects.requireNonNull(reset);
        this.localCap = Math.max(1, localCap);
        if (sharedCap <= 0) throw new IllegalArgumentException("sharedCap must be > 0");
        this.shared = new MpmcArrayQueue<>(sharedCap);
        this.local = ThreadLocal.withInitial(() -> new ArrayDeque<>(this.localCap));
    }

    public T borrow() {
        ArrayDeque<T> q = local.get();
        T t = q.pollLast();
        if (t != null) return t;

        int moved = 0;
        while (moved < localCap) {
            T s = shared.relaxedPoll();
            if (s == null) break;
            q.addLast(s);
            moved++;
        }

        t = q.pollLast();
        if (t != null) return t;

        t = factory.get();
        if (t == null) throw new NullPointerException();
        return t;
    }

    public void recycle(T t) {
        if (t == null) throw new NullPointerException();
        try {
            reset.accept(t);
        } catch (RuntimeException _) {
            return;
        }
        ArrayDeque<T> q = local.get();
        if (q.size() < localCap) {
            q.addLast(t);
            return;
        }
        shared.relaxedOffer(t);
    }

    public void clearLocal() {
        local.remove();
    }

    public int trimSharedTo(int max) {
        if (max < 0) throw new IllegalArgumentException("max < 0");
        int toRemove = Math.max(0, shared.size() - max);
        int removed = 0;
        while (removed < toRemove) {
            if (shared.poll() == null) break;
            removed++;
        }
        return removed;
    }
}
