package com.hbm.render.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;

/**
 * HBM-owned {@link BakedQuad} carrying pre-baked per-vertex lighting from
 * {@link AbstractWavefrontBakedModel}. Identified via {@link IHbmBakedQuad} so item-pipeline fixes
 * (see {@code com.hbm.mixin.mod.optifine.MixinRenderItem}) apply only to our quads.
 */
public final class HbmBakedQuad extends BakedQuad implements IHbmBakedQuad {

    public HbmBakedQuad(int[] vertexData, int tintIndex, EnumFacing face, TextureAtlasSprite sprite,
                        VertexFormat format) {
        super(vertexData, tintIndex, face, sprite, false, format);
    }
}
