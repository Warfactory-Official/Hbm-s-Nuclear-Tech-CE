package com.hbm.inventory.container;

import com.hbm.inventory.TransferStrategy;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineUF6Tank;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineUF6Tank extends Container {

	private TileEntityMachineUF6Tank tank;

	private final TransferStrategy transferStrategy = TransferStrategy.builder(4)
                                                                      .rule(0, 2, s -> Library.isStackDrainableForTank(s, tank.tank))
                                                                      .rule(2, 4, s -> Library.isStackFillableForTank(s, tank.tank))
                                                                      .build();

	public ContainerMachineUF6Tank(InventoryPlayer invPlayer, TileEntityMachineUF6Tank tedf) {
		tank = tedf;

		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 44, 17));
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 1, 44, 53));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 116, 17));
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 3, 116, 53));

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
		return InventoryUtil.transferStack(this.inventorySlots, index, this.transferStrategy, player);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return tank.isUseableByPlayer(player);
	}
}
