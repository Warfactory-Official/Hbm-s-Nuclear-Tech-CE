package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.ItemDrillbit;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineExcavator;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineExcavator extends Container {
	
	TileEntityMachineExcavator excavator;

	public ContainerMachineExcavator(InventoryPlayer invPlayer, TileEntityMachineExcavator tile) {
		this.excavator = tile;

		//Battery: 0
		this.addSlotToContainer(new SlotItemHandler(tile.inventory, 0, 220, 72));
		//Fluid ID: 1
		this.addSlotToContainer(new SlotItemHandler(tile.inventory, 1, 202, 72));
		//Upgrades: 2-4
		for(int i = 0; i < 3; i++) {
			this.addSlotToContainer(new SlotItemHandler(tile.inventory, 2 + i, 136 + i * 18, 75));
		}
		//Buffer: 5-13
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				this.addSlotToContainer(SlotFiltered.takeOnly(tile.inventory, 5 + j + i * 3, 136 + j * 18, 5 + i * 18));
			}
		}
		
		//Inventory
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 41 + j * 18, 122 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 41 + i * 18, 180));
		}
	}

	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
		return InventoryUtil.transferStack(this.inventorySlots, index, 14,
                Library::isBattery, 1,
                s -> s.getItem() instanceof IItemFluidIdentifier, 2,
                Library::isMachineUpgrade, 4,
                s -> s.getItem() instanceof ItemDrillbit, 5);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return excavator.isUseableByPlayer(player);
	}
}
