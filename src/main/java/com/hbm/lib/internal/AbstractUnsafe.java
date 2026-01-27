package com.hbm.lib.internal;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.nio.ByteOrder;

public abstract sealed class AbstractUnsafe permits InternalUnsafeWrapper, SunUnsafeWrapper {
    static final boolean BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
    static final Unsafe sunUnsafe;
    static final MethodHandles.Lookup IMPL_LOOKUP;
    static final int MAJOR_VERSION;
    static final boolean JPMS;

    static {
        MAJOR_VERSION = Runtime.version().feature(); // JVMDG downgrade
        JPMS = MAJOR_VERSION >= 9;
        sunUnsafe = getSunUnsafe();
        IMPL_LOOKUP = getImplLookupUnsafe(sunUnsafe);
    }

    public static AbstractUnsafe getUnsafe() {
        return JPMS ? new InternalUnsafeWrapper() : new SunUnsafeWrapper();
    }

    @SuppressWarnings("removal")
    private static MethodHandles.Lookup getImplLookupUnsafe(Unsafe unsafe) {
        try {
            Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Object base = unsafe.staticFieldBase(lookupField);
            long offset = unsafe.staticFieldOffset(lookupField);
            return (MethodHandles.Lookup) unsafe.getObject(base, offset);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static Unsafe getSunUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    // ================================================================================================================
    // 1. Offsets, Info & Instantiation
    // ================================================================================================================

    public abstract long objectFieldOffset(Field f);

    public abstract Object staticFieldBase(Field f);

    public abstract long staticFieldOffset(Field f);

    public abstract Object allocateInstance(Class<?> cls) throws InstantiationException;

    public abstract Object allocateUninitializedArray(Class<?> componentType, int length);

    public abstract long arrayBaseOffset(Class<?> cls);

    public abstract int arrayIndexScale(Class<?> cls);

    public abstract int addressSize();

    public abstract int pageSize();


    // ================================================================================================================
    // 2. References (Object)
    // ================================================================================================================

    public abstract Object getReference(Object o, long offset);

    public abstract void putReference(Object o, long offset, Object x);

    public abstract Object getReferenceVolatile(Object o, long offset);

    public abstract void putReferenceVolatile(Object o, long offset, Object x);

    public abstract Object getReferenceAcquire(Object o, long offset);

    public abstract void putReferenceRelease(Object o, long offset, Object x);

    public abstract Object getReferenceOpaque(Object o, long offset);

    public abstract void putReferenceOpaque(Object o, long offset, Object x);

    // CAS & Atomic - Reference
    public abstract boolean compareAndSetReference(Object o, long offset, Object expected, Object x);

    public abstract Object getAndSetReference(Object o, long offset, Object x);

    public abstract boolean weakCompareAndSetReference(Object o, long offset, Object expected, Object x);

    public abstract boolean weakCompareAndSetReferenceAcquire(Object o, long offset, Object expected, Object x);

    public abstract boolean weakCompareAndSetReferenceRelease(Object o, long offset, Object expected, Object x);

    public abstract boolean weakCompareAndSetReferencePlain(Object o, long offset, Object expected, Object x);

    public abstract Object compareAndExchangeReference(Object o, long offset, Object expected, Object x);

    public abstract Object compareAndExchangeReferenceAcquire(Object o, long offset, Object expected, Object x);

    public abstract Object compareAndExchangeReferenceRelease(Object o, long offset, Object expected, Object x);

    public abstract Object getAndSetReferenceAcquire(Object o, long offset, Object x);

    public abstract Object getAndSetReferenceRelease(Object o, long offset, Object x);


    // ================================================================================================================
    // 3. Primitives: Int
    // ================================================================================================================

    public abstract int getInt(Object o, long offset);

    public abstract void putInt(Object o, long offset, int x);

    public abstract int getIntVolatile(Object o, long offset);

    public abstract void putIntVolatile(Object o, long offset, int x);

    public abstract int getIntAcquire(Object o, long offset);

    public abstract void putIntRelease(Object o, long offset, int x);

    public abstract int getIntOpaque(Object o, long offset);

    public abstract void putIntOpaque(Object o, long offset, int x);

    public abstract boolean compareAndSetInt(Object o, long offset, int expected, int x);

    public abstract int getAndAddInt(Object o, long offset, int delta);

    public abstract int getAndSetInt(Object o, long offset, int x);

    public abstract boolean weakCompareAndSetInt(Object o, long offset, int expected, int x);

    public abstract boolean weakCompareAndSetIntAcquire(Object o, long offset, int expected, int x);

    public abstract boolean weakCompareAndSetIntRelease(Object o, long offset, int expected, int x);

    public abstract boolean weakCompareAndSetIntPlain(Object o, long offset, int expected, int x);

    public abstract int compareAndExchangeInt(Object o, long offset, int expected, int x);

    public abstract int compareAndExchangeIntAcquire(Object o, long offset, int expected, int x);

    public abstract int compareAndExchangeIntRelease(Object o, long offset, int expected, int x);

    public abstract int getAndSetIntAcquire(Object o, long offset, int x);

    public abstract int getAndSetIntRelease(Object o, long offset, int x);


    // ================================================================================================================
    // 4. Primitives: Long
    // ================================================================================================================

    public abstract long getLong(Object o, long offset);

    public abstract void putLong(Object o, long offset, long x);

    public abstract long getLongVolatile(Object o, long offset);

    public abstract void putLongVolatile(Object o, long offset, long x);

    public abstract long getLongAcquire(Object o, long offset);

    public abstract void putLongRelease(Object o, long offset, long x);

    public abstract long getLongOpaque(Object o, long offset);

    public abstract void putLongOpaque(Object o, long offset, long x);

    public abstract boolean compareAndSetLong(Object o, long offset, long expected, long x);

    public abstract long getAndAddLong(Object o, long offset, long delta);

    public abstract long getAndSetLong(Object o, long offset, long x);

    public abstract boolean weakCompareAndSetLong(Object o, long offset, long expected, long x);

    public abstract boolean weakCompareAndSetLongAcquire(Object o, long offset, long expected, long x);

    public abstract boolean weakCompareAndSetLongRelease(Object o, long offset, long expected, long x);

    public abstract boolean weakCompareAndSetLongPlain(Object o, long offset, long expected, long x);

    public abstract long compareAndExchangeLong(Object o, long offset, long expected, long x);

    public abstract long compareAndExchangeLongAcquire(Object o, long offset, long expected, long x);

    public abstract long compareAndExchangeLongRelease(Object o, long offset, long expected, long x);

    public abstract long getAndSetLongAcquire(Object o, long offset, long x);

    public abstract long getAndSetLongRelease(Object o, long offset, long x);


    // ================================================================================================================
    // 5. Primitives: Boolean
    // ================================================================================================================

    public abstract boolean getBoolean(Object o, long offset);

    public abstract void putBoolean(Object o, long offset, boolean x);

    public abstract boolean getBooleanVolatile(Object o, long offset);

    public abstract void putBooleanVolatile(Object o, long offset, boolean x);

    public abstract boolean getBooleanAcquire(Object o, long offset);

    public abstract void putBooleanRelease(Object o, long offset, boolean x);

    public abstract boolean getBooleanOpaque(Object o, long offset);

    public abstract void putBooleanOpaque(Object o, long offset, boolean x);

    public abstract boolean compareAndSetBoolean(Object o, long offset, boolean expected, boolean x);

    public abstract boolean weakCompareAndSetBoolean(Object o, long offset, boolean expected, boolean x);

    public abstract boolean weakCompareAndSetBooleanAcquire(Object o, long offset, boolean expected, boolean x);

    public abstract boolean weakCompareAndSetBooleanRelease(Object o, long offset, boolean expected, boolean x);

    public abstract boolean weakCompareAndSetBooleanPlain(Object o, long offset, boolean expected, boolean x);

    public abstract boolean compareAndExchangeBoolean(Object o, long offset, boolean expected, boolean x);

    public abstract boolean compareAndExchangeBooleanAcquire(Object o, long offset, boolean expected, boolean x);

    public abstract boolean compareAndExchangeBooleanRelease(Object o, long offset, boolean expected, boolean x);


    // ================================================================================================================
    // 6. Primitives: Byte
    // ================================================================================================================

    public abstract byte getByte(Object o, long offset);

    public abstract void putByte(Object o, long offset, byte x);

    public abstract byte getByteVolatile(Object o, long offset);

    public abstract void putByteVolatile(Object o, long offset, byte x);

    public abstract byte getByteAcquire(Object o, long offset);

    public abstract void putByteRelease(Object o, long offset, byte x);

    public abstract byte getByteOpaque(Object o, long offset);

    public abstract void putByteOpaque(Object o, long offset, byte x);

    public abstract boolean compareAndSetByte(Object o, long offset, byte expected, byte x);

    public abstract boolean weakCompareAndSetByte(Object o, long offset, byte expected, byte x);

    public abstract boolean weakCompareAndSetByteAcquire(Object o, long offset, byte expected, byte x);

    public abstract boolean weakCompareAndSetByteRelease(Object o, long offset, byte expected, byte x);

    public abstract boolean weakCompareAndSetBytePlain(Object o, long offset, byte expected, byte x);

    public abstract byte compareAndExchangeByte(Object o, long offset, byte expected, byte x);

    public abstract byte compareAndExchangeByteAcquire(Object o, long offset, byte expected, byte x);

    public abstract byte compareAndExchangeByteRelease(Object o, long offset, byte expected, byte x);


    // ================================================================================================================
    // 7. Primitives: Short
    // ================================================================================================================

    public abstract short getShort(Object o, long offset);

    public abstract void putShort(Object o, long offset, short x);

    public abstract short getShortVolatile(Object o, long offset);

    public abstract void putShortVolatile(Object o, long offset, short x);

    public abstract short getShortAcquire(Object o, long offset);

    public abstract void putShortRelease(Object o, long offset, short x);

    public abstract short getShortOpaque(Object o, long offset);

    public abstract void putShortOpaque(Object o, long offset, short x);

    public abstract boolean compareAndSetShort(Object o, long offset, short expected, short x);

    public abstract boolean weakCompareAndSetShort(Object o, long offset, short expected, short x);

    public abstract boolean weakCompareAndSetShortAcquire(Object o, long offset, short expected, short x);

    public abstract boolean weakCompareAndSetShortRelease(Object o, long offset, short expected, short x);

    public abstract boolean weakCompareAndSetShortPlain(Object o, long offset, short expected, short x);

    public abstract short compareAndExchangeShort(Object o, long offset, short expected, short x);

    public abstract short compareAndExchangeShortAcquire(Object o, long offset, short expected, short x);

    public abstract short compareAndExchangeShortRelease(Object o, long offset, short expected, short x);


    // ================================================================================================================
    // 8. Primitives: Char
    // ================================================================================================================

    public abstract char getChar(Object o, long offset);

    public abstract void putChar(Object o, long offset, char x);

    public abstract char getCharVolatile(Object o, long offset);

    public abstract void putCharVolatile(Object o, long offset, char x);

    public abstract char getCharAcquire(Object o, long offset);

    public abstract void putCharRelease(Object o, long offset, char x);

    public abstract char getCharOpaque(Object o, long offset);

    public abstract void putCharOpaque(Object o, long offset, char x);

    public abstract boolean compareAndSetChar(Object o, long offset, char expected, char x);

    public abstract boolean weakCompareAndSetChar(Object o, long offset, char expected, char x);

    public abstract boolean weakCompareAndSetCharAcquire(Object o, long offset, char expected, char x);

    public abstract boolean weakCompareAndSetCharRelease(Object o, long offset, char expected, char x);

    public abstract boolean weakCompareAndSetCharPlain(Object o, long offset, char expected, char x);

    public abstract char compareAndExchangeChar(Object o, long offset, char expected, char x);

    public abstract char compareAndExchangeCharAcquire(Object o, long offset, char expected, char x);

    public abstract char compareAndExchangeCharRelease(Object o, long offset, char expected, char x);


    // ================================================================================================================
    // 9. Primitives: Float
    // ================================================================================================================

    public abstract float getFloat(Object o, long offset);

    public abstract void putFloat(Object o, long offset, float x);

    public abstract float getFloatVolatile(Object o, long offset);

    public abstract void putFloatVolatile(Object o, long offset, float x);

    public abstract float getFloatAcquire(Object o, long offset);

    public abstract void putFloatRelease(Object o, long offset, float x);

    public abstract float getFloatOpaque(Object o, long offset);

    public abstract void putFloatOpaque(Object o, long offset, float x);

    public abstract boolean compareAndSetFloat(Object o, long offset, float expected, float x);

    public abstract boolean weakCompareAndSetFloat(Object o, long offset, float expected, float x);

    public abstract boolean weakCompareAndSetFloatAcquire(Object o, long offset, float expected, float x);

    public abstract boolean weakCompareAndSetFloatRelease(Object o, long offset, float expected, float x);

    public abstract boolean weakCompareAndSetFloatPlain(Object o, long offset, float expected, float x);

    public abstract float compareAndExchangeFloat(Object o, long offset, float expected, float x);

    public abstract float compareAndExchangeFloatAcquire(Object o, long offset, float expected, float x);

    public abstract float compareAndExchangeFloatRelease(Object o, long offset, float expected, float x);


    // ================================================================================================================
    // 10. Primitives: Double
    // ================================================================================================================

    public abstract double getDouble(Object o, long offset);

    public abstract void putDouble(Object o, long offset, double x);

    public abstract double getDoubleVolatile(Object o, long offset);

    public abstract void putDoubleVolatile(Object o, long offset, double x);

    public abstract double getDoubleAcquire(Object o, long offset);

    public abstract void putDoubleRelease(Object o, long offset, double x);

    public abstract double getDoubleOpaque(Object o, long offset);

    public abstract void putDoubleOpaque(Object o, long offset, double x);

    public abstract boolean compareAndSetDouble(Object o, long offset, double expected, double x);

    public abstract boolean weakCompareAndSetDouble(Object o, long offset, double expected, double x);

    public abstract boolean weakCompareAndSetDoubleAcquire(Object o, long offset, double expected, double x);

    public abstract boolean weakCompareAndSetDoubleRelease(Object o, long offset, double expected, double x);

    public abstract boolean weakCompareAndSetDoublePlain(Object o, long offset, double expected, double x);

    public abstract double compareAndExchangeDouble(Object o, long offset, double expected, double x);

    public abstract double compareAndExchangeDoubleAcquire(Object o, long offset, double expected, double x);

    public abstract double compareAndExchangeDoubleRelease(Object o, long offset, double expected, double x);


    // ================================================================================================================
    // 11. Raw Memory Access
    // ================================================================================================================

    public abstract long allocateMemory(long bytes);

    public abstract void freeMemory(long address);

    public abstract long reallocateMemory(long address, long bytes);

    public abstract void setMemory(long address, long bytes, byte value);

    public abstract void copyMemory(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes);

    // Byte
    public abstract byte getByte(long address);

    public abstract void putByte(long address, byte x);

    // Short
    public abstract short getShort(long address);

    public abstract void putShort(long address, short x);

    // Char
    public abstract char getChar(long address);

    public abstract void putChar(long address, char x);

    // Int
    public abstract int getInt(long address);

    public abstract void putInt(long address, int x);

    // Long
    public abstract long getLong(long address);

    public abstract void putLong(long address, long x);

    // Float
    public abstract float getFloat(long address);

    public abstract void putFloat(long address, float x);

    // Double
    public abstract double getDouble(long address);

    public abstract void putDouble(long address, double x);

    // Address
    public abstract long getAddress(long address);

    public abstract void putAddress(long address, long x);


    // ================================================================================================================
    // 12. Fences & Miscellaneous
    // ================================================================================================================

    public abstract void loadFence();

    public abstract void storeFence();

    public abstract void fullFence();

    public abstract void park(boolean isAbsolute, long time);

    public abstract void unpark(Object thread);

    public abstract void throwException(Throwable ee);

    /**
     * @deprecated use {@link #getReference(Object, long)} whenever possible
     */
    @Deprecated
    public final Object getObject(Object o, long offset) {
        return getReference(o, offset);
    }

    /**
     * @deprecated use {@link #putReference(Object, long, Object)} whenever possible
     */
    @Deprecated
    public final void putObject(Object o, long offset, Object x) {
        putReference(o, offset, x);
    }

    /**
     * @deprecated use {@link #getReferenceVolatile(Object, long)} whenever possible
     */
    @Deprecated
    public final Object getObjectVolatile(Object o, long offset) {
        return getReferenceVolatile(o, offset);
    }

    /**
     * @deprecated use {@link #putReferenceVolatile(Object, long, Object)} whenever possible
     */
    @Deprecated
    public final void putObjectVolatile(Object o, long offset, Object x) {
        putReferenceVolatile(o, offset, x);
    }

    /**
     * @deprecated use {@link #compareAndSetReference(Object, long, Object, Object)} whenever possible
     */
    @Deprecated
    public final boolean compareAndSwapObject(Object o, long offset, Object expected, Object x) {
        return compareAndSetReference(o, offset, expected, x);
    }

    /**
     * @deprecated use {@link #compareAndSetInt(Object, long, int, int)} whenever possible
     */
    @Deprecated
    public final boolean compareAndSwapInt(Object o, long offset, int expected, int x) {
        return compareAndSetInt(o, offset, expected, x);
    }

    /**
     * @deprecated use {@link #compareAndSetLong(Object, long, long, long)} whenever possible
     */
    @Deprecated
    public final boolean compareAndSwapLong(Object o, long offset, long expected, long x) {
        return compareAndSetLong(o, offset, expected, x);
    }

    /**
     * @deprecated use {@link #getAndSetReference(Object, long, Object)} whenever possible
     */
    @Deprecated
    public final Object getAndSetObject(Object o, long offset, Object x) {
        return getAndSetReference(o, offset, x);
    }

    /**
     * @deprecated use {@link #putIntRelease(Object, long, int)} whenever possible
     */
    @Deprecated
    public final void putOrderedInt(Object o, long offset, int x) {
        putIntRelease(o, offset, x);
    }

    /**
     * @deprecated use {@link #putLongRelease(Object, long, long)} whenever possible
     */
    @Deprecated
    public final void putOrderedLong(Object o, long offset, long x) {
        putLongRelease(o, offset, x);
    }

    /**
     * @deprecated use {@link #putReferenceRelease(Object, long, Object)} whenever possible
     */
    @Deprecated
    public final void putOrderedObject(Object o, long offset, Object x) {
        putReferenceRelease(o, offset, x);
    }
}
