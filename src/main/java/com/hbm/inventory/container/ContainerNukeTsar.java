package com.hbm.inventory.container;

import com.hbm.tileentity.bomb.TileEntityNukeTsar;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerNukeTsar extends Container {

	private final TileEntityNukeTsar nukeTsar;
	
	public ContainerNukeTsar(InventoryPlayer invPlayer, TileEntityNukeTsar tedf) {
		
		nukeTsar = tedf;

		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 48, 101));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 66, 101));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 84, 101));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 102, 101));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 55, 51));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 138, 101));


		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, 9 + j + i * 9, 48 + j * 18, 151 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 48 + i * 18, 209));
		}
	}

	@Override
	public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index <= 5) {
				if (!this.mergeItemStack(itemstack1, 6, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}


	@Override
	public boolean canInteractWith(@NotNull EntityPlayer player) {
		return nukeTsar.isUseableByPlayer(player);
	}
}
