package com.hbm.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ContainerRadioTorchReceiver extends Container {
  public ContainerRadioTorchReceiver() {
    super();
  }

  @Override
  public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer playerIn, int index) {
    return ItemStack.EMPTY;
  }

  @Override
  public boolean canInteractWith(@NotNull EntityPlayer player) {
    return true;
  }
}
