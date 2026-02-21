package com.hbm.items.armor;

import com.hbm.capability.HbmLivingProps;
import com.hbm.handler.ArmorModHandler;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.potion.HbmPotion;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemModAuto extends ItemArmorMod {

	public ItemModAuto(String s) {
		super(ArmorModHandler.extra, false, true, false, false, s);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn){
		list.add(TextFormatting.BLUE + "Imported from Japsterdam.");
		
		list.add("");
		super.addInformation(stack, worldIn, list, flagIn);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.BLUE + "  " + stack.getDisplayName());
	}
	
	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {
        if (!entity.world.isRemote) {

            if (HbmLivingProps.getDigamma(entity) >= 5D) {
                ArmorModHandler.removeMod(armor, ArmorModHandler.extra);
                entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, HBMSoundHandler.syringeUse, SoundCategory.PLAYERS, 1.0F, 1.0F);
                HbmLivingProps.setDigamma(entity, HbmLivingProps.getDigamma(entity) - 5D);
                entity.addPotionEffect(new PotionEffect(HbmPotion.stability, 60 * 20, 0));
                entity.heal(20F);
            }
        }
    }
}
