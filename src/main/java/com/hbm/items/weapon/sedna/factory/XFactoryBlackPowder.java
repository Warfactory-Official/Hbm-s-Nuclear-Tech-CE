package com.hbm.items.weapon.sedna.factory;

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
import com.hbm.render.misc.RenderScreenOverlay;
import net.minecraft.item.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class XFactoryBlackPowder {
    public static BulletConfig stone = new BulletConfig().setItem(GunFactory.EnumAmmo.STONE).setBlackPowder(true).setHeadshot(1F).setSpread(0.025F).setRicochetAngle(15);
    public static BulletConfig flint = new BulletConfig().setItem(GunFactory.EnumAmmo.STONE_AP).setBlackPowder(true).setHeadshot(1F).setSpread(0.01F).setRicochetAngle(5).setDoesPenetrate(true).setDamage(1.5F);
    public static BulletConfig iron = new BulletConfig().setItem(GunFactory.EnumAmmo.STONE_IRON).setBlackPowder(true).setHeadshot(1F).setSpread(0F).setRicochetAngle(90).setRicochetCount(5).setDoesPenetrate(true).setDamageFalloffByPen(false).setDamage(1.5F);
    public static BulletConfig shot = new BulletConfig().setItem(GunFactory.EnumAmmo.STONE_SHOT).setBlackPowder(true).setHeadshot(1F).setSpread(0.1F).setRicochetAngle(45).setProjectiles(6, 6).setDamage(1F/6F);

    public static void init() {

        ModItems.gun_pepperbox = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.A_SIDE, "gun_pepperbox", new GunConfig()
                .dura(300).draw(4).inspect(23).crosshair(RenderScreenOverlay.Crosshair.CIRCLE).smoke(Lego.LAMBDA_STANDARD_SMOKE)
                .rec(new Receiver(0)
                        .dmg(5F).delay(27).reload(67).jam(58).sound(HBMSoundHandler.fireBlackPowder, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 6).addConfigs(stone, flint, iron, shot))
                        .offset(0.75, -0.0625, -0.1875D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_PEPPERBOX))
                .setupStandardConfiguration()
                .anim(LAMBDA_PEPPERBOX_ANIMS).orchestra(Orchestras.ORCHESTRA_PEPPERBOX)
        );
    }

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_RECOIL_PEPPERBOX = (stack, ctx) -> ItemGunBaseNT.setupRecoil(10, (float) (ctx.getPlayer().getRNG().nextGaussian() * 1.5));

    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.GunAnimation, BusAnimationSedna> LAMBDA_PEPPERBOX_ANIMS = (stack, type) -> switch (type) {
        case CYCLE -> new BusAnimationSedna()
                .addBus("ROTATE", new BusAnimationSequenceSedna().addPos(0, 0, 0, 1025).addPos(60, 0, 0, 250))
                .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(0, 0, 0, 50).addPos(45, 0, 0, 150, IType.SIN_DOWN).addPos(45, 0, 0, 50).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("HAMMER", new BusAnimationSequenceSedna().addPos(80, 0, 0, 25).addPos(80, 0, 0, 1000).addPos(0, 0, 0, 250))
                .addBus("TRIGGER", new BusAnimationSequenceSedna().addPos(1, 0, 0, 25).addPos(1, 0, 0, 250).addPos(0, 0, 0, 100));
        case CYCLE_DRY -> new BusAnimationSedna()
                .addBus("ROTATE", new BusAnimationSequenceSedna().addPos(0, 0, 0, 525).addPos(60, 0, 0, 250))
                .addBus("HAMMER", new BusAnimationSequenceSedna().addPos(80, 0, 0, 25).addPos(80, 0, 0, 500).addPos(0, 0, 0, 250))
                .addBus("TRIGGER", new BusAnimationSequenceSedna().addPos(1, 0, 0, 25).addPos(1, 0, 0, 250).addPos(0, 0, 0, 100));
        case EQUIP -> new BusAnimationSedna()
                .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(-45, 0, 0, 0).addPos(0, 0, 0, 200, IType.SIN_DOWN));
        case RELOAD -> new BusAnimationSedna()
                .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(90, 0, 0, 500, IType.SIN_FULL).addPos(90, 0, 0, 1600).addPos(0, 0, 0, 500, IType.SIN_FULL).addPos(-5, 0, 0, 200, IType.SIN_UP).addPos(0, 0, 0, 200, IType.SIN_DOWN))
                .addBus("TRANSLATE", new BusAnimationSequenceSedna().addPos(0, -12, 5, 500, IType.SIN_FULL).addPos(0, -12, 5, 700).addPos(0, -13, 5, 200).addPos(0, -12, 5, 200).addPos(0, -12, 5, 500).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("LOADER", new BusAnimationSequenceSedna().addPos(0, 0, 0, 500).addPos(0, 5, -5, 0).addPos(0, 0, -0.1, 500, IType.SIN_FULL).addPos(0, 0, -1, 200).addPos(0, 0, -1, 200).addPos(0, 0, -0.1, 200).addPos(0, 5, -5, 500, IType.SIN_FULL).addPos(0, 0, 0, 0))
                .addBus("ROTATE", new BusAnimationSequenceSedna().addPos(0, 0, 0, 2600).addPos(-360, 0, 0, 750, IType.SIN_FULL))
                .addBus("SHOT", new BusAnimationSequenceSedna().addPos(1, 0, 0, 1400).addPos(0, 0, 0, 0));
        case INSPECT -> new BusAnimationSedna()
                .addBus("ROTATE", new BusAnimationSequenceSedna().addPos(-360, 0, 0, 750, IType.SIN_FULL))
                .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(-5, 0, 0, 200, IType.SIN_UP).addPos(0, 0, 0, 200, IType.SIN_DOWN));
        case JAMMED -> new BusAnimationSedna()
                .addBus("ROTATE", new BusAnimationSequenceSedna().addPos(0, 0, 0, 1300).addPos(60, 0, 0, 500, IType.SIN_FULL).addPos(60, 0, 0, 400).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("TRANSLATE", new BusAnimationSequenceSedna().addPos(0, 0, 0, 500).addPos(0, -6, 0, 400, IType.SIN_FULL).addPos(0, -6, 0, 2000).addPos(0, 0, 0, 400, IType.SIN_FULL))
                .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(0, 0, 0, 500).addPos(45, 0, 0, 400, IType.SIN_FULL).addPos(45, 0, 0, 2000).addPos(0, 0, 0, 400, IType.SIN_FULL));
        default -> null;
    };
}
