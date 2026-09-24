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
    public void renderArmor(Entity par1Entity, float par7) {
        switch (type) {
            case 3 -> {
                bindTexture(ResourceManager.rpa_helmet);
                head.render(par7);
            }
            case 2 -> {
                this.body.copyTo(this.glow);

                bindTexture(ResourceManager.rpa_chest);
                body.render(par7);
                bindTexture(ResourceManager.rpa_arm);
                leftArm.render(par7);
                rightArm.render(par7);

                bindTexture(ResourceManager.rpa_chest);
                float lastX = OpenGlHelper.lastBrightnessX;
                float lastY = OpenGlHelper.lastBrightnessY;
                RenderUtil.pushAttrib(GL11.GL_LIGHTING_BIT);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
                GlStateManager.disableLighting();
                this.glow.render(par7);
                GlStateManager.enableLighting();
                RenderUtil.popAttrib();
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
                // END GLOW //

                // START FAN //
                bindTexture(ResourceManager.rpa_chest);
                GlStateManager.pushMatrix();
                double px = 0.0625D;
                this.body.applyTransform(par7);
                GlStateManager.translate(0, 4.875 * px, 0);
                GlStateManager.rotate((float) (-System.currentTimeMillis() / 2D % 360), 0, 0, 1);
                GlStateManager.translate(0, -4.875 * px, 0);
                this.fan.render(par7);
                GlStateManager.popMatrix();
                // END FAN //
            }
            case 1 -> {
                bindTexture(ResourceManager.rpa_leg);
                leftLeg.render(par7);
                rightLeg.render(par7);
            }
            case 0 -> {
                bindTexture(ResourceManager.rpa_leg);
                leftFoot.render(par7);
                rightFoot.render(par7);
            }
        }
    }
}