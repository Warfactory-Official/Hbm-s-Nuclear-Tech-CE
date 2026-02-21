package com.hbm.render.model;

import com.hbm.render.loader.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Vector3f;
import java.util.*;

/**
 * Base implementation for baked models that render exported Wavefront meshes.
 */
@SideOnly(Side.CLIENT)
public abstract class AbstractWavefrontBakedModel extends AbstractBakedModel {

    protected final HFRWavefrontObject model;
    protected final VertexFormat format;
    protected final float baseScale;
    protected final float tx;
    protected final float ty;
    protected final float tz;

    protected AbstractWavefrontBakedModel(HFRWavefrontObject model, VertexFormat format, float baseScale, float tx, float ty, float tz, ItemCameraTransforms transforms) {
        super(transforms);
        this.model = model;
        this.format = format;
        this.baseScale = baseScale;
        this.tx = tx;
        this.ty = ty;
        this.tz = tz;
    }

    private static int computeShade(float nx, float ny, float nz) {
        float brightness = (ny + 0.7F) * 0.9F - Math.abs(nx) * 0.1F + Math.abs(nz) * 0.1F;
        if (brightness < 0.45F) brightness = 0.45F;
        if (brightness > 1.0F) brightness = 1.0F;
        return clampColor((int) (brightness * 255.0F));
    }

    protected static int clampColor(int value) {
        return Math.min(Math.max(value, 0), 255);
    }

    protected static @NotNull EnumFacing facingFromNormal(float nx, float ny, float nz) {
        return EnumFacing.getFacingFromVector(nx, ny, nz);
    }

    @Contract("_, _, _, _ -> new")
    protected static double @NotNull [] rotateX(double x, double y, double z, float angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double ny = y * cos - z * sin;
        double nz = y * sin + z * cos;
        return new double[]{x, ny, nz};
    }

    @Contract("_, _, _, _ -> new")
    protected static double @NotNull [] rotateY(double x, double y, double z, float angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double nx = x * cos + z * sin;
        double nz = -x * sin + z * cos;
        return new double[]{nx, y, nz};
    }

    @Contract("_, _, _, _ -> new")
    protected static double @NotNull [] rotateZ(double x, double y, double z, float angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double nx = x * cos - y * sin;
        double ny = x * sin + y * cos;
        return new double[]{nx, ny, z};
    }

    protected List<BakedQuad> bakeSimpleQuads(Collection<String> partNames, float roll, float pitch, float yaw, boolean applyShading, boolean centerToBlock, TextureAtlasSprite sprite) {
        return bakeSimpleQuads(partNames, roll, pitch, yaw, applyShading, centerToBlock, sprite, -1);
    }

    protected List<BakedQuad> bakeSimpleQuads(Collection<String> partNames, float roll, float pitch, float yaw, boolean applyShading, boolean centerToBlock, TextureAtlasSprite sprite, int tintIndex) {
        List<FaceGeometry> geometries = buildGeometry(partNames, roll, pitch, yaw, applyShading, centerToBlock);
        List<BakedQuad> quads = new ArrayList<>(geometries.size());
        for (FaceGeometry geometry : geometries) {
            quads.add(geometry.buildQuad(sprite, tintIndex));
        }
        return quads;
    }

    protected List<FaceGeometry> buildGeometry(Collection<String> partNames, float roll, float pitch, float yaw, boolean applyShading, boolean centerToBlock) {
        List<FaceGeometry> geometries = new ArrayList<>();
        Set<String> filter = prepareFilter(partNames);

        for (GroupObject group : model.groupObjects) {
            if (filter != null && !filter.contains(group.name)) {
                continue;
            }

            for (Face face : group.faces) {
                Vertex normal = face.faceNormal;

                double[] n1 = rotateX(normal.x, normal.y, normal.z, roll);
                double[] n2 = rotateZ(n1[0], n1[1], n1[2], pitch);
                double[] n3 = rotateY(n2[0], n2[1], n2[2], yaw);

                float fnx = (float) n3[0];
                float fny = (float) n3[1];
                float fnz = (float) n3[2];

                int color = applyShading ? computeShade(fnx, fny, fnz) : 255;

                int vertexCount = face.vertices.length;
                if (vertexCount < 3) {
                    continue;
                }

                int[] indices = vertexCount >= 4 ? new int[]{0, 1, 2, 3} : new int[]{0, 1, 2, 2};

                float[] px = new float[4];
                float[] py = new float[4];
                float[] pz = new float[4];
                float[] uu = new float[4];
                float[] vv = new float[4];

                for (int v = 0; v < 4; v++) {
                    int idx = indices[v];
                    Vertex vertex = face.vertices[idx];

                    double[] p1 = rotateX(vertex.x, vertex.y, vertex.z, roll);
                    double[] p2 = rotateZ(p1[0], p1[1], p1[2], pitch);
                    double[] p3 = rotateY(p2[0], p2[1], p2[2], yaw);

                    float x = (float) p3[0];
                    float y = (float) p3[1];
                    float z = (float) p3[2];

                    if (centerToBlock) {
                        x += 0.5F;
                        y += 0.5F;
                        z += 0.5F;
                    }

                    x = x * baseScale + tx;
                    y = y * baseScale + ty;
                    z = z * baseScale + tz;

                    TextureCoordinate tex = face.textureCoordinates[idx];
                    uu[v] = (float) (tex.u * 16.0D);
                    vv[v] = (float) (tex.v * 16.0D);

                    px[v] = x;
                    py[v] = y;
                    pz[v] = z;
                }

                EnumFacing facing = facingFromNormal(fnx, fny, fnz);
                Vector3f vectorNormal = new Vector3f(fnx, fny, fnz);
                vectorNormal.normalize();

                geometries.add(new FaceGeometry(facing, px, py, pz, uu, vv, vectorNormal, color));
            }
        }

        return geometries;
    }

    private static Set<String> prepareFilter(Collection<String> partNames) {
        if (partNames == null) return null;
        if (partNames instanceof Set<?>) {
            // unfortunately instanceof Set<String> stringSet is only available in Java 16+
            return (Set<String>) partNames;
        }
        return new HashSet<>(partNames);
    }

    protected void putVertex(UnpackedBakedQuad.Builder builder, float x, float y, float z, float u16, float v16, int cr, int cg, int cb, Vector3f normal, TextureAtlasSprite sprite) {
        for (int elementIndex = 0; elementIndex < format.getElementCount(); elementIndex++) {
            VertexFormatElement element = format.getElement(elementIndex);
            switch (element.getUsage()) {
                case POSITION -> builder.put(elementIndex, x, y, z);
                case COLOR -> builder.put(elementIndex, cr / 255.0F, cg / 255.0F, cb / 255.0F, 1.0F);
                case UV -> {
                    if (element.getIndex() == 0)
                        builder.put(elementIndex, sprite.getInterpolatedU(u16), sprite.getInterpolatedV(v16));
                    else builder.put(elementIndex, 0.0F, 0.0F);
                }
                case NORMAL -> builder.put(elementIndex, normal.x, normal.y, normal.z);
                case PADDING -> builder.put(elementIndex, 0.0F);
                default -> builder.put(elementIndex);
            }
        }
    }

    protected final class FaceGeometry {
        private final EnumFacing facing;
        private final float[] px;
        private final float[] py;
        private final float[] pz;
        private final float[] uu;
        private final float[] vv;
        private final Vector3f normal;
        private final int color;

        private FaceGeometry(EnumFacing facing, float[] px, float[] py, float[] pz, float[] uu, float[] vv, Vector3f normal, int color) {
            this.facing = facing;
            this.px = px;
            this.py = py;
            this.pz = pz;
            this.uu = uu;
            this.vv = vv;
            this.normal = normal;
            this.color = color;
        }

        public BakedQuad buildQuad(TextureAtlasSprite sprite, int tintIndex) {
            UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
            builder.setQuadOrientation(facing);
            builder.setTexture(sprite);
            builder.setApplyDiffuseLighting(true);
            if (tintIndex >= 0) builder.setQuadTint(tintIndex);
            for (int i = 0; i < 4; i++)
                putVertex(builder, px[i], py[i], pz[i], uu[i], vv[i], color, color, color, normal, sprite);
            return builder.build();
        }
    }
}
