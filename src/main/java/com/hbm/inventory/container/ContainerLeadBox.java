package com.hbm.inventory.container;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import com.hbm.items.tool.ItemLeadBox;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.SlotItemHandler;

// see comments at ContainerCrateTemplate
@Optional.Interface(iface = "com.cleanroommc.bogosorter.api.ISortableContainer", modid = "bogosorter")
public class ContainerLeadBox extends Container implements ISortableContainer {

    private ItemLeadBox.InventoryLeadBox box;
    private boolean isMainHand;

    public ContainerLeadBox(InventoryPlayer invPlayer, ItemLeadBox.InventoryLeadBox box) {
        this.box = box;
        this.isMainHand = box.player.getHeldItemMainhand() == box.box;
        this.box.openInventory();

        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 5; j++) {
                this.addSlotToContainer(new SlotItemHandler(box, j + i * 5, 43 + j * 18, 18 + i * 18));
            }
        }

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 104 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 162));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, box.getSlots(), true, player);
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        if(isMainHand) {
            if(clickTypeIn == ClickType.SWAP && dragType == player.inventory.currentItem) return ItemStack.EMPTY;
            int heldSlot = 47 + player.inventory.currentItem;
            if(slotId == heldSlot) return ItemStack.EMPTY;
        }
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        this.box.closeInventory();
    }

    @Override
    @Optional.Method(modid = "bogosorter")
    public void buildSortingContext(ISortingContextBuilder builder) {
        builder.addSlotGroup(0, box.getSlots(), 5);
    }
}
