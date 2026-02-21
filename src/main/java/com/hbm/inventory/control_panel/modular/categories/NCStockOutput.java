package com.hbm.inventory.control_panel.modular.categories;

import com.hbm.inventory.control_panel.DataValue;
import com.hbm.inventory.control_panel.DataValueFloat;
import com.hbm.inventory.control_panel.ItemList;
import com.hbm.inventory.control_panel.SubElementNodeEditor;
import com.hbm.inventory.control_panel.modular.INodeMenuCreator;
import com.hbm.inventory.control_panel.nodes.*;

import java.util.HashMap;
import java.util.Map;

public class NCStockOutput implements INodeMenuCreator {
	@Override
	public Node selectItem(String s2,float x,float y,SubElementNodeEditor editor) {
		if(s2.equals("Broadcast")){
			return new NodeEventBroadcast(x, y, editor.sendEvents);
		} else if(s2.equals("Cancel")){
			return new NodeCancelEvent(x, y);
		} else if(s2.equals("Set Variable")){
			return new NodeSetVar(x, y, editor.gui.currentEditControl);
		}
		return null;
	}
	@Override
	public void addItems(ItemList list,float x,float y,SubElementNodeEditor editor) {
		if(editor.sendEvents != null){
			if(!editor.sendEvents.isEmpty())
				list.addItems("Broadcast");
		} else {
			list.addItems("Cancel");
		}
		list.addItems("Set Variable");
	}
}
