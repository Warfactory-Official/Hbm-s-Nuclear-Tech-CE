package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

import static com.hbm.render.NTMRenderHelper.bindTexture;

public class ModelArmorDigamma extends ModelArmorBase {

  private final ModelRendererObj cassette;

  public ModelArmorDigamma(int type) {
    super(type);

    head = new ModelRendererObj(ResourceManager.armor_fau, "Head");
    body = new ModelRendererObj(ResourceManager.armor_fau, "Body");
    cassette = new ModelRendererObj(ResourceManager.armor_fau, "Cassette");
    leftArm =
        new ModelRendererObj(ResourceManager.armor_fau, "LeftArm")
            .setRotationPoint(-5.0F, 2.0F, 0.0F);
    rightArm =
        new ModelRendererObj(ResourceManager.armor_fau, "RightArm")
            .setRotationPoint(5.0F, 2.0F, 0.0F);
    leftLeg =
        new ModelRendererObj(ResourceManager.armor_fau, "LeftLeg")
            .setRotationPoint(1.9F, 12.0F, 0.0F);
    rightLeg =
        new ModelRendererObj(ResourceManager.armor_fau, "RightLeg")
            .setRotationPoint(-1.9F, 12.0F, 0.0F);
    leftFoot =
        new ModelRendererObj(ResourceManager.armor_fau, "LeftBoot")
            .setRotationPoint(1.9F, 12.0F, 0.0F);
    rightFoot =
        new ModelRendererObj(ResourceManager.armor_fau, "RightBoot")
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

    this.body.copyTo(this.cassette);

    switch (type) {
      case 0 -> {
        bindTexture(ResourceManager.fau_helmet);
        this.head.render(scaleFactor);
      }
      case 1 -> {
        bindTexture(ResourceManager.fau_chest);
        this.body.render(scaleFactor);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(
            GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        bindTexture(ResourceManager.fau_cassette);
        this.cassette.render(scaleFactor);
        bindTexture(ResourceManager.fau_arm);
        this.leftArm.render(scaleFactor);
        this.rightArm.render(scaleFactor);
      }
      case 2 -> {
        bindTexture(ResourceManager.fau_leg);
        this.leftLeg.render(scaleFactor);
        this.rightLeg.render(scaleFactor);
      }
      case 3 -> {
        bindTexture(ResourceManager.fau_leg);
        this.leftFoot.render(scaleFactor);
        this.rightFoot.render(scaleFactor);
      }
    }

    GlStateManager.shadeModel(GL11.GL_FLAT);
    GlStateManager.popMatrix();
  }

  @Override
  public void renderArmor(Entity par1Entity, float par7) {

    body.copyTo(cassette);

    switch (type) {
      case 3 -> {
        bindTexture(ResourceManager.fau_helmet);
        head.render(par7 * 1.1F);
      }
      case 2 -> {
        bindTexture(ResourceManager.fau_chest);
        body.render(par7 * 1.1F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        bindTexture(ResourceManager.fau_cassette);
        cassette.render(par7 * 1.1F);
        bindTexture(ResourceManager.fau_arm);
        leftArm.render(par7 * 1.1F);
        rightArm.render(par7 * 1.1F);
      }
      case 1 -> {
        bindTexture(ResourceManager.fau_leg);
        leftLeg.render(par7 * 1.1F);
        rightLeg.render(par7 * 1.1F);
      }
      case 0 -> {
        bindTexture(ResourceManager.fau_leg);
        leftFoot.render(par7 * 1.1F);
        rightFoot.render(par7 * 1.1F);
      }
    }
  }
}
