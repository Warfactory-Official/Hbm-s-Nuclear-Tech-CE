package com.hbm.hazard.type;

import com.hbm.config.RadiationConfig;
import com.hbm.hazard.modifier.IHazardModifier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

//mlbv: made it an interface to be more flexible, was abstract class HazardTypeBase
public interface IHazardType {
	int hazardRate = RadiationConfig.hazardRate;

	/**
	 * Does the thing. Called by HazardEntry.applyHazard
	 * @param target the holder
	 * @param level the final level after calculating all the modifiers
	 */
    void onUpdate(EntityLivingBase target, double level, ItemStack stack);

	/**
	 * Updates the hazard for dropped items. Used for things like explosive and hydroactive items.
	 * @param item
	 * @param level
	 */
    void updateEntity(EntityItem item, double level);
	
	/**
	 * Adds item tooltip info. Called by Item.addInformation
	 * @param player
	 * @param list
	 * @param level the base level, mods are passed separately
	 * @param stack
	 * @param modifiers
	 */
	@SideOnly(Side.CLIENT)
    void addHazardInformation(EntityPlayer player, List<String> list, double level, ItemStack stack, List<IHazardModifier> modifiers);

    @FunctionalInterface
    interface HazardInfoConsumer {
        void accept(EntityPlayer player, List<String> list, double level, ItemStack stack, List<IHazardModifier> modifiers);
    }
}
