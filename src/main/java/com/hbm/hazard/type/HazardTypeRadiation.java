package com.hbm.hazard.type;

import com.hbm.config.GeneralConfig;
import com.hbm.hazard.helper.HazardHelper;
import com.hbm.hazard.modifier.IHazardModifier;
import com.hbm.lib.Library;
import com.hbm.util.BobMathUtil;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import com.hbm.util.I18nUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class HazardTypeRadiation implements IHazardType {

	@Override
    public void onUpdate(final EntityLivingBase target, double level, final ItemStack stack) {


		final boolean reacher = HazardHelper.isHoldingReacher(target);

        level *= stack.getCount();

        if (level > 0) {
            double rad = level / 20D;

            if (GeneralConfig.enable528 && reacher) {
                rad = rad / 49D;    //More realistic function for 528: x / distance^2
            } else if (reacher) {
                rad = BobMathUtil.sqrt(rad); //Reworked radiation function: sqrt(x+1/(x+2)^2)-1/(x+2)
            }

            ContaminationUtil.contaminate(target, HazardType.RADIATION, ContaminationType.CREATIVE, rad * hazardRate);
        }
    }

	@Override
    public void updateEntity(final EntityItem item, final double level) {
    }

	@Override
	@SideOnly(Side.CLIENT)
    public void addHazardInformation(final EntityPlayer player, final List<String> list, double level, final ItemStack stack, final List<IHazardModifier> modifiers) {

        level = IHazardModifier.evalAllModifiers(stack, player, level, modifiers);
		if(level == 0) return;
		list.add("§a[" + I18nUtil.resolveKey("trait.radioactive") + "]");
		list.add(" §e" + (Library.roundFloat(getNewValue(level), 3)+ getSuffix(level) + " " + I18nUtil.resolveKey("desc.rads")));

        if (stack.getCount() > 1) {
            double stackRad = level * stack.getCount();
            list.add(" §e" + I18nUtil.resolveKey("desc.stack") + " " + Library.roundFloat(getNewValue(stackRad), 3) + getSuffix(stackRad) + " " + I18nUtil.resolveKey("desc.rads"));
        }
    }


    public static double getNewValue(double radiation) {
        if (radiation < 1000000) {
            return radiation;
        } else if (radiation < 1000000000) {
            return radiation * 0.000001D;
        } else {
            return radiation * 0.000000001D;
        }
    }

    public static String getSuffix(double radiation) {
        if (radiation < 1000000) {
            return "";
        } else if (radiation < 1000000000) {
            return I18nUtil.resolveKey("desc.mil");
        } else {
            return I18nUtil.resolveKey("desc.bil");
        }
    }

}
