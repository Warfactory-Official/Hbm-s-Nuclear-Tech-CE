package com.hbm.inventory.control_panel.nodes;

import com.hbm.inventory.control_panel.DataValue;
import com.hbm.inventory.control_panel.DataValueFloat;
import com.hbm.inventory.control_panel.IControllable;
import com.hbm.inventory.control_panel.NodeConnection;
import com.hbm.inventory.control_panel.NodeDropdown;
import com.hbm.inventory.control_panel.NodeSystem;
import com.hbm.inventory.control_panel.modular.StockNodesRegister;
import com.hbm.tileentity.machine.TileEntityControlPanel;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;

public class NodeRedstoneOutput extends NodeOutput {

	private EnumFacing facing = EnumFacing.NORTH;
	private OutputMode mode = OutputMode.WEAK;

	public NodeRedstoneOutput(float x, float y) {
		super(x, y);
		this.inputs.add(new NodeConnection("Strength", this, inputs.size(), true, DataValue.DataType.NUMBER, new DataValueFloat(0)));

		NodeDropdown faceSelector = new NodeDropdown(this, otherElements.size(), s -> {
			for(EnumFacing candidate : EnumFacing.VALUES) {
				if(candidate.getName().equalsIgnoreCase(s)) {
					facing = candidate;
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
			for(OutputMode candidate : OutputMode.values()) {
				if(candidate.label.equals(s)) {
					mode = candidate;
					break;
				}
			}
			return null;
		}, () -> mode.label);
		for(OutputMode candidate : OutputMode.values()) {
			modeSelector.list.addItems(candidate.label);
		}
		this.otherElements.add(modeSelector);

		recalcSize();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag, NodeSystem sys) {
		tag.setString("nodeType", "redstoneOutput");
		tag.setInteger("facing", facing.getIndex());
		tag.setInteger("mode", mode.ordinal());
		return super.writeToNBT(tag, sys);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag, NodeSystem sys) {
		facing = EnumFacing.byIndex(tag.getInteger("facing"));
		mode = OutputMode.values()[tag.getInteger("mode") % OutputMode.values().length];
		super.readFromNBT(tag, sys);
	}

	@Override
	public boolean doOutput(IControllable from, Map<String, NodeSystem> sendNodeMap, Map<String,BlockPos> positions) {
		if(from instanceof TileEntityControlPanel panel) {
			int strength = Math.round(inputs.get(0).evaluate().getNumber());
			if(mode == OutputMode.WEAK) {
				panel.setWeakRedstoneOutput(facing, strength);
			} else {
				panel.setStrongRedstoneOutput(facing, strength);
			}
		}
		return true;
	}

	@Override
	public float[] getColor() {
		return StockNodesRegister.colorOutput;
	}

	@Override
	public String getDisplayName() {
		return "Redstone Output";
	}

	private enum OutputMode {
		WEAK("Weak"),
		STRONG("Strong");

		private final String label;

		OutputMode(String label) {
			this.label = label;
		}
	}
}
