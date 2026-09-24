package com.hbm.blocks.generic;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;

public class LogicBlockInvis extends LogicBlock {

    public LogicBlockInvis(String regName) {
        super(regName);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }
}
