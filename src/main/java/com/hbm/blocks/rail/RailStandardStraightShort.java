package com.hbm.blocks.rail;

import com.hbm.blocks.BlockDummyable;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class RailStandardStraightShort extends BlockDummyable implements IRailNTM {

    protected static final AxisAlignedBB RAIL_BOX = new AxisAlignedBB(0F, 0F, 0F, 1F, 0.125F, 1F);

    public RailStandardStraightShort(String s) {
        super(Material.IRON, s);
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {
        return null;
    }

    @Override
    public int[] getDimensions() {
        return new int[] {0, 0, 0, 0, 1, 0};
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public @NotNull AxisAlignedBB getBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos) {
        return RAIL_BOX;
    }

    @Override
    public Vec3d getSnappingPos(World world, int x, int y, int z, double trainX, double trainY, double trainZ) {
        return snapAndMove(world, x, y, z, trainX, trainY, trainZ, 0, 0, 0, 0, new RailContext());
    }

    @Override
    public Vec3d getTravelLocation(World world, int x, int y, int z, double trainX, double trainY, double trainZ, double motionX, double motionY, double motionZ, double speed, RailContext info, MoveContext context) {
        return snapAndMove(world, x, y, z, trainX, trainY, trainZ, motionX, motionY, motionZ, speed, info);
    }

    public Vec3d snapAndMove(World world, int x, int y, int z, double trainX, double trainY, double trainZ, double motionX, double motionY, double motionZ, double speed, RailContext info) {
        int[] pos = this.findCore(world, x, y, z);
        if(pos == null) return new Vec3d(trainX, trainY, trainZ);
        int cX = pos[0];
        int cZ = pos[2];
        int meta = world.getBlockState(new BlockPos(pos[0], pos[1], pos[2])).getValue(META) - offset;
        ForgeDirection dir = ForgeDirection.getOrientation(meta);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        if(dir == Library.POS_X || dir == Library.NEG_X) {
            double targetX = trainX;
            if(motionX > 0) {
                targetX += speed;
                info.yaw(-90F);
            } else {
                targetX -= speed;
                info.yaw(90F);
            }
            double vX = MathHelper.clamp(targetX, cX, cX + 1);
            info.dist(Math.abs(targetX - vX) * Math.signum(speed));
            info.pos(new BlockPos(cX + (motionX * speed > 0 ? 1 : -1), y, cZ));
            return new Vec3d(vX, y + 0.1875, cZ + 0.5 + rot.offsetZ * 0.5);
        } else {
            double targetZ = trainZ;
            if(motionZ > 0) {
                targetZ += speed;
                info.yaw(0F);
            } else {
                targetZ -= speed;
                info.yaw(180F);
            }
            double vZ = MathHelper.clamp(targetZ, cZ, cZ + 1);
            info.dist(Math.abs(targetZ - vZ) * Math.signum(speed));
            info.pos(new BlockPos(cX, y, cZ + (motionZ * speed > 0 ? 1 : -1)));
            return new Vec3d(cX + 0.5 + rot.offsetX * 0.5, y + 0.1875, vZ);
        }
    }

    @Override
    public TrackGauge getGauge(World world, int x, int y, int z) {
        return TrackGauge.STANDARD;
    }
}
