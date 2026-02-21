package com.hbm.items.food;

import com.hbm.items.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.List;

public class ItemLemon extends ItemFood {

	public ItemLemon(int amount, float saturation, boolean isWolfFood, String s) {
		super(amount, saturation, isWolfFood);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		if(this == ModItems.lemon) {
			list.add("Eh, good enough.");
		}
		
		if(this == ModItems.med_ipecac) {
			list.add("Bitter juice that will cause your stomach");
			list.add("to forcefully eject it's contents.");
		}
		
		if(this == ModItems.med_ptsd) {
			list.add("This isn't even PTSD mediaction, it's just");
			list.add("Ipecac in a different bottle!");
		}
		
		if(this == ModItems.med_schizophrenia) {
			list.add("Makes the voices go away. Just for a while.");
			list.add("");
			list.add("...");
			list.add("Better not take it.");
		}
		
		if(this == ModItems.loops) {
			list.add("Brøther, may I have some lööps?");
		}
		
		if(this == ModItems.loop_stew) {
			list.add("A very, very healthy breakfast.");
		}
		
		if(this == ModItems.twinkie) {
			list.add("Expired 600 years ago!");
		}
		if(this == ModItems.pudding) {
			list.add("What if he did?");
			list.add("What if he didn't?");
			list.add("What if the world was made of pudding?");
		}
		if(this == ModItems.ingot_semtex) {
			list.add("Semtex H Plastic Explosive");
			list.add("Performant explosive for many applications.");
			list.add("Edible");
		}

		if(this == ModItems.marshmallow) {
			list.add("Gets grilled in the heat of burning nuclear failure");
		}

		if(this == ModItems.marshmallow_roasted) {
			list.add("Hmmm... tastes a bit metallic");
		}
	}
	
	
	@Override
	protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
		if(this == ModItems.med_ipecac || this == ModItems.med_ptsd) {
			player.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 50, 49));
		}
		
		if(this == ModItems.loop_stew) {
			player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 20 * 20, 1));
			player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 60 * 20, 2));
			player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 60 * 20, 1));
			player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 20 * 20, 2));
		}
	}
	
	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
		ItemStack sta = super.onItemUseFinish(stack, worldIn, entityLiving);
        
        if(this == ModItems.loop_stew)
        	return new ItemStack(Items.BOWL);
        
        return sta;
	}

	public static void tryAddItem(EntityPlayer player, ItemStack stack) {
		if(!player.inventory.addItemStackToInventory(stack)) {
			player.dropItem(stack, false);
		}
	}
}
