package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.oil.TileEntityMachinePyroOven;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerPyroOven extends Container {

    private TileEntityMachinePyroOven pyro;

    public ContainerPyroOven(InventoryPlayer invPlayer, TileEntityMachinePyroOven tedf) {
        pyro = tedf;

        //Battery
        this.addSlotToContainer(new SlotBattery(tedf.inventory, 0, 152, 72));
        //Input
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 35, 45));
        //Output
        this.addSlotToContainer(SlotFiltered.takeOnly(tedf.inventory, 2, 89, 45));
        //Fluid ID
        this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 3, 8, 72));
        //Upgrades
        this.addSlotToContainer(new SlotUpgrade(tedf.inventory, 4, 71, 72));
        this.addSlotToContainer(new SlotUpgrade(tedf.inventory, 5, 89, 72));

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
        return InventoryUtil.transferStack(this.inventorySlots, index, 6,
                Library::isBattery, 1,
                s -> !(s.getItem() instanceof IItemFluidIdentifier) && !Library.isMachineUpgrade(s), 3,
                s -> s.getItem() instanceof IItemFluidIdentifier, 4,
                Library::isMachineUpgrade, 6
        );
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return pyro.isUseableByPlayer(player);
    }
}
