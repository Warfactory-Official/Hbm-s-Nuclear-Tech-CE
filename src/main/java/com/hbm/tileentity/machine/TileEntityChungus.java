package com.hbm.tileentity.machine;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.BlockDummyable;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IConfigurableMachine;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
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
public class TileEntityChungus extends TileEntityTurbineBase implements SimpleComponent, CompatHandler.OCComponent, IConfigurableMachine {

	public long power;
	private int turnTimer;
	public float rotor;
	public float lastRotor;
	public float fanAcceleration = 0F;

	private AudioWrapper audio;
	private final float audioDesync;

	//Configurable values
	public static int inputTankSize = 1_000_000_000;
	public static int outputTankSize = 1_000_000_000;
	public static double efficiency = 0.85D;
	
	public TileEntityChungus() {
		super();
		tanks = new FluidTankNTM[2];
		tanks[0] = new FluidTankNTM(Fluids.STEAM, inputTankSize);
		tanks[1] = new FluidTankNTM(Fluids.SPENTSTEAM, outputTankSize);

		Random rand = new Random();
		audioDesync = rand.nextFloat() * 0.05F;
	}

	@Override
	public String getConfigName() {
		return "steamturbineLeviathan";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		inputTankSize = IConfigurableMachine.grab(obj, "I:inputTankSize", inputTankSize);
		outputTankSize = IConfigurableMachine.grab(obj, "I:outputTankSize", outputTankSize);
		efficiency = IConfigurableMachine.grab(obj, "D:efficiency", efficiency);
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("INFO").value("leviathan steam turbine consumes all availible steam per tick");
		writer.name("I:inputTankSize").value(inputTankSize);
		writer.name("I:outputTankSize").value(outputTankSize);
		writer.name("D:efficiency").value(efficiency);
	}

	@Override public double consumptionPercent() { return 1D; }
	@Override public double getEfficiency() { return efficiency; }

	@Override
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
	public DirPos[] getPowerPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		return new DirPos[] { new DirPos(pos.getX() - dir.offsetX * 11, pos.getY(), pos.getZ() - dir.offsetZ * 11, dir.getOpposite()) };
	}

	@Override
	public void onServerTick() {
		turnTimer--;
		if(operational) turnTimer = 25;
	}

	@Override
	public void onClientTick() {
		this.lastRotor = this.rotor;
		this.rotor += this.fanAcceleration;

		if (this.rotor >= 360) {
			this.rotor -= 360;
			this.lastRotor -= 360;
		}

		if (turnTimer > 0) {
			// Fan accelerates with a random offset to ensure the audio doesn't perfectly align, makes for a more pleasant hum
			this.fanAcceleration = Math.max(0F, Math.min(25F, this.fanAcceleration + (0.075F + audioDesync)));

			Random rand = world.rand;
			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
			ForgeDirection side = dir.getRotation(ForgeDirection.UP);

			for (int i = 0; i < 10; i++) {
				world.spawnParticle(EnumParticleTypes.CLOUD,
						pos.getX() + 0.5 + dir.offsetX * (rand.nextDouble() + 1.25) + rand.nextGaussian() * side.offsetX * 0.65,
						pos.getY() + 2.5 + rand.nextGaussian() * 0.65,
						pos.getZ() + 0.5 + dir.offsetZ * (rand.nextDouble() + 1.25) + rand.nextGaussian() * side.offsetZ * 0.65,
						-dir.offsetX * 0.2, 0, -dir.offsetZ * 0.2);
			}


			if (audio == null) {
				audio = MainRegistry.proxy.getLoopedSound(HBMSoundHandler.chungusOperate, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 1.0F, 20F, 1.0F);
				audio.startSound();
			}

			float turbineSpeed = this.fanAcceleration / 25F;
			audio.updateVolume(getVolume(0.5f * turbineSpeed));
			audio.updatePitch(0.25F + 0.75F * turbineSpeed);
			audio.keepAlive();
		} else {
			this.fanAcceleration = Math.max(0F, Math.min(25F, this.fanAcceleration -= 0.1F));

			if (audio != null) {
				if (this.fanAcceleration > 0) {
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

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(this.turnTimer);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.turnTimer = buf.readInt();
	}
	
	@Override
	public @NotNull AxisAlignedBB getRenderBoundingBox() {
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

	@Callback(direct = true, doc = "function():table -- Gets current tanks state. The format is the following: <input tank amount>, <input tank capacity>, <output tank amount>, <output tank capacity>")
	@Optional.Method(modid = "opencomputers")
	public Object[] getFluid(Context context, Arguments args) {
		return new Object[] {tanks[0].getFill(), tanks[0].getMaxFill(), tanks[1].getFill(), tanks[1].getMaxFill()};
	}

	@Callback(direct = true, doc = "function():number -- Gets the current input tank fluid type. 0 stands for steam, 1 for dense steam, 2 for super dense steam and 3 for ultra dense steam.")
	@Optional.Method(modid = "opencomputers")
	public Object[] getType(Context context, Arguments args) {
		return CompatHandler.steamTypeToInt(tanks[0].getTankType());
	}

	@Callback(direct = true, limit = 4, doc = "function(type:number) -- Sets the input tank fluid type. Refer getType() for the accepted values information.")
	@Optional.Method(modid = "opencomputers")
	public Object[] setType(Context context, Arguments args) {
		tanks[0].setTankType(CompatHandler.intToSteamType(args.checkInteger(0)));
		return new Object[] {};
	}

	@Callback(direct = true, doc = "function():number -- Gets the power buffer of the turbine.")
	@Optional.Method(modid = "opencomputers")
	public Object[] getPower(Context context, Arguments args) {
		return new Object[] {powerBuffer};
	}

	@Callback(direct = true, doc = "function():table -- Gets information about this turbine. The format is the following: <input tank amount>, <input tank capacity>, <output tank amount>, <output tank capacity>, <input tank fluid type>, <power>")
	@Optional.Method(modid = "opencomputers")
	public Object[] getInfo(Context context, Arguments args) {
		return new Object[] {tanks[0].getFill(), tanks[0].getMaxFill(), tanks[1].getFill(), tanks[1].getMaxFill(), CompatHandler.steamTypeToInt(tanks[0].getTankType())[0], powerBuffer};
	}

	@Override
	@Optional.Method(modid = "opencomputers")
	public String[] methods() {
		return new String[] {
				"getFluid",
				"getType",
				"setType",
				"getPower",
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
            case ("getPower") -> getPower(context, args);
            case ("getInfo") -> getInfo(context, args);
            default -> throw new NoSuchMethodException();
        };
    }

	@Override
	public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == CapabilityEnergy.ENERGY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
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