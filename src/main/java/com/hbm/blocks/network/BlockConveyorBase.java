package com.hbm.blocks.network;

import com.hbm.api.block.IToolable;
import com.hbm.api.conveyor.IConveyorBelt;
import com.hbm.blocks.ModBlocks;
import com.hbm.entity.item.EntityMovingItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public abstract class BlockConveyorBase extends Block implements IConveyorBelt, IToolable {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    private static final AxisAlignedBB CONVEYOR_BB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.25F, 1.0F);

    BlockConveyorBase(Material materialIn, String s) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public boolean canItemStay(World world, int x, int y, int z, Vec3d itemPos) {
        return true;
    }

    @Override
    public Vec3d getTravelLocation(World world, int x, int y, int z, Vec3d itemPos, double speed) {
        BlockPos pos = new BlockPos(x, y, z);
        EnumFacing dir = this.getTravelDirection(world, pos, itemPos);
        Vec3d snap = this.getClosestSnappingPosition(world, pos, itemPos);
        Vec3d dest = new Vec3d(
                snap.x - dir.getXOffset() * speed,
                snap.y - dir.getYOffset() * speed,
                snap.z - dir.getZOffset() * speed
        );
        Vec3d delta = dest.subtract(itemPos);
        double d2 = delta.lengthSquared();
        if (d2 < 1.0e-12) {
            return new Vec3d(
                    itemPos.x - dir.getXOffset() * speed,
                    itemPos.y - dir.getYOffset() * speed,
                    itemPos.z - dir.getZOffset() * speed
            );
        }
        double inv = speed / Math.sqrt(d2);
        return new Vec3d(
                itemPos.x + delta.x * inv,
                itemPos.y + delta.y * inv,
                itemPos.z + delta.z * inv
        );
    }

    public EnumFacing getInputDirection(World world, BlockPos pos) {
        return world.getBlockState(pos).getValue(FACING);
    }

    public EnumFacing getOutputDirection(World world, BlockPos pos) {
        return world.getBlockState(pos).getValue(FACING).getOpposite();
    }

    public EnumFacing getTravelDirection(World world, BlockPos pos, Vec3d itemPos) {
        return world.getBlockState(pos).getValue(FACING);
    }

    @Override
    public Vec3d getClosestSnappingPosition(World world, BlockPos pos, Vec3d itemPos) {
        EnumFacing dir = this.getTravelDirection(world, pos, itemPos);

        double posX = MathHelper.clamp(itemPos.x, pos.getX(), pos.getX() + 1);
        double posZ = MathHelper.clamp(itemPos.z, pos.getZ(), pos.getZ() + 1);

        double x = pos.getX() + 0.5;
        double z = pos.getZ() + 0.5;
        double y = pos.getY() + 0.25;

        if (dir.getAxis() == EnumFacing.Axis.X) {
            x = posX;
        } else if (dir.getAxis() == EnumFacing.Axis.Z) {
            z = posZ;
        }

        return new Vec3d(x, y, z);
    }

    @Override
    public void onEntityCollision(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull Entity entity) {
        if (!world.isRemote) {

            if (entity instanceof EntityItem && entity.ticksExisted > 10 && !entity.isDead) {

                EntityMovingItem item = new EntityMovingItem(world);
                item.setItemStack(((EntityItem) entity).getItem());
                Vec3d entityPos = new Vec3d(entity.posX, entity.posY, entity.posZ);
                Vec3d snap = this.getClosestSnappingPosition(world, pos, entityPos);
                item.setPositionAndRotation(snap.x, snap.y, snap.z, 0, 0);
                world.spawnEntity(item);

                entity.setDead();
            }
        }
    }


    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean isFullCube(@NotNull IBlockState state) {
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
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public @NotNull BlockFaceShape getBlockFaceShape(@NotNull IBlockAccess worldIn, @NotNull IBlockState state, @NotNull BlockPos pos,
                                                     @NotNull EnumFacing face) {
        return BlockFaceShape.CENTER;
    }

    @Override
    public @NotNull AxisAlignedBB getBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos) {
        return CONVEYOR_BB;
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

        if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand,
                           IToolable.ToolType tool) {
        if (tool != IToolable.ToolType.SCREWDRIVER) {
            return false;
        }

        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (!player.isSneaking()) {
            world.setBlockState(pos, state.withRotation(Rotation.CLOCKWISE_90), 3);
        } else {
            if (block instanceof BlockConveyorChute) {
                int type = state.getValue(BlockConveyorChute.TYPE);
                int newType = (type + 1) % 3; // 0 -> 1 -> 2 -> 0
                world.setBlockState(pos, state.withProperty(BlockConveyorChute.TYPE, newType), 3);
            }
        }

        return true;
    }

    @Override
    public @NotNull IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }
}
