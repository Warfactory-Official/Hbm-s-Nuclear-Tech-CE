package com.hbm.inventory.control_panel;

import com.hbm.inventory.control_panel.types.DataValue;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface IControllable {

	default List<String> getInEvents(){return Collections.emptyList();}
	default List<String> getOutEvents(){return Collections.emptyList();}

	default Map<String,DataValue> getQueryData(){return Collections.emptyMap();}

	// here most things respond by sending the out event back
	// -- a lazy fix for other controls that share a panel not receiving the broadcast event too.
	default void receiveEvent(BlockPos from, ControlEvent e) {}
	
	public BlockPos getControlPos();
	public World getControlWorld();
}
