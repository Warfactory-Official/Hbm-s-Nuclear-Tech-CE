package com.hbm.mixin.vanilla.base;

import com.hbm.mixin.MixinCompiledChunk;
import com.hbm.render.chunk.ISectionGeometryHolder;
import com.hbm.render.chunk.SectionGeometry;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderChunk.class)
public abstract class MixinRenderChunk {

    @Final
    @Shadow
    private BlockPos.MutableBlockPos position;

    @Shadow
    protected abstract void preRenderBlocks(BufferBuilder bufferBuilderIn, BlockPos pos);

    @WrapOperation(method = "rebuildChunk", require = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/ChunkCache;isEmpty()Z"))
    private boolean hbm$emitSectionGeometry(ChunkCache view, Operation<Boolean> original,
                                            @Local(argsOnly = true) ChunkCompileTaskGenerator generator,
                                            @Local CompiledChunk compiled) {
        boolean empty = original.call(view);
        SectionGeometry.Snapshot snapshot = ((ISectionGeometryHolder) generator).hbm$sectionGeometry();
        if (snapshot == null) return empty;
        for (TileEntity tile : snapshot.tiles()) compiled.addTileEntity(tile);
        snapshot.emit(view, new SectionGeometry.BufferSink(snapshot, view, layer -> {
            BufferBuilder buffer = generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(layer);
            if (!compiled.isLayerStarted(layer)) {
                compiled.setLayerStarted(layer);
                preRenderBlocks(buffer, position);
            }
            ((MixinCompiledChunk) compiled).hbm$setLayerUsed(layer);
            return buffer;
        }));
        return false;
    }
}
