package meldexun.nothirium.renderer.chunk;

import meldexun.nothirium.api.renderer.chunk.IRenderChunk;
import meldexun.nothirium.util.SectionPos;
import meldexun.nothirium.util.VisibilitySet;
import meldexun.renderlib.util.Frustum;

/** Stub for compilation only — provided at runtime by Nothirium. */
public abstract class AbstractRenderChunk implements IRenderChunk {
    private VisibilitySet visibilitySet = new VisibilitySet();

    @Override
    public SectionPos getPos() { return null; }
    public boolean isFogCulled(double cameraX, double cameraY, double cameraZ, double fogEndSqr) { return false; }
    public boolean isFrustumCulled(Frustum frustum) { return false; }
    public void setVisibility(VisibilitySet visibilitySet) {
        this.visibilitySet = visibilitySet;
    }
}
