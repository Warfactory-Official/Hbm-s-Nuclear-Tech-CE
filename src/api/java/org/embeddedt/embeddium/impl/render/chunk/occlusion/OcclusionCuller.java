package org.embeddedt.embeddium.impl.render.chunk.occlusion;

import org.embeddedt.embeddium.impl.render.viewport.Viewport;

/** Stub for compilation only — provided at runtime by Celeritas/Embeddium. */
public abstract class OcclusionCuller {
    public static boolean isWithinFrustum(Viewport viewport, OcclusionNode section) {
        throw new AssertionError();
    }
}
