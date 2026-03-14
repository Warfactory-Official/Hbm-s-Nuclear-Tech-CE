package com.hbm.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public interface IBlockMulti {

    int getSubCount();

    default String getTranslationKey(ItemStack stack) {
        return ((Block)this).getTranslationKey();
    }

    default String getOverrideDisplayName(ItemStack stack) {
        return null;
    }

    default int rectify(int meta) {
        return Math.abs(meta % getSubCount());
    }
}