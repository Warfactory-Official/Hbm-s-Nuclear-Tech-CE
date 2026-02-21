package com.hbm.tileentity.bomb;

import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerNukeFleija;
import com.hbm.inventory.gui.GUINukeFleija;
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
public class TileEntityNukeFleija extends TileEntity implements IGUIProvider {

	public UUID placerID;

	public ItemStackHandler inventory = new ItemStackHandler(11){
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			markDirty();
		};
	};
	private String customName;

	public boolean isUseableByPlayer(EntityPlayer player) {
		if (world.getTileEntity(pos) != this) {
			return false;
		} else {
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound tag = inventory.serializeNBT();
		compound.setTag("inventory", tag);
		if (placerID != null)
			compound.setUniqueId("placer", placerID);
		return super.writeToNBT(compound);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("inventory"))
			inventory.deserializeNBT(compound.getCompoundTag("inventory"));
		if (compound.hasUniqueId("placer"))
			placerID = compound.getUniqueId("placer");
		super.readFromNBT(compound);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? true
				: super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
				? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory)
				: super.getCapability(capability, facing);
	}

	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "container.nukeFleija";
	}

	public boolean hasCustomInventoryName() {
		return this.customName != null && this.customName.length() > 0;
	}

	public void setCustomName(String name) {
		this.customName = name;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	public boolean isReady() {

        return inventory.getStackInSlot(0).getItem() == ModItems.fleija_igniter
                && inventory.getStackInSlot(1).getItem() == ModItems.fleija_igniter
                && inventory.getStackInSlot(2).getItem() == ModItems.fleija_propellant && inventory.getStackInSlot(3).getItem() == ModItems.fleija_propellant
                && inventory.getStackInSlot(4).getItem() == ModItems.fleija_propellant && inventory.getStackInSlot(5).getItem() == ModItems.fleija_core
                && inventory.getStackInSlot(6).getItem() == ModItems.fleija_core && inventory.getStackInSlot(7).getItem() == ModItems.fleija_core
                && inventory.getStackInSlot(8).getItem() == ModItems.fleija_core && inventory.getStackInSlot(9).getItem() == ModItems.fleija_core
                && inventory.getStackInSlot(10).getItem() == ModItems.fleija_core;
    }

	public void clearSlots() {
		for (int i = 0; i < inventory.getSlots(); i++) {
			inventory.setStackInSlot(i, ItemStack.EMPTY);
		}
	}

	@Override
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerNukeFleija(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUINukeFleija(player.inventory, this);
	}

}
