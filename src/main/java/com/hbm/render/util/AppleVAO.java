package com.hbm.render.util;

import com.hbm.interfaces.SuppressCheckedExceptions;
import com.hbm.main.MainRegistry;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import static com.hbm.lib.internal.UnsafeHolder.U;
import static com.hbm.lib.internal.UnsafeHolder.fieldOffset;

@SuppressCheckedExceptions
public final class AppleVAO {
    private static final long ADDR_OFF = fieldOffset(Buffer.class, "address");
    private static final long POS_OFF = fieldOffset(Buffer.class, "position");
    private static final IntBuffer TMP = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
    private static final long P_TMP = memAddress(TMP);
    private static MethodHandle mh_glBindVertexArrayAPPLE;   // (int)void
    private static MethodHandle mh_glGenVertexArraysAPPLE;   // (int, long)void
    private static MethodHandle mh_glDeleteVertexArraysAPPLE;// (int, long)void
    private static MethodHandle mh_glIsVertexArrayAPPLE;     // (int)boolean

    private AppleVAO() {
    }

    public static long memAddress(IntBuffer b) {
        long base = U.getLong(b, ADDR_OFF);
        int pos = U.getInt(b, POS_OFF);
        return base + (((long) pos & 0xFFFF_FFFFL) << 2);
    }

    public static boolean init() {
        try {
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();

            Class<?> cGL = Class.forName("org.lwjgl.opengl.GL");
            Class<?> cFP = Class.forName("org.lwjgl.system.FunctionProvider");
            Class<?> cJNI = Class.forName("org.lwjgl.system.JNI");

            MethodHandle mh_getFunctionProvider = lookup.findStatic(cGL, "getFunctionProvider", MethodType.methodType(cFP)).asType(MethodType.methodType(Object.class));
            MethodHandle mh_getFunctionAddress = lookup.findVirtual(cFP, "getFunctionAddress", MethodType.methodType(long.class, CharSequence.class)).asType(MethodType.methodType(long.class, Object.class, CharSequence.class));

            MethodHandle mh_invokeV = lookup.findStatic(cJNI, "invokeV", MethodType.methodType(void.class, int.class, long.class));
            MethodHandle mh_invokePV = lookup.findStatic(cJNI, "invokePV", MethodType.methodType(void.class, int.class, long.class, long.class));
            MethodHandle mh_invokeZ = lookup.findStatic(cJNI, "invokeZ", MethodType.methodType(boolean.class, int.class, long.class));

            Object fp = (Object) mh_getFunctionProvider.invokeExact();
            if (fp == null) {
                MainRegistry.logger.error("[AppleVAO] Failed to get FunctionProvider");
                return false;
            }

            long p_glBindVertexArrayAPPLE = getFunctionPointer(mh_getFunctionAddress, fp, "glBindVertexArrayAPPLE");
            long p_glGenVertexArraysAPPLE = getFunctionPointer(mh_getFunctionAddress, fp, "glGenVertexArraysAPPLE");
            long p_glDeleteVertexArraysAPPLE = getFunctionPointer(mh_getFunctionAddress, fp, "glDeleteVertexArraysAPPLE");
            long p_glIsVertexArrayAPPLE = getFunctionPointer(mh_getFunctionAddress, fp, "glIsVertexArrayAPPLE");

            boolean ok = p_glBindVertexArrayAPPLE != 0L && p_glGenVertexArraysAPPLE != 0L && p_glDeleteVertexArraysAPPLE != 0L && p_glIsVertexArrayAPPLE != 0L;
            if (!ok) return false;

            mh_glBindVertexArrayAPPLE = MethodHandles.insertArguments(mh_invokeV, 1, p_glBindVertexArrayAPPLE).asType(MethodType.methodType(void.class, int.class));
            mh_glGenVertexArraysAPPLE = MethodHandles.insertArguments(mh_invokePV, 2, p_glGenVertexArraysAPPLE).asType(MethodType.methodType(void.class, int.class, long.class));
            mh_glDeleteVertexArraysAPPLE = MethodHandles.insertArguments(mh_invokePV, 2, p_glDeleteVertexArraysAPPLE).asType(MethodType.methodType(void.class, int.class, long.class));
            mh_glIsVertexArrayAPPLE = MethodHandles.insertArguments(mh_invokeZ, 1, p_glIsVertexArrayAPPLE).asType(MethodType.methodType(boolean.class, int.class));
            return true;
        } catch (Throwable t) {
            MainRegistry.logger.catching(t);
            return false;
        }
    }

    private static long getFunctionPointer(MethodHandle getFuncAddrMH, Object fp, String name) throws Throwable {
        long addr = (long) getFuncAddrMH.invokeExact(fp, (CharSequence) name);
        if (addr == 0L) MainRegistry.logger.error("[AppleVAO] Failed to get function pointer of {}", name);
        return addr;
    }

    public static int glGenVertexArraysAPPLE() {
        TMP.put(0, 0);
        mh_glGenVertexArraysAPPLE.invokeExact(1, P_TMP);
        return TMP.get(0);
    }

    public static void glGenVertexArraysAPPLE(IntBuffer arrays) {
        mh_glGenVertexArraysAPPLE.invokeExact(arrays.remaining(), memAddress(arrays));
    }

    public static void glBindVertexArrayAPPLE(int vao) {
        mh_glBindVertexArrayAPPLE.invokeExact(vao);
    }

    public static void glDeleteVertexArraysAPPLE(int vao) {
        TMP.put(0, vao);
        mh_glDeleteVertexArraysAPPLE.invokeExact(1, P_TMP);
    }

    public static void glDeleteVertexArraysAPPLE(IntBuffer arrays) {
        mh_glDeleteVertexArraysAPPLE.invokeExact(arrays.remaining(), memAddress(arrays));
    }

    public static boolean glIsVertexArrayAPPLE(int vao) {
        return (boolean) mh_glIsVertexArrayAPPLE.invokeExact(vao);
    }
}
