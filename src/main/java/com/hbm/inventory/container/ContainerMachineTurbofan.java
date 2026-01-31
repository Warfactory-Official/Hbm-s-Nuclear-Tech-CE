package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.items.ModItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineTurbofan;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineTurbofan extends Container {

    private final TileEntityMachineTurbofan turbofan;
    private int afterburner;

    public ContainerMachineTurbofan(InventoryPlayer invPlayer, TileEntityMachineTurbofan tedf) {
        afterburner = 0;

        turbofan = tedf;

        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 17, 17));
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 1, 17, 53));
        this.addSlotToContainer(new SlotUpgradeTurbofan(tedf.inventory, 2, 98, 71));
        this.addSlotToContainer(new SlotBattery(tedf.inventory, 3, 143, 71));
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 44, 71));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 121 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 179));
        }
    }

    private static class SlotUpgradeTurbofan extends SlotUpgrade {
        SlotUpgradeTurbofan(IItemHandler inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }
        @Override
        public boolean isItemValid(@NotNull ItemStack stack) {
            return super.isItemValid(stack) || stack.getItem() == ModItems.flame_pony;
        }
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 1, this.turbofan.afterburner);
    }

    private static boolean isNormal(ItemStack stack) {
        return !Library.isBattery(stack) && !Library.isMachineUpgrade(stack) && !(stack.getItem() instanceof IItemFluidIdentifier);
    }

    @NotNull
    @Override
    public ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 5,
                ContainerMachineTurbofan::isNormal, 2,
                s -> Library.isMachineUpgrade(s) || s.getItem() == ModItems.flame_pony, 3,
                Library::isChargeableBattery, 4,
                s -> s.getItem() instanceof IItemFluidIdentifier, 5);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return turbofan.isUseableByPlayer(player);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (IContainerListener listener : this.listeners) {
            if (this.afterburner != this.turbofan.afterburner) {
                listener.sendWindowProperty(this, 1, this.turbofan.afterburner);
            }
        }

        this.afterburner = this.turbofan.afterburner;
    }

    @Override
    public void updateProgressBar(int id, int data) {
        if (id == 1) {
            turbofan.afterburner = data;
        }
    }

}
