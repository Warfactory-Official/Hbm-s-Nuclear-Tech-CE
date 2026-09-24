package com.hbm.render.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompositeBakedModel implements IBakedModel {
    private final IBakedModel primary;
    private final List<IBakedModel> delegates;

    public CompositeBakedModel(IBakedModel primary, IBakedModel... delegates) {
        this.primary = primary;
        this.delegates = Arrays.asList(delegates);
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        List<BakedQuad> quads = new ArrayList<>(primary.getQuads(state, side, rand));
        for (IBakedModel delegate : delegates) {
            quads.addAll(delegate.getQuads(state, side, rand));
        }
        return quads;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return primary.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return primary.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return primary.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return primary.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return primary.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return primary.getOverrides();
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        Pair<? extends IBakedModel, Matrix4f> pair = primary.handlePerspective(cameraTransformType);
        return Pair.of(this, pair.getRight());
    }
}
