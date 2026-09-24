package com.hbm.blocks.machine.rbmk;

import com.hbm.tileentity.machine.rbmk.TileEntityRBMKModerator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class RBMKModerator extends RBMKBase {

	public RBMKModerator(String s, String c){
		super(s, c);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		if(meta >= offset)
			return new TileEntityRBMKModerator();
		return null;
	}

}
