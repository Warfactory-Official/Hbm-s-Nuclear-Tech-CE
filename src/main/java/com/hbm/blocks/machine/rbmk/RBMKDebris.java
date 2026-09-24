package com.hbm.blocks.machine.rbmk;

import com.hbm.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import org.jetbrains.annotations.NotNull;

public class RBMKDebris extends Block {

	public RBMKDebris(String s) {
		super(Material.IRON);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModBlocks.ALL_BLOCKS.add(this);
	}


	@Override
	public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state){
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public boolean isOpaqueCube(@NotNull IBlockState state){
		return false;
	}
	
	@Override
	public boolean isNormalCube(@NotNull IBlockState state){
		return false;
	}
	
	@Override
	public boolean isBlockNormalCube(@NotNull IBlockState state){
		return false;
	}
}
