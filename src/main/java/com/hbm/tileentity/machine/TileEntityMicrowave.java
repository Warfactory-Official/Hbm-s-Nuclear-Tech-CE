package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.ICopiable;
import com.hbm.inventory.container.ContainerMicrowave;
import com.hbm.inventory.gui.GUIMicrowave;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityMicrowave extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IGUIProvider, ICopiable {

	public long power;
	public static final long maxPower = 50000;
	public static final int consumption = 50;
	public static final int maxTime = 300;
	public int time;
	public int speed;
	public static final int maxSpeed = 5;
	
	public TileEntityMicrowave() {
		super(3);
	}
	
	@Override
	public String getDefaultName() {
		return "container.microwave";
	}
	
	@Override
	public void update() {
		if(!world.isRemote) {

			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
				this.trySubscribe(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
			this.power = Library.chargeTEFromItems(inventory, 2, power, maxPower);

			if(canProcess()) {

				if(speed >= maxSpeed) {
					world.destroyBlock(pos, false);
					world.newExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 7.5F, true, true);
					return;
				}

				if(time >= maxTime) {
					process();
					time = 0;
				}
				
				if(canProcess()) {
					power -= consumption;
					time += speed * 2;
				}
			}

			networkPackNT(50);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(time);
		buf.writeInt(speed);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		time = buf.readInt();
		speed = buf.readInt();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		power = compound.getLong("power");
		speed = compound.getInteger("speed");
		super.readFromNBT(compound);
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setLong("power", power);
		compound.setInteger("speed", speed);
		return super.writeToNBT(compound);
	}
	
	@Override
	public void handleButtonPacket(int value, int meta) {
		if(value == 0)
			speed++;

		if(value == 1)
			speed--;

		if(speed < 0)
			speed = 0;

		if(speed > maxSpeed)
			speed = maxSpeed;
	}
	
	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return i == 0 && !FurnaceRecipes.instance().getSmeltingResult(stack).isEmpty();
	}
	
	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
		return slot == 1;
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e) {
		return e.ordinal() == 0 ? new int[] { 1 } : new int[] { 0 };
	}
	
	private void process() {

		ItemStack stack = FurnaceRecipes.instance().getSmeltingResult(inventory.getStackInSlot(0)).copy();

		if(inventory.getStackInSlot(1).isEmpty()) {
			inventory.setStackInSlot(1, stack);
		} else {
			inventory.getStackInSlot(1).setCount(inventory.getStackInSlot(1).getCount() + stack.getCount());
		}

		inventory.getStackInSlot(0).shrink(1);

		this.markDirty();
	}

	private boolean canProcess() {

		if(speed  == 0)
			return false;

		if(power < consumption)
			return false;

		if(!FurnaceRecipes.instance().getSmeltingResult(inventory.getStackInSlot(0)).isEmpty()) {

			ItemStack stack = FurnaceRecipes.instance().getSmeltingResult(inventory.getStackInSlot(0));

			if(inventory.getStackInSlot(1).isEmpty())
				return true;

			if(!stack.isItemEqual(inventory.getStackInSlot(1)))
				return false;

			return stack.getCount() + inventory.getStackInSlot(1).getCount() <= stack.getMaxStackSize();
		}

		return false;
	}
	
	public long getPowerScaled(int i) {
		return (power * i) / maxPower;
	}

	public int getProgressScaled(int i) {
		return (time * i) / maxTime;
	}

	public int getSpeedScaled(int i) {
		return (speed * i) / maxSpeed;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
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
		return maxPower;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(
					new NTMEnergyCapabilityWrapper(this)
			);
		}
		return super.getCapability(capability, facing);
	}
	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMicrowave(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMicrowave(player.inventory, this);
	}

	@Override
	public NBTTagCompound getSettings(World world, int x, int y, int z) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("microSpeed", speed);
		return null;
	}

	@Override
	public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
		if(nbt.hasKey("microSpeed")) speed = nbt.getInteger("microSpeed");
	}

	@Override
	public String[] infoForDisplay(World world, int x, int y, int z) {
		return new String[]{ "copyTool.speed"};
	}
}
