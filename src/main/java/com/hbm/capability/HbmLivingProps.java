package com.hbm.capability;

import com.hbm.capability.HbmLivingCapability.IEntityHbmProps;
import com.hbm.config.RadiationConfig;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.AdvancementManager;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.packet.toclient.PlayerInformPacketLegacy;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import java.util.List;
import java.util.UUID;

public class HbmLivingProps {

    public static final UUID digamma_UUID = UUID.fromString("2a3d8aec-5ab9-4218-9b8b-ca812bdf378b");
    public static final int maxAsbestos = 60 * 60 * 20;
    public static final int maxBlacklung = 2 * 60 * 60 * 20;

    public static IEntityHbmProps getData(EntityLivingBase entity) {
        return entity.hasCapability(HbmLivingCapability.EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null)
                ? entity.getCapability(HbmLivingCapability.EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null)
                : HbmLivingCapability.EntityHbmPropsProvider.DUMMY;
    }

    /// RADIATION ///
    public static double getRadiation(EntityLivingBase entity) {
        if (!RadiationConfig.enableContamination) return 0;
        return getData(entity).getRads();
    }

    public static void setRadiation(EntityLivingBase entity, double rad) {
        if (RadiationConfig.enableContamination) getData(entity).setRads(rad);
    }

    public static void incrementRadiation(EntityLivingBase entity, double rad) {
        if (!RadiationConfig.enableContamination) return;
        double radiation = getRadiation(entity) + rad;

        if (radiation > 25000000)
            radiation = 25000000;
        if (radiation < 0)
            radiation = 0;

        setRadiation(entity, radiation);
    }

    // Neutron Radiation

    public static double getNeutron(EntityLivingBase entity) {
        return getData(entity).getNeutrons();
    }

    public static void setNeutron(EntityLivingBase entity, double rad) {
        getData(entity).setNeutrons(rad);
    }


    /// RAD ENV ///
    public static double getRadEnv(EntityLivingBase entity) {
        return getData(entity).getRadsEnv();
    }

    public static void setRadEnv(EntityLivingBase entity, double rad) {
        getData(entity).setRadsEnv(rad);
    }

    /// RAD BUF ///
    public static double getRadBuf(EntityLivingBase entity) {
        return getData(entity).getRadBuf();
    }

    public static void setRadBuf(EntityLivingBase entity, double rad) {
        getData(entity).setRadBuf(rad);
    }

    /// DIGAMA ///
    public static double getDigamma(EntityLivingBase entity) {
        return getData(entity).getDigamma();
    }

    public static void setDigamma(EntityLivingBase entity, double digamma) {

        getData(entity).setDigamma(digamma);

        double healthMod = Math.pow(0.5, digamma) - 1D;

        IAttributeInstance attributeinstance = entity.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH);

        try {
            attributeinstance.removeModifier(attributeinstance.getModifier(digamma_UUID));
        } catch (Exception ex) {
        }

        attributeinstance.applyModifier(new AttributeModifier(digamma_UUID, "digamma", healthMod, 2));

        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }

        if ((entity.getMaxHealth() <= 0 || digamma >= 10.0D) && entity.isEntityAlive()) {
            entity.setAbsorptionAmount(0);
            entity.attackEntityFrom(ModDamageSource.digamma, 5000000F);
            entity.setHealth(0);
            entity.onDeath(ModDamageSource.digamma);

            NBTTagCompound data = new NBTTagCompound();
            data.setString("type", "sweat");
            data.setInteger("count", 50);
            data.setInteger("block", Block.getIdFromBlock(Blocks.SOUL_SAND));
            data.setInteger("entity", entity.getEntityId());
            PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, 0, 0, 0), new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 50));
        }

        if (entity instanceof EntityPlayer) {

            double di = getData(entity).getDigamma();

            if (di > 0D)
                AdvancementManager.grantAchievement(((EntityPlayer) entity), AdvancementManager.digammaSee);
            if (di >= 2D)
                AdvancementManager.grantAchievement(((EntityPlayer) entity), AdvancementManager.digammaFeel);
            if (di >= 10D)
                AdvancementManager.grantAchievement(((EntityPlayer) entity), AdvancementManager.digammaKnow);
        }
    }

    public static void incrementDigamma(EntityLivingBase entity, double digamma) {
        double dRad = getDigamma(entity) + digamma;

        if (dRad > 10)
            dRad = 10;
        if (dRad < 0)
            dRad = 0;

        setDigamma(entity, dRad);
    }

    /// ASBESTOS ///
    public static int getAsbestos(EntityLivingBase entity) {
        return getData(entity).getAsbestos();
    }

    public static void setAsbestos(EntityLivingBase entity, int asbestos) {
        IEntityHbmProps props = getData(entity);
        props.setAsbestos(asbestos);

        if (asbestos >= HbmLivingCapability.EntityHbmProps.maxAsbestos) {
            props.setAsbestos(0);
            entity.attackEntityFrom(ModDamageSource.asbestos, 1000);
        }
    }

    public static void incrementAsbestos(EntityLivingBase entity, int asbestos) {
        setAsbestos(entity, getAsbestos(entity) + asbestos);

        if (entity instanceof EntityPlayerMP) {
            PacketDispatcher.wrapper.sendTo(new PlayerInformPacketLegacy(new TextComponentTranslation("info.asbestos").setStyle(new Style().setColor(TextFormatting.RED)), 10, 3000), (EntityPlayerMP) entity);
        }
    }

    public static void addCont(EntityLivingBase entity, ContaminationEffect cont) {
        getData(entity).getContaminationEffectList().add(cont);
    }

    /// BLACK LUNG DISEASE ///
    public static int getBlackLung(EntityLivingBase entity) {
        return getData(entity).getBlacklung();
    }

    public static void setBlackLung(EntityLivingBase entity, int blacklung) {
        IEntityHbmProps props = getData(entity);
        props.setBlacklung(blacklung);

        if (blacklung >= HbmLivingCapability.EntityHbmProps.maxBlacklung) {
            props.setBlacklung(0);
            entity.attackEntityFrom(ModDamageSource.blacklung, 1000);
        }
    }

    public static void incrementBlackLung(EntityLivingBase entity, int blacklung) {
        setBlackLung(entity, getBlackLung(entity) + blacklung);

        if (entity instanceof EntityPlayerMP) {
            PacketDispatcher.wrapper.sendTo(new PlayerInformPacketLegacy(new TextComponentTranslation("info.coaldust").setStyle(new Style().setColor(TextFormatting.RED)), 10, 3000), (EntityPlayerMP) entity);
        }
    }

    /// TIME BOMB ///
    public static int getTimer(EntityLivingBase entity) {
        return getData(entity).getBombTimer();
    }

    public static void setTimer(EntityLivingBase entity, int bombTimer) {
        getData(entity).setBombTimer(bombTimer);
    }

    /// CONTAGION ///
    public static int getContagion(EntityLivingBase entity) {
        return getData(entity).getContagion();
    }

    public static void setContagion(EntityLivingBase entity, int contageon) {
        getData(entity).setContagion(contageon);
    }

    public static List<ContaminationEffect> getCont(EntityLivingBase e) {
        return getData(e).getContaminationEffectList();
    }

    /// OIL //
    public static int getOil(EntityLivingBase entity) {
        return getData(entity).getOil();
    }

    public static void setOil(EntityLivingBase entity, int oil) {
        getData(entity).setOil(oil);
    }


    public static class ContaminationEffect {

        public double maxRad;
        public int maxTime;
        public int time;
        public boolean ignoreArmor;

        public ContaminationEffect(double rad, int time, boolean ignoreArmor) {
            this.maxRad = rad;
            this.maxTime = this.time = time;
            this.ignoreArmor = ignoreArmor;
        }

        public static ContaminationEffect load(NBTTagCompound nbt, int index) {
            NBTTagCompound me = nbt.getCompoundTag("cont_" + index);
            double maxRad = me.getDouble("maxRad");
            int maxTime = me.getInteger("maxTime");
            int time = me.getInteger("time");
            boolean ignoreArmor = me.getBoolean("ignoreArmor");

            ContaminationEffect effect = new ContaminationEffect(maxRad, maxTime, ignoreArmor);
            effect.time = time;
            return effect;
        }

        public double getRad() {
            return maxRad * ((double) time / (double) maxTime);
        }

        public void save(NBTTagCompound nbt, int index) {
            NBTTagCompound me = new NBTTagCompound();
            me.setDouble("maxRad", this.maxRad);
            me.setInteger("maxTime", this.maxTime);
            me.setInteger("time", this.time);
            me.setBoolean("ignoreArmor", ignoreArmor);
            nbt.setTag("cont_" + index, me);
        }
    }
}
