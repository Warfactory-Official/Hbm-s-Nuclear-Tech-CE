package com.hbm.tileentity.machine;

import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityMachineBAT9000 extends TileEntityBarrel {

	public TileEntityMachineBAT9000() {
		super(2048000);
	}
	
	@Override
	public String getDefaultName() {
		return "container.bat9000";
	}
	
	@Override
	public void checkFluidInteraction() {
		if(tankNew.getTankType().isAntimatter()) {
			world.destroyBlock(pos, false);
			world.newExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, true, true);
		}
	}

	@Override
	protected DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() + 3, Library.POS_Z),
				new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() + 3, Library.POS_Z),
				new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() - 3, Library.NEG_Z),
				new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() - 3, Library.NEG_Z),
				new DirPos(pos.getX() + 3, pos.getY(), pos.getZ() + 1, Library.POS_X),
				new DirPos(pos.getX() - 3, pos.getY(), pos.getZ() + 1, Library.NEG_X),
				new DirPos(pos.getX() + 3, pos.getY(), pos.getZ() - 1, Library.POS_X),
				new DirPos(pos.getX() - 3, pos.getY(), pos.getZ() - 1, Library.NEG_X)
		};
	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = new AxisAlignedBB(
					pos.getX() - 2,
					pos.getY(),
					pos.getZ() - 2,
					pos.getX() + 3,
					pos.getY() + 5,
					pos.getZ() + 3
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