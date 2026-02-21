package com.hbm.tileentity.machine;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.api.energymk2.IEnergyReceiverMK2;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import org.jetbrains.annotations.Nullable;

@AutoRegister
public class TileEntityCondenserPowered extends TileEntityCondenser implements IEnergyReceiverMK2 {
	
	public long power;
	public float spin;
	public float lastSpin;
	
	//Configurable values
	public static long maxPower = 10_000_000;
	public static int inputTankSizeP = 1_000_000;
	public static int outputTankSizeP = 1_000_000;
	public static int powerConsumption = 10;

	public TileEntityCondenserPowered() {
		tanks = new FluidTankNTM[2];
		tanks[0] = new FluidTankNTM(Fluids.SPENTSTEAM, inputTankSizeP);
		tanks[1] = new FluidTankNTM(Fluids.WATER, outputTankSizeP);
	}
	
	@Override
	public String getConfigName() {
		return "condenserPowered";
	}
	@Override
	public void readIfPresent(JsonObject obj) {
		maxPower = IConfigurableMachine.grab(obj, "L:maxPower", maxPower);
		inputTankSizeP = IConfigurableMachine.grab(obj, "I:inputTankSize", inputTankSizeP);
		outputTankSizeP = IConfigurableMachine.grab(obj, "I:outputTankSize", outputTankSizeP);
		powerConsumption = IConfigurableMachine.grab(obj, "I:powerConsumption", powerConsumption);
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("L:maxPower").value(maxPower);
		writer.name("I:inputTankSize").value(inputTankSizeP);
		writer.name("I:outputTankSize").value(outputTankSizeP);
		writer.name("I:powerConsumption").value(powerConsumption);
	}

	@Override
	public void update() {
		super.update();
		
		if(world.isRemote) {
			
			this.lastSpin = this.spin;
			
			if(this.waterTimer > 0) {
				this.spin += 30F;
				
				if(this.spin >= 360F) {
					this.spin -= 360F;
					this.lastSpin -= 360F;
				}
				//MetalloloM: Suggestion - add operating sound from TileEntityChungus
				if(world.getTotalWorldTime() % 4 == 0) {
					ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
					world.spawnParticle(EnumParticleTypes.CLOUD, pos.getX() + 0.5 + dir.offsetX * 1.5, pos.getY() + 1.5, pos.getZ() + 0.5 + dir.offsetZ * 1.5, dir.offsetX * 0.1, 0, dir.offsetZ * 0.1);
					world.spawnParticle(EnumParticleTypes.CLOUD, pos.getX() + 0.5 - dir.offsetX * 1.5, pos.getY() + 1.5, pos.getZ() + 0.5 - dir.offsetZ * 1.5, dir.offsetX * -0.1, 0, dir.offsetZ * -0.1);
				}
			}
		}
	}

	@Override
	public void packExtra(NBTTagCompound data) {
		data.setLong("power", power);
	}
	
	@Override
	public boolean extraCondition(int convert) {
		return power >= convert * 10;
	}

	@Override
	public void postConvert(int convert) {
		this.power -= convert * powerConsumption;
		if(this.power < 0) this.power = 0;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(this.power);
		this.tanks[0].serialize(buf);
		this.tanks[1].serialize(buf);
		buf.writeByte(this.waterTimer);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.tanks[0].deserialize(buf);
		this.tanks[1].deserialize(buf);
		this.waterTimer = buf.readByte();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.power = nbt.getLong("power");
		tanks[0].readFromNBT(nbt, "water");
		tanks[1].readFromNBT(nbt, "steam");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		tanks[0].writeToNBT(nbt, "water");
		tanks[1].writeToNBT(nbt, "steam");
        return nbt;
    }

	@Override
	public void subscribeToAllAround(FluidType type, TileEntity te) {
		for(DirPos dirPos : getConPos()) {
			BlockPos pos = dirPos.getPos();
			this.trySubscribe(this.tanks[0].getTankType(), world, pos.getX(), pos.getY(), pos.getZ(), dirPos.getDir());
			this.trySubscribe(world, pos.getX(), pos.getY(), pos.getZ(), dirPos.getDir());
		}
	}

	@Override
	public void sendFluidToAll(FluidTankNTM tank, TileEntity te) {
		for(DirPos dirPos : getConPos()) {
			BlockPos pos = dirPos.getPos();
			this.sendFluid(this.tanks[1], world, pos.getX(), pos.getY(), pos.getZ(), dirPos.getDir());
		}
	}
	
	public DirPos[] getConPos() {
		
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		
		return new DirPos[] {
				new DirPos(pos.getX() + rot.offsetX * 4, pos.getY() + 1, pos.getZ() + rot.offsetZ * 4, rot),
				new DirPos(pos.getX() - rot.offsetX * 4, pos.getY() + 1, pos.getZ() - rot.offsetZ * 4, rot.getOpposite()),
				new DirPos(pos.getX() + dir.offsetX * 2 - rot.offsetX, pos.getY() + 1, pos.getZ() + dir.offsetZ * 2 - rot.offsetZ, dir),
				new DirPos(pos.getX() + dir.offsetX * 2 + rot.offsetX, pos.getY() + 1, pos.getZ() + dir.offsetZ * 2 + rot.offsetZ, dir),
				new DirPos(pos.getX() - dir.offsetX * 2 - rot.offsetX, pos.getY() + 1, pos.getZ() - dir.offsetZ * 2 - rot.offsetZ, dir.getOpposite()),
				new DirPos(pos.getX() - dir.offsetX * 2 + rot.offsetX, pos.getY() + 1, pos.getZ() - dir.offsetZ * 2 + rot.offsetZ, dir.getOpposite())
		};
	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = new AxisAlignedBB(
					pos.getX() - 3,
					pos.getY(),
					pos.getZ() - 3,
					pos.getX() + 4,
					pos.getY() + 3,
					pos.getZ() + 4
					);
		}
		
		return bb;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public long getPower() {
		return this.power;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getMaxPower() {
		return this.maxPower;
	}

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) return CapabilityEnergy.ENERGY.cast(new NTMEnergyCapabilityWrapper(this));
        return super.getCapability(capability, facing);
    }
}
