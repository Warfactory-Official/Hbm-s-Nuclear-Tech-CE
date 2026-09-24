package com.hbm.blocks.generic;

import com.hbm.blocks.BlockBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;

import java.util.Random;

public class BlockNoDrop extends BlockBase {

	public BlockNoDrop(Material materialIn, String s) {
		super(materialIn, s);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}
}
