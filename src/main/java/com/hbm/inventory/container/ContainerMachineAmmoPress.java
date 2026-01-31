package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.inventory.recipes.AmmoPressRecipes;
import com.hbm.tileentity.machine.TileEntityMachineAmmoPress;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineAmmoPress extends Container {

    private final TileEntityMachineAmmoPress press;

    public ContainerMachineAmmoPress(InventoryPlayer playerInv, TileEntityMachineAmmoPress tile) {
        press = tile;

        //Inputs
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                this.addSlotToContainer(new SlotItemHandler(tile.inventory, i * 3 + j, 116 + j * 18, 18 + i * 18));
            }
        }
        // Output
        this.addSlotToContainer(SlotFiltered.takeOnly(tile.inventory, 9, 134, 72));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 118 + i * 18));
            }
        }
        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 176));
        }
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
        ItemStack rStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if(slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            rStack = stack.copy();

            if(index <= 9) {
                if(!this.mergeItemStack(stack, 10, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {

                if(press.selectedRecipe < 0 || press.selectedRecipe >= AmmoPressRecipes.recipes.size()) return ItemStack.EMPTY;
                AmmoPressRecipes.AmmoPressRecipe recipe = AmmoPressRecipes.recipes.get(press.selectedRecipe);

                for(int i = 0; i < 9; i++) {
                    if(recipe.input[i] == null) continue;
                    if(recipe.input[i].matchesRecipe(stack, true)) {
                        if(!this.mergeItemStack(stack, i, i + 1, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }

                return ItemStack.EMPTY;
            }

            if(stack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return rStack;
    }

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer player) {
        return press.isUseableByPlayer(player);
    }
}
