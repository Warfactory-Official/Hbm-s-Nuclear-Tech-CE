package com.hbm.blocks.machine;

import com.hbm.blocks.BlockEnums;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

public class SpotlightModular extends Spotlight {

    public SpotlightModular(String name, Material mat, int beamLength, BlockEnums.LightType type, boolean isOn) {
        super(name, mat, beamLength, type, isOn);
    }

    @Override
    public String getPartName(int connectionCount) {
        if (connectionCount == 0) return "FluoroSingle";
        if (connectionCount == 1) return "FluoroCap";
        return "FluoroMid";
    }

    // for correct splitting behavior, doesn't exist upstream
    @Override
    public @NotNull IBlockState getActualState(IBlockState state, @NotNull IBlockAccess world, BlockPos pos) {
        int mask = getSelectedConnectionMask(world, pos, state);
        boolean cu = hasMutualConnection(world, pos, mask, EnumFacing.UP);
        boolean cd = hasMutualConnection(world, pos, mask, EnumFacing.DOWN);
        boolean cn = hasMutualConnection(world, pos, mask, EnumFacing.NORTH);
        boolean cs = hasMutualConnection(world, pos, mask, EnumFacing.SOUTH);
        boolean cw = hasMutualConnection(world, pos, mask, EnumFacing.WEST);
        boolean ce = hasMutualConnection(world, pos, mask, EnumFacing.EAST);
        return state.withProperty(CONN_UP, cu).withProperty(CONN_DOWN, cd).withProperty(CONN_NORTH, cn)
                    .withProperty(CONN_SOUTH, cs).withProperty(CONN_WEST, cw).withProperty(CONN_EAST, ce);
    }

    private boolean hasMutualConnection(IBlockAccess world, BlockPos pos, int ownMask, EnumFacing direction) {
        int bit = 1 << direction.ordinal();
        if ((ownMask & bit) == 0) {
            return false;
        }

        BlockPos neighborPos = pos.offset(direction);
        IBlockState neighborState = world.getBlockState(neighborPos);
        if (neighborState.getBlock() != this) {
            return false;
        }

        int neighborMask = getSelectedConnectionMask(world, neighborPos, neighborState);
        return (neighborMask & (1 << direction.getOpposite().ordinal())) != 0;
    }

    private int getSelectedConnectionMask(IBlockAccess world, BlockPos pos, IBlockState state) {
        EnumFacing facing = state.getValue(FACING);
        EnumFacing primary = null;

        for (EnumFacing direction : EnumFacing.VALUES) {
            if (direction == facing || direction == facing.getOpposite()) {
                continue;
            }
            if (world.getBlockState(pos.offset(direction)).getBlock() == this) {
                primary = direction;
                break;
            }
        }

        if (primary == null) return 0;
        int mask = 1 << primary.ordinal();
        if (world.getBlockState(pos.offset(primary.getOpposite())).getBlock() == this) {
            mask |= 1 << primary.getOpposite().ordinal();
        }

        return mask;
    }
}
