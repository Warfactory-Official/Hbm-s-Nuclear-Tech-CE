package com.hbm.items.armor;

import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import com.hbm.main.MainRegistry;
import com.hbm.particle.helper.HbmEffectNT;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class ItemModWD40 extends ItemArmorMod {

	public ItemModWD40(String s) {
		super(ArmorModHandler.extra, true, true, true, true, s);
	}
	
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn){
		String color = "" + (System.currentTimeMillis() % 1000 < 500 ? TextFormatting.BLUE : TextFormatting.YELLOW);

		list.add(color + "Highly reduces damage taken by armor, +2 HP");
		list.add("Dropped by 1:500 Cyber Crab");
		list.add("");
		super.addInformation(stack, worldIn, list, flagIn);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		
		String color = "" + (System.currentTimeMillis() % 1000 < 500 ? TextFormatting.BLUE : TextFormatting.YELLOW);
		
		list.add(color + "  " + stack.getDisplayName() + " (-80% armor wear / +2 HP)");
	}
	
	@Override
	public void modDamage(LivingHurtEvent event, ItemStack armor) {
		if(!event.getEntityLiving().world.isRemote && armor.getItemDamage() > 0 && event.getEntityLiving().getRNG().nextInt(5) != 0) {
			armor.setItemDamage(armor.getItemDamage() - 1);
		}
	}
	
	@Override
	public Multimap<String, AttributeModifier> getModifiers(EntityEquipmentSlot slot, ItemStack armor){
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, armor);
		multimap.put(SharedMonsterAttributes.MAX_HEALTH.getName(),
				new AttributeModifier(ArmorModHandler.UUIDs[((ItemArmor)armor.getItem()).armorType.getIndex()], "NTM Armor Mod Health", 4, 0));
		return multimap;
	}
	
	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {
		
		if(entity.world.isRemote && entity.hurtTime > 0) {
			NBTTagCompound data = new NBTTagCompound();
			data.setDouble("mX", 0.01);
			data.setDouble("mY", 0.5);
			data.setDouble("mZ", 0.8);
			MainRegistry.proxy.effectNT(HbmEffectNT.VanillaExt_RedDust,
                    entity.posX + (entity.getRNG().nextDouble() - 0.5) * entity.width * 2,
                    entity.posY - entity.getYOffset() + entity.getRNG().nextDouble() * entity.height,
                    entity.posZ + (entity.getRNG().nextDouble() - 0.5) * entity.width * 2,
                    data);
		}
	}
}