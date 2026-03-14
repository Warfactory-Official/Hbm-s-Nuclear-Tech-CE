package com.hbm.render.model;

import com.hbm.blocks.machine.rbmk.RBMKBase;
import com.hbm.main.ResourceManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Vector3f;

import java.util.*;

@SideOnly(Side.CLIENT)
public class RBMKRodBakedModel extends AbstractWavefrontBakedModel {

    private final TextureAtlasSprite sideSprite;
    private final TextureAtlasSprite innerSprite;
    private final TextureAtlasSprite capSprite;
    private final TextureAtlasSprite coverTopSprite;
    private final TextureAtlasSprite coverSideSprite;
    private final TextureAtlasSprite glassTopSprite;
    private final TextureAtlasSprite glassSideSprite;

    private final boolean isInventory;

    private List<BakedQuad> cacheNullSideNoLid;
    private List<BakedQuad> cacheNullSideNormalLid;
    private List<BakedQuad> cacheNullSideGlassLid;
    private List<BakedQuad> cacheInventory;
    private int cacheNullSideNoLidHeight = Integer.MIN_VALUE;
    private int cacheNullSideNormalLidHeight = Integer.MIN_VALUE;
    private int cacheNullSideGlassLidHeight = Integer.MIN_VALUE;

    public RBMKRodBakedModel(TextureAtlasSprite side,
                             TextureAtlasSprite inner, TextureAtlasSprite cap,
                             TextureAtlasSprite coverTop, TextureAtlasSprite coverSide,
                             TextureAtlasSprite glassTop, TextureAtlasSprite glassSide,
                             boolean isInventory) {
        super(ResourceManager.rbmk_element, DefaultVertexFormats.BLOCK,
                1.0F, 0.5F, 0.0F, 0.5F, BakedModelTransforms.rbmkColumn());
        this.sideSprite = side;
        this.innerSprite = inner;
        this.capSprite = cap;
        this.coverTopSprite = coverTop;
        this.coverSideSprite = coverSide;
        this.glassTopSprite = glassTop;
        this.glassSideSprite = glassSide;
        this.isInventory = isInventory;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state,
                                             @Nullable EnumFacing side, long rand) {
        if (side != null) {
            return Collections.emptyList();
        }

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
            if (cacheNullSideNormalLid == null || cacheNullSideNormalLidHeight != columnHeight) {
                cacheNullSideNormalLid = Collections.unmodifiableList(buildNullSideQuads(RBMKBase.LID_STANDARD, columnHeight));
                cacheNullSideNormalLidHeight = columnHeight;
            }
            return cacheNullSideNormalLid;
        } else if (lidType == RBMKBase.LID_GLASS) {
            if (cacheNullSideGlassLid == null || cacheNullSideGlassLidHeight != columnHeight) {
                cacheNullSideGlassLid = Collections.unmodifiableList(buildNullSideQuads(RBMKBase.LID_GLASS, columnHeight));
                cacheNullSideGlassLidHeight = columnHeight;
            }
            return cacheNullSideGlassLid;
        } else if(lidType == RBMKBase.LID_NONE) {
            if (cacheNullSideNoLid == null || cacheNullSideNoLidHeight != columnHeight) {
                cacheNullSideNoLid = Collections.unmodifiableList(buildNullSideQuads(RBMKBase.LID_NONE, columnHeight));
                cacheNullSideNoLidHeight = columnHeight;
            }
            return cacheNullSideNoLid;
        }
        return Collections.unmodifiableList(buildNullSideQuads(RBMKBase.LID_NULL, columnHeight));
    }

    private List<BakedQuad> buildInventoryQuads() {
        List<BakedQuad> quads = new ArrayList<>();
        FaceBakery bakery = new FaceBakery();
        EnumFacing[] baseFaces = {
                EnumFacing.NORTH, EnumFacing.SOUTH,
                EnumFacing.EAST, EnumFacing.WEST
        };

        for (int i = 0; i < 4; i++) {
            quads.addAll(bakeWavefrontAtYOffset(Collections.singleton("Inner"), i, innerSprite));
            quads.addAll(bakeWavefrontAtYOffset(Collections.singleton("Cap"), i, capSprite));

            Vector3f from = new Vector3f(0, i * 16.0f, 0);
            Vector3f to = new Vector3f(16.0f, (i + 1) * 16.0f, 16.0f);

            for (EnumFacing face : baseFaces) {
                BlockFaceUV uv = AbstractBakedModel.makeFaceUV(face, new Vector3f(0, 0, 0), new Vector3f(16.0f, 16.0f, 16.0f));
                BlockPartFace partFace = new BlockPartFace(face, -1, "", uv);
                BakedQuad quad = bakery.makeBakedQuad(from, to, partFace, sideSprite, face,
                        TRSRTransformation.identity(), null, true, true);
                quads.add(quad);
            }
        }
        return quads;
    }

    private List<BakedQuad> buildNullSideQuads(int lidType, float columnHeight) {
        List<BakedQuad> quads = new ArrayList<>();

        quads.addAll(bakeSimpleQuads(Collections.singleton("Inner"), 0, 0, 0, true, false, innerSprite));
        quads.addAll(bakeSimpleQuads(Collections.singleton("Cap"), 0, 0, 0, true, false, capSprite));

        FaceBakery bakery = new FaceBakery();
        Vector3f from = new Vector3f(0, 0, 0);
        Vector3f to = new Vector3f(16, 16, 16);

        EnumFacing[] baseFaces = {
                EnumFacing.NORTH, EnumFacing.SOUTH,
                EnumFacing.EAST, EnumFacing.WEST
        };

        for (EnumFacing face : baseFaces) {
            BlockFaceUV uv = AbstractBakedModel.makeFaceUV(face, from, to);
            BlockPartFace partFace = new BlockPartFace(face, -1, "", uv);
            BakedQuad quad = bakery.makeBakedQuad(from, to, partFace, sideSprite, face,
                    TRSRTransformation.identity(), null, true, true);
            quads.add(quad);
        }

        if (lidType != RBMKBase.LID_NONE && lidType != RBMKBase.LID_NULL) {
            TextureAtlasSprite lidTop = (lidType == RBMKBase.LID_GLASS) ? glassTopSprite : coverTopSprite;
            TextureAtlasSprite lidSide = (lidType == RBMKBase.LID_GLASS) ? glassSideSprite : coverSideSprite;
            RBMKColumnBakedModel.addTexturedBox(quads, 0.0F, columnHeight, 0.0F, 1.0F, columnHeight + 0.25F, 1.0F, lidTop, lidSide, lidTop);
        }

        return quads;
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
        return sideSprite;
    }
}
