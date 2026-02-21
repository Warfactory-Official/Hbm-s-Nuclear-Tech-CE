package com.hbm.inventory.control_panel.nodes;

import com.hbm.inventory.control_panel.*;
import com.hbm.inventory.control_panel.DataValue.DataType;
import com.hbm.inventory.control_panel.modular.StockNodesRegister;
import net.minecraft.nbt.NBTTagCompound;

public class NodeBoolean extends Node {

    public BoolOperation op = BoolOperation.AND;

    public NodeBoolean(float x, float y) {
        super(x, y);
        this.outputs.add(new NodeConnection("Output", this, outputs.size(), false, DataType.NUMBER, new DataValueFloat(0)));
        NodeDropdown opSelector = new NodeDropdown(this, otherElements.size(), s -> {
            BoolOperation op = BoolOperation.getByName(s);
            if (op != null) setOperation(op);
            return null;
        }, () -> op.name());
        for (BoolOperation op : BoolOperation.VALUES) {
            opSelector.list.addItems(op.name());
        }
        this.otherElements.add(opSelector);
        setOperation(BoolOperation.AND);
        evalCache = new DataValue[1];
    }

    @Override
    public float[] getColor() {
        return StockNodesRegister.colorBoolean;
    }

    @Override
    public String getDisplayName() {
        return op.name();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag, NodeSystem sys) {
        tag.setString("nodeType", "boolean");
        tag.setInteger("op", op.ordinal());
        return super.writeToNBT(tag, sys);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag, NodeSystem sys) {
        op = BoolOperation.VALUES[tag.getInteger("op")%BoolOperation.VALUES.length];
        super.readFromNBT(tag, sys);
    }

    @Override
    public DataValue evaluate(int idx) {
        if (cacheValid)
            return evalCache[0];
        cacheValid = true;

        DataValue[] evals = new DataValue[inputs.size()];
        for (int i=0; i < evals.length; i++) {
            evals[i] = inputs.get(i).evaluate();
            if (evals[i] == null)
                return null;
        }

        switch (op) {
            case AND: return evalCache[0] = new DataValueFloat(evals[0].getBoolean() && evals[1].getBoolean());
            case OR: return evalCache[0] = new DataValueFloat(evals[0].getBoolean() || evals[1].getBoolean());
            case NOT: return evalCache[0] = new DataValueFloat(!evals[0].getBoolean());
            case XOR: return evalCache[0] = new DataValueFloat(evals[0].getBoolean() ^ evals[1].getBoolean());
            case NAND: return evalCache[0] = new DataValueFloat(!(evals[0].getBoolean() && evals[1].getBoolean()));
            case NOR: return evalCache[0] = new DataValueFloat(!(evals[0].getBoolean() || evals[1].getBoolean()));
            case XNOR: return evalCache[0] = new DataValueFloat(evals[0].getBoolean() == evals[1].getBoolean());
        }

        return evalCache[0] = null;
    }

    public void setOperation(BoolOperation op) {
        this.op = op;
        for (NodeConnection c : inputs) {
            c.removeConnection();
        }
        this.inputs.clear();
        String s1 = "Input 1";
        String s2 = "Input 2";

        if (op == BoolOperation.NOT) {
            inputs.add(new NodeConnection("Input", this, inputs.size(), true, DataType.NUMBER, new DataValueFloat(0)));
        } else {
            inputs.add(new NodeConnection(s1, this, inputs.size(), true, DataType.NUMBER, new DataValueFloat(0)));
            inputs.add(new NodeConnection(s2, this, inputs.size(), true, DataType.NUMBER, new DataValueFloat(0)));
        }

        recalcSize();
    }

    public NodeBoolean setData(BoolOperation op) {
        setOperation(op);
        return this;
    }

    public enum BoolOperation {
        AND,
        OR,
        NOT,
        XOR,
        NAND,
        NOR,
        XNOR;

        public static final BoolOperation[] VALUES = values();

        public static BoolOperation getByName(String name) {
            for (BoolOperation o : BoolOperation.VALUES) {
                if (o.name().equals(name)) {
                    return o;
                }
            }
            return null;
        }
    }
}
