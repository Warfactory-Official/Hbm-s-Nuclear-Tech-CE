package com.hbm.inventory.container;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.inventory.TransferStrategy;
import com.hbm.inventory.slot.SlotNonRetarded;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBlueprints;
import com.hbm.util.InventoryUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineRockMill extends ContainerBase {

    private static final TransferStrategy TRANSFER_STRATEGY = TransferStrategy.builder(8)
            .rule(0, 1, s -> s.getItem() instanceof IBatteryItem || s.getItem() == ModItems.battery_creative)
            .rule(1, 2, s -> s.getItem() instanceof ItemBlueprints)
            .rule(2, 5, ContainerMachineRockMill::isNormal)
            .ruleDispatchMode(TransferStrategy.RuleDispatchMode.FALLTHROUGH_ON_FAILURE)
            .playerFallbackMode(TransferStrategy.PlayerFallbackMode.REBALANCE_SECTIONS)
            .build();

    public ContainerMachineRockMill(InventoryPlayer invPlayer, ItemStackHandler rockMill) {
        super(invPlayer, rockMill);

        // Battery
        this.addSlotToContainer(new SlotNonRetarded(rockMill, 0, 152, 91));
        // Schematic
        this.addSlotToContainer(new SlotNonRetarded(rockMill, 1, 35, 90));
        // Solid Input
        this.addSlots(rockMill, 2, 8, 27, 1, 3);
        // Solid Output
        this.addOutputSlots(invPlayer.player, rockMill, 5, 80, 27, 1, 3);

        this.playerInv(invPlayer, 8, 138);
    }

    private static boolean isNormal(ItemStack stack) {
        return !(stack.getItem() instanceof IBatteryItem || stack.getItem() == ModItems.battery_creative) && !(stack.getItem() instanceof ItemBlueprints);
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, TRANSFER_STRATEGY, player);
    }
}