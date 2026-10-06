package com.hbm.mixin.mod.schematica;

import com.hbm.render.chunk.ISectionGeometryState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = {
        "com.github.lunatrius.schematica.client.renderer.chunk.proxy.SchematicRenderChunkVbo",
        "com.github.lunatrius.schematica.client.renderer.chunk.proxy.SchematicRenderChunkList"
}, remap = false)
public class MixinSchematicRenderChunk {

    @Inject(method = {"func_178581_b", "rebuildChunk"}, at = @At("HEAD"), remap = false)
    private void hbm$beginSchematicRebuild(CallbackInfo ci) {
        ISectionGeometryState.FOREIGN_MESHER.set(true);
    }

    @Inject(method = {"func_178581_b", "rebuildChunk"}, at = @At("RETURN"), remap = false)
    private void hbm$endSchematicRebuild(CallbackInfo ci) {
        ISectionGeometryState.FOREIGN_MESHER.set(false);
    }
}
