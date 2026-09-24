package com.hbm.tileentity.machine;

import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityMachineBigAssTank extends TileEntityBarrel {

	public TileEntityMachineBigAssTank() {
		super(16_000_000);
	}

	@Override
	public String getDefaultName() {
		return "container.bigAssTank";
	}

	@Override public long getReceiverSpeed(FluidType type, int pressure) { return Math.max(50_000, (tankNew.getMaxFill() - tankNew.getFill()) / 100); }
	@Override public long getProviderSpeed(FluidType type, int pressure) { return Math.max(50_000, tankNew.getFill() / 100); }

	@Override
	public void update() {
		if(!world.isRemote) {
			this.checkTilt(TiltType.UNAVOIDABLE, true);
		}
		super.update();
	}

	@Override public int getFloorCount() { return 4 * 4; }
	@Override public BlockPos getFloorPosFromIndex(int index) { return this.standardFloor7x7(index); }

	@Override
	public void checkFluidInteraction() {
		if(tankNew.getTankType().isAntimatter()) {
			world.destroyBlock(pos, false);
			world.newExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, true, true);
		}
	}

	@Override
	public DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);

		return new DirPos[] {
				new DirPos(pos.getX() + dir.offsetX * 7, pos.getY(), pos.getZ() + dir.offsetZ * 7, dir),
				new DirPos(pos.getX() - dir.offsetX * 7, pos.getY(), pos.getZ() - dir.offsetZ * 7, dir.getOpposite())
		};
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(
					pos.getX() - 6,
					pos.getY(),
					pos.getZ() - 6,
					pos.getX() + 7,
					pos.getY() + 5,
					pos.getZ() + 7);
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
