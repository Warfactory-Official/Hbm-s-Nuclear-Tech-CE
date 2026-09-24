package com.hbm.util;

import com.hbm.core.ModPresence;
import com.hbm.interfaces.SuppressCheckedExceptions;
import com.hbm.lib.internal.MethodHandleHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

@SideOnly(Side.CLIENT)
@SuppressCheckedExceptions
public class ShaderHelper {

    private static final MethodHandle SHADER_PACK_LOADED;
    private static final MethodHandle IS_SHADOW_PASS;
    private static final MethodHandle NEXT_BLOCK_ENTITY;
    private static final MethodHandle APPLY_HAND_DEPTH;
    private static final MethodHandle IS_SKIP_RENDER_HAND;

    static {
        if (ModPresence.OPTIFINE) {
            Class<?> shadersClass = Class.forName("net.optifine.shaders.Shaders");
            SHADER_PACK_LOADED = MethodHandleHelper.findStaticGetter(shadersClass, "shaderPackLoaded", boolean.class);
            IS_SHADOW_PASS = MethodHandleHelper.findStaticGetter(shadersClass, "isShadowPass", boolean.class);
            NEXT_BLOCK_ENTITY = MethodHandleHelper.findStatic(shadersClass, "nextBlockEntity",
                    MethodType.methodType(void.class, TileEntity.class));
            APPLY_HAND_DEPTH = MethodHandleHelper.findStatic(shadersClass, "applyHandDepth",
                    MethodType.methodType(void.class));
            IS_SKIP_RENDER_HAND = MethodHandleHelper.findStatic(shadersClass, "isSkipRenderHand",
                    MethodType.methodType(boolean.class, EnumHand.class));
        } else {
            SHADER_PACK_LOADED = null;
            IS_SHADOW_PASS = null;
            NEXT_BLOCK_ENTITY = null;
            APPLY_HAND_DEPTH = null;
            IS_SKIP_RENDER_HAND = null;
        }
    }

    public static boolean areShadersActive() {
        if (SHADER_PACK_LOADED == null) return false;
        return (boolean) SHADER_PACK_LOADED.invokeExact();
    }

    public static boolean isShadowPass() {
        if (IS_SHADOW_PASS == null) return false;
        return (boolean) IS_SHADOW_PASS.invokeExact();
    }

    public static void nextBlockEntity(TileEntity tileEntity) {
        if (NEXT_BLOCK_ENTITY == null) return;
        NEXT_BLOCK_ENTITY.invokeExact(tileEntity);
    }

    public static void applyHandDepth() {
        if (APPLY_HAND_DEPTH == null) return;
        APPLY_HAND_DEPTH.invokeExact();
    }

    public static boolean isSkipRenderHand(EnumHand hand) {
        if (IS_SKIP_RENDER_HAND == null) return false;
        return (boolean) IS_SKIP_RENDER_HAND.invokeExact(hand);
    }
}
