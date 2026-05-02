package com.hbm.render.model;

import net.minecraft.client.model.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class FixedTransformModelRendererBakedModel extends AbstractBakedModel {
    private final Supplier<ModelBase> modelFactory;
    private final TextureAtlasSprite sprite;
    private final Matrix4f transform;
    private final float modelScale;
    private final float uScale;
    private final float vScale;
    private List<BakedQuad> cache;

    public FixedTransformModelRendererBakedModel(Supplier<ModelBase> modelFactory, TextureAtlasSprite sprite,
                                                 Matrix4f transform, float modelScale) {
        this(modelFactory, sprite, transform, modelScale, 1.0F, 1.0F);
    }

    public FixedTransformModelRendererBakedModel(Supplier<ModelBase> modelFactory, TextureAtlasSprite sprite,
                                                 Matrix4f transform, float modelScale,
                                                 float uScale, float vScale) {
        super(BakedModelTransforms.forDeco(BakedModelTransforms.standardBlock()));
        this.modelFactory = modelFactory;
        this.sprite = sprite;
        this.transform = new Matrix4f(transform);
        this.modelScale = modelScale;
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
        ModelBase model = modelFactory.get();
        if (model == null) {
            return Collections.emptyList();
        }

        List<BakedQuad> quads = new ArrayList<>();
        for (ModelRenderer renderer : model.boxList) {
            if (renderer == null || renderer.isHidden || !renderer.showModel || renderer.cubeList == null) {
                continue;
            }
            for (ModelBox box : renderer.cubeList) {
                if (box == null || box.quadList == null) {
                    continue;
                }
                for (TexturedQuad quad : box.quadList) {
                    BakedQuad bakedQuad = bakeQuad(renderer, quad);
                    if (bakedQuad != null) {
                        quads.add(bakedQuad);
                    }
                }
            }
        }
        return quads;
    }

    private BakedQuad bakeQuad(ModelRenderer renderer, TexturedQuad quad) {
        PositionTextureVertex[] vertices = quad.vertexPositions;
        if (vertices == null || vertices.length != 4) {
            return null;
        }

        float[] px = new float[4];
        float[] py = new float[4];
        float[] pz = new float[4];
        float[] uu = new float[4];
        float[] vv = new float[4];

        for (int i = 0; i < 4; i++) {
            PositionTextureVertex vertex = vertices[i];
            float[] local = {
                    ((float) vertex.vector3D.x) * modelScale,
                    ((float) vertex.vector3D.y) * modelScale,
                    ((float) vertex.vector3D.z) * modelScale
            };

            local = GeometryBakeUtil.rotateX(local[0], local[1], local[2], renderer.rotateAngleX);
            local = GeometryBakeUtil.rotateY(local[0], local[1], local[2], renderer.rotateAngleY);
            local = GeometryBakeUtil.rotateZ(local[0], local[1], local[2], renderer.rotateAngleZ);

            local[0] += renderer.rotationPointX * modelScale + renderer.offsetX;
            local[1] += renderer.rotationPointY * modelScale + renderer.offsetY;
            local[2] += renderer.rotationPointZ * modelScale + renderer.offsetZ;

            Vector3f transformed = BakedModelMatrixUtil.transformPosition(transform, local[0], local[1], local[2]);
            px[i] = transformed.x;
            py[i] = transformed.y;
            pz[i] = transformed.z;
            uu[i] = vertex.texturePositionX * 16.0F;
            vv[i] = vertex.texturePositionY * 16.0F;
        }

        Vector3f normal = computeNormal(px, py, pz);
        int color = GeometryBakeUtil.computeShade(normal.x, normal.y, normal.z);
        EnumFacing facing = EnumFacing.getFacingFromVector(normal.x, normal.y, normal.z);
        int[] vertexData = new int[DefaultVertexFormats.BLOCK.getIntegerSize() * 4];
        float[] scratch = new float[4];
        for (int i = 0; i < 4; i++) {
            GeometryBakeUtil.putVertex(DefaultVertexFormats.BLOCK, vertexData, i, px[i], py[i], pz[i], uu[i] * uScale,
                    vv[i] * vScale, color, color, color, normal, sprite, scratch);
        }
        return new HbmBakedQuad(vertexData, -1, facing, sprite, DefaultVertexFormats.BLOCK);
    }

    private static Vector3f computeNormal(float[] px, float[] py, float[] pz) {
        float ax = px[1] - px[0];
        float ay = py[1] - py[0];
        float az = pz[1] - pz[0];
        float bx = px[1] - px[2];
        float by = py[1] - py[2];
        float bz = pz[1] - pz[2];
        Vector3f normal = new Vector3f(
                by * az - bz * ay,
                bz * ax - bx * az,
                bx * ay - by * ax
        );
        if (normal.lengthSquared() > 0.0F) {
            normal.normalize();
        } else {
            normal.set(0.0F, 1.0F, 0.0F);
        }
        return normal;
    }
}
