package com.hbm.render.model;

import com.hbm.blocks.machine.rbmk.RBMKBase;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.rbmk.RBMKDials;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class RBMKColumnBakedModel extends AbstractWavefrontBakedModel {

    protected final TextureAtlasSprite topSprite;
    protected final TextureAtlasSprite sideSprite;
    protected final TextureAtlasSprite coverTopSprite;
    protected final TextureAtlasSprite coverSideSprite;
    protected final TextureAtlasSprite glassTopSprite;
    protected final TextureAtlasSprite glassSideSprite;

    protected final boolean isInventory;

    private List<BakedQuad> cacheNoLid;
    private List<BakedQuad> cacheNormalLid;
    private List<BakedQuad> cacheGlassLid;
    private List<BakedQuad> cacheInventory;
    private int cacheNoLidHeight = Integer.MIN_VALUE;
    private int cacheNormalLidHeight = Integer.MIN_VALUE;
    private int cacheGlassLidHeight = Integer.MIN_VALUE;

    public RBMKColumnBakedModel(
            TextureAtlasSprite top, TextureAtlasSprite side,
            TextureAtlasSprite coverTop, TextureAtlasSprite coverSide,
            TextureAtlasSprite glassTop, TextureAtlasSprite glassSide,
            boolean isInventory) {
        super(ResourceManager.rbmk_element, DefaultVertexFormats.BLOCK, 1.0F, 0.5F, 0.0F, 0.5F, BakedModelTransforms.rbmkColumn());
        this.topSprite = top;
        this.sideSprite = side;
        this.coverTopSprite = coverTop;
        this.coverSideSprite = coverSide;
        this.glassTopSprite = glassTop;
        this.glassSideSprite = glassSide;
        this.isInventory = isInventory;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        if (isInventory) {
            if (cacheInventory == null) cacheInventory = Collections.unmodifiableList(buildInventoryQuads());
            return cacheInventory;
        }

        int columnHeight = getColumnHeight();
        int lidType = RBMKBase.LID_NONE;
        if (state != null) {
            int meta = state.getBlock().getMetaFromState(state);
            lidType = RBMKBase.metaToLid(meta);
        }

        if (lidType == RBMKBase.LID_STANDARD) {
            if (cacheNormalLid == null || cacheNormalLidHeight != columnHeight) {
                cacheNormalLid = Collections.unmodifiableList(buildWorldQuads(RBMKBase.LID_STANDARD, columnHeight));
                cacheNormalLidHeight = columnHeight;
            }
            return cacheNormalLid;
        } else if (lidType == RBMKBase.LID_GLASS) {
            if (cacheGlassLid == null || cacheGlassLidHeight != columnHeight) {
                cacheGlassLid = Collections.unmodifiableList(buildWorldQuads(RBMKBase.LID_GLASS, columnHeight));
                cacheGlassLidHeight = columnHeight;
            }
            return cacheGlassLid;
        } else if (lidType == RBMKBase.LID_NONE) {
            if (cacheNoLid == null || cacheNoLidHeight != columnHeight) {
                cacheNoLid = Collections.unmodifiableList(buildWorldQuads(RBMKBase.LID_NONE, columnHeight));
                cacheNoLidHeight = columnHeight;
            }
            return cacheNoLid;
        }
        return Collections.unmodifiableList(buildWorldQuads(RBMKBase.LID_NULL, columnHeight));
    }

    protected List<BakedQuad> buildInventoryQuads() {
        List<BakedQuad> quads = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            addTexturedBox(quads, 0.0F, i, 0.0F, 1.0F, i + 1.0F, 1.0F, topSprite, sideSprite, topSprite);
        }
        return quads;
    }

    protected List<BakedQuad> buildWorldQuads(int lidType, int columnHeight) {
        List<BakedQuad> quads = new ArrayList<>();

        addTexturedBox(quads, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, topSprite, sideSprite, topSprite);

        if (lidType != RBMKBase.LID_NONE && lidType != RBMKBase.LID_NULL) {
            TextureAtlasSprite lidTop = (lidType == RBMKBase.LID_GLASS) ? glassTopSprite : coverTopSprite;
            TextureAtlasSprite lidSide = (lidType == RBMKBase.LID_GLASS) ? glassSideSprite : coverSideSprite;
            addTexturedBox(quads, 0.0F, columnHeight, 0.0F, 1.0F, columnHeight + 0.25F, 1.0F, lidTop, lidSide, lidTop);
        }


        return quads;
    }

    public static int getColumnHeight() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null) return 0;
        return RBMKDials.getColumnHeight(mc.world) + 2;
    }

    // Th3_Sl1ze: I'd consider to move this somewhere else..
    public static void addTexturedBox(List<BakedQuad> quads, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, TextureAtlasSprite top, TextureAtlasSprite side, TextureAtlasSprite bottom) {
        FaceBakery bakery = new FaceBakery();
        Vector3f from = new Vector3f(minX * 16.0f, minY * 16.0f, minZ * 16.0f);
        Vector3f to = new Vector3f(maxX * 16.0f, maxY * 16.0f, maxZ * 16.0f);

        for (EnumFacing face : EnumFacing.VALUES) {
            TextureAtlasSprite sprite = face == EnumFacing.UP ? top : (face == EnumFacing.DOWN ? bottom : side);

            Vector3f uvFrom = new Vector3f((minX % 1f) * 16f, (minY % 1f) * 16f, (minZ % 1f) * 16f);
            Vector3f uvTo = new Vector3f(uvFrom.x + (maxX - minX) * 16f, uvFrom.y + (maxY - minY) * 16f, uvFrom.z + (maxZ - minZ) * 16f);

            if ((maxX - minX) == 1f) { uvFrom.x = 0f; uvTo.x = 16f; }
            if ((maxY - minY) == 1f) { uvFrom.y = 0f; uvTo.y = 16f; }
            if ((maxZ - minZ) == 1f) { uvFrom.z = 0f; uvTo.z = 16f; }

            BlockFaceUV uv = AbstractBakedModel.makeFaceUV(face, uvFrom, uvTo);
            BlockPartFace partFace = new BlockPartFace(face, -1, "", uv);
            BakedQuad quad = bakery.makeBakedQuad(from, to, partFace, sprite, face, TRSRTransformation.identity(), null, true, true);
            quads.add(quad);
        }
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return sideSprite;
    }
}
