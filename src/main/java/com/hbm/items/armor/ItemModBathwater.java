package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class ItemModBathwater extends ItemArmorMod {

	public ItemModBathwater(String s) {
		super(ArmorModHandler.extra, true, true, true, true, s);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn){
		String color = "";
		if(this == ModItems.bathwater){
			color = "" + (System.currentTimeMillis() % 1000 < 500 ? TextFormatting.BLUE : TextFormatting.LIGHT_PURPLE);
			list.add(color + "Inflicts Poison II on the attacker");
		}
		if(this == ModItems.bathwater_mk2){
			color = "" + (System.currentTimeMillis() % 1000 < 500 ? TextFormatting.GREEN : TextFormatting.YELLOW);
			list.add(color + "Inflicts Wither IV on the attacker");
		}
		list.add("");
		super.addInformation(stack, worldIn, list, flagIn);
	}
	
	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		String color = "";
		if(this == ModItems.bathwater){
			color = "" + (System.currentTimeMillis() % 1000 < 500 ? TextFormatting.BLUE : TextFormatting.LIGHT_PURPLE);
			list.add(color + "  " + stack.getDisplayName() + " (Poisons attackers)");
		}
		if(this == ModItems.bathwater_mk2){
			color = "" + (System.currentTimeMillis() % 1000 < 500 ? TextFormatting.GREEN : TextFormatting.YELLOW);
			list.add(color + "  " + stack.getDisplayName() + " (Withers attackers)");
		}
	}
	
	@Override
	public void modDamage(LivingHurtEvent event, ItemStack armor) {
		
		if(!event.getEntityLiving().world.isRemote) {

			if(event.getSource() instanceof EntityDamageSource) {
				
				Entity attacker = ((EntityDamageSource)event.getSource()).getTrueSource();
				
				if(attacker instanceof EntityLivingBase) {
					
					if(this == ModItems.bathwater)
						((EntityLivingBase)attacker).addPotionEffect(new PotionEffect(MobEffects.POISON, 200, 2));
					
					else if(this == ModItems.bathwater_mk2)
						((EntityLivingBase)attacker).addPotionEffect(new PotionEffect(MobEffects.WITHER, 200, 4));
				}
			}
		}
	}
}