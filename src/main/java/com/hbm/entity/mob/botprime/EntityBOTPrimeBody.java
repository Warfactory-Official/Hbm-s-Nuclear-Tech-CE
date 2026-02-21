package com.hbm.entity.mob.botprime;

import com.hbm.entity.mob.ai.EntityAINearestAttackableTargetNT;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
@AutoRegister(name = "entity_balls_o_tron_seg", trackingRange = 1000)
public class EntityBOTPrimeBody extends EntityBOTPrimeBase {

	public static final DataParameter<Boolean> SHIELD = EntityDataManager.createKey(EntityBOTPrimeBody.class, DataSerializers.BOOLEAN);
	
	private WormMovementBodyNT movement = new WormMovementBodyNT(this);

	public EntityBOTPrimeBody(World world) {
		super(world);
		this.bodySpeed = 0.6D;
		this.rangeForParts = 70.0D;
		this.segmentDistance = 3.5D;
	    this.maxBodySpeed = 1.4D;
	    this.targetTasks.addTask(1, new EntityAINearestAttackableTargetNT(this, EntityPlayer.class, 0, false, false, this.selector, 128.0D));
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(SHIELD, false);
	}

	@Override
	public float getAttackStrength(Entity target) {
		if(target instanceof EntityLivingBase) {
			return ((EntityLivingBase) target).getHealth() * 0.75F;
		}

		return 100;
	}
	
	@Override
	public boolean isPotionApplicable(PotionEffect potioneffectIn) {
		return false;
	}
	
	@Override
	protected void updateAITasks() {
		this.movement.updateMovement();
		this.targetTasks.onUpdateTasks();

		if(this.didCheck) {
			if(this.targetedEntity == null || !this.targetedEntity.isEntityAlive()) {
				setHealth(getHealth() - 1999.0F);
			}
			if(((this.followed == null) || (!this.followed.isEntityAlive())) && (this.rand.nextInt(60) == 0)) {
				this.world.createExplosion(this, this.posX, this.posY, this.posZ, 2.0F, false);
			}
		}
		if(this.followed != null && this.followed.isEntityAlive() && getAttackTarget() != null) {
			if(canEntityBeSeen(getAttackTarget())) {
				this.attackCounter += 1;
				if(this.attackCounter == 10) {
					laserAttack(this.getAttackTarget(), false);

					this.attackCounter = -20;
				}
			} else if(this.attackCounter > 0) {
				this.attackCounter -= 1;
			}
		} else if(this.attackCounter > 0) {
			this.attackCounter -= 1;
		}

		if(this.targetedEntity != null) {
			double dx = targetedEntity.posX - posX;
			double dy = targetedEntity.posY - posY;
			double dz = targetedEntity.posZ - posZ;
	        float f3 = MathHelper.sqrt(dx * dx + dz * dz);
	        this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(dx, dz) * 180.0D / Math.PI);
	        this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(dy, f3) * 180.0D / Math.PI);
		}
	}

	@Override
	public void onUpdate() {

		super.onUpdate();

		if(this.targetedEntity != null) {
			double dx = targetedEntity.posX - posX;
			double dy = targetedEntity.posY - posY;
			double dz = targetedEntity.posZ - posZ;
			float f3 = MathHelper.sqrt(dx * dx + dz * dz);
			this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(dx, dz) * 180.0D / Math.PI);
			this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(dy, f3) * 180.0D / Math.PI);
		}
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setInteger("partID", this.getPartNumber());
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		setPartNumber(compound.getInteger("partID"));
	}
}
