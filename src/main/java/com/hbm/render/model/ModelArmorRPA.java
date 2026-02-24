package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

import static com.hbm.render.NTMRenderHelper.bindTexture;

public class ModelArmorRPA extends ModelArmorBase {
    ModelRendererObj fan;
    ModelRendererObj glow;

    public ModelArmorRPA(int type) {
        super(type);

        head = new ModelRendererObj(ResourceManager.armor_remnant, "Head");
        body = new ModelRendererObj(ResourceManager.armor_remnant, "Body");
        fan = new ModelRendererObj(ResourceManager.armor_remnant, "Fan");
        glow = new ModelRendererObj(ResourceManager.armor_remnant, "Glow");
        leftArm = new ModelRendererObj(ResourceManager.armor_remnant, "LeftArm").setRotationPoint(5.0F, 2.0F, 0.0F);
        rightArm = new ModelRendererObj(ResourceManager.armor_remnant, "RightArm").setRotationPoint(-5.0F, 2.0F, 0.0F);
        leftLeg = new ModelRendererObj(ResourceManager.armor_remnant, "LeftLeg").setRotationPoint(1.9F, 12.0F, 0.0F);
        rightLeg = new ModelRendererObj(ResourceManager.armor_remnant, "RightLeg").setRotationPoint(-1.9F, 12.0F, 0.0F);
        leftFoot = new ModelRendererObj(ResourceManager.armor_remnant, "LeftBoot").setRotationPoint(1.9F, 12.0F, 0.0F);
        rightFoot = new ModelRendererObj(ResourceManager.armor_remnant, "RightBoot").setRotationPoint(-1.9F, 12.0F, 0.0F);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {

        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
        //body.copyTo(fan);
        this.body.copyTo(this.glow);

        GlStateManager.pushMatrix();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        switch(type) {
            case 0 -> {
                bindTexture(ResourceManager.rpa_helmet);
                this.head.render(scaleFactor);
            }
            case 1 -> {
                bindTexture(ResourceManager.rpa_arm);
                this.leftArm.render(scaleFactor);
                this.rightArm.render(scaleFactor);

                bindTexture(ResourceManager.rpa_chest);
                this.body.render(scaleFactor);

                // START GLOW //
                float lastX = OpenGlHelper.lastBrightnessX;
                float lastY = OpenGlHelper.lastBrightnessY;
                RenderUtil.pushAttrib(GL11.GL_LIGHTING_BIT);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
                GlStateManager.disableLighting();
                this.glow.render(scaleFactor);
                GlStateManager.enableLighting();
                RenderUtil.popAttrib();
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
                // END GLOW //

                // START FAN //
                GlStateManager.pushMatrix();
                double px = 0.0625D;
                GlStateManager.translate(this.body.offsetX * (float) px, this.body.offsetY * (float) px, this.body.offsetZ * (float) px);
                GlStateManager.translate(this.body.rotationPointX * (float) px, this.body.rotationPointY * (float) px, this.body.rotationPointZ * (float) px);

                if(this.body.rotateAngleZ != 0.0F) {
                    GlStateManager.rotate(this.body.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
                }

                if(this.body.rotateAngleY != 0.0F) {
                    GlStateManager.rotate(this.body.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
                }

                if(this.body.rotateAngleX != 0.0F) {
                    GlStateManager.rotate(this.body.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
                }

                GlStateManager.translate(0, 4.875 * px, 0);
                GlStateManager.rotate(-System.currentTimeMillis() / 2D % 360, 0, 0, 1);
                GlStateManager.translate(0, -4.875 * px, 0);
                this.fan.render(scaleFactor);
                GlStateManager.popMatrix();
                // END FAN //
            }
            case 2 -> {
                bindTexture(ResourceManager.rpa_leg);
                this.leftLeg.render(scaleFactor);
                this.rightLeg.render(scaleFactor);
            }
            case 3 -> {
                bindTexture(ResourceManager.rpa_leg);
                this.leftFoot.render(scaleFactor);
                this.rightFoot.render(scaleFactor);
            }
        }

        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glPopMatrix();
    }

    @Override
    public void renderArmor(Entity par1Entity, float par7) {
        switch (type) {
            case 3 -> {
                bindTexture(ResourceManager.rpa_helmet);
                head.render(par7 * 1.05F);
            }
            case 2 -> {
                bindTexture(ResourceManager.rpa_chest);
                body.render(par7 * 1.05F);
                bindTexture(ResourceManager.rpa_arm);
                leftArm.render(par7 * 1.05F);
                rightArm.render(par7 * 1.05F);
            }
            case 1 -> {
                bindTexture(ResourceManager.rpa_leg);
                leftLeg.render(par7 * 1.05F);
                rightLeg.render(par7 * 1.05F);
            }
            case 0 -> {
                bindTexture(ResourceManager.rpa_leg);
                leftFoot.render(par7 * 1.05F);
                rightFoot.render(par7 * 1.05F);
            }
        }
    }
}