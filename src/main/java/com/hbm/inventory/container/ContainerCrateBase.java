package com.hbm.inventory.container;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import com.hbm.inventory.TransferStrategy;
import com.hbm.tileentity.machine.IHandHeldCrate;
import com.hbm.tileentity.machine.TileEntityCrate;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

@Optional.Interface(iface = "com.cleanroommc.bogosorter.api.ISortableContainer", modid = "bogosorter")
public class ContainerCrateBase extends Container implements ISortableContainer {

    protected final TileEntityCrate crate;
    private final TransferStrategy transferStrategy;

    public ContainerCrateBase(InventoryPlayer invPlayer, TileEntityCrate crate) {
        this.crate = crate;
        this.transferStrategy = TransferStrategy.builder(() -> this.crate.inventory.getSlots())
                                                .genericMachineRange(0)
                                                .build();

        for (int row = 0; row < crate.getCrateRows(); row++) {
            for (int col = 0; col < crate.getCrateColumns(); col++) {
                int slot = col + row * crate.getCrateColumns();
                this.addSlotToContainer(new SlotItemHandler(crate.inventory, slot, crate.getCrateX() + col * 18, crate.getCrateY() + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlotToContainer(new Slot(invPlayer, col + row * 9 + 9, crate.getPlayerInventoryX() + col * 18, crate.getPlayerInventoryY() + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlotToContainer(new Slot(invPlayer, col, crate.getPlayerInventoryX() + col * 18, crate.getHotbarY()));
        }
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, this.transferStrategy, player);
    }

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer player) {
        return crate.isUseableByPlayer(player);
    }

    @Override
    public void onContainerClosed(@NotNull EntityPlayer player) {
        super.onContainerClosed(player);
        if (!player.world.isRemote && crate instanceof IHandHeldCrate held) {
            held.finishHeldInventorySession(player);
        }
    }

    @Override
    @Optional.Method(modid = "bogosorter")
    public void buildSortingContext(ISortingContextBuilder builder) {
        builder.addSlotGroup(0, crate.inventory.getSlots(), crate.getCrateColumns());
    }

    @Override
    public @NotNull ItemStack slotClick(int slotId, int dragType, @NotNull ClickType clickTypeIn, @NotNull EntityPlayer player) {
        if (crate instanceof IHandHeldCrate held && held.blocksHeldSlotInteraction(slotId, dragType, clickTypeIn, player, this.inventorySlots)) {
            return ItemStack.EMPTY;
        }

        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }
}
