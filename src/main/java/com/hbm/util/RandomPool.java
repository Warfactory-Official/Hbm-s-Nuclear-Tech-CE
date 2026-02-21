package com.hbm.util;

import com.hbm.lib.internal.MethodHandleHelper;
import mcp.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Random;

// not thread safe
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class RandomPool {
    private static final MethodHandle UNIQUIFIER = MethodHandleHelper.findStatic(Random.class, "seedUniquifier", MethodType.methodType(long.class));
    private static final int CAPACITY = 256;
    private static final Random[] POOL = new Random[CAPACITY];
    private static int size;

    private RandomPool() {
    }

    public static Random borrow(long seed) {
        if (size == 0) {
            return new Random(seed);
        } else {
            int i = --size;
            Random rand = POOL[i];
            POOL[i] = null;
            rand.setSeed(seed);
            return rand;
        }
    }

    public static Random borrow() {
        if (size == 0) {
            return new Random();
        } else {
            int i = --size;
            Random rand = POOL[i];
            POOL[i] = null;
            try {
                rand.setSeed((long) UNIQUIFIER.invokeExact() ^ System.nanoTime());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return rand;
        }
    }

    public static void recycle(Random rand) {
        if (size < CAPACITY) {
            POOL[size++] = rand;
        }
    }
}
