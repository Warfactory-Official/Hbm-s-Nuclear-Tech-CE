package com.hbm.render.model;

import com.hbm.blocks.network.energy.PowerCableBox;
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
public class CableBoxBakedModel extends AbstractBakedModel {

    private static final int INVENTORY_INDEX = 1024;

    private final int meta;
    private final StampedLock lock = new StampedLock();
    private final Int2ObjectOpenHashMap<List<BakedQuad>> cache = new Int2ObjectOpenHashMap<>();

    public CableBoxBakedModel(int meta) {
        super(BakedModelTransforms.standardBlock());
        this.meta = meta;
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
                nZ = ext.getValue(PowerCableBox.CONN_NORTH);
                pZ = ext.getValue(PowerCableBox.CONN_SOUTH);
                nX = ext.getValue(PowerCableBox.CONN_WEST);
                pX = ext.getValue(PowerCableBox.CONN_EAST);
                nY = ext.getValue(PowerCableBox.CONN_DOWN);
                pY = ext.getValue(PowerCableBox.CONN_UP);
                useMeta = ext.getValue(PowerCableBox.META);
            } catch (Exception ignored) { pX = true; nX = true; pY = false; nY = false; pZ = false; nZ = false; }

            int mask = (pX ? 32 : 0) | (nX ? 16 : 0) | (pY ? 8 : 0) | (nY ? 4 : 0) | (pZ ? 2 : 0) | (nZ ? 1 : 0);
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

    private static void addPart(List<BakedQuad> quads, float x1, float y1, float z1, float x2, float y2, float z2,
                                int meta, int mask, int count, boolean pX, boolean nX, boolean pY, boolean nY,
                                boolean pZ, boolean nZ,
                                int[] rotations) {
        TextureAtlasSprite[] sprites = new TextureAtlasSprite[6];
        for (EnumFacing face : EnumFacing.VALUES) {
            sprites[face.getIndex()] = getIcon(meta, face.ordinal(), mask, count, pX, nX, pY, nY, pZ, nZ);
        }
        addLegacyBox(quads, x1 * 16.0f, y1 * 16.0f, z1 * 16.0f, x2 * 16.0f, y2 * 16.0f, z2 * 16.0f, sprites, LEGACY_ALL_FACES, rotations);
    }

    private static TextureAtlasSprite getIcon(int meta, int side, int mask, int count,
                                              boolean pX, boolean nX, boolean pY, boolean nY, boolean pZ, boolean nZ) {
        int m = meta % 5;

        if ((mask & 0b001111) == 0 && mask > 0) return (side == 4 || side == 5) ? PowerCableBox.iconEnd[m] : PowerCableBox.iconStraight;
        if ((mask & 0b111100) == 0 && mask > 0) return (side == 2 || side == 3) ? PowerCableBox.iconEnd[m] : PowerCableBox.iconStraight;
        if ((mask & 0b110011) == 0 && mask > 0) return (side == 0 || side == 1) ? PowerCableBox.iconEnd[m] : PowerCableBox.iconStraight;

        if (count == 2) {
            if ((nY && pZ) || (pY && nZ)) return side == 4 ? PowerCableBox.iconCurveTR : PowerCableBox.iconCurveTL;
            if ((nY && nZ) || (pY && pZ)) return side == 5 ? PowerCableBox.iconCurveTR : PowerCableBox.iconCurveTL;
            if ((nY && pX) || (pY && nX)) return side == 3 ? PowerCableBox.iconCurveBR : PowerCableBox.iconCurveBL;
            if ((nX && nZ) || (pX && pZ)) return side == 2 ? PowerCableBox.iconCurveBR : PowerCableBox.iconCurveBL;
            return PowerCableBox.iconStraight;
        }

        return PowerCableBox.iconJunction;
    }

    private static List<BakedQuad> buildQuads(boolean inventory, int useMeta, boolean pX, boolean nX, boolean pY,
                                              boolean nY, boolean pZ, boolean nZ) {
        int sizeLevel = Math.min(useMeta, 4);
        float lower = 0.125f + sizeLevel * 0.0625f;
        float upper = 0.875f - sizeLevel * 0.0625f;
        int mask = (pX ? 32 : 0) | (nX ? 16 : 0) | (pY ? 8 : 0) | (nY ? 4 : 0) | (pZ ? 2 : 0) | (nZ ? 1 : 0);
        int count = Integer.bitCount(mask);
        List<BakedQuad> quads = new ArrayList<>(24);

        if (inventory) {
            TextureAtlasSprite[] inventorySprites = new TextureAtlasSprite[]{
                    PowerCableBox.iconStraight,
                    PowerCableBox.iconStraight,
                    PowerCableBox.iconEnd[useMeta % 5],
                    PowerCableBox.iconEnd[useMeta % 5],
                    PowerCableBox.iconStraight,
                    PowerCableBox.iconStraight
            };
            addLegacyBox(quads, lower * 16.0f, lower * 16.0f, 0.0f, upper * 16.0f, upper * 16.0f, 16.0f, inventorySprites, LEGACY_ALL_FACES, new int[]{0, 0, 0, 0, 1, 2});
            return Collections.unmodifiableList(quads);
        }

        boolean straightX = (mask & 0b001111) == 0 && mask > 0;
        boolean straightY = (mask & 0b110011) == 0 && mask > 0;
        boolean straightZ = (mask & 0b111100) == 0 && mask > 0;

        if (straightX) {
            addPart(quads, 0, lower, lower, 1, upper, upper, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, new int[]{1, 1, 2, 1, 0, 0});
        } else if (straightY) {
            addPart(quads, lower, 0, lower, upper, 1, upper, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, LEGACY_NO_ROTATION);
        } else if (straightZ) {
            addPart(quads, lower, lower, 0, upper, upper, 1, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, new int[]{0, 0, 0, 0, 1, 2});
        } else if (count == 2) {
            int[] rotations = (nY || pY) && (nX || pX) ? new int[]{1, 1, 0, 0, 0, 0}
                    : (!nY && !pY ? new int[]{0, 0, 2, 1, 1, 2} : LEGACY_NO_ROTATION);
            addPart(quads, lower, lower, lower, upper, upper, upper, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, rotations);
            if (nX) addPart(quads, 0, lower, lower, lower, upper, upper, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, rotations);
            if (pX) addPart(quads, upper, lower, lower, 1, upper, upper, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, rotations);
            if (nY) addPart(quads, lower, 0, lower, upper, lower, upper, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, rotations);
            if (pY) addPart(quads, lower, upper, lower, upper, 1, upper, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, rotations);
            if (nZ) addPart(quads, lower, lower, 0, upper, upper, lower, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, rotations);
            if (pZ) addPart(quads, lower, lower, upper, upper, upper, 1, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, rotations);
        } else {
            addPart(quads, lower, lower, lower, upper, upper, upper, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, LEGACY_NO_ROTATION);
            if (nX) addPart(quads, 0, lower, lower, lower, upper, upper, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, LEGACY_NO_ROTATION);
            if (pX) addPart(quads, upper, lower, lower, 1, upper, upper, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, LEGACY_NO_ROTATION);
            if (nY) addPart(quads, lower, 0, lower, upper, lower, upper, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, LEGACY_NO_ROTATION);
            if (pY) addPart(quads, lower, upper, lower, upper, 1, upper, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, LEGACY_NO_ROTATION);
            if (nZ) addPart(quads, lower, lower, 0, upper, upper, lower, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, LEGACY_NO_ROTATION);
            if (pZ) addPart(quads, lower, lower, upper, upper, upper, 1, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, LEGACY_NO_ROTATION);
        }

        return Collections.unmodifiableList(quads);
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return PowerCableBox.iconStraight;
    }
}
