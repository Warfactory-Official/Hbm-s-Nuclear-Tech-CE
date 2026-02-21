package com.hbm.render.model;

import com.hbm.blocks.generic.BlockBarrier;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class BlockBarrierBakedModel extends AbstractBakedModel {

    private final TextureAtlasSprite sprite;
    private final boolean isInventory;
    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] cache = new List[32];
    private List<BakedQuad> inventoryCache;

    public BlockBarrierBakedModel(TextureAtlasSprite sprite, boolean isInventory) {
        super(BakedModelTransforms.standardBlock());
        this.sprite = sprite;
        this.isInventory = isInventory;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        List<BakedQuad> quads = new ArrayList<>();

        if (isInventory) {
            if (inventoryCache != null) return inventoryCache;
            addBox(quads, 0.4375f, 0.0f, 0.4375f, 0.5625f, 1.0f, 0.5625f, sprite);
            addBox(quads, 0.5f, 0.0625f, 0.0f, 0.5625f, 0.4375f, 1.0f, sprite);
            addBox(quads, 0.5f, 0.5625f, 0.0f, 0.5625f, 0.9375f, 1.0f, sprite);
            return inventoryCache = Collections.unmodifiableList(quads);
        }

        boolean negX = false;
        boolean posX = false;
        boolean negZ = false;
        boolean posZ = false;
        boolean posY = false;

        if (state instanceof IExtendedBlockState ext) {
            try {
                negX = ext.getValue(BlockBarrier.CONN_NEG_X);
                posX = ext.getValue(BlockBarrier.CONN_POS_X);
                negZ = ext.getValue(BlockBarrier.CONN_NEG_Z);
                posZ = ext.getValue(BlockBarrier.CONN_POS_Z);
                posY = ext.getValue(BlockBarrier.CONN_POS_Y);
            } catch (Exception _) { }
        }

        int mask = (negX ? 1 : 0) | (posX ? 2 : 0) | (negZ ? 4 : 0) | (posZ ? 8 : 0) | (posY ? 16 : 0);
        if (cache[mask] != null) return cache[mask];

        if (negX) {
            addBox(quads, 0.0f, 0.0f, 0.4375f, 0.125f, 1.0f, 0.5625f, sprite);
            addBox(quads, 0.0f, 0.0625f, negZ ? 0.125f : 0.0f, 0.0625f, 0.4375f, posZ ? 0.875f : 1.0f, sprite);
            addBox(quads, 0.0f, 0.5625f, negZ ? 0.125f : 0.0f, 0.0625f, 0.9375f, posZ ? 0.875f : 1.0f, sprite);
        }

        if (negZ) {
            addBox(quads, 0.4375f, 0.0f, 0.0f, 0.5625f, 1.0f, 0.125f, sprite);
            addBox(quads, negX ? 0.125f : 0.0f, 0.0625f, 0.0f, posX ? 0.875f : 1.0f, 0.4375f, 0.0625f, sprite);
            addBox(quads, negX ? 0.125f : 0.0f, 0.5625f, 0.0f, posX ? 0.875f : 1.0f, 0.9375f, 0.0625f, sprite);
        }

        if (posX) {
            addBox(quads, 0.875f, 0.0f, 0.4375f, 1.0f, 1.0f, 0.5625f, sprite);
            addBox(quads, 0.9375f, 0.0625f, negZ ? 0.125f : 0.0f, 1.0f, 0.4375f, posZ ? 0.875f : 1.0f, sprite);
            addBox(quads, 0.9375f, 0.5625f, negZ ? 0.125f : 0.0f, 1.0f, 0.9375f, posZ ? 0.875f : 1.0f, sprite);
        }

        if (posZ) {
            addBox(quads, 0.4375f, 0.0f, 0.875f, 0.5625f, 1.0f, 1.0f, sprite);
            addBox(quads, negX ? 0.125f : 0.0f, 0.0625f, 0.9375f, posX ? 0.875f : 1.0f, 0.4375f, 1.0f, sprite);
            addBox(quads, negX ? 0.125f : 0.0f, 0.5625f, 0.9375f, posX ? 0.875f : 1.0f, 0.9375f, 1.0f, sprite);
        }

        if (posY) {
            addBox(quads, 0.0f, 0.875f, 0.0f, 0.125f, 0.9375f, 1.0f, sprite);
            addBox(quads, 0.875f, 0.875f, 0.0f, 1.0f, 0.9375f, 1.0f, sprite);
            addBox(quads, 0.0f, 0.9375f, 0.0625f, 1.0f, 1.0f, 0.4375f, sprite);
            addBox(quads, 0.0f, 0.9375f, 0.5625f, 1.0f, 1.0f, 0.9375f, sprite);
        }

        return cache[mask] = Collections.unmodifiableList(quads);
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return sprite;
    }
}
