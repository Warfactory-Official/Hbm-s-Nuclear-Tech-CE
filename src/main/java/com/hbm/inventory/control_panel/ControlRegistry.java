package com.hbm.inventory.control_panel;

import com.hbm.inventory.control_panel.controls.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ControlRegistry {
	
	public static Map<String, Control> registry = new Object2ObjectLinkedOpenHashMap<>();

	public static List<Control> addonControls = new ArrayList<>();

	private ControlRegistry(){
	}
	
	public static void init(){
		registry.put("button_push", new ButtonPush("Push Button","button_push",null));
		registry.put("button_emergency_push",new ButtonEmergencyPush("Emergency Push Button","button_emergency_push",null));
		registry.put("button_encased_push",new ButtonEncasedPush("Encased Push Button","button_encased_push",null));

		registry.put("switch_toggle",new SwitchToggle("Toggle Switch","switch_toggle",null));
		registry.put("switch_rotary_toggle",new SwitchRotaryToggle("Rotary Toggle Switch","switch_rotary_toggle",null));

		registry.put("slider_vertical",new SliderVertical("Vertical Slider","slider_vertical",null));

		registry.put("knob_control",new KnobControl("Control Knob","knob_control",null));

		registry.put("display_7seg",new DisplaySevenSeg("7-seg Display","display_7seg",null));
		registry.put("display_text",new DisplayText("Text Display","display_text",null));

		registry.put("dial_square",new DialSquare("Square Dial","dial_square",null));
		registry.put("dial_large",new DialLarge("Large Dial","dial_large",null));

		registry.put("indicator_lamp",new IndicatorLamp("Indicator Lamp","indicator_lamp",null));

		registry.put("indicator_lamp_rgb",new IndicatorLampRGB("Indicator Lamp (RGB)","indicator_lamp_rgb",null));

		registry.put("label",new Label("Label","label",null));

		for (Control control : addonControls)
			registry.put(control.registryName,control);
	}
	
	public static List<Control> getAllControls(){
		List<Control> l = new ArrayList<>(registry.size());
		for(Control c : registry.values())
			l.add(c);
		return l;
	}

	public static List<String> getAllControlsOfType(ControlType type) {
		List<String> l = new ArrayList<>();
		for (Entry<String, Control> c : registry.entrySet())
			if (c.getValue().getControlType() == type) {
				l.add(c.getKey());
			}
		return l;
	}

	public static Control getNew(String name, ControlPanel panel){
		Control control = registry.get(name);
		return control != null ? control.newControl(panel) : null;
	}

	public static boolean isRegistered(String name) {
		return registry.containsKey(name);
	}
}
