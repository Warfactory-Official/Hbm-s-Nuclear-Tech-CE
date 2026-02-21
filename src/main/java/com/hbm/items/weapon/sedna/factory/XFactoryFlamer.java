package com.hbm.items.weapon.sedna.factory;

import com.hbm.capability.HbmLivingCapability;
import com.hbm.capability.HbmLivingProps;
import com.hbm.entity.effect.EntityFireLingering;
import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.impl.ItemGunChemthrower;
import com.hbm.items.weapon.sedna.mags.MagazineFluid;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.particle.helper.FlameCreator;
import com.hbm.render.anim.sedna.BusAnimationKeyframeSedna.IType;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.BusAnimationSequenceSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.render.misc.RenderScreenOverlay.Crosshair;
import com.hbm.util.DamageResistanceHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class XFactoryFlamer {
    public static BulletConfig flame_nograv;

    public static BulletConfig flame_diesel;
    public static BulletConfig flame_gas;
    public static BulletConfig flame_napalm;
    public static BulletConfig flame_balefire;

    public static BulletConfig flame_topaz_diesel;
    public static BulletConfig flame_topaz_gas;
    public static BulletConfig flame_topaz_napalm;
    public static BulletConfig flame_topaz_balefire;

    public static BulletConfig flame_daybreaker_diesel;
    public static BulletConfig flame_daybreaker_gas;
    public static BulletConfig flame_daybreaker_napalm;
    public static BulletConfig flame_daybreaker_balefire;

    public static Consumer<Entity> LAMBDA_FIRE = (bullet) -> {
        if(bullet.world.isRemote && MainRegistry.proxy.me().getDistance(bullet) < 100) FlameCreator.composeEffectClient(bullet.world, bullet.posX, bullet.posY - 0.125, bullet.posZ, FlameCreator.META_FIRE);
    };
    public static Consumer<Entity> LAMBDA_BALEFIRE = (bullet) -> {
        if(bullet.world.isRemote && MainRegistry.proxy.me().getDistance(bullet) < 100) FlameCreator.composeEffectClient(bullet.world, bullet.posX, bullet.posY - 0.125, bullet.posZ, FlameCreator.META_BALEFIRE);
    };
    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_IGNITE_FIRE = (bullet, mop) -> {
        if(mop.entityHit instanceof EntityLivingBase) {
            HbmLivingCapability.IEntityHbmProps props = HbmLivingProps.getData((EntityLivingBase) mop.entityHit);
            if(props.getFire() < 100) props.setFire(100);
        }
    };
    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_IGNITE_BALEFIRE = (bullet, mop) -> {
        if(mop.entityHit instanceof EntityLivingBase) {
            HbmLivingCapability.IEntityHbmProps props = HbmLivingProps.getData((EntityLivingBase) mop.entityHit);
            if(props.getBalefire() < 200) props.setBalefire(200);
        }
    };
    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_LINGER_DIESEL = (bullet, mop) -> { if(!igniteIfPossible(bullet, mop)) spawnFire(bullet, mop, 2F, 1F, 100, EntityFireLingering.TYPE_DIESEL); };
    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_LINGER_GAS = (bullet, mop) -> { igniteIfPossible(bullet, mop); };
    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_LINGER_NAPALM = (bullet, mop) -> { if(!igniteIfPossible(bullet, mop)) spawnFire(bullet, mop, 2.5F, 1F, 200, EntityFireLingering.TYPE_DIESEL); };
    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_LINGER_BALEFIRE = (bullet, mop) -> { spawnFire(bullet, mop, 3F, 1F, 300, EntityFireLingering.TYPE_BALEFIRE); };

    public static boolean igniteIfPossible(EntityBulletBaseMK4 bullet, RayTraceResult mop) {
        if(mop.typeOfHit == mop.typeOfHit.BLOCK) {
            World world = bullet.world;
            Block b = world.getBlockState(mop.getBlockPos()).getBlock();
            ForgeDirection dir = ForgeDirection.getOrientation(mop.sideHit);
            if(b.isFlammable(world, mop.getBlockPos(), Objects.requireNonNull(dir.getOpposite().toEnumFacing()))) {
                if(world.getBlockState(mop.getBlockPos().add(dir.offsetX, dir.offsetY, dir.offsetZ)).getBlock().isAir(
                        world.getBlockState(mop.getBlockPos().add(dir.offsetX, dir.offsetY, dir.offsetZ)), world,
                        mop.getBlockPos().add(dir.offsetX, dir.offsetY, dir.offsetZ))) {
                    world.setBlockState(mop.getBlockPos().add(dir.offsetX, dir.offsetY, dir.offsetZ), Blocks.FIRE.getDefaultState());
                    return true;
                }
            }
            bullet.setDead();
        }
        return false;
    }

    public static void spawnFire(EntityBulletBaseMK4 bullet, RayTraceResult mop, float width, float height, int duration, int type) {
        if(mop.typeOfHit == mop.typeOfHit.BLOCK) {
            List<EntityFireLingering> fires = bullet.world.getEntitiesWithinAABB(EntityFireLingering.class,
                    new AxisAlignedBB(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z).expand(width / 2 + 0.5, height / 2 + 0.5, width / 2 + 0.5));
            if(fires.isEmpty()) {
                EntityFireLingering fire = new EntityFireLingering(bullet.world).setArea(width, height).setDuration(duration).setType(type);
                fire.setPosition(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
                bullet.world.spawnEntity(fire);
            }
            bullet.setDead();
        }
    }

    public static void init() {
        flame_diesel = new BulletConfig().setItem(GunFactory.EnumAmmo.FLAME_DIESEL).setCasing(new ItemStack(ModItems.plate_steel, 2), 500).setupDamageClass(DamageResistanceHandler.DamageClass.FIRE).setLife(100).setVel(1F).setGrav(0.02F).setReloadCount(500).setSelfDamageDelay(20).setKnockback(0F)
                .setOnUpdate(LAMBDA_FIRE).setOnRicochet(LAMBDA_LINGER_DIESEL);
        flame_gas = new BulletConfig().setItem(GunFactory.EnumAmmo.FLAME_GAS).setCasing(new ItemStack(ModItems.plate_steel, 2), 500).setupDamageClass(DamageResistanceHandler.DamageClass.FIRE).setLife(10).setSpread(0.05F).setVel(1F).setGrav(0.0F).setReloadCount(500).setSelfDamageDelay(20).setKnockback(0F)
                .setOnUpdate(LAMBDA_FIRE).setOnRicochet(LAMBDA_LINGER_GAS);
        flame_napalm = new BulletConfig().setItem(GunFactory.EnumAmmo.FLAME_NAPALM).setCasing(new ItemStack(ModItems.plate_steel, 2), 500).setupDamageClass(DamageResistanceHandler.DamageClass.FIRE).setLife(200).setVel(1F).setGrav(0.02F).setReloadCount(500).setSelfDamageDelay(20).setKnockback(0F)
                .setOnUpdate(LAMBDA_FIRE).setOnRicochet(LAMBDA_LINGER_NAPALM);
        flame_balefire = new BulletConfig().setItem(GunFactory.EnumAmmo.FLAME_BALEFIRE).setCasing(new ItemStack(ModItems.plate_steel, 2), 500).setupDamageClass(DamageResistanceHandler.DamageClass.FIRE).setLife(200).setVel(1F).setGrav(0.02F).setReloadCount(500).setSelfDamageDelay(20).setKnockback(0F)
                .setOnUpdate(LAMBDA_BALEFIRE).setOnRicochet(LAMBDA_LINGER_BALEFIRE);

        flame_nograv = flame_diesel.clone().setGrav(0);

        flame_topaz_diesel = flame_diesel		.clone().setProjectiles(2).setSpread(0.05F).setLife(60).setGrav(0.0F);
        flame_topaz_gas = flame_gas				.clone().setProjectiles(2).setSpread(0.05F);
        flame_topaz_napalm = flame_napalm		.clone().setProjectiles(2).setSpread(0.05F).setLife(60).setGrav(0.0F);
        flame_topaz_balefire = flame_balefire	.clone().setProjectiles(2).setSpread(0.05F).setLife(60).setGrav(0.0F);

        flame_daybreaker_diesel = flame_diesel.clone().setLife(200).setVel(2F).setGrav(0.035F)
                .setOnImpact((bullet, mop) -> { Lego.standardExplode(bullet, mop, 5F); spawnFire(bullet, mop, 6F, 2F, 200, EntityFireLingering.TYPE_DIESEL); bullet.setDead(); });
        flame_daybreaker_gas = flame_gas.clone().setLife(200).setVel(2F).setGrav(0.035F)
                .setOnImpact((bullet, mop) -> { Lego.standardExplode(bullet, mop, 5F); bullet.setDead(); });
        flame_daybreaker_napalm = flame_napalm.clone().setLife(200).setVel(2F).setGrav(0.035F)
                .setOnImpact((bullet, mop) -> { Lego.standardExplode(bullet, mop, 7.5F); spawnFire(bullet, mop, 6F, 2F, 300, EntityFireLingering.TYPE_DIESEL); bullet.setDead(); });
        flame_daybreaker_balefire = flame_balefire.clone().setLife(200).setVel(2F).setGrav(0.035F)
                .setOnImpact((bullet, mop) -> { Lego.standardExplode(bullet, mop, 5F); spawnFire(bullet, mop, 7.5F, 2.5F, 400, EntityFireLingering.TYPE_BALEFIRE); bullet.setDead(); });

        ModItems.gun_flamer = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.A_SIDE, "gun_flamer", new GunConfig()
                .dura(20_000).draw(10).inspect(17).crosshair(Crosshair.L_CIRCLE)
                .rec(new Receiver(0)
                        .dmg(1F).spreadHipfire(0F).delay(1).auto(true).reload(90).jam(17)
                        .mag(new MagazineFullReload(0, 300).addConfigs(flame_diesel, flame_gas, flame_napalm, flame_balefire))
                        .offset(0.75, -0.0625, -0.25D)
                        .setupStandardFire())
                .setupStandardConfiguration()
                .anim(LAMBDA_FLAMER_ANIMS).orchestra(Orchestras.ORCHESTRA_FLAMER)
        );
        ModItems.gun_flamer_topaz = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.B_SIDE, "gun_flamer_topaz", new GunConfig()
                .dura(20_000).draw(10).inspect(17).crosshair(Crosshair.L_CIRCLE)
                .rec(new Receiver(0)
                        .dmg(1.5F).spreadHipfire(0F).delay(1).auto(true).reload(90).jam(17)
                        .mag(new MagazineFullReload(0, 500).addConfigs(flame_topaz_diesel, flame_topaz_gas, flame_topaz_napalm, flame_topaz_balefire))
                        .offset(0.75, -0.0625, -0.25D)
                        .setupStandardFire())
                .setupStandardConfiguration()
                .anim(LAMBDA_FLAMER_ANIMS).orchestra(Orchestras.ORCHESTRA_FLAMER)
        );
        ModItems.gun_flamer_daybreaker = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.LEGENDARY, "gun_flamer_daybreaker", new GunConfig()
                .dura(20_000).draw(10).inspect(17).crosshair(Crosshair.L_CIRCLE)
                .rec(new Receiver(0)
                        .dmg(25F).spreadHipfire(0F).delay(10).auto(true).reload(90).jam(17).sound(HBMSoundHandler.fireBlackPowder, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 50).addConfigs(flame_daybreaker_diesel, flame_daybreaker_gas, flame_daybreaker_napalm, flame_daybreaker_balefire))
                        .offset(0.75, -0.0625, -0.25D)
                        .setupStandardFire())
                .setupStandardConfiguration()
                .anim(LAMBDA_FLAMER_ANIMS).orchestra(Orchestras.ORCHESTRA_FLAMER_DAYBREAKER)
        );

        ModItems.gun_chemthrower = new ItemGunChemthrower(ItemGunBaseNT.WeaponQuality.A_SIDE, "gun_chemthrower", new GunConfig()
                .dura(90_000).draw(10).inspect(17).crosshair(Crosshair.L_CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .delay(1).spreadHipfire(0F).auto(true)
                        .mag(new MagazineFluid(0, 3_000))
                        .offset(0.75, -0.0625, -0.25D)
                        .canFire(ItemGunChemthrower.LAMBDA_CAN_FIRE).fire(ItemGunChemthrower.LAMBDA_FIRE))
                .pp(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
                .anim(LAMBDA_CHEMTHROWER_ANIMS).orchestra(Orchestras.ORCHESTRA_CHEMTHROWER)
        );
    }

    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.AnimType, BusAnimationSedna> LAMBDA_FLAMER_ANIMS = (stack, type) -> {
        switch(type) {
            case EQUIP: return new BusAnimationSedna()
                    .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(-45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
            case RELOAD: return ResourceManager.flamethrower_anim.get("Reload");
            case INSPECT:
            case JAMMED: return new BusAnimationSedna()
                    .addBus("ROTATE", new BusAnimationSequenceSedna().addPos(0, 0, 45, 250, IType.SIN_FULL).addPos(0, 0, 45, 350).addPos(0, 0, -15, 150, IType.SIN_FULL).addPos(0, 0, 0, 100, IType.SIN_FULL));
        }

        return null;
    };

    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.AnimType, BusAnimationSedna> LAMBDA_CHEMTHROWER_ANIMS = (stack, type) -> {
        switch(type) {
            case EQUIP: return new BusAnimationSedna()
                    .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(-45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
        }

        return null;
    };
}
