package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineWoodBurner;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineWoodBurner extends Container {
	
	protected TileEntityMachineWoodBurner burner;
	
	public ContainerMachineWoodBurner(InventoryPlayer Playerinv, TileEntityMachineWoodBurner burner) {
		this.burner = burner;
		//this.burner.openInventory();

		//Fuel
		this.addSlotToContainer(new SlotItemHandler(burner.inventory, 0, 26, 18));
		//Ashes
		this.addSlotToContainer(SlotFiltered.takeOnly(burner.inventory, 1, 26, 54));
		//Fluid ID
		this.addSlotToContainer(new SlotItemHandler(burner.inventory, 2, 98, 54));
		//Fluid Container
		this.addSlotToContainer(new SlotItemHandler(burner.inventory, 3, 98, 18));
		this.addSlotToContainer(SlotFiltered.takeOnly(burner.inventory, 4, 98, 36));
		//Battery
		this.addSlotToContainer(new SlotBattery(burner.inventory, 5, 143, 54));
		
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(Playerinv, j + i * 9 + 9, 8 + j * 18, 104 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(Playerinv, i, 8 + i * 18, 162));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		return InventoryUtil.transferStack(this.inventorySlots, index, 6,
                TileEntityFurnace::isItemFuel, 2,
                s -> s.getItem() instanceof IItemFluidIdentifier, 3,
                s -> Library.isStackDrainableForTank(s, burner.tank), 5,
                Library::isChargeableBattery, 6);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return burner.isUseableByPlayer(player);
	}
/*
	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		this.burner.closeInventory();
	}
 */
}
