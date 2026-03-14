package com.hbm.blocks.machine.rbmk;

import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKCooler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class RBMKCooler extends RBMKBase {

	public RBMKCooler(String s, String c){
		super(s, c);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		if(meta >= offset) return new TileEntityRBMKCooler();
		if(hasExtra(meta)) return new TileEntityProxyCombo().fluid();
		return null;
	}

}
