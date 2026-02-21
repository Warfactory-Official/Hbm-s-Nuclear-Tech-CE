package com.hbm.items.special;

import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ItemEnums;
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
        tooltip.add("Skin:");
        switch (ItemEnums.SoyuzSkinType.values()[stack.getMetadata()]) {
            case NORMAL -> tooltip.add(TextFormatting.GOLD + "Original");
            case LUNAR -> tooltip.add(TextFormatting.BLUE + "Luna Space Center");
            case POST_WAR -> tooltip.add(TextFormatting.GREEN + "Post War");
        }
    }
}
