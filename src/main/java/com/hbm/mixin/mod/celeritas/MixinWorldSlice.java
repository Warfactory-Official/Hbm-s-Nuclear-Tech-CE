package com.hbm.mixin.mod.celeritas;

import com.hbm.render.chunk.SectionGeometry;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.embeddedt.embeddium.impl.util.position.SectionPos;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.taumc.celeritas.impl.world.WorldSlice;

@Mixin(value = WorldSlice.class, remap = false)
public abstract class MixinWorldSlice {

    @Dynamic
    @ModifyVariable(method = "prepare", at = @At("STORE"), require = 1)
    private static ExtendedBlockStorage hbm$admitSectionGeometry(ExtendedBlockStorage storage,
                                                                 @Local(argsOnly = true) SectionPos origin) {
        return SectionGeometry.admit(storage, origin.minX() >> 4, origin.minY() >> 4, origin.minZ() >> 4);
    }
}
