package org.embeddedt.embeddium.impl.render.chunk.occlusion;

import org.embeddedt.embeddium.impl.render.chunk.AbstractSection;
import org.embeddedt.embeddium.impl.render.chunk.RenderSection;

/** Stub for compilation only — provided at runtime by Celeritas/Embeddium. */
public class OcclusionNode extends AbstractSection {
    public OcclusionNode(RenderSection section) {
        super(section.getChunkX(), section.getChunkY(), section.getChunkZ());
    }

    public RenderSection getRenderSection() {
        throw new AssertionError();
    }
}
