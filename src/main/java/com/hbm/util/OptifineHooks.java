package com.hbm.util;

import com.hbm.core.ModPresence;
import com.hbm.interfaces.SuppressCheckedExceptions;
import com.hbm.lib.internal.MethodHandleHelper;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

@SideOnly(Side.CLIENT)
@SuppressCheckedExceptions
public final class OptifineHooks {

    private static final MethodHandle BEGIN_ADD_VERTEX;
    private static final MethodHandle BEGIN_ADD_VERTEX_DATA;
    private static final MethodHandle END_ADD_VERTEX_DATA;
    private static final MethodHandle TO_SINGLE_U;
    private static final MethodHandle TO_SINGLE_V;

    static {
        if (!ModPresence.OPTIFINE) {
            BEGIN_ADD_VERTEX = null;
            BEGIN_ADD_VERTEX_DATA = null;
            END_ADD_VERTEX_DATA = null;
            TO_SINGLE_U = null;
            TO_SINGLE_V = null;
        } else {
            try {
                Class<?> sVertexBuilderClass = Class.forName("net.optifine.shaders.SVertexBuilder");
                BEGIN_ADD_VERTEX = MethodHandleHelper.findStatic(sVertexBuilderClass, "beginAddVertex",
                        MethodType.methodType(void.class, BufferBuilder.class));
                BEGIN_ADD_VERTEX_DATA = MethodHandleHelper.findStatic(sVertexBuilderClass, "beginAddVertexData",
                        MethodType.methodType(void.class, BufferBuilder.class, int[].class));
                END_ADD_VERTEX_DATA = MethodHandleHelper.findStatic(sVertexBuilderClass, "endAddVertexData",
                        MethodType.methodType(void.class, BufferBuilder.class));
                Class<?> spriteClass = Class.forName("net.minecraft.client.renderer.texture.TextureAtlasSprite");
                TO_SINGLE_U = MethodHandleHelper.findVirtual(spriteClass, "toSingleU", MethodType.methodType(float.class, float.class));
                TO_SINGLE_V = MethodHandleHelper.findVirtual(spriteClass, "toSingleV", MethodType.methodType(float.class, float.class));
            } catch (ClassNotFoundException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    private OptifineHooks() {
    }

    public static void beginAddVertex(BufferBuilder bufferBuilder) {
        BEGIN_ADD_VERTEX.invokeExact(bufferBuilder);
    }

    public static void beginAddVertexData(BufferBuilder bufferBuilder, int[] vertexData) {
        BEGIN_ADD_VERTEX_DATA.invokeExact(bufferBuilder, vertexData);
    }

    public static void endAddVertexData(BufferBuilder bufferBuilder) {
        END_ADD_VERTEX_DATA.invokeExact(bufferBuilder);
    }

    public static float toSingleU(TextureAtlasSprite sprite, float u) {
        return (float) TO_SINGLE_U.invokeExact(sprite, u);
    }

    public static float toSingleV(TextureAtlasSprite sprite, float v) {
        return (float) TO_SINGLE_V.invokeExact(sprite, v);
    }
}
