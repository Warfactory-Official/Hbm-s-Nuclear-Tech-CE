package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

import static com.hbm.render.NTMRenderHelper.bindTexture;

public class ModelArmorHEV extends ModelArmorBase {

  public ModelArmorHEV(int type) {
    super(type);

    head = new ModelRendererObj(ResourceManager.armor_hev, "Head");
    body = new ModelRendererObj(ResourceManager.armor_hev, "Body");
    leftArm =
        new ModelRendererObj(ResourceManager.armor_hev, "LeftArm")
            .setRotationPoint(-5.0F, 2.0F, 0.0F);
    rightArm =
        new ModelRendererObj(ResourceManager.armor_hev, "RightArm")
            .setRotationPoint(5.0F, 2.0F, 0.0F);
    leftLeg =
        new ModelRendererObj(ResourceManager.armor_hev, "LeftLeg")
            .setRotationPoint(1.9F, 12.0F, 0.0F);
    rightLeg =
        new ModelRendererObj(ResourceManager.armor_hev, "RightLeg")
            .setRotationPoint(-1.9F, 12.0F, 0.0F);
    leftFoot =
        new ModelRendererObj(ResourceManager.armor_hev, "LeftFoot")
            .setRotationPoint(1.9F, 12.0F, 0.0F);
    rightFoot =
        new ModelRendererObj(ResourceManager.armor_hev, "RightFoot")
            .setRotationPoint(-1.9F, 12.0F, 0.0F);
  }

  @Override
  public void renderArmor(Entity par1Entity, float par7) {

    switch (type) {
      case 0 -> {
        bindTexture(ResourceManager.hev_helmet);
        head.render(par7 * 1.15F);
      }
      case 1 -> {
        bindTexture(ResourceManager.hev_chest);
        body.render(par7);
        bindTexture(ResourceManager.hev_arm);
        leftArm.render(par7);
        rightArm.render(par7);
      }
      case 2 -> {
        bindTexture(ResourceManager.hev_leg);
        leftLeg.render(par7);
        rightLeg.render(par7);
      }
      case 3 -> {
        bindTexture(ResourceManager.hev_leg);
        leftFoot.render(par7);
        rightFoot.render(par7);
      }
    }
  }
}
