package com.hbm.blocks.machine;

import com.hbm.tileentity.machine.TileEntityMachineTapeDrive;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class MachineTapeDrive extends BlockMachineBase {

	private static final AxisAlignedBB BB_FULL = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
	private static final AxisAlignedBB BB_EAST = new AxisAlignedBB(0, 0, 0, 0.75D, 1, 1);
	private static final AxisAlignedBB BB_WEST = new AxisAlignedBB(0.25D, 0, 0, 1, 1, 1);
	private static final AxisAlignedBB BB_SOUTH = new AxisAlignedBB(0, 0, 0, 1, 1, 0.75D);
	private static final AxisAlignedBB BB_NORTH = new AxisAlignedBB(0, 0, 0.25D, 1, 1, 1);

	public MachineTapeDrive(Material materialIn, String s) {
		super(materialIn, 0, s);
	}

	@Override
	protected boolean rotatable() {
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		return new TileEntityMachineTapeDrive();
	}

	@Override
	public @NotNull AxisAlignedBB getBoundingBox(IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos) {
		if(!state.getPropertyKeys().contains(BlockHorizontal.FACING)) return BB_FULL;

		switch(state.getValue(BlockHorizontal.FACING)) {
		case EAST: return BB_EAST;
		case WEST: return BB_WEST;
		case SOUTH: return BB_SOUTH;
		case NORTH: return BB_NORTH;
		default: return BB_FULL;
		}
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
	public boolean isFullBlock(@NotNull IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(@NotNull IBlockState state) {
		return false;
	}

	@Override
	public boolean isBlockNormalCube(@NotNull IBlockState state) {
		return false;
	}

	@Override
	public @NotNull BlockFaceShape getBlockFaceShape(@NotNull IBlockAccess world, @NotNull IBlockState state, @NotNull BlockPos pos, @NotNull EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}
}
