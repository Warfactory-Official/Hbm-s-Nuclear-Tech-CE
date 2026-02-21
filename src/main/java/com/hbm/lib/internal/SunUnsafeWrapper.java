package com.hbm.lib.internal;

import java.lang.reflect.Field;

/**
 * Direct wrapper for sun.misc.Unsafe
 */
@SuppressWarnings("removal")
public final class SunUnsafeWrapper extends AbstractUnsafe {

    SunUnsafeWrapper() {
    }

    public long objectFieldOffset(Field f) {
        return sunUnsafe.objectFieldOffset(f);
    }

    public Object staticFieldBase(Field f) {
        return sunUnsafe.staticFieldBase(f);
    }

    public long staticFieldOffset(Field f) {
        return sunUnsafe.staticFieldOffset(f);
    }

    public Object allocateInstance(Class<?> cls) throws InstantiationException {
        return sunUnsafe.allocateInstance(cls);
    }

    public Object allocateUninitializedArray(Class<?> componentType, int length) {
        if (componentType == null) throw new IllegalArgumentException("Component type is null");
        if (!componentType.isPrimitive()) throw new IllegalArgumentException("Component type is not primitive");
        if (length < 0) throw new IllegalArgumentException("Negative length");
        if (componentType == byte.class) return new byte[length];
        if (componentType == boolean.class) return new boolean[length];
        if (componentType == short.class) return new short[length];
        if (componentType == char.class) return new char[length];
        if (componentType == int.class) return new int[length];
        if (componentType == float.class) return new float[length];
        if (componentType == long.class) return new long[length];
        if (componentType == double.class) return new double[length];
        return null;
    }

    public long arrayBaseOffset(Class<?> cls) {
        return sunUnsafe.arrayBaseOffset(cls);
    }

    public int arrayIndexScale(Class<?> cls) {
        return sunUnsafe.arrayIndexScale(cls);
    }

    public int addressSize() {
        return sunUnsafe.addressSize();
    }

    public int pageSize() {
        return sunUnsafe.pageSize();
    }

    // --- 2. References ---

    public Object getReference(Object o, long offset) {
        return sunUnsafe.getObject(o, offset);
    }

    public void putReference(Object o, long offset, Object x) {
        sunUnsafe.putObject(o, offset, x);
    }

    public Object getReferenceVolatile(Object o, long offset) {
        return sunUnsafe.getObjectVolatile(o, offset);
    }

    public void putReferenceVolatile(Object o, long offset, Object x) {
        sunUnsafe.putObjectVolatile(o, offset, x);
    }

    public Object getReferenceAcquire(Object o, long offset) {
        Object v = sunUnsafe.getObject(o, offset);
        sunUnsafe.loadFence();
        return v;
    }

    public void putReferenceRelease(Object o, long offset, Object x) {
        sunUnsafe.putOrderedObject(o, offset, x);
    }

    public Object getReferenceOpaque(Object o, long offset) {
        return getReferenceAcquire(o, offset);
    }

    public void putReferenceOpaque(Object o, long offset, Object x) {
        putReferenceRelease(o, offset, x);
    }

    public boolean compareAndSetReference(Object o, long offset, Object expected, Object x) {
        return sunUnsafe.compareAndSwapObject(o, offset, expected, x);
    }

    public Object getAndSetReference(Object o, long offset, Object x) {
        return sunUnsafe.getAndSetObject(o, offset, x);
    }

    public boolean weakCompareAndSetReference(Object o, long offset, Object expected, Object x) {
        return sunUnsafe.compareAndSwapObject(o, offset, expected, x);
    }

    public boolean weakCompareAndSetReferenceAcquire(Object o, long offset, Object expected, Object x) {
        return sunUnsafe.compareAndSwapObject(o, offset, expected, x);
    }

    public boolean weakCompareAndSetReferenceRelease(Object o, long offset, Object expected, Object x) {
        return sunUnsafe.compareAndSwapObject(o, offset, expected, x);
    }

    public boolean weakCompareAndSetReferencePlain(Object o, long offset, Object expected, Object x) {
        return sunUnsafe.compareAndSwapObject(o, offset, expected, x);
    }

    public Object compareAndExchangeReference(Object o, long offset, Object expected, Object x) {
        while (true) {
            Object witness = sunUnsafe.getObjectVolatile(o, offset);
            if (witness != expected) {
                return witness;
            }
            if (sunUnsafe.compareAndSwapObject(o, offset, expected, x)) {
                return expected;
            }
        }
    }

    public Object compareAndExchangeReferenceAcquire(Object o, long offset, Object expected, Object x) {
        return compareAndExchangeReference(o, offset, expected, x);
    }

    public Object compareAndExchangeReferenceRelease(Object o, long offset, Object expected, Object x) {
        return compareAndExchangeReference(o, offset, expected, x);
    }

    public Object getAndSetReferenceAcquire(Object o, long offset, Object x) {
        return sunUnsafe.getAndSetObject(o, offset, x);
    }

    public Object getAndSetReferenceRelease(Object o, long offset, Object x) {
        return sunUnsafe.getAndSetObject(o, offset, x);
    }

    // --- 3. Int ---

    public int getInt(Object o, long offset) {
        return sunUnsafe.getInt(o, offset);
    }

    public void putInt(Object o, long offset, int x) {
        sunUnsafe.putInt(o, offset, x);
    }

    public int getIntVolatile(Object o, long offset) {
        return sunUnsafe.getIntVolatile(o, offset);
    }

    public void putIntVolatile(Object o, long offset, int x) {
        sunUnsafe.putIntVolatile(o, offset, x);
    }

    public int getIntAcquire(Object o, long offset) {
        int v = sunUnsafe.getInt(o, offset);
        sunUnsafe.loadFence();
        return v;
    }

    public void putIntRelease(Object o, long offset, int x) {
        sunUnsafe.putOrderedInt(o, offset, x);
    }

    public int getIntOpaque(Object o, long offset) {
        return getIntAcquire(o, offset);
    }

    public void putIntOpaque(Object o, long offset, int x) {
        putIntRelease(o, offset, x);
    }

    public boolean compareAndSetInt(Object o, long offset, int expected, int x) {
        return sunUnsafe.compareAndSwapInt(o, offset, expected, x);
    }

    public int getAndAddInt(Object o, long offset, int delta) {
        return sunUnsafe.getAndAddInt(o, offset, delta);
    }

    public int getAndSetInt(Object o, long offset, int x) {
        return sunUnsafe.getAndSetInt(o, offset, x);
    }

    public boolean weakCompareAndSetInt(Object o, long offset, int expected, int x) {
        return sunUnsafe.compareAndSwapInt(o, offset, expected, x);
    }

    public boolean weakCompareAndSetIntAcquire(Object o, long offset, int expected, int x) {
        return sunUnsafe.compareAndSwapInt(o, offset, expected, x);
    }

    public boolean weakCompareAndSetIntRelease(Object o, long offset, int expected, int x) {
        return sunUnsafe.compareAndSwapInt(o, offset, expected, x);
    }

    public boolean weakCompareAndSetIntPlain(Object o, long offset, int expected, int x) {
        return sunUnsafe.compareAndSwapInt(o, offset, expected, x);
    }

    public int compareAndExchangeInt(Object o, long offset, int expected, int x) {
        while (true) {
            int witness = sunUnsafe.getIntVolatile(o, offset);
            if (witness != expected) {
                return witness;
            }
            if (sunUnsafe.compareAndSwapInt(o, offset, expected, x)) {
                return expected;
            }
        }
    }

    public int compareAndExchangeIntAcquire(Object o, long offset, int expected, int x) {
        return compareAndExchangeInt(o, offset, expected, x);
    }

    public int compareAndExchangeIntRelease(Object o, long offset, int expected, int x) {
        return compareAndExchangeInt(o, offset, expected, x);
    }

    public int getAndSetIntAcquire(Object o, long offset, int x) {
        return sunUnsafe.getAndSetInt(o, offset, x);
    }

    public int getAndSetIntRelease(Object o, long offset, int x) {
        return sunUnsafe.getAndSetInt(o, offset, x);
    }

    // --- 4. Long ---

    public long getLong(Object o, long offset) {
        return sunUnsafe.getLong(o, offset);
    }

    public void putLong(Object o, long offset, long x) {
        sunUnsafe.putLong(o, offset, x);
    }

    public long getLongVolatile(Object o, long offset) {
        return sunUnsafe.getLongVolatile(o, offset);
    }

    public void putLongVolatile(Object o, long offset, long x) {
        sunUnsafe.putLongVolatile(o, offset, x);
    }

    public long getLongAcquire(Object o, long offset) {
        long v = sunUnsafe.getLong(o, offset);
        sunUnsafe.loadFence();
        return v;
    }

    public void putLongRelease(Object o, long offset, long x) {
        sunUnsafe.putOrderedLong(o, offset, x);
    }

    public long getLongOpaque(Object o, long offset) {
        return getLongAcquire(o, offset);
    }

    public void putLongOpaque(Object o, long offset, long x) {
        putLongRelease(o, offset, x);
    }

    public boolean compareAndSetLong(Object o, long offset, long expected, long x) {
        return sunUnsafe.compareAndSwapLong(o, offset, expected, x);
    }

    public long getAndAddLong(Object o, long offset, long delta) {
        return sunUnsafe.getAndAddLong(o, offset, delta);
    }

    public long getAndSetLong(Object o, long offset, long x) {
        return sunUnsafe.getAndSetLong(o, offset, x);
    }

    public boolean weakCompareAndSetLong(Object o, long offset, long expected, long x) {
        return sunUnsafe.compareAndSwapLong(o, offset, expected, x);
    }

    public boolean weakCompareAndSetLongAcquire(Object o, long offset, long expected, long x) {
        return sunUnsafe.compareAndSwapLong(o, offset, expected, x);
    }

    public boolean weakCompareAndSetLongRelease(Object o, long offset, long expected, long x) {
        return sunUnsafe.compareAndSwapLong(o, offset, expected, x);
    }

    public boolean weakCompareAndSetLongPlain(Object o, long offset, long expected, long x) {
        return sunUnsafe.compareAndSwapLong(o, offset, expected, x);
    }

    public long compareAndExchangeLong(Object o, long offset, long expected, long x) {
        while (true) {
            long witness = sunUnsafe.getLongVolatile(o, offset);
            if (witness != expected) {
                return witness;
            }
            if (sunUnsafe.compareAndSwapLong(o, offset, expected, x)) {
                return expected;
            }
        }
    }

    public long compareAndExchangeLongAcquire(Object o, long offset, long expected, long x) {
        return compareAndExchangeLong(o, offset, expected, x);
    }

    public long compareAndExchangeLongRelease(Object o, long offset, long expected, long x) {
        return compareAndExchangeLong(o, offset, expected, x);
    }

    public long getAndSetLongAcquire(Object o, long offset, long x) {
        return sunUnsafe.getAndSetLong(o, offset, x);
    }

    public long getAndSetLongRelease(Object o, long offset, long x) {
        return sunUnsafe.getAndSetLong(o, offset, x);
    }

    // --- 5. Boolean ---

    public boolean getBoolean(Object o, long offset) {
        return sunUnsafe.getBoolean(o, offset);
    }

    public void putBoolean(Object o, long offset, boolean x) {
        sunUnsafe.putBoolean(o, offset, x);
    }

    public boolean getBooleanVolatile(Object o, long offset) {
        return sunUnsafe.getBooleanVolatile(o, offset);
    }

    public void putBooleanVolatile(Object o, long offset, boolean x) {
        sunUnsafe.putBooleanVolatile(o, offset, x);
    }

    public boolean getBooleanAcquire(Object o, long offset) {
        boolean v = sunUnsafe.getBoolean(o, offset);
        sunUnsafe.loadFence();
        return v;
    }

    public void putBooleanRelease(Object o, long offset, boolean x) {
        sunUnsafe.storeFence();
        sunUnsafe.putBoolean(o, offset, x);
    }

    public boolean getBooleanOpaque(Object o, long offset) {
        return getBooleanAcquire(o, offset);
    }

    public void putBooleanOpaque(Object o, long offset, boolean x) {
        putBooleanRelease(o, offset, x);
    }

    public boolean compareAndSetBoolean(Object o, long offset, boolean expected, boolean x) {
        return compareAndSetByte(o, offset, (byte) (expected ? 1 : 0), (byte) (x ? 1 : 0));
    }

    public boolean weakCompareAndSetBoolean(Object o, long offset, boolean expected, boolean x) {
        return compareAndSetBoolean(o, offset, expected, x);
    }

    public boolean weakCompareAndSetBooleanAcquire(Object o, long offset, boolean expected, boolean x) {
        return compareAndSetBoolean(o, offset, expected, x);
    }

    public boolean weakCompareAndSetBooleanRelease(Object o, long offset, boolean expected, boolean x) {
        return compareAndSetBoolean(o, offset, expected, x);
    }

    public boolean weakCompareAndSetBooleanPlain(Object o, long offset, boolean expected, boolean x) {
        return compareAndSetBoolean(o, offset, expected, x);
    }

    public boolean compareAndExchangeBoolean(Object o, long offset, boolean expected, boolean x) {
        return compareAndExchangeByte(o, offset, (byte) (expected ? 1 : 0), (byte) (x ? 1 : 0)) != 0;
    }

    public boolean compareAndExchangeBooleanAcquire(Object o, long offset, boolean expected, boolean x) {
        return compareAndExchangeBoolean(o, offset, expected, x);
    }

    public boolean compareAndExchangeBooleanRelease(Object o, long offset, boolean expected, boolean x) {
        return compareAndExchangeBoolean(o, offset, expected, x);
    }

    // --- 6. Byte ---

    public byte getByte(Object o, long offset) {
        return sunUnsafe.getByte(o, offset);
    }

    public void putByte(Object o, long offset, byte x) {
        sunUnsafe.putByte(o, offset, x);
    }

    public byte getByteVolatile(Object o, long offset) {
        return sunUnsafe.getByteVolatile(o, offset);
    }

    public void putByteVolatile(Object o, long offset, byte x) {
        sunUnsafe.putByteVolatile(o, offset, x);
    }

    public byte getByteAcquire(Object o, long offset) {
        byte v = sunUnsafe.getByte(o, offset);
        sunUnsafe.loadFence();
        return v;
    }

    public void putByteRelease(Object o, long offset, byte x) {
        sunUnsafe.storeFence();
        sunUnsafe.putByte(o, offset, x);
    }

    public byte getByteOpaque(Object o, long offset) {
        return getByteAcquire(o, offset);
    }

    public void putByteOpaque(Object o, long offset, byte x) {
        putByteRelease(o, offset, x);
    }

    public boolean compareAndSetByte(Object o, long offset, byte expected, byte x) {
        final long wordOffset = offset & ~3L;
        int byteIndex = (int) offset & 3;
        if (BIG_ENDIAN) {
            byteIndex ^= 3;
        }
        final int shift = byteIndex << 3;
        final int mask = 0xFF << shift;
        final int expBits = (expected & 0xFF) << shift;
        final int newBits = (x & 0xFF) << shift;
        while (true) {
            final int fullWord = sunUnsafe.getIntVolatile(o, wordOffset);
            final int currentBits = fullWord & mask;
            if (currentBits != expBits) {
                return false;
            }
            final int updatedWord = (fullWord & ~mask) | newBits;
            if (sunUnsafe.compareAndSwapInt(o, wordOffset, fullWord, updatedWord)) {
                return true;
            }
        }
    }

    public boolean weakCompareAndSetByte(Object o, long offset, byte expected, byte x) {
        return compareAndSetByte(o, offset, expected, x);
    }

    public boolean weakCompareAndSetByteAcquire(Object o, long offset, byte expected, byte x) {
        return compareAndSetByte(o, offset, expected, x);
    }

    public boolean weakCompareAndSetByteRelease(Object o, long offset, byte expected, byte x) {
        return compareAndSetByte(o, offset, expected, x);
    }

    public boolean weakCompareAndSetBytePlain(Object o, long offset, byte expected, byte x) {
        return compareAndSetByte(o, offset, expected, x);
    }

    public byte compareAndExchangeByte(Object o, long offset, byte expected, byte x) {
        final long wordOffset = offset & ~3L;
        int byteIndex = (int) offset & 3;
        if (BIG_ENDIAN) {
            byteIndex ^= 3;
        }

        final int shift = byteIndex << 3;
        final int mask = 0xFF << shift;
        final int expBits = (expected & 0xFF) << shift;
        final int newBits = (x & 0xFF) << shift;

        while (true) {
            final int fullWord = sunUnsafe.getIntVolatile(o, wordOffset);
            final int currentBits = fullWord & mask;
            if (currentBits != expBits) {
                return (byte) (currentBits >>> shift);
            }
            final int updatedWord = (fullWord & ~mask) | newBits;
            if (sunUnsafe.compareAndSwapInt(o, wordOffset, fullWord, updatedWord)) {
                return expected;
            }
        }
    }

    public byte compareAndExchangeByteAcquire(Object o, long offset, byte expected, byte x) {
        return compareAndExchangeByte(o, offset, expected, x);
    }

    public byte compareAndExchangeByteRelease(Object o, long offset, byte expected, byte x) {
        return compareAndExchangeByte(o, offset, expected, x);
    }

    // --- 7. Short ---

    public short getShort(Object o, long offset) {
        return sunUnsafe.getShort(o, offset);
    }

    public void putShort(Object o, long offset, short x) {
        sunUnsafe.putShort(o, offset, x);
    }

    public short getShortVolatile(Object o, long offset) {
        return sunUnsafe.getShortVolatile(o, offset);
    }

    public void putShortVolatile(Object o, long offset, short x) {
        sunUnsafe.putShortVolatile(o, offset, x);
    }

    public short getShortAcquire(Object o, long offset) {
        short v = sunUnsafe.getShort(o, offset);
        sunUnsafe.loadFence();
        return v;
    }

    public void putShortRelease(Object o, long offset, short x) {
        sunUnsafe.storeFence();
        sunUnsafe.putShort(o, offset, x);
    }

    public short getShortOpaque(Object o, long offset) {
        return getShortAcquire(o, offset);
    }

    public void putShortOpaque(Object o, long offset, short x) {
        putShortRelease(o, offset, x);
    }

    public boolean compareAndSetShort(Object o, long offset, short expected, short x) {
        int byteIndex = (int) offset & 3;
        if (byteIndex == 3) {
            throw new IllegalArgumentException("short CAS crosses word boundary");
        }
        final long wordOffset = offset & ~3L;
        final int shift;
        if (BIG_ENDIAN) {
            shift = (2 - byteIndex) << 3;
        } else {
            shift = byteIndex << 3;
        }
        final int mask = 0xFFFF << shift;
        final int expBits = (expected & 0xFFFF) << shift;
        final int newBits = (x & 0xFFFF) << shift;
        while (true) {
            final int fullWord = sunUnsafe.getIntVolatile(o, wordOffset);
            final int currentBits = fullWord & mask;
            if (currentBits != expBits) {
                return false;
            }
            final int updatedWord = (fullWord & ~mask) | newBits;
            if (sunUnsafe.compareAndSwapInt(o, wordOffset, fullWord, updatedWord)) {
                return true;
            }
        }
    }

    public boolean weakCompareAndSetShort(Object o, long offset, short expected, short x) {
        return compareAndSetShort(o, offset, expected, x);
    }

    public boolean weakCompareAndSetShortAcquire(Object o, long offset, short expected, short x) {
        return compareAndSetShort(o, offset, expected, x);
    }

    public boolean weakCompareAndSetShortRelease(Object o, long offset, short expected, short x) {
        return compareAndSetShort(o, offset, expected, x);
    }

    public boolean weakCompareAndSetShortPlain(Object o, long offset, short expected, short x) {
        return compareAndSetShort(o, offset, expected, x);
    }

    public short compareAndExchangeShort(Object o, long offset, short expected, short x) {
        int byteIndex = (int) offset & 3;
        if (byteIndex == 3) {
            throw new IllegalArgumentException("short CAS crosses word boundary");
        }
        final long wordOffset = offset & ~3L;
        final int shift;
        if (BIG_ENDIAN) {
            shift = (2 - byteIndex) << 3;
        } else {
            shift = byteIndex << 3;
        }

        final int mask = 0xFFFF << shift;
        final int expBits = (expected & 0xFFFF) << shift;
        final int newBits = (x & 0xFFFF) << shift;

        while (true) {
            final int fullWord = sunUnsafe.getIntVolatile(o, wordOffset);
            final int currentBits = fullWord & mask;
            if (currentBits != expBits) {
                return (short) (currentBits >>> shift);
            }

            final int updatedWord = (fullWord & ~mask) | newBits;

            if (sunUnsafe.compareAndSwapInt(o, wordOffset, fullWord, updatedWord)) {
                return expected;
            }
        }
    }

    public short compareAndExchangeShortAcquire(Object o, long offset, short expected, short x) {
        return compareAndExchangeShort(o, offset, expected, x);
    }

    public short compareAndExchangeShortRelease(Object o, long offset, short expected, short x) {
        return compareAndExchangeShort(o, offset, expected, x);
    }

    // --- 8. Char ---

    public char getChar(Object o, long offset) {
        return sunUnsafe.getChar(o, offset);
    }

    public void putChar(Object o, long offset, char x) {
        sunUnsafe.putChar(o, offset, x);
    }

    public char getCharVolatile(Object o, long offset) {
        return sunUnsafe.getCharVolatile(o, offset);
    }

    public void putCharVolatile(Object o, long offset, char x) {
        sunUnsafe.putCharVolatile(o, offset, x);
    }

    public char getCharAcquire(Object o, long offset) {
        char v = sunUnsafe.getChar(o, offset);
        sunUnsafe.loadFence();
        return v;
    }

    public void putCharRelease(Object o, long offset, char x) {
        sunUnsafe.storeFence();
        sunUnsafe.putChar(o, offset, x);
    }

    public char getCharOpaque(Object o, long offset) {
        return getCharAcquire(o, offset);
    }

    public void putCharOpaque(Object o, long offset, char x) {
        putCharRelease(o, offset, x);
    }

    public boolean compareAndSetChar(Object o, long offset, char expected, char x) {
        return compareAndSetShort(o, offset, (short) expected, (short) x);
    }

    public boolean weakCompareAndSetChar(Object o, long offset, char expected, char x) {
        return compareAndSetChar(o, offset, expected, x);
    }

    public boolean weakCompareAndSetCharAcquire(Object o, long offset, char expected, char x) {
        return compareAndSetChar(o, offset, expected, x);
    }

    public boolean weakCompareAndSetCharRelease(Object o, long offset, char expected, char x) {
        return compareAndSetChar(o, offset, expected, x);
    }

    public boolean weakCompareAndSetCharPlain(Object o, long offset, char expected, char x) {
        return compareAndSetChar(o, offset, expected, x);
    }

    public char compareAndExchangeChar(Object o, long offset, char expected, char x) {
        return (char) compareAndExchangeShort(o, offset, (short) expected, (short) x);
    }

    public char compareAndExchangeCharAcquire(Object o, long offset, char expected, char x) {
        return compareAndExchangeChar(o, offset, expected, x);
    }

    public char compareAndExchangeCharRelease(Object o, long offset, char expected, char x) {
        return compareAndExchangeChar(o, offset, expected, x);
    }

    // --- 9. Float ---

    public float getFloat(Object o, long offset) {
        return sunUnsafe.getFloat(o, offset);
    }

    public void putFloat(Object o, long offset, float x) {
        sunUnsafe.putFloat(o, offset, x);
    }

    public float getFloatVolatile(Object o, long offset) {
        return sunUnsafe.getFloatVolatile(o, offset);
    }

    public void putFloatVolatile(Object o, long offset, float x) {
        sunUnsafe.putFloatVolatile(o, offset, x);
    }

    public float getFloatAcquire(Object o, long offset) {
        float v = sunUnsafe.getFloat(o, offset);
        sunUnsafe.loadFence();
        return v;
    }

    public void putFloatRelease(Object o, long offset, float x) {
        sunUnsafe.putOrderedInt(o, offset, Float.floatToRawIntBits(x));
    }

    public float getFloatOpaque(Object o, long offset) {
        return getFloatAcquire(o, offset);
    }

    public void putFloatOpaque(Object o, long offset, float x) {
        putFloatRelease(o, offset, x);
    }

    public boolean compareAndSetFloat(Object o, long offset, float expected, float x) {
        return sunUnsafe.compareAndSwapInt(o, offset, Float.floatToRawIntBits(expected), Float.floatToRawIntBits(x));
    }

    public boolean weakCompareAndSetFloat(Object o, long offset, float expected, float x) {
        return compareAndSetFloat(o, offset, expected, x);
    }

    public boolean weakCompareAndSetFloatAcquire(Object o, long offset, float expected, float x) {
        return compareAndSetFloat(o, offset, expected, x);
    }

    public boolean weakCompareAndSetFloatRelease(Object o, long offset, float expected, float x) {
        return compareAndSetFloat(o, offset, expected, x);
    }

    public boolean weakCompareAndSetFloatPlain(Object o, long offset, float expected, float x) {
        return compareAndSetFloat(o, offset, expected, x);
    }

    public float compareAndExchangeFloat(Object o, long offset, float expected, float x) {
        return Float.intBitsToFloat(compareAndExchangeInt(o, offset, Float.floatToRawIntBits(expected), Float.floatToRawIntBits(x)));
    }

    public float compareAndExchangeFloatAcquire(Object o, long offset, float expected, float x) {
        return compareAndExchangeFloat(o, offset, expected, x);
    }

    public float compareAndExchangeFloatRelease(Object o, long offset, float expected, float x) {
        return compareAndExchangeFloat(o, offset, expected, x);
    }

    // --- 10. Double ---

    public double getDouble(Object o, long offset) {
        return sunUnsafe.getDouble(o, offset);
    }

    public void putDouble(Object o, long offset, double x) {
        sunUnsafe.putDouble(o, offset, x);
    }

    public double getDoubleVolatile(Object o, long offset) {
        return sunUnsafe.getDoubleVolatile(o, offset);
    }

    public void putDoubleVolatile(Object o, long offset, double x) {
        sunUnsafe.putDoubleVolatile(o, offset, x);
    }

    public double getDoubleAcquire(Object o, long offset) {
        double v = sunUnsafe.getDouble(o, offset);
        sunUnsafe.loadFence();
        return v;
    }

    public void putDoubleRelease(Object o, long offset, double x) {
        sunUnsafe.putOrderedLong(o, offset, Double.doubleToRawLongBits(x));
    }

    public double getDoubleOpaque(Object o, long offset) {
        return getDoubleAcquire(o, offset);
    }

    public void putDoubleOpaque(Object o, long offset, double x) {
        putDoubleRelease(o, offset, x);
    }

    public boolean compareAndSetDouble(Object o, long offset, double expected, double x) {
        return sunUnsafe.compareAndSwapLong(o, offset, Double.doubleToRawLongBits(expected), Double.doubleToRawLongBits(x));
    }

    public boolean weakCompareAndSetDouble(Object o, long offset, double expected, double x) {
        return compareAndSetDouble(o, offset, expected, x);
    }

    public boolean weakCompareAndSetDoubleAcquire(Object o, long offset, double expected, double x) {
        return compareAndSetDouble(o, offset, expected, x);
    }

    public boolean weakCompareAndSetDoubleRelease(Object o, long offset, double expected, double x) {
        return compareAndSetDouble(o, offset, expected, x);
    }

    public boolean weakCompareAndSetDoublePlain(Object o, long offset, double expected, double x) {
        return compareAndSetDouble(o, offset, expected, x);
    }

    public double compareAndExchangeDouble(Object o, long offset, double expected, double x) {
        return Double.longBitsToDouble(compareAndExchangeLong(o, offset, Double.doubleToRawLongBits(expected), Double.doubleToRawLongBits(x)));
    }

    public double compareAndExchangeDoubleAcquire(Object o, long offset, double expected, double x) {
        return compareAndExchangeDouble(o, offset, expected, x);
    }

    public double compareAndExchangeDoubleRelease(Object o, long offset, double expected, double x) {
        return compareAndExchangeDouble(o, offset, expected, x);
    }

    // --- 11. Memory ---

    public long allocateMemory(long bytes) {
        return sunUnsafe.allocateMemory(bytes);
    }

    public void freeMemory(long address) {
        sunUnsafe.freeMemory(address);
    }

    public long reallocateMemory(long address, long bytes) {
        return sunUnsafe.reallocateMemory(address, bytes);
    }

    public void setMemory(long address, long bytes, byte value) {
        sunUnsafe.setMemory(address, bytes, value);
    }

    public void copyMemory(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes) {
        sunUnsafe.copyMemory(srcBase, srcOffset, destBase, destOffset, bytes);
    }

    public byte getByte(long address) {
        return sunUnsafe.getByte(address);
    }

    public void putByte(long address, byte x) {
        sunUnsafe.putByte(address, x);
    }

    public short getShort(long address) {
        return sunUnsafe.getShort(address);
    }

    public void putShort(long address, short x) {
        sunUnsafe.putShort(address, x);
    }

    public char getChar(long address) {
        return sunUnsafe.getChar(address);
    }

    public void putChar(long address, char x) {
        sunUnsafe.putChar(address, x);
    }

    public int getInt(long address) {
       return sunUnsafe.getInt(address);
    }

    public void putInt(long address, int x) {
        sunUnsafe.putInt(address, x);
    }

    public long getLong(long address) {
        return sunUnsafe.getLong(address);
    }

    public void putLong(long address, long value) {
        sunUnsafe.putLong(address, value);
    }

    public float getFloat(long address) {
        return sunUnsafe.getFloat(address);
    }

    public void putFloat(long address, float x) {
        sunUnsafe.putFloat(address, x);
    }

    public double getDouble(long address) {
        return sunUnsafe.getDouble(address);
    }

    public void putDouble(long address, double x) {
        sunUnsafe.putDouble(address, x);
    }

    public long getAddress(long address) {
        return sunUnsafe.getAddress(address);
    }

    public void putAddress(long address, long x) {
        sunUnsafe.putAddress(address, x);
    }

    public void loadFence() {
        sunUnsafe.loadFence();
    }

    public void storeFence() {
        sunUnsafe.storeFence();
    }

    public void fullFence() {
        sunUnsafe.fullFence();
    }

    public void park(boolean isAbsolute, long time) {
        sunUnsafe.park(isAbsolute, time);
    }

    public void unpark(Object thread) {
        sunUnsafe.unpark(thread);
    }

    public void throwException(Throwable ee) {
        sunUnsafe.throwException(ee);
    }
}
