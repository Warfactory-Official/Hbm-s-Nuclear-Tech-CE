package com.hbm.render.model;

import com.hbm.blocks.machine.rbmk.RBMKBase;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.tileentity.machine.rbmk.RBMKDials;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
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
public abstract class AbstractRBMKLiddedBakedModel extends AbstractWavefrontBakedModel {

    protected final TextureAtlasSprite coverTopSprite;
    protected final TextureAtlasSprite coverSideSprite;
    protected final TextureAtlasSprite glassTopSprite;
    protected final TextureAtlasSprite glassSideSprite;
    protected final boolean isInventory;

    protected static class QuadLookup {
        private final List<BakedQuad> generalQuads;
        private final List<BakedQuad>[] sideQuads;

        protected QuadLookup(List<BakedQuad> generalQuads, List<BakedQuad>[] sideQuads) {
            this.generalQuads = generalQuads;
            this.sideQuads = sideQuads;
        }

        public @NotNull List<BakedQuad> get(@Nullable EnumFacing side) {
            if (side == null) {
                return generalQuads;
            }
            return sideQuads[side.ordinal()];
        }
    }

    private QuadLookup[] worldCache = new QuadLookup[4];
    private List<BakedQuad> cacheInventory;
    private int cachedColumnHeight = Integer.MIN_VALUE;

    protected AbstractRBMKLiddedBakedModel(HFRWavefrontObject model, VertexFormat format, float baseScale, float tx, float ty, float tz,
                                           ItemCameraTransforms transforms,
                                           TextureAtlasSprite coverTop, TextureAtlasSprite coverSide,
                                           TextureAtlasSprite glassTop, TextureAtlasSprite glassSide,
                                           boolean isInventory) {
        super(model, format, baseScale, tx, ty, tz, transforms);
        coverTopSprite = coverTop;
        coverSideSprite = coverSide;
        glassTopSprite = glassTop;
        glassSideSprite = glassSide;
        this.isInventory = isInventory;
    }

    @Override
    public final @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (isInventory) {
            if (side != null) return Collections.emptyList();
            if (cacheInventory == null) cacheInventory = Collections.unmodifiableList(buildInventoryQuads());
            return cacheInventory;
        }

        int columnHeight = getColumnHeight();
        if (cachedColumnHeight != columnHeight) {
            worldCache = new QuadLookup[4];
            cachedColumnHeight = columnHeight;
        }
        int lidType = RBMKBase.LID_NONE;
        if (state != null) {
            int meta = state.getBlock().getMetaFromState(state);
            lidType = RBMKBase.metaToLid(meta);
        }

        int cacheKey = lidType + 1;
        QuadLookup cache = worldCache[cacheKey];
        if (cache == null) {
            cache = buildWorldQuads(lidType, columnHeight);
            worldCache[cacheKey] = cache;
        }
        return cache.get(side);
    }

    protected abstract @NotNull List<BakedQuad> buildInventoryQuads();

    protected abstract @NotNull QuadLookup buildWorldQuads(int lidType, int columnHeight);

    protected final @Nullable TextureAtlasSprite resolveLidTopSprite(int lidType) {
        if (lidType == RBMKBase.LID_GLASS) {
            return glassTopSprite;
        }
        if (lidType == RBMKBase.LID_STANDARD) {
            return coverTopSprite;
        }
        return null;
    }

    protected final @Nullable TextureAtlasSprite resolveLidSideSprite(int lidType) {
        if (lidType == RBMKBase.LID_GLASS) {
            return glassSideSprite;
        }
        if (lidType == RBMKBase.LID_STANDARD) {
            return coverSideSprite;
        }
        return null;
    }

    protected final void addLidBox(List<BakedQuad> quads, int lidType, float columnHeight) {
        TextureAtlasSprite lidTop = resolveLidTopSprite(lidType);
        if (lidTop == null) {
            return;
        }
        TextureAtlasSprite lidSide = resolveLidSideSprite(lidType);
        addTexturedBox(quads, 0.0F, columnHeight, 0.0F, 1.0F, columnHeight + 0.25F, 1.0F, lidTop, lidSide, lidTop);
    }

    public static int getColumnHeight() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null) return 0;
        return RBMKDials.getColumnHeightRuleValue(mc.world);
    }

    @SuppressWarnings("unchecked")
    protected static @NotNull List<BakedQuad>[] createSideArray() {
        List<BakedQuad>[] sideQuads = (List<BakedQuad>[]) new List[EnumFacing.VALUES.length];
        for (EnumFacing face : EnumFacing.VALUES) {
            sideQuads[face.ordinal()] = new ArrayList<>();
        }
        return sideQuads;
    }

    @SuppressWarnings("unchecked")
    protected static @NotNull QuadLookup freeze(List<BakedQuad> generalQuads, List<BakedQuad>[] sideQuads) {
        List<BakedQuad>[] frozenSideQuads = (List<BakedQuad>[]) new List[EnumFacing.VALUES.length];
        for (EnumFacing face : EnumFacing.VALUES) {
            frozenSideQuads[face.ordinal()] = Collections.unmodifiableList(sideQuads[face.ordinal()]);
        }
        return new QuadLookup(generalQuads, frozenSideQuads);
    }

    protected static void addTexturedBox(List<BakedQuad> quads, float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                                         TextureAtlasSprite top, TextureAtlasSprite side, TextureAtlasSprite bottom) {
        for (EnumFacing face : EnumFacing.VALUES) {
            addTexturedBoxFace(quads, minX, minY, minZ, maxX, maxY, maxZ, face, top, side, bottom);
        }
    }

    protected static void addTexturedBoxFace(List<BakedQuad> quads, float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                                             EnumFacing face, TextureAtlasSprite top, TextureAtlasSprite side, TextureAtlasSprite bottom) {
        FaceBakery bakery = new FaceBakery();
        Vector3f from = new Vector3f(minX * 16.0f, minY * 16.0f, minZ * 16.0f);
        Vector3f to = new Vector3f(maxX * 16.0f, maxY * 16.0f, maxZ * 16.0f);
        TextureAtlasSprite sprite = face == EnumFacing.UP ? top : (face == EnumFacing.DOWN ? bottom : side);

        Vector3f uvFrom = new Vector3f((minX % 1f) * 16f, (minY % 1f) * 16f, (minZ % 1f) * 16f);
        Vector3f uvTo = new Vector3f(uvFrom.x + (maxX - minX) * 16f, uvFrom.y + (maxY - minY) * 16f, uvFrom.z + (maxZ - minZ) * 16f);

        if ((maxX - minX) == 1f) { uvFrom.x = 0f; uvTo.x = 16f; }
        if ((maxY - minY) == 1f) { uvFrom.y = 0f; uvTo.y = 16f; }
        if ((maxZ - minZ) == 1f) { uvFrom.z = 0f; uvTo.z = 16f; }

        BlockFaceUV uv = makeFaceUV(face, uvFrom, uvTo);
        BlockPartFace partFace = new BlockPartFace(face, -1, "", uv);
        BakedQuad quad = bakery.makeBakedQuad(from, to, partFace, sprite, face, TRSRTransformation.identity(), null, true, true);
        quads.add(quad);
    }
}
