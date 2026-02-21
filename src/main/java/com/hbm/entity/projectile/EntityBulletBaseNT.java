package com.hbm.entity.projectile;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockDetonatable;
import com.hbm.blocks.generic.RedBarrel;
import com.hbm.entity.effect.EntityCloudFleijaRainbow;
import com.hbm.entity.effect.EntityEMPBlast;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.*;
import com.hbm.handler.ArmorUtil;
import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.handler.BulletConfiguration;
import com.hbm.handler.GunConfigurationSedna;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.ItemGunBaseSedna;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.potion.HbmPotion;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Tuple;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.ArrayList;
import java.util.List;
/**
 * MK3 which features several improvements:
 * - uses generic throwable code, reducing boilerplate nonsense
 * - uses approach-based interpolation, preventing desyncs and making movement silky-smooth
 * - new adjustments in the base class allow for multiple MOP impacts per frame
 * - also comes with tons of legacy code to ensure compat (sadly)
 * @author hbm
 */
@AutoRegister(name = "entity_bullet_mk3", sendVelocityUpdates = false)
public class EntityBulletBaseNT extends EntityThrowableInterp implements IBulletBase {

    public static final DataParameter<Integer> STYLE = EntityDataManager.createKey(EntityBulletBaseNT.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> TRAIL = EntityDataManager.createKey(EntityBulletBaseNT.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> BULLETCONFIG = EntityDataManager.createKey(EntityBulletBaseNT.class, DataSerializers.VARINT);

    @Override public double prevX() { return prevRenderX; }
    @Override public double prevY() { return prevRenderY; }
    @Override public double prevZ() { return prevRenderZ; }
    @Override public void prevX(double d) { prevRenderX = d; }
    @Override public void prevY(double d) { prevRenderY = d; }
    @Override public void prevZ(double d) { prevRenderZ = d; }
    @Override public List<Tuple.Pair<Vec3d, Double>> nodes() { return this.trailNodes; }

    private BulletConfiguration config;
    public float overrideDamage;

    public double prevRenderX;
    public double prevRenderY;
    public double prevRenderZ;
    public final List<Tuple.Pair<Vec3d, Double>> trailNodes = new ArrayList<>();

    public BulletConfiguration getConfig() {
        return config;
    }

    public EntityBulletBaseNT(World world) {
        super(world);
        if(world.isRemote)
            setRenderDistanceWeight(10.0D);
        this.setSize(0.5F, 0.5F);
    }

    public EntityBulletBaseNT(World world, int config) {
        super(world);
        this.config = BulletConfigSyncingUtil.pullConfig(config);
        this.getDataManager().set(BULLETCONFIG, config);
        if(this.config == null) {
            this.setDead();
            return;
        }
        this.getDataManager().set(STYLE, this.config.style);
        this.getDataManager().set(TRAIL, this.config.trail);
        if(world.isRemote)
            setRenderDistanceWeight(10.0D);

        if(this.config == null) {
            this.setDead();
            return;
        }

        this.setSize(0.5F, 0.5F);
    }

    public EntityBulletBaseNT(World world, int config, EntityLivingBase entity) {
        super(world);
        this.config = BulletConfigSyncingUtil.pullConfig(config);
        this.getDataManager().set(BULLETCONFIG, config);
        if(this.config == null) {
            this.setDead();
            return;
        }
        this.getDataManager().set(STYLE, this.config.style);
        this.getDataManager().set(TRAIL, this.config.trail);
        thrower = entity;

        ItemStack gun = entity.getHeldItemMainhand();
        boolean offsetShot = true;
        boolean accuracyBoost = false;

        if(!gun.isEmpty() && gun.getItem() instanceof ItemGunBaseSedna) {
            GunConfigurationSedna cfg = ((ItemGunBaseSedna) gun.getItem()).mainConfig;

            if(cfg != null) {
                if(cfg.hasSights && entity.isSneaking()) {
                    offsetShot = false;
                    accuracyBoost = true;
                }

                if(cfg.isCentered){
                    offsetShot = false;
                }
            }
        }

        this.setLocationAndAngles(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ, entity.rotationYaw, entity.rotationPitch);

        if(offsetShot) {
            double sideOffset = 0.16D;

            this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * sideOffset;
            this.posY -= 0.1D;
            this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * sideOffset;
        } else {
            this.posY -= 0.1D;
        }
        this.setPosition(this.posX, this.posY, this.posZ);

        this.motionX = -MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
        this.motionZ = MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
        this.motionY = (-MathHelper.sin(this.rotationPitch / 180.0F * (float) Math.PI));

        this.setRenderDistanceWeight(10.0D);
        this.setSize(0.5F, 0.5F);

        this.shoot(this.motionX, this.motionY, this.motionZ, 1.0F, this.config.spread * (accuracyBoost ? 0.25F : 1F));
    }

    public EntityBulletBaseNT(World world, int config, EntityLivingBase entity, EntityLivingBase target, float motion, float deviation) {
        super(world);

        this.config = BulletConfigSyncingUtil.pullConfig(config);
        this.getDataManager().set(BULLETCONFIG, config);
        if(this.config == null) {
            this.setDead();
            return;
        }
        this.getDataManager().set(STYLE, this.config.style);
        this.getDataManager().set(TRAIL, this.config.trail);
        this.thrower = entity;
        if(world.isRemote)
            setRenderDistanceWeight(10.0D);
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
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(STYLE, 0);
        this.getDataManager().register(TRAIL, 0);
        this.getDataManager().register(BULLETCONFIG, 0);
    }

    @Override
    public void onUpdate() {

        if(config == null) config = BulletConfigSyncingUtil.pullConfig(this.getDataManager().get(BULLETCONFIG));

        if(config == null){
            this.setDead();
            return;
        }

        if(world.isRemote && config.style == BulletConfiguration.STYLE_TAU) {
            if(trailNodes.isEmpty()) {
                this.ignoreFrustumCheck = true;
                trailNodes.add(new Tuple.Pair<Vec3d, Double>(new Vec3d(-motionX * 2, -motionY * 2, -motionZ * 2), 0D));
            } else {
                trailNodes.add(new Tuple.Pair<Vec3d, Double>(new Vec3d(0, 0, 0), 1D));
            }
        }

        if(world.isRemote && this.config.blackPowder && this.ticksExisted == 1) {

            for(int i = 0; i < 15; i++) {
                double mod = rand.nextDouble();
                this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY, this.posZ,
                        (this.motionX + rand.nextGaussian() * 0.05) * mod,
                        (this.motionY + rand.nextGaussian() * 0.05) * mod,
                        (this.motionZ + rand.nextGaussian() * 0.05) * mod);
            }

            double mod = 0.5;
            this.world.spawnParticle(EnumParticleTypes.FLAME, this.posX + this.motionX * mod, this.posY + this.motionY * mod, this.posZ + this.motionZ * mod, 0, 0, 0);
        }

        if(!world.isRemote) {

            if(config.maxAge == 0) {
                if(this.config.bntUpdate != null) this.config.bntUpdate.behaveUpdate(this);
                this.setDead();
                return;
            }

            if(this.ticksExisted > config.maxAge) this.setDead();
        }

        if(this.config.bntUpdate != null) this.config.bntUpdate.behaveUpdate(this);

        this.prevPosX = posX;
        this.prevPosY = posY;
        this.prevPosZ = posZ;

        super.onUpdate();

        if(world.isRemote && !config.vPFX.isEmpty()) {

            Vec3d vec = new Vec3d(posX - prevPosX, posY - prevPosY, posZ - prevPosZ);
            double motion = Math.max(vec.length(), 0.1);
            vec = vec.normalize();

            for(double d = 0; d < motion; d += 0.5) {

                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setString("type", "vanillaExt");
                nbt.setString("mode", config.vPFX);
                nbt.setDouble("posX", this.posX - vec.x * d);
                nbt.setDouble("posY", this.posY - vec.y * d);
                nbt.setDouble("posZ", this.posZ - vec.z * d);
                MainRegistry.proxy.effectNT(nbt);
            }
        }
    }

    @Override
    protected void onImpact(RayTraceResult mop) {

        if(mop.typeOfHit == RayTraceResult.Type.BLOCK) {

            boolean hRic = rand.nextInt(100) < config.HBRC;
            boolean doesRic = config.doesRicochet && hRic;

            if(!config.isSpectral && !doesRic) {
                this.setPosition(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
                this.onBlockImpact(mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ(), mop.sideHit.getIndex());
            }

            if(doesRic) {

                Vec3d face = null;

                switch(mop.sideHit.getIndex()) {
                    case 0: face = new Vec3d(0, -1, 0); break;
                    case 1: face = new Vec3d(0, 1, 0); break;
                    case 2: face = new Vec3d(0, 0, 1); break;
                    case 3: face = new Vec3d(0, 0, -1); break;
                    case 4: face = new Vec3d(-1, 0, 0); break;
                    case 5: face = new Vec3d(1, 0, 0); break;
                }

                if(face != null) {

                    Vec3d vel = new Vec3d(motionX, motionY, motionZ);
                    vel.normalize();

                    boolean lRic = rand.nextInt(100) < config.LBRC;
                    double angle = Math.abs(BobMathUtil.getCrossAngle(vel, face) - 90);

                    if(hRic || (angle <= config.ricochetAngle && lRic)) {
                        switch(mop.sideHit.getIndex()) {
                            case 0:
                            case 1: motionY *= -1; break;
                            case 2:
                            case 3: motionZ *= -1; break;
                            case 4:
                            case 5: motionX *= -1; break;
                        }

                        if(config.plink == 1)
                            world.playSound(null, this.posX, this.posY, this.posZ, HBMSoundHandler.ricochet, SoundCategory.BLOCKS, 0.25F, 1.0F);
                        if(config.plink == 2)
                            world.playSound(null, this.posX, this.posY, this.posZ, HBMSoundHandler.grenadeBounce, SoundCategory.BLOCKS, 1.0F, 1.0F);

                        this.setPosition(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
                        onRicochet(mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ());

                        //world.setBlock((int) Math.floor(posX), (int) Math.floor(posY), (int) Math.floor(posZ), Blocks.dirt);

                    } else {
                        if(!world.isRemote) {
                            this.setPosition(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
                            onBlockImpact(mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ(), mop.sideHit.getIndex());
                        }
                    }

					/*this.posX += (mop.hitVec.xCoord - this.posX) * 0.6;
					this.posY += (mop.hitVec.yCoord - this.posY) * 0.6;
					this.posZ += (mop.hitVec.zCoord - this.posZ) * 0.6;*/

                    this.motionX *= config.bounceMod;
                    this.motionY *= config.bounceMod;
                    this.motionZ *= config.bounceMod;
                }
            }

        }

        if(mop.entityHit != null) {

            DamageSource damagesource = this.config.getDamage(this, this.thrower);
            Entity victim = mop.entityHit;

            if(!config.doesPenetrate) {
                this.setPosition(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
                onEntityImpact(victim);
            } else {
                onEntityHurt(victim);
            }

            float damage = rand.nextFloat() * (config.dmgMax - config.dmgMin) + config.dmgMin;

            if(overrideDamage != 0)
                damage = overrideDamage;

            boolean headshot = false;

            if(victim instanceof EntityLivingBase living && this.config.headshotMult > 1F) {
                double head = living.height - living.getEyeHeight();

                if(living.isEntityAlive() && mop.hitVec != null && mop.hitVec.y > (living.posY + living.height - head * 2)) {
                    damage *= this.config.headshotMult;
                    headshot = true;
                }
            }

            if(victim != null && !victim.attackEntityFrom(damagesource, damage) && victim instanceof EntityLivingBase ent) {
                    float dmg = damage + ent.lastDamage;
                    if(!victim.attackEntityFrom(damagesource, dmg)) headshot = false;

            }

            if(!world.isRemote && headshot) {
                EntityLivingBase living = (EntityLivingBase) victim;
                double head = living.height - living.getEyeHeight();
                NBTTagCompound data = new NBTTagCompound();
                data.setString("type", "vanillaburst");
                data.setInteger("count", 15);
                data.setDouble("motion", 0.1D);
                data.setString("mode", "blockdust");
                data.setInteger("block", Block.getIdFromBlock(Blocks.REDSTONE_BLOCK));
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, living.posX, living.posY + living.height - head, living.posZ), new NetworkRegistry.TargetPoint(living.dimension, living.posX, living.posY, living.posZ, 50));
                world.playSound(null, victim.posX, victim.posY, victim.posZ, SoundEvents.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, SoundCategory.PLAYERS, 1.0F, 0.95F + rand.nextFloat() * 0.2F);
            }
        }
    }

    //for when a bullet dies by hitting a block
    private void onBlockImpact(int bX, int bY, int bZ, int sideHit) {
        IBlockState state = world.getBlockState(new BlockPos(bX, bY, bZ));
        Block block = state.getBlock();

        if(config.bntImpact != null)
            config.bntImpact.behaveBlockHit(this, bX, bY, bZ, sideHit);

        if(!world.isRemote) {
            if(!config.liveAfterImpact && !config.isSpectral && bY > -1 && !this.inGround) this.setDead();
            if(!config.doesPenetrate && bY == -1) this.setDead();
        }

        if(config.incendiary > 0 && !this.world.isRemote) {
            BlockPos pos = new BlockPos((int)posX, (int)posY, (int)posZ);
            if(world.rand.nextInt(3) == 0 && world.isAirBlock(pos)) world.setBlockState(pos, Blocks.FIRE.getDefaultState());
            if(world.rand.nextInt(3) == 0 && world.isAirBlock(pos.add(1, 0, 0))) world.setBlockState(pos.add(1, 0, 0), Blocks.FIRE.getDefaultState());
            if(world.rand.nextInt(3) == 0 && world.isAirBlock(pos.add(-1, 0, 0))) world.setBlockState(pos.add(-1, 0, 0), Blocks.FIRE.getDefaultState());
            if(world.rand.nextInt(3) == 0 && world.isAirBlock(pos.add(0, 1, 0))) world.setBlockState(pos.add(0, 1, 0), Blocks.FIRE.getDefaultState());
            if(world.rand.nextInt(3) == 0 && world.isAirBlock(pos.add(0, -1, 0))) world.setBlockState(pos.add(0, -1, 0), Blocks.FIRE.getDefaultState());
            if(world.rand.nextInt(3) == 0 && world.isAirBlock(pos.add(0, 0, 1))) world.setBlockState(pos.add(0, 0, 1), Blocks.FIRE.getDefaultState());
            if(world.rand.nextInt(3) == 0 && world.isAirBlock(pos.add(0, 0, -1))) world.setBlockState(pos.add(0, 0, -1), Blocks.FIRE.getDefaultState());
        }

        if(config.emp > 0)
            ExplosionNukeGeneric.empBlast(this.world, thrower, (int)(this.posX + 0.5D), (int)(this.posY + 0.5D), (int)(this.posZ + 0.5D), config.emp);

        if(config.emp > 3) {
            if (!this.world.isRemote) {

                EntityEMPBlast cloud = new EntityEMPBlast(this.world, config.emp);
                cloud.posX = this.posX;
                cloud.posY = this.posY + 0.5F;
                cloud.posZ = this.posZ;

                this.world.spawnEntity(cloud);
            }
        }

        if(config.jolt > 0 && !world.isRemote)
            ExplosionLarge.jolt(world, thrower, posX, posY, posZ, config.jolt, 150, 0.25);

        if(config.explosive > 0 && !world.isRemote) {
            //world.newExplosion(this.thrower, posX, posY, posZ, config.explosive, config.incendiary > 0, config.blockDamage);
            ExplosionVNT vnt = new ExplosionVNT(world, posX, posY, posZ, config.explosive, this.thrower);
            vnt.setBlockAllocator(new BlockAllocatorStandard());
            if(config.blockDamage)	vnt.setBlockProcessor(new BlockProcessorStandard().withBlockEffect(config.incendiary > 0 ? new BlockMutatorFire() : null));
            else					vnt.setBlockProcessor(new BlockProcessorNoDamage().withBlockEffect(config.incendiary > 0 ? new BlockMutatorFire() : null));
            vnt.setEntityProcessor(new EntityProcessorStandard().allowSelfDamage());
            vnt.setPlayerProcessor(new PlayerProcessorStandard());
            vnt.setSFX(new ExplosionEffectStandard());
            vnt.explode();
        }

        if(config.shrapnel > 0 && !world.isRemote)
            ExplosionLarge.spawnShrapnels(world, posX, posY, posZ, config.shrapnel);

        if(config.chlorine > 0 && !world.isRemote) {
            ExplosionChaos.spawnChlorine(world, posX, posY, posZ, config.chlorine, 1.5, 0);
            world.playSound(null, (double)(posX + 0.5F), (double)(posY + 0.5F), (double)(posZ + 0.5F), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 5.0F, 2.6F + (rand.nextFloat() - rand.nextFloat()) * 0.8F);
        }

        if(config.rainbow > 0 && !world.isRemote) {
            EntityNukeExplosionMK3 ex = EntityNukeExplosionMK3.statFacFleija(world, posX, posY, posZ, config.rainbow);
            if(!ex.isDead) {
                this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 100.0f, this.world.rand.nextFloat() * 0.1F + 0.9F);
                world.spawnEntity(ex);

                EntityCloudFleijaRainbow cloud = new EntityCloudFleijaRainbow(this.world, config.rainbow);
                cloud.posX = this.posX;
                cloud.posY = this.posY;
                cloud.posZ = this.posZ;
                this.world.spawnEntity(cloud);
            }
        }

        if(config.nuke > 0 && !world.isRemote) {
            world.spawnEntity(EntityNukeExplosionMK5.statFac(world, config.nuke, posX, posY, posZ).setDetonator(thrower));
            if(MainRegistry.polaroidID == 11 || rand.nextInt(100) == 0) EntityNukeTorex.statFacBale(world, posX, posY + 0.5, posZ, config.nuke);
            else EntityNukeTorex.statFac(world, posX, posY + 0.5, posZ, config.nuke);
        }
        BlockPos pos = new BlockPos(bX, bY, bZ);
        if(config.destroysBlocks && !world.isRemote) {
            if(block.getBlockHardness(world.getBlockState(pos), world, pos) <= 120)
                world.destroyBlock(pos, false);
        } else if(config.doesBreakGlass && !world.isRemote) {
            if (state.getMaterial() == Material.GLASS && block.getExplosionResistance(null) < 0.6f) {
                world.destroyBlock(pos, false);
            } else if(block == ModBlocks.red_barrel)
                ((RedBarrel) ModBlocks.red_barrel).explode(world, pos.getX(), pos.getY(), pos.getZ());
            else if(block instanceof BlockDetonatable) {
                ((BlockDetonatable) block).onShot(world, pos);
            }
        }
    }

    //for when a bullet dies by hitting a block
    private void onRicochet(int bX, int bY, int bZ) {

        if(config.bntRicochet != null)
            config.bntRicochet.behaveBlockRicochet(this, bX, bY, bZ);
    }

    //for when a bullet dies by hitting an entity
    private void onEntityImpact(Entity e) {
        onEntityHurt(e);
        onBlockImpact(-1, -1, -1, -1);

        if(config.bntHit != null)
            config.bntHit.behaveEntityHit(this, e);

        //this.setDead();
    }

    //for when a bullet hurts an entity, not necessarily dying
    private void onEntityHurt(Entity e) {

        if(config.bntHurt != null)
            config.bntHurt.behaveEntityHurt(this, e);

        if(config.incendiary > 0 && !world.isRemote) {
            e.setFire(config.incendiary);
        }

        if(config.leadChance > 0 && !world.isRemote && world.rand.nextInt(100) < config.leadChance && e instanceof EntityLivingBase) {
            ((EntityLivingBase)e).addPotionEffect(new PotionEffect(HbmPotion.lead, 10 * 20, 0));
        }

        if(e instanceof EntityLivingBase && config.effects != null && !config.effects.isEmpty() && !world.isRemote) {

            for(PotionEffect effect : config.effects) {
                ((EntityLivingBase)e).addPotionEffect(new PotionEffect(effect));
            }
        }

        if(config.instakill && e instanceof EntityLivingBase && !world.isRemote) {

            if(!(e instanceof EntityPlayer && ((EntityPlayer)e).capabilities.isCreativeMode))
                ((EntityLivingBase)e).setHealth(0.0F);
        }

        if(config.caustic > 0 && e instanceof EntityPlayer){
            ArmorUtil.damageSuit((EntityPlayer)e, 0, config.caustic);
            ArmorUtil.damageSuit((EntityPlayer)e, 1, config.caustic);
            ArmorUtil.damageSuit((EntityPlayer)e, 2, config.caustic);
            ArmorUtil.damageSuit((EntityPlayer)e, 3, config.caustic);
        }
    }

    @Override
    public boolean doesPenetrate() {
        return this.config.doesPenetrate;
    }

    @Override
    public boolean isSpectral() {
        return this.config.isSpectral;
    }

    @Override
    public int selfDamageDelay() {
        return this.config.selfDamageDelay;
    }

    @Override
    protected double headingForceMult() {
        return 1D;
    }

    @Override
    public float getGravityVelocity() {
        return this.config.gravity;
    }

    @Override
    protected double motionMult() {
        return this.config.velocity;
    }

    @Override
    protected float getAirDrag() {
        return 1F;
    }

    @Override
    protected float getWaterDrag() {
        return 1F;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
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
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("config", this.getDataManager().get(BULLETCONFIG));
        nbt.setFloat("damage", this.overrideDamage);
    }

    public static interface IBulletHurtBehaviorNT { public void behaveEntityHurt(EntityBulletBaseNT bullet, Entity hit); }
    public static interface IBulletHitBehaviorNT { public void behaveEntityHit(EntityBulletBaseNT bullet, Entity hit); }
    public static interface IBulletRicochetBehaviorNT { public void behaveBlockRicochet(EntityBulletBaseNT bullet, int x, int y, int z); }
    public static interface IBulletImpactBehaviorNT { public void behaveBlockHit(EntityBulletBaseNT bullet, int x, int y, int z, int sideHit); }
    public static interface IBulletUpdateBehaviorNT { public void behaveUpdate(EntityBulletBaseNT bullet); }
}
