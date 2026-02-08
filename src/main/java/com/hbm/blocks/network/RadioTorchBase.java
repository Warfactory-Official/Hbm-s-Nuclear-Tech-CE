package com.hbm.blocks.network;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.main.MainRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public abstract class RadioTorchBase extends BlockContainer implements ILookOverlay, ITooltipProvider {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    public static final PropertyBool LIT = PropertyBool.create("lit");

    public RadioTorchBase() {
        super(Material.CIRCUITS);
        setSoundType(SoundType.WOOD);
        setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.UP).withProperty(LIT, false));
    }

    @Override
    public @NotNull IBlockState getStateForPlacement(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @NotNull EntityLivingBase placer) {

        return this.getDefaultState().withProperty(FACING, facing).withProperty(LIT, false);
    }

    @Override
    protected @NotNull BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, LIT);
    }

    @Override
    public @NotNull IBlockState getStateFromMeta(int meta) {
        boolean lit = (meta & 1) == 1;
        EnumFacing facing = EnumFacing.byIndex((meta >> 1) & 7);
        return getDefaultState().withProperty(LIT, lit).withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int m = state.getValue(LIT) ? 1 : 0;
        m |= (state.getValue(FACING).getIndex() << 1);
        return m;
    }

    @Override
    public @NotNull IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public @NotNull BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
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
    public boolean shouldSideBeRendered(@NotNull IBlockState blockState, @NotNull IBlockAccess blockAccess, @NotNull BlockPos pos, @NotNull EnumFacing side) {
        return true;
    }

    @Override
    public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    private static AxisAlignedBB makeAABBFor(EnumFacing dir) {
        double minX = dir.getXOffset() == 1 ? 0.0 : 0.375;
        double minY = dir.getYOffset() == 1 ? 0.0 : 0.375;
        double minZ = dir.getZOffset() == 1 ? 0.0 : 0.375;

        double maxX = dir.getXOffset() == -1 ? 1.0 : 0.625;
        double maxY = dir.getYOffset() == -1 ? 1.0 : 0.625;
        double maxZ = dir.getZOffset() == -1 ? 1.0 : 0.625;

        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public @NotNull AxisAlignedBB getBoundingBox(IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos) {
        return makeAABBFor(state.getValue(FACING));
    }

    @Override
    public @NotNull AxisAlignedBB getSelectedBoundingBox(@NotNull IBlockState state, @NotNull World worldIn, @NotNull BlockPos pos) {
        return getBoundingBox(state, worldIn, pos).offset(pos);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(@NotNull IBlockState blockState, @NotNull IBlockAccess worldIn, @NotNull BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public void neighborChanged(IBlockState state, @NotNull World worldIn, @NotNull BlockPos pos, @NotNull Block blockIn, @NotNull BlockPos fromPos) {
        EnumFacing dir = state.getValue(FACING);
        BlockPos checkPos = pos.offset(dir.getOpposite());
        IBlockState checkState = worldIn.getBlockState(checkPos);
        Block b = checkState.getBlock();

        if (!canBlockStay(worldIn, dir, b, checkPos, checkState)) {
            worldIn.destroyBlock(pos, true);
        }
    }

    @Override
    public @NotNull BlockFaceShape getBlockFaceShape(@NotNull IBlockAccess worldIn, @NotNull IBlockState state, @NotNull BlockPos pos, @NotNull EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean canPlaceBlockOnSide(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumFacing side) {
        if (!super.canPlaceBlockOnSide(worldIn, pos, side)) return false;
        BlockPos checkPos = pos.offset(side.getOpposite());
        IBlockState checkState = worldIn.getBlockState(checkPos);

        return canBlockStay(worldIn, side, checkState.getBlock(), checkPos, checkState);
    }

    public boolean canBlockStay(World world, EnumFacing dir, Block b, BlockPos checkPos, IBlockState checkState) {
        return checkState.isSideSolid(world, checkPos, dir) || b.hasComparatorInputOverride(checkState) || b.canProvidePower(checkState) || (checkState.isFullCube() && !b.isAir(checkState, world, checkPos));
    }

    @Override
    public boolean onBlockActivated(World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer playerIn, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote && !playerIn.isSneaking()) {
            FMLNetworkHandler.openGui(playerIn, MainRegistry.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        } else {
            return !playerIn.isSneaking();
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip, @NotNull ITooltipFlag flagIn) {
        addStandardInfo(tooltip);
    }
}
