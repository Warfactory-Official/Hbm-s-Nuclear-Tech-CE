package com.hbm.render.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;

public class StaticModelRendererBakedModel extends AbstractBakedModel {
    private final IntFunction<ModelBase> modelFactory;
    private final TextureAtlasSprite sprite;
    private final float uScale;
    private final float vScale;
    private final float[] yawsByMeta;
    private final float roll;
    private final float pitch;
    private final float preTranslateX;
    private final float preTranslateY;
    private final float preTranslateZ;
    private final float tx;
    private final float ty;
    private final float tz;
    private final float scale;
    private final List<BakedQuad>[] cache;

    public StaticModelRendererBakedModel(IntFunction<ModelBase> modelFactory, TextureAtlasSprite sprite, float uScale,
                                         float vScale, float[] yawsByMeta,
                                         float roll, float pitch,
                                         float preTranslateX, float preTranslateY, float preTranslateZ,
                                         float tx, float ty, float tz, float scale) {
        super(BakedModelTransforms.forDeco(BakedModelTransforms.standardBlock()));
        this.modelFactory = modelFactory;
        this.sprite = sprite;
        this.uScale = uScale;
        this.vScale = vScale;
        this.yawsByMeta = Arrays.copyOf(yawsByMeta, yawsByMeta.length);
        this.roll = roll;
        this.pitch = pitch;
        this.preTranslateX = preTranslateX;
        this.preTranslateY = preTranslateY;
        this.preTranslateZ = preTranslateZ;
        this.tx = tx;
        this.ty = ty;
        this.tz = tz;
        this.scale = scale;
        this.cache = new List[this.yawsByMeta.length];
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side != null || state == null) {
            return Collections.emptyList();
        }
        int meta = state.getBlock().getMetaFromState(state);
        if (meta < 0 || meta >= yawsByMeta.length) {
            return Collections.emptyList();
        }
        float yaw = yawsByMeta[meta];
        if (Float.isNaN(yaw)) {
            return Collections.emptyList();
        }
        List<BakedQuad> quads = cache[meta];
        if (quads != null) {
            return quads;
        }
        quads = Collections.unmodifiableList(buildQuads(meta, yaw));
        cache[meta] = quads;
        return quads;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return sprite;
    }

    private List<BakedQuad> buildQuads(int meta, float yaw) {
        ModelBase model = modelFactory.apply(meta);
        if (model == null) {
            return Collections.emptyList();
        }

        float[] rotatedPreTranslate = GeometryBakeUtil.rotateY(preTranslateX, preTranslateY, preTranslateZ, yaw);
        float worldTx = tx + rotatedPreTranslate[0];
        float worldTy = ty + rotatedPreTranslate[1];
        float worldTz = tz + rotatedPreTranslate[2];

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
                    BakedQuad bakedQuad = bakeQuad(renderer, quad, yaw, worldTx, worldTy, worldTz);
                    if (bakedQuad != null) {
                        quads.add(bakedQuad);
                    }
                }
            }
        }
        return quads;
    }

    private BakedQuad bakeQuad(ModelRenderer renderer, TexturedQuad quad, float yaw, float worldTx, float worldTy,
                               float worldTz) {
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
                    ((float) vertex.vector3D.x) * scale,
                    ((float) vertex.vector3D.y) * scale,
                    ((float) vertex.vector3D.z) * scale
            };

            local = GeometryBakeUtil.rotateX(local[0], local[1], local[2], renderer.rotateAngleX);
            local = GeometryBakeUtil.rotateY(local[0], local[1], local[2], renderer.rotateAngleY);
            local = GeometryBakeUtil.rotateZ(local[0], local[1], local[2], renderer.rotateAngleZ);

            local[0] += renderer.rotationPointX * scale + renderer.offsetX;
            local[1] += renderer.rotationPointY * scale + renderer.offsetY;
            local[2] += renderer.rotationPointZ * scale + renderer.offsetZ;

            local = GeometryBakeUtil.rotateX(local[0], local[1], local[2], roll);
            local = GeometryBakeUtil.rotateZ(local[0], local[1], local[2], pitch);
            local = GeometryBakeUtil.rotateY(local[0], local[1], local[2], yaw);

            px[i] = local[0] + worldTx;
            py[i] = local[1] + worldTy;
            pz[i] = local[2] + worldTz;
            uu[i] = vertex.texturePositionX * 16.0F * uScale;
            vv[i] = vertex.texturePositionY * 16.0F * vScale;
        }

        Vector3f normal = computeNormal(px, py, pz);
        int color = GeometryBakeUtil.computeShade(normal.x, normal.y, normal.z);
        EnumFacing facing = EnumFacing.getFacingFromVector(normal.x, normal.y, normal.z);
        int[] vertexData = new int[DefaultVertexFormats.BLOCK.getIntegerSize() * 4];
        float[] scratch = new float[4];
        for (int i = 0; i < 4; i++) {
            GeometryBakeUtil.putVertex(DefaultVertexFormats.BLOCK, vertexData, i, px[i], py[i], pz[i], uu[i], vv[i],
                    color, color, color, normal, sprite, scratch);
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
