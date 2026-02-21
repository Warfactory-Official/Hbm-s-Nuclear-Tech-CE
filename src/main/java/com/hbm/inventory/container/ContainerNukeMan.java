package com.hbm.inventory.container;

import com.hbm.items.ModItems;
import com.hbm.tileentity.bomb.TileEntityNukeMan;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerNukeMan extends Container {

	private TileEntityNukeMan nukeMan;
	
	public ContainerNukeMan(InventoryPlayer invPlayer, TileEntityNukeMan tedf) {

		nukeMan = tedf;

		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 26, 35));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 8, 17));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 44, 17));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 8, 53));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 44, 53));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 98, 35));

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 6,
                s -> s.getItem() == ModItems.man_igniter, 1,
                s -> s.getItem() == ModItems.early_explosive_lenses, 5,
                s -> s.getItem() == ModItems.man_core, 6);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return nukeMan.isUseableByPlayer(player);
	}
}
