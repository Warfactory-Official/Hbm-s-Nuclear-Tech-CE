package com.hbm.entity.mob.ai;

import com.hbm.util.Vec3NT;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EntityAI_MLPF extends EntityAIBase {

	private Class targetClass;
    private EntityLivingBase target;
	private EntityLiving mover;
    private final Sorter theNearestAttackableTargetSorter;
	private int range;
	private int distance;
	private static final int vertical = 10;
    private double speed;

	public EntityAI_MLPF(EntityLiving entity, Class targetClass, int range, double speed, int distance)
	{
		this.mover = entity;
		this.targetClass = targetClass;
		this.range = range;
		this.speed = speed;
		this.distance = distance;
        this.theNearestAttackableTargetSorter = new Sorter(entity);
	}

	@Override
	public boolean shouldExecute() {

		//roll the dice for targetiing if there's nothing to track
		if(mover.getRNG().nextInt(100) < 5 && mover.getAttackTarget() == null) {
			//System.out.println("Randomizer fired!");
			//load potential targets
			calculateTarget();

			//start if there is a valid target
			return target != null;
		}

		return false;
	}

    public void startExecuting() {

    	//create a path line from mover to target
    	Vec3NT vec = Vec3NT.createVectorHelper(
    			target.posX - mover.posX,
    			target.posY - mover.posY,
    			target.posZ - mover.posZ);


    	//line length is capped so the pathfinder can manage it
    	int range = distance;
    	
    	if(vec.length() < 16)
    		mover.setAttackTarget(target);

    	vec = vec.normalize();
    	vec.setX(vec.x * (range));
    	vec.setY(vec.y * (range));
    	vec.setZ(vec.z * (range));

    	//target positions are set (with randomized Y-offset)
    	double x = mover.posX + vec.x;
    	double y = mover.posY + vec.y - 5 + mover.getRNG().nextInt(11);
    	double z = mover.posZ + vec.z;

    	//System.out.println("Routing to " + x + "/" + y + "/" + z);

		//this is where the magic happens
    	this.mover.getNavigator().tryMoveToXYZ(x, y, z, this.speed);

        //System.out.println("Start successful? " + success);
    }

	@Override
	public boolean shouldContinueExecuting() {

		//only continue if the path is valid
        return !this.mover.getNavigator().noPath();
	}

	@Override
	public void resetTask()
	{
		//once the task is complete, remove target
		target = null;
	}

	//scans the area and determines a new target entity
	private void calculateTarget() {

		List list = mover.world.getEntitiesWithinAABB(targetClass, new AxisAlignedBB(
				mover.posX - range,
				mover.posY - vertical,
				mover.posZ - range,
				mover.posX + range,
				mover.posY + vertical,
				mover.posZ + range));

		Collections.sort(list, theNearestAttackableTargetSorter);

		if (!list.isEmpty())
        {
			target = (EntityLivingBase)list.get(0);
        }
	}

	public static class Sorter implements Comparator {
		private final Entity theEntity;

		public Sorter(Entity p_i1662_1_) {
			this.theEntity = p_i1662_1_;
		}

		public int compare(Entity p_compare_1_, Entity p_compare_2_) {
			double d0 = this.theEntity.getDistanceSq(p_compare_1_);
			double d1 = this.theEntity.getDistanceSq(p_compare_2_);
			return d0 < d1 ? -1 : (d0 > d1 ? 1 : 0);
		}

		public int compare(Object p_compare_1_, Object p_compare_2_) {
			return this.compare((Entity) p_compare_1_, (Entity) p_compare_2_);
		}
	}
}