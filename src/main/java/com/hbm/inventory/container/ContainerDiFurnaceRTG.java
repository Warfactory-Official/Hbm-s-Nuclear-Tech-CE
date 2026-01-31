package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.machine.ItemRTGPellet;
import com.hbm.tileentity.machine.TileEntityDiFurnaceRTG;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerDiFurnaceRTG extends Container {
	private TileEntityDiFurnaceRTG bFurnace;
	// private int progress;

	public ContainerDiFurnaceRTG(InventoryPlayer playerInv, TileEntityDiFurnaceRTG teIn) {
		bFurnace = teIn;
		// Input
		this.addSlotToContainer(new SlotItemHandler(teIn.inventory, 0, 80, 18));
		this.addSlotToContainer(new SlotItemHandler(teIn.inventory, 1, 80, 54));
		// Output
		this.addSlotToContainer(SlotFiltered.takeOnly(teIn.inventory, 2, 134, 36));
		// RTG pellets
		this.addSlotToContainer(new SlotItemHandler(teIn.inventory, 3, 22, 18));
		this.addSlotToContainer(new SlotItemHandler(teIn.inventory, 4, 40, 18));
		this.addSlotToContainer(new SlotItemHandler(teIn.inventory, 5, 22, 36));
		this.addSlotToContainer(new SlotItemHandler(teIn.inventory, 6, 40, 36));
		this.addSlotToContainer(new SlotItemHandler(teIn.inventory, 7, 22, 54));
		this.addSlotToContainer(new SlotItemHandler(teIn.inventory, 8, 40, 54));

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 142));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return bFurnace.isUseableByPlayer(player);
	}

	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		return InventoryUtil.transferStack(this.inventorySlots, index, 9,
                s -> !(s.getItem() instanceof ItemRTGPellet), 3,
                s -> s.getItem() instanceof ItemRTGPellet, 9);
    }
}
