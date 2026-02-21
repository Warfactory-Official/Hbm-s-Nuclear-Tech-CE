package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotCraftingOutput;
import com.hbm.inventory.slot.SlotNonRetarded;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.items.machine.ItemBlueprints;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.Library;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ContainerMachineAssemblyFactory extends ContainerBase {

    public ContainerMachineAssemblyFactory(InventoryPlayer invPlayer, IItemHandler assemFac) {
        super(invPlayer, assemFac);

        // Battery
        this.addSlotToContainer(new SlotNonRetarded(assemFac, 0, 234, 112));
        // Upgrades
        this.addSlotToContainer(new SlotUpgrade(assemFac, 1, 214, 149));
        this.addSlotToContainer(new SlotUpgrade(assemFac, 2, 214, 167));
        this.addSlotToContainer(new SlotUpgrade(assemFac, 3, 214, 185));

        for(int i = 0; i < 4; i++) {
            // Template
            this.addSlots(assemFac, 4 + i * 14, 25 + (i % 2) * 109, 54 + (i / 2) * 56, 1, 1);
            // Solid Input
            this.addSlots(assemFac, 5 + i * 14, 7 + (i % 2) * 109, 20 + (i / 2) * 56, 2, 6, 16);
            // Solid Output
            this.addOutputSlots(invPlayer.player, assemFac, 17 + i * 14, 87 + (i % 2) * 109, 54 + (i / 2) * 56, 1, 1);
        }

        this.playerInv(invPlayer, 33, 158);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
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

                if(Library.isBattery(slotOriginal)) {
                    if(!this.mergeItemStack(slotStack, 0, 1, false)) return ItemStack.EMPTY;
                } else if(slotOriginal.getItem() instanceof ItemBlueprints) {
                    if(!this.mergeItemStack(slotStack, 4, 5, false)) return ItemStack.EMPTY;
                } else if(slotOriginal.getItem() instanceof ItemBlueprints) {
                    if(!this.mergeItemStack(slotStack, 18, 19, false)) return ItemStack.EMPTY;
                } else if(slotOriginal.getItem() instanceof ItemBlueprints) {
                    if(!this.mergeItemStack(slotStack, 32, 33, false)) return ItemStack.EMPTY;
                } else if(slotOriginal.getItem() instanceof ItemBlueprints) {
                    if(!this.mergeItemStack(slotStack, 44, 45, false)) return ItemStack.EMPTY;
                } else if(slotOriginal.getItem() instanceof ItemMachineUpgrade) {
                    if(!this.mergeItemStack(slotStack, 1, 4, false)) return ItemStack.EMPTY;
                } else {
                    if(!InventoryUtil.mergeItemStack(this.inventorySlots, slotStack, 5, 17, false) &&
                            !InventoryUtil.mergeItemStack(this.inventorySlots, slotStack, 19, 31, false) &&
                            !InventoryUtil.mergeItemStack(this.inventorySlots, slotStack, 33, 46, false) &&
                            !InventoryUtil.mergeItemStack(this.inventorySlots, slotStack, 47, 59, false)) return ItemStack.EMPTY;
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
