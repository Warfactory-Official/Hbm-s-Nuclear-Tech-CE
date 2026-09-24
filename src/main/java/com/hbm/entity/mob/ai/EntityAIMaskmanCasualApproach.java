package com.hbm.entity.mob.ai;

import com.hbm.util.Vec3NT;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityAIMaskmanCasualApproach extends EntityAIBase {

	World worldObj;
	EntityCreature attacker;
	int attackTick;
	double speedTowardsTarget;
	boolean longMemory;
	Path entityPathEntity;
	Class classTarget;
	private int pathTimer;
	private double lastX;
	private double lastY;
	private double lastZ;

	private int failedPathFindingPenalty;
	
	public EntityAIMaskmanCasualApproach(EntityCreature owner, Class target, double speed, boolean longMemory) {
		this(owner, speed, longMemory);
		this.classTarget = target;
	}

	public EntityAIMaskmanCasualApproach(EntityCreature owner, double speed, boolean longMemory) {
		this.attacker = owner;
		this.worldObj = owner.world;
		this.speedTowardsTarget = speed;
		this.longMemory = longMemory;
		this.setMutexBits(3);
	}
	
	@Override
	public boolean shouldExecute() {
		EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

		if(entitylivingbase == null) {
			return false;

		} else if(!entitylivingbase.isEntityAlive()) {
			return false;

		} else if(this.classTarget != null && !this.classTarget.isAssignableFrom(entitylivingbase.getClass())) {
			return false;

		} else {

			if(--this.pathTimer <= 0) {

				double[] pos = getApproachPos();
				this.entityPathEntity = this.attacker.getNavigator().getPathToXYZ(pos[0], pos[1], pos[2]);
				this.pathTimer = 4 + this.attacker.getRNG().nextInt(7);
				return this.entityPathEntity != null;

			} else {
				return true;
			}
		}
	}
	
	@Override
	public boolean shouldContinueExecuting() {
		EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

		return entitylivingbase == null ? false
				: (!entitylivingbase.isEntityAlive() ? false
				: (!this.longMemory ? !this.attacker.getNavigator().noPath()
				: this.attacker.isWithinHomeDistanceFromPosition(new BlockPos(MathHelper.floor(entitylivingbase.posX), MathHelper.floor(entitylivingbase.posY), MathHelper.floor(entitylivingbase.posZ)))));
	}
	
	@Override
	public void startExecuting() {
		this.attacker.getNavigator().setPath(this.entityPathEntity, this.speedTowardsTarget);
		this.pathTimer = 0;
	}
	
	@Override
	public void resetTask() {
		this.attacker.getNavigator().clearPath();
	}
	
	@Override
	public void updateTask() {
		EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();
		this.attacker.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);
		double d0 = this.attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
		double d1 = (double) (this.attacker.width * 2.0F * this.attacker.width * 2.0F + entitylivingbase.width);

		this.pathTimer--;

		if((this.longMemory || this.attacker.getEntitySenses().canSee(entitylivingbase)) && this.pathTimer <= 0
				&& (this.lastX == 0.0D && this.lastY == 0.0D && this.lastZ == 0.0D
				|| entitylivingbase.getDistanceSq(this.lastX, this.lastY, this.lastZ) >= 1.0D
				|| this.attacker.getRNG().nextFloat() < 0.05F)) {

			this.lastX = entitylivingbase.posX;
			this.lastY = entitylivingbase.getEntityBoundingBox().minY;
			this.lastZ = entitylivingbase.posZ;
			this.pathTimer = failedPathFindingPenalty + 4 + this.attacker.getRNG().nextInt(7);

			if(this.attacker.getNavigator().getPath() != null) {

				PathPoint finalPathPoint = this.attacker.getNavigator().getPath().getFinalPathPoint();
				if(finalPathPoint != null && entitylivingbase.getDistanceSq(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z) < 1) {
					failedPathFindingPenalty = 0;

				} else {
					failedPathFindingPenalty += 10;
				}

			} else {
				failedPathFindingPenalty += 10;
			}

			if(d0 > 1024.0D) {
				this.pathTimer += 10;
			} else if(d0 > 256.0D) {
				this.pathTimer += 5;
			}

			double[] pos = getApproachPos();
			if(!this.attacker.getNavigator().tryMoveToXYZ(pos[0], pos[1], pos[2], speedTowardsTarget)) {
				this.pathTimer += 15;
			}
		}

		this.attackTick = Math.max(this.attackTick - 1, 0);

		/*if(d0 <= d1 && this.attackTick <= 20) {
			this.attackTick = 20;
			if(this.attacker.getHeldItem() != null) {
				this.attacker.swingItem();
			}
			this.attacker.attackEntityAsMob(entitylivingbase);
		}*/
	}
	
	public double[] getApproachPos() {

		EntityLivingBase target = this.attacker.getAttackTarget();

		Vec3NT vec = Vec3NT.createVectorHelper(this.attacker.posX - target.posX, this.attacker.posY - target.posY, this.attacker.posZ - target.posZ);

		double range = Math.min(vec.length(), 20) - 10;

		vec = vec.normalize();

    	double x = this.attacker.posX + vec.x * range + this.attacker.getRNG().nextGaussian() * 2;
    	double y = this.attacker.posY + vec.y - 5 + this.attacker.getRNG().nextInt(11);
    	double z = this.attacker.posZ + vec.z * range + this.attacker.getRNG().nextGaussian() * 2;

    	return new double[] {x, y, z};
	}

}
