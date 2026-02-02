package com.hbm.tileentity.machine.rbmk;

import com.hbm.entity.projectile.EntityRBMKDebris.DebrisType;
import com.hbm.handler.neutron.RBMKNeutronHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.tileentity.machine.rbmk.RBMKColumn.ColumnType;

@AutoRegister
public class TileEntityRBMKAbsorber extends TileEntityRBMKBase {
	
	@Override
	public void onMelt(int reduce) {
		
		int count = 1 + world.rand.nextInt(2);
		
		for(int i = 0; i < count; i++) {
			spawnDebris(DebrisType.BLANK);
		}
		
		super.onMelt(reduce);
	}

	@Override
	public RBMKNeutronHandler.RBMKType getRBMKType() {
		return RBMKNeutronHandler.RBMKType.ABSORBER;
	}


	@Override
	public ColumnType getConsoleType() {
		return ColumnType.ABSORBER;
	}
}
