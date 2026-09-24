package com.hbm.blocks.machine;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class BlockCMGlass<E extends Enum<E>> extends BlockCM<E> {

	public BlockCMGlass(String registryName, E[] blockEnum) {
		super(registryName, blockEnum);
	}

	@Override
	public boolean isOpaqueCube(@NotNull IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(@NotNull IBlockState state) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public @NotNull BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(@NotNull IBlockState blockState, @NotNull IBlockAccess blockAccess, @NotNull BlockPos pos, @NotNull EnumFacing side) {
		IBlockState other = blockAccess.getBlockState(pos.offset(side));
		return other.getBlock() == this ? false : super.shouldSideBeRendered(blockState, blockAccess, pos, side);
	}
}
