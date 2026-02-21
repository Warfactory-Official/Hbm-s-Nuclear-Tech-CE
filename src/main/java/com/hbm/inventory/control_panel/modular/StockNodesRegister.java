package com.hbm.inventory.control_panel.modular;

import com.hbm.inventory.control_panel.modular.categories.*;

import java.util.ArrayList;
import java.util.Collections;

public class StockNodesRegister {
	//Input for the system, such as the in variables for the event or a single value
	public static final float[] colorInput = new float[]{0, 1, 0};
	//Intermediate node for math. Has a dropdown menu for operations like add, multiply, sin, abs, etc.
	public static final float[] colorMath = new float[]{1, 1, 0};
	//for boolean functions: and, not, xor etc.
	public static final float[] colorBoolean = new float[]{1, 0, 1};
	//Intermediate nodes for logic such as for loops and if statements. Each contains child node systems, which possibly produce outputs.
	public static final float[] colorLogic = new float[]{.4F, .4F, 1};
	//The outputs that get evaluated, such as broadcasting a new event or setting an arbitrary variable
	public static final float[] colorOutput = new float[]{1, 0, 0};

	public static void register() {
		NTMControlPanelRegistry.addMenuCategories.add("Input");
		NTMControlPanelRegistry.addMenuCategories.add("Output");
		NTMControlPanelRegistry.addMenuCategories.add("Math");
		NTMControlPanelRegistry.addMenuCategories.add("Boolean");
		NTMControlPanelRegistry.addMenuCategories.add("Logic");
		NTMControlPanelRegistry.addMenuControl.put("Input",new ArrayList<>(Collections.singletonList(new NCStockInput())));
		NTMControlPanelRegistry.addMenuControl.put("Output",new ArrayList<>(Collections.singletonList(new NCStockOutput())));
		NTMControlPanelRegistry.addMenuControl.put("Math",new ArrayList<>(Collections.singletonList(new NCStockMath())));
		NTMControlPanelRegistry.addMenuControl.put("Boolean",new ArrayList<>(Collections.singletonList(new NCStockBoolean())));
		NTMControlPanelRegistry.addMenuControl.put("Logic",new ArrayList<>(Collections.singletonList(new NCStockLogic())));
		NTMControlPanelRegistry.nbtNodeLoaders.add(new StockNodeLoader());
	}
}
