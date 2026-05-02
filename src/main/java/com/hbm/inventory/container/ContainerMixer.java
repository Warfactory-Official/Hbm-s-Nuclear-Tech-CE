package com.hbm.inventory.container;

import com.hbm.inventory.TransferStrategy;
import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineMixer;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMixer extends Container {

	private TileEntityMachineMixer mixer;

	private static final TransferStrategy TRANSFER_STRATEGY = TransferStrategy.builder(5)
                                                                              .rule(0, 1, Library::isBattery)
                                                                              .rule(1, 2, s -> !(s.getItem() instanceof IItemFluidIdentifier) && !Library.isMachineUpgrade(s))
                                                                              .rule(2, 3, s -> s.getItem() instanceof IItemFluidIdentifier)
                                                                              .rule(3, 5, Library::isMachineUpgrade)
                                                                              .build();

	public ContainerMixer(InventoryPlayer player, TileEntityMachineMixer mixer) {
		this.mixer = mixer;

		//Battery
		this.addSlotToContainer(new SlotBattery(mixer.inventory, 0, 23, 77));
		//Item Input
		this.addSlotToContainer(new SlotItemHandler(mixer.inventory, 1, 43, 77));
		//Fluid ID
		this.addSlotToContainer(new SlotItemHandler(mixer.inventory, 2, 117, 77));
		//Upgrades
		this.addSlotToContainer(new SlotUpgrade(mixer.inventory, 3, 137, 24));
		this.addSlotToContainer(new SlotUpgrade(mixer.inventory, 4, 137, 42));

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(player, j + i * 9 + 9, 8 + j * 18, 122 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(player, i, 8 + i * 18, 180));
		}
	}

	@Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
		return InventoryUtil.transferStack(this.inventorySlots, index, this.TRANSFER_STRATEGY, player);
    }

	@Override
	public boolean canInteractWith(@NotNull EntityPlayer player) {
		return mixer.isUseableByPlayer(player);
	}
}
