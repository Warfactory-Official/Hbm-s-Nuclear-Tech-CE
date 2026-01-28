package com.hbm.util;

import java.util.Random;

// not thread safe
public final class RandomPool {

    private static final int CAPACITY = 256;
    private static final Random[] POOL = new Random[CAPACITY];
    private static int size;

    private RandomPool() {
    }

    public static Random borrow(long seed) {
        Random rand;
        if (size == 0) {
            rand = new Random();
        } else {
            int i = --size;
            rand = POOL[i];
            POOL[i] = null;
        }
        rand.setSeed(seed);
        return rand;
    }

    public static void recycle(Random rand) {
        if (rand == null) return;
        if (size < CAPACITY) {
            POOL[size++] = rand;
        }
    }
}
