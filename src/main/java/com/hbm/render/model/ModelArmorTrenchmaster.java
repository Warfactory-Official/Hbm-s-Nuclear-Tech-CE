package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

import static com.hbm.render.NTMRenderHelper.bindTexture;

public class ModelArmorTrenchmaster extends ModelArmorBase {

  ModelRendererObj light;

  public ModelArmorTrenchmaster(int type) {
    super(type);

    this.head = new ModelRendererObj(ResourceManager.armor_trenchmaster, "Helmet");
    this.light = new ModelRendererObj(ResourceManager.armor_trenchmaster, "Light");
    this.body = new ModelRendererObj(ResourceManager.armor_trenchmaster, "Chest");
    this.leftArm =
        new ModelRendererObj(ResourceManager.armor_trenchmaster, "LeftArm")
            .setRotationPoint(5.0F, 2.0F, 0.0F);
    this.rightArm =
        new ModelRendererObj(ResourceManager.armor_trenchmaster, "RightArm")
            .setRotationPoint(-5.0F, 2.0F, 0.0F);
    this.leftLeg =
        new ModelRendererObj(ResourceManager.armor_trenchmaster, "LeftLeg")
            .setRotationPoint(1.9F, 12.0F, 0.0F);
    this.rightLeg =
        new ModelRendererObj(ResourceManager.armor_trenchmaster, "RightLeg")
            .setRotationPoint(-1.9F, 12.0F, 0.0F);
    this.leftFoot =
        new ModelRendererObj(ResourceManager.armor_trenchmaster, "LeftBoot")
            .setRotationPoint(1.9F, 12.0F, 0.0F);
    this.rightFoot =
        new ModelRendererObj(ResourceManager.armor_trenchmaster, "RightBoot")
            .setRotationPoint(-1.9F, 12.0F, 0.0F);
  }

  @Override
  public void render(
      Entity entity,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      float netHeadYaw,
      float headPitch,
      float scaleFactor) {
    super.setRotationAngles(
        limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
    this.head.copyTo(this.light);

    GlStateManager.pushMatrix();

    switch (type) {
      case 0 -> {
        bindTexture(ResourceManager.trenchmaster_helmet);
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(
            GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        this.head.render(scaleFactor);
        GlStateManager.disableBlend();

        /// START GLOW ///
        float lastX = OpenGlHelper.lastBrightnessX;
        float lastY = OpenGlHelper.lastBrightnessY;
        GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
        GlStateManager.disableLighting();
        this.light.render(scaleFactor);
        GlStateManager.enableLighting();
        GL11.glPopAttrib();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
        /// END GLOW ///
      }
      case 1 -> {
        bindTexture(ResourceManager.trenchmaster_chest);
        this.body.render(scaleFactor);
        bindTexture(ResourceManager.trenchmaster_arm);
        this.leftArm.render(scaleFactor);
        this.rightArm.render(scaleFactor);
      }
      case 2 -> {
        bindTexture(ResourceManager.trenchmaster_leg);
        GlStateManager.translate(-0.01, 0, 0);
        this.leftLeg.render(scaleFactor);
        GlStateManager.translate(0.02, 0, 0);
        this.rightLeg.render(scaleFactor);
      }
      case 3 -> {
        bindTexture(ResourceManager.trenchmaster_leg);
        GlStateManager.translate(-0.01, 0, 0);
        this.leftFoot.render(scaleFactor);
        GlStateManager.translate(0.02, 0, 0);
        this.rightFoot.render(scaleFactor);
      }
    }

    GlStateManager.popMatrix();
  }

  @Override
  public void renderArmor(Entity par1Entity, float par7) {
    switch (type) {
      case 0 -> {
        this.head.copyTo(this.light);
        bindTexture(ResourceManager.trenchmaster_helmet);
        head.render(par7 * 1.001F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        head.render(par7);
        GlStateManager.disableBlend();

        /// START GLOW ///
        float lastX = OpenGlHelper.lastBrightnessX;
        float lastY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
        boolean prevLighting = RenderUtil.isLightingEnabled();
        GlStateManager.disableLighting();
        this.light.render(par7);
        if (prevLighting) GlStateManager.enableLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
        /// END GLOW ///
      }
      case 1 -> {
        bindTexture(ResourceManager.trenchmaster_chest);
        body.render(par7);
        bindTexture(ResourceManager.trenchmaster_arm);
        leftArm.render(par7);
        rightArm.render(par7);
      }
      case 2 -> {
        bindTexture(ResourceManager.trenchmaster_leg);
        leftLeg.render(par7);
        rightLeg.render(par7);
      }
      case 3 -> {
        bindTexture(ResourceManager.trenchmaster_leg);
        leftFoot.render(par7);
        rightFoot.render(par7);
      }
    }
  }
}
