package com.hbm.inventory.container;

import com.hbm.blocks.test.TestEventTester;
import com.hbm.blocks.test.TestEventTester.TileEntityTestStorage;
import com.hbm.inventory.slot.SlotNonRetarded;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerTestStorage extends Container {

	protected TileEntityTestStorage tile;

	public ContainerTestStorage(InventoryPlayer invPlayer, TileEntityTestStorage tile) {
		this.tile = tile;
		setupWithScroll(tile, invPlayer, 0);
	}

	@Override
	public ItemStack slotClick(int slotIndex, int button, ClickType mode, EntityPlayer player) {

		if(slotIndex == TestEventTester.SLOT_CLICK_ID_REFRESH) {
			this.setupWithScroll(tile, player.inventory, button);
			return ItemStack.EMPTY;
		}

		return super.slotClick(slotIndex, button, mode, player);
	}

	public void setupWithScroll(TileEntityTestStorage tile, InventoryPlayer invPlayer, int rowOffset) {
		this.inventorySlots.clear();
		this.inventoryItemStacks.clear();

		for(int row = 0; row < 6; row++) {
			for(int col = 0; col < 8; col++) {
				int index = col + (row + rowOffset) * 8;
				if(index >= tile.inventory.getSlots()) continue;
				this.addSlotToContainer(new SlotNonRetarded(tile.inventory, index, 8 + col * 18, 18 + row * 18));
			}
		}

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 140 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 198));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}
}
