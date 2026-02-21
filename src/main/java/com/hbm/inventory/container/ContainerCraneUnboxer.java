package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.tileentity.network.TileEntityCraneUnboxer;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import java.util.function.Predicate;

public class ContainerCraneUnboxer extends Container {

    protected TileEntityCraneUnboxer unboxer;

    public ContainerCraneUnboxer(InventoryPlayer invPlayer, TileEntityCraneUnboxer unboxer) {
        this.unboxer = unboxer;

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 7; j++) {
                this.addSlotToContainer(new SlotItemHandler(unboxer.inventory, j + i * 7, 8 + j * 18, 17 + i * 18));
            }
        }

        //upgrades
        this.addSlotToContainer(new SlotUpgrade(unboxer.inventory, 21, 152, 23));
        this.addSlotToContainer(new SlotUpgrade(unboxer.inventory, 22, 152, 47));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 103 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 161));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 23,
                Predicate.not(Library::isMachineUpgrade), 21,
                ContainerCraneUnboxer::isUpgradeStack, 22,
                ContainerCraneUnboxer::isUpgradeEjector, 23);
    }

    private static boolean isUpgradeStack(ItemStack item) {
        return item.getItem() == ModItems.upgrade_stack_1 || item.getItem() == ModItems.upgrade_stack_2 || item.getItem() == ModItems.upgrade_stack_3;
    }

    private static boolean isUpgradeEjector(ItemStack item) {
        return item.getItem() == ModItems.upgrade_ejector_1 ||  item.getItem() == ModItems.upgrade_ejector_2 ||  item.getItem() == ModItems.upgrade_ejector_3;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return unboxer.isUseableByPlayer(player);
    }
}
