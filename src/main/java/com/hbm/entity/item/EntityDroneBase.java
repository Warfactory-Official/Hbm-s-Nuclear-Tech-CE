package com.hbm.entity.item;

import com.hbm.render.amlfrom1710.Vec3;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityDroneBase extends Entity {
    public static final DataParameter<Byte> APPEARANCE = EntityDataManager.createKey(EntityDroneBase.class, DataSerializers.BYTE);
    public static final DataParameter<Boolean> IS_EXPRESS = EntityDataManager.createKey(EntityDroneBase.class, DataSerializers.BOOLEAN);
    protected static final int theNumberThree = 3; //Actually controls how precise the interpolation should be, but 'theNumberThree' is such a funny name so I won't rename it.
    protected int interpolationTicks;
    @SideOnly(Side.CLIENT) protected double syncPosX, syncPosY, syncPosZ, lastSyncPosX, lastSyncPosY, lastSyncPosZ, velocityX, velocityY, velocityZ;

    public double targetX = -1;
    public double targetY = -1;
    public double targetZ = -1;

    public EntityDroneBase(World world) {
        super(world);
        this.setSize(0.75F, 0.75F);
    }

    public void setTarget(double x, double y, double z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean hitByEntity(Entity attacker) {

        if(attacker instanceof EntityPlayer) {
            this.setDead();
        }

        return false;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(APPEARANCE, (byte) 0);
    }

    /**
     * 0: Empty<br>
     * 1: Crate<br>
     * 2: Barrel<br>
     */
    public void setAppearance(int style) {
        this.dataManager.set(APPEARANCE, (byte) style);
    }

    public int getAppearance() {
        return this.dataManager.get(APPEARANCE);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if(world.isRemote) {
            this.interpolateMovement();

            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX + 1.125, posY + 0.75, posZ, 0, -0.2, 0);
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX - 1.125, posY + 0.75, posZ, 0, -0.2, 0);
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY + 0.75, posZ + 1.125, 0, -0.2, 0);
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY + 0.75, posZ - 1.125, 0, -0.2, 0);
        } else {
            this.motionX = 0;
            this.motionY = 0;
            this.motionZ = 0;

            if(this.targetY != -1) {

                Vec3 dist = Vec3.createVectorHelper(targetX - posX, targetY - posY, targetZ - posZ);
                double speed = Math.min(getSpeed(), dist.length());

                dist = dist.normalize();
                this.motionX = dist.xCoord * speed;
                this.motionY = dist.yCoord * speed;
                this.motionZ = dist.zCoord * speed;
            }
            if(collidedHorizontally){
                motionY += 1;
            }
            this.loadNeighboringChunks();
            this.move(MoverType.SELF, motionX, motionY, motionZ);
        }
    }

    private void interpolateMovement()
    {
        if (this.interpolationTicks > 0)
        {
            double interpX = this.lastSyncPosX + (this.syncPosX - this.lastSyncPosX) / (double) this.interpolationTicks;
            double interpY = this.lastSyncPosY + (this.syncPosY - this.lastSyncPosY) / (double) this.interpolationTicks;
            double interpZ = this.lastSyncPosZ + (this.syncPosZ - this.lastSyncPosZ) / (double) this.interpolationTicks;
            --this.interpolationTicks;
            this.setPosition(interpX, interpY, interpZ);
        }
    }

    protected void loadNeighboringChunks() {}

    public double getSpeed() {
        return 0.125D;
    }

    @SideOnly(Side.CLIENT)
    public void setVelocity(double motionX, double motionY, double motionZ) {
        this.velocityX = this.motionX = motionX;
        this.velocityY = this.motionY = motionY;
        this.velocityZ = this.motionZ = motionZ;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        this.lastSyncPosX = this.posX;
        this.lastSyncPosY = this.posY;
        this.lastSyncPosZ = this.posZ;
        this.syncPosX = x;
        this.syncPosY = y;
        this.syncPosZ = z;
        this.interpolationTicks = theNumberThree;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setDouble("tX", targetX);
        compound.setDouble("tY", targetY);
        compound.setDouble("tZ", targetZ);

        compound.setByte("app", this.dataManager.get(APPEARANCE));
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        if(compound.hasKey("tY")) {
            this.targetX = compound.getDouble("tX");
            this.targetY = compound.getDouble("tY");
            this.targetZ = compound.getDouble("tZ");
        }

        this.dataManager.set(APPEARANCE, compound.getByte("app"));
    }
}
