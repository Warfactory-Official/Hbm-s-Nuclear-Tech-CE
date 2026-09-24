package com.hbm.render.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Vector3f;

public final class GeometryBakeUtil {

    private GeometryBakeUtil() {
    }

    public static int computeShade(float nx, float ny, float nz) {
        return (int) (LightUtil.diffuseLight(nx, ny, nz) * 255.0F);
    }

    public static float @NotNull [] rotateX(float x, float y, float z, float angle) {
        float cos = MathHelper.cos(angle);
        float sin = MathHelper.sin(angle);
        float ny = y * cos + z * sin;
        float nz = z * cos - y * sin;
        return new float[]{x, ny, nz};
    }

    public static float @NotNull [] rotateY(float x, float y, float z, float angle) {
        float cos = MathHelper.cos(angle);
        float sin = MathHelper.sin(angle);
        float nx = x * cos + z * sin;
        float nz = -x * sin + z * cos;
        return new float[]{nx, y, nz};
    }

    public static float @NotNull [] rotateZ(float x, float y, float z, float angle) {
        float cos = MathHelper.cos(angle);
        float sin = MathHelper.sin(angle);
        float nx = x * cos + y * sin;
        float ny = y * cos - x * sin;
        return new float[]{nx, ny, z};
    }

    public static void putVertex(VertexFormat format, int[] vertexData, int vertexIndex, float x, float y, float z,
                                 float u16, float v16,
                                 int cr, int cg, int cb, Vector3f normal, TextureAtlasSprite sprite, float[] scratch) {
        for (int elementIndex = 0; elementIndex < format.getElementCount(); elementIndex++) {
            VertexFormatElement element = format.getElement(elementIndex);
            switch (element.getUsage()) {
                case POSITION -> {
                    scratch[0] = x;
                    scratch[1] = y;
                    scratch[2] = z;
                    LightUtil.pack(scratch, vertexData, format, vertexIndex, elementIndex);
                }
                case COLOR -> {
                    scratch[0] = cr / 255.0F;
                    scratch[1] = cg / 255.0F;
                    scratch[2] = cb / 255.0F;
                    scratch[3] = 1.0F;
                    LightUtil.pack(scratch, vertexData, format, vertexIndex, elementIndex);
                }
                case UV -> {
                    if (element.getIndex() == 0) {
                        scratch[0] = sprite.getInterpolatedU(u16);
                        scratch[1] = sprite.getInterpolatedV(v16);
                    } else {
                        scratch[0] = 0.0F;
                        scratch[1] = 0.0F;
                    }
                    LightUtil.pack(scratch, vertexData, format, vertexIndex, elementIndex);
                }
                case NORMAL -> {
                    scratch[0] = normal.x;
                    scratch[1] = normal.y;
                    scratch[2] = normal.z;
                    LightUtil.pack(scratch, vertexData, format, vertexIndex, elementIndex);
                }
                case PADDING -> {
                    scratch[0] = 0.0F;
                    LightUtil.pack(scratch, vertexData, format, vertexIndex, elementIndex);
                }
                default -> {
                }
            }
        }
    }
}
