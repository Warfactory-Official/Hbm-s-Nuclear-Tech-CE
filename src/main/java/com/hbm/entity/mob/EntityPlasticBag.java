package com.hbm.entity.mob;

import com.hbm.entity.item.EntityItemBuoyant;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@AutoRegister(name = "entity_plastic_bag", trackingRange = 64)
public class EntityPlasticBag extends EntityWaterMob {

	public float rotation;
	public float prevRotation;
	private float randomMotionSpeed;
	private float rotationVelocity;
	private float randomMotionVecX;
	private float randomMotionVecY;
	private float randomMotionVecZ;

	public EntityPlasticBag(World world) {
		super(world);
		this.setSize(0.45F, 0.45F);
		this.rotationVelocity = 1.0F / (this.rand.nextFloat() + 1.0F) * 0.2F;
	}

	@Override
	public boolean attackEntityFrom(@NotNull DamageSource source, float amount) {

		if(!world.isRemote) {
			this.setDead();
			this.dropItem(ModItems.plastic_bag, 1);
		}

		return true;
	}

	@Override
	public EntityItem entityDropItem(@NotNull ItemStack stack, float offset) {
		if(stack.isEmpty()) return null;

		EntityItemBuoyant entityitem = new EntityItemBuoyant(this.world, this.posX, this.posY + offset, this.posZ, stack);
		entityitem.setPickupDelay(10);

		if(captureDrops) {
			capturedDrops.add(entityitem);
		} else {
			this.world.spawnEntity(entityitem);
		}
		return entityitem;
	}

	@Override protected @Nullable SoundEvent getAmbientSound() { return null; }
	@Override protected @Nullable SoundEvent getHurtSound(@NotNull DamageSource source) { return null; }
	@Override protected @Nullable SoundEvent getDeathSound() { return null; }

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	public boolean isInWater() {
		return this.world.handleMaterialAcceleration(this.getEntityBoundingBox().grow(0.0D, -0.6D, 0.0D), Material.WATER, this);
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		this.prevRotation = this.rotation;
		this.rotation += this.rotationVelocity;

		if(this.rotation > ((float) Math.PI * 2F)) {
			this.rotation -= ((float) Math.PI * 2F);

			if(this.rand.nextInt(10) == 0) {
				this.rotationVelocity = 1.0F / (this.rand.nextFloat() + 1.0F) * 0.2F;
			}
		}

		if(this.isInWater()) {
			float f;

			if(this.rotation < (float) Math.PI) {
				f = this.rotation / (float) Math.PI;

				if(f > 0.75D) {
					this.randomMotionSpeed = 0.1F;
				}
			} else {
				this.randomMotionSpeed *= 0.999F;
			}

			if(!this.world.isRemote) {
				this.motionX = this.randomMotionVecX * this.randomMotionSpeed;
				this.motionY = this.randomMotionVecY * this.randomMotionSpeed;
				this.motionZ = this.randomMotionVecZ * this.randomMotionSpeed;
			}

			f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.renderYawOffset += (-((float) MathHelper.atan2(this.motionX, this.motionZ)) * 180.0F / (float) Math.PI - this.renderYawOffset) * 0.1F;
			this.rotationYaw = this.renderYawOffset;
			this.rotationPitch = (float) (MathHelper.atan2(this.motionY, f) * 180.0D / Math.PI);
		} else {
			if(!this.world.isRemote) {
				this.motionX = 0.0D;
				this.motionY -= 0.08D;
				this.motionY *= 0.98D;
				this.motionZ = 0.0D;
			}
		}
	}

	@Override
	public void travel(float strafe, float vertical, float forward) {
		this.move(net.minecraft.entity.MoverType.SELF, this.motionX, this.motionY, this.motionZ);
	}

	@Override
	protected void updateAITasks() {
		++this.idleTime;

		if(this.idleTime > 100) {
			this.randomMotionVecX = this.randomMotionVecY = this.randomMotionVecZ = 0.0F;
		} else if(this.rand.nextInt(50) == 0 || !this.inWater || this.randomMotionVecX == 0.0F && this.randomMotionVecY == 0.0F && this.randomMotionVecZ == 0.0F) {
			float f = this.rand.nextFloat() * (float) Math.PI * 2.0F;
			this.randomMotionVecX = MathHelper.cos(f) * 0.2F;
			this.randomMotionVecY = -0.1F + this.rand.nextFloat() * 0.2F;
			this.randomMotionVecZ = MathHelper.sin(f) * 0.2F;
		}

		this.despawnEntity();
	}

	@Override
	public boolean getCanSpawnHere() {
		return this.posY > 45.0D && this.posY < 63.0D && this.getRNG().nextInt(10) == 0 && super.getCanSpawnHere();
	}
}
