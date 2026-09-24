package com.hbm.entity.mob;

import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.entity.projectile.EntityBulletBase;
import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IRadiationImmune;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.AdvancementManager;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.HbmEffectNT;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
@AutoRegister(name = "entity_ntm_ufo", trackingRange = 1000)
public class EntityUFO extends EntityFlying implements IMob, IRadiationImmune {

	public static final DataParameter<Boolean> BEAM = EntityDataManager.createKey(EntityUFO.class, DataSerializers.BOOLEAN);
	public static final DataParameter<BlockPos> WAYPOINT = EntityDataManager.createKey(EntityUFO.class, DataSerializers.BLOCK_POS);
	
	private final BossInfoServer bossInfo = (BossInfoServer)(new BossInfoServer(this.getDisplayName(), BossInfo.Color.RED, BossInfo.Overlay.PROGRESS));
	
	public int courseChangeCooldown;
	public int scanCooldown;
	/*public double waypointX;
	public double waypointY;
	public double waypointZ;*/
	public int hurtCooldown;
	public int beamTimer;
	private Entity target;
	private List<Entity> secondaries = new ArrayList<>();
	
	public EntityUFO(World p_i1587_1_) {
		super(p_i1587_1_);
		this.setSize(15F, 4F);
		this.isImmuneToFire = true;
		this.experienceValue = 500;
		this.ignoreFrustumCheck = true;
		this.deathTime = -30;
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		
		if(hurtCooldown > 0)
			return false;
		
		boolean hit = super.attackEntityFrom(source, amount);
		
		if(hit)
			hurtCooldown = 5;
		
		return hit;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20000.0D);
	}

	@Override
	protected void updateAITasks() {
		if(!this.world.isRemote) {
			
			if(this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
				this.setDead();
				return;
			}
			
			if(this.hurtCooldown > 0) {
				this.hurtCooldown--;
			}
		}

		if(this.courseChangeCooldown > 0) {
			this.courseChangeCooldown--;
		}
		if(this.scanCooldown > 0) {
			this.scanCooldown--;
		}
		
		if(this.target != null && !this.target.isEntityAlive()) {
			this.target = null;
		}
		
		if(this.scanCooldown <= 0) {
			List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, this.getEntityBoundingBox().grow(100, 50, 100));
			this.secondaries.clear();
			this.target = null;
			
			for(Entity entity : entities) {
				
				if(!entity.isEntityAlive() || !canAttackClass(entity.getClass()))
					continue;
				
				if(entity instanceof EntityPlayer) {
					
					if(((EntityPlayer)entity).capabilities.isCreativeMode)
						continue;
					
					if(((EntityPlayer)entity).isPotionActive(MobEffects.INVISIBILITY))
						continue;
					
					if(this.target == null) {
						this.target = entity;
					} else {
						if(this.getDistanceSq(entity) < this.getDistanceSq(this.target)) {
							this.target = entity;
						}
					}
				}
				
				if(entity instanceof EntityLivingBase && this.getDistanceSq(entity) < 100 * 100 && this.canEntityBeSeen(entity) && entity != this.target) {
					this.secondaries.add(entity);
				}
			}
			
			if(this.target == null && !this.secondaries.isEmpty())
				this.target = this.secondaries.get(rand.nextInt(this.secondaries.size()));
			
			this.scanCooldown = 50;
		}
		
		if(this.target != null && this.courseChangeCooldown <= 0) {
			
			Vec3d vec = new Vec3d(this.posX - this.target.posX, 0, this.posZ - this.target.posZ);
			
			if(rand.nextInt(3) > 0)
				vec = vec.rotateYaw((float)Math.PI * 2 * rand.nextFloat());
			
			double length = vec.length();
			double overshoot = 35;
			
			int wX = (int)Math.floor(this.target.posX - vec.x / length * overshoot);
			int wZ = (int)Math.floor(this.target.posZ - vec.z / length * overshoot);
			
			this.setWaypoint(wX, Math.max(this.world.getHeight(wX, wZ) + 20 + rand.nextInt(15), (int) this.target.posY + 15),  wZ);
			
			this.courseChangeCooldown = 40 + rand.nextInt(20);
		}
		
		if(!world.isRemote) {
			
			if(beamTimer <= 0 && this.getBeam()) {
				this.setBeam(false);
			}

			if(this.target != null) {
				double dist = Math.abs(this.target.posX - this.posX) + Math.abs(this.target.posZ - this.posZ);
				if(dist < 25)
					this.beamTimer = 30;
			}
			
			if(beamTimer > 0) {
				this.beamTimer--;
				
				if(!this.getBeam()) {
					world.playSound(null, this.posX, this.posY, this.posZ, HBMSoundHandler.ufoBeam, SoundCategory.HOSTILE, 10.0F, 1.0F);
					this.setBeam(true);
				}

				int ix = (int)Math.floor(this.posX);
				int iz = (int)Math.floor(this.posZ);
				int iy = 0;
				
				for(int i = (int)Math.ceil(this.posY); i >= 0; i--) {
					
					if(this.world.getBlockState(new BlockPos(ix, i, iz)).getBlock() != Blocks.AIR) {
						iy = i;
						break;
					}
				}
				
				if(iy < this.posY) {
					List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(this.posX, iy, this.posZ, this.posX, this.posY, this.posZ).grow(5, 0, 5));
					
					for(Entity e : entities) {
						if(this.canAttackClass(e.getClass())) {
							e.attackEntityFrom(ModDamageSource.causeCombineDamage(this, e), 1000F);
							e.setFire(5);
							
							if(e instanceof EntityLivingBase)
								ContaminationUtil.contaminate((EntityLivingBase)e, HazardType.RADIATION, ContaminationType.CREATIVE, 5F);
						}
					}

					PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(HbmEffectNT.UFO, null, posX, iy + 0.5, posZ),  new TargetPoint(dimension, posX, iy + 0.5, posZ, 150));
					PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(HbmEffectNT.UFO, null, posX + this.motionX * 0.5, iy + 0.5, posZ + this.motionZ * 0.5),  new TargetPoint(dimension, posX + this.motionX * 0.5, iy + 0.5, posZ + this.motionZ * 0.5, 150));
				}
			}
			
			if(this.ticksExisted % 300 < 200) {
				
				if(this.ticksExisted % 4 == 0) {
					
					if(!this.secondaries.isEmpty()){
						Entity e = this.secondaries.get(rand.nextInt(this.secondaries.size()));
						
						if(!e.isEntityAlive())
							this.secondaries.remove(e);
						else
							laserAttack(e);
						
					} else if(this.target != null) {
						laserAttack(this.target);
					}
					
				} else if(this.ticksExisted % 4 == 2) {
					if(this.target != null) {
						laserAttack(this.target);
					}
				}
			} else {

				if(this.ticksExisted % 20 == 0) {
					
					if(!this.secondaries.isEmpty()){
						Entity e = this.secondaries.get(rand.nextInt(this.secondaries.size()));
						
						if(!e.isEntityAlive())
							this.secondaries.remove(e);
						else
							rocketAttack(e);
						
					} else if(this.target != null) {
						rocketAttack(this.target);
					}
					
				} else if(this.ticksExisted % 20 == 10) {
					if(this.target != null) {
						rocketAttack(this.target);
					}
				}
			}
			
		}
		
		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;
		
		if(this.courseChangeCooldown > 0) {
			
			double deltaX = this.getX() - this.posX;
			double deltaY = this.getY() - this.posY;
			double deltaZ = this.getZ() - this.posZ;
			Vec3d delta = new Vec3d(deltaX, deltaY, deltaZ);
			double len = delta.length();
			double speed = this.target instanceof EntityPlayer ? 5D : 2D;
			
			if(len > 5) {
				if(isCourseTraversable(this.getX(), this.getY(), this.getZ(), len)) {
					this.motionX = delta.x * speed / len;
					this.motionY = delta.y * speed / len;
					this.motionZ = delta.z * speed / len;
				} else {
					this.courseChangeCooldown = 0;
				}
			}
		}
	}
	
	protected void onDeathUpdate() {
		
		if(this.getBeam())
			this.setBeam(false);
		
		this.motionY -= 0.05D;
		
		if(this.deathTime == -10) {
			world.playSound(null, this.posX, this.posY, this.posZ, HBMSoundHandler.chopperDamage, SoundCategory.HOSTILE, 10.0F, 1.0F);
		}
		
		if(this.deathTime == 19 && !world.isRemote) {
			EntityNukeTorex.statFac(world, this.posX, this.posY, this.posZ, 25);
			world.spawnEntity(EntityNukeExplosionMK5.statFacNoRad(world, 25, posX + 0.5, posY + 0.5, posZ + 0.5).setDetonator(this));
            
			List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow(200, 200, 200));
			for(EntityPlayer player : players) {
				AdvancementManager.grantAchievement(player, AdvancementManager.bossUFO);
				player.inventory.addItemStackToInventory(new ItemStack(ModItems.coin_ufo));
			}
		}
		
		super.onDeathUpdate();
	}
	
	private void laserAttack(Entity e) {
		
		Vec3d vec = new Vec3d(this.posX - e.posX, 0, this.posZ - e.posZ);
		vec = vec.rotateYaw((float) Math.toRadians(-80 + rand.nextInt(160))).normalize();

		double pivotX = this.posX - vec.x * 10;
		double pivotY = this.posY + 0.5;
		double pivotZ = this.posZ - vec.z * 10;

		Vec3d heading = new Vec3d(e.posX - pivotX, e.posY + e.height / 2 - pivotY, e.posZ - pivotZ);
		heading = heading.normalize();

		EntityBulletBase bullet = new EntityBulletBase(this.world, BulletConfigSyncingUtil.WORM_LASER);
		bullet.shooter = this;
		bullet.setPosition(pivotX, pivotY, pivotZ);
		bullet.shoot(heading.x, heading.y, heading.z, 2F, 0.02F);
		this.world.spawnEntity(bullet);
		this.playSound(HBMSoundHandler.ballsLaser, 5.0F, 1.0F);
	}
	
	private void rocketAttack(Entity e) {
		Vec3d heading = new Vec3d(e.posX - this.posX, e.posY + e.height / 2 - posY - 0.5D, e.posZ - this.posZ);
		heading = heading.normalize();

		EntityBulletBase bullet = new EntityBulletBase(this.world, BulletConfigSyncingUtil.UFO_ROCKET);
		bullet.shooter = this;
		bullet.setPosition(this.posX, this.posY - 0.5D, this.posZ);
		bullet.shoot(heading.x, heading.y, heading.z, 2F, 0.02F);
		bullet.getEntityData().setInteger("homingTarget", e.getEntityId());
		this.world.spawnEntity(bullet);
		this.playSound(HBMSoundHandler.richard_fire, 5.0F, 1.0F);
	}
	
	@Override
	public boolean canAttackClass(Class entityClass) {
		return entityClass != this.getClass() && entityClass != EntityBulletBase.class;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(BEAM, false);
		this.dataManager.register(WAYPOINT, new BlockPos(0, 0, 0));
	}
	
	private boolean isCourseTraversable(double p_70790_1_, double p_70790_3_, double p_70790_5_, double p_70790_7_) {
		
		double d4 = (this.getX() - this.posX) / p_70790_7_;
		double d5 = (this.getY() - this.posY) / p_70790_7_;
		double d6 = (this.getZ() - this.posZ) / p_70790_7_;
		AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();

		for(int i = 1; i < p_70790_7_; ++i) {
			this.setEntityBoundingBox(axisalignedbb.offset(d4, d5, d6));

			if(!this.world.getCollisionBoxes(this, axisalignedbb).isEmpty()) {
				return false;
			}
		}

		return true;
	}
	
	@Override
	protected float getSoundVolume() {
		return 10.0F;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn){
		return SoundEvents.ENTITY_BLAZE_HURT;
	}
	
	@Override
	protected SoundEvent getDeathSound(){
		return null;
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound p_70014_1_) {
		super.writeEntityToNBT(p_70014_1_);
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound p_70037_1_) {
		super.readEntityFromNBT(p_70037_1_);
	}

	public void setBeam(boolean b) {
		this.dataManager.set(BEAM, b);
	}

	public boolean getBeam() {
		return this.dataManager.get(BEAM);
	}

	public void setWaypoint(int x, int y, int z) {
		this.dataManager.set(WAYPOINT, new BlockPos(x, y, z));
	}

	public BlockPos getWaypoint(){
		return this.dataManager.get(WAYPOINT);
	}
	
	public int getX() {
		return this.dataManager.get(WAYPOINT).getX();
	}

	public int getY() {
		return this.dataManager.get(WAYPOINT).getY();
	}

	public int getZ() {
		return this.dataManager.get(WAYPOINT).getZ();
	}
	
	@Override
	public void onLivingUpdate(){
		super.onLivingUpdate();
		this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
	}
	
	@Override
	public void addTrackingPlayer(EntityPlayerMP player){
		super.addTrackingPlayer(player);
		bossInfo.addPlayer(player);
	}
	
	@Override
	public void removeTrackingPlayer(EntityPlayerMP player){
		super.removeTrackingPlayer(player);
		bossInfo.removePlayer(player);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return distance < 500000;
	}
}