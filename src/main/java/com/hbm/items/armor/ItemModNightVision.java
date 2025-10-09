package com.hbm.items.armor;

import com.hbm.capability.HbmCapability;
import com.hbm.handler.ArmorModHandler;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemModNightVision extends ItemArmorMod {
    private static final String NIGHT_VISION_ACTIVE_NBT_KEY = "ITEM_MOD_NV_ACTIVE";

    public ItemModNightVision(String s) {
        super(ArmorModHandler.helmet_only, true, false, false, false, s);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
        list.add(TextFormatting.AQUA + I18nUtil.resolveKey("item.night_vision.description.item"));
        list.add("");
        super.addInformation(stack, worldIn, list, flagIn);
    }

    @Override
    public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
        list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("item.night_vision.description.in_armor", stack.getDisplayName()));
    }

    @Override
    public void modUpdate(EntityLivingBase entity, ItemStack armor) {
        if(!entity.world.isRemote && entity instanceof EntityPlayer player && armor.getItem() instanceof ArmorFSBPowered && ArmorFSBPowered.hasFSBArmor(player)) {
            if(HbmCapability.getData(entity).getEnableHUD()) {
                // 15 seconds to make less flickering if the client lags
                entity.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 15 * 20));
                if(!armor.hasTagCompound()) {
                    armor.setTagCompound(new NBTTagCompound());
                }
                if(!armor.getTagCompound().hasKey(NIGHT_VISION_ACTIVE_NBT_KEY)) {
                    armor.getTagCompound().setBoolean(NIGHT_VISION_ACTIVE_NBT_KEY, true); // Value does not matter, it's just a flag
                }

                if (entity.getRNG().nextInt(100) == 0) {
                    armor.damageItem(1, entity);
                }
            } else if(armor.hasTagCompound() && armor.getTagCompound().hasKey(NIGHT_VISION_ACTIVE_NBT_KEY)) { // Disable night vision if it was the armor mod that applied it to avoid removing other night vision sources.
                entity.removePotionEffect(MobEffects.NIGHT_VISION);
                armor.getTagCompound().removeTag(NIGHT_VISION_ACTIVE_NBT_KEY);
            }
        }
    }
}
