package com.hbm.tileentity.machine;

import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerMachineSatLinker;
import com.hbm.inventory.gui.GUIMachineSatLinker;
import com.hbm.items.ISatChip;
import com.hbm.lib.ItemStackHandlerWrapper;
import com.hbm.saveddata.satellites.SatelliteSavedData;
import com.hbm.tileentity.IGUIProvider;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityMachineSatLinker extends TileEntity implements ITickable, IGUIProvider {

	public ItemStackHandler inventory;
	
	//public static final int maxFill = 64 * 3;

	//private static final int[] slots_top = new int[] {0};
	//private static final int[] slots_bottom = new int[] {1};
	//private static final int[] slots_side = new int[] {2};
	
	private String customName;
	
	public TileEntityMachineSatLinker() {
		inventory = new ItemStackHandler(3){
			@Override
			protected void onContentsChanged(int slot) {
				super.onContentsChanged(slot);
				markDirty();
			}

			@Override
			public boolean isItemValid(int slot, @NotNull ItemStack stack) {
				return stack.getItem() instanceof ISatChip;
			}
		};
	}
	
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "container.satLinker";
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
	public void readFromNBT(NBTTagCompound compound) {
		if(compound.hasKey("inventory"))
			inventory.deserializeNBT(compound.getCompoundTag("inventory"));
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("inventory", inventory.serializeNBT());
		return super.writeToNBT(compound);
	}
	
	@Override
	public void update() {
		if(!world.isRemote)
		{
			if(inventory.getStackInSlot(0).getItem() instanceof ISatChip && inventory.getStackInSlot(1).getItem() instanceof ISatChip) {
				ISatChip.setFreqS(inventory.getStackInSlot(1), ISatChip.getFreqS(inventory.getStackInSlot(0)));
			}
			if(inventory.getStackInSlot(2).getItem() instanceof ISatChip){
				SatelliteSavedData satelliteData = SatelliteSavedData.getData(world);
				int newId = world.rand.nextInt(100000);
				if(!satelliteData.isFreqTaken(newId)) {
					ISatChip.setFreqS(inventory.getStackInSlot(2), newId);
				}
			}
		}
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {

		if(capability != CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return super.getCapability(capability, facing);
		if(facing == null) return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);

		// only the copy slot is reachable from the outside, and only once it carries the source frequency
		return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new ItemStackHandlerWrapper(inventory, new int[] { 1 }) {
			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate) {
				if(ISatChip.getFreqS(handle.getStackInSlot(0)) != ISatChip.getFreqS(handle.getStackInSlot(1))) return ItemStack.EMPTY;
				return super.extractItem(slot, amount, simulate);
			}
		});
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineSatLinker(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineSatLinker(player.inventory, this);
	}
}
