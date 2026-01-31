package com.hbm.inventory.container;

import com.hbm.inventory.*;
import com.hbm.inventory.recipes.SolderingRecipes;
import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotNonRetarded;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineSolderingStation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineSolderingStation extends Container {
  private final TileEntityMachineSolderingStation soldering_station;

  public ContainerMachineSolderingStation(
      InventoryPlayer playerInv, TileEntityMachineSolderingStation tile) {
    soldering_station = tile;

    // Inputs
    for (int i = 0; i < 2; i++)
      for (int j = 0; j < 3; j++)
        this.addSlotToContainer(
            new SlotNonRetarded(tile.inventory, i * 3 + j, 17 + j * 18, 18 + i * 18));
    // Output
    this.addSlotToContainer(SlotFiltered.takeOnly(tile.inventory, 6, 107, 27));
    // Battery
    this.addSlotToContainer(new SlotBattery(tile.inventory, 7, 152, 72));
    // Fluid ID
    this.addSlotToContainer(new SlotItemHandler(tile.inventory, 8, 17, 63));
    // Upgrades
    this.addSlotToContainer(new SlotUpgrade(tile.inventory, 9, 89, 63));
    this.addSlotToContainer(new SlotUpgrade(tile.inventory, 10, 107, 63));

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 9; j++) {
        this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 122 + i * 18));
      }
    }

    for (int i = 0; i < 9; i++) {
      this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 180));
    }
  }

  @Override
  public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
    ItemStack rStack = ItemStack.EMPTY;
    Slot slot = this.inventorySlots.get(index);

    if (slot != null && slot.getHasStack()) {
      ItemStack stack = slot.getStack();
      rStack = stack.copy();

      if (index <= 10) {
        if (!this.mergeItemStack(stack, 11, this.inventorySlots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else {

        if (Library.isBattery(rStack)) {
          if (!this.mergeItemStack(stack, 7, 8, false)) return ItemStack.EMPTY;
        } else if (rStack.getItem() instanceof IItemFluidIdentifier) {
          if (!this.mergeItemStack(stack, 8, 9, false)) return ItemStack.EMPTY;
        } else if (rStack.getItem() instanceof ItemMachineUpgrade) {
          if (!this.mergeItemStack(stack, 9, 11, false)) return ItemStack.EMPTY;
        } else {
          for (RecipesCommon.AStack t : SolderingRecipes.toppings)
            if (t.matchesRecipe(stack, false))
              if (!this.mergeItemStack(stack, 0, 3, false)) return ItemStack.EMPTY;
          for (RecipesCommon.AStack t : SolderingRecipes.pcb)
            if (t.matchesRecipe(stack, false))
              if (!this.mergeItemStack(stack, 3, 5, false)) return ItemStack.EMPTY;
          for (RecipesCommon.AStack t : SolderingRecipes.solder)
            if (t.matchesRecipe(stack, false))
              if (!this.mergeItemStack(stack, 5, 6, false)) return ItemStack.EMPTY;
          return ItemStack.EMPTY;
        }
      }

      if (stack.isEmpty()) {
        slot.putStack(ItemStack.EMPTY);
      } else {
        slot.onSlotChanged();
      }
    }

    return rStack;
  }

  @Override
  public boolean canInteractWith(@NotNull EntityPlayer player) {
    return soldering_station.isUseableByPlayer(player);
  }
}
