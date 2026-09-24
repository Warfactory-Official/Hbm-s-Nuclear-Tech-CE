package com.hbm.entity.cart;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.tool.ItemModMinecart;
import com.hbm.items.tool.ItemModMinecart.EnumCartBase;
import com.hbm.items.tool.ItemModMinecart.EnumMinecart;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

@AutoRegister(name = "entity_ntm_cart_ore", trackingRange = 250, sendVelocityUpdates = false)
public class EntityMinecartOre extends EntityMinecartNTM {

    public EntityMinecartOre(World world) {
        super(world);
    }

    public EntityMinecartOre(World world, double x, double y, double z, EnumCartBase type) {
        super(world, x, y, z, type);
    }

    @Override
    public @NotNull ItemStack getCartItem() {
        return ItemModMinecart.createCartItem(this.getBase(), EnumMinecart.EMPTY);
    }
}
