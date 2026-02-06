package com.hbm.render.util;

import net.minecraft.client.renderer.BufferBuilder;

import java.nio.ByteOrder;

/**
 * Fast path for POSITION_COLOR writes that bypasses repeated pos/color/endVertex chaining.
 */
public final class PositionColorVertexWriter {
    private static final boolean NATIVE_LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
    private static final int INTS_PER_VERTEX = 4;
    public static final int QUAD_VERTEX_DATA_INTS = INTS_PER_VERTEX * 4;

    private PositionColorVertexWriter() {
    }

    public static int packRgbaColorToNativeInt(float red, float green, float blue, float alpha) {
        return packRgbaColorToNativeInt((int) (red * 255.0f), (int) (green * 255.0f), (int) (blue * 255.0f),
                (int) (alpha * 255.0f));
    }

    public static int packRgbaColorToNativeInt(int red, int green, int blue, int alpha) {
        int r = red & 255;
        int g = green & 255;
        int b = blue & 255;
        int a = alpha & 255;
        return NATIVE_LITTLE_ENDIAN
                ? (a << 24) | (b << 16) | (g << 8) | r
                : (r << 24) | (g << 16) | (b << 8) | a;
    }

    public static void appendPositionColorQuad(BufferBuilder buffer, int[] quadVertexData,
                                               double x0, double y0, double z0,
                                               double x1, double y1, double z1,
                                               double x2, double y2, double z2,
                                               double x3, double y3, double z3,
                                               int packedColor) {
        if (quadVertexData.length < QUAD_VERTEX_DATA_INTS) {
            throw new IllegalArgumentException("quadVertexData must have at least " + QUAD_VERTEX_DATA_INTS + " ints");
        }
        writePositionColorVertex(quadVertexData, 0, x0, y0, z0, packedColor);
        writePositionColorVertex(quadVertexData, 4, x1, y1, z1, packedColor);
        writePositionColorVertex(quadVertexData, 8, x2, y2, z2, packedColor);
        writePositionColorVertex(quadVertexData, 12, x3, y3, z3, packedColor);
        buffer.addVertexData(quadVertexData);
    }

    private static void writePositionColorVertex(int[] target, int offset, double x, double y, double z,
                                                 int packedColor) {
        target[offset] = Float.floatToRawIntBits((float) x);
        target[offset + 1] = Float.floatToRawIntBits((float) y);
        target[offset + 2] = Float.floatToRawIntBits((float) z);
        target[offset + 3] = packedColor;
    }
}
