package com.hbm.blocks.machine.rbmk;

import com.hbm.tileentity.machine.rbmk.TileEntityRBMKAbsorber;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class RBMKAbsorber extends RBMKBase {

	public RBMKAbsorber(String s, String c){
		super(s, c);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		if(meta >= offset)
			return new TileEntityRBMKAbsorber();
		return null;
	}

}
