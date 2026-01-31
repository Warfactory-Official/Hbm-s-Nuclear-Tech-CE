package com.hbm.inventory.container;

import com.hbm.api.item.IDesignatorItem;
import com.hbm.inventory.slot.SlotCraftingOutput;
import com.hbm.items.ModItems;
import com.hbm.tileentity.bomb.TileEntityLaunchPadRusted;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerLaunchPadRusted extends Container {

    private TileEntityLaunchPadRusted launchpad;

    public ContainerLaunchPadRusted(InventoryPlayer invPlayer, TileEntityLaunchPadRusted tedf) {
        this.launchpad = tedf;

        this.addSlotToContainer(new SlotCraftingOutput(invPlayer.player, tedf.inventory, 0, 26, 72));
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 116, 45));
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 134, 45));
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 26, 99));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 154 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 212));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack var3 = ItemStack.EMPTY;
        Slot var4 = this.inventorySlots.get(index);

        if(var4 != null && var4.getHasStack()) {
            ItemStack var5 = var4.getStack();
            var3 = var5.copy();

            if(index <= 3) {
                if(!this.mergeItemStack(var5, 4, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {

                if(var3.getItem() instanceof IDesignatorItem) {
                    if(!this.mergeItemStack(var5, 3, 4, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if(var3.getItem() == ModItems.launch_code) {
                    if(!this.mergeItemStack(var5, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if(var3.getItem() == ModItems.launch_key) {
                    if(!this.mergeItemStack(var5, 2, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if(var5.getCount() == 0) {
                var4.putStack(ItemStack.EMPTY);
            } else {
                var4.onSlotChanged();
            }
        }

        return var3;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return launchpad.isUseableByPlayer(player);
    }
}
