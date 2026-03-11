package com.hbm.inventory.container;

import com.hbm.tileentity.machine.rbmk.TileEntityRBMKRod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerRBMKRod extends Container {

	private final TileEntityRBMKRod rbmk;

	public ContainerRBMKRod(InventoryPlayer invPlayer, TileEntityRBMKRod tedf) {
		rbmk = tedf;

		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 80, 45));

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + 20));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142 + 20));
		}
	}

	@Override
	public @NotNull ItemStack slotClick(int slotId, int dragType, @NotNull ClickType clickTypeIn, @NotNull EntityPlayer player) {

		if(slotId == 0) {

			if(rbmk.coldEnoughForManual()) {
				return super.slotClick(slotId, dragType, clickTypeIn, player);
			} else {

				Slot slot = this.getSlot(slotId);
				ItemStack ret = ItemStack.EMPTY;

				if(slot != null && slot.getHasStack()) {
					ret = slot.getStack().copy();
				}

				return ret;
			}
		}

		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}


	@Override
	public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int par2) {
		ItemStack var3 = ItemStack.EMPTY;
		Slot var4 = this.inventorySlots.get(par2);

		if(var4 != null && var4.getHasStack()) {
			ItemStack var5 = var4.getStack();
			var3 = var5.copy();

			if(par2 <= rbmk.inventory.getSlots() - 1) {
				if(!rbmk.coldEnoughForManual()) return ItemStack.EMPTY;
				if(!this.mergeItemStack(var5, rbmk.inventory.getSlots(), this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if(!this.mergeItemStack(var5, 0, rbmk.inventory.getSlots(), false)) {
				return ItemStack.EMPTY;
			}

			if(var5.getCount() == 0) {
				var4.putStack(ItemStack.EMPTY);
			} else {
				var4.onSlotChanged();
			}
		}

		return var3;
	}

	@Override
	public boolean canInteractWith(@NotNull EntityPlayer player) {
		return rbmk.isUseableByPlayer(player);
	}
}