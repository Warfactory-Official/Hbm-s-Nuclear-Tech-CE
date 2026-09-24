package com.hbm.items.armor;

import com.hbm.capability.HbmCapability;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemModLodestone extends ItemArmorMod {

	int range;

	public ItemModLodestone(int range, String s) {
		super(ArmorModHandler.extra, true, true, true, true, s);
		this.range = range;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn){
		list.add(TextFormatting.DARK_GRAY + "Attracts nearby items");
		list.add(TextFormatting.DARK_GRAY + "Item attraction range: " + range);
		if(this == ModItems.lodestone)
			list.add("Dropped by 1:64 Smelted Iron Ingots");
		list.add("");
		super.addInformation(stack, worldIn, list, flagIn);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.DARK_GRAY + "  " + stack.getDisplayName() + " (Magnetic range: " + range + ")");
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {

		// No magnet if keybind toggled
		if(entity instanceof EntityPlayer && !HbmCapability.getData((EntityPlayer) entity).isMagnetActive()) return;

		List<EntityItem> items = entity.world.getEntitiesWithinAABB(EntityItem.class, entity.getEntityBoundingBox().grow(range, range, range));
		
		for(EntityItem item : items) {
			
			Vec3d vec = new Vec3d(entity.posX - item.posX, entity.posY - item.posY, entity.posZ - item.posZ).normalize();

			item.motionX += vec.x * 0.05;
			item.motionY += vec.y * 0.05;
			item.motionZ += vec.z * 0.05;
			
			if(vec.y > 0 && item.motionY < 0.04)
				item.motionY += 0.2;
		}
	}
}