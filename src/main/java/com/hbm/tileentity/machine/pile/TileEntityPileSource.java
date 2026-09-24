package com.hbm.tileentity.machine.pile;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
@Deprecated
@AutoRegister
public class TileEntityPileSource extends TileEntityPileBase {

	@Override
	public void update() {
		if(!world.isRemote) {
			
			int n = this.getBlockType() == ModBlocks.block_graphite_source ? 1 : 2;
			
			for(int i = 0; i < 12; i++) {
				this.castRay(n);
			}
		}
	}
}
