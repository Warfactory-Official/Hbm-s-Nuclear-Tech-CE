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
    private final int rotationOffset;
    private final boolean rotationInLowBits;
    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] cache = new List[4];
    private List<BakedQuad> itemQuads;

    public BlockDecoBakedModel(HFRWavefrontObject model, TextureAtlasSprite sprite, boolean forBlock, float baseScale, float tx, float ty, float tz, int rotationOffset, boolean rotationInLowBits) {
        super(model, DefaultVertexFormats.ITEM, baseScale, tx, ty, tz, BakedModelTransforms.forDeco(BakedModelTransforms.standardBlock()));
        this.sprite = sprite;
        this.forBlock = forBlock;
        this.rotationOffset = rotationOffset;
        this.rotationInLowBits = rotationInLowBits;
    }

    public static BlockDecoBakedModel forBlock(HFRWavefrontObject model, TextureAtlasSprite sprite) {
        return new BlockDecoBakedModel(model, sprite, true, 1.0F, 0.0F, 0.0F, 0.0F, 0, false);
    }

    public static BlockDecoBakedModel forBlock(HFRWavefrontObject model, TextureAtlasSprite sprite, float ty) {
        return new BlockDecoBakedModel(model, sprite, true, 1.0F, 0.0F, ty, 0.0F, 0, true);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        if (!forBlock) {
            if (itemQuads == null) itemQuads = super.bakeSimpleQuads(null, 0.0F, 0.0F, 0.0F, true, false, sprite);
            return itemQuads;
        }

        int orient = 1;
        if (state != null) {
            int meta = state.getValue(BlockEnumMeta.META);
            orient = rotationInLowBits ? (meta & 3) : ((meta >> 2) & 3);
        }

        List<BakedQuad> quads = cache[orient];
        if (quads != null) return quads;

        quads = buildQuadsForOrient(orient);
        return cache[orient] = quads;
    }

    private List<BakedQuad> buildQuadsForOrient(int orient) {
        float yaw = 0;

        if (rotationInLowBits) {
            yaw = switch (orient) {
                case 0 -> 0.5F;  // South
                case 1 -> 0.0F;  // West
                case 2 -> 1.5F;  // North
                case 3 -> 1.0F;  // East
                default -> 0.0F;
            };
        } else {
            yaw = switch (orient) {
                case 0 -> 1.0F;  // South
                case 1 -> 0.0F;  // North
                case 2 -> 1.5F;  // East
                case 3 -> 0.5F;  // West
                default -> 0.0F;
            };
        }

        float totalYaw = (yaw + (0.5F * rotationOffset)) * (float) Math.PI;
        return super.bakeSimpleQuads(null, 0.0F, 0.0F, totalYaw, true, true, sprite);
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return sprite;
    }
}
