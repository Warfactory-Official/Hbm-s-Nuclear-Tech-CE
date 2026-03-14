package com.hbm.inventory.fluid.tank;

import com.hbm.capability.NTMFluidCapabilityHandler;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class FluidLoaderForge implements IFluidLoadingHandler {

    private static @Nullable IFluidHandlerItem getIFluidHandler(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (NTMFluidCapabilityHandler.isNtmFluidContainer(stack.getItem())) return null;
        if (!stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) return null;
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
    }

    @Override
    public boolean fillItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
        if (tank.pressure != 0 || tank.getFill() <= 0) return false;
        FluidType tankType = tank.getTankType();
        if (tankType == Fluids.NONE) return false;
        Fluid forgeFluid = tankType.getFF();
        if (forgeFluid == null) return true;
        ItemStack inputStack = slots.getStackInSlot(in);
        ItemStack singleItem = slots.extractItem(in, 1, true);
        IFluidHandlerItem handler = getIFluidHandler(singleItem);
        if (handler == null) return false;
        int offer = tank.getFill();
        int canFill = handler.fill(new FluidStack(forgeFluid, offer), false);
        if (canFill <= 0) return false;
        int actualFill = handler.fill(new FluidStack(forgeFluid, canFill), true);
        if (actualFill <= 0) return false;
        ItemStack container = handler.getContainer();
        if (inputStack.getCount() > 1) {
            if (!slots.insertItem(out, container, true).isEmpty()) return false;
            slots.extractItem(in, 1, false);
            tank.setFill(tank.getFill() - actualFill);
            slots.insertItem(out, container, false);
            return true;
        }

        boolean wouldEmptyTank = tank.getFill() - actualFill == 0;
        boolean preferOut = handler.fill(new FluidStack(forgeFluid, 1), false) <= 0 || wouldEmptyTank;
        boolean toOut = preferOut && slots.insertItem(out, container, true).isEmpty();
        slots.extractItem(in, 1, false);
        tank.setFill(tank.getFill() - actualFill);
        slots.insertItem(toOut ? out : in, container, false);
        return true;
    }

    @Override
    public boolean emptyItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
        ItemStack inputStack = slots.getStackInSlot(in);
        ItemStack singleItem = slots.extractItem(in, 1, true);
        IFluidHandlerItem handler = getIFluidHandler(singleItem);
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
        FluidStack drained = handler.drain(toDrain, true);
        if (drained == null || drained.amount <= 0) return false;
        ItemStack container = handler.getContainer();
        if (inputStack.getCount() > 1) {
            if (!slots.insertItem(out, container, true).isEmpty()) return false;
            if (tankType == Fluids.NONE) tank.setTankType(itemType);
            slots.extractItem(in, 1, false);
            tank.setFill(tank.getFill() + drained.amount);
            slots.insertItem(out, container, false);
            return true;
        }

        FluidStack remaining = handler.drain(Integer.MAX_VALUE, false);
        boolean wouldFillTank = tank.getFill() + drained.amount >= tank.getMaxFill();
        boolean preferOut = remaining == null || remaining.amount <= 0 || wouldFillTank;
        boolean toOut = preferOut && slots.insertItem(out, container, true).isEmpty();
        if (tankType == Fluids.NONE) tank.setTankType(itemType);
        slots.extractItem(in, 1, false);
        tank.setFill(tank.getFill() + drained.amount);
        slots.insertItem(toOut ? out : in, container, false);
        return true;
    }
}
