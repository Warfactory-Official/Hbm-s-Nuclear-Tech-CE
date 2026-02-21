package com.hbm.entity.grenade;

import com.hbm.explosion.ExplosionNukeSmall;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemGrenade;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import java.util.Random;

@AutoRegister(name = "entity_grenade_nuclear")
public class EntityGrenadeNuclear extends EntityGrenadeBouncyBase {

    @SuppressWarnings("unused")
    public EntityGrenadeNuclear(World world) {
        super(world);
    }

    public EntityGrenadeNuclear(World p_i1774_1_, EntityLivingBase p_i1774_2_, EnumHand hand) {
        super(p_i1774_1_, p_i1774_2_, hand);
    }

    public EntityGrenadeNuclear(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Override
    public void explode() {
        if (!world.isRemote) {
            this.setDead();

            ExplosionNukeSmall.explode(world, posX, posY + 0.5, posZ, ExplosionNukeSmall.PARAMS_TOTS);
        }
    }

    @Override
    protected int getMaxTimer() {
        return ItemGrenade.getFuseTicks(ModItems.grenade_nuclear);
    }

    @Override
    protected double getBounceMod() {
        return 0.25D;
    }
}
