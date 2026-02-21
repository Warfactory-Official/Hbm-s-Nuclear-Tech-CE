package com.hbm.inventory.control_panel.controls;

import com.hbm.render.loader.WaveFrontObjectVAO;
import com.hbm.inventory.control_panel.*;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Map;


public class DisplaySevenSeg extends Control {

    private float[] color = new float[] {1, 1, 1};
    private int digitCount = 1;
    private boolean isDecimal = false;

    public DisplaySevenSeg(String name, ControlPanel panel) {
        super(name, panel);
        vars.put("value", new DataValueFloat(0));
        configMap.put("colorR", new DataValueFloat(color[0]));
        configMap.put("colorG", new DataValueFloat(color[1]));
        configMap.put("colorB", new DataValueFloat(color[2]));
        configMap.put("digitCount", new DataValueFloat(digitCount));
        configMap.put("isDecimal", new DataValueFloat(0));
    }

    @Override
    public ControlType getControlType() {
        return ControlType.DISPLAY;
    }

    @Override
    public float[] getSize() {
        return new float[] {.75F, 1.125F, .06F};
    }

    @Override
    public float[] getBox() {
        float width = getSize()[0];
        float length = getSize()[1];
        return new float[] {posX - (width*digitCount-((digitCount-1)*.125F)) + width, posY, posX + width, posY + length};
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
                case "digitCount" : {
                    digitCount = (int) e.getValue().getNumber();
//                    posX = posX + ((digitCount-1)*getSize()[0]);
                    break;
                }
                case "isDecimal" : {
                    isDecimal = e.getValue().getBoolean();
                    break;
                }
            }
        }
    }

//    A
//  F   B
//    G
//  E   C
//    D

    // abcdefg encoding
    private static final byte[] chars = {
            0x7E, 0x30, 0x6D, 0x79, 0x33, 0x5B, 0x5F, 0x70, 0x7F, 0x7B, 0x77, 0x1F, 0x4E, 0x3D, 0x4F, 0x47
    };

    @Override
    public void render() {
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.ctrl_display_seven_seg_tex);

        WaveFrontObjectVAO model = (WaveFrontObjectVAO) getModel();

        int value = Math.max(0, (int) getVar("value").getNumber()); // TODO: negative config
        int base = (isDecimal) ? 10 : 16;

        float lX = OpenGlHelper.lastBrightnessX;
        float lY = OpenGlHelper.lastBrightnessY;

        for (int i = 0; i < digitCount; i++) {
            byte character = chars[value % base];
            value /= base;

            float t_off = i * getSize()[0] - i * (i > 0 ? 0.125F : 0);

            GlStateManager.pushMatrix();
            GlStateManager.translate(posX - t_off, 0.0F, posY);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            model.renderPart("base");

            GlStateManager.disableTexture2D();
            GlStateManager.color(0.31F, 0.31F, 0.31F, 1.0F);
            model.renderPart("border");

            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
            for (int j = 0; j < 7; j++) {
                boolean enabled = (character & (1 << j)) != 0;
                float cMul = enabled ? 1.0F : 0.1F;
                GlStateManager.color(color[0] * cMul, color[1] * cMul, color[2] * cMul, 1.0F);
                model.renderPart("seg_" + (6 - j));
            }
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lX, lY);

            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IModelCustom getModel() {
        return ResourceManager.ctrl_display_seven_seg;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ResourceLocation getGuiTexture() {
        return ResourceManager.ctrl_display_seven_seg_gui_tex;
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
        return new DisplaySevenSeg(name, panel);
    }
}
