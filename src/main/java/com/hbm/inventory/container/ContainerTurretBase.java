package com.hbm.inventory.container;

import com.hbm.interfaces.IContainerOpenEventListener;
import com.hbm.inventory.TransferStrategy;
import com.hbm.inventory.slot.SlotBattery;
import com.hbm.items.machine.ItemTurretBiometry;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.tileentity.turret.TileEntityTurretBaseNT;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerTurretBase extends Container implements IContainerOpenEventListener {

  private final TileEntityTurretBaseNT turret;

  private static final TransferStrategy TRANSFER_STRATEGY = TransferStrategy.builder(11)
                                                                            .rule(0, 1, ContainerTurretBase::isBiometry)
                                                                            .rule(1, 10, ContainerTurretBase::isGeneralInventoryItem)
                                                                            .rule(10, 11, Library::isBattery)
                                                                            .ruleDispatchMode(TransferStrategy.RuleDispatchMode.FALLTHROUGH_ON_FAILURE)
                                                                            .playerFallbackMode(TransferStrategy.PlayerFallbackMode.REBALANCE_SECTIONS)
                                                                            .build();

  public ContainerTurretBase(InventoryPlayer invPlayer, TileEntityTurretBaseNT te) {
    turret = te;

    this.addSlotToContainer(new SlotItemHandler(te.inventory, 0, 98, 27));

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        this.addSlotToContainer(
            new SlotItemHandler(te.inventory, 1 + i * 3 + j, 80 + j * 18, 63 + i * 18));
      }
    }

    this.addSlotToContainer(new SlotBattery(te.inventory, 10, 152, 99));

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 9; j++) {
        this.addSlotToContainer(
            new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + (18 * 3) + 2));
      }
    }

    for (int i = 0; i < 9; i++) {
      this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142 + (18 * 3) + 2));
    }
  }

  private static boolean isBiometry(ItemStack stack) {
    return stack.getItem() instanceof ItemTurretBiometry;
  }

  private static boolean isGeneralInventoryItem(ItemStack stack) {
    return !isBiometry(stack) && !Library.isBattery(stack);
  }

  @Override
  public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
    return InventoryUtil.transferStack(this.inventorySlots, index, this.TRANSFER_STRATEGY, player);
  }

  @Override
  public boolean canInteractWith(@NotNull EntityPlayer player) {
    return turret.isUseableByPlayer(player);
  }

  @Override
  public void onContainerOpened(EntityPlayer player) {
    if (!player.world.isRemote && !player.isSpectator()) {
      player.world.playSound(null, turret.getPos().getX() + 0.5, turret.getPos().getY() + 0.5, turret.getPos().getZ() + 0.5, HBMSoundHandler.openC, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }
  }

  @Override
  public void onContainerClosed(EntityPlayer player) {
    super.onContainerClosed(player);
    if (!player.world.isRemote && !player.isSpectator()) {
      player.world.playSound(null, turret.getPos().getX() + 0.5, turret.getPos().getY() + 0.5, turret.getPos().getZ() + 0.5, HBMSoundHandler.closeC, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }
  }
}
