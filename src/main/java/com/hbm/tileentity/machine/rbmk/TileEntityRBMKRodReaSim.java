package com.hbm.tileentity.machine.rbmk;

import com.hbm.handler.neutron.NeutronNodeWorld;
import com.hbm.handler.neutron.RBMKNeutronHandler;
import com.hbm.handler.neutron.RBMKNeutronHandler.RBMKNeutronNode;
import com.hbm.interfaces.AutoRegister;
import com.hbm.tileentity.machine.rbmk.RBMKColumn.ColumnType;
import com.hbm.util.MutableVec3d;

import static com.hbm.handler.neutron.RBMKNeutronHandler.makeNode;

@AutoRegister
public class TileEntityRBMKRodReaSim extends TileEntityRBMKRod {
	
	public TileEntityRBMKRodReaSim() {
		super();
	}

	@Override
	public String getName() {
		return "container.rbmkReaSim";
	}
	
	@Override
	protected void spreadFlux(double flux, double ratio) {
		if(flux == 0) {
			// simple way to remove the node from the cache when no flux is going into it!
			NeutronNodeWorld.removeNode(world, pos);
			return;
		}
		NeutronNodeWorld.StreamWorld streamWorld = NeutronNodeWorld.getOrAddWorld(world);
		RBMKNeutronNode node = (RBMKNeutronNode) streamWorld.getNode(pos);

		if(node == null) {
			node = makeNode(streamWorld, this);
			streamWorld.addNode(node);
		}

		int count = RBMKDials.getReaSimCount(world);

		for(int i = 0; i < count; i++) {
            MutableVec3d neutronVector = new MutableVec3d(1, 0, 0);

			neutronVector.rotateYawSelf((float)(Math.PI * 2D * world.rand.nextDouble()));

			new RBMKNeutronHandler.RBMKNeutronStream(makeNode(streamWorld, this), neutronVector, flux, ratio);
			// Create new neutron streams
		}
	}

	@Override
	public ColumnType getConsoleType() {
		return ColumnType.FUEL_SIM;
	}
}
