package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BlockHangingVine extends Block implements IShearable {

	public static final PropertyEnum<VinePart> PART = PropertyEnum.create("part", VinePart.class);

	public BlockHangingVine(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setSoundType(SoundType.PLANT);
		this.setDefaultState(this.blockState.getBaseState().withProperty(PART, VinePart.HANG));

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, PART);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		BlockPos below = pos.down();
		IBlockState under = world.getBlockState(below);

		if(under.getBlockFaceShape(world, below, EnumFacing.UP) == BlockFaceShape.SOLID)
			return state.withProperty(PART, VinePart.GROUND);
		if(under.getBlock() == this)
			return state.withProperty(PART, VinePart.MIDDLE);

		return state.withProperty(PART, VinePart.HANG);
	}

	@Override
	public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
		entity.motionX *= 0.5;
		entity.motionY *= 0.5;
		entity.motionZ *= 0.5;
		entity.fallDistance = 0F;
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		return this.canBlockStay(world, pos);
	}

	public boolean canBlockStay(World world, BlockPos pos) {
		BlockPos above = pos.up();
		IBlockState over = world.getBlockState(above);
		return over.getBlockFaceShape(world, above, EnumFacing.DOWN) == BlockFaceShape.SOLID || over.getBlock() == this;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if(!world.isRemote && !this.canBlockStay(world, pos))
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
	}

	@Override
	protected boolean canSilkHarvest() {
		return true;
	}

	@Override
	public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
		return true;
	}

	@Override
	public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
		ArrayList<ItemStack> ret = new ArrayList<>();
		ret.add(new ItemStack(this));
		return ret;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return NULL_AABB;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	public enum VinePart implements IStringSerializable {
		GROUND, MIDDLE, HANG;

		@Override
		public String getName() {
			return this.name().toLowerCase(Locale.US);
		}
	}
}
