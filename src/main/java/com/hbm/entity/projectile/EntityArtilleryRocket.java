package com.hbm.entity.projectile;

import com.hbm.api.entity.IRadarDetectable;
import com.hbm.entity.logic.IChunkLoader;
import com.hbm.entity.projectile.rocketbehavior.IRocketSteeringBehavior;
import com.hbm.entity.projectile.rocketbehavior.IRocketTargetingBehavior;
import com.hbm.entity.projectile.rocketbehavior.RocketSteeringBallisticArc;
import com.hbm.entity.projectile.rocketbehavior.RocketTargetingPredictive;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.ItemAmmoHIMARS;
import com.hbm.main.MainRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
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
    init(
        ForgeChunkManager.requestTicket(
            MainRegistry.instance, world, ForgeChunkManager.Type.ENTITY));
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

      if (this.targeting == null) {
        this.targeting = new RocketTargetingPredictive();
      }
      if (this.steering == null) {
        this.steering = new RocketSteeringBallisticArc();
      }

      if (this.targetEntity == null) {
        Vec3d delta = new Vec3d(
                this.lastTargetPos.x - this.posX,
                this.lastTargetPos.y - this.posY,
                this.lastTargetPos.z - this.posZ);
        if (delta.length() <= 15D) {
          this.targeting = null;
          this.steering = null;
        }
      }

      if (this.targeting != null && this.targetEntity != null)
        this.targeting.recalculateTargetPosition(this, this.targetEntity);
      if (this.steering != null) this.steering.adjustCourse(this, 25D, 15D);

      loadNeighboringChunks((int) Math.floor(posX / 16D), (int) Math.floor(posZ / 16D));
      this.getType().onUpdate(this);
    } else {

      Vec3d v = new Vec3d(lastTickPosX - posX, lastTickPosY - posY, lastTickPosZ - posZ);
      double velocity = v.length();
      v = v.normalize();

      int offset = 6;
      if (velocity > 1) {
        for (int i = offset; i < velocity + offset; i++) {
//          NBTTagCompound data = new NBTTagCompound();
//          data.setDouble("posX", posX + v.x * i);
//          data.setDouble("posY", posY + v.y * i);
//          data.setDouble("posZ", posZ + v.z * i);
//          data.setString("type", "exKerosene");
//          MainRegistry.proxy.effectNT(data);
           MainRegistry.proxy.spawnParticle(posX + v.x * i, posY + v.y * i, posZ + v.z * i, "exKerosene", null);
        }
      }
    }
  }

  @Override
  protected void onImpact(RayTraceResult mop) {
    if (!world.isRemote) {
      this.getType().onImpact(this, mop);
    }
  }

  @Override
  public void init(ForgeChunkManager.Ticket ticket) {
    if (!world.isRemote && ticket != null) {
      if (loaderTicket == null) {
        loaderTicket = ticket;
        loaderTicket.bindEntity(this);
        loaderTicket.getModData();
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

  public void clearChunkLoader() {
    if (!world.isRemote && loaderTicket != null) {
      for (ChunkPos chunk : loadedChunks) {
        ForgeChunkManager.unforceChunk(loaderTicket, chunk);
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
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound nbt) {
    super.readEntityFromNBT(nbt);

    this.lastTargetPos = new Vec3d(nbt.getDouble("targetX"), nbt.getDouble("targetY"), nbt.getDouble("targetZ"));
    this.setType(nbt.getInteger("type"));
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
}
