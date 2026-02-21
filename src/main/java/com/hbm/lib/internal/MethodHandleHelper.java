package com.hbm.lib.internal;

import com.hbm.core.HbmCorePlugin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static com.hbm.lib.internal.AbstractUnsafe.IMPL_LOOKUP;

public final class MethodHandleHelper {

    private MethodHandleHelper() {
    }

    public static MethodHandles.Lookup lookup() {
        return IMPL_LOOKUP;
    }

    public static MethodHandle findVirtual(Class<?> owner, String mcp, String srg, MethodType type) {
        try {
            return IMPL_LOOKUP.findVirtual(owner, HbmCorePlugin.chooseName(mcp, srg), type);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle findStatic(Class<?> owner, String mcp, String srg, MethodType type) {
        try {
            return IMPL_LOOKUP.findStatic(owner, HbmCorePlugin.chooseName(mcp, srg), type);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle findSpecial(Class<?> owner, Class<?> specialCaller, String mcp, String srg, MethodType type) {
        try {
            return IMPL_LOOKUP.findSpecial(owner, HbmCorePlugin.chooseName(mcp, srg), type, specialCaller);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle findGetter(Class<?> owner, String mcp, String srg, Class<?> fieldType) {
        try {
            return IMPL_LOOKUP.findGetter(owner, HbmCorePlugin.chooseName(mcp, srg), fieldType);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle findStaticGetter(Class<?> owner, String mcp, String srg, Class<?> fieldType) {
        try {
            return IMPL_LOOKUP.findStaticGetter(owner, HbmCorePlugin.chooseName(mcp, srg), fieldType);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle findSetter(Class<?> owner, String mcp, String srg, Class<?> fieldType) {
        try {
            return IMPL_LOOKUP.findSetter(owner, HbmCorePlugin.chooseName(mcp, srg), fieldType);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle findStaticSetter(Class<?> owner, String mcp, String srg, Class<?> fieldType) {
        try {
            return IMPL_LOOKUP.findStaticSetter(owner, HbmCorePlugin.chooseName(mcp, srg), fieldType);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle findVirtual(Class<?> owner, String name, MethodType type) {
        try {
            return IMPL_LOOKUP.findVirtual(owner, name, type);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle findStatic(Class<?> owner, String name, MethodType type) {
        try {
            return IMPL_LOOKUP.findStatic(owner, name, type);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle findSpecial(Class<?> owner, Class<?> specialCaller, String name, MethodType type) {
        try {
            return IMPL_LOOKUP.findSpecial(owner, name, type, specialCaller);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle findGetter(Class<?> owner, String name, Class<?> fieldType) {
        try {
            return IMPL_LOOKUP.findGetter(owner, name, fieldType);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle findStaticGetter(Class<?> owner, String name, Class<?> fieldType) {
        try {
            return IMPL_LOOKUP.findStaticGetter(owner, name, fieldType);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle findSetter(Class<?> owner, String name, Class<?> fieldType) {
        try {
            return IMPL_LOOKUP.findSetter(owner, name, fieldType);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle findStaticSetter(Class<?> owner, String name, Class<?> fieldType) {
        try {
            return IMPL_LOOKUP.findStaticSetter(owner, name, fieldType);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle findConstructor(Class<?> owner, MethodType type) {
        try {
            return IMPL_LOOKUP.findConstructor(owner, type);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
