package com.hbm.tileentity.machine.rbmk;

import com.hbm.handler.neutron.NeutronNodeWorld;
import com.hbm.handler.neutron.RBMKNeutronHandler;
import com.hbm.handler.neutron.RBMKNeutronHandler.RBMKNeutronNode;
import com.hbm.interfaces.AutoRegister;
import com.hbm.tileentity.machine.rbmk.RBMKColumn.ColumnType;
import com.hbm.util.MutableVec3d;
import com.hbm.util.Vec3NT;
import net.minecraft.util.math.BlockPos;

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

	private BlockPos posReasimRod;

	@Override
	protected void spreadFlux(double flux, double ratio) {

		if(posReasimRod == null) posReasimRod = new BlockPos(pos);

		if(flux == 0) {
			// simple way to remove the node from the cache when no flux is going into it!
			NeutronNodeWorld.removeNode(world, posReasimRod);
			return;
		}
		NeutronNodeWorld.StreamWorld streamWorld = NeutronNodeWorld.getOrAddWorld(world);
		RBMKNeutronNode node = (RBMKNeutronNode) streamWorld.getNode(posReasimRod);

		if(node == null) {
			node = makeNode(streamWorld, this);
			streamWorld.addNode(node);
		}

		Vec3NT vec = new Vec3NT(1, 0, 0);
		vec.rotateAroundYDeg(world.rand.nextInt(4) * 9D);
		for(int i = 0; i < 8; i++) {
			new RBMKNeutronHandler.RBMKNeutronStream(node, new Vec3NT(vec), flux * 0.75, ratio);
			vec.rotateAroundYDeg(45D);
		}
	}

	@Override
	public ColumnType getConsoleType() {
		return ColumnType.FUEL_SIM;
	}
}
