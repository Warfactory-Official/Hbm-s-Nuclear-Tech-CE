package com.hbm.tileentity.machine.rbmk;

import com.hbm.api.fluid.IFluidStandardReceiver;
import com.hbm.blocks.machine.rbmk.RBMKBase;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

@AutoRegister
public class TileEntityRBMKInlet extends TileEntityLoadedBase implements ITickable, IFluidStandardReceiver, IBufPacketReceiver {

	public FluidTankNTM water;

	public TileEntityRBMKInlet() {
		water = new FluidTankNTM(Fluids.WATER, 32000).withOwner(this);
	}

	@Override
	public void update() {

		if(!world.isRemote) {

			this.subscribeToAllAround(water.getTankType(), this);

			if(RBMKDials.getReasimBoilers(world)) for(int i = 2; i < 6; i++) {
				ForgeDirection dir = ForgeDirection.getOrientation(i);
				Block b = world.getBlockState(pos.add(dir.offsetX, 0, dir.offsetZ)).getBlock();

				if(b instanceof RBMKBase) {
					int[] pos = ((RBMKBase)b).findCore(world, this.pos.getX() + dir.offsetX, this.pos.getY(), this.pos.getZ() + dir.offsetZ);

					if(pos != null) {
						TileEntity te = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));

						if(te instanceof TileEntityRBMKBase rbmk) {
                            int prov = Math.min(TileEntityRBMKBase.maxWater - rbmk.reasimWater, water.getFill());
							rbmk.reasimWater += prov;
							water.setFill(water.getFill() - prov);
						}
					}
				}
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.water.readFromNBT(nbt, "tank");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		this.water.writeToNBT(nbt, "tank");
		return nbt;
	}

	public void serialize(ByteBuf buf) {
		this.water.serialize(buf);
	}

	public void deserialize(ByteBuf buf) {
		this.water.deserialize(buf);
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[] {water};
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] {water};
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new NTMFluidHandlerWrapper(this));
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}
}
