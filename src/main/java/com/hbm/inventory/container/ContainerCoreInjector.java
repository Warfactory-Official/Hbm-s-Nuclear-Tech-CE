package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.tileentity.machine.TileEntityCoreInjector;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerCoreInjector extends Container {

    //TODO: add slots
    private final TileEntityCoreInjector coreInjector;

    public ContainerCoreInjector(InventoryPlayer invPlayer, TileEntityCoreInjector injector) {
        coreInjector = injector;
        this.addSlotToContainer(new SlotItemHandler(injector.inventory, 0, 15, 16));
        this.addSlotToContainer(SlotFiltered.takeOnly(injector.inventory, 1, 15, 52));
        this.addSlotToContainer(new SlotItemHandler(injector.inventory, 2, 146, 17));
        this.addSlotToContainer(SlotFiltered.takeOnly(injector.inventory, 3, 146, 53));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142));
        }

    }

    @NotNull
    @Override
    public ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
		
        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            result = stack.copy();

            if (index <= 3) {
                if (!this.mergeItemStack(stack, 4, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return result;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return coreInjector.isUseableByPlayer(player);
    }
}
