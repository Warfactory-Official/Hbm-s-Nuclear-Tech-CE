package com.hbm.render.model;

import com.hbm.blocks.network.energy.PowerCableBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class CableBoxBakedModel extends AbstractBakedModel {

    private final int meta;
    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] cache = new List[64];

    public CableBoxBakedModel(int meta) {
        super(BakedModelTransforms.standardBlock());
        this.meta = meta;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();
        boolean pX, nX, pY, nY, pZ, nZ;
        int useMeta = this.meta;

        if (state == null) {
            pX = nX = true;
            pY = nY = pZ = nZ = false;
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
            } catch (Exception ignored) {
                pX = nX = true;
                pY = nY = pZ = nZ = false;
            }
        }
        int mask = (pX ? 32 : 0) | (nX ? 16 : 0) | (pY ? 8 : 0) | (nY ? 4 : 0) | (pZ ? 2 : 0) | (nZ ? 1 : 0);
        List<BakedQuad> cachedQuads = cache[mask];
        if (cachedQuads != null) {
            return cachedQuads;
        }
        int sizeLevel = Math.min(useMeta, 4);
        float lower = 0.125f + sizeLevel * 0.0625f;
        float upper = 0.875f - sizeLevel * 0.0625f;

        int count = Integer.bitCount(mask);
        List<BakedQuad> quads = new ArrayList<>(24);
        Vector3f from = new Vector3f();
        Vector3f to = new Vector3f();
        switch (mask) {
            case 48 -> addPart(quads, 0, lower, lower, 1, upper, upper, 0, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, from, to);
            case 12 -> addPart(quads, lower, 0, lower, upper, 1, upper, 1, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, from, to);
            case 3 -> addPart(quads, lower, lower, 0, upper, upper, 1, 2, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, from, to);
            default -> {
                addPart(quads, lower, lower, lower, upper, upper, upper, 3, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, from, to);
                if (nX) addPart(quads, 0, lower, lower, lower, upper, upper, 0, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, from, to);
                if (pX) addPart(quads, upper, lower, lower, 1, upper, upper, 0, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, from, to);
                if (nY) addPart(quads, lower, 0, lower, upper, lower, upper, 1, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, from, to);
                if (pY) addPart(quads, lower, upper, lower, upper, 1, upper, 1, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, from, to);
                if (nZ) addPart(quads, lower, lower, 0, upper, upper, lower, 2, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, from, to);
                if (pZ) addPart(quads, lower, lower, upper, upper, upper, 1, 2, useMeta, mask, count, pX, nX, pY, nY, pZ, nZ, from, to);
            }
        }
        return cache[mask] = Collections.unmodifiableList(quads);
    }

    private static void addPart(List<BakedQuad> quads, float x1, float y1, float z1, float x2, float y2, float z2, int axis,
                                int meta, int mask, int count, boolean pX, boolean nX, boolean pY, boolean nY, boolean pZ, boolean nZ,
                                Vector3f from, Vector3f to) {

        float minX = x1 * 16f, minY = y1 * 16f, minZ = z1 * 16f;
        float maxX = x2 * 16f, maxY = y2 * 16f, maxZ = z2 * 16f;

        from.set(minX, minY, minZ);
        to.set(maxX, maxY, maxZ);

        FaceBakery faceBakery = new FaceBakery();

        EnumFacing[] faces = EnumFacing.VALUES;
        for (int i = 0; i < faces.length; i++) {
            EnumFacing face = faces[i];
            TextureAtlasSprite sprite = getIcon(meta, i, mask, count, pX, nX, pY, nY, pZ, nZ);

            float uMin, uMax, vMin, vMax;
            int rot = 0;

            if (axis == 0) {
                if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
                    rot = 90; uMin = minZ; uMax = maxZ; vMin = minX; vMax = maxX;
                } else if (face == EnumFacing.NORTH || face == EnumFacing.SOUTH) {
                    rot = 90; uMin = minY; uMax = maxY; vMin = minX; vMax = maxX;
                } else {
                    uMin = minZ; uMax = maxZ; vMin = minY; vMax = maxY;
                }
            } else if (axis == 2) {
                if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
                    uMin = minX; uMax = maxX; vMin = minZ; vMax = maxZ;
                } else if (face == EnumFacing.WEST || face == EnumFacing.EAST) {
                    rot = 90; uMin = minY; uMax = maxY; vMin = minZ; vMax = maxZ;
                } else {
                    uMin = minX; uMax = maxX; vMin = minY; vMax = maxY;
                }
            } else {
                if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
                    uMin = minX; uMax = maxX; vMin = minZ; vMax = maxZ;
                } else if (face == EnumFacing.NORTH || face == EnumFacing.SOUTH) {
                    uMin = minX; uMax = maxX; vMin = minY; vMax = maxY;
                } else {
                    uMin = minZ; uMax = maxZ; vMin = minY; vMax = maxY;
                }
            }

            if (uMin > uMax) { float t = uMin; uMin = uMax; uMax = t; }
            if (vMin > vMax) { float t = vMin; vMin = vMax; vMax = t; }

            quads.add(faceBakery.makeBakedQuad(from, to,
                    new BlockPartFace(null, 0, "", new BlockFaceUV(new float[]{uMin, vMin, uMax, vMax}, rot)),
                    sprite, face, ModelRotation.X0_Y0, null, false, true));
        }
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

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return PowerCableBox.iconStraight;
    }
}
