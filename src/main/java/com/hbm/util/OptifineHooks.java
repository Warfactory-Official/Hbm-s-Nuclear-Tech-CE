package com.hbm.util;

import com.hbm.core.ModPresence;
import com.hbm.interfaces.SuppressCheckedExceptions;
import com.hbm.lib.internal.MethodHandleHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
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
    private static final MethodHandle PUSH_ENTITY;
    private static final MethodHandle POP_ENTITY;
    private static final MethodHandle SET_SPRITE;
    private static final MethodHandle SET_BLOCK_LAYER;

    static {
        if (!ModPresence.OPTIFINE) {
            BEGIN_ADD_VERTEX = null;
            BEGIN_ADD_VERTEX_DATA = null;
            END_ADD_VERTEX_DATA = null;
            TO_SINGLE_U = null;
            TO_SINGLE_V = null;
            PUSH_ENTITY = null;
            POP_ENTITY = null;
            SET_SPRITE = null;
            SET_BLOCK_LAYER = null;
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
                PUSH_ENTITY = MethodHandleHelper.findStatic(sVertexBuilderClass, "pushEntity",
                        MethodType.methodType(void.class, IBlockState.class, BlockPos.class, IBlockAccess.class, BufferBuilder.class));
                POP_ENTITY = MethodHandleHelper.findStatic(sVertexBuilderClass, "popEntity",
                        MethodType.methodType(void.class, BufferBuilder.class));
                SET_SPRITE = MethodHandleHelper.findVirtual(BufferBuilder.class, "setSprite",
                        MethodType.methodType(void.class, TextureAtlasSprite.class));
                SET_BLOCK_LAYER = MethodHandleHelper.findVirtual(BufferBuilder.class, "setBlockLayer",
                        MethodType.methodType(void.class, BlockRenderLayer.class));
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

    public static void pushEntity(IBlockState state, BlockPos pos, IBlockAccess world, BufferBuilder bufferBuilder) {
        PUSH_ENTITY.invokeExact(state, pos, world, bufferBuilder);
    }

    public static void popEntity(BufferBuilder bufferBuilder) {
        POP_ENTITY.invokeExact(bufferBuilder);
    }

    public static void setSprite(BufferBuilder bufferBuilder, TextureAtlasSprite sprite) {
        SET_SPRITE.invokeExact(bufferBuilder, sprite);
    }

    public static void setBlockLayer(BufferBuilder bufferBuilder, BlockRenderLayer layer) {
        SET_BLOCK_LAYER.invokeExact(bufferBuilder, layer);
    }
}
