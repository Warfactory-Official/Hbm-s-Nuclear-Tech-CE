package com.hbm.entity.mob.ai;

import com.hbm.entity.mob.EntityPigeon;
import com.hbm.entity.mob.IFlyingCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;

import java.util.List;

public class EntityAIEatBread extends EntityAIBase {

    private final EntityPigeon pigeon;
    private final double speed;
    private EntityItem item;

    public EntityAIEatBread(EntityPigeon pigeon, double speed) {
        this.pigeon = pigeon;
        this.speed = speed;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (this.pigeon.isFat() || this.pigeon.getFlyingState() != IFlyingCreature.STATE_WALKING) {
            return false;
        }

        List<EntityItem> items = this.pigeon.world.getEntitiesWithinAABB(EntityItem.class, this.pigeon.getEntityBoundingBox().grow(10.0D, 10.0D, 10.0D));

        for (EntityItem entityItem : items) {
            ItemStack stack = entityItem.getItem();
            if (!stack.isEmpty() && stack.getItem() == Items.BREAD) {
                this.item = entityItem;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.item != null && this.item.isEntityAlive() && this.shouldExecute();
    }

    @Override
    public void updateTask() {
        if (this.item == null) {
            return;
        }

        this.pigeon.getLookHelper().setLookPositionWithEntity(this.item, 30.0F, this.pigeon.getVerticalFaceSpeed());

        if (this.pigeon.getDistance(this.item) > 1.0D) {
            this.pigeon.getNavigator().tryMoveToEntityLiving(this.item, this.speed);
        } else {
            if (this.pigeon.getRNG().nextInt(3) == 0) {
                ItemStack stack = this.item.getItem();

                if (stack.getCount() > 1) {
                    stack.shrink(1);
                    EntityItem newItem = new EntityItem(this.pigeon.world, this.item.posX, this.item.posY, this.item.posZ, stack.copy());
                    this.pigeon.world.spawnEntity(newItem);
                }

                this.item.setDead();
                this.item = null;
            }

            this.pigeon.setFat(true);
            this.pigeon.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.5F + 0.5F * this.pigeon.getRNG().nextInt(2), (this.pigeon.getRNG().nextFloat() - this.pigeon.getRNG().nextFloat()) * 0.2F + 1.0F);
        }
    }
}
