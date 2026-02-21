package com.hbm.lib.internal;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static com.hbm.lib.internal.AbstractUnsafe.IMPL_LOOKUP;
import static com.hbm.lib.internal.AbstractUnsafe.JPMS;

public final class InternalUtil {

    private static final MethodHandle LOAD_MODULE;
    private static final MethodHandle ADD_EXPORTS_ALL_UNNAMED;
    private static final MethodHandle ADD_OPENS_ALL_UNNAMED;
    private static final Object JAVA_BASE_MODULE;

    static {
        MethodHandle loadModule = null;
        MethodHandle addExportsAllUnnamed = null;
        MethodHandle addOpensAllUnnamed = null;
        Object javaBaseModule = null;
        try {
            if (JPMS) {
                Class<?> moduleClass = Class.forName("java.lang.Module");
                Class<?> modulesClass = Class.forName("jdk.internal.module.Modules");
                loadModule = IMPL_LOOKUP.findStatic(modulesClass, "loadModule", MethodType.methodType(moduleClass, String.class));
                MethodType moduleStringVoid = MethodType.methodType(void.class, moduleClass, String.class);
                addExportsAllUnnamed = IMPL_LOOKUP.findStatic(modulesClass, "addExportsToAllUnnamed", moduleStringVoid);
                addOpensAllUnnamed = IMPL_LOOKUP.findStatic(modulesClass, "addOpensToAllUnnamed", moduleStringVoid);
                MethodHandle getModule = IMPL_LOOKUP.findVirtual(Class.class, "getModule", MethodType.methodType(moduleClass));
                javaBaseModule = getModule.invoke(Object.class);
            }
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
        LOAD_MODULE = loadModule;
        ADD_EXPORTS_ALL_UNNAMED = addExportsAllUnnamed;
        ADD_OPENS_ALL_UNNAMED = addOpensAllUnnamed;
        JAVA_BASE_MODULE = javaBaseModule;
    }

    private InternalUtil() {
    }

    public static boolean loadModule(String moduleName) {
        if (LOAD_MODULE == null) return false;
        try {
            return LOAD_MODULE.invoke(moduleName) != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static <T extends Throwable> void addOpensToAllUnnamed(String pn) throws T {
        if (ADD_OPENS_ALL_UNNAMED == null || JAVA_BASE_MODULE == null) return;
        try {
            ADD_OPENS_ALL_UNNAMED.invoke(JAVA_BASE_MODULE, pn);
        } catch (Throwable t) {
            throw (T) t;
        }
    }

    public static <T extends Throwable> void addExportsToAllUnnamed(String pn) throws T {
        if (ADD_EXPORTS_ALL_UNNAMED == null || JAVA_BASE_MODULE == null) return;
        try {
            ADD_EXPORTS_ALL_UNNAMED.invoke(JAVA_BASE_MODULE, pn);
        } catch (Throwable t) {
            throw (T) t;
        }
    }
}
