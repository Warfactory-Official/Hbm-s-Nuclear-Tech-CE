package com.hbm.entity.mob;

import com.hbm.entity.grenade.EntityGrenadeStrong;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
@AutoRegister(name = "entity_ntm_fbi_drone", trackingRange = 80, updateFrequency = 3, eggColors = {0x008000, 0x404040})
public class EntityFBIDrone extends EntityUFOBase {

    private int attackCooldown;

    public EntityFBIDrone(World worldIn) {
        super(worldIn);
    }

    @Override
    protected void updateEntityActionState() {
        super.updateEntityActionState();
        if (this.courseChangeCooldown > 0) {
            this.courseChangeCooldown--;
        }
        if (this.scanCooldown > 0) {
            this.scanCooldown--;
        }

        if (!this.world.isRemote) {
            if (this.attackCooldown > 0) {
                this.attackCooldown--;
            }

            if (this.target != null && this.attackCooldown <= 0) {
                Vec3d vec = new Vec3d(this.posX - this.target.posX, this.posY - this.target.posY, this.posZ - this.target.posZ);
                if (Math.abs(vec.x) < 5.0D && Math.abs(vec.z) < 5.0D && vec.y > 3.0D) {
                    this.attackCooldown = 60;
                    EntityGrenadeStrong grenade = new EntityGrenadeStrong(this.world);
                    grenade.setPosition(this.posX, this.posY, this.posZ);
                    this.world.spawnEntity(grenade);
                }
            }
        }

        if (this.courseChangeCooldown > 0) {
            this.approachPosition(this.target == null ? 0.25D : 0.5D);
        }
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(35.0D);
    }

    @Override
    protected int getScanRange() {
        return 100;
    }

    @Override
    protected int targetHeightOffset() {
        return 7 + this.rand.nextInt(4);
    }

    @Override
    protected int wanderHeightOffset() {
        return 7 + this.rand.nextInt(4);
    }
}
