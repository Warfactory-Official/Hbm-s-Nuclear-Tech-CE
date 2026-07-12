package com.hbm.render.model;

import com.hbm.render.loader.HFRWavefrontObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Matrix4f;
import java.util.*;

import static com.hbm.render.model.BakedModelMatrixUtil.*;

@SideOnly(Side.CLIENT)
public class StaticWavefrontItemBakedModel extends AbstractWavefrontBakedModel {
    private static final Matrix4f HALF_BLOCK_NEGATIVE = translate(-0.5, -0.5, -0.5);
    private static final Matrix4f HALF_BLOCK_POSITIVE = translate(0.5, 0.5, 0.5);
    private static final Matrix4f FLIP_X = scale(-1.0, 1.0, 1.0);

    private final TextureAtlasSprite sprite;
    private final Set<String> partNames;
    private final float yaw;
    private final float roll;
    private final float pitch;
    private final float preTranslateX;
    private final float preTranslateY;
    private final float preTranslateZ;
    private final float uScale;
    private final float vScale;
    private final boolean doubleSided;
    private final EnumMap<ItemCameraTransforms.TransformType, Matrix4f> perspectiveMatrices = new EnumMap<>(
            ItemCameraTransforms.TransformType.class);
    private List<BakedQuad> cache;

    public StaticWavefrontItemBakedModel(HFRWavefrontObject model, TextureAtlasSprite sprite,
                                         @Nullable String[] partNames,
                                         float scale, float yaw, boolean doubleSided, float uScale, float vScale,
                                         float roll, float pitch,
                                         double guiTranslateX, double guiTranslateY, double guiTranslateZ,
                                         double guiScale, double guiYaw,
                                         float preTranslateX, float preTranslateY, float preTranslateZ,
                                         float translateX, float translateY, float translateZ) {
        super(model, DefaultVertexFormats.ITEM, scale, translateX, translateY, translateZ,
                ItemCameraTransforms.DEFAULT);
        this.sprite = sprite;
        this.partNames = partNames == null || partNames.length == 0 ? null : new LinkedHashSet<>(
                Arrays.asList(partNames));
        this.yaw = yaw;
        this.roll = roll;
        this.pitch = pitch;
        this.doubleSided = doubleSided;
        this.uScale = uScale;
        this.vScale = vScale;
        this.preTranslateX = preTranslateX;
        this.preTranslateY = preTranslateY;
        this.preTranslateZ = preTranslateZ;
        initPerspectiveMatrices(guiTranslateX, guiTranslateY, guiTranslateZ, guiScale, guiYaw);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side != null) {
            return Collections.emptyList();
        }

        if (cache != null) {
            return cache;
        }

        float[] rotatedPreTranslate = GeometryBakeUtil.rotateY(preTranslateX, preTranslateY, preTranslateZ, yaw);
        float extraTx = rotatedPreTranslate[0];
        float extraTy = rotatedPreTranslate[1];
        float extraTz = rotatedPreTranslate[2];

        List<FaceGeometry> geometry = buildGeometry(partNames, roll, pitch, yaw, true, false, extraTx, extraTy,
                extraTz);
        List<BakedQuad> quads = new ArrayList<>(doubleSided ? geometry.size() * 2 : geometry.size());
        for (FaceGeometry face : geometry) {
            quads.add(face.buildQuad(sprite, -1, uScale, vScale));
            if (doubleSided) {
                quads.add(face.buildBackQuad(sprite, -1, uScale, vScale));
            }
        }
        cache = Collections.unmodifiableList(quads);
        return cache;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return sprite;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(
            ItemCameraTransforms.TransformType cameraTransformType) {
        Matrix4f matrix = perspectiveMatrices.get(cameraTransformType);
        return Pair.of(this, matrix != null ? new Matrix4f(matrix) : null);
    }

    private void initPerspectiveMatrices(double guiTranslateX, double guiTranslateY, double guiTranslateZ,
                                         double guiScale, double guiYaw) {
        // Each context wraps the exact-1.7 ItemRenderBase frame (X_C) in stageTransform, which conjugates it
        // by T(-0.5)/T(0.5). That makes the baked-model engine path (multiply M_hp, then the engine's
        // translate(-0.5)) reproduce the TEISR path (translate(-0.5), then multMatrix(X_C)): the baked geometry
        // plays the role of the machine render body, so the shared frame lands it exactly where 1.7 did.
        // The per-item gui overrides then tune each model within the slot, as a machine's renderInventory does.
        perspectiveMatrices.put(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND,
                stageTransform(
                        translate(0.5, 0.5, 0.5),
                        rotateY(45.0),
                        scale(0.4),
                        translate(0.0, -0.3, 0.0),
                        scale(1.5),
                        rotateY(50.0),
                        rotateZ(335.0),
                        translate(-0.9375, -0.0625, 0.0),
                        translate(0.5, 0.25, 0.0),
                        scale(0.25),
                        rotateY(90.0)
                ));
        perspectiveMatrices.put(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
                leftHandStageTransform(
                        translate(0.5, 0.5, 0.5),
                        rotateY(-45.0),
                        scale(0.4),
                        translate(0.0, -0.3, 0.0),
                        scale(1.5),
                        rotateY(-50.0),
                        rotateZ(-335.0),
                        translate(0.9375, -0.0625, 0.0),
                        translate(-0.5, 0.25, 0.0),
                        scale(0.25),
                        rotateY(-90.0)
                ));
        perspectiveMatrices.put(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND,
                stageTransform(
                        translate(0.4375, 0.375, 1.125),
                        rotateY(180.0),
                        rotateX(90.0),
                        translate(0.1875, 0.625, -0.125),
                        scale(0.375),
                        rotateZ(60.0),
                        rotateX(-90.0),
                        rotateZ(20.0),
                        translate(0.0, -0.3, 0.0),
                        scale(1.5),
                        rotateY(50.0),
                        rotateZ(335.0),
                        translate(-0.9375, -0.0625, 0.0),
                        translate(0.5, 0.25, 0.0),
                        scale(0.25)
                ));
        perspectiveMatrices.put(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND,
                leftHandStageTransform(
                        translate(0.5625, 0.375, 1.125),
                        rotateY(180.0),
                        rotateX(90.0),
                        translate(-0.1875, 0.625, -0.125),
                        scale(0.375),
                        rotateZ(-60.0),
                        rotateX(-90.0),
                        rotateZ(-20.0),
                        translate(0.0, -0.3, 0.0),
                        scale(1.5),
                        rotateY(-50.0),
                        rotateZ(-335.0),
                        translate(0.9375, -0.0625, 0.0),
                        translate(-0.5, 0.25, 0.0),
                        scale(0.25)
                ));
        perspectiveMatrices.put(ItemCameraTransforms.TransformType.HEAD,
                stageTransform(
                        translate(0.5, 0.5, 0.5),
                        scale(1.6, -1.6, -1.6),
                        rotateY(180.0),
                        translate(0.0, 0.25, 0.0),
                        translate(0.0, -0.3, 0.0),
                        scale(1.5),
                        rotateY(50.0),
                        rotateZ(335.0),
                        translate(-0.9375, -0.0625, 0.0),
                        translate(0.5, 0.25, 0.0),
                        scale(0.25)
                ));
        perspectiveMatrices.put(ItemCameraTransforms.TransformType.GROUND,
                stageTransform(
                        translate(0.5, 0.25, 0.5),
                        scale(0.1875),
                        rotateY(90.0)
                ));
        perspectiveMatrices.put(ItemCameraTransforms.TransformType.FIXED,
                stageTransform(
                        translate(0.5, 0.34, 0.53125),
                        rotateY(-90.0),
                        scale(0.375)
                ));
        perspectiveMatrices.put(ItemCameraTransforms.TransformType.GUI,
                stageTransform(
                        translate(0.5, 0.375, 0.0),
                        rotateX(30.0),
                        rotateY(225.0),
                        scale(0.0625),
                        translate(guiTranslateX, guiTranslateY, guiTranslateZ),
                        scale(guiScale),
                        rotateY(guiYaw)
                ));
        perspectiveMatrices.put(ItemCameraTransforms.TransformType.NONE, null);
    }

    private static Matrix4f stageTransform(Matrix4f... itemRenderBaseFrame) {
        return compose(HALF_BLOCK_NEGATIVE, compose(itemRenderBaseFrame), HALF_BLOCK_POSITIVE);
    }

    private static Matrix4f leftHandStageTransform(Matrix4f... itemRenderBaseFrame) {
        // Forge applies its own flip-X conjugation to non-null left-hand perspective matrices.
        return compose(FLIP_X, stageTransform(itemRenderBaseFrame), FLIP_X);
    }
}
