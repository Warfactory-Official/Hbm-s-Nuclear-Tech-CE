package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMicrowave;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import java.util.function.Predicate;

public class ContainerMicrowave extends Container {

    private TileEntityMicrowave microwave;

	public ContainerMicrowave(InventoryPlayer invPlayer, TileEntityMicrowave tedf) {

		microwave = tedf;

		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 80, 35));
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 1, 140, 35));
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 2, 8, 53));

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
                Predicate.not(Library::isBattery), 2);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return microwave.isUseableByPlayer(player);
	}
}