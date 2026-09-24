package com.hbm.mixin.mod.celeritas;

import com.hbm.render.chunk.ISectionGeometryHolder;
import com.hbm.render.chunk.SectionGeometry;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildContext;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildOutput;
import org.embeddedt.embeddium.impl.render.chunk.data.MinecraftBuiltRenderSectionData;
import org.embeddedt.embeddium.impl.util.task.CancellationToken;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.taumc.celeritas.impl.render.terrain.compile.VintageChunkBuildContext;
import org.taumc.celeritas.impl.render.terrain.compile.task.ChunkBuilderMeshingTask;
import org.taumc.celeritas.impl.world.WorldSlice;

import java.util.Collections;

@Mixin(value = ChunkBuilderMeshingTask.class, remap = false)
public abstract class MixinChunkBuilderMeshingTask implements ISectionGeometryHolder {

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
    @Inject(method = "execute", require = 1, at = @At(value = "INVOKE",
            target = "Lorg/taumc/celeritas/impl/render/terrain/compile/VintageChunkBuildContext;convertVanillaDataToCeleritasData(Lorg/embeddedt/embeddium/impl/render/chunk/compile/ChunkBuildBuffers;)V"))
    private void hbm$emitSectionGeometry(ChunkBuildContext context, CancellationToken cancellationToken,
                                         CallbackInfoReturnable<ChunkBuildOutput> cir,
                                         @Local VintageChunkBuildContext buildContext,
                                         @Local MinecraftBuiltRenderSectionData<TextureAtlasSprite, TileEntity> renderData) {
        SectionGeometry.Snapshot snapshot = hbm$sectionGeometry;
        if (snapshot == null) return;
        Collections.addAll(renderData.culledBlockEntities, snapshot.tiles());
        WorldSlice view = buildContext.getWorldSlice();
        snapshot.emit(view, new SectionGeometry.BufferSink(snapshot, view, buildContext::getBufferForLayer));
    }
}
