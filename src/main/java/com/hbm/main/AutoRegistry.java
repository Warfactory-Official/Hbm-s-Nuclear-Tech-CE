package com.hbm.main;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.hbm.config.MachineDynConfig;
import com.hbm.tileentity.IConfigurableMachine;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

public final class AutoRegistry {
    public static final List<Class<? extends IConfigurableMachine>> configurableMachineClasses = new ArrayList<>();
    private static final String GENERATED_REGISTRAR_CLASS = "com.hbm.generated.GeneratedHBMRegistrar";
    private static final MethodHandle registerEntitiesHandle;
    private static final MethodHandle registerTileEntitiesHandle;
    private static final MethodHandle registerItemRenderersHandle;
    private static final MethodHandle registerEntityRenderersHandle;
    private static final MethodHandle registerTileEntityRenderersHandle;
    private static final MethodHandle configurableMachineGetterHandle;

    static {
        Class<?> registrarClass;
        try {
             registrarClass = Class.forName(GENERATED_REGISTRAR_CLASS);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not find the generated registrar class '" + GENERATED_REGISTRAR_CLASS + "'. Did the annotation " +
                    "processor run correctly?", e);
        }
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        registerEntitiesHandle = findMethodHandle(lookup, registrarClass, "registerEntities", MethodType.methodType(int.class, int.class));
        registerTileEntitiesHandle = findMethodHandle(lookup, registrarClass, "registerTileEntities", MethodType.methodType(void.class));
        registerItemRenderersHandle = findMethodHandle(lookup, registrarClass, "registerItemRenderers", MethodType.methodType(void.class));
        registerEntityRenderersHandle = findMethodHandle(lookup, registrarClass, "registerEntityRenderers", MethodType.methodType(void.class));
        registerTileEntityRenderersHandle = findMethodHandle(lookup, registrarClass, "registerTileEntityRenderers", MethodType.methodType(void.class));
        try {
            configurableMachineGetterHandle = lookup.findStaticGetter(registrarClass, "CONFIGURABLE_MACHINES", List.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Could not find getter for static field 'CONFIGURABLE_MACHINES' in generated registrar.", e);
        }
    }

    @CanIgnoreReturnValue
    static int registerEntities(int startId) {
        if (registerEntitiesHandle == null) {
            MainRegistry.logger.debug("Registration method 'registerEntities(int)' not found. Skipping entity registration.");
            return startId;
        }
        try {
            int nextId = (int) registerEntitiesHandle.invokeExact(startId);
            MainRegistry.logger.debug("Entity registration complete. Next available ID is now: {}", nextId);
            return nextId;
        } catch (Throwable e) {
            throw new RuntimeException("Failed to invoke 'registerEntities' from generated registrar.", e);
        }
    }

    static void registerTileEntities() {
        invokeRegistrationMethod("registerTileEntities", registerTileEntitiesHandle);
    }

    static void preInitClient() {
        invokeRegistrationMethod("registerItemRenderers", registerItemRenderersHandle);
    }

    @SideOnly(Side.CLIENT)
    static void registerRenderInfo() {
        invokeRegistrationMethod("registerEntityRenderers", registerEntityRenderersHandle);
        invokeRegistrationMethod("registerTileEntityRenderers", registerTileEntityRenderersHandle);
    }

    static void loadAuxiliaryData() {
        MainRegistry.logger.debug("Loading auxiliary registration data...");
        try {
            //noinspection unchecked
            List<Class<? extends IConfigurableMachine>> foundClasses = (List<Class<? extends IConfigurableMachine>>) configurableMachineGetterHandle.invokeExact();
            configurableMachineClasses.addAll(foundClasses);
            MachineDynConfig.initialize();
            MainRegistry.logger.debug("Successfully loaded {} configurable machine classes.", configurableMachineClasses.size());
        } catch (Throwable e) {
            throw new RuntimeException("Failed to load configurable machine data from generated registrar.", e);
        }
    }

    private static void invokeRegistrationMethod(String methodName, MethodHandle handle) {
        if (handle == null) {
            MainRegistry.logger.debug("Registration method '{}' not found. Skipping registration.", methodName);
            return;
        }
        try {
            handle.invokeExact();
            MainRegistry.logger.debug("Successfully invoked registration method: {}", methodName);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to invoke registration method '" + methodName + "' from generated registrar.", e);
        }
    }

    private static MethodHandle findMethodHandle(MethodHandles.Lookup lookup, Class<?> registrarClass, String methodName, MethodType methodType) {
        try {
            return lookup.findStatic(registrarClass, methodName, methodType);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Method '" + methodName + "' is not public? How is that supposed to happen?", e);
        }
    }
}
