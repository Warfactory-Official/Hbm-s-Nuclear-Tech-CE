package com.hbm.blocks.network;

import com.hbm.api.block.IToolable;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class BlockConveyorBendable extends BlockConveyorBase implements IToolable {

    public static final PropertyEnum<CurveType> CURVE = PropertyEnum.create("curve", CurveType.class);

    public BlockConveyorBendable(net.minecraft.block.material.Material materialIn, String name) {
        super(materialIn, name);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(CURVE, CurveType.STRAIGHT));
    }

    @Override
    @NotNull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, CURVE);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumFacing facing = state.getValue(FACING);
        CurveType curve = state.getValue(CURVE);
        int baseMeta = switch (facing) {
            case SOUTH -> 3;
            case WEST -> 4;
            case EAST -> 5;
            default -> 2;
        };
        int curveOffset = switch (curve) {
            case LEFT -> 4;
            case RIGHT -> 8;
            default -> 0;
        };
        return baseMeta + curveOffset;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        CurveType curve;
        int baseMeta;
        if (meta >= 10 && meta <= 13) {
            curve = CurveType.RIGHT;
            baseMeta = meta - 8;
        } else if (meta >= 6 && meta <= 9) {
            curve = CurveType.LEFT;
            baseMeta = meta - 4;
        } else {
            curve = CurveType.STRAIGHT;
            baseMeta = meta;
        }
        EnumFacing facing = switch (baseMeta) {
            case 3 -> EnumFacing.SOUTH;
            case 4 -> EnumFacing.WEST;
            case 5 -> EnumFacing.EAST;
            default -> EnumFacing.NORTH;
        };
        return this.getDefaultState().withProperty(FACING, facing).withProperty(CURVE, curve);
    }

    @Override
    public EnumFacing getInputDirection(World world, BlockPos pos) {
        return world.getBlockState(pos).getValue(FACING);
    }

    @Override
    public EnumFacing getOutputDirection(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        CurveType curve = state.getValue(CURVE);
        EnumFacing primaryOutput = state.getValue(FACING).getOpposite();
        if (curve == CurveType.RIGHT) return primaryOutput.rotateY();
        if (curve == CurveType.LEFT) return primaryOutput.rotateYCCW();
        return primaryOutput;
    }

    @Override
    public EnumFacing getTravelDirection(World world, BlockPos pos, Vec3d itemPos) {
        IBlockState state = world.getBlockState(pos);
        CurveType curve = state.getValue(CURVE);

        if (curve == CurveType.STRAIGHT) {
            return super.getTravelDirection(world, pos, itemPos);
        }

        EnumFacing primary = state.getValue(FACING);
        int dir = (curve == CurveType.LEFT) ? 0 : 1;

        double ix = pos.getX() + 0.5;
        double iz = pos.getZ() + 0.5;
        EnumFacing secondary = primary.rotateY();

        ix -= -primary.getXOffset() * 0.5 + secondary.getXOffset() * (0.5 - dir);
        iz -= -primary.getZOffset() * 0.5 + secondary.getZOffset() * (0.5 - dir);

        double dX = Math.abs(itemPos.x - ix);
        double dZ = Math.abs(itemPos.z - iz);
        if (dX + dZ >= 1) {
            if (curve == CurveType.LEFT) {
                return secondary.getOpposite();
            } else {
                return secondary;
            }
        }
        return primary;
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand,
                           IToolable.ToolType tool) {
        if (tool != IToolable.ToolType.SCREWDRIVER) {
            return false;
        }

        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);

        if (!player.isSneaking()) {
            // Rotate the block clockwise
            world.setBlockState(pos, state.withRotation(Rotation.CLOCKWISE_90), 3);
        } else {
            // Cycle through curve types: STRAIGHT -> LEFT -> RIGHT -> STRAIGHT
            CurveType curve = state.getValue(CURVE);
            int nextOrdinal = (curve.ordinal() + 1) % CurveType.VALUES.length;
            CurveType newCurve = CurveType.VALUES[nextOrdinal];
            world.setBlockState(pos, state.withProperty(CURVE, newCurve), 3);
        }

        return true;
    }

    @Override
    @NotNull
    public IBlockState withMirror(@NotNull IBlockState state, Mirror mirrorIn) {
        IBlockState mirroredState = state.withProperty(FACING, mirrorIn.mirror(state.getValue(FACING)));
        CurveType curve = mirroredState.getValue(CURVE);
        if (curve == CurveType.LEFT) {
            mirroredState = mirroredState.withProperty(CURVE, CurveType.RIGHT);
        } else if (curve == CurveType.RIGHT) {
            mirroredState = mirroredState.withProperty(CURVE, CurveType.LEFT);
        }
        return mirroredState;
    }

    // Enum to represent the curve type of the conveyor
    public enum CurveType implements IStringSerializable {
        STRAIGHT("straight"),
        LEFT("left"),
        RIGHT("right");

        public static final CurveType[] VALUES = values();

        private final String name;

        CurveType(String name) {
            this.name = name;
        }

        @Override
        @NotNull
        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
