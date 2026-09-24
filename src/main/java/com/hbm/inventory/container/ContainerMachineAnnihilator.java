package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotCraftingOutput;
import com.hbm.inventory.slot.SlotNonRetarded;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.util.InventoryUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineAnnihilator extends ContainerBase {

    public ContainerMachineAnnihilator(InventoryPlayer invPlayer, IItemHandler annihilator) {
        super(invPlayer, annihilator);

        // Input
        this.addSlotToContainer(new SlotNonRetarded(annihilator, 0, 17, 45));
        // Fluid ID
        this.addSlotToContainer(new SlotNonRetarded(annihilator, 1, 35, 45));
        // Output
        this.addOutputSlots(invPlayer.player, annihilator, 2, 80, 36, 2, 3);
        // Monitor
        this.addSlotToContainer(new SlotNonRetarded(annihilator, 8, 152, 18));
        // Payout Request
        this.addSlotToContainer(new SlotNonRetarded(annihilator, 9, 152, 62));
        // Payout Item
        this.addSlotToContainer(new SlotCraftingOutput(invPlayer.player, annihilator, 10, 152, 80));

        this.playerInv(invPlayer, 8, 126);
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack slotOriginal = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if(slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            slotOriginal = slotStack.copy();

            if(index <= tile.getSlots() - 1) {
                SlotCraftingOutput.checkAchievements(player, slotStack);
                if(!this.mergeItemStack(slotStack, tile.getSlots(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {

                if(slotOriginal.getItem() instanceof IItemFluidIdentifier) {
                    if(!this.mergeItemStack(slotStack, 1, 2, false)) return ItemStack.EMPTY;
                } else {
                    if(!InventoryUtil.mergeItemStack(this.inventorySlots, slotStack, 0, 1, false)) return ItemStack.EMPTY;
                }
            }

            if(slotStack.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            slot.onTake(player, slotStack);
        }

        return slotOriginal;
    }
}
