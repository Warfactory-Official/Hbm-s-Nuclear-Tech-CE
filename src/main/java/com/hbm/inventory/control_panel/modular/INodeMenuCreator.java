package com.hbm.inventory.control_panel.modular;

import com.hbm.inventory.control_panel.ItemList;
import com.hbm.inventory.control_panel.SubElementNodeEditor;
import com.hbm.inventory.control_panel.nodes.Node;

public interface INodeMenuCreator {
	Node selectItem(String s2,float x,float y,SubElementNodeEditor editor);
	void addItems(ItemList list,float x,float y,SubElementNodeEditor editor);
}
