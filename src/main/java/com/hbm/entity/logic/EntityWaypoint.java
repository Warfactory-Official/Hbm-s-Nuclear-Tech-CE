package com.hbm.entity.logic;

import com.hbm.config.MobConfig;
import com.hbm.entity.mob.glyphid.EntityGlyphid;
import com.hbm.entity.mob.glyphid.EntityGlyphidNuclear;
import com.hbm.entity.mob.glyphid.EntityGlyphidScout;
import com.hbm.main.MainRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

import static com.hbm.entity.mob.glyphid.EntityGlyphid.*;

public class EntityWaypoint extends Entity {
    public static final DataParameter<Byte> WAYPOINT_TYPE = EntityDataManager.createKey(EntityWaypoint.class, DataSerializers.BYTE);

    public EntityWaypoint(World world) {
        super(world);
        this.isImmuneToFire = true;
        this.noClip = true;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(WAYPOINT_TYPE, (byte) 0);
    }

    public int maxAge = 2400;
    public int radius = 3;
    public boolean highPriority = false;
    protected EntityWaypoint additional;

    public void setHighPriority() {
        highPriority = true;
    }

    public byte getWaypointType() {
        return this.dataManager.get(WAYPOINT_TYPE);
    }

    public void setAdditionalWaypoint(EntityWaypoint waypoint) {
        additional = waypoint;
    }

    public void setWaypointType(byte waypointType) {
        this.dataManager.set(WAYPOINT_TYPE, waypointType);
    }

    boolean hasSpawned = false;

    public int getColor() {
        return switch (getWaypointType()) {
            case TASK_RETREAT_FOR_REINFORCEMENTS -> 0x5FA6E8;
            case TASK_BUILD_HIVE, TASK_INITIATE_RETREAT -> 0x127766;
            default -> 0x566573;
        };
    }

    AxisAlignedBB bb;

    @Override
    public void onEntityUpdate() {
        if(ticksExisted >= maxAge) {
            this.setDead();
        }

        bb = new AxisAlignedBB(this.posX, this.posY, this.posZ, this.posX, this.posY, this.posZ).expand(radius, radius, radius);

        if(!world.isRemote) {

            if(ticksExisted % 40 == 0) {

                List<Entity> targets = world.getEntitiesWithinAABBExcludingEntity(this, bb);

                for(Entity e : targets) {
                    if(e instanceof EntityGlyphid bug) {

                        if(additional != null && !hasSpawned) {
                            world.spawnEntity(additional);
                            hasSpawned = true;
                        }

                        boolean exceptions = bug.getWaypoint() != this || e instanceof EntityGlyphidScout || e instanceof EntityGlyphidNuclear;

                        if(!exceptions)
                            bug.setCurrentTask(getWaypointType(), additional);

                        if(getWaypointType() == TASK_BUILD_HIVE) {
                            if(e instanceof EntityGlyphidScout)
                                setDead();
                        } else {
                            setDead();
                        }

                    }
                }
            }
        } else if(MobConfig.waypointDebug) {

            double x = bb.minX + (rand.nextDouble() - 0.5) * (bb.maxX - bb.minX);
            double y = bb.minY + rand.nextDouble() * (bb.maxY - bb.minY);
            double z = bb.minZ + (rand.nextDouble() - 0.5) * (bb.maxZ - bb.minZ);

            NBTTagCompound fx = new NBTTagCompound();
            fx.setString("type", "tower");
            fx.setFloat("lift", 0.5F);
            fx.setFloat("base", 0.75F);
            fx.setFloat("max", 2F);
            fx.setInteger("life", 50 + world.rand.nextInt(10));
            fx.setInteger("color", getColor());
            fx.setDouble("posX", x);
            fx.setDouble("posY", y);
            fx.setDouble("posZ", z);
            MainRegistry.proxy.effectNT(fx);
        }

    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        this.setWaypointType(nbt.getByte("type"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setByte("type", getWaypointType());
    }
}
