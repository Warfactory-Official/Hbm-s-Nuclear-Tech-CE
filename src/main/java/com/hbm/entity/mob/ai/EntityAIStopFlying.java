package com.hbm.entity.mob.ai;

import com.hbm.entity.mob.IFlyingCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIStopFlying extends EntityAIBase {

    private final EntityLivingBase living;
    private final IFlyingCreature flying;

    public EntityAIStopFlying(EntityLivingBase living, IFlyingCreature flying) {
        this.living = living;
        this.flying = flying;
    }

    @Override
    public boolean shouldExecute() {
        return this.flying.getFlyingState() == IFlyingCreature.STATE_FLYING && this.living.getRNG().nextInt(200) == 0;
    }

    @Override
    public void startExecuting() {
        this.flying.setFlyingState(IFlyingCreature.STATE_WALKING);
    }
}
