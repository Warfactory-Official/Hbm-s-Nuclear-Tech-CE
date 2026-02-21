package com.hbm.inventory.container;

import com.hbm.tileentity.machine.TileEntityStorageDrum;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerStorageDrum extends Container {

	private TileEntityStorageDrum drum;

	public ContainerStorageDrum(InventoryPlayer invPlayer, TileEntityStorageDrum drum) {
		this.drum = drum;

		int index = 0;
		for(int j = 0; j < 6; j++) {
			for(int i = 0; i < 6; i++) {

				if(i + j > 1 && i + j < 9 && 5 - i + j > 1 && i + 5 - j > 1) {
					this.addSlotToContainer(new SlotItemHandler(drum.inventory, index, 35 + i * 18, 24 + j * 18));
					index++;
				}
			}
		}

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 155 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 213));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		return InventoryUtil.transferStack(this.inventorySlots, index, drum.inventory.getSlots());
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return drum.isUseableByPlayer(player);
	}
}