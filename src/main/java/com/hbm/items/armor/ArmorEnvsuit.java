package com.hbm.items.armor;

import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;
import com.hbm.items.gear.ArmorFSB;
import com.hbm.render.model.ModelArmorEnvsuit;
import com.hbm.util.Vec3NT;
import net.minecraft.init.MobEffects;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.UUID;

public class ArmorEnvsuit extends ArmorFSBPowered {

	public ArmorEnvsuit(ArmorMaterial material, int layer, EntityEquipmentSlot slot, String texture, long maxPower, long chargeRate, long consumption, long drain, String s) {
		super(material, layer, slot, texture, maxPower, chargeRate, consumption, drain, s);
	}

	@SideOnly(Side.CLIENT)
	ModelArmorEnvsuit[] models;

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {

		if(models == null) {
			models = new ModelArmorEnvsuit[4];

			for(int i = 0; i < 4; i++)
				models[i] = new ModelArmorEnvsuit(i);
		}

		return models[3-armorSlot.getIndex()];
	}

	private static final UUID speed = UUID.fromString("6ab858ba-d712-485c-bae9-e5e765fc555a");

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {

		super.onArmorTick(world, player, stack);

		if(this != ModItems.envsuit_plate)
			return;

		/// SPEED ///
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(EntityEquipmentSlot.CHEST, stack);
		multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(speed, "SQUIRREL SPEED", 0.1, 0));
		player.getAttributeMap().removeAttributeModifiers(multimap);

		if(hasFSBArmor(player)) {

			if(player.isSprinting()) player.getAttributeMap().applyAttributeModifiers(multimap);

			if(player.isInWater()) {

				if(!world.isRemote) {
					player.setAir(300);
					player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 15 * 20, 0));
				}

				double mo = 0.1 * player.moveForward;
				Vec3NT vec = new Vec3NT(player.getLookVec());
				vec.setX(vec.x * mo);
				vec.setY(vec.y * mo);
				vec.setZ(vec.z * mo);

				player.motionX += vec.x;
				player.motionY += vec.y;
				player.motionZ += vec.z;
			} else {
				boolean canRemoveNightVision = true;
				ItemStack helmet = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
				ItemStack helmetMod = ArmorModHandler.pryMod(helmet, ArmorModHandler.helmet_only); // Get the modification!
				if (!helmetMod.isEmpty() && helmetMod.getItem() instanceof ItemModNightVision) {
					canRemoveNightVision = false;
				}

				if(!world.isRemote && canRemoveNightVision) {
					player.removePotionEffect(MobEffects.NIGHT_VISION);
				}
			}
		}
	}
}
