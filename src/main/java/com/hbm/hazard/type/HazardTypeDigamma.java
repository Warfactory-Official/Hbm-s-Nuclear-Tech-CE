package com.hbm.hazard.type;

import com.hbm.hazard.modifier.IHazardModifier;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.I18nUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class HazardTypeDigamma implements IHazardType {

	@Override
    public void onUpdate(final EntityLivingBase target, final double level, final ItemStack stack) {
        ContaminationUtil.applyDigammaData(target, (level * stack.getCount() / 20D) * hazardRate);
    }

    @Override
    public void updateEntity(final EntityItem item, final double level) {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addHazardInformation(final EntityPlayer player, final List<String> list, double level, final ItemStack stack, final List<IHazardModifier> modifiers) {
        level = IHazardModifier.evalAllModifiers(stack, player, level, modifiers);

        final double displayLevel = Math.round(level * 10000D) / 10D;
        list.add(TextFormatting.RED + "[" + I18nUtil.resolveKey("trait.digamma") + "]");
        list.add(TextFormatting.DARK_RED + "" + displayLevel + "mDRX/s");

        if (stack.getCount() > 1) {
            final double stackLevel = displayLevel * stack.getCount();
            list.add(TextFormatting.DARK_RED + "Stack: " + stackLevel + "mDRX/s");
        }
    }

}
