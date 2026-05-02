package com.hbm.render.util;

import com.hbm.lib.internal.UnsafeHolder;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.nio.Buffer;

import static com.hbm.lib.internal.UnsafeHolder.U;

@SideOnly(Side.CLIENT)
public interface NTMBufferBuilder {

    int POSITION_COLOR_INTS_PER_VERTEX = 4;
    int POSITION_COLOR_QUAD_INTS = POSITION_COLOR_INTS_PER_VERTEX * 4;
    long BUFFER_ADDRESS_OFFSET = UnsafeHolder.fieldOffset(Buffer.class, "address");
    int PARTICLE_POSITION_TEX_COLOR_LMAP_STRIDE = 7 * Integer.BYTES;
    int PARTICLE_POSITION_TEX_COLOR_LMAP_QUAD_BYTES = PARTICLE_POSITION_TEX_COLOR_LMAP_STRIDE * 4;

    void beginFast(int drawMode, VertexFormat format, int expectedVertices);

    default BufferBuilder vanilla() {
        return (BufferBuilder) this;
    }

    void appendRawVertexData(int[] data, int intsPerVertex, VertexFormat requiredFormat);

    void reservePositionColorQuads(int quadCount);

    void appendPosition(float x, float y, float z);

    void appendPositionColor(float x, float y, float z, int packedColor);

    void appendPositionColorQuad(float x0, float y0, float z0,
                                 float x1, float y1, float z1,
                                 float x2, float y2, float z2,
                                 float x3, float y3, float z3,
                                 int packedColor);

    void appendPositionTex(float x, float y, float z, float u, float v);

    void appendPositionTexColor(float x, float y, float z, float u, float v, int packedColor);

    void appendPositionTexNormal(float x, float y, float z, float u, float v, int packedNormal);

    void appendPositionTexLmapColor(float x, float y, float z, float u, float v, int packedLightmap,
                                    int packedColor);

    void appendPositionTexColorNormal(float x, float y, float z, float u, float v, int packedColor,
                                      int packedNormal);

    void appendPositionNormal(float x, float y, float z, int packedNormal);

    void appendParticlePositionTexColorLmap(float x, float y, float z, float u, float v, int packedColor,
                                            int packedLightmap);

    void appendPositionUnchecked(float x, float y, float z);

    void appendPositionQuadUnchecked(float x0, float y0, float z0,
                                     float x1, float y1, float z1,
                                     float x2, float y2, float z2,
                                     float x3, float y3, float z3);

    void appendPositionColorUnchecked(float x, float y, float z, int packedColor);

    void appendPositionColorQuadUnchecked(float x0, float y0, float z0,
                                          float x1, float y1, float z1,
                                          float x2, float y2, float z2,
                                          float x3, float y3, float z3,
                                          int packedColor);

    void appendPositionTexUnchecked(float x, float y, float z, float u, float v);

    void appendPositionTexQuadUnchecked(float x0, float y0, float z0, float u0, float v0,
                                        float x1, float y1, float z1, float u1, float v1,
                                        float x2, float y2, float z2, float u2, float v2,
                                        float x3, float y3, float z3, float u3, float v3);

    void appendPositionTexColorUnchecked(float x, float y, float z, float u, float v, int packedColor);

    void appendPositionTexNormalUnchecked(float x, float y, float z, float u, float v, int packedNormal);

    void appendPositionTexLmapColorUnchecked(float x, float y, float z, float u, float v, int packedLightmap,
                                             int packedColor);

    void appendPositionTexColorNormalUnchecked(float x, float y, float z, float u, float v, int packedColor,
                                               int packedNormal);

    void appendPositionTexColorQuadUnchecked(float x0, float y0, float z0, float u0, float v0, int c0,
                                             float x1, float y1, float z1, float u1, float v1, int c1,
                                             float x2, float y2, float z2, float u2, float v2, int c2,
                                             float x3, float y3, float z3, float u3, float v3, int c3);

    void appendPositionNormalUnchecked(float x, float y, float z, int packedNormal);

    void appendParticlePositionTexColorLmapUnchecked(float x, float y, float z, float u, float v,
                                                     int packedColor, int packedLightmap);

    void appendParticlePositionTexColorLmapQuadUnchecked(float x0, float y0, float z0, float u0, float v0,
                                                         float x1, float y1, float z1, float u1, float v1,
                                                         float x2, float y2, float z2, float u2, float v2,
                                                         float x3, float y3, float z3, float u3, float v3,
                                                         int packedColor, int packedLightmap);

    void setVertexCount(int vertexCount);

    static int packColor(int red, int green, int blue, int alpha) {
        int r = red & 255;
        int g = green & 255;
        int b = blue & 255;
        int a = alpha & 255;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    static int packColor(float red, float green, float blue, float alpha) {
        return packColor((int) (red * 255.0f), (int) (green * 255.0f), (int) (blue * 255.0f), (int) (alpha * 255.0f));
    }

    static int packNormal(float x, float y, float z) {
        int nx = ((int) (x * 127.0F)) & 255;
        int ny = ((int) (y * 127.0F)) & 255;
        int nz = ((int) (z * 127.0F)) & 255;
        return (nz << 16) | (ny << 8) | nx;
    }

    static int packLightmap(int skyLight, int blockLight) {
        int block = blockLight & 0xFFFF;
        int sky = skyLight & 0xFFFF;
        return (sky << 16) | block;
    }

    static long address(Buffer buffer) {
        return U.getLong(buffer, BUFFER_ADDRESS_OFFSET);
    }

    static void writeParticlePositionTexColorLmap(long address, float x, float y, float z, float u, float v,
                                                  int packedColor, int packedLightmap) {
        U.putFloat(address, x);
        U.putFloat(address + 4L, y);
        U.putFloat(address + 8L, z);
        U.putFloat(address + 12L, u);
        U.putFloat(address + 16L, v);
        U.putInt(address + 20L, packedColor);
        U.putInt(address + 24L, packedLightmap);
    }

    static void writeParticlePositionTexColorLmapQuad(long address,
                                                      float x0, float y0, float z0, float u0, float v0,
                                                      float x1, float y1, float z1, float u1, float v1,
                                                      float x2, float y2, float z2, float u2, float v2,
                                                      float x3, float y3, float z3, float u3, float v3,
                                                      int packedColor, int packedLightmap) {
        U.putFloat(address, x0);
        U.putFloat(address + 4L, y0);
        U.putFloat(address + 8L, z0);
        U.putFloat(address + 12L, u0);
        U.putFloat(address + 16L, v0);
        U.putInt(address + 20L, packedColor);
        U.putInt(address + 24L, packedLightmap);

        address += PARTICLE_POSITION_TEX_COLOR_LMAP_STRIDE;
        U.putFloat(address, x1);
        U.putFloat(address + 4L, y1);
        U.putFloat(address + 8L, z1);
        U.putFloat(address + 12L, u1);
        U.putFloat(address + 16L, v1);
        U.putInt(address + 20L, packedColor);
        U.putInt(address + 24L, packedLightmap);

        address += PARTICLE_POSITION_TEX_COLOR_LMAP_STRIDE;
        U.putFloat(address, x2);
        U.putFloat(address + 4L, y2);
        U.putFloat(address + 8L, z2);
        U.putFloat(address + 12L, u2);
        U.putFloat(address + 16L, v2);
        U.putInt(address + 20L, packedColor);
        U.putInt(address + 24L, packedLightmap);

        address += PARTICLE_POSITION_TEX_COLOR_LMAP_STRIDE;
        U.putFloat(address, x3);
        U.putFloat(address + 4L, y3);
        U.putFloat(address + 8L, z3);
        U.putFloat(address + 12L, u3);
        U.putFloat(address + 16L, v3);
        U.putInt(address + 20L, packedColor);
        U.putInt(address + 24L, packedLightmap);
    }

}
