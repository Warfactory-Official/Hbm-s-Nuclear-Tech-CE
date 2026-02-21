package com.hbm.inventory.control_panel.modular.categories;

import com.hbm.inventory.control_panel.DataValue;
import com.hbm.inventory.control_panel.DataValueFloat;
import com.hbm.inventory.control_panel.ItemList;
import com.hbm.inventory.control_panel.SubElementNodeEditor;
import com.hbm.inventory.control_panel.modular.INodeMenuCreator;
import com.hbm.inventory.control_panel.nodes.*;

import java.util.HashMap;
import java.util.Map;

public class NCStockInput implements INodeMenuCreator {
	@Override
	public Node selectItem(String s2,float x,float y,SubElementNodeEditor editor) {
		if(s2.equals("Event Data")){
			Map<String,DataValue> vars = new HashMap<>(editor.currentEvent.vars);
			vars.put(editor.sendEvents == null ? "to index" : "from index", new DataValueFloat(0));
			return new NodeInput(x, y, "Event Data").setVars(vars);
		} else if(s2.equals("Get Variable")){
			return new NodeGetVar(x, y, editor.gui.currentEditControl);
		} else if (s2.equals("Query Block")) {
			return new NodeQueryBlock(x, y, editor.gui.currentEditControl);
		}
		return null;
	}
	@Override
	public void addItems(ItemList list,float x,float y,SubElementNodeEditor editor) {
		list.addItems("Event Data");
		list.addItems("Get Variable");
		list.addItems("Query Block");
	}
}
