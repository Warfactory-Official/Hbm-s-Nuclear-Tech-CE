package me.jellysquid.mods.sodium.client.render.chunk.cull.graph;

import me.jellysquid.mods.sodium.client.util.math.FrustumExtended;
import net.minecraft.client.renderer.chunk.SetVisibility;

/** Stub for compilation only — provided at runtime by Neonium. */
public class ChunkGraphNode {
    private long visibilityData;

    public int getOriginX() { return 0; }
    public int getOriginY() { return 0; }
    public int getOriginZ() { return 0; }
    public boolean isCulledByFrustum(FrustumExtended frustum) { return false; }
    public void setOcclusionData(SetVisibility occlusionData) {
        this.visibilityData = calculateVisibilityData(occlusionData);
    }

    private static long calculateVisibilityData(SetVisibility occlusionData) {
        return 0L;
    }
}
