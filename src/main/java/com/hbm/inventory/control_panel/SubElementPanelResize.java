package com.hbm.inventory.control_panel;

import com.hbm.Tags;
import com.hbm.main.ResourceManager;
import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.lwjgl.opengl.GL11;

// placeholder gui until i make something better
public class SubElementPanelResize extends SubElement {

    public static ResourceLocation bg_tex = new ResourceLocation(Tags.MODID + ":textures/gui/control_panel/gui_base.png");
    public static ResourceLocation black_shit = new ResourceLocation(Tags.MODID + ":textures/gui/control_panel/gui_resize_grid.png");
    public static ResourceLocation smol_gridz = new ResourceLocation(Tags.MODID + ":textures/gui/control_panel/resize_grids.png");

    GuiButton back;
    GuiSlider a_offset;
    GuiSlider b_offset;
    GuiSlider c_offset;
    GuiSlider d_offset;
    GuiSlider panel_height;
    GuiSlider panel_angle;

    public SubElementPanelResize(GuiControlEdit gui){
        super(gui);
    }

    @Override
    protected void initGui(){
        int cX = gui.width/2;
        int cY = gui.height/2;

        back = gui.addButton(new GuiButton(gui.currentButtonId(), gui.getGuiLeft()+7, gui.getGuiTop()+13, 30, 20, "Back"));
        a_offset = gui.addButton(new GuiSlider(gui.currentButtonId(), cX+18, gui.getGuiTop()+47, 80, 15, "A ", "", 0, 100, gui.control.panel.a_off*100, false, true));
        b_offset = gui.addButton(new GuiSlider(gui.currentButtonId(), cX+18, gui.getGuiTop()+47+20, 80, 15, "B ", "", 0, 100, gui.control.panel.b_off*100, false, true));
        c_offset = gui.addButton(new GuiSlider(gui.currentButtonId(), cX+18, gui.getGuiTop()+47+40, 80, 15, "C ", "", 0, 100, gui.control.panel.c_off*100, false, true));
        d_offset = gui.addButton(new GuiSlider(gui.currentButtonId(), cX+18, gui.getGuiTop()+47+60, 80, 15, "D ", "", 0, 100, gui.control.panel.d_off*100, false, true));
        panel_height = gui.addButton(new GuiSlider(gui.currentButtonId(), cX+18, 194, 80, 15, "Height ", "", 0, 100, gui.control.panel.height*100, false, true));
        panel_angle = gui.addButton(new GuiSlider(gui.currentButtonId(), cX+18, 214, 80, 15, "Angle ", "", -45, 45, Math.toDegrees(gui.control.panel.angle), false, true));

        super.initGui();
    }

    @Override
    protected void drawScreen(){
        int cX = gui.width/2;
        int cY = gui.height/2;

        int minX = gui.getGuiLeft() + 38;
        int maxX = gui.getGuiLeft() + 111;
        int minY = gui.getGuiTop() + 56;
        int maxY = gui.getGuiTop() + 129;
        int minY_2 = gui.getGuiTop() + 146 - 1;
        int maxY_2 = gui.getGuiTop() + 219;

        int box_len = maxX-minX;
        int height0 = (int) (box_len*ControlPanel.getSlopeHeightFromZ(gui.control.panel.a_off, gui.control.panel.height, -gui.control.panel.angle)-1);
        int height1 = (int) (box_len*ControlPanel.getSlopeHeightFromZ(1-gui.control.panel.c_off, gui.control.panel.height, -gui.control.panel.angle)-1);

        GlStateManager.disableLighting();
        gui.mc.getTextureManager().bindTexture(ResourceManager.white);
        // top view
        NTMRenderHelper.drawGuiRectColor(minX+gui.control.panel.d_off*box_len, minY+gui.control.panel.a_off*box_len, 0, 0, (maxX-gui.control.panel.b_off*box_len)-(minX+gui.control.panel.d_off*box_len), 1, 1, 1, 1F, .2F, .2F, 1F);
        NTMRenderHelper.drawGuiRectColor(maxX-gui.control.panel.b_off*box_len-1, minY+gui.control.panel.a_off*box_len, 0, 0, 1, (maxY-gui.control.panel.c_off*box_len)-(minY+gui.control.panel.a_off*box_len), 1, 1, 1F, 1F, 1F, 1F);
        NTMRenderHelper.drawGuiRectColor(minX+gui.control.panel.d_off*box_len, maxY-gui.control.panel.c_off*box_len-1, 0, 0, (maxX-gui.control.panel.b_off*box_len)-(minX+gui.control.panel.d_off*box_len), 1, 1, 1, .2F, 1F, .2F, 1F);
        NTMRenderHelper.drawGuiRectColor(minX+gui.control.panel.d_off*box_len, minY+gui.control.panel.a_off*box_len, 0, 0, 1, (maxY-gui.control.panel.c_off*box_len)-(minY+gui.control.panel.a_off*box_len), 1, 1, 1F, 1F, 1F, 1F);
        // side view
        NTMRenderHelper.drawGuiRectColor(maxX-gui.control.panel.c_off*box_len-1, minY_2+(box_len-height1), 0, 0, 1, height1, 1, 1, .2F, 1F, .2F, 1F);
        NTMRenderHelper.drawGuiRectColor(minX+gui.control.panel.a_off*box_len, maxY_2-1, 0, 0, (maxX-gui.control.panel.c_off*box_len)-(minX+gui.control.panel.a_off*box_len), 1, 1, 1, 1, 1, 1, 1F);
        NTMRenderHelper.drawGuiRectColor(minX+gui.control.panel.a_off*box_len, minY_2+(box_len-height0), 0, 0, 1, height0, 1, 1, 1F, .2F, .2F, 1F);
        // side view slope
        GlStateManager.disableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.glLineWidth(3);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        Tessellator.getInstance().getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        BufferBuilder buf = Tessellator.getInstance().getBuffer();
        buf.pos(minX+gui.control.panel.a_off*box_len, minY_2+(box_len-height0), 0).endVertex();
        buf.pos(maxX-gui.control.panel.c_off*box_len, minY_2+(box_len-height1), 0).endVertex();
        Tessellator.getInstance().draw();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.glLineWidth(2);
        GlStateManager.enableTexture2D();

        GlStateManager.enableLighting();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == back) {
            gui.popElement();
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        float a_off = (float) a_offset.sliderValue;
        float b_off = (float) b_offset.sliderValue;
        float c_off = (float) c_offset.sliderValue;
        float d_off = (float) d_offset.sliderValue;
        float height = (float) panel_height.sliderValue;
        float angle = (float) Math.toRadians(panel_angle.getValueInt());

        float height0 = ControlPanel.getSlopeHeightFromZ(a_off, height, -angle);
        float height1 = ControlPanel.getSlopeHeightFromZ(1-c_off, height, -angle);

        // lazy
        if (((1-c_off) > a_off) && ((1-b_off) > d_off) && ((Math.max(height0, height1) <= 1)) && ((Math.min(height0, height1) >= -0.01))) {
            gui.control.panel.a_off = a_off;
            gui.control.panel.b_off = b_off;
            gui.control.panel.c_off = c_off;
            gui.control.panel.d_off = d_off;
            gui.control.panel.height = height;
            gui.control.panel.angle = angle;
        } else {
            a_offset.sliderValue = gui.control.panel.a_off;
            b_offset.sliderValue = gui.control.panel.b_off;
            c_offset.sliderValue = gui.control.panel.c_off;
            d_offset.sliderValue = gui.control.panel.d_off;
            panel_height.sliderValue = gui.control.panel.height;
            panel_angle.setValue(Math.toDegrees(gui.control.panel.angle));

            a_offset.updateSlider();
            b_offset.updateSlider();
            c_offset.updateSlider();
            d_offset.updateSlider();
            panel_height.updateSlider();
            panel_angle.updateSlider();
        }
    }

    @Override
    protected void renderBackground() {
        gui.mc.getTextureManager().bindTexture(bg_tex);
        gui.drawTexturedModalRect(gui.getGuiLeft(), gui.getGuiTop(), 0, 0, gui.getXSize(), gui.getYSize());
        gui.mc.getTextureManager().bindTexture(black_shit);
        gui.drawTexturedModalRect(gui.getGuiLeft(), gui.getGuiTop(), 0, 0, 120, gui.getYSize());
        gui.mc.getTextureManager().bindTexture(smol_gridz);
        gui.drawTexturedModalRect(gui.getGuiLeft(), gui.getGuiTop(), 0, 0, gui.getXSize(), gui.getYSize());
    }

    @Override
    protected void enableButtons(boolean enable){
        back.visible = enable;
        back.enabled = enable;
        a_offset.visible = enable;
        a_offset.enabled = enable;
        b_offset.visible = enable;
        b_offset.enabled = enable;
        c_offset.visible = enable;
        c_offset.enabled = enable;
        d_offset.visible = enable;
        d_offset.enabled = enable;
        panel_height.visible = enable;
        panel_height.enabled = enable;
        panel_angle.visible = enable;
        panel_angle.enabled = enable;
    }
}
