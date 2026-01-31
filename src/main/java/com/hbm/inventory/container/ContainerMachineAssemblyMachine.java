package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotCraftingOutput;
import com.hbm.inventory.slot.SlotNonRetarded;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.Library;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ContainerMachineAssemblyMachine extends ContainerBase {

    public ContainerMachineAssemblyMachine(InventoryPlayer invPlayer, IItemHandler assembler) {
        super(invPlayer, assembler);

        // Battery
        this.addSlotToContainer(new SlotNonRetarded(assembler, 0, 152, 81));
        // Schematic
        this.addSlotToContainer(new SlotNonRetarded(assembler, 1, 35, 126));
        // Upgrades
        this.addSlots(assembler, 2, 152, 108, 2, 1);
        // Input
        this.addSlots(assembler, 4, 8, 18, 4, 3);
        // Output
        this.addSlotToContainer(new SlotCraftingOutput(invPlayer.player, assembler, 16, 98, 45));

        this.playerInv(invPlayer, 8, 174);
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
                } else if(slotOriginal.getItem() == ModItems.blueprints) {
                    if(!this.mergeItemStack(slotStack, 1, 2, false)) return ItemStack.EMPTY;
                } else if(slotOriginal.getItem() instanceof ItemMachineUpgrade) {
                    if(!this.mergeItemStack(slotStack, 2, 4, false)) return ItemStack.EMPTY;
                } else {
                    if(!InventoryUtil.mergeItemStack(this.inventorySlots, slotStack, 4, 16, false)) return ItemStack.EMPTY;
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
