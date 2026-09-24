package org.taumc.celeritas.impl.render.terrain.compile.task;

import org.embeddedt.embeddium.impl.render.chunk.RenderSection;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildContext;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildOutput;
import org.embeddedt.embeddium.impl.util.task.CancellationToken;

/** Stub for compilation only — provided at runtime by Celeritas. */
public class ChunkBuilderMeshingTask {
    private final RenderSection render = null;

    public ChunkBuildOutput execute(ChunkBuildContext context, CancellationToken cancellationToken) {
        throw new AssertionError();
    }
}
