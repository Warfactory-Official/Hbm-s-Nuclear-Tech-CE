package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotPattern;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.tileentity.network.TileEntityCraneExtractor;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import java.util.function.Predicate;

public class ContainerCraneExtractor extends Container  {
    protected TileEntityCraneExtractor extractor;

    public ContainerCraneExtractor(InventoryPlayer invPlayer, TileEntityCraneExtractor extractor) {
        this.extractor = extractor;

        //filter
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                this.addSlotToContainer(new SlotPattern(extractor.inventory, j + i * 3, 71 + j * 18, 17 + i * 18));
            }
        }

        //buffer
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                this.addSlotToContainer(new SlotItemHandler(extractor.inventory, 9 + j + i * 3, 8 + j * 18, 17 + i * 18));
            }
        }

        //upgrades
        this.addSlotToContainer(new SlotUpgrade(extractor.inventory, 18, 152, 23));
        this.addSlotToContainer(new SlotUpgrade(extractor.inventory, 19, 152, 47));

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
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        if (slotId < 0 || slotId >= 9) {
            return super.slotClick(slotId, dragType, clickTypeIn, player);
        }

        Slot slot = this.inventorySlots.get(slotId);

        ItemStack ret = ItemStack.EMPTY;
        ItemStack held = player.inventory.getItemStack();

        if (slot.getHasStack()) {
            ret = slot.getStack().copy();
        }

        if (clickTypeIn == ClickType.PICKUP && dragType == 1 && slot.getHasStack()) {
            extractor.nextMode(slotId);
        } else {
            slot.putStack(held.isEmpty() ? ItemStack.EMPTY : held.copy());

            if (slot.getHasStack()) {
                slot.getStack().setCount(1);
            }

            slot.onSlotChanged();
            extractor.initPattern(slot.getStack(), slotId);
        }

        return ret;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 20,
                _ -> false, 9,
                Predicate.not(Library::isMachineUpgrade), 18,
                ContainerCraneExtractor::isUpgradeStack, 19,
                ContainerCraneExtractor::isUpgradeEjector, 20);
    }

    private static boolean isUpgradeStack(ItemStack item) {
        return item.getItem() == ModItems.upgrade_stack_1 || item.getItem() == ModItems.upgrade_stack_2 || item.getItem() == ModItems.upgrade_stack_3;
    }

    private static boolean isUpgradeEjector(ItemStack item) {
        return item.getItem() == ModItems.upgrade_ejector_1 ||  item.getItem() == ModItems.upgrade_ejector_2 ||  item.getItem() == ModItems.upgrade_ejector_3;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return extractor.isUseableByPlayer(player);
    }
}
