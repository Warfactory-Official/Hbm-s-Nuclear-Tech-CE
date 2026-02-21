package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotPattern;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineAutocrafter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerAutocrafter extends Container {

    private final TileEntityMachineAutocrafter autoCrafter;

    public ContainerAutocrafter(InventoryPlayer invPlayer, TileEntityMachineAutocrafter tedf) {
        autoCrafter = tedf;

        /* TEMPLATE */
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                this.addSlotToContainer(new SlotPattern(tedf.inventory, j + i * 3, 44 + j * 18, 22 + i * 18));
            }
        }
        this.addSlotToContainer(new SlotPattern(tedf.inventory, 9, 116, 40, true));

        /* RECIPE */
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                this.addSlotToContainer(new SlotItemHandler(tedf.inventory, j + i * 3 + 10, 44 + j * 18, 86 + i * 18));
            }
        }
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 19, 116, 104));

        //Battery
        this.addSlotToContainer(new SlotBattery(tedf.inventory, 20, 17, 99));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 158 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 216));
        }
    }

    @Override
    public @NotNull ItemStack slotClick(int index, int button, @NotNull ClickType clickTypeIn, @NotNull EntityPlayer player) {
        if(index < 0 || index > 9) {
            return super.slotClick(index, button, clickTypeIn, player);
        }

        Slot slot = this.getSlot(index);

        ItemStack ret = ItemStack.EMPTY;
        ItemStack held = player.inventory.getItemStack();

        if(slot.getHasStack())
            ret = slot.getStack().copy();

        //Don't allow any interaction for the template's output
        if(index == 9) {

            if(button == 1 && clickTypeIn == ClickType.PICKUP && slot.getHasStack()) {
                autoCrafter.nextTemplate();
                this.detectAndSendChanges();
            }

            return ret;
        }

        if(button == 1 && clickTypeIn == ClickType.PICKUP && slot.getHasStack()) {
            autoCrafter.nextMode(index);

        } else {
            slot.putStack(!held.isEmpty() ? held.copy() : ItemStack.EMPTY);

            if(slot.getHasStack()) {
                slot.getStack().setCount(1);
            }

            slot.onSlotChanged();
            autoCrafter.initPattern(slot.getStack(), index);
            autoCrafter.updateTemplateGrid();
        }
        return ret;
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
        ItemStack rStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if(slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            rStack = stack.copy();

            if(index <= 20 && index >= 10) {
                if(!this.mergeItemStack(stack, 21, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if(index > 20){
                if(Library.isDischargeableBattery(rStack)) {
                    if(!this.mergeItemStack(stack, 20, 21, false)) return ItemStack.EMPTY;
                } else return ItemStack.EMPTY;
            }

            if (stack.isEmpty()){
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return rStack;
    }

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer player) {
        return autoCrafter.isUseableByPlayer(player);
    }
}
