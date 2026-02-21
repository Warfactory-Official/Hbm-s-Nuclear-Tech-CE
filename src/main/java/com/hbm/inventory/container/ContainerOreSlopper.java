package com.hbm.inventory.container;

import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.items.ModItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityMachineOreSlopper;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import java.util.function.Predicate;

public class ContainerOreSlopper extends Container {

    public TileEntityMachineOreSlopper slopper;

    public ContainerOreSlopper(InventoryPlayer player, TileEntityMachineOreSlopper slopper) {
        this.slopper = slopper;

        //Battery
        this.addSlotToContainer(new SlotBattery(slopper.inventory, 0, 8, 72));
        //Fluid ID
        this.addSlotToContainer(new SlotItemHandler(slopper.inventory, 1, 26, 72));
        //Input
        this.addSlotToContainer(new SlotItemHandler(slopper.inventory, 2, 71, 27));
        //Outputs
        this.addSlotToContainer(new SlotItemHandler(slopper.inventory, 3, 134, 18));
        this.addSlotToContainer(new SlotItemHandler(slopper.inventory, 4, 152, 18));
        this.addSlotToContainer(new SlotItemHandler(slopper.inventory, 5, 134, 36));
        this.addSlotToContainer(new SlotItemHandler(slopper.inventory, 6, 152, 36));
        this.addSlotToContainer(new SlotItemHandler(slopper.inventory, 7, 134, 54));
        this.addSlotToContainer(new SlotItemHandler(slopper.inventory, 8, 152, 54));
        //Upgrades
        this.addSlotToContainer(new SlotUpgrade(slopper.inventory, 9, 62, 72));
        this.addSlotToContainer(new SlotUpgrade(slopper.inventory, 10, 80, 72));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(player, j + i * 9 + 9, 8 + j * 18, 122 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(player, i, 8 + i * 18, 180));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 11,
                Library::isBattery, 1,
                s -> s.getItem() instanceof IItemFluidIdentifier, 2,
                s -> s.getItem() == ModItems.bedrock_ore_base, 3,
                Predicate.not(Library::isMachineUpgrade), 9,
                Library::isMachineUpgrade, 11
        );
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return slopper.isUseableByPlayer(player);
    }
}
