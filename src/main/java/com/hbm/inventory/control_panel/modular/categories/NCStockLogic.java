package com.hbm.inventory.control_panel.modular.categories;

import com.hbm.inventory.control_panel.DataValue;
import com.hbm.inventory.control_panel.DataValueFloat;
import com.hbm.inventory.control_panel.ItemList;
import com.hbm.inventory.control_panel.SubElementNodeEditor;
import com.hbm.inventory.control_panel.modular.INodeMenuCreator;
import com.hbm.inventory.control_panel.nodes.*;

import java.util.HashMap;
import java.util.Map;

public class NCStockLogic implements INodeMenuCreator {
	@Override
	public Node selectItem(String s2,float x,float y,SubElementNodeEditor editor) {
		if (s2.equals("Function")) {
			return new NodeFunction(x, y);
		}
		else if (s2.equals("Buffer")) {
			return new NodeBuffer(x, y);
		}
		else if (s2.equals("Conditional")) {
			return new NodeConditional(x, y);
		}
		return null;
	}
	@Override
	public void addItems(ItemList list,float x,float y,SubElementNodeEditor editor) {
		list.addItems("Function");
		list.addItems("Buffer");
		list.addItems("Conditional");
	}
}
