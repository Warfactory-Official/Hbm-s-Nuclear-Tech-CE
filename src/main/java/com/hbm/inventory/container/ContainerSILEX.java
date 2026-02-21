package com.hbm.inventory.container;

import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.TileEntitySILEX;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerSILEX extends Container {

	private TileEntitySILEX silex;

	public ContainerSILEX(InventoryPlayer invPlayer, TileEntitySILEX te) {
		silex = te;

		//Input
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 0, 80, 12));
		//Fluid Id
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 1, 8, 24));
		//Fluid Container
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 2, 8 + 18, 24));
		this.addSlotToContainer(SlotFiltered.takeOnly(te.inventory, 3, 8 + 18*2, 24));
		//Output
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 4, 116, 90));
		//Output Queue
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 5, 134, 72));
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 6, 152, 72));
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 7, 134, 90));
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 8, 152, 90));
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 9, 134, 108));
		this.addSlotToContainer(new SlotItemHandler(te.inventory, 10, 152, 108));
		
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
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 11,
                s -> !(s.getItem() instanceof IItemFluidIdentifier) && FluidContainerRegistry.getFluidContent(s, silex.tank.getTankType()) == 0, 1,
                s -> s.getItem() instanceof IItemFluidIdentifier, 2,
                s -> FluidContainerRegistry.getFluidContent(s, silex.tank.getTankType()) > 0, 4,
                _ -> false, 11
        );
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return silex.isUseableByPlayer(player);
	}
}