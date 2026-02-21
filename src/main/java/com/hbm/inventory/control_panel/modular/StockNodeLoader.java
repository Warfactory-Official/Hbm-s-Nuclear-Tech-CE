package com.hbm.inventory.control_panel.modular;

import com.hbm.inventory.control_panel.ControlEvent;
import com.hbm.inventory.control_panel.NodeSystem;
import com.hbm.inventory.control_panel.nodes.*;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

public class StockNodeLoader implements INodeLoader {
	@Override
	public Node nodeFromNBT(NBTTagCompound tag,NodeSystem sys) {
		Node node = null;
		switch(tag.getString("nodeType")){
			case "cancelEvent":
				node = new NodeCancelEvent(0, 0);
				break;
			case "eventBroadcast":
				NBTTagCompound list = tag.getCompoundTag("itemList");
				List<ControlEvent> l = new ArrayList<>();
				for(int i = 0; i < list.getKeySet().size(); i ++){
					l.add(ControlEvent.getRegisteredEvent(list.getString("item"+i)));
				}
				node = new NodeEventBroadcast(0, 0, l);
				break;
			case "getVar":
				int ctrlIdx = tag.getInteger("controlIdx");
				node = new NodeGetVar(0, 0, sys.parent.panel.controls.get(ctrlIdx));
				break;
			case "queryBlock":
				ctrlIdx = tag.getInteger("controlIdx");
				node = new NodeQueryBlock(0, 0, sys.parent.panel.controls.get(ctrlIdx));
				break;
			case "math":
				node = new NodeMath(0, 0);
				break;
			case "boolean":
				node = new NodeBoolean(0, 0);
				break;
			case "function":
				node = new NodeFunction(0, 0);
				break;
			case "buffer":
				node = new NodeBuffer(0, 0);
				break;
			case "conditional":
				node = new NodeConditional(0, 0);
				break;
			case "setVar":
				int ctrlIdx2 = tag.getInteger("controlIdx");
				node = new NodeSetVar(0, 0, sys.parent.panel.controls.get(ctrlIdx2));
				break;
			case "input":
				node = new NodeInput(0, 0, null);
				break;
		}
		return node;
	}
}
