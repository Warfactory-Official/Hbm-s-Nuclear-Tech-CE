package com.hbm.entity.mob.ai;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget.Sorter;
import net.minecraft.entity.ai.EntityAITarget;

import java.util.List;

public class EntityAINearestAttackableTargetNT extends EntityAITarget {
	
	private final Class<? extends Entity> targetClass;
	private final int targetChance;
	private final Sorter theNearestAttackableTargetSorter;
	private final Predicate<Entity> targetEntitySelector;
	private EntityLivingBase targetEntity;
	private final double searchRange;

	public EntityAINearestAttackableTargetNT(EntityCreature owner, Class<? extends Entity> targetClass, int targetChance, boolean shouldCheckSight, boolean nearbyOnly, final Predicate<Entity> selector, double range) {
		super(owner, shouldCheckSight, nearbyOnly);
		this.targetClass = targetClass;
		this.targetChance = targetChance;
		this.theNearestAttackableTargetSorter = new Sorter(owner);
		this.searchRange = range;
		setMutexBits(1);

		this.targetEntitySelector = entity -> (selector == null || selector.apply(entity)) && (entity instanceof EntityLivingBase && EntityAINearestAttackableTargetNT.this.isSuitableTarget((EntityLivingBase) entity, false));
	}

	@Override
	protected double getTargetDistance() {
		return this.searchRange;
	}

	@Override
	public boolean shouldExecute() {

		if((this.targetChance > 0) && (this.taskOwner.getRNG().nextInt(this.targetChance) != 0)) {
			return false;
		}
		double range = getTargetDistance();
		List<Entity> targets = this.taskOwner.world.getEntitiesWithinAABB(this.targetClass, this.taskOwner.getEntityBoundingBox().grow(range, range, range), this.targetEntitySelector);
		targets.sort(this.theNearestAttackableTargetSorter);

		if(targets.isEmpty()) {
			return false;
		}
		this.targetEntity = ((EntityLivingBase) targets.get(0));
		return true;
	}

	@Override
	public void resetTask() {
		this.taskOwner.setAttackTarget(targetEntity);
	}
}
