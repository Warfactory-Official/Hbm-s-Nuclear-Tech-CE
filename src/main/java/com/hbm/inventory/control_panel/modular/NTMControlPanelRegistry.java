package com.hbm.inventory.control_panel.modular;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.*;

public class NTMControlPanelRegistry {
	public static List<String> addMenuCategories = new ArrayList<>();
	public static Map<String,List<INodeMenuCreator>> addMenuControl = new Object2ObjectOpenHashMap<>();
	public static List<INodeLoader> nbtNodeLoaders = new ArrayList<>();
}
