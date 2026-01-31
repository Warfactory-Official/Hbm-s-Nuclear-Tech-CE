package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineLargeTurbine;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineLargeTurbine extends Container {

	private TileEntityMachineLargeTurbine turbine;

	public ContainerMachineLargeTurbine(InventoryPlayer invPlayer, TileEntityMachineLargeTurbine tedf) {

		turbine = tedf;

		//Fluid ID
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 8, 17));
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 1, 8, 53));
		//Input IO
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 44, 17));
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 3, 44, 53));
		//Battery
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 4, 98, 53));
		//Output IO
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 152, 17));
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 6, 152, 53));

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
		return InventoryUtil.transferStack(this.inventorySlots, index, 7,
                s -> s.getItem() instanceof IItemFluidIdentifier, 2,
                s -> Library.isStackDrainableForTank(s, turbine.tanksNew[0]), 4,
                Library::isChargeableBattery, 5,
                s -> Library.isStackFillableForTank(s, turbine.tanksNew[1]), 7);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return turbine.isUseableByPlayer(player);
	}
}