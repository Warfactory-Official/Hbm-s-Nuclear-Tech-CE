package com.hbm.entity.mob.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIWander;

import java.util.function.Predicate;

public class EntityAIConditionalWander<T extends EntityCreature> extends EntityAIWander {
    private final T entity;
    private final Predicate<T> wanderPredicate;

    public EntityAIConditionalWander(T creatureIn, double speedIn, Predicate<T> wanderPredicate) {
        super(creatureIn, speedIn);
        this.wanderPredicate = wanderPredicate;
        this.entity = creatureIn;
    }

    @Override
    public boolean shouldExecute() {
        if (wanderPredicate.test(entity)) {
            return super.shouldExecute();
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (wanderPredicate.test(entity)) {
            return super.shouldContinueExecuting();
        }
        return false;
    }
}
