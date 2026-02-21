package com.hbm.render.model;

import com.hbm.blocks.network.FluidDuctBox;
import com.hbm.blocks.network.FluidDuctBoxExhaust;
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
public class DuctBakedModel extends AbstractBakedModel {

    private final int meta;
    private final boolean isExhaust;  // note: I'd do it in an enum but there are only 2 blocks using this model, so it's redundant for now I guess?..
    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] cache = new List[1024]; // 16 metas * 64 connection states

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
    // Th3_Sl1ze: okay, so half of this clusterfuck is made by me, and half of it by llm
    // It's still quite a big method but it's as debloated as I can do rn (considering that you need to rotate uv fucking manually)
    // UV's not perfect, but I'm NOT going to try finding where it fucks itself
    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        boolean pX, nX, pY, nY, pZ, nZ;
        int useMeta = this.meta;

        if (state == null) {
            pX = true; nX = true; pY = false; nY = false; pZ = false; nZ = false;
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
            } catch (Exception _) {
                pX = true; nX = true; pY = false; nY = false; pZ = false; nZ = false;
            }
        }

        int mask = (pX ? 32 : 0) + (nX ? 16 : 0) + (pY ? 8 : 0) + (nY ? 4 : 0) + (pZ ? 2 : 0) + (nZ ? 1 : 0);
        int cacheIndex = ((useMeta & 0xF) << 6) | mask;

        if (cache[cacheIndex] != null) return cache[cacheIndex];

        List<BakedQuad> quads = new ArrayList<>();
        int sizeLevel = Math.min(useMeta / 3, 4);
        float lower = 0.125f + sizeLevel * 0.0625f;
        float upper = 0.875f - sizeLevel * 0.0625f;
        float jLower = 0.0625f + sizeLevel * 0.0625f;
        float jUpper = 0.9375f - sizeLevel * 0.0625f;

        int count = Integer.bitCount(mask);
        boolean straightX = (mask & 0b001111) == 0 && mask > 0;
        boolean straightY = (mask & 0b110011) == 0 && mask > 0;
        boolean straightZ = (mask & 0b111100) == 0 && mask > 0;

        int[] uvRotate = new int[6];

        List<float[]> boundsList = new ArrayList<>();
        if (straightX) boundsList.add(new float[]{0, lower, lower, 1, upper, upper});
        else if (straightZ) boundsList.add(new float[]{lower, lower, 0, upper, upper, 1});
        else if (straightY) boundsList.add(new float[]{lower, 0, lower, upper, 1, upper});
        else if (count == 2) {
            boundsList.add(new float[]{lower, lower, lower, upper, upper, upper});
            if (nY) boundsList.add(new float[]{lower, 0, lower, upper, lower, upper});
            if (pY) boundsList.add(new float[]{lower, upper, lower, upper, 1, upper});
            if (nX) boundsList.add(new float[]{0, lower, lower, lower, upper, upper});
            if (pX) boundsList.add(new float[]{upper, lower, lower, 1, upper, upper});
            if (nZ) boundsList.add(new float[]{lower, lower, 0, upper, upper, lower});
            if (pZ) boundsList.add(new float[]{lower, lower, upper, upper, upper, 1});
        } else {
            boundsList.add(new float[]{jLower, jLower, jLower, jUpper, jUpper, jUpper});
            if (nY) boundsList.add(new float[]{lower, 0, lower, upper, jLower, upper});
            if (pY) boundsList.add(new float[]{lower, jUpper, lower, upper, 1, upper});
            if (nX) boundsList.add(new float[]{0, lower, lower, jLower, upper, upper});
            if (pX) boundsList.add(new float[]{jUpper, lower, lower, 1, upper, upper});
            if (nZ) boundsList.add(new float[]{lower, lower, 0, upper, upper, jLower});
            if (pZ) boundsList.add(new float[]{lower, lower, jUpper, upper, upper, 1});
        }

        FaceBakery faceBakery = new FaceBakery();

        for (float[] b : boundsList) {
            float minX = b[0] * 16f, minY = b[1] * 16f, minZ = b[2] * 16f;
            float maxX = b[3] * 16f, maxY = b[4] * 16f, maxZ = b[5] * 16f;
            if (minX == maxX || minY == maxY || minZ == maxZ) continue;

            for (EnumFacing face : EnumFacing.VALUES) {
                int s = face.ordinal();
                TextureAtlasSprite sprite = getPipeIcon(useMeta, s, pX, nX, pY, nY, pZ, nZ, isExhaust);
                if (sprite == null) continue;

                float uMin = 0, uMax = 0, vMin = 0, vMax = 0;
                switch (face) {
                    case UP:
                        boolean swapUV = !straightZ && (straightX || ((nY || pY) && (nX || pX)));
                        uMin = swapUV ? minZ : minX; uMax = swapUV ? maxZ : maxX;
                        vMin = swapUV ? minX : minZ; vMax = swapUV ? maxX : maxZ;
                        uvRotate[s] = swapUV ? 90 : 0;
                        break;
                    case DOWN:
                        uMin = minX; uMax = maxX; vMin = minZ; vMax = maxZ; uvRotate[s] = 0;
                        if (straightX || (pZ && nX) || (pX && nZ)) {
                            uMin = minZ; uMax = maxZ; vMin = minX; vMax = maxX; uvRotate[s] = 90;
                        } else if (nZ && nX) {
                            uMin = maxZ; uMax = minZ; vMin = maxX; vMax = minX; uvRotate[s] = 270;
                        } else if (pX && pZ) {
                            uMin = maxZ; uMax = minZ; vMin = maxX; vMax = minX; uvRotate[s] = 270;
                        }
                        break;
                    case SOUTH:
                        vMin = minY; vMax = maxY; uMin = 16f - maxZ; uMax = 16f - minZ; uvRotate[s] = 90;
                        if (straightY || (count == 2 && !straightX)) { uMin = minX; uMax = maxX; uvRotate[s] = 0; }
                        if ((nZ && nX) || (nZ && pX)) { uMin = 16f - maxZ; uMax = 16f - minZ; uvRotate[s] = 90; }
                        break;
                    case NORTH:
                        vMin = minY; vMax = maxY; uMin = minZ; uMax = maxZ; uvRotate[s] = 90;
                        if (straightY || (count == 2 && !straightX)) { uMin = 16f - maxX; uMax = 16f - minX; uvRotate[s] = 0; }
                        if ((pZ && nX) || (pZ && pX)) { uMin = minZ; uMax = maxZ; uvRotate[s] = 90; }
                        break;
                    case EAST:
                        vMin = minY; vMax = maxY; uMin = 16f - maxX; uMax = 16f - minX; uvRotate[s] = 90;
                        if (straightY || (count == 2 && !straightZ)) { uMin = minZ; uMax = maxZ; uvRotate[s] = 0; }
                        if ((nX && nZ) || (nX && pZ)) { uMin = 16f - maxX; uMax = 16f - minX; uvRotate[s] = 90; }
                        break;
                    case WEST:
                        vMin = minY; vMax = maxY; uMin = 16f - maxX; uMax = 16f - minX; uvRotate[s] = 90;
                        if (straightY || (count == 2 && !straightZ)) { uMin = minZ; uMax = maxZ; uvRotate[s] = 0; }
                        if ((pX && nZ) || (pX && pZ)) { uMin = 16f - maxX; uMax = 16f - minX; uvRotate[s] = 90; }
                        break;
                }

                if (uMin > uMax) { float temp = uMin; uMin = uMax; uMax = temp; }
                if (vMin > vMax) { float temp = vMin; vMin = vMax; vMax = temp; }

                float[] uvs = new float[]{uMin, vMin, uMax, vMax};
                BlockPartFace bpf = new BlockPartFace(null, 0, "", new BlockFaceUV(uvs, uvRotate[s]));

                Vector3f from = new Vector3f(), to = new Vector3f();
                switch (face) {
                    case DOWN: from.set(minX, minY, minZ); to.set(maxX, minY, maxZ); break;
                    case UP: from.set(minX, maxY, minZ); to.set(maxX, maxY, maxZ); break;
                    case NORTH: from.set(minX, minY, minZ); to.set(maxX, maxY, minZ); break;
                    case SOUTH: from.set(minX, minY, maxZ); to.set(maxX, maxY, maxZ); break;
                    case WEST: from.set(minX, minY, minZ); to.set(minX, maxY, maxZ); break;
                    case EAST: from.set(maxX, minY, minZ); to.set(maxX, maxY, maxZ); break;
                }

                BakedQuad quad = faceBakery.makeBakedQuad(from, to, bpf, sprite, face, ModelRotation.X0_Y0, null, false, true);
                quads.add(quad);
            }
        }

        return cache[cacheIndex] = Collections.unmodifiableList(quads);
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return getPipeIcon(meta, EnumFacing.UP.getIndex(), false, false, false, false, false, false, isExhaust);
    }
}
