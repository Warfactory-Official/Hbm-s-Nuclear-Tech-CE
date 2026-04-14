package com.hbm.items.weapon.grenade;

import com.hbm.entity.grenade.EntityGrenadeUniversal;
import com.hbm.items.ItemEnumMulti;
import com.hbm.items.weapon.grenade.ItemGrenadeFuze.EnumGrenadeFuze;
import com.hbm.util.Vec3NT;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ItemGrenadeExtra extends ItemEnumMulti<ItemGrenadeExtra.EnumGrenadeExtra> {

    public ItemGrenadeExtra(String registryName) {
        super(registryName, EnumGrenadeExtra.VALUES, true, true);
    }

    public enum EnumGrenadeExtra {
        GLUE(null, EXTRA_GLUE, null),         // sticky bombs
        PROXY_FUZE(EXTRA_PROXY, null, null),  // 10m EntityLivingBase trigger
        FRAG_SLEEVE(null, null, EXTRA_FRAG),  // 25 extra frags
        TRIPLEX(null, null, EXTRA_TRIPLEX);   // [THE BIG ONE]

        public static final EnumGrenadeExtra[] VALUES = values();

        public final Consumer<EntityGrenadeUniversal> updateTick;
        public final BiConsumer<EntityGrenadeUniversal, RayTraceResult> onImpact;
        public final Consumer<EntityGrenadeUniversal> onExplode;

        EnumGrenadeExtra(Consumer<EntityGrenadeUniversal> updateTick,
                         BiConsumer<EntityGrenadeUniversal, RayTraceResult> onImpact,
                         Consumer<EntityGrenadeUniversal> onExplode) {
            this.updateTick = updateTick;
            this.onImpact = onImpact;
            this.onExplode = onExplode;
        }
    }

    public static final BiConsumer<EntityGrenadeUniversal, RayTraceResult> EXTRA_GLUE = (grenade, mop) -> {
        if (mop.typeOfHit == RayTraceResult.Type.BLOCK) {
            grenade.setPosition(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
            EnumFacing side = mop.sideHit == null ? EnumFacing.UP : mop.sideHit;
            grenade.getStuck(mop.getBlockPos(), side.ordinal());
        }
    };

    public static final Consumer<EntityGrenadeUniversal> EXTRA_PROXY = grenade -> {
        if (grenade.getTimer() >= 10 && grenade.getTimer() % 3 == 0) {
            AxisAlignedBB box = new AxisAlignedBB(
                    grenade.posX, grenade.posY, grenade.posZ,
                    grenade.posX, grenade.posY, grenade.posZ).grow(10, 10, 10);
            List<EntityLivingBase> living = grenade.world.getEntitiesWithinAABB(EntityLivingBase.class, box);
            for (EntityLivingBase e : living) {
                if (e == grenade.getThrower()) continue;
                if (e.getDistance(grenade) <= 10) {
                    grenade.explode();
                    return;
                }
            }
        }
    };

    public static final Consumer<EntityGrenadeUniversal> EXTRA_FRAG = grenade -> {
        ItemGrenadeFilling.standardFragmentation(grenade, 25);
    };

    public static final Consumer<EntityGrenadeUniversal> EXTRA_TRIPLEX = grenade -> {
        ItemStack frag = ItemGrenadeUniversal.make(grenade.getShell(), grenade.getFilling(), EnumGrenadeFuze.S3);

        Vec3NT vec = new Vec3NT(0.25, 0, 0).rotateAroundYDeg(grenade.world.rand.nextDouble() * 360);

        for (int i = 0; i < 3; i++) {
            EntityGrenadeUniversal triplet = new EntityGrenadeUniversal(grenade.world, frag).setTrail(EntityGrenadeUniversal.TRAIL_TRIPLET);
            triplet.setPosition(grenade.posX, grenade.posY, grenade.posZ);
            triplet.setThrower(grenade.getThrower());
            triplet.motionX = vec.x;
            triplet.motionY = 0.75D;
            triplet.motionZ = vec.z;
            grenade.world.spawnEntity(triplet);
            vec.rotateAroundYDeg(120);
        }
    };
}
