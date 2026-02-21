package com.hbm.items.weapon.sedna.factory;

import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.*;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.mags.MagazineSingleReload;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.render.anim.sedna.BusAnimationKeyframeSedna.IType;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.BusAnimationSequenceSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.render.misc.RenderScreenOverlay.Crosshair;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class XFactoryCatapult {
    public static BulletConfig nuke_standard;
    public static BulletConfig nuke_demo;
    public static BulletConfig nuke_high;
    public static BulletConfig nuke_tots;
    public static BulletConfig nuke_hive;
    public static BulletConfig nuke_balefire;

    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_NUKE_STANDARD = (bullet, mop) -> {
        if(mop.typeOfHit == mop.typeOfHit.ENTITY && bullet.ticksExisted < 3 && mop.entityHit == bullet.getThrower()) return;
        if(bullet.isDead) return;
        bullet.setDead();

        ExplosionVNT vnt = new ExplosionVNT(bullet.world, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, 10);
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(2, bullet.damage).withRangeMod(1.5F));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.explode();

        incrementRad(bullet.world, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, 1F);
        spawnMush(bullet, mop);
    };

    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_NUKE_DEMO = (bullet, mop) -> {
        if(mop.typeOfHit == mop.typeOfHit.ENTITY && bullet.ticksExisted < 3 && mop.entityHit == bullet.getThrower()) return;
        if(bullet.isDead) return;
        bullet.setDead();

        ExplosionVNT vnt = new ExplosionVNT(bullet.world, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, 10);
        vnt.setBlockAllocator(new BlockAllocatorStandard(64));
        vnt.setBlockProcessor(new BlockProcessorStandard());
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(2, bullet.damage).withRangeMod(1.5F));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.explode();

        incrementRad(bullet.world, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, 1.5F);
        spawnMush(bullet, mop);
    };

    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_NUKE_HIGH = (bullet, mop) -> {
        if(mop.typeOfHit == mop.typeOfHit.ENTITY && bullet.ticksExisted < 3 && mop.entityHit == bullet.getThrower()) return;
        if(bullet.isDead) return;
        bullet.setDead();
        bullet.world.spawnEntity(EntityNukeExplosionMK5.statFac(bullet.world, 35, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z));
        spawnMush(bullet, mop);
    };

    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_NUKE_BALEFIRE = (bullet, mop) -> {
        if(mop.typeOfHit == mop.typeOfHit.ENTITY && bullet.ticksExisted < 3 && mop.entityHit == bullet.getThrower()) return;
        if(bullet.isDead) return;
        bullet.setDead();

        ExplosionVNT vnt = new ExplosionVNT(bullet.world, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, 10);
        vnt.setBlockAllocator(new BlockAllocatorStandard(64));
        vnt.setBlockProcessor(new BlockProcessorStandard().withBlockEffect(new BlockMutatorBalefire()));
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(2, bullet.damage).withRangeMod(1.5F));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.explode();

        incrementRad(bullet.world, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, 1.5F);

        bullet.world.playSound(null, mop.hitVec.x, mop.hitVec.y + 0.5, mop.hitVec.z, HBMSoundHandler.mukeExplosion, SoundCategory.HOSTILE, 15.0F, 1.0F);
        NBTTagCompound data = new NBTTagCompound();
        data.setString("type", "muke");
        data.setBoolean("balefire", true);
        PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, mop.hitVec.x, mop.hitVec.y + 0.5, mop.hitVec.z), new NetworkRegistry.TargetPoint(bullet.dimension, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, 250));
    };

    public static void incrementRad(World world, double posX, double posY, double posZ, float mult) {
        for(int i = -2; i <= 2; i++) { for(int j = -2; j <= 2; j++) {
            if(Math.abs(i) + Math.abs(j) < 4) {
                ChunkRadiationManager.proxy.incrementRad(world, new BlockPos((int) Math.floor(posX + i * 16), (int) Math.floor(posY), (int) Math.floor(posZ + j * 16)), 50F / (Math.abs(i) + Math.abs(j) + 1) * mult);
            }
        }
        }
    }

    public static void spawnMush(EntityBulletBaseMK4 bullet, RayTraceResult mop) {
        // mlbv: Sound disabled because Torex will handle it
//        bullet.world.playSound(null, mop.hitVec.x, mop.hitVec.y + 0.5, mop.hitVec.z, HBMSoundHandler.mukeExplosion, SoundCategory.HOSTILE, 15.0F, 1.0F);
        if(MainRegistry.polaroidID == 11 || bullet.world.rand.nextInt(100) == 0) EntityNukeTorex.statFacBale(bullet.world, mop.hitVec.x, mop.hitVec.y + 0.5, mop.hitVec.z, 0.3F);
        else EntityNukeTorex.statFac(bullet.world, mop.hitVec.x, mop.hitVec.y + 0.5, mop.hitVec.z, 0.4F);
    }

    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_NUKE_TINYTOT = (bullet, mop) -> {
        if(mop.typeOfHit == mop.typeOfHit.ENTITY && bullet.ticksExisted < 3 && mop.entityHit == bullet.getThrower()) return;
        if(bullet.isDead) return;
        bullet.setDead();

        ExplosionVNT vnt = new ExplosionVNT(bullet.world, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, 5);
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(2, bullet.damage).withRangeMod(1.5F));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.explode();

        incrementRad(bullet.world, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, 0.25F);
        // mlbv: Sound disabled because Torex will handle it
//        bullet.world.playSound(null, mop.hitVec.x, mop.hitVec.y + 0.5, mop.hitVec.z, HBMSoundHandler.mukeExplosion, SoundCategory.HOSTILE, 15.0F, 1.0F);
        if(MainRegistry.polaroidID == 11 || bullet.world.rand.nextInt(100) == 0) EntityNukeTorex.statFacBale(bullet.world, mop.hitVec.x, mop.hitVec.y + 0.5, mop.hitVec.z, 0.25F);
        else EntityNukeTorex.statFac(bullet.world, mop.hitVec.x, mop.hitVec.y + 0.5, mop.hitVec.z, 0.25F);
    };

    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_NUKE_HIVE = (bullet, mop) -> {
        if(mop.typeOfHit == mop.typeOfHit.ENTITY && bullet.ticksExisted < 3 && mop.entityHit == bullet.getThrower()) return;
        if(bullet.isDead) return;
        bullet.setDead();
        ExplosionVNT vnt = new ExplosionVNT(bullet.world, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, 5);
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, bullet.damage).withRangeMod(1.5F));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F));
        vnt.explode();
    };

    public static void init() {

        nuke_standard = new BulletConfig().setItem(GunFactory.EnumAmmo.NUKE_STANDARD).setLife(300).setVel(3F).setGrav(0.025F).setOnImpact(LAMBDA_NUKE_STANDARD);
        nuke_demo = new BulletConfig().setItem(GunFactory.EnumAmmo.NUKE_DEMO).setLife(300).setVel(3F).setGrav(0.025F).setOnImpact(LAMBDA_NUKE_DEMO);
        nuke_high = new BulletConfig().setItem(GunFactory.EnumAmmo.NUKE_HIGH).setLife(300).setVel(3F).setGrav(0.025F).setOnImpact(LAMBDA_NUKE_HIGH);
        nuke_tots = new BulletConfig().setItem(GunFactory.EnumAmmo.NUKE_TOTS).setProjectiles(8).setLife(300).setVel(3F).setGrav(0.025F).setSpread(0.1F).setDamage(0.35F).setOnImpact(LAMBDA_NUKE_TINYTOT);
        nuke_hive = new BulletConfig().setItem(GunFactory.EnumAmmo.NUKE_HIVE).setProjectiles(12).setLife(300).setVel(1F).setGrav(0.025F).setSpread(0.15F).setDamage(0.25F).setOnImpact(LAMBDA_NUKE_HIVE);
        nuke_balefire = new BulletConfig().setItem(GunFactory.EnumAmmo.NUKE_BALEFIRE).setDamage(2.5F).setLife(300).setVel(3F).setGrav(0.025F).setOnImpact(LAMBDA_NUKE_BALEFIRE);

        ModItems.gun_fatman = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.A_SIDE, "gun_fatman", new GunConfig()
                .dura(300).draw(20).inspect(30).crosshair(Crosshair.L_CIRCUMFLEX).hideCrosshair(false)
                .rec(new Receiver(0)
                        .dmg(100F).spreadHipfire(0F).delay(10).reload(57).jam(40).sound(HBMSoundHandler.fireFatman, 1.0F, 1.0F)
                        .mag(new MagazineSingleReload(0, 1).addConfigs(nuke_standard, nuke_demo, nuke_high, nuke_tots, nuke_hive, nuke_balefire))
                        .offset(1, -0.0625 * 1.5, -0.1875D).offsetScoped(1, -0.0625 * 1.5, -0.125D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_FATMAN))
                .setupStandardConfiguration()
                .anim(LAMBDA_FATMAN_ANIMS).orchestra(Orchestras.ORCHESTRA_FATMAN)
        );
    }

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_RECOIL_FATMAN = (stack, ctx) -> { };

    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.AnimType, BusAnimationSedna> LAMBDA_FATMAN_ANIMS = (stack, type) -> {
        switch (type) {
            case EQUIP -> {
                return new BusAnimationSedna()
                        .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(60, 0, 0, 0).addPos(0, 0, 0, 1000, IType.SIN_DOWN));
            }
            case CYCLE -> {
                Random rand = MainRegistry.proxy.me().getRNG();
                return new BusAnimationSedna()
                        .addBus("GAUGE", new BusAnimationSequenceSedna().addPos(0, 0, 135 + rand.nextInt(136), 100, IType.SIN_DOWN).addPos(0, 0, 0, 500, IType.SIN_DOWN))
                        .addBus("PISTON", new BusAnimationSequenceSedna().addPos(0, 0, 3, 100, IType.SIN_UP))
                        .addBus("NUKE", new BusAnimationSequenceSedna().addPos(0, 0, 3, 100, IType.SIN_UP).addPos(0, 0, 0, 0));
            }
            case RELOAD -> {
                return new BusAnimationSedna()
                        .addBus("LID", new BusAnimationSequenceSedna().addPos(0, 0, 0, 250).addPos(0, 0, -45, 250, IType.SIN_UP).addPos(0, 0, -45, 1200).addPos(0, 0, 0, 250, IType.SIN_UP))
                        .addBus("HANDLE", new BusAnimationSequenceSedna().addPos(0, 0, -2, 500, IType.SIN_FULL).addPos(0, 0, -2, 1700).addPos(0, 0, 0, 750, IType.SIN_FULL))
                        .addBus("NUKE", new BusAnimationSequenceSedna().addPos(5, -4, 3, 0).addPos(5, -4, 3, 750).addPos(2, 0.5, 3, 500, IType.SIN_UP).addPos(1, 0.5, 3, 100).addPos(0, 0, 3, 100).addPos(0, 0, 3, 750).addPos(0, 0, 0, 750, IType.SIN_FULL))
                        .addBus("PISTON", new BusAnimationSequenceSedna().addPos(0, 0, 3, 0).addPos(0, 0, 3, 2200).addPos(0, 0, 0, 750, IType.SIN_FULL))
                        .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(5, 0, 0, 500, IType.SIN_FULL).addPos(0, 0, 0, 500, IType.SIN_FULL).addPos(0, 0, 0, 450).addPos(3, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL).addPos(0, 0, 0, 500).addPos(-10, 0, 0, 375, IType.SIN_DOWN).addPos(0, 0, 0, 375, IType.SIN_UP));
            }
            case JAMMED -> {
                return new BusAnimationSedna()
                        .addBus("HANDLE", new BusAnimationSequenceSedna().addPos(0, 0, 0, 750).addPos(0, 0, -2, 250, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL).addPos(0, 0, -2, 250, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL))
                        .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(0, 0, 0, 500).addPos(-15, 0, 0, 250, IType.SIN_FULL).addPos(-15, 0, 0, 1000).addPos(0, 0, 0, 250, IType.SIN_FULL));
            }
            case INSPECT -> {
                return new BusAnimationSedna()
                        .addBus("HANDLE", new BusAnimationSequenceSedna().addPos(0, 0, 0, 250).addPos(0, 0, -2, 250, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL).addPos(0, 0, -2, 250, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_FULL))
                        .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(-15, 0, 0, 250, IType.SIN_FULL).addPos(-15, 0, 0, 1000).addPos(0, 0, 0, 250, IType.SIN_FULL));
            }
        }
        return null;
    };
}
