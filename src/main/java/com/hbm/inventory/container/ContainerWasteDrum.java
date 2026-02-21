package com.hbm.inventory.container;

import com.hbm.tileentity.machine.TileEntityWasteDrum;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerWasteDrum extends Container {
	
	private TileEntityWasteDrum drum;
	
	public ContainerWasteDrum(InventoryPlayer invPlayer, TileEntityWasteDrum tedf) {
		drum = tedf;

		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 71, 21));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 89, 21));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 53, 39));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 71, 39));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 89, 39));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 107, 39));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 6, 53, 57));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 7, 71, 57));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 8, 89, 57));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 9, 107, 57));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 10, 71, 75));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 11, 89, 75));
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 92 + i * 18 + 20));
			}
		}
		
		for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 170));
		}
	}
	
	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
		return InventoryUtil.transferStack(this.inventorySlots, index, drum.inventory.getSlots());
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return drum.isUseableByPlayer(player);
	}
}
