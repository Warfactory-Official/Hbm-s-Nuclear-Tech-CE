package com.hbm.render.model;

import com.hbm.blocks.network.CranePartitioner;
import com.hbm.render.loader.HFRWavefrontObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
// FIXME fucking itemcameratransforms again..
@SideOnly(Side.CLIENT)
public class CranePartitionerBakedModel extends AbstractWavefrontBakedModel {

    private static final float RAD_90 = (float) Math.PI / 2.0F;
    private static final float RAD_180 = (float) Math.PI;
    private static final float RAD_270 = RAD_90 * 3.0F;

    private final TextureAtlasSprite sideSprite;
    private final TextureAtlasSprite topSprite;
    private final TextureAtlasSprite backSprite;
    private final TextureAtlasSprite beltSprite;
    private final TextureAtlasSprite innerSprite;
    private final TextureAtlasSprite innerSideSprite;

    private final boolean forBlock;
    private final float itemYaw;
    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] cache = new List[6];
    private List<BakedQuad> itemQuads;

    private CranePartitionerBakedModel(HFRWavefrontObject model, VertexFormat format, float baseScale, float tx, float ty, float tz,
                                       TextureAtlasSprite sideSprite, TextureAtlasSprite topSprite, TextureAtlasSprite backSprite,
                                       TextureAtlasSprite beltSprite, TextureAtlasSprite innerSprite, TextureAtlasSprite innerSideSprite,
                                       boolean forBlock, float itemYaw) {
        super(model, format, baseScale, tx, ty, tz, BakedModelTransforms.pipeItem());
        this.sideSprite = sideSprite;
        this.topSprite = topSprite;
        this.backSprite = backSprite;
        this.beltSprite = beltSprite;
        this.innerSprite = innerSprite;
        this.innerSideSprite = innerSideSprite;
        this.forBlock = forBlock;
        this.itemYaw = itemYaw;
    }

    public static CranePartitionerBakedModel forBlock(HFRWavefrontObject model, TextureAtlasSprite side, TextureAtlasSprite top, TextureAtlasSprite back,
                                                      TextureAtlasSprite belt, TextureAtlasSprite inner, TextureAtlasSprite innerSide) {
        return new CranePartitionerBakedModel(model, DefaultVertexFormats.BLOCK, 1.0F, 0.5F, 0.0F, 0.5F,
                side, top, back, belt, inner, innerSide, true, 0.0F
        );
    }

    public static CranePartitionerBakedModel forItem(HFRWavefrontObject model, TextureAtlasSprite side, TextureAtlasSprite top, TextureAtlasSprite back,
                                                     TextureAtlasSprite belt, TextureAtlasSprite inner, TextureAtlasSprite innerSide) {
        return new CranePartitionerBakedModel(
                model,
                DefaultVertexFormats.ITEM,
                1.0F,
                0.0F,
                -0.5F,
                0.0F,
                side,
                top,
                back,
                belt,
                inner,
                innerSide,
                false,
                RAD_90
        );
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side != null) {
            return Collections.emptyList();
        }

        if (!forBlock) {
            if (itemQuads == null) {
                itemQuads = buildItemQuads();
            }
            return itemQuads;
        }
        EnumFacing facing = EnumFacing.SOUTH;
        if (state != null) {
            try {
                facing = state.getValue(CranePartitioner.FACING);
            } catch (Exception ignored) {
            }
        }
        int index = facing.ordinal();
        List<BakedQuad> quads = cache[index];
        if (quads != null) return quads;
        quads = buildBlockQuads(facing);
        return cache[index] = quads;
    }

    private List<BakedQuad> buildBlockQuads(EnumFacing facing) {
        float yaw = yawForFacing(facing);
        List<BakedQuad> quads = new ArrayList<>(6);
        addPart(quads, "Side", yaw, true, false, sideSprite);
        addPart(quads, "Back", yaw, true, false, backSprite);
        addPart(quads, "Top_Top.001", yaw, true, false, topSprite);
        addPart(quads, "Inner", yaw, true, false, innerSprite);
        addPart(quads, "InnerSide", yaw, true, false, innerSideSprite);
        addPart(quads, "Belt", yaw, true, false, beltSprite);
        return quads;
    }

    private List<BakedQuad> buildItemQuads() {
        List<BakedQuad> quads = new ArrayList<>(6);
        addPart(quads, "Side", itemYaw, false, false, sideSprite);
        addPart(quads, "Back", itemYaw, false, false, backSprite);
        addPart(quads, "Top_Top.001", itemYaw, false, false, topSprite);
        addPart(quads, "Inner", itemYaw, false, false, innerSprite);
        addPart(quads, "InnerSide", itemYaw, false, false, innerSideSprite);
        addPart(quads, "Belt", itemYaw, false, false, beltSprite);
        return quads;
    }

    private void addPart(List<BakedQuad> target,
                         String part,
                         float yaw,
                         boolean shade,
                         boolean center,
                         TextureAtlasSprite sprite) {
        target.addAll(bakeSimpleQuads(Collections.singleton(part), 0.0F, 0.0F, yaw, shade, center, sprite));
    }

    private static float yawForFacing(EnumFacing facing) {
        return switch (facing) {
            case NORTH -> RAD_90;
            case WEST -> RAD_180;
            case SOUTH -> RAD_270;
            default -> 0.0F; // EAST and others
        };
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return sideSprite;
    }
}
