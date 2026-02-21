package com.hbm.inventory.control_panel.nodes;

import com.hbm.inventory.control_panel.*;
import com.hbm.inventory.control_panel.DataValue.DataType;
import com.hbm.inventory.control_panel.modular.StockNodesRegister;
import net.minecraft.nbt.NBTTagCompound;

public class NodeBuffer extends Node {

    private int internalCount;

    public NodeBuffer(float x, float y) {
        super(x, y);
        this.inputs.add(new NodeConnection("Input", this, inputs.size(), true, DataType.NUMBER, new DataValueFloat(0)));
        this.inputs.add(new NodeConnection("Delay", this, inputs.size(), true, DataType.NUMBER, new DataValueFloat(0)));
        this.outputs.add(new NodeConnection("Output", this, outputs.size(), false, DataType.NUMBER, new DataValueFloat(0)));
        evalCache = new DataValue[1];
        recalcSize();
    }

    @Override
    public float[] getColor() {
        return StockNodesRegister.colorLogic;
    }

    @Override
    public String getDisplayName() {
        return "Buffer";
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag, NodeSystem sys) {
        tag.setString("nodeType", "buffer");
        tag.setInteger("internalCount", this.internalCount);
        return super.writeToNBT(tag, sys);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag, NodeSystem sys) {
        this.internalCount = tag.getInteger("internalCount");
        super.readFromNBT(tag, sys);
    }

    @Override
    public DataValue evaluate(int inx) {
        if (cacheValid)
            return evalCache[0];
        cacheValid = true;

        DataValue in = inputs.get(0).evaluate();
        DataValue delay = inputs.get(1).evaluate();
        if (in == null || delay == null)
            return null;
        int delayInt = Math.abs(Math.round(delay.getNumber()));

        if (in.getBoolean() || (internalCount != 0)) {
            if (internalCount == delayInt) {
                internalCount = 0;
                return evalCache[0] = in;
            }
            internalCount++;
        }

        return evalCache[0] = new DataValueFloat(0);
    }
}
