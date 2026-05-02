package org.embeddedt.embeddium.impl.render.viewport;

import org.embeddedt.embeddium.impl.render.viewport.frustum.Frustum;
import org.embeddedt.embeddium.impl.shadow.joml.Vector3d;
import org.embeddedt.embeddium.impl.shadow.joml.Vector3i;

/** Stub for compilation only — provided at runtime by Celeritas/Embeddium. */
public final class Viewport {
    private final Frustum frustum = null;
    private final CameraTransform transform = null;
    private final Vector3i chunkCoords = null;
    private final Vector3i blockCoords = null;

    public Viewport(Frustum frustum, Vector3d position) {
        throw new AssertionError();
    }

    public boolean isBoxVisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        throw new AssertionError();
    }

    public CameraTransform getTransform() {
        throw new AssertionError();
    }
}
