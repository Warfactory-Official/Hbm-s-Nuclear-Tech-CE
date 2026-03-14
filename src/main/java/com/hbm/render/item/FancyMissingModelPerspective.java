package com.hbm.render.item;

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
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.Collections;
import java.util.List;

/**
 * Real deterministic fancy missing model transformations (R)
 *
 * @author mlbv
 */
public class FancyMissingModelPerspective implements IBakedModel {

    private static final Matrix4f FIRST_PERSON_LEFT = new TRSRTransformation(new Vector3f(-0.62f, 0.5f, -0.5f), new Quat4f(1, -1, -1, 1), null, null).getMatrix();
    private static final Matrix4f FIRST_PERSON_RIGHT = new TRSRTransformation(new Vector3f(-0.5f, 0.5f, -0.5f), new Quat4f(1, 1, 1, 1), null, null).getMatrix();
    private static final Matrix4f GUI = new TRSRTransformation(null, new Quat4f(1, 1, 1, 1), null, null).getMatrix();
    private static final Matrix4f FIXED = new TRSRTransformation(null, new Quat4f(-1, -1, 1, 1), null, null).getMatrix();

    private final TEISRBase renderer;
    private final IBakedModel original;

    public FancyMissingModelPerspective(TEISRBase renderer, IBakedModel original) {
        this.renderer = renderer;
        this.original = original;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        return Collections.emptyList();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return original.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return new ItemOverrideList(Collections.emptyList()) {
            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
                renderer.entity = entity;
                renderer.world = world;
                return super.handleItemState(originalModel, stack, world, entity);
            }
        };
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        renderer.type = cameraTransformType;
        if (renderer.doNullTransform() && cameraTransformType == ItemCameraTransforms.TransformType.GUI) {
            return Pair.of(this, null);
        }
        return Pair.of(this, getMatrix(cameraTransformType));
    }

    private static Matrix4f getMatrix(ItemCameraTransforms.TransformType type) {
        return switch (type) {
            case FIRST_PERSON_LEFT_HAND -> FIRST_PERSON_LEFT;
            case FIRST_PERSON_RIGHT_HAND -> FIRST_PERSON_RIGHT;
            case GUI -> GUI;
            case FIXED -> FIXED;
            default -> TRSRTransformation.identity().getMatrix();
        };
    }
}
