package com.hbm.inventory.container;

import com.hbm.inventory.TransferStrategy;
import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.oil.TileEntityMachineVacuumDistill;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineVacuumDistill extends Container {

    private TileEntityMachineVacuumDistill distill;

    private final TransferStrategy transferStrategy = TransferStrategy.builder(10)
                                                                      .rule(0, 1, Library::isBattery)
                                                                      .rule(1, 3, s -> Library.isStackFillableForTank(s, distill.tanks[1]))
                                                                      .rule(3, 5, s -> Library.isStackFillableForTank(s, distill.tanks[2]))
                                                                      .rule(5, 7, s -> Library.isStackFillableForTank(s, distill.tanks[3]))
                                                                      .rule(7, 9, s -> Library.isStackFillableForTank(s, distill.tanks[4]))
                                                                      .rule(9, 10, s -> s.getItem() instanceof IItemFluidIdentifier)
                                                                      .build();

    public ContainerMachineVacuumDistill(InventoryPlayer invPlayer, TileEntityMachineVacuumDistill tedf) {

        distill = tedf;

        //Battery
        this.addSlotToContainer(new SlotBattery(tedf.inventory, 0, 26, 90));
        //Canister Input (removed, requires pressurization)
        // this.addSlotToContainer(new SlotDeprecated(tedf, 1, 44, 90));
        //Canister Output (same as above)
        // this.addSlotToContainer(new SlotDeprecated(tedf, 2, 44, 108));
        //Heavy Oil Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 80, 90));
        //Heavy Oil Output
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 2, 80, 108));
        //Nahptha Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 98, 90));
        //Nahptha Output
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 4, 98, 108));
        //Light Oil Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 5, 116, 90));
        //Light Oil Output
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 6, 116, 108));
        //Petroleum Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 7, 134, 90));
        //Petroleum Output
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 8, 134, 108));
        //Fluid ID
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 9, 26, 108));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 156 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 214));
        }
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, this.transferStrategy, player);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return distill.isUseableByPlayer(player);
    }
}
