package com.hbm.render.util;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.nio.ByteOrder;
import java.nio.IntBuffer;

@SideOnly(Side.CLIENT)
public interface NTMBufferBuilder {

    boolean NATIVE_LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
    int POSITION_COLOR_INTS_PER_VERTEX = 4;
    int POSITION_COLOR_QUAD_INTS = POSITION_COLOR_INTS_PER_VERTEX * 4;

    default BufferBuilder vanilla() {
        return (BufferBuilder) this;
    }

    default void beginFast(int drawMode, VertexFormat format, int expectedVertices) {
        BufferBuilder self = vanilla();
        self.begin(drawMode, format);
        self.rawIntBuffer.clear();
        reserveVertices(expectedVertices);
    }

    default NTMFastVertexFormat getFastFormat() {
        return NTMFastVertexFormat.from(vanilla().getVertexFormat());
    }

    default void reserveVertices(int expectedVertices) {
        if (expectedVertices > 0) {
            reserveAdditionalBytes(expectedVertices * vanilla().getVertexFormat().getSize());
        }
    }

    default void reserveAdditionalBytes(int additionalBytes) {
        if (additionalBytes > 0) {
            vanilla().growBuffer(additionalBytes);
        }
    }

    default void appendRawVertexData(int[] data, int intsPerVertex, NTMFastVertexFormat requiredFormat) {
        if (data == null || data.length == 0) return;
        if (intsPerVertex <= 0 || data.length % intsPerVertex != 0) {
            throw new IllegalArgumentException("Invalid raw vertex payload length " + (data == null ? 0 : data.length) + " for stride " + intsPerVertex);
        }

        ensureDrawing(requiredFormat);
        BufferBuilder self = vanilla();
        if (!hasRemainingInts(data.length)) {
            self.growBuffer(data.length * Integer.BYTES);
        }
        self.rawIntBuffer.position(self.vertexCount * self.getVertexFormat().getIntegerSize());
        self.rawIntBuffer.put(data);
        self.vertexCount += data.length / intsPerVertex;
    }

    default void reservePositionColorQuads(int quadCount) {
        ensureDrawing(NTMFastVertexFormat.POSITION_COLOR);
        if (quadCount > 0) {
            vanilla().growBuffer(quadCount * POSITION_COLOR_QUAD_INTS * Integer.BYTES);
        }
    }

    default void appendPosition(double x, double y, double z) {
        ensureDrawing(NTMFastVertexFormat.POSITION);
        if (!hasRemainingInts(3)) {
            vanilla().growBuffer(3 * Integer.BYTES);
        }
        appendPositionUnchecked(x, y, z);
    }

    default void appendPositionColor(double x, double y, double z, int packedColor) {
        ensureDrawing(NTMFastVertexFormat.POSITION_COLOR);
        if (!hasRemainingInts(POSITION_COLOR_INTS_PER_VERTEX)) {
            vanilla().growBuffer(POSITION_COLOR_INTS_PER_VERTEX * Integer.BYTES);
        }
        appendPositionColorUnchecked(x, y, z, packedColor);
    }

    default void appendPositionColorQuad(double x0, double y0, double z0,
                                         double x1, double y1, double z1,
                                         double x2, double y2, double z2,
                                         double x3, double y3, double z3,
                                         int packedColor) {
        ensureDrawing(NTMFastVertexFormat.POSITION_COLOR);
        if (!hasRemainingInts(POSITION_COLOR_QUAD_INTS)) {
            vanilla().growBuffer(POSITION_COLOR_QUAD_INTS * Integer.BYTES);
        }
        appendPositionColorQuadUnchecked(x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, packedColor);
    }

    default void appendPositionTex(double x, double y, double z, double u, double v) {
        ensureDrawing(NTMFastVertexFormat.POSITION_TEX);
        if (!hasRemainingInts(5)) {
            vanilla().growBuffer(5 * Integer.BYTES);
        }
        appendPositionTexUnchecked(x, y, z, u, v);
    }

    default void appendPositionTexColor(double x, double y, double z, double u, double v, int packedColor) {
        ensureDrawing(NTMFastVertexFormat.POSITION_TEX_COLOR);
        if (!hasRemainingInts(6)) {
            vanilla().growBuffer(6 * Integer.BYTES);
        }
        appendPositionTexColorUnchecked(x, y, z, u, v, packedColor);
    }

    default void appendPositionTexNormal(double x, double y, double z, double u, double v, int packedNormal) {
        ensureDrawing(NTMFastVertexFormat.POSITION_TEX_NORMAL);
        if (!hasRemainingInts(6)) {
            vanilla().growBuffer(6 * Integer.BYTES);
        }
        appendPositionTexNormalUnchecked(x, y, z, u, v, packedNormal);
    }

    default void appendPositionTexLmapColor(double x, double y, double z, double u, double v, int packedLightmap,
                                            int packedColor) {
        ensureDrawing(NTMFastVertexFormat.POSITION_TEX_LMAP_COLOR);
        if (!hasRemainingInts(7)) {
            vanilla().growBuffer(7 * Integer.BYTES);
        }
        appendPositionTexLmapColorUnchecked(x, y, z, u, v, packedLightmap, packedColor);
    }

    default void appendPositionTexColorNormal(double x, double y, double z, double u, double v, int packedColor,
                                              int packedNormal) {
        ensureDrawing(NTMFastVertexFormat.POSITION_TEX_COLOR_NORMAL);
        if (!hasRemainingInts(7)) {
            vanilla().growBuffer(7 * Integer.BYTES);
        }
        appendPositionTexColorNormalUnchecked(x, y, z, u, v, packedColor, packedNormal);
    }

    default void appendPositionNormal(double x, double y, double z, int packedNormal) {
        ensureDrawing(NTMFastVertexFormat.POSITION_NORMAL);
        if (!hasRemainingInts(4)) {
            vanilla().growBuffer(4 * Integer.BYTES);
        }
        appendPositionNormalUnchecked(x, y, z, packedNormal);
    }

    default void appendParticlePositionTexColorLmap(double x, double y, double z, double u, double v, int packedColor,
                                                    int packedLightmap) {
        ensureDrawing(NTMFastVertexFormat.PARTICLE_POSITION_TEX_COLOR_LMAP);
        if (!hasRemainingInts(7)) {
            vanilla().growBuffer(7 * Integer.BYTES);
        }
        appendParticlePositionTexColorLmapUnchecked(x, y, z, u, v, packedColor, packedLightmap);
    }

    default void appendPositionUnchecked(double x, double y, double z) {
        BufferBuilder self = vanilla();
        int base = self.vertexCount * 3;
        IntBuffer target = self.rawIntBuffer;
        target.put(base, Float.floatToRawIntBits(applyXOffset(x)));
        target.put(base + 1, Float.floatToRawIntBits(applyYOffset(y)));
        target.put(base + 2, Float.floatToRawIntBits(applyZOffset(z)));
        self.vertexCount++;
    }

    default void appendPositionQuadUnchecked(double x0, double y0, double z0,
                                             double x1, double y1, double z1,
                                             double x2, double y2, double z2,
                                             double x3, double y3, double z3) {
        appendPositionUnchecked(x0, y0, z0);
        appendPositionUnchecked(x1, y1, z1);
        appendPositionUnchecked(x2, y2, z2);
        appendPositionUnchecked(x3, y3, z3);
    }

    default void appendPositionColorUnchecked(double x, double y, double z, int packedColor) {
        BufferBuilder self = vanilla();
        int base = self.vertexCount * POSITION_COLOR_INTS_PER_VERTEX;
        IntBuffer target = self.rawIntBuffer;
        target.put(base, Float.floatToRawIntBits(applyXOffset(x)));
        target.put(base + 1, Float.floatToRawIntBits(applyYOffset(y)));
        target.put(base + 2, Float.floatToRawIntBits(applyZOffset(z)));
        target.put(base + 3, packedColor);
        self.vertexCount++;
    }

    default void appendPositionColorQuadUnchecked(double x0, double y0, double z0,
                                                  double x1, double y1, double z1,
                                                  double x2, double y2, double z2,
                                                  double x3, double y3, double z3,
                                                  int packedColor) {
        BufferBuilder self = vanilla();
        int base = self.vertexCount * POSITION_COLOR_INTS_PER_VERTEX;
        IntBuffer target = self.rawIntBuffer;
        writePositionColorVertex(target, base, applyXOffset(x0), applyYOffset(y0), applyZOffset(z0), packedColor);
        writePositionColorVertex(target, base + POSITION_COLOR_INTS_PER_VERTEX, applyXOffset(x1), applyYOffset(y1), applyZOffset(z1), packedColor);
        writePositionColorVertex(target, base + POSITION_COLOR_INTS_PER_VERTEX * 2, applyXOffset(x2), applyYOffset(y2), applyZOffset(z2), packedColor);
        writePositionColorVertex(target, base + POSITION_COLOR_INTS_PER_VERTEX * 3, applyXOffset(x3), applyYOffset(y3), applyZOffset(z3), packedColor);
        self.vertexCount += 4;
    }

    default void appendPositionTexUnchecked(double x, double y, double z, double u, double v) {
        BufferBuilder self = vanilla();
        int base = self.vertexCount * 5;
        IntBuffer target = self.rawIntBuffer;
        target.put(base, Float.floatToRawIntBits(applyXOffset(x)));
        target.put(base + 1, Float.floatToRawIntBits(applyYOffset(y)));
        target.put(base + 2, Float.floatToRawIntBits(applyZOffset(z)));
        target.put(base + 3, Float.floatToRawIntBits((float) u));
        target.put(base + 4, Float.floatToRawIntBits((float) v));
        self.vertexCount++;
    }

    default void appendPositionTexQuadUnchecked(double x0, double y0, double z0, double u0, double v0,
                                                double x1, double y1, double z1, double u1, double v1,
                                                double x2, double y2, double z2, double u2, double v2,
                                                double x3, double y3, double z3, double u3, double v3) {
        appendPositionTexUnchecked(x0, y0, z0, u0, v0);
        appendPositionTexUnchecked(x1, y1, z1, u1, v1);
        appendPositionTexUnchecked(x2, y2, z2, u2, v2);
        appendPositionTexUnchecked(x3, y3, z3, u3, v3);
    }

    default void appendPositionTexColorUnchecked(double x, double y, double z, double u, double v, int packedColor) {
        BufferBuilder self = vanilla();
        int base = self.vertexCount * 6;
        IntBuffer target = self.rawIntBuffer;
        target.put(base, Float.floatToRawIntBits(applyXOffset(x)));
        target.put(base + 1, Float.floatToRawIntBits(applyYOffset(y)));
        target.put(base + 2, Float.floatToRawIntBits(applyZOffset(z)));
        target.put(base + 3, Float.floatToRawIntBits((float) u));
        target.put(base + 4, Float.floatToRawIntBits((float) v));
        target.put(base + 5, packedColor);
        self.vertexCount++;
    }

    default void appendPositionTexNormalUnchecked(double x, double y, double z, double u, double v, int packedNormal) {
        BufferBuilder self = vanilla();
        int base = self.vertexCount * 6;
        IntBuffer target = self.rawIntBuffer;
        target.put(base, Float.floatToRawIntBits(applyXOffset(x)));
        target.put(base + 1, Float.floatToRawIntBits(applyYOffset(y)));
        target.put(base + 2, Float.floatToRawIntBits(applyZOffset(z)));
        target.put(base + 3, Float.floatToRawIntBits((float) u));
        target.put(base + 4, Float.floatToRawIntBits((float) v));
        target.put(base + 5, packedNormal);
        self.vertexCount++;
    }

    default void appendPositionTexLmapColorUnchecked(double x, double y, double z, double u, double v,
                                                     int packedLightmap, int packedColor) {
        BufferBuilder self = vanilla();
        int base = self.vertexCount * 7;
        IntBuffer target = self.rawIntBuffer;
        target.put(base, Float.floatToRawIntBits(applyXOffset(x)));
        target.put(base + 1, Float.floatToRawIntBits(applyYOffset(y)));
        target.put(base + 2, Float.floatToRawIntBits(applyZOffset(z)));
        target.put(base + 3, Float.floatToRawIntBits((float) u));
        target.put(base + 4, Float.floatToRawIntBits((float) v));
        target.put(base + 5, packedLightmap);
        target.put(base + 6, packedColor);
        self.vertexCount++;
    }

    default void appendPositionTexColorNormalUnchecked(double x, double y, double z, double u, double v, int packedColor,
                                                       int packedNormal) {
        BufferBuilder self = vanilla();
        int base = self.vertexCount * 7;
        IntBuffer target = self.rawIntBuffer;
        target.put(base, Float.floatToRawIntBits(applyXOffset(x)));
        target.put(base + 1, Float.floatToRawIntBits(applyYOffset(y)));
        target.put(base + 2, Float.floatToRawIntBits(applyZOffset(z)));
        target.put(base + 3, Float.floatToRawIntBits((float) u));
        target.put(base + 4, Float.floatToRawIntBits((float) v));
        target.put(base + 5, packedColor);
        target.put(base + 6, packedNormal);
        self.vertexCount++;
    }

    default void appendPositionTexColorQuadUnchecked(double x0, double y0, double z0, double u0, double v0, int c0,
                                                     double x1, double y1, double z1, double u1, double v1, int c1,
                                                     double x2, double y2, double z2, double u2, double v2, int c2,
                                                     double x3, double y3, double z3, double u3, double v3, int c3) {
        appendPositionTexColorUnchecked(x0, y0, z0, u0, v0, c0);
        appendPositionTexColorUnchecked(x1, y1, z1, u1, v1, c1);
        appendPositionTexColorUnchecked(x2, y2, z2, u2, v2, c2);
        appendPositionTexColorUnchecked(x3, y3, z3, u3, v3, c3);
    }

    default void appendPositionNormalUnchecked(double x, double y, double z, int packedNormal) {
        BufferBuilder self = vanilla();
        int base = self.vertexCount * 4;
        IntBuffer target = self.rawIntBuffer;
        target.put(base, Float.floatToRawIntBits(applyXOffset(x)));
        target.put(base + 1, Float.floatToRawIntBits(applyYOffset(y)));
        target.put(base + 2, Float.floatToRawIntBits(applyZOffset(z)));
        target.put(base + 3, packedNormal);
        self.vertexCount++;
    }

    default void appendParticlePositionTexColorLmapUnchecked(double x, double y, double z, double u, double v,
                                                             int packedColor, int packedLightmap) {
        BufferBuilder self = vanilla();
        int base = self.vertexCount * 7;
        IntBuffer target = self.rawIntBuffer;
        target.put(base, Float.floatToRawIntBits(applyXOffset(x)));
        target.put(base + 1, Float.floatToRawIntBits(applyYOffset(y)));
        target.put(base + 2, Float.floatToRawIntBits(applyZOffset(z)));
        target.put(base + 3, Float.floatToRawIntBits((float) u));
        target.put(base + 4, Float.floatToRawIntBits((float) v));
        target.put(base + 5, packedColor);
        target.put(base + 6, packedLightmap);
        self.vertexCount++;
    }

    default boolean hasRemainingInts(int additionalInts) {
        BufferBuilder self = vanilla();
        return self.vertexCount * self.getVertexFormat().getIntegerSize() + additionalInts <= self.rawIntBuffer.capacity();
    }

    default void ensureDrawing(NTMFastVertexFormat requiredFormat) {
        BufferBuilder self = vanilla();
        if (!self.isDrawing) {
            throw new IllegalStateException("Not building!");
        }
        NTMFastVertexFormat actualFormat = getFastFormat();
        if (actualFormat != requiredFormat) {
            throw new IllegalStateException("Expected " + requiredFormat + ", got " + actualFormat);
        }
    }

    default float applyXOffset(double x) {
        return (float) (x + vanilla().xOffset);
    }

    default float applyYOffset(double y) {
        return (float) (y + vanilla().yOffset);
    }

    default float applyZOffset(double z) {
        return (float) (z + vanilla().zOffset);
    }

    static void writePositionColorVertex(IntBuffer target, int offset, double x, double y, double z, int packedColor) {
        target.put(offset, Float.floatToRawIntBits((float) x));
        target.put(offset + 1, Float.floatToRawIntBits((float) y));
        target.put(offset + 2, Float.floatToRawIntBits((float) z));
        target.put(offset + 3, packedColor);
    }

    static int packColor(int red, int green, int blue, int alpha) {
        int r = red & 255;
        int g = green & 255;
        int b = blue & 255;
        int a = alpha & 255;
        return NATIVE_LITTLE_ENDIAN ? (a << 24) | (b << 16) | (g << 8) | r : (r << 24) | (g << 16) | (b << 8) | a;
    }

    static int packColor(float red, float green, float blue, float alpha) {
        return packColor((int) (red * 255.0f), (int) (green * 255.0f), (int) (blue * 255.0f), (int) (alpha * 255.0f));
    }

    static int packNormal(float x, float y, float z) {
        int nx = (byte) ((int) (x * 127.0F)) & 255;
        int ny = (byte) ((int) (y * 127.0F)) & 255;
        int nz = (byte) ((int) (z * 127.0F)) & 255;
        return NATIVE_LITTLE_ENDIAN ? (nz << 16) | (ny << 8) | nx : (nx << 24) | (ny << 16) | (nz << 8);
    }

    static int packLightmap(int skyLight, int blockLight) {
        int block = blockLight & 0xFFFF;
        int sky = skyLight & 0xFFFF;
        return NATIVE_LITTLE_ENDIAN ? (sky << 16) | block : (block << 16) | sky;
    }

    enum NTMFastVertexFormat {
        GENERIC,
        BLOCK,
        ITEM,
        POSITION,
        POSITION_COLOR,
        POSITION_TEX,
        POSITION_NORMAL,
        POSITION_TEX_COLOR,
        POSITION_TEX_NORMAL,
        POSITION_TEX_LMAP_COLOR,
        POSITION_TEX_COLOR_NORMAL,
        PARTICLE_POSITION_TEX_COLOR_LMAP;

        public static NTMFastVertexFormat from(VertexFormat format) {
            if (format == null) return GENERIC;
            if (format == DefaultVertexFormats.BLOCK || DefaultVertexFormats.BLOCK.equals(format)) return BLOCK;
            if (format == DefaultVertexFormats.ITEM || DefaultVertexFormats.ITEM.equals(format)) return ITEM;
            if (format == DefaultVertexFormats.POSITION || DefaultVertexFormats.POSITION.equals(format)) return POSITION;
            if (format == DefaultVertexFormats.POSITION_COLOR || DefaultVertexFormats.POSITION_COLOR.equals(format)) return POSITION_COLOR;
            if (format == DefaultVertexFormats.POSITION_TEX || DefaultVertexFormats.POSITION_TEX.equals(format)) return POSITION_TEX;
            if (format == DefaultVertexFormats.POSITION_NORMAL || DefaultVertexFormats.POSITION_NORMAL.equals(format)) return POSITION_NORMAL;
            if (format == DefaultVertexFormats.POSITION_TEX_COLOR || DefaultVertexFormats.POSITION_TEX_COLOR.equals(format)) return POSITION_TEX_COLOR;
            if (format == DefaultVertexFormats.POSITION_TEX_NORMAL || DefaultVertexFormats.POSITION_TEX_NORMAL.equals(format)) return POSITION_TEX_NORMAL;
            if (format == DefaultVertexFormats.POSITION_TEX_LMAP_COLOR || DefaultVertexFormats.POSITION_TEX_LMAP_COLOR.equals(format)) return POSITION_TEX_LMAP_COLOR;
            if (format == DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL || DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL.equals(format)) return POSITION_TEX_COLOR_NORMAL;
            if (format == DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP || DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP.equals(format)) return PARTICLE_POSITION_TEX_COLOR_LMAP;
            return GENERIC;
        }
    }
}
