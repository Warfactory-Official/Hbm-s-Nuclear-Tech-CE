package com.hbm.render.model;

import com.hbm.render.loader.HFRWavefrontObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class RBMKMiniPanelItemBakedModel extends AbstractWavefrontBakedModel {

    private final TextureAtlasSprite panelSprite;
    private final TextureAtlasSprite partSprite;
    private final Set<String> partNames;
    private final float[][] unitOffsets;
    private final float partUScale;
    private final float partVScale;
    private List<BakedQuad> cache;

    public RBMKMiniPanelItemBakedModel(HFRWavefrontObject model, Set<String> partNames, TextureAtlasSprite panelSprite,
                                       TextureAtlasSprite partSprite, float[][] unitOffsets, float partUScale,
                                       float partVScale) {
        super(model, DefaultVertexFormats.ITEM, 1.0F, 0.0F, 0.0F, 0.0F, BakedModelTransforms.standardBlock());
        this.panelSprite = panelSprite;
        this.partSprite = partSprite;
        this.partNames = partNames;
        this.unitOffsets = unitOffsets;
        this.partUScale = partUScale;
        this.partVScale = partVScale;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();
        if (cache != null) return cache;

        List<BakedQuad> quads = new ArrayList<>();
        addBox(quads, 0.0F, 0.0F, 0.0F, 0.75F, 1.0F, 1.0F, panelSprite);

        for (float[] offset : unitOffsets) {
            quads.addAll(bakeSimpleQuads(partNames, 0.0F, 0.0F, 0.0F, true, false, partSprite, -1, offset[0], offset[1],
                    offset[2], partUScale, partVScale));
        }

        return cache = Collections.unmodifiableList(quads);
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return panelSprite;
    }
}
