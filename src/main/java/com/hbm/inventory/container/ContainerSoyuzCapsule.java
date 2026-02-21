package com.hbm.inventory.container;

import com.hbm.tileentity.machine.TileEntitySoyuzCapsule;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerSoyuzCapsule extends Container {
	
	private TileEntitySoyuzCapsule capsule;
	
	public ContainerSoyuzCapsule(InventoryPlayer invPlayer, TileEntitySoyuzCapsule tedf) {
		capsule = tedf;
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 6; j++)
			{
				this.addSlotToContainer(new SlotItemHandler(tedf.inventory, j + i * 6, 8 + j * 18 + 18 * 2, 17 + i * 18));
			}
		}
		
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 18, 8, 35));
		
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
		return InventoryUtil.transferStack(this.inventorySlots, index, capsule.inventory.getSlots());
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return capsule.isUseableByPlayer(player);
	}
}
