package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
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

public class ContainerMachinePUREX extends ContainerBase {

    public ContainerMachinePUREX(InventoryPlayer invPlayer, IItemHandler chemicalPlant) {
        super(invPlayer, chemicalPlant);

        // Battery
        this.addSlotToContainer(new SlotBattery(chemicalPlant, 0, 152, 81));
        // Schematic
        this.addSlotToContainer(new SlotNonRetarded(chemicalPlant, 1, 35, 126));
        // Upgrades
        this.addSlotToContainer(new SlotUpgrade(chemicalPlant, 2, 152, 108));
        this.addSlotToContainer(new SlotUpgrade(chemicalPlant, 3, 152, 126));
        // Solid Input
        this.addSlots(chemicalPlant, 4, 8, 90, 1, 3);
        // Solid Output
        this.addOutputSlots(invPlayer.player, chemicalPlant, 7, 80, 36, 3, 2);

        this.playerInv(invPlayer, 8, 174);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack slotOriginal = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            slotOriginal = slotStack.copy();

            if (index <= tile.getSlots() - 1) {
                SlotCraftingOutput.checkAchievements(player, slotStack);
                if (!this.mergeItemStack(slotStack, tile.getSlots(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {

                if (Library.isBattery(slotOriginal)) {
                    if (!this.mergeItemStack(slotStack, 0, 1, false)) return ItemStack.EMPTY;
                } else if (slotOriginal.getItem() instanceof ItemBlueprints) {
                    if (!this.mergeItemStack(slotStack, 1, 2, false)) return ItemStack.EMPTY;
                } else if (slotOriginal.getItem() instanceof ItemMachineUpgrade) {
                    if (!this.mergeItemStack(slotStack, 2, 4, false)) return ItemStack.EMPTY;
                } else {
                    if (!InventoryUtil.mergeItemStack(this.inventorySlots, slotStack, 4, 7, false)) return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return slotOriginal;
    }
}
