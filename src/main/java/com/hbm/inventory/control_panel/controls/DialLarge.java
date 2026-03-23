package com.hbm.inventory.control_panel.controls;

import com.hbm.inventory.control_panel.controls.configs.SubElementBaseConfig;
import com.hbm.inventory.control_panel.controls.configs.SubElementDialSquare;
import com.hbm.render.loader.WaveFrontObjectVAO;
import com.hbm.inventory.control_panel.*;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Map;

public class DialLarge extends Control {

    private String label = "    POWER            (RS)";

    public DialLarge(String name,String registryName,ControlPanel panel) {
        super(name,registryName, panel);
        vars.put("value", new DataValueFloat(0));
        configMap.put("label", new DataValueString(label));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubElementBaseConfig getConfigSubElement(GuiControlEdit gui,Map<String,DataValue> configs) {
        return new SubElementDialSquare(gui,configs);
    }

    @Override
    public ControlType getControlType() {
        return ControlType.DIAL;
    }

    @Override
    public float[] getSize() {
        return new float[] {4.5F, 2.25F, .4F};
    }

    @Override
    protected void onConfigMapChanged() {
        for (Map.Entry<String, DataValue> e : configMap.entrySet()) {
            switch (e.getKey()) {
                case "label": {
                    label = e.getValue().toString();
                    break;
                }
            }
        }
    }

    @Override
    public void render() {
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.ctrl_dial_large_tex);

        WaveFrontObjectVAO model = (WaveFrontObjectVAO) getModel();
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;

        int value = (int) getVar("value").getNumber();

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, 0.0, posY);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        model.renderPart("base");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, 0.0F, posY + 0.77F);
        GlStateManager.rotate(
                (float) -MathHelper.clamp(value * (180F / 100F), 0F, 180F),
                0F, 1F, 0F
        );
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        model.renderPart("dial");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, 0.07F, posY);
        GlStateManager.scale(0.023F, 0.023F, 0.023F);
        GlStateManager.rotate(90F, 1F, 0F, 0F);
        GL11.glNormal3f(0F, 0F, -1F);

        for (int i = 0; i < 11; i++) {
            double angle = (Math.PI * 1.1) / 11F * i;
            float r = 68F;

            double x = r * Math.cos(angle - Math.PI) + i;
            double y = r * Math.sin(angle - Math.PI);

            String labelText = Integer.toString(i * 10);
            float textOffset = (i == 10) ? 14F : 10F;

            font.drawString(labelText, (float) (x - textOffset), (float) (29.5F + y), 0x303030, false);
        }

        font.drawSplitString(label, -31, -10, 70, 0x303030);
        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.color(1F, 1F, 1F, 1F);
    }


    @Override
    public IModelCustom getModel() {
        return ResourceManager.ctrl_dial_large;
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return ResourceManager.ctrl_dial_large_gui_tex;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderControl(float[] renderBox,Control selectedControl,GuiControlEdit gui) {
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buf = tes.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        float red = 1F;
        float green = (this == selectedControl) ? .8F : 1F;
        float blue = 1F;
        buf.pos(renderBox[0], renderBox[1], 0).tex(0, 0).color(red, green, blue, 1).endVertex();
        buf.pos(renderBox[0], renderBox[3], 0).tex(0, .5).color(red, green, blue, 1).endVertex();
        buf.pos(renderBox[2], renderBox[3], 0).tex(1, .5).color(red, green, blue, 1).endVertex();
        buf.pos(renderBox[2], renderBox[1], 0).tex(1, 0).color(red, green, blue, 1).endVertex();
        tes.draw();
    }

    @Override
    public Control newControl(ControlPanel panel) {
        return new DialLarge(name,registryName,panel);
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return null;
    }

    @Override
    public void populateDefaultNodes(List<ControlEvent> receiveEvents) {

    }
}
