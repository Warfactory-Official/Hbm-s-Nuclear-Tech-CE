package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.items.machine.ItemFELCrystal;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityFEL;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerFEL extends Container {

	private TileEntityFEL fel;

	public ContainerFEL(InventoryPlayer invPlayer, TileEntityFEL tedf) {

		fel = tedf;

		//battery
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 0, 182, 144));
		//laser crystal
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 141, 23));

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 83 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 141));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		return InventoryUtil.transferStack(this.inventorySlots, index, 2,
                Library::isBattery, 1,
                s -> s.getItem() instanceof ItemFELCrystal, 2);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return fel.isUseableByPlayer(player);
	}
}