package com.hbm.mixin.vanilla.optifine;

import com.hbm.mixin.MixinCompiledChunk;
import com.hbm.render.chunk.ISectionGeometryHolder;
import com.hbm.render.chunk.SectionGeometry;
import com.hbm.util.OptifineHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.optifine.override.ChunkCacheOF;
import org.spongepowered.asm.mixin.Dynamic;
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

    @Dynamic
    @WrapOperation(method = "rebuildChunk", require = 1, at = @At(value = "INVOKE", remap = false,
            target = "Lnet/minecraft/client/renderer/chunk/RenderChunk;isChunkRegionEmpty(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean hbm$admitSectionGeometry(RenderChunk self, BlockPos pos, Operation<Boolean> original,
                                             @Local(argsOnly = true) ChunkCompileTaskGenerator generator) {
        return original.call(self, pos) && ((ISectionGeometryHolder) generator).hbm$sectionGeometry() == null;
    }

    @Dynamic
    @WrapOperation(method = "rebuildChunk", require = 1, at = @At(value = "INVOKE", remap = false,
            target = "Lnet/optifine/override/ChunkCacheOF;renderStart()V"))
    private void hbm$emitSectionGeometry(ChunkCacheOF view, Operation<Void> original,
                                         @Local(argsOnly = true) ChunkCompileTaskGenerator generator,
                                         @Local CompiledChunk compiled) {
        original.call(view);
        SectionGeometry.Snapshot snapshot = ((ISectionGeometryHolder) generator).hbm$sectionGeometry();
        if (snapshot == null) return;
        for (TileEntity tile : snapshot.tiles()) compiled.addTileEntity(tile);
        snapshot.emit(view, new SectionGeometry.BufferSink(snapshot, view, layer -> {
            BufferBuilder buffer = generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(layer);
            if (!compiled.isLayerStarted(layer)) {
                compiled.setLayerStarted(layer);
                OptifineHooks.setBlockLayer(buffer, layer);
                preRenderBlocks(buffer, position);
            }
            ((MixinCompiledChunk) compiled).hbm$setLayerUsed(layer);
            return buffer;
        }));
    }
}
