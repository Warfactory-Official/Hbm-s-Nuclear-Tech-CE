package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotPattern;
import com.hbm.tileentity.network.TileEntityPneumoTube;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerPneumoTube extends Container {
    private final TileEntityPneumoTube tube;

    public ContainerPneumoTube(InventoryPlayer invPlayer, TileEntityPneumoTube tube) {
        this.tube = tube;

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 5; j++) {
                this.addSlotToContainer(new SlotPattern(tube.inventory, i * 5 + j, 35 + j * 18, 17 + i * 18));
            }
        }


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
    public ItemStack slotClick(int index, int button, ClickType mode, EntityPlayer player) {
        if(index < 0 || index >= 15) return super.slotClick(index, button, mode, player);

        Slot slot = this.getSlot(index);
        ItemStack ret = ItemStack.EMPTY;
        ItemStack held = player.inventory.getItemStack();

        if(slot.getHasStack()) ret = slot.getStack().copy();

        if(button == 1 && mode == ClickType.PICKUP && slot.getHasStack()) {
            tube.nextMode(index);
        } else {
            slot.putStack(held);
            tube.initPattern(slot.getStack(), index);
        }
        return ret;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return tube.isUseableByPlayer(playerIn);
    }
}
