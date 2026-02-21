package com.hbm.inventory.fluid.tank;

import net.minecraftforge.items.IItemHandler;

public interface IFluidLoadingHandler {

    /**
     * {@inheritDoc}
     * @return true = stop, false = fallthrough to next handler
     */
	boolean fillItem(IItemHandler slots, int in, int out, FluidTankNTM tank);

    /**
     * {@inheritDoc}
     * @return true = stop, false = fallthrough to next handler
     */
	boolean emptyItem(IItemHandler slots, int in, int out, FluidTankNTM tank);
}
