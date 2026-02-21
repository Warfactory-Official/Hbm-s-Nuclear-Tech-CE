package com.hbm.inventory.control_panel.controls;

import com.hbm.render.loader.WaveFrontObjectVAO;
import com.hbm.inventory.control_panel.*;
import com.hbm.inventory.control_panel.nodes.NodeBoolean;
import com.hbm.inventory.control_panel.nodes.NodeGetVar;
import com.hbm.inventory.control_panel.nodes.NodeSetVar;
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

public class SwitchRotaryToggle extends Control {

    public SwitchRotaryToggle(String name, ControlPanel panel) {
        super(name, panel);
        vars.put("isOn", new DataValueFloat(0));
    }

    @Override
    public ControlType getControlType() {
        return ControlType.SWITCH;
    }

    @Override
    public float[] getSize() {
        return new float[] {1, 1, .68F};
    }

    @Override
    public void render() {
        boolean isFlipped = getVar("isOn").getBoolean();

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.ctrl_switch_rotary_toggle_tex);

        WaveFrontObjectVAO model = (WaveFrontObjectVAO) getModel(); // VAO model

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, 0F, posY);
        GlStateManager.color(1F, 1F, 1F, 1F);
        model.renderPart("base");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, 0F, posY);

        if (isFlipped) {
            GlStateManager.rotate(-90F, 0F, 1F, 0F);
        }

        GlStateManager.color(1F, 1F, 1F, 1F);
        model.renderPart("lever");
        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IModelCustom getModel() {
        return ResourceManager.ctrl_switch_rotary_toggle;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ResourceLocation getGuiTexture() {
        return ResourceManager.ctrl_switch_rotary_toggle_gui_tex;
    }

    @Override
    public List<String> getOutEvents() {
        return Collections.singletonList("ctrl_press");
    }

    @Override
    public void populateDefaultNodes(List<ControlEvent> receiveEvents) {
        NodeSystem ctrl_press = new NodeSystem(this);
        {
            NodeGetVar node0 = new NodeGetVar(170, 100, this).setData("isOn", false);
            ctrl_press.addNode(node0);
            NodeBoolean node1 = new NodeBoolean(230, 120).setData(NodeBoolean.BoolOperation.NOT);
            node1.inputs.get(0).setData(node0, 0, true);
            ctrl_press.addNode(node1);
            NodeSetVar node2 = new NodeSetVar(290, 140, this).setData("isOn", false);
            node2.inputs.get(0).setData(node1, 0, true);
            ctrl_press.addNode(node2);
        }
        receiveNodeMap.put("ctrl_press", ctrl_press);
    }

    @Override
    public Control newControl(ControlPanel panel) {
        return new SwitchRotaryToggle(name, panel);
    }
}
