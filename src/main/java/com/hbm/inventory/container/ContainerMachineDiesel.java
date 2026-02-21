package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineDiesel;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineDiesel extends Container {
	
	private TileEntityMachineDiesel generator;
	
	public ContainerMachineDiesel(InventoryPlayer invPlayer, TileEntityMachineDiesel tedf) {
		
		generator = tedf;
		
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 44, 17));
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 1, 44, 53));
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 2, 116, 53));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 8, 17));
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 4, 8, 53));
		
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
		return InventoryUtil.transferStack(this.inventorySlots, index, 5,
                s -> Library.isStackDrainableForTank(s, generator.tank), 2,
                Library::isChargeableBattery, 3,
                s -> s.getItem() instanceof IItemFluidIdentifier, 5);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return generator.isUseableByPlayer(player);
	}
}
