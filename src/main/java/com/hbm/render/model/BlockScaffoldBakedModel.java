package com.hbm.render.model;

import com.hbm.blocks.generic.BlockScaffold;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class BlockScaffoldBakedModel extends AbstractWavefrontBakedModel {
    private final TextureAtlasSprite sprite;
    private final boolean isInventory;
    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] cache = new List[4];
    private List<BakedQuad> inventoryCache;

    public BlockScaffoldBakedModel(HFRWavefrontObject model, TextureAtlasSprite sprite, boolean isInventory,
                                   float baseScale, float tx, float ty, float tz) {
        super(model, isInventory ? DefaultVertexFormats.ITEM : DefaultVertexFormats.BLOCK, baseScale, tx, ty, tz,
                BakedModelTransforms.isbrh());

        this.sprite = sprite;
        this.isInventory = isInventory;
    }

    public static BlockScaffoldBakedModel forBlock(HFRWavefrontObject model, TextureAtlasSprite sprite) {
        return new BlockScaffoldBakedModel(model, sprite, false, 1.0F, 0.0F, 0.0F, 0.0F);
    }

    public static BlockScaffoldBakedModel forItem(HFRWavefrontObject model, TextureAtlasSprite sprite) {
        return new BlockScaffoldBakedModel(model, sprite, true, 1.0F, 0.5F, 0.0F, 0.5F);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        if (isInventory) {
            if (inventoryCache != null) return inventoryCache;
            return inventoryCache = Collections.unmodifiableList(buildItemQuads());
        }

        if (state == null) {
            return Collections.emptyList();
        }

        BlockScaffold.EnumScaffoldOrient orient = state.getValue(BlockScaffold.ORIENT);
        int orientIndex = orient.ordinal();

        float yaw = (float) (-Math.PI * 0.5);
        float pitch = 0.0f;
        float extraTx = 0.0f;
        float extraTy = -0.5f;
        float extraTz = 0.0f;

        switch (orient) {
            case VERTICAL_EW:
                pitch = (float) (Math.PI * -0.5);
                yaw = (float) (-Math.PI);
                extraTx = -0.5f;
                extraTy = 0.0f;
                break;
            case HORIZONTAL_EW:
                yaw = (float) (-Math.PI);
                break;
            case VERTICAL_NS:
                pitch = (float) (Math.PI * -0.5);
                extraTy = 0.0f;
                extraTz = 0.5f;
                break;
            case HORIZONTAL_NS:
            default:
                break;
        }

        if (cache[orientIndex] != null) return cache[orientIndex];
        return cache[orientIndex] = Collections.unmodifiableList(
                buildWorldQuads(pitch, yaw, extraTx, extraTy, extraTz));
    }

    private List<BakedQuad> buildWorldQuads(float pitch, float yaw, float extraTx, float extraTy, float extraTz) {
        return new ArrayList<>(
                bakeSimpleQuads(Collections.singleton("Scaffold"), 0, pitch, yaw, true, true, sprite, -1, extraTx,
                        extraTy, extraTz));
    }

    private List<BakedQuad> buildItemQuads() {
        return new ArrayList<>(
                bakeSimpleQuads(Collections.singleton("Scaffold"), 0, 0, 0, true, false, sprite));
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return sprite;
    }

    @Override
    public boolean isAmbientOcclusion(@NotNull IBlockState state) {
        return true;
    }
}
