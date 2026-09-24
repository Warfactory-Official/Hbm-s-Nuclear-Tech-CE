package com.hbm.inventory.container;

import com.hbm.inventory.TransferStrategy;
import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.oil.TileEntityOilDrillBase;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineOilWell extends Container {

	private TileEntityOilDrillBase oilDrill;

    private final TransferStrategy transferStrategy = TransferStrategy.builder(8)
                                                                      .rule(0, 1, Library::isBattery)
                                                                      .rule(1, 3, s -> Library.isStackFillableForTank(s,
                                                                              oilDrill.tanks[0]))
                                                                      .rule(3, 5, s -> Library.isStackFillableForTank(s,
                                                                              oilDrill.tanks[1]))
                                                                      .rule(5, 7, Library::isMachineUpgrade)
                                                                      .build();

    public ContainerMachineOilWell(InventoryPlayer invPlayer, TileEntityOilDrillBase tedf) {
		oilDrill = tedf;

		// Battery
		this.addSlotToContainer(new SlotBattery(tedf.inventory, 0, 8, 58));
		// Canister Input
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 94, 22));
		// Canister Output
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 2, 94, 58));
		// Gas Input
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 130, 22));
		// Gas Output
		this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 4, 130, 58));
		//Upgrades
		this.addSlotToContainer(new SlotUpgrade(tedf.inventory, 5, 156, 36));
		this.addSlotToContainer(new SlotUpgrade(tedf.inventory, 6, 156, 54));

		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 12 + j * 18, 108 + i * 18));
			}
		}

        for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 12 + i * 18, 166));
		}
	}

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
        return InventoryUtil.transferStack(this.inventorySlots, index, this.transferStrategy, player);
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return oilDrill.isUseableByPlayer(player);
	}
}
