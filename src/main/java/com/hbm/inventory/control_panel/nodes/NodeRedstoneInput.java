package com.hbm.inventory.control_panel.nodes;

import com.hbm.inventory.control_panel.types.DataValue;
import com.hbm.inventory.control_panel.types.DataValueFloat;
import com.hbm.inventory.control_panel.NodeConnection;
import com.hbm.inventory.control_panel.NodeDropdown;
import com.hbm.inventory.control_panel.NodeSystem;
import com.hbm.inventory.control_panel.Control;
import com.hbm.inventory.control_panel.modular.StockNodesRegister;
import com.hbm.tileentity.machine.TileEntityControlPanel;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class NodeRedstoneInput extends Node {

	private final Control ctrl;
	private EnumFacing facing = EnumFacing.NORTH;
	private ReadMode mode = ReadMode.POWER;

	public NodeRedstoneInput(float x, float y, Control ctrl) {
		super(x, y);
		this.ctrl = ctrl;
		this.outputs.add(new NodeConnection("Output", this, outputs.size(), false, DataValue.DataType.NUMBER, new DataValueFloat(0)));

		NodeDropdown faceSelector = new NodeDropdown(this, otherElements.size(), s -> {
			for(EnumFacing candidate : EnumFacing.VALUES) {
				if(candidate.getName().equalsIgnoreCase(s)) {
					facing = candidate;
					cacheValid = false;
					break;
				}
			}
			return null;
		}, () -> facing.getName());
		for(EnumFacing candidate : EnumFacing.VALUES) {
			faceSelector.list.addItems(candidate.getName());
		}
		this.otherElements.add(faceSelector);

		NodeDropdown modeSelector = new NodeDropdown(this, otherElements.size(), s -> {
			for(ReadMode candidate : ReadMode.values()) {
				if(candidate.label.equals(s)) {
					mode = candidate;
					cacheValid = false;
					break;
				}
			}
			return null;
		}, () -> mode.label);
		for(ReadMode candidate : ReadMode.values()) {
			modeSelector.list.addItems(candidate.label);
		}
		this.otherElements.add(modeSelector);

		evalCache = new DataValue[1];
		recalcSize();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag, NodeSystem sys) {
		tag.setString("nodeType", "redstoneInput");
		tag.setInteger("facing", facing.getIndex());
		tag.setInteger("mode", mode.ordinal());
		return super.writeToNBT(tag, sys);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag, NodeSystem sys) {
		facing = EnumFacing.byIndex(tag.getInteger("facing"));
		mode = ReadMode.values()[tag.getInteger("mode") % ReadMode.values().length];
		super.readFromNBT(tag, sys);
	}

	@Override
	public DataValue evaluate(int idx) {
		if(cacheValid) {
			return evalCache[0];
		}
		cacheValid = true;
		if(!(ctrl.panel.parent instanceof TileEntityControlPanel panel)) {
			return evalCache[0] = new DataValueFloat(0);
		}

		switch(mode) {
			case POWER:
				return evalCache[0] = new DataValueFloat(panel.getRedstoneInputPower(facing));
			case WEAK:
				return evalCache[0] = new DataValueFloat(panel.getRedstoneInputWeak(facing));
			case STRONG:
				return evalCache[0] = new DataValueFloat(panel.getRedstoneInputStrong(facing));
			case IS_POWERED:
				return evalCache[0] = new DataValueFloat(panel.getRedstoneInputPower(facing) > 0);
			case IS_WEAKLY_POWERED:
				return evalCache[0] = new DataValueFloat(panel.getRedstoneInputWeak(facing) > 0);
			case IS_STRONGLY_POWERED:
				return evalCache[0] = new DataValueFloat(panel.getRedstoneInputStrong(facing) > 0);
			default:
				return evalCache[0] = new DataValueFloat(0);
		}
	}

	@Override
	public float[] getColor() {
		return StockNodesRegister.colorInput;
	}

	@Override
	public String getDisplayName() {
		return "Redstone Input";
	}

	private enum ReadMode {
		POWER("Power"),
		WEAK("Weak Power"),
		STRONG("Strong Power"),
		IS_POWERED("Is Powered"),
		IS_WEAKLY_POWERED("Is Weak"),
		IS_STRONGLY_POWERED("Is Strong");

		private final String label;

		ReadMode(String label) {
			this.label = label;
		}
	}
}
