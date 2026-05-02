package org.embeddedt.embeddium.impl.render.viewport.frustum;

/** Stub for compilation only — provided at runtime by Celeritas/Embeddium. */
public interface Frustum {
    boolean testAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ);
}
