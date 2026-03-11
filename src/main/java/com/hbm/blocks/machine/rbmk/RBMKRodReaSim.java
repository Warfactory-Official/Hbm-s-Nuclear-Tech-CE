package com.hbm.blocks.machine.rbmk;

import com.hbm.tileentity.TileEntityProxyInventory;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKRodReaSim;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class RBMKRodReaSim extends RBMKRod {

	public RBMKRodReaSim(boolean moderated, String s, String c) {
		super(moderated, s, c);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		
		if(meta >= offset)
			return new TileEntityRBMKRodReaSim();
		
		if(hasExtra(meta))
			return new TileEntityProxyInventory();
		
		return null;
	}
}
