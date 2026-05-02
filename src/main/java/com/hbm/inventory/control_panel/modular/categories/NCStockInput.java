package com.hbm.inventory.control_panel.modular.categories;

import com.hbm.inventory.control_panel.types.DataValue;
import com.hbm.inventory.control_panel.types.DataValueFloat;
import com.hbm.inventory.control_panel.types.DataValueString;
import com.hbm.inventory.control_panel.ItemList;
import com.hbm.inventory.control_panel.SubElementNodeEditor;
import com.hbm.inventory.control_panel.modular.INodeMenuCreator;
import com.hbm.inventory.control_panel.nodes.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class NCStockInput implements INodeMenuCreator {
	@Override
	public Node selectItem(String s2,float x,float y,SubElementNodeEditor editor) {
		if(s2.equals("Event Data")){
			Map<String,DataValue> vars = new LinkedHashMap<>(editor.currentEvent.vars);
			if(editor.sendEvents == null) {
				vars.put("to index", new DataValueFloat(0));
			} else {
				vars.put("tag", new DataValueString(""));
				vars.put("from index", new DataValueFloat(0));
			}
			return new NodeInput(x, y, "Event Data").setVars(vars);
		} else if(s2.equals("Get Variable")){
			return new NodeGetVar(x, y, editor.currentSystem.parent);
		} else if (s2.equals("Query Block")) {
			return new NodeQueryBlock(x, y, editor.currentSystem.parent);
		} else if (s2.equals("Redstone Input")) {
			return new NodeRedstoneInput(x, y, editor.currentSystem.parent);
		}
		return null;
	}
	@Override
	public void addItems(ItemList list,float x,float y,SubElementNodeEditor editor) {
		list.addItems("Event Data");
		list.addItems("Get Variable");
		list.addItems("Query Block");
		list.addItems("Redstone Input");
	}
}
