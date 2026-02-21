package com.hbm.entity.projectile;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockDetonatable;
import com.hbm.blocks.generic.RedBarrel;
import com.hbm.config.BombConfig;
import com.hbm.config.CompatibilityConfig;
import com.hbm.entity.effect.EntityCloudFleijaRainbow;
import com.hbm.entity.effect.EntityEMPBlast;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.handler.ArmorUtil;
import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.handler.BulletConfiguration;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.bullet_hit.EntityHitDataHandler;
import com.hbm.potion.HbmPotion;
import com.hbm.util.BobMathUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
@AutoRegister(name = "entity_bullet_mk2")
public class EntityBulletBase extends Entity implements IProjectile {

	public static final DataParameter<Integer> STYLE = EntityDataManager.createKey(EntityBulletBase.class, DataSerializers.VARINT);
	public static final DataParameter<Integer> TRAIL = EntityDataManager.createKey(EntityBulletBase.class, DataSerializers.VARINT);
	public static final DataParameter<Integer> BULLETCONFIG = EntityDataManager.createKey(EntityBulletBase.class, DataSerializers.VARINT);

	private BulletConfiguration config;
	public EntityLivingBase shooter;
	public float overrideDamage;
	public int overrideMaxAge = -1;

	public BulletConfiguration getConfig() {
		return config;
	}
	
	public EntityBulletBase(World world) {
		super(world);
		this.setSize(0.5F, 0.5F);
	}

	public EntityBulletBase(World world, int config) {
		super(world);
		this.config = BulletConfigSyncingUtil.pullConfig(config);
		this.getDataManager().set(BULLETCONFIG, config);
		if(this.config == null) {
			this.setDead();
			return;
		}
		this.getDataManager().set(STYLE, this.config.style);
		this.getDataManager().set(TRAIL, this.config.trail);
		this.setSize(0.5F, 0.5F);
	}

	public EntityBulletBase(World world, int config, EntityLivingBase entity, EntityLivingBase target, float motion, float deviation) {
		super(world);

		this.config = BulletConfigSyncingUtil.pullConfig(config);
		this.getDataManager().set(BULLETCONFIG, config);
		this.shooter = entity;

		this.setSize(0.5F, 0.5F);

		this.posY = entity.posY + entity.getEyeHeight() - 0.10000000149011612D;
		double d0 = target.posX - entity.posX;
		double d1 = target.getEntityBoundingBox().minY + target.height / 3.0F - this.posY;
		double d2 = target.posZ - entity.posZ;
		double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);

		if (d3 >= 1.0E-7D) {
			float f2 = (float) (Math.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F;
			float f3 = (float) (-(Math.atan2(d1, d3) * 180.0D / Math.PI));
			double d4 = d0 / d3;
			double d5 = d2 / d3;
			this.setLocationAndAngles(entity.posX + d4, this.posY, entity.posZ + d5, f2, f3);
			this.shoot(d0, d1, d2, motion, deviation);
		}

		this.getDataManager().set(STYLE, this.config.style);
		this.getDataManager().set(TRAIL, this.config.trail);
	}
	
	public EntityBulletBase(World world, int config, EntityLivingBase entity, EnumHand hand) {
		super(world);
		this.config = BulletConfigSyncingUtil.pullConfig(config);
		this.getDataManager().set(BULLETCONFIG, config);
		shooter = entity;

		this.setLocationAndAngles(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ, entity.rotationYaw, entity.rotationPitch);
		if (hand == EnumHand.MAIN_HAND) {
			this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
			this.posY -= 0.10000000149011612D;
			this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
		} else {
			this.posX += MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
			this.posY -= 0.10000000149011612D;
			this.posZ += MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
		}
		this.setPosition(this.posX, this.posY, this.posZ);

		this.motionX = -MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionZ = MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionY = (-MathHelper.sin(this.rotationPitch / 180.0F * (float) Math.PI));

		this.setSize(0.5F, 0.5F);

		this.shoot(this.motionX, this.motionY, this.motionZ, 2.0F, this.config.spread);
		
		this.getDataManager().set(STYLE, this.config.style);
		this.getDataManager().set(TRAIL, this.config.trail);
	}

	public EntityBulletBase(World world, int config, EntityLivingBase entity) {
		super(world);
		this.config = BulletConfigSyncingUtil.pullConfig(config);
		this.getDataManager().set(BULLETCONFIG, config);
		shooter = entity;

		this.setLocationAndAngles(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ, entity.rotationYaw, entity.rotationPitch);
		
		this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
		this.posY -= 0.10000000149011612D;
		this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
		this.setPosition(this.posX, this.posY, this.posZ);
		
		this.motionX = -MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionZ = MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionY = (-MathHelper.sin(this.rotationPitch / 180.0F * (float) Math.PI));

		this.setSize(0.5F, 0.5F);

		this.shoot(this.motionX, this.motionY, this.motionZ, 1.0F, this.config.spread);
		
		this.getDataManager().set(STYLE, this.config.style);
		this.getDataManager().set(TRAIL, this.config.trail);
	}
	
	public void overrideStyle(int style){
		this.getDataManager().set(STYLE, style);
	}
	
	@Override
	public void shoot(double moX, double moY, double moZ, float mult1, float mult2) {

		float f2 = MathHelper.sqrt(moX * moX + moY * moY + moZ * moZ);
		moX /= f2;
		moY /= f2;
		moZ /= f2;
		moX += this.rand.nextGaussian() * mult2;
		moY += this.rand.nextGaussian() * mult2;
		moZ += this.rand.nextGaussian() * mult2;
		moX *= mult1;
		moY *= mult1;
		moZ *= mult1;
		this.motionX = moX;
		this.motionY = moY;
		this.motionZ = moZ;

		float f3 = MathHelper.sqrt(moX * moX + moZ * moZ);
		this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(moX, moZ) * 180.0D / Math.PI);
		this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(moY, f3) * 180.0D / Math.PI);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setPositionAndRotationDirect(double x, double y, double z, float r0, float r1, int i, boolean b) {
		this.setPosition(x, y, z);
		this.setRotation(r0, r1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setVelocity(double x, double y, double z) {
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;

		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			float f = MathHelper.sqrt(x * x + z * z);
			this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(x, z) * 180.0D / Math.PI);
			this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(y, f) * 180.0D / Math.PI);
			this.prevRotationPitch = this.rotationPitch;
			this.prevRotationYaw = this.rotationYaw;
			this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
		}
	}

	@Override
	protected void entityInit() {
		this.getDataManager().register(STYLE, 0);
		this.getDataManager().register(TRAIL, 0);
		this.getDataManager().register(BULLETCONFIG, 0);
	}

	@Override
	public void onUpdate() {

		super.onUpdate();

		if (config == null)
			config = BulletConfigSyncingUtil.pullConfig(this.getDataManager().get(BULLETCONFIG));
		
		if(config == null){
			this.setDead();
			return;
		}
		
		if(config.maxAge == 0) {
			this.setDead();
			return;
		}

		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
			this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(this.motionY, (double) f) * 180.0D / Math.PI);
		}

		/// ZONE 1 START ///
		// entity and block collision, plinking

		/// ZONE 2 START ///
		// entity detection
		Vec3d vecOrigin = new Vec3d(this.posX, this.posY, this.posZ);
		Vec3d vecDestination = new Vec3d(this.posX + this.motionX * this.config.velocity, this.posY + this.motionY * this.config.velocity, this.posZ + this.motionZ * this.config.velocity);
		RayTraceResult movement = this.world.rayTraceBlocks(vecOrigin, vecDestination, false, true, false);
		vecOrigin = new Vec3d(this.posX, this.posY, this.posZ);
		vecDestination = new Vec3d(this.posX + this.motionX * this.config.velocity, this.posY + this.motionY * this.config.velocity, this.posZ + this.motionZ * this.config.velocity);

		Entity victim = null;
		RayTraceResult victimResult = null;
		List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow(Math.abs(this.motionX * this.config.velocity), Math.abs(this.motionY * this.config.velocity), Math.abs(this.motionZ * this.config.velocity)).grow(1.0D));

		double d0 = 0.0D;
		int i;
		float f1;

		for (i = 0; i < list.size(); ++i) {
			Entity entity1 = (Entity) list.get(i);

			if (entity1.canBeCollidedWith() && (entity1 != this.shooter)) {
				f1 = 0.3F;
				AxisAlignedBB axisalignedbb1 = entity1.getEntityBoundingBox().grow(f1);
				RayTraceResult RayTraceResult1 = axisalignedbb1.calculateIntercept(vecOrigin, vecDestination);

				if (RayTraceResult1 != null && RayTraceResult1.typeOfHit != Type.MISS) {
					double d1 = vecOrigin.distanceTo(RayTraceResult1.hitVec);
					if (d1 < d0 || d0 == 0.0D) {
						victim = entity1;
						victimResult = RayTraceResult1;
						d0 = d1;
					}
				}
			}
		}

		if (victim != null) {
			movement = victimResult;
			movement.typeOfHit = Type.ENTITY;
			movement.entityHit = victim;
		}

		/// ZONE 2 END ///

		boolean didBounce = false;

		if (movement != null) {

			// handle entity collision
			if (movement.entityHit != null && CompatibilityConfig.isWarDim(world)) {

				DamageSource damagesource = null;

				if (this.shooter == null) {
					damagesource = ModDamageSource.causeBulletDamage(this, this);
				} else {
					damagesource = ModDamageSource.causeBulletDamage(this, shooter);
				}

				if (!world.isRemote) {
					if (!config.doesPenetrate)
						onEntityImpact(victim, movement);
					else
						onEntityHurt(victim, movement, true);
				}

				float damage = rand.nextFloat() * (config.dmgMax - config.dmgMin) + config.dmgMin;
				
				if(overrideDamage != 0)
					damage = overrideDamage;
				try {
					if (!victim.attackEntityFrom(damagesource, damage)) {
						float dmg = damage + ((EntityLivingBase) victim).lastDamage;
						victim.attackEntityFrom(damagesource, dmg);
					}
				} catch (Exception ignored) {}

				// handle block collision
			} else if (world.getBlockState(movement.getBlockPos()).getMaterial() != Material.AIR) {

				boolean hRic = rand.nextInt(100) < config.HBRC;
				boolean doesRic = config.doesRicochet || hRic;

				if (!config.isSpectral && !doesRic)
					this.onBlockImpact(movement.getBlockPos(), movement);

				if (doesRic) {

					Vec3d face = null;

					switch (movement.sideHit) {
					case DOWN:
						face = new Vec3d(0, -1, 0);
						break;
					case UP:
						face = new Vec3d(0, 1, 0);
						break;
					case SOUTH:
						face = new Vec3d(0, 0, 1);
						break;
					case NORTH:
						face = new Vec3d(0, 0, -1);
						break;
					case WEST:
						face = new Vec3d(-1, 0, 0);
						break;
					case EAST:
						face = new Vec3d(1, 0, 0);
						break;
					}

					if (face != null) {

						Vec3d vel = new Vec3d(motionX, motionY, motionZ);
						vel.normalize();

						boolean lRic = rand.nextInt(100) < config.LBRC;
						double angle = Math.abs(BobMathUtil.getCrossAngle(vel, face) - 90);

						if (hRic || (angle <= config.ricochetAngle && lRic)) {
							switch (movement.sideHit.getAxis()) {
							case Y:
								motionY *= -1;
								break;
							case Z:
								motionZ *= -1;
								break;
							case X:
								motionX *= -1;
								break;
							}

							if (config.plink == 1)
								world.playSound(this.posX, this.posY, this.posZ, HBMSoundHandler.ricochet, SoundCategory.HOSTILE, 0.25F, 1.0F, true);
							if (config.plink == 2)
								world.playSound(this.posX, this.posY, this.posZ, HBMSoundHandler.grenadeBounce, SoundCategory.HOSTILE, 1.0F, 1.0F, true);

							onRicochet(movement.getBlockPos());

						} else {
							if (!world.isRemote)
								onBlockImpact(movement.getBlockPos(), movement);
						}

						this.posX += (movement.hitVec.x - this.posX) * 0.6;
						this.posY += (movement.hitVec.y - this.posY) * 0.6;
						this.posZ += (movement.hitVec.z - this.posZ) * 0.6;

						this.motionX *= config.bounceMod;
						this.motionY *= config.bounceMod;
						this.motionZ *= config.bounceMod;

						didBounce = true;
					}
				}
			}

		}

		/// ZONE 1 END ///

		if (!didBounce) {
			motionY -= config.gravity;
			this.posX += this.motionX * this.config.velocity;
			this.posY += this.motionY * this.config.velocity;
			this.posZ += this.motionZ * this.config.velocity;
			this.setPosition(this.posX, this.posY, this.posZ);
		}
		
		/// SPECIAL UPDATE BEHAVIOR ///
        if(this.config.bUpdate != null)
        	this.config.bUpdate.behaveUpdate(this);

		// if (this.config.style == BulletConfiguration.STYLE_ROCKET && !world.isRemote) this.world.spawnEntity(new EntityTSmokeFX(world, this.posX, this.posY, this.posZ, 0, 0, 0));

		float f2;
		this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
		f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
		for (this.rotationPitch = (float) (Math.atan2(this.motionY, (double) f2) * 180.0D / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
			;
		}

		while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
			this.prevRotationPitch += 360.0F;
		}

		while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
			this.prevRotationYaw -= 360.0F;
		}

		while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
			this.prevRotationYaw += 360.0F;
		}

		if (this.ticksExisted > config.maxAge || (overrideMaxAge != -1 && this.ticksExisted > overrideMaxAge))
			this.setDead();

		if(world.isRemote && !config.vPFX.isEmpty()) {
			double motion = Math.min(new Vec3d(motionX, motionY, motionZ).length(), 0.1);

			for (double d = 0; d < 1; d += 1 / motion) {

				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("type", "vanillaExt");
				nbt.setString("mode", config.vPFX);
				nbt.setDouble("posX", (this.lastTickPosX - this.posX) * d + this.posX);
				nbt.setDouble("posY", (this.lastTickPosY - this.posY) * d + this.posY);
				nbt.setDouble("posZ", (this.lastTickPosZ - this.posZ) * d + this.posZ);
				MainRegistry.proxy.effectNT(nbt);
			}
		}
	}
	
	private void doHitVFX(@Nullable BlockPos pos, RayTraceResult hit){
		if(getDataManager().get(STYLE) == BulletConfiguration.STYLE_TRACER){
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("type", "bimpact");
			tag.setByte("hitType", (byte) hit.typeOfHit.ordinal());
			Vec3d norm = Library.normalFromRayTrace(hit);
			tag.setFloat("nX", (float) norm.x);
			tag.setFloat("nY", (float) norm.y);
			tag.setFloat("nZ", (float) norm.z);
			tag.setFloat("dirX", (float) motionX);
			tag.setFloat("dirY", (float) motionY);
			tag.setFloat("dirZ", (float) motionZ);
			if(hit.typeOfHit == Type.BLOCK){
				IBlockState blockstate = world.getBlockState(pos);
				Block block = blockstate.getBlock();
				tag.setInteger("block", Block.getIdFromBlock(block));
				tag.setByte("meta", (byte) block.getMetaFromState(blockstate));
			}
			PacketThreading.createSendToAllTrackingThreadedPacket(new AuxParticlePacketNT(tag, hit.hitVec.x, hit.hitVec.y, hit.hitVec.z), this);
			if(hit.typeOfHit == Type.ENTITY && hit.entityHit instanceof EntityLivingBase){
				EntityHitDataHandler.addHit((EntityLivingBase) hit.entityHit, this, hit.hitVec, new Vec3d(this.motionX, this.motionY, this.motionZ).normalize());
			}
		}
	}

	// for when a bullet dies by hitting a block
	private void onBlockImpact(BlockPos pos, RayTraceResult hit) {
		if(config.bImpact != null)
			config.bImpact.behaveBlockHit(this, pos.getX(), pos.getY(), pos.getZ());
		if(!world.isRemote){
			this.setDead();
		}
		
		IBlockState blockstate = world.getBlockState(pos);
		Block block = blockstate.getBlock();
		
		doHitVFX(pos, hit);

		if(!CompatibilityConfig.isWarDim(world)){
			return;
		}
		if (config.incendiary > 0 && !this.world.isRemote) {
			if (world.rand.nextInt(3) == 0 && world.getBlockState(new BlockPos((int) posX, (int) posY, (int) posZ)).getBlock() == Blocks.AIR)
				world.setBlockState(new BlockPos((int) posX, (int) posY, (int) posZ), Blocks.FIRE.getDefaultState());
			if (world.rand.nextInt(3) == 0 && world.getBlockState(new BlockPos((int) posX + 1, (int) posY, (int) posZ)).getBlock() == Blocks.AIR)
				world.setBlockState(new BlockPos((int) posX + 1, (int) posY, (int) posZ), Blocks.FIRE.getDefaultState());
			if (world.rand.nextInt(3) == 0 && world.getBlockState(new BlockPos((int) posX - 1, (int) posY, (int) posZ)).getBlock() == Blocks.AIR)
				world.setBlockState(new BlockPos((int) posX - 1, (int) posY, (int) posZ), Blocks.FIRE.getDefaultState());
			if (world.rand.nextInt(3) == 0 && world.getBlockState(new BlockPos((int) posX, (int) posY + 1, (int) posZ)).getBlock() == Blocks.AIR)
				world.setBlockState(new BlockPos((int) posX, (int) posY + 1, (int) posZ), Blocks.FIRE.getDefaultState());
			if (world.rand.nextInt(3) == 0 && world.getBlockState(new BlockPos((int) posX, (int) posY - 1, (int) posZ)).getBlock() == Blocks.AIR)
				world.setBlockState(new BlockPos((int) posX, (int) posY - 1, (int) posZ), Blocks.FIRE.getDefaultState());
			if (world.rand.nextInt(3) == 0 && world.getBlockState(new BlockPos((int) posX, (int) posY, (int) posZ + 1)).getBlock() == Blocks.AIR)
				world.setBlockState(new BlockPos((int) posX, (int) posY, (int) posZ + 1), Blocks.FIRE.getDefaultState());
			if (world.rand.nextInt(3) == 0 && world.getBlockState(new BlockPos((int) posX, (int) posY, (int) posZ - 1)).getBlock() == Blocks.AIR)
				world.setBlockState(new BlockPos((int) posX, (int) posY, (int) posZ - 1), Blocks.FIRE.getDefaultState());
		}

		if (config.emp > 0)
			ExplosionNukeGeneric.empBlast(this.world, shooter, (int) (this.posX + 0.5D), (int) (this.posY + 0.5D), (int) (this.posZ + 0.5D), config.emp);

		if (config.emp > 3) {
			if (!this.world.isRemote) {

				EntityEMPBlast cloud = new EntityEMPBlast(this.world, config.emp);
				cloud.posX = this.posX;
				cloud.posY = this.posY + 0.5F;
				cloud.posZ = this.posZ;

				this.world.spawnEntity(cloud);
			}
		}

		if (config.jolt > 0 && !world.isRemote)
			ExplosionLarge.jolt(world, shooter, posX, posY, posZ, config.jolt, 150, 0.25);

		if (config.explosive > 0 && !world.isRemote)
			world.newExplosion(this, posX, posY, posZ, config.explosive, config.incendiary > 0, config.blockDamage);

		if (config.shrapnel > 0 && !world.isRemote)
			ExplosionLarge.spawnShrapnels(world, posX, posY, posZ, config.shrapnel);

		if (config.chlorine > 0 && !world.isRemote) {
			ExplosionChaos.spawnChlorine(world, posX, posY, posZ, config.chlorine, 1.5, 0);
			world.playSound((double) (posX + 0.5F), (double) (posY + 0.5F), (double) (posZ + 0.5F), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.HOSTILE, 5.0F, 2.6F + (rand.nextFloat() - rand.nextFloat()) * 0.8F, true);
		}

		if (config.rainbow > 0 && !world.isRemote) {
			this.world.playSound(this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 100.0f, this.world.rand.nextFloat() * 0.1F + 0.9F, true);

			EntityNukeExplosionMK3 entity = new EntityNukeExplosionMK3(this.world);
			entity.posX = this.posX;
			entity.posY = this.posY;
			entity.posZ = this.posZ;
			entity.destructionRange = config.rainbow;
			entity.speed = 25;
			entity.coefficient = 1.0F;
			entity.waste = false;

			this.world.spawnEntity(entity);

			EntityCloudFleijaRainbow cloud = new EntityCloudFleijaRainbow(this.world, config.rainbow);
			cloud.posX = this.posX;
			cloud.posY = this.posY;
			cloud.posZ = this.posZ;
			this.world.spawnEntity(cloud);
		}

		if (config.nuke > 0 && !world.isRemote) {
			world.spawnEntity(EntityNukeExplosionMK5.statFac(world, config.nuke, posX, posY, posZ).setDetonator(shooter));
			
			if(BombConfig.enableNukeClouds) {
				if(MainRegistry.polaroidID == 11 || rand.nextInt(100) == 0){
					EntityNukeTorex.statFacBale(world, pos.getX() + 0.5, pos.getY() + 5, pos.getZ() + 0.5, config.nuke);
				}
				else{
					EntityNukeTorex.statFac(world, pos.getX() + 0.5, pos.getY() + 5, pos.getZ() + 0.5, config.nuke);
				}
			}
			world.playSound(null, posX, posY, posZ, HBMSoundHandler.mukeExplosion, SoundCategory.HOSTILE, 15.0F, 1.0F);
		}

		if (config.destroysBlocks && !world.isRemote) {
			if (blockstate.getBlockHardness(world, pos) <= 120)
				world.destroyBlock(pos, false);
		} else if (config.doesBreakGlass && !world.isRemote) {
            if (blockstate.getMaterial() == Material.GLASS && block.getExplosionResistance(null) < 0.6f)
                world.destroyBlock(pos, false);
            if(block == ModBlocks.red_barrel)
                ((RedBarrel) ModBlocks.red_barrel).explode(world, pos.getX(), pos.getY(), pos.getZ());
            if(block instanceof BlockDetonatable detonatable) detonatable.onShot(world, pos);
		}
	}

	// for when a bullet dies by hitting a block
	private void onRicochet(BlockPos pos) {
		if(config.bRicochet != null)
			config.bRicochet.behaveBlockRicochet(this, pos.getX(), pos.getY(), pos.getZ());
	}

	// for when a bullet dies by hitting an entity
	private void onEntityImpact(Entity e, RayTraceResult rt) {
		if(!CompatibilityConfig.isWarDim(world)){
			return;
		}
		onEntityHurt(e, rt, false);
		onBlockImpact(new BlockPos(e), rt);

		if(config.bHit != null)
			config.bHit.behaveEntityHit(this, e);
	}

	// for when a bullet hurts an entity, not necessarily dying
	private void onEntityHurt(Entity e, RayTraceResult rt, boolean doVFX) {
		if(doVFX)
			doHitVFX(null, rt);
		if(!CompatibilityConfig.isWarDim(world)){
			return;
		}
		if(config.bHurt != null)
			config.bHurt.behaveEntityHurt(this, e);
		
		if (config.incendiary > 0 && !world.isRemote) {
			e.setFire(config.incendiary);
		}

		if (config.leadChance > 0 && !world.isRemote && world.rand.nextInt(100) < config.leadChance && e instanceof EntityLivingBase) {
			((EntityLivingBase) e).addPotionEffect(new PotionEffect(HbmPotion.lead, 10 * 20, 0));
		}

		if (e instanceof EntityLivingBase && config.effects != null && !config.effects.isEmpty() && !world.isRemote) {

			for (PotionEffect effect : config.effects) {
				((EntityLivingBase) e).addPotionEffect(new PotionEffect(effect));
			}
		}

		if (config.instakill && e instanceof EntityLivingBase && !world.isRemote) {
			((EntityLivingBase) e).setHealth(0.0F);
			if(!(e instanceof EntityPlayer && (((EntityPlayer)e).capabilities.isCreativeMode || ((EntityPlayer)e).isSpectator())))
				((EntityLivingBase)e).setHealth(0.0F);
		}

		if (config.caustic > 0 && e instanceof EntityPlayer) {
			ArmorUtil.damageSuit((EntityPlayer) e, 0, config.caustic);
			ArmorUtil.damageSuit((EntityPlayer) e, 1, config.caustic);
			ArmorUtil.damageSuit((EntityPlayer) e, 2, config.caustic);
			ArmorUtil.damageSuit((EntityPlayer) e, 3, config.caustic);
		}
	}
	
	@Override
	public float getEyeHeight() {
		return 0;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {

		int cfg = nbt.getInteger("config");
		this.config = BulletConfigSyncingUtil.pullConfig(cfg);
		
		if(this.config == null) {
			this.setDead();
			return;
		}
		
		this.getDataManager().set(BULLETCONFIG, cfg);

		this.getDataManager().set(STYLE, nbt.getInteger("overrideStyle"));
		this.getDataManager().set(TRAIL, this.config.trail);
		
		this.overrideDamage = nbt.getFloat("damage");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("config", this.getDataManager().get(BULLETCONFIG));
		nbt.setInteger("overrideStyle", this.getDataManager().get(STYLE));
		nbt.setFloat("damage", this.overrideDamage);
	}
}
