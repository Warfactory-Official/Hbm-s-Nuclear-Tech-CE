package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.machine.TileEntityGeiger;
import com.hbm.util.ContaminationUtil;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
// Th3_Sl1ze: I've decided to re-port it, fuck this
// also baked model doesn't do what I need it to do, will stick for .json here as a workaround for now
public class GeigerCounter extends BlockContainer {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	private static final AxisAlignedBB AABB_NORTH = new AxisAlignedBB(0.09375D, 0.0D, 0.0D, 1.0D, 0.5625D, 0.875D);
	private static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(0.0D, 0.0D, 0.125D, 0.90625D, 0.5625D, 1.0D);
	private static final AxisAlignedBB AABB_WEST = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.875D, 0.5625D, 0.90625D);
	private static final AxisAlignedBB AABB_EAST = new AxisAlignedBB(0.125D, 0.0D, 0.09375D, 1.0D, 0.5625D, 1.0D);
	private static final AxisAlignedBB AABB_DEFAULT = new AxisAlignedBB(0.0D, 0.0D, 0.125D, 1.0D, 1.0D, 0.875D);

	public GeigerCounter(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		this.setLightOpacity(0);
		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityGeiger();
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
	public boolean causesSuffocation(IBlockState state) {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return getShapeForFacing(state.getValue(FACING));
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return getShapeForFacing(state.getValue(FACING));
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState) {
		AxisAlignedBB axisAlignedBB = getShapeForFacing(state.getValue(FACING)).offset(pos);
		if (entityBox.intersects(axisAlignedBB)) {
			collidingBoxes.add(axisAlignedBB);
		}
	}

	private AxisAlignedBB getShapeForFacing(EnumFacing facing) {
		return switch (facing) {
			case NORTH -> AABB_NORTH;
			case SOUTH -> AABB_SOUTH;
			case WEST -> AABB_WEST;
			case EAST -> AABB_EAST;
			default -> AABB_DEFAULT;
		};
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.fromAngle(placer.rotationYaw));
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		EnumFacing enumFacing = EnumFacing.fromAngle(placer.rotationYaw);
		worldIn.setBlockState(pos, state.withProperty(FACING, enumFacing), 2);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (hand != EnumHand.MAIN_HAND) {
			return true;
		}

		if (worldIn.isRemote) {
			return true;
		}

		if (!playerIn.isSneaking()) {
			SoundEvent soundEvent = SoundEvent.REGISTRY.getObject(new ResourceLocation("hbm", "item.techBoop"));
			if (soundEvent != null) {
				worldIn.playSound(null, pos, soundEvent, SoundCategory.PLAYERS, 1.0F, 1.0F);
			}
			ContaminationUtil.printGeigerData(playerIn);
			return true;
		}

		return false;
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState state, World worldIn, BlockPos pos) {
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (!(tileEntity instanceof TileEntityGeiger tileEntityGeiger)) {
			return 0;
		}

		double radiation = tileEntityGeiger.check();
		return Math.min((int) Math.ceil(radiation / 5.0F), 15);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing facing = switch (meta) {
			case 2 -> EnumFacing.NORTH;
			case 4 -> EnumFacing.WEST;
			case 5 -> EnumFacing.EAST;
			default -> EnumFacing.SOUTH;
		};
		return this.getDefaultState().withProperty(FACING, facing);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return switch (state.getValue(FACING)) {
			case NORTH -> 2;
			case WEST -> 4;
			case EAST -> 5;
			default -> 3;
		};
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
		return state.withProperty(FACING, mirrorIn.mirror(state.getValue(FACING)));
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}
}
