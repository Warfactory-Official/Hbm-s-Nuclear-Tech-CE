package com.hbm.inventory.fluid.tank;

import com.hbm.capability.NTMFluidCapabilityHandler;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class FluidLoaderStandard implements IFluidLoadingHandler {

	@Override
	public boolean fillItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
		if(tank.pressure != 0) return false;
		ItemStack inputStack = slots.extractItem(in, 1, true);
		if(inputStack.isEmpty()) return true;
        if (!NTMFluidCapabilityHandler.isEmptyNtmFluidContainer(inputStack.getItem())) return false;
		FluidType type = tank.getTankType();
		ItemStack full = FluidContainerRegistry.getFullContainer(inputStack, type);//this returns a copy
        if (full == null) return false;
        int remainder = tank.getFill() - FluidContainerRegistry.getFluidContent(full, type);
        if(remainder >= 0) {
            if (inputStack.hasDisplayName())
                full.setStackDisplayName(inputStack.getDisplayName());
			if(slots.insertItem(out, full, true).isEmpty()) {
				tank.setFill(remainder);
				slots.insertItem(out, full, false);
				slots.extractItem(in, 1, false);
			}
		}
		return false;
	}

	@Override
	public boolean emptyItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
		ItemStack inputStack = slots.extractItem(in, 1, true);
		if (inputStack.isEmpty()) return true;
		FluidType itemFluidType = FluidContainerRegistry.getFluidType(inputStack);
		if (itemFluidType == Fluids.NONE) return false;
        //1.7 uses FluidContainerRegistry.getFluidContent(stackIn, tankType) here. logic changed to make
        //empty tanks change its type automatically to the fluid type of the container
		int amount = FluidContainerRegistry.getFluidContent(inputStack, itemFluidType);
		if (amount <= 0) return false;
        if (tank.getTankType() != Fluids.NONE && tank.getTankType() != itemFluidType) return false;
		if (tank.getFill() + amount > tank.getMaxFill()) return false;

		ItemStack emptyContainer = FluidContainerRegistry.getEmptyContainer(inputStack);//a copy
        if (emptyContainer != null && inputStack.hasDisplayName())
            emptyContainer.setStackDisplayName(inputStack.getDisplayName());
        if (emptyContainer == null || slots.insertItem(out, emptyContainer, true).isEmpty()) {
            if (tank.getTankType() == Fluids.NONE) tank.setTankType(itemFluidType);
            tank.setFill(tank.getFill() + amount);
            slots.extractItem(in, 1, false);
            if (emptyContainer != null)
                slots.insertItem(out, emptyContainer, false);
        }
        return true;
	}

}
