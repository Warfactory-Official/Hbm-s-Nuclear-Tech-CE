package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnumMeta;
import com.hbm.blocks.BlockEnums;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BlockStalagmite extends BlockEnumMeta<BlockEnums.EnumStalagmiteType> {

    public BlockStalagmite(String registryName) {
        super(Material.ROCK, SoundType.STONE, registryName, BlockEnums.EnumStalagmiteType.VALUES, true, true);
    }

    public static int getMetaFromResource(int meta) {
        return meta;
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
    public boolean isNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean causesSuffocation(IBlockState state) {
        return false;
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    private boolean canStay(World world, BlockPos pos) {
        if (this == ModBlocks.stalagmite) {
            BlockPos below = pos.down();
            IBlockState s = world.getBlockState(below);
            return s.isSideSolid(world, below, EnumFacing.UP);
        }
        if (this == ModBlocks.stalactite) {
            BlockPos above = pos.up();
            IBlockState s = world.getBlockState(above);
            return s.isSideSolid(world, above, EnumFacing.DOWN);
        }
        return true;
    }

    @Override
    protected BlockBakeFrame[] generateBlockFrames(String registryName) {
        return Arrays.stream(blockEnum)
                     .map(Enum::name)
                     .map(n -> registryName + "." + n.toLowerCase(Locale.US))
                     .map(tex -> new BlockBakeFrame(BlockBakeFrame.BlockForm.CROSS_UNTINTED, tex))
                     .toArray(BlockBakeFrame[]::new);
    }


    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos) && canStay(worldIn, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!canStay(worldIn, pos)) {
            worldIn.destroyBlock(pos, true);
        }
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(this, 1, state.getValue(META));
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        switch (state.getValue(META)) {
            case 0: return Collections.singletonList(new ItemStack(ModItems.sulfur));
            case 1: return Collections.singletonList(new ItemStack(ModItems.powder_asbestos));
            default: return Collections.emptyList();
        }
    }
}
