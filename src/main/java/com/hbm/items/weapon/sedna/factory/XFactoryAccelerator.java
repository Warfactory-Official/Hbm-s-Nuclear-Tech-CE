package com.hbm.items.weapon.sedna.factory;

import com.hbm.entity.projectile.EntityBulletBeamBase;
import com.hbm.entity.projectile.EntityCoin;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.impl.ItemGunNI4NI;
import com.hbm.items.weapon.sedna.mags.MagazineBelt;
import com.hbm.items.weapon.sedna.mags.MagazineInfinite;
import com.hbm.items.weapon.sedna.mags.MagazineSingleReload;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.render.anim.sedna.BusAnimationKeyframeSedna.IType;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.BusAnimationSequenceSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.render.misc.RenderScreenOverlay.Crosshair;
import com.hbm.util.DamageResistanceHandler;
import com.hbm.util.Vec3NT;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class XFactoryAccelerator {
    public static MagazineBelt tauChargeMag = new MagazineBelt();

    public static BulletConfig tau_uranium;
    public static BulletConfig tau_uranium_charge;

    public static BulletConfig coil_tungsten;
    public static BulletConfig coil_ferrouranium;

    public static BulletConfig ni4ni_arc;

    public static Consumer<Entity> LAMBDA_UPDATE_TUNGSTEN = (entity) -> breakInPath(entity, 1.25F);
    public static Consumer<Entity> LAMBDA_UPDATE_FERRO = (entity) -> breakInPath(entity, 2.5F);

    public static void breakInPath(Entity entity, float threshold) {

        Vec3d vec = new Vec3d(entity.posX - entity.prevPosX, entity.posY - entity.prevPosY, entity.posZ - entity.prevPosZ);
        double motion = Math.max(vec.length(), 0.1);
        vec = vec.normalize();

        for(double d = 0; d < motion; d += 0.5) {

            double dX = entity.posX - vec.x * d;
            double dY = entity.posY - vec.y * d;
            double dZ = entity.posZ - vec.z * d;

            if(entity.world.isRemote) {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setString("type", "vanillaExt");
                nbt.setString("mode", "fireworks");
                nbt.setDouble("posX", dX);
                nbt.setDouble("posY", dY);
                nbt.setDouble("posZ", dZ);
                MainRegistry.proxy.effectNT(nbt);

            } else {
                int x = (int) Math.floor(dX);
                int y = (int) Math.floor(dY);
                int z = (int) Math.floor(dZ);
                BlockPos pos = new BlockPos(x, y, z);
                IBlockState bS = entity.world.getBlockState(pos);
                Block b = bS.getBlock();
                float hardness = b.getBlockHardness(bS, entity.world, pos);
                if(b.isAir(bS, entity.world, pos) && hardness >= 0 && hardness < threshold) {
                    entity.world.destroyBlock(pos, false);
                }
            }
        }
    }

    public static void init() {

        tau_uranium = new BulletConfig().setItem(GunFactory.EnumAmmo.TAU_URANIUM).setCasing(new ItemStack(ModItems.plate_lead, 2), 16).setupDamageClass(DamageResistanceHandler.DamageClass.SUBATOMIC).setBeam().setLife(5).setRenderRotations(false).setDoesPenetrate(true).setDamageFalloffByPen(false)
                .setOnBeamImpact(BulletConfig.LAMBDA_BEAM_HIT);
        tau_uranium_charge = new BulletConfig().setItem(GunFactory.EnumAmmo.TAU_URANIUM).setCasing(new ItemStack(ModItems.plate_lead, 2), 16).setupDamageClass(DamageResistanceHandler.DamageClass.SUBATOMIC).setBeam().setLife(5).setRenderRotations(false).setDoesPenetrate(true).setDamageFalloffByPen(false).setSpectral(true)
                .setOnBeamImpact(BulletConfig.LAMBDA_BEAM_HIT);

        coil_tungsten = new BulletConfig().setItem(GunFactory.EnumAmmo.COIL_TUNGSTEN).setVel(7.5F).setLife(50).setDoesPenetrate(true).setDamageFalloffByPen(false).setSpectral(true)
                .setOnUpdate(LAMBDA_UPDATE_TUNGSTEN);
        coil_ferrouranium = new BulletConfig().setItem(GunFactory.EnumAmmo.COIL_FERROURANIUM).setVel(7.5F).setLife(50).setDoesPenetrate(true).setDamageFalloffByPen(false).setSpectral(true)
                .setOnUpdate(LAMBDA_UPDATE_FERRO);

        ni4ni_arc = new BulletConfig().setupDamageClass(DamageResistanceHandler.DamageClass.PHYSICAL).setBeam().setLife(5).setThresholdNegation(10F).setArmorPiercing(0.2F).setRenderRotations(false).setDoesPenetrate(false)
                .setOnBeamImpact(BulletConfig.LAMBDA_BEAM_HIT);

        tauChargeMag.addConfigs(tau_uranium_charge);

        ModItems.gun_tau = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.A_SIDE, "gun_tau", new GunConfig()
                .dura(6_400).draw(10).inspect(10).crosshair(Crosshair.CIRCLE)
                .rec(new Receiver(0)
                        .dmg(25F).spreadHipfire(0F).delay(4).auto(true).spread(0F)
                        .mag(new MagazineBelt().addConfigs(tau_uranium))
                        .offset(1, -0.0625 * 2.5, -0.25D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_TAU))
                .pp(Lego.LAMBDA_STANDARD_CLICK_PRIMARY)
                .rp(LAMBDA_TAU_PRIMARY_RELEASE)
                .ps(LAMBDA_TAU_SECONDARY_PRESS)
                .rs(LAMBDA_TAU_SECONDARY_RELEASE)
                .pr(Lego.LAMBDA_STANDARD_RELOAD)
                .decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
                .anim(LAMBDA_TAU_ANIMS).orchestra(Orchestras.ORCHESTRA_TAU)
        );

        ModItems.gun_coilgun = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.SPECIAL, "gun_coilgun", new GunConfig()
                .dura(400).draw(5).inspect(39).crosshair(Crosshair.L_CIRCUMFLEX)
                .rec(new Receiver(0)
                        .dmg(35F).delay(5).reload(20).jam(33).sound(HBMSoundHandler.coilgunShoot, 1.0F, 1.0F)
                        .mag(new MagazineSingleReload(0, 1).addConfigs(coil_tungsten, coil_ferrouranium))
                        .offset(0.75, -0.0625, -0.1875D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_COILGUN))
                .setupStandardConfiguration()
                .anim(LAMBDA_COILGUN_ANIMS).orchestra(Orchestras.ORCHESTRA_COILGUN)
        );

        ModItems.gun_n_i_4_n_i = new ItemGunNI4NI(ItemGunBaseNT.WeaponQuality.SPECIAL, "gun_n_i_4_n_i", new GunConfig()
                .dura(0).draw(5).inspect(39).crosshair(Crosshair.CIRCLE)
                .rec(new Receiver(0)
                        .dmg(35F).delay(10).sound(HBMSoundHandler.coilgunShoot, 1.0F, 1.0F)
                        .mag(new MagazineInfinite(ni4ni_arc))
                        .offset(0.75, -0.0625, -0.1875D)
                        .setupStandardFire().fire(Lego.LAMBDA_NOWEAR_FIRE))
                .setupStandardConfiguration()
                .ps(LAMBDA_NI4NI_SECONDARY_PRESS)
                .anim(LAMBDA_NI4NI_ANIMS).orchestra(Orchestras.ORCHESTRA_COILGUN)
        );
    }

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_TAU_PRIMARY_RELEASE = (stack, ctx) -> {
        if(ctx.getPlayer() == null || ItemGunBaseNT.getLastAnim(stack, ctx.configIndex) != HbmAnimationsSedna.GunAnimation.CYCLE) return;
        ctx.getPlayer().world.playSound(null, ctx.getPlayer().posX, ctx.getPlayer().posY, ctx.getPlayer().posZ, HBMSoundHandler.fireTauRelease, SoundCategory.PLAYERS, 1F, 1F);
    };

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_TAU_SECONDARY_PRESS = (stack, ctx) -> {
        if(ctx.getPlayer() == null) return;
        if(ctx.config.getReceivers(stack)[0].getMagazine(stack).getAmount(stack, ctx.inventory) <= 0) return;
        ItemGunBaseNT.playAnimation(ctx.getPlayer(), stack, HbmAnimationsSedna.GunAnimation.SPINUP, ctx.configIndex);
        MagazineBelt.getMagType(stack); //caches the last loaded ammo
    };

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_TAU_SECONDARY_RELEASE = (stack, ctx) -> {
        if(ctx.getPlayer() == null) return;
        int timer = ItemGunBaseNT.getAnimTimer(stack, ctx.configIndex);

        if(timer >= 10 && ItemGunBaseNT.getLastAnim(stack, ctx.configIndex) == HbmAnimationsSedna.GunAnimation.SPINUP) {
            ItemGunBaseNT.playAnimation(ctx.getPlayer(), stack, HbmAnimationsSedna.GunAnimation.ALT_CYCLE, ctx.configIndex);
            int unitsUsed = 1 + Math.min(12, timer / 10);

            EntityLivingBase entity = ctx.entity;
            int index = ctx.configIndex;

            Receiver primary = ctx.config.getReceivers(stack)[0];
            BulletConfig config = tauChargeMag.getFirstConfig(stack, ctx.inventory);

            Vec3d offset = primary.getProjectileOffset(stack);
            double forwardOffset = offset.x;
            double heightOffset = offset.y;
            double sideOffset = offset.z;

            float damage = Lego.getStandardWearDamage(stack, ctx.config, index) * unitsUsed * 5;
            float spread = Lego.calcSpread(ctx, stack, primary, config, true, index, false);
            EntityBulletBeamBase mk4 = new EntityBulletBeamBase(entity, config, damage, spread, sideOffset, heightOffset, forwardOffset);
            entity.world.spawnEntity(mk4);

            ItemGunBaseNT.setWear(stack, index, Math.min(ItemGunBaseNT.getWear(stack, index) + config.wear * unitsUsed, ctx.config.getDurability(stack)));

        } else {
            ItemGunBaseNT.playAnimation(ctx.getPlayer(), stack, HbmAnimationsSedna.GunAnimation.CYCLE_DRY, ctx.configIndex);
        }
    };

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_NI4NI_SECONDARY_PRESS = (stack, ctx) -> {
        if(ctx.getPlayer() == null) return;
        EntityPlayer player = ctx.getPlayer();

        if(ItemGunNI4NI.getCoinCount(stack) > 0) {
            Vec3NT vec = new Vec3NT(player.getLookVec()).multiply(0.8D);
            EntityCoin coin = new EntityCoin(player.world);
            coin.setPosition(player.posX, player.posY + player.getEyeHeight() - coin.height - 0.125, player.posZ);
            coin.motionX = vec.x;
            coin.motionY = vec.y + 0.5;
            coin.motionZ = vec.z;
            coin.rotationYaw = player.rotationYaw;
            coin.setThrower(player);
            player.world.spawnEntity(coin);

            player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 1F + player.getRNG().nextFloat() * 0.25F);

            ItemGunNI4NI.setCoinCount(stack, ItemGunNI4NI.getCoinCount(stack) - 1);
        }
    };

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_RECOIL_TAU = (stack, ctx) -> { };

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_RECOIL_COILGUN = (stack, ctx) -> ItemGunBaseNT.setupRecoil(10, (float) (ctx.getPlayer().getRNG().nextGaussian() * 1.5));

    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.GunAnimation, BusAnimationSedna> LAMBDA_TAU_ANIMS = (stack, type) -> switch (type) {
        case EQUIP -> new BusAnimationSedna()
                .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_FULL));
        case CYCLE -> new BusAnimationSedna()
                .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(0, 0, -0.5, 50).addPos(0, 0, 0, 150, IType.SIN_FULL))
                .addBus("ROTATE", new BusAnimationSequenceSedna().addPos(0, 0, -5, 50, IType.SIN_DOWN).addPos(0, 0, 5, 100, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_UP));
        case ALT_CYCLE -> new BusAnimationSedna()
                .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(0, 0, -3, 100, IType.SIN_DOWN).addPos(0, 0, 0, 250, IType.SIN_FULL))
                .addBus("ROTATE", new BusAnimationSequenceSedna().addPos(0, 0, -5, 50, IType.SIN_DOWN).addPos(0, 0, 5, 100, IType.SIN_FULL).addPos(0, 0, 0, 50, IType.SIN_UP));
        case CYCLE_DRY -> new BusAnimationSedna();
        case INSPECT -> new BusAnimationSedna()
                .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(2, 0, 0, 150, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL))
                .addBus("ROTATE", new BusAnimationSequenceSedna().addPos(0, 0, -360 * 3, 500 * 3, IType.SIN_DOWN));
        case SPINUP -> new BusAnimationSedna()
                .addBus("ROTATE", new BusAnimationSequenceSedna().addPos(0, 0, 360 * 6, 3000, IType.SIN_UP).addPos(0, 0, 0, 0).addPos(0, 0, 360 * 40, 500 * 20));
        default -> null;
    };

    public static BiFunction<ItemStack, HbmAnimationsSedna.GunAnimation, BusAnimationSedna> LAMBDA_COILGUN_ANIMS = (stack, type) -> {
        if(type == HbmAnimationsSedna.GunAnimation.EQUIP) return new BusAnimationSedna().addBus("RELOAD", new BusAnimationSequenceSedna().addPos(1, 0, 0, 0).addPos(0, 0, 0, 250));
        if(type == HbmAnimationsSedna.GunAnimation.CYCLE) return new BusAnimationSedna().addBus("RECOIL", new BusAnimationSequenceSedna().addPos(ItemGunBaseNT.getIsAiming(stack) ? 0.5 : 1, 0, 0, 100).addPos(0, 0, 0, 200));
        if(type == HbmAnimationsSedna.GunAnimation.RELOAD) return new BusAnimationSedna().addBus("RELOAD", new BusAnimationSequenceSedna().addPos(1, 0, 0, 250).addPos(1, 0, 0, 500).addPos(0, 0, 0, 250));
        return null;
    };

    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.GunAnimation, BusAnimationSedna> LAMBDA_NI4NI_ANIMS = (stack, type) -> switch (type) {
        case EQUIP -> new BusAnimationSedna()
                .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(-360 * 2, 0, 0, 500));
        case CYCLE -> {
            boolean aiming = ItemGunBaseNT.getIsAiming(stack);
            yield new BusAnimationSedna()
                    .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(aiming ? -5 : -30, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 150, IType.SIN_FULL))
                    .addBus("DRUM", new BusAnimationSequenceSedna().hold(50).addPos(0, 0, 120, 300, IType.SIN_FULL));
        }
        case INSPECT -> new BusAnimationSedna()
                .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(-360 * 3, 0, 0, 750).hold(100).addPos(0, 0, 0, 750));
        default -> null;
    };
}
