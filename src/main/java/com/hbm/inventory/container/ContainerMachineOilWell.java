package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.oil.TileEntityOilDrillBase;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineOilWell extends Container {

	private TileEntityOilDrillBase oilDrill;
	
	public ContainerMachineOilWell(InventoryPlayer invPlayer, TileEntityOilDrillBase tedf) {
		oilDrill = tedf;

		// Battery
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 0, 8, 53));
		// Canister Input
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 80, 17));
		// Canister Output
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 2, 80, 53));
		// Gas Input
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 125, 17));
		// Gas Output
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 4, 125, 53));
		//Upgrades
		this.addSlotToContainer(new SlotUpgrade(tedf.inventory, 5, 152, 17));
		this.addSlotToContainer(new SlotUpgrade(tedf.inventory, 6, 152, 35));
		this.addSlotToContainer(new SlotUpgrade(tedf.inventory, 7, 152, 53));
		
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
		return InventoryUtil.transferStack(this.inventorySlots, index, 8,
                Library::isBattery, 1,
                s -> Library.isStackFillableForTank(s, oilDrill.tanks[0]), 3,
                s -> Library.isStackFillableForTank(s, oilDrill.tanks[1]), 5,
                Library::isMachineUpgrade, 8);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return oilDrill.isUseableByPlayer(player);
	}
}
