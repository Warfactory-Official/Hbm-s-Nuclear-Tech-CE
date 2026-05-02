package com.hbm.render.skinlayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;

public class BobbleSkinModel {

    private static final float BODY_SCALE = 0.0625f;
    private static final float HEAD_SCALE = 0.09375f;

    private static final float HEAD_OVERLAY_XZ = 1.08f;
    private static final float HEAD_OVERLAY_Y = 1.035f;
    private static final float BODY_OVERLAY_XZ = 1.05f;
    private static final float BODY_OVERLAY_Y = 1.035f;
    private static final float LIMB_OVERLAY_XZ = 1.15f;
    private static final float LIMB_OVERLAY_Y = 1.035f;

    private static final float LIMB_PIVOT_Y = 12f;
    private static final float BODY_PIVOT_Y = 12f;
    private static final float HEAD_PIVOT_Y = 0f;
    private static BobbleSkinModel grayModel;
    private static ResourceLocation grayTextureLoc;
    private final SkinBoxVoxelizer headBase;
    private final SkinBoxVoxelizer bodyBase;
    private final SkinBoxVoxelizer rightArmBase;
    private final SkinBoxVoxelizer leftArmBase;
    private final SkinBoxVoxelizer rightLegBase;
    private final SkinBoxVoxelizer leftLegBase;
    private final SkinBoxVoxelizer headOverlay;
    private final SkinBoxVoxelizer bodyOverlay;
    private final SkinBoxVoxelizer rightArmOverlay;
    private final SkinBoxVoxelizer leftArmOverlay;
    private final SkinBoxVoxelizer rightLegOverlay;
    private final SkinBoxVoxelizer leftLegOverlay;

    public BobbleSkinModel(BufferedImage skin) {
        headBase = SkinBoxVoxelizer.create(skin, 8, 8, 8, 0, 0);
        bodyBase = SkinBoxVoxelizer.create(skin, 8, 12, 4, 16, 16);
        rightArmBase = SkinBoxVoxelizer.create(skin, 4, 12, 4, 40, 16);
        leftArmBase = SkinBoxVoxelizer.create(skin, 4, 12, 4, 32, 48);
        rightLegBase = SkinBoxVoxelizer.create(skin, 4, 12, 4, 0, 16);
        leftLegBase = SkinBoxVoxelizer.create(skin, 4, 12, 4, 16, 48);
        headOverlay = SkinBoxVoxelizer.create(skin, 8, 8, 8, 32, 0);
        bodyOverlay = SkinBoxVoxelizer.create(skin, 8, 12, 4, 16, 32);
        rightArmOverlay = SkinBoxVoxelizer.create(skin, 4, 12, 4, 40, 32);
        leftArmOverlay = SkinBoxVoxelizer.create(skin, 4, 12, 4, 48, 48);
        rightLegOverlay = SkinBoxVoxelizer.create(skin, 4, 12, 4, 0, 32);
        leftLegOverlay = SkinBoxVoxelizer.create(skin, 4, 12, 4, 0, 48);
    }

    public static BobbleSkinModel gray() {
        if (grayModel == null) genGray();
        return grayModel;
    }

    public static ResourceLocation grayTexture() {
        if (grayModel == null) genGray();
        return grayTextureLoc;
    }

    private static void genGray() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        int gray = 0xFFA0A0A0;
        fillRect(img, 0, 0, 32, 16, gray);    // head base
        fillRect(img, 0, 16, 16, 16, gray);   // right leg
        fillRect(img, 16, 16, 24, 16, gray);  // body
        fillRect(img, 40, 16, 16, 16, gray);  // right arm
        fillRect(img, 16, 48, 16, 16, gray);  // left leg
        fillRect(img, 32, 48, 16, 16, gray);  // left arm
        grayModel = new BobbleSkinModel(img);
        DynamicTexture dt = new DynamicTexture(img);
        grayTextureLoc = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("ntm_bobble_gray_skin", dt);
    }

    private static void fillRect(BufferedImage img, int x, int y, int w, int h, int argb) {
        for (int j = 0; j < h; j++)
            for (int i = 0; i < w; i++)
                img.setRGB(x + i, y + j, argb);
    }

    private void renderPartPair(SkinBoxVoxelizer base, SkinBoxVoxelizer overlay, float scale, float scaleXZ,
                                float scaleY, float pivotY) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        base.render();
        if (!overlay.isEmpty()) {
            GlStateManager.translate(0, pivotY, 0);
            GlStateManager.scale(scaleXZ, scaleY, scaleXZ);
            GlStateManager.translate(0, -pivotY, 0);
            overlay.render();
        }
        GlStateManager.popMatrix();
    }

    public void render(long time, double[] rotLeftArm, double[] rotRightArm, double[] rotLeftLeg, double[] rotRightLeg,
                       double rotBody, double[] rotHead) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate((float) rotBody - 90.0F, 0, 1, 0);

        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.color(1F, 1F, 1F, 1F);

        // LEFT LEG
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.125, 1, 0);
        GlStateManager.rotate((float) rotLeftLeg[0], 1, 0, 0);
        GlStateManager.rotate((float) rotLeftLeg[1], 0, 1, 0);
        GlStateManager.rotate((float) rotLeftLeg[2], 0, 0, 1);
        GlStateManager.translate(0, -0.75, 0);
        renderPartPair(leftLegBase, leftLegOverlay, BODY_SCALE, LIMB_OVERLAY_XZ, LIMB_OVERLAY_Y, LIMB_PIVOT_Y);
        GlStateManager.popMatrix();

        // RIGHT LEG
        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.125, 1, 0);
        GlStateManager.rotate((float) rotRightLeg[0], 1, 0, 0);
        GlStateManager.rotate((float) rotRightLeg[1], 0, 1, 0);
        GlStateManager.rotate((float) rotRightLeg[2], 0, 0, 1);
        GlStateManager.translate(0, -0.75, 0);
        renderPartPair(rightLegBase, rightLegOverlay, BODY_SCALE, LIMB_OVERLAY_XZ, LIMB_OVERLAY_Y, LIMB_PIVOT_Y);
        GlStateManager.popMatrix();

        // LEFT ARM
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.25, 1.625, 0);
        GlStateManager.rotate((float) rotLeftArm[0], 1, 0, 0);
        GlStateManager.rotate((float) rotLeftArm[1], 0, 1, 0);
        GlStateManager.rotate((float) rotLeftArm[2], 0, 0, 1);
        GlStateManager.translate(0.125, -0.625, 0);
        renderPartPair(leftArmBase, leftArmOverlay, BODY_SCALE, LIMB_OVERLAY_XZ, LIMB_OVERLAY_Y, LIMB_PIVOT_Y);
        GlStateManager.popMatrix();

        // RIGHT ARM
        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.25, 1.625, 0);
        GlStateManager.rotate((float) rotRightArm[0], 1, 0, 0);
        GlStateManager.rotate((float) rotRightArm[1], 0, 1, 0);
        GlStateManager.rotate((float) rotRightArm[2], 0, 0, 1);
        GlStateManager.translate(-0.125, -0.625, 0);
        renderPartPair(rightArmBase, rightArmOverlay, BODY_SCALE, LIMB_OVERLAY_XZ, LIMB_OVERLAY_Y, LIMB_PIVOT_Y);
        GlStateManager.popMatrix();

        // BODY
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1.0, 0);
        renderPartPair(bodyBase, bodyOverlay, BODY_SCALE, BODY_OVERLAY_XZ, BODY_OVERLAY_Y, BODY_PIVOT_Y);
        GlStateManager.popMatrix();

        // HEAD
        double speed = 0.005;
        double amplitude = 1;

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1.75, 0);
        GlStateManager.rotate((float) (Math.sin(time * speed) * amplitude), 1, 0, 0);
        GlStateManager.rotate((float) (Math.sin(time * speed + Math.PI * 0.5) * amplitude), 0, 0, 1);
        GlStateManager.rotate((float) rotHead[0], 1, 0, 0);
        GlStateManager.rotate((float) rotHead[1], 0, 1, 0);
        GlStateManager.rotate((float) rotHead[2], 0, 0, 1);
        renderPartPair(headBase, headOverlay, HEAD_SCALE, HEAD_OVERLAY_XZ, HEAD_OVERLAY_Y, HEAD_PIVOT_Y);
        GlStateManager.popMatrix();

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();

        GlStateManager.popMatrix();
    }
}
