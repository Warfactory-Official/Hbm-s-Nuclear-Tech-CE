package com.hbm.inventory.control_panel.controls;

import com.hbm.render.loader.WaveFrontObjectVAO;
import com.hbm.inventory.control_panel.*;
import com.hbm.inventory.control_panel.nodes.*;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.List;


public class ButtonPush extends Control {

    public ButtonPush(String name, ControlPanel panel) {
        super(name, panel);
        vars.put("isPushed", new DataValueFloat(0));
        vars.put("isLit", new DataValueFloat(0));
        vars.put("color", new DataValueEnum<>(EnumDyeColor.RED));
    }

    @Override
    public ControlType getControlType() {
        return ControlType.BUTTON;
    }

    @Override
    public float[] getSize() {
        return new float[] {1, 1, .4F};
    }

    @Override
    public void render() {
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.ctrl_button_push_tex);
        WaveFrontObjectVAO model = (WaveFrontObjectVAO) getModel();
        boolean isPushed = getVar("isPushed").getBoolean();
        boolean isLit = getVar("isLit").getBoolean();
        float[] color = getVar("color").getEnum(EnumDyeColor.class).getColorComponentValues();
        float lX = OpenGlHelper.lastBrightnessX;
        float lY = OpenGlHelper.lastBrightnessY;

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, 0.0, posY);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        model.renderPart("base");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, 0.0, posY);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        model.renderPart("btn_base");
        GlStateManager.popMatrix();

        if (isLit) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        }

        float cMul = isLit ? 1.0F : 0.6F;

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, isPushed ? -0.1F : 0.0F, posY);
        GlStateManager.color(color[0] * cMul, color[1] * cMul, color[2] * cMul, 1.0F);
        model.renderPart("btn_top");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, isPushed ? -0.1F : 0.0F, posY);
        GlStateManager.color(color[0] * cMul, color[1] * cMul, color[2] * cMul, 1.0F);
        model.renderPart("btn_top_top");
        GlStateManager.popMatrix();

        if (isLit) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lX, lY);
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IModelCustom getModel() {
        return ResourceManager.ctrl_button_push;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ResourceLocation getGuiTexture() {
        return ResourceManager.ctrl_button_push_gui_tex;
    }

    @Override
    public List<String> getOutEvents() {
        return Collections.singletonList("ctrl_press");
    }

    @Override
    public void populateDefaultNodes(List<ControlEvent> receiveEvents) {
        NodeSystem ctrl_press = new NodeSystem(this);
        {
            NodeGetVar node0 = new NodeGetVar(170, 100, this).setData("isPushed", false);
            ctrl_press.addNode(node0);
            NodeBoolean node1 = new NodeBoolean(230, 120).setData(NodeBoolean.BoolOperation.NOT);
            node1.inputs.get(0).setData(node0, 0, true);
            ctrl_press.addNode(node1);
            NodeSetVar node2 = new NodeSetVar(290, 140, this).setData("isLit", false);
            node2.inputs.get(0).setData(node1, 0, true);
            ctrl_press.addNode(node2);
            NodeSetVar node3 = new NodeSetVar(290, 90, this).setData("isPushed", false);
            node3.inputs.get(0).setData(node1, 0, true);
            ctrl_press.addNode(node3);
        }
        receiveNodeMap.put("ctrl_press", ctrl_press);
        NodeSystem tick = new NodeSystem(this);
        {
            NodeGetVar node0 = new NodeGetVar(170, 100, this).setData("isPushed", false);
            tick.addNode(node0);
            NodeBuffer node1 = new NodeBuffer(230, 120);
            node1.inputs.get(0).setData(node0, 0, true);
            node1.inputs.get(1).setDefault(new DataValueFloat(10));
            tick.addNode(node1);
            NodeFunction node2 = new NodeFunction(290, 130);
            NodeSystem node2_subsystem = new NodeSystem(this);
            {
                node2_subsystem.addNode(new NodeSetVar(290, 90, this).setData("isPushed", false));
                node2_subsystem.addNode(new NodeSetVar(290, 140, this).setData("isLit", false));
            }
            node2.inputs.get(0).setData(node1, 0, true);
            tick.subSystems.put(node2, node2_subsystem);
            tick.addNode(node2);
        }
        receiveNodeMap.put("tick", tick);
    }

    @Override
    public Control newControl(ControlPanel panel) {
        return new ButtonPush(name, panel);
    }
}
