package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import com.hbm.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

public class ModelArmorEnvsuit extends ModelArmorBase {

    ModelRendererObj lamps;

    public ModelArmorEnvsuit(int type) {
        super(type);

        this.head = new ModelRendererObj(ResourceManager.armor_envsuit, "Helmet");
        this.lamps = new ModelRendererObj(ResourceManager.armor_envsuit, "Lamps");
        this.body = new ModelRendererObj(ResourceManager.armor_envsuit, "Chest");
        this.leftArm = new ModelRendererObj(ResourceManager.armor_envsuit, "LeftArm").setRotationPoint(5.0F, 2.0F, 0.0F);
        this.rightArm = new ModelRendererObj(ResourceManager.armor_envsuit, "RightArm").setRotationPoint(-5.0F, 2.0F, 0.0F);
        this.leftLeg = new ModelRendererObj(ResourceManager.armor_envsuit, "LeftLeg").setRotationPoint(1.9F, 12.0F, 0.0F);
        this.rightLeg = new ModelRendererObj(ResourceManager.armor_envsuit, "RightLeg").setRotationPoint(-1.9F, 12.0F, 0.0F);
        this.leftFoot = new ModelRendererObj(ResourceManager.armor_envsuit, "LeftFoot").setRotationPoint(1.9F, 12.0F, 0.0F);
        this.rightFoot = new ModelRendererObj(ResourceManager.armor_envsuit, "RightFoot").setRotationPoint(-1.9F, 12.0F, 0.0F);
    }

    @Override
    public void renderArmor(Entity par1Entity, float par7) {
        switch (type) {
            case 0 -> {
                this.head.copyTo(this.lamps);
                Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.envsuit_helmet);
                final boolean prevBlend = RenderUtil.isBlendEnabled();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
                this.head.render(par7);
                GlStateManager.disableBlend();

                /// START GLOW ///
                final float lastX = OpenGlHelper.lastBrightnessX;
                final float lastY = OpenGlHelper.lastBrightnessY;

                final boolean prevLighting = RenderUtil.isLightingEnabled();
                final boolean prevTex2D    = RenderUtil.isTexture2DEnabled();
                final float prevR = RenderUtil.getCurrentColorRed();
                final float prevG = RenderUtil.getCurrentColorGreen();
                final float prevB = RenderUtil.getCurrentColorBlue();
                final float prevA = RenderUtil.getCurrentColorAlpha();

                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

                if (prevLighting) GlStateManager.disableLighting();
                if (prevTex2D)    GlStateManager.disableTexture2D();

                GlStateManager.color(1F, 1F, 0.8F, 1F);
                this.lamps.render(par7);
                GlStateManager.color(prevR, prevG, prevB, prevA);
                if (prevTex2D)    GlStateManager.enableTexture2D();
                if (prevLighting) GlStateManager.enableLighting();

                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
                if (prevBlend) GlStateManager.enableBlend();
                /// END GLOW ///
            }
            case 1 -> {
                Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.envsuit_chest);
                body.render(par7);
                Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.envsuit_arm);
                leftArm.render(par7);
                rightArm.render(par7);
            }
            case 2 -> {
                Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.envsuit_leg);
                leftLeg.render(par7);
                rightLeg.render(par7);
            }
            case 3 -> {
                Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.envsuit_leg);
                leftFoot.render(par7);
                rightFoot.render(par7);
            }
        }
    }
}
