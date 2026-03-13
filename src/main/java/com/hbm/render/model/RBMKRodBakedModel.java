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

    private List<BakedQuad> cacheNullSideNoLid;
    private List<BakedQuad> cacheNullSideNormalLid;
    private List<BakedQuad> cacheNullSideGlassLid;

    public RBMKRodBakedModel(TextureAtlasSprite side,
                             TextureAtlasSprite inner, TextureAtlasSprite cap,
                             TextureAtlasSprite coverTop, TextureAtlasSprite coverSide,
                             TextureAtlasSprite glassTop, TextureAtlasSprite glassSide) {
        super(ResourceManager.rbmk_element, DefaultVertexFormats.BLOCK,
                1.0F, 0.5F, 0.0F, 0.5F, BakedModelTransforms.rbmkColumn());
        this.sideSprite = side;
        this.innerSprite = inner;
        this.capSprite = cap;
        this.coverTopSprite = coverTop;
        this.coverSideSprite = coverSide;
        this.glassTopSprite = glassTop;
        this.glassSideSprite = glassSide;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state,
                                             @Nullable EnumFacing side, long rand) {
        // Because isOpaqueCube is false, put all geometry into the unculled (side == null) list to ensure it renders
        if (side != null) {
            return Collections.emptyList();
        }
        int lidType = RBMKBase.LID_NONE;
        if (state != null) {
            int meta = state.getBlock().getMetaFromState(state);
            lidType = RBMKBase.metaToLid(meta);
        }

        if (lidType == RBMKBase.LID_STANDARD) {
            if (cacheNullSideNormalLid == null) {
                cacheNullSideNormalLid = Collections.unmodifiableList(buildNullSideQuads(RBMKBase.LID_STANDARD));
            }
            return cacheNullSideNormalLid;
        } else if (lidType == RBMKBase.LID_GLASS) {
            if (cacheNullSideGlassLid == null) {
                cacheNullSideGlassLid = Collections.unmodifiableList(buildNullSideQuads(RBMKBase.LID_GLASS));
            }
            return cacheNullSideGlassLid;
        } else if(lidType == RBMKBase.LID_NONE) {
            if (cacheNullSideNoLid == null) cacheNullSideNoLid = Collections.unmodifiableList(buildNullSideQuads(RBMKBase.LID_NONE));
            return cacheNullSideNoLid;
        }
        return Collections.unmodifiableList(buildNullSideQuads(RBMKBase.LID_NULL));
    }

    private List<BakedQuad> buildNullSideQuads(int lidType) {
        List<BakedQuad> quads = new ArrayList<>();

        quads.addAll(bakeSimpleQuads(Collections.singletonList("Inner"), 0, 0, 0, true, false, innerSprite));
        quads.addAll(bakeSimpleQuads(Collections.singletonList("Cap"), 0, 0, 0, true, false, capSprite));

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
            RBMKColumnBakedModel.addTexturedBox(quads, 0.0F, RBMKColumnBakedModel.getColumnHeight(), 0.0F, 1.0F, RBMKColumnBakedModel.getColumnHeight() + 0.25F, 1.0F, lidTop, lidSide, lidTop);
        }

        return quads;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return sideSprite;
    }
}