package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineFluidTank;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineFluidTank extends Container {

  private final TileEntityMachineFluidTank fluidTank;

  public ContainerMachineFluidTank(InventoryPlayer invPlayer, TileEntityMachineFluidTank tedf) {
    fluidTank = tedf;

    this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 8, 17));
    this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 8, 53));
    this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 53 - 18, 17));
    this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 3, 53 - 18, 53));
    this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 4, 125, 17));
    this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 5, 125, 53));

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 9; j++) {
        this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
      }
    }

    for (int i = 0; i < 9; i++) {
      this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142));
    }
  }

  @Override
  public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
      return InventoryUtil.transferStack(this.inventorySlots, index, 6,
              s -> s.getItem() instanceof IItemFluidIdentifier, 2,
              s -> Library.isStackDrainableForTank(s, fluidTank.tank), 4,
              s -> Library.isStackFillableForTank(s, fluidTank.tank), 6);
  }

  @Override
  public boolean canInteractWith(@NotNull EntityPlayer player) {
    return fluidTank.isUseableByPlayer(player);
  }
}
