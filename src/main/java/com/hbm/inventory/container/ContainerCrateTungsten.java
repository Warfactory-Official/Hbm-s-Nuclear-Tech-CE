package com.hbm.inventory.container;

import com.hbm.tileentity.machine.TileEntityCrateTungsten;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerCrateTungsten extends Container {

	// mlbv: do not rename this field, it's shadowed in one of bogosorter's mixin
	private final TileEntityCrateTungsten crate;

	public ContainerCrateTungsten(InventoryPlayer invPlayer, TileEntityCrateTungsten te) {
		crate = te;

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new SlotItemHandler(te.inventory, j + i * 9, 8 + j * 18, 18 + i * 18));
			}
		}

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 32 + i * 18 + (18 * 3)));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 90 + (18 * 3)));
		}
	}

	@Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, this.crate.inventory.getSlots());
    }

	@Override
	public boolean canInteractWith(@NotNull EntityPlayer player) {
		return crate.isUseableByPlayer(player);
	}

	@Override
	public @NotNull ItemStack slotClick(int slotId, int dragType, @NotNull ClickType clickTypeIn, @NotNull EntityPlayer player) {
		if (this.crate != null && !this.crate.boundItem.isEmpty()) {
			if (slotId >= 0 && slotId < this.inventorySlots.size()) {
				Slot slot = this.inventorySlots.get(slotId);
				if (slot != null && slot.inventory == player.inventory && slot.getSlotIndex() == player.inventory.currentItem) {
					return ItemStack.EMPTY;
				}
			}

			if (clickTypeIn == ClickType.SWAP && dragType == player.inventory.currentItem) {
				return ItemStack.EMPTY;
			}
		}

		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}
}