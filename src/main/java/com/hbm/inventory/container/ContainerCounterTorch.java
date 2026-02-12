package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotPattern;
import com.hbm.tileentity.network.TileEntityRadioTorchCounter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ContainerCounterTorch extends ContainerBase {

    protected TileEntityRadioTorchCounter radio;

    public ContainerCounterTorch(InventoryPlayer invPlayer, TileEntityRadioTorchCounter radio) {
        super(invPlayer, radio.inventory);
        this.radio = radio;

        for(int i = 0; i < 3; i++) {
            this.addSlotToContainer(new SlotPattern(radio.inventory, i, 138, 18 + 44 * i));
        }

        playerInv(invPlayer, 12, 156, 214);
    }

    @Override public @NotNull ItemStack transferStackInSlot(EntityPlayer player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack slotClick(int slotId, int dragType, @NotNull ClickType clickTypeIn, @NotNull EntityPlayer player) {
        //L/R: 0
        //M3: 3
        //SHIFT: 1
        //DRAG: 5
        if(slotId < 0 || slotId > 2) {
            return super.slotClick(slotId, dragType, clickTypeIn, player);
        }

        Slot slot = this.getSlot(slotId);

        ItemStack ret = ItemStack.EMPTY;
        ItemStack held = player.inventory.getItemStack();

        if(slot.getHasStack())
            ret = slot.getStack().copy();

        if(dragType == 1 && clickTypeIn == ClickType.PICKUP && slot.getHasStack()) {
            radio.matcher.nextMode(radio.getWorld(), slot.getStack(), slotId);
            return ret;

        } else {
            slot.putStack(held);
            radio.matcher.initPatternStandard(radio.getWorld(), slot.getStack(), slotId);

            return ret;
        }
    }
}
