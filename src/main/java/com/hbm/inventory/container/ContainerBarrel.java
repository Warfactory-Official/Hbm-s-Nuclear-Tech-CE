package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityBarrel;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerBarrel extends Container {

	private TileEntityBarrel barrel;
	private int mode;

	public ContainerBarrel(InventoryPlayer invPlayer, TileEntityBarrel tedf) {
		mode = 0;

		barrel = tedf;

		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 8, 17));
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 1, 8, 53));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 53 - 18, 17));
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 3, 53 - 18, 53));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 125, 17));
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 5, 125, 53));

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142));
		}
	}

	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);
		listener.sendWindowProperty(this, 0, barrel.mode);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 6,
                s -> s.getItem() instanceof IItemFluidIdentifier, 2,
                s -> Library.isStackDrainableForTank(s, barrel.tankNew), 4,
                s -> Library.isStackFillableForTank(s, barrel.tankNew), 6);
	}

	@Override
	public void detectAndSendChanges() {
		for(IContainerListener listener : this.listeners) {
			if(this.mode != barrel.mode) {
				listener.sendWindowProperty(this, 0, barrel.mode);
				this.mode = barrel.mode;
			}
		}
		super.detectAndSendChanges();
	}
	
	@Override
	public void updateProgressBar(int id, int data) {
		if(id == 0)
			barrel.mode = (short) data;
		super.updateProgressBar(id, data);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return barrel.isUseableByPlayer(player);
	}
}
