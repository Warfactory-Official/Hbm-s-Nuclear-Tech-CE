package com.hbm.entity.mob;

import com.hbm.interfaces.AutoRegister;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AutoRegister(name = "entity_ntm_ghost", trackingRange = 1000)
public class EntityGhost extends EntityCreature {

	public EntityGhost(World world) {
		super(world);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(2, new EntityAILookIdle(this));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(!world.isRemote) {
			double despawnRange = 50;
			List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow(despawnRange, despawnRange, despawnRange));
			if(!players.isEmpty()) this.setDead();
		}
	}

	@Override
	public void setHealth(float health) {
		super.setHealth(this.getMaxHealth());
	}

	@Override
	public boolean isEntityInvulnerable(@NotNull DamageSource source) {
		return true;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return super.getRenderBoundingBox().grow(10);
	}
}
