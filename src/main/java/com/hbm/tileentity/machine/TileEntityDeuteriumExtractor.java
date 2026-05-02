package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityDeuteriumExtractor extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardTransceiver, IFluidCopiable {
	
	public long power = 0;
	public FluidTankNTM[] tanks;

	public TileEntityDeuteriumExtractor() {
		super(0, true, true);
		tanks = new FluidTankNTM[2];
		tanks[0] = new FluidTankNTM(Fluids.WATER, 1000).withOwner(this);
		tanks[1] = new FluidTankNTM(Fluids.HEAVYWATER, 100).withOwner(this);
	}

	@Override
	public String getDefaultName() {
		return "container.deuterium";
	}

	@Override
	public void update() {
		
		if(!world.isRemote) {
			this.updateConnections();

			if(hasPower() && hasEnoughWater() && tanks[1].getMaxFill() > tanks[1].getFill()) {
				int convert = Math.min(tanks[1].getMaxFill(), tanks[0].getFill()) / 50;
				convert = Math.min(convert, tanks[1].getMaxFill() - tanks[1].getFill());

				tanks[0].setFill(tanks[0].getFill() - convert * 50); //dividing first, then multiplying, will remove any rounding issues
				tanks[1].setFill(tanks[1].getFill() + convert);
				power -= this.getMaxPower() / 20;
			}

			this.subscribeToAllAround(tanks[0].getTankType(), this);
			this.sendFluidToAll(tanks[1], this);

			this.networkPackNT(50);
		}
	}
	
	protected void updateConnections() {
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) this.trySubscribe(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
	}


	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		tanks[0].serialize(buf);
		tanks[1].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
	}

	public boolean hasPower() {
		return power >= this.getMaxPower() / 20;
	}

	public boolean hasEnoughWater() {
		return tanks[0].getFluidAmount() >= 100;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.power = nbt.getLong("power");
		tanks[0].readFromNBT(nbt, "water");
		tanks[1].readFromNBT(nbt, "heavyWater");
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setLong("power", power);
		tanks[0].writeToNBT(nbt, "water");
		tanks[1].writeToNBT(nbt, "heavyWater");
		return super.writeToNBT(nbt);
	}

	@Override
	public void setPower(long i) {
		power = i;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return 10_000;
	}

	@Override
	public FluidTankNTM[] getSendingTanks() {
		return new FluidTankNTM[] { tanks[1] };
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] { tanks[0] };
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTankNTM getTankToPaste() {
		return null;
	}
}