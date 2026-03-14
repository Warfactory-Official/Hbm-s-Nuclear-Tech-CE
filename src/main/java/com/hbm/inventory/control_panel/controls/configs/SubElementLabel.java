package com.hbm.inventory.control_panel.controls.configs;

import com.hbm.inventory.control_panel.DataValue;
import com.hbm.inventory.control_panel.DataValueFloat;
import com.hbm.inventory.control_panel.DataValueString;
import com.hbm.inventory.control_panel.GuiControlEdit;
import com.hbm.main.ResourceManager;
import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class SubElementLabel extends SubElementBaseConfig {

    private static final int[] TRANSFORM = {137, 116, 8, 88};
    private float colorR;
    private float colorG;
    private float colorB;
    private String text;
    private int scale;

    GuiSlider slideColorR;
    GuiSlider slideColorG;
    GuiSlider slideColorB;
    GuiTextField textField;
    GuiSlider slideScale;

    public SubElementLabel(GuiControlEdit gui, Map<String, DataValue> map) {
        super(gui,map);
        this.colorR = map.get("colorR").getNumber();
        this.colorG = map.get("colorG").getNumber();
        this.colorB = map.get("colorB").getNumber();
        this.text = map.get("text").toString();
        this.scale = (int) map.get("scale").getNumber();
    }

    @Override
    public void fillConfigs(Map<String, DataValue> configs) {
        putFloatConfig(configs, "colorR", colorR);
        putFloatConfig(configs, "colorG", colorG);
        putFloatConfig(configs, "colorB", colorB);
        putStringConfig(configs, "text", text);
        putFloatConfig(configs, "scale", scale);
    }

    @Override
    public void initGui() {
        int cX = gui.width/2;
        int cY = gui.height/2;

        slideColorR = gui.addButton(new GuiSlider(gui.currentButtonId(), cX-85, gui.getGuiTop()+70, 80, 15, TextFormatting.RED+"R ", "", 0, 100, colorR*100, false, true));
        slideColorG = gui.addButton(new GuiSlider(gui.currentButtonId(), cX-85, gui.getGuiTop()+90, 80, 15, TextFormatting.GREEN+"G ", "", 0, 100, colorG*100, false, true));
        slideColorB = gui.addButton(new GuiSlider(gui.currentButtonId(), cX-85, gui.getGuiTop()+110, 80, 15, TextFormatting.BLUE+"B ", "", 0, 100, colorB*100, false, true));
        slideScale = gui.addButton(new GuiSlider(gui.currentButtonId(), cX-85, gui.getGuiTop()+140, 80, 15, "Scale ", "", 10, 100, scale, false, true));
        textField = new GuiTextField(gui.currentButtonId(), Minecraft.getMinecraft().fontRenderer, cX+10, gui.getGuiTop()+70, 100, 30);
        textField.setText(text);

        super.initGui();
    }

    @Override
    protected void update() {
        super.update();
        textField.updateCursorCounter();
        text = textField.getText();
    }

    @Override
    protected void drawScreen() {
        GlStateManager.disableLighting();
        gui.mc.getTextureManager().bindTexture(ResourceManager.white);
        NTMRenderHelper.drawGuiRectColor(gui.getGuiLeft()+20, gui.getGuiTop()+70, 0, 0, 15, 15, 1, 1, colorR, colorG, colorB, 1F);
        GlStateManager.enableLighting();

        textField.drawTextBox();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        this.textField.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        this.textField.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        colorR = (float) slideColorR.sliderValue;
        colorG = (float) slideColorG.sliderValue;
        colorB = (float) slideColorB.sliderValue;
        scale = slideScale.getValueInt();
    }

    @Override
    public void enableButtons(boolean enable) {
        slideColorR.visible = enable;
        slideColorR.enabled = enable;
        slideColorG.visible = enable;
        slideColorG.enabled = enable;
        slideColorB.visible = enable;
        slideColorB.enabled = enable;
        textField.setEnabled(enable);
        textField.setVisible(enable);
        slideScale.visible = enable;
        slideScale.enabled = enable;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int[] getPreviewTransform() {
        return TRANSFORM;
    }
}
