package com.hbm.blocks;

import com.hbm.lib.TriFunction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import java.util.Random;
import java.util.function.BiFunction;

public interface IOreType {
    BiFunction<IBlockState, Random, ItemStack> getDropFunction();
    TriFunction<IBlockState, Integer, Random, Integer> getQuantityFunction();
}
