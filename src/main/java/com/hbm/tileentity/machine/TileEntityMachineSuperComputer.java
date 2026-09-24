package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerMachineSuperComputer;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineSuperComputer;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.ModItems;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.modules.machine.ModuleMachineSuperComputer;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister(name = "tileentity_supercomputer")
public class TileEntityMachineSuperComputer extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IControlReceiver, IGUIProvider {

	public FluidTankNTM inputTank;
	public FluidTankNTM outputTank;

	public long power;
	public long maxPower = 100_000;
	public boolean didProcess = false;

	public ModuleMachineSuperComputer computerModule;

	public TileEntityMachineSuperComputer() {
		super(8);
		this.inputTank = new FluidTankNTM(Fluids.NONE, 4_000);
		this.outputTank = new FluidTankNTM(Fluids.NONE, 4_000);

		this.computerModule = new ModuleMachineSuperComputer(0, this, inventory)
				.itemInput(2).itemOutput(5)
				.fluidInput(inputTank).fluidOutput(outputTank);
	}

	@Override
	public String getDefaultName() {
		return "container.machineSuperComputer";
	}

	@Override
	public void update() {

		if(maxPower <= 0) this.maxPower = 1_000_000;

		if(!world.isRemote) {

			GenericRecipe recipe = computerModule.getRecipe();
			if(recipe != null) {
				this.maxPower = recipe.power * 100;
			}
			this.maxPower = BobMathUtil.max(this.power, this.maxPower, 100_000);
			this.power = Library.chargeTEFromItems(inventory, 0, power, maxPower);

			for(DirPos conPos : getConPos()) {
				this.trySubscribe(world, conPos);
				if(inputTank.getTankType() != Fluids.NONE) this.trySubscribe(inputTank.getTankType(), world, conPos);
				if(outputTank.getFill() > 0) this.tryProvide(outputTank, world, conPos);
			}

			this.computerModule.update(1D, 1D, true, inventory.getStackInSlot(1));
			this.didProcess = this.computerModule.didProcess;
			if(this.computerModule.markDirty) this.markDirty();

			this.networkPackNT(100);
		}
	}

	public DirPos[] getConPos() {

		int meta = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
		ForgeDirection dir = ForgeDirection.getOrientation(meta - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		return new DirPos[] {
				new DirPos(pos.getX() + dir.offsetX * 9, pos.getY(), pos.getZ() + dir.offsetZ * 9, dir),
				new DirPos(pos.getX() + dir.offsetX * 7 + rot.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 7 + rot.offsetZ * 2, rot),
				new DirPos(pos.getX() + dir.offsetX * 7 - rot.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 7 - rot.offsetZ * 2, rot.getOpposite()),
				new DirPos(pos.getX() + dir.offsetX * 5 + rot.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 5 + rot.offsetZ * 2, rot),
				new DirPos(pos.getX() + dir.offsetX * 5 - rot.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 5 - rot.offsetZ * 2, rot.getOpposite()),
		};
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		this.inputTank.serialize(buf);
		this.outputTank.serialize(buf);
		buf.writeLong(power);
		buf.writeLong(maxPower);
		buf.writeBoolean(didProcess);
		this.computerModule.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.inputTank.deserialize(buf);
		this.outputTank.deserialize(buf);
		this.power = buf.readLong();
		this.maxPower = buf.readLong();
		this.didProcess = buf.readBoolean();
		this.computerModule.deserialize(buf);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.inputTank.readFromNBT(nbt, "i");
		this.outputTank.readFromNBT(nbt, "o");
		this.power = nbt.getLong("power");
		this.maxPower = nbt.getLong("maxPower");
		this.computerModule.readFromNBT(nbt);
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		this.inputTank.writeToNBT(nbt, "i");
		this.outputTank.writeToNBT(nbt, "o");
		nbt.setLong("power", power);
		nbt.setLong("maxPower", maxPower);
		this.computerModule.writeToNBT(nbt);
		return super.writeToNBT(nbt);
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot == 0 && stack.getItem() instanceof IBatteryItem) return true;
		if(slot == 1 && stack.getItem() == ModItems.blueprints) return true;
		return this.computerModule.isItemValid(slot, stack); // recipe input crap
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		return i >= 5 || this.computerModule.isSlotClogged(i);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing side) {
		return new int[] {2, 3, 4, 5, 6, 7};
	}

	@Override public long getPower() { return power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override public FluidTankNTM[] getReceivingTanks() { return new FluidTankNTM[] {inputTank}; }
	@Override public FluidTankNTM[] getSendingTanks() { return new FluidTankNTM[] {outputTank}; }
	@Override public FluidTankNTM[] getAllTanks() { return new FluidTankNTM[] {inputTank, outputTank}; }

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineSuperComputer(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineSuperComputer(player.inventory, this);
	}

	@Override public boolean hasPermission(EntityPlayer player) { return player.getDistanceSq(pos) < 100; }

	@Override
	public void receiveControl(EntityPlayerMP player, NBTTagCompound data) {
		if(data.hasKey("index") && data.hasKey("selection")) {
			int index = data.getInteger("index");
			String selection = data.getString("selection");
			if(index == 0) {
				this.computerModule.setRecipe(selection, false);
				this.markDirty();
			}
		}
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) bb = new AxisAlignedBB(pos.getX() - 8, pos.getY(), pos.getZ() - 8, pos.getX() + 9, pos.getY() + 9, pos.getZ() + 9);
		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
