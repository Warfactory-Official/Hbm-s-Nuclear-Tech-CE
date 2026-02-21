package com.hbm.inventory.control_panel.controls;

import com.hbm.render.loader.WaveFrontObjectVAO;
import com.hbm.inventory.control_panel.*;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.List;


public class IndicatorLamp extends Control {

    public IndicatorLamp(String name, ControlPanel panel) {
        super(name, panel);
        vars.put("isLit", new DataValueFloat(0));
        vars.put("color", new DataValueEnum<>(EnumDyeColor.RED));
    }

    @Override
    public ControlType getControlType() {
        return ControlType.INDICATOR;
    }

    @Override
    public float[] getSize() {
        return new float[] {.5F, .5F, .18F};
    }

    @Override
    public void render() {
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.ctrl_button_push_tex);

        WaveFrontObjectVAO model = (WaveFrontObjectVAO) getModel();
        boolean isLit = getVar("isLit").getBoolean();
        float[] color = getVar("color").getEnum(EnumDyeColor.class).getColorComponentValues();

        float lX = OpenGlHelper.lastBrightnessX;
        float lY = OpenGlHelper.lastBrightnessY;

        // --- Base (untextured dark plastic) ---
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.translate(posX, 0.0, posY);
        GlStateManager.color(0.3F, 0.3F, 0.3F, 1.0F);
        model.renderPart("base");
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();

        // --- Lamp emissive ---
        if (isLit) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        }

        float cMul = isLit ? 1.5F : 0.5F;
        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, 0.0, posY);
        GlStateManager.color(color[0] * cMul, color[1] * cMul, color[2] * cMul, 1.0F);
        model.renderPart("lamp");
        GlStateManager.popMatrix();

        if (isLit) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lX, lY);
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public IModelCustom getModel() {
        return ResourceManager.ctrl_indicator_lamp;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ResourceLocation getGuiTexture() {
        return ResourceManager.ctrl_indicator_lamp_gui_tex;
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return null;
    }

    @Override
    public void populateDefaultNodes(List<ControlEvent> receiveEvents) {
    }

    @Override
    public Control newControl(ControlPanel panel) {
        return new IndicatorLamp(name, panel);
    }
}
