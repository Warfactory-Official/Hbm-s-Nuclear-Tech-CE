package com.hbm.tileentity.deco;

import com.hbm.interfaces.AutoRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

@AutoRegister
public class TileEntityTestRender extends TileEntity {
	private AxisAlignedBB bb;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (bb == null) bb = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
		return bb;
	}

}
