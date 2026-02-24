package com.hbm.blocks.machine;

import com.hbm.tileentity.TileEntityData;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SpotlightBeam extends BlockBeamBase {

  public SpotlightBeam(String name) {
    super(name);
  }

  // If a block is placed onto the beam, handle the new cutoff
  @Override
  public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
    if (!worldIn.isRemote) {
      for (EnumFacing facing : getDirections(worldIn, pos)) {
        Spotlight.unpropagateBeam(worldIn, pos, facing);
      }
    }

    super.breakBlock(worldIn, pos, state);
  }

  // If a block in the beam path is removed, repropagate beam
  @Override
  public void neighborChanged(
      @NotNull IBlockState state,
      World world,
      @NotNull BlockPos pos,
      @NotNull Block blockIn,
      @NotNull BlockPos fromPos) {
    if (world.isRemote) return;
    if (blockIn instanceof SpotlightBeam) return;

    for (EnumFacing facing : getDirections(world, pos)) {
      Spotlight.backPropagate(world, pos, facing);
    }
  }

  // Directions are stored as a set of 6 bits:
  // 000000 -> no incoming light directions are set, will be removed
  // 010000 -> UP bit set, at least one direction is providing light
  // 111111 -> ALL directions illuminated, all incoming lights need to be disabled to turn off the
  // beam
  public static List<EnumFacing> getDirections(World world, BlockPos pos) {
    TileEntityData te = (TileEntityData) world.getTileEntity(pos);
    if (te == null) return new ArrayList<>();
    return getDirections(te.metadata);
  }

  public static List<EnumFacing> getDirections(int metadata) {
    List<EnumFacing> directions = new ArrayList<>(6);
    for (EnumFacing facing : EnumFacing.VALUES) {
      int flag = 1 << facing.getIndex();
      if ((metadata & flag) != 0) directions.add(facing);
    }
    return directions;
  }

  // Returns the final metadata, so the caller can optionally remove the block
  public static int setDirection(World world, BlockPos pos, EnumFacing facing, boolean state) {
    TileEntityData te = (TileEntityData) world.getTileEntity(pos);
    if (te == null) return 0; // This shouldn't happen, and if it does, cancel propagation
    int transformedMetadata = applyDirection(te.metadata, facing, state);
    te.metadata = transformedMetadata;
    return transformedMetadata;
  }

  // Sets the metadata bit for a given direction
  public static int applyDirection(int metadata, EnumFacing facing, boolean state) {
    int flag = 1 << facing.getIndex();
    if (state) {
      return metadata | flag;
    } else {
      return metadata & ~flag;
    }
  }

  @Override
  public TileEntity createNewTileEntity(@NotNull World world, int meta) {
    return new TileEntityData();
  }
}
