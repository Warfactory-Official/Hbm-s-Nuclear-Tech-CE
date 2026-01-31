package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineGasCent;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineGasCent extends Container {

  private final TileEntityMachineGasCent gasCent;

  public ContainerMachineGasCent(InventoryPlayer invPlayer, TileEntityMachineGasCent teGasCent) {

    gasCent = teGasCent;

    // Output
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        this.addSlotToContainer(
            SlotFiltered.takeOnly(gasCent.inventory, j + i * 2, 71 + j * 18, 53 + i * 18));
      }
    }

    // Battery
    this.addSlotToContainer(new SlotBattery(gasCent.inventory, 4, 182, 71));

    // Fluid ID IO
    this.addSlotToContainer(new SlotItemHandler(gasCent.inventory, 5, 91, 15));

    // Upgrade
    this.addSlotToContainer(new SlotUpgrade(gasCent.inventory, 6, 69, 15));

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 9; j++) {
        this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 122 + i * 18));
      }
    }

    for (int i = 0; i < 9; i++) {
      this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 180));
    }
  }

  private static boolean isNormal(ItemStack stack) {
      return !Library.isBattery(stack) && !Library.isMachineUpgrade(stack) && !(stack.getItem() instanceof IItemFluidIdentifier);
  }

  @Override
  public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
    return InventoryUtil.transferStack(this.inventorySlots, index, 7,
            ContainerMachineGasCent::isNormal, 4,
            Library::isBattery, 5,
            s -> s.getItem() instanceof IItemFluidIdentifier, 6,
            Library::isMachineUpgrade, 7);
  }

  @Override
  public boolean canInteractWith(@NotNull EntityPlayer player) {
    return gasCent.isUseableByPlayer(player);
  }
}
