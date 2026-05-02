package org.embeddedt.embeddium.impl.render.terrain;

import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.embeddedt.embeddium.impl.render.viewport.Viewport;

/** Stub for compilation only — provided at runtime by Celeritas/Embeddium. */
public abstract class SimpleWorldRenderer<WORLD, SECTIONMANAGER extends RenderSectionManager, LAYER, BLOCKENTITY, BLOCKENTITY_RENDER_CONTEXT> {
    protected SECTIONMANAGER renderSectionManager;

    public static class CameraState {
        public double x() {
            throw new AssertionError();
        }

        public double y() {
            throw new AssertionError();
        }

        public double z() {
            throw new AssertionError();
        }
    }

    public void setupTerrain(Viewport viewport, CameraState cameraState, int frame, boolean spectator,
                             boolean updateChunksImmediately) {
        throw new AssertionError();
    }
}
