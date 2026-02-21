package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.ModItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineCombustionEngine;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerCombustionEngine extends Container {

  private final TileEntityMachineCombustionEngine engine;

  public ContainerCombustionEngine(
      InventoryPlayer invPlayer, TileEntityMachineCombustionEngine tile) {
    this.engine = tile;

    this.addSlotToContainer(new SlotItemHandler(tile.inventory, 0, 17, 17));
    this.addSlotToContainer(SlotFiltered.takeOnly(tile.inventory, 1, 17, 53));
    this.addSlotToContainer(new SlotItemHandler(tile.inventory, 2, 88, 71));
    this.addSlotToContainer(new SlotBattery(tile.inventory, 3, 143, 71));
    this.addSlotToContainer(new SlotItemHandler(tile.inventory, 4, 35, 71));

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 9; j++) {
        this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 121 + i * 18));
      }
    }

    for (int i = 0; i < 9; i++) {
      this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 179));
    }
  }

  private static boolean isNormal(ItemStack stack) {
      return !Library.isChargeableBattery(stack) && !(stack.getItem() instanceof IItemFluidIdentifier) && stack.getItem() != ModItems.piston_set;
  }

  @Override
  public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
    return InventoryUtil.transferStack(this.inventorySlots, index, 5,
            ContainerCombustionEngine::isNormal, 2,
            s -> s.getItem() == ModItems.piston_set, 3,
            Library::isChargeableBattery, 4,
            s -> s.getItem() instanceof IItemFluidIdentifier, 5);
  }

  @Override
  public boolean canInteractWith(@NotNull EntityPlayer player) {
    return engine.isUseableByPlayer(player);
  }
}
