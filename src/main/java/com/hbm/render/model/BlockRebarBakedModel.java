package com.hbm.render.model;

import com.hbm.config.ClientConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class BlockRebarBakedModel extends AbstractBakedModel {

    private final TextureAtlasSprite sprite;
    private List<BakedQuad> cacheDetailed;
    private List<BakedQuad> cacheSimple;

    public BlockRebarBakedModel(TextureAtlasSprite sprite) {
        super(BakedModelTransforms.standardBlock());
        this.sprite = sprite;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return sprite;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        boolean simple = ClientConfig.RENDER_REBAR_SIMPLE.get();
        if (simple && cacheSimple != null) return cacheSimple;
        if (!simple && cacheDetailed != null) return cacheDetailed;

        List<BakedQuad> quads = new ArrayList<>();

        float min = 0.0f;
        float max = 1.0f;
        float baseMin = 0.4375f;
        float baseMax = 0.5625f;

        if (simple) {
            addBox(quads, baseMin, min, baseMin, baseMax, max, baseMax, sprite);
            addBox(quads, min, baseMin, baseMin, max, baseMax, baseMax, sprite);
            addBox(quads, baseMin, baseMin, min, baseMax, baseMax, max, sprite);
        } else {
            float o = 0.25f;

            float min0 = baseMin - o;
            float max0 = baseMax - o;
            float min1 = baseMin + o;
            float max1 = baseMax + o;

            // Y axis rods
            addBox(quads, min0, min, min0, max0, max, max0, sprite);
            addBox(quads, min0, min, min1, max0, max, max1, sprite);
            addBox(quads, min1, min, min0, max1, max, max0, sprite);
            addBox(quads, min1, min, min1, max1, max, max1, sprite);

            // X axis rods
            addBox(quads, min, min0, min0, max, max0, max0, sprite);
            addBox(quads, min, min0, min1, max, max0, max1, sprite);
            addBox(quads, min, min1, min0, max, max1, max0, sprite);
            addBox(quads, min, min1, min1, max, max1, max1, sprite);

            // Z axis rods
            addBox(quads, min0, min0, min, max0, max0, max, sprite);
            addBox(quads, min0, min1, min, max0, max1, max, sprite);
            addBox(quads, min1, min0, min, max1, max0, max, sprite);
            addBox(quads, min1, min1, min, max1, max1, max, sprite);
        }

        List<BakedQuad> cache = Collections.unmodifiableList(quads);
        if (simple) {
            cacheSimple = cache;
        } else {
            cacheDetailed = cache;
        }
        return cache;
    }
}
