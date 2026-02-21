package com.hbm.hazard.type;

import com.hbm.config.RadiationConfig;
import com.hbm.hazard.helper.HazardHelper;
import com.hbm.hazard.modifier.IHazardModifier;
import com.hbm.util.I18nUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class HazardTypeHot implements IHazardType {

	@Override
    public void onUpdate(final EntityLivingBase target, final double level, final ItemStack stack) {

		final boolean wetOrReacher = HazardHelper.isHoldingReacher(target) || target.isWet() ;
		if(RadiationConfig.disableHot || wetOrReacher) return;
		if(target instanceof EntityPlayer player && player.capabilities.isCreativeMode) return;
        target.setFire((int) Math.ceil(level) * hazardRate);
    }

    @Override
    public void updateEntity(final EntityItem item, final double level) {
    }

	@Override
	@SideOnly(Side.CLIENT)
    public void addHazardInformation(final EntityPlayer player, final List<String> list, double level, final ItemStack stack, final List<IHazardModifier> modifiers) {

        level = IHazardModifier.evalAllModifiers(stack, player, level, modifiers);

        if (level > 0)
            list.add(TextFormatting.GOLD + "[" + I18nUtil.resolveKey("trait.hot") + "]");
    }

}
