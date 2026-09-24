package com.hbm.render.model;

import com.hbm.render.loader.HFRWavefrontObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
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
    private final float[][] worldTranslateByMeta;
    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] cache;

    public StaticMetaWavefrontBakedModel(HFRWavefrontObject model, TextureAtlasSprite sprite, float[] yawsByMeta,
                                         @Nullable String[] partNames, float roll, float pitch,
                                         boolean doubleSided, float uScale, float vScale,
                                         float preTranslateX, float preTranslateY, float preTranslateZ,
                                         float translateX, float translateY, float translateZ) {
        this(model, sprite, yawsByMeta, partNames, roll, pitch, doubleSided, uScale, vScale,
                preTranslateX, preTranslateY, preTranslateZ, translateX, translateY, translateZ, null);
    }

    public StaticMetaWavefrontBakedModel(HFRWavefrontObject model, TextureAtlasSprite sprite, float[] yawsByMeta,
                                         @Nullable String[] partNames, float roll, float pitch,
                                         boolean doubleSided, float uScale, float vScale,
                                         float preTranslateX, float preTranslateY, float preTranslateZ,
                                         float translateX, float translateY, float translateZ,
                                         float[] @Nullable [] worldTranslateByMeta) {
        super(model, DefaultVertexFormats.BLOCK, 1.0F, translateX, translateY, translateZ,
                ItemCameraTransforms.DEFAULT);
        this.worldTranslateByMeta = worldTranslateByMeta;
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

        quads = Collections.unmodifiableList(buildQuads(yaw, meta));
        cache[meta] = quads;
        return quads;
    }

    private float[] worldTranslate(int meta) {
        if (worldTranslateByMeta == null || meta < 0 || meta >= worldTranslateByMeta.length
                || worldTranslateByMeta[meta] == null) {
            return null;
        }
        return worldTranslateByMeta[meta];
    }

    private List<BakedQuad> buildQuads(float yaw, int meta) {
        float[] rotatedPreTranslate = GeometryBakeUtil.rotateY(preTranslateX, preTranslateY, preTranslateZ, yaw);
        float extraTx = 0.5F + rotatedPreTranslate[0];
        float extraTy = rotatedPreTranslate[1];
        float extraTz = 0.5F + rotatedPreTranslate[2];

        float[] world = worldTranslate(meta);
        if (world != null) {
            extraTx += world[0];
            extraTy += world[1];
            extraTz += world[2];
        }

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

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return sprite;
    }
}
