package com.hbm.inventory.control_panel.nodes;

import com.hbm.inventory.control_panel.*;
import com.hbm.inventory.control_panel.DataValue.DataType;
import com.hbm.inventory.control_panel.modular.StockNodesRegister;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class NodeEventBroadcast extends NodeOutput {

	public String eventName;
	
	public NodeEventBroadcast(float x, float y, List<ControlEvent> ableToBroadcast){
		super(x, y);
		NodeDropdown opSelector = new NodeDropdown(this, otherElements.size(), s -> {
			setEvent(s);
			return null;
		}, () -> eventName);
		for(ControlEvent c : ableToBroadcast){
			opSelector.list.addItems(c.name);
		}
		this.otherElements.add(opSelector);
		setEvent(ableToBroadcast.get(0).name);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag, NodeSystem sys){
		tag.setString("nodeType", "eventBroadcast");
		tag.setString("eventName", eventName);
		NBTTagCompound list = new NBTTagCompound();
		int i = 0;
		for(String s : ((NodeDropdown)this.otherElements.get(0)).list.itemNames){
			list.setString("item"+i, s);
			i++;
		}
		tag.setTag("itemList", list);
		return super.writeToNBT(tag, sys);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag, NodeSystem sys){
		eventName = tag.getString("eventName");
		super.readFromNBT(tag, sys);
	}

	@Override
	public boolean doOutput(IControllable from, Map<String, NodeSystem> sendNodeMap, List<BlockPos> positions){
		World world = from.getControlWorld();
		ControlEvent e = ControlEvent.newEvent(eventName);
		for(NodeConnection c : inputs){
			if(c.name.startsWith("Cancel")){
				if(c.evaluate().getBoolean())
					return true;
				continue;
			}
			e.vars.put(c.name, c.evaluate());
		}
		if(sendNodeMap != null){
			if(sendNodeMap.containsKey(e.name)){
				NodeSystem sys = sendNodeMap.get(e.name);
				cont:
				for(int i = 0; i < positions.size(); i ++){
					sys.resetCachedValues();
					sys.setVar("receiver_id", new DataValueFloat(i));
					for(NodeOutput o : sys.outputNodes){
						//If canceled, don't continue;
						if(!o.doOutput(from, sendNodeMap, positions))
							continue cont;
					}
					ControlEventSystem.get(world).broadcastEvent(from.getControlPos(), e, positions.get(i));
				}
			} else {
				ControlEventSystem.get(world).broadcastEvent(from.getControlPos(), e, positions);
			}
		} else {
			ControlEventSystem.get(world).broadcastEvent(from.getControlPos(), e, positions);
		}
		return true;
	}
	
	public void setEvent(String name){
		eventName = name;
		for(NodeConnection c : inputs){
			c.removeConnection();
		}
		this.inputs.clear();
		ControlEvent evt = ControlEvent.getRegisteredEvent(name);
		for(Entry<String, DataValue> e : evt.vars.entrySet()){
			inputs.add(new NodeConnection(e.getKey(), this, inputs.size(), true, e.getValue().getType(), e.getValue()));
		}
		inputs.add(new NodeConnection("Cancel", this, inputs.size(), true, DataType.GENERIC, new DataValueFloat(0)));
		recalcSize();
	}

	@Override
	public float[] getColor() {
		return StockNodesRegister.colorOutput;
	}

	@Override
	public String getDisplayName(){
		return "Broadcast";
	}

}
