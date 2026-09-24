package com.hbm.mixin.mod.nothirium;

import com.hbm.render.chunk.ISectionGeometryHolder;
import com.hbm.render.chunk.SectionGeometry;
import meldexun.nothirium.mc.renderer.chunk.RenderChunk;
import meldexun.nothirium.mc.renderer.chunk.RenderChunkTaskCompile;
import meldexun.nothirium.renderer.chunk.AbstractRenderChunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RenderChunk.class, remap = false)
public abstract class MixinRenderChunk extends AbstractRenderChunk {

    @Unique
    private static final String CREATE_COMPILE_TASK = "createCompileTask(Lmeldexun/nothirium/api/renderer/chunk/IChunkRenderer;Lmeldexun/nothirium/api/renderer/chunk/IRenderChunkDispatcher;)Lmeldexun/nothirium/mc/renderer/chunk/RenderChunkTaskCompile;";

    @Dynamic
    @ModifyVariable(method = CREATE_COMPILE_TASK, at = @At("STORE"), require = 1)
    private ExtendedBlockStorage hbm$admitSectionGeometry(ExtendedBlockStorage storage) {
        return SectionGeometry.admit(storage, getX() >> 4, getY() >> 4, getZ() >> 4);
    }

    @Dynamic
    @Inject(method = CREATE_COMPILE_TASK, at = @At("RETURN"), require = 1)
    private void hbm$captureSectionGeometry(CallbackInfoReturnable<RenderChunkTaskCompile> cir) {
        RenderChunkTaskCompile task = cir.getReturnValue();
        if (task != null) {
            ((ISectionGeometryHolder) task).hbm$sectionGeometry(SectionGeometry.capture(getX() >> 4, getY() >> 4, getZ() >> 4));
        }
    }
}
