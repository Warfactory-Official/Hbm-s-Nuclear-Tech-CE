package com.hbm.hazard.type;

import com.hbm.config.RadiationConfig;
import com.hbm.handler.ArmorUtil;
import com.hbm.hazard.helper.HazardHelper;
import com.hbm.hazard.modifier.IHazardModifier;
import com.hbm.util.I18nUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import static com.hbm.hazard.helper.HazardHelper.applyPotionEffect;

public class HazardTypeCold implements IHazardType {


    @Override
    public void onUpdate(final EntityLivingBase target, final double level, final ItemStack stack) {
        final boolean reacher = HazardHelper.isHoldingReacher(target);
        if (RadiationConfig.disableCold || reacher) return;

        if (target instanceof EntityPlayer && ArmorUtil.checkForHazmat(target)) return; // Early return if protected

        final int baseLevel = (int) level - 1;
        final int witherLevel = (int) level - 3;

        applyPotionEffect(target, MobEffects.MINING_FATIGUE, 110, baseLevel);
        applyPotionEffect(target, MobEffects.SLOWNESS, 110, Math.min(4, baseLevel));
        applyPotionEffect(target, MobEffects.WEAKNESS, 110, baseLevel);

        if (level > 4) {
           applyPotionEffect(target, MobEffects.WITHER, 110, witherLevel);
        }
    }

    @Override
    public void updateEntity(final EntityItem item, final double level) {

    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addHazardInformation(final EntityPlayer player, final List<String> list, final double level, final ItemStack stack, final List<IHazardModifier> modifiers) {
        list.add(TextFormatting.AQUA + "[" + I18nUtil.resolveKey("trait.cryogenic") + "]");

    }
}
