package com.hbm.inventory.control_panel.modular;

import java.util.*;

public class NTMControlPanelRegistry {
	public static List<String> addMenuCategories = new ArrayList<>();
	public static Map<String,List<INodeMenuCreator>> addMenuControl = new HashMap<>();
	public static List<INodeLoader> nbtNodeLoaders = new ArrayList<>();
}
