package com.hbm.handler.neutron;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class NeutronStream {

    public enum NeutronType {
        DUMMY,
        RBMK,
        PILE
    }

    public NeutronNode origin;
    public double fluxQuantity;
    public double fluxRatio;
    public NeutronType type = NeutronType.DUMMY;
    public Vec3d vector;

    public NeutronStream(NeutronNode origin, Vec3d vector) {
        this.origin = origin;
        this.vector = vector;
    }

    public NeutronStream(NeutronNode origin, Vec3d vector, double flux, double ratio, NeutronType type) {
        this.origin = origin;
        this.vector = vector;
        this.fluxQuantity = flux;
        this.fluxRatio = ratio;
        this.type = type;

        NeutronNodeWorld.getOrAddWorld(origin.tile.getWorld()).addStream(this);
    }

    public abstract void runStreamInteraction(World worldObj, NeutronNodeWorld.StreamWorld streamWorld);
}
