package com.hbm.tileentity.machine;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.api.energymk2.IEnergyProviderMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.container.ContainerMachineDiesel;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbm.inventory.gui.GUIMachineDiesel;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;

@AutoRegister
public class TileEntityMachineDiesel extends TileEntityMachinePolluting implements ITickable, IEnergyProviderMK2, IFluidStandardTransceiver, IGUIProvider, IConfigurableMachine, IFluidCopiable {

	public long power;

	/* CONFIGURABLE CONSTANTS */
	public static int fluidCap = 16000;
	public static long maxPower = 50000;
	public long powerCap = 50000;
	public int age = 0;
	public FluidTankNTM tank;

	public boolean wasOn = false;
	private AudioWrapper audio;

	private static final int[] slots_top = new int[] { 0 };
	private static final int[] slots_bottom = new int[] { 1, 2 };
	private static final int[] slots_side = new int[] { 2 };

	public static HashMap<FT_Combustible.FuelGrade, Double> fuelEfficiency = new HashMap<>();
	static {
		fuelEfficiency.put(FT_Combustible.FuelGrade.MEDIUM,	0.5D);
		fuelEfficiency.put(FT_Combustible.FuelGrade.HIGH, 0.75D);
		fuelEfficiency.put(FT_Combustible.FuelGrade.AERO, 0.1D);
	}

	public TileEntityMachineDiesel() {
		super(5, 100, true, true);
		tank = new FluidTankNTM(Fluids.DIESEL, 4_000);
	}
	
	@Override
	public String getDefaultName() {
		return "container.machineDiesel";
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setLong("powerTime", power);
		compound.setLong("powerCap", powerCap);
		tank.writeToNBT(compound, "tank");
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.power = compound.getLong("powerTime");
		this.powerCap = compound.getLong("powerCap");
		tank.readFromNBT(compound, "tank");
		super.readFromNBT(compound);
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e) {
		int ordinal = e.ordinal();
		return ordinal == 0 ? slots_bottom : (ordinal == 1 ? slots_top : slots_side);
	}
	
	public long getPowerScaled(long i) {
		return (power * i) / powerCap;
	}
	
	@Override
	public void update() {
		if (!world.isRemote) {
			this.wasOn = false;
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				this.tryProvide(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
				this.sendSmoke(pos.getX() + dir.offsetX, pos.getX() + dir.offsetY, pos.getX() + dir.offsetZ, dir);
			}

			//Tank Management
			FluidType last = tank.getTankType();
			if(tank.setType(3, 4, inventory)) this.unsubscribeToAllAround(last, this);
			tank.loadTank(0, 1, inventory);

			this.subscribeToAllAround(tank.getTankType(), this);

			// Battery Item
			power = Library.chargeItemsFromTE(inventory, 2, power, powerCap);
			generate();
			this.networkPackNT(50);
		} else {

			if(wasOn) {

				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}

				audio.keepAlive();
				audio.updateVolume(this.getVolume(1F));

			} else {

				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound(new SoundEvent(new ResourceLocation("hbm:block.engine")), SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 1.0F, 10F, 1.0F, 10);
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
	public void serialize(ByteBuf buf){
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeLong(powerCap);
		buf.writeBoolean(wasOn);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf){
		super.deserialize(buf);
		power = buf.readLong();
		powerCap = buf.readLong();
		this.wasOn = buf.readBoolean();
		tank.deserialize(buf);
	}

	public boolean hasAcceptableFuel() {
		return getHEFromFuel() > 0;
	}
	
	public long getHEFromFuel() {
        return getHEFromFuel(tank.getTankType());
	}
	
	public static long getHEFromFuel(FluidType type) {
		if(type.hasTrait(FT_Combustible.class)) {
			FT_Combustible fuel = type.getTrait(FT_Combustible.class);
			FT_Combustible.FuelGrade grade = fuel.getGrade();
			double efficiency = fuelEfficiency.containsKey(grade) ? fuelEfficiency.get(grade) : 0;

			if(fuel.getGrade() != FT_Combustible.FuelGrade.LOW) {
				return (long) (fuel.getCombustionEnergy() / 1000L * efficiency);
			}
		}
		
		return 0;
	}

	public void generate() {
		if (world.isBlockPowered(pos)) return;
		if(hasAcceptableFuel()) {
			if (tank.getFill() > 0) {

				this.wasOn = true;

				tank.setFill(tank.getFill() - 1);
				if(tank.getFill() < 0)
					tank.setFill(0);

				if(world.getTotalWorldTime() % 5 == 0) {
					super.pollute(tank.getTankType(), FluidTrait.FluidReleaseType.BURN, 5F);
				}

				if(power + getHEFromFuel() <= powerCap) {
					power += getHEFromFuel();
				} else {
					power = powerCap;
				}
			}
		}
	}
	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		if(i == 0) return FluidContainerRegistry.getFluidContent(stack, tank.getTankType()) > 0;
		if(i == 2) return Library.isChargeableBattery(stack);
		return false;
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
	public FluidTankNTM[] getSendingTanks() {
		return this.getSmokeTanks();
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[]{tank};
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[]{tank};
	}

	@Override
	public String getConfigName() {
		return "dieselgen";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		maxPower = IConfigurableMachine.grab(obj, "L:powerCap", maxPower);
		fluidCap = IConfigurableMachine.grab(obj, "I:fuelCap", fluidCap);

		if(obj.has("D[:efficiency")) {
			JsonArray array = obj.get("D[:efficiency").getAsJsonArray();
			for(FT_Combustible.FuelGrade grade : FT_Combustible.FuelGrade.VALUES) {
				fuelEfficiency.put(grade, array.get(grade.ordinal()).getAsDouble());
			}
		}
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("L:powerCap").value(maxPower);
		writer.name("I:fuelCap").value(fluidCap);

		String info = "Fuel grades in order: ";
		for(FT_Combustible.FuelGrade grade : FT_Combustible.FuelGrade.VALUES) info += grade.name() + " ";
		info = info.trim();
		writer.name("INFO").value(info);

		writer.name("D[:efficiency").beginArray().setIndent("");
		for(FT_Combustible.FuelGrade grade : FT_Combustible.FuelGrade.VALUES) {
			double d = fuelEfficiency.containsKey(grade) ? fuelEfficiency.get(grade) : 0.0D;
			writer.value(d);
		}
		writer.endArray().setIndent("  ");
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineDiesel(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineDiesel(player.inventory, this);
	}
}
