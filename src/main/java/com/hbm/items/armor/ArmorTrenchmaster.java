package com.hbm.items.armor;

import com.hbm.capability.HbmCapability;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;
import com.hbm.items.gear.ArmorFSB;
import com.hbm.render.model.ModelArmorTrenchmaster;
import com.hbm.util.I18nUtil;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ArmorTrenchmaster extends ArmorFSB {

    @SideOnly(Side.CLIENT)
    ModelArmorTrenchmaster[] models;

    public ArmorTrenchmaster(ArmorMaterial material, int layer, EntityEquipmentSlot slot, String texture, String s) {
        super(material, layer, slot, texture, s);
        this.setMaxDamage(0);
    }

    public static boolean isTrenchMaster(EntityPlayer player) {
        if (player == null) return false;
        return player.inventory.armorItemInSlot(2) != null && player.inventory.armorItemInSlot(2).getItem() == ModItems.trenchmaster_plate && ArmorFSB.hasFSBArmor(player);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {

        if (models == null) {
            models = new ModelArmorTrenchmaster[4];

            for (int i = 0; i < 4; i++)
                models[i] = new ModelArmorTrenchmaster(i);
        }

        return models[3 - armorSlot.getIndex()];
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn) {
        super.addInformation(stack, world, list, flagIn);

        //list.add(TextFormatting.RED + "  " + I18nUtil.resolveKey("armor.fasterReload"));
        list.add("§c" + "  " + I18nUtil.resolveKey("armor.moreAmmo"));
    }

    @Override
    public void handleHurt(LivingHurtEvent event) {
        super.handleHurt(event);
        if (event.getEntityLiving() instanceof EntityPlayer player) {
            if (ArmorFSB.hasFSBArmor(player)) {
                if (event.getSource().isExplosion() && event.getSource().getTrueSource() instanceof EntityPlayer) {
                    event.setAmount(0);
                }
            }
        }
    }

    @Override
    public void handleAttack(LivingAttackEvent event) {
        super.handleAttack(event);
        EntityLivingBase e = event.getEntityLiving();

        if (e instanceof EntityPlayer player) {

            if (ArmorFSB.hasFSBArmor(player)) {

                if (e.getRNG().nextInt(3) == 0) {
                    HbmCapability.plink(player, SoundEvents.ENTITY_ITEM_BREAK, 0.5F, 1.0F + e.getRNG().nextFloat() * 0.5F);
                    event.setCanceled(true);
                }
            }
        }
    }

	public static boolean hasAoS(EntityPlayer player) {
		if(player == null) return false;
		if(!player.inventory.armorItemInSlot(3).isEmpty()) {
			ItemStack[] mods =  ArmorModHandler.pryMods(player.inventory.armorItemInSlot(3));
			ItemStack helmet = mods[ArmorModHandler.helmet_only];
			return helmet != null && helmet.getItem() == ModItems.card_aos;
		}
		return false;
	}
}
