package com.hbm.items.weapon.sedna.factory;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockDetonatable;
import com.hbm.blocks.generic.BlockMeta;
import com.hbm.capability.HbmLivingCapability;
import com.hbm.capability.HbmLivingProps;
import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.entity.projectile.EntityDuchessGambit;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.mags.MagazineBelt;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.items.weapon.sedna.mags.MagazineSingleReload;
import com.hbm.items.weapon.sedna.mods.WeaponModManager;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.SpentCasing;
import com.hbm.render.anim.sedna.BusAnimationKeyframeSedna.IType;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.BusAnimationSequenceSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.render.misc.RenderScreenOverlay.Crosshair;
import com.hbm.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class XFactory12ga {

    public static BulletConfig g12_bp;
    public static BulletConfig g12_bp_magnum;
    public static BulletConfig g12_bp_slug;
    public static BulletConfig g12;
    public static BulletConfig g12_slug;
    public static BulletConfig g12_flechette;
    public static BulletConfig g12_magnum;
    public static BulletConfig g12_explosive;
    public static BulletConfig g12_phosphorus;
    public static BulletConfig g12_anthrax;
    public static BulletConfig g12_equestrian_bj;
    public static BulletConfig g12_equestrian_tkr;

    public static BulletConfig g12_shredder;
    public static BulletConfig g12_shredder_slug;
    public static BulletConfig g12_shredder_flechette;
    public static BulletConfig g12_shredder_magnum;
    public static BulletConfig g12_shredder_explosive;
    public static BulletConfig g12_shredder_phosphorus;

    public static BulletConfig g12_sub;
    public static BulletConfig g12_sub_slug;
    public static BulletConfig g12_sub_flechette;
    public static BulletConfig g12_sub_magnum;
    public static BulletConfig g12_sub_explosive;
    public static BulletConfig g12_sub_phosphorus;

    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_STANDARD_EXPLODE = (bullet, mop) -> {
        Lego.standardExplode(bullet, mop, 2F); bullet.setDead();
    };

    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_BOAT = (bullet, mop) -> {
        EntityDuchessGambit pippo = new EntityDuchessGambit(bullet.world);
        pippo.posX = mop.hitVec.x;
        pippo.posY = mop.hitVec.y + 50;
        pippo.posZ = mop.hitVec.z;
        bullet.world.spawnEntity(pippo);
        bullet.world.playSound(null, pippo.posX, pippo.posY + 50, pippo.posZ, HBMSoundHandler.boatWeapon, SoundCategory.PLAYERS, 100F, 1F);
        bullet.setDead();
    };

    public static BulletConfig makeShredderConfig(BulletConfig original, BulletConfig submunition) {
        BulletConfig cfg = new BulletConfig().setBeam().setRenderRotations(false).setLife(5).setDamage(original.damageMult * original.projectilesMax).setupDamageClass(DamageResistanceHandler.DamageClass.LASER);
        cfg.setItem(original.ammo);
        cfg.setCasing(original.casing);
        cfg.setOnBeamImpact((beam, mop) -> {

            int projectiles = submunition.projectilesMin;
            if(submunition.projectilesMax > submunition.projectilesMin) projectiles += beam.world.rand.nextInt(submunition.projectilesMax - submunition.projectilesMin + 1);

            if(mop.typeOfHit == mop.typeOfHit.BLOCK) {

                ForgeDirection dir = ForgeDirection.getOrientation(mop.sideHit);

                mop.hitVec.add(dir.offsetX * 0.1, dir.offsetY * 0.1, dir.offsetZ * 0.1);

                spawnPulse(beam.world, mop, beam.rotationYaw, beam.rotationPitch);

                List<Entity> blast = beam.world.getEntitiesWithinAABBExcludingEntity(beam, new AxisAlignedBB(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z).expand(0.75, 0.75, 0.75));
                DamageSource source = BulletConfig.getDamage(beam, beam.getThrower(), DamageResistanceHandler.DamageClass.LASER);

                for(Entity e : blast) {
                    if(!e.isEntityAlive()) continue;
                    if(e instanceof EntityLivingBase) {
                        EntityDamageUtil.attackEntityFromNT((EntityLivingBase) e, source, beam.damage, true, false, 0D, 0F, 0F);
                        if(!e.isEntityAlive()) ConfettiUtil.decideConfetti((EntityLivingBase) e, source);
                    } else {
                        e.attackEntityFrom(source, beam.damage);
                    }
                }

                for(int i = 0; i < projectiles; i++) {
                    EntityBulletBaseMK4 bullet = new EntityBulletBaseMK4(beam.world, beam.thrower, submunition, beam.damage * submunition.damageMult, 0.2F, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, dir.offsetX, dir.offsetY, dir.offsetZ);
                    bullet.world.spawnEntity(bullet);
                }
            }

            if(mop.typeOfHit == mop.typeOfHit.ENTITY) {

                spawnPulse(beam.world, mop, beam.rotationYaw, beam.rotationPitch);

                for(int i = 0; i < projectiles; i++) {
                    Vec3NT vec = new Vec3NT(beam.world.rand.nextGaussian(), beam.world.rand.nextGaussian(), beam.world.rand.nextGaussian()).normalizeSelf();
                    EntityBulletBaseMK4 bullet = new EntityBulletBaseMK4(beam.world, beam.thrower, submunition, beam.damage * submunition.damageMult, 0.2F, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, vec.x, vec.y, vec.z);
                    bullet.world.spawnEntity(bullet);
                }
            }
        });
        return cfg;
    }

    public static BulletConfig makeShredderSubmunition(BulletConfig original) {
        BulletConfig cfg = original.clone();
        cfg.setRicochetAngle(90).setRicochetCount(3).setVel(0.5F).setLife(50).setupDamageClass(DamageResistanceHandler.DamageClass.LASER).setOnRicochet(LAMBDA_SHREDDER_RICOCHET);
        return cfg;
    }

    //this sucks
    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_SHREDDER_RICOCHET = (bullet, mop) -> {

        if(mop.typeOfHit == mop.typeOfHit.BLOCK) {
            IBlockState bs = bullet.world.getBlockState(mop.getBlockPos());
            Block b = bs.getBlock();
            if(b.getMaterial(bs) == Material.GLASS) {
                bullet.world.destroyBlock(mop.getBlockPos(), false);
                bullet.setPosition(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
                return;
            }
            if(b instanceof BlockDetonatable detonatable) {
                detonatable.onShot(bullet.world, mop.getBlockPos());
            }
            if (b == ModBlocks.deco_crt) {
                bullet.world.setBlockState(mop.getBlockPos(), bs.withProperty(BlockMeta.META, b.getMetaFromState(bs) % 4 + 4));
            }
            ForgeDirection dir = ForgeDirection.getOrientation(mop.sideHit);
            Vec3d face = new Vec3d(dir.offsetX, dir.offsetY, dir.offsetZ);
            Vec3d vel = new Vec3d(bullet.motionX, bullet.motionY, bullet.motionZ).normalize();

            double angle = Math.abs(BobMathUtil.getCrossAngle(vel, face) - 90);

            if(angle <= bullet.config.ricochetAngle) {

                spawnPulse(bullet.world, mop, bullet.rotationYaw, bullet.rotationPitch);

                List<Entity> blast = bullet.world.getEntitiesWithinAABBExcludingEntity(bullet, new AxisAlignedBB(bullet.posX, bullet.posY, bullet.posZ, bullet.posX, bullet.posY, bullet.posZ).expand(0.5, 0.5, 0.5));
                DamageSource source = BulletConfig.getDamage(bullet, bullet.getThrower(), DamageResistanceHandler.DamageClass.LASER);

                for(Entity e : blast) {
                    if(!e.isEntityAlive()) continue;
                    if(e instanceof EntityLivingBase) {
                        EntityDamageUtil.attackEntityFromNT((EntityLivingBase) e, source, bullet.damage, true, false, 0D, 0F, 0F);
                        if(!e.isEntityAlive()) ConfettiUtil.decideConfetti((EntityLivingBase) e, source);
                    } else {
                        e.attackEntityFrom(source, bullet.damage);
                    }
                }

                bullet.ricochets++;
                if(bullet.ricochets > bullet.config.maxRicochetCount) {
                    bullet.setPosition(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
                    bullet.setDead();
                }

                switch (mop.sideHit.getIndex()) {
                    case 0, 1 -> bullet.motionY *= -1;
                    case 2, 3 -> bullet.motionZ *= -1;
                    case 4, 5 -> bullet.motionX *= -1;
                }
                bullet.setPosition(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
                //send a teleport so the ricochet is more accurate instead of the interp smoothing fucking everything up
                if(bullet.world instanceof WorldServer) TrackerUtil.sendTeleport(bullet.world, bullet);

            } else {
                bullet.setPosition(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
                bullet.setDead();
            }
        }
    };

    public static void spawnPulse(World world, RayTraceResult mop, float yaw, float pitch) {

        double x = mop.hitVec.x;
        double y = mop.hitVec.y;
        double z = mop.hitVec.z;

        if(mop.typeOfHit == mop.typeOfHit.BLOCK) {
            if(mop.sideHit.getIndex() == ForgeDirection.UP.ordinal()) { yaw = 0F; pitch = 0F; }
            if(mop.sideHit.getIndex() == ForgeDirection.DOWN.ordinal()) { yaw = 0F; pitch = 0F; }
            if(mop.sideHit.getIndex() == ForgeDirection.NORTH.ordinal()) { yaw = 0F; pitch = 90F; }
            if(mop.sideHit.getIndex() == ForgeDirection.SOUTH.ordinal()) { yaw = 180F; pitch = 90F; }
            if(mop.sideHit.getIndex() == ForgeDirection.EAST.ordinal()) { yaw = 90F; pitch = 90F; }
            if(mop.sideHit.getIndex() == ForgeDirection.WEST.ordinal()) { yaw = 270F; pitch = 90F; }

            ForgeDirection dir = ForgeDirection.getOrientation(mop.sideHit);

            x += dir.offsetX * 0.05;
            y += dir.offsetY * 0.05;
            z += dir.offsetZ * 0.05;
        }

        NBTTagCompound data = new NBTTagCompound();
        data.setString("type", "plasmablast");
        data.setFloat("r", 0.5F);
        data.setFloat("g", 0.5F);
        data.setFloat("b", 1.0F);
        data.setFloat("pitch", pitch);
        data.setFloat("yaw", yaw);
        data.setFloat("scale", 0.75F);
        PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z), new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, 100));
    }

    public static void init() {

        float buckshotSpread = 0.035F;
        float magnumSpread = 0.015F;
        g12_bp = new BulletConfig().setItem(GunFactory.EnumAmmo.G12_BP).setCasing(ItemEnums.EnumCasingType.SHOTSHELL, 12).setBlackPowder(true).setProjectiles(8).setDamage(0.75F/8F).setSpread(buckshotSpread).setRicochetAngle(15).setCasing(new SpentCasing(SpentCasing.CasingType.SHOTGUN).setColor(SpentCasing.COLOR_CASE_BRASS, SpentCasing.COLOR_CASE_BRASS).setScale(0.75F).register("12GA_BP"));
        g12_bp_magnum = new BulletConfig().setItem(GunFactory.EnumAmmo.G12_BP_MAGNUM).setCasing(ItemEnums.EnumCasingType.SHOTSHELL, 12).setBlackPowder(true).setProjectiles(4).setDamage(0.75F/4F).setSpread(buckshotSpread).setRicochetAngle(25).setCasing(new SpentCasing(SpentCasing.CasingType.SHOTGUN).setColor(SpentCasing.COLOR_CASE_BRASS, SpentCasing.COLOR_CASE_BRASS).setScale(0.75F).register("12GA_BP_MAGNUM"));
        g12_bp_slug = new BulletConfig().setItem(GunFactory.EnumAmmo.G12_BP_SLUG).setCasing(ItemEnums.EnumCasingType.SHOTSHELL, 12).setBlackPowder(true).setDamage(0.75F).setSpread(0.01F).setRicochetAngle(5).setCasing(new SpentCasing(SpentCasing.CasingType.SHOTGUN).setColor(SpentCasing.COLOR_CASE_BRASS, SpentCasing.COLOR_CASE_BRASS).setScale(0.75F).register("12GA_BP_SLUG"));
        g12 = new BulletConfig().setItem(GunFactory.EnumAmmo.G12).setCasing(ItemEnums.EnumCasingType.BUCKSHOT, 6).setProjectiles(8).setDamage(1F/8F).setSpread(buckshotSpread).setRicochetAngle(15).setThresholdNegation(2F).setCasing(new SpentCasing(SpentCasing.CasingType.SHOTGUN).setColor(0xB52B2B, SpentCasing.COLOR_CASE_BRASS).setScale(0.75F).register("12GA"));
        g12_slug = new BulletConfig().setItem(GunFactory.EnumAmmo.G12_SLUG).setCasing(ItemEnums.EnumCasingType.BUCKSHOT, 6).setHeadshot(1.5F).setSpread(0.0F).setRicochetAngle(25).setThresholdNegation(4F).setArmorPiercing(0.15F).setCasing(new SpentCasing(SpentCasing.CasingType.SHOTGUN).setColor(0x393939, SpentCasing.COLOR_CASE_BRASS).setScale(0.75F).register("12GA_SLUG"));
        g12_flechette = new BulletConfig().setItem(GunFactory.EnumAmmo.G12_FLECHETTE).setCasing(ItemEnums.EnumCasingType.BUCKSHOT, 6).setProjectiles(8).setDamage(1F/8F).setThresholdNegation(5F).setThresholdNegation(3F).setArmorPiercing(0.2F).setSpread(0.025F).setRicochetAngle(5).setCasing(new SpentCasing(SpentCasing.CasingType.SHOTGUN).setColor(0x3C80F0, SpentCasing.COLOR_CASE_BRASS).setScale(0.75F).register("12GA_FLECHETTE"));
        g12_magnum = new BulletConfig().setItem(GunFactory.EnumAmmo.G12_MAGNUM).setCasing(ItemEnums.EnumCasingType.BUCKSHOT_ADVANCED, 6).setProjectiles(4).setDamage(2F/4F).setSpread(magnumSpread).setRicochetAngle(15).setThresholdNegation(4F).setCasing(new SpentCasing(SpentCasing.CasingType.SHOTGUN).setColor(0x278400, SpentCasing.COLOR_CASE_12GA).setScale(0.75F).register("12GA_MAGNUM"));
        g12_explosive = new BulletConfig().setItem(GunFactory.EnumAmmo.G12_EXPLOSIVE).setCasing(ItemEnums.EnumCasingType.BUCKSHOT_ADVANCED, 6).setDamage(2.5F).setOnImpact(LAMBDA_STANDARD_EXPLODE).setSpread(0F).setRicochetAngle(15).setCasing(new SpentCasing(SpentCasing.CasingType.SHOTGUN).setColor(0xDA4127, SpentCasing.COLOR_CASE_12GA).setScale(0.75F).register("12GA_EXPLOSIVE"));
        g12_phosphorus = new BulletConfig().setItem(GunFactory.EnumAmmo.G12_PHOSPHORUS).setCasing(ItemEnums.EnumCasingType.BUCKSHOT_ADVANCED, 6).setProjectiles(8).setDamage(1F/8F).setSpread(magnumSpread).setRicochetAngle(15).setCasing(new SpentCasing(SpentCasing.CasingType.SHOTGUN).setColor(0x910001, SpentCasing.COLOR_CASE_12GA).setScale(0.75F).register("12GA_PHOSPHORUS"))
                .setOnImpact((bullet, mop) -> { if(mop.entityHit instanceof EntityLivingBase) { HbmLivingCapability.IEntityHbmProps data = HbmLivingProps.getData((EntityLivingBase) mop.entityHit); if(data.getPhosphorus() < 300) data.setPhosphorus(300); } });
        //g12_anthrax = new BulletConfig().setItem(EnumAmmo.G12_ANTHRAX).setProjectiles(8).setDamage(1F/8F).setSpread(0.015F).setRicochetAngle(15).setCasing(new SpentCasing(CasingType.SHOTGUN).setColor(0x749300, SpentCasing.COLOR_CASE_12GA).setScale(0.75F).register("12GA_ANTHRAX"));
        g12_equestrian_bj = new BulletConfig().setItem(GunFactory.EnumAmmoSecret.G12_EQUESTRIAN).setDamage(0F).setOnImpact(LAMBDA_BOAT).setCasing(new SpentCasing(SpentCasing.CasingType.SHOTGUN).setColor(SpentCasing.COLOR_CASE_EQUESTRIAN, SpentCasing.COLOR_CASE_12GA).setScale(0.75F).register("12gaEquestrianBJ"));
        g12_equestrian_tkr = new BulletConfig().setItem(GunFactory.EnumAmmoSecret.G12_EQUESTRIAN).setDamage(0F).setCasing(new SpentCasing(SpentCasing.CasingType.SHOTGUN).setColor(SpentCasing.COLOR_CASE_EQUESTRIAN, SpentCasing.COLOR_CASE_12GA).setScale(0.75F).register("12gaEquestrianTKR"));

        BulletConfig[] all = new BulletConfig[] {g12_bp, g12_bp_magnum, g12_bp_slug, g12, g12_slug, g12_flechette, g12_magnum, g12_explosive, g12_phosphorus};

        g12_sub =					makeShredderSubmunition(g12);
        g12_sub_slug =				makeShredderSubmunition(g12_slug);
        g12_sub_flechette =			makeShredderSubmunition(g12_flechette);
        g12_sub_magnum =			makeShredderSubmunition(g12_magnum);
        g12_sub_explosive =			makeShredderSubmunition(g12_explosive);
        g12_sub_phosphorus =		makeShredderSubmunition(g12_phosphorus);
        g12_shredder =				makeShredderConfig(g12, g12_sub);
        g12_shredder_slug =			makeShredderConfig(g12_slug, g12_sub_slug);
        g12_shredder_flechette =	makeShredderConfig(g12_flechette, g12_sub_flechette);
        g12_shredder_magnum =		makeShredderConfig(g12_magnum, g12_sub_magnum);
        g12_shredder_explosive =	makeShredderConfig(g12_explosive, g12_sub_explosive);
        g12_shredder_phosphorus =	makeShredderConfig(g12_phosphorus, g12_sub_phosphorus);

        ModItems.gun_maresleg = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.A_SIDE, "gun_maresleg", new GunConfig()
                .dura(600).draw(10).inspect(39).reloadSequential(true).crosshair(Crosshair.L_CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(16F).delay(20).reload(22, 10, 13, 0).jam(24).sound(HBMSoundHandler.fireShotgun, 1.0F, 1.0F)
                        .mag(new MagazineSingleReload(0, 6).addConfigs(all))
                        .offset(0.75, -0.0625, -0.1875)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_MARESLEG))
                .setupStandardConfiguration()
                .anim(LAMBDA_MARESLEG_ANIMS).orchestra(Orchestras.ORCHESTRA_MARESLEG)
        ).setNameMutator(LAMBDA_NAME_MARESLEG);
        ModItems.gun_maresleg_akimbo = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.B_SIDE, "gun_maresleg_akimbo",
                new GunConfig().dura(600).draw(5).inspect(39).reloadSequential(true).crosshair(Crosshair.L_CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                        .rec(new Receiver(0)
                                .dmg(16F).spreadHipfire(0F).spreadAmmo(1.35F).delay(20).reload(22, 10, 13, 0).jam(24).sound(HBMSoundHandler.fireShotgun, 1.0F, 1.0F)
                                .mag(new MagazineSingleReload(0, 6).addConfigs(all))
                                .offset(0.75, -0.0625, 0.1875D)
                                .setupStandardFire().recoil(LAMBDA_RECOIL_MARESLEG))
                        .pp(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).pr(Lego.LAMBDA_STANDARD_RELOAD)
                        .decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
                        .anim(LAMBDA_MARESLEG_SHORT_ANIMS).orchestra(Orchestras.ORCHESTRA_MARESLEG_AKIMBO),
                new GunConfig().dura(600).draw(5).inspect(39).reloadSequential(true).crosshair(Crosshair.L_CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                        .rec(new Receiver(0)
                                .dmg(16F).spreadHipfire(0F).spreadAmmo(1.35F).delay(20).reload(22, 10, 13, 0).jam(24).sound(HBMSoundHandler.fireShotgun, 1.0F, 1.0F)
                                .mag(new MagazineSingleReload(1, 6).addConfigs(all))
                                .offset(0.75, -0.0625, -0.1875)
                                .setupStandardFire().recoil(LAMBDA_RECOIL_MARESLEG))
                        .ps(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).pr(Lego.LAMBDA_STANDARD_RELOAD)
                        .decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
                        .anim(LAMBDA_MARESLEG_SHORT_ANIMS).orchestra(Orchestras.ORCHESTRA_MARESLEG_AKIMBO)
        );
        ModItems.gun_maresleg_broken = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.LEGENDARY, "gun_maresleg_broken", new GunConfig()
                .dura(0).draw(5).inspect(39).reloadSequential(true).crosshair(Crosshair.L_CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(32F).spreadAmmo(1.15F).delay(20).reload(22, 10, 13, 0).jam(24).sound(HBMSoundHandler.fireShotgun, 1.0F, 1.0F)
                        .mag(new MagazineSingleReload(0, 6).addConfigs(g12_equestrian_tkr, g12_bp, g12_bp_magnum, g12_bp_slug, g12, g12_slug, g12_flechette, g12_magnum, g12_explosive, g12_phosphorus))
                        .offset(0.75, -0.0625, -0.1875)
                        .canFire(Lego.LAMBDA_STANDARD_CAN_FIRE).fire(Lego.LAMBDA_NOWEAR_FIRE).recoil(LAMBDA_RECOIL_MARESLEG))
                .setupStandardConfiguration()
                .anim(LAMBDA_MARESLEG_SHORT_ANIMS).orchestra(Orchestras.ORCHESTRA_MARESLEG_SHORT)
        );

        ModItems.gun_liberator = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.A_SIDE, "gun_liberator", new GunConfig()
                .dura(200).draw(20).inspect(21).reloadSequential(true).crosshair(Crosshair.L_CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(16F).delay(20).rounds(4).reload(25, 15, 7, 0).jam(45).sound(HBMSoundHandler.fireShotgunAlt, 1.0F, 1.0F)
                        .mag(new MagazineSingleReload(0, 4).addConfigs(all))
                        .offset(0.75, -0.0625, -0.1875)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_LIBERATOR))
                .setupStandardConfiguration()
                .anim(LAMBDA_LIBERATOR_ANIMS).orchestra(Orchestras.ORCHESTRA_LIBERATOR)
        );

        ModItems.gun_spas12 = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.A_SIDE, "gun_spas12", new GunConfig()
                .dura(600).draw(20).inspect(39).reloadSequential(true).crosshair(Crosshair.L_CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(32F).spreadHipfire(0F).delay(20).reload(5, 10, 10, 10, 0).jam(36).sound(HBMSoundHandler.shotgunShoot, 1.0F, 1.0F)
                        .mag(new MagazineSingleReload(0, 8).addConfigs(all))
                        .offset(0.75, -0.0625, -0.1875)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_MARESLEG))
                .setupStandardConfiguration().ps(LAMBDA_SPAS_SECONDARY).pt(null)
                .anim(LAMBDA_SPAS_ANIMS).orchestra(Orchestras.ORCHESTRA_SPAS)
        );

        ModItems.gun_autoshotgun = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.A_SIDE, "gun_autoshotgun", new GunConfig()
                .dura(2_000).draw(10).inspect(33).reloadSequential(true).crosshair(Crosshair.L_CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(48F).delay(10).auto(true).autoAfterDry(true).dryfireAfterAuto(true).reload(44).jam(19).sound(HBMSoundHandler.fireShotgunAuto, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 20).addConfigs(all))
                        .offset(0.75, -0.125, -0.25)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_AUTOSHOTGUN))
                .setupStandardConfiguration()
                .anim(LAMBDA_SHREDDER_ANIMS).orchestra(Orchestras.ORCHESTRA_SHREDDER)
        );
        ModItems.gun_autoshotgun_shredder = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.B_SIDE, "gun_autoshotgun_shredder", new GunConfig()
                .dura(2_000).draw(10).inspect(33).reloadSequential(true).crosshair(Crosshair.L_CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(50F).delay(10).auto(true).autoAfterDry(true).dryfireAfterAuto(true).reload(44).jam(19).sound(HBMSoundHandler.fireShotgunAuto, 1.0F, 1.0F)
                        .mag(new MagazineBelt().addConfigs(g12_shredder, g12_shredder_slug, g12_shredder_flechette, g12_shredder_magnum, g12_shredder_explosive, g12_shredder_phosphorus))
                        .offset(0.75, -0.125, -0.25)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_AUTOSHOTGUN))
                .setupStandardConfiguration()
                .anim(LAMBDA_SHREDDER_ANIMS).orchestra(Orchestras.ORCHESTRA_SHREDDER)
        );
        ModItems.gun_autoshotgun_sexy = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.LEGENDARY, "gun_autoshotgun_sexy", new GunConfig()
                .dura(5_000).draw(20).inspect(65).reloadSequential(true).inspectCancel(false).crosshair(Crosshair.L_CIRCLE).hideCrosshair(false).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(64F).delay(4).auto(true).dryfireAfterAuto(true).reload(110).jam(19).sound(HBMSoundHandler.fireShotgunAuto, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 100).addConfigs(g12_equestrian_bj, g12_bp, g12_bp_magnum, g12_bp_slug, g12, g12_slug, g12_flechette, g12_magnum, g12_explosive, g12_phosphorus))
                        .offset(0.75, -0.125, -0.25)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_SEXY))
                .setupStandardConfiguration()
                .anim(LAMBDA_SEXY_ANIMS).orchestra(Orchestras.ORCHESTRA_SHREDDER_SEXY)
        );
    }

    public static Function<ItemStack, String> LAMBDA_NAME_MARESLEG = (stack) -> {
        if(WeaponModManager.hasUpgrade(stack, 0, WeaponModManager.ID_SAWED_OFF)) return stack.getTranslationKey() + "_short";
        return null;
    };

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_RECOIL_MARESLEG = (stack, ctx) -> ItemGunBaseNT.setupRecoil(10, (float) (ctx.getPlayer().getRNG().nextGaussian() * 1.5));

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_RECOIL_LIBERATOR = (stack, ctx) -> ItemGunBaseNT.setupRecoil(5, (float) (ctx.getPlayer().getRNG().nextGaussian() * 1.5));

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_RECOIL_AUTOSHOTGUN = (stack, ctx) -> ItemGunBaseNT.setupRecoil((float) (ctx.getPlayer().getRNG().nextGaussian() * 1.5) + 1.5F, (float) (ctx.getPlayer().getRNG().nextGaussian() * 0.5));

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_RECOIL_SEXY = (stack, ctx) -> ItemGunBaseNT.setupRecoil((float) (ctx.getPlayer().getRNG().nextGaussian() * 0.5), (float) (ctx.getPlayer().getRNG().nextGaussian() * 0.5));

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_SPAS_SECONDARY = (stack, ctx) -> {
        EntityLivingBase entity = ctx.entity;
        EntityPlayer player = ctx.getPlayer();
        Receiver rec = ctx.config.getReceivers(stack)[0];
        int index = ctx.configIndex;
        ItemGunBaseNT.GunState state = ItemGunBaseNT.getState(stack, index);
        if(state == ItemGunBaseNT.GunState.IDLE) {
            if(rec.getCanFire(stack).apply(stack, ctx)) {
                rec.getOnFire(stack).accept(stack, ctx);
                int remaining = rec.getRoundsPerCycle(stack);
                int timeFired = 1;
                for(int i = 0; i < remaining; i++) {
                    if(rec.getCanFire(stack).apply(stack, ctx)) {
                        rec.getOnFire(stack).accept(stack, ctx);
                        timeFired++;
                    }
                }
                if(rec.getFireSound(stack) != null) entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, rec.getFireSound(stack), SoundCategory.PLAYERS, rec.getFireVolume(stack), rec.getFirePitch(stack) * (timeFired > 1 ? 0.9F : 1F));
                ItemGunBaseNT.setState(stack, index, ItemGunBaseNT.GunState.COOLDOWN);
                ItemGunBaseNT.setTimer(stack, index, 20);
            } else {
                if(rec.getDoesDryFire(stack)) {
                    ItemGunBaseNT.playAnimation(player, stack, HbmAnimationsSedna.AnimType.CYCLE_DRY, index);
                    ItemGunBaseNT.setState(stack, index, ItemGunBaseNT.GunState.DRAWING);
                    ItemGunBaseNT.setTimer(stack, index, rec.getDelayAfterDryFire(stack));
                }
            }
        }
        if(state == ItemGunBaseNT.GunState.RELOADING) {
            ItemGunBaseNT.setReloadCancel(stack, true);
        }
    };

    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.AnimType, BusAnimationSedna> LAMBDA_MARESLEG_ANIMS = (stack, type) -> {
        switch (type) {
            case EQUIP -> {
                return new BusAnimationSedna()
                        .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(-60, 0, 0, 0).addPos(0, 0, -3, 500, IType.SIN_DOWN));
            }
            case CYCLE -> {
                return new BusAnimationSedna()
                        .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(0, 0, 0, 50).addPos(0, 0, -1, 50).addPos(0, 0, 0, 250))
                        .addBus("SIGHT", new BusAnimationSequenceSedna().addPos(35, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                        .addBus("LEVER", new BusAnimationSequenceSedna().addPos(0, 0, 0, 600).addPos(-85, 0, 0, 200).addPos(0, 0, 0, 200))
                        .addBus("TURN", new BusAnimationSequenceSedna().addPos(0, 0, 0, 600).addPos(0, 0, 45, 200, IType.SIN_DOWN).addPos(0, 0, 0, 200, IType.SIN_UP))
                        .addBus("HAMMER", new BusAnimationSequenceSedna().addPos(30, 0, 0, 50).addPos(30, 0, 0, 550).addPos(0, 0, 0, 200));
            }
            case CYCLE_DRY -> {
                return new BusAnimationSedna()
                        .addBus("LEVER", new BusAnimationSequenceSedna().addPos(0, 0, 0, 600).addPos(-90, 0, 0, 200).addPos(0, 0, 0, 200))
                        .addBus("TURN", new BusAnimationSequenceSedna().addPos(0, 0, 0, 600).addPos(0, 0, 45, 200, IType.SIN_DOWN).addPos(0, 0, 0, 200, IType.SIN_UP))
                        .addBus("HAMMER", new BusAnimationSequenceSedna().addPos(30, 0, 0, 50).addPos(30, 0, 0, 550).addPos(0, 0, 0, 200));
            }
            case RELOAD -> {
                boolean empty = ((ItemGunBaseNT) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, MainRegistry.proxy.me().inventory) <= 0;
                return new BusAnimationSedna()
                        .addBus("LIFT", new BusAnimationSequenceSedna().addPos(30, 0, 0, 400, IType.SIN_FULL))
                        .addBus("LEVER", new BusAnimationSequenceSedna().addPos(0, 0, 0, 400).addPos(-85, 0, 0, 200))
                        .addBus("SHELL", new BusAnimationSequenceSedna().addPos(0, 0, 0, 600).addPos(0, 0.25, -3, 0).addPos(0, empty ? 0.25 : 0.125, -1.5, 150, IType.SIN_UP).addPos(0, empty ? 0.25 : -0.25, 0, 150, IType.SIN_DOWN))
                        .addBus("FLAG", new BusAnimationSequenceSedna().addPos(0, 0, 0, empty ? 900 : 0).addPos(1, 1, 1, 0));
            }
            case RELOAD_CYCLE -> {
                return new BusAnimationSedna()
                        .addBus("LIFT", new BusAnimationSequenceSedna().addPos(30, 0, 0, 0))
                        .addBus("LEVER", new BusAnimationSequenceSedna().addPos(-85, 0, 0, 0))
                        .addBus("SHELL", new BusAnimationSequenceSedna().addPos(0, 0.25, -3, 0).addPos(0, 0.125, -1.5, 150, IType.SIN_UP).addPos(0, -0.125, 0, 150, IType.SIN_DOWN))
                        .addBus("FLAG", new BusAnimationSequenceSedna().addPos(1, 1, 1, 0));
            }
            case RELOAD_END -> {
                return new BusAnimationSedna()
                        .addBus("LIFT", new BusAnimationSequenceSedna().addPos(30, 0, 0, 0).addPos(30, 0, 0, 250).addPos(0, 0, 0, 400, IType.SIN_FULL))
                        .addBus("LEVER", new BusAnimationSequenceSedna().addPos(-85, 0, 0, 0).addPos(0, 0, 0, 200))
                        .addBus("FLAG", new BusAnimationSequenceSedna().addPos(1, 1, 1, 0));
            }
            case JAMMED -> {
                return new BusAnimationSedna()
                        .addBus("LIFT", new BusAnimationSequenceSedna().addPos(30, 0, 0, 0).addPos(30, 0, 0, 250).addPos(0, 0, 0, 400, IType.SIN_FULL))
                        .addBus("LEVER", new BusAnimationSequenceSedna().addPos(-85, 0, 0, 0).addPos(-15, 0, 0, 200).addPos(-15, 0, 0, 650).addPos(-85, 0, 0, 200).addPos(-15, 0, 0, 200).addPos(-15, 0, 0, 200).addPos(-85, 0, 0, 200).addPos(0, 0, 0, 200))
                        .addBus("TURN", new BusAnimationSequenceSedna().addPos(0, 0, 0, 850).addPos(0, 0, 45, 200, IType.SIN_DOWN).addPos(0, 0, 45, 800).addPos(0, 0, 0, 200, IType.SIN_UP))
                        .addBus("FLAG", new BusAnimationSequenceSedna().addPos(1, 1, 1, 0));
            }
            case INSPECT -> {
                return new BusAnimationSedna()
                        .addBus("LIFT", new BusAnimationSequenceSedna().addPos(-35, 0, 0, 300, IType.SIN_FULL).addPos(-35, 0, 0, 1150).addPos(0, 0, 0, 500, IType.SIN_FULL))
                        .addBus("TURN", new BusAnimationSequenceSedna().addPos(0, 0, 0, 450).addPos(0, 0, -90, 500, IType.SIN_FULL).addPos(0, 0, -90, 500).addPos(0, 0, 0, 500, IType.SIN_FULL));
            }
        }

        return null;
    };

    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.AnimType, BusAnimationSedna> LAMBDA_MARESLEG_SHORT_ANIMS = (stack, type) -> switch (type) {
        case EQUIP -> new BusAnimationSedna()
                .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(-60, 0, 0, 0).addPos(0, 0, -3, 250, IType.SIN_DOWN));
        case CYCLE -> new BusAnimationSedna()
                .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(0, 0, 0, 50).addPos(0, 0, -1, 50).addPos(0, 0, 0, 250))
                .addBus("SIGHT", new BusAnimationSequenceSedna().addPos(35, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("LEVER", new BusAnimationSequenceSedna().addPos(0, 0, 0, 600).addPos(-85, 0, 0, 200).addPos(0, 0, 0, 200))
                .addBus("HAMMER", new BusAnimationSequenceSedna().addPos(30, 0, 0, 50).addPos(30, 0, 0, 550).addPos(0, 0, 0, 200))
                .addBus("FLIP", new BusAnimationSequenceSedna().addPos(0, 0, 0, 600).addPos(360, 0, 0, 400))
                .addBus("SHELL", new BusAnimationSequenceSedna().addPos(-20, 0, 0, 0)); //gets rid of the shell in the barrel during cycling
        case CYCLE_DRY -> new BusAnimationSedna()
                .addBus("LEVER", new BusAnimationSequenceSedna().addPos(0, 0, 0, 600).addPos(-90, 0, 0, 200).addPos(0, 0, 0, 200))
                .addBus("HAMMER", new BusAnimationSequenceSedna().addPos(30, 0, 0, 50).addPos(30, 0, 0, 550).addPos(0, 0, 0, 200))
                .addBus("FLIP", new BusAnimationSequenceSedna().addPos(0, 0, 0, 600).addPos(360, 0, 0, 400))
                .addBus("SHELL", new BusAnimationSequenceSedna().addPos(-20, 0, 0, 0));
        case JAMMED -> new BusAnimationSedna()
                .addBus("LIFT", new BusAnimationSequenceSedna().addPos(30, 0, 0, 0).addPos(30, 0, 0, 250).addPos(0, 0, 0, 400, IType.SIN_FULL))
                .addBus("LEVER", new BusAnimationSequenceSedna().addPos(-85, 0, 0, 0).addPos(-15, 0, 0, 200).addPos(-15, 0, 0, 650).addPos(-85, 0, 0, 200).addPos(-15, 0, 0, 200).addPos(-15, 0, 0, 200).addPos(-85, 0, 0, 200).addPos(0, 0, 0, 200))
                .addBus("FLAG", new BusAnimationSequenceSedna().addPos(1, 1, 1, 0));
        default -> LAMBDA_MARESLEG_ANIMS.apply(stack, type);
    };

    /** This fucking sucks */
    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.AnimType, BusAnimationSedna> LAMBDA_LIBERATOR_ANIMS = (stack, type) -> {
        int ammo = ((ItemGunBaseNT) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, MainRegistry.proxy.me().inventory);
        switch(type) {
            case EQUIP: return new BusAnimationSedna()
                    .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(60, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
            case CYCLE: return new BusAnimationSedna()
                    .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(0, 0, -2.5, 50, IType.SIN_DOWN).addPos(0, 0, 0, 350, IType.SIN_FULL));
            case CYCLE_DRY: return new BusAnimationSedna();
            case RELOAD: if(ammo == 0) return new BusAnimationSedna()
                    .addBus("LATCH", new BusAnimationSequenceSedna().addPos(15, 0, 0, 100))
                    .addBus("BREAK", new BusAnimationSequenceSedna().addPos(0, 0, 0, 100).addPos(60, 0, 0, 350, IType.SIN_DOWN))
                    .addBus("SHELL1", new BusAnimationSequenceSedna().addPos(2, -4, -2, 0).addPos(2, -4, -2, 400).addPos(0, 0, -2, 450, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_UP))
                    .addBus("SHELL2", new BusAnimationSequenceSedna().addPos(2, -4, -2, 0))
                    .addBus("SHELL3", new BusAnimationSequenceSedna().addPos(2, -4, -2, 0))
                    .addBus("SHELL4", new BusAnimationSequenceSedna().addPos(2, -4, -2, 0));
                if(ammo == 1) return new BusAnimationSedna()
                        .addBus("LATCH", new BusAnimationSequenceSedna().addPos(15, 0, 0, 100))
                        .addBus("BREAK", new BusAnimationSequenceSedna().addPos(0, 0, 0, 100).addPos(60, 0, 0, 350, IType.SIN_DOWN))
                        .addBus("SHELL1", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                        .addBus("SHELL2", new BusAnimationSequenceSedna().addPos(2, -4, -2, 0).addPos(2, -4, -2, 400).addPos(0, 0, -2, 450, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_UP))
                        .addBus("SHELL3", new BusAnimationSequenceSedna().addPos(2, -4, -2, 0))
                        .addBus("SHELL4", new BusAnimationSequenceSedna().addPos(2, -4, -2, 0));
                if(ammo == 2) return new BusAnimationSedna()
                        .addBus("LATCH", new BusAnimationSequenceSedna().addPos(15, 0, 0, 100))
                        .addBus("BREAK", new BusAnimationSequenceSedna().addPos(0, 0, 0, 100).addPos(60, 0, 0, 350, IType.SIN_DOWN))
                        .addBus("SHELL1", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                        .addBus("SHELL2", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                        .addBus("SHELL3", new BusAnimationSequenceSedna().addPos(2, -4, -2, 0).addPos(2, -4, -2, 400).addPos(0, 0, -2, 450, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_UP))
                        .addBus("SHELL4", new BusAnimationSequenceSedna().addPos(2, -4, -2, 0));
                if(ammo == 3) return new BusAnimationSedna()
                        .addBus("LATCH", new BusAnimationSequenceSedna().addPos(15, 0, 0, 100))
                        .addBus("BREAK", new BusAnimationSequenceSedna().addPos(0, 0, 0, 100).addPos(60, 0, 0, 350, IType.SIN_DOWN))
                        .addBus("SHELL1", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                        .addBus("SHELL2", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                        .addBus("SHELL3", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                        .addBus("SHELL4", new BusAnimationSequenceSedna().addPos(2, -4, -2, 0).addPos(2, -4, -2, 400).addPos(0, 0, -2, 450, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_UP));
            case RELOAD_CYCLE:
                if(ammo == 0) return new BusAnimationSedna()
                        .addBus("LATCH", new BusAnimationSequenceSedna().addPos(15, 0, 0, 0))
                        .addBus("BREAK", new BusAnimationSequenceSedna().addPos(60, 0, 0, 0))
                        .addBus("SHELL1", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                        .addBus("SHELL2", new BusAnimationSequenceSedna().addPos(2, -4, -2, 0).addPos(0, 0, -2, 450, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_UP))
                        .addBus("SHELL3", new BusAnimationSequenceSedna().addPos(2, -4, -2, 0))
                        .addBus("SHELL4", new BusAnimationSequenceSedna().addPos(2, -4, -2, 0));
                if(ammo == 1) return new BusAnimationSedna()
                        .addBus("LATCH", new BusAnimationSequenceSedna().addPos(15, 0, 0, 0))
                        .addBus("BREAK", new BusAnimationSequenceSedna().addPos(60, 0, 0, 0))
                        .addBus("SHELL1", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                        .addBus("SHELL2", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                        .addBus("SHELL3", new BusAnimationSequenceSedna().addPos(2, -4, -2, 0).addPos(0, 0, -2, 450, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_UP))
                        .addBus("SHELL4", new BusAnimationSequenceSedna().addPos(2, -4, -2, 0));
                if(ammo == 2) return new BusAnimationSedna()
                        .addBus("LATCH", new BusAnimationSequenceSedna().addPos(15, 0, 0, 0))
                        .addBus("BREAK", new BusAnimationSequenceSedna().addPos(60, 0, 0, 0))
                        .addBus("SHELL1", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                        .addBus("SHELL2", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                        .addBus("SHELL3", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                        .addBus("SHELL4", new BusAnimationSequenceSedna().addPos(2, -4, -2, 0).addPos(0, 0, -2, 450, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_UP));
                return null;
            case RELOAD_END: return new BusAnimationSedna()
                    .addBus("LATCH", new BusAnimationSequenceSedna().addPos(15, 0, 0, 0).addPos(15, 0, 0, 250).addPos(0, 0, 0, 50))
                    .addBus("BREAK", new BusAnimationSequenceSedna().addPos(60, 0, 0, 0).addPos(0, 0, 0, 250, IType.SIN_UP))
                    .addBus(ammo >= 0 ? "SHELL1" : "NULL", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                    .addBus(ammo >= 1 ? "SHELL2" : "NULL", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                    .addBus(ammo >= 2 ? "SHELL3" : "NULL", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                    .addBus(ammo >= 3 ? "SHELL4" : "NULL", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                    .addBus(ammo < 0 ? "SHELL1" : "NULL", new BusAnimationSequenceSedna().addPos(2, -8, -2, 0))
                    .addBus(ammo < 1 ? "SHELL2" : "NULL", new BusAnimationSequenceSedna().addPos(2, -8, -2, 0))
                    .addBus(ammo < 2 ? "SHELL3" : "NULL", new BusAnimationSequenceSedna().addPos(2, -8, -2, 0))
                    .addBus(ammo < 3 ? "SHELL4" : "NULL", new BusAnimationSequenceSedna().addPos(2, -8, -2, 0));
            case JAMMED: return new BusAnimationSedna()
                    .addBus("LATCH", new BusAnimationSequenceSedna().addPos(15, 0, 0, 0).addPos(15, 0, 0, 250).addPos(0, 0, 0, 50).addPos(0, 0, 0, 550).addPos(15, 0, 0, 100).addPos(15, 0, 0, 600).addPos(0, 0, 0, 50))
                    .addBus("BREAK", new BusAnimationSequenceSedna().addPos(60, 0, 0, 0).addPos(0, 0, 0, 250, IType.SIN_UP).addPos(0, 0, 0, 600).addPos(45, 0, 0, 250, IType.SIN_DOWN).addPos(45, 0, 0, 300).addPos(0, 0, 0, 150, IType.SIN_UP))
                    .addBus(ammo >= 0 ? "SHELL1" : "NULL", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                    .addBus(ammo >= 1 ? "SHELL2" : "NULL", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                    .addBus(ammo >= 2 ? "SHELL3" : "NULL", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                    .addBus(ammo >= 3 ? "SHELL4" : "NULL", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                    .addBus(ammo < 0 ? "SHELL1" : "NULL", new BusAnimationSequenceSedna().addPos(2, -8, -2, 0))
                    .addBus(ammo < 1 ? "SHELL2" : "NULL", new BusAnimationSequenceSedna().addPos(2, -8, -2, 0))
                    .addBus(ammo < 2 ? "SHELL3" : "NULL", new BusAnimationSequenceSedna().addPos(2, -8, -2, 0))
                    .addBus(ammo < 3 ? "SHELL4" : "NULL", new BusAnimationSequenceSedna().addPos(2, -8, -2, 0));
            case INSPECT: return new BusAnimationSedna()
                    .addBus("LATCH", new BusAnimationSequenceSedna().addPos(15, 0, 0, 100).addPos(15, 0, 0, 1100).addPos(0, 0, 0, 50))
                    .addBus("BREAK", new BusAnimationSequenceSedna().addPos(0, 0, 0, 100).addPos(60, 0, 0, 350, IType.SIN_DOWN).addPos(60, 0, 0, 500).addPos(0, 0, 0, 250, IType.SIN_UP))
                    .addBus(ammo > 0 ? "SHELL1" : "NULL", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                    .addBus(ammo > 1 ? "SHELL2" : "NULL", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                    .addBus(ammo > 2 ? "SHELL3" : "NULL", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                    .addBus(ammo > 3 ? "SHELL4" : "NULL", new BusAnimationSequenceSedna().addPos(0, 0, 0, 0))
                    .addBus(ammo < 1 ? "SHELL1" : "NULL", new BusAnimationSequenceSedna().addPos(2, -8, -2, 0))
                    .addBus(ammo < 2 ? "SHELL2" : "NULL", new BusAnimationSequenceSedna().addPos(2, -8, -2, 0))
                    .addBus(ammo < 3 ? "SHELL3" : "NULL", new BusAnimationSequenceSedna().addPos(2, -8, -2, 0))
                    .addBus(ammo < 4 ? "SHELL4" : "NULL", new BusAnimationSequenceSedna().addPos(2, -8, -2, 0));
        }

        return null;
    };

    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.AnimType, BusAnimationSedna> LAMBDA_SPAS_ANIMS = (stack, type) -> {
        switch (type) {
            case EQUIP -> {
                return new BusAnimationSedna()
                        .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(-60, 0, 0, 0).addPos(0, 0, -3, 500, IType.SIN_DOWN));
            }
            case CYCLE -> {
                return ResourceManager.spas_12_anim.get("Fire");
            }
            case CYCLE_DRY -> {
                return ResourceManager.spas_12_anim.get("FireDry");
            }
            case ALT_CYCLE -> {
                return ResourceManager.spas_12_anim.get("FireAlt");
            }
            case RELOAD -> {
                boolean empty = ((ItemGunBaseNT) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, MainRegistry.proxy.me().inventory) <= 0;
                return ResourceManager.spas_12_anim.get(empty ? "ReloadEmptyStart" : "ReloadStart");
            }
            case RELOAD_CYCLE -> {
                return ResourceManager.spas_12_anim.get("Reload");
            }
            case RELOAD_END -> {
                return ResourceManager.spas_12_anim.get("ReloadEnd");
            }
            case JAMMED -> {
                return ResourceManager.spas_12_anim.get("Jammed");
            }
            case INSPECT -> {
                return ResourceManager.spas_12_anim.get("Inspect");
            }
        }

        return null;
    };

    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.AnimType, BusAnimationSedna> LAMBDA_SHREDDER_ANIMS = (stack, type) -> switch (type) {
        case EQUIP -> new BusAnimationSedna()
                .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(60, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
        case CYCLE -> new BusAnimationSedna()
                .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(0, 0, -1, 50, IType.SIN_DOWN).addPos(0, 0, 0, 150, IType.SIN_FULL))
                .addBus("CYCLE", new BusAnimationSequenceSedna().addPos(0, 0, 0, 150).addPos(0, 0, 18, 100));
        case CYCLE_DRY -> new BusAnimationSedna()
                .addBus("CYCLE", new BusAnimationSequenceSedna().addPos(0, 0, 0, 150).addPos(0, 0, 18, 100));
        case RELOAD -> new BusAnimationSedna()
                .addBus("MAG", new BusAnimationSequenceSedna().addPos(0, -8, 0, 250, IType.SIN_UP).addPos(0, -8, 0, 1000).addPos(0, 0, 0, 300))
                .addBus("LIFT", new BusAnimationSequenceSedna().addPos(0, 0, 0, 750).addPos(-25, 0, 0, 300, IType.SIN_FULL).addPos(-25, 0, 0, 500).addPos(-27, 0, 0, 100, IType.SIN_DOWN).addPos(-25, 0, 0, 100, IType.SIN_FULL).addPos(-25, 0, 0, 150).addPos(0, 0, 0, 300, IType.SIN_FULL));
        case JAMMED -> new BusAnimationSedna()
                .addBus("MAG", new BusAnimationSequenceSedna().addPos(0, 0, 0, 500).addPos(0, -2, 0, 150, IType.SIN_UP).addPos(0, 0, 0, 100))
                .addBus("LIFT", new BusAnimationSequenceSedna().addPos(0, 0, 0, 750).addPos(-2, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
        case INSPECT -> new BusAnimationSedna()
                .addBus("MAG", new BusAnimationSequenceSedna()
                        .addPos(0, -1, 0, 150).addPos(6, -1, 0, 150).addPos(6, 12, 0, 350, IType.SIN_DOWN).addPos(6, -2, 0, 350, IType.SIN_UP).addPos(6, -1, 0, 50)
                        .addPos(6, -1, 0, 100).addPos(0, -1, 0, 150, IType.SIN_FULL).addPos(0, 0, 0, 150, IType.SIN_UP))
                .addBus("SPEEN", new BusAnimationSequenceSedna().addPos(0, 0, 0, 300).addPos(360, 0, 0, 700))
                .addBus("LIFT", new BusAnimationSequenceSedna().addPos(0, 0, 0, 1450).addPos(-2, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
        default -> null;
    };
    // TODO: port AnimationEnums
	@SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.AnimType, BusAnimationSedna> LAMBDA_SEXY_ANIMS = (stack, type) -> {
        switch (type) {
            case EQUIP -> {
                return new BusAnimationSedna()
                        .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(45, 0, 0, 0).addPos(0, 0, 0, 1000, IType.SIN_DOWN));
            }
            case CYCLE -> {
                int amount = ((ItemGunBaseNT) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, null);
                return new BusAnimationSedna()
                        .addBus("RECOIL", new BusAnimationSequenceSedna().hold(50).addPos(0, 0, -0.25, 50, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                        .addBus("BARREL", new BusAnimationSequenceSedna().addPos(0, 0, -1, 50, IType.SIN_DOWN).addPos(0, 0, 0, 150))
                        .addBus("CYCLE", new BusAnimationSequenceSedna().addPos(1, 0, 0, 150))
                        .addBus("HOOD", new BusAnimationSequenceSedna().hold(50).addPos(3, 0, 0, 50, IType.SIN_DOWN).addPos(0, 0, 0, 50, IType.SIN_UP))
                        .addBus("SHELLS", new BusAnimationSequenceSedna().setPos(amount - 1, 0, 0));
            }
            case CYCLE_DRY -> {
                return new BusAnimationSedna()
                        .addBus("CYCLE", new BusAnimationSequenceSedna().addPos(0, 0, 18, 50));
            }
            case RELOAD -> {
                return new BusAnimationSedna()
                        .addBus("LOWER", new BusAnimationSequenceSedna().addPos(15, 0, 0, 500, IType.SIN_FULL).hold(2750).addPos(12, 0, 0, 100, IType.SIN_DOWN).addPos(15, 0, 0, 100, IType.SIN_FULL).hold(1050).addPos(18, 0, 0, 100, IType.SIN_DOWN).addPos(15, 0, 0, 100, IType.SIN_FULL).hold(300).addPos(0, 0, 0, 500, IType.SIN_FULL))
                        .addBus("LEVER", new BusAnimationSequenceSedna().addPos(0, 0, 1, 150).hold(4700).addPos(0, 0, 0, 150))
                        .addBus("HOOD", new BusAnimationSequenceSedna().hold(250).addPos(60, 0, 0, 500, IType.SIN_FULL).hold(3250).addPos(0, 0, 0, 500, IType.SIN_UP))
                        .addBus("BELT", new BusAnimationSequenceSedna().setPos(1, 0, 0).hold(750).addPos(0, 0, 0, 500, IType.SIN_UP).hold(2000).addPos(1, 0, 0, 500, IType.SIN_UP))
                        .addBus("MAG", new BusAnimationSequenceSedna().hold(1500).addPos(0, -1, 0, 250, IType.SIN_UP).addPos(2, -1, 0, 500, IType.SIN_UP).addPos(7, 1, 0, 250, IType.SIN_UP).addPos(15, 2, 0, 250).setPos(0, -2, 0).addPos(0, 0, 0, 500, IType.SIN_UP))
                        .addBus("MAGROT", new BusAnimationSequenceSedna().hold(2250).addPos(0, 0, -180, 500, IType.SIN_FULL).setPos(0, 0, 0));
            }
            case INSPECT -> {
                return new BusAnimationSedna()
                        .addBus("BOTTLE", new BusAnimationSequenceSedna().setPos(8, -8, -2).addPos(6, -4, -2, 500, IType.SIN_DOWN).addPos(3, -3, -5, 500, IType.SIN_FULL).addPos(3, -2, -5, 1000).addPos(4, -6, -2, 750, IType.SIN_FULL).addPos(6, -8, -2, 500, IType.SIN_UP))
                        .addBus("SIP", new BusAnimationSequenceSedna().setPos(25, 0, 0).hold(500).addPos(-90, 0, 0, 500, IType.SIN_FULL).addPos(-110, 0, 0, 1000).addPos(25, 0, 0, 750, IType.SIN_FULL));
            }
        }

		return null;
	};
}
