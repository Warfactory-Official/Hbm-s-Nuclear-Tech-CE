package com.hbm.mixin.mod.celeritas;

import com.hbm.render.chunk.ISectionGeometryHolder;
import com.hbm.render.chunk.SectionGeometry;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.embeddedt.embeddium.impl.render.chunk.RenderSection;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.taumc.celeritas.impl.render.terrain.VintageRenderSectionManager;
import org.taumc.celeritas.impl.render.terrain.compile.task.ChunkBuilderMeshingTask;

@Mixin(value = VintageRenderSectionManager.class, remap = false)
public abstract class MixinVintageRenderSectionManager {

    @Dynamic
    @ModifyReturnValue(method = "isSectionVisuallyEmpty", at = @At("RETURN"), require = 1)
    private boolean hbm$admitSectionGeometry(boolean empty, @Local(argsOnly = true, ordinal = 0) int x,
                                             @Local(argsOnly = true, ordinal = 1) int y,
                                             @Local(argsOnly = true, ordinal = 2) int z) {
        return empty && !SectionGeometry.admits(x, y, z);
    }

    @Dynamic
    @Inject(method = "createRebuildTask", at = @At("RETURN"), require = 1)
    private void hbm$captureSectionGeometry(RenderSection render, int frame, CallbackInfoReturnable<?> cir) {
        if (cir.getReturnValue() instanceof ChunkBuilderMeshingTask task) {
            ((ISectionGeometryHolder) task).hbm$sectionGeometry(
                    SectionGeometry.capture(render.getChunkX(), render.getChunkY(), render.getChunkZ()));
        }
    }
}
