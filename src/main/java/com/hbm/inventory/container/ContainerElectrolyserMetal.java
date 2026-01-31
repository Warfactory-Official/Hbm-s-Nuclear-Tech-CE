package com.hbm.inventory.container;

import com.hbm.inventory.recipes.ElectrolyserMetalRecipes;
import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityElectrolyser;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public class ContainerElectrolyserMetal extends Container {

    private TileEntityElectrolyser electrolyser;
    Predicate<ItemStack> INPUT_FILTER = itemStack -> ElectrolyserMetalRecipes.recipes.keySet().stream().anyMatch(aStack -> aStack.getStack().getItem().equals(itemStack.getItem()));

    public ContainerElectrolyserMetal(InventoryPlayer invPlayer, TileEntityElectrolyser tedf) {
        electrolyser = tedf;

        //Battery
        this.addSlotToContainer(new SlotBattery(tedf.inventory, 0, 186, 109));
        //Upgrades
        this.addSlotToContainer(new SlotUpgrade(tedf.inventory, 1, 186, 140));
        this.addSlotToContainer(new SlotUpgrade(tedf.inventory, 2, 186, 158));
        //Input
        this.addSlotToContainer(SlotFiltered.withWhitelist(tedf.inventory, 14, 10, 22, INPUT_FILTER)
        );
        //Outputs
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 15, 136, 18));
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 16, 154, 18));
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 17, 136, 36));
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 18, 154, 36));
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 19, 136, 54));
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 20, 154, 54));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 122 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 180));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 21,
                Library::isBattery, 1,
                Library::isMachineUpgrade, 3,
                INPUT_FILTER, 14

               );
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return electrolyser.isUseableByPlayer(player);
    }
}
