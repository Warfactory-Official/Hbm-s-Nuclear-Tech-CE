package com.hbm.core;

import net.minecraft.block.BlockAir;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

/// used for shitass ASM
public class BlockMetaAir extends BlockAir {
	public static final PropertyInteger META = PropertyInteger.create("meta",0,15);
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this,META);
	}
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(META);
	}
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(META,meta);
	}
}
