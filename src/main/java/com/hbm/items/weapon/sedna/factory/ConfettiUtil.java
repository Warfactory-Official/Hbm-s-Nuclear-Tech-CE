package com.hbm.items.weapon.sedna.factory;

import com.hbm.entity.mob.*;
import com.hbm.entity.mob.botprime.EntityBOTPrimeBase;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.AshesCreator;
import com.hbm.particle.helper.SkeletonCreator;
import com.hbm.util.DamageResistanceHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.Locale;

public class ConfettiUtil {

    public static void decideConfetti(EntityLivingBase entity, DamageSource source) {
        if(entity.isEntityAlive()) return;
        if(source.damageType.equals(DamageResistanceHandler.DamageClass.LASER.name().toLowerCase(Locale.US))) pulverize(entity);
        if(source.damageType.equals(DamageResistanceHandler.DamageClass.ELECTRIC.name().toLowerCase(Locale.US))) pulverize(entity);
        if(source.isExplosion()) gib(entity);
        if(source.isFireDamage()) cremate(entity);
    }

    public static void pulverize(EntityLivingBase entity) {
        int amount = MathHelper.clamp((int) (entity.width * entity.height * entity.width * 25), 5, 50);
        AshesCreator.composeEffect(entity.world, entity, amount, 0.125F);
        SkeletonCreator.composeEffect(entity.world, entity, 1F);
        entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, HBMSoundHandler.fireDisintegration, SoundCategory.PLAYERS, 2.0F, 0.9F + entity.getRNG().nextFloat() * 0.2F);
    }

    public static void cremate(EntityLivingBase entity) {
        int amount = MathHelper.clamp((int) (entity.width * entity.height * entity.width * 25), 5, 50);
        AshesCreator.composeEffect(entity.world, entity, amount, 0.125F);
        SkeletonCreator.composeEffect(entity.world, entity, 0.25F);
        entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, HBMSoundHandler.fireDisintegration, SoundCategory.PLAYERS, 2.0F, 0.9F + entity.getRNG().nextFloat() * 0.2F);
    }

    public static void gib(EntityLivingBase entity) {
        if(entity instanceof EntityOcelot) return;

        int type = 0;
        if(entity instanceof EntitySlime) type = 1;
        if(entity instanceof EntityMagmaCube) type = 1;
        if(entity instanceof EntityCreeper) type = 1;
        if(entity instanceof EntityGolem) type = 2;
        if(entity instanceof EntityIronGolem) type = 2;
        if(entity instanceof EntityCyberCrab) type = 2;
        if(entity instanceof EntityTeslaCrab) type = 2;
        if(entity instanceof EntityTaintCrab) type = 2;
        if(entity instanceof EntityBlaze) type = 2;
        if(entity instanceof EntityFBIDrone) type = 2;
        if(entity instanceof EntityRADBeast) type = 2;
        if(entity instanceof EntityUFO) type = 2;
        if(entity instanceof EntityBOTPrimeBase) type = 2;

        SkeletonCreator.composeEffectGib(entity.world, entity, 0.25F);

        if(entity instanceof EntitySkeleton) return;

        NBTTagCompound vdat = new NBTTagCompound();
        vdat.setString("type", "giblets");
        vdat.setInteger("ent", entity.getEntityId());
        vdat.setInteger("gibType", type);
        PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(vdat, entity.posX, entity.posY + entity.height * 0.5, entity.posZ), new NetworkRegistry.TargetPoint(entity.dimension, entity.posX, entity.posY + entity.height * 0.5, entity.posZ, 150));
        entity.world.playSound(
                null,
                entity.posX, entity.posY, entity.posZ,
                SoundEvents.ENTITY_ZOMBIE_BREAK_DOOR_WOOD,
                SoundCategory.HOSTILE,
                2.0F,
                0.95F + entity.getRNG().nextFloat() * 0.2F
        );

    }
}
