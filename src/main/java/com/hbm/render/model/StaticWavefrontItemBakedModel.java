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
                                         double guiScale) {
        initPerspectiveMatrices(guiTranslateX, guiTranslateY, guiTranslateZ, guiScale, 0.0D);
    }

    private void initPerspectiveMatrices(double guiTranslateX, double guiTranslateY, double guiTranslateZ,
                                         double guiScale, double guiYaw) {
        perspectiveMatrices.put(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND,
                stageTransform(
                        translate(0.5, 0.0, 0.5),
                        translate(0.0, 0.3, 0.0),
                        scale(0.2),
                        rotateY(135.0)
                ));
        perspectiveMatrices.put(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
                leftHandStageTransform(
                        translate(0.5, 0.0, 0.5),
                        translate(0.0, 0.3, 0.0),
                        scale(0.2),
                        rotateY(45.0)
                ));
        Matrix4f thirdRight = stageTransform(
                translate(0.5, 0.0, 0.5),
                translate(0.0, 0.25, 0.0),
                scale(0.1875),
                rotateY(180.0)
        );
        perspectiveMatrices.put(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, thirdRight);
        perspectiveMatrices.put(ItemCameraTransforms.TransformType.HEAD, thirdRight);
        perspectiveMatrices.put(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND,
                leftHandStageTransform(
                        translate(0.5, 0.0, 0.5),
                        translate(0.0, 0.25, 0.0),
                        scale(0.1875)
                ));
        perspectiveMatrices.put(ItemCameraTransforms.TransformType.GROUND,
                stageTransform(
                        translate(0.5, 0.0, 0.5),
                        translate(0.0, 0.3, 0.0),
                        scale(0.125),
                        rotateY(90.0)
                ));
        perspectiveMatrices.put(ItemCameraTransforms.TransformType.FIXED,
                stageTransform(
                        translate(0.5, 0.0, 0.5),
                        translate(0.0, 0.3, 0.0),
                        scale(0.25),
                        rotateY(90.0)
                ));
        perspectiveMatrices.put(ItemCameraTransforms.TransformType.GUI,
                stageTransform(
                        rotateX(30.0),
                        rotateY(225.0),
                        scale(0.0620),
                        translate(0.0, 11.3, -11.3),
                        translate(guiTranslateX, guiTranslateY, guiTranslateZ),
                        scale(guiScale),
                        rotateY(guiYaw)
                ));
        perspectiveMatrices.put(ItemCameraTransforms.TransformType.NONE, null);
    }

    private static Matrix4f stageTransform(Matrix4f... oldTeisrOperations) {
        return compose(HALF_BLOCK_NEGATIVE, compose(oldTeisrOperations), HALF_BLOCK_POSITIVE);
    }

    private static Matrix4f leftHandStageTransform(Matrix4f... oldTeisrOperations) {
        return compose(FLIP_X, stageTransform(oldTeisrOperations), FLIP_X);
    }
}
