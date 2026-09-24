package org.embeddedt.embeddium.impl.render.chunk;

import org.embeddedt.embeddium.impl.render.chunk.data.BuiltRenderSectionData;

/** Stub for compilation only — provided at runtime by Celeritas/Embeddium. */
public class RenderSection extends AbstractSection {
    public RenderSection(int chunkX, int chunkY, int chunkZ) {
        super(chunkX, chunkY, chunkZ);
    }

    public BuiltRenderSectionData getBuiltContext() {
        throw new AssertionError();
    }
}
