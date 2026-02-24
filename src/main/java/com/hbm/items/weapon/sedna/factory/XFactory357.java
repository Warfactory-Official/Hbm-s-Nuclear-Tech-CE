package com.hbm.items.weapon.sedna.factory;

import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.render.anim.sedna.BusAnimationKeyframeSedna.IType;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.BusAnimationSequenceSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.render.misc.RenderScreenOverlay.Crosshair;
import net.minecraft.item.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class XFactory357 {
    public static BulletConfig m357_bp;
    public static BulletConfig m357_sp;
    public static BulletConfig m357_fmj;
    public static BulletConfig m357_jhp;
    public static BulletConfig m357_ap;
    public static BulletConfig m357_express;

    public static void init() {
        m357_bp = new BulletConfig().setItem(GunFactory.EnumAmmo.M357_BP).setCasing(ItemEnums.EnumCasingType.SMALL, 16).setDamage(0.75F).setBlackPowder(true);
        m357_sp = new BulletConfig().setItem(GunFactory.EnumAmmo.M357_SP).setCasing(ItemEnums.EnumCasingType.SMALL, 8);
        m357_fmj = new BulletConfig().setItem(GunFactory.EnumAmmo.M357_FMJ).setCasing(ItemEnums.EnumCasingType.SMALL, 8).setDamage(0.8F).setThresholdNegation(2F).setArmorPiercing(0.1F);
        m357_jhp = new BulletConfig().setItem(GunFactory.EnumAmmo.M357_JHP).setCasing(ItemEnums.EnumCasingType.SMALL, 8).setDamage(1.5F).setHeadshot(1.5F).setArmorPiercing(-0.25F);
        m357_ap = new BulletConfig().setItem(GunFactory.EnumAmmo.M357_AP).setCasing(ItemEnums.EnumCasingType.SMALL_STEEL, 8).setDoesPenetrate(true).setDamageFalloffByPen(false).setDamage(1.5F).setThresholdNegation(5F).setArmorPiercing(0.15F);
        m357_express = new BulletConfig().setItem(GunFactory.EnumAmmo.M357_EXPRESS).setCasing(ItemEnums.EnumCasingType.SMALL, 8).setDoesPenetrate(true).setDamage(1.5F).setThresholdNegation(2F).setArmorPiercing(0.1F).setWear(1.5F);

        ModItems.gun_light_revolver = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.A_SIDE, "gun_light_revolver", new GunConfig()
                .dura(300).draw(4).inspect(23).crosshair(Crosshair.CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(7.5F).delay(16).reload(55).jam(45).sound(HBMSoundHandler.firePistol, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 6).addConfigs(m357_bp, m357_sp, m357_fmj, m357_jhp, m357_ap, m357_express))
                        .offset(0.75, -0.0625, -0.3125D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_ATLAS))
                .setupStandardConfiguration()
                .anim(LAMBDA_ATLAS_ANIMS).orchestra(Orchestras.ORCHESTRA_ATLAS)
        );
        ModItems.gun_light_revolver_atlas = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.B_SIDE, "gun_light_revolver_atlas", new GunConfig()
                .dura(300).draw(4).inspect(23).crosshair(Crosshair.CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(12.5F).delay(16).reload(55).jam(45).sound(HBMSoundHandler.firePistol, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 6).addConfigs(m357_bp, m357_sp, m357_fmj, m357_jhp, m357_ap, m357_express))
                        .offset(0.75, -0.0625, -0.3125D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_ATLAS))
                .setupStandardConfiguration()
                .anim(LAMBDA_ATLAS_ANIMS).orchestra(Orchestras.ORCHESTRA_ATLAS)
        );
        ModItems.gun_light_revolver_dani = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.LEGENDARY, "gun_light_revolver_dani",
                new GunConfig().dura(30_000).draw(20).inspect(23).crosshair(Crosshair.CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                        .rec(new Receiver(0)
                                .dmg(15F).spreadHipfire(0F).delay(11).reload(55).jam(45).sound(HBMSoundHandler.firePistol, 1.0F, 1.1F)
                                .mag(new MagazineFullReload(0, 6).addConfigs(m357_bp, m357_sp, m357_fmj, m357_jhp, m357_ap, m357_express))
                                .offset(0.75, -0.0625, 0.3125D)
                                .setupStandardFire().recoil(LAMBDA_RECOIL_DANI))
                        .pp(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).pr(Lego.LAMBDA_STANDARD_RELOAD)
                        .decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
                        .anim(LAMBDA_DANI_ANIMS).orchestra(Orchestras.ORCHESTRA_DANI),
                new GunConfig().dura(30_000).draw(20).inspect(23).crosshair(Crosshair.CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                        .rec(new Receiver(0)
                                .dmg(15F).spreadHipfire(0F).delay(11).reload(55).jam(45).sound(HBMSoundHandler.firePistol, 1.0F, 0.9F)
                                .mag(new MagazineFullReload(1, 6).addConfigs(m357_bp, m357_sp, m357_fmj, m357_jhp, m357_ap, m357_express))
                                .offset(0.75, -0.0625, -0.3125D)
                                .setupStandardFire().recoil(LAMBDA_RECOIL_DANI))
                        .ps(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).pr(Lego.LAMBDA_STANDARD_RELOAD)
                        .decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
                        .anim(LAMBDA_DANI_ANIMS).orchestra(Orchestras.ORCHESTRA_DANI)
        );
    }

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_RECOIL_ATLAS = (stack, ctx) -> ItemGunBaseNT.setupRecoil(10, (float) (ctx.getPlayer().getRNG().nextGaussian() * 1.5));

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_RECOIL_DANI = (stack, ctx) -> ItemGunBaseNT.setupRecoil(5, (float) (ctx.getPlayer().getRNG().nextGaussian() * 0.75));

    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.GunAnimation, BusAnimationSedna> LAMBDA_ATLAS_ANIMS = (stack, type) -> switch (type) {
        case EQUIP -> new BusAnimationSedna()
                .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(-90, 0, 0, 0).addPos(0, 0, 0, 350, IType.SIN_DOWN));
        case CYCLE -> new BusAnimationSedna()
                .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(0, 0, 0, 50).addPos(0, 0, -3, 50).addPos(0, 0, 0, 250))
                .addBus("HAMMER", new BusAnimationSequenceSedna().addPos(0, 0, 1, 50).addPos(0, 0, 1, 550).addPos(0, 0, 0, 200))
                .addBus("DRUM", new BusAnimationSequenceSedna().addPos(0, 0, 0, 600).addPos(0, 0, 1, 200));
        case CYCLE_DRY -> new BusAnimationSedna()
                .addBus("HAMMER", new BusAnimationSequenceSedna().addPos(0, 0, 1, 50).addPos(0, 0, 1, 550).addPos(0, 0, 0, 200))
                .addBus("DRUM", new BusAnimationSequenceSedna().addPos(0, 0, 0, 600).addPos(0, 0, 1, 200));
        case RELOAD -> new BusAnimationSedna()
                .addBus("LATCH", new BusAnimationSequenceSedna().addPos(0, 0, 90, 300).addPos(0, 0, 90, 2000).addPos(0, 0, 0, 150))
                .addBus("FRONT", new BusAnimationSequenceSedna().addPos(0, 0, 0, 200).addPos(0, 0, 45, 150).addPos(0, 0, 45, 2000).addPos(0, 0, 0, 75))
                .addBus("RELOAD_ROT", new BusAnimationSequenceSedna().addPos(0, 0, 0, 300).addPos(60, 0, 0, 500).addPos(60, 0, 0, 500).addPos(0, -90, -90, 0).addPos(0, -90, -90, 600).addPos(0, 0, 0, 300).addPos(0, 0, 0, 100).addPos(-45, 0, 0, 50).addPos(-45, 0, 0, 100).addPos(0, 0, 0, 300))
                .addBus("RELOAD_MOVE", new BusAnimationSequenceSedna().addPos(0, 0, 0, 300).addPos(0, -15, 0, 1000).addPos(0, 0, 0, 450))
                .addBus("DRUM_PUSH", new BusAnimationSequenceSedna().addPos(0, 0, 0, 1600).addPos(0, 0, -5, 0).addPos(0, 0, 0, 300));
        case INSPECT -> new BusAnimationSedna()
                .addBus("LATCH", new BusAnimationSequenceSedna().addPos(0, 0, 90, 300).addPos(0, 0, 90, 1000).addPos(0, 0, 0, 150))
                .addBus("FRONT", new BusAnimationSequenceSedna().addPos(0, 0, 0, 200).addPos(0, 0, 45, 150).addPos(0, 0, 45, 1000).addPos(0, 0, 0, 75))
                .addBus("RELOAD_ROT", new BusAnimationSequenceSedna().addPos(0, 0, 0, 300).addPos(45, 0, 0, 500, IType.SIN_FULL).addPos(45, 0, 0, 500).addPos(-45, 0, 0, 50).addPos(-45, 0, 0, 100).addPos(0, 0, 0, 300))
                .addBus("RELOAD_MOVE", new BusAnimationSequenceSedna().addPos(0, 0, 0, 300).addPos(0, -2.5, 0, 500, IType.SIN_FULL).addPos(0, -2.5, 0, 500).addPos(0, 0, 0, 350));
        case JAMMED -> new BusAnimationSedna()
                .addBus("LATCH", new BusAnimationSequenceSedna().addPos(0, 0, 0, 500).addPos(0, 0, 90, 300).addPos(0, 0, 90, 1000).addPos(0, 0, 0, 150))
                .addBus("FRONT", new BusAnimationSequenceSedna().addPos(0, 0, 0, 500).addPos(0, 0, 0, 200).addPos(0, 0, 45, 150).addPos(0, 0, 45, 1000).addPos(0, 0, 0, 75))
                .addBus("RELOAD_ROT", new BusAnimationSequenceSedna().addPos(0, 0, 0, 500).addPos(0, 0, 0, 300).addPos(45, 0, 0, 500, IType.SIN_FULL).addPos(45, 0, 0, 500).addPos(-45, 0, 0, 50).addPos(-45, 0, 0, 100).addPos(0, 0, 0, 300))
                .addBus("RELOAD_MOVE", new BusAnimationSequenceSedna().addPos(0, 0, 0, 500).addPos(0, 0, 0, 300).addPos(0, -2.5, 0, 500, IType.SIN_FULL).addPos(0, -2.5, 0, 500).addPos(0, 0, 0, 350));
        default -> null;
    };

    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.GunAnimation, BusAnimationSedna> LAMBDA_DANI_ANIMS = (stack, type) -> switch (type) {
        case EQUIP ->
                new BusAnimationSedna().addBus("EQUIP", new BusAnimationSequenceSedna().addPos(360 * 3, 0, 0, 1000, IType.SIN_DOWN));
        case CYCLE -> new BusAnimationSedna()
                .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(0, 0, 0, 50).addPos(0, 0, -3, 50).addPos(0, 0, 0, 250))
                .addBus("HAMMER", new BusAnimationSequenceSedna().addPos(0, 0, 1, 50).addPos(0, 0, 1, 300).addPos(0, 0, 0, 200))
                .addBus("DRUM", new BusAnimationSequenceSedna().addPos(0, 0, 0, 350).addPos(0, 0, 1, 200));
        case CYCLE_DRY -> new BusAnimationSedna()
                .addBus("HAMMER", new BusAnimationSequenceSedna().addPos(0, 0, 1, 50).addPos(0, 0, 1, 200).addPos(0, 0, 0, 200))
                .addBus("DRUM", new BusAnimationSequenceSedna().addPos(0, 0, 0, 350).addPos(0, 0, 1, 200));
        default -> LAMBDA_ATLAS_ANIMS.apply(stack, type);
    };
}
