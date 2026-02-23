package com.hbm.inventory.fluid.tank;

import com.hbm.capability.NTMFluidCapabilityHandler;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.machine.ItemFluidTankV2;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import static micdoodle8.mods.galacticraft.core.world.gen.BiomeOrbit.space;

public class FluidLoaderForge implements IFluidLoadingHandler {

    private static @Nullable IFluidHandlerItem getIFluidHandler(IItemHandler slots, int in) {
        ItemStack extracted = slots.extractItem(in, 1, true);
        if (extracted.isEmpty()) return null;
        if (NTMFluidCapabilityHandler.isNtmFluidContainer(extracted.getItem())) return null;
        if (!extracted.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) return null;
        return extracted.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
    }

    @Override
    public boolean fillItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
        if (tank.pressure != 0 || tank.getFill() <= 0) return false;
        FluidType tankType = tank.getTankType();
        if (tankType == Fluids.NONE) return false;
        Fluid forgeFluid = tankType.getFF();
        if (forgeFluid == null) return true;
        IFluidHandlerItem handler = getIFluidHandler(slots, in);
        if (handler == null) return false;
        int offer = tank.getFill();
        int canFill = handler.fill(new FluidStack(forgeFluid, offer), false);
        if (canFill <= 0) return false;

        // Prevent Fluid Dupe if stack count is greater than one and output slot is occupied.
        ItemStack stack = slots.getStackInSlot(in);
        if (stack.getCount() > 1 && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) && stack.getItem() instanceof ItemFluidTankV2 fluidTankV2) {
            System.err.println("FIX A");
            if (fluidTankV2.cap > offer) return false;
        }


        int actualFill = handler.fill(new FluidStack(forgeFluid, canFill), true);
        if (actualFill <= 0) return false;

        ItemStack container = handler.getContainer();

        if (!slots.insertItem(out, container, true).isEmpty()) return false;
        slots.extractItem(in, 1, false);
        tank.setFill(tank.getFill() - actualFill);
        slots.insertItem(out, container, false);
        return true;
    }

    @Override
    public boolean emptyItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
        IFluidHandlerItem handler = getIFluidHandler(slots, in);
        if (handler == null) return true;
        FluidStack contained = handler.drain(Integer.MAX_VALUE, false);
        if (contained == null || contained.amount <= 0) return false;
        FluidType itemType = NTMFluidCapabilityHandler.getFluidType(contained.getFluid());
        if (itemType == null || itemType == Fluids.NONE) return false;
        FluidType tankType = tank.getTankType();
        if (tankType != Fluids.NONE && tankType != itemType) return false;
        int space = tank.getMaxFill() - tank.getFill();
        if (space <= 0) return false;
        int toDrain = Math.min(space, contained.amount);
        if (toDrain <= 0) return false;

        // Prevent Fluid Dupe if stack count is greater than one and output slot is occupied.
        ItemStack stack = slots.getStackInSlot(in);
        if (stack.getCount() > 1 && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) && stack.getItem() instanceof ItemFluidTankV2 fluidTankV2) {
            if (fluidTankV2.cap > space) return false;
        }
        FluidStack drained = handler.drain(toDrain, true);
        if (drained == null || drained.amount <= 0) return false;
        ItemStack container = handler.getContainer();
        if (!slots.insertItem(out, container, true).isEmpty()) {
            tank.setFill(tank.getFill() + drained.amount);
            return true;
        }
        if (tankType == Fluids.NONE) tank.setTankType(itemType);
        slots.extractItem(in, 1, false);
        tank.setFill(tank.getFill() + drained.amount);
        slots.insertItem(out, container, false);
        return true;
    }
}
