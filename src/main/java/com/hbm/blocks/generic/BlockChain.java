package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockChain extends Block {

    public static final PropertyBool WALL = PropertyBool.create("wall");
    public static final PropertyBool END = PropertyBool.create("end");
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockChain(Material materialIn, String s) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setDefaultState(this.blockState.getBaseState().withProperty(WALL, false).withProperty(END, false).withProperty(FACING, EnumFacing.NORTH));

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        return true;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        float f = 0.125F;
        float minY = 0;
        if (!source.isSideSolid(pos.down(), EnumFacing.UP, false))
            minY = 0.25F;
        int meta = state.getValue(FACING).ordinal();

        if (!state.getValue(WALL)) {
            return new AxisAlignedBB(3 * f, minY, 3 * f, 5 * f, 1, 5 * f);
        }

        return switch (meta) {
            case 2 -> new AxisAlignedBB(3 * f, minY, 1.0F - f, 5 * f, 1.0F, 1.0F);
            case 3 -> new AxisAlignedBB(3 * f, minY, 0.0F, 5 * f, 1.0F, f);
            case 4 -> new AxisAlignedBB(1.0F - f, minY, 3 * f, 1.0F, 1.0F, 5 * f);
            case 5 -> new AxisAlignedBB(0.0F, minY, 3 * f, f, 1.0F, 5 * f);
            default -> new AxisAlignedBB(3 * f, minY, 3 * f, 5 * f, 1, 5 * f);
        };
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        if (world.isSideSolid(pos.up(), EnumFacing.DOWN) || world.getBlockState(pos.up()).getBlock() == this)
            return true;

        return world.isSideSolid(pos.west(), EnumFacing.EAST) ||
                world.isSideSolid(pos.east(), EnumFacing.WEST) ||
                world.isSideSolid(pos.north(), EnumFacing.SOUTH) ||
                world.isSideSolid(pos.south(), EnumFacing.NORTH);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        int j1 = meta;

        if (facing.ordinal() == 2 && world.isSideSolid(pos.south(), EnumFacing.NORTH))
            j1 = 2;

        if (facing.ordinal() == 3 && world.isSideSolid(pos.north(), EnumFacing.SOUTH))
            j1 = 3;

        if (facing.ordinal() == 4 && world.isSideSolid(pos.east(), EnumFacing.WEST))
            j1 = 4;

        if (facing.ordinal() == 5 && world.isSideSolid(pos.west(), EnumFacing.EAST))
            j1 = 5;

        boolean end = true;
        if (world.getBlockState(pos.down()).getBlock() == this && (j1 != 0) == world.getBlockState(pos.down()).getValue(WALL) || world.isSideSolid(pos.down(), EnumFacing.UP))
            end = false;

        if (j1 == 0) {
            if (world.getBlockState(pos.up()).getBlock() == this)
                return this.getDefaultState().withProperty(FACING, world.getBlockState(pos.up()).getValue(FACING)).withProperty(WALL, world.getBlockState(pos.up()).getValue(WALL)).withProperty(END, end);

            if (world.isSideSolid(pos.up(), EnumFacing.DOWN))
                return this.getDefaultState().withProperty(FACING, EnumFacing.NORTH).withProperty(WALL, false).withProperty(END, end);
        }

        if (j1 == 0) {
            if (world.isSideSolid(pos.south(), EnumFacing.NORTH))
                j1 = 2;

            if (world.isSideSolid(pos.north(), EnumFacing.SOUTH))
                j1 = 3;

            if (world.isSideSolid(pos.east(), EnumFacing.WEST))
                j1 = 4;

            if (world.isSideSolid(pos.west(), EnumFacing.EAST))
                j1 = 5;
        }
        return this.getDefaultState().withProperty(FACING, j1 > 0 ? EnumFacing.VALUES[j1] : EnumFacing.NORTH).withProperty(WALL, j1 != 0).withProperty(END, end);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        int l = state.getValue(FACING).ordinal();
        boolean flag = false;

        if (world.getBlockState(pos.down()).getBlock() == this && world.getBlockState(pos).getValue(WALL) == world.getBlockState(pos.down()).getValue(WALL) || world.isSideSolid(pos.down(), EnumFacing.UP)) {
            world.setBlockState(pos, state.withProperty(END, false));
        } else {
            world.setBlockState(pos, state.withProperty(END, true));
        }

        if (world.getBlockState(pos.up()).getBlock() == this && world.getBlockState(pos).getValue(WALL) == world.getBlockState(pos.up()).getValue(WALL)) {
            return;
        }

        if (world.isSideSolid(pos.up(), EnumFacing.DOWN) && !world.getBlockState(pos).getValue(WALL)) {
            return;
        }

        if (l == 2 && world.isSideSolid(pos.south(), EnumFacing.NORTH))
            flag = true;

        if (l == 3 && world.isSideSolid(pos.north(), EnumFacing.SOUTH))
            flag = true;

        if (l == 4 && world.isSideSolid(pos.east(), EnumFacing.WEST))
            flag = true;

        if (l == 5 && world.isSideSolid(pos.west(), EnumFacing.EAST))
            flag = true;

        if (!flag)
            world.destroyBlock(pos, true);

    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, WALL, END, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int wall = state.getValue(WALL) ? 1 : 0;
        int end = state.getValue(END) ? 1 : 0;
        int facing = state.getValue(FACING).ordinal() - 2;
        return (wall << 3) | (end << 2) | facing;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(WALL, ((meta >> 3) & 1) > 0 ? true : false)
                .withProperty(END, ((meta >> 2) & 1) > 0 ? true : false)
                .withProperty(FACING, EnumFacing.VALUES[(meta & 2) + 2]);
    }


}
