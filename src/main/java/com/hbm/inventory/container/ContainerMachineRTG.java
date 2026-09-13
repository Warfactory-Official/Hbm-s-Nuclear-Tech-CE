package com.hbm.inventory.container;

import com.hbm.inventory.TransferStrategy;
import com.hbm.tileentity.machine.TileEntityMachineRTG;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineRTG extends Container {

	private final TileEntityMachineRTG testNuke;
	private int heat;

    private static final TransferStrategy TRANSFER_STRATEGY = TransferStrategy.builder(15)
                                                                              .genericMachineRange(0)
                                                                              .build();

    public ContainerMachineRTG(InventoryPlayer invPlayer, TileEntityMachineRTG tedf) {
		heat = 0;

		testNuke = tedf;

		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 16, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 34, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 52, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 70, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 88, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 16, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 6, 34, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 7, 52, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 8, 70, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 9, 88, 36));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 10, 16, 54));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 11, 34, 54));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 12, 52, 54));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 13, 70, 54));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 14, 88, 54));

        for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 106 + i * 18));
			}
		}

        for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 164));
		}
	}

    @Override
	public void addListener(@NotNull IContainerListener listener) {
		super.addListener(listener);
		listener.sendWindowProperty(this, 0, testNuke.heat);
	}

    @Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index)
    {
        return InventoryUtil.transferStack(this.inventorySlots, index, TRANSFER_STRATEGY, player);
    }

	@Override
	public boolean canInteractWith(@NotNull EntityPlayer player) {
		return testNuke.isUseableByPlayer(player);
	}

    @Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

        for (IContainerListener par1 : this.listeners) {
            if (this.heat != this.testNuke.heat) {
                par1.sendWindowProperty(this, 0, this.testNuke.heat);
            }
        }

		this.heat = this.testNuke.heat;
	}

    @Override
	public void updateProgressBar(int i, int j) {
		if(i == 0)
		{
			testNuke.heat = j;
		}
	}
}
