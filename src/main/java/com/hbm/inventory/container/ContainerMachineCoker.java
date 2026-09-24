package com.hbm.inventory.container;

import com.hbm.inventory.TransferStrategy;
import com.hbm.inventory.slot.SlotCraftingOutput;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.oil.TileEntityMachineCoker;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineCoker extends Container {

    private TileEntityMachineCoker coker;

    private static final TransferStrategy TRANSFER_STRATEGY = TransferStrategy.builder(2)
                                                                              .rule(0, 1,
                                                                                      s -> s.getItem() instanceof IItemFluidIdentifier)
                                                                              .genericMachineRange(1)
                                                                              .build();

    public ContainerMachineCoker(InventoryPlayer invPlayer, TileEntityMachineCoker tedf) {

        coker = tedf;

        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 35, 72));
        this.addSlotToContainer(new SlotCraftingOutput(invPlayer.player, tedf.inventory, 1, 97, 27));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 122 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 180));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, this.TRANSFER_STRATEGY, player);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return coker.isUseableByPlayer(player);
    }
}
