package com.hbm.inventory.container;

import com.hbm.inventory.TransferStrategy;
import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineCentrifuge;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerCentrifuge extends Container {

	private final TileEntityMachineCentrifuge centrifuge;
    private static final TransferStrategy TRANSFER_STRATEGY = TransferStrategy.builder(8)
                                                                              .rule(0, 1, ContainerCentrifuge::isNormal)
                                                                              .rule(1, 2, Library::isBattery)
                                                                              .rule(2, 6, ContainerCentrifuge::isNormal)
                                                                              .rule(6, 8, Library::isMachineUpgrade)
                                                                              .build();

    public ContainerCentrifuge(InventoryPlayer invPlayer, TileEntityMachineCentrifuge te) {

		centrifuge = te;

		this.addSlotToContainer(new SlotItemHandler(te.inventory, 0, 44, 57));
		// Battery
		this.addSlotToContainer(new SlotBattery(te.inventory, 1, 8, 57));
		// Outputs
		this.addSlotToContainer(SlotFiltered.takeOnly(te.inventory, 2, 70, 57));
		this.addSlotToContainer(SlotFiltered.takeOnly(te.inventory, 3, 90, 57));
		this.addSlotToContainer(SlotFiltered.takeOnly(te.inventory, 4, 110, 57));
		this.addSlotToContainer(SlotFiltered.takeOnly(te.inventory, 5, 130, 57));
		// Upgrades
		this.addSlotToContainer(new SlotUpgrade(te.inventory, 6, 156, 31));
		this.addSlotToContainer(new SlotUpgrade(te.inventory, 7, 156, 49));

        for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 11 + j * 18, 107 + i * 18));
			}
		}

        for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 11 + i * 18, 165));
		}
	}

    private static boolean isNormal(ItemStack stack) {
        return !Library.isBattery(stack) && !Library.isMachineUpgrade(stack);
    }

	@Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, TRANSFER_STRATEGY, player);
    }

	@Override
	public boolean canInteractWith(@NotNull EntityPlayer player) {
		return centrifuge.isUseableByPlayer(player);
	}
}
