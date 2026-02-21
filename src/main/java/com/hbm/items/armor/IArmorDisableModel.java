package com.hbm.items.armor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IArmorDisableModel {

    boolean disablesPart(EntityPlayer player, ItemStack stack, EnumPlayerPart part);

    enum EnumPlayerPart {
        HEAD,
        HAT,
        BODY,
        LEFT_ARM,
        RIGHT_ARM,
        LEFT_LEG,
        RIGHT_LEG;

        public static EnumPlayerPart[] VALUES = values();
    }
}
