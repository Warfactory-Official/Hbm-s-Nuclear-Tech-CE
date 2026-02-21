package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityMachineOrbus extends TileEntityBarrel {

	public TileEntityMachineOrbus() {
		super(512000);
	}
	
	@Override
	public String getDefaultName() {
		return "container.orbus";
	}
	
	@Override
	public void checkFluidInteraction() { } //NO!

	protected DirPos[] conPos;

	@Override
	protected DirPos[] getConPos() {

		if(conPos != null)
			return conPos;

		conPos = new DirPos[8];

		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
		ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

		for(int i = -1; i < 6; i += 6) {
			ForgeDirection out = i == -1 ? ForgeDirection.DOWN : ForgeDirection.UP;
			int index = i == -1 ? 0 : 4;
			conPos[index + 0] = new DirPos(pos.getX(),								pos.getY() + i,	pos.getZ(),								out);
			conPos[index + 1] = new DirPos(pos.getX() + dir.offsetX,				pos.getY() + i,	pos.getZ() + dir.offsetZ,				out);
			conPos[index + 2] = new DirPos(pos.getX() + rot.offsetX,				pos.getY() + i,	pos.getZ() + rot.offsetZ,				out);
			conPos[index + 3] = new DirPos(pos.getX() + dir.offsetX + rot.offsetX,	pos.getY() + i,	pos.getZ() + dir.offsetZ + rot.offsetZ,	out);
		}

		return conPos;
	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) {
			bb = new AxisAlignedBB(
					pos.getX() - 2,
					pos.getY(),
					pos.getZ() - 2,
					pos.getX() + 2,
					pos.getY() + 5,
					pos.getZ() + 2
					);
		}
		return bb;
	}

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        return null;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}