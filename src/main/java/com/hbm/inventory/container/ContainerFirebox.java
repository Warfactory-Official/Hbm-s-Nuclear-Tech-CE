package com.hbm.inventory.container;

import com.hbm.inventory.TransferStrategy;
import com.hbm.tileentity.machine.TileEntityFireboxBase;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerFirebox extends Container {

	protected TileEntityFireboxBase firebox;

    private static final TransferStrategy TRANSFER_STRATEGY = TransferStrategy.builder(2)
                                                                              .genericMachineRange(0)
                                                                              .build();

    public ContainerFirebox(InventoryPlayer invPlayer, TileEntityFireboxBase furnace) {
		this.firebox = furnace;
		this.firebox.playersUsing++;

        this.addSlotToContainer(new SlotItemHandler(furnace.inventory, 0, 44, 27));
		this.addSlotToContainer(new SlotItemHandler(furnace.inventory, 1, 62, 27));

        for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 86 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 144));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, this.TRANSFER_STRATEGY, player);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return firebox.isUseableByPlayer(player);
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		this.firebox.playersUsing--;
	}
}
