package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.deco.TileEntityDecoPoleSatelliteReceiver;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class DecoPoleSatelliteReceiver extends BlockContainer implements INBTBlockTransformable {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	public DecoPoleSatelliteReceiver(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public @NotNull BlockFaceShape getBlockFaceShape(@NotNull IBlockAccess worldIn, @NotNull IBlockState state, @NotNull BlockPos pos, @NotNull EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World worldIn, int meta) {
		return new TileEntityDecoPoleSatelliteReceiver();
	}

	@Override
	public @NotNull IBlockState getStateForPlacement(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean isOpaqueCube(@NotNull IBlockState state) {
		return false;
	}

	@Override
	public boolean isBlockNormalCube(@NotNull IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(@NotNull IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos) {
		return false;
	}

	@Override
	public boolean isFullCube(@NotNull IBlockState state) {
		return false;
	}

	@Override
	protected @NotNull BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex();
	}

	@Override
	public @NotNull IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.byIndex(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing);
	}



	@Override
	public @NotNull IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public @NotNull IBlockState withMirror(IBlockState state, Mirror mirrorIn)
	{
	   return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
	}

	@Override
	public int transformMeta(int meta, int coordBaseMode) {
		return INBTBlockTransformable.transformMetaDeco(meta, coordBaseMode);
	}

}
