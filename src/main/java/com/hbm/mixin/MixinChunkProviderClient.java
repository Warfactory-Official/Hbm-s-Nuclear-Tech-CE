package com.hbm.mixin;

import com.hbm.render.chunk.SectionGeometry;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkProviderClient.class)
public abstract class MixinChunkProviderClient {

    @Inject(method = "unloadChunk", at = @At("HEAD"))
    private void hbm$sectionGeometryUnload(int x, int z, CallbackInfo ci) {
        SectionGeometry.chunkUnloaded(x, z);
    }
}
