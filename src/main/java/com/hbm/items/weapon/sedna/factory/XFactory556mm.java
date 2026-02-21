package com.hbm.items.weapon.sedna.factory;

import com.hbm.Tags;
import com.hbm.config.ClientConfig;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.items.weapon.sedna.mods.WeaponModManager;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.particle.SpentCasing;
import com.hbm.render.anim.sedna.BusAnimationKeyframeSedna.IType;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.BusAnimationSequenceSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.render.misc.RenderScreenOverlay.Crosshair;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class XFactory556mm {
    public static final ResourceLocation scope = new ResourceLocation(Tags.MODID, "textures/misc/scope_bolt.png");

    public static BulletConfig r556_sp;
    public static BulletConfig r556_fmj;
    public static BulletConfig r556_jhp;
    public static BulletConfig r556_ap;

    public static BulletConfig r556_inc_sp;
    public static BulletConfig r556_inc_fmj;
    public static BulletConfig r556_inc_jhp;
    public static BulletConfig r556_inc_ap;

    public static void init() {
        SpentCasing casing556 = new SpentCasing(SpentCasing.CasingType.BOTTLENECK).setColor(SpentCasing.COLOR_CASE_BRASS).setScale(0.8F);
        r556_sp = new BulletConfig().setItem(GunFactory.EnumAmmo.R556_SP).setCasing(ItemEnums.EnumCasingType.SMALL, 8)
                .setCasing(casing556.clone().register("r556"));
        r556_fmj = new BulletConfig().setItem(GunFactory.EnumAmmo.R556_FMJ).setCasing(ItemEnums.EnumCasingType.SMALL, 8).setDamage(0.8F).setThresholdNegation(4F).setArmorPiercing(0.1F)
                .setCasing(casing556.clone().register("r556fmj"));
        r556_jhp = new BulletConfig().setItem(GunFactory.EnumAmmo.R556_JHP).setCasing(ItemEnums.EnumCasingType.SMALL, 8).setDamage(1.5F).setHeadshot(1.5F).setArmorPiercing(-0.25F)
                .setCasing(casing556.clone().register("r556jhp"));
        r556_ap = new BulletConfig().setItem(GunFactory.EnumAmmo.R556_AP).setCasing(ItemEnums.EnumCasingType.SMALL_STEEL, 8).setDoesPenetrate(true).setDamageFalloffByPen(false).setDamage(1.5F).setThresholdNegation(10F).setArmorPiercing(0.15F)
                .setCasing(casing556.clone().setColor(SpentCasing.COLOR_CASE_44).register("r556ap"));

        ModItems.gun_g3 = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.A_SIDE, "gun_g3", new GunConfig()
                .dura(3_000).draw(10).inspect(33).crosshair(Crosshair.CIRCLE).smoke(LAMBDA_SMOKE)
                .rec(new Receiver(0)
                        .dmg(5F).delay(2).auto(true).dry(15).spread(0.0F).reload(50).jam(47).sound(HBMSoundHandler.fireAssault, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 30).addConfigs(r556_sp, r556_fmj, r556_jhp, r556_ap))
                        .offset(1, -0.0625 * 2.5, -0.25D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_G3))
                .setupStandardConfiguration().ps(Lego.LAMBDA_STANDARD_CLICK_SECONDARY)
                .anim(LAMBDA_G3_ANIMS).orchestra(Orchestras.ORCHESTRA_G3)
        );
        ModItems.gun_g3_zebra = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.B_SIDE, "gun_g3_zebra", new GunConfig()
                .dura(6_000).draw(10).inspect(33).crosshair(Crosshair.CIRCLE).smoke(LAMBDA_SMOKE).scopeTexture(scope)
                .rec(new Receiver(0)
                        .dmg(7.5F).delay(2).auto(true).dry(15).spreadHipfire(0.01F).reload(50).jam(47).sound(HBMSoundHandler.fireSilenced, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 30).addConfigs(r556_inc_sp, r556_inc_fmj, r556_inc_jhp, r556_inc_ap))
                        .offset(1, -0.0625 * 2.5, -0.25D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_ZEBRA))
                .setupStandardConfiguration().ps(Lego.LAMBDA_STANDARD_CLICK_SECONDARY)
                .anim(LAMBDA_G3_ANIMS).orchestra(Orchestras.ORCHESTRA_G3)
        ).setNameMutator(LAMBDA_NAME_G3);

        ModItems.gun_stg77 = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.A_SIDE, "gun_stg77", new GunConfig()
                .dura(3_000).draw(10).inspect(125).crosshair(Crosshair.CIRCLE).scopeTexture(scope).smoke(LAMBDA_SMOKE)
                .rec(new Receiver(0)
                        .dmg(10F).delay(2).dry(15).auto(true).spread(0.0F).reload(46).jam(0).sound(HBMSoundHandler.fireAssault, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 30).addConfigs(r556_sp, r556_fmj, r556_jhp, r556_ap))
                        .offset(1, -0.0625 * 2.5, -0.25D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_STG))
                .pp(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).ps(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).pr(Lego.LAMBDA_STANDARD_RELOAD).pt(Lego.LAMBDA_TOGGLE_AIM)
                .decider(LAMBDA_STG77_DECIDER)
                .anim(LAMBDA_STG77_ANIMS).orchestra(Orchestras.ORCHESTRA_STG77)
        );
    }

    public static Function<ItemStack, String> LAMBDA_NAME_G3 = (stack) -> {
        if(WeaponModManager.hasUpgrade(stack, 0, WeaponModManager.ID_SILENCER) &&
                WeaponModManager.hasUpgrade(stack, 0, WeaponModManager.ID_NO_STOCK) &&
                WeaponModManager.hasUpgrade(stack, 0, WeaponModManager.ID_FURNITURE_BLACK) &&
                WeaponModManager.hasUpgrade(stack, 0, WeaponModManager.ID_SCOPE)) return stack.getTranslationKey() + "_infiltrator";
        if(!WeaponModManager.hasUpgrade(stack, 0, WeaponModManager.ID_NO_STOCK) &&
                WeaponModManager.hasUpgrade(stack, 0, WeaponModManager.ID_FURNITURE_GREEN)) return stack.getTranslationKey() + "_a3";
        return null;
    };

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_SMOKE = (stack, ctx) -> Lego.handleStandardSmoke(ctx.entity, stack, 1500, 0.075D, 1.1D, 0);

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_STG77_DECIDER = (stack, ctx) -> {
        int index = ctx.configIndex;
        ItemGunBaseNT.GunState lastState = ItemGunBaseNT.getState(stack, index);
        GunStateDecider.deciderStandardFinishDraw(stack, lastState, index);
        GunStateDecider.deciderStandardClearJam(stack, lastState, index);
        GunStateDecider.deciderStandardReload(stack, ctx, lastState, 0, index);
        GunStateDecider.deciderAutoRefire(stack, ctx, lastState, 0, index, () -> ItemGunBaseNT.getSecondary(stack, index));
    };

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_RECOIL_G3 = (stack, ctx) -> ItemGunBaseNT.setupRecoil((float) (ctx.getPlayer().getRNG().nextGaussian() * 0.25), (float) (ctx.getPlayer().getRNG().nextGaussian() * 0.25));
    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_RECOIL_ZEBRA = (stack, ctx) -> ItemGunBaseNT.setupRecoil((float) (ctx.getPlayer().getRNG().nextGaussian() * 0.125), (float) (ctx.getPlayer().getRNG().nextGaussian() * 0.125));
    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_RECOIL_STG = (stack, ctx) -> { };

    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.AnimType, BusAnimationSedna> LAMBDA_G3_ANIMS = (stack, type) -> {
        boolean empty = ((ItemGunBaseNT) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, MainRegistry.proxy.me().inventory) <= 0;
        return switch (type) {
            case EQUIP -> new BusAnimationSedna()
                    .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL));
            case CYCLE -> new BusAnimationSedna()
                    .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(0, 0, ItemGunBaseNT.getIsAiming(stack) ? -0.5 : -0.75, 25, IType.SIN_DOWN).addPos(0, 0, 0, 75, IType.SIN_FULL));
            case CYCLE_DRY -> new BusAnimationSedna()
                    .addBus("BOLT", new BusAnimationSequenceSedna().addPos(0, 0, 0, 250).addPos(0, 0, -3.25, 150).addPos(0, 0, 0, 100))
                    .addBus("LIFT", new BusAnimationSequenceSedna().addPos(0, 0, 0, 400).addPos(-1, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
            case RELOAD -> new BusAnimationSedna()
                    .addBus("MAG", new BusAnimationSequenceSedna()
                            .addPos(0, -8, 0, 250, IType.SIN_UP)    //250
                            .addPos(0, -8, 0, 1000)                    //1250
                            .addPos(0, 0, 0, 300))                    //1550
                    .addBus("BOLT", new BusAnimationSequenceSedna()
                            .addPos(0, 0, 0, 250)                    //250
                            .addPos(0, 0, -3.25, 150)                //400
                            .addPos(0, 0, -3.25, 1250)                //1750
                            .addPos(0, 0, 0, 100))                    //1850
                    .addBus("HANDLE", new BusAnimationSequenceSedna()
                            .addPos(0, 0, 0, 500)                    //500
                            .addPos(0, 0, 45, 50)                    //550
                            .addPos(0, 0, 45, 1150)                    //1700
                            .addPos(0, 0, 0, 50))                    //1750
                    .addBus("LIFT", new BusAnimationSequenceSedna()
                            .addPos(0, 0, 0, 750)                    //750
                            .addPos(-25, 0, 0, 500, IType.SIN_FULL)    //1250
                            .addPos(-25, 0, 0, 750)                    //2000
                            .addPos(0, 0, 0, 500, IType.SIN_FULL))    //3500
                    .addBus("BULLET", new BusAnimationSequenceSedna().addPos(empty ? 1 : 0, 0, 0, 0).addPos(0, 0, 0, 1000));
            case INSPECT -> new BusAnimationSedna()
                    .addBus("MAG", new BusAnimationSequenceSedna()
                            .addPos(0, -1, 0, 150)                    //150
                            .addPos(2, -1, 0, 150)                    //300
                            .addPos(2, 8, 0, 350, IType.SIN_DOWN)    //650
                            .addPos(2, -2, 0, 350, IType.SIN_UP)    //1000
                            .addPos(2, -1, 0, 50)                    //1050
                            .addPos(2, -1, 0, 100)                    //1150
                            .addPos(0, -1, 0, 150, IType.SIN_FULL)    //1300
                            .addPos(0, 0, 0, 150, IType.SIN_UP))    //1450
                    .addBus("SPEEN", new BusAnimationSequenceSedna().addPos(0, 0, 0, 300).addPos(0, 360, 360, 700))
                    .addBus("LIFT", new BusAnimationSequenceSedna().addPos(0, 0, 0, 1450).addPos(-2, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                    .addBus("BULLET", new BusAnimationSequenceSedna().addPos(empty ? 1 : 0, 0, 0, 0));
            case JAMMED -> new BusAnimationSedna()
                    .addBus("LIFT", new BusAnimationSequenceSedna().addPos(0, 0, 0, 500).addPos(-25, 0, 0, 250, IType.SIN_FULL).addPos(-25, 0, 0, 1250).addPos(0, 0, 0, 350, IType.SIN_FULL))
                    .addBus("BOLT", new BusAnimationSequenceSedna().addPos(0, 0, 0, 1000).addPos(0, 0, -3.25, 150).addPos(0, 0, 0, 100).addPos(0, 0, 0, 250).addPos(0, 0, -3.25, 150).addPos(0, 0, 0, 100));
            default -> null;
        };

    };

    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.AnimType, BusAnimationSedna> LAMBDA_STG77_ANIMS = (stack, type) -> {
        if(ClientConfig.GUN_ANIMS_LEGACY.get()) {
            switch (type) {
                case EQUIP -> {
                    return new BusAnimationSedna()
                            .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL));
                }
                case CYCLE -> {
                    return new BusAnimationSedna()
                            .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(0, 0, ItemGunBaseNT.getIsAiming(stack) ? -0.125 : -0.375, 25, IType.SIN_DOWN).addPos(0, 0, 0, 75, IType.SIN_FULL))
                            .addBus("SAFETY", new BusAnimationSequenceSedna().addPos(0.25, 0, 0, 0).addPos(0.25, 0, 0, 2000).addPos(0, 0, 0, 50));
                }
                case CYCLE_DRY -> {
                    return new BusAnimationSedna()
                            .addBus("BOLT", new BusAnimationSequenceSedna().addPos(0, 0, 0, 250).addPos(0, 0, -2, 150).addPos(0, 0, 0, 100, IType.SIN_UP))
                            .addBus("SAFETY", new BusAnimationSequenceSedna().addPos(0.25, 0, 0, 0).addPos(0.25, 0, 0, 2000).addPos(0, 0, 0, 50));
                }
                case RELOAD -> {
                    return new BusAnimationSedna()
                            .addBus("BOLT", new BusAnimationSequenceSedna().addPos(0, 0, -2, 150).addPos(0, 0, -2, 1600).addPos(0, 0, 0, 100, IType.SIN_UP))
                            .addBus("HANDLE", new BusAnimationSequenceSedna().addPos(0, 0, 0, 150).addPos(0, 0, 20, 50).addPos(0, 0, 20, 1500).addPos(0, 0, 0, 50))
                            .addBus("LIFT", new BusAnimationSequenceSedna().addPos(0, 0, 0, 200).addPos(-2, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
                }
                case INSPECT -> {
                    return new BusAnimationSedna()
                            .addBus("BOLT", new BusAnimationSequenceSedna().addPos(0, 0, -2, 150).addPos(0, 0, -2, 6100).addPos(0, 0, 0, 100, IType.SIN_UP))
                            .addBus("HANDLE", new BusAnimationSequenceSedna().addPos(0, 0, 0, 150).addPos(0, 0, 20, 50).addPos(0, 0, 20, 6000).addPos(0, 0, 0, 50))
                            .addBus("INSPECT_LEVER", new BusAnimationSequenceSedna().addPos(0, 0, 0, 500).addPos(0, 0, -10, 100).addPos(0, 0, -10, 100).addPos(0, 0, 0, 100))
                            .addBus("INSPECT_BARREL", new BusAnimationSequenceSedna().addPos(0, 0, 0, 600).addPos(0, 0, 20, 150).addPos(0, 0, 0, 400).addPos(0, 0, 0, 500).addPos(15, 0, 0, 500).addPos(15, 0, 0, 2000).addPos(0, 0, 0, 500).addPos(0, 0, 0, 500).addPos(0, 0, 20, 200).addPos(0, 0, 20, 400).addPos(0, 0, 0, 150))
                            .addBus("INSPECT_MOVE", new BusAnimationSequenceSedna().addPos(0, 0, 0, 750).addPos(0, 0, 6, 1000).addPos(2, 0, 3, 500, IType.SIN_FULL).addPos(2, 0.75, 0, 500, IType.SIN_FULL).addPos(2, 0.75, 0, 1000).addPos(2, 0, 3, 500, IType.SIN_FULL).addPos(0, 0, 6, 500).addPos(0, 0, 0, 1000))
                            .addBus("INSPECT_GUN", new BusAnimationSequenceSedna().addPos(0, 0, 0, 1750).addPos(15, 0, -70, 500, IType.SIN_FULL).addPos(15, 0, -70, 1500).addPos(0, 0, 0, 500, IType.SIN_FULL));
                }
            }
        } else {
            switch (type) {
                case EQUIP -> {
                    return new BusAnimationSedna()
                            .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL));
                }
                case CYCLE -> {
                    return new BusAnimationSedna()
                            .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(0, 0, ItemGunBaseNT.getIsAiming(stack) ? -0.125 : -0.375, 25, IType.SIN_DOWN).addPos(0, 0, 0, 75, IType.SIN_FULL))
                            .addBus("SAFETY", new BusAnimationSequenceSedna().addPos(0.25, 0, 0, 0).addPos(0.25, 0, 0, 2000).addPos(0, 0, 0, 50));
                }
                case CYCLE_DRY -> {
                    return ResourceManager.stg77_anim.get("FireDry");
                }
                case RELOAD -> {
                    return ResourceManager.stg77_anim.get("Reload");
                }
                case INSPECT -> {
                    return ResourceManager.stg77_anim.get("Inspect");
                }
            }
        }


        return null;
    };
}
