package com.hbm.items.weapon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class ItemGrenadeKyiv extends ItemGenericGrenade {

    public ItemGrenadeKyiv(int fuse, String s) {
        super(fuse, s);
    }

    @Override
    public void explode(Entity grenade, EntityLivingBase thrower, World world, double x, double y, double z) {
        world.newExplosion(grenade, x, y, z, 5F, true, true);
    }
}
