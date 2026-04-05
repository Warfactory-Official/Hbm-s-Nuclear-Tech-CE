package org.embeddedt.embeddium.impl.render.chunk;

import org.embeddedt.embeddium.impl.render.chunk.data.BuiltRenderSectionData;

/** Stub for compilation only — provided at runtime by Celeritas/Embeddium. */
public abstract class RenderSectionManager {
    public boolean isSectionVisible(int x, int y, int z) {
        throw new AssertionError();
    }

    protected boolean updateSectionInfo(RenderSection render, BuiltRenderSectionData info) {
        throw new AssertionError();
    }

    public void destroy() {
        throw new AssertionError();
    }
}
