package com.hbm.entity.mob.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigateGround;

import java.util.function.Predicate;
/**
 * Identical to EntityAISwimming, but with an added conditional lambda for maximum reusability.
 *
 * @author hbm
 */
public class EntityAISwimmingConditional extends EntityAIBase {

    private final EntityLiving living;
    private final Predicate<EntityLivingBase> condition;

    public EntityAISwimmingConditional(EntityLiving living, Predicate<EntityLivingBase> condition) {
        this.living = living;
        this.condition = condition;
        this.setMutexBits(4);
        if (this.living.getNavigator() instanceof PathNavigateGround) {
            ((PathNavigateGround) this.living.getNavigator()).setCanSwim(true);
        }
    }

    @Override
    public boolean shouldExecute() {
        return (this.living.isInWater() || this.living.isInLava()) && this.condition.test(this.living);
    }

    @Override
    public void updateTask() {
        if (this.living.getRNG().nextFloat() < 0.8F) {
            this.living.getJumpHelper().setJumping();
        }
    }
}
