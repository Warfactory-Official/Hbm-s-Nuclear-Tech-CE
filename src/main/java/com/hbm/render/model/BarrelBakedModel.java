package com.hbm.render.model;

import com.hbm.blocks.machine.BlockFluidBarrel;
import com.hbm.render.loader.HFRWavefrontObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class BarrelBakedModel extends AbstractWavefrontBakedModel {

    private static final Set<String> BARREL = Set.of("Barrel");
    private static final Set<String> CONNECTOR = Set.of("Connector");

    private final TextureAtlasSprite sprite;
    private final boolean forBlock;
    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] cache = new List[16];

    private BarrelBakedModel(HFRWavefrontObject model, TextureAtlasSprite sprite, boolean forBlock) {
        super(model, forBlock ? DefaultVertexFormats.BLOCK : DefaultVertexFormats.ITEM, 1.0F, 0.5F, 0.0F, 0.5F, BakedModelTransforms.standardBlock());
        this.sprite = sprite;
        this.forBlock = forBlock;
    }

    public static BarrelBakedModel forBlock(HFRWavefrontObject model, TextureAtlasSprite sprite) {
        return new BarrelBakedModel(model, sprite, true);
    }

    public static BarrelBakedModel forItem(HFRWavefrontObject model, TextureAtlasSprite sprite) {
        return new BarrelBakedModel(model, sprite, false);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        int mask = 0;
        if (forBlock && state != null && state.getBlock() instanceof BlockFluidBarrel) {
            if (state.getValue(BlockFluidBarrel.CONN_POS_X)) mask |= 1;
            if (state.getValue(BlockFluidBarrel.CONN_NEG_X)) mask |= 2;
            if (state.getValue(BlockFluidBarrel.CONN_NEG_Z)) mask |= 4;
            if (state.getValue(BlockFluidBarrel.CONN_POS_Z)) mask |= 8;
        }

        List<BakedQuad> quads = cache[mask];
        if (quads == null) {
            quads = buildQuads(mask);
            cache[mask] = quads;
        }
        return quads;
    }

    private List<BakedQuad> buildQuads(int mask) {
        List<BakedQuad> quads = new ArrayList<>(bakeSimpleQuads(BARREL, 0.0F, 0.0F, 0.0F, true, false, sprite));
        if ((mask & 1) != 0) quads.addAll(bakeSimpleQuads(CONNECTOR, 0.0F, 0.0F, 0.0F, true, false, sprite));
        if ((mask & 2) != 0) quads.addAll(bakeSimpleQuads(CONNECTOR, 0.0F, 0.0F, (float) Math.PI, true, false, sprite));
        if ((mask & 4) != 0) quads.addAll(bakeSimpleQuads(CONNECTOR, 0.0F, 0.0F, (float) Math.PI / 2F, true, false, sprite));
        if ((mask & 8) != 0) quads.addAll(bakeSimpleQuads(CONNECTOR, 0.0F, 0.0F, -(float) Math.PI / 2F, true, false, sprite));
        return quads;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return sprite;
    }
}
