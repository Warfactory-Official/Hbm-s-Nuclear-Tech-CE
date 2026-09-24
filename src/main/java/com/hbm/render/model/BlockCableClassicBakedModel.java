package com.hbm.render.model;

import com.hbm.blocks.network.energy.BlockCable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class BlockCableClassicBakedModel extends AbstractBakedModel {

    private static final float POS_NIL = 0F;
    private static final float POS_ONE = 1F;
    private static final float POS_MIN = 0.34375F;
    private static final float POS_MAX = 0.65625F;

    private static final float UV_CL = 0F;
    private static final float UV_CR = 5F;
    private static final float UV_CT = 0F;
    private static final float UV_CB = 5F;

    private static final float UV_SL = 5F;
    private static final float UV_SR = 10F;
    private static final float UV_ST = 0F;
    private static final float UV_SB = 5F;

    private static final int TOP = 255;
    private static final int BRIGHT = 204;
    private static final int DARK = 153;
    private static final int BOTTOM = 127;

    private static final float ITEM_SCALE = 1.25F;

    private final TextureAtlasSprite sprite;
    private final boolean forBlock;
    private final VertexFormat format;
    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] cache = new List[64];
    private List<BakedQuad> itemQuads;

    private BlockCableClassicBakedModel(TextureAtlasSprite sprite, boolean forBlock) {
        super(false, true, false, BakedModelTransforms.isbrh());
        this.sprite = sprite;
        this.forBlock = forBlock;
        this.format = forBlock ? DefaultVertexFormats.BLOCK : DefaultVertexFormats.ITEM;
    }

    public static BlockCableClassicBakedModel forBlock(TextureAtlasSprite sprite) {
        return new BlockCableClassicBakedModel(sprite, true);
    }

    public static BlockCableClassicBakedModel forItem(TextureAtlasSprite sprite) {
        return new BlockCableClassicBakedModel(sprite, false);
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return sprite;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        if (!forBlock) {
            if (itemQuads == null) itemQuads = Collections.unmodifiableList(buildItemQuads());
            return itemQuads;
        }

        boolean pX = false, nX = false, pY = false, nY = false, pZ = false, nZ = false;

        if (state != null) {
            try {
                pX = state.getValue(BlockCable.POS_X);
                nX = state.getValue(BlockCable.NEG_X);
                pY = state.getValue(BlockCable.POS_Y);
                nY = state.getValue(BlockCable.NEG_Y);
                pZ = state.getValue(BlockCable.POS_Z);
                nZ = state.getValue(BlockCable.NEG_Z);
            } catch (Exception ignored) {
            }
        }

        int mask = (pX ? 1 : 0) | (nX ? 2 : 0) | (pY ? 4 : 0) | (nY ? 8 : 0) | (pZ ? 16 : 0) | (nZ ? 32 : 0);
        List<BakedQuad> quads = cache[mask];
        if (quads != null) return quads;

        return cache[mask] = Collections.unmodifiableList(buildWorldQuads(pX, nX, pY, nY, pZ, nZ));
    }

    private List<BakedQuad> buildWorldQuads(boolean pX, boolean nX, boolean pY, boolean nY, boolean pZ, boolean nZ) {
        List<BakedQuad> quads = new ArrayList<>();

        if (!pY) {
            quad(quads, EnumFacing.UP, TOP,
                    POS_MAX, POS_MAX, POS_MIN, UV_CR, UV_CT,
                    POS_MIN, POS_MAX, POS_MIN, UV_CL, UV_CT,
                    POS_MIN, POS_MAX, POS_MAX, UV_CL, UV_CB,
                    POS_MAX, POS_MAX, POS_MAX, UV_CR, UV_CB);
        } else {
            quad(quads, EnumFacing.NORTH, BRIGHT,
                    POS_MAX, POS_MAX, POS_MIN, UV_SL, UV_ST,
                    POS_MIN, POS_MAX, POS_MIN, UV_SL, UV_SB,
                    POS_MIN, POS_ONE, POS_MIN, UV_SR, UV_SB,
                    POS_MAX, POS_ONE, POS_MIN, UV_SR, UV_ST);
            quad(quads, EnumFacing.EAST, DARK,
                    POS_MAX, POS_MAX, POS_MAX, UV_SL, UV_ST,
                    POS_MAX, POS_MAX, POS_MIN, UV_SL, UV_SB,
                    POS_MAX, POS_ONE, POS_MIN, UV_SR, UV_SB,
                    POS_MAX, POS_ONE, POS_MAX, UV_SR, UV_ST);
            quad(quads, EnumFacing.SOUTH, BRIGHT,
                    POS_MIN, POS_MAX, POS_MAX, UV_SL, UV_ST,
                    POS_MAX, POS_MAX, POS_MAX, UV_SL, UV_SB,
                    POS_MAX, POS_ONE, POS_MAX, UV_SR, UV_SB,
                    POS_MIN, POS_ONE, POS_MAX, UV_SR, UV_ST);
            quad(quads, EnumFacing.WEST, DARK,
                    POS_MIN, POS_MAX, POS_MIN, UV_SL, UV_ST,
                    POS_MIN, POS_MAX, POS_MAX, UV_SL, UV_SB,
                    POS_MIN, POS_ONE, POS_MAX, UV_SR, UV_SB,
                    POS_MIN, POS_ONE, POS_MIN, UV_SR, UV_ST);
        }

        if (!nY) {
            quad(quads, EnumFacing.DOWN, BOTTOM,
                    POS_MIN, POS_MIN, POS_MIN, UV_CL, UV_CT,
                    POS_MAX, POS_MIN, POS_MIN, UV_CR, UV_CT,
                    POS_MAX, POS_MIN, POS_MAX, UV_CR, UV_CB,
                    POS_MIN, POS_MIN, POS_MAX, UV_CL, UV_CB);
        } else {
            quad(quads, EnumFacing.NORTH, BRIGHT,
                    POS_MIN, POS_MIN, POS_MIN, UV_SL, UV_ST,
                    POS_MAX, POS_MIN, POS_MIN, UV_SL, UV_SB,
                    POS_MAX, POS_NIL, POS_MIN, UV_SR, UV_SB,
                    POS_MIN, POS_NIL, POS_MIN, UV_SR, UV_ST);
            quad(quads, EnumFacing.EAST, DARK,
                    POS_MAX, POS_MIN, POS_MIN, UV_SL, UV_ST,
                    POS_MAX, POS_MIN, POS_MAX, UV_SL, UV_SB,
                    POS_MAX, POS_NIL, POS_MAX, UV_SR, UV_SB,
                    POS_MAX, POS_NIL, POS_MIN, UV_SR, UV_ST);
            quad(quads, EnumFacing.SOUTH, BRIGHT,
                    POS_MAX, POS_MIN, POS_MAX, UV_SL, UV_ST,
                    POS_MIN, POS_MIN, POS_MAX, UV_SL, UV_SB,
                    POS_MIN, POS_NIL, POS_MAX, UV_SR, UV_SB,
                    POS_MAX, POS_NIL, POS_MAX, UV_SR, UV_ST);
            quad(quads, EnumFacing.WEST, DARK,
                    POS_MIN, POS_MIN, POS_MAX, UV_SL, UV_ST,
                    POS_MIN, POS_MIN, POS_MIN, UV_SL, UV_SB,
                    POS_MIN, POS_NIL, POS_MIN, UV_SR, UV_SB,
                    POS_MIN, POS_NIL, POS_MAX, UV_SR, UV_ST);
        }

        if (!pX) {
            quad(quads, EnumFacing.EAST, DARK,
                    POS_MAX, POS_MAX, POS_MIN, UV_CR, UV_CT,
                    POS_MAX, POS_MAX, POS_MAX, UV_CL, UV_CT,
                    POS_MAX, POS_MIN, POS_MAX, UV_CL, UV_CB,
                    POS_MAX, POS_MIN, POS_MIN, UV_CR, UV_CB);
        } else {
            quad(quads, EnumFacing.UP, TOP,
                    POS_MAX, POS_MAX, POS_MIN, UV_SL, UV_ST,
                    POS_MAX, POS_MAX, POS_MAX, UV_SL, UV_SB,
                    POS_ONE, POS_MAX, POS_MAX, UV_SR, UV_SB,
                    POS_ONE, POS_MAX, POS_MIN, UV_SR, UV_ST);
            quad(quads, EnumFacing.NORTH, BRIGHT,
                    POS_MAX, POS_MIN, POS_MIN, UV_SL, UV_ST,
                    POS_MAX, POS_MAX, POS_MIN, UV_SL, UV_SB,
                    POS_ONE, POS_MAX, POS_MIN, UV_SR, UV_SB,
                    POS_ONE, POS_MIN, POS_MIN, UV_SR, UV_ST);
            quad(quads, EnumFacing.DOWN, BOTTOM,
                    POS_MAX, POS_MIN, POS_MAX, UV_SL, UV_ST,
                    POS_MAX, POS_MIN, POS_MIN, UV_SL, UV_SB,
                    POS_ONE, POS_MIN, POS_MIN, UV_SR, UV_SB,
                    POS_ONE, POS_MIN, POS_MAX, UV_SR, UV_ST);
            quad(quads, EnumFacing.SOUTH, BRIGHT,
                    POS_MAX, POS_MAX, POS_MAX, UV_SL, UV_ST,
                    POS_MAX, POS_MIN, POS_MAX, UV_SL, UV_SB,
                    POS_ONE, POS_MIN, POS_MAX, UV_SR, UV_SB,
                    POS_ONE, POS_MAX, POS_MAX, UV_SR, UV_ST);
        }

        if (!nX) {
            quad(quads, EnumFacing.WEST, DARK,
                    POS_MIN, POS_MAX, POS_MAX, UV_CR, UV_CT,
                    POS_MIN, POS_MAX, POS_MIN, UV_CL, UV_CT,
                    POS_MIN, POS_MIN, POS_MIN, UV_CL, UV_CB,
                    POS_MIN, POS_MIN, POS_MAX, UV_CR, UV_CB);
        } else {
            quad(quads, EnumFacing.UP, TOP,
                    POS_MIN, POS_MAX, POS_MAX, UV_SL, UV_ST,
                    POS_MIN, POS_MAX, POS_MIN, UV_SL, UV_SB,
                    POS_NIL, POS_MAX, POS_MIN, UV_SR, UV_SB,
                    POS_NIL, POS_MAX, POS_MAX, UV_SR, UV_ST);
            quad(quads, EnumFacing.NORTH, BRIGHT,
                    POS_MIN, POS_MAX, POS_MIN, UV_SL, UV_ST,
                    POS_MIN, POS_MIN, POS_MIN, UV_SL, UV_SB,
                    POS_NIL, POS_MIN, POS_MIN, UV_SR, UV_SB,
                    POS_NIL, POS_MAX, POS_MIN, UV_SR, UV_ST);
            quad(quads, EnumFacing.DOWN, BOTTOM,
                    POS_MIN, POS_MIN, POS_MIN, UV_SL, UV_ST,
                    POS_MIN, POS_MIN, POS_MAX, UV_SL, UV_SB,
                    POS_NIL, POS_MIN, POS_MAX, UV_SR, UV_SB,
                    POS_NIL, POS_MIN, POS_MIN, UV_SR, UV_ST);
            quad(quads, EnumFacing.SOUTH, BRIGHT,
                    POS_MIN, POS_MIN, POS_MAX, UV_SL, UV_ST,
                    POS_MIN, POS_MAX, POS_MAX, UV_SL, UV_SB,
                    POS_NIL, POS_MAX, POS_MAX, UV_SR, UV_SB,
                    POS_NIL, POS_MIN, POS_MAX, UV_SR, UV_ST);
        }

        if (!pZ) {
            quad(quads, EnumFacing.SOUTH, BRIGHT,
                    POS_MAX, POS_MAX, POS_MAX, UV_CR, UV_CT,
                    POS_MIN, POS_MAX, POS_MAX, UV_CL, UV_CT,
                    POS_MIN, POS_MIN, POS_MAX, UV_CL, UV_CB,
                    POS_MAX, POS_MIN, POS_MAX, UV_CR, UV_CB);
        } else {
            quad(quads, EnumFacing.UP, TOP,
                    POS_MAX, POS_MAX, POS_MAX, UV_SL, UV_ST,
                    POS_MIN, POS_MAX, POS_MAX, UV_SL, UV_SB,
                    POS_MIN, POS_MAX, POS_ONE, UV_SR, UV_SB,
                    POS_MAX, POS_MAX, POS_ONE, UV_SR, UV_ST);
            quad(quads, EnumFacing.WEST, DARK,
                    POS_MIN, POS_MAX, POS_MAX, UV_SL, UV_ST,
                    POS_MIN, POS_MIN, POS_MAX, UV_SL, UV_SB,
                    POS_MIN, POS_MIN, POS_ONE, UV_SR, UV_SB,
                    POS_MIN, POS_MAX, POS_ONE, UV_SR, UV_ST);
            quad(quads, EnumFacing.DOWN, BOTTOM,
                    POS_MIN, POS_MIN, POS_MAX, UV_SL, UV_ST,
                    POS_MAX, POS_MIN, POS_MAX, UV_SL, UV_SB,
                    POS_MAX, POS_MIN, POS_ONE, UV_SR, UV_SB,
                    POS_MIN, POS_MIN, POS_ONE, UV_SR, UV_ST);
            quad(quads, EnumFacing.EAST, DARK,
                    POS_MAX, POS_MIN, POS_MAX, UV_SL, UV_ST,
                    POS_MAX, POS_MAX, POS_MAX, UV_SL, UV_SB,
                    POS_MAX, POS_MAX, POS_ONE, UV_SR, UV_SB,
                    POS_MAX, POS_MIN, POS_ONE, UV_SR, UV_ST);
        }

        if (!nZ) {
            quad(quads, EnumFacing.NORTH, BRIGHT,
                    POS_MIN, POS_MAX, POS_MIN, UV_CR, UV_CT,
                    POS_MAX, POS_MAX, POS_MIN, UV_CL, UV_CT,
                    POS_MAX, POS_MIN, POS_MIN, UV_CL, UV_CB,
                    POS_MIN, POS_MIN, POS_MIN, UV_CR, UV_CB);
        } else {
            quad(quads, EnumFacing.UP, TOP,
                    POS_MIN, POS_MAX, POS_MIN, UV_SL, UV_ST,
                    POS_MAX, POS_MAX, POS_MIN, UV_SL, UV_SB,
                    POS_MAX, POS_MAX, POS_NIL, UV_SR, UV_SB,
                    POS_MIN, POS_MAX, POS_NIL, UV_SR, UV_ST);
            quad(quads, EnumFacing.WEST, DARK,
                    POS_MIN, POS_MIN, POS_MIN, UV_SL, UV_ST,
                    POS_MIN, POS_MAX, POS_MIN, UV_SL, UV_SB,
                    POS_MIN, POS_MAX, POS_NIL, UV_SR, UV_SB,
                    POS_MIN, POS_MIN, POS_NIL, UV_SR, UV_ST);
            quad(quads, EnumFacing.DOWN, BOTTOM,
                    POS_MAX, POS_MIN, POS_MIN, UV_SL, UV_ST,
                    POS_MIN, POS_MIN, POS_MIN, UV_SL, UV_SB,
                    POS_MIN, POS_MIN, POS_NIL, UV_SR, UV_SB,
                    POS_MAX, POS_MIN, POS_NIL, UV_SR, UV_ST);
            quad(quads, EnumFacing.EAST, DARK,
                    POS_MAX, POS_MAX, POS_MIN, UV_SL, UV_ST,
                    POS_MAX, POS_MIN, POS_MIN, UV_SL, UV_SB,
                    POS_MAX, POS_MIN, POS_NIL, UV_SR, UV_SB,
                    POS_MAX, POS_MAX, POS_NIL, UV_SR, UV_ST);
        }

        return quads;
    }

    private List<BakedQuad> buildItemQuads() {
        List<BakedQuad> quads = buildWorldQuads(true, true, false, false, true, true);

        quad(quads, EnumFacing.EAST, DARK,
                POS_ONE, POS_MAX, POS_MIN, UV_CR, UV_CT,
                POS_ONE, POS_MAX, POS_MAX, UV_CL, UV_CT,
                POS_ONE, POS_MIN, POS_MAX, UV_CL, UV_CB,
                POS_ONE, POS_MIN, POS_MIN, UV_CR, UV_CB);
        quad(quads, EnumFacing.WEST, DARK,
                POS_NIL, POS_MAX, POS_MAX, UV_CR, UV_CT,
                POS_NIL, POS_MAX, POS_MIN, UV_CL, UV_CT,
                POS_NIL, POS_MIN, POS_MIN, UV_CL, UV_CB,
                POS_NIL, POS_MIN, POS_MAX, UV_CR, UV_CB);
        quad(quads, EnumFacing.SOUTH, BRIGHT,
                POS_MAX, POS_MAX, POS_ONE, UV_CR, UV_CT,
                POS_MIN, POS_MAX, POS_ONE, UV_CL, UV_CT,
                POS_MIN, POS_MIN, POS_ONE, UV_CL, UV_CB,
                POS_MAX, POS_MIN, POS_ONE, UV_CR, UV_CB);
        quad(quads, EnumFacing.NORTH, BRIGHT,
                POS_MIN, POS_MAX, POS_NIL, UV_CR, UV_CT,
                POS_MAX, POS_MAX, POS_NIL, UV_CL, UV_CT,
                POS_MAX, POS_MIN, POS_NIL, UV_CL, UV_CB,
                POS_MIN, POS_MIN, POS_NIL, UV_CR, UV_CB);

        return quads;
    }

    private void quad(List<BakedQuad> out, EnumFacing face, int shade,
                      float x0, float y0, float z0, float u0, float v0,
                      float x1, float y1, float z1, float u1, float v1,
                      float x2, float y2, float z2, float u2, float v2,
                      float x3, float y3, float z3, float u3, float v3) {

        int[] vertexData = new int[format.getIntegerSize() * 4];
        float[] scratch = new float[4];
        Vector3f normal = new Vector3f(face.getXOffset(), face.getYOffset(), face.getZOffset());

        put(vertexData, 0, x0, y0, z0, u0, v0, shade, normal, scratch);
        put(vertexData, 1, x1, y1, z1, u1, v1, shade, normal, scratch);
        put(vertexData, 2, x2, y2, z2, u2, v2, shade, normal, scratch);
        put(vertexData, 3, x3, y3, z3, u3, v3, shade, normal, scratch);

        out.add(new HbmBakedQuad(vertexData, -1, face, sprite, format));
    }

    private void put(int[] vertexData, int index, float x, float y, float z, float u, float v,
                     int shade, Vector3f normal, float[] scratch) {
        if (!forBlock) {
            x = (x - 0.5F) * ITEM_SCALE + 0.5F;
            y = (y - 0.5F) * ITEM_SCALE + 0.5F;
            z = (z - 0.5F) * ITEM_SCALE + 0.5F;
        }
        GeometryBakeUtil.putVertex(format, vertexData, index, x, y, z, u, v, shade, shade, shade, normal, sprite, scratch);
    }
}
