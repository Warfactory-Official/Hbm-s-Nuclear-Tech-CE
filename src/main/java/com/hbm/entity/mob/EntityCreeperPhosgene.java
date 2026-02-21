package com.hbm.entity.mob;

import com.hbm.entity.effect.EntityMist;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

@AutoRegister(name = "entity_mob_phosgene_creeper", trackingRange = 80, eggColors = {0xE3D398, 0xB8A06B})
public class EntityCreeperPhosgene extends EntityCreeper {

    public EntityCreeperPhosgene(World world) {
        super(world);
        this.fuseTime = 20; //ehehehehehe
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {

        if(!source.isDamageAbsolute() && !source.isUnblockable()) {
            amount -= 4F;
        }

        if(amount < 0) return false;

        return super.attackEntityFrom(source, amount);
    }

    @Override
    public boolean getCanSpawnHere() {
        return super.getCanSpawnHere() && this.posY <= 40 && this.dimension == 0;
    }

    @Override
    protected void explode() {
        if(!this.world.isRemote) {
            world.createExplosion(this, posX, posY + this.height / 2, posZ, 2F, false);
            EntityMist mist = new EntityMist(world);
            mist.setType(Fluids.PHOSGENE);
            mist.setPosition(posX, posY, posZ);
            mist.setArea(10, 5);
            mist.setDuration(150);
            world.spawnEntity(mist);
            this.setDead();
        }
    }
}
