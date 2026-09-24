package com.hbm.inventory.control_panel.controls;

import java.util.ArrayList;
import java.util.List;

// ffs with the enums
public class ControlType {
    public final String name;
    public static final List<ControlType> ALL_VALUES = new ArrayList<>();
    /// Do not use this, use registerNew
    private ControlType(String name){
        this.name = name;
        ALL_VALUES.add(this);
    }

    public static ControlType registerOrGet(String name) {
        ControlType ret = getByName(name);
        if (ret == null)
            ret = new ControlType(name);
        return ret;
    }

    public static ControlType getByName(String name){
        for(ControlType o : ALL_VALUES){
            if(o.name.equals(name)){
                return o;
            }
        }
        return null;
    }

    public static final ControlType BUTTON = registerOrGet("Button");
    public static final ControlType SWITCH = registerOrGet("Switch");
    public static final ControlType SLIDER = registerOrGet("Slider");
    public static final ControlType KNOB = registerOrGet("Knob");
    public static final ControlType DIAL = registerOrGet("Dial");
    public static final ControlType DISPLAY = registerOrGet("Display");
    public static final ControlType INDICATOR = registerOrGet("Indicator");
    public static final ControlType METER = registerOrGet("Meter"); // unused
    public static final ControlType LABEL = registerOrGet("Label");
}
