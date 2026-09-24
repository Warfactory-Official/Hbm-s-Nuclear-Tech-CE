package com.hbm.mixin.mod.nothirium;

import com.hbm.core.ModPresence;
import com.hbm.mixin.vanilla.nothirium.MixinBufferBuilderDrawing;
import com.hbm.render.chunk.ISectionGeometryHolder;
import com.hbm.render.chunk.SectionGeometry;
import com.hbm.util.OptifineHooks;
import meldexun.nothirium.api.renderer.chunk.RenderChunkTaskResult;
import meldexun.nothirium.mc.renderer.chunk.RenderChunk;
import meldexun.nothirium.mc.renderer.chunk.RenderChunkTaskCompile;
import meldexun.nothirium.renderer.chunk.AbstractRenderChunkTask;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RenderChunkTaskCompile.class, remap = false)
public abstract class MixinRenderChunkTaskCompile extends AbstractRenderChunkTask<RenderChunk> implements ISectionGeometryHolder {

    @Shadow
    @Final
    private IBlockAccess chunkCache;

    @Unique
    private SectionGeometry.@Nullable Snapshot hbm$sectionGeometry;

    @Override
    public SectionGeometry.@Nullable Snapshot hbm$sectionGeometry() {
        return hbm$sectionGeometry;
    }

    @Override
    public void hbm$sectionGeometry(SectionGeometry.@Nullable Snapshot snapshot) {
        hbm$sectionGeometry = snapshot;
    }

    @Dynamic
    @Inject(method = "compileSection(Lnet/minecraft/client/renderer/RegionRenderCacheBuilder;)Lmeldexun/nothirium/api/renderer/chunk/RenderChunkTaskResult;",
            at = @At(value = "INVOKE", target = "Lmeldexun/nothirium/util/VisibilityGraph;compute()Lmeldexun/nothirium/util/VisibilitySet;"),
            require = 1)
    private void hbm$emitSectionGeometry(RegionRenderCacheBuilder buffers, CallbackInfoReturnable<RenderChunkTaskResult> cir) {
        SectionGeometry.Snapshot snapshot = hbm$sectionGeometry;
        if (snapshot == null) return;
        snapshot.emit(chunkCache, new SectionGeometry.BufferSink(snapshot, chunkCache, layer -> {
            BufferBuilder buffer = buffers.getWorldRendererByLayer(layer);
            if (!((MixinBufferBuilderDrawing) buffer).hbm$isDrawing()) {
                if (ModPresence.OPTIFINE) OptifineHooks.setBlockLayer(buffer, layer);
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                buffer.setTranslation(-renderChunk.getX(), -renderChunk.getY(), -renderChunk.getZ());
            }
            return buffer;
        }));
    }
}
