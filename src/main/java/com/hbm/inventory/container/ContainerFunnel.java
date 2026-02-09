package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.tileentity.machine.TileEntityMachineFunnel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerFunnel extends Container {

  private final TileEntityMachineFunnel funnel;

  public ContainerFunnel(InventoryPlayer playerInv, TileEntityMachineFunnel tile) {
    funnel = tile;

    for (int i = 0; i < 9; i++)
      this.addSlotToContainer(new SlotItemHandler(tile.inventory, i, 8 + 18 * i, 18));
    for (int i = 0; i < 9; i++)
      this.addSlotToContainer(SlotFiltered.takeOnly(tile.inventory, i + 9, 8 + 18 * i, 54));

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 9; j++) {
        this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 86 + i * 18));
      }
    }

    for (int i = 0; i < 9; i++) {
      this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 144));
    }
  }

  @Override
  public boolean canInteractWith(@NotNull EntityPlayer player) {
    return funnel.isUseableByPlayer(player);
  }

  @Override
  public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
    ItemStack rStack = ItemStack.EMPTY;
    Slot slot = this.inventorySlots.get(index);

    if (slot != null && slot.getHasStack()) {
      ItemStack stack = slot.getStack();
      rStack = stack.copy();

      if (index <= 17) {
        if (!this.mergeItemStack(stack, 18, this.inventorySlots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else if (!this.mergeItemStack(stack, 0, 9, false)) {
        return ItemStack.EMPTY;
      }

      if (stack.isEmpty()) {
        slot.putStack(ItemStack.EMPTY);
      } else {
        slot.onSlotChanged();
      }
    }

    return rStack;
  }
}
