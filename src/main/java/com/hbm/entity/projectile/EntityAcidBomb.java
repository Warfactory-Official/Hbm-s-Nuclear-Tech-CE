package com.hbm.entity.projectile;

import com.hbm.entity.mob.glyphid.EntityGlyphid;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ModDamageSource;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

@AutoRegister(name = "entity_acid_bomb", trackingRange = 1000)
public class EntityAcidBomb extends EntityThrowableInterp {
    public float damage = 1.5F;

    public EntityAcidBomb(World world) {
        super(world);
    }

    public EntityAcidBomb(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if(world.isRemote) return;

        if(result.typeOfHit == RayTraceResult.Type.ENTITY) {

            if(!(result.entityHit instanceof EntityGlyphid)) {
                result.entityHit.attackEntityFrom(new EntityDamageSourceIndirect(ModDamageSource.s_acid, this, thrower), damage);
                this.setDead();
            }
        }

        if(result.typeOfHit == RayTraceResult.Type.BLOCK)
            this.setDead();
    }

    @Override
    public float getGravityVelocity() {
        return 0.04F;
    }

    @Override
    protected float getAirDrag() {
        return 1.0F;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setFloat("damage", damage);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        this.damage = nbt.getFloat("damage");
    }
}
