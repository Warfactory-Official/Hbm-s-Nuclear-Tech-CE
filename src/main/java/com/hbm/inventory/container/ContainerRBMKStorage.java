package com.hbm.inventory.container;

import com.hbm.tileentity.machine.rbmk.TileEntityRBMKStorage;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerRBMKStorage extends Container {

	private TileEntityRBMKStorage rbmk;

	public ContainerRBMKStorage(InventoryPlayer invPlayer, TileEntityRBMKStorage tedf) {
		rbmk = tedf;

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 4; j++) {
				this.addSlotToContainer(new SlotItemHandler(tedf.inventory, i + j * 3, 32 + 32 * j, 29 + 16 * i));
			}
		}

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
		return InventoryUtil.transferStack(this.inventorySlots, index, 12);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return rbmk.isUseableByPlayer(player);
	}
}
