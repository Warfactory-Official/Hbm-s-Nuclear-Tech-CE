package com.hbm.render.model;

import com.hbm.blocks.machine.rbmk.RBMKBase;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.HFRWavefrontObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

@SideOnly(Side.CLIENT)
public class RBMKControlBakedModel extends AbstractWavefrontBakedModel {

    private final TextureAtlasSprite baseTopSprite;
    private TextureAtlasSprite baseBottomSprite;
    private final TextureAtlasSprite baseSideSprite;

    private final TextureAtlasSprite pipeTopSprite;
    private final TextureAtlasSprite pipeSideSprite;

    private final TextureAtlasSprite coverTopSprite;
    private final TextureAtlasSprite coverSideSprite;
    private final TextureAtlasSprite glassTopSprite;
    private final TextureAtlasSprite glassSideSprite;

    private final TextureAtlasSprite lidSprite;
    private final boolean isInventory;

    private List<BakedQuad> cacheInventory;
    private List<BakedQuad> cacheWorldNoLid;
    private List<BakedQuad> cacheWorldNormalLid;
    private List<BakedQuad> cacheWorldGlassLid;
    private int cacheWorldNoLidHeight = Integer.MIN_VALUE;
    private int cacheWorldNormalLidHeight = Integer.MIN_VALUE;
    private int cacheWorldGlassLidHeight = Integer.MIN_VALUE;

    public RBMKControlBakedModel(TextureAtlasSprite baseTop, TextureAtlasSprite baseSide,
                                 TextureAtlasSprite pipeTop, TextureAtlasSprite pipeSide,
                                 TextureAtlasSprite coverTop, TextureAtlasSprite coverSide,
                                 TextureAtlasSprite glassTop, TextureAtlasSprite glassSide,
                                 TextureAtlasSprite lidSprite,
                                 boolean isInventory) {
        super((HFRWavefrontObject) ResourceManager.rbmk_rods, DefaultVertexFormats.BLOCK,
                1.0F, 0.5F, 0.0F, 0.5F,
                BakedModelTransforms.rbmkColumn());
        this.baseTopSprite = baseTop;
        this.baseSideSprite = baseSide;
        this.baseBottomSprite = baseTop;

        this.pipeTopSprite = pipeTop;
        this.pipeSideSprite = pipeSide;

        this.coverTopSprite = coverTop;
        this.coverSideSprite = coverSide;
        this.glassTopSprite = glassTop;
        this.glassSideSprite = glassSide;

        this.lidSprite = lidSprite;
        this.isInventory = isInventory;
    }

    public RBMKControlBakedModel setBottomSprite(TextureAtlasSprite baseBottom) {
        this.baseBottomSprite = baseBottom;
        return this;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        if (isInventory) {
            if (cacheInventory == null) cacheInventory = Collections.unmodifiableList(buildInventoryQuads());
            return cacheInventory;
        }

        int columnHeight = RBMKColumnBakedModel.getColumnHeight();
        int lidType = RBMKBase.LID_NONE;
        if (state != null) {
            int meta = state.getBlock().getMetaFromState(state);
            lidType = RBMKBase.metaToLid(meta);
        }

        if (lidType == RBMKBase.LID_STANDARD) {
            if (cacheWorldNormalLid == null || cacheWorldNormalLidHeight != columnHeight) {
                cacheWorldNormalLid = Collections.unmodifiableList(buildWorldQuads(RBMKBase.LID_STANDARD, columnHeight));
                cacheWorldNormalLidHeight = columnHeight;
            }
            return cacheWorldNormalLid;
        } else if (lidType == RBMKBase.LID_GLASS) {
            if (cacheWorldGlassLid == null || cacheWorldGlassLidHeight != columnHeight) {
                cacheWorldGlassLid = Collections.unmodifiableList(buildWorldQuads(RBMKBase.LID_GLASS, columnHeight));
                cacheWorldGlassLidHeight = columnHeight;
            }
            return cacheWorldGlassLid;
        } else if (lidType == RBMKBase.LID_NONE) {
            if (cacheWorldNoLid == null || cacheWorldNoLidHeight != columnHeight) {
                cacheWorldNoLid = Collections.unmodifiableList(buildWorldQuads(RBMKBase.LID_NONE, columnHeight));
                cacheWorldNoLidHeight = columnHeight;
            }
            return cacheWorldNoLid;
        }
        return Collections.unmodifiableList(buildWorldQuads(RBMKBase.LID_NULL, columnHeight));
    }

    private List<BakedQuad> buildInventoryQuads() {
        List<BakedQuad> quads = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            RBMKColumnBakedModel.addTexturedBox(quads, 0.0F, i, 0.0F, 1.0F, i + 1.0F, 1.0F, baseTopSprite, baseSideSprite, baseBottomSprite);
        }

        addPipes(quads, 4.0F);

        if (lidSprite != null) {
            quads.addAll(bakeWavefrontAtYOffset(Collections.singleton("Lid"), 3.0F, lidSprite));
        }

        return quads;
    }

    private List<BakedQuad> buildWorldQuads(int lidType, float columnHeight) {
        List<BakedQuad> quads = new ArrayList<>();

        RBMKColumnBakedModel.addTexturedBox(quads, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, baseTopSprite, baseSideSprite, baseBottomSprite);

        if (lidType != RBMKBase.LID_NONE && lidType != RBMKBase.LID_NULL) {
            if (coverTopSprite != null) {
                TextureAtlasSprite lidTop = (lidType == RBMKBase.LID_GLASS) ? glassTopSprite : coverTopSprite;
                TextureAtlasSprite lidSide = (lidType == RBMKBase.LID_GLASS) ? glassSideSprite : coverSideSprite;
                RBMKColumnBakedModel.addTexturedBox(quads, 0.0F, columnHeight, 0.0F, 1.0F, columnHeight + 0.25F, 1.0F, lidTop, lidSide, lidTop);
            }
        } else if(lidType != RBMKBase.LID_NULL) {
            addPipes(quads, columnHeight);
        }

        return quads;
    }

    private void addPipes(List<BakedQuad> quads, float yBase) {
        RBMKColumnBakedModel.addTexturedBox(quads, 0.0625F, yBase, 0.0625F, 0.4375F, yBase + 0.125F, 0.4375F, pipeTopSprite, pipeSideSprite, pipeTopSprite);
        RBMKColumnBakedModel.addTexturedBox(quads, 0.0625F, yBase, 0.5625F, 0.4375F, yBase + 0.125F, 0.9375F, pipeTopSprite, pipeSideSprite, pipeTopSprite);
        RBMKColumnBakedModel.addTexturedBox(quads, 0.5625F, yBase, 0.5625F, 0.9375F, yBase + 0.125F, 0.9375F, pipeTopSprite, pipeSideSprite, pipeTopSprite);
        RBMKColumnBakedModel.addTexturedBox(quads, 0.5625F, yBase, 0.0625F, 0.9375F, yBase + 0.125F, 0.4375F, pipeTopSprite, pipeSideSprite, pipeTopSprite);
    }

    private List<BakedQuad> bakeWavefrontAtYOffset(Set<String> parts, float yOffsetBlocks, TextureAtlasSprite sprite) {
        float oldTy = this.ty;
        try {
            this.ty = oldTy + yOffsetBlocks;
            return bakeSimpleQuads(parts, 0, 0, 0, true, false, sprite);
        } finally {
            this.ty = oldTy;
        }
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return baseSideSprite;
    }
}
