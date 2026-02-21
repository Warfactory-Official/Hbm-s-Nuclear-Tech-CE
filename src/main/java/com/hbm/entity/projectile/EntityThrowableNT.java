package com.hbm.entity.projectile;

import com.hbm.api.entity.IThrowable;
import com.hbm.lib.Library;
import com.hbm.util.TrackerUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.Predicate;


/**
 * Near-identical copy of EntityThrowable but deobfuscated & untangled
 *
 * @author hbm
 */
//mlbv: this class is cursed, do not attempt to make it override EntityThrowable, better leave it as is
public abstract class EntityThrowableNT extends Entity implements IProjectile, IThrowable {
    private static final DataParameter<Byte> STUCK_IN = EntityDataManager.createKey(EntityThrowableNT.class, DataSerializers.BYTE);
    public int throwableShake;
    protected boolean inGround;
    protected EntityLivingBase thrower;
    private int stuckBlockX = -1;
    private int stuckBlockY = -1;
    private int stuckBlockZ = -1;
    private Block stuckBlock;
    private String throwerName;
    private int ticksInGround;
    private int ticksInAir;

    public EntityThrowableNT(World worldIn) {
        super(worldIn);
        setSize(0.25F, 0.25F);
    }

    public EntityThrowableNT(World worldIn, double x, double y, double z) {
        this(worldIn);
        ticksInGround = 0;
        setSize(0.25F, 0.25F);
        setPosition(x, y, z);
    }

    public EntityThrowableNT(World world, EntityLivingBase thrower) {
        super(world);
        this.thrower = thrower;
        setSize(0.25F, 0.25F);
        setLocationAndAngles(thrower.posX, thrower.posY + (double) thrower.getEyeHeight(), thrower.posZ, thrower.rotationYaw, thrower.rotationPitch);
        posX -= (double) (MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
        posY -= 0.1D;
        posZ -= (double) (MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
        setPosition(posX, posY, posZ);
        float velocity = 0.4F;
        motionX = (double) (-MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI) * velocity);
        motionZ = (double) (MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI) * velocity);
        motionY = (double) (-MathHelper.sin((rotationPitch + throwAngle()) / 180.0F * (float) Math.PI) * velocity);
        shoot(motionX, motionY, motionZ, throwForce(), 1.0F);
    }

    @Override
    protected void entityInit() {
        dataManager.register(STUCK_IN, (byte) 0);
    }

    public int getStuckIn() {
        return dataManager.get(STUCK_IN);
    }

    public void setStuckIn(int side) {
        dataManager.set(STUCK_IN, (byte) side);
    }

    protected float throwForce() {
        return 1.5F;
    }

    protected double headingForceMult() {
        return 0.0075D;
    }

    protected float throwAngle() {
        return 0.0F;
    }

    protected double motionMult() {
        return 1.0D;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isInRangeToRenderDist(double distance) {
        double d0 = getEntityBoundingBox().getAverageEdgeLength() * 4.0D;
        if (Double.isNaN(d0)) {
            d0 = 4.0D;
        }
        d0 = d0 * 64.0D;
        return distance < d0 * d0;
    }

    @Override
    public void shoot(double motionX, double motionY, double motionZ, float velocity, float inaccuracy) {
        float throwLen = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        //Euclidean Distance ^^

        motionX /= (double) throwLen;
        motionY /= (double) throwLen;
        motionZ /= (double) throwLen;

        motionX += rand.nextGaussian() * headingForceMult() * (double) inaccuracy;
        motionY += rand.nextGaussian() * headingForceMult() * (double) inaccuracy;
        motionZ += rand.nextGaussian() * headingForceMult() * (double) inaccuracy;

        motionX *= (double) velocity;
        motionY *= (double) velocity;
        motionZ *= (double) velocity;

        //Motion should be fine as a double

        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        float hyp = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
        prevRotationYaw = rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI);
        prevRotationPitch = rotationPitch = (float) (Math.atan2(motionY, (double) hyp) * 180.0D / Math.PI);
        ticksInGround = 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setVelocity(double x, double y, double z) {
        motionX = x;
        motionY = y;
        motionZ = z;

        if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F) {
            float hyp = MathHelper.sqrt(x * x + z * z);
            prevRotationYaw = rotationYaw = (float) (Math.atan2(x, z) * 180.0D / Math.PI);
            prevRotationPitch = rotationPitch = (float) (Math.atan2(y, (double) hyp) * 180.0D / Math.PI);
        }
    }

    @Override
    public void onUpdate() { //FIXME: This wont work for 1.12 due to 1.9 physics changes
        super.onUpdate();

        if (throwableShake > 0) {
            --throwableShake;
        }

        if (inGround) {
            if (world.getBlockState(new BlockPos(stuckBlockX, stuckBlockY, stuckBlockZ)).getBlock() == stuckBlock) {
                ++ticksInGround;

                if (groundDespawn() > 0 && ticksInGround == groundDespawn()) {
                    setDead();
                }

                return;
            }

            inGround = false;
//                this.motionX *= (double) (this.rand.nextFloat() * 0.2F);
//                this.motionY *= (double) (this.rand.nextFloat() * 0.2F);
//                this.motionZ *= (double) (this.rand.nextFloat() * 0.2F);
            //Randomizing motion why?? Im assuming for unpredicadbility but no

            ticksInGround = 0;
            ticksInAir = 0;
        }

        ++ticksInAir;
        double mm = motionMult();
        Vec3d pos = new Vec3d(posX, posY, posZ);
        Vec3d nextPos = new Vec3d(posX + motionX * mm, posY + motionY * mm, posZ + motionZ * mm);

        RayTraceResult mop = null;
        if (!isSpectral()) {
            mop = Library.rayTraceBlocks(world, pos, nextPos, false, true, false);
        }
        //Looks fine too theres no Float divs,

        if (mop != null) {
            nextPos = new Vec3d(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
        }

        if (!world.isRemote && doesImpactEntities()) {
            EntityLivingBase shooter = getThrower();

            Predicate<? super Entity> filter = null;
            if (shooter != null && ticksInAir < selfDamageDelay()) {
                filter = e -> e != shooter;
            }

            if (!doesPenetrate()) {
                RayTraceResult entHit = Library.rayTraceEntities(world, this, pos, nextPos, 0.3D, filter);
                if (entHit != null) {
                    mop = entHit;
                }
            } else {
                AxisAlignedBB swept = getEntityBoundingBox().expand(motionX * mm, motionY * mm, motionZ * mm).grow(1.0D);
                List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(this, swept);
                for (Entity entity : list) {
                    if (!entity.canBeCollidedWith()) continue;
                    if (filter != null && !filter.test(entity)) continue;
                    double hitbox = 0.3D;
                    AxisAlignedBB aabb = entity.getEntityBoundingBox().expand(hitbox, hitbox, hitbox);
                    RayTraceResult hitMop = aabb.calculateIntercept(pos, nextPos);
                    if (hitMop != null) {
                        onImpact(new RayTraceResult(entity, hitMop.hitVec));
                    }
                }
            }
        }

        if (mop != null) {
            if (mop.typeOfHit == RayTraceResult.Type.BLOCK && world.getBlockState(mop.getBlockPos()).getBlock() == Blocks.PORTAL) {
                setPortal(mop.getBlockPos());
            } else if (!ForgeEventFactory.onProjectileImpact(this, mop)) {
                onImpact(mop);
            }
        }

        // Code for motion and rotation during flight
        if (!onGround) {
            float hyp = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
            rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI);

            rotationPitch = (float) (Math.atan2(motionY, (double) hyp) * 180.0D / Math.PI);
            while (rotationPitch - prevRotationPitch < -180.0F) prevRotationPitch -= 360.0F;
            while (rotationPitch - prevRotationPitch >= 180.0F) prevRotationPitch += 360.0F;
            while (rotationYaw - prevRotationYaw < -180.0F) prevRotationYaw -= 360.0F;
            while (rotationYaw - prevRotationYaw >= 180.0F) prevRotationYaw += 360.0F;

            rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
            rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
        }

        float drag = getAirDrag();
        float gravity = getGravityVelocity();
        //Why are we fetching a const on every frame update??
        //Is gravity expected to change ?

        posX += motionX * mm;
        posY += motionY * mm;
        posZ += motionZ * mm;

        if (isInWater()) {
            for (int i = 0; i < 4; ++i) {
                float f = 0.25F;
                world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX - motionX * (double) f, posY - motionY * (double) f, posZ - motionZ * (double) f, motionX, motionY, motionZ);
            }

            drag = getWaterDrag();
        }

        motionX *= (double) drag;
        motionY *= (double) drag;
        motionZ *= (double) drag;
        motionY -= gravity;

        setPosition(posX, posY, posZ);
    }

    public boolean doesImpactEntities() {
        return true;
    }

    public boolean doesPenetrate() {
        return false;
    }

    public boolean isSpectral() {
        return false;
    }

    public int selfDamageDelay() {
        return 5;
    }

    public void getStuck(BlockPos pos, int side) {
        stuckBlockX = pos.getX();
        stuckBlockY = pos.getY();
        stuckBlockZ = pos.getZ();
        stuckBlock = world.getBlockState(pos).getBlock();
        inGround = true;
        motionX = 0;
        motionY = 0;
        motionZ = 0;
        setStuckIn(side);
        TrackerUtil.sendTeleport(world, this);
    }

    public float getGravityVelocity() {
        return 0.03F;
        //Why 0.03? this is overridden in every child class no?
    }

    protected abstract void onImpact(RayTraceResult result);

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("xTile", stuckBlockX);
        compound.setInteger("yTile", stuckBlockY);
        compound.setInteger("zTile", stuckBlockZ);
        compound.setByte("inTile", (byte) Block.getIdFromBlock(stuckBlock));
        compound.setByte("shake", (byte) throwableShake);
        compound.setByte("inGround", (byte) (inGround ? 1 : 0));

        if ((throwerName == null || throwerName.isEmpty()) && thrower instanceof EntityPlayer) {
            throwerName = thrower.getCommandSenderEntity().getName();
        }

        compound.setString("ownerName", throwerName == null ? "" : throwerName);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        stuckBlockX = compound.getShort("xTile");
        stuckBlockY = compound.getShort("yTile");
        stuckBlockZ = compound.getShort("zTile");
        stuckBlock = Block.getBlockById(compound.getByte("inTile") & 255);

        throwableShake = compound.getByte("shake") & 255;
        inGround = compound.getByte("inGround") == 1;

        throwerName = compound.getString("ownerName");
        if (throwerName != null && throwerName.isEmpty()) throwerName = null;
    }

    public EntityLivingBase getThrower() {
        if (thrower == null && throwerName != null && throwerName.length() > 0) {
            thrower = world.getPlayerEntityByName(throwerName);
        }
        return thrower;
    }

    @Override
    public void setThrower(EntityLivingBase thrower) {
        this.thrower = thrower;
    }

    /* ================================== Additional Getters =====================================*/
    //Use lombok for love of god

    protected float getAirDrag() {
        return 0.99F;
    }

    protected float getWaterDrag() {
        return 0.8F;
    }

    protected int groundDespawn() {
        return 1200;
    }
}
