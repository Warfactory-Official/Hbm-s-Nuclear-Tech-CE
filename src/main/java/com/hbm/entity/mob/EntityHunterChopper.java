package com.hbm.entity.mob;

import com.hbm.entity.projectile.EntityBullet;
import com.hbm.entity.projectile.EntityChopperMine;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IRadiationImmune;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.render.amlfrom1710.Vec3;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//Drillgon200: This whole thing is messed up and janky and I don't know what to about it.
@AutoRegister(name = "entity_hunter_chopper", trackingRange = 1000, eggColors = {0x000020, 0x2D2D72})
public class EntityHunterChopper extends EntityFlying implements IMob, IRadiationImmune {

	public static final DataParameter<Boolean> DYING = EntityDataManager.createKey(EntityHunterChopper.class, DataSerializers.BOOLEAN);
	
	public int courseChangeCooldown;
	public double waypointX;
	public double waypointY;
	public double waypointZ;
	private Entity targetedEntity;
	//private int aggroCooldown;
	public int prevAttackCounter;
	public int attackCounter;
	public int mineDropCounter;
	public boolean isDying = false;
	
	private final BossInfoServer bossInfo = (BossInfoServer)(new BossInfoServer(this.getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS)).setDarkenSky(true);
	
	public EntityHunterChopper(World worldIn) {
		super(worldIn);
		this.setSize(8.25F, 3.0F);
		this.isImmuneToFire = true;
		this.experienceValue = 500;
		this.ignoreFrustumCheck = true;
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isEntityInvulnerable(source) || !(source == ModDamageSource.nuclearBlast || source == ModDamageSource.blackhole || source.isExplosion()  || ModDamageSource.getIsTau(source) || ModDamageSource.getIsSubatomic(source) || ModDamageSource.getIsDischarge(source))) {
			return false;
		} else if(amount >= this.getHealth()) {
			this.initDeath();
			this.setIsDying(true);
			this.setHealth(0.1F);
			return false;
		}
		
		if(rand.nextInt(15) == 0)
		{
			if(!world.isRemote && !this.isDying)
			{
				this.world.createExplosion(this, this.posX, this.posY, this.posZ, 5F, true);
				this.dropDamageItem();
			}
		}

		for (int j = 0; j < 3; j++) {
			double d0 = rand.nextDouble() / 20 * rand.nextInt(2) == 0 ? -1 : 1;
			double d1 = rand.nextDouble() / 20 * rand.nextInt(2) == 0 ? -1 : 1;
			double d2 = rand.nextDouble() / 20 * rand.nextInt(2) == 0 ? -1 : 1;

			for (int i = 0; i < 8; i++)
				if(this.world.isRemote)
					world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX, this.posY, this.posZ, d0 * i * 0.25, d1 * i * 0.25, d2 * i * 0.25);
		}

		return super.attackEntityFrom(source, amount);
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(DYING, false);
	}
	
	@Override
	public boolean isNonBoss() {
		return false;
	}
	
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(750.0D);
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!this.world.isRemote && this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
			this.setDead();
		}
		if (!isDying) {
			this.world.playSound(null, this.posX, this.posY, this.posZ, HBMSoundHandler.nullChopper, SoundCategory.HOSTILE, 10.0F, 0.5F);

			this.prevAttackCounter = this.attackCounter;
			double d0 = this.waypointX - this.posX;
			double d1 = this.waypointY - this.posY;
			double d2 = this.waypointZ - this.posZ;
			double d3 = d0 * d0 + d1 * d1 + d2 * d2;

			if (d3 < 1.0D || d3 > 3600.0D) {
				if (this.targetedEntity != null) {
					this.waypointX = targetedEntity.posX + (this.rand.nextFloat() * 2.0F - 1.0F) * 16.0F;
					this.waypointZ = targetedEntity.posZ + (this.rand.nextFloat() * 2.0F - 1.0F) * 16.0F;
					this.waypointY = this.world.getHeight((int) waypointX, (int) waypointZ) + 10
							+ rand.nextInt(15);
				} else {
					this.waypointX = this.posX + (this.rand.nextFloat() * 2.0F - 1.0F) * 16.0F;
					this.waypointZ = this.posZ + (this.rand.nextFloat() * 2.0F - 1.0F) * 16.0F;
					this.waypointY = this.world.getHeight((int) waypointX, (int) waypointZ) + 10
							+ rand.nextInt(15);
				}
			}

			if (this.courseChangeCooldown-- <= 0) {
				this.courseChangeCooldown += this.rand.nextInt(5) + 2;
				d3 = MathHelper.sqrt(d3);

				if (this.isCourseTraversable(this.waypointX, this.waypointY, this.waypointZ, d3)) {
					this.motionX += d0 / d3 * 0.1D;
					this.motionY += d1 / d3 * 0.1D;
					this.motionZ += d2 / d3 * 0.1D;
				} else {
					this.waypointX = this.posX + (this.rand.nextFloat() * 2.0F - 1.0F) * 16.0F;
					this.waypointZ = this.posZ + (this.rand.nextFloat() * 2.0F - 1.0F) * 16.0F;
					this.waypointY = this.world.getHeight((int) waypointX, (int) waypointZ) + 10
							+ rand.nextInt(15);
				}
			}

			if (this.targetedEntity != null && this.targetedEntity.isDead) {
				this.targetedEntity = null;
			}

			if (this.targetedEntity == null || this.attackCounter <= 0) {
				// this.targetedEntity =
				// this.world.getClosestVulnerablePlayerToEntity(this,
				// 100.0D);
				this.targetedEntity = Library.getClosestEntityForChopper(world, this.posX, this.posY, this.posZ, 250);

				if (this.targetedEntity != null) {
					//this.aggroCooldown = 20;
				}
			}

			double d4 = 64.0D;

			if (this.targetedEntity != null && this.targetedEntity.getDistanceSq(this) < d4 * d4) {
				double d8 = 2.0D;
				Vec3d vec3 = this.getLook(1.0F);
				double xStart = this.posX + vec3.x * d8;
				double yStart = this.posY - 0.5;
				double zStart = this.posZ + vec3.z * d8;
				double d5 = this.targetedEntity.posX - xStart;
				double d6 = this.targetedEntity.getEntityBoundingBox().minY + this.targetedEntity.height / 2.0F
						- yStart;
				double d7 = this.targetedEntity.posZ - zStart;

				++this.attackCounter;
				if (attackCounter >= 200) {
					attackCounter -= 200;
				}

				if (this.attackCounter % 2 == 0 && attackCounter >= 120) {
					world.playSound(null, this.posX, this.posY, this.posZ, HBMSoundHandler.osiprShoot, SoundCategory.HOSTILE, 10.0F, 1.0F);
					// EntityLargeFireball entitylargefireball = new
					// EntityLargeFireball(this.world, this, d5, d6, d7);
					EntityBullet entityarrow = new EntityBullet(this.world, this, 3.0F, 35, 45, false, "chopper", EnumHand.MAIN_HAND);
					Vec3 vec2 = Vec3.createVectorHelper(d5 - 1 + rand.nextInt(3), d6 - 1 + rand.nextInt(3),
							d7 - 1 + rand.nextInt(3)).normalize();
					double motion = 3;
					entityarrow.motionX = vec2.xCoord * motion;
					entityarrow.motionY = vec2.yCoord * motion;
					entityarrow.motionZ = vec2.zCoord * motion;
					// entitylargefireball.field_92057_e =
					// this.explosionStrength;
					entityarrow.setDamage(3 + rand.nextInt(5));
					// entitylargefireball.posX = this.posX + vec3.xCoord * d8;
					// entitylargefireball.posY = this.posY +
					// (double)(this.height /
					// 2.0F) + 0.5D;
					// entitylargefireball.posZ = this.posZ + vec3.zCoord * d8;
					entityarrow.posX = xStart;
					entityarrow.posY = yStart;
					entityarrow.posZ = zStart;
					// this.world.spawnEntityInWorld(entitylargefireball);
					this.world.spawnEntity(entityarrow);
				}
				if (this.attackCounter == 80) {
					world.playSound(null, this.posX, this.posY, this.posZ, HBMSoundHandler.chopperCharge, SoundCategory.HOSTILE, 5.0F, 1.0F);
				}

				this.mineDropCounter++;
				if (mineDropCounter > 100 && rand.nextInt(15) == 0) {
		    		world.playSound(null, this.posX, this.posY, this.posZ, HBMSoundHandler.chopperDrop, SoundCategory.HOSTILE, 15.0F, 1.0F);
					EntityChopperMine mine = new EntityChopperMine(world, this.posX, this.posY - 0.5, this.posZ, 0, -0.3, 0, this);
					this.mineDropCounter = 0;
					this.world.spawnEntity(mine);
					
					if(rand.nextInt(3) == 0)
					{
						EntityChopperMine mine1 = new EntityChopperMine(world, this.posX, this.posY - 0.5, this.posZ, 1, -0.3, 0, this);
						EntityChopperMine mine2 = new EntityChopperMine(world, this.posX, this.posY - 0.5, this.posZ, 0, -0.3, 1, this);
						EntityChopperMine mine3 = new EntityChopperMine(world, this.posX, this.posY - 0.5, this.posZ, -1, -0.3, 0, this);
						EntityChopperMine mine4 = new EntityChopperMine(world, this.posX, this.posY - 0.5, this.posZ, 0, -0.3, -1, this);
						this.world.spawnEntity(mine1);
						this.world.spawnEntity(mine2);
						this.world.spawnEntity(mine3);
						this.world.spawnEntity(mine4);
					}
				}

			} else {

				if (this.attackCounter > 0) {
					this.attackCounter = 0;
				}
			}

			//Drillgon200: I don't think this is needed? Not sure though.
			/*if (!this.world.isRemote) {
				byte b1 = this.dataWatcher.getWatchableObjectByte(16);
				byte b0 = (byte) (this.attackCounter > 10 ? 1 : 0);

				if (b1 != b0) {
					this.dataWatcher.updateObject(16, Byte.valueOf(b0));
				}
			}*/
		} else {
			motionY -= 0.08;
			if(Math.sqrt(Math.pow(this.motionX, 2) + Math.pow(this.motionZ, 2)) * 1.2 < 1.8)
			{
				this.motionX *= 1.2;
				this.motionZ *= 1.2;
			}
			
			if(rand.nextInt(20) == 0)
			{
		    	this.world.createExplosion(this, this.posX, this.posY, this.posZ, 5F, true);
			}

			if(!world.isRemote) {

				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "exhaust");
				data.setString("mode", "meteor");
				data.setInteger("count", 10);
				data.setDouble("width", 1);
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, posX, posY, posZ),  new NetworkRegistry.TargetPoint(dimension, posX, posY, posZ, 100));
			}
			
			rotationYaw += 20;
			
			if(this.onGround)
			{
		    	this.world.createExplosion(this, this.posX, this.posY, this.posZ, 15F, true);
		    	this.dropItems();
				this.setDead();
			}
			if (this.ticksExisted % 2 == 0)
				this.world.playSound(null, this.posX, this.posY, this.posZ, HBMSoundHandler.nullCrashing, SoundCategory.HOSTILE, 10.0F, 0.5F);
		}
		if (this.targetedEntity == null) {
			float f3 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			if (this.rotationYaw - (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI) >= 10)
				this.prevRotationYaw = this.rotationYaw -= 10;
			if (this.rotationYaw - (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI) <= -10)
				this.prevRotationYaw = this.rotationYaw += 10;
			if (this.rotationYaw - (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI) < 10 && this.rotationYaw - (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI) > 10)
				this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
			this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(this.motionY, f3) * 180.0D / Math.PI);
		} else {
			
			float f3 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			if (this.rotationYaw - (float) (Math.atan2(this.posX - targetedEntity.posX, this.posZ - targetedEntity.posZ) * 180.0D / Math.PI) >= 10)
				this.prevRotationYaw = this.rotationYaw -= 10;
			if (this.rotationYaw - (float) (Math.atan2(this.posX - targetedEntity.posX, this.posZ - targetedEntity.posZ) * 180.0D / Math.PI) <= -10)
				this.prevRotationYaw = this.rotationYaw += 10;
			if (this.rotationYaw - (float) (Math.atan2(this.posX - targetedEntity.posX, this.posZ - targetedEntity.posZ) * 180.0D / Math.PI) < 10 && this.rotationYaw - (float) (Math.atan2(this.posX - targetedEntity.posX, this.posZ - targetedEntity.posZ) * 180.0D / Math.PI) > 10)
				this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(this.posX - targetedEntity.posX, this.posZ - targetedEntity.posZ) * 180.0D / Math.PI);
			
			this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(this.motionY, f3) * 180.0D / Math.PI);
			
			//double d8 = 2.0D;
			//Vec3d vec3 = this.getLook(1.0F);
			//double xStart = this.posX + vec3.x * d8;
			//double yStart = this.posY - 0.5;
			//double zStart = this.posZ + vec3.z * d8;
			//double d5 = this.targetedEntity.posX - xStart;
			//double d6 = this.targetedEntity.getEntityBoundingBox().minY + this.targetedEntity.height / 2.0F - yStart;
			//double d7 = this.targetedEntity.posZ - zStart;
		}

		if(rotationPitch <= 330 && rotationPitch >= 30)
		{
			if(rotationPitch < 180)
				rotationPitch = 30;
			if(rotationPitch >= 180)
				rotationPitch = 330;
		}
		this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
	}
	
	private boolean isCourseTraversable(double p_70790_1_, double p_70790_3_, double p_70790_5_, double p_70790_7_) {
		double d4 = (this.waypointX - this.posX) / p_70790_7_;
		double d5 = (this.waypointY - this.posY) / p_70790_7_;
		double d6 = (this.waypointZ - this.posZ) / p_70790_7_;
		AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();

		for (int i = 1; i < p_70790_7_; ++i) {
			axisalignedbb.offset(d4, d5, d6);

			if (!this.world.getCollisionBoxes(this, axisalignedbb).isEmpty()) {
				return false;
			}
		}

		return true;
	}
	
	@Override
	public void addTrackingPlayer(EntityPlayerMP player) {
		super.addTrackingPlayer(player);
		bossInfo.addPlayer(player);
	}
	
	@Override
	public void removeTrackingPlayer(EntityPlayerMP player) {
		super.removeTrackingPlayer(player);
		bossInfo.removePlayer(player);
	}
	
	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return null;
	}
	
	@Override
	protected SoundEvent getDeathSound() {
		return null;
	}
	
	protected void dropItems() {

		if(rand.nextInt(2) == 0)
			this.dropItem(ModItems.chopper_head, 1);
		if(rand.nextInt(2) == 0)
			this.dropItem(ModItems.chopper_torso, 1);
		if(rand.nextInt(2) == 0)
			this.dropItem(ModItems.chopper_wing, 1);
		if(rand.nextInt(3) == 0)
			this.dropItem(ModItems.chopper_tail, 1);
		if(rand.nextInt(3) == 0)
			this.dropItem(ModItems.chopper_gun, 1);
		if(rand.nextInt(3) == 0)
			this.dropItem(ModItems.chopper_blades, 1);

		this.dropItem(ModItems.combine_scrap, rand.nextInt(8) + 1);
		this.dropItem(ModItems.plate_combine_steel, rand.nextInt(5) + 1);
		this.entityDropItem(new ItemStack(ModItems.wire_fine, 1, Mats.MAT_MAGTUNG.id), rand.nextInt(3) + 1);
	}
	
	@Override
	protected float getSoundVolume() {
		return 10.0F;
	}
	
	@Override
	public boolean getCanSpawnHere() {
		return this.rand.nextInt(20) == 0 && super.getCanSpawnHere()
				&& this.world.getDifficulty() != EnumDifficulty.PEACEFUL;
	}
	
	@Override
	public int getMaxSpawnedInChunk() {
		return 1;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return distance < 25000;
	}
	
	public void initDeath() {
    	this.world.createExplosion(this, this.posX, this.posY, this.posZ, 10F, true);
    	if(!this.isDying)
    		world.playSound(null, this.posX, this.posY, this.posZ, HBMSoundHandler.chopperDamage, SoundCategory.HOSTILE, 10.0F, 1.0F);
    	isDying = true;
    }
    
    public void dropDamageItem() {
    	int i = rand.nextInt(10);

    	if(i < 6)
			this.dropItem(ModItems.combine_scrap, 1);
    	else if(i > 7)
			this.dropItem(ModItems.plate_combine_steel, 1);
    	else
			this.entityDropItem(new ItemStack(ModItems.wire_fine, 1, Mats.MAT_MAGTUNG.id), 1);
    }

	public void setIsDying(boolean b) {
		this.getDataManager().set(DYING, b);
	}

	public boolean getIsDying() {
		return this.getDataManager().get(DYING);
	}
	
}
