package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

import static com.hbm.render.NTMRenderHelper.bindTexture;

public class ModelArmorNCRPA extends ModelArmorBase {

    ModelRendererObj eyes;

    public ModelArmorNCRPA(int type) {
        super(type);

        this.head = new ModelRendererObj(ResourceManager.armor_ncr, "Helmet");
        this.eyes = new ModelRendererObj(ResourceManager.armor_ncr, "Eyes");
        this.body = new ModelRendererObj(ResourceManager.armor_ncr, "Chest");
        this.leftArm = new ModelRendererObj(ResourceManager.armor_ncr, "LeftArm").setRotationPoint(5.0F, 2.0F, 0.0F);
        this.rightArm = new ModelRendererObj(ResourceManager.armor_ncr, "RightArm").setRotationPoint(-5.0F, 2.0F, 0.0F);
        this.leftLeg = new ModelRendererObj(ResourceManager.armor_ncr, "LeftLeg").setRotationPoint(1.9F, 12.0F, 0.0F);
        this.rightLeg = new ModelRendererObj(ResourceManager.armor_ncr, "RightLeg").setRotationPoint(-1.9F, 12.0F, 0.0F);
        this.leftFoot = new ModelRendererObj(ResourceManager.armor_ncr, "LeftBoot").setRotationPoint(1.9F, 12.0F, 0.0F);
        this.rightFoot = new ModelRendererObj(ResourceManager.armor_ncr, "RightBoot").setRotationPoint(-1.9F, 12.0F, 0.0F);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {

        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
        this.head.copyTo(this.eyes);

        GlStateManager.pushMatrix();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        switch(type) {
            case 0 -> {
                bindTexture(ResourceManager.ncrpa_helmet);
                this.head.render(scaleFactor);

                // START GLOW //
                float lastX = OpenGlHelper.lastBrightnessX;
                float lastY = OpenGlHelper.lastBrightnessY;
                RenderUtil.pushAttrib(GL11.GL_LIGHTING_BIT);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
                GlStateManager.disableLighting();
                this.eyes.render(scaleFactor);
                GlStateManager.enableLighting();
                RenderUtil.popAttrib();
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
                // END GLOW //
            }
            case 1 -> {
                bindTexture(ResourceManager.ncrpa_arm);
                this.leftArm.render(scaleFactor);
                this.rightArm.render(scaleFactor);

                bindTexture(ResourceManager.ncrpa_chest);
                this.body.render(scaleFactor);
            }
            case 2 -> {
                bindTexture(ResourceManager.ncrpa_leg);
                this.leftLeg.render(scaleFactor);
                this.rightLeg.render(scaleFactor);
            }
            case 3 -> {
                bindTexture(ResourceManager.ncrpa_leg);
                this.leftFoot.render(scaleFactor);
                this.rightFoot.render(scaleFactor);
            }
        }

        GL11.glShadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }

    @Override
    public void renderArmor(Entity par1Entity, float par7) {
        switch (type) {
            case 3 -> {
                bindTexture(ResourceManager.ncrpa_helmet);
                head.render(par7 * 1.05F);
            }
            case 2 -> {
                bindTexture(ResourceManager.ncrpa_chest);
                body.render(par7 * 1.05F);
                bindTexture(ResourceManager.ncrpa_arm);
                leftArm.render(par7 * 1.05F);
                rightArm.render(par7 * 1.05F);
            }
            case 1 -> {
                bindTexture(ResourceManager.ncrpa_leg);
                leftLeg.render(par7 * 1.05F);
                rightLeg.render(par7 * 1.05F);
            }
            case 0 -> {
                bindTexture(ResourceManager.ncrpa_leg);
                leftFoot.render(par7 * 1.05F);
                rightFoot.render(par7 * 1.05F);
            }
        }
    }
}