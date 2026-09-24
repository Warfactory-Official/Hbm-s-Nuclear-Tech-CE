package com.hbm.mixin;

import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.util.BlockRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CompiledChunk.class)
public interface MixinCompiledChunk {

    @Invoker("setLayerUsed")
    void hbm$setLayerUsed(BlockRenderLayer layer);
}
