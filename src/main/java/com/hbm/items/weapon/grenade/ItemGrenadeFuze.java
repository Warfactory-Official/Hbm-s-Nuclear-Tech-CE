package com.hbm.items.weapon.grenade;

import com.hbm.entity.grenade.EntityGrenadeUniversal;
import com.hbm.items.ItemEnumMulti;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ItemGrenadeFuze extends ItemEnumMulti<ItemGrenadeFuze.EnumGrenadeFuze> {

    public ItemGrenadeFuze(String registryName) {
        super(registryName, EnumGrenadeFuze.VALUES, true, true);
    }

    public enum EnumGrenadeFuze {
        S3(FUZE_3S,             0x000000),   // 3s timed
        S7(FUZE_7S,             0x404040),   // 7s timed
        S15(FUZE_15S,           0x808080),   // 15s timed
        IMPACT(FUZE_IMPACT,     0xE36C17),   // on block/entity impact, 0.5s safety
        AIRBURST(FUZE_AIRBURST, 0x56A137);   // 2s safety, explodes 10 blocks above ground

        public static final EnumGrenadeFuze[] VALUES = values();

        public final Consumer<EntityGrenadeUniversal> updateTick;
        public final BiConsumer<EntityGrenadeUniversal, RayTraceResult> onImpact;
        public final int bandColor;

        EnumGrenadeFuze(Consumer<EntityGrenadeUniversal> updateTick, int color) { this(updateTick, null, color); }
        EnumGrenadeFuze(BiConsumer<EntityGrenadeUniversal, RayTraceResult> onImpact, int color) { this(null, onImpact, color); }
        EnumGrenadeFuze(Consumer<EntityGrenadeUniversal> updateTick, BiConsumer<EntityGrenadeUniversal, RayTraceResult> onImpact, int color) {
            this.updateTick = updateTick;
            this.onImpact = onImpact;
            this.bandColor = color;
        }
    }

    public static final Consumer<EntityGrenadeUniversal> FUZE_3S  = grenade -> { if (grenade.getTimer() >= 60)  grenade.explode(); };
    public static final Consumer<EntityGrenadeUniversal> FUZE_7S  = grenade -> { if (grenade.getTimer() >= 140) grenade.explode(); };
    public static final Consumer<EntityGrenadeUniversal> FUZE_15S = grenade -> { if (grenade.getTimer() >= 300) grenade.explode(); };

    public static final BiConsumer<EntityGrenadeUniversal, RayTraceResult> FUZE_IMPACT = (grenade, mop) -> {
        if (grenade.getTimer() >= 10) {
            grenade.setPosition(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
            grenade.explode();
        }
    };

    public static final Consumer<EntityGrenadeUniversal> FUZE_AIRBURST = grenade -> {
        if (grenade.getTimer() >= 30) {
            Vec3d start = new Vec3d(grenade.posX, grenade.posY, grenade.posZ);
            Vec3d end   = new Vec3d(grenade.posX, grenade.posY - 10, grenade.posZ);
            RayTraceResult mop = grenade.world.rayTraceBlocks(start, end, false, false, true);
            if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) grenade.explode();
        }
    };
}
