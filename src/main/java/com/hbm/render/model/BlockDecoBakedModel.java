package com.hbm.render.model;

import com.hbm.blocks.BlockEnumMeta;
import com.hbm.render.loader.HFRWavefrontObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SideOnly(Side.CLIENT)
public class BlockDecoBakedModel extends AbstractWavefrontBakedModel {

    private final TextureAtlasSprite sprite;
    private final boolean forBlock;
    private final int rotation;
    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] cache = new List[4];
    private List<BakedQuad> itemQuads;

    public BlockDecoBakedModel(HFRWavefrontObject model, TextureAtlasSprite sprite, boolean forBlock, float baseScale, float tx, float ty, float tz, int rotation) {
        super(model, DefaultVertexFormats.ITEM, baseScale, tx, ty, tz, BakedModelTransforms.forDeco(BakedModelTransforms.standardBlock()));
        this.sprite = sprite;
        this.forBlock = forBlock;
        this.rotation = rotation;
    }

    public static BlockDecoBakedModel forBlock(HFRWavefrontObject model, TextureAtlasSprite sprite) {
        return new BlockDecoBakedModel(model, sprite, true, 1.0F, 0.0F, 0.0F, 0.0F, 0);
    }

    public static BlockDecoBakedModel forBlock(HFRWavefrontObject model, TextureAtlasSprite sprite, float ty) {
        return new BlockDecoBakedModel(model, sprite, true, 1.0F, 0.0F, ty, 0.0F, 0);
    }

    public static BlockDecoBakedModel forBlock(HFRWavefrontObject model, TextureAtlasSprite sprite, float ty, int rotation) {
        return new BlockDecoBakedModel(model, sprite, true, 1.0F, 0.0F, ty, 0.0F, rotation);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        if (!forBlock) {
            if (itemQuads == null) {
                itemQuads = buildItemQuads();
            }
            return itemQuads;
        }

        int orient = 1;
        if (state != null) {
            try {
                orient = (state.getValue(BlockEnumMeta.META) >> 2) & 3;
            } catch (Exception ignored) {
            }
        }

        List<BakedQuad> quads = cache[orient];
        if (quads != null) return quads;

        quads = buildQuadsForOrient(orient);
        return cache[orient] = quads;
    }

    private List<BakedQuad> buildQuadsForOrient(int orient) {
        float yawOffset;
        switch (orient) {
            case 0 -> yawOffset = 1.0F; // NORTH
            case 2 -> yawOffset = 1.5F; // WEST
            case 3 -> yawOffset = 0.5F; // EAST
            default -> yawOffset = 0.0F; // SOUTH (case 1)
        }
        float yaw = (0.5F * rotation + yawOffset) * (float) Math.PI;
        return super.bakeSimpleQuads(null, 0.0F, 0.0F, yaw, true, true, sprite);
    }

    private List<BakedQuad> buildItemQuads() {
        // Item: no shadow, no centering (+0.5), but apply base scale and translation
        return super.bakeSimpleQuads(null, 0.0F, 0.0F, 0.0F, false, false, sprite);
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return sprite;
    }
}
