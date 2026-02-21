package com.hbm.inventory.container;

import com.hbm.tileentity.machine.rbmk.TileEntityRBMKHeater;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerRBMKHeater extends Container {

	private TileEntityRBMKHeater rbmk;

	public ContainerRBMKHeater(InventoryPlayer invPlayer, TileEntityRBMKHeater tile) {
		rbmk = tile;

		this.addSlotToContainer(new SlotItemHandler(tile.inventory, 0, 41, 45));

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
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		return InventoryUtil.transferStack(this.inventorySlots, index, 1);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return rbmk.isUseableByPlayer(player);
	}
}
