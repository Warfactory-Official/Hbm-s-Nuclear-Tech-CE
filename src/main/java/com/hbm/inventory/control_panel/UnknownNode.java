package com.hbm.inventory.control_panel;

import com.hbm.inventory.control_panel.nodes.Node;
import com.hbm.inventory.control_panel.types.DataValue;
import com.hbm.inventory.control_panel.types.DataValueComposite;
import com.hbm.inventory.control_panel.types.DataValueFloat;
import com.hbm.inventory.control_panel.types.DataValueString;
import net.minecraft.nbt.NBTTagCompound;

public class UnknownNode extends Node {
	NBTTagCompound tagBase;
	public UnknownNode(float x,float y,NBTTagCompound tag) {
		super(x,y);
		tagBase = tag;
		NBTTagCompound inputs = tag.getCompoundTag("in");
		for(int i = 0; i < inputs.getKeySet().size(); i ++){
			NodeConnection c = new NodeConnection(null, this, 0, false, null, new DataValueFloat(0));
			c.buildFromNBT(inputs.getCompoundTag("con"+i));
			this.inputs.add(c);
		}
		NBTTagCompound outputs = tag.getCompoundTag("out");
		for(int i = 0; i < outputs.getKeySet().size(); i ++){
			NodeConnection c = new NodeConnection(null, this, 0, false, null, new DataValueFloat(0));
			c.buildFromNBT(outputs.getCompoundTag("con"+i));
			this.outputs.add(c);
		}
		recalcSize();
	}
	@Override
	public DataValue evaluate(int idx) {
		return switch(outputs.get(idx).type) {
			case ENUM -> throw new UnsupportedOperationException("Control panel contains unresolved node type!");
			case COMPOSITE -> new DataValueComposite();
			case NUMBER -> new DataValueFloat(Float.NaN);
			case STRING,GENERIC -> new DataValueString("ERROR");
			default -> null;
		};
	}
	@Override
	public float[] getColor() {
		return new float[]{1,1,1};
	}
	@Override
	public String getDisplayName() {
		return "Unknown";
	}
	@Override
	public void readFromNBT(NBTTagCompound tag,NodeSystem sys) {
		super.readFromNBT(tag,sys);
	}
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag,NodeSystem sys) {
		for (String s : tagBase.getKeySet())
			tag.setTag(s,tagBase.getTag(s));
		return super.writeToNBT(tag,sys);
	}
}
