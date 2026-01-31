package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.machine.ItemBreedingRod;
import com.hbm.tileentity.machine.TileEntityMachineReactorBreeding;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineReactorBreeding extends Container {

	private TileEntityMachineReactorBreeding reactor;

	public ContainerMachineReactorBreeding(InventoryPlayer invPlayer, TileEntityMachineReactorBreeding tedf) {

		reactor = tedf;

		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 35, 35));
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 1, 125, 35));

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 2,
                s -> s.getItem() instanceof ItemBreedingRod, 1);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return reactor.isUseableByPlayer(player);
	}
}
