package com.hbm.entity.projectile;

import com.hbm.api.entity.IRadarDetectable;
import com.hbm.config.ServerConfig;
import com.hbm.entity.logic.IChunkLoader;
import com.hbm.entity.projectile.rocketbehavior.IRocketSteeringBehavior;
import com.hbm.entity.projectile.rocketbehavior.IRocketTargetingBehavior;
import com.hbm.entity.projectile.rocketbehavior.RocketSteeringBallisticArc;
import com.hbm.entity.projectile.rocketbehavior.RocketTargetingPredictive;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.ItemAmmoHIMARS;
import com.hbm.main.MainRegistry;
import com.hbm.particle.helper.HbmEffectNT;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
@AutoRegister(name = "entity_artillery_rocket", trackingRange = 1000)
public class EntityArtilleryRocket extends EntityThrowableNT
    implements IChunkLoader, IRadarDetectable {

  private Ticket loaderTicket;

  public Entity targetEntity = null;
  public Vec3d lastTargetPos;

  public IRocketTargetingBehavior targeting;
  public IRocketSteeringBehavior steering;
  private boolean awaitingTicketRestore;

  private static final DataParameter<Integer> TYPE =
      EntityDataManager.createKey(EntityArtilleryRocket.class, DataSerializers.VARINT);

  public EntityArtilleryRocket(World world) {
    super(world);
    this.ignoreFrustumCheck = true;

    this.targeting = new RocketTargetingPredictive();
    this.steering = new RocketSteeringBallisticArc();
  }

  @Override
  protected void entityInit() {
    super.entityInit();
    this.dataManager.register(TYPE, 0);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public boolean isInRangeToRenderDist(double distance) {
    return true;
  }

  public EntityArtilleryRocket setType(int type) {
    this.dataManager.set(TYPE, type);
    return this;
  }

  public ItemAmmoHIMARS.HIMARSRocket getType() {
    try {
      return ItemAmmoHIMARS.itemTypes[this.dataManager.get(TYPE)];
    } catch (Exception ex) {
      return ItemAmmoHIMARS.itemTypes[0];
    }
  }

  public EntityArtilleryRocket setTarget(Entity target) {
    this.targetEntity = target;
    setTarget(target.posX, target.posY - target.getYOffset() + target.height / 2D, target.posZ);
    return this;
  }

  public EntityArtilleryRocket setTarget(double x, double y, double z) {
    this.lastTargetPos =  new Vec3d(x, y, z);
    return this;
  }

  public Vec3d getLastTarget() {
    return this.lastTargetPos;
  }

  @Override
  public void onUpdate() {

    if (world.isRemote) {
      this.lastTickPosX = this.posX;
      this.lastTickPosY = this.posY;
      this.lastTickPosZ = this.posZ;
    }

    super.onUpdate();

    if (!world.isRemote) {
      requestChunkLoaderTicketIfNeeded();

      if (this.lastTargetPos == null) this.lastTargetPos = new Vec3d(this.posX, this.posY, this.posZ);

      //close to the target, stop steering and fly straight at it
      Vec3d delta = new Vec3d(
              this.lastTargetPos.x - this.posX,
              this.lastTargetPos.y - this.posY,
              this.lastTargetPos.z - this.posZ);
      double momentum = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ) * motionMult();
      if (delta.length() <= momentum * 1.5D) {
        disableSteering(delta, momentum);
      } else {
        if (this.targeting != null && this.targetEntity != null)
          this.targeting.recalculateTargetPosition(this, this.targetEntity);
        if (this.steering != null) this.steering.adjustCourse(this, 25D, 15D);
      }

      loadNeighboringChunks((int) Math.floor(posX / 16D), (int) Math.floor(posZ / 16D));
      this.getType().onUpdate(this);
    } else {

      Vec3d v = new Vec3d(lastTickPosX - posX, lastTickPosY - posY, lastTickPosZ - posZ);
      double velocity = v.length();
      v = v.normalize();

      int offset = 6;
      if (velocity > 1) {
        for (int i = offset; i < velocity + offset; i++) {
           MainRegistry.proxy.effectNT(HbmEffectNT.ExKeroseneOld, posX + v.x * i, posY + v.y * i, posZ + v.z * i);
        }
      }
    }
  }

  public int health = 100;

  @Override
  public boolean attackEntityFrom(DamageSource source, float amount) {
    if (!ServerConfig.ARTILLERY_PROJECTILES_SHOOTABLE.get() || this.isEntityInvulnerable(source)) return false;
    if (this.health > 0 && !this.world.isRemote) {
      this.health -= (int) amount;
      if (this.health <= 0) this.setDead();
    }
    return true;
  }

  /** Drops the guidance and points the velocity straight at the target, keeping the speed */
  public void disableSteering(Vec3d delta, double momentum) {
    if (this.steering == null && this.targeting == null) return;

    this.steering = null;
    this.targeting = null;

    if (delta.lengthSquared() > 1.0E-6D) {
      Vec3d dir = delta.normalize();
      this.motionX = dir.x * momentum / motionMult();
      this.motionY = dir.y * momentum / motionMult();
      this.motionZ = dir.z * momentum / motionMult();
    }
  }

  @Override
  protected void onImpact(RayTraceResult mop) {
    if (!world.isRemote) {
      this.getType().onImpact(this, mop);
    }
  }

  @Override
  public boolean shouldBootstrapChunkLoader() {
    return loaderTicket == null && !awaitingTicketRestore;
  }

  @Override
  public void init(ForgeChunkManager.Ticket ticket) {
    if (!world.isRemote && ticket != null) {
      if (loaderTicket == null) {
        loaderTicket = ticket;
        loaderTicket.bindEntity(this);
        loaderTicket.getModData();
      } else if(loaderTicket != ticket) {
        ForgeChunkManager.releaseTicket(ticket);
      }
      ForgeChunkManager.forceChunk(loaderTicket, new ChunkPos(chunkCoordX, chunkCoordZ));
    }
  }

  List<ChunkPos> loadedChunks = new ArrayList<>();

  public void loadNeighboringChunks(int newChunkX, int newChunkZ) {
    if (!world.isRemote && loaderTicket != null) {

      clearChunkLoader();

      loadedChunks.clear();
      loadedChunks.add(new ChunkPos(newChunkX, newChunkZ));

      // ChunkCoordIntPair doesnt exist in 1.12.2
      // loadedChunks.add(new ChunkCoordIntPair(newChunkX + (int) Math.floor((this.posX +
      // this.motionX) / 16D), newChunkZ + (int) Math.floor((this.posZ + this.motionZ) / 16D)));

      for (ChunkPos chunk : loadedChunks) {
        ForgeChunkManager.forceChunk(loaderTicket, chunk);
      }
    }
  }

  public void killAndClear() {
    this.setDead();
    this.clearChunkLoader();
  }

  @Override
  public void setDead() {
    super.setDead();
    this.clearChunkLoader();
  }

  public void clearChunkLoader() {
    if (!world.isRemote && loaderTicket != null) {
      for (ChunkPos chunk : loadedChunks) {
        ForgeChunkManager.unforceChunk(loaderTicket, chunk);
      }
      loadedChunks.clear();
      if(this.isDead) {
        ForgeChunkManager.releaseTicket(loaderTicket);
        loaderTicket = null;
      }
    }
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound nbt) {
    super.writeEntityToNBT(nbt);

    if(this.lastTargetPos == null) {
      this.lastTargetPos = new Vec3d(posX, posY, posZ);
    }

    nbt.setDouble("targetX", this.lastTargetPos.x);
    nbt.setDouble("targetY", this.lastTargetPos.y);
    nbt.setDouble("targetZ", this.lastTargetPos.z);

    nbt.setInteger("type", this.dataManager.get(TYPE));
    nbt.setInteger("health", this.health);
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound nbt) {
    super.readEntityFromNBT(nbt);
    awaitingTicketRestore = true;

    this.lastTargetPos = new Vec3d(nbt.getDouble("targetX"), nbt.getDouble("targetY"), nbt.getDouble("targetZ"));
    this.setType(nbt.getInteger("type"));
    if (nbt.hasKey("health")) this.health = nbt.getInteger("health");
  }

  @Override
  protected float getAirDrag() {
    return 1.0F;
  }

  @Override
  public float getGravityVelocity() {
    return this.steering != null ? 0F : 0.01F;
  }

  @Override
  public RadarTargetType getTargetType() {
    return RadarTargetType.ARTILLERY;
  }

  private void requestChunkLoaderTicketIfNeeded() {
    if(world.isRemote || loaderTicket != null) return;
    if(awaitingTicketRestore) {
      awaitingTicketRestore = false;
      return;
    }
    init(ForgeChunkManager.requestTicket(MainRegistry.instance, world, ForgeChunkManager.Type.ENTITY));
  }
}
