package com.hbm.tileentity.machine;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.api.energymk2.IEnergyProviderMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Random;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityChungus extends TileEntityLoadedBase implements ITickable, IFluidStandardTransceiver, SimpleComponent, IEnergyProviderMK2, CompatHandler.OCComponent, IFluidCopiable, IConfigurableMachine, IFFtoNTMF {

	public long power;
	private int turnTimer;
	public float rotor;
	public float lastRotor;
	public float fanAcceleration = 0F;

	private AudioWrapper audio;
	private final float audioDesync;
	
	public FluidTank[] tanksOld;
	public Fluid[] types;
	// for convertation (for explanation. in some cases you can see here I did rename new tanks instead of old ones because I did port them before doing IFFtoNTMF. From this tileentity, I'm doing that in exact opposite order)
	public FluidTankNTM[] tanks;
	private static boolean converted = false;

	//Configurable values
	public static long maxPower = 100000000000L;
	public static int inputTankSize = 1_000_000_000;
	public static int outputTankSize = 1_000_000_000;
	public static double efficiency = 0.85D;
	
	public TileEntityChungus() {
		super();
		tanksOld = new FluidTank[2];
		types = new Fluid[2];
		tanksOld[0] = new FluidTank(2000000000);
		tanksOld[1] = new FluidTank(2000000000);
		types[0] = Fluids.STEAM.getFF();
		types[1] = Fluids.SPENTSTEAM.getFF();

		tanks = new FluidTankNTM[2];
		tanks[0] = new FluidTankNTM(Fluids.STEAM, inputTankSize);
		tanks[1] = new FluidTankNTM(Fluids.SPENTSTEAM, outputTankSize);

		Random rand = new Random();
		audioDesync = rand.nextFloat() * 0.05F;
		converted = true;
	}

	@Override
	public String getConfigName() {
		return "steamturbineLeviathan";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		maxPower = IConfigurableMachine.grab(obj, "L:maxPower", maxPower);
		inputTankSize = IConfigurableMachine.grab(obj, "I:inputTankSize", inputTankSize);
		outputTankSize = IConfigurableMachine.grab(obj, "I:outputTankSize", outputTankSize);
		efficiency = IConfigurableMachine.grab(obj, "D:efficiency", efficiency);
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("L:maxPower").value(maxPower);
		writer.name("INFO").value("leviathan steam turbine consumes all availible steam per tick");
		writer.name("I:inputTankSize").value(inputTankSize);
		writer.name("I:outputTankSize").value(outputTankSize);
		writer.name("D:efficiency").value(efficiency);
	}

	@Override
	public void update() {
		if(!converted){
			convertAndSetFluids(types, tanksOld, tanks);
			converted = true;
		}
		if(!world.isRemote) {

			this.power *= 0.95;

			boolean operational = false;
			FluidType in = tanks[0].getTankType();
			boolean valid = false;
			if(in.hasTrait(FT_Coolable.class)) {
				FT_Coolable trait = in.getTrait(FT_Coolable.class);
				double eff = trait.getEfficiency(FT_Coolable.CoolingType.TURBINE) * efficiency; //85% efficiency by default
				if(eff > 0) {
					tanks[1].setTankType(trait.coolsTo);
					int inputOps = tanks[0].getFill() / trait.amountReq;
					int outputOps = (tanks[1].getMaxFill() - tanks[1].getFill()) / trait.amountProduced;
					int ops = Math.min(inputOps, outputOps);
					tanks[0].setFill(tanks[0].getFill() - ops * trait.amountReq);
					tanks[1].setFill(tanks[1].getFill() + ops * trait.amountProduced);
					this.power += (long) (ops * trait.heatEnergy * eff);
					valid = true;
					operational = ops > 0;
				}
			}

			if(!valid) tanks[1].setTankType(Fluids.NONE);
			if(power > maxPower) power = maxPower;

			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
			this.tryProvide(world, pos.getX() - dir.offsetX * 11, pos.getY(), pos.getZ() - dir.offsetZ * 11, dir.getOpposite());

			for(DirPos pos : this.getConPos()) {
				this.sendFluid(tanks[1], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
				this.trySubscribe(tanks[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
			}

			if(power > maxPower)
				power = maxPower;

			turnTimer--;

			if(operational) turnTimer = 25;
			
			networkPackNT(150);

		} else {

			this.lastRotor = this.rotor;
			this.rotor += this.fanAcceleration;

			if(this.rotor >= 360) {
				this.rotor -= 360;
				this.lastRotor -= 360;
			}

			if(turnTimer > 0) {
				// Fan accelerates with a random offset to ensure the audio doesn't perfectly align, makes for a more pleasant hum
				this.fanAcceleration = Math.max(0F, Math.min(25F, this.fanAcceleration + (0.075F + audioDesync)));

				Random rand = world.rand;
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
				ForgeDirection side = dir.getRotation(ForgeDirection.UP);

				for(int i = 0; i < 10; i++) {
					world.spawnParticle(EnumParticleTypes.CLOUD,
							pos.getX() + 0.5 + dir.offsetX * (rand.nextDouble() + 1.25) + rand.nextGaussian() * side.offsetX * 0.65,
							pos.getY() + 2.5 + rand.nextGaussian() * 0.65,
							pos.getZ() + 0.5 + dir.offsetZ * (rand.nextDouble() + 1.25) + rand.nextGaussian() * side.offsetZ * 0.65,
							-dir.offsetX * 0.2, 0, -dir.offsetZ * 0.2);
				}


				if(audio == null) {
					audio = MainRegistry.proxy.getLoopedSound(HBMSoundHandler.chungusOperate, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 1.0F, 20F, 1.0F);
					audio.startSound();
				}

				float turbineSpeed = this.fanAcceleration / 25F;
				audio.updateVolume(getVolume(0.5f * turbineSpeed));
				audio.updatePitch(0.25F + 0.75F * turbineSpeed);
			} else {
				this.fanAcceleration = Math.max(0F, Math.min(25F, this.fanAcceleration -= 0.1F));

				if(audio != null) {
					if(this.fanAcceleration > 0) {
						float turbineSpeed = this.fanAcceleration / 25F;
						audio.updateVolume(getVolume(0.5f * turbineSpeed));
						audio.updatePitch(0.25F + 0.75F * turbineSpeed);
					} else {
						audio.stopSound();
						audio = null;
					}
				}
			}
		}
	}

	public void onLeverPull(FluidType previous) {
		for(DirPos pos : getConPos()) {
			this.tryUnsubscribe(previous, world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ());
		}
	}

	public DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		return new DirPos[] {
				new DirPos(pos.getX() + dir.offsetX * 5, pos.getY() + 2, pos.getZ() + dir.offsetZ * 5, dir),
				new DirPos(pos.getX() + rot.offsetX * 3, pos.getY(), pos.getZ() + rot.offsetZ * 3, rot),
				new DirPos(pos.getX() - rot.offsetX * 3, pos.getY(), pos.getZ() - rot.offsetZ * 3, rot.getOpposite())
		};
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeLong(this.power);
		buf.writeInt(this.turnTimer);
		this.tanks[0].serialize(buf);
		this.tanks[1].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.power = buf.readLong();
		this.turnTimer = buf.readInt();
		this.tanks[0].deserialize(buf);
		this.tanks[1].deserialize(buf);
	}


	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if(!converted) {
			tanksOld[0].readFromNBT(nbt.getCompoundTag("tank0"));
			tanksOld[1].readFromNBT(nbt.getCompoundTag("tank1"));
			types[0] = FluidRegistry.getFluid(nbt.getString("types0"));
			types[1] = FluidRegistry.getFluid(nbt.getString("types1"));
		} else{
			tanks[0].readFromNBT(nbt, "water");
			tanks[1].readFromNBT(nbt, "steam");
			if(nbt.hasKey("tank)")){
				nbt.removeTag("tank0");
				nbt.removeTag("tank1");
				nbt.removeTag("types0");
				nbt.removeTag("types1");
			}
		}
		power = nbt.getLong("power");
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if(!converted) {
			nbt.setTag("tank0", tanksOld[0].writeToNBT(new NBTTagCompound()));
			nbt.setTag("tank1", tanksOld[1].writeToNBT(new NBTTagCompound()));
			nbt.setString("types0", types[0].getName());
			nbt.setString("types1", types[1].getName());
		} else{
			tanks[0].writeToNBT(nbt, "water");
			tanks[1].writeToNBT(nbt, "steam");
		}
		nbt.setLong("power", power);
		return nbt;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public boolean canConnect(ForgeDirection dir) {
		return dir != ForgeDirection.UP && dir != ForgeDirection.DOWN && dir != ForgeDirection.UNKNOWN;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public void setPower(long i) {
		power = i;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	@Optional.Method(modid = "opencomputers")
	public String getComponentName() {
		return "ntm_turbine";
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getFluid(Context context, Arguments args) {
		return new Object[] {tanks[0].getFill(), tanks[0].getMaxFill(), tanks[1].getFill(), tanks[1].getMaxFill()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getType(Context context, Arguments args) {
		return CompatHandler.steamTypeToInt(tanks[1].getTankType());
	}

	@Callback(direct = true, limit = 4)
	@Optional.Method(modid = "opencomputers")
	public Object[] setType(Context context, Arguments args) {
		tanks[0].setTankType(CompatHandler.intToSteamType(args.checkInteger(0)));
		return new Object[] {};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getInfo(Context context, Arguments args) {
		return new Object[] {tanks[0].getFill(), tanks[0].getMaxFill(), tanks[1].getFill(), tanks[1].getMaxFill(), CompatHandler.steamTypeToInt(tanks[0].getTankType())};
	}

	@Override
	@Optional.Method(modid = "opencomputers")
	public String[] methods() {
		return new String[] {
				"getFluid",
				"getType",
				"setType",
				"getInfo"
		};
	}

	@Override
	@Optional.Method(modid = "opencomputers")
	public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        return switch (method) {
            case ("getFluid") -> getFluid(context, args);
            case ("getType") -> getType(context, args);
            case ("setType") -> setType(context, args);
            case ("getInfo") -> getInfo(context, args);
            default -> throw new NoSuchMethodException();
        };
    }

	@Override
	public FluidTankNTM[] getSendingTanks() {
		return new FluidTankNTM[] {tanks[1]};
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] {tanks[0]};
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
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == CapabilityEnergy.ENERGY) {
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
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(
					new NTMEnergyCapabilityWrapper(this)
			);
		}
		return super.getCapability(capability, facing);
	}
}