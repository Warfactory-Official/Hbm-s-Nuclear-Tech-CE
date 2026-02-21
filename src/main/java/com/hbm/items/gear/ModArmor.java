package com.hbm.items.gear;

import com.hbm.Tags;
import com.hbm.items.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class ModArmor extends ItemArmor {

	public ModArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn, String s) {
		super(materialIn, renderIndexIn, equipmentSlotIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(CreativeTabs.COMBAT);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		if(stack.getItem().equals(ModItems.steel_helmet) || stack.getItem().equals(ModItems.steel_plate) || stack.getItem().equals(ModItems.steel_boots)) {
			return (Tags.MODID + ":textures/armor/steel_1.png");
		}
		if(stack.getItem().equals(ModItems.steel_legs)) {
			return (Tags.MODID + ":textures/armor/steel_2.png");
		}
		if(stack.getItem().equals(ModItems.titanium_helmet) || stack.getItem().equals(ModItems.titanium_plate) || stack.getItem().equals(ModItems.titanium_boots)) {
			return (Tags.MODID + ":textures/armor/titanium_1.png");
		}
		if(stack.getItem().equals(ModItems.titanium_legs)) {
			return (Tags.MODID + ":textures/armor/titanium_2.png");
		}
		if(stack.getItem().equals(ModItems.alloy_helmet) || stack.getItem().equals(ModItems.alloy_plate) || stack.getItem().equals(ModItems.alloy_boots)) {
			return (Tags.MODID + ":textures/armor/alloy_1.png");
		}
		if(stack.getItem().equals(ModItems.alloy_legs)) {
			return (Tags.MODID + ":textures/armor/alloy_2.png");
		}
		if(stack.getItem().equals(ModItems.cmb_helmet) || stack.getItem().equals(ModItems.cmb_plate) || stack.getItem().equals(ModItems.cmb_boots)) {
			return (Tags.MODID + ":textures/armor/cmb_1.png");
		}
		if(stack.getItem().equals(ModItems.cmb_legs)) {
			return (Tags.MODID + ":textures/armor/cmb_2.png");
		}
		if(stack.getItem().equals(ModItems.paa_helmet) || stack.getItem().equals(ModItems.paa_plate) || stack.getItem().equals(ModItems.paa_boots)) {
			return (Tags.MODID + ":textures/armor/paa_1.png");
		}
		if(stack.getItem().equals(ModItems.paa_legs)) {
			return (Tags.MODID + ":textures/armor/paa_2.png");
		}
		if(stack.getItem().equals(ModItems.asbestos_helmet) || stack.getItem().equals(ModItems.asbestos_plate) || stack.getItem().equals(ModItems.asbestos_boots)) {
			return (Tags.MODID + ":textures/armor/asbestos_1.png");
		}
		if(stack.getItem().equals(ModItems.asbestos_legs)) {
			return (Tags.MODID + ":textures/armor/asbestos_2.png");
		}
		if(stack.getItem().equals(ModItems.jackt)) {
			return (Tags.MODID + ":textures/armor/jackt.png");
		}
		if(stack.getItem().equals(ModItems.jackt2)) {
			return (Tags.MODID + ":textures/armor/jackt2.png");
		}
		if(stack.getItem().equals(ModItems.security_helmet) || stack.getItem().equals(ModItems.security_plate) || stack.getItem().equals(ModItems.security_boots)) {
			return (Tags.MODID + ":textures/armor/security_1.png");
		}
		if(stack.getItem().equals(ModItems.security_legs)) {
			return (Tags.MODID + ":textures/armor/security_2.png");
		}
		
		return null;
	}

}
