package com.hbm.inventory.container;

import com.hbm.items.ModItems;
import com.hbm.tileentity.bomb.TileEntityNukeN2;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerNukeN2 extends Container {

	private TileEntityNukeN2 nukeSol;
	
	public ContainerNukeN2(InventoryPlayer invPlayer, TileEntityNukeN2 tedf) {
		
		nukeSol = tedf;
		
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 98, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 116, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 134, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 98, 54));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 116, 54));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 134, 54));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 6, 98, 72));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 7, 116, 72));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 8, 134, 72));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 9, 98, 90));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 10, 116, 90));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 11, 134, 90));
		
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
        return InventoryUtil.transferStack(this.inventorySlots, index, 12,
                s -> s.getItem() == ModItems.n2_charge, 12);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return nukeSol.isUseableByPlayer(player);
	}
}
