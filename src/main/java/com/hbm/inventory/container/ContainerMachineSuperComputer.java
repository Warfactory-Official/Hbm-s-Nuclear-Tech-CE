package com.hbm.inventory.container;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.inventory.TransferStrategy;
import com.hbm.inventory.slot.SlotNonRetarded;
import com.hbm.items.machine.ItemBlueprints;
import com.hbm.tileentity.machine.TileEntityMachineSuperComputer;
import com.hbm.util.InventoryUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineSuperComputer extends ContainerBase {

	private static final TransferStrategy TRANSFER_STRATEGY = TransferStrategy.builder(8)
			.rule(0, 1, s -> s.getItem() instanceof IBatteryItem)
			.rule(1, 2, s -> s.getItem() instanceof ItemBlueprints)
			.rule(2, 5, ContainerMachineSuperComputer::isNormal)
			.ruleDispatchMode(TransferStrategy.RuleDispatchMode.FALLTHROUGH_ON_FAILURE)
			.playerFallbackMode(TransferStrategy.PlayerFallbackMode.REBALANCE_SECTIONS)
			.build();

	public ContainerMachineSuperComputer(InventoryPlayer invPlayer, TileEntityMachineSuperComputer computer) {
		super(invPlayer, computer.inventory);

		// Battery
		this.addSlotToContainer(new SlotNonRetarded(computer.inventory, 0, 152, 81));
		// Schematic
		this.addSlotToContainer(new SlotNonRetarded(computer.inventory, 1, 35, 80));
		// Input
		this.addSlots(computer.inventory, 2, 8, 27, 1, 3);
		// Output
		this.addOutputSlots(invPlayer.player, computer.inventory, 5, 80, 27, 1, 3);

		this.playerInv(invPlayer, 8, 129);
	}

	private static boolean isNormal(ItemStack stack) {
		return !(stack.getItem() instanceof IBatteryItem) && !(stack.getItem() instanceof ItemBlueprints);
	}

	@Override
	public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
		return InventoryUtil.transferStack(this.inventorySlots, index, TRANSFER_STRATEGY, player);
	}
}
