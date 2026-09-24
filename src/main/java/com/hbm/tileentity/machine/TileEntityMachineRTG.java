package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyProviderMK2;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerMachineRTG;
import com.hbm.inventory.gui.GUIMachineRTG;
import com.hbm.items.machine.ItemRTGPellet;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.RTGUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoRegister
public class TileEntityMachineRTG extends TileEntityLoadedBase implements ITickable, IEnergyProviderMK2, IGUIProvider {

	public ItemStackHandler inventory;

	public int heat;
	public final int heatMax = 6000;
	public long power;
	public final long maxPower = 1000000;
	private AxisAlignedBB bb;
	
	//private static final int[] slots_top = new int[] { 0 };
	//private static final int[] slots_bottom = new int[] { 0 };
	//private static final int[] slots_side = new int[] { 0 };
	
	private String customName;	
	
	public TileEntityMachineRTG() {
		inventory = new ItemStackHandler(15){
			@Override
			protected void onContentsChanged(int slot) {
				markDirty();
				super.onContentsChanged(slot);
			}
			
			@Override
			public boolean isItemValid(int slot, ItemStack itemStack) {
				if(itemStack != null && (itemStack.getItem() instanceof ItemRTGPellet))
					return true;
				return false;
			}
			@Override
			public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
				if(isItemValid(slot, stack))
					return super.insertItem(slot, stack, simulate);
				return stack;
			}

			//Alcater moment
//			public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
//				return !isItemValid(slot, itemStack);
//			}

//			@Override
//			public ItemStack extractItem(int slot, int amount, boolean simulate) {
//				if(canExtractItem(slot, inventory.getStackInSlot(slot), amount))
//					return super.extractItem(slot, amount, simulate);
//				return ItemStack.EMPTY;
//			}
		};
	}
	
	@Override
	public void update() {
		if(!world.isRemote)
		{
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
				this.tryProvide(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
			int[] slots = new int[inventory.getSlots()];
			for(int i = 0; i < inventory.getSlots();i++){
				slots[i] = i;
			}
			heat = RTGUtil.updateRTGs(inventory, slots);
			
			if(heat > heatMax)
				heat = heatMax;
			
			this.power += heat* 5L;
			if(this.power > maxPower)
				this.power = maxPower;
			
			
			detectAndSendChanges();
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		heat = compound.getInteger("heat");
		detectHeat = heat + 1;
		power = compound.getLong("power");
		detectPower = power + 1;
		if(compound.hasKey("inventory"))
			inventory.deserializeNBT(compound.getCompoundTag("inventory"));
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("heat", this.heat);
		compound.setLong("power", this.power);
		compound.setTag("inventory", inventory.serializeNBT());
		
		return super.writeToNBT(compound);
	}
	
	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}
	
	public int getHeatScaled(int i) {
		return (heat * i) / heatMax;
	}
	
	public boolean hasPower() {
		return this.power > 0;

	}
	
	public boolean hasHeat() {
		return heat > 0;
	}

	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "container.rtg";
	}

	public boolean hasCustomInventoryName() {
		return this.customName != null && this.customName.length() > 0;
	}
	
	public void setCustomName(String name) {
		this.customName = name;
	}
	
	public boolean isUseableByPlayer(EntityPlayer player) {
		if(world.getTileEntity(pos) != this)
		{
			return false;
		}else{
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <=64;
		}
	}

	@Override
	public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == CapabilityEnergy.ENERGY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
		}
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(
					new NTMEnergyCapabilityWrapper(this)
			);
		}
		return super.getCapability(capability, facing);
	}
	
	private int detectHeat;
	private long detectPower;
	
	private void detectAndSendChanges() {
		
		boolean mark = false;
		if(detectHeat != heat){
			mark = true;
			detectHeat = heat;
		}
		if(detectPower != power){
			mark = true;
			detectPower = power;
		}
		networkPackNT(10);
		if(mark)
			markDirty();
	}

	@Override
	public void serialize(ByteBuf buf){
		buf.writeLong(power);
	}

	@Override
	public void deserialize(ByteBuf buf){
		power = buf.readLong();
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
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineRTG(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineRTG(player.inventory, this);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (bb == null) bb = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
		return bb;
	}

}
