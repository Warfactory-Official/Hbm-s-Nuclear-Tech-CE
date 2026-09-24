package com.hbm.blocks.machine.rbmk;

import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKOutlet;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class RBMKOutlet extends BlockContainer {

	public RBMKOutlet(Material mat, String s) {
		super(mat);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		return new TileEntityRBMKOutlet();
	}
	
	@Override
	public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state){
		return EnumBlockRenderType.MODEL;
	}
}
