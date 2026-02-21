package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyProviderMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.forgefluid.FFUtils;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.container.ContainerMachineTurbine;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.inventory.gui.GUIMachineTurbine;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@AutoRegister
public class TileEntityMachineTurbine extends TileEntityLoadedBase implements ITickable, IEnergyProviderMK2, IFluidStandardTransceiver, IGUIProvider, IFFtoNTMF {

	public ItemStackHandler inventory;

	public long power;
	public static final long maxPower = 1000000;
	public int age = 0;
	public FluidTankNTM[] tanksNew;
	public FluidTank[] tanks;
	public Fluid[] tankTypes;
	private static boolean converted = false;
	//Drillgon200: Not even used but I'm too lazy to remove them

	// private static final int[] slots_top = new int[] {4};
	// private static final int[] slots_bottom = new int[] {6};
	// private static final int[] slots_side = new int[] {4};

	private String customName;

	public TileEntityMachineTurbine() {
		inventory = new ItemStackHandler(7) {
			@Override
			protected void onContentsChanged(int slot) {
				super.onContentsChanged(slot);
				markDirty();
			}

			@Override
			public boolean isItemValid(int slot, @NotNull ItemStack stack) {
				if(slot == 0)
					return stack.getItem() instanceof IItemFluidIdentifier;

				if(slot == 4)
					return Library.isBattery(stack);

				return true;
			}

			@Override
			public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
				if(this.isItemValid(slot, stack))
					return super.insertItem(slot, stack, simulate);
				return ItemStack.EMPTY;
			}
		};
		tanksNew = new FluidTankNTM[2];
		tanksNew[0] = new FluidTankNTM(Fluids.STEAM, 64000, 0);
		tanksNew[1] = new FluidTankNTM(Fluids.SPENTSTEAM, 128000, 1);

		tanks = new FluidTank[2];
		tankTypes = new Fluid[2];
		tanks[0] = new FluidTank(64000);
		tankTypes[0] = Fluids.STEAM.getFF();
		tanks[1] = new FluidTank(128000);
		tankTypes[1] = Fluids.SPENTSTEAM.getFF();;

		converted = true;
	}

	@Override
	public void update() {
		if(!world.isRemote) {
			if(!converted){
				convertAndSetFluids(tankTypes, tanks, tanksNew);
				converted = true;
			}
			age++;
			if(age >= 2) {
				age = 0;
			}

			this.subscribeToAllAround(tanksNew[0].getTankType(), this);

			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
				this.tryProvide(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);

			tanksNew[0].setType(0, 1, inventory);
			tanksNew[0].loadTank(2, 3, inventory);
			power = Library.chargeItemsFromTE(inventory, 4, power, maxPower);

			FluidType in = tanksNew[0].getTankType();
			boolean valid = false;
			if(in.hasTrait(FT_Coolable.class)) {
				FT_Coolable trait = in.getTrait(FT_Coolable.class);
				double eff = trait.getEfficiency(FT_Coolable.CoolingType.TURBINE) * 0.85D; //small turbine is only 85% efficient
				if(eff > 0) {
					tanksNew[1].setTankType(trait.coolsTo);
					int inputOps = tanksNew[0].getFill() / trait.amountReq;
					int outputOps = (tanksNew[1].getMaxFill() - tanksNew[1].getFill()) / trait.amountProduced;
					int cap = 6_000 / trait.amountReq;
					int ops = Math.min(inputOps, Math.min(outputOps, cap));
					tanksNew[0].setFill(tanksNew[0].getFill() - ops * trait.amountReq);
					tanksNew[1].setFill(tanksNew[1].getFill() + ops * trait.amountProduced);
					this.power += (ops * trait.heatEnergy * eff);
					valid = true;
				}
			}
			if(!valid) tanksNew[1].setTankType(Fluids.NONE);
			if(power > maxPower) power = maxPower;

			this.sendFluidToAll(tanksNew[1], this);

			tanksNew[1].unloadTank(5, 6, inventory);
			networkPackNT(50);
		}
	}

	@Override
	public void serialize(ByteBuf buf){
		buf.writeLong(power);
		for(FluidTankNTM tank : tanksNew)
			tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf){
		power = buf.readLong();
		for(FluidTankNTM tank : tanksNew)
			tank.deserialize(buf);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		if(!converted){
			if(nbt.hasKey("tankType0"))
				tankTypes[0] = FluidRegistry.getFluid(nbt.getString("tankType0"));
			else
				tankTypes[0] = null;
			if(nbt.hasKey("tankType1"))
				tankTypes[1] = FluidRegistry.getFluid(nbt.getString("tankType1"));
			else
				tankTypes[1] = null;
			if(nbt.hasKey("tanks"))
				FFUtils.deserializeTankArray(nbt.getTagList("tanks", 10), tanks);
		} else {
			tanksNew[0].readFromNBT(nbt, "water");
			tanksNew[1].readFromNBT(nbt, "steam");
			if(nbt.hasKey("tankType0")){
				nbt.removeTag("tankType0");
				nbt.removeTag("tankType1");
				nbt.removeTag("tanks");
			}
		}
		power = nbt.getLong("power");

		NBTTagList list = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			int slot = nbt1.getByte("Slot") & 255;
			if (slot >= 0 && slot < inventory.getSlots()) {
				inventory.setStackInSlot(slot, new ItemStack(nbt1));
			}
		}
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if(!converted){
			nbt.setTag("tanks", FFUtils.serializeTankArray(tanks));
			if(tankTypes[0] != null)
				nbt.setString("tankType0", tankTypes[0].getName());
			if(tankTypes[1] != null)
				nbt.setString("tankType1", tankTypes[1].getName());
		} else {
			tanksNew[0].writeToNBT(nbt, "water");
			tanksNew[1].writeToNBT(nbt, "steam");
		}
		nbt.setLong("power", power);

		NBTTagList list = new NBTTagList();
		for (int i = 0; i < inventory.getSlots(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (!stack.isEmpty()) {
				NBTTagCompound nbt1 = new NBTTagCompound();
				nbt1.setByte("Slot", (byte)i);
				stack.writeToNBT(nbt1);
				list.appendTag(nbt1);
			}
		}
		nbt.setTag("Items", list);
		return nbt;
	}

	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "container.machineTurbine";
	}

	public boolean hasCustomInventoryName() {
		return this.customName != null && this.customName.length() > 0;
	}

	public void setCustomName(String name) {
		this.customName = name;
	}

	public boolean isUseableByPlayer(EntityPlayer player) {
		if(world.getTileEntity(pos) != this) {
			return false;
		} else {
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64;
		}
	}

	public long getPowerScaled(int i) {
		return (power * i) / maxPower;
	}

	private long detectPower;
	private FluidTankNTM[] detectTanks = new FluidTankNTM[] { null, null };
	private Fluid[] detectFluids = new Fluid[] { null, null };

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
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineTurbine(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineTurbine(player.inventory, this);
	}

	@Override
	public FluidTankNTM[] getSendingTanks() {
		return new FluidTankNTM[] { tanksNew[1] };
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] { tanksNew[0] };
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return tanksNew;
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
