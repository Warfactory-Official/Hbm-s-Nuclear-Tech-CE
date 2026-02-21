package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.oil.TileEntityMachineRefinery;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineRefinery extends Container {

    private TileEntityMachineRefinery refinery;
	
	public ContainerMachineRefinery(InventoryPlayer invPlayer, TileEntityMachineRefinery tedf) {
		refinery = tedf;

		//Battery
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 0, 186, 72));
		//Canister Input
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 8, 99));
		//Canister Output
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 2, 8, 119));
		//Heavy Oil Input
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 86, 99));
		//Heavy Oil Output
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 4, 86, 119));
		//Naphtha Input
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 106, 99));
		//Naphtha Output
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 6, 106, 119));
		//Light Oil Input
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 7, 126, 99));
		//Light Oil Output
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 8, 126, 119));
		//Petroleum Input
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 9, 146, 99));
		//Petroleum Output
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 10, 146, 119));
		//Sulfur Output
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 11, 58, 119));
		//Fluid ID
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 12, 186, 106));

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 150 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 208));
		}
	}
	
	@Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
		return InventoryUtil.transferStack(this.inventorySlots, index, 13,
                Library::isBattery, 1,
                s -> Library.isStackDrainableForTank(s, refinery.tanks[0]), 3,
                s -> Library.isStackFillableForTank(s, refinery.tanks[1]), 5,
                s -> Library.isStackFillableForTank(s, refinery.tanks[2]), 7,
                s -> Library.isStackFillableForTank(s, refinery.tanks[3]), 9,
                s -> Library.isStackFillableForTank(s, refinery.tanks[4]), 11);
    }

	@Override
	public boolean canInteractWith(@NotNull EntityPlayer player) {
		return refinery.isUseableByPlayer(player);
	}
}
