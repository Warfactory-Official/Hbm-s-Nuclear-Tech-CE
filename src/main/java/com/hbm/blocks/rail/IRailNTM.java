package com.hbm.blocks.rail;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface IRailNTM {

    Vec3d getSnappingPos(World world, int x, int y, int z, double trainX, double trainY, double trainZ);

    Vec3d getTravelLocation(World world, int x, int y, int z, double trainX, double trainY, double trainZ, double motionX, double motionY, double motionZ, double speed, RailContext info, MoveContext context);

    TrackGauge getGauge(World world, int x, int y, int z);

    enum TrackGauge {
        STANDARD,
        NARROW
    }

    class RailContext {
        public float yaw;
        public double overshoot;
        public BlockPos pos;

        public RailContext yaw(float y) { this.yaw = y; return this; }
        public RailContext dist(double d) { this.overshoot = d; return this; }
        public RailContext pos(BlockPos d) { this.pos = d; return this; }
    }

    class MoveContext {
        public RailCheckType type;
        public double collisionBogieDistance;
        public boolean collision = false;
        public double overshoot;

        public MoveContext(RailCheckType type, double collisionBogieDistance) {
            this.type = type;
            this.collisionBogieDistance = collisionBogieDistance;
        }
    }

    enum RailCheckType {
        CORE,
        FRONT,
        BACK,
        OTHER
    }
}
