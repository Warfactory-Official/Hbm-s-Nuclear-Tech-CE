package com.hbm.items.weapon.grenade;

import com.hbm.entity.effect.EntityCloudFleija;
import com.hbm.entity.effect.EntityFireLingering;
import com.hbm.entity.grenade.EntityGrenadeUniversal;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.entity.projectile.EntityBulletBeamBase;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockMutatorFire;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectTiny;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.ItemEnumMulti;
import com.hbm.items.weapon.grenade.ItemGrenadeShell.EnumGrenadeShell;
import static com.hbm.items.weapon.grenade.ItemGrenadeShell.EnumGrenadeShell.*;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.Lego;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.util.DamageResistanceHandler.DamageClass;
import com.hbm.util.Vec3NT;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ItemGrenadeFilling extends ItemEnumMulti<ItemGrenadeFilling.EnumGrenadeFilling> {

    public static BulletConfig fragmentation;
    public static BulletConfig pellets;
    public static BulletConfig pellets_heavy;
    public static BulletConfig laser;

    public ItemGrenadeFilling(String registryName) {
        super(registryName, EnumGrenadeFilling.VALUES, true, true);
        fragmentation = new BulletConfig().setLife(3).setThresholdNegation(5F).setRicochetAngle(90).setRicochetCount(2);
        pellets = new BulletConfig().setLife(100).setGrav(0.04F).setVel(1.5F).setOnImpact(LAMBDA_TINY_EXPLODE);
        pellets_heavy = new BulletConfig().setLife(100).setGrav(0.04F).setVel(1.5F).setOnImpact(LAMBDA_EXPLODE);
        laser = new BulletConfig().setBeam().setupDamageClass(DamageClass.LASER).setLife(3).setRenderRotations(false).setThresholdNegation(10F).setOnBeamImpact(BulletConfig.LAMBDA_STANDARD_BEAM_HIT);
    }

    public enum EnumGrenadeFilling {
        POWDER(EXPLODE_POWDER,              0x424242, 0x939176, FRAG, STICK),  // gunpowder
        HE(EXPLODE_HE,                      0x595533, 0xA49D62, FRAG, STICK),  // high explosive
        DEMO(EXPLODE_DEMO,                  0x595533, 0xDD4029, FRAG, STICK),  // demolition
        INC(EXPLODE_INC,                    0x5A5A5A, 0xFF5F21, FRAG, STICK),  // incendiary
        WP(EXPLODE_WP,                      0xDCDCDC, 0xFF5F21, FRAG, STICK),  // white phosphorus
        CLUSTER(EXPLODE_CLUSTER,            0x5A5A5A, 0xFFC711, FRAG, STICK),  // explosive pellets
        EMP(EXPLODE_EMP,                    0x93A1AC, 0x00FFFF, TECH),         // tesla
        PLASMA(EXPLODE_PLASMA,              0x655B2C, 0x4CFF00, TECH),         // EMP but more oomph
        LASER(EXPLODE_LASER,                0x493A3A, 0xFF0000, TECH),         // pew pew pew
        CLUSTER_HEAVY(EXPLODE_CLUSTER_HEAVY,0x5A5A5A, 0xFF5F21, NUKE),         // cluster but fat
        NUCLEAR(EXPLODE_NUKE,               0xDFD7A8, 0xA49D62, NUKE),         // nuka grenade
        NUCLEAR_DEMO(EXPLODE_NUKE_DEMO,     0xDFD7A8, 0xDD4029, NUKE),         // demolition nuka grenade
        SCHRAB(EXPLODE_SCHRAB,              0x00BDBD, 0x000000, NUKE);         // what used to be aschrab

        public static final EnumGrenadeFilling[] VALUES = values();

        public final Consumer<EntityGrenadeUniversal> explode;
        public final Set<EnumGrenadeShell> compatibleShells = new HashSet<>();
        public final int bodyColor;
        public final int labelColor;

        EnumGrenadeFilling(Consumer<EntityGrenadeUniversal> explode, int bodyColor, int labelColor, EnumGrenadeShell... compatibleShells) {
            this.explode = explode;
            Collections.addAll(this.compatibleShells, compatibleShells);
            this.bodyColor = bodyColor;
            this.labelColor = labelColor;
        }
    }

    public static final Consumer<EntityGrenadeUniversal> EXPLODE_POWDER = grenade -> standardExplode(grenade, 5F, 10F, 5F, 0F);

    public static final Consumer<EntityGrenadeUniversal> EXPLODE_HE = grenade -> standardExplode(grenade, 7.5F, 25F, 10F, 0.1F);

    public static final Consumer<EntityGrenadeUniversal> EXPLODE_CLUSTER = grenade -> {
        standardExplode(grenade, 7.5F, 15F, 10F, 0.1F);
        int frags = 30;
        if (grenade.getShell() == FRAG) frags *= 1.25;
        for (int i = 0; i < frags; i++) {
            EntityBulletBaseMK4 bullet = new EntityBulletBaseMK4(
                    grenade.world, pellets, 15F, 0F,
                    grenade.world.rand.nextFloat() * 2F * (float) Math.PI,
                    (grenade.world.rand.nextFloat() * 0.5F + 0.5F) * (float) Math.PI);
            bullet.setPosition(grenade.posX, grenade.posY + 0.05, grenade.posZ);
            bullet.motionX *= 0.5;
            bullet.motionY *= 0.75;
            bullet.motionZ *= 0.5;
            grenade.world.spawnEntity(bullet);
        }
    };

    public static final Consumer<EntityGrenadeUniversal> EXPLODE_CLUSTER_HEAVY = grenade -> {
        standardExplode(grenade, 7.5F, 15F, 10F, 0.1F);
        int frags = 15;
        for (int i = 0; i < frags; i++) {
            EntityBulletBaseMK4 bullet = new EntityBulletBaseMK4(
                    grenade.world, pellets_heavy, 30F, 0F,
                    grenade.world.rand.nextFloat() * 2F * (float) Math.PI,
                    (grenade.world.rand.nextFloat() * 0.5F + 0.5F) * (float) Math.PI);
            bullet.setPosition(grenade.posX, grenade.posY + 0.05, grenade.posZ);
            bullet.motionX *= 0.5;
            bullet.motionY *= 1.25;
            bullet.motionZ *= 0.5;
            grenade.world.spawnEntity(bullet);
        }
    };

    public static final Consumer<EntityGrenadeUniversal> EXPLODE_DEMO = grenade -> {
        ExplosionVNT vnt = new ExplosionVNT(grenade.world, grenade.posX, grenade.posY, grenade.posZ, 5F, grenade.getThrower());
        vnt.setBlockAllocator(new BlockAllocatorStandard());
        vnt.setBlockProcessor(new BlockProcessorStandard());
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, 10F));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F));
        vnt.explode();
    };

    public static final Consumer<EntityGrenadeUniversal> EXPLODE_INC = grenade -> {
        World world = grenade.world;
        standardExplode(grenade, 3F, 10F);
        EntityFireLingering fire = new EntityFireLingering(world).setArea(6, 2).setDuration(200).setType(EntityFireLingering.TYPE_DIESEL);
        fire.setPosition(grenade.posX, grenade.posY, grenade.posZ);
        world.spawnEntity(fire);
        igniteAround(world, grenade.posX, grenade.posY, grenade.posZ, 2);
    };

    public static final Consumer<EntityGrenadeUniversal> EXPLODE_WP = grenade -> {
        World world = grenade.world;
        standardExplode(grenade, 3F, 10F);
        EntityFireLingering fire = new EntityFireLingering(world).setArea(6, 2).setDuration(600).setType(EntityFireLingering.TYPE_PHOSPHORUS);
        fire.setPosition(grenade.posX, grenade.posY, grenade.posZ);
        world.spawnEntity(fire);
        igniteAround(world, grenade.posX, grenade.posY, grenade.posZ, 3);
        for (int i = 0; i < 3; i++) {
            NBTTagCompound haze = new NBTTagCompound();
            haze.setString("type", "haze");
            PacketThreading.createAllAroundThreadedPacket(
                    new AuxParticlePacketNT(haze,
                            grenade.posX + world.rand.nextGaussian() * 4,
                            grenade.posY,
                            grenade.posZ + world.rand.nextGaussian() * 4),
                    new TargetPoint(grenade.dimension, grenade.posX, grenade.posY, grenade.posZ, 150));
        }
    };

    public static final Consumer<EntityGrenadeUniversal> EXPLODE_EMP = grenade ->
            explodeStandardEnergy(grenade, 30F, 3F, DamageClass.ELECTRIC, 0.5F, 0.5F, 1F, 3F);

    public static final Consumer<EntityGrenadeUniversal> EXPLODE_PLASMA = grenade ->
            explodeStandardEnergy(grenade, 50F, 5F, DamageClass.PLASMA, 0.5F, 1F, 0.5F, 4F); // TODO: unique effect because this sucks

    public static final Consumer<EntityGrenadeUniversal> EXPLODE_LASER = grenade -> {
        tinyExplode(grenade, 2, 5F);

        double x = grenade.posX;
        double y = grenade.posY + 0.125;
        double z = grenade.posZ;

        double range = 15;
        List<EntityLivingBase> potentialTargets = grenade.world.getEntitiesWithinAABB(
                EntityLivingBase.class,
                new AxisAlignedBB(x, y, z, x, y, z).grow(range, range, range));
        Collections.shuffle(potentialTargets);

        for (EntityLivingBase target : potentialTargets) {
            if (target == grenade.getThrower()) continue;

            Vec3d delta = new Vec3d(target.posX - x, target.posY + target.height / 2 - y, target.posZ - z);
            if (delta.length() > range) continue;
            EntityBulletBeamBase sub = new EntityBulletBeamBase(grenade.world, laser, 30);
            sub.thrower = grenade.getThrower();
            sub.setPosition(x, y, z);
            sub.setRotationsFromVector(new Vec3NT(delta));
            sub.performHitscanExternal(delta.length());
            grenade.world.spawnEntity(sub);
        }
    };

    public static final Consumer<EntityGrenadeUniversal> EXPLODE_NUKE = grenade -> {
        ExplosionVNT vnt = new ExplosionVNT(grenade.world, grenade.posX, grenade.posY, grenade.posZ, 10);
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(2, 100).withRangeMod(1.5F));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.explode();

        incrementRad(grenade.world, grenade.posX, grenade.posY, grenade.posZ, 1F);
        spawnMush(grenade);
    };

    public static final Consumer<EntityGrenadeUniversal> EXPLODE_NUKE_DEMO = grenade -> {
        ExplosionVNT vnt = new ExplosionVNT(grenade.world, grenade.posX, grenade.posY, grenade.posZ, 10);
        vnt.setBlockAllocator(new BlockAllocatorStandard(64));
        vnt.setBlockProcessor(new BlockProcessorStandard().withBlockEffect(new BlockMutatorFire()));
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(2, 50).withRangeMod(1.5F));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.explode();

        incrementRad(grenade.world, grenade.posX, grenade.posY, grenade.posZ, 1.5F);
        spawnMush(grenade);
    };

    public static final Consumer<EntityGrenadeUniversal> EXPLODE_SCHRAB = grenade -> {
        EntityNukeExplosionMK3 ex = EntityNukeExplosionMK3.statFacFleija(grenade.world, grenade.posX, grenade.posY, grenade.posZ, 50);
        if (!ex.isDead) {
            grenade.world.playSound(null, grenade.posX, grenade.posY, grenade.posZ,
                    SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 100.0F,
                    grenade.world.rand.nextFloat() * 0.1F + 0.9F);
            grenade.world.spawnEntity(ex);
            EntityCloudFleija cloud = new EntityCloudFleija(grenade.world, 50);
            cloud.setPosition(grenade.posX, grenade.posY, grenade.posZ);
            grenade.world.spawnEntity(cloud);
        }
    };

    private static void igniteAround(World world, double px, double py, double pz, int radius) {
        for (int dx = -radius; dx <= radius; dx++)
            for (int dy = -radius; dy <= radius; dy++)
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos bp = new BlockPos((int) Math.floor(px) + dx, (int) Math.floor(py) + dy, (int) Math.floor(pz) + dz);
                    if (!world.isAirBlock(bp)) continue;
                    for (EnumFacing dir : EnumFacing.values()) {
                        BlockPos np = bp.offset(dir);
                        if (world.getBlockState(np).getBlock().isFlammable(world, np, dir.getOpposite())) {
                            world.setBlockState(bp, Blocks.FIRE.getDefaultState());
                            break;
                        }
                    }
                }
    }

    public static void incrementRad(World world, double posX, double posY, double posZ, float mult) {
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                if (Math.abs(i) + Math.abs(j) < 4) {
                    ChunkRadiationManager.proxy.incrementRad(world,
                            new BlockPos((int) Math.floor(posX + i * 16), (int) Math.floor(posY), (int) Math.floor(posZ + j * 16)),
                            50F / (Math.abs(i) + Math.abs(j) + 1) * mult);
                }
            }
        }
    }

    public static void spawnMush(EntityGrenadeUniversal grenade) {
        grenade.world.playSound(null, grenade.posX, grenade.posY, grenade.posZ,
                SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 15.0F, 1.0F);
        NBTTagCompound data = new NBTTagCompound();
        data.setString("type", "muke");
        data.setBoolean("balefire", MainRegistry.polaroidID == 11 || grenade.world.rand.nextInt(100) == 0);
        PacketThreading.createAllAroundThreadedPacket(
                new AuxParticlePacketNT(data, grenade.posX, grenade.posY + 0.5, grenade.posZ),
                new TargetPoint(grenade.dimension, grenade.posX, grenade.posY, grenade.posZ, 250));
    }

    public static void explodeStandardEnergy(EntityGrenadeUniversal grenade, float damage, float range, DamageClass damageClass, float r, float g, float b, float scale) {
        ExplosionVNT vnt = new ExplosionVNT(grenade.world, grenade.posX, grenade.posY, grenade.posZ, range, grenade.getThrower());
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, damage).setDamageClass(damageClass));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.explode();
        grenade.world.playSound(null, grenade.posX, grenade.posY, grenade.posZ,
                HBMSoundHandler.ufoBlast, SoundCategory.HOSTILE, 5.0F,
                0.9F + grenade.world.rand.nextFloat() * 0.2F);
        grenade.world.playSound(null, grenade.posX, grenade.posY, grenade.posZ,
                SoundEvents.ENTITY_FIREWORK_BLAST, SoundCategory.HOSTILE, 5.0F, 0.5F);

        float yaw = grenade.world.rand.nextFloat() * 180F;
        for (int i = 0; i < 3; i++) {
            NBTTagCompound data = new NBTTagCompound();
            data.setString("type", "plasmablast");
            data.setFloat("r", r);
            data.setFloat("g", g);
            data.setFloat("b", b);
            data.setFloat("pitch", -60F + 60F * i);
            data.setFloat("yaw", yaw);
            data.setFloat("scale", scale);
            PacketThreading.createAllAroundThreadedPacket(
                    new AuxParticlePacketNT(data, grenade.posX, grenade.posY + 0.125, grenade.posZ),
                    new TargetPoint(grenade.dimension, grenade.posX, grenade.posY, grenade.posZ, 100));
        }
    }

    public static final BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_TINY_EXPLODE = (bullet, mop) -> {
        if (bullet.ticksExisted < 2) return;
        Lego.tinyExplode(bullet, mop, 1.5F);
        bullet.setDead();
    };

    public static final BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_EXPLODE = (bullet, mop) -> {
        if (bullet.ticksExisted < 2) return;
        Lego.standardExplode(bullet, mop, 5F);
        bullet.setDead();
    };

    public static void standardExplode(EntityGrenadeUniversal grenade, float range, float damage) { standardExplode(grenade, range, damage, 0F, 0F); }
    public static void standardExplode(EntityGrenadeUniversal grenade, float range, float damage, float dt, float dr) {
        ExplosionVNT vnt = new ExplosionVNT(grenade.world, grenade.posX, grenade.posY, grenade.posZ, range, grenade.getThrower());
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, damage).setupPiercing(dt, dr));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F));
        vnt.explode();
    }

    public static void tinyExplode(EntityGrenadeUniversal grenade, float range, float damage) { tinyExplode(grenade, range, damage, 0F, 0F); }
    public static void tinyExplode(EntityGrenadeUniversal grenade, float range, float damage, float dt, float dr) {
        ExplosionVNT vnt = new ExplosionVNT(grenade.world, grenade.posX, grenade.posY, grenade.posZ, range, grenade.getThrower());
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(0.5, damage).setupPiercing(dt, dr).setKnockback(0.25D));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.setSFX(new ExplosionEffectTiny());
        vnt.explode();
    }

    public static void standardFragmentation(EntityGrenadeUniversal grenade, float frags) {
        if (grenade.getShell() == FRAG) frags *= 1.5;
        for (int i = 0; i < frags; i++) {
            EntityBulletBaseMK4 bullet = new EntityBulletBaseMK4(
                    grenade.world, fragmentation, 10F, 0F,
                    grenade.world.rand.nextFloat() * 2F * (float) Math.PI,
                    (grenade.world.rand.nextFloat() - 0.5F) * 2F * (float) Math.PI);
            bullet.setPosition(grenade.posX, grenade.posY + 0.05, grenade.posZ);
            grenade.world.spawnEntity(bullet);
        }
    }
}
