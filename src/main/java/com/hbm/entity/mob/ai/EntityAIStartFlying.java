package com.hbm.entity.mob.ai;

import com.hbm.entity.mob.IFlyingCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIStartFlying extends EntityAIBase {

    private final EntityLivingBase living;
    private final IFlyingCreature flying;

    public EntityAIStartFlying(EntityLivingBase living, IFlyingCreature flying) {
        this.living = living;
        this.flying = flying;
    }

    @Override
    public boolean shouldExecute() {
        return this.flying.getFlyingState() == IFlyingCreature.STATE_WALKING && (this.living.getRevengeTarget() != null || this.living.isBurning() || this.living.getRNG().nextInt(600) == 0);
    }

    @Override
    public void startExecuting() {
        this.flying.setFlyingState(IFlyingCreature.STATE_FLYING);
    }
}
