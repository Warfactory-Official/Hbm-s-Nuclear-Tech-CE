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

public class RailStandardCurveBase extends BlockDummyable implements IRailNTM {

    protected static final AxisAlignedBB RAIL_BOX = new AxisAlignedBB(0F, 0F, 0F, 1F, 0.125F, 1F);

    protected int width = 4;

    public RailStandardCurveBase(String s) {
        super(Material.IRON, s);
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {
        return null;
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

        double turnRadius = width;
        double axisDist = width + 0.5D;

        double axisX = cX + 0.5 + dir.offsetX * 0.5 + rot.offsetX * axisDist;
        double axisZ = cZ + 0.5 + dir.offsetZ * 0.5 + rot.offsetZ * axisDist;

        Vec3d dist = new Vec3d(trainX - axisX, 0, trainZ - axisZ).normalize().scale(turnRadius);

        double moveAngle = Math.atan2(motionX, motionZ) * 180D / Math.PI + 90;

        if(speed == 0) {
            info.dist(0).pos(new BlockPos(x, y, z)).yaw((float) moveAngle);
            return new Vec3d(axisX + dist.x, y, axisZ + dist.z);
        }

        double angleDeg = Math.atan2(dist.x, dist.z) * 180D / Math.PI + 90;
        if(dir == Library.NEG_X) angleDeg -= 90;
        if(dir == Library.POS_X) angleDeg += 90;
        if(dir == Library.POS_Z) angleDeg += 180;
        angleDeg = MathHelper.wrapDegrees(angleDeg);
        double length90Deg = turnRadius * Math.PI / 2D;
        double angularChange = speed / length90Deg * 90D;

        ForgeDirection moveDir;

        if(Math.abs(motionX) > Math.abs(motionZ)) {
            moveDir = motionX > 0 ? Library.POS_X : Library.NEG_X;
        } else {
            moveDir = motionZ > 0 ? Library.POS_Z : Library.NEG_Z;
        }

        if(moveDir == dir || moveDir == rot.getOpposite()) {
            angularChange *= -1;
        }

        double effAngle = angleDeg + angularChange;
        moveAngle += angularChange;

        if(effAngle > 90) {
            double angleOvershoot = effAngle - 90D;
            moveAngle -= angleOvershoot;
            double lengthOvershoot = angleOvershoot * length90Deg / 90D;
            info.dist(lengthOvershoot * Math.signum(speed * angularChange)).pos(new BlockPos(cX - dir.offsetX * width + rot.offsetX * (width + 1), y, cZ - dir.offsetZ * width + rot.offsetZ * (width + 1))).yaw((float) moveAngle);
            return new Vec3d(axisX - dir.offsetX * turnRadius, y + 0.1875, axisZ - dir.offsetZ * turnRadius);
        }

        if(effAngle < 0) {
            double angleOvershoot = -effAngle;
            moveAngle -= angleOvershoot;
            double lengthOvershoot = angleOvershoot * length90Deg / 90D;
            info.dist(-lengthOvershoot * Math.signum(speed * angularChange)).pos(new BlockPos(cX + dir.offsetX, y, cZ + dir.offsetZ)).yaw((float) moveAngle);
            return new Vec3d(axisX - rot.offsetX * turnRadius, y + 0.1875, axisZ - rot.offsetZ * turnRadius);
        }

        double radianChange = angularChange * Math.PI / 180D;
        dist = dist.rotateYaw((float) radianChange);

        return new Vec3d(axisX + dist.x, y + 0.1875, axisZ + dist.z);
    }

    @Override
    public TrackGauge getGauge(World world, int x, int y, int z) {
        return TrackGauge.STANDARD;
    }

    @Override
    public int[] getDimensions() {
        return new int[] {0, 0, width, 0, width, 0};
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public @NotNull AxisAlignedBB getBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos) {
        return RAIL_BOX;
    }

    protected static boolean replaceable(World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        return world.getBlockState(pos).getBlock().isReplaceable(world, pos);
    }

    protected void place(World world, int x, int y, int z, int meta) {
        world.setBlockState(new BlockPos(x, y, z), this.getStateFromMeta(meta), 3);
    }

    @Override
    public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {

        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        dir = dir.getOpposite();

        int dX = dir.offsetX;
        int dZ = dir.offsetZ;
        int rX = rot.offsetX;
        int rZ = rot.offsetZ;

        return replaceable(world, x + dX, y, z + dZ) &&
                replaceable(world, x + rX, y, z + rZ) &&
                replaceable(world, x + dX + rX, y, z + dZ + rZ) &&
                replaceable(world, x + dX + rX * 2, y, z + dZ + rZ * 2) &&
                replaceable(world, x + dX * 2 + rX, y, z + dZ * 2 + rZ) &&
                replaceable(world, x + dX * 2 + rX * 2, y, z + dZ * 2 + rZ * 2) &&
                replaceable(world, x + dX * 3 + rX, y, z + dZ * 3 + rZ) &&
                replaceable(world, x + dX * 3 + rX * 2, y, z + dZ * 3 + rZ * 2) &&
                replaceable(world, x + dX * 2 + rX * 3, y, z + dZ * 2 + rZ * 3) &&
                replaceable(world, x + dX * 3 + rX * 3, y, z + dZ * 3 + rZ * 3) &&
                replaceable(world, x + dX * 4 + rX * 3, y, z + dZ * 4 + rZ * 3) &&
                replaceable(world, x + dX * 3 + rX * 4, y, z + dZ * 3 + rZ * 4) &&
                replaceable(world, x + dX * 4 + rX * 4, y, z + dZ * 4 + rZ * 4);
    }

    @Override
    protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {

        BlockDummyable.safeRem = true;

        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        dir = dir.getOpposite();

        int dX = dir.offsetX;
        int dZ = dir.offsetZ;
        int rX = rot.offsetX;
        int rZ = rot.offsetZ;

        int d = dir.ordinal();
        int r = rot.ordinal();

        place(world, x + dX, y, z + dZ, d);
        place(world, x + rX, y, z + rZ, r);
        place(world, x + dX + rX, y, z + dZ + rZ, r);
        place(world, x + dX + rX * 2, y, z + dZ + rZ * 2, r);
        place(world, x + dX * 2 + rX, y, z + dZ * 2 + rZ, d);
        place(world, x + dX * 2 + rX * 2, y, z + dZ * 2 + rZ * 2, d);
        place(world, x + dX * 3 + rX, y, z + dZ * 3 + rZ, d);
        place(world, x + dX * 3 + rX * 2, y, z + dZ * 3 + rZ * 2, d);
        place(world, x + dX * 2 + rX * 3, y, z + dZ * 2 + rZ * 3, r);
        place(world, x + dX * 3 + rX * 3, y, z + dZ * 3 + rZ * 3, r);
        place(world, x + dX * 4 + rX * 3, y, z + dZ * 4 + rZ * 3, d);
        place(world, x + dX * 3 + rX * 4, y, z + dZ * 3 + rZ * 4, r);
        place(world, x + dX * 4 + rX * 4, y, z + dZ * 4 + rZ * 4, r);

        BlockDummyable.safeRem = false;
    }
}
