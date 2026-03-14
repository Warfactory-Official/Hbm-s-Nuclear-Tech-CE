package com.hbm.render.model;

import com.hbm.blocks.network.FluidDuctBox;
import com.hbm.blocks.network.FluidDuctBoxExhaust;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import java.util.concurrent.locks.StampedLock;

@SideOnly(Side.CLIENT)
public class DuctBakedModel extends AbstractBakedModel {

    private static final int INVENTORY_INDEX = 1024;

    private final int meta;
    private final boolean isExhaust;
    private final StampedLock lock = new StampedLock();
    private final Int2ObjectOpenHashMap<List<BakedQuad>> cache = new Int2ObjectOpenHashMap<>();

    public DuctBakedModel(int meta, boolean isExhaust) {
        super(BakedModelTransforms.standardBlock());
        this.meta = meta;
        this.isExhaust = isExhaust;
    }

    public static TextureAtlasSprite getPipeIcon(int meta, int side, boolean pX, boolean nX, boolean pY, boolean nY, boolean pZ, boolean nZ, boolean isExhaust) {
        int m = isExhaust ? 0 : (meta % 3);
        int mask = (pX ? 32 : 0) + (nX ? 16 : 0) + (pY ? 8 : 0) + (nY ? 4 : 0) + (pZ ? 2 : 0) + (nZ ? 1 : 0);
        int count = Integer.bitCount(mask);

        TextureAtlasSprite[] straight = isExhaust ? FluidDuctBoxExhaust.iconStraight : FluidDuctBox.iconStraight;
        TextureAtlasSprite[] end = isExhaust ? FluidDuctBoxExhaust.iconEnd : FluidDuctBox.iconEnd;
        TextureAtlasSprite[] curveTL = isExhaust ? FluidDuctBoxExhaust.iconCurveTL : FluidDuctBox.iconCurveTL;
        TextureAtlasSprite[] curveTR = isExhaust ? FluidDuctBoxExhaust.iconCurveTR : FluidDuctBox.iconCurveTR;
        TextureAtlasSprite[] curveBL = isExhaust ? FluidDuctBoxExhaust.iconCurveBL : FluidDuctBox.iconCurveBL;
        TextureAtlasSprite[] curveBR = isExhaust ? FluidDuctBoxExhaust.iconCurveBR : FluidDuctBox.iconCurveBR;
        TextureAtlasSprite[][] junction = isExhaust ? FluidDuctBoxExhaust.iconJunction : FluidDuctBox.iconJunction;

        if ((mask & 0b001111) == 0 && mask > 0) {
            return (side == 4 || side == 5) ? end[m] : straight[m];
        } else if ((mask & 0b111100) == 0 && mask > 0) {
            return (side == 2 || side == 3) ? end[m] : straight[m];
        } else if ((mask & 0b110011) == 0 && mask > 0) {
            return (side == 0 || side == 1) ? end[m] : straight[m];
        } else if (count == 2) {
            if (side == 0 && nY || side == 1 && pY || side == 2 && nZ || side == 3 && pZ || side == 4 && nX || side == 5 && pX)
                return end[m];
            if (side == 1 && nY || side == 0 && pY || side == 3 && nZ || side == 2 && pZ || side == 5 && nX || side == 4 && pX)
                return straight[m];

            if (nY && pZ) return side == 4 ? curveBR[m] : curveBL[m];
            if (nY && nZ) return side == 5 ? curveBR[m] : curveBL[m];
            if (nY && pX) return side == 3 ? curveBR[m] : curveBL[m];
            if (nY && nX) return side == 2 ? curveBR[m] : curveBL[m];
            if (pY && pZ) return side == 4 ? curveTR[m] : curveTL[m];
            if (pY && nZ) return side == 5 ? curveTR[m] : curveTL[m];
            if (pY && pX) return side == 3 ? curveTR[m] : curveTL[m];
            if (pY && nX) return side == 2 ? curveTR[m] : curveTL[m];

            if (pX && nZ) return curveTR[m];
            if (pX && pZ) return curveBR[m];
            if (nX && nZ) return curveTL[m];
            if (nX && pZ) return curveBL[m];

            return straight[m];
        }

        return junction[m][meta / 3];
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        boolean inventory = state == null, pX, nX, pY, nY, pZ, nZ;
        int useMeta = this.meta;
        int cacheIndex;

        if (inventory) {
            pX = nX = pY = nY = pZ = nZ = false;
            cacheIndex = INVENTORY_INDEX;
        } else {
            try {
                IExtendedBlockState ext = (IExtendedBlockState) state;
                nZ = ext.getValue(FluidDuctBox.CONN_NORTH);
                pZ = ext.getValue(FluidDuctBox.CONN_SOUTH);
                nX = ext.getValue(FluidDuctBox.CONN_WEST);
                pX = ext.getValue(FluidDuctBox.CONN_EAST);
                nY = ext.getValue(FluidDuctBox.CONN_DOWN);
                pY = ext.getValue(FluidDuctBox.CONN_UP);
                useMeta = ext.getValue(FluidDuctBox.META);
            } catch (Exception _) { pX = true; nX = true; pY = false; nY = false; pZ = false; nZ = false; }

            int mask = (pX ? 32 : 0) + (nX ? 16 : 0) + (pY ? 8 : 0) + (nY ? 4 : 0) + (pZ ? 2 : 0) + (nZ ? 1 : 0);
            cacheIndex = ((useMeta & 0xF) << 6) | mask;
        }

        List<BakedQuad> quads;
        long stamp = lock.tryOptimisticRead();
        quads = cache.get(cacheIndex);

        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                quads = cache.get(cacheIndex);
            } finally {
                lock.unlockRead(stamp);
            }
        }

        if (quads != null) return quads;

        stamp = lock.writeLock();
        try {
            quads = cache.get(cacheIndex);
            if (quads == null) {
                quads = buildQuads(inventory, useMeta, pX, nX, pY, nY, pZ, nZ);
                cache.put(cacheIndex, quads);
            }
        } finally {
            lock.unlockWrite(stamp);
        }

        return quads;
    }

    private void addPart(List<BakedQuad> quads, float x1, float y1, float z1, float x2, float y2, float z2,
                         int meta, boolean pX, boolean nX, boolean pY, boolean nY, boolean pZ, boolean nZ,
                         int[] rotations) {
        TextureAtlasSprite[] sprites = new TextureAtlasSprite[6];
        for (EnumFacing face : EnumFacing.VALUES) {
            sprites[face.getIndex()] = getPipeIcon(meta, face.ordinal(), pX, nX, pY, nY, pZ, nZ, isExhaust);
        }
        addLegacyBox(quads, x1 * 16.0f, y1 * 16.0f, z1 * 16.0f, x2 * 16.0f, y2 * 16.0f, z2 * 16.0f, sprites, LEGACY_ALL_FACES, rotations);
    }

    private TextureAtlasSprite getStraightSprite(int meta) {
        return isExhaust ? FluidDuctBoxExhaust.iconStraight[0] : FluidDuctBox.iconStraight[meta % 3];
    }

    private TextureAtlasSprite getEndSprite(int meta) {
        return isExhaust ? FluidDuctBoxExhaust.iconEnd[0] : FluidDuctBox.iconEnd[meta % 3];
    }

    private List<BakedQuad> buildQuads(boolean inventory, int useMeta, boolean pX, boolean nX, boolean pY, boolean nY, boolean pZ, boolean nZ) {
        List<BakedQuad> quads = new ArrayList<>();
        int sizeLevel = Math.min(useMeta / 3, 4);
        float lower = 0.125f + sizeLevel * 0.0625f;
        float upper = 0.875f - sizeLevel * 0.0625f;
        float jLower = 0.0625f + sizeLevel * 0.0625f;
        float jUpper = 0.9375f - sizeLevel * 0.0625f;

        if (inventory) {
            TextureAtlasSprite[] inventorySprites = new TextureAtlasSprite[]{
                    getStraightSprite(useMeta),
                    getStraightSprite(useMeta),
                    getEndSprite(useMeta),
                    getEndSprite(useMeta),
                    getStraightSprite(useMeta),
                    getStraightSprite(useMeta)
            };
            addLegacyBox(quads, lower * 16.0f, lower * 16.0f, 0.0f, upper * 16.0f, upper * 16.0f, 16.0f, inventorySprites, LEGACY_ALL_FACES, new int[]{0, 0, 0, 0, 1, 2});
            return Collections.unmodifiableList(quads);
        }

        int mask = (pX ? 32 : 0) + (nX ? 16 : 0) + (pY ? 8 : 0) + (nY ? 4 : 0) + (pZ ? 2 : 0) + (nZ ? 1 : 0);
        int count = Integer.bitCount(mask);
        boolean straightX = (mask & 0b001111) == 0 && mask > 0;
        boolean straightY = (mask & 0b110011) == 0 && mask > 0;
        boolean straightZ = (mask & 0b111100) == 0 && mask > 0;

        if (straightX) {
            addPart(quads, 0, lower, lower, 1, upper, upper, useMeta, pX, nX, pY, nY, pZ, nZ, new int[]{1, 1, 2, 1, 0, 0});
        } else if (straightZ) {
            addPart(quads, lower, lower, 0, upper, upper, 1, useMeta, pX, nX, pY, nY, pZ, nZ, new int[]{0, 0, 0, 0, 1, 2});
        } else if (straightY) {
            addPart(quads, lower, 0, lower, upper, 1, upper, useMeta, pX, nX, pY, nY, pZ, nZ, LEGACY_NO_ROTATION);
        } else if (count == 2) {
            int[] rotations = (nY || pY) && (nX || pX) ? new int[]{1, 1, 0, 0, 0, 0}
                    : (!nY && !pY ? new int[]{0, 0, 2, 1, 1, 2} : LEGACY_NO_ROTATION);
            addPart(quads, lower, lower, lower, upper, upper, upper, useMeta, pX, nX, pY, nY, pZ, nZ, rotations);
            if (nY) addPart(quads, lower, 0, lower, upper, lower, upper, useMeta, pX, nX, pY, nY, pZ, nZ, rotations);
            if (pY) addPart(quads, lower, upper, lower, upper, 1, upper, useMeta, pX, nX, pY, nY, pZ, nZ, rotations);
            if (nX) addPart(quads, 0, lower, lower, lower, upper, upper, useMeta, pX, nX, pY, nY, pZ, nZ, rotations);
            if (pX) addPart(quads, upper, lower, lower, 1, upper, upper, useMeta, pX, nX, pY, nY, pZ, nZ, rotations);
            if (nZ) addPart(quads, lower, lower, 0, upper, upper, lower, useMeta, pX, nX, pY, nY, pZ, nZ, rotations);
            if (pZ) addPart(quads, lower, lower, upper, upper, upper, 1, useMeta, pX, nX, pY, nY, pZ, nZ, rotations);
        } else {
            addPart(quads, jLower, jLower, jLower, jUpper, jUpper, jUpper, useMeta, pX, nX, pY, nY, pZ, nZ, LEGACY_NO_ROTATION);
            if (nY) addPart(quads, lower, 0, lower, upper, jLower, upper, useMeta, pX, nX, pY, nY, pZ, nZ, LEGACY_NO_ROTATION);
            if (pY) addPart(quads, lower, jUpper, lower, upper, 1, upper, useMeta, pX, nX, pY, nY, pZ, nZ, LEGACY_NO_ROTATION);
            if (nX) addPart(quads, 0, lower, lower, jLower, upper, upper, useMeta, pX, nX, pY, nY, pZ, nZ, LEGACY_NO_ROTATION);
            if (pX) addPart(quads, jUpper, lower, lower, 1, upper, upper, useMeta, pX, nX, pY, nY, pZ, nZ, LEGACY_NO_ROTATION);
            if (nZ) addPart(quads, lower, lower, 0, upper, upper, jLower, useMeta, pX, nX, pY, nY, pZ, nZ, LEGACY_NO_ROTATION);
            if (pZ) addPart(quads, lower, lower, jUpper, upper, upper, 1, useMeta, pX, nX, pY, nY, pZ, nZ, LEGACY_NO_ROTATION);
        }

        return Collections.unmodifiableList(quads);
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return getPipeIcon(meta, EnumFacing.UP.getIndex(), false, false, false, false, false, false, isExhaust);
    }
}
