package com.hbm.render.model;

import com.hbm.render.loader.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.*;

public class FixedTransformWavefrontBakedModel extends AbstractBakedModel {
    private final HFRWavefrontObject model;
    private final TextureAtlasSprite sprite;
    private final Set<String> partNames;
    private final Matrix4f transform;
    private final boolean shaded;
    private final float uScale;
    private final float vScale;
    private List<BakedQuad> cache;

    public FixedTransformWavefrontBakedModel(HFRWavefrontObject model, TextureAtlasSprite sprite,
                                             @Nullable String[] partNames,
                                             Matrix4f transform, boolean shaded) {
        this(model, sprite, partNames, transform, shaded, 1.0F, 1.0F);
    }

    public FixedTransformWavefrontBakedModel(HFRWavefrontObject model, TextureAtlasSprite sprite,
                                             @Nullable String[] partNames,
                                             Matrix4f transform, boolean shaded, float uScale, float vScale) {
        super(BakedModelTransforms.forDeco(BakedModelTransforms.standardBlock()));
        this.model = model;
        this.sprite = sprite;
        this.partNames = partNames == null || partNames.length == 0 ? null : new LinkedHashSet<>(
                Arrays.asList(partNames));
        this.transform = new Matrix4f(transform);
        this.shaded = shaded;
        this.uScale = uScale;
        this.vScale = vScale;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable net.minecraft.block.state.IBlockState state,
                                             @Nullable EnumFacing side, long rand) {
        if (side != null) {
            return Collections.emptyList();
        }
        if (cache == null) {
            cache = Collections.unmodifiableList(buildQuads());
        }
        return cache;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return sprite;
    }

    private List<BakedQuad> buildQuads() {
        List<BakedQuad> quads = new ArrayList<>();

        for (GroupObject group : model.groupObjects) {
            if (partNames != null && !partNames.contains(group.name)) {
                continue;
            }

            for (Face face : group.faces) {
                Vertex faceNormal = face.faceNormal;
                Vector3f transformedFaceNormal = BakedModelMatrixUtil.transformNormal(transform, faceNormal.x,
                        faceNormal.y, faceNormal.z);

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
                    Vector3f transformed = BakedModelMatrixUtil.transformPosition(transform, vertex.x, vertex.y,
                            vertex.z);
                    px[v] = transformed.x;
                    py[v] = transformed.y;
                    pz[v] = transformed.z;

                    TextureCoordinate tex = face.textureCoordinates[idx];
                    uu[v] = (float) (tex.u * 16.0D);
                    vv[v] = (float) (tex.v * 16.0D);

                    Vertex vertexNormal = face.vertexNormals != null && idx < face.vertexNormals.length ? face.vertexNormals[idx] : null;
                    Vector3f transformedVertexNormal = vertexNormal != null
                            ? BakedModelMatrixUtil.transformNormal(transform, vertexNormal.x, vertexNormal.y,
                            vertexNormal.z)
                            : new Vector3f(transformedFaceNormal);
                    if (transformedVertexNormal.lengthSquared() <= 0.0F) {
                        transformedVertexNormal.set(transformedFaceNormal);
                    }
                    vertexNormals[v] = transformedVertexNormal;
                    colors[v] = shaded ? GeometryBakeUtil.computeShade(transformedVertexNormal.x,
                            transformedVertexNormal.y, transformedVertexNormal.z) : 255;
                }

                EnumFacing facing = EnumFacing.getFacingFromVector(transformedFaceNormal.x, transformedFaceNormal.y,
                        transformedFaceNormal.z);
                int[] vertexData = new int[DefaultVertexFormats.BLOCK.getIntegerSize() * 4];
                float[] scratch = new float[4];
                for (int i = 0; i < 4; i++) {
                    GeometryBakeUtil.putVertex(DefaultVertexFormats.BLOCK, vertexData, i, px[i], py[i], pz[i],
                            uu[i] * uScale, vv[i] * vScale, colors[i], colors[i], colors[i], vertexNormals[i], sprite, scratch);
                }
                quads.add(new HbmBakedQuad(vertexData, -1, facing, sprite, DefaultVertexFormats.BLOCK));
            }
        }

        return quads;
    }
}
