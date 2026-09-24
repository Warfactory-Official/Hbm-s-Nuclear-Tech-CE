package com.hbm.mixin.vanilla.neonium;

import com.hbm.render.chunk.SectionGeometry;
import com.llamalad7.mixinextras.sugar.Local;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderManager;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ChunkRenderManager.class, remap = false)
public abstract class MixinChunkRenderManager {

    @Dynamic
    @ModifyVariable(method = "createChunkRender", at = @At("STORE"), require = 1)
    private ExtendedBlockStorage hbm$admitSectionGeometry(ExtendedBlockStorage storage,
                                                          @Local(argsOnly = true, ordinal = 0) int x,
                                                          @Local(argsOnly = true, ordinal = 1) int y,
                                                          @Local(argsOnly = true, ordinal = 2) int z) {
        return SectionGeometry.admit(storage, x, y, z);
    }
}
