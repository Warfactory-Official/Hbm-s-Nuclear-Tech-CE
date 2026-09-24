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

import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class RBMKMiniPanelItemBakedModel extends AbstractWavefrontBakedModel {

    private final TextureAtlasSprite panelSprite;
    private final List<Layer> layers;
    private List<BakedQuad> cache;

    public RBMKMiniPanelItemBakedModel(HFRWavefrontObject model, Set<String> partNames, TextureAtlasSprite panelSprite,
                                       TextureAtlasSprite partSprite, float[][] unitOffsets, float partUScale,
                                       float partVScale) {
        this(panelSprite, Collections.singletonList(new Layer(model, partNames, partSprite, partUScale, partVScale, 0xFFFFFF, null, unitOffsets)));
    }

    public RBMKMiniPanelItemBakedModel(TextureAtlasSprite panelSprite, List<Layer> layers) {
        super(layers.get(0).model, DefaultVertexFormats.ITEM, 1.0F, 0.5F, 0.0F, 0.5F, BakedModelTransforms.isbrh());
        this.panelSprite = panelSprite;
        this.layers = layers;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();
        if (cache != null) return cache;

        List<BakedQuad> quads = new ArrayList<>();
        addBox(quads, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.75F, panelSprite);

        for (Layer layer : layers) {
            for (float[] offset : layer.unitOffsets) {
                Matrix4f transform = BakedModelMatrixUtil.compose(
                        BakedModelMatrixUtil.translate(baseTx + offset[0], baseTy + offset[1], baseTz + offset[2]),
                        BakedModelMatrixUtil.rotateY(-90),
                        layer.localTransform != null ? layer.localTransform : BakedModelMatrixUtil.identity());
                for (FaceGeometry geometry : buildGeometryMatrix(layer.model, layer.partNames, transform, true)) {
                    quads.add(geometry.buildQuad(layer.sprite, -1, layer.uScale, layer.vScale, layer.color));
                }
            }
        }

        return cache = Collections.unmodifiableList(quads);
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return panelSprite;
    }

    public static final class Layer {
        final HFRWavefrontObject model;
        final Set<String> partNames;
        final TextureAtlasSprite sprite;
        final float uScale;
        final float vScale;
        final int color;
        final Matrix4f localTransform;
        final float[][] unitOffsets;

        public Layer(HFRWavefrontObject model, @Nullable Set<String> partNames, TextureAtlasSprite sprite, float uScale,
                     float vScale, int color, @Nullable Matrix4f localTransform, float[][] unitOffsets) {
            this.model = model;
            this.partNames = partNames;
            this.sprite = sprite;
            this.uScale = uScale;
            this.vScale = vScale;
            this.color = color;
            this.localTransform = localTransform;
            this.unitOffsets = unitOffsets;
        }
    }
}
