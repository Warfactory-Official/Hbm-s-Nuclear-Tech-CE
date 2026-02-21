package com.hbm.inventory.container;

import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.TileEntityMachineRotaryFurnace;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineRotaryFurnace extends Container {

    private final TileEntityMachineRotaryFurnace furnace;

    public ContainerMachineRotaryFurnace(InventoryPlayer invPlayer, TileEntityMachineRotaryFurnace tile) {
        furnace = tile;

        // Inputs
        this.addSlotToContainer(new SlotItemHandler(tile.inventory, 0, 8, 18));
        this.addSlotToContainer(new SlotItemHandler(tile.inventory, 1, 26, 18));
        this.addSlotToContainer(new SlotItemHandler(tile.inventory, 2, 44, 18));
        //Fluid ID
        this.addSlotToContainer(new SlotItemHandler(tile.inventory, 3, 8, 54));
        //Solid fuel
        this.addSlotToContainer(new SlotItemHandler(tile.inventory, 4, 44, 54));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 104 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 162));
        }
    }

    private static boolean isNormal(ItemStack stack) {
        return !(stack.getItem() instanceof IItemFluidIdentifier) && !TileEntityFurnace.isItemFuel(stack);
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, 5,
                ContainerMachineRotaryFurnace::isNormal, 3,
                s -> s.getItem() instanceof IItemFluidIdentifier, 4,
                TileEntityFurnace::isItemFuel, 5);
    }

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer player) {
        return furnace.isUseableByPlayer(player);
    }
}
