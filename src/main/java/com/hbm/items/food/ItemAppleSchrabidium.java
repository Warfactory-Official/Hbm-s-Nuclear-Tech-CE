package com.hbm.items.food;

import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.potion.HbmPotion;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.potion.PotionEffect;

public class ItemAppleSchrabidium extends ItemFood {

    public ItemAppleSchrabidium(int amount, float saturation, boolean isWolfFood, String s) {
        super(amount, saturation, isWolfFood);
        this.setHasSubtypes(true);
        this.setAlwaysEdible();
        this.setTranslationKey(s);
        this.setRegistryName(s);
        ModItems.ALL_ITEMS.add(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return stack.getItemDamage() == 2;
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        if (worldIn.isRemote) return;

        final Item item = stack.getItem();
        final int meta = stack.getItemDamage();

        if (item == ModItems.apple_schrabidium) {

            if (meta == 0) {
                player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 600, 4));
                player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 6000, 0));
                player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 6000, 0));
            }

            if (meta == 1) {
                player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 1200, 4));
                player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 1200, 4));
                player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 1200, 0));
                player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 1200, 4));
                player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 1200, 2));
                player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 1200, 2));
                player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 1200, 4));
                player.addPotionEffect(new PotionEffect(MobEffects.HEALTH_BOOST, 1200, 9));
                player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 1200, 4));
                player.addPotionEffect(new PotionEffect(MobEffects.SATURATION, 1200, 9));
            }

            if (meta == 2) {
                final int INF = Integer.MAX_VALUE;
                player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, INF, 4));
                player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, INF, 1));
                player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, INF, 0));
                player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, INF, 9));
                player.addPotionEffect(new PotionEffect(MobEffects.HASTE, INF, 4));
                player.addPotionEffect(new PotionEffect(MobEffects.SPEED, INF, 3));
                player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, INF, 4));
                player.addPotionEffect(new PotionEffect(MobEffects.HEALTH_BOOST, INF, 24));
                player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, INF, 14));
                player.addPotionEffect(new PotionEffect(MobEffects.SATURATION, INF, 99));
            }
        }

        if (item == ModItems.apple_lead) {

            if (meta == 0) {
                player.addPotionEffect(new PotionEffect(HbmPotion.lead, 15 * 20, 2));
            }

            if (meta == 1) {
                player.addPotionEffect(new PotionEffect(HbmPotion.lead, 60 * 20, 4));
            }

            if (meta == 2) {
                player.attackEntityFrom(ModDamageSource.lead, 500.0F);
            }
        }
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        switch (stack.getItemDamage()) {
            case 0:  return EnumRarity.UNCOMMON;
            case 1:  return EnumRarity.RARE;
            case 2:  return EnumRarity.EPIC;
            default: return EnumRarity.COMMON;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) return;

        items.add(new ItemStack(this, 1, 0));
        items.add(new ItemStack(this, 1, 1));
        items.add(new ItemStack(this, 1, 2));
    }
}
