package com.hbm.inventory.control_panel.controls;

import com.hbm.inventory.control_panel.*;
import com.hbm.inventory.control_panel.controls.configs.SubElementBaseConfig;
import com.hbm.inventory.control_panel.controls.configs.SubElementLabel;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;

public class Label extends Control {

    private float[] color = new float[] {1, 1, 1};
    private String text = "label";
    private int scale = 25;

    float width = 0;
    float height = 0;

    public Label(String name,String registryName,ControlPanel panel) {
        super(name,registryName, panel);
        vars.put("isLit", new DataValueFloat(0));
        configMap.put("colorR", new DataValueFloat(color[0]));
        configMap.put("colorG", new DataValueFloat(color[1]));
        configMap.put("colorB", new DataValueFloat(color[2]));
        configMap.put("text", new DataValueString(text));
        configMap.put("scale", new DataValueFloat(scale));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubElementBaseConfig getConfigSubElement(GuiControlEdit gui,Map<String,DataValue> configs) {
        return new SubElementLabel(gui,configs);
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
    public void fillBox(float[] box) {
        box[0] = posX;
        box[1] = posY;
        box[2] = posX + (width * scale / 500F);
        box[3] = posY + (height * scale / 500F);
    }

    @Override
    protected void onConfigMapChanged() {
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

    @SideOnly(Side.CLIENT)
    @Override
    public void renderControl(float[] renderBox,Control selectedControl,GuiControlEdit gui) {
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buf = tes.getBuffer();
        String text = getConfigs().get("text").toString();
        float scale = getConfigs().get("scale").getNumber()/500F;

        int r = (int) (getConfigs().get("colorR").getNumber()*255);
        int g = (int) (getConfigs().get("colorG").getNumber()*255 * ((this == selectedControl) ? .5F : 1F));
        int b = (int) (getConfigs().get("colorB").getNumber()*255);
        int rgb2 = (r << 16) | (g << 8) | b;

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, posY, 0);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(-posX, -posY, 0);
        gui.getFontRenderer().drawString(text, posX, posY, rgb2, false);
        GlStateManager.popMatrix();
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return null;
    }

    @Override
    public Control newControl(ControlPanel panel) {
        return new Label(name,registryName,panel);
    }

    @Override
    public void populateDefaultNodes(List<ControlEvent> receiveEvents) {

    }
}
