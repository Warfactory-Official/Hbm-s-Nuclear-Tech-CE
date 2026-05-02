package com.hbm.render.model;

import com.hbm.lib.internal.UnsafeHolder;
import com.hbm.render.loader.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.hbm.lib.internal.UnsafeHolder.U;

/**
 * Base implementation for baked models that render exported Wavefront meshes.
 */
@SideOnly(Side.CLIENT)
public abstract class AbstractWavefrontBakedModel extends AbstractBakedModel {

    protected final HFRWavefrontObject model;
    protected final VertexFormat format;
    protected final float baseScale;
    protected final float baseTx;
    protected final float baseTy;
    protected final float baseTz;

    protected AbstractWavefrontBakedModel(HFRWavefrontObject model, VertexFormat format, float baseScale, float tx,
                                          float ty, float tz, ItemCameraTransforms transforms) {
        this(false, model, format, baseScale, tx, ty, tz, transforms);
    }

    protected AbstractWavefrontBakedModel(boolean ambientOcclusion, HFRWavefrontObject model, VertexFormat format,
                                          float baseScale, float tx, float ty, float tz,
                                          ItemCameraTransforms transforms) {
        super(ambientOcclusion, true, false, transforms, ItemOverrideList.NONE);
        this.model = model;
        this.format = format;
        this.baseScale = baseScale;
        baseTx = tx;
        baseTy = ty;
        baseTz = tz;
    }

    protected List<BakedQuad> bakeSimpleQuads(Set<String> partNames, float roll, float pitch, float yaw,
                                              boolean applyShading, boolean centerToBlock, TextureAtlasSprite sprite) {
        return bakeSimpleQuads(partNames, roll, pitch, yaw, applyShading, centerToBlock, sprite, -1);
    }

    protected List<BakedQuad> bakeSimpleQuads(Set<String> partNames, float roll, float pitch, float yaw,
                                              boolean applyShading, boolean centerToBlock, TextureAtlasSprite sprite,
                                              int tintIndex) {
        return bakeSimpleQuads(partNames, roll, pitch, yaw, applyShading, centerToBlock, sprite, tintIndex, 0.0F, 0.0F,
                0.0F);
    }

    protected List<BakedQuad> bakeSimpleQuads(Set<String> partNames, float roll, float pitch, float yaw,
                                              boolean applyShading,
                                              boolean centerToBlock, TextureAtlasSprite sprite, int tintIndex,
                                              float extraTx, float extraTy, float extraTz) {
        return bakeSimpleQuads(partNames, roll, pitch, yaw, applyShading, centerToBlock, sprite, tintIndex,
                extraTx, extraTy, extraTz, 1.0F, 1.0F);
    }

    protected List<BakedQuad> bakeSimpleQuads(Set<String> partNames, float roll, float pitch, float yaw,
                                              boolean applyShading,
                                              boolean centerToBlock, TextureAtlasSprite sprite, int tintIndex,
                                              float extraTx, float extraTy, float extraTz,
                                              float uScale, float vScale) {
        List<FaceGeometry> geometries = buildGeometry(partNames, roll, pitch, yaw, applyShading, centerToBlock, extraTx,
                extraTy, extraTz);
        List<BakedQuad> quads = new ArrayList<>(geometries.size());
        for (FaceGeometry geometry : geometries) {
            quads.add(geometry.buildQuad(sprite, tintIndex, uScale, vScale));
        }
        return quads;
    }

    protected List<FaceGeometry> buildGeometry(Set<String> partNames, float roll, float pitch, float yaw,
                                               boolean applyShading, boolean centerToBlock) {
        return buildGeometry(partNames, roll, pitch, yaw, applyShading, centerToBlock, 0.0F, 0.0F, 0.0F);
    }

    protected List<FaceGeometry> buildGeometry(Set<String> partNames, float roll, float pitch, float yaw,
                                               boolean applyShading,
                                               boolean centerToBlock, float extraTx, float extraTy, float extraTz) {
        List<FaceGeometry> geometries = new ArrayList<>();

        for (GroupObject group : model.groupObjects) {
            if (partNames != null && !partNames.contains(group.name)) {
                continue;
            }

            for (Face face : group.faces) {
                Vertex normal = face.faceNormal;

                float[] n1 = GeometryBakeUtil.rotateX(normal.x, normal.y, normal.z, roll);
                float[] n2 = GeometryBakeUtil.rotateZ(n1[0], n1[1], n1[2], pitch);
                float[] n3 = GeometryBakeUtil.rotateY(n2[0], n2[1], n2[2], yaw);

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
                Vector3f[] vertexNormals = new Vector3f[4];
                int[] colors = new int[4];

                for (int v = 0; v < 4; v++) {
                    int idx = indices[v];
                    Vertex vertex = face.vertices[idx];

                    float[] p1 = GeometryBakeUtil.rotateX(vertex.x, vertex.y, vertex.z, roll);
                    float[] p2 = GeometryBakeUtil.rotateZ(p1[0], p1[1], p1[2], pitch);
                    float[] p3 = GeometryBakeUtil.rotateY(p2[0], p2[1], p2[2], yaw);

                    float x = p3[0];
                    float y = p3[1];
                    float z = p3[2];

                    if (centerToBlock) {
                        x += 0.5F;
                        y += 0.5F;
                        z += 0.5F;
                    }

                    x = x * baseScale + baseTx + extraTx;
                    y = y * baseScale + baseTy + extraTy;
                    z = z * baseScale + baseTz + extraTz;

                    TextureCoordinate tex = face.textureCoordinates[idx];
                    uu[v] = tex.u * 16.0f;
                    vv[v] = tex.v * 16.0f;

                    Vertex vertexNormal = face.vertexNormals != null && idx < face.vertexNormals.length ? face.vertexNormals[idx] : null;
                    if (vertexNormal != null) {
                        float[] vn1 = GeometryBakeUtil.rotateX(vertexNormal.x, vertexNormal.y, vertexNormal.z, roll);
                        float[] vn2 = GeometryBakeUtil.rotateZ(vn1[0], vn1[1], vn1[2], pitch);
                        float[] vn3 = GeometryBakeUtil.rotateY(vn2[0], vn2[1], vn2[2], yaw);
                        Vector3f vectorNormal = new Vector3f(vn3[0], vn3[1], vn3[2]);
                        if (vectorNormal.lengthSquared() > 0.0F) {
                            vectorNormal.normalize();
                        } else {
                            vectorNormal.set(n3[0], n3[1], n3[2]);
                        }
                        vertexNormals[v] = vectorNormal;
                        colors[v] = applyShading ? GeometryBakeUtil.computeShade(vectorNormal.x, vectorNormal.y, vectorNormal.z) : 255;
                    } else {
                        vertexNormals[v] = new Vector3f(n3[0], n3[1], n3[2]);
                        colors[v] = applyShading ? GeometryBakeUtil.computeShade(n3[0], n3[1], n3[2]) : 255;
                    }

                    px[v] = x;
                    py[v] = y;
                    pz[v] = z;
                }

                EnumFacing facing = EnumFacing.getFacingFromVector(n3[0], n3[1], n3[2]);
                geometries.add(new FaceGeometry(facing, px, py, pz, uu, vv, vertexNormals, colors));
            }
        }

        return geometries;
    }

    /**
     * Matrix4f-driven variant of {@link #buildGeometry(Set, float, float, float, boolean, boolean)}.
     * Applies the given transform as-is to OBJ vertex positions and normals; ignores {@code baseScale}
     * and {@code baseTx/ty/tz}. Useful when each part of a model needs a distinct pivot/rotation
     * chain that can't be expressed as shared roll/pitch/yaw.
     */
    protected List<FaceGeometry> buildGeometryMatrix(Set<String> partNames, Matrix4f transform,
                                                     boolean applyShading) {
        return buildGeometryMatrix(model, partNames, transform, applyShading);
    }

    /**
     * Matrix-driven geometry builder that accepts an explicit {@link HFRWavefrontObject} source,
     * used when a baked model composites parts from more than one OBJ.
     */
    protected List<FaceGeometry> buildGeometryMatrix(HFRWavefrontObject objModel, Set<String> partNames,
                                                     Matrix4f transform, boolean applyShading) {
        List<FaceGeometry> geometries = new ArrayList<>();

        for (GroupObject group : objModel.groupObjects) {
            if (partNames != null && !partNames.contains(group.name)) continue;

            for (Face face : group.faces) {
                int vertexCount = face.vertices.length;
                if (vertexCount < 3) continue;

                if (face.faceNormal == null) {
                    face.faceNormal = face.calculateFaceNormal();
                }

                int[] indices = vertexCount >= 4 ? new int[]{0, 1, 2, 3} : new int[]{0, 1, 2, 2};

                float[] px = new float[4];
                float[] py = new float[4];
                float[] pz = new float[4];
                float[] uu = new float[4];
                float[] vv = new float[4];
                Vector3f[] vertexNormals = new Vector3f[4];
                int[] colors = new int[4];

                Vector3f faceNormal = BakedModelMatrixUtil.transformNormal(transform, face.faceNormal.x,
                        face.faceNormal.y, face.faceNormal.z);

                for (int v = 0; v < 4; v++) {
                    int idx = indices[v];
                    Vertex vertex = face.vertices[idx];
                    Vector3f p = BakedModelMatrixUtil.transformPosition(transform, vertex.x, vertex.y, vertex.z);
                    px[v] = p.x;
                    py[v] = p.y;
                    pz[v] = p.z;

                    TextureCoordinate tex = face.textureCoordinates[idx];
                    uu[v] = tex.u * 16.0F;
                    vv[v] = tex.v * 16.0F;

                    Vertex source = face.vertexNormals != null && idx < face.vertexNormals.length
                            ? face.vertexNormals[idx] : null;
                    Vector3f n = source != null
                            ? BakedModelMatrixUtil.transformNormal(transform, source.x, source.y, source.z)
                            : new Vector3f(faceNormal);
                    if (n.lengthSquared() <= 0.0F) n.set(faceNormal);
                    vertexNormals[v] = n;
                    colors[v] = applyShading ? GeometryBakeUtil.computeShade(n.x, n.y, n.z) : 255;
                }

                EnumFacing facing = EnumFacing.getFacingFromVector(faceNormal.x, faceNormal.y, faceNormal.z);
                geometries.add(new FaceGeometry(facing, px, py, pz, uu, vv, vertexNormals, colors));
            }
        }

        return geometries;
    }

    protected float[] computeGeometryBounds(Set<String> partNames, float roll, float pitch, float yaw,
                                            boolean centerToBlock, float extraTx, float extraTy, float extraTz) {
        List<FaceGeometry> geometries = buildGeometry(partNames, roll, pitch, yaw, false, centerToBlock, extraTx,
                extraTy, extraTz);
        if (geometries.isEmpty()) {
            return null;
        }

        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;

        for (FaceGeometry geometry : geometries) {
            for (int i = 0; i < 4; i++) {
                minX = Math.min(minX, geometry.px[i]);
                minY = Math.min(minY, geometry.py[i]);
                minZ = Math.min(minZ, geometry.pz[i]);
                maxX = Math.max(maxX, geometry.px[i]);
                maxY = Math.max(maxY, geometry.py[i]);
                maxZ = Math.max(maxZ, geometry.pz[i]);
            }
        }

        return new float[]{minX, minY, minZ, maxX, maxY, maxZ};
    }

    protected final class FaceGeometry {
        private static final long COLORS_BASE = UnsafeHolder.fieldOffset(FaceGeometry.class, "colors01");
        static {
            if (UnsafeHolder.fieldOffset(FaceGeometry.class, "colors23") != COLORS_BASE + 8L)
                throw new AssertionError("colors01 and colors23 are not contiguous");
        }

        private final EnumFacing facing;
        private final float[] px;
        private final float[] py;
        private final float[] pz;
        private final float[] uu;
        private final float[] vv;
        private final Vector3f[] vertexNormals;
        private final long colors01;
        private final long colors23;

        FaceGeometry(EnumFacing facing, float[] px, float[] py, float[] pz, float[] uu, float[] vv,
                     Vector3f[] vertexNormals, int[] colors) {
            this.facing = facing;
            this.px = px;
            this.py = py;
            this.pz = pz;
            this.uu = uu;
            this.vv = vv;
            this.vertexNormals = vertexNormals;
            colors01 = (colors[0] & 0xFFFFFFFFL) | ((long) colors[1] << 32);
            colors23 = (colors[2] & 0xFFFFFFFFL) | ((long) colors[3] << 32);
        }

        public BakedQuad buildQuad(TextureAtlasSprite sprite, int tintIndex) {
            return buildQuad(sprite, tintIndex, 1.0F, 1.0F);
        }

        public BakedQuad buildQuad(TextureAtlasSprite sprite, int tintIndex, float uScale, float vScale) {
            int[] vertexData = new int[format.getIntegerSize() * 4];
            float[] scratch = new float[4];
            for (int i = 0; i < 4; i++) {
                int c = U.getInt(this, COLORS_BASE + i * 4L);
                GeometryBakeUtil.putVertex(format, vertexData, i, px[i], py[i], pz[i], uu[i] * uScale, vv[i] * vScale,
                        c, c, c, vertexNormals[i], sprite, scratch);
            }
            return new HbmBakedQuad(vertexData, tintIndex, facing, sprite, format);
        }

        public BakedQuad buildBackQuad(TextureAtlasSprite sprite, int tintIndex) {
            return buildBackQuad(sprite, tintIndex, 1.0F, 1.0F);
        }

        public BakedQuad buildBackQuad(TextureAtlasSprite sprite, int tintIndex, float uScale, float vScale) {
            int[] vertexData = new int[format.getIntegerSize() * 4];
            float[] scratch = new float[4];
            int[] order = new int[]{0, 3, 2, 1};
            int outIndex = 0;
            for (int index : order) {
                Vector3f normal = vertexNormals[index];
                Vector3f reversedNormal = new Vector3f(-normal.x, -normal.y, -normal.z);
                int vertexIndex = outIndex++;
                int c = U.getInt(this, COLORS_BASE + index * 4L);
                GeometryBakeUtil.putVertex(format, vertexData, vertexIndex, px[index], py[index], pz[index],
                        uu[index] * uScale, vv[index] * vScale, c, c, c, reversedNormal, sprite, scratch);
            }
            return new HbmBakedQuad(vertexData, tintIndex, facing.getOpposite(), sprite, format);
        }
    }
}
