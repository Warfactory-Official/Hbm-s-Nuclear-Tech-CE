package com.hbm.inventory.container;

import com.hbm.items.ISatChip;
import com.hbm.tileentity.machine.TileEntityMachineSatDock;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineSatDock extends Container {
	
	private TileEntityMachineSatDock dock;
	
	public ContainerMachineSatDock(InventoryPlayer invPlayer, TileEntityMachineSatDock tedf) {
		
		dock = tedf;
        IItemHandler inventory = tedf.getCheckedInventory();

		//Storage
		this.addSlotToContainer(new SlotItemHandler(inventory, 0, 62, 17));
		this.addSlotToContainer(new SlotItemHandler(inventory, 1, 80, 17));
		this.addSlotToContainer(new SlotItemHandler(inventory, 2, 98, 17));
		this.addSlotToContainer(new SlotItemHandler(inventory, 3, 116, 17));
		this.addSlotToContainer(new SlotItemHandler(inventory, 4, 134, 17));
		this.addSlotToContainer(new SlotItemHandler(inventory, 5, 62, 35));
		this.addSlotToContainer(new SlotItemHandler(inventory, 6, 80, 35));
		this.addSlotToContainer(new SlotItemHandler(inventory, 7, 98, 35));
		this.addSlotToContainer(new SlotItemHandler(inventory, 8, 116, 35));
		this.addSlotToContainer(new SlotItemHandler(inventory, 9, 134, 35));
		this.addSlotToContainer(new SlotItemHandler(inventory, 10, 62, 53));
		this.addSlotToContainer(new SlotItemHandler(inventory, 11, 80, 53));
		this.addSlotToContainer(new SlotItemHandler(inventory, 12, 98, 53));
		this.addSlotToContainer(new SlotItemHandler(inventory, 13, 116, 53));
		this.addSlotToContainer(new SlotItemHandler(inventory, 14, 134, 53));
		//Chip
		this.addSlotToContainer(new SlotItemHandler(inventory, 15, 26, 35));
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}
		
		for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142));
		}
	}
	
	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
		return InventoryUtil.transferStack(this.inventorySlots, index, 16,
                s -> !(s.getItem() instanceof ISatChip), 15,
                s -> s.getItem() instanceof ISatChip, 16);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return dock.isUseableByPlayer(player);
	}
}
