package com.hbm.blocks.machine;

import com.hbm.blocks.ISpotlight;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockBakeBase;
import com.hbm.main.MainRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class TritiumLamp extends BlockBakeBase implements ISpotlight {

  private final boolean isOn;

  public TritiumLamp(String name, boolean isOn) {
    super(Material.REDSTONE_LIGHT, name);

    setSoundType(SoundType.GLASS);
    setCreativeTab(!isOn ? MainRegistry.blockTab : null);

    this.isOn = isOn;

    if (isOn) {
      this.setLightLevel(1.0F);
    }
  }

  @Override
  public void onBlockAdded(World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {

    if (!worldIn.isRemote) {

      if (this.isOn && !worldIn.isBlockPowered(pos)) {
        worldIn.scheduleBlockUpdate(pos, this, 4, 0);

      } else if (!this.isOn && worldIn.isBlockPowered(pos)) {
        worldIn.setBlockState(pos, getOn().getDefaultState(), 2);
      }

      updateBeam(worldIn, pos);
    }
  }

  @Override
  public void neighborChanged(
      @NotNull IBlockState state,
      World world,
      @NotNull BlockPos pos,
      @NotNull Block blockIn,
      @NotNull BlockPos fromPos) {

    if (!world.isRemote) {

      if (this.isOn && !world.isBlockPowered(pos)) {
        world.scheduleBlockUpdate(pos, this, 4, 0);

      } else if (!this.isOn && world.isBlockPowered(pos)) {
        world.setBlockState(pos, getOn().getDefaultState(), 2);
      }

      updateBeam(world, pos);
    }
  }

  @Override
  public void updateTick(
      @NotNull World world,
      @NotNull BlockPos pos,
      @NotNull IBlockState state,
      @NotNull Random rand) {

    if (!world.isRemote && this.isOn && !world.isBlockPowered(pos)) {
      world.setBlockState(pos, getOff().getDefaultState(), 2);
    }
  }

  @Override
  public void breakBlock(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
    super.breakBlock(world, pos, state);
    if (world.isRemote) return;

    for (EnumFacing facing : EnumFacing.VALUES) Spotlight.unpropagateBeam(world, pos, facing);
  }

  private void updateBeam(World world, BlockPos pos) {
    if (!isOn) return;

    for (EnumFacing facing : EnumFacing.VALUES)
      Spotlight.propagateBeam(world, pos, facing, getBeamLength(), getMeta());
  }

  @Override
  public Item getItemDropped(IBlockState state, Random rand, int fortune) {
    return Item.getItemFromBlock(getOff());
  }

  protected int getMeta() {
    if (this == ModBlocks.lamp_tritium_green_off || this == ModBlocks.lamp_tritium_green_on)
      return Spotlight.META_GREEN;
    if (this == ModBlocks.lamp_tritium_blue_off || this == ModBlocks.lamp_tritium_blue_on)
      return Spotlight.META_BLUE;
    return Spotlight.META_YELLOW;
  }

  protected Block getOff() {
    if (this == ModBlocks.lamp_tritium_green_on) return ModBlocks.lamp_tritium_green_off;
    if (this == ModBlocks.lamp_tritium_blue_on) return ModBlocks.lamp_tritium_blue_off;
    return this;
  }

  protected Block getOn() {
    if (this == ModBlocks.lamp_tritium_green_off) return ModBlocks.lamp_tritium_green_on;
    if (this == ModBlocks.lamp_tritium_blue_off) return ModBlocks.lamp_tritium_blue_on;
    return this;
  }

  @Override
  public int getBeamLength() {
    return 8;
  }
}
