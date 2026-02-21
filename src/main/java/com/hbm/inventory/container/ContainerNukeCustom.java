package com.hbm.inventory.container;

import com.hbm.tileentity.bomb.TileEntityNukeCustom;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerNukeCustom extends Container {

	private TileEntityNukeCustom nukeBoy;
	
	public ContainerNukeCustom(InventoryPlayer invPlayer, TileEntityNukeCustom tedf) {
		
		nukeBoy = tedf;

		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 8, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 26, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 44, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 62, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 80, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 98, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 6, 116, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 7, 134, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 8, 152, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 9, 8, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 10, 26, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 11, 44, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 12, 62, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 13, 80, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 14, 98, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 15, 116, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 16, 134, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 17, 152, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 18, 8, 54));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 19, 26, 54));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 20, 44, 54));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 21, 62, 54));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 22, 80, 54));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 23, 98, 54));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 24, 116, 54));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 25, 134, 54));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 26, 152, 54));
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + 56));
			}
		}
		
		for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142 + 56));
		}
	}
	
	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
		return InventoryUtil.transferStack(this.inventorySlots, index, 27);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return nukeBoy.isUseableByPlayer(player);
	}
}