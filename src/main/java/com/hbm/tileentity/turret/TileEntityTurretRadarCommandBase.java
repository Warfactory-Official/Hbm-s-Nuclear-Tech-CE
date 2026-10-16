package com.hbm.tileentity.turret;

import com.hbm.config.ServerConfig;
import com.hbm.entity.projectile.EntityArtilleryRocket;
import com.hbm.entity.projectile.EntityArtilleryShell;
import com.hbm.tileentity.IRadarCommandReceiver;
import com.hbm.util.CompatExternal;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/** A turret that a linked radar can hand targets to, see TileEntityMachineRadarNT */
public abstract class TileEntityTurretRadarCommandBase extends TileEntityTurretBaseNT implements IRadarCommandReceiver {

    //true while the current target was designated by a radar
    public boolean radarDesignatedTarget = false;
    //true while sendCommandEntity validates a target, it checks the range itself
    private boolean ignoreRadarRange = false;

    /** How many times the turret's own detector range a radar designated target may be engaged at */
    protected double getRadarRangeMultiplier() {
        return Math.max(0D, ServerConfig.CIWS_RADAR_RANGE_MULT.get());
    }

    @Override
    public boolean sendCommandEntity(Entity target) {
        if (!ServerConfig.CIWS_RADAR_LINK_ENABLED.get()) return false;
        if (this.target != null || !isOn) return false;
        if (target == null || !target.isEntityAlive()) return false;

        //the flag is only kept if the target ends up locked
        this.radarDesignatedTarget = true;
        this.ignoreRadarRange = true;
        boolean valid;
        try {
            valid = entityAcceptableTarget(target) && entityInLOS(target);
        } finally {
            this.ignoreRadarRange = false;
        }
        if (!valid) {
            this.radarDesignatedTarget = false;
            return false;
        }

        Vec3d pos = this.getTurretPos();
        Vec3d ent = this.getEntityPos(target);
        if (pos.distanceTo(ent) > this.getDecetorRange() * getRadarRangeMultiplier()) {
            //too far to engage, only aim at it
            this.radarDesignatedTarget = false;
            turnTowards(ent);
            return true;
        }

        this.target = target;
        return true;
    }

    //artillery shells and rockets count as machines, but only for these turrets
    @Override
    public boolean entityAcceptableTarget(Entity e) {
        if (super.entityAcceptableTarget(e)) return true;
        if (!this.targetMachines || e.isDead || !ServerConfig.ARTILLERY_PROJECTILES_SHOOTABLE.get()) return false;
        if (!(e instanceof EntityArtilleryShell) && !(e instanceof EntityArtilleryRocket)) return false;
        for (Class<?> c : CompatExternal.turretTargetBlacklist) if (c.isAssignableFrom(e.getClass())) return false;
        return true;
    }

    @Override
    public boolean sendCommandPosition(int x, int y, int z) {
        if (!isOn) return false;
        turnTowards(new Vec3d(x + 0.5D, y, z + 0.5D));
        return true;
    }

    @Override
    public void update() {
        super.update();
        if (!world.isRemote && this.target == null) this.radarDesignatedTarget = false;
    }

    @Override
    protected void seekNewTarget() {
        this.radarDesignatedTarget = false;
        super.seekNewTarget();
    }

    @Override
    protected boolean isBeyondLosRange(double distance) {
        if (!this.radarDesignatedTarget) return super.isBeyondLosRange(distance);
        if (this.ignoreRadarRange) return false;
        return distance > this.getDecetorRange() * getRadarRangeMultiplier() * 1.1D;
    }

    @Override
    public boolean entityInLOS(Entity e) {
        if (this.radarDesignatedTarget && !isLosPathLoaded(e)) return false;
        return super.entityInLOS(e);
    }

    //the obstruction check loads chunks along a ray of up to 200 blocks, so only run it if the ray is loaded
    private boolean isLosPathLoaded(Entity e) {
        Vec3d pos = this.getTurretPos();
        Vec3d ent = this.getEntityPos(e);
        Vec3d delta = pos.subtract(ent);
        double len = delta.length();
        if (len < 1D) return true;
        Vec3d end = len <= 200D ? pos : ent.add(delta.scale(200D / len));
        Vec3d mid = ent.add(end).scale(0.5D);
        return world.isBlockLoaded(new BlockPos(ent.x, ent.y, ent.z))
                && world.isBlockLoaded(new BlockPos(mid.x, mid.y, mid.z))
                && world.isBlockLoaded(new BlockPos(end.x, end.y, end.z));
    }
}
