package com.hbm.inventory.fluid.tank;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.tool.ItemFluidContainerInfinite;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.Random;

public class FluidLoaderInfinite implements IFluidLoadingHandler {

	private static Random rand = new Random();

	@Override
	public boolean fillItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {

        ItemStack inputStack = slots.getStackInSlot(in);
        if(inputStack.isEmpty() || !(inputStack.getItem() instanceof ItemFluidContainerInfinite item)) return false;

        if(!item.allowPressure(tank.pressure)) return false;
		if(item.getType() != null && tank.type != item.getType()) return false;

		if(item.getChance() <= 1 || rand.nextInt(item.getChance()) == 0) {
			tank.setFill(Math.max(tank.getFill() - item.getAmount() * inputStack.getCount(), 0));
		}

		return true;
	}

	@Override
	public boolean emptyItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
		ItemStack inputStack = slots.getStackInSlot(in);
		if (inputStack.isEmpty() || !(inputStack.getItem() instanceof ItemFluidContainerInfinite item)) {
			return false;
		}
        FluidType itemType = item.getType();
		if (tank.getTankType() == Fluids.NONE) {
			if (itemType != null) {
				tank.setTankType(itemType);
			} else return false;
		}
		if (itemType != null && tank.getTankType() != itemType) return false;

		if (item.getChance() <= 1 || rand.nextInt(item.getChance()) == 0) {
			tank.setFill(Math.min(tank.getFill() + item.getAmount() * inputStack.getCount(), tank.getMaxFill()));
		}

		return true;
	}
}