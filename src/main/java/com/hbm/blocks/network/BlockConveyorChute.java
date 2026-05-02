package com.hbm.blocks.network;

import com.hbm.api.conveyor.IConveyorBelt;
import com.hbm.api.conveyor.IEnterableBlock;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BlockConveyorChute extends BlockConveyorBase {
    public static final PropertyInteger TYPE = PropertyInteger.create("type", 0, 2); //Bottom 0, Middle 1, Input 2

    public BlockConveyorChute(Material materialIn, String s) {
        super(materialIn, s);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(TYPE, 0));
    }

    @Override
    public Vec3d getTravelLocation(World world, int x, int y, int z, Vec3d itemPos, double speed) {
        BlockPos pos = new BlockPos(x, y, z);
        Block belowBlock = world.getBlockState(pos.down()).getBlock();

        if (belowBlock instanceof IConveyorBelt || belowBlock instanceof IEnterableBlock) {
            speed *= 5;
        } else if (itemPos.y > pos.getY() + 0.25) {
            speed *= 3;
        }

        return super.getTravelLocation(world, x, y, z, itemPos, speed);
    }

    @Override
    public EnumFacing getInputDirection(World world, BlockPos pos) {
        return world.getBlockState(pos).getValue(FACING);
    }

    @Override
    public EnumFacing getOutputDirection(World world, BlockPos pos) {
        return EnumFacing.DOWN;
    }

    @Override
    public Vec3d getClosestSnappingPosition(World world, BlockPos pos, Vec3d itemPos) {
        Block below = world.getBlockState(pos.down()).getBlock();
        if (below instanceof IConveyorBelt || below instanceof IEnterableBlock || itemPos.y > pos.getY() + 0.25) {
            return new Vec3d(pos.getX() + 0.5, itemPos.y, pos.getZ() + 0.5);
        } else {
            return super.getClosestSnappingPosition(world, pos, itemPos);
        }
    }

    @Override
    public EnumFacing getTravelDirection(World world, BlockPos pos, Vec3d itemPos) {
        Block belowBlock = world.getBlockState(pos.down()).getBlock();

        if (belowBlock instanceof IConveyorBelt || belowBlock instanceof IEnterableBlock || itemPos.y > pos.getY() + 0.25) {
            return EnumFacing.UP;
        }

        return world.getBlockState(pos).getValue(FACING);
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand,
                           ToolType tool) {
        if (tool != ToolType.SCREWDRIVER) {
            return false;
        }

        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);

        if (!player.isSneaking()) {
            world.setBlockState(pos, state.withRotation(Rotation.CLOCKWISE_90), 3);
        } else {
            IBlockState conveyorState = ModBlocks.conveyor.getDefaultState().withProperty(FACING, state.getValue(FACING))
                                                          .withProperty(BlockConveyorBendable.CURVE, BlockConveyorBendable.CurveType.STRAIGHT);
            world.setBlockState(pos, conveyorState, 3);
        }
        return true;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ModItems.conveyor_wand;
    }

    @Override
    public @NotNull IBlockState getStateForPlacement(World worldIn, @NotNull BlockPos pos, @NotNull EnumFacing side, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing facing = placer.getHorizontalFacing().getOpposite();
        return this.getDefaultState().withProperty(FACING, facing).withProperty(TYPE, getUpdatedType(worldIn, pos, facing));
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        world.setBlockState(pos, state.withProperty(TYPE, getUpdatedType(world, pos)), 3);
    }

    public int getUpdatedType(World world, BlockPos pos) {
        return getUpdatedType(world, pos, world.getBlockState(pos).getValue(FACING));
    }

    public int getUpdatedType(World world, BlockPos pos, EnumFacing facing) {
        boolean hasChuteBelow = world.getBlockState(pos.down()).getBlock() instanceof BlockConveyorChute;
        Block inputBlock = world.getBlockState(pos.offset(facing)).getBlock();
        boolean hasInputBelt = (inputBlock instanceof IConveyorBelt || inputBlock instanceof IEnterableBlock);
        if (hasChuteBelow) return hasInputBelt ? 2 : 1;
        return 0;
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
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        return false;
    }

    @Override
    public @NotNull AxisAlignedBB getBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess source, BlockPos pos) {
        return FULL_BLOCK_AABB;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public @NotNull BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, TYPE);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int horizontalIndex = state.getValue(FACING).getHorizontalIndex();
        int type = state.getValue(TYPE);
        return horizontalIndex + (type << 2);
    }

    @Override
    public @NotNull IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byHorizontalIndex(meta & 3);
        int type = (meta >> 2) & 3;
        return this.getDefaultState().withProperty(FACING, facing).withProperty(TYPE, Math.min(type, 2));
    }
}
