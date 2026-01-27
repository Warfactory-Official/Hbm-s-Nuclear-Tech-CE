package com.hbm.items.armor;

import com.hbm.capability.HbmLivingProps;
import com.hbm.handler.ArmorModHandler;
import com.hbm.util.ContaminationUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemModMedal extends ItemArmorMod {
	private final float minusRads;
	private final float decayRate;

	public ItemModMedal(String s, float minusRads) {
		super(ArmorModHandler.plate_only, true, true, false, false, s);
		this.minusRads = minusRads;
		this.decayRate = (float)Math.pow(0.5, minusRads/6000);

	}
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn){
		list.add(TextFormatting.GOLD + "-"+minusRads*20+" RAD/s");
		if(15/minusRads < 60)
			list.add(TextFormatting.YELLOW + " "+15/minusRads+"s Item Decontamination Halflife");
		else
			list.add(TextFormatting.YELLOW + " "+15/(minusRads*60)+"min Item Decontamination Halflife");
		super.addInformation(stack, worldIn, list, flagIn);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.GOLD + "  " + stack.getDisplayName() + " (-"+minusRads*20+" RAD/s)"+TextFormatting.YELLOW + " ("+15/minusRads+"s Halflife)");
	}
	
	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {
        if (!entity.world.isRemote) {
            double rad = HbmLivingProps.getRadiation(entity);
            rad -= minusRads;
            HbmLivingProps.setRadiation(entity, Math.max(rad, 0));

			if(entity instanceof EntityPlayer){
				ContaminationUtil.neutronActivateInventory((EntityPlayer)entity, 0.0F, this.decayRate);
				((EntityPlayer)entity).inventoryContainer.detectAndSendChanges();
			}
		}
	}
}
