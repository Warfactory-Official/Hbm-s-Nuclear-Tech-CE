package com.hbm.tileentity.bomb;

import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerNukeSolinium;
import com.hbm.inventory.gui.GUINukeSolinium;
import com.hbm.items.ModItems;
import com.hbm.tileentity.IGUIProvider;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.UUID;

@AutoRegister
public class TileEntityNukeSolinium extends TileEntity implements IGUIProvider {

	public ItemStackHandler inventory;
	public UUID placerID;
    private String customName;
	
	public TileEntityNukeSolinium() {
		inventory = new ItemStackHandler(9){
			@Override
			protected void onContentsChanged(int slot) {
				markDirty();
				super.onContentsChanged(slot);
			}
		};
	}
	
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "container.nukeSolinium";
	}

	public boolean hasCustomInventoryName() {
		return this.customName != null && !this.customName.isEmpty();
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
		if(compound.hasKey("placer"))
			placerID = compound.getUniqueId("placer");
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("inventory", inventory.serializeNBT());
		if (placerID != null)
			compound.setUniqueId("placer", placerID);
		return super.writeToNBT(compound);
	}
	
	public boolean isReady() {
		
			if(inventory.getStackInSlot(0).getItem() == ModItems.solinium_igniter && 
			inventory.getStackInSlot(1).getItem() == ModItems.solinium_propellant && 
			inventory.getStackInSlot(2).getItem() == ModItems.solinium_propellant && 
			inventory.getStackInSlot(3).getItem() == ModItems.solinium_igniter && 
			inventory.getStackInSlot(4).getItem() == ModItems.solinium_core && 
			inventory.getStackInSlot(5).getItem() == ModItems.solinium_igniter && 
			inventory.getStackInSlot(6).getItem() == ModItems.solinium_propellant && 
			inventory.getStackInSlot(7).getItem() == ModItems.solinium_propellant && 
			inventory.getStackInSlot(8).getItem() == ModItems.solinium_igniter)
			{
				return true;
			}
		
		return false;
	}
	
	public void clearSlots() {
		for(int i = 0; i < inventory.getSlots(); i++)
		{
			inventory.setStackInSlot(i, ItemStack.EMPTY);
		}
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
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory) : super.getCapability(capability, facing);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerNukeSolinium(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUINukeSolinium(player.inventory, this);
	}
}
