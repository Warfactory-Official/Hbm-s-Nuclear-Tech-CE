package com.hbm.util;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ObjectPool<T> {
    private final ArrayDeque<T> pool;

    private final Supplier<T> factory;
    private final Consumer<T> reset;
    private final int cap;

    public ObjectPool(Supplier<T> factory, Consumer<T> reset, int cap) {
        this.factory = Objects.requireNonNull(factory);
        this.reset = Objects.requireNonNull(reset);
        this.cap = Math.max(1, cap);
        this.pool = new ArrayDeque<>(this.cap);
    }

    public T borrow() {
        T t = pool.pollLast();
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
        if (pool.size() < cap) pool.addLast(t);
    }

    public void clear() {
        pool.clear();
    }

    public int trimTo(int max) {
        if (max < 0) throw new IllegalArgumentException("max < 0");
        if (max == 0) {
            int size = pool.size();
            pool.clear();
            return size;
        }
        int toRemove = Math.max(0, pool.size() - max);
        int removed = 0;
        while (removed < toRemove) {
            if (pool.poll() == null) break;
            removed++;
        }
        return removed;
    }
}
