package com.hbm.inventory.container;

import com.hbm.tileentity.bomb.TileEntityBombMulti;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerBombMulti extends Container {

	private TileEntityBombMulti bombMulti;
	
	public ContainerBombMulti(InventoryPlayer invPlayer, TileEntityBombMulti tedf) {
		
		bombMulti = tedf;
		
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 44, 26));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 62, 26));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 80, 26));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 44, 44));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 62, 44));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 80, 44));
		
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
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		return InventoryUtil.transferStack(this.inventorySlots, index, 6);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return bombMulti.isUseableByPlayer(player);
	}

}