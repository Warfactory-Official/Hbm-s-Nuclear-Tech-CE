package com.hbm.inventory.control_panel.nodes;

import com.hbm.inventory.control_panel.types.DataValue;
import com.hbm.inventory.control_panel.IControllable;
import com.hbm.inventory.control_panel.NodeSystem;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public abstract class NodeOutput extends Node {

	public NodeOutput(float x, float y){
		super(x, y);
	}
	
	@Override
	public DataValue evaluate(int idx){
		//Output nodes don't need this
		return null;
	}

	/**
	 * Executes this output node.
	 * <p>
	 * Returning {@code true} means processing should continue normally.
	 * Returning {@code false} means the current receiver is canceled when this output is being
	 * evaluated from a send-node map, so {@link NodeEventBroadcast} will skip broadcasting the
	 * event to that receiver. Implementations should therefore return {@code false} only for
	 * explicit cancellation behavior such as {@link NodeCancelEvent}, and return {@code true}
	 * for side effect or no-op outputs like variable assignment and redstone output.
	 */
	public abstract boolean doOutput(IControllable from, Map<String, NodeSystem> sendNodeMap, Map<String,BlockPos> links);
}
