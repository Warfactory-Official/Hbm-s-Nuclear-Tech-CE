package com.hbm.inventory.control_panel.nodes;

import com.hbm.inventory.control_panel.NodeConnection;
import com.hbm.inventory.control_panel.NodeSystem;
import com.hbm.inventory.control_panel.types.DataValue;
import com.hbm.inventory.control_panel.types.DataValue.DataType;
import com.hbm.inventory.control_panel.types.DataValueComposite;
import com.hbm.inventory.control_panel.types.DataValueFloat;
import com.hbm.inventory.control_panel.types.DataValueString;
import net.minecraft.nbt.NBTTagCompound;

public class NodeCompositeWrite extends Node {
	public NodeCompositeWrite(float x,float y) {
		super(x,y);
		outputs.add(new NodeConnection("Modified Signal",this,outputs.size(),false,DataType.COMPOSITE,new DataValueComposite()));
		inputs.add(new NodeConnection("Enable",this,inputs.size(),true,DataType.NUMBER,new DataValueFloat(1)));
		inputs.add(new NodeConnection("Key",this,inputs.size(),true,DataType.STRING,new DataValueString("")));
		inputs.add(new NodeConnection("Value",this,inputs.size(),true,DataType.STRING,new DataValueString("")));
		inputs.add(new NodeConnection("Base Signal",this,inputs.size(),true,DataType.COMPOSITE,new DataValueComposite()));
		recalcSize();
		evalCache = new DataValue[1];
	}
	@Override
	public DataValue evaluate(int idx) {
		if (cacheValid)
			return evalCache[0];
		DataValueComposite signal = null;
		if (inputs.get(3).evaluate() instanceof DataValueComposite composite)
			signal = (DataValueComposite)composite.copy();
		if (signal == null)
			signal = new DataValueComposite();
		DataValue enable = inputs.get(0).evaluate();
		DataValue key = inputs.get(1).evaluate();
		DataValue value = inputs.get(2).evaluate();
		if (enable != null && key != null && value != null) {
			if (enable.getBoolean())
				signal.setValueOf(key.toString(),value.toString());
		}
		evalCache[0] = signal;
		cacheValid = true;
		return signal;
	}
	@Override
	public float[] getColor() {
		return DataType.COMPOSITE.getColor();
	}
	@Override
	public String getDisplayName() {
		return "Pack Write";
	}
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag,NodeSystem sys) {
		tag.setString("nodeType","composite_write");
		return super.writeToNBT(tag,sys);
	}
}
