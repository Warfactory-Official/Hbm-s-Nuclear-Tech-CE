package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

import static com.hbm.render.NTMRenderHelper.bindTexture;

public class ModelArmorBJ extends ModelArmorBase {

  private final ModelRendererObj jetpack;

  public ModelArmorBJ(int type) {
    super(type);
    head = new ModelRendererObj(ResourceManager.armor_bj, "Head");
    body = new ModelRendererObj(ResourceManager.armor_bj, "Body");
    jetpack = new ModelRendererObj(ResourceManager.armor_bj, "Jetpack");
    leftArm =
        new ModelRendererObj(ResourceManager.armor_bj, "LeftArm")
            .setRotationPoint(-5.0F, 2.0F, 0.0F);
    rightArm =
        new ModelRendererObj(ResourceManager.armor_bj, "RightArm")
            .setRotationPoint(5.0F, 2.0F, 0.0F);
    leftLeg =
        new ModelRendererObj(ResourceManager.armor_bj, "LeftLeg")
            .setRotationPoint(1.9F, 12.0F, 0.0F);
    rightLeg =
        new ModelRendererObj(ResourceManager.armor_bj, "RightLeg")
            .setRotationPoint(-1.9F, 12.0F, 0.0F);
    leftFoot =
        new ModelRendererObj(ResourceManager.armor_bj, "LeftFoot")
            .setRotationPoint(1.9F, 12.0F, 0.0F);
    rightFoot =
        new ModelRendererObj(ResourceManager.armor_bj, "RightFoot")
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
    this.body.copyTo(this.jetpack);

    GlStateManager.pushMatrix();

    switch (type) {
      case 0 -> {
        bindTexture(ResourceManager.bj_eyepatch);
        this.head.render(scaleFactor);
      }
      case 1, 5 -> {
        bindTexture(ResourceManager.bj_chest);
        this.body.render(scaleFactor);

        if (this.type == 5) {
          bindTexture(ResourceManager.bj_jetpack);
          this.jetpack.render(scaleFactor);
        }

        bindTexture(ResourceManager.bj_arm);
        this.leftArm.render(scaleFactor);
        this.rightArm.render(scaleFactor);
      }
      case 2 -> {
        bindTexture(ResourceManager.bj_leg);
        this.leftLeg.render(scaleFactor);
        this.rightLeg.render(scaleFactor);
      }
      case 3 -> {
        bindTexture(ResourceManager.bj_leg);
        this.leftFoot.render(scaleFactor);
        this.rightFoot.render(scaleFactor);
      }
    }

    GlStateManager.popMatrix();
  }

  @Override
  public void renderArmor(Entity par1Entity, float par7) {
    body.copyTo(jetpack);
    switch (type) {
      case 0 -> {
        bindTexture(ResourceManager.bj_eyepatch);
        head.render(par7 * 1.001F);
      }
      case 1, 5 -> {
        bindTexture(ResourceManager.bj_chest);
        body.render(par7);

        if (type == 5) {
          bindTexture(ResourceManager.bj_jetpack);
          jetpack.render(par7);
        }

        bindTexture(ResourceManager.bj_arm);
        leftArm.render(par7);
        rightArm.render(par7);
      }
      case 2 -> {
        bindTexture(ResourceManager.bj_leg);
        leftLeg.render(par7);
        rightLeg.render(par7);
      }
      case 3 -> {
        bindTexture(ResourceManager.bj_leg);
        leftFoot.render(par7);
        rightFoot.render(par7);
      }
    }
  }
}
