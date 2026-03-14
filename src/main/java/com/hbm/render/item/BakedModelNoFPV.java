package com.hbm.render.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.util.Collections;
import java.util.List;
// Th3_Sl1ze: the funny fucking thing. for now it translates only null transformtype meaning it will render fpv model for every transform type
// and if you try somehow translating the type it will render absolute nonsense dogshit without animations at all
// how? do I fucking know?
// TODO help me
public class BakedModelNoFPV implements IBakedModel {

    private final TEISRBase renderer;
    private final IBakedModel original;

    public BakedModelNoFPV(TEISRBase renderer, IBakedModel original) {
        this.renderer = renderer;
        this.original = original;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType type) {
        renderer.type = type;
        return Pair.of(this, TRSRTransformation.identity().getMatrix());
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) { return Collections.emptyList(); }
    @Override
    public boolean isAmbientOcclusion() { return original.isAmbientOcclusion(); }
    @Override
    public boolean isGui3d() { return original.isGui3d(); }
    @Override
    public boolean isBuiltInRenderer() { return true; }
    @Override
    public TextureAtlasSprite getParticleTexture() { return original.getParticleTexture(); }
    @Override
    public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
    @Override
    public ItemCameraTransforms getItemCameraTransforms() { return original.getItemCameraTransforms(); }
}
