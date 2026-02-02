package com.hbm.tileentity.machine.rbmk;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public abstract class TileEntityRBMKSlottedBase extends TileEntityRBMKActiveBase {

	public RBMKSlottedItemStackHandler inventory;

	public TileEntityRBMKSlottedBase(int scount) {
        inventory = new RBMKSlottedItemStackHandler(scount);
	}

	public int getGaugeScaled(int i, FluidTank tank) {
		return tank.getFluidAmount() * i / tank.getCapacity();
	}

	public void handleButtonPacket(int value, int meta) {
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if(!diag) {
			inventory.deserializeNBT(nbt.getCompoundTag("inventory"));
		}
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		
		if(!diag) {
			nbt.setTag("inventory", inventory.serializeNBT());
		}
		return nbt;
	}
	
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return true;
	}
	
	public boolean canInsertItem(int slot, ItemStack itemStack) {
		return this.isItemValidForSlot(slot, itemStack);
	}

	public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
		return true;
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory) : 
			super.getCapability(capability, facing);
	}

	public class RBMKSlottedItemStackHandler extends ItemStackHandler {
		public RBMKSlottedItemStackHandler(int scount) {
			super(scount);
		}

		@Override
		protected void onContentsChanged(int slot) {
			markDirty();
			super.onContentsChanged(slot);
		}

		@Override
		public boolean isItemValid(int slot, ItemStack itemStack) {
			return isItemValidForSlot(slot, itemStack);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if(canInsertItem(slot, stack))
				return super.insertItem(slot, stack, simulate);
			return stack;
		}

		public ItemStack insertItemUnchecked(int slot, ItemStack stack, boolean simulate) {
			return super.insertItem(slot, stack, simulate);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if(canExtractItem(slot, inventory.getStackInSlot(slot), amount))
				return super.extractItem(slot, amount, simulate);
			return ItemStack.EMPTY;
		}

		public ItemStack extractItemUnchecked(int slot, int amount, boolean simulate) {
			return super.extractItem(slot, amount, simulate);
		}
	}
}
