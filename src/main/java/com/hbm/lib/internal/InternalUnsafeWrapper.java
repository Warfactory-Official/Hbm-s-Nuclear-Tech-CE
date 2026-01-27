package com.hbm.lib.internal;

import com.hbm.interfaces.SuppressCheckedExceptions;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

/**
 * Switch between different versions of Unsafe automatically depending on the Java version.
 * Mirrors the API of jdk.internal.misc.Unsafe while supporting JDK 9 and onwards.
 *
 * @author mlbv
 */
@SuppressCheckedExceptions
final class InternalUnsafeWrapper extends AbstractUnsafe {

    private static final MethodHandle OBJECT_FIELD_OFFSET, STATIC_FIELD_BASE, STATIC_FIELD_OFFSET, ALLOCATE_INSTANCE, ARRAY_BASE_OFFSET, ARRAY_INDEX_SCALE, ADDRESS_SIZE, PAGE_SIZE;

    private static final MethodHandle GET_REFERENCE, PUT_REFERENCE, GET_REFERENCE_VOLATILE, PUT_REFERENCE_VOLATILE, GET_REFERENCE_ACQUIRE, PUT_REFERENCE_RELEASE, GET_REFERENCE_OPAQUE, PUT_REFERENCE_OPAQUE, COMPARE_AND_SET_REFERENCE, GET_AND_SET_REFERENCE;
    private static final MethodHandle WEAK_COMPARE_AND_SET_REFERENCE, WEAK_COMPARE_AND_SET_REFERENCE_ACQUIRE, WEAK_COMPARE_AND_SET_REFERENCE_RELEASE, WEAK_COMPARE_AND_SET_REFERENCE_PLAIN;
    private static final MethodHandle COMPARE_AND_EXCHANGE_REFERENCE, COMPARE_AND_EXCHANGE_REFERENCE_ACQUIRE, COMPARE_AND_EXCHANGE_REFERENCE_RELEASE, GET_AND_SET_REFERENCE_ACQUIRE, GET_AND_SET_REFERENCE_RELEASE;

    private static final MethodHandle GET_INT, PUT_INT, GET_INT_VOLATILE, PUT_INT_VOLATILE, GET_INT_ACQUIRE, PUT_INT_RELEASE, GET_INT_OPAQUE, PUT_INT_OPAQUE, GET_AND_ADD_INT, GET_AND_SET_INT, COMPARE_AND_SET_INT;
    private static final MethodHandle WEAK_COMPARE_AND_SET_INT, WEAK_COMPARE_AND_SET_INT_ACQUIRE, WEAK_COMPARE_AND_SET_INT_RELEASE, WEAK_COMPARE_AND_SET_INT_PLAIN;
    private static final MethodHandle COMPARE_AND_EXCHANGE_INT, COMPARE_AND_EXCHANGE_INT_ACQUIRE, COMPARE_AND_EXCHANGE_INT_RELEASE, GET_AND_SET_INT_ACQUIRE, GET_AND_SET_INT_RELEASE;

    private static final MethodHandle GET_LONG, PUT_LONG, GET_LONG_VOLATILE, PUT_LONG_VOLATILE, GET_LONG_ACQUIRE, PUT_LONG_RELEASE, GET_LONG_OPAQUE, PUT_LONG_OPAQUE, GET_AND_ADD_LONG, GET_AND_SET_LONG, COMPARE_AND_SET_LONG;
    private static final MethodHandle WEAK_COMPARE_AND_SET_LONG, WEAK_COMPARE_AND_SET_LONG_ACQUIRE, WEAK_COMPARE_AND_SET_LONG_RELEASE, WEAK_COMPARE_AND_SET_LONG_PLAIN;
    private static final MethodHandle COMPARE_AND_EXCHANGE_LONG, COMPARE_AND_EXCHANGE_LONG_ACQUIRE, COMPARE_AND_EXCHANGE_LONG_RELEASE, GET_AND_SET_LONG_ACQUIRE, GET_AND_SET_LONG_RELEASE;

    private static final MethodHandle GET_BOOLEAN, PUT_BOOLEAN, GET_BOOLEAN_VOLATILE, PUT_BOOLEAN_VOLATILE, GET_BOOLEAN_ACQUIRE, PUT_BOOLEAN_RELEASE, GET_BOOLEAN_OPAQUE, PUT_BOOLEAN_OPAQUE, COMPARE_AND_SET_BOOLEAN;
    private static final MethodHandle WEAK_COMPARE_AND_SET_BOOLEAN, WEAK_COMPARE_AND_SET_BOOLEAN_ACQUIRE, WEAK_COMPARE_AND_SET_BOOLEAN_RELEASE, WEAK_COMPARE_AND_SET_BOOLEAN_PLAIN;
    private static final MethodHandle COMPARE_AND_EXCHANGE_BOOLEAN, COMPARE_AND_EXCHANGE_BOOLEAN_ACQUIRE, COMPARE_AND_EXCHANGE_BOOLEAN_RELEASE;

    private static final MethodHandle GET_BYTE, PUT_BYTE, GET_BYTE_VOLATILE, PUT_BYTE_VOLATILE, GET_BYTE_ACQUIRE, PUT_BYTE_RELEASE, GET_BYTE_OPAQUE, PUT_BYTE_OPAQUE, COMPARE_AND_SET_BYTE;
    private static final MethodHandle WEAK_COMPARE_AND_SET_BYTE, WEAK_COMPARE_AND_SET_BYTE_ACQUIRE, WEAK_COMPARE_AND_SET_BYTE_RELEASE, WEAK_COMPARE_AND_SET_BYTE_PLAIN;
    private static final MethodHandle COMPARE_AND_EXCHANGE_BYTE, COMPARE_AND_EXCHANGE_BYTE_ACQUIRE, COMPARE_AND_EXCHANGE_BYTE_RELEASE;

    private static final MethodHandle GET_SHORT, PUT_SHORT, GET_SHORT_VOLATILE, PUT_SHORT_VOLATILE, GET_SHORT_ACQUIRE, PUT_SHORT_RELEASE, GET_SHORT_OPAQUE, PUT_SHORT_OPAQUE, COMPARE_AND_SET_SHORT;
    private static final MethodHandle WEAK_COMPARE_AND_SET_SHORT, WEAK_COMPARE_AND_SET_SHORT_ACQUIRE, WEAK_COMPARE_AND_SET_SHORT_RELEASE, WEAK_COMPARE_AND_SET_SHORT_PLAIN;
    private static final MethodHandle COMPARE_AND_EXCHANGE_SHORT, COMPARE_AND_EXCHANGE_SHORT_ACQUIRE, COMPARE_AND_EXCHANGE_SHORT_RELEASE;

    private static final MethodHandle GET_CHAR, PUT_CHAR, GET_CHAR_VOLATILE, PUT_CHAR_VOLATILE, GET_CHAR_ACQUIRE, PUT_CHAR_RELEASE, GET_CHAR_OPAQUE, PUT_CHAR_OPAQUE, COMPARE_AND_SET_CHAR;
    private static final MethodHandle WEAK_COMPARE_AND_SET_CHAR, WEAK_COMPARE_AND_SET_CHAR_ACQUIRE, WEAK_COMPARE_AND_SET_CHAR_RELEASE, WEAK_COMPARE_AND_SET_CHAR_PLAIN;
    private static final MethodHandle COMPARE_AND_EXCHANGE_CHAR, COMPARE_AND_EXCHANGE_CHAR_ACQUIRE, COMPARE_AND_EXCHANGE_CHAR_RELEASE;

    private static final MethodHandle GET_FLOAT, PUT_FLOAT, GET_FLOAT_VOLATILE, PUT_FLOAT_VOLATILE, GET_FLOAT_ACQUIRE, PUT_FLOAT_RELEASE, GET_FLOAT_OPAQUE, PUT_FLOAT_OPAQUE, COMPARE_AND_SET_FLOAT;
    private static final MethodHandle WEAK_COMPARE_AND_SET_FLOAT, WEAK_COMPARE_AND_SET_FLOAT_ACQUIRE, WEAK_COMPARE_AND_SET_FLOAT_RELEASE, WEAK_COMPARE_AND_SET_FLOAT_PLAIN;
    private static final MethodHandle COMPARE_AND_EXCHANGE_FLOAT, COMPARE_AND_EXCHANGE_FLOAT_ACQUIRE, COMPARE_AND_EXCHANGE_FLOAT_RELEASE;

    private static final MethodHandle GET_DOUBLE, PUT_DOUBLE, GET_DOUBLE_VOLATILE, PUT_DOUBLE_VOLATILE, GET_DOUBLE_ACQUIRE, PUT_DOUBLE_RELEASE, GET_DOUBLE_OPAQUE, PUT_DOUBLE_OPAQUE, COMPARE_AND_SET_DOUBLE;
    private static final MethodHandle WEAK_COMPARE_AND_SET_DOUBLE, WEAK_COMPARE_AND_SET_DOUBLE_ACQUIRE, WEAK_COMPARE_AND_SET_DOUBLE_RELEASE, WEAK_COMPARE_AND_SET_DOUBLE_PLAIN;
    private static final MethodHandle COMPARE_AND_EXCHANGE_DOUBLE, COMPARE_AND_EXCHANGE_DOUBLE_ACQUIRE, COMPARE_AND_EXCHANGE_DOUBLE_RELEASE;

    private static final MethodHandle ALLOCATE_MEMORY, FREE_MEMORY, REALLOCATE_MEMORY, SET_MEMORY, COPY_MEMORY, ALLOCATE_UNINITIALIZED_ARRAY;
    private static final MethodHandle GET_BYTE_ADDRESS, PUT_BYTE_ADDRESS, GET_SHORT_ADDRESS, PUT_SHORT_ADDRESS, GET_CHAR_ADDRESS, PUT_CHAR_ADDRESS;
    private static final MethodHandle GET_INT_ADDRESS, PUT_INT_ADDRESS, GET_LONG_ADDRESS, PUT_LONG_ADDRESS;
    private static final MethodHandle GET_FLOAT_ADDRESS, PUT_FLOAT_ADDRESS, GET_DOUBLE_ADDRESS, PUT_DOUBLE_ADDRESS;
    private static final MethodHandle GET_ADDRESS_ADDRESS, PUT_ADDRESS_ADDRESS;
    private static final MethodHandle LOAD_FENCE, STORE_FENCE, FULL_FENCE, PARK, UNPARK, THROW_EXCEPTION;

    static {// @formatter:off
        try {
            // JDK-8159995 did the internal Unsafe compare* renaming in JDK 10 and JDK-8181292 backported them to JDK 9.
            // I doubt if anyone would use java 9 to run the mod though...
            // Specifically:
            // - compareAndExchange*Volatile to compareAndExchange*
            // - compareAndSwap* to compareAndSet*
            // - weakCompareAndSwap*Volatile -> weakCompareAndSet*
            // - weakCompareAndSwap* -> weakCompareAndSet*Plain
            boolean JDK_8207146 = MAJOR_VERSION >= 12; // Rename xxxObject -> xxxReference
            boolean JDK_8344168 = MAJOR_VERSION >= 25; // arrayBaseOffset returns long
            Class<?> unsafeClass = Class.forName("jdk.internal.misc.Unsafe");
            Object unsafeInstance = IMPL_LOOKUP.findStatic(unsafeClass, "getUnsafe", MethodType.methodType(unsafeClass)).invoke();
            UnsafeBinder binder = new UnsafeBinder(IMPL_LOOKUP, unsafeClass, unsafeInstance);

            // --- 1. Offsets & Basics ---
            OBJECT_FIELD_OFFSET = binder.bind("objectFieldOffset", long.class, Field.class);
            STATIC_FIELD_BASE = binder.bind("staticFieldBase", Object.class, Field.class);
            STATIC_FIELD_OFFSET = binder.bind("staticFieldOffset", long.class, Field.class);
            ALLOCATE_INSTANCE = binder.bind("allocateInstance", Object.class, Class.class);
            ALLOCATE_UNINITIALIZED_ARRAY = binder.bind("allocateUninitializedArray", Object.class, Class.class, int.class);

            MethodHandle arrayBase = binder.bind("arrayBaseOffset", JDK_8344168 ? long.class : int.class, Class.class);
            if (!JDK_8344168) {
                arrayBase = MethodHandles.explicitCastArguments(arrayBase, MethodType.methodType(long.class, Class.class));
            }
            ARRAY_BASE_OFFSET = arrayBase;
            ARRAY_INDEX_SCALE = binder.bind("arrayIndexScale", int.class, Class.class);
            ADDRESS_SIZE = binder.bind("addressSize", int.class);
            PAGE_SIZE = binder.bind("pageSize", int.class);

            // --- 2. References ---
            String getRefName = JDK_8207146 ? "getReference" : "getObject";
            String putRefName = JDK_8207146 ? "putReference" : "putObject";
            String getRefVolName = JDK_8207146 ? "getReferenceVolatile" : "getObjectVolatile";
            String putRefVolName = JDK_8207146 ? "putReferenceVolatile" : "putObjectVolatile";
            String casRefName = JDK_8207146 ? "compareAndSetReference" : "compareAndSwapObject";
            String getAndSetRefName = JDK_8207146 ? "getAndSetReference" : "getAndSetObject";

            GET_REFERENCE = binder.bind(getRefName, Object.class, Object.class, long.class);
            PUT_REFERENCE = binder.bind(putRefName, void.class, Object.class, long.class, Object.class);
            GET_REFERENCE_VOLATILE = binder.bind(getRefVolName, Object.class, Object.class, long.class);
            PUT_REFERENCE_VOLATILE = binder.bind(putRefVolName, void.class, Object.class, long.class, Object.class);
            COMPARE_AND_SET_REFERENCE = binder.bind(casRefName, boolean.class, Object.class, long.class, Object.class, Object.class);
            GET_AND_SET_REFERENCE = binder.bind(getAndSetRefName, Object.class, Object.class, long.class, Object.class);

            String getRefAcqName = JDK_8207146 ? "getReferenceAcquire" : "getObjectAcquire";
            String putRefRelName = JDK_8207146 ? "putReferenceRelease" : "putObjectRelease";
            String getRefOpaName = JDK_8207146 ? "getReferenceOpaque" : "getObjectOpaque";
            String putRefOpaName = JDK_8207146 ? "putReferenceOpaque" : "putObjectOpaque";

            String weakRefBaseName = JDK_8207146 ? "weakCompareAndSetReference" : "weakCompareAndSetObject";
            String weakRefAcqName = weakRefBaseName + "Acquire";
            String weakRefRelName = weakRefBaseName + "Release";
            String weakRefPlainName = weakRefBaseName + "Plain";

            String caeRefBaseName = JDK_8207146 ? "compareAndExchangeReference" : "compareAndExchangeObject";
            String caeRefAcqName = caeRefBaseName + "Acquire";
            String caeRefRelName = caeRefBaseName + "Release";

            String getAndSetRefAcqName = JDK_8207146 ? "getAndSetReferenceAcquire" : "getAndSetObjectAcquire";
            String getAndSetRefRelName = JDK_8207146 ? "getAndSetReferenceRelease" : "getAndSetObjectRelease";

            GET_REFERENCE_ACQUIRE = binder.bind(getRefAcqName, Object.class, Object.class, long.class);
            PUT_REFERENCE_RELEASE = binder.bind(putRefRelName, void.class, Object.class, long.class, Object.class);
            GET_REFERENCE_OPAQUE = binder.bind(getRefOpaName, Object.class, Object.class, long.class);
            PUT_REFERENCE_OPAQUE = binder.bind(putRefOpaName, void.class, Object.class, long.class, Object.class);

            WEAK_COMPARE_AND_SET_REFERENCE = binder.bind(weakRefBaseName, boolean.class, Object.class, long.class, Object.class, Object.class);
            WEAK_COMPARE_AND_SET_REFERENCE_ACQUIRE = binder.bind(weakRefAcqName, boolean.class, Object.class, long.class, Object.class, Object.class);
            WEAK_COMPARE_AND_SET_REFERENCE_RELEASE = binder.bind(weakRefRelName, boolean.class, Object.class, long.class, Object.class, Object.class);
            WEAK_COMPARE_AND_SET_REFERENCE_PLAIN = binder.bind(weakRefPlainName, boolean.class, Object.class, long.class, Object.class, Object.class);

            COMPARE_AND_EXCHANGE_REFERENCE = binder.bind(caeRefBaseName, Object.class, Object.class, long.class, Object.class, Object.class);
            COMPARE_AND_EXCHANGE_REFERENCE_ACQUIRE = binder.bind(caeRefAcqName, Object.class, Object.class, long.class, Object.class, Object.class);
            COMPARE_AND_EXCHANGE_REFERENCE_RELEASE = binder.bind(caeRefRelName, Object.class, Object.class, long.class, Object.class, Object.class);

            GET_AND_SET_REFERENCE_ACQUIRE = binder.bind(getAndSetRefAcqName, Object.class, Object.class, long.class, Object.class);
            GET_AND_SET_REFERENCE_RELEASE = binder.bind(getAndSetRefRelName, Object.class, Object.class, long.class, Object.class);

            // --- 3. Int ---
            GET_INT = binder.bind("getInt", int.class, Object.class, long.class);
            PUT_INT = binder.bind("putInt", void.class, Object.class, long.class, int.class);
            GET_INT_VOLATILE = binder.bind("getIntVolatile", int.class, Object.class, long.class);
            PUT_INT_VOLATILE = binder.bind("putIntVolatile", void.class, Object.class, long.class, int.class);
            COMPARE_AND_SET_INT = binder.bind("compareAndSetInt", boolean.class, Object.class, long.class, int.class, int.class);
            GET_AND_ADD_INT = binder.bind("getAndAddInt", int.class, Object.class, long.class, int.class);
            GET_AND_SET_INT = binder.bind("getAndSetInt", int.class, Object.class, long.class, int.class);

            GET_INT_ACQUIRE = binder.bind("getIntAcquire", int.class, Object.class, long.class);
            PUT_INT_RELEASE = binder.bind("putIntRelease", void.class, Object.class, long.class, int.class);
            GET_INT_OPAQUE = binder.bind("getIntOpaque", int.class, Object.class, long.class);
            PUT_INT_OPAQUE = binder.bind("putIntOpaque", void.class, Object.class, long.class, int.class);

            WEAK_COMPARE_AND_SET_INT = binder.bind("weakCompareAndSetInt", boolean.class, Object.class, long.class, int.class, int.class);
            WEAK_COMPARE_AND_SET_INT_ACQUIRE = binder.bind("weakCompareAndSetIntAcquire", boolean.class, Object.class, long.class, int.class, int.class);
            WEAK_COMPARE_AND_SET_INT_RELEASE = binder.bind("weakCompareAndSetIntRelease", boolean.class, Object.class, long.class, int.class, int.class);
            WEAK_COMPARE_AND_SET_INT_PLAIN = binder.bind("weakCompareAndSetIntPlain", boolean.class, Object.class, long.class, int.class, int.class);

            COMPARE_AND_EXCHANGE_INT = binder.bind("compareAndExchangeInt", int.class, Object.class, long.class, int.class, int.class);
            COMPARE_AND_EXCHANGE_INT_ACQUIRE = binder.bind("compareAndExchangeIntAcquire", int.class, Object.class, long.class, int.class, int.class);
            COMPARE_AND_EXCHANGE_INT_RELEASE = binder.bind("compareAndExchangeIntRelease", int.class, Object.class, long.class, int.class, int.class);

            GET_AND_SET_INT_ACQUIRE = binder.bind("getAndSetIntAcquire", int.class, Object.class, long.class, int.class);
            GET_AND_SET_INT_RELEASE = binder.bind("getAndSetIntRelease", int.class, Object.class, long.class, int.class);

            // --- 4. Long ---
            GET_LONG = binder.bind("getLong", long.class, Object.class, long.class);
            PUT_LONG = binder.bind("putLong", void.class, Object.class, long.class, long.class);
            GET_LONG_VOLATILE = binder.bind("getLongVolatile", long.class, Object.class, long.class);
            PUT_LONG_VOLATILE = binder.bind("putLongVolatile", void.class, Object.class, long.class, long.class);
            COMPARE_AND_SET_LONG = binder.bind("compareAndSetLong", boolean.class, Object.class, long.class, long.class, long.class);
            GET_AND_ADD_LONG = binder.bind("getAndAddLong", long.class, Object.class, long.class, long.class);
            GET_AND_SET_LONG = binder.bind("getAndSetLong", long.class, Object.class, long.class, long.class);

            GET_LONG_ACQUIRE = binder.bind("getLongAcquire", long.class, Object.class, long.class);
            PUT_LONG_RELEASE = binder.bind("putLongRelease", void.class, Object.class, long.class, long.class);
            GET_LONG_OPAQUE = binder.bind("getLongOpaque", long.class, Object.class, long.class);
            PUT_LONG_OPAQUE = binder.bind("putLongOpaque", void.class, Object.class, long.class, long.class);

            WEAK_COMPARE_AND_SET_LONG = binder.bind("weakCompareAndSetLong", boolean.class, Object.class, long.class, long.class, long.class);
            WEAK_COMPARE_AND_SET_LONG_ACQUIRE = binder.bind("weakCompareAndSetLongAcquire", boolean.class, Object.class, long.class, long.class, long.class);
            WEAK_COMPARE_AND_SET_LONG_RELEASE = binder.bind("weakCompareAndSetLongRelease", boolean.class, Object.class, long.class, long.class, long.class);
            WEAK_COMPARE_AND_SET_LONG_PLAIN = binder.bind("weakCompareAndSetLongPlain", boolean.class, Object.class, long.class, long.class, long.class);

            COMPARE_AND_EXCHANGE_LONG = binder.bind("compareAndExchangeLong", long.class, Object.class, long.class, long.class, long.class);
            COMPARE_AND_EXCHANGE_LONG_ACQUIRE = binder.bind("compareAndExchangeLongAcquire", long.class, Object.class, long.class, long.class, long.class);
            COMPARE_AND_EXCHANGE_LONG_RELEASE = binder.bind("compareAndExchangeLongRelease", long.class, Object.class, long.class, long.class, long.class);

            GET_AND_SET_LONG_ACQUIRE = binder.bind("getAndSetLongAcquire", long.class, Object.class, long.class, long.class);
            GET_AND_SET_LONG_RELEASE = binder.bind("getAndSetLongRelease", long.class, Object.class, long.class, long.class);

            // --- 5. Boolean ---
            GET_BOOLEAN = binder.bind("getBoolean", boolean.class, Object.class, long.class);
            PUT_BOOLEAN = binder.bind("putBoolean", void.class, Object.class, long.class, boolean.class);
            GET_BOOLEAN_VOLATILE = binder.bind("getBooleanVolatile", boolean.class, Object.class, long.class);
            PUT_BOOLEAN_VOLATILE = binder.bind("putBooleanVolatile", void.class, Object.class, long.class, boolean.class);

            GET_BOOLEAN_ACQUIRE = binder.bind("getBooleanAcquire", boolean.class, Object.class, long.class);
            PUT_BOOLEAN_RELEASE = binder.bind("putBooleanRelease", void.class, Object.class, long.class, boolean.class);
            GET_BOOLEAN_OPAQUE = binder.bind("getBooleanOpaque", boolean.class, Object.class, long.class);
            PUT_BOOLEAN_OPAQUE = binder.bind("putBooleanOpaque", void.class, Object.class, long.class, boolean.class);
            COMPARE_AND_SET_BOOLEAN = binder.bind("compareAndSetBoolean", boolean.class, Object.class, long.class, boolean.class, boolean.class);

            WEAK_COMPARE_AND_SET_BOOLEAN = binder.bind("weakCompareAndSetBoolean", boolean.class, Object.class, long.class, boolean.class, boolean.class);
            WEAK_COMPARE_AND_SET_BOOLEAN_ACQUIRE = binder.bind("weakCompareAndSetBooleanAcquire", boolean.class, Object.class, long.class, boolean.class, boolean.class);
            WEAK_COMPARE_AND_SET_BOOLEAN_RELEASE = binder.bind("weakCompareAndSetBooleanRelease", boolean.class, Object.class, long.class, boolean.class, boolean.class);
            WEAK_COMPARE_AND_SET_BOOLEAN_PLAIN = binder.bind("weakCompareAndSetBooleanPlain", boolean.class, Object.class, long.class, boolean.class, boolean.class);

            COMPARE_AND_EXCHANGE_BOOLEAN = binder.bind("compareAndExchangeBoolean", boolean.class, Object.class, long.class, boolean.class, boolean.class);
            COMPARE_AND_EXCHANGE_BOOLEAN_ACQUIRE = binder.bind("compareAndExchangeBooleanAcquire", boolean.class, Object.class, long.class, boolean.class, boolean.class);
            COMPARE_AND_EXCHANGE_BOOLEAN_RELEASE = binder.bind("compareAndExchangeBooleanRelease", boolean.class, Object.class, long.class, boolean.class, boolean.class);

            // --- 6. Byte ---
            GET_BYTE = binder.bind("getByte", byte.class, Object.class, long.class);
            PUT_BYTE = binder.bind("putByte", void.class, Object.class, long.class, byte.class);
            GET_BYTE_VOLATILE = binder.bind("getByteVolatile", byte.class, Object.class, long.class);
            PUT_BYTE_VOLATILE = binder.bind("putByteVolatile", void.class, Object.class, long.class, byte.class);

            GET_BYTE_ACQUIRE = binder.bind("getByteAcquire", byte.class, Object.class, long.class);
            PUT_BYTE_RELEASE = binder.bind("putByteRelease", void.class, Object.class, long.class, byte.class);
            GET_BYTE_OPAQUE = binder.bind("getByteOpaque", byte.class, Object.class, long.class);
            PUT_BYTE_OPAQUE = binder.bind("putByteOpaque", void.class, Object.class, long.class, byte.class);
            COMPARE_AND_SET_BYTE = binder.bind("compareAndSetByte", boolean.class, Object.class, long.class, byte.class, byte.class);

            WEAK_COMPARE_AND_SET_BYTE = binder.bind("weakCompareAndSetByte", boolean.class, Object.class, long.class, byte.class, byte.class);
            WEAK_COMPARE_AND_SET_BYTE_ACQUIRE = binder.bind("weakCompareAndSetByteAcquire", boolean.class, Object.class, long.class, byte.class, byte.class);
            WEAK_COMPARE_AND_SET_BYTE_RELEASE = binder.bind("weakCompareAndSetByteRelease", boolean.class, Object.class, long.class, byte.class, byte.class);
            WEAK_COMPARE_AND_SET_BYTE_PLAIN = binder.bind("weakCompareAndSetBytePlain", boolean.class, Object.class, long.class, byte.class, byte.class);

            COMPARE_AND_EXCHANGE_BYTE = binder.bind("compareAndExchangeByte", byte.class, Object.class, long.class, byte.class, byte.class);
            COMPARE_AND_EXCHANGE_BYTE_ACQUIRE = binder.bind("compareAndExchangeByteAcquire", byte.class, Object.class, long.class, byte.class, byte.class);
            COMPARE_AND_EXCHANGE_BYTE_RELEASE = binder.bind("compareAndExchangeByteRelease", byte.class, Object.class, long.class, byte.class, byte.class);

            // --- 7. Short ---
            GET_SHORT = binder.bind("getShort", short.class, Object.class, long.class);
            PUT_SHORT = binder.bind("putShort", void.class, Object.class, long.class, short.class);
            GET_SHORT_VOLATILE = binder.bind("getShortVolatile", short.class, Object.class, long.class);
            PUT_SHORT_VOLATILE = binder.bind("putShortVolatile", void.class, Object.class, long.class, short.class);

            GET_SHORT_ACQUIRE = binder.bind("getShortAcquire", short.class, Object.class, long.class);
            PUT_SHORT_RELEASE = binder.bind("putShortRelease", void.class, Object.class, long.class, short.class);
            GET_SHORT_OPAQUE = binder.bind("getShortOpaque", short.class, Object.class, long.class);
            PUT_SHORT_OPAQUE = binder.bind("putShortOpaque", void.class, Object.class, long.class, short.class);
            COMPARE_AND_SET_SHORT = binder.bind("compareAndSetShort", boolean.class, Object.class, long.class, short.class, short.class);

            WEAK_COMPARE_AND_SET_SHORT = binder.bind("weakCompareAndSetShort", boolean.class, Object.class, long.class, short.class, short.class);
            WEAK_COMPARE_AND_SET_SHORT_ACQUIRE = binder.bind("weakCompareAndSetShortAcquire", boolean.class, Object.class, long.class, short.class, short.class);
            WEAK_COMPARE_AND_SET_SHORT_RELEASE = binder.bind("weakCompareAndSetShortRelease", boolean.class, Object.class, long.class, short.class, short.class);
            WEAK_COMPARE_AND_SET_SHORT_PLAIN = binder.bind("weakCompareAndSetShortPlain", boolean.class, Object.class, long.class, short.class, short.class);

            COMPARE_AND_EXCHANGE_SHORT = binder.bind("compareAndExchangeShort", short.class, Object.class, long.class, short.class, short.class);
            COMPARE_AND_EXCHANGE_SHORT_ACQUIRE = binder.bind("compareAndExchangeShortAcquire", short.class, Object.class, long.class, short.class, short.class);
            COMPARE_AND_EXCHANGE_SHORT_RELEASE = binder.bind("compareAndExchangeShortRelease", short.class, Object.class, long.class, short.class, short.class);

            // --- 8. Char ---
            GET_CHAR = binder.bind("getChar", char.class, Object.class, long.class);
            PUT_CHAR = binder.bind("putChar", void.class, Object.class, long.class, char.class);
            GET_CHAR_VOLATILE = binder.bind("getCharVolatile", char.class, Object.class, long.class);
            PUT_CHAR_VOLATILE = binder.bind("putCharVolatile", void.class, Object.class, long.class, char.class);

            GET_CHAR_ACQUIRE = binder.bind("getCharAcquire", char.class, Object.class, long.class);
            PUT_CHAR_RELEASE = binder.bind("putCharRelease", void.class, Object.class, long.class, char.class);
            GET_CHAR_OPAQUE = binder.bind("getCharOpaque", char.class, Object.class, long.class);
            PUT_CHAR_OPAQUE = binder.bind("putCharOpaque", void.class, Object.class, long.class, char.class);
            COMPARE_AND_SET_CHAR = binder.bind("compareAndSetChar", boolean.class, Object.class, long.class, char.class, char.class);

            WEAK_COMPARE_AND_SET_CHAR = binder.bind("weakCompareAndSetChar", boolean.class, Object.class, long.class, char.class, char.class);
            WEAK_COMPARE_AND_SET_CHAR_ACQUIRE = binder.bind("weakCompareAndSetCharAcquire", boolean.class, Object.class, long.class, char.class, char.class);
            WEAK_COMPARE_AND_SET_CHAR_RELEASE = binder.bind("weakCompareAndSetCharRelease", boolean.class, Object.class, long.class, char.class, char.class);
            WEAK_COMPARE_AND_SET_CHAR_PLAIN = binder.bind("weakCompareAndSetCharPlain", boolean.class, Object.class, long.class, char.class, char.class);

            COMPARE_AND_EXCHANGE_CHAR = binder.bind("compareAndExchangeChar", char.class, Object.class, long.class, char.class, char.class);
            COMPARE_AND_EXCHANGE_CHAR_ACQUIRE = binder.bind("compareAndExchangeCharAcquire", char.class, Object.class, long.class, char.class, char.class);
            COMPARE_AND_EXCHANGE_CHAR_RELEASE = binder.bind("compareAndExchangeCharRelease", char.class, Object.class, long.class, char.class, char.class);

            // --- 9. Float ---
            GET_FLOAT = binder.bind("getFloat", float.class, Object.class, long.class);
            PUT_FLOAT = binder.bind("putFloat", void.class, Object.class, long.class, float.class);
            GET_FLOAT_VOLATILE = binder.bind("getFloatVolatile", float.class, Object.class, long.class);
            PUT_FLOAT_VOLATILE = binder.bind("putFloatVolatile", void.class, Object.class, long.class, float.class);

            GET_FLOAT_ACQUIRE = binder.bind("getFloatAcquire", float.class, Object.class, long.class);
            PUT_FLOAT_RELEASE = binder.bind("putFloatRelease", void.class, Object.class, long.class, float.class);
            GET_FLOAT_OPAQUE = binder.bind("getFloatOpaque", float.class, Object.class, long.class);
            PUT_FLOAT_OPAQUE = binder.bind("putFloatOpaque", void.class, Object.class, long.class, float.class);
            COMPARE_AND_SET_FLOAT = binder.bind("compareAndSetFloat", boolean.class, Object.class, long.class, float.class, float.class);

            WEAK_COMPARE_AND_SET_FLOAT = binder.bind("weakCompareAndSetFloat", boolean.class, Object.class, long.class, float.class, float.class);
            WEAK_COMPARE_AND_SET_FLOAT_ACQUIRE = binder.bind("weakCompareAndSetFloatAcquire", boolean.class, Object.class, long.class, float.class, float.class);
            WEAK_COMPARE_AND_SET_FLOAT_RELEASE = binder.bind("weakCompareAndSetFloatRelease", boolean.class, Object.class, long.class, float.class, float.class);
            WEAK_COMPARE_AND_SET_FLOAT_PLAIN = binder.bind("weakCompareAndSetFloatPlain", boolean.class, Object.class, long.class, float.class, float.class);

            COMPARE_AND_EXCHANGE_FLOAT = binder.bind("compareAndExchangeFloat", float.class, Object.class, long.class, float.class, float.class);
            COMPARE_AND_EXCHANGE_FLOAT_ACQUIRE = binder.bind("compareAndExchangeFloatAcquire", float.class, Object.class, long.class, float.class, float.class);
            COMPARE_AND_EXCHANGE_FLOAT_RELEASE = binder.bind("compareAndExchangeFloatRelease", float.class, Object.class, long.class, float.class, float.class);

            // --- 10. Double ---
            GET_DOUBLE = binder.bind("getDouble", double.class, Object.class, long.class);
            PUT_DOUBLE = binder.bind("putDouble", void.class, Object.class, long.class, double.class);
            GET_DOUBLE_VOLATILE = binder.bind("getDoubleVolatile", double.class, Object.class, long.class);
            PUT_DOUBLE_VOLATILE = binder.bind("putDoubleVolatile", void.class, Object.class, long.class, double.class);

            GET_DOUBLE_ACQUIRE = binder.bind("getDoubleAcquire", double.class, Object.class, long.class);
            PUT_DOUBLE_RELEASE = binder.bind("putDoubleRelease", void.class, Object.class, long.class, double.class);
            GET_DOUBLE_OPAQUE = binder.bind("getDoubleOpaque", double.class, Object.class, long.class);
            PUT_DOUBLE_OPAQUE = binder.bind("putDoubleOpaque", void.class, Object.class, long.class, double.class);
            COMPARE_AND_SET_DOUBLE = binder.bind("compareAndSetDouble", boolean.class, Object.class, long.class, double.class, double.class);

            WEAK_COMPARE_AND_SET_DOUBLE = binder.bind("weakCompareAndSetDouble", boolean.class, Object.class, long.class, double.class, double.class);
            WEAK_COMPARE_AND_SET_DOUBLE_ACQUIRE = binder.bind("weakCompareAndSetDoubleAcquire", boolean.class, Object.class, long.class, double.class, double.class);
            WEAK_COMPARE_AND_SET_DOUBLE_RELEASE = binder.bind("weakCompareAndSetDoubleRelease", boolean.class, Object.class, long.class, double.class, double.class);
            WEAK_COMPARE_AND_SET_DOUBLE_PLAIN = binder.bind("weakCompareAndSetDoublePlain", boolean.class, Object.class, long.class, double.class, double.class);

            COMPARE_AND_EXCHANGE_DOUBLE = binder.bind("compareAndExchangeDouble", double.class, Object.class, long.class, double.class, double.class);
            COMPARE_AND_EXCHANGE_DOUBLE_ACQUIRE = binder.bind("compareAndExchangeDoubleAcquire", double.class, Object.class, long.class, double.class, double.class);
            COMPARE_AND_EXCHANGE_DOUBLE_RELEASE = binder.bind("compareAndExchangeDoubleRelease", double.class, Object.class, long.class, double.class, double.class);

            // --- 11. Memory ---
            ALLOCATE_MEMORY = binder.bind("allocateMemory", long.class, long.class);
            FREE_MEMORY = binder.bind("freeMemory", void.class, long.class);
            REALLOCATE_MEMORY = binder.bind("reallocateMemory", long.class, long.class, long.class);
            SET_MEMORY = binder.bind("setMemory", void.class, long.class, long.class, byte.class);
            COPY_MEMORY = binder.bind("copyMemory", void.class, Object.class, long.class, Object.class, long.class, long.class);

            // Raw Primitive Access
            GET_BYTE_ADDRESS = binder.bind("getByte", byte.class, long.class);
            PUT_BYTE_ADDRESS = binder.bind("putByte", void.class, long.class, byte.class);
            GET_SHORT_ADDRESS = binder.bind("getShort", short.class, long.class);
            PUT_SHORT_ADDRESS = binder.bind("putShort", void.class, long.class, short.class);
            GET_CHAR_ADDRESS = binder.bind("getChar", char.class, long.class);
            PUT_CHAR_ADDRESS = binder.bind("putChar", void.class, long.class, char.class);
            GET_INT_ADDRESS = binder.bind("getInt", int.class, long.class);
            PUT_INT_ADDRESS = binder.bind("putInt", void.class, long.class, int.class);
            GET_LONG_ADDRESS = binder.bind("getLong", long.class, long.class);
            PUT_LONG_ADDRESS = binder.bind("putLong", void.class, long.class, long.class);
            GET_FLOAT_ADDRESS = binder.bind("getFloat", float.class, long.class);
            PUT_FLOAT_ADDRESS = binder.bind("putFloat", void.class, long.class, float.class);
            GET_DOUBLE_ADDRESS = binder.bind("getDouble", double.class, long.class);
            PUT_DOUBLE_ADDRESS = binder.bind("putDouble", void.class, long.class, double.class);
            GET_ADDRESS_ADDRESS = binder.bind("getAddress", long.class, long.class);
            PUT_ADDRESS_ADDRESS = binder.bind("putAddress", void.class, long.class, long.class);

            // --- 12. Fences & Misc ---
            LOAD_FENCE = binder.bind("loadFence", void.class);
            STORE_FENCE = binder.bind("storeFence", void.class);
            FULL_FENCE = binder.bind("fullFence", void.class);
            PARK = binder.bind("park", void.class, boolean.class, long.class);
            UNPARK = binder.bind("unpark", void.class, Object.class);
            THROW_EXCEPTION = binder.bind("throwException", void.class, Throwable.class);

        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }// @formatter:on

    InternalUnsafeWrapper() {
    }

    public long objectFieldOffset(Field f) {
        return (long) OBJECT_FIELD_OFFSET.invokeExact(f);
    }

    public Object staticFieldBase(Field f) {
        return (Object) STATIC_FIELD_BASE.invokeExact(f);
    }

    public long staticFieldOffset(Field f) {
        return (long) STATIC_FIELD_OFFSET.invokeExact(f);
    }

    public Object allocateInstance(Class<?> cls) throws InstantiationException {
        return ALLOCATE_INSTANCE.invokeExact(cls);
    }

    public Object allocateUninitializedArray(Class<?> componentType, int length) {
        return ALLOCATE_UNINITIALIZED_ARRAY.invokeExact(componentType, length);
    }

    public long arrayBaseOffset(Class<?> cls) {
        return (long) ARRAY_BASE_OFFSET.invokeExact(cls);
    }

    public int arrayIndexScale(Class<?> cls) {
        return (int) ARRAY_INDEX_SCALE.invokeExact(cls);
    }

    public int addressSize() {
        return (int) ADDRESS_SIZE.invokeExact();
    }

    public int pageSize() {
        return (int) PAGE_SIZE.invokeExact();
    }

    public Object getReference(Object o, long offset) {
        return GET_REFERENCE.invokeExact(o, offset);
    }

    public void putReference(Object o, long offset, Object x) {
        PUT_REFERENCE.invokeExact(o, offset, x);
    }

    public Object getReferenceVolatile(Object o, long offset) {
        return GET_REFERENCE_VOLATILE.invokeExact(o, offset);
    }

    public void putReferenceVolatile(Object o, long offset, Object x) {
        PUT_REFERENCE_VOLATILE.invokeExact(o, offset, x);
    }

    public Object getReferenceAcquire(Object o, long offset) {
        return GET_REFERENCE_ACQUIRE.invokeExact(o, offset);
    }

    public void putReferenceRelease(Object o, long offset, Object x) {
        PUT_REFERENCE_RELEASE.invokeExact(o, offset, x);
    }

    public Object getReferenceOpaque(Object o, long offset) {
        return GET_REFERENCE_OPAQUE.invokeExact(o, offset);
    }

    public void putReferenceOpaque(Object o, long offset, Object x) {
        PUT_REFERENCE_OPAQUE.invokeExact(o, offset, x);
    }

    public boolean compareAndSetReference(Object o, long offset, Object expected, Object x) {
        return (boolean) COMPARE_AND_SET_REFERENCE.invokeExact(o, offset, expected, x);
    }

    public Object getAndSetReference(Object o, long offset, Object x) {
        return GET_AND_SET_REFERENCE.invokeExact(o, offset, x);
    }

    public boolean weakCompareAndSetReference(Object o, long offset, Object expected, Object x) {
        return (boolean) WEAK_COMPARE_AND_SET_REFERENCE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetReferenceAcquire(Object o, long offset, Object expected, Object x) {
        return (boolean) WEAK_COMPARE_AND_SET_REFERENCE_ACQUIRE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetReferenceRelease(Object o, long offset, Object expected, Object x) {
        return (boolean) WEAK_COMPARE_AND_SET_REFERENCE_RELEASE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetReferencePlain(Object o, long offset, Object expected, Object x) {
        return (boolean) WEAK_COMPARE_AND_SET_REFERENCE_PLAIN.invokeExact(o, offset, expected, x);
    }

    public Object compareAndExchangeReference(Object o, long offset, Object expected, Object x) {
        return COMPARE_AND_EXCHANGE_REFERENCE.invokeExact(o, offset, expected, x);
    }

    public Object compareAndExchangeReferenceAcquire(Object o, long offset, Object expected, Object x) {
        return COMPARE_AND_EXCHANGE_REFERENCE_ACQUIRE.invokeExact(o, offset, expected, x);
    }

    public Object compareAndExchangeReferenceRelease(Object o, long offset, Object expected, Object x) {
        return COMPARE_AND_EXCHANGE_REFERENCE_RELEASE.invokeExact(o, offset, expected, x);
    }

    public Object getAndSetReferenceAcquire(Object o, long offset, Object x) {
        return GET_AND_SET_REFERENCE_ACQUIRE.invokeExact(o, offset, x);
    }

    public Object getAndSetReferenceRelease(Object o, long offset, Object x) {
        return GET_AND_SET_REFERENCE_RELEASE.invokeExact(o, offset, x);
    }

    public int getInt(Object o, long offset) {
        return (int) GET_INT.invokeExact(o, offset);
    }

    public void putInt(Object o, long offset, int x) {
        PUT_INT.invokeExact(o, offset, x);
    }

    public int getIntVolatile(Object o, long offset) {
        return (int) GET_INT_VOLATILE.invokeExact(o, offset);
    }

    public void putIntVolatile(Object o, long offset, int x) {
        PUT_INT_VOLATILE.invokeExact(o, offset, x);
    }

    public int getIntAcquire(Object o, long offset) {
        return (int) GET_INT_ACQUIRE.invokeExact(o, offset);
    }

    public void putIntRelease(Object o, long offset, int x) {
        PUT_INT_RELEASE.invokeExact(o, offset, x);
    }

    public int getIntOpaque(Object o, long offset) {
        return (int) GET_INT_OPAQUE.invokeExact(o, offset);
    }

    public void putIntOpaque(Object o, long offset, int x) {
        PUT_INT_OPAQUE.invokeExact(o, offset, x);
    }

    public boolean compareAndSetInt(Object o, long offset, int expected, int x) {
        return (boolean) COMPARE_AND_SET_INT.invokeExact(o, offset, expected, x);
    }

    public int getAndAddInt(Object o, long offset, int delta) {
        return (int) GET_AND_ADD_INT.invokeExact(o, offset, delta);
    }

    public int getAndSetInt(Object o, long offset, int x) {
        return (int) GET_AND_SET_INT.invokeExact(o, offset, x);
    }

    public boolean weakCompareAndSetInt(Object o, long offset, int expected, int x) {
        return (boolean) WEAK_COMPARE_AND_SET_INT.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetIntAcquire(Object o, long offset, int expected, int x) {
        return (boolean) WEAK_COMPARE_AND_SET_INT_ACQUIRE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetIntRelease(Object o, long offset, int expected, int x) {
        return (boolean) WEAK_COMPARE_AND_SET_INT_RELEASE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetIntPlain(Object o, long offset, int expected, int x) {
        return (boolean) WEAK_COMPARE_AND_SET_INT_PLAIN.invokeExact(o, offset, expected, x);
    }

    public int compareAndExchangeInt(Object o, long offset, int expected, int x) {
        return (int) COMPARE_AND_EXCHANGE_INT.invokeExact(o, offset, expected, x);
    }

    public int compareAndExchangeIntAcquire(Object o, long offset, int expected, int x) {
        return (int) COMPARE_AND_EXCHANGE_INT_ACQUIRE.invokeExact(o, offset, expected, x);
    }

    public int compareAndExchangeIntRelease(Object o, long offset, int expected, int x) {
        return (int) COMPARE_AND_EXCHANGE_INT_RELEASE.invokeExact(o, offset, expected, x);
    }

    public int getAndSetIntAcquire(Object o, long offset, int x) {
        return (int) GET_AND_SET_INT_ACQUIRE.invokeExact(o, offset, x);
    }

    public int getAndSetIntRelease(Object o, long offset, int x) {
        return (int) GET_AND_SET_INT_RELEASE.invokeExact(o, offset, x);
    }

    public long getLong(Object o, long offset) {
        return (long) GET_LONG.invokeExact(o, offset);
    }

    public void putLong(Object o, long offset, long x) {
        PUT_LONG.invokeExact(o, offset, x);
    }

    public long getLongVolatile(Object o, long offset) {
        return (long) GET_LONG_VOLATILE.invokeExact(o, offset);
    }

    public void putLongVolatile(Object o, long offset, long x) {
        PUT_LONG_VOLATILE.invokeExact(o, offset, x);
    }

    public long getLongAcquire(Object o, long offset) {
        return (long) GET_LONG_ACQUIRE.invokeExact(o, offset);
    }

    public void putLongRelease(Object o, long offset, long x) {
        PUT_LONG_RELEASE.invokeExact(o, offset, x);
    }

    public long getLongOpaque(Object o, long offset) {
        return (long) GET_LONG_OPAQUE.invokeExact(o, offset);
    }

    public void putLongOpaque(Object o, long offset, long x) {
        PUT_LONG_OPAQUE.invokeExact(o, offset, x);
    }

    public boolean compareAndSetLong(Object o, long offset, long expected, long x) {
        return (boolean) COMPARE_AND_SET_LONG.invokeExact(o, offset, expected, x);
    }

    public long getAndAddLong(Object o, long offset, long delta) {
        return (long) GET_AND_ADD_LONG.invokeExact(o, offset, delta);
    }

    public long getAndSetLong(Object o, long offset, long x) {
        return (long) GET_AND_SET_LONG.invokeExact(o, offset, x);
    }

    public boolean weakCompareAndSetLong(Object o, long offset, long expected, long x) {
        return (boolean) WEAK_COMPARE_AND_SET_LONG.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetLongAcquire(Object o, long offset, long expected, long x) {
        return (boolean) WEAK_COMPARE_AND_SET_LONG_ACQUIRE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetLongRelease(Object o, long offset, long expected, long x) {
        return (boolean) WEAK_COMPARE_AND_SET_LONG_RELEASE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetLongPlain(Object o, long offset, long expected, long x) {
        return (boolean) WEAK_COMPARE_AND_SET_LONG_PLAIN.invokeExact(o, offset, expected, x);
    }

    public long compareAndExchangeLong(Object o, long offset, long expected, long x) {
        return (long) COMPARE_AND_EXCHANGE_LONG.invokeExact(o, offset, expected, x);
    }

    public long compareAndExchangeLongAcquire(Object o, long offset, long expected, long x) {
        return (long) COMPARE_AND_EXCHANGE_LONG_ACQUIRE.invokeExact(o, offset, expected, x);
    }

    public long compareAndExchangeLongRelease(Object o, long offset, long expected, long x) {
        return (long) COMPARE_AND_EXCHANGE_LONG_RELEASE.invokeExact(o, offset, expected, x);
    }

    public long getAndSetLongAcquire(Object o, long offset, long x) {
        return (long) GET_AND_SET_LONG_ACQUIRE.invokeExact(o, offset, x);
    }

    public long getAndSetLongRelease(Object o, long offset, long x) {
        return (long) GET_AND_SET_LONG_RELEASE.invokeExact(o, offset, x);
    }

    public boolean getBoolean(Object o, long offset) {
        return (boolean) GET_BOOLEAN.invokeExact(o, offset);
    }

    public void putBoolean(Object o, long offset, boolean x) {
        PUT_BOOLEAN.invokeExact(o, offset, x);
    }

    public boolean getBooleanVolatile(Object o, long offset) {
        return (boolean) GET_BOOLEAN_VOLATILE.invokeExact(o, offset);
    }

    public void putBooleanVolatile(Object o, long offset, boolean x) {
        PUT_BOOLEAN_VOLATILE.invokeExact(o, offset, x);
    }

    public boolean getBooleanAcquire(Object o, long offset) {
        return (boolean) GET_BOOLEAN_ACQUIRE.invokeExact(o, offset);
    }

    public void putBooleanRelease(Object o, long offset, boolean x) {
        PUT_BOOLEAN_RELEASE.invokeExact(o, offset, x);
    }

    public boolean getBooleanOpaque(Object o, long offset) {
        return (boolean) GET_BOOLEAN_OPAQUE.invokeExact(o, offset);
    }

    public void putBooleanOpaque(Object o, long offset, boolean x) {
        PUT_BOOLEAN_OPAQUE.invokeExact(o, offset, x);
    }

    public boolean compareAndSetBoolean(Object o, long offset, boolean expected, boolean x) {
        return (boolean) COMPARE_AND_SET_BOOLEAN.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetBoolean(Object o, long offset, boolean expected, boolean x) {
        return (boolean) WEAK_COMPARE_AND_SET_BOOLEAN.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetBooleanAcquire(Object o, long offset, boolean expected, boolean x) {
        return (boolean) WEAK_COMPARE_AND_SET_BOOLEAN_ACQUIRE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetBooleanRelease(Object o, long offset, boolean expected, boolean x) {
        return (boolean) WEAK_COMPARE_AND_SET_BOOLEAN_RELEASE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetBooleanPlain(Object o, long offset, boolean expected, boolean x) {
        return (boolean) WEAK_COMPARE_AND_SET_BOOLEAN_PLAIN.invokeExact(o, offset, expected, x);
    }

    public boolean compareAndExchangeBoolean(Object o, long offset, boolean expected, boolean x) {
        return (boolean) COMPARE_AND_EXCHANGE_BOOLEAN.invokeExact(o, offset, expected, x);
    }

    public boolean compareAndExchangeBooleanAcquire(Object o, long offset, boolean expected, boolean x) {
        return (boolean) COMPARE_AND_EXCHANGE_BOOLEAN_ACQUIRE.invokeExact(o, offset, expected, x);
    }

    public boolean compareAndExchangeBooleanRelease(Object o, long offset, boolean expected, boolean x) {
        return (boolean) COMPARE_AND_EXCHANGE_BOOLEAN_RELEASE.invokeExact(o, offset, expected, x);
    }

    public byte getByte(Object o, long offset) {
        return (byte) GET_BYTE.invokeExact(o, offset);
    }

    public void putByte(Object o, long offset, byte x) {
        PUT_BYTE.invokeExact(o, offset, x);
    }

    public byte getByteVolatile(Object o, long offset) {
        return (byte) GET_BYTE_VOLATILE.invokeExact(o, offset);
    }

    public void putByteVolatile(Object o, long offset, byte x) {
        PUT_BYTE_VOLATILE.invokeExact(o, offset, x);
    }

    public byte getByteAcquire(Object o, long offset) {
        return (byte) GET_BYTE_ACQUIRE.invokeExact(o, offset);
    }

    public void putByteRelease(Object o, long offset, byte x) {
        PUT_BYTE_RELEASE.invokeExact(o, offset, x);
    }

    public byte getByteOpaque(Object o, long offset) {
        return (byte) GET_BYTE_OPAQUE.invokeExact(o, offset);
    }

    public void putByteOpaque(Object o, long offset, byte x) {
        PUT_BYTE_OPAQUE.invokeExact(o, offset, x);
    }

    public boolean compareAndSetByte(Object o, long offset, byte expected, byte x) {
        return (boolean) COMPARE_AND_SET_BYTE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetByte(Object o, long offset, byte expected, byte x) {
        return (boolean) WEAK_COMPARE_AND_SET_BYTE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetByteAcquire(Object o, long offset, byte expected, byte x) {
        return (boolean) WEAK_COMPARE_AND_SET_BYTE_ACQUIRE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetByteRelease(Object o, long offset, byte expected, byte x) {
        return (boolean) WEAK_COMPARE_AND_SET_BYTE_RELEASE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetBytePlain(Object o, long offset, byte expected, byte x) {
        return (boolean) WEAK_COMPARE_AND_SET_BYTE_PLAIN.invokeExact(o, offset, expected, x);
    }

    public byte compareAndExchangeByte(Object o, long offset, byte expected, byte x) {
        return (byte) COMPARE_AND_EXCHANGE_BYTE.invokeExact(o, offset, expected, x);
    }

    public byte compareAndExchangeByteAcquire(Object o, long offset, byte expected, byte x) {
        return (byte) COMPARE_AND_EXCHANGE_BYTE_ACQUIRE.invokeExact(o, offset, expected, x);
    }

    public byte compareAndExchangeByteRelease(Object o, long offset, byte expected, byte x) {
        return (byte) COMPARE_AND_EXCHANGE_BYTE_RELEASE.invokeExact(o, offset, expected, x);
    }

    public short getShort(Object o, long offset) {
        return (short) GET_SHORT.invokeExact(o, offset);
    }

    public void putShort(Object o, long offset, short x) {
        PUT_SHORT.invokeExact(o, offset, x);
    }

    public short getShortVolatile(Object o, long offset) {
        return (short) GET_SHORT_VOLATILE.invokeExact(o, offset);
    }

    public void putShortVolatile(Object o, long offset, short x) {
        PUT_SHORT_VOLATILE.invokeExact(o, offset, x);
    }

    public short getShortAcquire(Object o, long offset) {
        return (short) GET_SHORT_ACQUIRE.invokeExact(o, offset);
    }

    public void putShortRelease(Object o, long offset, short x) {
        PUT_SHORT_RELEASE.invokeExact(o, offset, x);
    }

    public short getShortOpaque(Object o, long offset) {
        return (short) GET_SHORT_OPAQUE.invokeExact(o, offset);
    }

    public void putShortOpaque(Object o, long offset, short x) {
        PUT_SHORT_OPAQUE.invokeExact(o, offset, x);
    }

    public boolean compareAndSetShort(Object o, long offset, short expected, short x) {
        return (boolean) COMPARE_AND_SET_SHORT.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetShort(Object o, long offset, short expected, short x) {
        return (boolean) WEAK_COMPARE_AND_SET_SHORT.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetShortAcquire(Object o, long offset, short expected, short x) {
        return (boolean) WEAK_COMPARE_AND_SET_SHORT_ACQUIRE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetShortRelease(Object o, long offset, short expected, short x) {
        return (boolean) WEAK_COMPARE_AND_SET_SHORT_RELEASE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetShortPlain(Object o, long offset, short expected, short x) {
        return (boolean) WEAK_COMPARE_AND_SET_SHORT_PLAIN.invokeExact(o, offset, expected, x);
    }

    public short compareAndExchangeShort(Object o, long offset, short expected, short x) {
        return (short) COMPARE_AND_EXCHANGE_SHORT.invokeExact(o, offset, expected, x);
    }

    public short compareAndExchangeShortAcquire(Object o, long offset, short expected, short x) {
        return (short) COMPARE_AND_EXCHANGE_SHORT_ACQUIRE.invokeExact(o, offset, expected, x);
    }

    public short compareAndExchangeShortRelease(Object o, long offset, short expected, short x) {
        return (short) COMPARE_AND_EXCHANGE_SHORT_RELEASE.invokeExact(o, offset, expected, x);
    }

    public char getChar(Object o, long offset) {
        return (char) GET_CHAR.invokeExact(o, offset);
    }

    public void putChar(Object o, long offset, char x) {
        PUT_CHAR.invokeExact(o, offset, x);
    }

    public char getCharVolatile(Object o, long offset) {
        return (char) GET_CHAR_VOLATILE.invokeExact(o, offset);
    }

    public void putCharVolatile(Object o, long offset, char x) {
        PUT_CHAR_VOLATILE.invokeExact(o, offset, x);
    }

    public char getCharAcquire(Object o, long offset) {
        return (char) GET_CHAR_ACQUIRE.invokeExact(o, offset);
    }

    public void putCharRelease(Object o, long offset, char x) {
        PUT_CHAR_RELEASE.invokeExact(o, offset, x);
    }

    public char getCharOpaque(Object o, long offset) {
        return (char) GET_CHAR_OPAQUE.invokeExact(o, offset);
    }

    public void putCharOpaque(Object o, long offset, char x) {
        PUT_CHAR_OPAQUE.invokeExact(o, offset, x);
    }

    public boolean compareAndSetChar(Object o, long offset, char expected, char x) {
        return (boolean) COMPARE_AND_SET_CHAR.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetChar(Object o, long offset, char expected, char x) {
        return (boolean) WEAK_COMPARE_AND_SET_CHAR.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetCharAcquire(Object o, long offset, char expected, char x) {
        return (boolean) WEAK_COMPARE_AND_SET_CHAR_ACQUIRE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetCharRelease(Object o, long offset, char expected, char x) {
        return (boolean) WEAK_COMPARE_AND_SET_CHAR_RELEASE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetCharPlain(Object o, long offset, char expected, char x) {
        return (boolean) WEAK_COMPARE_AND_SET_CHAR_PLAIN.invokeExact(o, offset, expected, x);
    }

    public char compareAndExchangeChar(Object o, long offset, char expected, char x) {
        return (char) COMPARE_AND_EXCHANGE_CHAR.invokeExact(o, offset, expected, x);
    }

    public char compareAndExchangeCharAcquire(Object o, long offset, char expected, char x) {
        return (char) COMPARE_AND_EXCHANGE_CHAR_ACQUIRE.invokeExact(o, offset, expected, x);
    }

    public char compareAndExchangeCharRelease(Object o, long offset, char expected, char x) {
        return (char) COMPARE_AND_EXCHANGE_CHAR_RELEASE.invokeExact(o, offset, expected, x);
    }

    public float getFloat(Object o, long offset) {
        return (float) GET_FLOAT.invokeExact(o, offset);
    }

    public void putFloat(Object o, long offset, float x) {
        PUT_FLOAT.invokeExact(o, offset, x);
    }

    public float getFloatVolatile(Object o, long offset) {
        return (float) GET_FLOAT_VOLATILE.invokeExact(o, offset);
    }

    public void putFloatVolatile(Object o, long offset, float x) {
        PUT_FLOAT_VOLATILE.invokeExact(o, offset, x);
    }

    public float getFloatAcquire(Object o, long offset) {
        return (float) GET_FLOAT_ACQUIRE.invokeExact(o, offset);
    }

    public void putFloatRelease(Object o, long offset, float x) {
        PUT_FLOAT_RELEASE.invokeExact(o, offset, x);
    }

    public float getFloatOpaque(Object o, long offset) {
        return (float) GET_FLOAT_OPAQUE.invokeExact(o, offset);
    }

    public void putFloatOpaque(Object o, long offset, float x) {
        PUT_FLOAT_OPAQUE.invokeExact(o, offset, x);
    }

    public boolean compareAndSetFloat(Object o, long offset, float expected, float x) {
        return (boolean) COMPARE_AND_SET_FLOAT.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetFloat(Object o, long offset, float expected, float x) {
        return (boolean) WEAK_COMPARE_AND_SET_FLOAT.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetFloatAcquire(Object o, long offset, float expected, float x) {
        return (boolean) WEAK_COMPARE_AND_SET_FLOAT_ACQUIRE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetFloatRelease(Object o, long offset, float expected, float x) {
        return (boolean) WEAK_COMPARE_AND_SET_FLOAT_RELEASE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetFloatPlain(Object o, long offset, float expected, float x) {
        return (boolean) WEAK_COMPARE_AND_SET_FLOAT_PLAIN.invokeExact(o, offset, expected, x);
    }

    public float compareAndExchangeFloat(Object o, long offset, float expected, float x) {
        return (float) COMPARE_AND_EXCHANGE_FLOAT.invokeExact(o, offset, expected, x);
    }

    public float compareAndExchangeFloatAcquire(Object o, long offset, float expected, float x) {
        return (float) COMPARE_AND_EXCHANGE_FLOAT_ACQUIRE.invokeExact(o, offset, expected, x);
    }

    public float compareAndExchangeFloatRelease(Object o, long offset, float expected, float x) {
        return (float) COMPARE_AND_EXCHANGE_FLOAT_RELEASE.invokeExact(o, offset, expected, x);
    }

    public double getDouble(Object o, long offset) {
        return (double) GET_DOUBLE.invokeExact(o, offset);
    }

    public void putDouble(Object o, long offset, double x) {
        PUT_DOUBLE.invokeExact(o, offset, x);
    }

    public double getDoubleVolatile(Object o, long offset) {
        return (double) GET_DOUBLE_VOLATILE.invokeExact(o, offset);
    }

    public void putDoubleVolatile(Object o, long offset, double x) {
        PUT_DOUBLE_VOLATILE.invokeExact(o, offset, x);
    }

    public double getDoubleAcquire(Object o, long offset) {
        return (double) GET_DOUBLE_ACQUIRE.invokeExact(o, offset);
    }

    public void putDoubleRelease(Object o, long offset, double x) {
        PUT_DOUBLE_RELEASE.invokeExact(o, offset, x);
    }

    public double getDoubleOpaque(Object o, long offset) {
        return (double) GET_DOUBLE_OPAQUE.invokeExact(o, offset);
    }

    public void putDoubleOpaque(Object o, long offset, double x) {
        PUT_DOUBLE_OPAQUE.invokeExact(o, offset, x);
    }

    public boolean compareAndSetDouble(Object o, long offset, double expected, double x) {
        return (boolean) COMPARE_AND_SET_DOUBLE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetDouble(Object o, long offset, double expected, double x) {
        return (boolean) WEAK_COMPARE_AND_SET_DOUBLE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetDoubleAcquire(Object o, long offset, double expected, double x) {
        return (boolean) WEAK_COMPARE_AND_SET_DOUBLE_ACQUIRE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetDoubleRelease(Object o, long offset, double expected, double x) {
        return (boolean) WEAK_COMPARE_AND_SET_DOUBLE_RELEASE.invokeExact(o, offset, expected, x);
    }

    public boolean weakCompareAndSetDoublePlain(Object o, long offset, double expected, double x) {
        return (boolean) WEAK_COMPARE_AND_SET_DOUBLE_PLAIN.invokeExact(o, offset, expected, x);
    }

    public double compareAndExchangeDouble(Object o, long offset, double expected, double x) {
        return (double) COMPARE_AND_EXCHANGE_DOUBLE.invokeExact(o, offset, expected, x);
    }

    public double compareAndExchangeDoubleAcquire(Object o, long offset, double expected, double x) {
        return (double) COMPARE_AND_EXCHANGE_DOUBLE_ACQUIRE.invokeExact(o, offset, expected, x);
    }

    public double compareAndExchangeDoubleRelease(Object o, long offset, double expected, double x) {
        return (double) COMPARE_AND_EXCHANGE_DOUBLE_RELEASE.invokeExact(o, offset, expected, x);
    }

    public long allocateMemory(long bytes) {
        return (long) ALLOCATE_MEMORY.invokeExact(bytes);
    }

    public void freeMemory(long address) {
        FREE_MEMORY.invokeExact(address);
    }

    public long reallocateMemory(long address, long bytes) {
        return (long) REALLOCATE_MEMORY.invokeExact(address, bytes);
    }

    public void setMemory(long address, long bytes, byte value) {
        SET_MEMORY.invokeExact(address, bytes, value);
    }

    public void copyMemory(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes) {
        COPY_MEMORY.invokeExact(srcBase, srcOffset, destBase, destOffset, bytes);
    }
    public byte getByte(long address) {
        return (byte) GET_BYTE_ADDRESS.invokeExact(address);
    }

    public void putByte(long address, byte x) {
        PUT_BYTE_ADDRESS.invokeExact(address, x);
    }

    public short getShort(long address) {
        return (short) GET_SHORT_ADDRESS.invokeExact(address);
    }

    public void putShort(long address, short x) {
        PUT_SHORT_ADDRESS.invokeExact(address, x);
    }

    public char getChar(long address) {
        return (char) GET_CHAR_ADDRESS.invokeExact(address);
    }

    public void putChar(long address, char x) {
        PUT_CHAR_ADDRESS.invokeExact(address, x);
    }

    public int getInt(long address) {
        return (int) GET_INT_ADDRESS.invokeExact(address);
    }

    public void putInt(long address, int x) {
        PUT_INT_ADDRESS.invokeExact(address, x);
    }

    public long getLong(long address) {
        return (long) GET_LONG_ADDRESS.invokeExact(address);
    }

    public void putLong(long address, long value) {
        PUT_LONG_ADDRESS.invokeExact(address, value);
    }

    public float getFloat(long address) {
        return (float) GET_FLOAT_ADDRESS.invokeExact(address);
    }

    public void putFloat(long address, float x) {
        PUT_FLOAT_ADDRESS.invokeExact(address, x);
    }

    public double getDouble(long address) {
        return (double) GET_DOUBLE_ADDRESS.invokeExact(address);
    }

    public void putDouble(long address, double x) {
        PUT_DOUBLE_ADDRESS.invokeExact(address, x);
    }

    public long getAddress(long address) {
        return (long) GET_ADDRESS_ADDRESS.invokeExact(address);
    }

    public void putAddress(long address, long x) {
        PUT_ADDRESS_ADDRESS.invokeExact(address, x);
    }

    public void loadFence() {
        LOAD_FENCE.invokeExact();
    }

    public void storeFence() {
        STORE_FENCE.invokeExact();
    }

    public void fullFence() {
        FULL_FENCE.invokeExact();
    }

    public void park(boolean isAbsolute, long time) {
        PARK.invokeExact(isAbsolute, time);
    }

    public void unpark(Object thread) {
        UNPARK.invokeExact(thread);
    }

    public void throwException(Throwable ee) {
        THROW_EXCEPTION.invokeExact(ee);
    }

    private record UnsafeBinder(MethodHandles.Lookup lookup, Class<?> unsafeClass, Object unsafeInstance) {
        MethodHandle bind(String name, Class<?> rtype, Class<?>... ptypes) throws NoSuchMethodException, IllegalAccessException {
            return lookup.findVirtual(unsafeClass, name, MethodType.methodType(rtype, ptypes)).bindTo(unsafeInstance);
        }
    }
}
