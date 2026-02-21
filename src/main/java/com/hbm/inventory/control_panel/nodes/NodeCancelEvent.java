package com.hbm.inventory.control_panel.nodes;

import com.hbm.inventory.control_panel.DataValue.DataType;
import com.hbm.inventory.control_panel.*;
import com.hbm.inventory.control_panel.modular.StockNodesRegister;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;

public class NodeCancelEvent extends NodeOutput {

	public NodeCancelEvent(float x, float y){
		super(x, y);
		this.inputs.add(new NodeConnection("do cancel", this, 0, true, DataType.NUMBER, new DataValueFloat(0)));
		recalcSize();
	}

	@Override
	public boolean doOutput(IControllable from, Map<String, NodeSystem> sendNodeMap, List<BlockPos> positions){
		return !inputs.get(0).evaluate().getBoolean();
	}

	@Override
	public float[] getColor() {
		return StockNodesRegister.colorOutput;
	}

	@Override
	public String getDisplayName(){
		return "Cancel Event";
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag, NodeSystem sys){
		tag.setString("nodeType", "cancelEvent");
		return super.writeToNBT(tag, sys);
	}

}
