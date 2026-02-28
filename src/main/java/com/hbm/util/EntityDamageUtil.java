package com.hbm.util;

import com.hbm.handler.ArmorModHandler;
import com.hbm.interfaces.Untested;
import com.hbm.items.ModItems;
import com.hbm.lib.internal.MethodHandleHelper;
import com.hbm.main.MainRegistry;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;

@Untested
//OH man this will be a goldmine of bugs
public class EntityDamageUtil {
    private static final MethodHandle getDeathSoundHandle = MethodHandleHelper.findVirtual(EntityLivingBase.class, "getDeathSound", "func_184615_bR", MethodType.methodType(SoundEvent.class));
    private static final MethodHandle getHurtSoundHandle = MethodHandleHelper.findVirtual(EntityLivingBase.class, "getHurtSound", "func_184601_bQ", MethodType.methodType(SoundEvent.class, DamageSource.class));
    private static final MethodHandle getSoundVolumeHandle = MethodHandleHelper.findVirtual(EntityLivingBase.class, "getSoundVolume", "func_70599_aP", MethodType.methodType(float.class));
    private static final MethodHandle getSoundPitchHandle = MethodHandleHelper.findVirtual(EntityLivingBase.class, "getSoundPitch", "func_70647_i", MethodType.methodType(float.class));

    public static boolean attackEntityFromIgnoreIFrame(Entity victim, DamageSource src, float damage) {

        if (!victim.attackEntityFrom(src, damage)) {
            float lastDamage = 0;
            if (victim instanceof EntityLivingBase entityLivingBase) {
                lastDamage = entityLivingBase.lastDamage;
            }
            float dmg = damage + lastDamage;
            return victim.attackEntityFrom(src, dmg);
        } else {
            return true;
        }
    }

    // mlbv: yes this is empty
    public static void damageArmorNT(EntityLivingBase living, float amount) {
    }

    public static boolean wasAttackedByV1(DamageSource source) {

        if (source instanceof EntityDamageSource) {
            Entity attacker = source.getImmediateSource();

            if (attacker instanceof EntityPlayer player) {
                ItemStack chestplate = player.inventory.armorInventory.get(2);

                if (!chestplate.isEmpty() && ArmorModHandler.hasMods(chestplate)) {
                    ItemStack[] mods = ArmorModHandler.pryMods(chestplate);

                    return mods[ArmorModHandler.extra] != null && mods[ArmorModHandler.extra].getItem() == ModItems.v1;
                }
            }
        }

        return false;
    }

    public static boolean attackEntityFromNT(EntityLivingBase living, DamageSource source, float amount, boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier, float pierceDT, float pierce) {
        if (living instanceof EntityPlayerMP playerMP && source.getTrueSource() instanceof EntityPlayer attacker) {
            if (!playerMP.canAttackPlayer(attacker))
                return false; //handles wack-ass no PVP rule as well as scoreboard friendly fire
        }
        DamageResistanceHandler.setup(pierceDT, pierce);
        living.attackEntityFrom(source, 0F);
        try {
            return attackEntityFromNTInternal(living, source, amount, ignoreIFrame, allowSpecialCancel, knockbackMultiplier);
        } finally {
            DamageResistanceHandler.reset();
        }
    }

    public static void setBeenAttacked(EntityLivingBase living) {
        living.velocityChanged = living.getRNG().nextDouble() >= living.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue();
    }

    public static RayTraceResult getMouseOver(EntityPlayer attacker, double reach) {
        return getMouseOver(attacker, reach, 0D);
    }

    public static RayTraceResult getMouseOver(EntityPlayer attacker, double reach, double threshold) {

        World world = attacker.world;
        RayTraceResult objectMouseOver;
        Entity pointedEntity = null;

        objectMouseOver = rayTrace(attacker, reach, 1F);

        Vec3d pos = getPosition(attacker);
        Vec3d look = attacker.getLook(1F);
        Vec3d end = pos.add(look.x * reach, look.y * reach, look.z * reach);
        Vec3d hitvec = null;
        float grace = 1.0F;
        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(attacker, attacker.getEntityBoundingBox().expand(look.x * reach, look.y * reach, look.z * reach).expand(grace, grace, grace));

        double closest = reach;

        for (Entity entity : list) {
            if (entity.canBeCollidedWith()) {

                double borderSize = entity.getCollisionBorderSize() + threshold;
                AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().expand(borderSize, borderSize, borderSize);
                RayTraceResult movingobjectposition = axisalignedbb.calculateIntercept(pos, end);

                if (axisalignedbb.contains(pos)) {
                    if (0.0D <= closest) {
                        pointedEntity = entity;
                        hitvec = movingobjectposition == null ? pos : movingobjectposition.hitVec;
                        closest = 0.0D;
                    }

                } else if (movingobjectposition != null) {
                    double dist = pos.distanceTo(movingobjectposition.hitVec);

                    if (dist < closest || closest == 0.0D) {
                        if (entity == attacker.getRidingEntity() && !entity.canRiderInteract()) {
                            if (closest == 0.0D) {
                                pointedEntity = entity;
                                hitvec = movingobjectposition.hitVec;
                            }
                        } else {
                            pointedEntity = entity;
                            hitvec = movingobjectposition.hitVec;
                            closest = dist;
                        }
                    }
                }
            }
        }

        if(pointedEntity != null && (closest < reach || objectMouseOver == null)) {
            objectMouseOver = new RayTraceResult(pointedEntity, hitvec);
        }

        return objectMouseOver;
    }

    public static RayTraceResult rayTrace(EntityPlayer player, double dist, float interp) {
        Vec3d pos = getPosition(player);
        Vec3d look = player.getLook(interp);
        Vec3d end = pos.add(look.x * dist, look.y * dist, look.z * dist);
        return player.world.rayTraceBlocks(pos, end, false, false, true);
    }

    public static Vec3d getPosition(EntityPlayer player) {
        return new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
    }

    private static boolean attackEntityFromNTInternal(EntityLivingBase living, DamageSource source, float amount, boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier) {
        if (MinecraftForge.EVENT_BUS.post(new LivingAttackEvent(living, source, amount)) && allowSpecialCancel) return false;
        if (living.isEntityInvulnerable(source)) return false;
        if (living.world.isRemote) return false;
        if (living instanceof EntityPlayer && ((EntityPlayer) living).capabilities.disableDamage && !source.canHarmInCreative())
            return false;

        living.idleTime = 0;
        if (living.getHealth() <= 0.0F) return false;
        if (source.isFireDamage() && living.isPotionActive(MobEffects.FIRE_RESISTANCE)) return false;

        living.limbSwingAmount = 1.5F;
        boolean didAttackRegister = true;

        if (living.hurtResistantTime > living.maxHurtResistantTime / 2.0F && !ignoreIFrame) {
            if (amount <= living.lastDamage) {
                return false;
            }
            damageEntityNT(living, source, amount - living.lastDamage);
            living.lastDamage = amount;
            didAttackRegister = false;
        } else {
            living.lastDamage = amount;
            //living.prevHealth = living.getHealth(); I believe this safe to ommit?
            living.hurtResistantTime = living.maxHurtResistantTime;
            damageEntityNT(living, source, amount);
            living.hurtTime = living.maxHurtTime = 10;
        }

        living.attackedAtYaw = 0.0F;
        Entity entity = source.getTrueSource();

        if (entity != null) {
            if (entity instanceof EntityLivingBase entityLivingBase) {
                living.setRevengeTarget(entityLivingBase);
            }

            if (entity instanceof EntityPlayer player) {
                living.recentlyHit = 100;
                living.attackingPlayer = player;

            } else if (entity instanceof EntityTameable entitywolf) {

                if (entitywolf.isTamed()) {
                    living.recentlyHit = 100;
                    living.attackingPlayer = null; //Null? I HOPE this wont cause NPEs?
                    // mlbv: yes it won't
                }
            }
        }

        if (didAttackRegister) {
            living.world.setEntityState(living, (byte) 2);

            if (source != DamageSource.DROWN) setBeenAttacked(living); //#

            if (entity != null) {
                double deltaX = entity.posX - living.posX;
                double deltaZ;

                for (deltaZ = entity.posZ - living.posZ; deltaX * deltaX + deltaZ * deltaZ < 1.0E-4D; deltaZ = (Math.random() - Math.random()) * 0.01D) {
                    deltaX = (Math.random() - Math.random()) * 0.01D;
                }

                living.attackedAtYaw = (float) (Math.atan2(deltaZ, deltaX) * 180.0D / Math.PI) - living.rotationYaw;
                if (knockbackMultiplier > 0) knockBack(living, entity, amount, deltaX, deltaZ, knockbackMultiplier);
            } else {
                living.attackedAtYaw = (float) ((int) (Math.random() * 2.0D) * 180);
            }
        }

        SoundEvent sound;

        if (living.getHealth() <= 0.0F) {
            sound = getDeathSound(living);
            if (didAttackRegister && sound != null)
                living.playSound(sound, getSoundVolume(living), getSoundPitch(living)); //#
            living.onDeath(source);
        } else {
            sound = getHurtSound(living, source);
            if (didAttackRegister && sound != null)
                living.playSound(sound, getSoundVolume(living), getSoundPitch(living)); //#
        }

        return true;
    }

    public static void damageEntityNT(EntityLivingBase living, DamageSource source, float amount) {
        if (!living.isEntityInvulnerable(source)) {
            amount = ForgeHooks.onLivingHurt(living, source, amount);
            if (amount <= 0) return;

            amount = applyArmorCalculationsNT(living, source, amount);
            amount = applyPotionDamageCalculations(living, source, amount);

            float originalAmount = amount;
            amount = Math.max(amount - living.getAbsorptionAmount(), 0.0F);
            living.setAbsorptionAmount(living.getAbsorptionAmount() - (originalAmount - amount));

            if (amount != 0.0F) {
                float health = living.getHealth();
                living.setHealth(health - amount);
                living.getCombatTracker().trackDamage(source, health, amount);
                living.setAbsorptionAmount(living.getAbsorptionAmount() - amount);
            }
        }
    }

    public static float applyArmorCalculationsNT(EntityLivingBase living, DamageSource source, float amount) {
        if (!source.isUnblockable()) {
            float i = 25F - (living.getTotalArmorValue() * (1 - DamageResistanceHandler.currentPDR));
            float armor = amount * i;
            damageArmorNT(living, amount);
            amount = armor / 25.0F;
        }

        return amount;
    }


    public static float applyPotionDamageCalculations(EntityLivingBase living, DamageSource source, float amount) {
        if (source.isDamageAbsolute()) {
            return amount;
        } else {

            int resistance;
            int j;
            float f1;

            if (living.isPotionActive(MobEffects.RESISTANCE) && source != DamageSource.OUT_OF_WORLD) {
                resistance = (living.getActivePotionEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 5;
                j = 25 - resistance;
                f1 = amount * (float) j;
                amount = f1 / 25.0F;
            }

            if (amount <= 0.0F) {
                return 0.0F;
            } else {

                resistance = EnchantmentHelper.getEnchantmentModifierDamage(living.getArmorInventoryList(), source);

                if (resistance > 20) {
                    resistance = 20;
                }

                if (resistance > 0 && resistance <= 20) {
                    j = 25 - resistance;
                    f1 = amount * (float) j;
                    amount = f1 / 25.0F;
                }

                return amount;
            }
        }
    }

    public static void knockBack(EntityLivingBase living, Entity attacker, float damage, double motionX, double motionZ, double multiplier) {
        if (living.getRNG().nextDouble() >= living.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue()) {
            living.isAirBorne = true;
            double horizontal = Math.sqrt(motionX * motionX + motionZ * motionZ);
            double magnitude = 0.4D * multiplier;
            living.motionX /= 2.0D;
            living.motionY /= 2.0D;
            living.motionZ /= 2.0D;
            living.motionX -= motionX / horizontal * magnitude;
            living.motionY += magnitude;
            living.motionZ -= motionZ / horizontal * magnitude;

            if (living.motionY > 0.2D) {
                living.motionY = 0.2D * multiplier;
            }
        }
    }

    public static SoundEvent getDeathSound(EntityLivingBase living) {
        try {
            return (SoundEvent) getDeathSoundHandle.invokeExact(living);
        } catch (Throwable e) {
            MainRegistry.logger.catching(e);
        }
        return SoundEvents.ENTITY_GENERIC_DEATH;
    }

    public static SoundEvent getHurtSound(EntityLivingBase living, DamageSource source) {
        try {
            return (SoundEvent) getHurtSoundHandle.invokeExact(living, source);
        } catch (Throwable e) {
            MainRegistry.logger.catching(e);
        }
        return SoundEvents.ENTITY_GENERIC_HURT;
    }

    public static float getSoundVolume(EntityLivingBase living) {
        try {
            return (float) getSoundVolumeHandle.invokeExact(living);
        } catch (Throwable e) {
            MainRegistry.logger.catching(e);
        }
        return 1F;
    }

    public static float getSoundPitch(EntityLivingBase living) {
        try {
            return (float) getSoundPitchHandle.invokeExact(living);
        } catch (Throwable e) {
            MainRegistry.logger.catching(e);
        }
        return 1F;
    }
}
