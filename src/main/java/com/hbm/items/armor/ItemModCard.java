package com.hbm.items.armor;

import com.hbm.capability.HbmCapability;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class ItemModCard extends ItemArmorMod {

    public ItemModCard(String s) {
        super(ArmorModHandler.helmet_only, true, true, false, false, s);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
        if(this == ModItems.card_aos) {
            list.add(TextFormatting.RED + "Top of the line!");
            list.add(TextFormatting.RED + "Guns now have a 33% chance to not consume ammo.");
        }
        if(this == ModItems.card_qos) {
            list.add(TextFormatting.RED + "Power!");
            list.add(TextFormatting.RED + "Adds a 33% chance to tank damage with no cap.");
        }
        list.add("");
        super.addInformation(stack, worldIn, list, flagIn);
    }

    @Override
    public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
        list.add(TextFormatting.RED + stack.getDisplayName());
    }

    @Override
    public void modDamage(LivingHurtEvent event, ItemStack armor) {
        if(this == ModItems.card_qos && event.getEntityLiving().getRNG().nextInt(3) == 0 && event.getEntityLiving() instanceof EntityPlayer player) {
            HbmCapability.plink(player, SoundEvents.ENTITY_ITEM_BREAK, 0.5F, 1.0F + player.getRNG().nextFloat() * 0.5F);
            event.setAmount(0);
            event.setCanceled(true);
        }
    }
}
