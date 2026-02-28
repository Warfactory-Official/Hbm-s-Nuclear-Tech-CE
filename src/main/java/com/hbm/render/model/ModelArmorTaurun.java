package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

import static com.hbm.render.NTMRenderHelper.bindTexture;

public class ModelArmorTaurun extends ModelArmorBase {

  public ModelArmorTaurun(int type) {
    super(type);

    this.head = new ModelRendererObj(ResourceManager.armor_taurun, "Helmet");
    this.body = new ModelRendererObj(ResourceManager.armor_taurun, "Chest");
    this.leftArm =
        new ModelRendererObj(ResourceManager.armor_taurun, "LeftArm")
            .setRotationPoint(5.0F, 2.0F, 0.0F);
    this.rightArm =
        new ModelRendererObj(ResourceManager.armor_taurun, "RightArm")
            .setRotationPoint(-5.0F, 2.0F, 0.0F);
    this.leftLeg =
        new ModelRendererObj(ResourceManager.armor_taurun, "LeftLeg")
            .setRotationPoint(1.9F, 12.0F, 0.0F);
    this.rightLeg =
        new ModelRendererObj(ResourceManager.armor_taurun, "RightLeg")
            .setRotationPoint(-1.9F, 12.0F, 0.0F);
    this.leftFoot =
        new ModelRendererObj(ResourceManager.armor_taurun, "LeftBoot")
            .setRotationPoint(1.9F, 12.0F, 0.0F);
    this.rightFoot =
        new ModelRendererObj(ResourceManager.armor_taurun, "RightBoot")
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

    GlStateManager.pushMatrix();

    switch (type) {
      case 0 -> {
        bindTexture(ResourceManager.taurun_helmet);
        this.head.render(scaleFactor);
      }
      case 1 -> {
        bindTexture(ResourceManager.taurun_chest);
        this.body.render(scaleFactor);
        bindTexture(ResourceManager.taurun_arm);
        this.leftArm.render(scaleFactor);
        this.rightArm.render(scaleFactor);
      }
      case 2 -> {
        bindTexture(ResourceManager.taurun_leg);
        GlStateManager.translate(-0.01, 0, 0);
        this.leftLeg.render(scaleFactor);
        GlStateManager.translate(0.02, 0, 0);
        this.rightLeg.render(scaleFactor);
      }
      case 3 -> {
        bindTexture(ResourceManager.taurun_leg);
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
        bindTexture(ResourceManager.taurun_helmet);
        head.render(par7 * 1.001F);
      }
      case 1 -> {
        bindTexture(ResourceManager.taurun_chest);
        body.render(par7);
        bindTexture(ResourceManager.taurun_arm);
        leftArm.render(par7);
        rightArm.render(par7);
      }
      case 2 -> {
        bindTexture(ResourceManager.taurun_leg);
        leftLeg.render(par7);
        rightLeg.render(par7);
      }
      case 3 -> {
        bindTexture(ResourceManager.taurun_leg);
        leftFoot.render(par7);
        rightFoot.render(par7);
      }
    }
  }
}
