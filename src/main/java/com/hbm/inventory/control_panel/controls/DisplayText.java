package com.hbm.inventory.control_panel.controls;

import com.hbm.inventory.control_panel.*;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Map;

public class DisplayText extends Control {

    private int scale = 25;
    private float width = 20;
    private float textWidth = 0; //TODO: stop all-too-long text
    private float height = 0;

    public DisplayText(String name, ControlPanel panel) {
        super(name, panel);
        vars.put("isLit", new DataValueFloat(0));
        vars.put("text", new DataValueString("text"));
        vars.put("color", new DataValueEnum<>(EnumDyeColor.WHITE));
        configMap.put("scale", new DataValueFloat(scale));
        configMap.put("width", new DataValueFloat(width));
    }

    @Override
    public ControlType getControlType() {
        return ControlType.DISPLAY;
    }

    @Override
    public float[] getSize() {
        return new float[] {0, 0, 0};
    }

    @Override
    public float[] getBox() {
        float d = .1F;
        return new float[] {posX-d, posY-d, posX + (width*1.5F*scale/500F)+d, posY + (height*scale/500F)+d};
    }

    @Override
    public void applyConfigs(Map<String, DataValue> configs) {
        super.applyConfigs(configs);

        for (Map.Entry<String, DataValue> e : configMap.entrySet()) {
            switch (e.getKey()) {
                case "scale": {
                    scale = (int) e.getValue().getNumber();
                    break;
                }
                case "width": {
                    width = (int) e.getValue().getNumber();
                    break;
                }
            }
        }
    }

    @Override
    public void render() {
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;

        String text = getVar("text").toString();
        boolean isLit = getVar("isLit").getBoolean();
        EnumDyeColor dyeColor = getVar("color").getEnum(EnumDyeColor.class);
        int color = dyeColor.getColorValue();

        textWidth = font.getStringWidth(text);
        height = font.FONT_HEIGHT;
        float s = scale/500F;

        float lX = OpenGlHelper.lastBrightnessX;
        float lY = OpenGlHelper.lastBrightnessY;

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, 0, posY);

        GlStateManager.translate(0, .03F, 0);
        GlStateManager.scale(s, -s, s);
        GlStateManager.color(0.0F, 0.0F, -1.0F);
        GlStateManager.rotate(90, 1, 0, 0);

        if (isLit) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        }

        GlStateManager.disableLighting();
        font.drawString(text, 0, 0, color, false);
        GlStateManager.enableLighting();

        if (isLit) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lX, lY);
        }

        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(90, 1, 0, 0);
        GlStateManager.translate(0, 0, -.01F);

        GlStateManager.disableTexture2D();
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buf = tes.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        float[] box = getBox();
        float[] rgb = new float[]{0, 0, 0};
        float d = 0;
        buf.pos(box[0]-d, box[1]-d, -.01).tex(0, 0).color(rgb[0], rgb[1], rgb[2], 1).endVertex();
        buf.pos(box[0]-d, box[3], -.01).tex(0, 1).color(rgb[0], rgb[1], rgb[2], 1).endVertex();
        buf.pos(box[2]+d, box[3], -.01).tex(1, 1).color(rgb[0], rgb[1], rgb[2], 1).endVertex();
        buf.pos(box[2]+d, box[1]-d, -.01).tex(1, 0).color(rgb[0], rgb[1], rgb[2], 1).endVertex();
        rgb = new float[]{.3F, .3F, .3F};
        d = .05F;
        buf.pos(box[0]-d, box[1]-d, 0).tex(0, 0).color(rgb[0], rgb[1], rgb[2], 1).endVertex();
        buf.pos(box[0]-d, box[3]+d, 0).tex(0, 1).color(rgb[0], rgb[1], rgb[2], 1).endVertex();
        buf.pos(box[2]+d, box[3]+d, 0).tex(1, 1).color(rgb[0], rgb[1], rgb[2], 1).endVertex();
        buf.pos(box[2]+d, box[1]-d, 0).tex(1, 0).color(rgb[0], rgb[1], rgb[2], 1).endVertex();
        tes.draw();
        GlStateManager.enableTexture2D();
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
        return new DisplayText(name, panel);
    }

    @Override
    public void populateDefaultNodes(List<ControlEvent> receiveEvents) {

    }

}
