package com.hbm.items.weapon.sedna.factory;

import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.particle.SpentCasing;
import com.hbm.render.anim.sedna.BusAnimationKeyframeSedna.IType;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.BusAnimationSequenceSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.render.misc.RenderScreenOverlay;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static com.hbm.items.weapon.sedna.factory.XFactory762mm.LAMBDA_TINY_EXPLODE;

public class XFactory10ga {

    public static BulletConfig g10;
    public static BulletConfig g10_shrapnel;
    public static BulletConfig g10_du;
    public static BulletConfig g10_slug;
    public static BulletConfig g10_explosive;

    public static void init() {

        float buckshotSpread = 0.035F;
        g10 = new BulletConfig().setItem(GunFactory.EnumAmmo.G10).setCasing(ItemEnums.EnumCasingType.BUCKSHOT_ADVANCED, 4).setProjectiles(10).setDamage(1F/10F).setSpread(buckshotSpread).setRicochetAngle(15).setThresholdNegation(5F).setCasing(new SpentCasing(SpentCasing.CasingType.SHOTGUN).setColor(0xB52B2B, SpentCasing.COLOR_CASE_12GA).setScale(1F).register("10GA"));
        g10_shrapnel = new BulletConfig().setItem(GunFactory.EnumAmmo.G10_SHRAPNEL).setCasing(ItemEnums.EnumCasingType.BUCKSHOT_ADVANCED, 4).setProjectiles(10).setDamage(1F/10F).setSpread(buckshotSpread).setRicochetAngle(90).setRicochetCount(15).setThresholdNegation(5F).setCasing(new SpentCasing(SpentCasing.CasingType.SHOTGUN).setColor(0xE5DD00, SpentCasing.COLOR_CASE_12GA).setScale(1F).register("10GAShrapnel"));
        g10_du = new BulletConfig().setItem(GunFactory.EnumAmmo.G10_DU).setCasing(ItemEnums.EnumCasingType.BUCKSHOT_ADVANCED, 4).setProjectiles(10).setDamage(1F/4F).setSpread(buckshotSpread).setRicochetAngle(15).setThresholdNegation(10F).setArmorPiercing(0.2F).setDoesPenetrate(true).setDamageFalloffByPen(false).setCasing(new SpentCasing(SpentCasing.CasingType.SHOTGUN).setColor(0x538D53, SpentCasing.COLOR_CASE_12GA).setScale(1F).register("10GADU"));
        g10_slug = new BulletConfig().setItem(GunFactory.EnumAmmo.G10_SLUG).setCasing(ItemEnums.EnumCasingType.BUCKSHOT_ADVANCED, 4).setRicochetAngle(15).setThresholdNegation(10F).setArmorPiercing(0.1F).setDoesPenetrate(true).setCasing(new SpentCasing(SpentCasing.CasingType.SHOTGUN).setColor(0x808080, SpentCasing.COLOR_CASE_12GA).setScale(1F).register("10GASlug"));
        g10_explosive = new BulletConfig().setItem(GunFactory.EnumAmmo.G10_EXPLOSIVE).setCasing(ItemEnums.EnumCasingType.BUCKSHOT_ADVANCED, 4).setWear(3F).setProjectiles(10).setDamage(1F/4F).setSpread(buckshotSpread).setCasing(new SpentCasing(SpentCasing.CasingType.SHOTGUN).setColor(0xFAC943, SpentCasing.COLOR_CASE_12GA).setScale(1F).register("10GAEXP")).setOnImpact(LAMBDA_TINY_EXPLODE);

        ModItems.gun_double_barrel = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.SPECIAL, "gun_double_barrel", new GunConfig()
                .dura(1000).draw(10).inspect(39).crosshair(RenderScreenOverlay.Crosshair.L_CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(30F).rounds(2).delay(10).reload(41).reloadOnEmpty(true).sound(HBMSoundHandler.fireShotgun, 1.0F, 0.9F)
                        .mag(new MagazineFullReload(0, 2).addConfigs(g10, g10_shrapnel, g10_du, g10_slug, g10_explosive))
                        .offset(0.75, -0.0625, -0.1875)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_DOUBLE_BARREL))
                .setupStandardConfiguration().ps(LAMBDA_DOUBLE_SECONDARY)
                .anim(LAMBDA_DOUBLE_BARREL_ANIMS).orchestra(Orchestras.ORCHESTRA_DOUBLE_BARREL)
        );
        ModItems.gun_double_barrel_sacred_dragon = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.B_SIDE, "gun_double_barrel_sacred_dragon", new GunConfig()
                .dura(6000).draw(10).inspect(39).crosshair(RenderScreenOverlay.Crosshair.L_CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(45F).spreadAmmo(1.35F).rounds(2).delay(10).reload(41).reloadOnEmpty(true).sound(HBMSoundHandler.fireShotgun, 1.0F, 0.9F)
                        .mag(new MagazineFullReload(0, 2).addConfigs(g10, g10_shrapnel, g10_du, g10_slug, g10_explosive))
                        .offset(0.75, -0.0625, -0.1875)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_DOUBLE_BARREL))
                .setupStandardConfiguration().ps(LAMBDA_DOUBLE_SECONDARY)
                .anim(LAMBDA_DOUBLE_BARREL_ANIMS).orchestra(Orchestras.ORCHESTRA_DOUBLE_BARREL)
        );
        ModItems.gun_autoshotgun_heretic = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.DEBUG, "gun_autoshotgun_heretic", new GunConfig()
                .draw(20).inspect(65).reloadSequential(true).inspectCancel(false).crosshair(RenderScreenOverlay.Crosshair.L_CIRCLE).hideCrosshair(false).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(100F).delay(3).auto(true).dryfireAfterAuto(true).reload(110).jam(19).sound(HBMSoundHandler.fireShotgunAuto, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 250).addConfigs(g10, g10_shrapnel, g10_du, g10_slug, g10_explosive))
                        .offset(0.75, -0.125, -0.25)
                        .canFire(Lego.LAMBDA_STANDARD_CAN_FIRE).fire(Lego.LAMBDA_NOWEAR_FIRE).recoil(XFactory12ga.LAMBDA_RECOIL_SEXY))
                .setupStandardConfiguration()
                .anim(XFactory12ga.LAMBDA_SEXY_ANIMS).orchestra(Orchestras.ORCHESTRA_SHREDDER_SEXY)
        );
    }

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_RECOIL_DOUBLE_BARREL = (stack, ctx) -> ItemGunBaseNT.setupRecoil(10, (float) (ctx.getPlayer().getRNG().nextGaussian() * 1.5));

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_DOUBLE_SECONDARY = (stack, ctx) -> {

        EntityLivingBase entity = ctx.entity;
        EntityPlayer player = ctx.getPlayer();
        Receiver rec = ctx.config.getReceivers(stack)[0];
        int index = ctx.configIndex;
        ItemGunBaseNT.GunState state = ItemGunBaseNT.getState(stack, index);

        if(state == ItemGunBaseNT.GunState.IDLE) {

            if(rec.getCanFire(stack).apply(stack, ctx)) {
                rec.getOnFire(stack).accept(stack, ctx);

                if(rec.getFireSound(stack) != null)
                    entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, rec.getFireSound(stack), SoundCategory.PLAYERS, rec.getFireVolume(stack), rec.getFirePitch(stack));

                ItemGunBaseNT.setState(stack, index, ItemGunBaseNT.GunState.COOLDOWN);
                ItemGunBaseNT.setTimer(stack, index, rec.getDelayAfterFire(stack));
            } else {

                if(rec.getDoesDryFire(stack)) {
                    ItemGunBaseNT.playAnimation(player, stack, HbmAnimationsSedna.AnimType.CYCLE_DRY, index);
                    ItemGunBaseNT.setState(stack, index, rec.getRefireAfterDry(stack) ? ItemGunBaseNT.GunState.COOLDOWN : ItemGunBaseNT.GunState.DRAWING);
                    ItemGunBaseNT.setTimer(stack, index, rec.getDelayAfterDryFire(stack));
                }
            }
        }

        if(state == ItemGunBaseNT.GunState.RELOADING) {
            ItemGunBaseNT.setReloadCancel(stack, true);
        }
    };
    
    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.AnimType, BusAnimationSedna> LAMBDA_DOUBLE_BARREL_ANIMS = (stack, type) -> switch (type) {
        case EQUIP -> new BusAnimationSedna()
                .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(-60, 0, 0, 0).addPos(0, 0, -3, 500, IType.SIN_DOWN));
        case CYCLE -> new BusAnimationSedna()
                .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(0, 0, -1, 50).addPos(0, 0, 0, 250))
                .addBus("BUCKLE", new BusAnimationSequenceSedna().addPos(0, -60, 0, 50).addPos(0, 0, 0, 250));
        case RELOAD -> new BusAnimationSedna()
                .addBus("TURN", new BusAnimationSequenceSedna()
                        .addPos(0, 30, 0, 350, IType.SIN_FULL)
                        .addPos(0, 30, 0, 1150)
                        .addPos(0, 0, 0, 350, IType.SIN_FULL))
                .addBus("LEVER", new BusAnimationSequenceSedna()
                        .addPos(0, 0, 0, 250)
                        .addPos(0, 0, -90, 100, IType.SIN_FULL)
                        .addPos(0, 0, -90, 1300)
                        .addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("BARREL", new BusAnimationSequenceSedna()
                        .addPos(0, 0, 0, 300)
                        .addPos(60, 0, 0, 150, IType.SIN_UP)
                        .addPos(60, 0, 0, 1150)
                        .addPos(0, 0, 0, 150, IType.SIN_UP))
                .addBus("LIFT", new BusAnimationSequenceSedna()
                        .addPos(0, 0, 0, 350)
                        .addPos(-5, 0, 0, 150, IType.SIN_FULL)
                        .addPos(0, 0, 0, 100, IType.SIN_FULL)
                        .addPos(0, 0, 0, 700)
                        .addPos(-5, 0, 0, 100, IType.SIN_FULL)
                        .addPos(0, 0, 0, 100, IType.SIN_UP) //1500
                        .addPos(45, 0, 0, 150)
                        .addPos(45, 0, 0, 150)
                        .addPos(-5, 0, 0, 150, IType.SIN_DOWN)
                        .addPos(0, 0, 0, 100, IType.SIN_FULL)) //2050
                .addBus("SHELLS", new BusAnimationSequenceSedna()
                        .addPos(0, 0, 0, 450)
                        .addPos(0, 0, -2.5, 100)
                        .addPos(0, -5, -5, 350, IType.SIN_DOWN)
                        .addPos(0, -3, -2, 0)
                        .addPos(0, 0, -2, 250)
                        .addPos(0, 0, 0, 150, IType.SIN_UP)) //1300
                .addBus("SHELL_FLIP", new BusAnimationSequenceSedna().addPos(0, 0, 0, 450).addPos(-360, 0, 0, 450).addPos(0, 0, 0, 0));
        case INSPECT -> new BusAnimationSedna()
                .addBus("LEVER", new BusAnimationSequenceSedna()
                        .addPos(0, 0, 0, 250)
                        .addPos(0, 0, -90, 100, IType.SIN_FULL)
                        .addPos(0, 0, -90, 800)
                        .addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("BARREL", new BusAnimationSequenceSedna()
                        .addPos(0, 0, 0, 300)
                        .addPos(60, 0, 0, 150, IType.SIN_UP)
                        .addPos(60, 0, 0, 650)
                        .addPos(0, 0, 0, 150, IType.SIN_UP))
                .addBus("LIFT", new BusAnimationSequenceSedna()
                        .addPos(0, 0, 0, 350)
                        .addPos(-5, 0, 0, 150, IType.SIN_FULL)
                        .addPos(0, 0, 0, 100, IType.SIN_FULL)
                        .addPos(0, 0, 0, 200)
                        .addPos(-5, 0, 0, 100, IType.SIN_FULL)
                        .addPos(0, 0, 0, 100, IType.SIN_UP) //1500
                        .addPos(45, 0, 0, 150)
                        .addPos(45, 0, 0, 150)
                        .addPos(-5, 0, 0, 150, IType.SIN_DOWN)
                        .addPos(0, 0, 0, 100, IType.SIN_FULL));
        default -> null;
    };
}
