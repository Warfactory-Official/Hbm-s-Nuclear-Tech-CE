package com.hbm.tileentity.machine;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

@AutoRegister
public class TileEntityCondenser extends TileEntityLoadedBase implements ITickable, IFluidStandardTransceiver, IConfigurableMachine, IFluidCopiable {

	public int age = 0;
	public FluidTankNTM[] tanks;
	
	public int waterTimer = 0;
	protected int throughput;

	//Configurable values
	public static int inputTankSize = 100;
	public static int outputTankSize = 100;
	
	public TileEntityCondenser() {

		tanks = new FluidTankNTM[2];
		tanks[0] = new FluidTankNTM(Fluids.SPENTSTEAM, 100);
		tanks[1] = new FluidTankNTM(Fluids.WATER, 100);
	}

	@Override
	public String getConfigName() {
		return "condenser";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		inputTankSize = IConfigurableMachine.grab(obj, "I:inputTankSize", inputTankSize);
		outputTankSize = IConfigurableMachine.grab(obj, "I:outputTankSize", outputTankSize);
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("I:inputTankSize").value(inputTankSize);
		writer.name("I:outputTankSize").value(outputTankSize);
	}
	
	@Override
	public void update() {
		if(!world.isRemote) {

			age++;
			if(age >= 2) {
				age = 0;
			}

			if(this.waterTimer > 0)
				this.waterTimer--;

			int convert = Math.min(tanks[0].getFill(), tanks[1].getMaxFill() - tanks[1].getFill());
			this.throughput = convert;

			if(extraCondition(convert)) {
				tanks[0].setFill(tanks[0].getFill() - convert);

				if(convert > 0)
					this.waterTimer = 20;

				tanks[1].setFill(tanks[1].getFill() + convert);
				postConvert(convert);
			}

			this.subscribeToAllAround(tanks[0].getTankType(), this);
			this.sendFluidToAll(tanks[1], this);

			networkPackNT(150);
		}
	}

	public void packExtra(NBTTagCompound data) { }
	public boolean extraCondition(int convert) { return true; }
	public void postConvert(int convert) { }

	@Override
	public void serialize(ByteBuf buf) {
		this.tanks[0].serialize(buf);
		this.tanks[1].serialize(buf);
		buf.writeByte(this.waterTimer);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.tanks[0].deserialize(buf);
		this.tanks[1].deserialize(buf);
		this.waterTimer = buf.readByte();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		tanks[0].readFromNBT(nbt, "water");
		tanks[1].readFromNBT(nbt, "steam");
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		tanks[0].writeToNBT(nbt, "water");
		tanks[1].writeToNBT(nbt, "steam");
		return super.writeToNBT(nbt);
	}

	@Override
	public FluidTankNTM[] getSendingTanks() {
		return new FluidTankNTM[] {tanks [1]};
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] {tanks [0]};
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTankNTM getTankToPaste() {
		return null;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(
					new NTMFluidHandlerWrapper(this)
			);
		}
		return super.getCapability(capability, facing);
	}
}