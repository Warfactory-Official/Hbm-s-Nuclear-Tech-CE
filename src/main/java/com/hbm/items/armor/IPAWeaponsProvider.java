package com.hbm.items.armor;

import com.hbm.main.MainRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public interface IPAWeaponsProvider {

    IPAMelee getMeleeComponent(EntityPlayer entity);

    static IPAMelee getMeleeComponentClient() {
        return getMeleeComponentCommon(MainRegistry.proxy.me());
    }

    static IPAMelee getMeleeComponentCommon(EntityPlayer player) {
        if (player == null) return null;
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (!chest.isEmpty() && chest.getItem() instanceof IPAWeaponsProvider prov) {
            return prov.getMeleeComponent(player);
        }
        return null;
    }

    IPARanged getRangedComponent(EntityPlayer entity);

    static IPARanged getRangedComponentClient() {
        return getRangedComponentCommon(MainRegistry.proxy.me());
    }

    static IPARanged getRangedComponentCommon(EntityPlayer player) {
        if (player == null) return null;
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (!chest.isEmpty() && chest.getItem() instanceof IPAWeaponsProvider prov) {
            return prov.getRangedComponent(player);
        }
        return null;
    }
}
