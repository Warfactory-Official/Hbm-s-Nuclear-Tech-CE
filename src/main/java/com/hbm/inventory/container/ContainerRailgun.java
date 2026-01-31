package com.hbm.inventory.container;

import com.hbm.api.item.IDesignatorItem;
import com.hbm.inventory.slot.SlotBattery;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.tileentity.bomb.TileEntityRailgun;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerRailgun extends Container {

	private TileEntityRailgun diFurnace;
	
	public ContainerRailgun(InventoryPlayer invPlayer, TileEntityRailgun tedf) {
		
		diFurnace = tedf;

		//battery
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 0, 17, 17));
		//targeter
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 53, 17));
		//ammo
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 134, 17));
		
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
		return InventoryUtil.transferStack(this.inventorySlots, index, 3,
                Library::isBattery, 1,
                s -> s.getItem() instanceof IDesignatorItem, 2,
                s -> s.getItem() == ModItems.charge_railgun, 3
        );
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return diFurnace.isUseableByPlayer(player);
	}
}
