package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.lib.Library;
import com.hbm.tileentity.bomb.TileEntityCompactLauncher;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import java.util.function.Predicate;

public class ContainerCompactLauncher extends Container {

	private TileEntityCompactLauncher nukeBoy;
	
	public ContainerCompactLauncher(InventoryPlayer invPlayer, TileEntityCompactLauncher tedf) {
		
		nukeBoy = tedf;

		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 26, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 26, 72));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 116, 72));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 134, 72));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 152, 90));
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 5, 116, 108));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 6, 116, 90));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 7, 134, 90));
		
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
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		return InventoryUtil.transferStack(this.inventorySlots, index, 8,
                Predicate.not(Library::isBattery), 5,
                Library::isBattery, 6);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return nukeBoy.isUseableByPlayer(player);
	}
}
