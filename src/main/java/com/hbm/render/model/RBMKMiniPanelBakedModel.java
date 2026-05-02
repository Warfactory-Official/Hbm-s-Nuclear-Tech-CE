package com.hbm.render.model;

import com.hbm.blocks.machine.rbmk.RBMKMiniPanelBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class RBMKMiniPanelBakedModel extends AbstractBakedModel {

    private final TextureAtlasSprite sprite;
    private final boolean isInventory;
    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] cache = new List[6];
    private List<BakedQuad> inventoryCache;

    public RBMKMiniPanelBakedModel(TextureAtlasSprite sprite,boolean isInventory) {
        super(BakedModelTransforms.standardBlock());
        this.sprite = sprite;
        this.isInventory = isInventory;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        List<BakedQuad> quads = new ArrayList<>();

        if (isInventory) {
            if (inventoryCache != null) return inventoryCache;
            // setRenderBounds(0.25D, 0D, 0D, 1D, 1D, 1D); из 1.7.10
            addBox(quads, 0.25f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, sprite);
            return inventoryCache = Collections.unmodifiableList(quads);
        }

        EnumFacing facing = EnumFacing.NORTH;
        if (state != null && state.getPropertyKeys().contains(RBMKMiniPanelBase.FACING)) {
            facing = state.getValue(RBMKMiniPanelBase.FACING);
        }

        int index = facing.getIndex();
        if (cache[index] != null) return cache[index];

        switch (facing) {
            case WEST:
                addBox(quads, 0.25f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, sprite);
                break;
            case NORTH:
                addBox(quads, 0.0f, 0.0f, 0.25f, 1.0f, 1.0f, 1.0f, sprite);
                break;
            case EAST:
                addBox(quads, 0.0f, 0.0f, 0.0f, 0.75f, 1.0f, 1.0f, sprite);
                break;
            case SOUTH:
                addBox(quads, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.75f, sprite);
                break;
            default:
                addBox(quads, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, sprite);
                break;
        }

        return cache[index] = Collections.unmodifiableList(quads);
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return sprite;
    }
}
