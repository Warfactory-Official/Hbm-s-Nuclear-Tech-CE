package com.hbm.mixin.mod.neonium;

import com.hbm.render.chunk.ISectionGeometryHolder;
import com.hbm.render.chunk.SectionGeometry;
import com.llamalad7.mixinextras.sugar.Local;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderBounds;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import me.jellysquid.mods.sodium.client.render.chunk.format.ModelVertexSink;
import me.jellysquid.mods.sodium.client.render.chunk.tasks.ChunkRenderRebuildTask;
import me.jellysquid.mods.sodium.client.render.pipeline.context.ChunkRenderCacheLocal;
import me.jellysquid.mods.sodium.client.util.task.CancellationSource;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChunkRenderRebuildTask.class, remap = false)
public abstract class MixinChunkRenderRebuildTask implements ISectionGeometryHolder {

    @Shadow
    @Final
    private BlockPos offset;

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
    @Inject(method = "performBuild", require = 1, at = @At(value = "INVOKE",
            target = "Lme/jellysquid/mods/sodium/client/render/chunk/ChunkRenderContainer;setRebuildForTranslucents(Z)V"))
    private void hbm$emitSectionGeometry(ChunkRenderCacheLocal cache, ChunkBuildBuffers buffers,
                                         CancellationSource cancellationSource, CallbackInfoReturnable<?> cir,
                                         @Local ChunkRenderData.Builder renderData,
                                         @Local ChunkRenderBounds.Builder bounds) {
        SectionGeometry.Snapshot snapshot = hbm$sectionGeometry;
        if (snapshot == null) return;
        for (TileEntity tile : snapshot.tiles()) renderData.addBlockEntity(tile, true);
        if (!snapshot.hasGeometry()) return;
        buffers.setRenderOffset(snapshot.originX() - offset.getX(), snapshot.originY() - offset.getY(),
                snapshot.originZ() - offset.getZ());
        snapshot.emit(cache.getLocalSlice(), (layer, owner, sprite, quad, colors, lights) -> {
            renderData.addSprite(sprite);
            ModelVertexSink sink = buffers.get(layer).getSink(ModelQuadFacing.UNASSIGNED);
            sink.ensureCapacity(4);
            for (int v = 0; v < 4; v++) {
                int at = v * 5;
                sink.writeQuad(quad[at], quad[at + 1], quad[at + 2], colors[v], quad[at + 3], quad[at + 4], lights[v]);
            }
            sink.flush();
        });
        bounds.addBlock(0, 0, 0);
        bounds.addBlock(15, 15, 15);
    }
}
