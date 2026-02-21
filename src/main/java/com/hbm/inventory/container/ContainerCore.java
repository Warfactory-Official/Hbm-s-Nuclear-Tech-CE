package com.hbm.inventory.container;

import com.hbm.tileentity.machine.TileEntityCore;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerCore extends Container {

	private TileEntityCore core;

	public ContainerCore(InventoryPlayer invPlayer, TileEntityCore tedf) {

		core = tedf;

		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 80, 7));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 80, 39));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 80, 71));

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 122 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 180));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		return InventoryUtil.transferStack(this.inventorySlots, index, 3);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return core.isUseableByPlayer(player);
	}
}
