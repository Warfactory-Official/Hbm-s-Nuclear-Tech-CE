package com.hbm.tileentity.turret;

import com.hbm.blocks.BlockDummyable;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IRadarCommandReceiver;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Optional;

import java.util.ArrayList;
import java.util.List;

public abstract class TileEntityTurretBaseArtillery extends TileEntityTurretBaseNT implements IRadarCommandReceiver {

    protected List<Vec3d> targetQueue = new ArrayList<>();

    @Override
    public boolean sendCommandPosition(int x, int y, int z) {
        this.enqueueTarget(x + 0.5, y, z + 0.5);
        return true;
    }

    @Override
    public boolean sendCommandEntity(Entity target) {
        this.enqueueTarget(target.posX, target.posY, target.posZ);
        return true;
    }

    @Override
    public Vec3d getEntityPos(Entity e) {
        return new Vec3d(e.posX, e.posY + e.height * 0.5 - e.getYOffset(), e.posZ);
    }

    public void enqueueTarget(double x, double y, double z) {

        Vec3d pos = this.getTurretPos();
        Vec3d delta = new Vec3d(x - pos.x, y - pos.y, z - pos.z);
        if (delta.length() <= this.getDecetorRange()) {
            this.targetQueue.add(new Vec3d(x, y, z));
        }
    }

    public abstract boolean doLOSCheck();

    @Override
    public boolean entityInLOS(Entity e) {

        if (doLOSCheck()) {
            return super.entityInLOS(e);

        } else {
            Vec3d pos = this.getTurretPos();
            Vec3d ent = this.getEntityPos(e);
            Vec3d delta = new Vec3d(ent.x - pos.x, ent.y - pos.y, ent.z - pos.z);
            double length = delta.length();

            if (length < this.getDecetorGrace() || length > this.getDecetorRange() * 1.1) //the latter statement is only relevant for entities that have already been detected
                return false;

            int height = world.getHeight((int) Math.floor(e.posX), (int) Math.floor(e.posZ));
            return height < (e.posY + e.height);
        }
    }

    @Override
    protected void updateConnections() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 4; j++) {
                this.trySubscribe(world, pos.getX() + dir.offsetX * (-1 + j) + rot.offsetX * -3, pos.getY() + i, pos.getZ() + dir.offsetZ * (-1 + j) + rot.offsetZ * -3, ForgeDirection.SOUTH);
                this.trySubscribe(world, pos.getX() + dir.offsetX * (-1 + j) + rot.offsetX * 2, pos.getY() + i, pos.getZ() + dir.offsetZ * (-1 + j) + rot.offsetZ * 2, ForgeDirection.NORTH);
                this.trySubscribe(world, pos.getX() + dir.offsetX * -2 + rot.offsetX * (1 - j), pos.getY() + i, pos.getZ() + dir.offsetZ * -2 + rot.offsetZ * (1 - j), ForgeDirection.EAST);
                this.trySubscribe(world, pos.getX() + dir.offsetX * 3 + rot.offsetX * (1 - j), pos.getY() + i, pos.getZ() + dir.offsetZ * 3 + rot.offsetZ * (1 - j), ForgeDirection.WEST);
            }
        }
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "ntm_artillery";
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getCurrentTarget(Context context, Arguments args) {
        return new Object[]{targetQueue.get(0).x, targetQueue.get(0).y, targetQueue.get(0).z};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getTargetDistance(Context context, Arguments args) {
        return new Object[]{Math.sqrt(Math.pow(pos.getX() - args.checkDouble(0), 2) + Math.pow(pos.getY() - args.checkDouble(1), 2) + Math.pow(pos.getZ() - args.checkDouble(2), 2))};
    }
}
