package com.hbm.items.weapon.sedna.factory;

import com.hbm.Tags;
import com.hbm.capability.HbmLivingCapability;
import com.hbm.capability.HbmLivingProps;
import com.hbm.entity.effect.EntityFireLingering;
import com.hbm.entity.projectile.EntityBulletBeamBase;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.mags.MagazineBelt;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.render.anim.sedna.BusAnimationKeyframeSedna.IType;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.BusAnimationSequenceSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.render.misc.RenderScreenOverlay.Crosshair;
import com.hbm.util.DamageResistanceHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class XFactoryEnergy {
    public static final ResourceLocation scope_luna = new ResourceLocation(Tags.MODID, "textures/misc/scope_luna.png");

    public static BulletConfig energy_tesla;
    public static BulletConfig energy_tesla_overcharge;

    public static BulletConfig energy_las;
    public static BulletConfig energy_las_overcharge;
    public static BulletConfig energy_las_ir;
    public static BulletConfig energy_emerald;
    public static BulletConfig energy_emerald_overcharge;
    public static BulletConfig energy_emerald_ir;

    public static BiConsumer<EntityBulletBeamBase, RayTraceResult> LAMBDA_LIGHTNING_HIT = (beam, mop) -> {

        if(mop.typeOfHit == mop.typeOfHit.BLOCK) {
            ForgeDirection dir = ForgeDirection.getOrientation(mop.sideHit);
            mop.hitVec.add(dir.offsetX * 0.5, dir.offsetY * 0.5, dir.offsetZ * 0.5);
        }

        ExplosionVNT vnt = new ExplosionVNT(beam.world, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, 2F, beam.getThrower());
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, beam.damage).setDamageClass(beam.config.dmgClass));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.explode();
        beam.world.playSound(null, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, HBMSoundHandler.ufoBlast, SoundCategory.PLAYERS, 5.0F, 0.9F + beam.world.rand.nextFloat() * 0.2F);
        beam.world.playSound(null, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, SoundEvents.ENTITY_FIREWORK_BLAST, SoundCategory.PLAYERS, 5.0F, 0.5F);

        float yaw = beam.world.rand.nextFloat() * 180F;
        for(int i = 0; i < 3; i++) {
            NBTTagCompound data = new NBTTagCompound();
            data.setString("type", "plasmablast");
            data.setFloat("r", 0.5F);
            data.setFloat("g", 0.5F);
            data.setFloat("b", 1.0F);
            data.setFloat("pitch", -60F + 60F * i);
            data.setFloat("yaw", yaw);
            data.setFloat("scale", 2F);
            PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z),
                    new NetworkRegistry.TargetPoint(beam.world.provider.getDimension(), mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, 100));
        }

        if(mop.typeOfHit == mop.typeOfHit.ENTITY) {
            if(mop.entityHit instanceof EntityLivingBase) {
                ((EntityLivingBase) mop.entityHit).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 9));
                ((EntityLivingBase) mop.entityHit).addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 60, 9));
            }
        }
    };

    public static BiConsumer<EntityBulletBeamBase, RayTraceResult> LAMBDA_IR_HIT = (beam, mop) -> {
        BulletConfig.LAMBDA_STANDARD_BEAM_HIT.accept(beam, mop);

        if(mop.typeOfHit == mop.typeOfHit.ENTITY) {
            if(mop.entityHit instanceof EntityLivingBase living) {
                HbmLivingCapability.IEntityHbmProps props = HbmLivingProps.getData(living);
                if(props.getFire() < 100) props.setFire(100);
            }
        }

        if(mop.typeOfHit == mop.typeOfHit.BLOCK) {
            World world = beam.world;
            Block b = world.getBlockState(mop.getBlockPos()).getBlock();
            ForgeDirection dir = ForgeDirection.getOrientation(mop.sideHit);
            if(b.isFlammable(world, mop.getBlockPos(), Objects.requireNonNull(dir.getOpposite().toEnumFacing()))) {
                if(world.getBlockState(mop.getBlockPos().add(dir.offsetX, dir.offsetY, dir.offsetZ)).getBlock().isAir(world.getBlockState(mop.getBlockPos().add(dir.offsetX, dir.offsetY, dir.offsetZ)), world, mop.getBlockPos().add(dir.offsetX, dir.offsetY, dir.offsetZ))) {
                    world.setBlockState(mop.getBlockPos().add(dir.offsetX, dir.offsetY, dir.offsetZ), Blocks.FIRE.getDefaultState());
                    return;
                }
            }

            EntityFireLingering fire = new EntityFireLingering(beam.world).setArea(2, 1).setDuration(100).setType(EntityFireLingering.TYPE_DIESEL);
            fire.setPosition(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
            beam.world.spawnEntity(fire);
        }
    };

    public static void init() {

        energy_tesla = new BulletConfig().setItem(GunFactory.EnumAmmo.CAPACITOR).setCasing(new ItemStack(ModItems.ingot_polymer, 2), 4).setupDamageClass(DamageResistanceHandler.DamageClass.ELECTRIC).setBeam().setSpread(0.0F).setLife(5).setRenderRotations(false).setDoesPenetrate(true)
                .setOnBeamImpact(LAMBDA_LIGHTNING_HIT);
        energy_tesla_overcharge = new BulletConfig().setItem(GunFactory.EnumAmmo.CAPACITOR_OVERCHARGE).setCasing(new ItemStack(ModItems.ingot_polymer, 2), 4).setupDamageClass(DamageResistanceHandler.DamageClass.ELECTRIC).setBeam().setSpread(0.0F).setLife(5).setRenderRotations(false).setDoesPenetrate(true)
                .setDamage(1.5F).setOnBeamImpact(LAMBDA_LIGHTNING_HIT);

        energy_las = new BulletConfig().setItem(GunFactory.EnumAmmo.CAPACITOR).setCasing(new ItemStack(ModItems.ingot_polymer, 2), 4).setupDamageClass(DamageResistanceHandler.DamageClass.LASER).setBeam().setSpread(0.0F).setLife(5).setRenderRotations(false).setOnBeamImpact(BulletConfig.LAMBDA_STANDARD_BEAM_HIT);
        energy_las_overcharge = new BulletConfig().setItem(GunFactory.EnumAmmo.CAPACITOR_OVERCHARGE).setCasing(new ItemStack(ModItems.ingot_polymer, 2), 4).setupDamageClass(DamageResistanceHandler.DamageClass.LASER).setBeam().setSpread(0.0F).setLife(5).setRenderRotations(false).setDoesPenetrate(true).setOnBeamImpact(BulletConfig.LAMBDA_STANDARD_BEAM_HIT);
        energy_las_ir = new BulletConfig().setItem(GunFactory.EnumAmmo.CAPACITOR_IR).setCasing(new ItemStack(ModItems.ingot_polymer, 2), 4).setupDamageClass(DamageResistanceHandler.DamageClass.FIRE).setBeam().setSpread(0.0F).setLife(5).setRenderRotations(false).setOnBeamImpact(LAMBDA_IR_HIT);

        energy_emerald = energy_las.clone().setArmorPiercing(0.5F).setThresholdNegation(10F);
        energy_emerald_overcharge = energy_las_overcharge.clone().setArmorPiercing(0.5F).setThresholdNegation(15F);
        energy_emerald_ir = energy_las_ir.clone().setArmorPiercing(0.5F).setThresholdNegation(10F);

        ModItems.gun_tesla_cannon = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.A_SIDE, "gun_tesla_cannon", new GunConfig()
                .dura(2_000).draw(10).inspect(33).reloadSequential(true).crosshair(Crosshair.CIRCLE)
                .rec(new Receiver(0)
                        .dmg(35F).delay(20).reload(44).jam(19).sound(HBMSoundHandler.fireTesla, 1.0F, 1.0F)
                        .mag(new MagazineBelt().addConfigs(energy_tesla, energy_tesla_overcharge))
                        .offset(0.75, 0, -0.375).offsetScoped(0.75, 0, -0.25)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_ENERGY))
                .setupStandardConfiguration()
                .anim(LAMBDA_TESLA_ANIMS).orchestra(Orchestras.ORCHESTRA_TESLA)
        );

        ModItems.gun_laser_pistol = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.A_SIDE, "gun_laser_pistol", new GunConfig()
                .dura(500).draw(10).inspect(26).crosshair(Crosshair.CIRCLE)
                .rec(new Receiver(0)
                        .dmg(25F).delay(5).spread(1F).spreadHipfire(1F).reload(45).jam(37).sound(HBMSoundHandler.fireLaserPistol, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 30).addConfigs(energy_las, energy_las_overcharge, energy_las_ir))
                        .offset(0.75, -0.0625 * 1.5, -0.1875)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_ENERGY))
                .setupStandardConfiguration()
                .anim(LAMBDA_LASER_PISTOL).orchestra(Orchestras.ORCHESTRA_LASER_PISTOL)
        );
        ModItems.gun_laser_pistol_pew_pew = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.B_SIDE, "gun_laser_pistol_pew_pew", new GunConfig()
                .dura(500).draw(10).inspect(26).crosshair(Crosshair.CIRCLE)
                .rec(new Receiver(0)
                        .dmg(30F).rounds(5).delay(10).spread(0.25F).spreadHipfire(1F).reload(45).jam(37).sound(HBMSoundHandler.fireLaserPistol, 1.0F, 0.8F)
                        .mag(new MagazineFullReload(0, 10).addConfigs(energy_las, energy_las_overcharge, energy_las_ir))
                        .offset(0.75, -0.0625 * 1.5, -0.1875)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_ENERGY))
                .setupStandardConfiguration()
                .anim(LAMBDA_LASER_PISTOL).orchestra(Orchestras.ORCHESTRA_LASER_PISTOL)
        );
        ModItems.gun_laser_pistol_morning_glory = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.LEGENDARY, "gun_laser_pistol_morning_glory", new GunConfig()
                .dura(1_500).draw(10).inspect(26).crosshair(Crosshair.CIRCLE)
                .rec(new Receiver(0)
                        .dmg(20F).delay(7).spread(0F).spreadHipfire(0.5F).reload(45).jam(37).sound(HBMSoundHandler.fireLaserPistol, 1.0F, 1.1F)
                        .mag(new MagazineFullReload(0, 20).addConfigs(energy_emerald, energy_emerald_overcharge, energy_emerald_ir))
                        .offset(0.75, -0.0625 * 1.5, -0.1875)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_ENERGY))
                .setupStandardConfiguration()
                .anim(LAMBDA_LASER_PISTOL).orchestra(Orchestras.ORCHESTRA_LASER_PISTOL)
        );

        ModItems.gun_lasrifle = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.A_SIDE, "gun_lasrifle", new GunConfig()
                .dura(2_000).draw(10).inspect(26).reloadSequential(true).crosshair(Crosshair.CIRCLE).scopeTexture(scope_luna)
                .rec(new Receiver(0)
                        .dmg(50F).delay(8).reload(44).jam(36).sound(HBMSoundHandler.fireLaser, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 24).addConfigs(energy_las, energy_las_overcharge, energy_las_ir))
                        .offset(0.75, -0.0625 * 1.5, -0.1875)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_ENERGY))
                .setupStandardConfiguration()
                .anim(LAMBDA_LASRIFLE).orchestra(Orchestras.ORCHESTRA_LASRIFLE)
        );
    }

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_RECOIL_ENERGY = (stack, ctx) -> { };

    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.GunAnimation, BusAnimationSedna> LAMBDA_TESLA_ANIMS = (stack, type) -> {
        int amount = ((ItemGunBaseNT) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, MainRegistry.proxy.me().inventory);
        return switch (type) {
            case EQUIP -> new BusAnimationSedna()
                    .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(60, 0, 0, 0).addPos(0, 0, 0, 1000, IType.SIN_DOWN));
            case CYCLE -> new BusAnimationSedna()
                    .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(0, 0, ItemGunBaseNT.getIsAiming(stack) ? -0.5 : -1, 100, IType.SIN_DOWN).addPos(0, 0, 0, 250, IType.SIN_FULL))
                    .addBus("CYCLE", new BusAnimationSequenceSedna().addPos(0, 0, 0, 150).addPos(0, 0, 22.5, 350))
                    .addBus("COUNT", new BusAnimationSequenceSedna().addPos(amount, 0, 0, 0));
            case CYCLE_DRY -> new BusAnimationSedna()
                    .addBus("CYCLE", new BusAnimationSequenceSedna().addPos(0, 0, 0, 150).addPos(0, 0, 22.5, 350));
            case INSPECT -> new BusAnimationSedna()
                    .addBus("YOMI", new BusAnimationSequenceSedna().addPos(8, -4, 0, 0).addPos(4, -1, 0, 500, IType.SIN_DOWN).addPos(4, -1, 0, 1000).addPos(6, -6, 0, 500, IType.SIN_UP))
                    .addBus("SQUEEZE", new BusAnimationSequenceSedna().addPos(1, 1, 1, 0).addPos(1, 1, 1, 750).addPos(1, 1, 0.5, 125).addPos(1, 1, 1, 125));
            default -> null;
        };

    };

    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.GunAnimation, BusAnimationSedna> LAMBDA_LASER_PISTOL = (stack, type) -> {
        switch(type) {
            case EQUIP: return new BusAnimationSedna()
                    .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(60, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
            case CYCLE: return new BusAnimationSedna()
                    .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(0, 0, -0.5, 50, IType.SIN_DOWN).addPos(0, 0, 0, 150, IType.SIN_FULL));
            case RELOAD: return new BusAnimationSedna()
                    .addBus("LATCH", new BusAnimationSequenceSedna().addPos(0, -20, 0, 100).hold(1900).addPos(0, 0, 0, 100))
                    .addBus("LIFT", new BusAnimationSequenceSedna().hold(100).addPos(-45, 0, 0, 250, IType.SIN_FULL).hold(500).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("JOLT", new BusAnimationSequenceSedna().hold(350).addPos(0, 0, 0.5, 100, IType.SIN_FULL).addPos(0, 0, -1.5, 100, IType.SIN_UP).addPos(0, 0, 0, 150, IType.SIN_FULL).holdUntil(2100).addPos(-0.0625, 0, 0, 50, IType.SIN_UP).addPos(0, 0, 0, 100, IType.SIN_FULL))
                    .addBus("BATTERY", new BusAnimationSequenceSedna().hold(550).addPos(0, 0, 5, 250).hold(550).setPos(0, -2, -2).addPos(0, 0, -2, 250, IType.SIN_FULL).addPos(0, 0, 0, 250, IType.SIN_UP));
            case JAMMED: return new BusAnimationSedna()
                    .addBus("LATCH", new BusAnimationSequenceSedna().hold(500).addPos(0, -20, 0, 100).hold(250).addPos(0, 0, 0, 100))
                    .addBus("JOLT", new BusAnimationSequenceSedna().hold(950).addPos(-0.0625, 0, 0, 50, IType.SIN_UP).addPos(0, 0, 0, 100, IType.SIN_FULL))
                    .addBus("EQUIP", new BusAnimationSequenceSedna().hold(1500).addPos(7.5, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 250, IType.SIN_FULL));
            case INSPECT: return new BusAnimationSedna()
                    .addBus("SWIRL", new BusAnimationSequenceSedna().addPos(-720, 0, 0, 750, IType.SIN_FULL).hold(500).addPos(0, 0, 0, 750, IType.SIN_FULL));
        }
        return null;
    };

    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.GunAnimation, BusAnimationSedna> LAMBDA_LASRIFLE = (stack, type) -> {
        int amount = ((ItemGunBaseNT) stack.getItem()).getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, MainRegistry.proxy.me().inventory);
        return switch (type) {
            case EQUIP -> new BusAnimationSedna()
                    .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(60, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
            case CYCLE -> new BusAnimationSedna()
                    .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(0, 0, -0.5, 50, IType.SIN_DOWN).addPos(0, 0, 0, 150, IType.SIN_FULL))
                    .addBus("CYCLE", new BusAnimationSequenceSedna().addPos(0, 0, 0, 150).addPos(0, 0, 22.5, 350))
                    .addBus("COUNT", new BusAnimationSequenceSedna().addPos(amount, 0, 0, 0));
            case RELOAD -> new BusAnimationSedna()
                    .addBus("LEVER", new BusAnimationSequenceSedna().addPos(-90, 0, 0, 350, IType.SIN_UP).addPos(-90, 0, 0, 1500).addPos(0, 0, 0, 350, IType.SIN_UP))
                    .addBus("MAG", new BusAnimationSequenceSedna().addPos(0, 0, 0, 350).addPos(0, -5, 0, 350, IType.SIN_UP).addPos(0, -5, 0, 500).addPos(0, -0.25, 0, 500, IType.SIN_FULL).addPos(0, -0.25, 0, 150).addPos(0, 0, 0, 350))
                    .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(0, 0, 0, 1700).addPos(-2, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
            case JAMMED -> new BusAnimationSedna()
                    .addBus("LEVER", new BusAnimationSequenceSedna().addPos(0, 0, 0, 500).addPos(-90, 0, 0, 350, IType.SIN_UP).addPos(-90, 0, 0, 600).addPos(0, 0, 0, 350, IType.SIN_UP))
                    .addBus("MAG", new BusAnimationSequenceSedna().addPos(0, 0, 0, 500).addPos(0, 0, 0, 350).addPos(0, -2, 0, 200, IType.SIN_UP).addPos(0, -0.25, 0, 250, IType.SIN_FULL).addPos(0, -0.25, 0, 150).addPos(0, 0, 0, 350))
                    .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(0, 0, 0, 500).addPos(0, 0, 0, 800).addPos(-2, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
            case INSPECT -> new BusAnimationSedna()
                    .addBus("LEVER", new BusAnimationSequenceSedna().addPos(-90, 0, 0, 350, IType.SIN_UP).addPos(-90, 0, 0, 600).addPos(0, 0, 0, 350, IType.SIN_UP))
                    .addBus("MAG", new BusAnimationSequenceSedna().addPos(0, 0, 0, 350).addPos(0, -2, 0, 200, IType.SIN_UP).addPos(0, -0.25, 0, 250, IType.SIN_FULL).addPos(0, -0.25, 0, 150).addPos(0, 0, 0, 350))
                    .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(0, 0, 0, 800).addPos(-2, 0, 0, 100, IType.SIN_DOWN).addPos(0, 0, 0, 100, IType.SIN_FULL));
            default -> null;
        };

    };
}
