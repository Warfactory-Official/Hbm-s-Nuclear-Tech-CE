package com.hbm.render.item;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("deprecation")
public class WrappedTEISRModel implements IBakedModel {

    private final TEISRBase renderer;
    private final IBakedModel baseModel;
    private final IBakedModel perspectiveModel;
    private final ItemCameraTransforms transforms;
    private final ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transformMap;
    private final boolean useBaseModelInGui;
    private final boolean useIdentityTransform;
    private final ItemOverrideList captureOverrides = new ItemOverrideList(Collections.emptyList()) {
        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
            renderer.entity = entity;
            renderer.world = world;
            return super.handleItemState(originalModel, stack, world, entity);
        }
    };

    private ItemCameraTransforms.TransformType currentTransformType = ItemCameraTransforms.TransformType.NONE;

    public WrappedTEISRModel(TEISRBase renderer, IBakedModel baseModel, IBakedModel perspectiveModel, TEISRBase.ModelBinding binding, boolean useIdentityTransform) {
        this.renderer = renderer;
        this.baseModel = baseModel;
        this.perspectiveModel = perspectiveModel;
        this.transforms = binding.getTransforms();
        this.transformMap = PerspectiveMapWrapper.getTransforms(this.transforms);
        this.useBaseModelInGui = binding.useBaseModelInGui();
        this.useIdentityTransform = useIdentityTransform;
    }

    public IBakedModel getBaseModel() {
        return baseModel;
    }

    private boolean shouldUseBaseModelInGui() {
        return useBaseModelInGui && currentTransformType == ItemCameraTransforms.TransformType.GUI;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        return shouldUseBaseModelInGui() ? baseModel.getQuads(state, side, rand) : Collections.emptyList();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return shouldUseBaseModelInGui() && baseModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return !shouldUseBaseModelInGui() || baseModel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return !shouldUseBaseModelInGui() || baseModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return baseModel.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return transforms;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return shouldUseBaseModelInGui() ? baseModel.getOverrides() : captureOverrides;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        currentTransformType = cameraTransformType;
        renderer.type = cameraTransformType;

        if (renderer.doNullTransform() && cameraTransformType == ItemCameraTransforms.TransformType.GUI) {
            return Pair.of(this, null);
        }
        if (useIdentityTransform) {
            return Pair.of(this, TRSRTransformation.identity().getMatrix());
        }
        if (perspectiveModel != null) {
            return Pair.of(this, perspectiveModel.handlePerspective(cameraTransformType).getRight());
        }

        return PerspectiveMapWrapper.handlePerspective(this, transformMap, cameraTransformType);
    }
}
