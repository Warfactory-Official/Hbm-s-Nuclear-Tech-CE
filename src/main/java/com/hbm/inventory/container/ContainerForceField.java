package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityForceField;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerForceField extends Container {
	
	private TileEntityForceField diFurnace;
	
	public ContainerForceField(InventoryPlayer invPlayer, TileEntityForceField tedf) {
		
		diFurnace = tedf;

		//Battery
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 0, 26, 53));
		//Range up
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 89, 35));
		//Health up
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 107, 35));
		
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
		return InventoryUtil.transferStack(this.inventorySlots, index, 3,
                Library::isBattery, 1,
                s -> s.getItem() == ModItems.upgrade_radius, 2,
                s -> s.getItem() == ModItems.upgrade_health, 3);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return diFurnace.isUseableByPlayer(player);
	}
}