package com.hbm.entity.projectile;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockDetonatable;
import com.hbm.blocks.generic.RedBarrel;
import com.hbm.config.CompatibilityConfig;
import com.hbm.entity.grenade.EntityGrenadeTau;
import com.hbm.entity.mob.EntityCreeperNuclear;
import com.hbm.handler.ArmorUtil;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.ParticleBurstPacket;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
@AutoRegister(name = "entity_bullet")
public class EntityBullet extends Entity implements IProjectile {

	private static final DataParameter<Boolean> TAU = EntityDataManager.createKey(EntityBullet.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> CHOPPER = EntityDataManager.createKey(EntityBullet.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> CRITICAL = EntityDataManager.createKey(EntityBullet.class, DataSerializers.BOOLEAN);

	private double prevMotionX;
	private double prevMotionY;
	private double prevMotionZ;
	
	private int field_145791_d = -1;
	private int field_145792_e = -1;
	private int field_145789_f = -1;
	public double gravity = 0.0D;
	private Block field_145790_g;
	private IBlockState test_blockstate;
	private int inData;
	private boolean inGround;
	/** 1 if the player can pick up the arrow */
	public int canBePickedUp;
	/** Seems to be some sort of timer for animating an arrow. */
	public int arrowShake;
	/** The owner of this arrow. */
	public Entity shootingEntity;
	private int ticksInGround;
	public double damage;
	/** The amount of knockback an arrow applies when it hits a mob. */
	private int knockbackStrength;
	private boolean instakill = false;
	private boolean rad = false;
	public boolean antidote = false;
	public boolean fire = false;

	public EntityBullet(World worldIn) {
		super(worldIn);
		if(worldIn.isRemote)
			setRenderDistanceWeight(10.0D);
		this.setSize(0.5F, 0.5F);
	}

	public EntityBullet(World w, double x, double y, double z) {
		this(w);
		this.setPosition(x, y, z);
		this.setRenderYawOffset(0.0F);
	}

	public EntityBullet(World w, EntityLivingBase shooter, EntityLivingBase shootingAt, float velocity, float innacuracy) {
		this(w);
		this.shootingEntity = shooter;

		if (shooter instanceof EntityPlayer) {
			this.canBePickedUp = 1;
		}

		this.posY = shooter.posY + shooter.getEyeHeight() - 0.10000000149011612D;
		double d0 = shootingAt.posX - shooter.posX;
		double d1 = shootingAt.getEntityBoundingBox().minY + shootingAt.height / 3.0F - this.posY;
		double d2 = shootingAt.posZ - shooter.posZ;
		double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);

		if (d3 >= 1.0E-7D) {
			float f2 = (float) (Math.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F;
			float f3 = (float) (-(Math.atan2(d1, d3) * 180.0D / Math.PI));
			double d4 = d0 / d3;
			double d5 = d2 / d3;
			this.setLocationAndAngles(shooter.posX + d4, this.posY, shooter.posZ + d5, f2, f3);
			this.setRenderYawOffset(0.0F);
			//Adding f4 just makes cyber crabs miss
			//float f4 = (float) d3 * 0.2F;
			this.setThrowableHeading(d0, d1 /*+ f4*/, d2, velocity, innacuracy);
		}
	}

	public EntityBullet(World world, EntityLivingBase shooter, float velocity, int dmgMin, int dmgMax, boolean instakill, boolean rad, EnumHand hand) {
		this(world);
		this.shootingEntity = shooter;

		if (shooter instanceof EntityPlayer) {
			this.canBePickedUp = 1;
		}

		this.setSize(0.5F, 0.5F);
		this.setLocationAndAngles(shooter.posX, shooter.posY + shooter.getEyeHeight(), shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);
		if(hand == EnumHand.MAIN_HAND){
			this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
			this.posY -= 0.10000000149011612D;
			this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
		} else {
			this.posX += MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
			this.posY -= 0.10000000149011612D;
			this.posZ += MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
		}
		
		this.setPosition(this.posX, this.posY, this.posZ);
		this.setRenderYawOffset(0.0F);
		this.motionX = -MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionZ = MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionY = (-MathHelper.sin(this.rotationPitch / 180.0F * (float) Math.PI));
		this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, velocity * 1.5F, 1.0F);

		// this.dmgMin = dmgMin;
		// this.dmgMax = dmgMax;
		this.instakill = instakill;
		this.rad = rad;
	}

	public EntityBullet(World world, EntityLivingBase shooter, float velocity, EnumHand hand) {
		this(world);
		this.shootingEntity = shooter;

		if (shooter instanceof EntityPlayer) {
			this.canBePickedUp = 1;
		}

		this.setSize(0.5F, 0.5F);
		this.setLocationAndAngles(shooter.posX, shooter.posY + shooter.getEyeHeight(), shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);
		if(hand == EnumHand.MAIN_HAND){
			this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
			this.posY -= 0.10000000149011612D;
			this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
		} else {
			this.posX += MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
			this.posY -= 0.10000000149011612D;
			this.posZ += MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
		}
		
		this.setPosition(this.posX, this.posY, this.posZ);
		this.setRenderYawOffset(0.0F);
		this.motionX = -MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionZ = MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionY = (-MathHelper.sin(this.rotationPitch / 180.0F * (float) Math.PI));
		this.setThrowableHeading2(this.motionX, this.motionY, this.motionZ, velocity * 1.5F, 1.0F);
	}

	public EntityBullet(World world, EntityLivingBase shooter, float velocity, int dmgMin, int dmgMax, boolean instakill, String isTau, EnumHand hand) {
		this(world);
		this.shootingEntity = shooter;

		if (shooter instanceof EntityPlayer) {
			this.canBePickedUp = 1;
		}

		this.setSize(0.5F, 0.5F);
		this.setLocationAndAngles(shooter.posX, shooter.posY + shooter.getEyeHeight(), shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);
		if(hand == EnumHand.MAIN_HAND){
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
		this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, velocity * 1.5F, 1.0F);
		this.setTau(isTau.equals("tauDay"));
		this.setChopper(isTau.equals("chopper"));
		this.setIsCritical(!isTau.equals("chopper"));
	}

	// why the living shit did i make isTau a string? who knows, who cares.
	public EntityBullet(World world, EntityLivingBase shooter, float velocity, int dmgMin, int dmgMax, boolean instakill, String isTau, EntityGrenadeTau grenade) {
		this(world);
		this.shootingEntity = shooter;
	
		this.setSize(0.5F, 0.5F);
		this.setLocationAndAngles(grenade.posX, grenade.posY + grenade.getEyeHeight(), grenade.posZ, grenade.rotationYaw, grenade.rotationPitch);
		this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
		this.posY -= 0.10000000149011612D;
		this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
		this.setPosition(this.posX, this.posY, this.posZ);
		this.motionX = -MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionZ = MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionY = (-MathHelper.sin(this.rotationPitch / 180.0F * (float) Math.PI));
		this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, velocity * 1.5F, 1.0F);
		this.setTau(isTau.equals("tauDay"));
		this.setIsCritical(true);
	}

	public EntityBullet(World world, int x, int y, int z, double mx, double my, double mz, double grav) {
		super(world);
		this.posX = x + 0.5F;
		this.posY = y + 0.5F;
		this.posZ = z + 0.5F;

		this.motionX = mx;
		this.motionY = my;
		this.motionZ = mz;

		this.gravity = grav;
	}

	@Override
	protected void entityInit() {
		this.getDataManager().register(CRITICAL, false);
		this.getDataManager().register(TAU, false);
		this.getDataManager().register(CHOPPER, false);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
		this.setPosition(x, y, z);
		this.setRotation(yaw, pitch);
	}

	@SideOnly(Side.CLIENT)
	@Override
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
			this.ticksInGround = 0;
		}
	}

	@Override
	public void onUpdate() {
		if(this.firstUpdate){
			this.prevMotionX = motionX;
			this.prevMotionY = motionY;
			this.prevMotionZ = motionZ;
		}
		if(this.getIsCritical()){
			this.motionX = prevMotionX;
			this.motionY = prevMotionY;
			this.motionZ = prevMotionZ;
		}
		super.onUpdate();

		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
			// this.prevRotationPitch = this.rotationPitch =
			// (float)(Math.atan2(this.motionY, (double)f) * 180.0D / Math.PI);
		}

		BlockPos bulletTilePos = new BlockPos(this.field_145791_d, this.field_145792_e, this.field_145789_f);
		IBlockState blockstate = this.world.getBlockState(bulletTilePos);
		Block block = blockstate.getBlock();

		if (blockstate.getMaterial() != Material.AIR) {
			AxisAlignedBB axisalignedbb = blockstate.getCollisionBoundingBox(this.world, bulletTilePos);

			if (axisalignedbb != Block.NULL_AABB && axisalignedbb.offset(bulletTilePos).contains(new Vec3d(this.posX, this.posY, this.posZ)) && !this.getIsCritical()) {
				this.inGround = true;
			}

            if (block == ModBlocks.red_barrel) {
                ((RedBarrel) block).explode(world, this.field_145791_d, this.field_145792_e, this.field_145789_f);
            }
            if(block instanceof BlockDetonatable detonatable) detonatable.onShot(world, bulletTilePos);

            if (blockstate.getMaterial() == Material.GLASS && block.getExplosionResistance(null) < 0.6f) {
				this.world.setBlockToAir(bulletTilePos);
				//Drillgon200: add particle burst packet so the glass doesn't just disappear.
				PacketDispatcher.wrapper.sendToAll(new ParticleBurstPacket(field_145791_d, field_145792_e, field_145789_f, Block.getIdFromBlock(block), block.getMetaFromState(blockstate)));
				this.world.playSound(this.field_145791_d, this.field_145792_e, this.field_145789_f, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F, true);
			}
		}

		if (this.arrowShake > 0) {
			--this.arrowShake;
		}

		if (this.inGround && !this.getIsCritical()) {
			this.setDead();

		} else {
			Vec3d vec31 = new Vec3d(this.posX, this.posY, this.posZ);
			Vec3d vec3 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			//Drillgon200: Yeah I don't know if this is the best way, but at least bullets can hit entities that are closer than three blocks now.
			//vec31 = new Vec3d(this.posX - this.motionX, this.posY - this.motionY, this.posZ - this.motionZ);
			//Drillgon200: Ok that was retarded and completely breaks turrets.
			
			RayTraceResult RayTraceResult = this.world.rayTraceBlocks(vec31, vec3, false, true, false);
			vec31 = new Vec3d(this.posX, this.posY, this.posZ);
			vec3 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

			vec31 = new Vec3d(this.posX - this.motionX, this.posY - this.motionY, this.posZ - this.motionZ);
			
			if (RayTraceResult != null) {
				vec3 = new Vec3d(RayTraceResult.hitVec.x, RayTraceResult.hitVec.y, RayTraceResult.hitVec.z);
			}

			Entity entity = null;
			List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow(Math.abs(this.motionX), Math.abs(this.motionY), Math.abs(this.motionZ)).grow(1.0D));
			double d0 = 0.0D;
			int i;
			float f1;

			for (i = 0; i < list.size(); ++i) {
				Entity entity1 = (Entity) list.get(i);
				if(entity1 instanceof EntityBullet){
					if(((EntityBullet)entity1).shootingEntity == this.shootingEntity){
						continue;
					}
				}
				if (entity1.canBeCollidedWith() && (entity1 != this.shootingEntity)) {
					f1 = 0.3F;
					AxisAlignedBB axisalignedbb1 = entity1.getEntityBoundingBox().grow(f1);
					RayTraceResult RayTraceResult1 = axisalignedbb1.calculateIntercept(vec31, vec3);

					if (RayTraceResult1 != null) {
						double d1 = vec31.distanceTo(RayTraceResult1.hitVec);

						if (d1 < d0 || d0 == 0.0D) {
							entity = entity1;
							d0 = d1;
						}
					}
				}
			}

			if (entity != null) {
				RayTraceResult = new RayTraceResult(entity);
			}

			if (RayTraceResult != null && RayTraceResult.entityHit != null && RayTraceResult.entityHit instanceof EntityPlayer) {
				EntityPlayer entityplayer = (EntityPlayer) RayTraceResult.entityHit;

				if (entityplayer.capabilities.disableDamage || this.shootingEntity instanceof EntityPlayer && !((EntityPlayer) this.shootingEntity).canAttackPlayer(entityplayer)) {
					RayTraceResult = null;
				}
			}

			float f2;
			float f4;

			if (RayTraceResult != null && CompatibilityConfig.isWarDim(world)) {
				if (RayTraceResult.entityHit != null) {
					// TODO: Remove test feature in release version
					if (!(RayTraceResult.entityHit instanceof EntityItemFrame) || RayTraceResult.entityHit instanceof EntityItemFrame && (((EntityItemFrame) RayTraceResult.entityHit).getDisplayedItem() == null || ((EntityItemFrame) RayTraceResult.entityHit).getDisplayedItem() != null && ((EntityItemFrame) RayTraceResult.entityHit).getDisplayedItem().getItem() != ModItems.flame_pony)) {
						f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
						int k = MathHelper.ceil(f2 * this.damage);

						if (this.getIsCritical()) {
							k += this.rand.nextInt(k / 2 + 2);
						}

						DamageSource damagesource = null;

						// L: Crit
						// R: Chop
						// X: NOT
						// O: Direct

						// X X Bullet
						// \|
						// O-X Tau
						// |/
						// X-O Displacer

						if (!this.getIsCritical() && !this.getIsChopper()) {
							if (this.shootingEntity == null) {
								damagesource = ModDamageSource.causeBulletDamage(this, this);
							} else {
								damagesource = ModDamageSource.causeBulletDamage(this, shootingEntity);
							}
						} else if (!this.getIsChopper()) {
							if (this.shootingEntity == null) {
								damagesource = ModDamageSource.causeTauDamage(this, this);
							} else {
								damagesource = ModDamageSource.causeTauDamage(this, shootingEntity);
							}
						} else if (!this.getIsCritical()) {
							if (this.shootingEntity == null) {
								damagesource = ModDamageSource.causeDisplacementDamage(this, this);
							} else {
								damagesource = ModDamageSource.causeDisplacementDamage(this, shootingEntity);
							}
						}

						if (fire || this.isBurning() && !(RayTraceResult.entityHit instanceof EntityEnderman)) {
							RayTraceResult.entityHit.setFire(5);
						}

						if (RayTraceResult.entityHit.attackEntityFrom(damagesource, (float) damage)) {
							if (RayTraceResult.entityHit instanceof EntityLivingBase) {
								EntityLivingBase entitylivingbase = (EntityLivingBase) RayTraceResult.entityHit;

								if (rad) {
									if (entitylivingbase instanceof EntityPlayer && ArmorUtil.checkForHazmat((EntityPlayer) entitylivingbase)) {
									} else if (entitylivingbase instanceof EntityCreeper) {
										EntityCreeperNuclear creep = new EntityCreeperNuclear(this.world);
										creep.setLocationAndAngles(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ, entitylivingbase.rotationYaw, entitylivingbase.rotationPitch);
										if (!entitylivingbase.isDead)
											if (!world.isRemote)
												world.spawnEntity(creep);
										entitylivingbase.setDead();
									} else if (entitylivingbase instanceof EntityVillager) {
										EntityZombie creep = new EntityZombie(this.world);
										creep.setLocationAndAngles(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ, entitylivingbase.rotationYaw, entitylivingbase.rotationPitch);
										entitylivingbase.setDead();
										if (!this.world.isRemote)
											this.world.spawnEntity(creep);
									} else if (entitylivingbase instanceof EntityLivingBase && !(entitylivingbase instanceof EntityCreeperNuclear) && !(entitylivingbase instanceof EntityMooshroom) && !(entitylivingbase instanceof EntityZombie)) {
										entitylivingbase.addPotionEffect(new PotionEffect(MobEffects.POISON, 2 * 60 * 20, 2));
										entitylivingbase.addPotionEffect(new PotionEffect(MobEffects.WITHER, 20, 4));
										entitylivingbase.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 1 * 60 * 20, 1));
									}
								}

								if (antidote)
									entitylivingbase.clearActivePotions();

								if (this.knockbackStrength > 0) {
									f4 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

									if (f4 > 0.0F) {
										RayTraceResult.entityHit.addVelocity(this.motionX * this.knockbackStrength * 0.6000000238418579D / f4, 0.1D, this.motionZ * this.knockbackStrength * 0.6000000238418579D / f4);
									}
								}
								

								if (this.shootingEntity != null && this.shootingEntity instanceof EntityLivingBase) {
									EnchantmentHelper.applyThornEnchantments(entitylivingbase, this.shootingEntity);
									EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase) this.shootingEntity, entitylivingbase);
								}

								if (this.shootingEntity != null && RayTraceResult.entityHit != this.shootingEntity && RayTraceResult.entityHit instanceof EntityPlayer && this.shootingEntity instanceof EntityPlayerMP) {
									((EntityPlayerMP) this.shootingEntity).connection.sendPacket(new SPacketChangeGameState(6, 0.0F));
								}
							}

							if (!(RayTraceResult.entityHit instanceof EntityEnderman)) {
								if (!this.world.isRemote) {
									if (!instakill || RayTraceResult.entityHit instanceof EntityPlayer) {
										// RayTraceResult.entityHit.attackEntityFrom(DamageSource.generic,
										// dmgMin + rand.nextInt(dmgMax -
										// dmgMin));
									} else if (RayTraceResult.entityHit instanceof EntityLivingBase) {
										((EntityLivingBase) RayTraceResult.entityHit).setHealth(0.0F);
									}
								}
								if (!this.getIsCritical())
									// this.setDead();
									;
							}
						} else {
							if (RayTraceResult.entityHit instanceof EntityLivingBase) {
								float dmg = (float) damage + ((EntityLivingBase)RayTraceResult.entityHit).lastDamage;
								RayTraceResult.entityHit.attackEntityFrom(damagesource, dmg);
							}
						}

						/* else {
							if (RayTraceResult.entityHit instanceof EntityLivingBase && !(RayTraceResult.entityHit instanceof EntityHunterChopper)) {
								EntityLivingBase target = (EntityLivingBase) RayTraceResult.entityHit;
								target.setHealth((float) (target.getHealth() - damage));
							}
						}*/
					} else {
						this.setDead();
					}
				} else if (!this.getIsCritical()) {
					this.field_145791_d = RayTraceResult.getBlockPos().getX();
					this.field_145792_e = RayTraceResult.getBlockPos().getY();
					this.field_145789_f = RayTraceResult.getBlockPos().getZ();
					BlockPos pos = new BlockPos(this.field_145791_d, this.field_145792_e, this.field_145789_f);
					test_blockstate = this.world.getBlockState(pos);
					this.field_145790_g = test_blockstate.getBlock();
					this.inData = field_145790_g.getMetaFromState(test_blockstate);
					this.motionX = ((float) (RayTraceResult.hitVec.x - this.posX));
					this.motionY = ((float) (RayTraceResult.hitVec.y - this.posY));
					this.motionZ = ((float) (RayTraceResult.hitVec.z - this.posZ));
					f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
					this.posX -= this.motionX / f2 * 0.05000000074505806D;
					this.posY -= this.motionY / f2 * 0.05000000074505806D;
					this.posZ -= this.motionZ / f2 * 0.05000000074505806D;
					this.inGround = true;
					this.arrowShake = 7;

					if (this.test_blockstate.getMaterial() != Material.AIR) {
						this.field_145790_g.onEntityCollision(this.world, pos, test_blockstate, this);
					}
				}
			}

			if (world.isRemote && this.getIsCritical()) {
				for (i = 0; i < 8; ++i) {
					if (!this.getIsTau())
						this.world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, true, this.posX + this.motionX * i / 8.0D, this.posY + this.motionY * i / 8.0D, this.posZ + this.motionZ * i / 8.0D, 0, 0, 0/*-this.motionX, -this.motionY + 0.2D, -this.motionZ*/);
					else
						this.world.spawnParticle(EnumParticleTypes.REDSTONE, this.posX + this.motionX * i / 8.0D, this.posY + this.motionY * i / 8.0D, this.posZ + this.motionZ * i / 8.0D, 0, 0, 0/*-this.motionX, -this.motionY + 0.2D, -this.motionZ*/);
				}
			}

			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;
			f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

			// for (this.rotationPitch = (float)(Math.atan2(this.motionY,
			// (double)f2) * 180.0D / Math.PI); this.rotationPitch -
			// this.prevRotationPitch < -180.0F; this.prevRotationPitch -=
			// 360.0F)
			{
				;
			}

			/*
			 * while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
			 * this.prevRotationPitch += 360.0F; }
			 * 
			 * while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
			 * this.prevRotationYaw -= 360.0F; }
			 * 
			 * while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
			 * this.prevRotationYaw += 360.0F; }
			 */

			// this.rotationPitch = this.prevRotationPitch + (this.rotationPitch
			// - this.prevRotationPitch) * 0.2F;
			// this.rotationYaw = this.prevRotationYaw + (this.rotationYaw -
			// this.prevRotationYaw) * 0.2F;
			float f3 = 0.99F;
			f1 = 0.05F;

			if (world.isRemote && this.isInWater()) {
				for (int l = 0; l < 4; ++l) {
					f4 = 0.25F;
					this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * f4, this.posY - this.motionY * f4, this.posZ - this.motionZ * f4, this.motionX, this.motionY, this.motionZ);
				}

				f3 = 0.8F;
			}

			if (this.isWet()) {
				this.extinguish();
			}

			this.motionX *= f3;
			this.motionY *= f3;
			this.motionZ *= f3;
			this.motionY -= gravity;
			this.setPosition(this.posX, this.posY, this.posZ);
			this.doBlockCollisions();
		}

		if (this.ticksExisted > 250)
			this.setDead();
	}

	@Override
	public void onCollideWithPlayer(EntityPlayer entityIn) {
		if (!this.world.isRemote && this.inGround && this.arrowShake <= 0) {
			boolean flag = this.canBePickedUp == 1 || this.canBePickedUp == 2 && entityIn.capabilities.isCreativeMode;

			if (this.canBePickedUp == 1 && !entityIn.inventory.addItemStackToInventory(new ItemStack(ModItems.nothing, 1))) { //ammo_rocket
				flag = false;
			}

			if (flag) {
				entityIn.onItemPickup(this, 1);
				this.setDead();
			}
		}
	}
	

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getDamage() {
		return damage;
	}

	/**
	 * Sets the amount of knockback the arrow applies when it hits a mob.
	 */
	public void setKnockbackStrength(int p_70240_1_) {
		this.knockbackStrength = p_70240_1_;
	}

	@Override
	public boolean canBeAttackedWithItem() {
		return false;
	}

	public void setIsCritical(boolean critical) {
		this.getDataManager().set(CRITICAL, critical);
	}

	public void setTau(boolean tau) {
		this.getDataManager().set(TAU, tau);
	}

	public void setChopper(boolean isChopper) {
		this.getDataManager().set(CHOPPER, isChopper);
	}

	/**
	 * Whether the arrow has a stream of critical hit particles flying behind
	 * it.
	 */
	public boolean getIsCritical() {
		return this.getDataManager().get(CRITICAL);
	}

	public boolean getIsTau() {
		return this.getDataManager().get(TAU);
	}

	public boolean getIsChopper() {
		return this.getDataManager().get(CHOPPER);
	}

	@Override
	public float getBrightness() {
		if (this.getIsCritical() || this.getIsChopper())
			return 1.0F;
		else
			return super.getBrightness();
	}

	@Override
	public int getBrightnessForRender() {
		if (this.getIsCritical() || this.getIsChopper())
			return 15728880;
		else
			return super.getBrightnessForRender();
	}

	@Override
	public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
		this.setThrowableHeading(x, y, z, velocity, inaccuracy);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		this.field_145791_d = compound.getInteger("xTile");
		this.field_145792_e = compound.getInteger("yTile");
		this.field_145789_f = compound.getInteger("zTile");
		this.ticksInGround = compound.getInteger("life");
		this.field_145790_g = Block.getBlockById(compound.getInteger("inTile"));
		this.inData = compound.getInteger("inData") & 255;
		test_blockstate = field_145790_g == null ? null : field_145790_g.getStateFromMeta(inData);
		this.arrowShake = compound.getInteger("shake") & 255;
		this.inGround = compound.getInteger("inGround") == 1;

		if (compound.hasKey("damage", 99)) {
			this.damage = compound.getDouble("damage");
		}

		if (compound.hasKey("pickup", 99)) {
			this.canBePickedUp = compound.getInteger("pickup");
		} else if (compound.hasKey("player", 99)) {
			this.canBePickedUp = compound.getBoolean("player") ? 1 : 0;
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setInteger("xTile", this.field_145791_d);
		compound.setInteger("yTile", this.field_145792_e);
		compound.setInteger("zTile", this.field_145789_f);
		compound.setInteger("life", this.ticksInGround);
		compound.setInteger("inTile", Block.getIdFromBlock(this.field_145790_g));
		compound.setInteger("inData", this.inData);
		compound.setInteger("shake", this.arrowShake);
		compound.setInteger("inGround", (this.inGround ? 1 : 0));
		compound.setInteger("pickup", this.canBePickedUp);
		compound.setDouble("damage", this.damage);
	}

	public void setThrowableHeading(double x, double y, double z, float velocity, float inaccuracy) {
		float f2 = MathHelper.sqrt(x * x + y * y + z * z);
		x /= f2;
		y /= f2;
		z /= f2;
		x += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * inaccuracy;
		y += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * inaccuracy;
		z += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * inaccuracy;
		x *= velocity;
		y *= velocity;
		z *= velocity;
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
		float f3 = MathHelper.sqrt(x * x + z * z);
		this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(x, z) * 180.0D / Math.PI);
		this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(y, f3) * 180.0D / Math.PI);
		this.ticksInGround = 0;
	}

	/**
	 * Drillgon200: Same as setThrowableHeading except with about 5.6 times more
	 * inaccuracy?
	 */
	public void setThrowableHeading2(double x, double y, double z, float velocity, float inaccuracy) {
		float f2 = MathHelper.sqrt(x * x + y * y + z * z);
		x /= f2;
		y /= f2;
		z /= f2;
		x += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : 1) * 0.042499999832361937D * inaccuracy;
		y += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : 1) * 0.042499999832361937D * inaccuracy;
		z += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : 1) * 0.042499999832361937D * inaccuracy;
		x *= velocity;
		y *= velocity;
		z *= velocity;
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
		float f3 = MathHelper.sqrt(x * x + z * z);
		this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(x, z) * 180.0D / Math.PI);
		this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(y, f3) * 180.0D / Math.PI);
		this.ticksInGround = 0;
	}
	
	@Override
	public boolean handleWaterMovement() {
		double x = this.motionX;
		double y = this.motionY;
		double z = this.motionZ;
		boolean b = super.handleWaterMovement();
		if(this.getIsCritical()){
			this.motionX = x;
			this.motionY = y;
			this.motionZ = z;
		}
		return b;
	}
	
	@Override
	public boolean isPushedByWater() {
		return !this.getIsCritical();
	}

	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		double d0 = this.getEntityBoundingBox().getAverageEdgeLength() * 10.0D;

		if (Double.isNaN(d0)) {
			d0 = 1.0D;
		}

		d0 = d0 * 64.0D * getRenderDistanceWeight();
		return distance < d0 * d0;
	}
}
