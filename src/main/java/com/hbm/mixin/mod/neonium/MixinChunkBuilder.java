package com.hbm.mixin.mod.neonium;

import com.hbm.render.chunk.ISectionGeometryHolder;
import com.hbm.render.chunk.SectionGeometry;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderContainer;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.tasks.ChunkRenderBuildTask;
import me.jellysquid.mods.sodium.client.render.chunk.tasks.ChunkRenderRebuildTask;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChunkBuilder.class, remap = false)
public abstract class MixinChunkBuilder {

    @Dynamic
    @Inject(method = "createRebuildTask", at = @At("RETURN"), require = 1)
    private void hbm$captureSectionGeometry(ChunkRenderContainer<?> render,
                                            CallbackInfoReturnable<ChunkRenderBuildTask<?>> cir) {
        if (cir.getReturnValue() instanceof ChunkRenderRebuildTask<?> task) {
            ((ISectionGeometryHolder) task).hbm$sectionGeometry(
                    SectionGeometry.capture(render.getChunkX(), render.getChunkY(), render.getChunkZ()));
        }
    }
}
