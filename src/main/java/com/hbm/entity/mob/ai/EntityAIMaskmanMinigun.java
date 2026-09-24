package com.hbm.entity.mob.ai;

import com.hbm.entity.projectile.EntityBulletBase;
import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.util.Vec3NT;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIMaskmanMinigun extends EntityAIBase {

	private EntityCreature owner;
    private EntityLivingBase target;
    int delay;
    int timer;
	
    public EntityAIMaskmanMinigun(EntityCreature owner, boolean checkSight, boolean nearbyOnly, int delay) {
		this.owner = owner;
		this.delay = delay;
		timer = delay;
	}
    
	@Override
	public boolean shouldExecute() {
		EntityLivingBase entity = this.owner.getAttackTarget();

        if(entity == null) {
            return false;

        } else {
            this.target = entity;
            double dist = Vec3NT.createVectorHelper(target.posX - owner.posX, target.posY - owner.posY, target.posZ - owner.posZ).length();
            return dist > 5 && dist < 10;
        }
	}
	
	@Override
	public boolean shouldContinueExecuting() {
		return this.shouldExecute() || !this.owner.getNavigator().noPath();
	}
	
	@Override
	public void updateTask() {
		timer--;

		if(timer <= 0) {
			timer = delay;

			EntityBulletBase bullet = new EntityBulletBase(owner.world, BulletConfigSyncingUtil.MASKMAN_BULLET, owner, target, 1.0F, 0);
			owner.world.spawnEntity(bullet);
			owner.playSound(HBMSoundHandler.calShoot, 1.0F, 1.0F);
		}
		
		this.owner.rotationYaw = this.owner.rotationYawHead;
	}

}
