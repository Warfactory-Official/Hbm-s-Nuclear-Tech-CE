package com.hbm.inventory.control_panel.controls;

import com.hbm.inventory.control_panel.*;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;
import java.util.Map;

public class Label extends Control {

    private float[] color = new float[] {1, 1, 1};
    private String text = "label";
    private int scale = 25;

    float width = 0;
    float height = 0;

    public Label(String name, ControlPanel panel) {
        super(name, panel);
        vars.put("isLit", new DataValueFloat(0));
        configMap.put("colorR", new DataValueFloat(color[0]));
        configMap.put("colorG", new DataValueFloat(color[1]));
        configMap.put("colorB", new DataValueFloat(color[2]));
        configMap.put("text", new DataValueString(text));
        configMap.put("scale", new DataValueFloat(scale));
    }

    @Override
    public ControlType getControlType() {
        return ControlType.LABEL;
    }

    @Override
    public float[] getSize() {
        return new float[] {0, 0, 0};
    }

    @Override
    public float[] getBox() {
        return new float[] {posX, posY, posX + (width*scale/500F), posY + (height*scale/500F)};
    }

    @Override
    public void applyConfigs(Map<String, DataValue> configs) {
        super.applyConfigs(configs);

        for (Map.Entry<String, DataValue> e : configMap.entrySet()) {
            switch (e.getKey()) {
                case "colorR" : {
                    color[0] = e.getValue().getNumber();
                    break;
                }
                case "colorG" : {
                    color[1] = e.getValue().getNumber();
                    break;
                }
                case "colorB" : {
                    color[2] = e.getValue().getNumber();
                    break;
                }
                case "text" : {
                    text = e.getValue().toString();
                    break;
                }
                case "scale" : {
                    scale = (int) e.getValue().getNumber();
                    break;
                }
            }
        }
    }

    @Override
    public void render() {
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;

        float lX = OpenGlHelper.lastBrightnessX;
        float lY = OpenGlHelper.lastBrightnessY;

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, 0, posY);
        GlStateManager.depthMask(false);

        boolean isLit = getVar("isLit").getBoolean();
        width = font.getStringWidth(text);
        height = font.FONT_HEIGHT;

        float s = scale/500F;
        GlStateManager.scale(s, -s, s);
        GlStateManager.color(0.0F, 0.0F, -1.0F);
        GlStateManager.rotate(90, 1, 0, 0);
        GlStateManager.translate(0, 0, .1F);

        int r = (int) (color[0]*255);
        int g = (int) (color[1]*255);
        int b = (int) (color[2]*255);
        int rgb = (r << 16) | (g << 8) | b;

        if (isLit) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        }

        GlStateManager.disableLighting();
        font.drawString(text, 0, 0, rgb, false);
        GlStateManager.enableLighting();

        if (isLit) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lX, lY);
        }

        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    @Override
    public IModelCustom getModel() {
        return ResourceManager.ctrl_display_seven_seg;
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return ResourceManager.ctrl_display_seven_seg_gui_tex;
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return null;
    }

    @Override
    public Control newControl(ControlPanel panel) {
        return new Label(name, panel);
    }

    @Override
    public void populateDefaultNodes(List<ControlEvent> receiveEvents) {

    }
}
