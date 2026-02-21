package com.hbm.inventory.control_panel.nodes;

import com.hbm.inventory.control_panel.*;
import com.hbm.inventory.control_panel.DataValue.DataType;
import com.hbm.inventory.control_panel.modular.StockNodesRegister;
import net.minecraft.nbt.NBTTagCompound;

public class NodeGetVar extends Node {

	public Control ctrl;
	public boolean global = false;
	public String varName;
	public NodeDropdown varSelector;
	
	public NodeGetVar(float x, float y, Control ctrl){
		super(x, y);
		this.ctrl = ctrl;
		this.outputs.add(new NodeConnection("Output", this, outputs.size(), false, DataType.GENERIC, new DataValueFloat(0)));
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
			this.outputs.get(0).type = evaluate(0).getType();
			return null;
		}, () -> varName);
		setVarSelector();
		this.otherElements.add(varSelector);
		varName = "";
		recalcSize();
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag, NodeSystem sys){
		tag.setString("nodeType", "getVar");
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
	public DataValue evaluate(int idx){
		if(varName.isEmpty())
			return new DataValueFloat(0);
		if(global){
			return ctrl.getGlobalVar(varName);
		} else {
			return ctrl.getVar(varName);
		}
	}

	public NodeGetVar setData(String varName, boolean isGlobal) {
		this.varName = varName;
		this.global = isGlobal;
		this.outputs.get(0).type = evaluate(0).getType();
		return this;
	}

	@Override
	public float[] getColor() {
		return StockNodesRegister.colorInput;
	}

	@Override
	public String getDisplayName(){
		return "Get Variable";
	}

}
