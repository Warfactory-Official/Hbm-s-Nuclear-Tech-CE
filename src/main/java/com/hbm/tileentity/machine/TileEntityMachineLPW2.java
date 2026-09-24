package com.hbm.tileentity.machine;

import com.hbm.interfaces.AutoRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister(name = "tileentity_machine_lpw2")
public class TileEntityMachineLPW2 extends TileEntity {

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) bb = new AxisAlignedBB(pos.getX() - 10, pos.getY(), pos.getZ() - 10, pos.getX() + 11, pos.getY() + 7, pos.getZ() + 11);
		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
