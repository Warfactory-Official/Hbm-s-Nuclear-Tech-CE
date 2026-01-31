package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityHadron;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import java.util.function.Predicate;

public class ContainerHadron extends Container {

	private TileEntityHadron hadron;

	public ContainerHadron(InventoryPlayer invPlayer, TileEntityHadron tedf) {

		hadron = tedf;

		//Inputs
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 17, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 35, 36));
		//Outputs
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 2, 125, 36));
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 3, 143, 36));
		//Battery
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 4, 44, 108));

		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + (18 * 3) + 2));
			}
		}

		for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142 + (18 * 3) + 2));
		}
	}

	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
		return InventoryUtil.transferStack(this.inventorySlots, index, 5,
                Predicate.not(Library::isBattery), 4,
                Library::isBattery, 5);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return hadron.isUseableByPlayer(player);
	}
}