package com.hbm.inventory.control_panel.nodes;

import com.hbm.inventory.control_panel.*;
import com.hbm.inventory.control_panel.types.DataValue;
import com.hbm.inventory.control_panel.types.DataValue.DataType;
import com.hbm.inventory.control_panel.modular.StockNodesRegister;
import com.hbm.inventory.control_panel.types.DataValueFloat;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NodeFunction extends Node {

    public NodeFunction(float x, float y) {
        super(x, y);

        this.inputs.add(new NodeConnection("Enable", this, inputs.size(), true, DataType.NUMBER, new DataValueFloat(0)));
        this.otherElements.add(new NodeButton("Edit Body", this, otherElements.size()) {
            @SideOnly(Side.CLIENT)
            @Override
            public void onClicked(SubElement subElement) {
                if (subElement instanceof SubElementNodeEditor nodeEditor)
                    nodeEditor.descendSubsystem(this.parent);
            }
        });
        this.recalcSize();

        evalCache = new DataValue[1];
    }

    @Override
    public float[] getColor() {
        return StockNodesRegister.colorLogic;
    }

    @Override
    public String getDisplayName() {
        return "Function";
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag, NodeSystem sys) {
        tag.setString("nodeType", "function");
        return super.writeToNBT(tag, sys);
    }

    @Override
    public DataValue evaluate(int idx) {
        if (cacheValid)
            return evalCache[0];
        cacheValid = true;

        DataValue enable = inputs.get(0).evaluate();
        if (enable == null)
            return null;

        return evalCache[0] = new DataValueFloat(enable.getBoolean());
    }

}
