package com.hbm.inventory.container;

import com.hbm.inventory.TransferStrategy;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.ISatChip;
import com.hbm.tileentity.machine.TileEntityMachineSatDock;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineSatDock extends Container {

	private final TileEntityMachineSatDock dock;

    private static final TransferStrategy TRANSFER_STRATEGY = TransferStrategy.builder(16)
                                                                              .rule(0, 15,
                                                                                      s -> !(s.getItem() instanceof ISatChip))
                                                                              .rule(15, 16,
                                                                                      s -> s.getItem() instanceof ISatChip)
                                                                              .build();

    public ContainerMachineSatDock(InventoryPlayer invPlayer, TileEntityMachineSatDock tedf) {

        dock = tedf;
        IItemHandler inventory = tedf.getCheckedInventory();

		//Storage
		this.addSlotToContainer(SlotFiltered.takeOnly(inventory, 0, 71, 18));
		this.addSlotToContainer(SlotFiltered.takeOnly(inventory, 1, 71 + 18, 18));
		this.addSlotToContainer(SlotFiltered.takeOnly(inventory, 2, 71 + 18 * 2, 18));
		this.addSlotToContainer(SlotFiltered.takeOnly(inventory, 3, 71 + 18 * 3, 18));
		this.addSlotToContainer(SlotFiltered.takeOnly(inventory, 4, 71 + 18 * 4, 18));
		this.addSlotToContainer(SlotFiltered.takeOnly(inventory, 5, 71, 36));
		this.addSlotToContainer(SlotFiltered.takeOnly(inventory, 6, 71 + 18, 36));
		this.addSlotToContainer(SlotFiltered.takeOnly(inventory, 7, 71 + 18 * 2, 36));
		this.addSlotToContainer(SlotFiltered.takeOnly(inventory, 8, 71 + 18 * 3, 36));
		this.addSlotToContainer(SlotFiltered.takeOnly(inventory, 9, 71 + 18 * 4, 36));
		this.addSlotToContainer(SlotFiltered.takeOnly(inventory, 10, 71, 54));
		this.addSlotToContainer(SlotFiltered.takeOnly(inventory, 11, 71 + 18, 54));
		this.addSlotToContainer(SlotFiltered.takeOnly(inventory, 12, 71 + 18 * 2, 54));
		this.addSlotToContainer(SlotFiltered.takeOnly(inventory, 13, 71 + 18 * 3, 54));
		this.addSlotToContainer(SlotFiltered.takeOnly(inventory, 14, 71 + 18 * 4, 54));
		//Chip
		this.addSlotToContainer(new SlotItemHandler(inventory, 15, 26, 36));

        for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 104 + i * 18));
			}
		}

        for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 162));
		}
	}

    @Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index)
    {
        return InventoryUtil.transferStack(this.inventorySlots, index, this.TRANSFER_STRATEGY, player);
    }

	@Override
	public boolean canInteractWith(@NotNull EntityPlayer player) {
		return dock.isUseableByPlayer(player);
	}
}
