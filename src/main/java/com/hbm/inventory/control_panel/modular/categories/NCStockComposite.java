package com.hbm.inventory.control_panel.modular.categories;

import com.hbm.inventory.control_panel.ItemList;
import com.hbm.inventory.control_panel.SubElementNodeEditor;
import com.hbm.inventory.control_panel.modular.INodeMenuCreator;
import com.hbm.inventory.control_panel.nodes.Node;
import com.hbm.inventory.control_panel.nodes.NodeCompositeRead;
import com.hbm.inventory.control_panel.nodes.NodeCompositeWrite;

public class NCStockComposite implements INodeMenuCreator {
	@Override
	public Node selectItem(String s2,float x,float y,SubElementNodeEditor editor) {
		if(s2.equals("Pack Read")){
			return new NodeCompositeRead(x, y);
		} else if(s2.equals("Pack Write")){
			return new NodeCompositeWrite(x, y);
		}
		return null;
	}
	@Override
	public void addItems(ItemList list,float x,float y,SubElementNodeEditor editor) {
		list.addItems("Pack Read");
		list.addItems("Pack Write");
	}
}
