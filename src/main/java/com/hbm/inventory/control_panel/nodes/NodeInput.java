package com.hbm.inventory.control_panel.nodes;

import com.hbm.inventory.control_panel.DataValue;
import com.hbm.inventory.control_panel.NodeConnection;
import com.hbm.inventory.control_panel.NodeSystem;
import com.hbm.inventory.control_panel.modular.StockNodesRegister;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class NodeInput extends Node {

	public String name;
	
	public NodeInput(float x, float y, String name){
		super(x, y);
		this.name = name;
	}

	@Override
	public DataValue evaluate(int idx){
		return outputs.get(idx).defaultValue;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag, NodeSystem sys){
		tag.setString("nodeType", "input");
		tag.setString("name", name);
		return super.writeToNBT(tag, sys);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag, NodeSystem sys){
		this.name = tag.getString(name);
		super.readFromNBT(tag, sys);
	}

	@Override
	public float[] getColor() {
		return StockNodesRegister.colorInput;
	}
	
	@Override
	public String getDisplayName(){
		return name;
	}
	
	public NodeInput setVars(Map<String, DataValue> vars){
		outputs.clear();
		for(Entry<String, DataValue> e : vars.entrySet()){
			NodeConnection c = new NodeConnection(e.getKey(), this, outputs.size(), false, e.getValue().getType(), e.getValue());
			outputs.add(c);
		}
		this.recalcSize();
		return this;
	}
	
	public NodeInput setOutputFromVars(Map<String, DataValue> vars){
		for(NodeConnection c : outputs){
			if (Objects.equals(c.name, "from index")) {
				c.enumSelector = null;
				continue;
			}
			c.setDefault(vars.get(c.name));
		}
		return this;
	}
}
