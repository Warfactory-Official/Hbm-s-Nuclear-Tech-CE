package com.hbm.inventory.control_panel.nodes;

import com.hbm.inventory.control_panel.*;
import com.hbm.inventory.control_panel.DataValue.DataType;
import com.hbm.inventory.control_panel.modular.StockNodesRegister;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;

public class NodeSetVar extends NodeOutput {

	public Control ctrl;
	public boolean global = false;
	public NodeDropdown varSelector;
	public String varName;
	
	public NodeSetVar(float x, float y, Control ctrl){
		super(x, y);
		this.ctrl = ctrl;
		this.inputs.add(new NodeConnection("Input", this, 0, true, DataType.GENERIC, new DataValueFloat(0)));
		NodeDropdown globalSelector = new NodeDropdown(this, otherElements.size(), s -> {
			if(s.equals("Global") && !global){
				global = true;
				varName = "";
			}
			if(s.equals("Local") && global){
				global = false;
				varName = "";
			}
			setVarSelector();
			return null;
		}, () -> (global ? "Global" : "Local"));
		globalSelector.list.addItems("Global", "Local");
		this.otherElements.add(globalSelector);
		
		varSelector = new NodeDropdown(this, otherElements.size(), s -> {
			varName = s;
			DataValue val = global ? ctrl.getGlobalVar(s) : ctrl.getVar(s);
			this.inputs.get(0).type = val.getType();
			this.inputs.get(0).setDefault(val);
			return null;
		}, () -> varName);
		setVarSelector();
		this.otherElements.add(varSelector);
		
		varName = "";
		recalcSize();
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag, NodeSystem sys){
		tag.setString("nodeType", "setVar");
		tag.setInteger("controlIdx", sys.parent.panel.controls.indexOf(sys.parent));
		tag.setBoolean("global", global);
		tag.setString("varName", varName);
		return super.writeToNBT(tag, sys);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag, NodeSystem sys){
		global = tag.getBoolean("global");
		varName = tag.getString("varName");
		super.readFromNBT(tag, sys);
	}

	public void setVarSelector(){
		varSelector.list.itemNames.clear();
		if(global){
			for(String name : ctrl.panel.globalVars.keySet()){
				varSelector.list.addItems(name);
			}
		} else {
			for(String name : ctrl.vars.keySet()){
				varSelector.list.addItems(name);
			}
		}
	}
	
	@Override
	public boolean doOutput(IControllable from, Map<String, NodeSystem> sendNodeMap, List<BlockPos> positions){
		if(varName.isEmpty())
			return false;
		if(global){
			ctrl.panel.globalVars.put(varName, inputs.get(0).evaluate());
		} else {
			ctrl.vars.put(varName, inputs.get(0).evaluate());
		}
		return false;
	}

	public NodeSetVar setData(String varName, boolean isGlobal) {
		this.varName = varName;
		this.global = isGlobal;
		DataValue val = global ? ctrl.getGlobalVar(varName) : ctrl.getVar(varName);
		this.inputs.get(0).type = val.getType();
		this.inputs.get(0).setDefault(val);
		return this;
	}


	@Override
	public float[] getColor() {
		return StockNodesRegister.colorOutput;
	}

	@Override
	public String getDisplayName(){
		return "Set Variable";
	}

}
