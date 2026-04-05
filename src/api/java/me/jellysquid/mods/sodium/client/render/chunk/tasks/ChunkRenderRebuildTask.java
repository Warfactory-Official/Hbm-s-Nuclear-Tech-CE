package me.jellysquid.mods.sodium.client.render.chunk.tasks;

import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildResult;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderBounds;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import me.jellysquid.mods.sodium.client.render.pipeline.context.ChunkRenderCacheLocal;
import me.jellysquid.mods.sodium.client.util.math.ChunkSectionPos;
import me.jellysquid.mods.sodium.client.util.task.CancellationSource;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.util.EnumBlockRenderType;

/** Stub for compilation only — provided at runtime by Neonium. */
public abstract class ChunkRenderRebuildTask<T> {
    public ChunkBuildResult<T> performBuild(ChunkRenderCacheLocal cache, ChunkBuildBuffers buffers, CancellationSource cancellationSource) {
        ChunkRenderData.Builder renderData = new ChunkRenderData.Builder();
        VisGraph occluder = new VisGraph();
        ChunkRenderBounds.Builder bounds = new ChunkRenderBounds.Builder();
        IBlockState state = null;

        if (cancellationSource.isCancelled()) {
            return null;
        }

        EnumBlockRenderType renderType = state.getRenderType();
        if (renderType != EnumBlockRenderType.INVISIBLE) {
            bounds.addBlock(0, 0, 0);
        }

        SetVisibility visibility = occluder.computeVisibility();
        if (visibility == null) {
            return null;
        }

        renderData.setOcclusionData(visibility);
        renderData.setBounds(bounds.build(null));

        throw new AssertionError();
    }
}
