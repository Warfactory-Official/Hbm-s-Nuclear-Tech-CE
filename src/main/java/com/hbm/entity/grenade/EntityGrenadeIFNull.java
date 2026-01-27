package com.hbm.entity.grenade;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemGrenade;
import com.hbm.util.DelayedTick;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;

@AutoRegister(name = "entity_grenade_if_null")
public class EntityGrenadeIFNull extends EntityGrenadeBouncyBase {

    public EntityGrenadeIFNull(World p_i1773_1_)
    {
        super(p_i1773_1_);
    }

    public EntityGrenadeIFNull(World p_i1774_1_, EntityLivingBase p_i1774_2_, EnumHand hand)
    {
        super(p_i1774_1_, p_i1774_2_, hand);
    }

    public EntityGrenadeIFNull(World p_i1775_1_, double p_i1775_2_, double p_i1775_4_, double p_i1775_6_)
    {
        super(p_i1775_1_, p_i1775_2_, p_i1775_4_, p_i1775_6_);
    }

    @Override
    public void explode() {
        if (!this.world.isRemote) {
            this.setDead();
            MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for(int a = -3; a <= 3; a++)
                for(int b = -3; b <= 3; b++)
                    for(int c = -3; c <= 3; c++)
                        world.setBlockToAir(pos.setPos((int)posX + a, (int)posY + b, (int)posZ + c));
            List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB((int)posX + 0.5 - 3, (int)posY + 0.5 - 3, (int)posZ + 0.5 - 3, (int)posX + 0.5 + 3, (int)posY + 0.5 + 3, (int)posZ + 0.5 + 3));

            for(Entity entity : list) {
                tryKill(entity);
            }
        }
    }

    private static void tryKill(final Entity toKill) {
        if (toKill instanceof MultiPartEntityPart part) {
            if (part.parent instanceof Entity parent) {
                tryKill(parent);
                return;
            }
        }
        if (toKill instanceof EntityLivingBase livingBase) livingBase.setHealth(0f);
        else toKill.setDead();
        DelayedTick.nextWorldTickEnd(toKill.world, w -> {
            if (!toKill.isDead && toKill.isEntityAlive()) {
                WorldServer ws = (WorldServer) w;
                if (toKill instanceof EntityPlayer) {
                    ws.removeEntity(toKill);
                    return;
                }
                if (toKill.isBeingRidden()) toKill.removePassengers();
                if (toKill.isRiding()) toKill.dismountRidingEntity();
                Entity[] parts = toKill.getParts();
                if (parts != null) {
                    for (Entity p : parts) {
                        if (p != null) {
                            if (p.isBeingRidden()) p.removePassengers();
                            if (p.isRiding()) p.dismountRidingEntity();
                            ws.removeEntityDangerously(p);
                        }
                    }
                }
                ws.removeEntityDangerously(toKill);
            }
        });
    }

	@Override
	protected int getMaxTimer() {
		return ItemGrenade.getFuseTicks(ModItems.grenade_if_null);
	}

	@Override
	protected double getBounceMod() {
		return 0.25D;
	}
}
