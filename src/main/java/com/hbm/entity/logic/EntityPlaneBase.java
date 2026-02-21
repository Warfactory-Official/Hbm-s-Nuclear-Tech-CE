package com.hbm.entity.logic;

import com.google.common.collect.ImmutableSet;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.MainRegistry;
import com.hbm.particle.helper.ExplosionSmallCreator;
import com.hbm.util.ParticleUtil;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public abstract class EntityPlaneBase extends Entity implements IChunkLoader {

    public static final DataParameter<Float> HEALTH = EntityDataManager.createKey(EntityPlaneBase.class, DataSerializers.FLOAT);

    protected int turnProgress;
    protected double syncPosX;
    protected double syncPosY;
    protected double syncPosZ;
    protected double syncYaw;
    protected double syncPitch;
    @SideOnly(Side.CLIENT)
    protected double velocityX;
    @SideOnly(Side.CLIENT)
    protected double velocityY;
    @SideOnly(Side.CLIENT)
    protected double velocityZ;

    private ForgeChunkManager.Ticket loaderTicket;
    private List<ChunkPos> loadedChunks = new ArrayList<ChunkPos>();

    public float health = getMaxHealth();
    public int timer = getLifetime();

    public EntityPlaneBase(World world) { super(world); }

    public float getMaxHealth() { return 50F; }
    public int getLifetime() { return 200; }

    @Override public boolean canBeCollidedWith() { return this.health > 0; }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if(source == ModDamageSource.nuclearBlast) return false;
        if(this.isEntityInvulnerable(null)) return false;
        if(!this.isDead && !this.world.isRemote && this.health > 0) {
            health -= amount;
            if(this.health <= 0) this.killPlane();
        }
        return true;
    }

    protected void killPlane() {
        ExplosionSmallCreator.composeEffect(world, posX, posY, posZ, 25, 3.5F, 2F);
        world.playSound(null, posX, posY, posZ, HBMSoundHandler.planeShotDown, SoundCategory.NEUTRAL, 25.0F, 1.0F);
    }

    @Override
    protected void entityInit() {
        init(ForgeChunkManager.requestTicket(MainRegistry.instance, world, ForgeChunkManager.Type.ENTITY));
        this.dataManager.register(HEALTH, 50F);
    }

    @Override
    public void init(ForgeChunkManager.Ticket ticket) {
        if(!world.isRemote && ticket != null) {
            if(loaderTicket == null) {
                loaderTicket = ticket;
                loaderTicket.bindEntity(this);
                loaderTicket.getModData();
            }
            ForgeChunkManager.forceChunk(loaderTicket, new ChunkPos(chunkCoordX, chunkCoordZ));
        }
    }

    @Override
    public void onUpdate() {

        if(!world.isRemote) {
            this.dataManager.set(HEALTH, health);
        } else {
            health = this.dataManager.get(HEALTH);
        }

        if(world.isRemote) {

            this.lastTickPosX = this.posX;
            this.lastTickPosY = this.posY;
            this.lastTickPosZ = this.posZ;
            if(this.turnProgress > 0) {
                double interpX = this.posX + (this.syncPosX - this.posX) / (double) this.turnProgress;
                double interpY = this.posY + (this.syncPosY - this.posY) / (double) this.turnProgress;
                double interpZ = this.posZ + (this.syncPosZ - this.posZ) / (double) this.turnProgress;
                double d = MathHelper.wrapDegrees(this.syncYaw - (double) this.rotationYaw);
                this.rotationYaw = (float) ((double) this.rotationYaw + d / (double) this.turnProgress);
                this.rotationPitch = (float)((double)this.rotationPitch + (this.syncPitch - (double)this.rotationPitch) / (double)this.turnProgress);
                --this.turnProgress;
                this.setPosition(interpX, interpY, interpZ);
            } else {
                this.setPosition(this.posX, this.posY, this.posZ);
            }

        } else {
            this.lastTickPosX = this.prevPosX = posX;
            this.lastTickPosY = this.prevPosY = posY;
            this.lastTickPosZ = this.prevPosZ = posZ;
            this.setPosition(posX + motionX, posY + motionY, posZ + motionZ);

            this.rotation();

            if(this.health <= 0) {
                motionY -= 0.025;

                for(int i = 0; i < 10; i++) ParticleUtil.spawnGasFlame(this.world, this.posX + rand.nextGaussian() * 0.5 - motionX * 2, this.posY + rand.nextGaussian() * 0.5 - motionY * 2, this.posZ + rand.nextGaussian() * 0.5 - motionZ * 2, 0.0, 0.1, 0.0);
                BlockPos pos = new BlockPos((int)this.posX, (int)this.posY, (int)this.posZ);
                if((world.getBlockState(pos).getBlock() != Blocks.AIR || posY < 0)) {
                    this.setDead();
                    new ExplosionVNT(world, posX, posY, posZ, 15F).makeStandard().explode();
                    world.playSound(null, posX, posY, posZ, HBMSoundHandler.planeCrash, SoundCategory.NEUTRAL, 25.0F, 1.0F);
                    return;
                }
            } else {
                this.motionY = 0F;
            }

            if(this.ticksExisted > timer) this.setDead();
            loadNeighboringChunks((int)Math.floor(posX / 16D), (int)Math.floor(posZ / 16D));
        }
    }

    protected void rotation() {
        float motionHorizontal = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
        this.rotationPitch = (float) (Math.atan2(this.motionY, motionHorizontal) * 180.0D / Math.PI) - 90;
        while(this.rotationPitch - this.prevRotationPitch < -180.0F) this.prevRotationPitch -= 360.0F;
        while(this.rotationPitch - this.prevRotationPitch >= 180.0F) this.prevRotationPitch += 360.0F;
        while(this.rotationYaw - this.prevRotationYaw < -180.0F) this.prevRotationYaw -= 360.0F;
        while(this.rotationYaw - this.prevRotationYaw >= 180.0F) this.prevRotationYaw += 360.0F;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setVelocity(double velX, double velY, double velZ) {
        this.velocityX = this.motionX = velX;
        this.velocityY = this.motionY = velY;
        this.velocityZ = this.motionZ = velZ;
    }

    @Override // setPositionAndRotation2 on 1.7
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch,
                                             int increments, boolean teleport) {
        this.syncPosX = x;
        this.syncPosY = y;
        this.syncPosZ = z;
        this.syncYaw  = yaw;
        this.syncPitch = pitch;
        if (teleport || increments <= 0) {
            this.turnProgress = 0;
            this.setPosition(x, y, z);
            this.rotationYaw = yaw;
            this.rotationPitch = pitch;
            return;
        }
        this.turnProgress = increments;
        this.motionX = this.velocityX;
        this.motionY = this.velocityY;
        this.motionZ = this.velocityZ;
    }

    @Override
    public void setDead() {
        super.setDead();
        this.clearChunkLoader();
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        ticksExisted = nbt.getInteger("ticksExisted");
        this.getDataManager().set(HEALTH, nbt.getFloat("health"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setInteger("ticksExisted", ticksExisted);
        nbt.setFloat("health", this.getDataManager().get(HEALTH));
    }

    public void clearChunkLoader() {
        if(!world.isRemote && loaderTicket != null) {
            ForgeChunkManager.releaseTicket(loaderTicket);
            this.loaderTicket = null;
        }
    }

    @Override
    public void loadNeighboringChunks(int newChunkX, int newChunkZ) {
        if(!world.isRemote && loaderTicket != null) {
            for(ChunkPos chunk : ImmutableSet.copyOf(loaderTicket.getChunkList())) {
                ForgeChunkManager.unforceChunk(loaderTicket, chunk);
            }
            loadedChunks.clear();
            loadedChunks.add(new ChunkPos(newChunkX, newChunkZ));
            for(ChunkPos chunk : loadedChunks) ForgeChunkManager.forceChunk(loaderTicket, chunk);
        }
    }

    @Override @SideOnly(Side.CLIENT) public boolean isInRangeToRenderDist(double distance) { return true; }
}
