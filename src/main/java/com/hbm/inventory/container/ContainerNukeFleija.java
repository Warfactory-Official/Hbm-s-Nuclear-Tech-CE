package com.hbm.inventory.container;

import com.hbm.items.ModItems;
import com.hbm.tileentity.bomb.TileEntityNukeFleija;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerNukeFleija extends Container {

	private TileEntityNukeFleija nukeTsar;
	
	public ContainerNukeFleija(InventoryPlayer invPlayer, TileEntityNukeFleija tedf) {
		
		nukeTsar = tedf;

        //igniters
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 8, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 152, 36));
        //propellant
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 44, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 44, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 44, 54));
        //cores
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 80, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 6, 98, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 7, 80, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 8, 98, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 9, 80, 54));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 10, 98, 54));
		
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
		return InventoryUtil.transferStack(this.inventorySlots, index, 11,
                s -> s.getItem() == ModItems.fleija_igniter, 2,
                s -> s.getItem() == ModItems.fleija_propellant, 5,
                s -> s.getItem() == ModItems.fleija_core, 11);
    }
	
	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return nukeTsar.isUseableByPlayer(playerIn);
	}

}
