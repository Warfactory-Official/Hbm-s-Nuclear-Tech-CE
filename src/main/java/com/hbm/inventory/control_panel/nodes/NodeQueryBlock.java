package com.hbm.inventory.control_panel.nodes;

import com.hbm.inventory.control_panel.*;
import com.hbm.inventory.control_panel.modular.StockNodesRegister;
import com.hbm.inventory.control_panel.types.DataValue;
import com.hbm.inventory.control_panel.types.DataValueFloat;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NodeQueryBlock extends Node {

    public Control ctrl;

    public String tag = "";
    public String dataName = "";
    public NodeDropdown tagSelector;
    public NodeDropdown dataSelector;

    public NodeQueryBlock(float x, float y, Control ctrl) {
        super(x, y);
        this.ctrl = ctrl;

        this.outputs.add(new NodeConnection("Output", this, outputs.size(), false, DataValue.DataType.GENERIC, new DataValueFloat(0)));

        tagSelector = new NodeDropdown(this, otherElements.size(),s -> {
            tag = s;
            dataName = "";
            setDataSelector();
            return null;
        }, () ->tag);
        this.otherElements.add(tagSelector);
        setBlockPosSelector();

        dataSelector = new NodeDropdown(this, otherElements.size(), s -> {
            dataName = s;
            this.outputs.get(0).type = evaluate(0).getType();
            return null;
        }, () -> dataName);
        setDataSelector();
        this.otherElements.add(dataSelector);

        dataName = "";
        recalcSize();
    }

    private BlockPos getPos(String pos) {
        String[] coords = pos.split(", ");
        return new BlockPos(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
    }

    private void setBlockPosSelector() {
        tagSelector.list.itemNames.clear();
        for (String s : ctrl.taggedLinks.keySet()) {
            tagSelector.list.addItems(s);
        }
    }

    private void setDataSelector() {
        dataSelector.list.itemNames.clear();
        if (tag == null || tag.isEmpty()) return;
        BlockPos pos = ctrl.taggedLinks.get(tag);
        World world = ctrl.panel.parent.getControlWorld();
        if (!world.isBlockLoaded(pos)) return;
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IControllable te) {
            dataSelector.list.addItems(te.getQueryData().keySet());
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag, NodeSystem sys) {
        tag.setString("nodeType", "queryBlock");
        tag.setString("blockPos",this.tag);
        tag.setString("dataName", dataName);

        return super.writeToNBT(tag, sys);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag, NodeSystem sys) {
        this.tag = tag.getString("blockPos");
        dataName = tag.getString("dataName");
        setBlockPosSelector();
        setDataSelector();
        super.readFromNBT(tag, sys);
    }

    @Override
    public DataValue evaluate(int inx) {
        if (!dataName.isEmpty() && ctrl.taggedLinks.containsKey(tag)) {
            BlockPos pos = ctrl.taggedLinks.get(tag);
            World world = ctrl.panel.parent.getControlWorld();
            if (world.isBlockLoaded(pos) && world.getTileEntity(pos) instanceof IControllable te) {
                DataValue v = te.getQueryData().get(dataName);
                if (v != null) return v;
            }
            setDataSelector();
        }
        return new DataValueFloat(0);
    }

    public NodeQueryBlock setData(String blockPos) {
        this.tag = blockPos;
        return this;
    }

    @Override
    public float[] getColor() {
        return StockNodesRegister.colorInput;
    }

    @Override
    public String getDisplayName() {
        return "Query Block";
    }
}
