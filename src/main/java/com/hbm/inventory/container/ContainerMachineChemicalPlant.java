package com.hbm.inventory.container;

import com.hbm.capability.NTMFluidCapabilityHandler;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.slot.SlotCraftingOutput;
import com.hbm.inventory.slot.SlotNonRetarded;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.machine.ItemBlueprints;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.Library;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ContainerMachineChemicalPlant extends ContainerBase {

    public ContainerMachineChemicalPlant(InventoryPlayer invPlayer, IItemHandler chemicalPlant) {
        super(invPlayer, chemicalPlant);

        // Battery
        this.addSlotToContainer(new SlotNonRetarded(chemicalPlant, 0, 152, 81));
        // Schematic
        this.addSlotToContainer(new SlotNonRetarded(chemicalPlant, 1, 35, 126));
        // Upgrades
        this.addSlotToContainer(new SlotUpgrade(chemicalPlant, 2, 152, 108));
        this.addSlotToContainer(new SlotUpgrade(chemicalPlant, 3, 152, 126));
        // Solid Input
        this.addSlots(chemicalPlant, 4, 8, 99, 1, 3);
        // Solid Output
        this.addOutputSlots(invPlayer.player, chemicalPlant, 7, 80, 99, 1, 3);
        // Fluid Input
        this.addSlots(			chemicalPlant, 10, 8, 54, 1, 3);
        this.addTakeOnlySlots(	chemicalPlant, 13, 8, 72, 1, 3);
        // Fluid Output
        this.addSlots(			chemicalPlant, 16, 80, 54, 1, 3);
        this.addTakeOnlySlots(	chemicalPlant, 19, 80, 72, 1, 3);

        this.playerInv(invPlayer, 8, 174);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack slotOriginal = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if(slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            slotOriginal = slotStack.copy();

            int tileSize = tile.getSlots();

            if(index < tileSize) {
                SlotCraftingOutput.checkAchievements(player, slotStack);
                if(!this.mergeItemStack(slotStack, tileSize, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if(Library.isBattery(slotOriginal)) {
                    if(!this.mergeItemStack(slotStack, 0, 1, false)) return ItemStack.EMPTY;
                } else if(slotOriginal.getItem() instanceof ItemBlueprints) {
                    if(!this.mergeItemStack(slotStack, 1, 2, false)) return ItemStack.EMPTY;
                } else if(slotOriginal.getItem() instanceof ItemMachineUpgrade) {
                    if(!this.mergeItemStack(slotStack, 2, 4, false)) return ItemStack.EMPTY;
                } else {
                    boolean handled = false;

                    // NTM container detection
                    boolean isHBM = NTMFluidCapabilityHandler.isNtmFluidContainer(slotOriginal.getItem());
                    FluidType hbmType = isHBM ? FluidContainerRegistry.getFluidType(slotOriginal) : Fluids.NONE;
                    int hbmAmount = (isHBM && hbmType != Fluids.NONE) ? FluidContainerRegistry.getFluidContent(slotOriginal, hbmType) : 0;

                    // Forge fluid capability detection
                    int forgeAmount = 0;
                    boolean hasForgeTank = false;
                    net.minecraftforge.fluids.capability.IFluidHandlerItem fh = slotOriginal.hasCapability(net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)
                            ? slotOriginal.getCapability(net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)
                            : null;
                    if (fh != null) {
                        for (net.minecraftforge.fluids.capability.IFluidTankProperties prop : fh.getTankProperties()) {
                            hasForgeTank = true;
                            net.minecraftforge.fluids.FluidStack fs = prop.getContents();
                            if (fs != null && fs.amount > 0) forgeAmount += fs.amount;
                        }
                    }

                    boolean isFluidFull = (isHBM && hbmAmount > 0) || (hasForgeTank && forgeAmount > 0);
                    boolean isFluidEmpty = (isHBM && (hbmType == Fluids.NONE || hbmAmount == 0)) || (hasForgeTank && forgeAmount == 0);

                    if (isFluidFull) {
                        if(!this.mergeItemStack(slotStack, 10, 13, false)) return ItemStack.EMPTY;
                        handled = true;
                    } else if (isFluidEmpty) {
                        if(!this.mergeItemStack(slotStack, 16, 19, false)) return ItemStack.EMPTY;
                        handled = true;
                    }

                    // Fallback to solid input if not a fluid container
                    if(!handled) {
                        if(!InventoryUtil.mergeItemStack(this.inventorySlots, slotStack, 4, 7, false)) return ItemStack.EMPTY;
                    }
                }
            }

            if(slotStack.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            slot.onTake(player, slotStack);
        }

        return slotOriginal;
    }
}