package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

import static com.hbm.render.NTMRenderHelper.bindTexture;

public class ModelArmorDiesel extends ModelArmorBase {

  public ModelArmorDiesel(int type) {
    super(type);

    this.head = new ModelRendererObj(ResourceManager.armor_dieselsuit, "Head");
    this.body = new ModelRendererObj(ResourceManager.armor_dieselsuit, "Body");
    this.leftArm =
        new ModelRendererObj(ResourceManager.armor_dieselsuit, "LeftArm")
            .setRotationPoint(5.0F, 2.0F, 0.0F);
    this.rightArm =
        new ModelRendererObj(ResourceManager.armor_dieselsuit, "RightArm")
            .setRotationPoint(-5.0F, 2.0F, 0.0F);
    this.leftLeg =
        new ModelRendererObj(ResourceManager.armor_dieselsuit, "LeftLeg")
            .setRotationPoint(1.9F, 12.0F, 0.0F);
    this.rightLeg =
        new ModelRendererObj(ResourceManager.armor_dieselsuit, "RightLeg")
            .setRotationPoint(-1.9F, 12.0F, 0.0F);
    this.leftFoot =
        new ModelRendererObj(ResourceManager.armor_dieselsuit, "LeftBoot")
            .setRotationPoint(1.9F, 12.0F, 0.0F);
    this.rightFoot =
        new ModelRendererObj(ResourceManager.armor_dieselsuit, "RightBoot")
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
    GlStateManager.shadeModel(GL11.GL_SMOOTH);

    switch (type) {
      case 0 -> {
        bindTexture(ResourceManager.dieselsuit_helmet);
        this.head.render(scaleFactor);
      }
      case 1 -> {
        bindTexture(ResourceManager.dieselsuit_chest);
        this.body.render(scaleFactor);
        bindTexture(ResourceManager.dieselsuit_arm);
        this.leftArm.render(scaleFactor);
        this.rightArm.render(scaleFactor);
      }
      case 2 -> {
        bindTexture(ResourceManager.dieselsuit_leg);
        this.leftLeg.render(scaleFactor);
        this.rightLeg.render(scaleFactor);
      }
      case 3 -> {
        bindTexture(ResourceManager.dieselsuit_leg);
        this.leftFoot.render(scaleFactor);
        this.rightFoot.render(scaleFactor);
      }
    }

    GlStateManager.shadeModel(GL11.GL_FLAT);
    GlStateManager.popMatrix();
  }

  @Override
  public void renderArmor(Entity par1Entity, float par7) {
    switch (type) {
      case 0 -> {
        bindTexture(ResourceManager.dieselsuit_helmet);
        head.render(par7 * 1.001F);
      }
      case 1 -> {
        bindTexture(ResourceManager.dieselsuit_chest);
        body.render(par7);
        bindTexture(ResourceManager.dieselsuit_arm);
        leftArm.render(par7);
        rightArm.render(par7);
      }
      case 2 -> {
        bindTexture(ResourceManager.dieselsuit_leg);
        leftLeg.render(par7);
        rightLeg.render(par7);
      }
      case 3 -> {
        bindTexture(ResourceManager.dieselsuit_leg);
        leftFoot.render(par7);
        rightFoot.render(par7);
      }
    }
  }
}
