package com.hbm.blocks.generic;

import com.hbm.blocks.machine.ZirnoxDestroyed;
import com.hbm.blocks.machine.rbmk.RBMKDebris;
import com.hbm.main.MainRegistry;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BlockLayering extends BlockBakeBase {
  public BlockLayering(Material material, String name, SoundType type, String texture) {
    super(material, name, new BlockBakeFrame(BlockBakeFrame.BlockForm.LAYER, texture));
    setSoundType(type);
    setHarvestLevel("pickaxe", 0);
    setCreativeTab(MainRegistry.blockTab);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public @NotNull BlockRenderLayer getRenderLayer() {
    return BlockRenderLayer.CUTOUT;
  }

  @SuppressWarnings("deprecation")
  @Override
  public @NotNull AxisAlignedBB getBoundingBox(
      @NotNull IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos) {
    int meta = getMetaFromState(state) & 7;
    float height = (2 * (1 + meta)) / 16.0F;
    return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, height, 1.0D);
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean isOpaqueCube(@NotNull IBlockState state) {
    return false;
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean isFullCube(@NotNull IBlockState state) {
    return false;
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean canPlaceBlockAt(World world, BlockPos pos) {
    IBlockState state = world.getBlockState(pos.down());

    Block blockBelow = state.getBlock();

    if (blockBelow instanceof RBMKDebris || blockBelow instanceof ZirnoxDestroyed) {
      return true;
    }

    return blockBelow != Blocks.ICE
        && blockBelow != Blocks.PACKED_ICE
        && (blockBelow.isLeaves(state, world, pos.down())
            || (blockBelow == this && (getMetaFromState(state) & 7) == 7
                || blockBelow.isOpaqueCube(state)
                    && blockBelow.getMaterial(state).blocksMovement()));
  }

  @SuppressWarnings("deprecation")
  @Override
  public void neighborChanged(
      @NotNull IBlockState state,
      @NotNull World world,
      @NotNull BlockPos pos,
      @NotNull Block blockIn,
      @NotNull BlockPos fromPos) {
    if (!this.canPlaceBlockAt(world, pos)) {
      world.setBlockToAir(pos);
    }
  }

  @Override
  public void harvestBlock(
      @NotNull World world,
      @NotNull EntityPlayer player,
      @NotNull BlockPos pos,
      @NotNull IBlockState state,
      net.minecraft.tileentity.TileEntity te,
      @NotNull ItemStack stack) {
    super.harvestBlock(world, player, pos, state, te, stack);
    world.setBlockToAir(pos);
  }

  @Override
  public @NotNull Item getItemDropped(
      @NotNull IBlockState state, @NotNull Random rand, int fortune) {
    return Items.AIR;
  }

  @Override
  public int quantityDropped(@NotNull IBlockState state, int fortune, @NotNull Random random) {
    return (getMetaFromState(state) & 7) + 1;
  }

  @Override
  public boolean isReplaceable(IBlockAccess world, @NotNull BlockPos pos) {
    IBlockState state = world.getBlockState(pos);
    int meta = getMetaFromState(state);
    return meta < 7 && this.material.isReplaceable();
  }

  @SuppressWarnings("deprecation")
  @Override
  @SideOnly(Side.CLIENT)
  public boolean shouldSideBeRendered(
      @NotNull IBlockState blockState,
      @NotNull IBlockAccess blockAccess,
      @NotNull BlockPos pos,
      @NotNull EnumFacing side) {
    return side == EnumFacing.UP || super.shouldSideBeRendered(blockState, blockAccess, pos, side);
  }
}
