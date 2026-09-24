package com.hbm.blocks.network;

import com.hbm.inventory.fluid.FluidType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBlockFluidDuct {

    void changeTypeRecursively(World world, BlockPos pos, FluidType prevType, FluidType type, int loopsRemaining);
}
