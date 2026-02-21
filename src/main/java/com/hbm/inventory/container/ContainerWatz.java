package com.hbm.inventory.container;

import com.hbm.tileentity.machine.TileEntityWatz;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerWatz extends Container {
	
	protected TileEntityWatz watz;
	
	public ContainerWatz(InventoryPlayer invPlayer, TileEntityWatz tedf) {
		watz = tedf;

		int index = 0;
		for(int j = 0; j < 6; j++) {
			for(int i = 0; i < 6; i++) {

				if(i + j > 1 && i + j < 9 && 5 - i + j > 1 && i + 5 - j > 1) {
					this.addSlotToContainer(new SlotItemHandler(watz.inventory, index, 17 + i * 18, 8 + j * 18));
					index++;
				}
			}
		}
		
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 147 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 205));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		return InventoryUtil.transferStack(this.inventorySlots, index, watz.inventory.getSlots());
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return watz.isUseableByPlayer(player);
	}
}
