package com.hbm.entity.projectile;

import com.hbm.interfaces.AutoRegister;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
@AutoRegister(name = "entity_coin", trackingRange = 1000)
public class EntityCoin extends EntityThrowableInterp {

    public EntityCoin(World world) {
        super(world);
        this.setSize(1F, 1F);
        this.height = 0.5F;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
    }

    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        float f = this.width / 2.0F;
        this.setEntityBoundingBox(new AxisAlignedBB(x - (double) f, y - 0.5D, z - (double) f, x + (double) f, y - 0.5D + (double) this.height, z + (double) f));
    }

    @Override
    protected void onImpact(RayTraceResult mop) {
        if(mop.typeOfHit == RayTraceResult.Type.BLOCK) this.setDead();
    }

    @Override
    protected float getAirDrag() {
        return 1F;
    }

    @Override
    public float getGravityVelocity() {
        return 0.02F;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

}
