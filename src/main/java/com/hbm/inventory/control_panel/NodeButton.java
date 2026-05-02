package com.hbm.inventory.control_panel;

import com.hbm.inventory.control_panel.nodes.Node;
import com.hbm.render.NTMRenderHelper;
import com.hbm.render.util.NTMImmediate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class NodeButton extends NodeElement {

    public NodeButton(String name, Node parent, int idx) {
        super(parent, idx);
        this.name = name;
        resetOffset();
    }

    @Override
    public void render(float mX, float mY){
        Minecraft.getMinecraft().getTextureManager().bindTexture(NodeSystem.node_tex);
        NTMImmediate.INSTANCE.beginPositionTexColorQuads(1);
        float x = offsetX+4;
        float y = offsetY+8;
        NTMRenderHelper.drawGuiRectBatchedColor(x, y, 0F, 0.890625F, 32, 6, 0.609375F, 0.984375F, 1, 1, 1, 1);
        NTMImmediate.INSTANCE.draw();

        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GL11.glScaled(0.35, 0.35, 0.35);
        GlStateManager.translate(-x, -y, 0);
        font.drawString(name, x+43-font.getStringWidth(name)/2F, y+5, 0xFF5F5F5F, false);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean onClick(float x, float y) {
        if (NTMRenderHelper.intersects2DBox(x, y, getBox())) {
            return true;
        }
        return false;
    }

    public float[] getBox() {
        return new float[]{3 + offsetX, -3 + offsetY + 10, 37 + offsetX, 3 + offsetY + 10};
    }

}
