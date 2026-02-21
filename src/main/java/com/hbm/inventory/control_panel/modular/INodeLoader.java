package com.hbm.inventory.control_panel.modular;

import com.hbm.inventory.control_panel.NodeSystem;
import com.hbm.inventory.control_panel.nodes.Node;
import net.minecraft.nbt.NBTTagCompound;

public interface INodeLoader {
	Node nodeFromNBT(NBTTagCompound tag,NodeSystem sys);
}
