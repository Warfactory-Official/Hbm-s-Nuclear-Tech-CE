package com.hbm.interfaces;

import org.jetbrains.annotations.Contract;

public interface BitMask {
    boolean get(int bit);

    void set(int bit);

    boolean getAndSet(int bit);

    @Contract(pure = true)
    int nextSetBit(int from);

    @Contract(pure = true)
    int nextClearBit(int from);

    @Contract(pure = true)
    int previousSetBit(int from);

    @Contract(pure = true)
    int previousClearBit(int from);

    @Contract(pure = true)
    boolean isEmpty();

    @Contract(pure = true)
    long cardinality();

    @Contract(pure = true)
    int length();

    @Contract(pure = true)
    int size();

    @Contract(pure = true)
    int logicalSize();

    long[] toLongArray();

    default void free() {
    }
}

