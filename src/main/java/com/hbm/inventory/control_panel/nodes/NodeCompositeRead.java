package com.hbm.inventory.control_panel.nodes;

import com.hbm.inventory.control_panel.NodeConnection;
import com.hbm.inventory.control_panel.NodeSystem;
import com.hbm.inventory.control_panel.types.DataValue;
import com.hbm.inventory.control_panel.types.DataValue.DataType;
import com.hbm.inventory.control_panel.types.DataValueComposite;
import com.hbm.inventory.control_panel.types.DataValueString;
import net.minecraft.nbt.NBTTagCompound;

public class NodeCompositeRead extends Node {
	public NodeCompositeRead(float x,float y) {
		super(x,y);
		outputs.add(new NodeConnection("Value",this,outputs.size(),false,DataType.STRING,new DataValueString("")));
		inputs.add(new NodeConnection("Signal",this,inputs.size(),true,DataType.COMPOSITE,new DataValueComposite()));
		inputs.add(new NodeConnection("Key",this,inputs.size(),true,DataType.STRING,new DataValueString("")));
		recalcSize();
		evalCache = new DataValue[1];
	}
	@Override
	public DataValue evaluate(int idx) {
		if (cacheValid)
			return evalCache[0];
		DataValue signal = inputs.get(0).evaluate();
		DataValue key = inputs.get(1).evaluate();
		if (key != null && signal != null) {
			evalCache[0] = new DataValueString(signal.getValueOf(key.toString()));
		} else
			evalCache[0] = new DataValueString("ERROR");
		cacheValid = true;
		return evalCache[0];
	}
	@Override
	public float[] getColor() {
		return DataType.COMPOSITE.getColor();
	}
	@Override
	public String getDisplayName() {
		return "Pack Read";
	}
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag,NodeSystem sys) {
		tag.setString("nodeType","composite_read");
		return super.writeToNBT(tag,sys);
	}
}
