package com.hbm.inventory.control_panel.controls.configs;

import com.hbm.inventory.control_panel.DataValue;
import com.hbm.inventory.control_panel.DataValueFloat;
import com.hbm.inventory.control_panel.DataValueString;
import com.hbm.inventory.control_panel.GuiControlEdit;
import com.hbm.inventory.control_panel.SubElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class SubElementBaseConfig extends SubElement {
    protected static final int[] DEFAULT_PREVIEW_RECT = new int[]{84, 80, 88, 88};

    public SubElementBaseConfig(GuiControlEdit gui,Map<String, DataValue> map) {
        super(gui);
    }

    public Map<String, DataValue> getConfigs() {
        Map<String, DataValue> configs = new HashMap<>();
        fillConfigs(configs);
        return configs;
    }

    public void fillConfigs(Map<String, DataValue> configs) {
    }

    @SideOnly(Side.CLIENT)
    public int[] getPreviewTransform() {
        return DEFAULT_PREVIEW_RECT;
    }

    protected static void putFloatConfig(Map<String, DataValue> configs, String key, float value) {
        DataValue dataValue = configs.get(key);
        if (dataValue instanceof DataValueFloat) {
            ((DataValueFloat) dataValue).num = value;
        } else {
            configs.put(key, new DataValueFloat(value));
        }
    }

    protected static void putStringConfig(Map<String, DataValue> configs, String key, String value) {
        DataValue dataValue = configs.get(key);
        if (dataValue instanceof DataValueString) {
            ((DataValueString) dataValue).set(value);
        } else {
            configs.put(key, new DataValueString(value));
        }
    }
}
