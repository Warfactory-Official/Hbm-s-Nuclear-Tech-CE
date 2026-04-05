package com.hbm.inventory.container;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotCraftingOutput;
import com.hbm.inventory.slot.SlotNonRetarded;
import com.hbm.items.ModItems;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachinePlasmaForge extends ContainerBase {

    public ContainerMachinePlasmaForge(InventoryPlayer invPlayer, IItemHandler forge) {
        super(invPlayer, forge);

        this.addSlotToContainer(new SlotBattery(forge, 0, 152, 82));
        this.addSlotToContainer(new SlotNonRetarded(forge, 1, 35, 81));
        this.addSlotToContainer(new SlotNonRetarded(forge, 2, 98, 116));
        this.addSlots(forge, 3, 8, 18, 3, 4);
        this.addSlotToContainer(new SlotCraftingOutput(invPlayer.player, forge, 15, 116, 36));

        this.playerInv(invPlayer, 8, 162);
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
                if(slotOriginal.getItem() instanceof IBatteryItem || slotOriginal.getItem() == ModItems.battery_creative) {
                    if(!this.mergeItemStack(slotStack, 0, 1, false)) return ItemStack.EMPTY;
                } else if(slotOriginal.getItem() == ModItems.blueprints) {
                    if(!this.mergeItemStack(slotStack, 1, 2, false)) return ItemStack.EMPTY;
                } else {
                    if(!InventoryUtil.mergeItemStack(this.inventorySlots, slotStack, 2, 15, false)) return ItemStack.EMPTY;
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
