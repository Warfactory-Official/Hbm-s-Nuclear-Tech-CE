package meldexun.nothirium.mc.renderer.chunk;

import meldexun.nothirium.api.renderer.chunk.RenderChunkTaskResult;
import meldexun.nothirium.renderer.chunk.AbstractRenderChunkTask;
import meldexun.nothirium.util.VisibilityGraph;
import meldexun.nothirium.util.VisibilitySet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/** Stub for compilation only — provided at runtime by Nothirium. */
public class RenderChunkTaskCompile extends AbstractRenderChunkTask<RenderChunk> {
    protected IBlockAccess chunkCache;

    public RenderChunkTaskResult run() {
        return compileSection(new RegionRenderCacheBuilder());
    }

    private RenderChunkTaskResult compileSection(RegionRenderCacheBuilder bufferBuilderPack) {
        VisibilityGraph visibilityGraph = new VisibilityGraph();
        VisibilitySet visibilitySet = visibilityGraph.compute();
        if (visibilitySet == null) {
            return RenderChunkTaskResult.CANCELLED;
        }
        throw new AssertionError();
    }

    public void renderBlockState(IBlockState blockState, BlockPos pos, VisibilityGraph visibilityGraph, RegionRenderCacheBuilder bufferBuilderPack) {
        BufferBuilder bufferBuilder = bufferBuilderPack.getWorldRendererByLayer(null);
        Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(blockState, pos, this.chunkCache, bufferBuilder);
    }
}
