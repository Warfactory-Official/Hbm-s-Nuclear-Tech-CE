package com.hbm.items.weapon.sedna.factory;

import com.hbm.entity.effect.EntityFireLingering;
import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.*;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.impl.ItemGunStinger;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.items.weapon.sedna.mags.MagazineSingleReload;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.render.anim.sedna.BusAnimationKeyframeSedna.IType;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.BusAnimationSequenceSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.render.misc.RenderScreenOverlay.Crosshair;
import com.hbm.util.DamageResistanceHandler;
import com.hbm.util.EntityDamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class XFactoryRocket {

    public static BulletConfig[] rocket_template;

    public static BulletConfig[] rocket_rpzb;
    public static BulletConfig[] rocket_qd;
    public static BulletConfig[] rocket_ml;

    // FLYING
    public static Consumer<Entity> LAMBDA_STANDARD_ACCELERATE = (entity) -> {
        EntityBulletBaseMK4 bullet = (EntityBulletBaseMK4) entity;
        if(bullet.accel < 7) bullet.accel += 0.4D;
    };
    public static Consumer<Entity> LAMBDA_STEERING_ACCELERATE = (entity) -> {
        EntityBulletBaseMK4 bullet = (EntityBulletBaseMK4) entity;
        if(bullet.accel < 4) bullet.accel += 0.4D;
        if(bullet.getThrower() == null || !(bullet.getThrower() instanceof EntityPlayer player)) return;

        if(new Vec3d(bullet.posX - player.posX, bullet.posY - player.posY, bullet.posZ - player.posZ).length() > 100) return;
        if(player.getHeldItemMainhand().isEmpty() || !(player.getHeldItemMainhand().getItem() instanceof ItemGunBaseNT) || !ItemGunBaseNT.getIsAiming(player.getHeldItemMainhand())) return;

        RayTraceResult mop = Library.rayTrace(player, 200, 1);
        if(mop == null || mop.hitVec == null) return;

        Vec3d vec = new Vec3d(mop.hitVec.x - bullet.posX, mop.hitVec.y - bullet.posY, mop.hitVec.z - bullet.posZ);
        if(vec.length() < 3) return;
        vec = vec.normalize();

        double speed = new Vec3d(bullet.motionX, bullet.motionY, bullet.motionZ).length();
        bullet.motionX = vec.x * speed;
        bullet.motionY = vec.y * speed;
        bullet.motionZ = vec.z * speed;
    };

    // IMPACT
    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_STANDARD_EXPLODE = (bullet, mop) -> {
        if(mop.typeOfHit == mop.typeOfHit.ENTITY && bullet.ticksExisted < 3) return;
        Lego.standardExplode(bullet, mop, 5F); bullet.setDead();
    };
    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_STANDARD_EXPLODE_HEAT = (bullet, mop) -> {
        if(mop.typeOfHit == mop.typeOfHit.ENTITY && bullet.ticksExisted < 3) return;
        Lego.standardExplode(bullet, mop, 3.5F); bullet.setDead();
        if(mop.typeOfHit == mop.typeOfHit.ENTITY && mop.entityHit instanceof EntityLivingBase living) {
            EntityDamageUtil.attackEntityFromNT(living, bullet.config.getDamage(bullet, bullet.getThrower(), DamageResistanceHandler.DamageClass.EXPLOSIVE), bullet.damage * 3F, true, true, 0.5F, 5F, 0.2F);
        } else if(mop.typeOfHit == mop.typeOfHit.ENTITY) {
            mop.entityHit.attackEntityFrom(bullet.config.getDamage(bullet, bullet.getThrower(), DamageResistanceHandler.DamageClass.EXPLOSIVE), bullet.damage * 3F);
        }
    };
    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_STANDARD_EXPLODE_DEMO = (bullet, mop) -> {
        if(mop.typeOfHit == mop.typeOfHit.ENTITY && bullet.ticksExisted < 3) return;
        ExplosionVNT vnt = new ExplosionVNT(bullet.world, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, 5F, bullet.getThrower());
        vnt.setBlockAllocator(new BlockAllocatorStandard());
        vnt.setBlockProcessor(new BlockProcessorStandard());
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, bullet.damage));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F));
        vnt.explode();
        bullet.setDead();
    };
    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_STANDARD_EXPLODE_INC = (bullet, mop) -> spawnFire(bullet, mop, false, 300);
    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_STANDARD_EXPLODE_PHOSPHORUS = (bullet, mop) -> spawnFire(bullet, mop, true, 600);

    public static void spawnFire(EntityBulletBaseMK4 bullet, RayTraceResult mop, boolean phosphorus, int duration) {
        if(mop.typeOfHit == mop.typeOfHit.ENTITY && bullet.ticksExisted < 3) return;
        World world = bullet.world;
        Lego.standardExplode(bullet, mop, 3F);
        EntityFireLingering fire = new EntityFireLingering(world).setArea(6, 2).setDuration(duration).setType(phosphorus ? EntityFireLingering.TYPE_PHOSPHORUS : EntityFireLingering.TYPE_DIESEL);
        fire.setPosition(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
        world.spawnEntity(fire);
        bullet.setDead();
        for(int dx = -2; dx <= 2; dx++) {
            for(int dy = -2; dy <= 2; dy++) {
                for(int dz = -2; dz <= 2; dz++) {
                    int x = (int) Math.floor(mop.hitVec.x) + dx;
                    int y = (int) Math.floor(mop.hitVec.y) + dy;
                    int z = (int) Math.floor(mop.hitVec.z) + dz;
                    BlockPos pos = new BlockPos(x, y, z);
                    if(world.getBlockState(pos).getBlock().isAir(world.getBlockState(pos), bullet.world, pos)) for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                        if(world.getBlockState(pos.add(dir.offsetX, dir.offsetY, dir.offsetZ)).getBlock().isFlammable(world, pos.add(dir.offsetX, dir.offsetY, dir.offsetZ), Objects.requireNonNull(dir.getOpposite().toEnumFacing()))) {
                            world.setBlockState(pos, Blocks.FIRE.getDefaultState());
                            break;
                        }
                    }
                }
            }
        }
    }

    public static BulletConfig makeRPZB(BulletConfig original) { return original.clone(); }
    public static BulletConfig makeQD(BulletConfig original) { return original.clone().setLife(400).setOnUpdate(LAMBDA_STEERING_ACCELERATE); }
    public static BulletConfig makeML(BulletConfig original) { return original.clone(); }

    //this is starting to get messy but we need to put this crap *somewhere* and fragmenting it into a billion classes with two methods each just isn't gonna help
    public static void init() {

        rocket_template = new BulletConfig[5];

        BulletConfig baseRocket = new BulletConfig().setLife(300).setSelfDamageDelay(10).setVel(0F).setGrav(0F).setOnEntityHit(null).setOnRicochet(null).setOnUpdate(LAMBDA_STANDARD_ACCELERATE);

        rocket_template[0] = baseRocket.clone().setItem(GunFactory.EnumAmmo.ROCKET_HE).setOnImpact(LAMBDA_STANDARD_EXPLODE);
        rocket_template[1] = baseRocket.clone().setItem(GunFactory.EnumAmmo.ROCKET_HEAT).setDamage(0.5F).setOnImpact(LAMBDA_STANDARD_EXPLODE_HEAT);
        rocket_template[2] = baseRocket.clone().setItem(GunFactory.EnumAmmo.ROCKET_DEMO).setDamage(0.75F).setOnImpact(LAMBDA_STANDARD_EXPLODE_DEMO);
        rocket_template[3] = baseRocket.clone().setItem(GunFactory.EnumAmmo.ROCKET_INC).setDamage(0.75F).setOnImpact(LAMBDA_STANDARD_EXPLODE_INC);
        rocket_template[4] = baseRocket.clone().setItem(GunFactory.EnumAmmo.ROCKET_PHOSPHORUS).setDamage(0.75F).setOnImpact(LAMBDA_STANDARD_EXPLODE_PHOSPHORUS);

        rocket_rpzb = new BulletConfig[rocket_template.length];
        rocket_qd = new BulletConfig[rocket_template.length];
        rocket_ml = new BulletConfig[rocket_template.length];

        for(int i = 0; i < rocket_template.length; i++) {
            rocket_rpzb[i] = makeRPZB(rocket_template[i]);
            rocket_qd[i] = makeQD(rocket_template[i]);
            rocket_ml[i] = makeML(rocket_template[i]);
        }

        ModItems.gun_panzerschreck = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.A_SIDE, "gun_panzerschreck", new GunConfig()
                .dura(300).draw(7).inspect(40).crosshair(Crosshair.L_CIRCUMFLEX)
                .rec(new Receiver(0)
                        .dmg(25F).delay(5).reload(50).jam(40).sound(HBMSoundHandler.rpgShoot, 1.0F, 1.0F)
                        .mag(new MagazineSingleReload(0, 1).addConfigs(rocket_rpzb))
                        .offset(1, -0.0625 * 1.5, -0.1875D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_ROCKET))
                .setupStandardConfiguration()
                .anim(LAMBDA_PANZERSCHRECK_ANIMS).orchestra(Orchestras.ORCHESTRA_PANERSCHRECK)
        );

        ModItems.gun_stinger = new ItemGunStinger(ItemGunBaseNT.WeaponQuality.A_SIDE, "gun_stinger", new GunConfig()
                .dura(300).draw(7).inspect(40).crosshair(Crosshair.L_BOX_OUTLINE)
                .rec(new Receiver(0)
                        .dmg(35F).delay(5).reload(50).jam(40).sound(HBMSoundHandler.rpgShoot, 1.0F, 1.0F)
                        .mag(new MagazineSingleReload(0, 1).addConfigs(rocket_rpzb))
                        .offset(1, -0.0625 * 1.5, -0.1875D)
                        .setupLockonFire().recoil(LAMBDA_RECOIL_ROCKET))
                .setupStandardConfiguration().ps(LAMBDA_STINGER_SECONDARY_PRESS).rs(LAMBDA_STINGER_SECONDARY_RELEASE)
                .anim(LAMBDA_PANZERSCHRECK_ANIMS).orchestra(Orchestras.ORCHESTRA_STINGER)
        );

        ModItems.gun_quadro = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.A_SIDE, "gun_quadro", new GunConfig()
                .dura(400).draw(7).inspect(40).crosshair(Crosshair.L_CIRCUMFLEX).hideCrosshair(false)
                .rec(new Receiver(0)
                        .dmg(40F).spreadHipfire(0F).delay(10).reload(55).jam(40).sound(HBMSoundHandler.rpgShoot, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 4).addConfigs(rocket_qd))
                        .offset(1, -0.0625 * 1.5, -0.1875D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_ROCKET))
                .setupStandardConfiguration()
                .anim(LAMBDA_QUADRO_ANIMS).orchestra(Orchestras.ORCHESTRA_QUADRO)
        );

        ModItems.gun_missile_launcher = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.A_SIDE, "gun_missile_launcher", new GunConfig()
                .dura(500).draw(20).inspect(40).crosshair(Crosshair.L_CIRCUMFLEX).hideCrosshair(false)
                .rec(new Receiver(0)
                        .dmg(50F).spreadHipfire(0F).delay(5).reload(48).jam(33).sound(HBMSoundHandler.rpgShoot, 1.0F, 1.0F)
                        .mag(new MagazineSingleReload(0, 1).addConfigs(rocket_ml))
                        .offset(1, -0.0625 * 1.5, -0.1875D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_ROCKET))
                .setupStandardConfiguration().pp(LAMBDA_MISSILE_LAUNCHER_PRIMARY_PRESS)
                .anim(LAMBDA_MISSILE_LAUNCHER_ANIMS).orchestra(Orchestras.ORCHESTRA_MISSILE_LAUNCHER)
        );
    }

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_STINGER_SECONDARY_PRESS = (stack, ctx) -> ItemGunStinger.setIsLockingOn(stack, true);
    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_STINGER_SECONDARY_RELEASE = (stack, ctx) -> ItemGunStinger.setIsLockingOn(stack, false);

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_MISSILE_LAUNCHER_PRIMARY_PRESS = (stack, ctx) -> {
        if(ItemGunBaseNT.getIsAiming(stack)) {
            int target = ItemGunStinger.getLockonTarget(ctx.getPlayer(), 150D, 20D);
            if(target != -1) {
                ItemGunBaseNT.setLockonTarget(stack, target);
                ItemGunBaseNT.setIsLockedOn(stack, true);
            }
        }
        Lego.LAMBDA_STANDARD_CLICK_PRIMARY.accept(stack, ctx);
        ItemGunBaseNT.setIsLockedOn(stack, false);
    };

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_RECOIL_ROCKET = (stack, ctx) -> { };

    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.AnimType, BusAnimationSedna> LAMBDA_PANZERSCHRECK_ANIMS = (stack, type) -> {
        boolean empty = ((ItemGunBaseNT) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, MainRegistry.proxy.me().inventory) <= 0;
        switch(type) {
            case EQUIP: return new BusAnimationSedna()
                    .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(60, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
            case RELOAD: return new BusAnimationSedna()
                    .addBus("RELOAD", new BusAnimationSequenceSedna().addPos(90, 0, 0, 750, IType.SIN_FULL).addPos(90, 0, 0, 1000).addPos(0, 0, 0, 750, IType.SIN_FULL))
                    .addBus("ROCKET", new BusAnimationSequenceSedna().addPos(0, -3, -6, 0).addPos(0, -3, -6, 750).addPos(0, 0, -6.5, 500, IType.SIN_DOWN).addPos(0, 0, 0, 350, IType.SIN_UP));
            case JAMMED: empty = false;
            case INSPECT:
                return new BusAnimationSedna()
                        .addBus("RELOAD", new BusAnimationSequenceSedna().addPos(90, 0, 0, 750, IType.SIN_FULL).addPos(90, 0, 0, 500).addPos(0, 0, 0, 750, IType.SIN_FULL))
                        .addBus("ROCKET", new BusAnimationSequenceSedna().addPos(0, empty ? -3 : 0, 0, 0));
        }
        return null;
    };

    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.AnimType, BusAnimationSedna> LAMBDA_QUADRO_ANIMS = (stack, type) -> switch (type) {
        case EQUIP -> new BusAnimationSedna()
                .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(60, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
        case CYCLE -> new BusAnimationSedna()
                .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(0, 0, -0.5, 50).addPos(0, 0, 0, 50));
        case RELOAD -> new BusAnimationSedna()
                .addBus("RELOAD_ROTATE", new BusAnimationSequenceSedna().addPos(0, 0, 60, 500, IType.SIN_FULL).addPos(0, 0, 60, 1500).addPos(0, 0, 0, 750, IType.SIN_FULL))
                .addBus("RELOAD_PUSH", new BusAnimationSequenceSedna().addPos(-1, -1, 0, 0).addPos(-1, -1, 0, 500).addPos(-1, 0, 0, 350).addPos(0, 0, 0, 1000));
        case JAMMED, INSPECT -> new BusAnimationSedna()
                .addBus("RELOAD_ROTATE", new BusAnimationSequenceSedna().addPos(0, 0, 60, 750, IType.SIN_FULL).addPos(0, 0, 60, 500).addPos(0, 0, 0, 750, IType.SIN_FULL));
        default -> null;
    };

    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.AnimType, BusAnimationSedna> LAMBDA_MISSILE_LAUNCHER_ANIMS = (stack, type) -> switch (type) {
        case EQUIP -> new BusAnimationSedna()
                .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(60, 0, 0, 0).addPos(0, 0, 0, 1000, IType.SIN_DOWN));
        case RELOAD -> new BusAnimationSedna()
                .addBus("BARREL", new BusAnimationSequenceSedna().addPos(0, 0, 1.5, 150).addPos(0, 0, 1.5, 2100).addPos(0, 0, 0, 150))
                .addBus("OPEN", new BusAnimationSequenceSedna().addPos(0, 0, 0, 250).addPos(90, 0, 0, 500, IType.SIN_FULL).addPos(90, 0, 0, 1000).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(0, 0, 0, 2250).addPos(-1, 0, 0, 150, IType.SIN_DOWN).addPos(0, 0, 0, 150, IType.SIN_UP))
                .addBus("MISSILE", new BusAnimationSequenceSedna().addPos(-10, 0, 0, 0).addPos(-10, 0, 0, 750).addPos(3, 0, 2, 0).addPos(0, 0, -6, 350, IType.SIN_FULL).addPos(0, 0, 0, 350, IType.SIN_UP));
        case JAMMED, INSPECT -> new BusAnimationSedna()
                .addBus("BARREL", new BusAnimationSequenceSedna().addPos(0, 0, 1.5, 150).addPos(0, 0, 1.5, 1350).addPos(0, 0, 0, 150))
                .addBus("OPEN", new BusAnimationSequenceSedna().addPos(0, 0, 0, 250).addPos(90, 0, 0, 500, IType.SIN_FULL).addPos(90, 0, 0, 250).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(0, 0, 0, 1500).addPos(-1, 0, 0, 150, IType.SIN_DOWN).addPos(0, 0, 0, 150, IType.SIN_UP));
        default -> null;
    };
}
