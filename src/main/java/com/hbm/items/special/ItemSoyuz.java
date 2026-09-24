package com.hbm.items.special;

import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ItemEnums;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemSoyuz extends ItemEnumMulti<ItemEnums.SoyuzSkinType> {

    public ItemSoyuz(String s) {
        super(s, ItemEnums.SoyuzSkinType.VALUES, false, true);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return switch (ItemEnums.SoyuzSkinType.values()[stack.getMetadata()]) {
            case NORMAL -> EnumRarity.COMMON;
            case LUNAR -> EnumRarity.RARE;
            case POST_WAR -> EnumRarity.EPIC;
        };
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        String desc = I18nUtil.resolveKey("item.missile_soyuz.desc") + " ";
        switch (ItemEnums.SoyuzSkinType.values()[stack.getMetadata()]) {
            case NORMAL -> tooltip.add(desc + TextFormatting.GOLD + I18nUtil.resolveKey("item.missile_soyuz.orig.desc"));
            case LUNAR -> tooltip.add(desc + TextFormatting.BLUE + I18nUtil.resolveKey("item.missile_soyuz.luna.desc"));
            case POST_WAR -> tooltip.add(desc + TextFormatting.GREEN + I18nUtil.resolveKey("item.missile_soyuz.war.desc"));
        }
    }
}
