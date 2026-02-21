package com.hbm.hazard.type;

import com.hbm.capability.HbmLivingProps;
import com.hbm.config.RadiationConfig;
import com.hbm.handler.ArmorUtil;
import com.hbm.hazard.modifier.IHazardModifier;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.I18nUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class HazardTypeAsbestos implements IHazardType {

	@Override
    public void onUpdate(final EntityLivingBase target, final double level, final ItemStack stack) {

		if (RadiationConfig.disableAsbestos)
			return;

        if (ArmorRegistry.hasProtection(target, EntityEquipmentSlot.HEAD, HazardClass.PARTICLE_FINE))
            ArmorUtil.damageGasMaskFilter(target, (int) level * hazardRate);
        else
            HbmLivingProps.incrementAsbestos(target, (int) Math.min(level, 10) * hazardRate);
    }


	@Override
    public void updateEntity(final EntityItem item, final double level) {
    }

	@Override
	@SideOnly(Side.CLIENT)
    public void addHazardInformation(final EntityPlayer player, final List<String> list, final double level, final ItemStack stack, final List<IHazardModifier> modifiers) {
        list.add("§f[" + I18nUtil.resolveKey("trait.asbestos") + "]");
    }
}
