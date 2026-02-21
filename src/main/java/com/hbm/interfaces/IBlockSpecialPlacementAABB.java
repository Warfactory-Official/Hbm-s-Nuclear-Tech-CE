package com.hbm.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBlockSpecialPlacementAABB {
    AxisAlignedBB getCollisionBoundingBoxForPlacement(World worldIn, BlockPos pos, ItemStack stack);
}
