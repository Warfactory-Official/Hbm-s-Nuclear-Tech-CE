package com.hbm.util;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SideOnly(Side.CLIENT)
public class ShaderHelper {

    private static Boolean optifinePresent = null;
    private static Class<?> shadersClass = null;
    private static Field shaderPackLoadedField = null;
    private static Method isShadowPassMethod = null;
    private static Field isShadowPassField = null;

    private static void init() {
        if (optifinePresent != null) return;

        try {
            shadersClass = Class.forName("net.optifine.shaders.Shaders");
            optifinePresent = true;

            try {
                shaderPackLoadedField = shadersClass.getDeclaredField("shaderPackLoaded");
                shaderPackLoadedField.setAccessible(true);
            } catch (NoSuchFieldException ignored) {}

            try {
                isShadowPassMethod = shadersClass.getDeclaredMethod("isShadowPass");
                isShadowPassMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                try {
                    isShadowPassField = shadersClass.getDeclaredField("isShadowPass");
                    isShadowPassField.setAccessible(true);
                } catch (NoSuchFieldException ignored) {}
            }
        } catch (ClassNotFoundException e) {
            optifinePresent = false;
        }
    }

    public static boolean isOptifinePresent() {
        init();
        return optifinePresent;
    }

    public static boolean areShadersActive() {
        init();
        if (!optifinePresent || shaderPackLoadedField == null) return false;

        try {
            return shaderPackLoadedField.getBoolean(null);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isShadowPass() {
        init();
        if (!optifinePresent) return false;

        try {
            if (isShadowPassMethod != null) {
                return (Boolean) isShadowPassMethod.invoke(null);
            }
            if (isShadowPassField != null) {
                return isShadowPassField.getBoolean(null);
            }
        } catch (Exception ignored) {}
        return false;
    }
}