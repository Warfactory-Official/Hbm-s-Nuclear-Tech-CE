package com.hbm.entity.mob;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import java.util.List;

public abstract class EntityUFOBase extends EntityFlying implements IMob {

    private static final DataParameter<Integer> WAYPOINT_X = EntityDataManager.createKey(EntityUFOBase.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> WAYPOINT_Y = EntityDataManager.createKey(EntityUFOBase.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> WAYPOINT_Z = EntityDataManager.createKey(EntityUFOBase.class, DataSerializers.VARINT);

    protected int scanCooldown;
    protected int courseChangeCooldown;
    protected Entity target;

    public EntityUFOBase(World worldIn) {
        super(worldIn);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(WAYPOINT_X, 0);
        this.dataManager.register(WAYPOINT_Y, 0);
        this.dataManager.register(WAYPOINT_Z, 0);
    }

    @Override
    protected void updateEntityActionState() {
        if (!this.world.isRemote) {
            if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
                this.setDead();
                return;
            }
        }

        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;

        if (this.target != null && !this.target.isEntityAlive()) {
            this.target = null;
        }

        this.scanForTarget();

        if (this.courseChangeCooldown <= 0) {
            this.setCourse();
        }
    }

    protected void scanForTarget() {
        int range = this.getScanRange();

        if (this.scanCooldown <= 0) {
            List<EntityLivingBase> entities = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(range, range / 2.0D, range));
            this.target = null;

            for (EntityLivingBase entity : entities) {
                if (!entity.isEntityAlive() || !this.canAttackClass(entity.getClass())) {
                    continue;
                }

                if (entity instanceof EntityPlayer player) {

                    if (player.capabilities.isCreativeMode) {
                        continue;
                    }

                    if (player.isPotionActive(MobEffects.INVISIBILITY)) {
                        continue;
                    }

                    if (this.target == null) {
                        this.target = entity;
                    } else if (this.getDistanceSq(entity) < this.getDistanceSq(this.target)) {
                        this.target = entity;
                    }
                }
            }

            this.scanCooldown = this.getScanDelay();
        }
    }

    protected int getScanRange() {
        return 50;
    }

    protected int getScanDelay() {
        return 100;
    }

    protected boolean isCourseTraversable(double targetX, double targetY, double targetZ, double distance) {
        double stepX = (targetX - this.posX) / distance;
        double stepY = (targetY - this.posY) / distance;
        double stepZ = (targetZ - this.posZ) / distance;
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();

        for (int i = 1; i < distance; ++i) {
            axisalignedbb = axisalignedbb.offset(stepX, stepY, stepZ);

            if (!this.world.getCollisionBoxes(this, axisalignedbb).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    protected void approachPosition(double speed) {
        double deltaX = this.getX() - this.posX;
        double deltaY = this.getY() - this.posY;
        double deltaZ = this.getZ() - this.posZ;
        Vec3d delta = new Vec3d(deltaX, deltaY, deltaZ);
        double len = delta.length();

        if (len > 5.0D) {
            if (this.isCourseTraversable(this.getX(), this.getY(), this.getZ(), len)) {
                this.motionX = delta.x / len * speed;
                this.motionY = delta.y / len * speed;
                this.motionZ = delta.z / len * speed;
            } else {
                this.courseChangeCooldown = 0;
            }
        }
    }

    protected void setCourse() {
        if (this.target != null) {
            this.setCourseForTaget();
            this.courseChangeCooldown = 20 + this.rand.nextInt(20);
        } else {
            this.setCourseWithoutTaget();
            this.courseChangeCooldown = 60 + this.rand.nextInt(20);
        }
    }

    protected void setCourseForTaget() {
        Vec3d vec = new Vec3d(this.posX - this.target.posX, 0.0D, this.posZ - this.target.posZ);
        if (vec.lengthSquared() < 1.0E-6D) {
            vec = new Vec3d(1.0D, 0.0D, 0.0D);
        }

        vec = vec.rotateYaw((float) (Math.PI * 2 * this.rand.nextFloat()));
        double length = vec.length();
        if (length < 1.0E-6D) {
            length = 1.0D;
        }

        double overshoot = 10.0D + this.rand.nextDouble() * 10.0D;

        int wX = MathHelper.floor(this.target.posX - vec.x / length * overshoot);
        int wZ = MathHelper.floor(this.target.posZ - vec.z / length * overshoot);
        int targetY = Math.max(this.world.getTopSolidOrLiquidBlock(new BlockPos(wX, 0, wZ)).getY(), MathHelper.floor(this.target.posY)) + this.targetHeightOffset();

        this.setWaypoint(wX, targetY, wZ);
    }

    protected int targetHeightOffset() {
        return 2 + this.rand.nextInt(2);
    }

    protected int wanderHeightOffset() {
        return 2 + this.rand.nextInt(3);
    }

    protected void setCourseWithoutTaget() {
        int x = MathHelper.floor(this.posX + this.rand.nextGaussian() * 5.0D);
        int z = MathHelper.floor(this.posZ + this.rand.nextGaussian() * 5.0D);
        int y = this.world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY() + this.wanderHeightOffset();
        this.setWaypoint(x, y, z);
    }

    public void setWaypoint(int x, int y, int z) {
        this.dataManager.set(WAYPOINT_X, x);
        this.dataManager.set(WAYPOINT_Y, y);
        this.dataManager.set(WAYPOINT_Z, z);
    }

    public int getX() {
        return this.dataManager.get(WAYPOINT_X);
    }

    public int getY() {
        return this.dataManager.get(WAYPOINT_Y);
    }

    public int getZ() {
        return this.dataManager.get(WAYPOINT_Z);
    }
}
