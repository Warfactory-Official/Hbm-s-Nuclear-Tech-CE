package com.hbm.entity.missile;

import com.hbm.api.entity.IRadarDetectable;
import com.hbm.api.entity.IRadarDetectableNT;
import com.hbm.entity.logic.IChunkLoader;
import com.hbm.entity.projectile.EntityThrowableInterp;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityMachineRadarNT;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * Some of the logic here is Bailie's creation
 */
@AutoRegister(name = "entity_missile_ab", trackingRange = 1000)
public class EntityMissileAntiBallistic extends EntityThrowableInterp implements IChunkLoader, IRadarDetectable, IRadarDetectableNT {

	private ForgeChunkManager.Ticket loaderTicket;
	public Entity tracking;
	public double velocity;
	private int activationTimer;
	private static final double baseSpeed = 1.5D;

    public EntityMissileAntiBallistic(World world) {
		super(world);
		this.motionY = baseSpeed;
		this.setSize(1F, 8F);
	}
	@Override
	protected void entityInit() {
		super.entityInit();
		init(ForgeChunkManager.requestTicket(MainRegistry.instance, world, ForgeChunkManager.Type.ENTITY));
	}

	@Override
	protected double motionMult() {
		return velocity;
	}

	@Override
	public boolean doesImpactEntities() {
		return false;
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if(!world.isRemote) {

			if(velocity < 6) velocity += 0.1;

			if(activationTimer < 10) {
				activationTimer++;
				motionY = baseSpeed;
			} else {
				Entity prevTracking = this.tracking;

				if(this.tracking == null || this.tracking.isDead) this.targetMissile();

				if(prevTracking == null && this.tracking != null) {
					ExplosionLarge.spawnShock(world, posX, posY, posZ, 24, 3F);
				}

				if (this.tracking != null) { // Ensure tracking target exists
				    double distance = Math.sqrt(
				        Math.pow(this.tracking.posX - this.posX, 2) +
				        Math.pow(this.tracking.posY - this.posY, 2) +
				        Math.pow(this.tracking.posZ - this.posZ, 2)
				    );

				    if (distance < 15) { // Check if the distance is less than 10

				        List<Entity> explosionRadius = world.getEntitiesWithinAABB(Entity.class,
				            new AxisAlignedBB(posX - 15, posY - 15, posZ - 15, posX + 15, posY + 15, posZ + 15));

				        for (Entity entity : explosionRadius) {
				            if (entity instanceof EntityMissileBaseNT) {
				                EntityMissileBaseNT target = (EntityMissileBaseNT) entity;
				                target.health -= 51; //EntityMissileBaseNT has default health of 50 to die
				            }
				        }

				        this.setDead(); // Destroy the anti-missile
				        ExplosionLarge.explode(world, thrower, posX, posY, posZ, 20F, true, false, false);
				    }
				}
				if(this.tracking != null) {
					this.aimAtTarget();
				} else {
					if(this.ticksExisted > 600) this.setDead();
				}
			}

			loadNeighboringChunks((int) Math.floor(posX / 16), (int) Math.floor(posZ / 16));

			if(this.posY > 2000 && (this.tracking == null || this.tracking.isDead)) this.setDead();

		} else {

			Vec3d vec = new Vec3d(motionX, motionY, motionZ).normalize();
			MainRegistry.proxy.particleControl(posX - vec.x, posY - vec.y, posZ - vec.z, 2);
		}
		super.onUpdate();
		float f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
		this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
		for(this.rotationPitch = (float) (Math.atan2(this.motionY, f2) * 180.0D / Math.PI) - 90; this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F);
		while(this.rotationPitch - this.prevRotationPitch >= 180.0F) this.prevRotationPitch += 360.0F;
		while(this.rotationYaw - this.prevRotationYaw < -180.0F) this.prevRotationYaw -= 360.0F;
		while(this.rotationYaw - this.prevRotationYaw >= 180.0F) this.prevRotationYaw += 360.0F;
	}

	/** Detects and caches nearby EntityMissileBaseNT */
    private void targetMissile() {
		Entity closest = null;
		double dist = 1_000;
		int radarRange = TileEntityMachineRadarNT.radarRange;
        AxisAlignedBB boundingBox = new AxisAlignedBB(
                this.posX - radarRange, this.posY, this.posZ - radarRange,
                this.posX + radarRange, this.posY + radarRange, +this.posZ + radarRange
        );

		List<Entity> entitiesWithinRange = world.getEntitiesWithinAABB(Entity.class, boundingBox);

		for(Entity e : entitiesWithinRange) {
			if(e.dimension != this.dimension) continue;
			if(!(e instanceof EntityMissileBaseNT)) continue; //can only lock onto missiles
			if(e instanceof EntityMissileStealth) continue; //cannot lock onto missiles with stealth coating

			Vec3d vec = new Vec3d(e.posX - posX, e.posY - posY, e.posZ - posZ);

			if(vec.length() < dist) {
				closest = e;
			}
		}
		this.tracking = closest;
	}

	/** Predictive targeting system */
    private void aimAtTarget() {
		Vec3d delta = new Vec3d(tracking.posX - posX, tracking.posY - posY, tracking.posZ - posZ);
		double intercept = delta.length() / (baseSpeed * this.velocity);
		Vec3d predicted = new Vec3d(tracking.posX + (tracking.posX - tracking.lastTickPosX) * intercept, tracking.posY + (tracking.posY - tracking.lastTickPosY) * intercept, tracking.posZ + (tracking.posZ - tracking.lastTickPosZ) * intercept);
		Vec3d motion = new Vec3d(predicted.x - posX, predicted.y - posY, predicted.z - posZ).normalize();

		if(delta.length() < 10 && activationTimer >= 40) {
			System.out.println("I DIED HERE 1");
			this.setDead();
			ExplosionLarge.explode(world, thrower, posX, posY, posZ, 15F, true, false, false);
		}

		this.motionX = motion.x * baseSpeed;
		this.motionY = motion.y * baseSpeed;
		this.motionZ = motion.z * baseSpeed;
	}

	@Override
	protected void onImpact(RayTraceResult mop) {
		if(this.activationTimer >= 10) {
			this.setDead();
			ExplosionLarge.explode(world, thrower, posX, posY, posZ, 20F, true, false, false);
		}
	}

	@Override
	public float getGravityVelocity() {
		return 0.0F;
	}

	@Override
	protected float getAirDrag() {
		return 1F;
	}

	@Override
	protected float getWaterDrag() {
		return 1F;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.velocity = nbt.getDouble("veloc");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setDouble("veloc", this.velocity);
	}

	@Override
	public void init(ForgeChunkManager.Ticket ticket) {
		if(!world.isRemote) {
			if(ticket != null) {
				if(loaderTicket == null) {
					loaderTicket = ticket;
					loaderTicket.bindEntity(this);
					loaderTicket.getModData();
				}
				ForgeChunkManager.forceChunk(loaderTicket, new ChunkPos(chunkCoordX, chunkCoordZ));
			}
		}
	}

	private final List<ChunkPos> loadedChunks = new ArrayList<>();

	public void loadNeighboringChunks(int newChunkX, int newChunkZ) {
		if(!world.isRemote && loaderTicket != null) {

			clearChunkLoader();

			loadedChunks.clear();
			for(int i = -1; i <= 1; i++) for(int j = -1; j <= 1; j++) loadedChunks.add(new ChunkPos(newChunkX + i, newChunkZ + j));

			for(ChunkPos chunk : loadedChunks) {
				ForgeChunkManager.forceChunk(loaderTicket, chunk);
			}
		}
	}

	@Override
	public void setDead() {
		super.setDead();
		this.clearChunkLoader();
	}

    private void clearChunkLoader() {
		if(!world.isRemote && loaderTicket != null) {
			for(ChunkPos chunk : loadedChunks) {
				ForgeChunkManager.unforceChunk(loaderTicket, chunk);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return true;
	}

	@Override
	public RadarTargetType getTargetType() {
		return RadarTargetType.MISSILE_AB;
	}

	@Override
	public String getTranslationKey() {
		return "radar.target.abm";
	}

	@Override
	public int getBlipLevel() {
		return IRadarDetectableNT.TIER_AB;
	}

	@Override
	public boolean canBeSeenBy(Object radar) {
		return true;
	}

	@Override
	public boolean paramsApplicable(RadarScanParams params) {
		return params.scanMissiles;
	}

	@Override
	public boolean suppliesRedstone(RadarScanParams params) {
		return false;
	}
}
