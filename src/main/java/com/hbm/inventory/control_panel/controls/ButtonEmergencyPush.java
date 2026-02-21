package com.hbm.inventory.control_panel.controls;

import com.hbm.render.loader.WaveFrontObjectVAO;
import com.hbm.inventory.control_panel.*;
import com.hbm.inventory.control_panel.nodes.*;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.List;

public class ButtonEmergencyPush extends Control {

    public ButtonEmergencyPush(String name, ControlPanel panel) {
        super(name, panel);
        vars.put("isPushed", new DataValueFloat(0));
    }

    @Override
    public ControlType getControlType() {
        return ControlType.BUTTON;
    }

    @Override
    public float[] getSize() {
        return new float[]{1.5F, 1.5F, 1.13F};
    }

    @Override
    public void render() {
        boolean isPushed = getVar("isPushed").getBoolean();

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.ctrl_button_emergency_push_tex);

        WaveFrontObjectVAO model = (WaveFrontObjectVAO) getModel();

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, 0.0F, posY);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        model.renderPart("base");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, isPushed ? -0.125F : 0.0F, posY);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        model.renderPart("top");
        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IModelCustom getModel() {
        return ResourceManager.ctrl_button_emergency_push;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ResourceLocation getGuiTexture() {
        return ResourceManager.ctrl_button_emergency_push_gui_tex;
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
            NodeSetVar node2 = new NodeSetVar(290, 140, this).setData("isPushed", false);
            node2.inputs.get(0).setData(node1, 0, true);
            ctrl_press.addNode(node2);
        }
        receiveNodeMap.put("ctrl_press", ctrl_press);
        NodeSystem tick = new NodeSystem(this);
        {
            NodeGetVar node0 = new NodeGetVar(170, 100, this).setData("isPushed", false);
            tick.addNode(node0);
            NodeBuffer node1 = new NodeBuffer(230, 120);
            node1.inputs.get(0).setData(node0, 0, true);
            node1.inputs.get(1).setDefault(new DataValueFloat(15));
            tick.addNode(node1);
            NodeFunction node2 = new NodeFunction(290, 130);
            NodeSystem node2_subsystem = new NodeSystem(this);
            {
                node2_subsystem.addNode(new NodeSetVar(290, 90, this).setData("isPushed", false));
            }
            node2.inputs.get(0).setData(node1, 0, true);
            tick.subSystems.put(node2, node2_subsystem);
            tick.addNode(node2);
        }
        receiveNodeMap.put("tick", tick);
    }

    @Override
    public Control newControl(ControlPanel panel) {
        return new ButtonEmergencyPush(name, panel);
    }

}
