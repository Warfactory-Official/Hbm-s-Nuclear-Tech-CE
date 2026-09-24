package com.hbm.tileentity.bomb;

import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerBombMulti;
import com.hbm.inventory.gui.GUIBombMulti;
import com.hbm.items.ModItems;
import com.hbm.tileentity.IGUIProvider;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

@AutoRegister
public class TileEntityBombMulti extends TileEntity implements IGUIProvider {

	public ItemStackHandler inventory;
	private String customName;
	
	public TileEntityBombMulti() {
		inventory = new ItemStackHandler(6){
			@Override
			protected void onContentsChanged(int slot) {
				markDirty();
				super.onContentsChanged(slot);
			}
		};
	}
	
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "container.bombMulti";
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
	
	public boolean isLoaded(){
		
		if(inventory.getStackInSlot(0).getItem() == Item.getItemFromBlock(Blocks.TNT) && 
				inventory.getStackInSlot(1).getItem() == Item.getItemFromBlock(Blocks.TNT) && 
				inventory.getStackInSlot(3).getItem() == Item.getItemFromBlock(Blocks.TNT) && 
				inventory.getStackInSlot(4).getItem() == Item.getItemFromBlock(Blocks.TNT))
		{
			return true;
		}
			
		return false;
	}
	
	public int return2type() {

		if(!inventory.getStackInSlot(2).isEmpty())
		{
		if(inventory.getStackInSlot(2).getItem() == Items.GUNPOWDER)
		{
			return 1;
		}
		
		if(inventory.getStackInSlot(2).getItem() == Item.getItemFromBlock(Blocks.TNT))
		{
			return 2;
		}
		
		if(inventory.getStackInSlot(2).getItem() == ModItems.pellet_cluster)
		{
			return 3;
		}
		
		if(inventory.getStackInSlot(2).getItem() == ModItems.powder_fire)
		{
			return 4;
		}
		
		if(inventory.getStackInSlot(2).getItem() == ModItems.powder_poison)
		{
			return 5;
		}
		
		if(inventory.getStackInSlot(2).getItem() == ModItems.pellet_gas)
		{
			return 6;
		}
		}
		return 0;
	}
	
	public int return5type() {
		
		if(!inventory.getStackInSlot(5).isEmpty())
		{
		if(inventory.getStackInSlot(5).getItem() == Items.GUNPOWDER)
		{
			return 1;
		}
		
		if(inventory.getStackInSlot(5).getItem() == Item.getItemFromBlock(Blocks.TNT))
		{
			return 2;
		}
		
		if(inventory.getStackInSlot(5).getItem() == ModItems.pellet_cluster)
		{
			return 3;
		}
		
		if(inventory.getStackInSlot(5).getItem() == ModItems.powder_fire)
		{
			return 4;
		}
		
		if(inventory.getStackInSlot(5).getItem() == ModItems.powder_poison)
		{
			return 5;
		}
		
		if(inventory.getStackInSlot(5).getItem() == ModItems.pellet_gas)
		{
			return 6;
		}
		}
		return 0;
	}
	
	public void clearSlots() {
		for(int i = 0; i < inventory.getSlots(); i++)
		{
			inventory.setStackInSlot(i, ItemStack.EMPTY);
		}
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
		return new ContainerBombMulti(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIBombMulti(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
