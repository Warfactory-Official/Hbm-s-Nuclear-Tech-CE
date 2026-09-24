package me.jellysquid.mods.sodium.client.render.chunk.compile;

import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuffers;
import net.minecraft.util.BlockRenderLayer;

/** Stub for compilation only — provided at runtime by Neonium. */
public class ChunkBuildBuffers {
    public ChunkModelBuffers get(BlockRenderLayer layer) {
        throw new AssertionError();
    }

    public void setRenderOffset(int x, int y, int z) {
        throw new AssertionError();
    }
}
