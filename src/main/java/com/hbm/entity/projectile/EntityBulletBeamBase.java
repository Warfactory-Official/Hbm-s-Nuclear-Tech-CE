package com.hbm.entity.projectile;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.util.Vec3NT;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;
@AutoRegister(name = "entity_beam_mk4", trackingRange = 256)
public class EntityBulletBeamBase extends Entity implements IEntityAdditionalSpawnData {

    public EntityLivingBase thrower;
    public BulletConfig config;
    public float damage;
    public double headingX;
    public double headingY;
    public double headingZ;
    public double beamLength;
    private static final DataParameter<Integer> BULLET_CONFIG_ID =
            EntityDataManager.createKey(EntityBulletBeamBase.class, DataSerializers.VARINT);

    public EntityBulletBeamBase(World world) {
        super(world);
        this.ignoreFrustumCheck = true;
        if(world.isRemote)
            setRenderDistanceWeight(10.0D);
        this.setSize(0.5F, 0.5F);
        this.isImmuneToFire = true;
    }

    public EntityBulletBeamBase(EntityLivingBase entity, BulletConfig config, float baseDamage) {
        this(entity.world);

        this.thrower = entity;
        this.setBulletConfig(config);

        this.damage = baseDamage * this.config.damageMult;
    }

    public EntityBulletBeamBase(EntityLivingBase entity, BulletConfig config, float baseDamage, float angularInaccuracy, double sideOffset, double heightOffset, double frontOffset) {
        this(entity.world);

        this.thrower = entity;
        this.setBulletConfig(config);

        this.damage = baseDamage * this.config.damageMult;

        this.setLocationAndAngles(thrower.posX, thrower.posY + thrower.getEyeHeight(), thrower.posZ, thrower.rotationYaw + (float) rand.nextGaussian() * angularInaccuracy, thrower.rotationPitch + (float) rand.nextGaussian() * angularInaccuracy);

        Vec3NT offset = new Vec3NT(sideOffset, heightOffset, frontOffset);
        offset.rotateAroundXRad(this.rotationPitch / 180F * (float) Math.PI);
        offset.rotateAroundYRad(-this.rotationYaw / 180F * (float) Math.PI);

        this.posX += offset.x;
        this.posY += offset.y;
        this.posZ += offset.z;

        this.setPosition(this.posX, this.posY, this.posZ);

        this.headingX = -MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
        this.headingZ = MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
        this.headingY = -MathHelper.sin((this.rotationPitch) / 180.0F * (float) Math.PI);

        double range = 250D;
        this.headingX *= range;
        this.headingY *= range;
        this.headingZ *= range;

        performHitscan();
    }

    public void setRotationsFromVector(Vec3NT delta) {
        this.rotationPitch = (float) (-Math.asin(delta.y / delta.length()) * 180D / Math.PI);
        this.rotationYaw = (float) (-Math.atan2(delta.x, delta.z) * 180D / Math.PI);

        this.headingX = -MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
        this.headingZ = MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
        this.headingY = -MathHelper.sin((this.rotationPitch) / 180.0F * (float) Math.PI);
    }

    public void performHitscanExternal(double range) {
        this.headingX *= range;
        this.headingY *= range;
        this.headingZ *= range;
        performHitscan();
    }

    public EntityLivingBase getThrower() {
        return this.thrower;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(BULLET_CONFIG_ID, 0);
    }

    public BulletConfig getBulletConfig() {
        int id = this.dataManager.get(BULLET_CONFIG_ID);
        if (id < 0 || id > BulletConfig.configs.size()) return null;
        return BulletConfig.configs.get(id);
    }

    public void setBulletConfig(BulletConfig config) {
        this.config = config;
        this.dataManager.set(BULLET_CONFIG_ID, config.id);
    }

    @Override
    public void onUpdate() {

        if (config == null) config = this.getBulletConfig();

        if (config == null) {
            this.setDead();
            return;
        }

        if (config.onUpdate != null) config.onUpdate.accept(this);

        super.onUpdate();

        if (!world.isRemote && this.ticksExisted > config.expires) this.setDead();
    }

    protected void performHitscan() {

        Vec3d pos = new Vec3d(this.posX, this.posY, this.posZ);
        Vec3d nextPos = new Vec3d(this.posX + this.headingX, this.posY + this.headingY, this.posZ + this.headingZ);
        RayTraceResult mop = null;
        if (!this.isSpectral()) mop = this.world.rayTraceBlocks(pos, nextPos, false, true, false);
        pos = new Vec3d(this.posX, this.posY, this.posZ);
        nextPos = new Vec3d(this.posX + this.headingX, this.posY + this.headingY, this.posZ + this.headingZ);

        if (mop != null) {
            nextPos = new Vec3d(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
        }

        if (!this.world.isRemote && this.doesImpactEntities()) {

            Entity hitEntity = null;
            List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(this.headingX, this.headingY, this.headingZ).grow(1.0D, 1.0D, 1.0D));
            double nearest = 0.0D;
            RayTraceResult nonPenImpact = null;
            RayTraceResult coinHit = null;

            double closestCoin = 0;
            EntityCoin hitCoin = null;

            for(Entity entity : list) {
                if(entity.isDead) continue;
                if(entity instanceof EntityCoin) {
                    double hitbox = 0.3F;
                    AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(hitbox, hitbox, hitbox);
                    RayTraceResult hitMop = aabb.calculateIntercept(pos, nextPos);
                    if(hitMop != null) {
                        double dist = pos.distanceTo(hitMop.hitVec);
                        if(closestCoin == 0 || dist < closestCoin) {
                            closestCoin = dist;
                            hitCoin = (EntityCoin) entity;
                            coinHit = hitMop;
                        }
                    }
                }
            }

            for (Entity value : list) {

                if (value.canBeCollidedWith() && value != thrower) {
                    double hitbox = 0.3F;
                    AxisAlignedBB aabb = value.getEntityBoundingBox().grow(hitbox, hitbox, hitbox);
                    RayTraceResult hitMop = aabb.calculateIntercept(pos, nextPos);

                    if (hitMop != null) {

                        double dist = pos.distanceTo(hitMop.hitVec);

                        // if penetration is enabled, run impact for all intersecting entities
                        if (this.doesPenetrate()) {
                            if(hitCoin == null || dist < closestCoin) {
                                this.onImpact(new RayTraceResult(value, hitMop.hitVec));
                            }
                        } else {
                            if (dist < nearest || nearest == 0.0D) {
                                hitEntity = value;
                                nearest = dist;
                                nonPenImpact = hitMop;
                            }
                        }
                    }
                }
            }

            // if not, only run it for the closest MOP
            if (!this.doesPenetrate() && hitEntity != null) {
                mop = new RayTraceResult(hitEntity, nonPenImpact.hitVec);
            }

            if(hitCoin != null) {
                Vec3NT vec = new Vec3NT(coinHit.hitVec.x - posX, coinHit.hitVec.y - posY, coinHit.hitVec.z - posZ);
                this.beamLength = vec.length();

                double range = 50;
                List<Entity> targets = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(coinHit.hitVec.x, coinHit.hitVec.y, coinHit.hitVec.z, coinHit.hitVec.x, coinHit.hitVec.y, coinHit.hitVec.z).grow(range, range, range));
                Entity nearestCoin = null;
                Entity nearestPlayer = null;
                Entity nearestMob = null;
                Entity nearestOther = null;
                double coinDist = 0;
                double playerDist = 0;
                double mobDist = 0;
                double otherDist = 0;

                hitCoin.setDead();

                // well i mean we could just uuse a single var for all variants and then overwrite stuff
                // when we run into things with higher priority. however i can't be assed fuck off
                for(Entity entity : targets) {
                    if(entity == this.getThrower()) continue;
                    if(entity.isDead) continue;
                    double dist = entity.getDistance(hitCoin);
                    if(dist > range) continue;

                    switch (entity) {
                        case EntityCoin entityCoin -> {
                            if (coinDist == 0 || dist < coinDist) {
                                coinDist = dist;
                                nearestCoin = entity;
                            }
                        }
                        case EntityPlayer entityPlayer -> {
                            if (playerDist == 0 || dist < playerDist) {
                                playerDist = dist;
                                nearestPlayer = entity;
                            }
                        }
                        case EntityMob entityMob -> {
                            if (mobDist == 0 || dist < mobDist) {
                                mobDist = dist;
                                nearestMob = entity;
                            }
                        }
                        case EntityLivingBase entityLivingBase -> {
                            if (otherDist == 0 || dist < otherDist) {
                                otherDist = dist;
                                nearestOther = entity;
                            }
                        }
                        default -> {
                        }
                    }
                }

                // ternary of shame
                Entity target = nearestCoin != null ? nearestCoin :
                        nearestPlayer != null ? nearestPlayer :
                                nearestMob != null ? nearestMob :
                                        nearestOther;

                EntityBulletBeamBase newBeam = new EntityBulletBeamBase(hitCoin.getThrower() != null ? hitCoin.getThrower() : this.thrower, this.config, this.damage * 1.25F);
                newBeam.setPosition(coinHit.hitVec.x, coinHit.hitVec.y, coinHit.hitVec.z);
                if(target != null) {
                    Vec3NT delta = new Vec3NT(target.posX - newBeam.posX, (target.posY + target.height / 2D) - newBeam.posY, target.posZ - newBeam.posZ);
                    newBeam.setRotationsFromVector(delta);
                } else {
                    newBeam.setRotationsFromVector(new Vec3NT(rand.nextGaussian() * 0.5, -1, rand.nextGaussian() * 0.5));
                }
                newBeam.performHitscanExternal(250D);
                world.spawnEntity(newBeam);

                NBTTagCompound data = new NBTTagCompound();
                data.setString("type", "vanillaExt");
                data.setString("mode", "largeexplode");
                data.setFloat("size", 1.5F);
                data.setByte("count", (byte)1);
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, coinHit.hitVec.x, coinHit.hitVec.y, coinHit.hitVec.z), new NetworkRegistry.TargetPoint(world.provider.getDimension(), coinHit.hitVec.x, coinHit.hitVec.y, coinHit.hitVec.z, 100));


                return;
            }
        }

        if (mop != null) {
            if (mop.typeOfHit == RayTraceResult.Type.BLOCK && this.world.getBlockState(mop.getBlockPos()).getBlock() == Blocks.PORTAL) {
                this.setPortal(mop.getBlockPos());
            } else {
                this.onImpact(mop);
            }

            Vec3d vec = new Vec3d(mop.hitVec.x - posX, mop.hitVec.y - posY, mop.hitVec.z - posZ);
            this.beamLength = vec.length();
        } else {
            Vec3d vec = new Vec3d(nextPos.x - posX, nextPos.y - posY, nextPos.z - posZ);
            this.beamLength = vec.length();
        }

    }


    protected void onImpact(RayTraceResult mop) {
        if (!world.isRemote) {
            if (this.config.onImpactBeam != null) this.config.onImpactBeam.accept(this, mop);
        }
    }

    public boolean doesImpactEntities() {
        return this.config.impactsEntities;
    }

    public boolean doesPenetrate() {
        return this.config.doesPenetrate;
    }

    public boolean isSpectral() {
        return this.config.isSpectral;
    }

    @Override
    protected void writeEntityToNBT(@NotNull NBTTagCompound nbt) {
    }

    @Override
    public boolean writeToNBTOptional(@NotNull NBTTagCompound nbt) {
        return false;
    }

    @Override
    public void readEntityFromNBT(@NotNull NBTTagCompound nbt) {
        this.setDead();
    }

    @Override
    public void writeSpawnData(ByteBuf buf) {
        buf.writeDouble(beamLength);
        buf.writeFloat(rotationYaw);
        buf.writeFloat(rotationPitch);
    }

    @Override
    public void readSpawnData(ByteBuf buf) {
        this.beamLength = buf.readDouble();
        this.rotationYaw = buf.readFloat();
        this.rotationPitch = buf.readFloat();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderOnFire() {
        return false;
    }
}
