package com.hbm.entity.projectile;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.CompatibilityConfig;
import com.hbm.explosion.ExplosionNT;
import com.hbm.explosion.ExplosionNT.ExAttrib;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ModDamageSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;

@AutoRegister(name = "entity_shrapnel", trackingRange = 1000)
public class EntityShrapnel extends EntityThrowable {

	public static final DataParameter<Byte> TRAIL = EntityDataManager.createKey(EntityShrapnel.class, DataSerializers.BYTE);

	public EntityShrapnel(World world) {
		super(world);
	}

	public EntityShrapnel(World world, EntityLivingBase thrower) {
		super(world, thrower);
	}

	public EntityShrapnel(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	@Override
	public void entityInit() {
		this.dataManager.register(TRAIL, (byte) 0);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (world.isRemote) {
			spawnFlameParticle();
		}
	}

	private void spawnFlameParticle() {
		world.spawnParticle(EnumParticleTypes.FLAME, posX, posY, posZ, 0.0, 0.0, 0.0);
	}

	@Override
	protected void onImpact(RayTraceResult mop) {
		if (!CompatibilityConfig.isWarDim(world)) {
			setDead();
			return;
		}

		handleEntityImpact(mop);
		handleBlockImpact(mop);
	}

	private void handleEntityImpact(RayTraceResult mop) {
		if (mop.entityHit != null) {
			mop.entityHit.attackEntityFrom(ModDamageSource.shrapnel, 15);
		}
	}

	private void handleBlockImpact(RayTraceResult mop) {
		if (ticksExisted <= 5) return;

		setDead();
		byte trailType = this.dataManager.get(TRAIL);

		if (trailType == 2) {
			processVolcanoImpact(mop);
		} else if (trailType == 3) {
			placeMudBlock(mop);
		} else {
			spawnLavaParticles();
		}

		playExtinguishSound();
	}

	private void processVolcanoImpact(RayTraceResult mop) {
		if (world.isRemote || mop.typeOfHit != Type.BLOCK || mop.getBlockPos() == null) return;

		BlockPos impactPos = mop.getBlockPos().up();

		if (motionY < -0.2D && world.getBlockState(impactPos).getBlock().isReplaceable(world, impactPos)) {
			world.setBlockState(impactPos, ModBlocks.volcanic_lava_block.getDefaultState());
			spreadGas(mop);
		}

		if (motionY > 0) {
			triggerExplosion(mop);
		}
	}

	private void spreadGas(RayTraceResult mop) {
		BlockPos origin = mop.getBlockPos();

		for (int x = origin.getX() - 1; x <= origin.getX() + 1; x++) {
			for (int y = origin.getY(); y <= origin.getY() + 2; y++) {
				for (int z = origin.getZ() - 1; z <= origin.getZ() + 1; z++) {
					BlockPos pos = new BlockPos(x, y, z);
					if (world.getBlockState(pos).getBlock() == Blocks.AIR) {
						world.setBlockState(pos, ModBlocks.gas_monoxide.getDefaultState());
					}
				}
			}
		}
	}

	private void triggerExplosion(RayTraceResult mop) {
		ExplosionNT explosion = new ExplosionNT(world, null, mop.getBlockPos().getX() + 0.5, mop.getBlockPos().getY() + 0.5, mop.getBlockPos().getZ() + 0.5, 7);
		explosion.addAttrib(ExAttrib.NODROP);
		explosion.addAttrib(ExAttrib.LAVA_V);
		explosion.addAttrib(ExAttrib.NOSOUND);
		explosion.addAttrib(ExAttrib.ALLMOD);
		explosion.addAttrib(ExAttrib.NOHURT);
		explosion.explode();
	}

	private void placeMudBlock(RayTraceResult mop) {
        if (world.isRemote || mop.typeOfHit != Type.BLOCK || mop.getBlockPos() == null) return;
		BlockPos targetPos = mop.getBlockPos().up();
		if (world.getBlockState(targetPos).getBlock().isReplaceable(world, targetPos)) {
			world.setBlockState(targetPos, ModBlocks.mud_block.getDefaultState());
		}
	}

	private void spawnLavaParticles() {
		for (int i = 0; i < 5; i++) {
			world.spawnParticle(EnumParticleTypes.LAVA, posX, posY, posZ, 0.0, 0.0, 0.0);
		}
	}

	private void playExtinguishSound() {
		world.playSound(null, posX, posY, posZ, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.HOSTILE, 1.0F, 1.0F);
	}

	public void setTrail(boolean enabled) {
		this.dataManager.set(TRAIL, (byte) (enabled ? 1 : 0));
	}

	public void setVolcano(boolean enabled) {
		this.dataManager.set(TRAIL, (byte) (enabled ? 2 : 0));
	}

	public void setWatz(boolean enabled) {
		this.dataManager.set(TRAIL, (byte) (enabled ? 3 : 0));
	}
}

