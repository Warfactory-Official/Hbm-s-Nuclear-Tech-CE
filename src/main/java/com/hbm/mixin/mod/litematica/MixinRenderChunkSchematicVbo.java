package com.hbm.mixin.mod.litematica;

import com.hbm.render.chunk.ISectionGeometryState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "fi.dy.masa.litematica.render.schematic.RenderChunkSchematicVbo", remap = false)
public class MixinRenderChunkSchematicVbo {

    @Inject(method = "renderBlocksAndOverlay", at = @At("HEAD"), remap = false)
    private void hbm$beginSchematicBlock(CallbackInfo ci) {
        ISectionGeometryState.FOREIGN_MESHER.set(true);
    }

    @Inject(method = "renderBlocksAndOverlay", at = @At("RETURN"), remap = false)
    private void hbm$endSchematicBlock(CallbackInfo ci) {
        ISectionGeometryState.FOREIGN_MESHER.set(false);
    }
}
