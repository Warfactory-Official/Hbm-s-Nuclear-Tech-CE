package com.hbm.inventory.control_panel.controls.configs;

import com.hbm.inventory.control_panel.types.DataValue;
import com.hbm.inventory.control_panel.GuiControlEdit;
import com.hbm.main.ResourceManager;
import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

public class SubElementDisplaySevenSeg extends SubElementBaseConfig {

    private static final int[] TRANSFORM = {132, 112, 88, 88};
    private float colorR;
    private float colorG;
    private float colorB;
    private int digitCount;
    private boolean isDecimal;

    GuiSlider slide_colorR;
    GuiSlider slide_colorG;
    GuiSlider slide_colorB;
    GuiSlider slide_digitCount;
    GuiCheckBox check_isDecimal;

    public SubElementDisplaySevenSeg(GuiControlEdit gui, Map<String, DataValue> map) {
        super(gui,map);
        this.colorR = map.get("colorR").getNumber();
        this.colorG = map.get("colorG").getNumber();
        this.colorB = map.get("colorB").getNumber();
        this.digitCount = (int) map.get("digitCount").getNumber();
        this.isDecimal = map.get("isDecimal").getBoolean();
    }

    @Override
    public void fillConfigs(Map<String, DataValue> configs) {
        putFloatConfig(configs, "colorR", colorR);
        putFloatConfig(configs, "colorG", colorG);
        putFloatConfig(configs, "colorB", colorB);
        putFloatConfig(configs, "digitCount", digitCount);
        putFloatConfig(configs, "isDecimal", isDecimal ? 1F : 0F);
    }

    @Override
    public void initGui() {
        int cX = gui.width/2;
        int cY = gui.height/2;

        slide_colorR = gui.addButton(new GuiSlider(gui.currentButtonId(), cX-85, gui.getGuiTop()+70, 80, 15, TextFormatting.RED+"R ", "", 0, 100, colorR*100, false, true));
        slide_colorG = gui.addButton(new GuiSlider(gui.currentButtonId(), cX-85, gui.getGuiTop()+90, 80, 15, TextFormatting.GREEN+"G ", "", 0, 100, colorG*100, false, true));
        slide_colorB = gui.addButton(new GuiSlider(gui.currentButtonId(), cX-85, gui.getGuiTop()+110, 80, 15, TextFormatting.BLUE+"B ", "", 0, 100, colorB*100, false, true));

        slide_digitCount = gui.addButton(new GuiSlider(gui.currentButtonId(), cX+10, gui.getGuiTop()+70, 75, 15, "Digits ", "", 0, 8, digitCount, false, true));
        check_isDecimal = gui.addButton(new GuiCheckBox(gui.currentButtonId(), cX+10, gui.getGuiTop()+92, "Decimal", isDecimal));

        super.initGui();
    }

    @Override
    public void drawScreen() {
        GlStateManager.disableLighting();
        gui.mc.getTextureManager().bindTexture(ResourceManager.white);
        NTMRenderHelper.drawGuiRectColor(gui.getGuiLeft()+20, gui.getGuiTop()+70, 0, 0, 15, 15, 1, 1, colorR, colorG, colorB, 1F);
        GlStateManager.enableLighting();
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        colorR = (float) slide_colorR.sliderValue;
        colorG = (float) slide_colorG.sliderValue;
        colorB = (float) slide_colorB.sliderValue;
        digitCount = slide_digitCount.getValueInt();
        isDecimal = check_isDecimal.isChecked();
    }

    @Override
    public void enableButtons(boolean enable) {
        slide_colorR.visible = enable;
        slide_colorR.enabled = enable;
        slide_colorG.visible = enable;
        slide_colorG.enabled = enable;
        slide_colorB.visible = enable;
        slide_colorB.enabled = enable;
        slide_digitCount.visible = enable;
        slide_digitCount.enabled = enable;
        check_isDecimal.visible = enable;
        check_isDecimal.enabled = enable;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int[] getPreviewTransform() {
        return TRANSFORM;
    }
}
