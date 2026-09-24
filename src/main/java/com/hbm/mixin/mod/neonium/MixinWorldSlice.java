package com.hbm.mixin.mod.neonium;

import com.hbm.render.chunk.SectionGeometry;
import com.llamalad7.mixinextras.sugar.Local;
import me.jellysquid.mods.sodium.client.util.math.ChunkSectionPos;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = WorldSlice.class, remap = false)
public abstract class MixinWorldSlice {

    @Dynamic
    @ModifyVariable(method = "prepare", at = @At("STORE"), require = 1)
    private static ExtendedBlockStorage hbm$admitSectionGeometry(ExtendedBlockStorage storage,
                                                                 @Local(argsOnly = true) ChunkSectionPos origin) {
        return SectionGeometry.admit(storage, origin.getMinX() >> 4, origin.getMinY() >> 4, origin.getMinZ() >> 4);
    }
}
