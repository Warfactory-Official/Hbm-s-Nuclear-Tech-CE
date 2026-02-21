package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.tileentity.machine.TileEntityMachineMissileAssembly;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineMissileAssembly extends Container {

	private TileEntityMachineMissileAssembly missileAssembly;
	
	public ContainerMachineMissileAssembly(InventoryPlayer invPlayer, TileEntityMachineMissileAssembly tedf) {
		
		missileAssembly = tedf;

		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 8, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 26, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 44, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 62, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 80, 36));
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 5, 152, 36));
		
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
		return InventoryUtil.transferStack(this.inventorySlots, index, 6);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return missileAssembly.isUseableByPlayer(player);
	}
}
