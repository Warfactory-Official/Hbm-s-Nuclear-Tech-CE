package com.hbm.inventory.control_panel.controls.configs;

import com.hbm.inventory.control_panel.types.DataValue;
import com.hbm.inventory.control_panel.GuiControlEdit;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

public class SubElementKnobControl extends SubElementBaseConfig {

    private static final int[] TRANSFORM = {149, 64, 88, 88};
    private int positions;
    GuiSlider slidePositions;

    public SubElementKnobControl(GuiControlEdit gui, Map<String, DataValue> map) {
        super(gui,map);
        this.positions = (int) map.get("positions").getNumber();
    }

    @Override
    public void initGui() {
        int cX = gui.width/2;
        slidePositions = gui.addButton(new GuiSlider(gui.currentButtonId(), cX-85, gui.getGuiTop()+70, 80, 15, "Positions ", "", 1, 11, positions, false, true));
        super.initGui();
    }

    @Override
    public void fillConfigs(Map<String, DataValue> configs) {
        putFloatConfig(configs, "positions", positions);
    }

    @Override
    public void mouseReleased(int mX, int mY, int state) {
        positions = slidePositions.getValueInt();
    }

    @Override
    public void enableButtons(boolean enable) {
        slidePositions.visible = enable;
        slidePositions.enabled = enable;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int[] getPreviewTransform() {
        return TRANSFORM;
    }
}
