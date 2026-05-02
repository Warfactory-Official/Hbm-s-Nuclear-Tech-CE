package com.hbm.render.model;

import com.hbm.render.loader.HFRWavefrontObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SideOnly(Side.CLIENT)
public class StaticMetaWavefrontBakedModel extends AbstractWavefrontBakedModel {
    private final TextureAtlasSprite sprite;
    private final Set<String> partNames;
    private final float[] yawsByMeta;
    private final float roll;
    private final float pitch;
    private final float preTranslateX;
    private final float preTranslateY;
    private final float preTranslateZ;
    private final float uScale;
    private final float vScale;
    private final boolean doubleSided;
    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] cache;

    public StaticMetaWavefrontBakedModel(HFRWavefrontObject model, TextureAtlasSprite sprite, float[] yawsByMeta,
                                         @Nullable String[] partNames, float roll, float pitch,
                                         boolean doubleSided, float uScale, float vScale,
                                         float preTranslateX, float preTranslateY, float preTranslateZ,
                                         float translateX, float translateY, float translateZ) {
        super(model, DefaultVertexFormats.BLOCK, 1.0F, translateX, translateY, translateZ,
                BakedModelTransforms.forDeco(BakedModelTransforms.standardBlock()));
        this.sprite = sprite;
        this.yawsByMeta = Arrays.copyOf(yawsByMeta, yawsByMeta.length);
        this.partNames = partNames == null || partNames.length == 0 ? null : new LinkedHashSet<>(
                Arrays.asList(partNames));
        this.roll = roll;
        this.pitch = pitch;
        this.doubleSided = doubleSided;
        this.uScale = uScale;
        this.vScale = vScale;
        this.preTranslateX = preTranslateX;
        this.preTranslateY = preTranslateY;
        this.preTranslateZ = preTranslateZ;
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

        quads = Collections.unmodifiableList(buildQuads(yaw));
        cache[meta] = quads;
        return quads;
    }

    private List<BakedQuad> buildQuads(float yaw) {
        float[] rotatedPreTranslate = GeometryBakeUtil.rotateY(preTranslateX, preTranslateY, preTranslateZ, yaw);
        float extraTx = 0.5F + rotatedPreTranslate[0];
        float extraTy = rotatedPreTranslate[1];
        float extraTz = 0.5F + rotatedPreTranslate[2];

        List<FaceGeometry> geometry = buildGeometry(partNames, roll, pitch, yaw, true, false, extraTx, extraTy,
                extraTz);
        List<BakedQuad> quads = new ArrayList<>(doubleSided ? geometry.size() * 2 : geometry.size());
        for (FaceGeometry face : geometry) {
            quads.add(face.buildQuad(sprite, -1, uScale, vScale));
            if (doubleSided) {
                quads.add(face.buildBackQuad(sprite, -1, uScale, vScale));
            }
        }
        return quads;
    }

    public int[][] captureRenderExtentsByMeta() {
        int[][] extentsByMeta = new int[yawsByMeta.length][];
        for (int meta = 0; meta < yawsByMeta.length; meta++) {
            extentsByMeta[meta] = captureRenderExtents(meta);
        }
        return extentsByMeta;
    }

    private int[] captureRenderExtents(int meta) {
        if (meta < 0 || meta >= yawsByMeta.length) {
            return null;
        }

        float yaw = yawsByMeta[meta];
        if (Float.isNaN(yaw)) {
            return null;
        }

        float[] rotatedPreTranslate = GeometryBakeUtil.rotateY(preTranslateX, preTranslateY, preTranslateZ, yaw);
        float extraTx = 0.5F + rotatedPreTranslate[0];
        float extraTy = rotatedPreTranslate[1];
        float extraTz = 0.5F + rotatedPreTranslate[2];
        float[] bounds = computeGeometryBounds(partNames, roll, pitch, yaw, false, extraTx, extraTy, extraTz);
        if (bounds == null) {
            return null;
        }

        return new int[]{
                extentAbove(bounds[4]),
                extentBelow(bounds[1]),
                extentBelow(bounds[2]),
                extentAbove(bounds[5]),
                extentBelow(bounds[0]),
                extentAbove(bounds[3])
        };
    }

    private static final float EXTENT_EPSILON = 1e-3F;

    private static int extentBelow(float min) {
        return min < -EXTENT_EPSILON ? (int) Math.ceil(-min) : 0;
    }

    private static int extentAbove(float max) {
        return max > 1.0F + EXTENT_EPSILON ? (int) Math.ceil(max - 1.0F) : 0;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return sprite;
    }
}
