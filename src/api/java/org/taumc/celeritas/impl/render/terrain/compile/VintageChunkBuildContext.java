package org.taumc.celeritas.impl.render.terrain.compile;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.BlockRenderLayer;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildContext;
import org.taumc.celeritas.impl.world.WorldSlice;

public class VintageChunkBuildContext extends ChunkBuildContext {
    public BufferBuilder getBufferForLayer(BlockRenderLayer layer) {
        throw new AssertionError();
    }

    public WorldSlice getWorldSlice() {
        throw new AssertionError();
    }
}
