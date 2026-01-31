package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityITER;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerITER extends Container {

private TileEntityITER iter;

	public ContainerITER(InventoryPlayer invPlayer, TileEntityITER tedf) {

		iter = tedf;

		//Battery
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 0, 107, 108));
		//Breeder In
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 26, 18));
		//Breeder Out
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 2, 62, 18));
		//Plasma Shield
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 98, 18));
		//Byproduct
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 4, 134, 18));

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
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
		return InventoryUtil.transferStack(this.inventorySlots, index, 5,
                Library::isBattery, 1);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return iter.isUseableByPlayer(player);
	}
}