package com.hbm.blocks.rail;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class RailStandardRamp extends BlockDummyable implements IRailNTM {

    public RailStandardRamp(String s) {
        super(Material.IRON, s);
        this.bounding.add(new AxisAlignedBB(-2.5, 0.0, -1.5, -1.5, 0.1, 0.5));
        this.bounding.add(new AxisAlignedBB(-1.5, 0.0, -1.5, -0.5, 0.3, 0.5));
        this.bounding.add(new AxisAlignedBB(-0.5, 0.0, -1.5, 0.5, 0.5, 0.5));
        this.bounding.add(new AxisAlignedBB(0.5, 0.0, -1.5, 1.5, 0.7, 0.5));
        this.bounding.add(new AxisAlignedBB(1.5, 0.0, -1.5, 2.5, 0.9, 0.5));
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {
        return null;
    }

    @Override
    public int[] getDimensions() {
        return new int[] {0, 0, 2, 2, 1, 0};
    }

    @Override
    public int getOffset() {
        return 2;
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
        int cY = pos[1];
        int cZ = pos[2];
        int meta = world.getBlockState(new BlockPos(cX, cY, cZ)).getValue(META) - offset;
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
            double dist = (cX + 0.5 - targetX + 2.5) / 5;
            double vX = MathHelper.clamp(targetX, cX - 2, cX + 3);
            double vY = MathHelper.clamp(dir == Library.POS_X ? cY + dist : cY + 1 - dist, cY, cY + 1) + 0.1875;
            info.dist(Math.abs(targetX - vX) * Math.signum(speed));
            info.pos(new BlockPos(cX + (motionX * speed > 0 ? 3 : -3), cY + (motionX * speed > 0 ^ dir == Library.POS_X ? 1 : 0), cZ));
            return new Vec3d(vX, vY, cZ + 0.5 + rot.offsetZ * 0.5);
        } else {
            double targetZ = trainZ;
            if(motionZ > 0) {
                targetZ += speed;
                info.yaw(0F);
            } else {
                targetZ -= speed;
                info.yaw(180F);
            }
            double dist = (cZ + 0.5 - targetZ + 2.5) / 5;
            double vY = MathHelper.clamp(dir == Library.POS_Z ? cY + dist : cY + 1 - dist, cY, cY + 1) + 0.1875;
            double vZ = MathHelper.clamp(targetZ, cZ - 2, cZ + 3);
            info.dist(Math.abs(targetZ - vZ) * Math.signum(speed));
            info.pos(new BlockPos(cX, cY + (motionZ * speed > 0 ^ dir == Library.POS_Z ? 1 : 0), cZ + (motionZ * speed > 0 ? 3 : -3)));
            return new Vec3d(cX + 0.5 + rot.offsetX * 0.5, vY, vZ);
        }
    }

    @Override
    public TrackGauge getGauge(World world, int x, int y, int z) {
        return TrackGauge.STANDARD;
    }

    @Override
    public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
        return MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o, getDimensions(), x, y, z, dir) &&
                MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {1, -1, 2, 2, 1, 0}, x, y, z, dir);
    }

    @Override
    protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o, getDimensions(), this, dir);
        MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {1, -1, 2, 2, 1, 0}, this, dir);
    }
}
